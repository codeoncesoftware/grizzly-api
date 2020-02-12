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
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloudUrlConnection extends URLConnection {

	private static Logger log = LoggerFactory.getLogger(CloudURLStreamHandler.class);

	private final String path;

	/**
	 * This service will be used to dynamically load the resource based in the version stored in the ThreadLocal.
	 */
	private ICloudResolverService cloudResolverService;

	protected CloudUrlConnection(URL url, ICloudResolverService cloudResolverService) {
		super(url);
		
		//EXTRACT PATH : REMOVE Cloud://
		path = StringUtils.substringAfter(url.toExternalForm(), "://");
		
		this.cloudResolverService = cloudResolverService;
	}

	@Override
	public void connect() throws IOException {
		//NOTHING TO DO
	}
	
	@Override
	public String getContentType() {
		return "";
	}

	@Override
	public InputStream getInputStream() throws IOException {
		log.info("Resource requested  : '{}' ", path);
		return cloudResolverService.getInputStream(path);
	}
}
