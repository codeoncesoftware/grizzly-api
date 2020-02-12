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
package fr.codeonce.grizzly.common.runtime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import fr.codeonce.grizzly.common.runtime.resource.RuntimeResourceParameter;

public class RuntimeQueryRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String httpMethod;
	private String path;
	private String executionType;
	private String queryType;
	private String query;
	private String dbsourceConnectionMode;
	private String dbsourceId;
	private String connectionMode;
	private String physicalDatabaseName;
	private String databaseName;
	private String collectionName;
	private String returnType;
	private List<String> fields;
	private List<RuntimeResourceParameter> parameters;
	private boolean pageable;
	private boolean many;
	private List<String> securityLevel = new ArrayList<>();
	private String username;

	public RuntimeQueryRequest() {
		this.fields = new ArrayList<>();
	}

	public String getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getExecutionType() {
		return executionType;
	}

	public void setExecutionType(String executionType) {
		this.executionType = executionType;
	}

	public String getQueryType() {
		return queryType;
	}

	public void setQueryType(String queryType) {
		this.queryType = queryType;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getDbsourceConnectionMode() {
		return dbsourceConnectionMode;
	}

	public void setDbsourceConnectionMode(String dbsourceConnectionMode) {
		this.dbsourceConnectionMode = dbsourceConnectionMode;
	}

	public String getDbsourceId() {
		return dbsourceId;
	}

	public void setDbsourceId(String dbsourceId) {
		this.dbsourceId = dbsourceId;
	}

	public String getConnectionMode() {
		return connectionMode;
	}

	public void setConnectionMode(String connectionMode) {
		this.connectionMode = connectionMode;
	}

	public String getPhysicalDatabaseName() {
		return physicalDatabaseName;
	}

	public void setPhysicalDatabaseName(String physicalDatabaseName) {
		this.physicalDatabaseName = physicalDatabaseName;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	public String getCollectionName() {
		return collectionName;
	}

	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
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
	
	public List<RuntimeResourceParameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<RuntimeResourceParameter> parameters) {
		this.parameters = parameters;
	}

	public boolean isPageable() {
		return pageable;
	}

	public void setPageable(boolean pageable) {
		this.pageable = pageable;
	}

	public boolean isMany() {
		return many;
	}

	public void setMany(boolean many) {
		this.many = many;
	}

	public List<String> getSecurityLevel() {
		return securityLevel;
	}

	public void setSecurityLevel(List<String> securityLevel) {
		this.securityLevel = securityLevel;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

}
