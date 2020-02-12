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
package fr.codeonce.grizzly.core.domain.container.hierarchy;

import org.springframework.data.mongodb.core.mapping.Document;

@Document("hierarchy")
public class ContainerHierarchy {

	private String id;
	private String hierarchy;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getHierarchy() {
		return hierarchy;
	}

	public void setHierarchy(String hierarchy) {
		this.hierarchy = hierarchy;
	}
	
	public ContainerHierarchy() {}

	public ContainerHierarchy(String hierarchy) {
		this.hierarchy = hierarchy;
	}
}