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
package fr.codeonce.grizzly.core.service.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import fr.codeonce.grizzly.core.domain.container.Container;
import fr.codeonce.grizzly.core.domain.container.ContainerRepository;
import fr.codeonce.grizzly.core.domain.datasource.DBSource;
import fr.codeonce.grizzly.core.domain.enums.Const;
import fr.codeonce.grizzly.core.domain.enums.PredefinedQuery;
import fr.codeonce.grizzly.core.domain.enums.SecurityLevel;
import fr.codeonce.grizzly.core.domain.project.Project;
import fr.codeonce.grizzly.core.domain.project.ProjectRepository;
import fr.codeonce.grizzly.core.domain.project.SecurityApiConfig;
import fr.codeonce.grizzly.core.domain.resource.APIResponse;
import fr.codeonce.grizzly.core.domain.resource.CustomQuery;
import fr.codeonce.grizzly.core.domain.resource.Resource;
import fr.codeonce.grizzly.core.domain.resource.ResourceFile;
import fr.codeonce.grizzly.core.domain.resource.ResourceGroup;
import fr.codeonce.grizzly.core.domain.resource.ResourceParameter;
import fr.codeonce.grizzly.core.domain.resource.ResourceType;
import fr.codeonce.grizzly.core.service.analytics.AnalyticsService;
import fr.codeonce.grizzly.core.service.datasource.DBSourceDto;
import fr.codeonce.grizzly.core.service.datasource.DBSourceService;
import fr.codeonce.grizzly.core.service.datasource.cache.ConnectionCacheService;
import fr.codeonce.grizzly.core.service.fs.FilesHandler;
import fr.codeonce.grizzly.core.service.fs.MockMultipartFile;
import fr.codeonce.grizzly.core.service.fs.ZipHandler;
import fr.codeonce.grizzly.core.service.util.GlobalExceptionUtil;

@Service
public class ProjectExample {

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private ContainerRepository containerRepository;

	@Autowired
	private DBSourceService dbSourceService;

	@Autowired
	private FilesHandler filesHandler;

	@Autowired
	private ZipHandler zipHandler;

	@Autowired
	private ConnectionCacheService cacheService;

	@Value("${resource-url}")
	private String resourceUrl;

	@Autowired
	private AnalyticsService analyticsService;

