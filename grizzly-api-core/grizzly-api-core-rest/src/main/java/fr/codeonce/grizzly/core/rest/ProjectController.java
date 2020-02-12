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

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.codeonce.grizzly.core.service.project.ProjectDto;
import fr.codeonce.grizzly.core.service.project.ProjectService;

@RestController
@RequestMapping("/api/project")
public class ProjectController {
	
	private static final Logger log = LoggerFactory.getLogger(ProjectController.class);

	@Autowired
	private ProjectService projectService;

	/**
	 * Check if the new project name is not taken
	 * 
	 * @param name (the new project's name)
	 * @return true if already exists, false if not
	 */
	@GetMapping("/check/{name}")
	public boolean existsProjectName(@PathVariable String name) {
		log.info("Request for Checking project name unicity : {}", name);
		return projectService.existsProjectName(name, null);
	}
	
	@GetMapping("/check/{name}/{projectId}")
	public boolean existsProjectName(@PathVariable String name, @PathVariable String projectId) {
		log.info("Request for Checking project name unicity : {}", name);
		return projectService.existsProjectName(name, projectId);
	}

	/**
	 * Creates a new project in the database with the given project DTO
	 * 
	 * @param projectDto
	 * @return ProjectDto
	 */
	@PostMapping("/create")
	public ProjectDto createProject(@RequestBody ProjectDto projectDto) {
		log.info("Request for Creating new project with name :{}", projectDto.getName());
		return projectService.createProject(projectDto);
	}

	/**
	 * Returns a single project with its given id
	 *
	 * @param id
	 * @return ProjectDto
	 */
	@GetMapping("/{id}")
	public ProjectDto getProject(@PathVariable String id) {
		log.info("Request for Getting project with ID : {}", id);
		return projectService.get(id);
	}

	/**
	 * Updates the old project with its given id and the new project DTO
	 * 
	 * @param newProjectDto
	 * @param projectId
	 * @return ProjectDto
	 */
	@PutMapping("/update/{projectId}")
	public ProjectDto update(@RequestBody ProjectDto newProjectDto, @PathVariable String projectId) {
		log.info("Request for Updating a project with ID : {}", projectId);
		return projectService.update(newProjectDto, projectId);
	}

	/**
	 * Deletes a single project with its given id
	 * 
	 * @param projectId
	 * @return
	 */
	@DeleteMapping("/delete/{projectId}")
	public void delete(@PathVariable String projectId) {
		log.info("Request for Deleting a project with ID : {}", projectId);
		this.projectService.delete(projectId);
	}

	/**
	 * Deletes all the projects in the database
	 * 
	 * @return
	 */
	@DeleteMapping("/deleteAll")
	public void deleteAll() {
		log.info("Delete all projects Request");
		projectService.deleteAll();
	}

	@GetMapping("/export/{projectId}")
	public String exportProject(@PathVariable String projectId) throws IOException {
		log.info("Request to export a project with ID : {}", projectId);
		return projectService.exportProject(projectId);
	}

	/**
	 * Returns a list of all the projects in the database
	 * 
	 * @return List<ProjectDto>
	 */
	@GetMapping("/all")
	public List<ProjectDto> getAll() {
		log.info("Request for Deleting all projects for the signed-in user");
		return projectService.getAllByUser();
	}

}
