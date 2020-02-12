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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;

import fr.codeonce.grizzly.core.domain.container.Container;
import fr.codeonce.grizzly.core.domain.container.hierarchy.ContainerHierarchy;
import fr.codeonce.grizzly.core.domain.project.Project;
import fr.codeonce.grizzly.core.service.container.ContainerSwaggerService;
import fr.codeonce.grizzly.core.service.swagger.utils.DefinitonGenerator;
import fr.codeonce.grizzly.core.service.swagger.utils.IDefinitionGenerator;
import io.swagger.models.ModelImpl;

public class ContainerSwaggerServiceTest extends AbstractServiceTest {

	@Autowired
	private ContainerSwaggerService containerSwaggerService;

	@Spy
	@InjectMocks
	private IDefinitionGenerator definitionGenerator = new DefinitonGenerator();

	Container container;
	ContainerHierarchy hierarchy;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		container = new Container();
		hierarchy = new ContainerHierarchy();
		container.setId("containerID");
		container.setHierarchyId("containerHierarchyID");
		given(containerRepository.findById("containerID")).willReturn(Optional.of(container));
		given(containerHierarchyRepository.findById("containerHierarchyID")).willReturn(Optional.of(hierarchy));
		given(projectRepository.findById(Mockito.any())).willReturn(Optional.of(new Project()));

	}

	@Test
	public void testGenerateSwagger() {
		given(containerRepository.findById("containerID")).willReturn(Optional.of(container));
		given(containerHierarchyRepository.findById("containerHierarchyID")).willReturn(Optional.of(hierarchy));
		when(definitionGenerator.getSignUp()).thenReturn(new ModelImpl());
		when(definitionGenerator.getSignIn()).thenReturn(new ModelImpl());
		try {
			File file = containerSwaggerService.generateSwagger("dev", "containerID");
			String fileContent = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
			FileUtils.forceDelete(file);
			assertEquals(
					"{\"swagger\":\"2.0\",\"info\":{\"description\":\"Container description\"},\"host\":\"localhost:4200\",\"basePath\":\"/runtime/containerID\",\"schemes\":[\"https\",\"http\"],\"securityDefinitions\":{\"api_key\":{\"description\":\"Standard Authorization header using the Bearer scheme. \\n\\n Value example: \\\"Bearer {token}\\\"\",\"type\":\"apiKey\",\"name\":\"Authorization\",\"in\":\"header\"}},\"definitions\":{\"signIn\":{\"properties\":{\"username\":{\"type\":\"string\"},\"password\":{\"type\":\"string\"}}},\"signUp\":{\"properties\":{\"firstname\":{\"type\":\"string\"},\"lastname\":{\"type\":\"string\"},\"username\":{\"type\":\"string\"},\"password\":{\"type\":\"string\"},\"email\":{\"type\":\"string\"},\"phone\":{\"type\":\"number\"}}}}}",
					fileContent);
		} catch (IOException e) {
			fail();
		}
	}

	@Test
	public void testDownloadSwaggerFile() {
		MockHttpServletResponse response = new MockHttpServletResponse();
		try {
			containerSwaggerService.downloadSwaggerJsonFile(response, "dev", "containerID");
			assertEquals(
					"{\"swagger\":\"2.0\",\"info\":{\"description\":\"Container description\"},\"host\":\"localhost:4200\",\"basePath\":\"/runtime/containerID\",\"schemes\":[\"https\",\"http\"],\"securityDefinitions\":{\"api_key\":{\"description\":\"Standard Authorization header using the Bearer scheme. \\n\\n Value example: \\\"Bearer {token}\\\"\",\"type\":\"apiKey\",\"name\":\"Authorization\",\"in\":\"header\"}},\"definitions\":{\"signIn\":{\"properties\":{\"username\":{\"type\":\"string\"},\"password\":{\"type\":\"string\"}}},\"signUp\":{\"properties\":{\"firstname\":{\"type\":\"string\"},\"lastname\":{\"type\":\"string\"},\"username\":{\"type\":\"string\"},\"password\":{\"type\":\"string\"},\"email\":{\"type\":\"string\"},\"phone\":{\"type\":\"number\"}}}}}",
					response.getContentAsString());
		} catch (IOException e) {
			fail();
		}
	}

}
