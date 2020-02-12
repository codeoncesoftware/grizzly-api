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

import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.codeonce.grizzly.core.domain.container.Container;
import fr.codeonce.grizzly.core.domain.container.hierarchy.ContainerHierarchy;
import fr.codeonce.grizzly.core.domain.container.hierarchy.ContainerHierarchyRepository;
import fr.codeonce.grizzly.core.domain.resource.Resource;
import fr.codeonce.grizzly.core.domain.resource.ResourceFile;
import fr.codeonce.grizzly.core.service.container.ContainerDto;
import fr.codeonce.grizzly.core.service.container.ContainerMapperService;
import fr.codeonce.grizzly.core.service.resource.ResourceDto;
import fr.codeonce.grizzly.core.service.resource.utils.ResourceGroupDto;
import fr.codeonce.grizzly.core.service.test.AbstractServiceTest;

public class ContainerMapperTest extends AbstractServiceTest {

	@Autowired
	private ContainerMapperService containerMapper;

	@Autowired
	private ContainerHierarchyRepository containerHierarchyRepository;

	private ContainerHierarchy containerHierarchy;
	private Resource resource;
	private ResourceDto resourceDto;

	@Before
	public void init() {
		// Container
		containerHierarchy = new ContainerHierarchy();
		containerHierarchy.setId("id");
		// Resource
		resource = new Resource();
		ResourceFile resourceFile = new ResourceFile();
		resourceFile.setFileId("123");
		resourceFile.setFileUri("uri");
		resource.setResourceFile(resourceFile);
		// ResourceDto
		resourceDto = new ResourceDto();
	}

	@Test
	public void testMapContainerDtoToDomain() {
		// INIT
		Container cont = new Container();
		cont.setHierarchyId("id");
		Optional<Container> opCont = Optional.of(cont);
		given(containerHierarchyRepository.findById("id")).willReturn(Optional.of(containerHierarchy));
		given(containerHierarchyRepository.save(new ContainerHierarchy(""))).willReturn(containerHierarchy);
		given(containerRepository.findById("idCont")).willReturn(opCont);
		// DATA
		ContainerDto containerDto = new ContainerDto();
		containerDto.setId("idCont");
		containerDto.setResourceGroups(Collections.singletonList(new ResourceGroupDto(null)));
		// TEST
		assertTrue(containerMapper.mapToDomain(containerDto) instanceof Container);

	}
	
	@Test
	public void testMapResourceToDto() {
		assertTrue(containerMapper.mapToDto(resource) instanceof ResourceDto);
	}
	
	@Test
	public void mapResourceToDomain() {
		assertTrue(containerMapper.mapToDomain(resourceDto) instanceof Resource);
	}

}
