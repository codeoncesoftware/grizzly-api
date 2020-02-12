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

/**
 * 
 * Controller to Handle file's requests
 * Using {@link ResourceService}
 *
 * @author rayen
 *
 */

import java.io.IOException;
import java.util.List;

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
import org.springframework.web.multipart.MultipartFile;

import fr.codeonce.grizzly.common.runtime.resource.RuntimeResource;
import fr.codeonce.grizzly.core.service.resource.ResourceService;
import fr.codeonce.grizzly.core.service.util.CustomGitAPIException;

@RestController
@CrossOrigin(origins = { "*" }, allowedHeaders = { "*" })
@RequestMapping("/api/resource")

public class ResourceController {

	private static final Logger log = LoggerFactory.getLogger(ResourceController.class);

	@Autowired
	private ResourceService resourceService;

	@PostMapping(value = "/getbranchslist")
	public List<String> getRepoBranchsList(@RequestBody String body) {
		
		log.debug("Request to get git repository branchs list");
		
		return this.resourceService.getRepoBranchsList(body);
	}

	@PostMapping(value = "/importGitProject")
	public String cloneGitRepo(@RequestBody String body) throws CustomGitAPIException {
		log.info("Request to clone GIT repository");
		return this.resourceService.cloneGitRepository(body);
	}

	@PostMapping(value = "/importZipProject")
	public String importZipFile(@RequestParam MultipartFile zipFile, @RequestParam String idContainer,
			@RequestParam String dbsourceId, @RequestParam String databaseName) throws IOException {
		log.info("Request to import ZIP File for container with ID : {}", idContainer);
		return this.resourceService.importZipFile(zipFile, idContainer, dbsourceId, databaseName);
	}

	@GetMapping("/public")
	public RuntimeResource getResource(@RequestParam("containerId") String containerId,
			@RequestParam("resourcePath") String resourcePath) {
		log.info("Request to fetch resource from container with ID : {} and resource path is : {}", containerId,
				resourcePath);

		// DEBUG PERFORMANCE
		StopWatch stopWatch = new StopWatch();
		stopWatch.start("request to fetch resource");

		RuntimeResource runtimeResource = this.resourceService.getRuntimeResource(containerId, resourcePath);

		// SHOW DEBUG
		stopWatch.stop();
		log.debug(stopWatch.prettyPrint());

		return runtimeResource;
	}
	
	@PostMapping(value = "/uploadFile")
	public String uploadFile(@RequestParam MultipartFile file, @RequestParam String containerId) throws IOException {
		return this.resourceService.uploadFile(file, containerId);
	}
	
	@DeleteMapping(value = "/delete/{containerId}")
	public void deleteFiles(@PathVariable String containerId) {
		this.resourceService.deleteFiles(containerId);
	}

}
