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
package fr.codeonce.grizzly.core.service.swagger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import fr.codeonce.grizzly.core.domain.container.Container;
import fr.codeonce.grizzly.core.domain.datasource.DBSource;
import fr.codeonce.grizzly.core.domain.datasource.DBSourceRepository;
import fr.codeonce.grizzly.core.domain.project.Project;
import fr.codeonce.grizzly.core.domain.project.ProjectRepository;
import fr.codeonce.grizzly.core.domain.resource.APIResponse;
import fr.codeonce.grizzly.core.domain.resource.Resource;
import fr.codeonce.grizzly.core.domain.resource.ResourceFile;
import fr.codeonce.grizzly.core.domain.resource.ResourceGroup;
import fr.codeonce.grizzly.core.domain.resource.ResourceParameter;
import fr.codeonce.grizzly.core.domain.resource.ResourceType;
import fr.codeonce.grizzly.core.service.swagger.utils.IDefinitionGenerator;
import fr.codeonce.grizzly.core.service.swagger.utils.ParameterFactory;
import fr.codeonce.grizzly.core.service.util.GlobalExceptionUtil;
import io.swagger.models.HttpMethod;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Scheme;
import io.swagger.models.Swagger;
import io.swagger.models.auth.ApiKeyAuthDefinition;
import io.swagger.models.auth.In;
import io.swagger.models.auth.SecuritySchemeDefinition;
import io.swagger.models.parameters.Parameter;

@Service
public class SwaggerMapperService extends SwaggerMapperImpl {

	@Autowired
	Environment environment;

	@Autowired
	private IDefinitionGenerator definitionGenerator;

	@Autowired
	private ProjectRepository projectRepository;
	
	@Autowired
	private DBSourceRepository dbRepository;

	private ParameterFactory factory = ParameterFactory.getInstance();

	private static final String XNAME = "x-name";
	private static final String AUTH_GRIZZLY = "Authentication Grizzly";

	
	private static final Logger log = LoggerFactory.getLogger(SwaggerMapperService.class);

	/**
	 * Map a Container To a Swagger Object
	 */
	public Swagger mapToSwagger(Container container, String type) {

		Project project = projectRepository.findById(container.getProjectId())
				.orElseThrow(GlobalExceptionUtil.notFoundException(DBSource.class, container.getProjectId()));

		Swagger swagger = super.mapToSwagger(container);
		swagger.getInfo().setDescription("Container description");
		swagger.getInfo().setTitle(project.getName());
		swagger.setTags(container.getResourceGroups().stream().map(super::mapToTag).collect(Collectors.toList()));
		swagger.setPaths(fetchPathsFromContainer(container, type));
		swagger.setResponses(fetchResponsesFromContainer(container));
		swagger.setHost(
				environment.getProperty("frontUrl").substring(environment.getProperty("frontUrl").indexOf("//") + 2));
		swagger.setBasePath("/runtime/" + container.getId());
		swagger.setSchemes(List.of(Scheme.HTTPS, Scheme.HTTP));

		// Set Definitions
		Map<String, Model> map = new HashMap<>();
		map.put("signUp", definitionGenerator.getSignUp());
		map.put("signIn", definitionGenerator.getSignIn());
		swagger.setDefinitions(map);

		// Set Security Definitions
		Map<String, SecuritySchemeDefinition> security = new HashMap<>();
		ApiKeyAuthDefinition apiKeyDef = new ApiKeyAuthDefinition().in(In.HEADER).name("Authorization");
		apiKeyDef.setDescription(
				"Standard Authorization header using the Bearer scheme. \n\n Value example: \"Bearer {token}\"");
		security.put("api_key", apiKeyDef);
		swagger.setSecurityDefinitions(security);
		return swagger;
	}

