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
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import fr.codeonce.grizzly.core.domain.container.Container;
import fr.codeonce.grizzly.core.domain.container.hierarchy.ContainerHierarchy;
import fr.codeonce.grizzly.core.domain.resource.Resource;
import fr.codeonce.grizzly.core.domain.resource.ResourceParameter;
import fr.codeonce.grizzly.core.service.container.hierarchy.ContainerHierarchyDto;
import fr.codeonce.grizzly.core.service.resource.ResourceDto;
import fr.codeonce.grizzly.core.service.resource.utils.ResourceGroupDto;
import fr.codeonce.grizzly.core.service.resource.utils.ResourceParameterDto;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
interface ContainerMapper {

	ContainerDto mapToDto(Container container);

	Container mapToDomain(ContainerDto containerDto);

	default List<String> toResourceGroup(List<ResourceGroupDto> resourceGroupDtoList) {
		return resourceGroupDtoList.stream().map(ResourceGroupDto::getName).collect(Collectors.toList());
	}


	ResourceDto mapToDto(Resource resource);

	Resource mapToDomain(ResourceDto dto);

	void mapToDomain(Resource dto, @MappingTarget Resource entity);

	ResourceParameterDto mapToDto(ResourceParameter entity);

	ResourceParameter mapToDomain(ResourceParameterDto dto);

	void mapToDomain(ResourceParameterDto dto, @MappingTarget ResourceParameter entity);

	ContainerHierarchy mapToDto(ContainerHierarchyDto dto);

	ContainerHierarchyDto mapToDomain(ContainerHierarchy entity);

}
