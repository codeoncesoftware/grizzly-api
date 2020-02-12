/*-
 * ========================LICENSE_START=================================
 * grizzly-api-core-domain
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
package fr.codeonce.grizzly.core.domain.util;

import java.io.File;

import org.apache.commons.io.FileUtils;

/**
 * A utility class for handling File System IO
 * 
 */
public class FileSystemUtil {
	
	private FileSystemUtil() {
	}

	public static String getTempFolder() {
		String tmpdir = System.getProperty("java.io.tmpdir");
		if(!tmpdir.endsWith(File.separator)) {
			tmpdir = tmpdir + File.separator;
		}
		return tmpdir;
	}
	
	public static String getTempFolderPath(String folderName) {
		return getTempFolder() +  folderName;
	}
	
	public static File getTempFolder(String folderName) {
		return new File(getTempFolderPath(folderName));
	}

	public static String getTempFilePath(String folderName, String fileName) {
		return getTempFolder()  + folderName + File.separator + fileName;
	}
	
	public static File getTempFile(String folderName, String fileName) {
		return FileUtils.getFile(getTempFilePath(folderName,fileName));
	}
	
	public static String getTempFSPath(String folderName) {
		return getTempFolder()  + folderName ;
	}

}
