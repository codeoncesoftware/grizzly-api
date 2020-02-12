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
package fr.codeonce.grizzly.runtime.service.query.authentication;

import java.io.FileNotFoundException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.MongoClient;

import fr.codeonce.grizzly.common.runtime.RuntimeQueryRequest;
import fr.codeonce.grizzly.core.service.container.ContainerService;
import fr.codeonce.grizzly.core.service.project.ProjectDto;
import fr.codeonce.grizzly.core.service.project.ProjectService;
import fr.codeonce.grizzly.runtime.service.query.QueryHandler;

@Service
public class RolesHandler {

	@Autowired
	private ProjectService projectService;

	@Autowired
	private ContainerService containerService;
	
	@Autowired 
	private QueryHandler queryHandler;
	
	public Object handleAllRoles(RuntimeQueryRequest queryRequest, String databaseName, MongoClient mClient,
			HttpServletRequest req, HttpServletResponse res, String containerId) {
		String projectId = containerService.get(containerId).getProjectId();
		ProjectDto p = projectService.get(projectId);
		return p.getRoles();
	}

	@SuppressWarnings("unlikely-arg-type")
	public Object handleGrantRoles(RuntimeQueryRequest queryRequest, String databaseName, MongoClient mClient,
			HttpServletRequest req, HttpServletResponse res, String containerId, String parsedBody) throws FileNotFoundException {
		
		queryRequest.setHttpMethod("GET");
		Object findResult = queryHandler.handleFindQuery(queryRequest, databaseName, mClient, req, res);
		if (findResult.toString().trim().equals("[]")) {
			res.setStatus(404);
			return new Document("message", "Username does not exist");
		}
		
		@SuppressWarnings("unchecked")
		List<String> roles = (List<String>) Document.parse(parsedBody).get("roles");
		String projectId = containerService.get(containerId).getProjectId();

		List<String> definedRoles = projectService.get(projectId).getRoles();
		if(checkRoles(definedRoles, roles)) {
			queryRequest.setHttpMethod("PUT");
			return queryHandler.handleUpdateQuery(queryRequest, databaseName, mClient, req, parsedBody);
		} else {
			res.setStatus(404);
			return new Document("message", "This role does not exist. Please enter a valid role.");
		}
	}
	
	private boolean checkRoles(List<String> definedRoles, List<String> roles) {
			for (String role: roles) {
				if(!definedRoles.contains(role)) {
					return false;
				}
			}
			return true;
	}

}
