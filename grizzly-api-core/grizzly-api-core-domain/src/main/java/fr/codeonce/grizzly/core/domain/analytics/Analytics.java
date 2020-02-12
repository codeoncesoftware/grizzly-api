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
package fr.codeonce.grizzly.core.domain.analytics;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Analytics {

	@Id
	private String id;

	@Indexed(name="idx_analytics_username")
	private String username;

	@Indexed(name="idx_analytics_container")
	private String containerId;
	
	private long requestCount;

	private List<ApiCount> apiCounts = new ArrayList<>();

	public String getContainerId() {
		return containerId;
	}

	public String getUsername() {
		return username;
	}

	public String getId() {
		return id;
	}

	public void setContainerId(String containerId) {
		this.containerId = containerId;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<ApiCount> getApiCounts() {
		return apiCounts;
	}

	public void setApiCounts(List<ApiCount> apiCounts) {
		this.apiCounts = apiCounts;
	}

	public long getRequestCount() {
		return requestCount;
	}

	public void setRequestCount(long requestCount) {
		this.requestCount = requestCount;
	}

}
