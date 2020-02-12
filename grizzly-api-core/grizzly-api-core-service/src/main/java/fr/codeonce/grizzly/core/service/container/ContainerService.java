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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.codeonce.grizzly.core.domain.container.Container;
import fr.codeonce.grizzly.core.domain.container.ContainerRepository;
import fr.codeonce.grizzly.core.domain.container.hierarchy.ContainerHierarchyRepository;
import fr.codeonce.grizzly.core.domain.datasource.DBSource;
import fr.codeonce.grizzly.core.domain.datasource.DBSourceRepository;
import fr.codeonce.grizzly.core.domain.project.Project;
import fr.codeonce.grizzly.core.domain.project.ProjectRepository;
import fr.codeonce.grizzly.core.domain.resource.ResourceGroup;
import fr.codeonce.grizzly.core.service.analytics.AnalyticsService;
import fr.codeonce.grizzly.core.service.datasource.DBSourceDto;
import fr.codeonce.grizzly.core.service.datasource.mapper.DBSourceMapperService;
import fr.codeonce.grizzly.core.service.fs.FilesHandler;
import fr.codeonce.grizzly.core.service.project.ProjectExample;
import fr.codeonce.grizzly.core.service.util.GlobalExceptionUtil;
import fr.codeonce.grizzly.core.service.util.SecurityContextUtil;

@Service
@Transactional
public class ContainerService {

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private ProjectExample projectExample;

	@Autowired
	private ContainerRepository containerRepository;

	@Autowired
	private ContainerMapperService containerMapper;

	@Autowired
	private DBSourceRepository dbSourceRepository;

	@Autowired
	private ContainerHierarchyRepository hierarchyRepository;

	@Autowired
	private FilesHandler filesHandler;

	@Autowired
	private AnalyticsService analyticsService;

	@Autowired
	private DBSourceMapperService dbSourceMapperService;

	/**
	 * Creates a new container or updates the existing one given a container DTO
	 * 
	 * @param containerDto
	 * @return ContainerDto
	 */
	public ContainerDto saveContainer(ContainerDto containerDto) {

		String projectId = containerDto.getProjectId();

		Optional<Project> p = projectRepository.findById(projectId);
		// MAP DTO

		if (p.isPresent()) {

			// set Container to save
			Container containerToSave = containerMapper.mapToDomain(containerDto);
			// If a DB is linked
			if (p.get().getDbsourceId() != null) {
				this.dbSourceRepository.findById(p.get().getDbsourceId()).ifPresent(db -> {
					containerToSave.setDbsourceId(db.getId());
					String dbName = "";

					if (db.getConnectionMode().equalsIgnoreCase("FREE")) {
						dbName = db.getPhysicalDatabase();
					} else {
						dbName = p.get().getDatabaseName();
					}
					containerToSave.setDatabaseName(dbName);

				});
			}

			if (containerDto.getId() == null) { // Container creation only
				Optional<DBSource> db = dbSourceRepository.findById(p.get().getDbsourceId());

				// Security Group
				if (db.isPresent()) {
					ResourceGroup security = new ResourceGroup();
					security.setName("Authentication Grizzly");
					security.setDescription("JWT token");
					containerToSave.getResourceGroups().add(security);
					DBSourceDto savedDataSource = dbSourceMapperService.mapToDto(db.get());
					String dbName = "";

					if (db.get().getConnectionMode().equalsIgnoreCase("FREE")) {
						dbName = db.get().getPhysicalDatabase();
					} else {
						dbName = p.get().getDatabaseName();
					}
					containerToSave
							.setResources(projectExample.createAuthGroup(savedDataSource, security.getName(), dbName));
				}

				// Default Group
				ResourceGroup rg = new ResourceGroup();
				rg.setName("Untitled");
				rg.setDescription("This is a description for this group of APIs");
				List<ResourceGroup> lrg = new ArrayList<>();
				lrg.add(rg);
				containerToSave.getResourceGroups().add(rg);

			}

			// SET EMAIL
			containerToSave.setUsername(SecurityContextUtil.getCurrentUsername());

			// Set the Swagger Salt
			if (StringUtils.isBlank(containerToSave.getSwaggerUuid())) {
				containerToSave.setSwaggerUuid(UUID.randomUUID().toString().substring(0, 8));
			}
			// SAVE
			Container savedContainer = containerRepository.save(containerToSave);

			// Save Analytics
			analyticsService.updateContainerMetrics(savedContainer);

			// RETURN DTO
			return containerMapper.mapToDto(savedContainer);

		} else {
			throw GlobalExceptionUtil.notFoundException(Project.class, projectId).get();
		}

	}

