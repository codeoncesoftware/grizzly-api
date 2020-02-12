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

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import io.swagger.models.properties.Property;
import io.swagger.models.properties.PropertyBuilder;

/**
 * io.swagger.models.properties.Property is an Interface, so the Object Mapper Can't determine with Class to use for Instantiation
 * This Deserializer is configured to use the PropertyBuilder to return a valid Property Object
 * @author rayen
 *
 */
public class PropertyDeserializer extends StdDeserializer<io.swagger.models.properties.Property> {

	private static final long serialVersionUID = 1L;

	public PropertyDeserializer() {
		this(null);
	}

	public PropertyDeserializer(Class<?> vc) {
		super(vc);
	}

	@Override
	public Property deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
		return PropertyBuilder.build("string", null, null);
	}
}
