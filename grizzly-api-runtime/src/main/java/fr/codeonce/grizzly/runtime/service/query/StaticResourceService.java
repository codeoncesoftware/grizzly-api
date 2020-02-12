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
package fr.codeonce.grizzly.runtime.service.query;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.stereotype.Service;

import com.mongodb.client.gridfs.model.GridFSFile;

import fr.codeonce.grizzly.core.service.resource.ResourceService;

@Service
public class StaticResourceService {

	@Autowired
	private ResourceService resourceService;
	
	public GridFsResource getResource(String containerId, HttpServletRequest request, HttpServletResponse response) {
		String requestURL = request.getRequestURL().toString();
		String path = requestURL.split("/path/")[1];
		GridFSFile fsFile = this.resourceService.getResource(containerId, path);
		return this.resourceService.getGridFsResource(fsFile, containerId);
	}
	
	public GridFsResource getResourceWithId(String containerId, String fileId) {
		GridFSFile fsFile = this.resourceService.getResourceFileWithId(containerId, fileId);
		return this.resourceService.getGridFsResource(fsFile, containerId);
	}
	
	public void setHttpServletResponse(GridFsResource resource, HttpServletResponse response) throws IOException {
		InputStream inputStream = null;
		
		try (OutputStream outputStream = response.getOutputStream()) {

			// display name from file.
			String fileName = resource.getFilename();

			// set Content Type
			response.setContentType(URLConnection.guessContentTypeFromName(fileName));

			// the length
			response.setContentLengthLong(resource.contentLength());

			fileName = URLDecoder.decode(fileName, "ISO8859_1");

			response.setHeader("Content-disposition", "inline; filename=" + fileName);

			// The INPUTSTREAM from MONGO
			inputStream = resource.getInputStream();

			// response out put stream will be used to write the file content.

			byte[] buf = new byte[1024];

			int count = 0;

			while ((count = inputStream.read(buf)) >= 0) {

				outputStream.write(buf, 0, count);
			}
			outputStream.flush();
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
		}
	}

	
}
