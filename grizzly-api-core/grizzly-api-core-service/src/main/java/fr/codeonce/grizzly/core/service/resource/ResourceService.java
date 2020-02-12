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
package fr.codeonce.grizzly.core.service.resource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.MongoClient;
import com.mongodb.client.gridfs.model.GridFSFile;

import fr.codeonce.grizzly.common.runtime.resource.RuntimeResource;
import fr.codeonce.grizzly.core.domain.container.Container;
import fr.codeonce.grizzly.core.domain.container.ContainerRepository;
import fr.codeonce.grizzly.core.domain.container.hierarchy.ContainerHierarchy;
import fr.codeonce.grizzly.core.domain.container.hierarchy.ContainerHierarchyRepository;
import fr.codeonce.grizzly.core.domain.datasource.DBSource;
import fr.codeonce.grizzly.core.domain.datasource.DBSourceRepository;
import fr.codeonce.grizzly.core.domain.project.ProjectRepository;
import fr.codeonce.grizzly.core.domain.resource.Resource;
import fr.codeonce.grizzly.core.service.analytics.AnalyticsService;
import fr.codeonce.grizzly.core.service.datasource.cache.ConnectionCacheService;
import fr.codeonce.grizzly.core.service.fs.FilesHandler;
import fr.codeonce.grizzly.core.service.fs.GitHandler;
import fr.codeonce.grizzly.core.service.fs.ZipHandler;
import fr.codeonce.grizzly.core.service.fs.model.CustomFile;
import fr.codeonce.grizzly.core.service.fs.model.CustomFolder;
import fr.codeonce.grizzly.core.service.resource.utils.ResourceRuntimeMapper;
import fr.codeonce.grizzly.core.service.util.CustomGitAPIException;

/**
 * A service to Handle files with GridFs : Insert, Delete, Retrieve and Update
 * 
 * @author rayen
 *
 */

@Service
public class ResourceService {

	@Autowired
	private GitHandler gitHandler;

	@Autowired
	private ZipHandler zipHandler;

	@Autowired
	private FilesHandler filesHandler;

	@Autowired
	private ContainerRepository containerRepository;

	@Autowired
	private ConnectionCacheService cacheService;

	@Autowired
	private DBSourceRepository dbSourceRepository;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private ContainerHierarchyRepository cHierarchyRepository;

	@Autowired
	private ResourceRuntimeMapper resourceRuntimeMapper;

	@Autowired
	private AnalyticsService analyticsService;

	@Autowired
	private MappingJackson2HttpMessageConverter springMvcJacksonConverter;

	private static final Logger log = LoggerFactory.getLogger(ResourceService.class);

	/**
	 * Upload a file with GridFs
	 * 
	 * @param file to insert in MongoDb
	 * @return Id of the new inserted resource
	 * @throws IOException in case of missing file
	 */

	public String cloneGitRepository(String body) throws CustomGitAPIException {
		try {
			ObjectNode node = springMvcJacksonConverter.getObjectMapper().readValue(body, ObjectNode.class);
			String gitRepoUrl = (node.get("gitRepoUrl") != null ? node.get("gitRepoUrl").textValue() : null);
			String branch = (node.get("branch") != null ? node.get("branch").textValue() : null);
			String containerId = (node.get("containerId") != null ? node.get("containerId").textValue() : null);
			String dbsourceId = (node.get("dbsourceId") != null ? node.get("dbsourceId").textValue() : null);
			String databaseName = (node.get("databaseName") != null ? node.get("databaseName").textValue() : null);
			String gitUsername = (node.get("gitUsername") != null ? node.get("gitUsername").textValue() : null);
			String gitPassword = (node.get("gitPassword") != null ? node.get("gitPassword").textValue() : null);

			log.debug("Request to get git repository branchs list from : {}", gitRepoUrl);

			return this.gitHandler.cloneGitRepository(gitRepoUrl, branch, containerId, dbsourceId, databaseName,
					gitUsername, gitPassword);

		} catch (IOException e) {
			log.debug("Error while parsing GIT informations");
			throw new BadCredentialsException("4011");
		}
	}

	public List<String> getRepoBranchsList(String body) {
		try {
			ObjectNode node = springMvcJacksonConverter.getObjectMapper().readValue(body, ObjectNode.class);
			String gitRepoUrl = (node.get("gitRepoUrl") != null ? node.get("gitRepoUrl").textValue() : null);
			String gitUsername = (node.get("gitUsername") != null ? node.get("gitUsername").textValue() : null);
			String gitPassword = (node.get("gitPassword") != null ? node.get("gitPassword").textValue() : null);
			log.debug("Request to get git repository branchs list from : {}", gitRepoUrl);
			return this.gitHandler.getRepoBranchsList(gitRepoUrl, gitUsername, gitPassword);
		} catch (IOException e) {
			log.debug("Error while parsing GIT informations");
			throw new BadCredentialsException("4011");
		}
	}

