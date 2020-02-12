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
package fr.codeonce.grizzly.core.service.fs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.multipart.MultipartFile;

import fr.codeonce.grizzly.core.domain.util.FileSystemUtil;
import fr.codeonce.grizzly.core.service.util.GlobalExceptionUtil;

@Service
public class ZipHandler {

	static final String TMPDIR = FileSystemUtil.getTempFolder();

	private static final Logger log = LoggerFactory.getLogger(FilesHandler.class);

	@Autowired
	private FilesHandler filesHandler;

	public String importZipFile(MultipartFile zipFile, String idContainer, String dbsourceId, String databaseName)
			throws IOException {
		log.debug("system : {}", System.getProperty("os.name"));

		log.debug("tmpdir : {}", TMPDIR);

		StopWatch stopWatch = new StopWatch();

		String zipFileName = zipFile.getOriginalFilename().substring(0, zipFile.getOriginalFilename().lastIndexOf('.'));

		stopWatch.start("saving zip in temp");
		saveZip(zipFile, idContainer + File.separator + zipFileName);
		stopWatch.stop();

		stopWatch.start("unzipping");
		unzip(TMPDIR + idContainer + File.separator + zipFileName, zipFile.getOriginalFilename());
		stopWatch.stop();

		stopWatch.start("getJsonHierarchy");
		String hierarchy = filesHandler.getJsonHierarchy(TMPDIR + idContainer + File.separator + zipFileName,
				idContainer, dbsourceId, databaseName);
		stopWatch.stop();

		log.debug(stopWatch.prettyPrint());

		return hierarchy;
	}

	/**
	 * Saves the Zip in the TMP directory
	 * 
	 * @param file
	 * @param idContainer
	 */
	public void saveZip(MultipartFile file, String pathToSave) {
		try {
			if (!Files.exists(Paths.get(TMPDIR + pathToSave))) {
				Files.createDirectories(Paths.get(TMPDIR + pathToSave));
				log.debug("folder to save zip in : {}", TMPDIR + pathToSave);

			}
			log.debug("file to save : {}", file.getOriginalFilename());
			Files.write(Paths.get(TMPDIR + pathToSave + File.separator + file.getOriginalFilename()), file.getBytes());
		} catch (IOException e1) {
			GlobalExceptionUtil.fileNotFoundException(file.getOriginalFilename());
		}
	}

	/**
	 * UNZIP a .ZIP Folder in a directory to parse later and extract hierarchy
	 * 
	 * @param zipFile     : .ZIP
	 * @param idContainer (as a unique name for the folder)
	 * @throws IOException
	 */
	public void unzip(String pathToSave, String zipFileOriginalName) throws IOException {
		try (ZipInputStream zipinputstream = new ZipInputStream(
				new FileInputStream(pathToSave + File.separator + zipFileOriginalName), StandardCharsets.ISO_8859_1)) {
			// Copy ZIP File on server
			byte[] buf = new byte[1024];
			ZipEntry zipentry;
			zipentry = zipinputstream.getNextEntry();
			// Browse file per file
			while (zipentry != null) {
				// for each entry to be extracted, eliminate spaces before saving
				String entryName = pathToSave + File.separator + zipentry.getName().replaceAll("\\s", "");

				if (!zipentry.getName().contains("__MACOSX")) {
					int n;
					File newFile = new File(entryName);
					String directory = newFile.getParent();
					// Creating parent directories
					if (directory == null) {
						if (newFile.isDirectory()) {
							break;
						}
					} else {
						if (!directory.contains("__MACOSX")) {
							new File(directory).mkdirs();
						}
					}
					if (!zipentry.isDirectory() && !zipentry.getName().equals(zipFileOriginalName)) {
						try (FileOutputStream fileoutputstream = new FileOutputStream(entryName);) {
							while ((n = zipinputstream.read(buf, 0, 1024)) > -1) {
								fileoutputstream.write(buf, 0, n);
							}
							fileoutputstream.flush();
						}
					}
				}
				zipinputstream.closeEntry();
				zipentry = zipinputstream.getNextEntry();
			} // while
			zipinputstream.closeEntry();
		} catch (Exception e) {
			FileUtils.deleteDirectory(new File(pathToSave));
			log.debug("a zip execption: {}", e);
			throw new FileNotFoundException("File is not valid");
		} finally {
			FileUtils.deleteQuietly(new File(pathToSave + File.separator + zipFileOriginalName));
		}
	}

}
