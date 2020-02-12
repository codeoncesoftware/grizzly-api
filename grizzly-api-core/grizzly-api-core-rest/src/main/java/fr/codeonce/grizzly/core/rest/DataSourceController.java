/*-
 * ========================LICENSE_START=================================
 * grizzly-api-core-rest
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
package fr.codeonce.grizzly.core.rest;

import java.util.List;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.codeonce.grizzly.core.domain.datasource.DBSource;
import fr.codeonce.grizzly.core.service.datasource.DBSourceDto;
import fr.codeonce.grizzly.core.service.datasource.DBSourceService;
import fr.codeonce.grizzly.core.service.datasource.DBSourceStatsService;

@RestController
@CrossOrigin(origins = { "*" })
@RequestMapping("/api/dbsource")
public class DataSourceController {

	private static final Logger log = LoggerFactory.getLogger(DataSourceController.class);

	@Autowired
	private DBSourceService dbSourceService;
	
	@Autowired
	private DBSourceStatsService dbSourceStatsService;

	@PostMapping("/create")
	public DBSourceDto saveDatasource(@RequestBody DBSourceDto dbSourceDto) {
		log.info("request to create datasource with name : {}", dbSourceDto.getName());
		return dbSourceService.saveDBSource(dbSourceDto);
	}

	@PostMapping("/check")
	public boolean checkConnection(@RequestBody DBSourceDto dbSourceDto) {
		log.info("request to check validity of the datasource named : {}", dbSourceDto.getName());
		return dbSourceService.checkTempConnection(dbSourceDto);
	}
	
	@GetMapping("/check/name/{dbsourceName}/{dbsourceId}")
	public boolean checkUnicity(@PathVariable String dbsourceName, @PathVariable String dbsourceId) {
		return dbSourceService.checkUnicity(dbsourceName, dbsourceId);
	}

	@GetMapping("/all")
	public List<DBSourceDto> getAll() {
		log.info("request to get all datasources");
		return dbSourceService.getAll();
	}

	@DeleteMapping("/delete/{dbsourceId}")
	public void deleteDBSource(@PathVariable String dbsourceId) {
		log.info("request to delete datasource with ID : {}", dbsourceId);
		this.dbSourceService.deleteByID(dbsourceId);
	}

	@GetMapping("/{dbsourceId}")
	public DBSourceDto getDbSourceById(@PathVariable String dbsourceId) {
		log.info("request to get datasource DTO with ID : {}", dbsourceId);
		return this.dbSourceService.getDbSourceDtoById(dbsourceId);
	}

	@GetMapping("/stats/{dbsourceId}/{databaseName}/{collectionName}")
	public Document getCollectionStats(@PathVariable("dbsourceId") String dbsourceId,
			@PathVariable("databaseName") String databaseName, @PathVariable("collectionName") String collectionName) {
		log.info(
				"request to get collection stats for datasource with ID : {}, databaseName : {} and collectionName: {}",
				dbsourceId, databaseName, collectionName);
		return this.dbSourceStatsService.getCollectionStats(dbsourceId, databaseName, collectionName);
	}

	@DeleteMapping("/drop/{dbsourceId}/{databaseName}/{collectionName}")
	public void dropCollection(@PathVariable("dbsourceId") String dbsourceId,
			@PathVariable("databaseName") String databaseName, @PathVariable("collectionName") String collectionName) {
		log.info("request to drop collection named : {} for datasource with ID : {}", collectionName, dbsourceId);
		this.dbSourceService.dropCollection(dbsourceId, databaseName, collectionName);
	}

	@PostMapping("/addcollection/{containerId}/{collectionName}")
	public boolean addNewCollection(@PathVariable String containerId, @PathVariable String collectionName) {
		log.info("request to create new collection named : {} for container with ID : {}", collectionName, containerId);
		return this.dbSourceService.addNewCollection(containerId, collectionName);
	}

	@GetMapping("/public")
	public DBSource getDBSource(@RequestParam String dbsourceId) {
		log.info("request to get DBSource with ID : {}", dbsourceId);
		
		// DEBUG PERFORMANCE
		StopWatch stopWatch = new StopWatch();
		stopWatch.start("request to get DBSource");

		DBSource dbSource = this.dbSourceService.getDbSourceById(dbsourceId);

		// SHOW DEBUG
		stopWatch.stop();
		log.debug(stopWatch.prettyPrint());

		return dbSource;
	}

}