	@Override
	public Resource mapToResource(Operation operation) {
		List<APIResponse> responsesList = new ArrayList<>();
		Resource resource = super.mapToResource(operation);
		try {
			if (operation.getResponses() != null) {
				operation.getResponses().forEach((code, response) -> {
					APIResponse apiResponse = new APIResponse();
					apiResponse.setCode(code);
					apiResponse.setDescription(response.getDescription());
					responsesList.add(apiResponse);
				});
			}
		} catch (Exception e) {
			log.debug("an error has occured {}", e);
		}
		resource.setResponses(responsesList);
		return resource;
	}

	/**
	 * Add the Responses List to the Operation Object after Mapping
	 */
	@Override
	public Operation mapToOperation(Resource resource) {
		Operation operation = new Operation();
		Map<String, Response> responsesList = new HashMap<>();
		resource.getResponses().stream().forEach(response -> {
			Response responseToReturn = new Response();
			responseToReturn.setDescription(response.getDescription());
			responsesList.put(response.getCode(), responseToReturn);
		});
		operation.setResponses(responsesList);
		return operation;
	}

	private Map<String, Response> fetchResponsesFromContainer(Container container) {
		Map<String, Response> responses = new HashMap<>();
		container.getResources().forEach(resource -> {
			Operation operation = super.mapToOperation(resource);
			if (operation.getResponses() != null) {
				operation.getResponses().forEach((k, v) -> responses.put(k, v));
			}
		});
		return responses;
	}

	/**
	 * Fetch Swagger Paths from a Container
	 * 
	 * @param container
	 * @return
	 */
	private Map<String, Path> fetchPathsFromContainer(Container container, String type) {

		Map<String, Path> paths = new HashMap<>();
		container.getResources().forEach(resource -> {
			Path path = new Path();
				Operation operation = setOperationFields(resource, type);
				if (operation.getResponses() == null || operation.getResponses().size() == 0) {
					Response res = new Response();
					res.description("OK");
					operation.setResponses(Collections.singletonMap("200", res));
				}
				if (ObjectUtils.isEmpty(paths.get(resource.getPath().toLowerCase()))) {
					path.set(resource.getHttpMethod().toLowerCase(), operation);
					paths.put(resource.getPath().toLowerCase(), path);
				} else {
					paths.get(resource.getPath().toLowerCase()).set(resource.getHttpMethod().toLowerCase(), operation);;
				}
			});

		return paths;
	}

