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
package fr.codeonce.grizzly.core.service.datasource.cache;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.MongoSocketException;
import com.mongodb.ServerAddress;

import fr.codeonce.grizzly.core.domain.datasource.DBSource;
import fr.codeonce.grizzly.core.domain.datasource.DBSourceRepository;
import fr.codeonce.grizzly.core.service.datasource.DBSourceDto;
import fr.codeonce.grizzly.core.service.datasource.mapper.DBSourceMapperService;
import fr.codeonce.grizzly.core.service.util.CryptoHelper;

@Service
public class ConnectionCacheService {

	@Autowired
	private DBSourceMapperService dbsourceMapper;

	@Autowired
	private DBSourceRepository dbSourceRepository;
	
	@Autowired
	private CryptoHelper encryption;
	
	@Autowired
	@Qualifier("atlasMongoClient")
	private MongoClient atlasMongoClient;

	/**
	 * Preparing a New MongoClient Instance with the Given Details
	 * 
	 * @deprecated - Please use {@link getMongoClient(DBSource dbsource) }
	 * @param dbSourceDto
	 * @return a MongoClient Instance
	 */
	@Deprecated
	@Cacheable(value = "mongoClients", key = "#dbsourceDto.id")
	public MongoClient getMongoClient(DBSourceDto dbsourceDto) {
		DBSource dbsource = this.dbsourceMapper.mapToDomain(dbsourceDto);
		return this.prepareMongoClient(dbsource);
	}

	@Cacheable(value = "mongoClients", key = "#dbsourceId")
	public MongoClient getMongoClient(String dbsourceId) {
		return this.dbSourceRepository.findById(dbsourceId).map(d -> {
			if (d.getConnectionMode().equalsIgnoreCase("FREE")) {
				return atlasMongoClient;
			} else {
				encryption.decrypt(d);
				return getMongoClient(d);
			}
		}).orElseThrow();
	}

	public MongoClient getTemporaryMClient(DBSourceDto dbsourceDto) {
		DBSource dbsource = this.dbsourceMapper.mapToDomain(dbsourceDto);
		encryption.decrypt(dbsource);
		return this.prepareMongoClient(dbsource);
	}

	public MongoClient getAtlasMongoClient() {
		return atlasMongoClient;
	}

	@CachePut(value = "mongoClients", key = "#dbsource.id")
	public MongoClient getUpdatedMongoClient(DBSource dbsource) {
		return this.prepareMongoClient(dbsource);
	}

	@Cacheable(value = "mongoClients", key = "#dbsource.id")
	public MongoClient getMongoClient(DBSource dbsource) {
		return this.prepareMongoClient(dbsource);

	}

	@Cacheable(value = "gridFsObjects")
	public GridFsTemplate getGridFs(MongoClient mongoClient, String databaseName) {
		MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, databaseName);
		return new GridFsTemplate(mongoTemplate.getMongoDbFactory(), mongoTemplate.getConverter());
	}

	@CacheEvict(value = "gridFsObjects")
	public void evictGridFs(MongoClient mongoClient, String databaseName) {
		// Empty GridFsTemplate Instance From Cache
	}

	@CacheEvict(value = "mongoClients", key = "#dbsourceId")
	public void evictMongoClient(String dbsourceId) {
		// Empty MongoClient Instance From Cache
	}

	private MongoClient prepareMongoClient(DBSource dbsource) {
		// IF AN URI IS PRESENT
		if (dbsource.getUri() != null && !dbsource.getUri().isBlank()) {
			return new MongoClient(new MongoClientURI(dbsource.getUri(), new MongoClientOptions.Builder()//
					.maxConnectionIdleTime(60000).serverSelectionTimeout(5000)));
		} else {
			// SERVER INFO
			try {
				ServerAddress serverAddress;
				if (dbsource.getPort() != null) {
					serverAddress = new ServerAddress(dbsource.getHost(), dbsource.getPort());
				} else {
					serverAddress = new ServerAddress(dbsource.getHost());
				}
				// REMOTE SERVER
				if (StringUtils.isNotBlank(dbsource.getUsername())) {
					// CREDENTIAL
					MongoCredential credential = MongoCredential.createCredential(dbsource.getUsername(),
							dbsource.getAuthenticationDatabase(), dbsource.getPassword());

					// CREATE MONGO CLIENT
					return new MongoClient(serverAddress, credential, new MongoClientOptions.Builder()//
							.maxConnectionIdleTime(60000).serverSelectionTimeout(5000)
							.build());
				}
				// LOCAL HOST
				return new MongoClient(serverAddress, new MongoClientOptions.Builder()//
						.maxConnectionIdleTime(60000).serverSelectionTimeout(5000)
						.build());
			} catch (MongoSocketException e) {
				return null;
			}
		}
	}
}
