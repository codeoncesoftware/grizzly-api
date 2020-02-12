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
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.gridfs.model.GridFSFile;

import fr.codeonce.grizzly.core.domain.container.Container;
import fr.codeonce.grizzly.core.domain.container.ContainerRepository;
import fr.codeonce.grizzly.core.domain.container.hierarchy.ContainerHierarchy;
import fr.codeonce.grizzly.core.domain.container.hierarchy.ContainerHierarchyRepository;
import fr.codeonce.grizzly.core.domain.datasource.DBSource;
import fr.codeonce.grizzly.core.domain.datasource.DBSourceRepository;
import fr.codeonce.grizzly.core.domain.util.FileSystemUtil;
import fr.codeonce.grizzly.core.service.datasource.cache.ConnectionCacheService;
import fr.codeonce.grizzly.core.service.fs.model.CustomFile;
import fr.codeonce.grizzly.core.service.fs.model.CustomFolder;

/**
 * Handle Project import : GIT or Local ZIP File and Return hierarchy as JSON
 * 
 * @author rayen
 *
 */
@Service
public class FilesHandler {

	// TMP Directory for storing files
	static final String TMPDIR = FileSystemUtil.getTempFolder();

	private static final Logger log = LoggerFactory.getLogger(FilesHandler.class);

	private static final ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private ContainerHierarchyRepository containerHierarchyRepository;

	@Autowired
	private ContainerRepository containerRepository;

	@Autowired
	private ConnectionCacheService cacheService;

	@Autowired
	private DBSourceRepository dbSourceRepository;

	/**
	 * Returns the hierarchy of a given directory
	 * 
	 * @param fullDirectoryName
	 * @param idContainer
	 * @return hierarchy of the directory
	 * @throws IllegalAccessException
	 */
	public String getJsonHierarchy(String fullDirectoryName, String idContainer, String dbsourceId,
			String databaseName) {
		GridFsTemplate gridFsTemp = getGridFsTemplate(dbsourceId, databaseName);
		String jsonHierarchy = "";
		StopWatch stopWatch = new StopWatch();

		stopWatch.start("Delete old files in GridFS");
		
		// Delete the old GridFs files under this container
		deleteGridfsFiles(idContainer);
		stopWatch.stop();

		stopWatch.start("saving the hierarchy detailed");
		
		try {	
			CustomFolder parentFolder = new CustomFolder(fullDirectoryName.replaceAll("\\s", ""));
			CustomFolder mylist = displayIt(parentFolder);			
			
			replaceParentWithChild(mylist);
			
			mylist.getChildren().forEach(item -> {
				if (item instanceof CustomFolder) {
					saveIt((CustomFolder) item, idContainer, gridFsTemp);
				} else if (item instanceof CustomFile) {
					File file = new File(((CustomFile) item).getName());
					InputStream inputStream;
					try {
						inputStream = new FileInputStream(file);
						Document metaData = new Document();
						metaData.put("containerId", idContainer);
						String fileUri = file.getPath()
								.substring(file.getPath().indexOf(idContainer) + idContainer.length() + 1)
								.replaceAll("\\\\", "/");
						metaData.put("fileUri", fileUri);
						metaData.put("parentPath", mylist.getName().indexOf(idContainer) + idContainer.length() + 1);
						((CustomFile) item)
								.setFileId(gridFsTemp.store(inputStream, file.getName(), metaData).toString());
						inputStream.close();

					} catch (FileNotFoundException e) {
						log.debug("FileNotFoundException {}", e);
					} catch (IOException e) {
						log.debug("IOException : {}", e);
					}
				}
			});
			jsonHierarchy = mapper.writeValueAsString(mylist);
			saveHierarchy(idContainer, jsonHierarchy);

			// Delete Folder from server after extracting the hierarchy
			FileUtils.deleteDirectory(new File(TMPDIR + idContainer));
		} catch (IOException e) {
			log.debug("IOException : {}", e);
		}
		stopWatch.stop();
		log.debug(stopWatch.prettyPrint());

		return jsonHierarchy;
	}

