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

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mongodb.client.gridfs.model.GridFSFile;

import fr.codeonce.grizzly.core.domain.container.Container;
import fr.codeonce.grizzly.core.domain.container.ContainerRepository;
import fr.codeonce.grizzly.core.domain.container.hierarchy.ContainerHierarchy;
import fr.codeonce.grizzly.core.domain.container.hierarchy.ContainerHierarchyRepository;
import fr.codeonce.grizzly.core.domain.resource.Resource;
import fr.codeonce.grizzly.core.service.datasource.cache.ConnectionCacheService;
import fr.codeonce.grizzly.core.service.fs.model.CustomFile;
import fr.codeonce.grizzly.core.service.fs.model.CustomFolder;
import fr.codeonce.grizzly.core.service.util.GlobalExceptionUtil;
import fr.codeonce.grizzly.core.service.util.JsonUtil;

@Service
@Transactional
public class ContainerCloneService {

	private static final Logger log = LoggerFactory.getLogger(ContainerCloneService.class);

	@Autowired
	private ContainerRepository containerRepository;

	@Autowired
	private ContainerMapperService containerMapper;

	@Autowired
	private ContainerHierarchyRepository hierarchyRepository;

	@Autowired
	private ConnectionCacheService cacheService;

	public ContainerDto cloneContainer(String currentContainerId, String newContainerName)
			throws IOException, JSONException {

		Container currentContainer = containerRepository.findById(currentContainerId)
				.orElseThrow(GlobalExceptionUtil.notFoundException(Container.class, currentContainerId));

		// preparing new container (use mapper better)
		Container newContainer = new Container();
		newContainer.setProjectId(currentContainer.getProjectId());
		newContainer.setName(newContainerName);
		newContainer.setResourceGroups(currentContainer.getResourceGroups());
		newContainer.setResources(currentContainer.getResources());
		newContainer.setDbsourceId(currentContainer.getDbsourceId());
		newContainer.setDatabaseName(currentContainer.getDatabaseName());

		// hierarchy
		String hierarchyId = currentContainer.getHierarchyId();
		String hierarchy = "";
		if(!StringUtils.isEmpty(hierarchyId)) {
			hierarchy = hierarchyRepository.findById(hierarchyId)
					.orElseThrow(GlobalExceptionUtil.notFoundException(ContainerHierarchy.class, hierarchyId))
					.getHierarchy();
		}

		// Save Cloned Container and get new ID
		Container savedContainer = containerRepository.save(newContainer);
		String newContainerId = savedContainer.getId();

		if (!StringUtils.isEmpty(hierarchy)) {
			// copying hierarchy with new id
			ContainerHierarchy h = new ContainerHierarchy();
			h.setHierarchy(hierarchy);

			// get saved hierarchy new ID
			ContainerHierarchy newHierarchy = hierarchyRepository.save(h);
			String newHierarchyId = newHierarchy.getId();
			savedContainer.setHierarchyId(newHierarchyId);
			//update new container with new hierarchyId
			containerRepository.save(savedContainer);
			
			hierarchy = hierarchy.replaceAll(currentContainerId, newContainerId);
			ContainerHierarchy oldHierarchy = hierarchyRepository.findById(newHierarchyId)
					.orElseThrow(GlobalExceptionUtil.notFoundException(ContainerHierarchy.class, hierarchyId));
			oldHierarchy.setHierarchy(hierarchy);
			hierarchyRepository.save(oldHierarchy);
			String jsonString = new JSONObject(hierarchy).toString();
			CustomFolder ccFolder = JsonUtil.readValue(jsonString, CustomFolder.class);
			saveClone(ccFolder, newContainerId);
		}
		
		Container c = containerRepository.findById(newContainerId)
				.orElseThrow(GlobalExceptionUtil.notFoundException(Container.class, newContainerId));

		return containerMapper.mapToDto(c);

	}

	@SuppressWarnings("unchecked")
	public void saveClone(CustomFolder mylist, String idContainer) {
		this.containerRepository.findById(idContainer).ifPresent(container -> {
			GridFsTemplate gridFsTemplate = cacheService.getGridFs(
					this.cacheService.getMongoClient(container.getDbsourceId()), container.getDatabaseName());
			mylist.getChildren().forEach(item -> {
				LinkedHashMap<String, String> i = (LinkedHashMap<String, String>) item;
				if (i.get("type").equals("file")) {
					String jsonString = new JSONObject(i).toString();
					try {
						CustomFile ccFile = JsonUtil.readValue(jsonString, CustomFile.class);
						InputStream inputStream;
						Document metaData;
						String fileUri;
						String oldFileId = ccFile.getFileId();
						GridFSFile file = gridFsTemplate.findOne(Query.query((Criteria.where("_id").is(oldFileId))));
						inputStream = gridFsTemplate.getResource(file.getFilename()).getInputStream();
						metaData = new Document();
						metaData.put("containerId", idContainer);
						fileUri = file.getMetadata().getString("fileUri");
						metaData.put("fileUri", fileUri);
						ccFile.setFileId(gridFsTemplate.store(inputStream, file.getFilename(), metaData).toString());
						String newFileId = ccFile.getFileId();

						// replace the old id by the new one
						Container c = containerRepository.findById(idContainer).get();

						List<Resource> listResources = c.getResources().stream().map(x -> {
							// Verify if the file is a primary resource

							String fileId = x.getResourceFile().getFileId();

							if (fileId.equals(oldFileId)) {
								x.getResourceFile().setFileId(newFileId);
							}

							// Verify if the file is a secondary resource
							x.getSecondaryFilePaths().stream().map(y -> {
								if (y.getFileId().equals(oldFileId)) {
									y.setFileId(newFileId);
								}
								return y;
							});
							return x;

						}).collect(Collectors.toList());

						c.setResources(listResources);
						containerRepository.save(c);

						inputStream.close();
					} catch (Exception e) {
						log.debug("{}", e);
					}
				} else {
					String jsonString = new JSONObject(i).toString();
					CustomFolder ccFolder;
					try {
						ccFolder = JsonUtil.readValue(jsonString, CustomFolder.class);
						saveClone(ccFolder, idContainer);
					} catch (IOException e) {
						log.debug("{}", e);
					}
				}
			});
		});

	}

}
