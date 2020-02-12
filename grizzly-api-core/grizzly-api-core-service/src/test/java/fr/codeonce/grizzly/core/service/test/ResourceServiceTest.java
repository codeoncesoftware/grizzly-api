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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import fr.codeonce.grizzly.core.service.fs.FilesHandler;
import fr.codeonce.grizzly.core.service.fs.GitHandler;
import fr.codeonce.grizzly.core.service.fs.ZipHandler;
import fr.codeonce.grizzly.core.service.resource.ResourceService;
import fr.codeonce.grizzly.core.service.util.CustomGitAPIException;

public class ResourceServiceTest extends AbstractServiceTest {

	@Autowired
	private ResourceService resourceService;

	@MockBean
	private GitHandler gitHandler;

	@MockBean
	private ZipHandler zipHandler;

	@MockBean
	private FilesHandler filesHandler;

	@Test
	public void shouldCallCloneGitRepository() throws CustomGitAPIException {
		resourceService.cloneGitRepository("{\"name\":\"git\"}");
		verify(gitHandler, times(1)).cloneGitRepository(null, null, null, null, null, null, null);
	}

	@Test
	public void shouldCallGetRepoBranchsList() throws CustomGitAPIException {
		resourceService.getRepoBranchsList("{\"name\":\"git\"}");
		verify(gitHandler, times(1)).getRepoBranchsList(null, null, null);
	}

	@Test
	public void shouldCallImportZipFile() throws IOException {
		resourceService.importZipFile(null, null, null, null);
		verify(zipHandler, times(1)).importZipFile(null, null, null, null);
	}

	@Test
	public void shouldCallGetResourceByPathAndContainerID() throws IOException {
		resourceService.getResource(null, null);
		verify(filesHandler, times(1)).getResource(null, null);
	}

}