	public void createProjectExample() throws IOException {

		// create data-source
		String username = getCurrentUsername();
		DBSourceDto dbSourceDto = new DBSourceDto();
		dbSourceDto.setName("Example");
		dbSourceDto.setDescription("This is a description for your first datasource");
		dbSourceDto.setAuthenticationDatabase(Const.ADMIN.getValue());
		dbSourceDto.setDatabase("Example");
		dbSourceDto.setConnectionMode("FREE");
		DBSourceDto savedDataSource = dbSourceService.saveDBSource(dbSourceDto);
		DBSource dbSource = this.dbSourceService.getDbSourceById(savedDataSource.getId());
		// create project linked to a user

		Project project = new Project();
		project.setName("Demo");
		project.setDescription("This is a description for your first project");
		project.setUsername(username);
		project.getRoles().add(Const.ADMIN.getValue());
		project.getRoles().add("user");
		SecurityApiConfig sec = new SecurityApiConfig();
		sec.setClientId("Demo");
		sec.setSecretKey(DigestUtils.sha256Hex(project.getName()) + DigestUtils.sha256Hex("co%de01/"));
		sec.setTokenExpiration(3600);
		project.setSecurityConfig(sec);
		project.setDbsourceId(savedDataSource.getId());
		project.setDatabaseName(savedDataSource.getDatabase());
		Project savedProject = projectRepository.save(project);

		// Create Default Version of Container V1

		Container containerTosave = new Container();
		containerTosave.setName("1.0.0");
		containerTosave.setDescription("This is a description for this project version");
		containerTosave.setProjectId(savedProject.getId());

		// create groups
		ResourceGroup security = new ResourceGroup();
		security.setName("Authentication Grizzly");
		security.setDescription("JWT token");
		ResourceGroup rg1 = new ResourceGroup();
		rg1.setName(Const.QUERY.getValue());
		ResourceGroup rg2 = new ResourceGroup();
		rg2.setName("XSL");
		ResourceGroup rg3 = new ResourceGroup();
		rg3.setName("ThymeLeaf");
		ResourceGroup rg4 = new ResourceGroup();
		rg4.setName("Freemarker");

		containerTosave.getResourceGroups().add(security);
		containerTosave.getResourceGroups().add(rg1);
		containerTosave.getResourceGroups().add(rg2);
		containerTosave.getResourceGroups().add(rg3);
		containerTosave.getResourceGroups().add(rg4);
		containerTosave.setDbsourceId(dbSource.getId());
		// Set Database Name
		String dbName = "";
		if (dbSource.getConnectionMode().equalsIgnoreCase("FREE")) {
			dbName = dbSource.getPhysicalDatabase();
		} else {
			dbName = dbSource.getDatabase();
		}
		
		containerTosave.setDatabaseName(dbName);

		// Set the Swagger Salt
		if (StringUtils.isBlank(containerTosave.getSwaggerUuid())) {
			containerTosave.setSwaggerUuid(UUID.randomUUID().toString().substring(0, 8));
		}

		// Save Container
		Container savedContainer = containerRepository.save(containerTosave);

		this.dbSourceService.createFirstCollection(this.cacheService.getMongoClient(dbSource.getId()),
				containerTosave.getDatabaseName(), "demo");

		String containerId = savedContainer.getId();

		// upload files
		File zipFile = new File(resourceUrl);
		FileInputStream input = new FileInputStream(zipFile);
		MultipartFile multipartFile = new MockMultipartFile("transformation.zip", zipFile.getName(), "application/zip",
				IOUtils.toByteArray(input));
		zipHandler.importZipFile(multipartFile, containerId, dbSource.getId(), dbSource.getPhysicalDatabase());

		// set hierarchy
		containerTosave.setHierarchyId(containerRepository.findById(containerId)
				.orElseThrow(GlobalExceptionUtil.notFoundException(Container.class, containerId)).getHierarchyId());

		// create Query API
		Resource queryGetOne = createQueryApi(rg1.getName(), "/getone", Const.GET.getValue(), savedDataSource,
				Const.FIELD_VALUE.getValue(), Const.INSERT.getValue(), false,
				Collections.singletonList(new ResourceParameter(Const.VALUE.getValue(), Const.STRING.getValue(), null,
						Const.QUERY.getValue())), dbName);
		Resource queryGetAll = createQueryApi(rg1.getName(), "/getall", Const.GET.getValue(), savedDataSource, "{}",
				Const.INSERT.getValue(), true, null, dbName);
		Resource queryPost = createQueryApi(rg1.getName(), "/add", Const.POST.getValue(), savedDataSource, "{}",
				Const.INSERT.getValue(), true, null, dbName);
		Resource queryPut = createQueryApi(rg1.getName(), "/update", Const.PUT.getValue(), savedDataSource,
				Const.FIELD_VALUE.getValue(), Const.UPDATE.getValue(), true,
				Collections.singletonList(new ResourceParameter(Const.VALUE.getValue(), Const.STRING.getValue(), null,
						Const.QUERY.getValue())), dbName);
		Resource queryDeleteOne = createQueryApi(rg1.getName(), "/deleteone", Const.DELETE.getValue(), savedDataSource,
				Const.FIELD_VALUE.getValue(), Const.INSERT.getValue(), false,
				Collections.singletonList(new ResourceParameter(Const.VALUE.getValue(), Const.STRING.getValue(), null,
						Const.QUERY.getValue())), dbName);
		Resource queryDeleteAll = createQueryApi(rg1.getName(), "/deleteall", Const.DELETE.getValue(), savedDataSource,
				"{}", Const.INSERT.getValue(), true, null, dbName);

		// create XSL API
		Resource xsl = createXslApi(rg2.getName(), containerId);

		// create Thymeleaf API
		Resource thymeleaf = createThymeleafApi(rg3.getName(), containerId);

		// create Freemarker API
		Resource freemarker = createFreemarkerApi(rg4.getName(), containerId);

		// Security APIs
		containerTosave.setResources(createAuthGroup(savedDataSource, security.getName(), dbName));

		// Query APIs
		containerTosave.getResources().add(queryGetOne);
		containerTosave.getResources().add(queryGetAll);
		containerTosave.getResources().add(queryPost);
		containerTosave.getResources().add(queryPut);
		containerTosave.getResources().add(queryDeleteOne);
		containerTosave.getResources().add(queryDeleteAll);

		// Transformation APIs
		containerTosave.getResources().add(xsl);
		containerTosave.getResources().add(thymeleaf);
		containerTosave.getResources().add(freemarker);

		// save container finally
		containerRepository.save(containerTosave);

		// Analytics
		analyticsService.updateContainerMetrics(containerTosave);
	}

