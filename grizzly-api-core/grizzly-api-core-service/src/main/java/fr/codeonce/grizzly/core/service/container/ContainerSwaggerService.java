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
package fr.codeonce.grizzly.core.service.container;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import fr.codeonce.grizzly.core.domain.container.Container;
import fr.codeonce.grizzly.core.domain.container.ContainerRepository;
import fr.codeonce.grizzly.core.domain.datasource.DBSource;
import fr.codeonce.grizzly.core.domain.datasource.DBSourceRepository;
import fr.codeonce.grizzly.core.domain.project.Project;
import fr.codeonce.grizzly.core.domain.project.ProjectRepository;
import fr.codeonce.grizzly.core.domain.resource.ResourceGroup;
import fr.codeonce.grizzly.core.service.datasource.DBSourceDto;
import fr.codeonce.grizzly.core.service.datasource.mapper.DBSourceMapperService;
import fr.codeonce.grizzly.core.service.project.ProjectExample;
import fr.codeonce.grizzly.core.service.swagger.SwaggerGenerator;
import fr.codeonce.grizzly.core.service.util.GlobalExceptionUtil;
import io.swagger.models.Swagger;

@Service
@Transactional
public class ContainerSwaggerService {

	@Autowired
	private ContainerRepository containerRepository;

	@Autowired
	private SwaggerGenerator swaggerGenerator;

	@Autowired
	private ProjectExample projectExample;

	@Autowired
	private ContainerMapperService containerMapper;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private DBSourceRepository dbSourceRepository;

	@Autowired
	private DBSourceMapperService dbSourceMapperService;

	private static final Logger log = LoggerFactory.getLogger(ContainerSwaggerService.class);

	/**
	 * Generate Swagger From a Container from it's given ID
	 * 
	 * @param containerId
	 * @return Swagger File
	 * @throws IOException
	 */
	public File generateSwagger(String type, String containerId) throws IOException {
		Optional<Container> container = containerRepository.findById(containerId);
		if (container.isPresent()) {
			String json = swaggerGenerator.generate(container.get(), type);
			// Write JSON file
			String fileName = buildFileName(container.get(), type);
			if (type.equalsIgnoreCase("dev")) {
				fileName = fileName + "-dev";
			}
			fileName = fileName.concat(".json");
			try (FileWriter fw = new FileWriter(fileName)) {
				fw.write(json);
				fw.flush();
				return new File(fileName);
			} catch (Exception e) {
				log.debug("error in json file {}:", e);
			}
		}
		throw GlobalExceptionUtil.notFoundException(Container.class, containerId).get();
	}

	private String buildFileName(Container container, String type) {
		return projectRepository.findById(container.getProjectId())//
				.map(p -> StringUtils.joinWith("_", p.getName(), container.getName()))
				.orElseThrow(GlobalExceptionUtil.notFoundException(Project.class, container.getProjectId()));
	}

	/**
	 * Set Swagger JSON file to the Response for download
	 * 
	 * @throws IOException
	 */
	public void downloadSwaggerJsonFile(HttpServletResponse response, String type, String containerId)
			throws IOException {
		// Generate a Swagger.json File
		File file = generateSwagger(type, containerId);
		response.setContentType("application/json");
		response.setContentLength((int) file.length());
		response.setHeader("Content-Disposition", String.format("attachment; filename=%s", file.getName()));
		response.setHeader("fileName", file.getName());
		try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
			FileCopyUtils.copy(inputStream, response.getOutputStream());
			// Delete the Generated File after Download
			FileUtils.deleteQuietly(file);
		} catch (Exception e) {
			throw GlobalExceptionUtil.fileNotFoundException(containerId).get();
		}
	}

	/**
	 * Create a Container from a JSON Swagger File, save it in the DB and map it to
	 * a ContainerDto
	 * 
	 * @param file
	 * @param projectId
	 * @return ContainerDto to be previewed in the front-end
	 * @throws Exception
	 * @throws IOException
	 */
	public ContainerDto importSwagger(MultipartFile file, String projectId, String containerName) throws Exception {
		Container container = saveSwaggerContainer(file, projectId, containerName);
		this.projectRepository.findById(projectId).ifPresent(proj -> {
			container.setDatabaseName(proj.getDatabaseName());
			container.setDbsourceId(proj.getDbsourceId());
		});
		if (container.getDbsourceId() != null) {
			this.dbSourceRepository.findById(container.getDbsourceId()).ifPresent(db -> {
				if (db.getConnectionMode().equalsIgnoreCase("FREE")) {
					container.setDatabaseName(db.getPhysicalDatabase());
				}
			});
		}

		Container containerToSave = addDefaultAuthGoup(container);

		return containerMapper.mapToDto(containerRepository.save(containerToSave));
	}

	public ContainerDto importSwaggerOnExistingContainer(MultipartFile file, String containerId) throws Exception {
		Container currentContainer = containerRepository.findById(containerId)
				.orElseThrow(GlobalExceptionUtil.notFoundException(Container.class, containerId));

		Project currentProject = this.projectRepository.findById(currentContainer.getProjectId())
				.orElseThrow(GlobalExceptionUtil.notFoundException(Container.class, containerId));

		Swagger swagger = swaggerGenerator.getSwagger(file);
		Container container = swaggerGenerator.mapSwaggerToContainer(swagger, currentProject.getId());
		container.setId(containerId);
		container.setProjectId(currentContainer.getProjectId());
		container.setName(currentContainer.getName());
		container.setHierarchyId(currentContainer.getHierarchyId());

		Container containerToSave = addDefaultAuthGoup(container);

		return containerMapper.mapToDto(containerRepository.save(containerToSave));
	}

	public Container saveSwaggerContainer(MultipartFile file, String projectId, String containerName) throws Exception {
		Swagger swagger = swaggerGenerator.getSwagger(file);

		Container container = swaggerGenerator.mapSwaggerToContainer(swagger, projectId);
		// Custom Mapping
		container.setProjectId(projectId);
		if (containerName != null) {
			container.setName(containerName);
		} else if (swagger.getInfo() != null) {
			// Set the Version as Name
			container.setName(swagger.getInfo().getVersion());
		}
		return container;
	}

	public String getSwaggerJson(String type, String containerId, String swaggerUuid) {
		Optional<Container> container = containerRepository.findById(containerId);
		if (container.isPresent() && container.get().getSwaggerUuid().equals(swaggerUuid)) {
			return this.swaggerGenerator.generate(container.get(), type);
		} else {
			throw new IllegalArgumentException("URL is not valid");
		}

	}

	private Container addDefaultAuthGoup(Container container) {
		Project project = projectRepository.findById(container.getProjectId())
				.orElseThrow(GlobalExceptionUtil.notFoundException(DBSource.class, container.getProjectId()));

		ResourceGroup security = new ResourceGroup();
		security.setName("Authentication Grizzly");
		security.setDescription("JWT token");
		container.getResourceGroups().add(0, security);
		DBSourceDto savedDataSource = dbSourceMapperService
				.mapToDto(dbSourceRepository.findById(project.getDbsourceId()).get());
		String dbName = "";
		DBSource db = dbSourceRepository.findById(project.getDbsourceId()).get();
		if (dbSourceRepository.findById(project.getDbsourceId()).get().getConnectionMode().equalsIgnoreCase("FREE")) {
			dbName = db.getPhysicalDatabase();
		} else {
			dbName = db.getDatabase();
		}
		container.getResources().addAll(0, projectExample.createAuthGroup(savedDataSource, security.getName(), dbName));
		return container;
	}

}