	/**
	 * Checks if the parent folder has on child as folder with the same Name
	 * If it exists, the parent folder will be skipped and the child takes it's place
	 * @param mylist
	 */
	private void replaceParentWithChild(CustomFolder mylist) {
		if (mylist.getChildren().size() == 1 &&  (mylist.getChildren().get(0) instanceof CustomFolder)) {
			
			CustomFolder firstFolder = (CustomFolder) mylist.getChildren().get(0);
			String firstFolderName = firstFolder.getName().substring(firstFolder.getName().lastIndexOf(File.separator) + 1);
			
			if (firstFolderName.equals(mylist.getName().substring(mylist.getName().lastIndexOf(File.separator) + 1))) {
				CustomFolder childFolder = (CustomFolder) mylist.getChildren().get(0);
				mylist.setName(childFolder.getName());
				mylist.setChildren(childFolder.getChildren());
			}
		}
	}

	public void deleteGridfsFiles(String containerId) {
		this.containerRepository.findById(containerId)
				.ifPresent(cont -> {
					if (cont.getDbsourceId() != null) {
						this.cacheService
						.getGridFs(this.cacheService.getMongoClient(cont.getDbsourceId()), cont.getDatabaseName())//
						.delete(Query.query((Criteria.where("metadata.containerId").is(containerId))));
						
						String hierarchyId = cont.getHierarchyId();
						this.containerHierarchyRepository.findById(hierarchyId).ifPresent(hierarchy -> {
							hierarchy.setHierarchy(null);
							this.containerHierarchyRepository.save(hierarchy);
							cont.setHierarchyId(hierarchyId);
							this.containerRepository.save(cont);
						});
					}
				});
	}

	/**
	 * Save the New Hierarchy in DataBase and Set the New Hierarchy ID in the
	 * Container
	 * 
	 * @param idContainer
	 * @param jsonHierarchy
	 */
	public void saveHierarchy(String idContainer, String jsonHierarchy) {
		// Save Hierarchy
		ContainerHierarchy hierarchy = new ContainerHierarchy(jsonHierarchy);
		String hierarchyId = containerHierarchyRepository.save(hierarchy).getId();
		Optional<Container> container = containerRepository.findById(idContainer);
		if (container.isPresent()) {
			Container containerToSave = container.get();
			String currentHierarchyId = containerToSave.getHierarchyId();
			containerHierarchyRepository.deleteById(currentHierarchyId);
			containerToSave.setHierarchyId(hierarchyId);
			containerRepository.save(containerToSave);
		}
	}

	/**
	 * Exports hierarchy as a JSON object for a given Directory
	 * 
	 * @param parentFolder
	 * @return
	 * @throws IOException
	 */
	public static CustomFolder displayIt(CustomFolder parentFolder) throws IOException {
		File node = new File(parentFolder.getName());
		// List all folders but the .GIT folder
		if (node.isDirectory()) {
			String[] subNote = node.list();
			for (String filename : subNote) {
				// Eliminate .gitFolder
				if (!filename.equals(".git")) {
					String path = node + File.separator + filename;
					if (new File(path).isDirectory()) {
						CustomFolder folder = new CustomFolder(path);
						parentFolder.addFolder(folder);
						displayIt(folder);
					} else {
						parentFolder.addFolder(new CustomFile(path));
					}
				}
			}
		}
		return parentFolder;
	}

