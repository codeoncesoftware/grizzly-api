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
package fr.codeonce.grizzly.core.service.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ResourceUtils;

import fr.codeonce.grizzly.core.domain.resource.ResourceParameter;
import fr.codeonce.grizzly.core.service.swagger.SwaggerGenerator;
import fr.codeonce.grizzly.core.service.swagger.utils.ParameterFactory;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.FormParameter;
import io.swagger.models.parameters.HeaderParameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;

public class SwaggerTest extends AbstractServiceTest {

	@Autowired
	private SwaggerGenerator swaggerGenerator;

	@Test
	public void testParameterFactory() {
		ResourceParameter resourceParameter = new ResourceParameter();
		resourceParameter.setName("testParameter");
		resourceParameter.setType("testType");
		resourceParameter.setValue("0");
		resourceParameter.setIn("Body");
		ParameterFactory factory = ParameterFactory.getInstance();
		assertTrue(factory.makeParameter(resourceParameter) instanceof BodyParameter);
		resourceParameter.setIn("Header");
		assertTrue(factory.makeParameter(resourceParameter) instanceof HeaderParameter);
		resourceParameter.setIn("formData");
		assertTrue(factory.makeParameter(resourceParameter) instanceof FormParameter);
		resourceParameter.setIn("Query");
		assertTrue(factory.makeParameter(resourceParameter) instanceof QueryParameter);
		resourceParameter.setIn("should return path Parameter");
		assertTrue(factory.makeParameter(resourceParameter) instanceof PathParameter);
	}

	@Test
	public void testGetSwaggerFromFile() throws IOException {

		File swaggerFile = ResourceUtils.getFile("classpath:swagger" + File.separator + "swagger.json");

		InputStream is = FileUtils.openInputStream(swaggerFile);
		// Verify Swagger Object
		Swagger swagger = swaggerGenerator.getSwagger(is);
		Assert.assertNotNull("swagger object should not be null", swagger);

		// Verify Swagger Content
		String filecontent = new String(Files.readAllBytes(Paths.get(swaggerFile.getPath()))).replaceAll("\\s+", "");
		String swaggerJson = swaggerGenerator.swaggerToJson(swagger);
		Assert.assertTrue("method should return same file content",
				filecontent.equalsIgnoreCase(swaggerJson.replaceAll("\\s+", "")));
	}

	// @Test
	public void testGetSwaggerWithSchema() throws IOException {

		File swaggerFile = ResourceUtils.getFile("classpath:swagger" + File.separator + "swaggerWithSchema.json");

		InputStream is = FileUtils.openInputStream(swaggerFile);
		// Verify Swagger Object
		Swagger swagger = swaggerGenerator.getSwagger(is);
		Assert.assertNotNull("swagger object should not be null", swagger);

		String swaggerJson = swaggerGenerator.swaggerToJson(swagger);
		System.out.println(swaggerJson);

		// Verify Swagger Content
		String filecontent = new String(Files.readAllBytes(Paths.get(swaggerFile.getPath())));
		Assert.assertTrue("method should return same file content", filecontent.equals(swaggerJson));
	}

}
