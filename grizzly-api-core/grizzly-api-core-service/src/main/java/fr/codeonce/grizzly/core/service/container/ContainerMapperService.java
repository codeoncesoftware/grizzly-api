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
package fr.codeonce.grizzly.core.service.container;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.codeonce.grizzly.core.domain.container.Container;
import fr.codeonce.grizzly.core.domain.container.ContainerRepository;
import fr.codeonce.grizzly.core.domain.container.hierarchy.ContainerHierarchy;
import fr.codeonce.grizzly.core.domain.container.hierarchy.ContainerHierarchyRepository;
import fr.codeonce.grizzly.core.domain.resource.Resource;
import fr.codeonce.grizzly.core.service.resource.ResourceDto;

@Service
public class ContainerMapperService extends ContainerMapperImpl {

	@Autowired
	private ContainerHierarchyRepository hierarchyRepository;

	@Autowired
	private ContainerRepository containerRepository;

	@Override
	public ContainerDto mapToDto(Container container) {
		ContainerDto containerDTO = super.mapToDto(container);
		// CUSTOM MAPPING
		containerDTO.getResourceGroups()
				.forEach(rg -> rg.getResources().addAll(filterResourcesFromDomain(container, rg.getName())));
		if (!container.getHierarchyId().isEmpty()) {
			Optional<ContainerHierarchy> hierarchy = hierarchyRepository.findById(container.getHierarchyId());
			if (hierarchy.isPresent()) {
				containerDTO.setHierarchy(hierarchy.get().getHierarchy());
			}
		} else {
			containerDTO.setHierarchy("");
		}
		return containerDTO;
	}

	private List<ResourceDto> filterResourcesFromDomain(Container container, String name) {
		return container.getResources().stream().filter(r -> r.getResourceGroup().equals(name)).map(this::mapToDto)
				.collect(Collectors.toList());
	}

	@Override
	public Container mapToDomain(ContainerDto containerDto) {
		Container container = super.mapToDomain(containerDto);
		// CUSTOM MAPPING
		containerDto.getResourceGroups().stream()
				.forEach(rg -> container.getResources().addAll(rg.getResources().stream().map(res -> {
					Resource resource = mapToDomain(res);
					resource.setResourceGroup(rg.getName());
					return resource;
				}).collect(Collectors.toList())));
		Optional<Container> oldContainer = Optional.of(container);
		if (containerDto.getId() != null) {
			oldContainer = this.containerRepository.findById(containerDto.getId());
		}
		if (oldContainer.isPresent()) {
			Optional<ContainerHierarchy> hierarchy = Optional.of(new ContainerHierarchy());
			if (oldContainer.get().getHierarchyId() != null) {
				hierarchy = this.hierarchyRepository.findById(oldContainer.get().getHierarchyId());
			}
			if (hierarchy.isPresent()) {
				container.setHierarchyId(hierarchy.get().getId());
			} else {
				container.setHierarchyId(
						this.hierarchyRepository.save(new ContainerHierarchy(containerDto.getHierarchy())).getId());
			}
		}
		return container;
	}



}
