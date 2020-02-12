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
import java.util.Date;
import java.util.List;

import fr.codeonce.grizzly.core.service.resource.utils.ResourceGroupDto;

public class ContainerDto {

	private String id;

	private String name;
	
	private String description;

	private Date creationTime;

	private Date lastUpdate;

	private String projectId;

	private List<ResourceGroupDto> resourceGroups = new ArrayList<>();

	private boolean enabled;

	private String hierarchy = "";
	
	private String swaggerUuid;

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

	public Date getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}

	public Date getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getHierarchy() {
		return hierarchy;
	}

	public void setHierarchy(String hierarchy) {
		this.hierarchy = hierarchy;
	}

	public List<ResourceGroupDto> getResourceGroups() {
		return resourceGroups;
	}

	public void setResourceGroups(List<ResourceGroupDto> resourceGroups) {
		this.resourceGroups = resourceGroups;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSwaggerUuid() {
		return swaggerUuid;
	}

	public void setSwaggerUuid(String swaggerUuid) {
		this.swaggerUuid = swaggerUuid;
	}
	
}