	/**
	 * Set Operation Object Fields from the Given Resource Object
	 * 
	 * @param resource
	 * @return
	 */
	private Operation setOperationFields(Resource resource, String type) {

		Operation operation = super.mapToOperation(resource);
		operation.setTags(Collections.singletonList(resource.getResourceGroup()));
		/*
		 * Calling Factory to Get the suitable Parameter depending on parameter In field
		 */
		operation.setParameters(
				resource.getParameters().stream().map(factory::makeParameter).collect(Collectors.toList()));
		if (resource.getCustomQuery() != null && (resource.getExecutionType().equalsIgnoreCase("query")
				&& (resource.getHttpMethod().equalsIgnoreCase("post")
						|| resource.getHttpMethod().equalsIgnoreCase("put")))) {
			if ((resource.getCustomQuery().getType() != null
					&& (resource.getCustomQuery().getType().equalsIgnoreCase("Insert")
							|| resource.getCustomQuery().getType().equalsIgnoreCase("Update")))
					&& !containsBodyParam(operation.getParameters(), "Body")
					&& resource.getParameters().stream().noneMatch(param -> param.getType().equalsIgnoreCase("file"))) {

				// Attach Sign-In and Sign-Up models to AUTH APIs
				if (resource.getResourceGroup().equals(AUTH_GRIZZLY) && resource.getPath().equals("/signup")) {
					operation.getParameters().add(factory.getModelDef("signup"));
				} else if (resource.getResourceGroup().equals(AUTH_GRIZZLY) && resource.getPath().equals("/signin")) {
					operation.getParameters().add(factory.getModelDef("signin"));
				} else {
					ResourceParameter param = new ResourceParameter();
					param.setIn("Body");
					param.setType("String");
					param.setName("body");
					operation.getParameters().add(factory.makeParameter(param));
				}

			}

		}
		// Setting Responses
		Map<String, Response> responses = new HashMap<>();
		resource.getResponses()
				.forEach(resp -> responses.put(resp.getCode(), new Response().description(resp.getDescription())));
		operation.setResponses(responses);

		// Set Security Attribute
		if (!resource.getSecurityLevel().isEmpty() && !resource.getSecurityLevel().get(0).equals("public")) {
			Map<String, List<String>> map = new HashMap<>();
			map.put("api_key", new ArrayList<String>());
			List<Map<String, List<String>>> list = new ArrayList<>();
			list.add(map);
			operation.setSecurity(list);
		} else {
			operation.setSecurity(null);
		}

		// Add Custom Fields If Swagger Is For Dev Mode
		if (type.equalsIgnoreCase("dev")) {
			if (resource.getName() != null) {
				operation.setVendorExtension(XNAME, resource.getName());
			}
			operation.setVendorExtension("x-executionType", resource.getExecutionType());
			/* Fetch associated fileID from files collection */
			if (resource.getResourceFile() != null && resource.getResourceFile().getFileId() != null
					&& !resource.getResourceFile().getFileId().isEmpty()) {

				/* Add Custom fields to Swagger for both ContainerId and FileUri */
				operation.getVendorExtensions().put("x-fileId", resource.getResourceFile().getFileId());
				operation.getVendorExtensions().put("x-fileUri", resource.getResourceFile().getFileUri());
			}
			if (resource.getExecutionType() != null && resource.getExecutionType().equals("Query")) {
				operation.getVendorExtensions().put("x-type", resource.getCustomQuery().getType());
				operation.getVendorExtensions().put("x-collectionName", resource.getCustomQuery().getCollectionName());
				operation.getVendorExtensions().put("x-query", resource.getCustomQuery().getQuery());
				operation.getVendorExtensions().put("x-query-many", resource.getCustomQuery().isMany());
				// Set Projection Fields
				if (resource.getFields() != null) {
					StringBuilder fields = new StringBuilder();
					resource.getFields().stream().forEach(field -> fields.append(field + ','));
					operation.getVendorExtensions().put("x-fields", fields.toString());
				}
			}
		}

		return operation;
	}

	/**
	 * Check if an Operation Contains Parameter with the same given Name
	 * 
	 * @param list
	 * @param name
	 * @return Found or Not
	 */
	private boolean containsBodyParam(final List<Parameter> list, final String name) {
		return list.stream().map(Parameter::getName).filter(name::equalsIgnoreCase).findFirst().isPresent();
	}
	
	public Container mapToContainer(Swagger swagger, String projectId) throws Exception {
		Container container = mapToContainer(swagger);
		this.projectRepository.findById(projectId).ifPresent(project -> {
			this.dbRepository.findById(project.getDbsourceId()).ifPresent(db -> {
				String databaseName;
				if (db.getConnectionMode().equalsIgnoreCase("FREE")) {
					databaseName = db.getPhysicalDatabase();
				} else {
					databaseName = project.getDatabaseName();
				}
				fetchContainerResources(swagger, container, project.getDbsourceId(), databaseName);
			});
		});
		
		return container;
	}

	@Override
	public Container mapToContainer(Swagger swagger) throws Exception {

		if (swagger != null) {
			Container container = super.mapToContainer(swagger);
			List<ResourceGroup> lrg = new ArrayList<>();
			if (swagger.getTags() != null) {
				swagger.getTags().stream().map(x -> {
					ResourceGroup rg = new ResourceGroup();
					rg.setName(x.getName());
					rg.setDescription(x.getDescription());
					lrg.add(rg);
					return x;
				}).collect(Collectors.toList());
			}
			container.setResourceGroups(lrg);

			// Delete Existing "Authentication Grizzly" Resource Group
			container.setResourceGroups(container.getResourceGroups().stream()
					.filter(rg -> !rg.getName().equalsIgnoreCase("Authentication Grizzly"))
					.collect(Collectors.toList()));

			return container;
		} else {
			throw new Exception("Swagger Format is invalid");
		}

	}