	/**
	 * Returns a Container with its given Id
	 * 
	 * @param containerId
	 * @return ContainerDto
	 */
	public ContainerDto get(String containerId) {
		return containerRepository.findById(containerId)//
				.map(c -> containerMapper.mapToDto(c))//
				.orElseThrow(GlobalExceptionUtil.notFoundException(Container.class, containerId));
	}

	/**
	 * Checks if the new container's name is unique
	 * 
	 * @param containerDto
	 * @return boolean
	 */
	public boolean existsContainerName(ContainerDto containerDto) {
		List<ContainerDto> list = containersByProject(containerDto.getProjectId()).stream()
				.filter(dto -> dto.getName().equalsIgnoreCase(containerDto.getName())).collect(Collectors.toList());
		return !(list.isEmpty());
	}

	/**
	 * Returns a list of all the containers in the database
	 * 
	 * @return List<ContainerDto>
	 */
	public List<ContainerDto> getAll() {
		return containerRepository.findAll().stream()//
				.map(c -> containerMapper.mapToDto(c))//
				.collect(Collectors.toList());
	}

	/**
	 * Returns a list of all the containers that belong to a specified project id
	 * 
	 * @param projectId
	 * @return List<ContainerDto>
	 */
	public List<ContainerDto> containersByProject(String projectId) {
		return containerRepository.findAllByProjectId(projectId).stream()//
				.map(c -> containerMapper.mapToDto(c))//
				.collect(Collectors.toList());
	}

	public void deleteContainersUnderProject(String projectId) {
		containersByProject(projectId).stream().map(c -> {
			delete(c.getId());
			return c;
		}).collect(Collectors.toList());
	}

	/**
	 * Deletes a container given its containerId
	 * 
	 * @param containerId
	 * @return
	 */
	public void delete(String containerId) {
		containerRepository.findById(containerId).map(c -> {
			deleteHierarchy(c);
			filesHandler.deleteGridfsFiles(containerId);
			containerRepository.delete(c);
			analyticsService.removeContainerAnalytics(c.getId());
			return true;
		}).orElseThrow(GlobalExceptionUtil.notFoundException(Container.class, containerId));
	}

	/**
	 * Delete a container's hierarchy
	 * 
	 * @param c, the given container to delete
	 */
	private void deleteHierarchy(Container c) {
		if (c.getHierarchyId().isEmpty())
			return;
		hierarchyRepository.deleteById(c.getHierarchyId());
	}

	/**
	 * Deletes all the containers in the database
	 * 
	 * @return
	 */
	public void deleteAll() {
		containerRepository.deleteAll();
	}

	/**
	 * Deletes all the containers under a project in the database
	 */
	public void deleteAllByProject(String projectId) {
		containerRepository.deleteContainerByProjectId(projectId);

	}

	/**
	 * Enable or Disable a Container
	 * 
	 * @param containerId
	 */
	public void enableDisableContainer(String containerId) {
		Optional<Container> container = containerRepository.findById(containerId);
		if (container.isPresent()) {
			containerRepository.findAllByProjectId(container.get().getProjectId()).stream().filter(Container::isEnabled)
					.forEach(cont -> {
						cont.setEnabled(false);
						containerRepository.save(cont);
					});
			container.get().setEnabled(!container.get().isEnabled());
			containerRepository.save(container.get());
		}
	}

	/**
	 * Update dbsourceId field in all resources on Project Database Change
	 * 
	 * @param id of the project
	 */
	public void updateResourcesDbsourceId(String projectId, String dbsourceId) {

		containerRepository.findAllByProjectId(projectId).stream().forEach(cont -> {
			cont.getResources().parallelStream().forEach(ress -> {
				ress.getCustomQuery().setDatasource(dbsourceId);
			});
			containerRepository.save(cont);
		});
	}

}
