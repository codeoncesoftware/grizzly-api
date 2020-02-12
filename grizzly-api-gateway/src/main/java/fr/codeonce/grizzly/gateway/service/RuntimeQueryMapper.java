/*-
 * ========================LICENSE_START=================================
 * grizzly-api-gateway
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
package fr.codeonce.grizzly.gateway.service;

import java.util.Optional;
import java.util.stream.Collectors;

import fr.codeonce.grizzly.common.runtime.RuntimeQueryRequest;
import fr.codeonce.grizzly.common.runtime.RuntimeRequest;
import fr.codeonce.grizzly.common.runtime.resource.RuntimeResourceFile;
import fr.codeonce.grizzly.common.runtime.resource.RuntimeResourceParameter;
import fr.codeonce.grizzly.common.runtime.resource.RuntimeResource;
import fr.codeonce.grizzly.gateway.filter.model.DBSource;

@SuppressWarnings("deprecation")
public class RuntimeQueryMapper {

	private RuntimeQueryMapper() {

	}

	public static RuntimeQueryRequest mapToRuntimeQueryRequest(RuntimeResource resource, DBSource dbSource) {
		if (resource == null) {
			return null;
		}

		RuntimeQueryRequest request = new RuntimeQueryRequest();
		request.setHttpMethod(resource.getHttpMethod());
		request.setPath(resource.getPath());
		request.setExecutionType(resource.getExecutionType());
		request.setQueryType(resource.getCustomQuery().getType());
		request.setQuery(resource.getCustomQuery().getQuery());
		request.setSecurityLevel(resource.getSecurityLevel());
		request.setPageable(resource.isPageable());
		request.setDbsourceId(resource.getCustomQuery().getDatasource());
		request.setConnectionMode(dbSource.getConnectionMode());
		request.setDatabaseName(resource.getCustomQuery().getDatabase());
		request.setPhysicalDatabaseName(dbSource.getPhysicalDatabase());
		request.setCollectionName(resource.getCustomQuery().getCollectionName());
		request.setMany(resource.getCustomQuery().isMany());
		request.setParameters(resource.getParameters());
		request.setReturnType(resource.getReturnType());

		return request;
	}

	public static RuntimeRequest<String> getRuntimeTransformationEquest(RuntimeResource resource, String containerId) {
		if (resource == null) {
			return null;
		}
		RuntimeRequest<String> runtimeRequest = new RuntimeRequest<>();
		runtimeRequest.setExecutablePath(resource.getResourceFile().getFileUri());
		Optional<RuntimeResourceParameter> parameter = resource.getParameters().stream()
				.filter(param -> param.getName().equalsIgnoreCase("body")).findFirst();
		if (parameter.isPresent()) {
			runtimeRequest.setBody(parameter.get().getValue());
		}
		runtimeRequest.setExecutionType(resource.getExecutionType());
		runtimeRequest.setContainerId(containerId);
		runtimeRequest.setSecondaryFilePaths(
				resource.getSecondaryFilePaths().stream().map(RuntimeResourceFile::getFileUri).collect(Collectors.toList()));

		return runtimeRequest;
	}

}
