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

import java.io.IOException;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.netflix.zuul.http.HttpServletRequestWrapper;
import com.netflix.zuul.http.ServletInputStreamWrapper;

import fr.codeonce.grizzly.common.runtime.RuntimeQueryRequest;
import fr.codeonce.grizzly.common.runtime.RuntimeRequest;
import fr.codeonce.grizzly.common.runtime.resource.RuntimeResource;
import fr.codeonce.grizzly.gateway.filter.model.DBSource;

@Service
public class APIService {

	@Autowired
	private FeignDiscovery feignDiscovery;

	/**
	 * Prepare the Runtime Request from Resource Object
	 * 
	 * @param authorization
	 * @param containerId
	 * @return the Runtime Request to be Forwarded to the MicroService
	 */
	public RuntimeRequest<?> getRuntimeRequest(String containerId, RuntimeResource resource) {
		return RuntimeQueryMapper.getRuntimeTransformationEquest(resource, containerId);
	}

	public RuntimeQueryRequest getRuntimeQueryRequest(RuntimeResource resource) {
		DBSource dbSource = this.getDBSource(resource.getCustomQuery().getDatasource());
		return RuntimeQueryMapper.mapToRuntimeQueryRequest(resource, dbSource);
	}

	/**
	 * Fetch a Resource From Database based on the Container Id and the Unique
	 * Resource Path
	 * 
	 * @param containerId
	 * @param resourcePath
	 * @return
	 */
	public RuntimeResource getResource(String containerId, String resourcePath) {
		return feignDiscovery.getResource(containerId, resourcePath);
	}

	public DBSource getDBSource(String dbsourceId) {
		return feignDiscovery.getDBSource(dbsourceId);
	}

	/**
	 * Add the Received Runtime Request to the Request Body
	 * 
	 * @param bytes,  the Body to be set in the new HttpServletRequest
	 * @param context to get the old HttpServeletRequest
	 * @return a new HttpServeletRequest with the Runtime Request in Body
	 */
	public HttpServletRequest makeHttpServletRequest(HttpServletRequest request, byte[] bytes) {
		return new HttpServletRequestWrapper(request) {

			@Override
			public ServletInputStream getInputStream() throws IOException {
				return new ServletInputStreamWrapper(bytes);
			}

			@Override
			public int getContentLength() {
				return bytes.length;
			}

			@Override
			public long getContentLengthLong() {
				return bytes.length;
			}
		};
	}
}