	private Resource createXslApi(String rg, String containerId) {

		Resource xsl = new Resource();
		xsl.setResourceGroup(rg);
		xsl.setHttpMethod(Const.POST.getValue());
		xsl.setPath("/xsl");
		xsl.setExecutionType(ResourceType.XSL.name());

		List<String> securityLevel = new ArrayList<>();
		securityLevel.add(Const.PUBLIC.getValue());
		xsl.setSecurityLevel(securityLevel);

		// add body
		List<ResourceParameter> parameters = new ArrayList<>();
		ResourceParameter rp = new ResourceParameter();
		rp.setIn("Body");
		rp.setName("body");
		rp.setType(Const.STRING.getValue());
		rp.setValue(
				"<?xml version='1.0' encoding='ISO-8859-1'?> <catalog xmlns:foo='http://www.foo.org/' xmlns:bar='http://www.bar.org'> <foo:cd> <title>Empire Burlesque</title> <artist>Bob Dylan</artist> <country>USA</country> <company>Columbia</company> <price>10.90</price> <bar:year>1985</bar:year> <img>./assets/img/bob.jpg</img> </foo:cd> <foo:cd> <title>Hide your heart</title> <artist>Bonnie Tyler</artist> <country>UK</country> <company>CBS Records</company> <price>9.90</price> <bar:year>1988</bar:year> <img>./assets/img/bonnie.jpg</img> </foo:cd> <foo:cd> <title>Greatest Hits</title> <artist>Dolly Parton</artist> <country>USA</country> <company>RCA</company> <price>9.90</price> <bar:year>1982</bar:year> <img>./assets/img/dolly.jpg</img> </foo:cd> </catalog>");
		parameters.add(rp);
		xsl.setParameters(parameters);

		// primary file
		ResourceFile primaryResourceFile = new ResourceFile();
		String filePath = "transformation/transformation/xsl/catalog.xsl";
		String fileId = filesHandler.getResource(containerId, filePath).getObjectId().toString();
		primaryResourceFile.setFileUri(filePath);
		primaryResourceFile.setFileId(fileId);
		xsl.setResourceFile(primaryResourceFile);

		// secondary files
		// CSS
		ResourceFile secondaryFileCss = new ResourceFile();
		String filePathCss = "transformation/transformation/xsl/assets/css/mycss.css";
		String fileIdCss = filesHandler.getResource(containerId, filePathCss).getObjectId().toString();
		secondaryFileCss.setFileUri(filePathCss);
		secondaryFileCss.setFileId(fileIdCss);
		xsl.getSecondaryFilePaths().add(secondaryFileCss);

		// JS
		ResourceFile secondaryFileJs = new ResourceFile();
		String filePathJs = "transformation/transformation/xsl/assets/js/myjs.js";
		String fileIdJs = filesHandler.getResource(containerId, filePathJs).getObjectId().toString();
		secondaryFileJs.setFileUri(filePathJs);
		secondaryFileJs.setFileId(fileIdJs);
		xsl.getSecondaryFilePaths().add(secondaryFileJs);

		addCommonConfig(xsl);

		return xsl;
	}

	private void addCommonConfig(Resource res) {

		res.setConsumes(Collections.singletonList("application/json"));
		res.setProduces(Collections.singletonList("application/json"));

		List<APIResponse> responses = new ArrayList<>();
		responses.add(new APIResponse("200", "Ok"));
		responses.add(new APIResponse("401", "Unauthorized"));
		responses.add(new APIResponse("403", "Forbidden"));

		res.setResponses(responses);

	}

