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
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import fr.codeonce.grizzly.core.domain.container.Container;
import fr.codeonce.grizzly.core.service.util.GlobalExceptionUtil;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import springfox.documentation.spring.web.SpringfoxWebMvcConfiguration;
import springfox.documentation.spring.web.json.JsonSerializer;
import springfox.documentation.swagger2.configuration.Swagger2DocumentationConfiguration;

@Service
@Import(value = { SpringfoxWebMvcConfiguration.class, Swagger2DocumentationConfiguration.class })
public class SwaggerGenerator implements ISwaggerGenerator {

	private static final Logger log = LoggerFactory.getLogger(SwaggerGenerator.class);

	@Autowired
	private JsonSerializer jsonSerializer;

	@Autowired
	private SwaggerMapperService swaggerMapperService;

	@Autowired
	private MappingJackson2HttpMessageConverter springMvcJacksonConverter;

	@Transactional
	public String generate(Container container, String type) {
		return jsonSerializer.toJson(swaggerMapperService.mapToSwagger(container, type)).value();
	}

	public String swaggerToJson(Swagger swagger) {
		return jsonSerializer.toJson(swagger).value();
	}

	@Transactional
	public Container mapSwaggerToContainer(Swagger swagger, String projectId) throws Exception {
		return swaggerMapperService.mapToContainer(swagger, projectId);
		
	}

	/**
	 * Parse a MultipartFile and construct a Swagger Object
	 * 
	 * @param file
	 * @return a Swagger Object
	 */
	public Swagger getSwagger(MultipartFile file) {
		try {
			return getSwagger(file.getInputStream());
		} catch (IOException e) {
			GlobalExceptionUtil.fileNotFoundException(file.getOriginalFilename());
			return null;
		}
	}

	public Swagger getSwagger(InputStream inputStream) throws IOException {
		// Get File Content
		StringWriter writer = new StringWriter();
		IOUtils.copy(inputStream, writer, StandardCharsets.UTF_8);
		String filecontent = writer.toString();
		// Prepare the Swagger Object
		ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
		Object obj = yamlReader.readValue(filecontent, Object.class);
		ObjectMapper jsonWriter = new ObjectMapper();
		return new SwaggerParser()
				.read(springMvcJacksonConverter.getObjectMapper().readTree(jsonWriter.writeValueAsString(obj)));
	}

}