	/**
	 * Save all files and sub directories of a given directory in GridFS and add
	 * file id to every file
	 * 
	 * @param mylist
	 */
	public void saveIt(CustomFolder mylist, String idContainer, GridFsTemplate gridFsTemp) {
		mylist.getChildren().forEach(item -> {
			if (item instanceof CustomFile) {
				File file = new File(((CustomFile) item).getName());
				try {
					InputStream inputStream = new FileInputStream(file);
					Document metaData = new Document();
					metaData.put("containerId", idContainer);
					String fileUri = file.getPath()
							.substring(file.getPath().indexOf(idContainer) + idContainer.length() + 1)
							.replaceAll("\\\\", "/");
					metaData.put("fileUri", fileUri);
					metaData.put("parentPath", mylist.getName().substring(mylist.getName().indexOf(idContainer) + idContainer.length() + 1));
					((CustomFile) item).setFileId(gridFsTemp.store(inputStream, file.getName(), metaData).toString());
					inputStream.close();
				} catch (Exception e) {
					log.debug("{}", e);
				}
			} else if (item instanceof CustomFolder) {
				saveIt((CustomFolder) item, idContainer, gridFsTemp);
			}
		});
	}

//	public byte[] getResourceFile(String containerId, String path) throws IOException {
//		GridFSFile file = gridFsTemplate.findOne(
//				Query.query((Criteria.where("metadata.fileUri").is(path).and("metadata.containerId").is(containerId))));
//		GridFsResource resource = gridFsTemplate.getResource(file);
//		return IOUtils.toByteArray(resource.getInputStream());
//	}

	public GridFSFile getResource(String containerId, String path) {
		
		return this.containerRepository.findById(containerId).map(cont -> {
			MongoClient mongoClient = this.cacheService.getMongoClient(cont.getDbsourceId());
			GridFsTemplate gridFsTemplate = this.cacheService.getGridFs(mongoClient, cont.getDatabaseName());
			return gridFsTemplate.findOne(Query.query(
					(Criteria.where("metadata.fileUri").is(path).and("metadata.containerId").is(containerId))));
		}).orElseThrow();
	}
	
	public GridFsTemplate getGridFsTemplate(String dbsourceId, String databaseName) {
		Optional<DBSource> dbOp = this.dbSourceRepository.findById(dbsourceId);
		MongoClient mClient;
		if (dbOp.isPresent()) {
			if (dbOp.get().getConnectionMode().equalsIgnoreCase("FREE")) {
				mClient = this.cacheService.getAtlasMongoClient();
				databaseName = dbOp.get().getPhysicalDatabase();
			} else {
				mClient = this.cacheService.getMongoClient(dbOp.get());
			}
			return this.cacheService.getGridFs(mClient, databaseName);
		}
		throw new IllegalArgumentException("Can't find the Datasource to retrieve the GridFsTemplate");
	}
	
	public GridFSFile getResourceFileWithId(String containerId, String fileId) {
		Map<String, GridFSFile> map = new HashMap<>();
		this.containerRepository.findById(containerId).ifPresent(container -> 
			 this.dbSourceRepository.findById(container.getDbsourceId()).ifPresent(db -> {
				String dbName;
				if (db.getConnectionMode().equalsIgnoreCase("FREE")) {
					dbName = db.getPhysicalDatabase(); 
				} else {
					dbName = container.getDatabaseName();
				}
				map.put("fs", this.cacheService.getGridFs(this.cacheService.getMongoClient(container.getDbsourceId()), dbName)//
						.findOne(
								Query.query((Criteria.where("_id").is(fileId)))));
			})
		);
		return map.get("fs");
	}

	public GridFsResource getGridFsResource(GridFSFile fsFile, String containerId) {
		Map<String, GridFsResource> map = new HashMap<>();
		this.containerRepository.findById(containerId).ifPresent(container -> {
			 this.dbSourceRepository.findById(container.getDbsourceId()).ifPresent(db -> {
				String dbName;
				if (db.getConnectionMode().equalsIgnoreCase("FREE")) {
					dbName = db.getPhysicalDatabase(); 
				} else {
					dbName = container.getDatabaseName();
				}
				map.put("fs", this.cacheService.getGridFs(this.cacheService.getMongoClient(container.getDbsourceId()), dbName).getResource(fsFile));
			});
			
		});
		return map.get("fs");
	}

}