	private Resource createThymeleafApi(String rg, String containerId) {

		Resource thymeleaf = new Resource();
		thymeleaf.setResourceGroup(rg);
		thymeleaf.setHttpMethod(Const.POST.getValue());
		thymeleaf.setPath("/thymeleaf");
		thymeleaf.setExecutionType(ResourceType.Thymeleaf.name());

		List<String> securityLevel = new ArrayList<>();
		securityLevel.add(Const.PUBLIC.getValue());
		thymeleaf.setSecurityLevel(securityLevel);

		// add body
		List<ResourceParameter> parameters = new ArrayList<>();
		ResourceParameter rp = new ResourceParameter();
		rp.setIn("Body");
		rp.setName("body");
		rp.setType(Const.STRING.getValue());
		rp.setValue("{\r\n" + "\"message\" : \"Code Oncer\",\r\n"
				+ "\"subscriptionDate\" : \"until December 2021\",\r\n" + "\"hobbies\": \r\n" + "[ \r\n"
				+ "\"Swimming\",\"Football\", \"Coding\"				\r\n" + "]\r\n" + "}");
		parameters.add(rp);
		thymeleaf.setParameters(parameters);

		// primary file
		ResourceFile primaryResourceFile = new ResourceFile();
		String filePath = "transformation/transformation/thyme/test.html";
		String fileId = filesHandler.getResource(containerId, filePath).getObjectId().toString();
		primaryResourceFile.setFileUri(filePath);
		primaryResourceFile.setFileId(fileId);
		thymeleaf.setResourceFile(primaryResourceFile);

		// secondary files
		// CSS
		ResourceFile secondaryFileCss = new ResourceFile();
		String filePathCss = "transformation/transformation/thyme/assets/css/myCss.css";
		String fileIdCss = filesHandler.getResource(containerId, filePathCss).getObjectId().toString();
		secondaryFileCss.setFileUri(filePathCss);
		secondaryFileCss.setFileId(fileIdCss);
		thymeleaf.getSecondaryFilePaths().add(secondaryFileCss);

		addCommonConfig(thymeleaf);

		return thymeleaf;
	}

	private Resource createFreemarkerApi(String rg, String containerId) {
		Resource freemarker = new Resource();
		freemarker.setResourceGroup(rg);
		freemarker.setHttpMethod(Const.POST.getValue());
		freemarker.setPath("/freemarker");
		freemarker.setExecutionType(ResourceType.FreeMarker.name());

		List<String> securityLevel = new ArrayList<>();
		securityLevel.add(Const.PUBLIC.getValue());
		freemarker.setSecurityLevel(securityLevel);

		// add body
		List<ResourceParameter> parameters = new ArrayList<>();
		ResourceParameter rp = new ResourceParameter();
		rp.setIn("Body");
		rp.setName("body");
		rp.setType(Const.STRING.getValue());
		rp.setValue(
				"{ \"country\": \"France\", \"cities\": [ { \"id\":\"1\", \"name\":\"Lyon\", \"population\":\"100000\" }, { \"id\":\"2\", \"name\":\"Paris\", \"population\":\"130000\" } ,{ \"id\":\"2\", \"name\":\"Nice\", \"population\":\"30000\" } ,{ \"id\":\"4\", \"name\":\"Lille\", \"population\":\"90000\" } ,{ \"id\":\"5\", \"name\":\"Nantes\", \"population\":\"70000\" } ] }");
		parameters.add(rp);
		freemarker.setParameters(parameters);

		// primary file
		ResourceFile primaryResourceFile = new ResourceFile();
		String filePath = "transformation/transformation/freemarker/main.ftl";
		String fileId = filesHandler.getResource(containerId, filePath).getObjectId().toString();
		primaryResourceFile.setFileUri(filePath);
		primaryResourceFile.setFileId(fileId);
		freemarker.setResourceFile(primaryResourceFile);

		// secondary files
		// CSS
		ResourceFile secondaryFileCss = new ResourceFile();
		String filePathCss = "transformation/transformation/freemarker/assets/css/mycss.css";
		String fileIdCss = filesHandler.getResource(containerId, filePathCss).getObjectId().toString();
		secondaryFileCss.setFileUri(filePathCss);
		secondaryFileCss.setFileId(fileIdCss);
		freemarker.getSecondaryFilePaths().add(secondaryFileCss);

		// JS
		ResourceFile secondaryFileJs = new ResourceFile();
		String filePathJs = "transformation/transformation/freemarker/assets/js/myjs.js";
		String fileIdJs = filesHandler.getResource(containerId, filePathJs).getObjectId().toString();
		secondaryFileJs.setFileUri(filePathJs);
		secondaryFileJs.setFileId(fileIdJs);
		freemarker.getSecondaryFilePaths().add(secondaryFileJs);

		addCommonConfig(freemarker);

		return freemarker;
	}

