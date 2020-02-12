/*-
 * ========================LICENSE_START=================================
 * grizzly-api-common
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
package fr.codeonce.grizzly.common.runtime.resource;

import java.util.ArrayList;
import java.util.List;

public class RuntimeResource {

	private String name;
	private String path;
	private String httpMethod;
	private String executionType;
	private RuntimeResourceFile resourceFile;
	private List<RuntimeResourceFile> secondaryFilePaths;
	private RuntimeCustomQuery customQuery;
	private List<RuntimeResourceParameter> parameters;
	private List<RuntimeAPIResponse> responses;
	private ArrayList<String> securityLevel = new ArrayList<>();
	private List<String> fields;
	private String returnType;
	private boolean pageable;
	private String securityKey;

	public RuntimeResource() {
		customQuery = new RuntimeCustomQuery();
		parameters = new ArrayList<>();
		responses = new ArrayList<>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}

	public String getExecutionType() {
		return executionType;
	}

	public void setExecutionType(String executionType) {
		this.executionType = executionType;
	}

	public RuntimeResourceFile getResourceFile() {
		return resourceFile;
	}

	public void setResourceFile(RuntimeResourceFile resourceFile) {
		this.resourceFile = resourceFile;
	}

	public List<RuntimeResourceFile> getSecondaryFilePaths() {
		return secondaryFilePaths;
	}

	public void setSecondaryFilePaths(List<RuntimeResourceFile> secondaryFilePaths) {
		this.secondaryFilePaths = secondaryFilePaths;
	}

	public RuntimeCustomQuery getCustomQuery() {
		return customQuery;
	}

	public void setCustomQuery(RuntimeCustomQuery customQuery) {
		this.customQuery = customQuery;
	}

	public List<RuntimeResourceParameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<RuntimeResourceParameter> parameters) {
		this.parameters = parameters;
	}

	public List<RuntimeAPIResponse> getResponses() {
		return responses;
	}

	public void setResponses(List<RuntimeAPIResponse> responses) {
		this.responses = responses;
	}

	public String getReturnType() {
		return returnType;
	}

	public void setReturnType(String returnType) {
		this.returnType = returnType;
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

	public String getSecurityKey() {
		return securityKey;
	}

	public void setSecurityKey(String securityKey) {
		this.securityKey = securityKey;
	}

	public List<String> getSecurityLevel() {
		return securityLevel;
	}

	public void setSecurityLevel(List<String> securityLevel) {
		this.securityLevel = (ArrayList<String>) securityLevel;
	}

	

}
