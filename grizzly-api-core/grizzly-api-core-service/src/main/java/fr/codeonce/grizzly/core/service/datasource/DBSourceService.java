/*-
 * ========================LICENSE_START=================================
 * grizzly-api-core-service
 * %%
 * Copyright (C) 2019 - 2020 CODE ONCE SOFTWARE
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package fr.codeonce.grizzly.core.service.datasource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Spliterator;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;

import fr.codeonce.grizzly.core.domain.container.ContainerRepository;
import fr.codeonce.grizzly.core.domain.datasource.DBSource;
import fr.codeonce.grizzly.core.domain.datasource.DBSourceRepository;
import fr.codeonce.grizzly.core.domain.project.Project;
import fr.codeonce.grizzly.core.domain.project.ProjectRepository;
import fr.codeonce.grizzly.core.service.datasource.cache.ConnectionCacheService;
import fr.codeonce.grizzly.core.service.datasource.mapper.DBSourceMapperService;
import fr.codeonce.grizzly.core.service.datasource.model.CustomDatabase;
import fr.codeonce.grizzly.core.service.util.CryptoHelper;
import fr.codeonce.grizzly.core.service.util.GlobalExceptionUtil;

@Service
public class DBSourceService {

	private static final String AUTHENTICATION_USER = "authentication_user";

	@Autowired
	private DBSourceRepository dbSourceRepository;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private ContainerRepository containerRepository;

	@Autowired
	private MongoProperties mongoProperties;

	@Autowired
	private MongoClient mongoClient;

	@Autowired
	private DBSourceMapperService mapper;

	@Autowired
	private ConnectionCacheService connectionCacheService;

	@Autowired
	private CryptoHelper encryption;

	@Autowired
	@Qualifier("atlasMongoClient")
	private MongoClient atlasMongoClient;

	private static final Logger log = LoggerFactory.getLogger(DBSourceService.class);

	/**
	 * Save a DBSource Object in DataBase
	 * 
	 * @param dbSource
	 * @return a mapped Instance of DBSource
	 */
	public DBSourceDto saveDBSource(DBSourceDto dbSourceDto) {

		MongoClient mClient;
		StopWatch watch = new StopWatch();
		watch.start("saving dbsource");

		boolean isFree = dbSourceDto.getConnectionMode().equalsIgnoreCase("FREE");
		dbSourceDto.setUserName(getCurrentUsername());

		if (dbSourceDto.getConnectionMode().equalsIgnoreCase("CLOUD")) {
			String dbName = dbSourceDto.getUri().substring(dbSourceDto.getUri().lastIndexOf('/'));
			dbName = dbName.substring(1, dbName.indexOf('?'));
			dbSourceDto.setDatabase(dbName);
		}

		DBSource newDbSource = mapper.mapToDomain(dbSourceDto);

		if (newDbSource.getId() != null) {
			encryption.decrypt(newDbSource);
		}

		// First Registration
		if (newDbSource.getId() == null && isFree) {
			newDbSource.setPhysicalDatabase(getCurrentUsername().substring(0, 5) + '_'
					+ newDbSource.getDatabase().replaceAll("-", "").replaceAll("_", "") + '_'
					+ UUID.randomUUID().toString().replaceAll("-", "").substring(0, 15));
		}

		String databaseName;
		if (newDbSource.getId() != null && isFree) {
			databaseName = this.dbSourceRepository.findById(newDbSource.getId()).map(DBSource::getPhysicalDatabase)
					.orElseThrow();
			newDbSource.setPhysicalDatabase(databaseName);
		} else {
			databaseName = newDbSource.getDatabase();
		}

		if (dbSourceDto.getConnectionMode().equalsIgnoreCase("ON-PREMISE")
				&& (dbSourceDto.getHost() == null || dbSourceDto.getPort() == null)) {
			throw new IllegalArgumentException("4014");
		}
		if (dbSourceDto.getConnectionMode().equalsIgnoreCase("CLOUD") && dbSourceDto.getUri() == null) {
			throw new IllegalArgumentException("4015");
		}

		encryption.encrypt(newDbSource);
		newDbSource = this.dbSourceRepository.save(newDbSource);
		encryption.decrypt(newDbSource);

		if (isFree) {
			databaseName = newDbSource.getPhysicalDatabase();
			mClient = this.atlasMongoClient;
		} else {
			mClient = getOnPremiseMClient(newDbSource);
		}
		// Create first collections
		if (isFree && getDBCollectionsList(mClient, databaseName).isEmpty()) {
			createFirstCollection(mClient, databaseName, AUTHENTICATION_USER);
		}

		watch.stop();
		log.info(watch.prettyPrint());

		return mapper.mapToDto(newDbSource);
	}

	private MongoClient getOnPremiseMClient(DBSource newDbSource) {
		MongoClient mClient;
		if (newDbSource.getId() != null) {
			mClient = this.connectionCacheService.getMongoClient(newDbSource);
		} else {
			mClient = this.connectionCacheService.getUpdatedMongoClient(newDbSource);
		}
		return mClient;
	}

	@Async
	private void setConnectivitystatus(DBSourceDto dbSourceDto, MongoClient mClient) {
		boolean status = checkConnection(mClient);
		dbSourceDto.setActive(status);
	}

	public DBSource getDBSource(String dbsourceName) {

		List<DBSource> dbSourcesList = dbSourceRepository.findByUserNameAndName(getCurrentUsername(), dbsourceName);
		if (dbSourcesList.size() != 1) {
			return null;
		} else {
			DBSource db = dbSourcesList.get(0);
			encryption.decrypt(db);
			return db;
		}
	}

	/**
	 * Check Whether the given Data source DTO Details are Correct or Not
	 * 
	 * @param dbSourceDto
	 * @return true on Success, false on Failure
	 */
	public boolean checkConnection(DBSourceDto dbSourceDto) {
		MongoClient mClient;
		if (dbSourceDto.getConnectionMode().equalsIgnoreCase("FREE")) {
			mClient = this.connectionCacheService.getAtlasMongoClient();
		} else {
			mClient = connectionCacheService.getMongoClient(dbSourceDto);
		}
		return checkConnection(mClient);
	}

	public boolean checkTempConnection(DBSourceDto dbSourceDto) {
		try {
			MongoClient mClient = connectionCacheService.getTemporaryMClient(dbSourceDto);
			boolean res = checkConnection(mClient);
			mClient.close();
			return res;
		} catch (Exception e) {
			throw new IllegalArgumentException("4061");
		}
	}

	/**
	 * Check Whether the given Data source Details are Correct or Not
	 * 
	 * @param dbSourceDto
	 * @return true on Success, false on Failure
	 */
	public boolean checkConnection(MongoClient mClient) {
		try {
			MongoIterable<String> databasesList = mongoClient.listDatabaseNames();
			MongoDatabase database = mClient.getDatabase(databasesList.first());

			Document res = database.runCommand(new Document("ping", "1"));
			if (res.get("ok").equals(Double.valueOf("1.0")) || res.get("ok").equals(Integer.valueOf("1"))) {
				return true;
			}
		} catch (Exception e) {
			throw new IllegalArgumentException("4061");
		}
		return false;
	}

	/**
	 * Fetch All the Data sources from the Database
	 * 
	 * @return A List of mapped Data sources
	 */
	public List<DBSourceDto> getAll() {
		return dbSourceRepository.findAllByUserName(getCurrentUsername()).parallelStream().map(db -> {
			try {
				encryption.decrypt(db);
			} catch (Exception e) {
				log.debug("Error while Decrypting : {}", e.getMessage());
			}
			return mapper.mapToDto(db);
		}).collect(Collectors.toList());
	}

	/**
	 * Delete a specific DataSource from the Database based on the Given ID
	 * 
	 * @param dbsourceId : Id of the Data source to Delete
	 */
	@Async
	public void deleteByID(String dbsourceId) {
		StopWatch watch = new StopWatch();
		watch.start("Deleting DataSource with id : " + dbsourceId);

		try {
			MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, mongoProperties.getDatabase());
			BasicQuery idQuery = new BasicQuery("{_id:\"" + dbsourceId + "\"}");
			// Delete Data Source From DB
			DBSource db = mongoTemplate.findAndRemove(idQuery, DBSource.class);
			if (db.getConnectionMode().equalsIgnoreCase("FREE")) {
				// If the DB is FREE, Drop It On Delete
				dropDB(db);
			}
			// Update the Related Projects
			Update update = new Update();
			update.unset("dbsourceId");
			update.unset("databaseName");
			BasicQuery query = new BasicQuery("{dbsourceId:\"" + dbsourceId + "\"}");
			this.projectRepository.findByDbsourceId(dbsourceId).parallelStream().forEach(project -> {
				mongoTemplate.findAndModify(query, update, Project.class);
			});
		} catch (Exception e) {
			log.debug("Database with id : {}  can't be deleted or does not exist", dbsourceId);
		} finally {
			watch.stop();
			log.info(watch.prettyPrint());
		}

	}

	/**
	 * Drop the Database if it is FREE
	 * 
	 * @param db
	 */
	private void dropDB(DBSource db) {
		connectionCacheService.getAtlasMongoClient().dropDatabase(db.getPhysicalDatabase());
	}

	/**
	 * Fetch a DBSource from DB based on the given Id and Return his Mapped DTO
	 * 
	 * @param dbsourceId
	 * @return DBSourceDto
	 */
	public DBSourceDto getDbSourceDtoById(String dbsourceId) {
		return this.dbSourceRepository.findById(dbsourceId).map(db -> {
			encryption.decrypt(db);
			return mapper.mapToDto(db);
		}).orElseThrow(GlobalExceptionUtil.notFoundException(DBSource.class, dbsourceId));
	}

	/**
	 * Fetch a DBSource from DB based on the given Id
	 * 
	 * @param dbsourceId
	 * @return DBSourceDto
	 */
	@Cacheable(value = "dbsources", key = "#dbsourceId")
	public DBSource getDbSourceById(String dbsourceId) {
		return this.dbSourceRepository.findById(dbsourceId)
				.orElseThrow(GlobalExceptionUtil.notFoundException(DBSource.class, dbsourceId));
	}

	/**
	 * @Deprecated Fetch the List of all Collections within a given MongoDB
	 * 
	 * @param dto
	 * @return
	 */
	public List<String> getDBCollectionsList(MongoClient mongoClient, String databaseName) {
		MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, databaseName);
		return mongoTemplate.getCollectionNames().stream()
				.filter(coll -> (!coll.equalsIgnoreCase("fs.chunks") && !coll.equalsIgnoreCase("fs.files")))
				.collect(Collectors.toList());
	}

	public List<CustomDatabase> getDBdatabasesList(DBSourceDto dto) {
		List<CustomDatabase> databasesList = new ArrayList<>();

		MongoClient mClient;
		if (dto.getConnectionMode().equalsIgnoreCase("FREE")) {
			mClient = this.connectionCacheService.getAtlasMongoClient();
			
		} else {
			mClient = connectionCacheService.getMongoClient(dto);
		}

		if (dto.getConnectionMode().equalsIgnoreCase("FREE")) {

			this.dbSourceRepository.findById(dto.getId())
					.map(dbsource -> databasesList.add(new CustomDatabase(dbsource.getDatabase(),
							getDBCollectionsList(mClient, dbsource.getPhysicalDatabase()))))
					.orElseThrow(GlobalExceptionUtil.notFoundException(DBSource.class, dto.getId()));
			return databasesList;
		}

		Spliterator<String> spitr = mClient.listDatabaseNames().spliterator();
		spitr.forEachRemaining(db -> {
			// Filter MONGO System Databases
			if (!db.equalsIgnoreCase("config") && !db.equalsIgnoreCase("local") && !db.equalsIgnoreCase("admin")) {

				CustomDatabase customDatabase = new CustomDatabase(db, getDBCollectionsList(mClient, db));
				databasesList.add(customDatabase);
			}
		});

		return databasesList;
	}

	public void dropCollection(String dbsourceId, String databaseName, String collectionName) {
		this.dbSourceRepository.findById(dbsourceId).ifPresent(dbsource -> {
			encryption.decrypt(dbsource);
			MongoClient mClient = connectionCacheService.getMongoClient(dbsourceId);
			if (dbsource.getConnectionMode().equalsIgnoreCase("FREE")) {
				dbsource.setDatabase(dbsource.getPhysicalDatabase());
			} else {
				dbsource.setDatabase(databaseName);
			}
			MongoDatabase database = mClient.getDatabase(dbsource.getDatabase());
			database.getCollection(collectionName).drop();
		});
	}

	public void createFirstCollection(MongoClient mClient, String databaseName, String collectionName) {
		addFirstCollections(mClient, databaseName, collectionName);
	}

	private boolean addFirstCollections(MongoClient mClient, String databaseName, String collectionName) {

		try {
			MongoDatabase database = mClient.getDatabase(databaseName);
			BasicDBObject options = new BasicDBObject();
			options.put("size", 12121212);
			database.createCollection(collectionName);
			if (collectionName.equals(AUTHENTICATION_USER)) {
				Document document = new Document();
				document.append("firstname", "Administrator");
				document.append("lastname", "");
				document.append("username", "admin");
				document.append("password", "admin");
				document.append("roles", Arrays.asList("admin"));
				document.append("enabled", true);
				database.getCollection(AUTHENTICATION_USER).insertOne(document);
			} else if (collectionName.equals("demo")) {
				Document document1 = new Document();
				document1.append("firstname", "John");
				document1.append("lastname", "Doe");
				Document document2 = new Document();
				document2.append("firstname", "Code");
				document2.append("lastname", "Once");
				database.getCollection("demo").insertOne(document1);
				database.getCollection("demo").insertOne(document2);

			}

			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean addNewCollection(String containerId, String collectionName) {
		try {
			this.containerRepository.findById(containerId).ifPresent(cont -> {
				DBSource dbsource = getDbSourceById(cont.getDbsourceId());
				encryption.decrypt(dbsource);
				MongoClient mClient = connectionCacheService.getMongoClient(dbsource.getId());
				String databaseName;
				if (dbsource.getConnectionMode().equalsIgnoreCase("FREE")) {
					databaseName = dbsource.getPhysicalDatabase();
				} else {
					databaseName = dbsource.getDatabase();
				}
				MongoDatabase database = mClient.getDatabase(databaseName);
				BasicDBObject options = new BasicDBObject();
				options.put("size", 12121212);
				database.createCollection(collectionName);

			});
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean checkUnicity(String name, String dbsourceId) {
		boolean[] exists = new boolean[] { true };
		this.dbSourceRepository.findByNameIgnoreCaseAndUserName(name, getCurrentUsername()).ifPresent(db -> {
			if (!(!StringUtils.isBlank(dbsourceId) && dbsourceId.equalsIgnoreCase(db.getId()))) {
				exists[0] = false;
			}
		});
		return exists[0];
	}

	private String getCurrentUsername() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication.getName();
	}

}
