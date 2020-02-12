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
package fr.codeonce.grizzly.core.service.project;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

import fr.codeonce.grizzly.core.domain.container.Container;
import fr.codeonce.grizzly.core.domain.container.ContainerRepository;
import fr.codeonce.grizzly.core.domain.datasource.DBSource;
import fr.codeonce.grizzly.core.domain.datasource.DBSourceRepository;
import fr.codeonce.grizzly.core.domain.enums.Const;
import fr.codeonce.grizzly.core.domain.project.Project;
import fr.codeonce.grizzly.core.domain.project.ProjectRepository;
import fr.codeonce.grizzly.core.domain.resource.ResourceGroup;
import fr.codeonce.grizzly.core.service.analytics.AnalyticsService;
import fr.codeonce.grizzly.core.service.container.ContainerDto;
import fr.codeonce.grizzly.core.service.container.ContainerMapperService;
import fr.codeonce.grizzly.core.service.container.ContainerService;
import fr.codeonce.grizzly.core.service.datasource.DBSourceDto;
import fr.codeonce.grizzly.core.service.datasource.cache.ConnectionCacheService;
import fr.codeonce.grizzly.core.service.datasource.mapper.DBSourceMapperService;
import fr.codeonce.grizzly.core.service.util.GlobalExceptionUtil;

@Service
@Transactional
public class ProjectService {

	private static final Logger log = LoggerFactory.getLogger(ProjectService.class);

	private static final String AUTHENTICATION_USER = "authentication_user";

	@Autowired
	private ProjectExample projectExample;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private ContainerService containerService;

	@Autowired
	private ContainerRepository containerRepository;

	@Autowired
	private DBSourceRepository dbSourceRepository;

	@Autowired
	private DBSourceMapperService dbSourceMapperService;

	@Autowired
	private MongoClient mongoClient;

	@Autowired
	private ProjectMapper mapper;

	@Autowired
	private ContainerMapperService containerMapper;

	@Value("${spring.data.mongodb.database}")
	private String mongoDatabase;

	@Autowired
	private AnalyticsService analyticsService;

	@Autowired
	private ConnectionCacheService connectionCacheService;

	/**
	 * Creates a new project in the database with the given project DTO
	 * 
	 * @param projectDto
	 * @return ProjectDto
	 */
	public ProjectDto createProject(ProjectDto projectDto) {

		// MAP DTO
		Project project = mapper.mapToDomain(projectDto);

		// SAVE NEW
		project.getSecurityConfig().setClientId(project.getName());
		project.getSecurityConfig()
				.setSecretKey(DigestUtils.sha256Hex(project.getName()) + DigestUtils.sha256Hex("co%de01/"));
		project.getSecurityConfig().setTokenExpiration(3600);
		project.getRoles().add(Const.ADMIN.getValue());
		project.getRoles().add("user");
		project.setUsername(getCurrentUsername());

		Project savedProject = projectRepository.save(project);

		// Create Default Version of Container V1
		Container containerTosave = new Container();
		containerTosave.setName("1.0.0");
		containerTosave.setDescription("This is a description for this project version");
		containerTosave.setProjectId(savedProject.getId());
		containerTosave.setDbsourceId(savedProject.getDbsourceId());

		DBSource db = dbSourceRepository.findById(savedProject.getDbsourceId())
				.orElseThrow(GlobalExceptionUtil.notFoundException(DBSource.class, savedProject.getDbsourceId()));

		// Set Database Name
		String dbName = projectDto.getDatabaseName();
		if (db.getConnectionMode().equalsIgnoreCase("FREE")) {
			dbName = db.getPhysicalDatabase();
		} 

		createFirstCollection(db.getId(), dbName);

		// Security Group
		ResourceGroup security = new ResourceGroup();
		security.setName("Authentication Grizzly");
		security.setDescription("JWT token");
		containerTosave.getResourceGroups().add(security);
		DBSourceDto savedDataSource = dbSourceMapperService.mapToDto(db);

		containerTosave.setResources(projectExample.createAuthGroup(savedDataSource, security.getName(), dbName));

		// Default Group
		ResourceGroup rg = new ResourceGroup();
		rg.setName("Untitled");
		rg.setDescription("This is a description for this group of APIs");
		containerTosave.getResourceGroups().add(rg);

		// Set the Swagger Salt
		if (StringUtils.isBlank(containerTosave.getSwaggerUuid())) {
			containerTosave.setSwaggerUuid(UUID.randomUUID().toString().substring(0, 8));
		}

		// Save Container
		containerRepository.save(containerTosave);

		// UPDATE METRICS
		analyticsService.updateContainerMetrics(containerTosave);

		// RETURN DTO
		return mapper.mapToDto(savedProject);

	}

	private void createFirstCollection(String dbsourceId, String databasName) {
		MongoClient mClient = connectionCacheService.getMongoClient(dbsourceId);
		MongoDatabase database = mClient.getDatabase(databasName);
		BasicDBObject options = new BasicDBObject();
		options.put("size", 12121212);
		if(database.getCollection(AUTHENTICATION_USER) == null) {
			database.createCollection(AUTHENTICATION_USER);
			Document document = new Document();
			document.append("firstname", "Administrator");
			document.append("lastname", "");
			document.append("username", "admin");
			document.append("password", "admin");
			document.append("roles", Arrays.asList("admin"));
			document.append("enabled", true);
			database.getCollection(AUTHENTICATION_USER).insertOne(document);
		}
	}