	public String importZipFile(MultipartFile file, String containerId, String dbsourceId, String databaseName)
			throws IOException {
		return this.zipHandler.importZipFile(file, containerId, dbsourceId, databaseName);
	}

	public GridFSFile getResourceFileWithId(String containerId, String fileId) {
		return this.filesHandler.getResourceFileWithId(containerId, fileId);
	}

	public GridFSFile getResourceFile(String containerId, String fileUri) {
		return this.filesHandler.getResource(containerId, fileUri);
	}

	public GridFSFile getResource(String containerId, String path) {
		return this.filesHandler.getResource(containerId, path);
	}

	public GridFsResource getGridFsResource(GridFSFile fsFile, String containerId) {
		return this.filesHandler.getGridFsResource(fsFile, containerId);
	}

	/**
	 * Prepare the Runtime Resource, API, to be forwarder to the GateWay
	 * 
	 * @param containerId
	 * @param resourcePath
	 * @return RuntimeResource
	 */
	public RuntimeResource getRuntimeResource(String containerId, String resourcePath) {
		RuntimeResource resource = new RuntimeResource();
		Optional<Container> cont = this.containerRepository.findById(containerId);
		if (cont.isPresent()) {
			Container container = cont.get();
			StringBuilder secretKey = new StringBuilder();
			// Prepare the RuntimeResource
			Resource ress = container.getResources().stream().filter(res -> hasSamePath(resourcePath, res)).findFirst()
					.orElse(null);
			resource = resourceRuntimeMapper.mapToRuntime(ress);
			this.projectRepository.findById(container.getProjectId())
					.ifPresent(proj -> secretKey.append(proj.getSecurityConfig().getSecretKey()));
			resource.setSecurityKey(secretKey.toString());
			resource.setReturnType(ress != null ? ress.getProduces().get(0) : "application/json");
		}
		// ASYNC ANALYTICS
		CompletableFuture.runAsync(() -> analyticsService.updateRequestCount(containerId));

		return resource;
	}

	/**
	 * Test if the Received Path matches the Resource Path considering PathVariables
	 * 
	 * @param resourcePath
	 * @param res
	 * @return boolean, True if paths match, false if not
	 */
	private boolean hasSamePath(String resourcePath, Resource res) {
		boolean result = true;

		List<String> receivedPath = Arrays.asList(resourcePath.substring(1).split("/"));
		List<String> resspath = Arrays.asList(res.getPath().substring(1).split("/"));

		if (receivedPath.size() != resspath.size()) {
			return false;
		}

		int index = 0;
		for (String part : resspath) {
			if (!(part.contains("{") && part.contains("}")) //
					&& receivedPath.size() >= index //
					&& !part.equalsIgnoreCase(receivedPath.get(index))) {
				result = false;
			}
			++index;
		}
		return result;
	}

	/**
	 * Handle Uploading File to GridFs
	 * 
	 * @param file
	 * @param idContainer
	 * @return
	 * @throws IOException
	 */
	public String uploadFile(MultipartFile file, String idContainer) throws IOException {
		Optional<Container> cont = this.containerRepository.findById(idContainer);
		if (cont.isPresent()) {
			Container container = cont.get();
			MongoClient mClient = this.cacheService.getMongoClient(container.getDbsourceId());
			String databaseName = null;
			Optional<DBSource> dbsource = this.dbSourceRepository.findById(container.getDbsourceId());
			if (dbsource.isPresent()) {
				databaseName = container.getDatabaseName();
			}
			Document metaData = new Document();
			metaData.put("containerId", idContainer);
			metaData.put("fileUri", file.getOriginalFilename());
			String fileId = this.cacheService.getGridFs(mClient, databaseName)
					.store(file.getInputStream(), file.getOriginalFilename(), metaData).toHexString();
			Optional<ContainerHierarchy> hierarchyOp = this.cHierarchyRepository.findById(container.getHierarchyId());
			if (hierarchyOp.isPresent()) {
				ContainerHierarchy hierarchy = hierarchyOp.get();
				ObjectMapper mapper = new ObjectMapper();
				CustomFolder folder = mapper.readValue(hierarchyOp.get().getHierarchy(), CustomFolder.class);
				folder.getChildren().add(new CustomFile(file.getOriginalFilename(), fileId));
				hierarchy.setHierarchy(mapper.writeValueAsString(folder));
				this.cHierarchyRepository.save(hierarchy);
			}

		}
		return null;
	}

	public void deleteFiles(String containerId) {

		this.filesHandler.deleteGridfsFiles(containerId);

	}

}
