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
package fr.codeonce.grizzly.core.domain.enums;

public enum PredefinedQuery {
	
	FIND_BY_USERNAME("{\"username\":\"%username\"}"),
	FIND_BY_SESSION_USERNAME("{\"username\":\"$session_username\"}");
	
	 private String value;

	private PredefinedQuery(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}

}