	/**
	 * Returns a single project with its given id
	 * 
	 * @param id
	 * @return ProjectDto
	 */
	public ProjectDto get(String id) {
		return projectRepository.findById(id)//
				.map(p -> mapper.mapToDto(p))//
				.orElseThrow(GlobalExceptionUtil.notFoundException(Project.class, id));
	}

	/**
	 * return a list of all the projects in the database
	 * 
	 * @return List<ProjectDto>
	 */
	public List<ProjectDto> getAll() {
		return projectRepository.findAll().stream()// GET ALL
				.map(p -> mapper.mapToDto(p))// MAP ALL TO DTOs
				.collect(Collectors.toList());
	}

	/**
	 * updates the old project with its given id and the new project DTO
	 * 
	 * @param newProjectDto
	 * @param projectId
	 * @return ProjectDto
	 */
	public ProjectDto update(ProjectDto newProjectDto, String projectId) {

		return projectRepository.findById(newProjectDto.getId()).map(p -> {
			if (!p.getDbsourceId().equals(newProjectDto.getDbsourceId())) {
				this.containerService.updateResourcesDbsourceId(newProjectDto.getId(), newProjectDto.getDbsourceId());
			}
			mapper.mapToDomainUpdate(newProjectDto, p);// MAP TO DOMAIN
			projectRepository.save(p);// UPDATE ENTITY
			updateRelatedContainers(p.getId(), p.getDbsourceId(), p.getDatabaseName());
			return mapper.mapToDto(p);// RETURN DTO
		}).orElseThrow(GlobalExceptionUtil.notFoundException(Project.class, projectId));

	}

	/**
	 * Update the Database Name for the Related Containers to a Project
	 * 
	 * @param id of the Project
	 */
	private void updateRelatedContainers(String projectId, String dbsourceId, String databaseName) {
		String name = "dbName";
		MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, mongoDatabase);
		Map<String, String> dbName = new HashMap<>();
		dbName.put(name, databaseName);
		this.dbSourceRepository.findById(dbsourceId).ifPresent(db -> {
			if (db.getConnectionMode().equalsIgnoreCase("FREE")) {
				dbName.put(name, db.getPhysicalDatabase());
			}
			Update update = new Update();
			update.set("enabled", true);
			update.set("dbsourceId", dbsourceId);
			update.set("databaseName", dbName.get(name));
			mongoTemplate.updateMulti(Query.query(Criteria.where("projectId").is(projectId)), update, Container.class);

		});

	}

	/**
	 * deletes a single project with its given id
	 * 
	 * @param id
	 * @return
	 */

	@Transactional
	public void delete(String projectId) {
		projectRepository.findById(projectId).ifPresentOrElse(p -> {
			projectRepository.delete(p);

			// delete all containers
			containerService.deleteContainersUnderProject(projectId);

		}, () -> {
			throw GlobalExceptionUtil.notFoundException(Project.class, projectId).get();
		});
	}

	/**
	 * Deletes all the projects in the database
	 * 
	 * @return
	 */
	public void deleteAll() {
		// Const.DELETE.getValue() ALL
		projectRepository.deleteAll();

	}

	/**
	 * Check if the given project name is taken
	 * 
	 * @param projectName
	 * @return true if a project with the given name already exists, false if not
	 */
	public boolean existsProjectName(String projectName, String projectId) {
		// CHECK PROJECT NAME UNICITY
		boolean[] exists = new boolean[] { false };
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String currentUsername = authentication.getName();
		log.info("username {}", currentUsername);

		this.projectRepository.findByNameIgnoreCaseAndUsername(projectName, currentUsername).ifPresent(proj -> {
			if (projectId != null && proj.getId().equals(projectId)) {
				exists[0] = false;
			} else {
				exists[0] = true;
			}
		});
		return exists[0];
	}

	/**
	 * Export project in JSON File
	 * 
	 * @param projectId
	 * @return JSON Configuration File
	 * @throws IOException
	 */
	public String exportProject(String projectId) throws IOException {
		StringBuilder projectJson = new StringBuilder();
		ProjectDto projectDto = projectRepository.findById(projectId)//
				.map(p -> mapper.mapToDto(p))//
				.orElseThrow(GlobalExceptionUtil.notFoundException(Project.class, projectId));
		ObjectMapper jsonMapper = new ObjectMapper();
		projectJson.append(jsonMapper.writeValueAsString(projectDto));
		projectJson.append(',');
		List<ContainerDto> containersList = containerRepository.findAll().stream()//
				.map(c -> containerMapper.mapToDto(c))//
				.collect(Collectors.toList());
		containersList.forEach(container -> {
			try {
				projectJson.append(jsonMapper.writeValueAsString(container));
				FileWriter file = new FileWriter("testApi.json");
				file.write(projectJson.toString());
				file.close();

			} catch (Exception e) {
				log.debug("{}", e);
			}
		});
		return projectJson.toString();
	}

	/**
	 * return a list of all the projects of a given user
	 * 
	 * @param userEmail
	 * @return List<ProjectDto>
	 */
	public List<ProjectDto> getAllByUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String currentUsername = authentication.getName();
		return projectRepository.findAllByUsername(currentUsername).stream()// GET ALL BY USER
				.map(p -> mapper.mapToDto(p))// MAP ALL TO DTOs
				.collect(Collectors.toList());
	}

	private String getCurrentUsername() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication.getName();
	}
}
