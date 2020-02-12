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

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import fr.codeonce.grizzly.core.domain.resource.Resource;
import fr.codeonce.grizzly.core.service.container.ContainerCloneService;
import fr.codeonce.grizzly.core.service.container.ContainerDto;
import fr.codeonce.grizzly.core.service.container.ContainerExportService;
import fr.codeonce.grizzly.core.service.container.ContainerImportService;
import fr.codeonce.grizzly.core.service.container.ContainerResourceService;
import fr.codeonce.grizzly.core.service.container.ContainerService;
import fr.codeonce.grizzly.core.service.container.ContainerSwaggerService;

@RestController
@CrossOrigin(origins = { "*" })
@RequestMapping("/api/container")
public class ContainerController {

	private static final Logger log = LoggerFactory.getLogger(ContainerController.class);

	@Autowired
	private ContainerService containerService;

	@Autowired
	private ContainerSwaggerService containerSwaggerService;

	@Autowired
	private ContainerImportService containerImportService;

	@Autowired
	private ContainerExportService containerExportService;

	@Autowired
	private ContainerCloneService containerCloneService;

	@Autowired
	private ContainerResourceService containerResourceService;

	/**
	 * Creates a new container given a container DTO
	 * 
	 * @param containerDto
	 * @return ContainerDto
	 */
	@PostMapping("/create")
	public ContainerDto saveContainer(@RequestBody ContainerDto containerDto) {
		log.info("request to save container {} ", containerDto.getName());
		return containerService.saveContainer(containerDto);
	}

	/**
	 * Check if the new container's name is unique in a project
	 * 
	 * @param containerDto
	 * @return ContainerDto
	 */
	@PostMapping("/check")
	public boolean existsContainerName(@RequestBody ContainerDto containerDto) {
		log.info("request to check container name ynicity for {} ", containerDto.getName());
		return containerService.existsContainerName(containerDto);
	}

	/**
	 * Returns a Container given its containerId
	 * 
	 * @param containerId
	 * @return ContainerDto
	 */
	@GetMapping("/{containerId}")
	public ContainerDto get(@PathVariable String containerId) {
		log.info("request to get container with ID {} ", containerId);
		return containerService.get(containerId);
	}

	/**
	 * Returns a list of all the containers in the database
	 * 
	 * @return List<ContainerDto>
	 */
	@GetMapping("/all")
	public List<ContainerDto> getAll() {
		log.info("request to get all containers");
		return containerService.getAll();
	}

	/**
	 * Returns a list of all the containers that belong to a specified project given
	 * its projectId
	 * 
	 * @param uuid
	 * @return
	 */
	@GetMapping("/project/{projectId}")
	public List<ContainerDto> containersByProject(@PathVariable String projectId) {
		log.info("request to get containers for project with ID : {} ", projectId);
		return containerService.containersByProject(projectId);
	}

	/**
	 * Deletes a container given its containerId
	 * 
	 * @param containerId
	 */
	@DeleteMapping("/delete/{containerId}")
	public void delete(@PathVariable String containerId) {
		log.info("request to delete container with ID : {} ", containerId);
		containerService.delete(containerId);
	}

	/**
	 * Deletes all the Containers in the DB
	 * 
	 */
	@DeleteMapping("/deleteAll")
	public void deleteAll() {
		log.info("request to delete all wontainers");
		containerService.deleteAll();
	}

	/**
	 * Deletes all the containers under a project in the database for a Given
	 * Project ID
	 * 
	 */
	@DeleteMapping("/deleteAll/project/{projectId}")
	public void deleteAllByProject(@PathVariable String projectId) {
		log.info("request to delete all containers for project with ID : {} ", projectId);
		containerService.deleteAllByProject(projectId);
	}

	/**
	 * Generate a new Container From an uploaded Swagger JSON File
	 * 
	 * @param file
	 * @param projectId
	 * @param containerName
	 * @return a new ContainerDto for the fresh Container
	 * @throws Exception
	 * @throws IOException
	 */
	@PostMapping("/importSwagger")
	public ContainerDto importSwagger(@RequestParam("file") MultipartFile file, @RequestParam String projectId,
			@RequestParam(required = false) String containerName) throws Exception {
		log.info("request to import a swagger file for container with name : {} of project with ID : {}", containerName,
				projectId);
		return containerSwaggerService.importSwagger(file, projectId, containerName);
	}

	/**
	 * Update the container to match the uploaded Swagger definition
	 * 
	 * @param file
	 * @param containerId
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/importSwaggerOnExistingContainer")
	public ContainerDto importSwaggerOnExistingContainer(@RequestParam("file") MultipartFile file,
			@RequestParam String containerId) throws Exception {
		log.info("request to import a swagger file for container with ID : {} ", containerId);
		return containerSwaggerService.importSwaggerOnExistingContainer(file, containerId);
	}

	/**
	 * Toggle container status, if active it will be used by kubernates 
	 * (This feature will be used in version 2)
	 * 
	 * @param containerId
	 */
	@GetMapping("/enableDisable/{containerId}")
	public void enableDisableContainer(@PathVariable String containerId) {
		log.info("request to enableDisable container with ID : {} ", containerId);
		containerService.enableDisableContainer(containerId);
	}

	/**
	 * Clone an existing container to create next version of the micro-service
	 * 
	 * @param containerId
	 * @param newContainerName
	 * @return
	 * @throws IOException
	 * @throws JSONException 
	 */
	@PostMapping("/clone/{containerId}")
	public ContainerDto cloneContainer(@PathVariable String containerId, @RequestBody String newContainerName)
			throws IOException, JSONException {
		log.info("request to clone container with ID : {} and new name as : {}", containerId, newContainerName);
		return containerCloneService.cloneContainer(containerId, newContainerName);
	}

	/**
	 * Fetch a Resource based on the Container ID and the Resource Path to be
	 * returned to ZUUL in order to Execute a Query API
	 * 
	 * @param containerId
	 * @param resourcePath
	 * @return
	 */
	@GetMapping("/getResource")
	public Resource getResource(@RequestParam("containerId") String containerId,
			@RequestParam("resourcePath") String resourcePath) {
		log.info("request to fetch resource from container with ID : {} and resource path as : {}", containerId,
				resourcePath);
		return containerResourceService.getResource(containerId, resourcePath);
	}

	/**
	 * Generate a ZIP file containing the Swagger file and the related uploaded resources
	 * 
	 * @param containerId
	 * @param response
	 * @throws JSONException
	 * @throws IOException
	 */
	@GetMapping(value = "/export/{containerId}", produces = "application/zip")
	public void export(@PathVariable String containerId, HttpServletResponse response)
			throws JSONException, IOException {
		log.info("request to export container with ID : {}", containerId);

		File directory = containerExportService.export(containerId);
		List<String> fileList = containerExportService.getFileList(directory);
		containerExportService.zipContainer(fileList, containerId, directory, response);
	}

	@PostMapping("/import")
	public ContainerDto importContainer(@RequestParam("file") MultipartFile file, @RequestParam String projectId,
			@RequestParam String containerName, @RequestParam String dbsourceId, @RequestParam String databaseName)
			throws Exception {
		log.info("request to import container for Project with ID : {}", projectId);
		return containerImportService.importContainer(file, containerName, projectId, dbsourceId, databaseName);
	}
	
}
