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

import java.util.ArrayList;
import java.util.List;

public class RuntimeRequest<T> {

	private T body;

	private List<RuntimeRequestParam<?>> params = new ArrayList<>();
	
	private String containerId;
	
	private String executablePath;
	
	private String executionType;
	
	private List<String> secondaryFilePaths = new ArrayList<>();

	
	public T getBody() {
		return body;
	}

	public void setBody(T body) {
		this.body = body;
	}

	public List<RuntimeRequestParam<?>> getParams() {
		return params;
	}

	public void setParams(List<RuntimeRequestParam<?>> params) {
		this.params = params;
	}

	public String getContainerId() {
		return containerId;
	}

	public void setContainerId(String containerId) {
		this.containerId = containerId;
	}

	public String getExecutablePath() {
		return executablePath;
	}

	public void setExecutablePath(String executablePath) {
		this.executablePath = executablePath;
	}

	public String getExecutionType() {
		return executionType;
	}

	public void setExecutionType(String executionType) {
		this.executionType = executionType;
	}

	public List<String> getSecondaryFilePaths() {
		return secondaryFilePaths;
	}

	public void setSecondaryFilePaths(List<String> secondaryFilePaths) {
		this.secondaryFilePaths = secondaryFilePaths;
	}

}
