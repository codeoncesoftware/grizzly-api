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
package fr.codeonce.grizzly.core.service.fs.model;

import java.util.ArrayList;
import java.util.List;

public class CustomFolder {

	private String name;
	private String type = "folder";
	private List<Object> children;

	public CustomFolder() { }
	public CustomFolder(String name) {
		children = new ArrayList<>();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Object> getChildren() {
		return children;
	}

	public void addFolder(Object folder) {
		this.children.add(folder);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setChildren(List<Object> children) {
		this.children = children;
	}
	
}
