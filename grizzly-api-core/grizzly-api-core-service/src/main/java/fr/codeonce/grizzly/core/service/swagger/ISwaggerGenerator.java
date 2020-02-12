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
package fr.codeonce.grizzly.core.service.swagger;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

import fr.codeonce.grizzly.core.domain.container.Container;
import io.swagger.models.Swagger;

public interface ISwaggerGenerator {

	public String generate(Container container, String type);

	public String swaggerToJson(Swagger swagger);

	public Container mapSwaggerToContainer(Swagger swagger, String projectId) throws Exception;

	/**
	 * Parse a MultipartFile and construct a Swagger Object
	 * 
	 * @param file
	 * @return a Swagger Object
	 */
	public Swagger getSwagger(MultipartFile file);

	public Swagger getSwagger(InputStream inputStream) throws IOException;

	
}
