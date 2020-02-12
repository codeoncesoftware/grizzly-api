/*-
 * ========================LICENSE_START=================================
 * grizzly-api-runtime
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
package fr.codeonce.grizzly.runtime.service.resolver;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import com.mongodb.MongoClient;
import com.mongodb.client.gridfs.model.GridFSFile;

import fr.codeonce.grizzly.core.domain.container.Container;
import fr.codeonce.grizzly.core.domain.container.ContainerRepository;
import fr.codeonce.grizzly.core.service.datasource.cache.ConnectionCacheService;
import fr.codeonce.grizzly.core.service.util.GlobalExceptionUtil;
import fr.codeonce.grizzly.runtime.service.ContainerContextHolder;

@Service
public class MongoResolverService implements ICloudResolverService {

	@Autowired
	private ConnectionCacheService connectionCacheService;
	
	@Autowired 
	private ContainerRepository containerRepository;

	@Override
	public InputStream getInputStream(String path) throws IOException {
		String containerId = ContainerContextHolder.getContext();
		 
		try {
			Container c =  containerRepository.findById(containerId).orElseThrow(GlobalExceptionUtil.notFoundException(Container.class, containerId));   
					
				MongoClient mongoClient = connectionCacheService.getMongoClient(c.getDbsourceId());
				GridFsTemplate gridFsTemplate = this.connectionCacheService.getGridFs(mongoClient, c.getDatabaseName());
				GridFSFile file = gridFsTemplate.findOne(Query.query(
						(Criteria.where("metadata.fileUri").is(path).and("metadata.containerId").is(containerId))));
				GridFsResource resource = gridFsTemplate.getResource(file);
				return resource.getInputStream();
			
		} catch (NullPointerException ex) {
			throw new IllegalArgumentException(
					String.format("this file path : %s is not valid under this container %s ", path, containerId));

		}
	}
}
