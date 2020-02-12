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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

import fr.codeonce.grizzly.core.domain.datasource.DBSource;
import fr.codeonce.grizzly.core.domain.datasource.DBSourceRepository;
import fr.codeonce.grizzly.core.service.datasource.cache.ConnectionCacheService;

@Service
public class DBSourceStatsService {
	
	
	private static final Logger log = LoggerFactory.getLogger(DBSourceStatsService.class);


	@Autowired
	private DBSourceRepository dbSourceRepository;

	@Autowired
	private ConnectionCacheService connectionCacheService;

	public Document getCollectionStats(DBSource dbSource, String databaseName, String collectionName) {
		Map<String, Object> options = new LinkedHashMap<>();
		options.put("collStats", collectionName);
		options.put("scale", 1024);
		return getStats(dbSource, databaseName, new Document(options));
	}

	public Document getCollectionStats(String dbsourceId, String databaseName, String collectionName) {
		return dbSourceRepository.findById(dbsourceId).map(d -> getCollectionStats(d, databaseName, collectionName))//
				.orElseThrow();
	}

	public Document getDbStats(String dbsourceId, String databaseName) {
		return dbSourceRepository.findById(dbsourceId).map(d -> getDbStats(d, databaseName))//
				.orElseThrow();
	}
	
	public Document getDbStats(DBSource dbsource, String databaseName) {
		Map<String, Object> options = new HashMap<>();
		options.put("dbStats", 1);
		options.put("scale", 1024);
		return getStats(dbsource, databaseName, new Document(options));
	}

	private Document getStats(DBSource dbsource, String databaseName, Document command) {
		MongoClient mClient = connectionCacheService.getMongoClient(dbsource.getId());
		if (dbsource.getConnectionMode().equalsIgnoreCase("FREE")) {
			dbsource.setDatabase(dbsource.getPhysicalDatabase());
		} else {
			dbsource.setDatabase(databaseName);
		}
		MongoDatabase database = mClient.getDatabase(dbsource.getDatabase());
		try {
			Document runCommand = database.runCommand(command);
			return runCommand;
		} catch (Exception e) {
			log.warn("could not get stats", e);
			return new Document();
		}
	}

}
