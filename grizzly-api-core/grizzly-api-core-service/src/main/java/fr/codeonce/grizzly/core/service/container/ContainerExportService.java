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
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
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
import org.springframework.util.ResourceUtils;

import com.mongodb.client.gridfs.model.GridFSFile;

import fr.codeonce.grizzly.core.domain.container.Container;
import fr.codeonce.grizzly.core.domain.container.ContainerRepository;
import fr.codeonce.grizzly.core.domain.container.hierarchy.ContainerHierarchy;
import fr.codeonce.grizzly.core.domain.container.hierarchy.ContainerHierarchyRepository;
import fr.codeonce.grizzly.core.domain.util.FileSystemUtil;
import fr.codeonce.grizzly.core.service.datasource.cache.ConnectionCacheService;
import fr.codeonce.grizzly.core.service.fs.model.CustomFile;
import fr.codeonce.grizzly.core.service.fs.model.CustomFolder;
import fr.codeonce.grizzly.core.service.util.GlobalExceptionUtil;
import fr.codeonce.grizzly.core.service.util.JsonUtil;

@Service
@Transactional
public class ContainerExportService {

	private static final Logger log = LoggerFactory.getLogger(ContainerExportService.class);

	@Autowired
	private ContainerSwaggerService containerSwaggerService;

	@Autowired
	private ContainerRepository containerRepository;

	@Autowired
	private ContainerHierarchyRepository hierarchyRepository;

	@Autowired
	private ConnectionCacheService cacheService;

	public File export(String currentContainerId) throws JSONException, IOException {
		Container c = containerRepository.findById(currentContainerId)
				.orElseThrow(GlobalExceptionUtil.notFoundException(Container.class, currentContainerId));
		String hierarchyId = c.getHierarchyId();
		// if files are imported, there is an hierarchy
		ContainerHierarchy hierarachy = hierarchyRepository.findById(hierarchyId)
				.orElseThrow(GlobalExceptionUtil.notFoundException(ContainerHierarchy.class, hierarchyId));
		String h = hierarachy.getHierarchy();
		String jsonString = new JSONObject(h).toString();
		CustomFolder ccFolder = JsonUtil.readValue(jsonString, CustomFolder.class);
		// recursive function to save files
		File projectFolder = new File(ccFolder.getName());
		if (projectFolder.mkdirs())
			log.debug("folder created");
		String rootFolderPath =  FileSystemUtil.getTempFolderPath(currentContainerId);
		GridFsTemplate gridFsTemplate = cacheService.getGridFs(cacheService.getMongoClient(c.getDbsourceId()),
				c.getDatabaseName());
		exportRecursive(rootFolderPath, ccFolder, currentContainerId, gridFsTemplate);
		return ResourceUtils.getFile(rootFolderPath);

	}

	private void exportRecursive(String rootFolderPath, CustomFolder list, String idContainer,
			GridFsTemplate gridFsTemplate) {
		list.getChildren().forEach(item -> {
			@SuppressWarnings("unchecked")
			LinkedHashMap<String, String> i = (LinkedHashMap<String, String>) item;
			if (i.get("type").equals("file")) {
				String jsonString = new JSONObject(i).toString();
				try {
					CustomFile ccFile = JsonUtil.readValue(jsonString, CustomFile.class);
					InputStream inputStream;

					String oldFileId = ccFile.getFileId();
					GridFSFile file = gridFsTemplate.findOne(Query.query((Criteria.where("_id").is(oldFileId))));

					String fileUri = file.getMetadata().getString("fileUri");

					File f = new File(rootFolderPath + File.separator + fileUri);
					if (f.createNewFile()) {
						log.debug("{} file created", fileUri);
						inputStream = gridFsTemplate.getResource(file.getFilename()).getInputStream();
						FileUtils.copyToFile(inputStream, f);
						inputStream.close();
					} else {
						log.debug("temp file already created {} :", file.getFilename());
					}

				} catch (Exception e) {
					log.debug("{}", e);
				}
			} else {
				String jsonString = new JSONObject(i).toString();
				CustomFolder ccFolder;
				try {
					ccFolder = JsonUtil.readValue(jsonString, CustomFolder.class);
					String fileUri = ccFolder.getName().replaceAll("\\\\", "/")
							.substring(ccFolder.getName().indexOf(idContainer) + idContainer.length() + 1);
					File folder = new File(rootFolderPath + File.separator + fileUri);
					if (folder.mkdirs()) {
						log.debug("{} folder created", folder.getAbsoluteFile());
					} else {
						log.debug("temp folder already created {} :", folder.getName());
					}
					exportRecursive(rootFolderPath, ccFolder, idContainer, gridFsTemplate);
				} catch (IOException e) {
					log.debug("{}", e);
				}
			}
		});
	}

	public List<String> getFileList(File directory) {
		List<String> fileList = new ArrayList<>();
		appendFileList(directory, fileList);
		return fileList;
	}

	/**
	 * Get files list from the directory recursive to the sub directory.
	 */
	private void appendFileList(File directory, List<String> fileList) {
		File[] files = directory.listFiles();
		if (files != null && files.length > 0) {
			for (File file : files) {
				if (file.isFile()) {
					fileList.add(file.getAbsolutePath());
				} else {
					appendFileList(file, fileList);
				}
			}
		}
	}

	public void zipContainer(List<String> fileList, String containerId, File directory, HttpServletResponse response)
			throws IOException {

		// setting headers
		String fileName = containerId + ".zip";
		response.setStatus(HttpServletResponse.SC_OK);
		response.addHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
		response.setHeader("Content-Type", "application/zip");

		String swaggerFileName = containerSwaggerService.generateSwagger("dev", containerId).getPath();
		File swaggerTemp = new File(directory.getAbsolutePath() + File.separator + swaggerFileName);
		if (swaggerTemp.createNewFile()) {
			log.debug("{} file created", swaggerFileName);
			FileUtils.copyFile(containerSwaggerService.generateSwagger("dev", containerId), swaggerTemp);
		} else {
			log.debug("error creating temp file {}", swaggerFileName);
		}
		fileList.add(swaggerTemp.getAbsolutePath());

		try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {

			for (String filePath : fileList) {

				// Creates a ZIP entry.
				String name = filePath.substring(directory.getAbsolutePath().lastIndexOf(File.separator) + 1);

				ZipEntry zipEntry = new ZipEntry(name);
				zos.putNextEntry(zipEntry);

				// Read file content and write to ZIP output stream.

				try (FileInputStream fis = new FileInputStream(filePath)) {
					byte[] buffer = new byte[1024];
					int length;
					while ((length = fis.read(buffer)) > 0) {
						zos.write(buffer, 0, length);
					}

					// Close the ZIP entry.
					zos.flush();
					zos.closeEntry();
				} catch (Exception e) {
					log.debug("an exception has occured : {}", e);
				}
			}

			deleteDirectoryStream(directory.getAbsoluteFile().toPath());

		} catch (IOException e) {
			log.debug("an exception has occured : {}", e);
		}

	}

	private void deleteDirectoryStream(Path path) throws IOException {
		Files.walk(path).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
	}

}
