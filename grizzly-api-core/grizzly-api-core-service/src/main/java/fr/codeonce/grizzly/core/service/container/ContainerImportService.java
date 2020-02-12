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
import java.io.FileInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import fr.codeonce.grizzly.core.domain.util.FileSystemUtil;
import fr.codeonce.grizzly.core.service.datasource.mapper.DBSourceMapperService;
import fr.codeonce.grizzly.core.service.fs.FilesHandler;
import fr.codeonce.grizzly.core.service.fs.MockMultipartFile;
import fr.codeonce.grizzly.core.service.fs.ZipHandler;
import fr.codeonce.grizzly.core.service.project.ProjectExample;
import fr.codeonce.grizzly.core.service.project.ProjectService;

@Service
@Transactional
public class ContainerImportService {

	private static final Logger log = LoggerFactory.getLogger(ContainerImportService.class);

	private static final String IMPORTED_ZIP_FOLDER = "importedZip";

	@Autowired
	private ZipHandler zipHandler;

	@Autowired
	private FilesHandler filesHandler;

	@Autowired
	private ContainerSwaggerService containerSwaggerService;

	public ContainerDto importContainer(MultipartFile zipFile, String containerName, String projectId,
			String dbsourceId, String databaseName) throws Exception {

		// generate hash
		zipHandler.saveZip(zipFile, IMPORTED_ZIP_FOLDER);

		String zipFileName = zipFile.getOriginalFilename();
		zipHandler.unzip(FileSystemUtil.getTempFSPath(IMPORTED_ZIP_FOLDER), zipFileName);

		File myUnzippedFolder = FileSystemUtil.getTempFile(IMPORTED_ZIP_FOLDER,
				zipFileName.substring(0, zipFileName.lastIndexOf('.')));

		log.debug("file name : {}", zipFileName.substring(0, zipFileName.lastIndexOf('.')));
		log.debug("unzipped folder : {}", myUnzippedFolder.getAbsoluteFile());
		File[] files = myUnzippedFolder.listFiles();
		log.debug("files size : {}", files.length);

		File projectFolder = null;
		File swaggerJson = null;
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				projectFolder = files[i];
			} else {
				swaggerJson = files[i];
			}
		}
		log.debug("project folder path : {}", projectFolder.getAbsoluteFile());
		log.debug("swagger files path : {}", swaggerJson.getAbsoluteFile());

		if (swaggerJson != null) {
			
			// convert file to Multipartfile
			FileInputStream input = new FileInputStream(swaggerJson);
			MultipartFile multipartFile = new MockMultipartFile("file.json", swaggerJson.getName(), "text/plain",
					IOUtils.toByteArray(input));
			ContainerDto container = containerSwaggerService.importSwagger(multipartFile, projectId, containerName);
			String containerId = container.getId();
			FileUtils.copyDirectory(FileSystemUtil.getTempFolder(IMPORTED_ZIP_FOLDER),
					FileSystemUtil.getTempFolder(containerId));

			if (projectFolder != null) {
				container.setHierarchy(filesHandler.getJsonHierarchy(
						projectFolder.getAbsolutePath().replace(IMPORTED_ZIP_FOLDER, containerId), containerId,
						dbsourceId, databaseName));
			}
			return container;
		}
		throw new IllegalArgumentException("could not import container");


	}

}
