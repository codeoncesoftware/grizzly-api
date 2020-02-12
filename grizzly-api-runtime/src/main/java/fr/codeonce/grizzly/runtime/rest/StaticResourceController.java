/*-
 * ========================LICENSE_START=================================
 * grizzly-api-runtime
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
package fr.codeonce.grizzly.runtime.rest;

import java.io.IOException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.codeonce.grizzly.runtime.service.query.StaticResourceService;

@RestController
@CrossOrigin(origins = { "*" }, allowedHeaders = { "*" })
@RequestMapping("/runtime/static")
public class StaticResourceController {

	@Autowired
	private StaticResourceService resourceService;

	@GetMapping(value = "/{containerId}/path/**")
	public void generateHttpResponseWithFileContent(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String containerId) throws IOException {
		
		//base URL
		URL requestURL = new URL(request.getRequestURL().toString());
		String port = requestURL.getPort() == -1 ? "" : ":" + requestURL.getPort();
		String baseUrl = requestURL.getProtocol() + "://" + requestURL.getHost() + port;
	    GridFsResource resource = this.resourceService.getResource(containerId, request, response);
		this.resourceService.setHttpServletResponse(resource, response);
	}

	@GetMapping(value = "/{containerId}/{fileId}")
	public void getResourceFile(@PathVariable String containerId, @PathVariable String fileId,
			HttpServletResponse response) throws IOException {

		GridFsResource resource = this.resourceService.getResourceWithId(containerId, fileId);
		this.resourceService.setHttpServletResponse(resource, response);
	}
}
