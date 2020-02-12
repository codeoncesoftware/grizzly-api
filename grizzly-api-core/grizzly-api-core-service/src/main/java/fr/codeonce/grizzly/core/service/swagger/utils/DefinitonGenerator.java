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
package fr.codeonce.grizzly.core.service.swagger.utils;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.PropertyBuilder;

@Service
public class DefinitonGenerator implements IDefinitionGenerator {

	public Model getSignUp() {
		Model user = new ModelImpl();
		Map<String, Property> properties = new LinkedHashMap<>();
		properties.put("firstname", PropertyBuilder.build("string", null, null));
		properties.put("lastname", PropertyBuilder.build("string", null, null));
		properties.put("username", PropertyBuilder.build("string", null, null));
		properties.put("password", PropertyBuilder.build("string", null, null));
		properties.put("email", PropertyBuilder.build("string", null, null));
		properties.put("phone", PropertyBuilder.build("number", null, null));
		user.setProperties(properties);
		return user;
	}

	public Model getSignIn() {
		Model user = new ModelImpl();
		Map<String, Property> properties = new LinkedHashMap<>();
		properties.put("username", PropertyBuilder.build("string", null, null));
		properties.put("password", PropertyBuilder.build("string", null, null));
		user.setProperties(properties);
		return user;
	}

}
