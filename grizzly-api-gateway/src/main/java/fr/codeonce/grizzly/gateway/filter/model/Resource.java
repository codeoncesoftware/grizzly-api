/*-
 * ========================LICENSE_START=================================
 * grizzly-api-gateway
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
package fr.codeonce.grizzly.gateway.filter.model;

import java.util.ArrayList;
import java.util.List;

public class Resource {

	private String name;
	private String summary;
	private String description;
	private ResourceFile resourceFile;
	private List<ResourceFile> secondaryFilePaths = new ArrayList<>();
	private String path;
	private String httpMethod;
	private String executionType;
	private CustomQuery customQuery = new CustomQuery();
	private List<String> consumes = new ArrayList<>();
	private List<String> produces = new ArrayList<>();
	private String resourceGroup;
	private List<ResourceParameter> parameters = new ArrayList<>();
	private List<APIResponse> responses = new ArrayList<>();
	private String securityLevel;
	private List<String> fields;
	private boolean pageable;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public List<ResourceParameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<ResourceParameter> parameters) {
		this.parameters = parameters;
	}

	public String getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}

	public String getResourceGroup() {
		return resourceGroup;
	}

	public void setResourceGroup(String resourceGroup) {
		this.resourceGroup = resourceGroup;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getExecutionType() {
		return executionType;
	}

	public void setExecutionType(String executionType) {
		this.executionType = executionType;
	}

	public CustomQuery getCustomQuery() {
		return customQuery;
	}

	public void setCustomQuery(CustomQuery customQuery) {
		this.customQuery = customQuery;
	}

	public List<String> getConsumes() {
		return consumes;
	}

	public void setConsumes(List<String> consumes) {
		this.consumes = consumes;
	}

	public List<String> getProduces() {
		return produces;
	}

	public void setProduces(List<String> produces) {
		this.produces = produces;
	}

	public ResourceFile getResourceFile() {
		return resourceFile;
	}

	public void setResourceFile(ResourceFile resourceFile) {
		this.resourceFile = resourceFile;
	}

	public List<APIResponse> getResponses() {
		return responses;
	}

	public void setResponses(List<APIResponse> responses) {
		this.responses = responses;
	}

	public List<ResourceFile> getSecondaryFilePaths() {
		return secondaryFilePaths;
	}

	public void setSecondaryFilePaths(List<ResourceFile> secondaryFilePaths) {
		this.secondaryFilePaths = secondaryFilePaths;
	}

	public List<String> getFields() {
		return fields;
	}

	public void setFields(List<String> fields) {
		this.fields = fields;
	}

	public boolean isPageable() {
		return pageable;
	}

	public void setPageable(boolean pageable) {
		this.pageable = pageable;
	}

	public String getSecurityLevel() {
		return securityLevel;
	}

	public void setSecurityLevel(String securityLevel) {
		this.securityLevel = securityLevel;
	}

}