	public Resource createQueryApi(String rgName, String path, String httpMethod, DBSourceDto savedDbSource,
			String queryContent, String type, boolean many, List<ResourceParameter> params, String dbName) {
		Resource query = new Resource();
		query.setResourceGroup(rgName);
		query.setHttpMethod(httpMethod);
		query.setPath(path);
		query.setExecutionType(ResourceType.Query.name());

		List<String> securityLevel = new ArrayList<>();
		if (path.equals("/allusers") || path.equals("/grant") || path.equals("/allroles") || path.equals("/activate")
				|| path.equals("/deleteuser")) {
			securityLevel.add(Const.ADMIN.getValue());
		} else if (path.equals("/updateuser")) {
			securityLevel.add("all");
		} else if (path.equals("/me")) {
			securityLevel.add(SecurityLevel.ALL.name().toLowerCase());
		} else {
			securityLevel.add(Const.PUBLIC.getValue());
		}
		query.setSecurityLevel(securityLevel);

		CustomQuery customQuery = new CustomQuery();
		customQuery.setDatasource(savedDbSource.getId());
		customQuery.setDatabase(dbName);
		if (rgName.equals("Authentication Grizzly")) {
			customQuery.setCollectionName("authentication_user");

		} else {
			customQuery.setCollectionName("demo");
		}
		customQuery.setType(type);
		customQuery.setQuery(queryContent);
		customQuery.setMany(many);
		query.setCustomQuery(customQuery);

		if (params != null) {
			query.setParameters(params);
		}

		addCommonConfig(query);

		return query;
	}

	private String getCurrentUsername() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication.getName();
	}

	public List<Resource> createAuthGroup(DBSourceDto savedDataSource, String groupName, String dbName) {

		List<Resource> resources = new ArrayList<>();

		// create security APIs
		Resource signIn = createQueryApi(groupName, "/signin", Const.POST.getValue(), savedDataSource, "{}",
				Const.INSERT.getValue(), false, null, dbName);
		Resource signUp = createQueryApi(groupName, "/signup", Const.POST.getValue(), savedDataSource, "{}",
				Const.INSERT.getValue(), false, null, dbName);
		Resource getAllUsers = createQueryApi(groupName, "/allusers", Const.GET.getValue(), savedDataSource, "{}",
				Const.INSERT.getValue(), true, null, dbName);
		Resource getAllRoles = createQueryApi(groupName, "/allroles", Const.GET.getValue(), savedDataSource, "{}",
				Const.INSERT.getValue(), true, null, dbName);
		Resource grantRole = createQueryApi(groupName, "/grant", Const.PUT.getValue(), savedDataSource,
				Const.USERNAME_USERNAME.getValue(), Const.UPDATE.getValue(), false,
				Collections.singletonList(new ResourceParameter(Const.USERNAME.getValue(), Const.STRING.getValue(),
						null, Const.QUERY.getValue())), dbName);
		Resource activateUser = createQueryApi(groupName, "/activate", Const.POST.getValue(), savedDataSource,
				Const.USERNAME_USERNAME.getValue(), Const.UPDATE.getValue(), false,
				Collections.singletonList(new ResourceParameter(Const.USERNAME.getValue(), Const.STRING.getValue(),
						null, Const.QUERY.getValue())), dbName);
		Resource updateUser = createQueryApi(groupName, "/updateuser", Const.PUT.getValue(), savedDataSource,
				Const.USERNAME_USERNAME.getValue(), Const.UPDATE.getValue(), false,
				Collections.singletonList(new ResourceParameter(Const.USERNAME.getValue(), Const.STRING.getValue(),
						null, Const.QUERY.getValue())), dbName);

		Resource deleteUser = createQueryApi(groupName, "/deleteuser", Const.DELETE.getValue(), savedDataSource,
				Const.USERNAME_USERNAME.getValue(), Const.INSERT.getValue(), false,
				Collections.singletonList(new ResourceParameter(Const.USERNAME.getValue(), Const.STRING.getValue(),
						null, Const.QUERY.getValue())), dbName);

		Resource meUser = createQueryApi(groupName, "/me", Const.GET.getValue(), savedDataSource,
				PredefinedQuery.FIND_BY_SESSION_USERNAME.getValue(), Const.INSERT.getValue(), false, null, dbName);

		resources.add(signIn);
		resources.add(signUp);
		resources.add(getAllRoles);
		resources.add(getAllUsers);
		resources.add(activateUser);
		resources.add(grantRole);
		resources.add(updateUser);
		resources.add(deleteUser);
		resources.add(meUser);

		return resources;

	}

}
