/*-
 * ========================LICENSE_START=================================
 * grizzly-api-core-domain
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

package fr.codeonce.grizzly.core.domain.project;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("project")
public class Project {

	@Id
	private String id;

	@Indexed(name = "idx_project_name")
	private String name;
	
	@Indexed(name = "idx_project_username")
	private String username;
	
	private String description;		

	@CreatedDate
	private Date creationTime;

	@LastModifiedDate
	private Date lastUpdate;
	
	@Indexed
	private String dbsourceId;
	
	private String databaseName;
	
	private SecurityApiConfig securityConfig;
	
	private List<String> roles = new ArrayList<>() ;


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public Date getCreationTime() {
		return creationTime;
	}

	public Date getLastUpdate() {
		return lastUpdate;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}


	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getDbsourceId() {
		return dbsourceId;
	}

	public void setDbsourceId(String dbsourceId) {
		this.dbsourceId = dbsourceId;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	public SecurityApiConfig getSecurityConfig() {
		return securityConfig;
	}

	public void setSecurityConfig(SecurityApiConfig securityConfig) {
		this.securityConfig = securityConfig;
	}
	
	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}
	
}
