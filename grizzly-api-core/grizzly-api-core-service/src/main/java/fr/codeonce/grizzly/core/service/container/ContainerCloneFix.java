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

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;

import fr.codeonce.grizzly.core.domain.container.Container;
import fr.codeonce.grizzly.core.domain.container.ContainerRepository;
import fr.codeonce.grizzly.core.domain.container.hierarchy.ContainerHierarchyRepository;
import fr.codeonce.grizzly.core.service.datasource.cache.ConnectionCacheService;
import fr.codeonce.grizzly.core.service.util.GlobalExceptionUtil;

public class ContainerCloneFix {

	private static final Logger log = LoggerFactory.getLogger(ContainerCloneService.class);

	@Autowired
	private ContainerRepository containerRepository;

	@Autowired
	private ContainerMapperService containerMapper;

	@Autowired
	private ContainerImportService containerImportService;

	@Autowired
	private ContainerExportService containerExportService;

	@Autowired
	private ContainerHierarchyRepository hierarchyRepository;

	@Autowired
	private ConnectionCacheService cacheService;

	public ContainerDto cloneContainer(String currentContainerId, String newContainerName)
			throws IOException, JSONException {

		Container currentContainer = containerRepository.findById(currentContainerId)
				.orElseThrow(GlobalExceptionUtil.notFoundException(Container.class, currentContainerId));

		File exportedContainer = containerExportService.export(currentContainerId);

//		MultipartFile multipartFile = new MockMultipartFile("exportedContainer.zip", exportedContainer.getName(), "text/plain",
//				IOUtils.toByteArray(input));
//		containerImportService.importContainer(multipartFile, newContainerName, currentContainer.getProjectId(),
//				currentContainer.getDbsourceId(), currentContainer.getDatabaseName());

		return null;
	}

}
