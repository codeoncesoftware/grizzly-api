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
package fr.codeonce.grizzly.core.service.project;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import fr.codeonce.grizzly.core.domain.project.Project;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

	ProjectDto mapToDto(Project entity);

	Project mapToDomain(ProjectDto dto);

	void mapToDomainUpdate(ProjectDto dto, @MappingTarget Project entity);

}