	private void fetchContainerResources(Swagger swagger, Container container, String dbSourceID, String dbName) {
		List<Resource> resources = new ArrayList<>();
		if (swagger.getPaths() != null) {
			swagger.getPaths().forEach((pathName, pathDetails) -> {
				if (pathDetails.getOperationMap() != null) {
					pathDetails.getOperationMap().forEach((httpMethod, operation) -> {
						if (!operation.getTags().contains("Authentication Grizzly")) {
							Resource resource = setResourceFields(operation, pathName, httpMethod, dbSourceID, dbName);
							resources.add(resource);
						}
					});
				}
			});
		}

		container.setResources(resources);
	}

	/**
	 * Set Resource Object Fields From the Given Operation Object
	 * 
	 * @param operation
	 * @param pathName
	 * @param httpMethod
	 * @return
	 */
	private Resource setResourceFields(Operation operation, String pathName, HttpMethod httpMethod, String dbSourceID, String dbName) {
		Resource resource = mapToResource(operation);
		resource.setPath(pathName);
		resource.setHttpMethod(httpMethod.toString());

		if (operation.getTags() != null) {
			resource.setResourceGroup(operation.getTags().get(0));
		}
		resource.setName(extractVendorField(operation, XNAME));

		String executionType = extractVendorField(operation, "x-executionType");
		if (executionType == null) {
			resource.setExecutionType("");
		} else {
			resource.setExecutionType(executionType);
		}

		if (extractVendorField(operation, "x-fileID") != null) {
			resource.setResourceFile(new ResourceFile(extractVendorField(operation, "x-fileID"),
					extractVendorField(operation, "x-fileUri")));
		}
		// For Query API
		if (executionType != null && executionType.equals(ResourceType.Query.name())) {
			resource.getCustomQuery().setType(extractVendorField(operation, "x-type"));
			resource.getCustomQuery().setDatasource(dbSourceID);
			resource.getCustomQuery().setDatabase(dbName);
			resource.getCustomQuery().setCollectionName(extractVendorField(operation, "x-collectionName"));
			resource.getCustomQuery().setQuery(extractVendorField(operation, "x-query"));
			resource.getCustomQuery().setMany(Boolean.valueOf(extractVendorField(operation, "x-query-many")));
			// Set Projection Fields
			String fields = extractVendorField(operation, "x-fields");
			if (fields != null) {
				resource.setFields(Arrays.asList(fields.split("\\,")));
				if (resource.getFields().get(resource.getFields().size() - 1).equals(",")) {
					resource.getFields().remove(resource.getFields().size() - 1);
				}
			}
		}
		if (operation.getParameters() != null) {
			resource.setParameters(operation.getParameters().stream().map(factory::makeResourceParameter)
					.collect(Collectors.toList()));
		}

		// Set Security Level
		if (operation.getSecurity() != null && !operation.getSecurity().isEmpty()) {
			resource.setSecurityLevel(new ArrayList<String>(Arrays.asList("admin")));
		} else {
			resource.setSecurityLevel(new ArrayList<String>(Arrays.asList("public")));
		}

		return resource;
	}

	/**
	 * Extract Fields From Operation Object in a Safe Mode
	 * 
	 * @param operation
	 * @param key
	 * @return the Value or null
	 */
	private String extractVendorField(Operation operation, String key) {
		Map<String, Object> vendorExtensions = operation.getVendorExtensions();
		if (vendorExtensions != null) {
			Object value = vendorExtensions.get(key);
			if (value != null) {
				return value.toString();
			}
		}
		return null;
	}

}
