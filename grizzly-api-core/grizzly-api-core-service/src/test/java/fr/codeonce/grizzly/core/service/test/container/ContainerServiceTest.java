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
package fr.codeonce.grizzly.core.service.test.container;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import fr.codeonce.grizzly.core.domain.container.Container;
import fr.codeonce.grizzly.core.domain.container.hierarchy.ContainerHierarchy;
import fr.codeonce.grizzly.core.domain.project.Project;
import fr.codeonce.grizzly.core.service.container.ContainerDto;
import fr.codeonce.grizzly.core.service.container.ContainerService;
import fr.codeonce.grizzly.core.service.datasource.cache.ConnectionCacheService;
import fr.codeonce.grizzly.core.service.project.ProjectDto;
import fr.codeonce.grizzly.core.service.test.AbstractServiceTest;

public class ContainerServiceTest extends AbstractServiceTest {

	@Autowired
	private ContainerService containerService;
	
	@MockBean
	private ConnectionCacheService connectionCacheService;

	private static final String MY_ID = "12345";

	private static final String MY_CONTAINER_NAME = "myContainer";


	private List<Container> testContainers = new ArrayList<>();
	private Container testContainer = new Container();
	private ContainerDto testContainerDto = new ContainerDto();
	private Project testProject = new Project();
	private ProjectDto testProjectDto = new ProjectDto();
	private ContainerHierarchy testContainerhierarchy = new ContainerHierarchy();
	
	@Before
	public void init() {

		testProject = new Project();
		testProject.setId("123");
		testProject.setName("heyhey");

		testProjectDto = new ProjectDto();
		testProjectDto.setId("123");
		testProjectDto.setName("heyhey");

		testContainers = new ArrayList<Container>();
		testContainer = new Container();
		testContainer.setId(MY_ID);
		testContainer.setName(MY_CONTAINER_NAME);
		testContainer.setProjectId(testProject.getId());
		testContainer.setHierarchyId(MY_ID);
		testContainers.add(testContainer);

		testContainerDto = new ContainerDto();
		testContainerDto.setId(MY_ID);
		testContainerDto.setName(MY_CONTAINER_NAME);
		testContainerDto.setProjectId(testProjectDto.getId());

	}

	// @Test
	public void testGetAllContainers() {

		// Call
		given(containerRepository.findAll()).willReturn(testContainers);

		// Test
		List<ContainerDto> allContainers = containerService.getAll();
		assertFalse(allContainers.isEmpty());
		assertEquals(MY_CONTAINER_NAME, testContainer.getName());
	}

	// @Test
	public void testGetContainer() {
		// Call
		given(containerRepository.findById(MY_ID)).willReturn(Optional.of(testContainer));

		// Test

		ContainerDto myContainerDto = containerService.get(MY_ID);
		Assert.assertFalse(myContainerDto == null);
		assertEquals(MY_CONTAINER_NAME, myContainerDto.getName());
	}

	@Test(expected = NoSuchElementException.class)
	public void testGetNotFoundContainer() {

		// Mock
		given(containerRepository.findById(MY_ID)).willReturn(Optional.empty());

		// Test
		ContainerDto c = containerService.get(MY_ID);
		Assert.assertTrue(c == null);

	}

	// @Test
	public void testCreateContainerWithProject() {

		// Call
		given(projectRepository.findById(testProject.getId())).willReturn(Optional.of(testProject));
		given(containerRepository.save(any())).willReturn(testContainer);

		// Test

		ContainerDto myCreatedContainerDto = containerService.saveContainer(testContainerDto);
		assertFalse(myCreatedContainerDto == null);
		assertEquals(MY_CONTAINER_NAME, myCreatedContainerDto.getName());

	}

	@Test(expected = NoSuchElementException.class)
	public void testCreateContainerWithoutProject() {

		// Call
		given(projectRepository.findById(testProject.getId())).willReturn(Optional.empty());

		// Test
		ContainerDto myCreatedContainerDto = containerService.saveContainer(testContainerDto);
		assertTrue(myCreatedContainerDto == null);

	}

	@Test
	public void testDeleteExisitingContainer() {
		// Mock
		given(containerRepository.findById(MY_ID)).willReturn(Optional.of(testContainer));
		given(containerHierarchyRepository.findById(MY_ID)).willReturn(Optional.of(testContainerhierarchy));
		given(connectionCacheService.getGridFs(any(),any())).willReturn(gridFsTemplate);
		// Call
		containerService.delete(MY_ID);
		Mockito.verify(containerRepository, times(1)).delete(testContainer);
	}

	@Test(expected = NoSuchElementException.class)
	public void testDeleteNotFoundContainer() {

		// Mock
		given(containerRepository.findById(MY_ID)).willReturn(Optional.empty());

		// Test
		containerService.delete(MY_ID);

	}

	@Test
	public void testDeleteAllContainers() {

		// Call
		containerService.deleteAll();
		// Verify
		Mockito.verify(containerRepository, times(1)).deleteAll();
	}

	// @Test
	public void testExistsContainerName() {
		// Mock
		given(containerRepository.findAllByProjectId(testProject.getId())).willReturn(testContainers);

		// Test
		boolean containerNameTaken = containerService.existsContainerName(testContainerDto);

		assertTrue(containerNameTaken);

	}

	// @Test
	public void testContainersByProject() {
		// Mock
		given(containerRepository.findAllByProjectId(testProject.getId())).willReturn(testContainers);

		// Test
		List<ContainerDto> listContainers = containerService.containersByProject(testProject.getId());
		assertFalse(listContainers.isEmpty());

	}
	
	@Test
	public void testDeleteAllByProject() {
		containerService.deleteAllByProject(MY_ID);
		Mockito.verify(containerRepository, times(1)).deleteContainerByProjectId(MY_ID);
	}
	
//	@Test
//	public void importContainerTest() throws IOException {
//		File zipFile = ResourceUtils.getFile("classpath:fs/xsl.zip");
//		String projectId = "5ce2955a3f4434b488096325";
//		String containerName = "hello_world";
//		String x = containerService.importContainer(zipFile, containerName, projectId);
//		Assert.assertEquals(projectId,x);
//	}

}
