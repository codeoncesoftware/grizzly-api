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
package fr.codeonce.grizzly.core.service.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ResourceUtils;

import com.google.common.base.Objects;

import fr.codeonce.grizzly.core.domain.util.FileSystemUtil;
import fr.codeonce.grizzly.core.service.fs.FilesHandler;
import fr.codeonce.grizzly.core.service.fs.GitHandler;
import fr.codeonce.grizzly.core.service.fs.ZipHandler;
import fr.codeonce.grizzly.core.service.fs.model.CustomFile;
import fr.codeonce.grizzly.core.service.fs.model.CustomFolder;

public class FilesHandlerTest extends AbstractServiceTest {
	
	@Autowired
	private GitHandler gitHandler;
	
	@Autowired 
	private ZipHandler zipHandler;

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	static final String TMPDIR = FileSystemUtil.getTempFolder();

	@Before
	public void init() {

	}

	@Test
	public void testGetRepoBranchsList() throws InvalidRemoteException, TransportException, GitAPIException {
		List<String> branchs = gitHandler.getRepoBranchsList("https://github.com/rayenrejeb/code_once_test.git", null,
				null);
		assertEquals(Collections.singletonList("master"), branchs);
	}

	@Test
	public void testUnzip() throws IOException {

		File testFolder = folder.newFolder("zipTest");
		Files.write(Paths.get(testFolder.getAbsolutePath() + File.separator + "test.zip"),
				IOUtils.toByteArray(new FileInputStream(ResourceUtils.getFile("classpath:fs"+ File.separator + "test.zip"))));

		zipHandler.unzip(testFolder.getAbsolutePath() + File.separator, "test.zip");
		File dir = new File(testFolder.getAbsolutePath() + File.separator + "test");
		List<String> savedFiles = new ArrayList<>();
		savedFiles.add("file1.txt");
		savedFiles.add("folder");
		assertTrue(Objects.equal(Arrays.asList(dir.list()), savedFiles));

	}

	@Test
	public void displayItShouldReturnHierarchyInCustomFolder() {

		try {
			CustomFolder result = FilesHandler
					.displayIt(new CustomFolder(ResourceUtils.getFile("classpath:fs" + File.separator + "test").getAbsolutePath()));
			assertTrue(result.getName().substring(result.getName().lastIndexOf(File.separator) + 1).equals("test"));
			result.getChildren().forEach(obj -> {
				if (obj instanceof CustomFolder) {
					CustomFolder folder = (CustomFolder) obj;
					String folderName = folder.getName().substring(folder.getName().lastIndexOf(File.separator) + 1);
					assertTrue(folderName.equals("folder"));
				} else {
					CustomFile file = (CustomFile) obj;
					String fileName = file.getName().substring(file.getName().lastIndexOf(File.separator) + 1);
					assertTrue(fileName.equals("file1.txt") || fileName.equals("file2.txt"));
				}
			});

		} catch (IOException e) {
			fail();
		}
	}

}
