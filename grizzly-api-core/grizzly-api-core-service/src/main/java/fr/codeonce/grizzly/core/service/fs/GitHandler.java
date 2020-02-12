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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import fr.codeonce.grizzly.core.domain.util.FileSystemUtil;
import fr.codeonce.grizzly.core.service.util.CustomGitAPIException;

@Service
public class GitHandler {

	static final String TMPDIR = FileSystemUtil.getTempFolder();

	private static final Logger log = LoggerFactory.getLogger(FilesHandler.class);

	@Autowired
	private FilesHandler filesHandler;

	/***
	 * For a given GIT Repository URL return Branch List
	 * 
	 * @param gitRepoUrl
	 * @return list of BRANCHS
	 * @throws CustomGitAPIException
	 */
	public List<String> getRepoBranchsList(String gitRepoUrl, String gitUsername, String gitPassword) {
		log.debug("Fetching Branchs for Repo : {}", log);
		Collection<Ref> refs;
		List<String> branches = new ArrayList<>();
		try {
			if (gitUsername != null || gitPassword != null) {
				refs = Git.lsRemoteRepository().setHeads(true).setRemote(gitRepoUrl)
						.setCredentialsProvider(new UsernamePasswordCredentialsProvider(gitUsername, gitPassword))
						.call();
			} else {
				refs = Git.lsRemoteRepository().setHeads(true).setRemote(gitRepoUrl).call();
			}

			for (Ref ref : refs) {
				branches.add(ref.getName().substring(ref.getName().lastIndexOf('/') + 1, ref.getName().length()));
			}
			Collections.sort(branches);
		} catch (Exception e) {
			throw new BadCredentialsException("4011");
		}
		return branches;
	}

	/**
	 * Get Hierarchy from a Remote Repository
	 * 
	 * @throws CustomGitAPIException
	 * @throws IllegalAccessException
	 */
	public String cloneGitRepository(String gitUrl, String branch, String containerId, String dbsourceId,
			String databaseName, String gitUsername, String gitPassword) throws CustomGitAPIException {
		String repoName = gitUrl.substring(gitUrl.lastIndexOf('/') + 1);
		Git git = null;
		try {
			CloneCommand gitCommand = Git.cloneRepository()//
					.setURI(gitUrl)//
					.setDirectory(makeGitDirectory(TMPDIR + containerId + File.separator + repoName))//
					.setBranch(branch);
			if (gitUsername != null || gitPassword != null) {
				git = gitCommand
						.setCredentialsProvider(new UsernamePasswordCredentialsProvider(gitUsername, gitPassword))
						.call();
			} else {
				git = gitCommand.call();
			}
		} catch (GitAPIException e) {
			throw new BadCredentialsException("4011");
		} catch (IOException | JGitInternalException e) {
			throw new CustomGitAPIException(repoName, e);
		} finally {
			if (git != null) {
				git.close();
			}
		}
		return filesHandler.getJsonHierarchy(TMPDIR + containerId + File.separator + repoName, containerId, dbsourceId,
				databaseName);
	}

	private File makeGitDirectory(String fullRepoName) throws IOException {
		FileUtils.deleteDirectory(new File(fullRepoName));
		File gitDirectory = new File(fullRepoName);
		gitDirectory.mkdir();
		return gitDirectory;
	}
}
