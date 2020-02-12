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

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import fr.codeonce.grizzly.core.domain.container.Container;
import fr.codeonce.grizzly.core.domain.resource.Resource;
import fr.codeonce.grizzly.core.domain.resource.ResourceGroup;
import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SwaggerMapper {

	@Mapping(source = "container.name", target = "info.version")
	Swagger mapToSwagger(Container container);

	Container mapToContainer(Swagger swagger) throws Exception;

	@Mapping(source = "resourceGroup.name", target = "name")
	@Mapping(source = "resourceGroup.description", target = "description")
	Tag mapToTag(ResourceGroup resourceGroup);

	@Mapping(ignore = true, target = "parameters")
	@Mapping(ignore = true, target = "responses")
	Operation mapToOperation(Resource resource);

	@Mapping(ignore = true, target = "responses")
	Resource mapToResource(Operation operation);

}
