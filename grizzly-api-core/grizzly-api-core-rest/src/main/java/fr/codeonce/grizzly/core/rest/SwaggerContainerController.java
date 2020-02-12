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

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.codeonce.grizzly.core.service.container.ContainerSwaggerService;

@RestController
@CrossOrigin(origins = { "*" })
@RequestMapping("/api/swagger")
public class SwaggerContainerController {
	
	@Autowired
	private ContainerSwaggerService containerSwaggerService;
	
	private static final Logger log = LoggerFactory.getLogger(SwaggerContainerController.class);
	
	@GetMapping("/{containerId}/{swaggerUuid}")
	public String getSwagger(@PathVariable String containerId, @PathVariable String swaggerUuid) {
		log.info("request to download swagger for container with ID : {}", containerId);
		return containerSwaggerService.getSwaggerJson("prod", containerId, swaggerUuid);
	}
	
	/**
	 * Generate Swagger for a Given Container Id return generated File for DOWNLOAD
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@GetMapping("/generateSwagger/{type}/{containerId}")
	public void downloadGeneratedSwagger(HttpServletResponse response, @PathVariable String type,
			@PathVariable String containerId) throws IOException {
		log.info("request to download a {} swagger for container with ID : {}", type, containerId);
		containerSwaggerService.downloadSwaggerJsonFile(response, type, containerId);
	}

}
