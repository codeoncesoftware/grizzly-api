/*-
 * ========================LICENSE_START=================================
 * grizzly-api-runtime
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
package fr.codeonce.grizzly.runtime.service.query;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.mongodb.MongoClient;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.result.UpdateResult;

import fr.codeonce.grizzly.common.runtime.RuntimeQueryRequest;
import fr.codeonce.grizzly.common.runtime.resource.RuntimeResourceParameter;
import fr.codeonce.grizzly.core.service.datasource.cache.ConnectionCacheService;
import fr.codeonce.grizzly.runtime.service.query.authentication.AllUsersHandler;
import fr.codeonce.grizzly.runtime.service.query.authentication.RolesHandler;
import fr.codeonce.grizzly.runtime.service.query.authentication.SignInHandler;
import fr.codeonce.grizzly.runtime.service.query.authentication.SignUpHandler;
import fr.codeonce.grizzly.runtime.service.query.authentication.UpdateUserHandler;
import fr.codeonce.grizzly.runtime.util.DocumentHexIdHandler;

@Service
public class QueryHandler {

	private static final String QUERY = "query";

	@Autowired
	private ConnectionCacheService connectionCacheService;

	@Autowired
	private SignInHandler signInHandler;

	@Autowired
	private SignUpHandler signUpHandler;

	@Autowired
	private RolesHandler rolesHandler;

	@Autowired
	private UpdateUserHandler updateUserHandler;

	@Autowired
	private AllUsersHandler allUsersHandler;

	@Autowired
	private MappingJackson2HttpMessageConverter springMvcJacksonConverter;

	// NEED TO BE CALLED WITH FEIGN
	@Autowired
	private StaticResourceService resourceService;

	@Value("${spring.data.mongodb.uri}")
	private String atlasUri;

	@Value("${frontUrl}")
	private String frontUrl;

	@Autowired
	private SessionParamsHandler sessionParamsHandler;

	private static final Logger log = LoggerFactory.getLogger(QueryHandler.class);

	public Object handleQuery(RuntimeQueryRequest queryRequest, String containerId, HttpServletRequest req,
			HttpServletResponse res) throws IOException, ServletException, ParseException {

		MongoClient mClient;
		String databaseName;
		// Set The DB Name and The MongoClient Instance
		if (queryRequest.getConnectionMode().equalsIgnoreCase("FREE")) {
			mClient = connectionCacheService.getAtlasMongoClient();
			databaseName = queryRequest.getPhysicalDatabaseName();
		} else {
			mClient = connectionCacheService.getMongoClient(queryRequest.getDbsourceId());
			databaseName = queryRequest.getDatabaseName();
		} 

		if (req.getMethod().equalsIgnoreCase("get")) {

			if (queryRequest.getPath().equals("/allroles")) {
				return rolesHandler.handleAllRoles(queryRequest, databaseName, mClient, req, res, containerId);
			} else if (queryRequest.getPath().equals("/allusers")) {
				return allUsersHandler.handleAllUsers(queryRequest, databaseName, mClient, req);
			} else {
				return handleFindQuery(queryRequest, databaseName, mClient, req, res);
			}

		} else if (req.getMethod().equalsIgnoreCase("put") || req.getMethod().equalsIgnoreCase("post")) {
			String parsedBody = new HttpServletRequestWrapper(req).getReader().lines().reduce("",
					(accumulator, actual) -> accumulator + actual);

			if (queryRequest.getPath().equals("/activate")) {
				return handleActivatePath(queryRequest, databaseName, mClient, req, res, parsedBody);
			}

			if (verifyJsonBody(parsedBody)) {
				if (queryRequest.getPath().equals("/signin")) {
					return signInHandler.handleSignIn(queryRequest, databaseName, mClient, req, res, containerId,
							parsedBody);
				} else if (queryRequest.getPath().equals("/signup")) {
					return signUpHandler.handleSignUp(queryRequest, databaseName, mClient, req, res, parsedBody);
				} else if (queryRequest.getPath().equals("/updateuser")) {
					return updateUserHandler.handleUpdateUser(queryRequest, databaseName, mClient, req, containerId,
							parsedBody);
				} else if (queryRequest.getPath().equals("/grant")) {
					return rolesHandler.handleGrantRoles(queryRequest, databaseName, mClient, req, res, containerId,
							parsedBody);
				} else if (queryRequest.getQueryType().equalsIgnoreCase("Insert")) {
					return handleInsertQuery(queryRequest, databaseName, mClient, containerId, req, parsedBody);
				}
				return handleUpdateQuery(queryRequest, databaseName, mClient, req, parsedBody);
			} else {
				res.setStatus(400);
				return new Document("message", "Your JSON body is malformatted!");
			}
		} else if (req.getMethod().equalsIgnoreCase("delete")) {
			handleDeleteQuery(queryRequest, databaseName, mClient, req);
			return null;
		}
		// If no Method Selected
		return new ArrayList<>();

	}

	private Object handleActivatePath(RuntimeQueryRequest queryRequest, String databaseName, MongoClient mClient,
			HttpServletRequest req, HttpServletResponse res, String parsedBody) throws IOException {

		if (!StringUtils.isEmpty(parsedBody) && !verifyJsonBody(parsedBody)) {
			res.setStatus(400);
			return new Document("message", "Your JSON body is malformatted!");
		}
		return updateUserHandler.activate(queryRequest, databaseName, mClient, req, res);

	}

	// FIX the parseRequestBody Stream closed problem
	private boolean verifyJsonBody(String body) throws IOException {
		JSONParser parser = new JSONParser();
		try {
			parser.parse(body);
			return true;
		} catch (ParseException e) {
			log.debug("an error has occured {}", e);
			return false;
		}
	}

	public Object handleUpdateQuery(RuntimeQueryRequest queryRequest, String databaseName, MongoClient mongoClient,
			HttpServletRequest req, String parsedBody) {

		if (!queryRequest.getHttpMethod().equalsIgnoreCase("post")
				&& !queryRequest.getHttpMethod().equalsIgnoreCase("put")) {
			return null;
		}

		UpdateResult updateResult = null;

		if (mongoClient != null) {
			MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, databaseName);

			Map<String, String> finalQuery = parseQuery(queryRequest, queryRequest.getQuery(), mongoClient,
					databaseName, req);
			BasicQuery basicQuery = new BasicQuery(finalQuery.get(QUERY));
			// Apply Action on One Or Many
			if (!queryRequest.isMany()) {
				basicQuery.limit(1);
			}

			parsedBody = this.parseSessionParams(queryRequest, parsedBody, mongoClient, databaseName);

			Document queryBodyParsed = Document.parse(parsedBody);
			Update update = new Update();
			queryBodyParsed.forEach(update::set);

			updateResult = mongoTemplate.updateMulti(basicQuery, update, queryRequest.getCollectionName());

		}
		return updateResult;
	}

	public Object handleInsertQuery(RuntimeQueryRequest queryRequest, String databaseName, MongoClient mongoClient,
			String containerId, HttpServletRequest req, String parsedBody) throws IOException, ServletException {

		if (!queryRequest.getHttpMethod().equalsIgnoreCase("post")
				&& !queryRequest.getHttpMethod().equalsIgnoreCase("put")) {
			return null;
		}

		// Check if it is a File Upload
		Optional<RuntimeResourceParameter> paramDto = queryRequest.getParameters().stream()
				.filter(param -> param.getType().equalsIgnoreCase("file")).findFirst();

		if (paramDto.isPresent()) {
			return handleFileSave(databaseName, paramDto.get().getName(), mongoClient, containerId, req);
		}

		List<Document> result = new ArrayList<>();

		// Parse Body
		parsedBody = parseQuery(queryRequest, parsedBody, mongoClient, databaseName, req).get(QUERY);

		if (mongoClient != null) {

			MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, databaseName);
			// Test if the Given Query have Multiple Objects
			if (parsedBody.length() > 1) {
				if (parsedBody.substring(0, 1).equals("[")) {
					JsonArray jsonArray = new JsonParser().parse(parsedBody).getAsJsonArray();
					jsonArray.forEach(element -> {
						String jsonToInsert = element.toString();
						Document document = mongoTemplate.insert(Document.parse(jsonToInsert),
								queryRequest.getCollectionName());
						result.add(DocumentHexIdHandler.transformMongoHexID(document));
					});
				} else {
					Document document = mongoTemplate.insert(Document.parse(parsedBody),
							queryRequest.getCollectionName());
					result.add(DocumentHexIdHandler.transformMongoHexID(document));
				}
			}

		}

		return !queryRequest.isMany() ? convertToReturnType(queryRequest, result.get(0))
				: convertToReturnType(queryRequest, result);
	}

	/**
	 * @param resource
	 * @param req
	 * @param mongoClient
	 * @throws IOException
	 * @throws ServletException
	 */
	private JsonNode handleFileSave(String databaseName, String fileName, MongoClient mongoClient, String containerId,
			HttpServletRequest req) throws IOException, ServletException {
		String resultId;
		String result = "";
		Part filePart = req.getPart(fileName);
		InputStream fileContent = filePart.getInputStream();

		if (mongoClient != null) {
			// Test if the Given Query have Multiple Objects
			GridFsTemplate gridFsTemplate = this.connectionCacheService.getGridFs(mongoClient, databaseName);
			resultId = gridFsTemplate.store(fileContent, filePart.getSubmittedFileName()).toHexString();
			String fileUrl = frontUrl + "/runtime/static/" + containerId + "/" + resultId;
			// File URL in MetaData
			Document metaData = new Document();
			metaData.put("url", frontUrl + "/runtime/static/" + containerId + "/" + resultId);
			result = "{\"id\":\"" + resultId + "\", \"url\":\"" + fileUrl + "\"}";
		}
		return new ObjectMapper().readTree(result);
	}

	public Object handleFindQuery(RuntimeQueryRequest queryRequest, String databaseName, MongoClient mongoClient,
			HttpServletRequest req, HttpServletResponse res) {

		if (queryRequest.getExecutionType().equalsIgnoreCase("FILE")) {
			try {
				return handleGetFile(mongoClient, databaseName, req, res);
			} catch (FileNotFoundException e) {
				throw new IllegalArgumentException("File not found");
			}
		}
		// The Query String as a MongoQuery Object
		Map<String, String> finalQuery = parseQuery(queryRequest, queryRequest.getQuery(), mongoClient, databaseName,
				req);
		// List for the Result Elements
		List<Document> result = new ArrayList<>();

		if (mongoClient != null) {
			MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, databaseName);
			// Get DB Query
			BasicQuery query = new BasicQuery(finalQuery.get(QUERY));

			// Apply Action on One Or Many
			if (!queryRequest.isMany()) {
				query.limit(1);
			}
			// Set Projections
			if (queryRequest.getFields() != null && !queryRequest.getFields().isEmpty()) {
				queryRequest.getFields().forEach(field -> query.fields().include(field));
			}

			if (!queryRequest.isPageable()) {
				mongoTemplate.executeQuery(query, queryRequest.getCollectionName(),
						document -> result.add(DocumentHexIdHandler.transformMongoHexID(document)));
			} else {
				// Pagination
				Pageable pageable = PageRequest.of(Integer.parseInt(req.getParameter("pageNumber")),
						Integer.parseInt(req.getParameter("pageSize")));
				Query pageableQuery = query.with(pageable);

				mongoTemplate.executeQuery(pageableQuery, queryRequest.getCollectionName(),
						document -> result.add(DocumentHexIdHandler.transformMongoHexID(document)));
				// Result Page

				return PageableExecutionUtils.getPage(result, pageable,
						() -> mongoTemplate.count(query, queryRequest.getCollectionName()));
			}
		}
		if (result.isEmpty())
			return "[]";
		return !queryRequest.isMany() ? convertToReturnType(queryRequest, result.get(0))
				: convertToReturnType(queryRequest, result);
	}

	private GridFsResource handleGetFile(MongoClient mongoClient, String databaseName, HttpServletRequest req,
			HttpServletResponse res) throws FileNotFoundException {
		GridFsTemplate gridFsTemplate = this.connectionCacheService.getGridFs(mongoClient, databaseName);
		GridFSFile file = gridFsTemplate.findOne(Query.query((Criteria.where("_id").is(req.getParameter("id")))));
		GridFsResource resource = gridFsTemplate.getResource(file);
		try {
			resourceService.setHttpServletResponse(resource, res);
		} catch (IOException e) {
			throw new FileNotFoundException();
		}
		return null;
	}

	public void handleDeleteQuery(RuntimeQueryRequest queryRequest, String databaseName, MongoClient mongoClient,
			HttpServletRequest req) {

		if (!queryRequest.getHttpMethod().equalsIgnoreCase("delete")) {
			return;
		}
		if (queryRequest.getExecutionType().equalsIgnoreCase("FILE")) {
			handleDeleteFile(mongoClient, databaseName, req);
		}

		Map<String, String> finalQuery = parseQuery(queryRequest, queryRequest.getQuery(), mongoClient, databaseName,
				req);

		if (mongoClient != null) {
			MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, databaseName);
			BasicQuery basicQuery = new BasicQuery(finalQuery.get(QUERY));
			if (!queryRequest.isMany()) {
				basicQuery.limit(1);
			}
			mongoTemplate.remove(basicQuery, queryRequest.getCollectionName());
		}
	}

	private void handleDeleteFile(MongoClient mongoClient, String databaseName, HttpServletRequest req) {
		BasicQuery query = new BasicQuery("{\"_id\":\"" + req.getParameter("id") + "\"}");
		this.connectionCacheService.getGridFs(mongoClient, databaseName).delete(query);
	}

	/**
	 * Parse Mongo Query and Replace params with their values
	 * 
	 * @param queryRequest
	 * @param query
	 * @param mClient
	 * @param dbName
	 * @param req
	 * @return final Query to execute
	 */
	public Map<String, String> parseQuery(RuntimeQueryRequest queryRequest, String query, MongoClient mClient,
			String dbName, HttpServletRequest req) {

		Map<String, String> finalQuery = new HashMap<>();
		finalQuery.put(QUERY, query);

		List<RuntimeResourceParameter> endpointParameters = queryRequest.getParameters();

		Map<String, String> pathVariablesMap = new HashMap<>();
		if (queryRequest.getPath().contains("{")) {
			getPathVariables(queryRequest, req.getServletPath(), pathVariablesMap);
		}
		
		String updatedQueryWithSessionParams = this.parseSessionParams(queryRequest, query, mClient, dbName);

		Map<String, List<String>> headersParamsMap = Collections.list(req.getHeaderNames()).stream()
				.collect(Collectors.toMap(Function.identity(), h -> Collections.list(req.getHeaders(h))));

		finalQuery.put(QUERY, updatedQueryWithSessionParams);

		if (endpointParameters != null && !endpointParameters.isEmpty()) {
			endpointParameters.stream().forEach(param -> {
				setQueryParameters(req, finalQuery, pathVariablesMap, headersParamsMap, param);
			});
		}

		Map<String, String> paramsMap = new HashMap<>();
		paramsMap.forEach((k, v) -> {
			String key = "%" + k;
			finalQuery.put(QUERY, finalQuery.get(QUERY).replaceAll(key, v));
		});

		return finalQuery;
	}

	/**
	 * Replace all params with their corresponding values
	 * 
	 * @param req
	 * @param finalQuery
	 * @param map
	 * @param headersMap
	 * @param param
	 */
	private void setQueryParameters(HttpServletRequest req, Map<String, String> finalQuery, Map<String, String> map,
			Map<String, List<String>> headersMap, RuntimeResourceParameter param) {
		String key = "%" + param.getName();
		String reqValue = null;
		if (param.getIn().equalsIgnoreCase(QUERY)) {
			reqValue = req.getParameter(param.getName());
		} else if (param.getIn().equalsIgnoreCase("path")) {
			reqValue = map.get(param.getName());
		} else if (param.getIn().equalsIgnoreCase("header")) {
			// Get Only the First Header Value
			reqValue = headersMap.get(param.getName()).get(0);
		}
		if (reqValue == null) {
			reqValue = param.getValue();
		}
		finalQuery.put(QUERY, finalQuery.get(QUERY).replaceAll(key, reqValue));
		// For Non String Values, Eliminate the " Char
		if (!param.getType().equalsIgnoreCase("String")) {
			String valueToReplace = "\"" + req.getParameter(param.getName()) + "\"";
			finalQuery.put(QUERY,
					finalQuery.get(QUERY).replaceAll(valueToReplace, req.getParameter(param.getName())));
		}
	}

	/**
	 * Get All Path Variables Values
	 * 
	 * @param queryRequest
	 * @param req
	 * @param map,         contains all values for Path Variables
	 */
	private void getPathVariables(RuntimeQueryRequest queryRequest, String servletPath, Map<String, String> map) {
		// 16 is more then length of "/runtime/query/{containerId} to get the path of
		// the API after the ContainerID
		List<String> receivedPath = Arrays
				.asList(servletPath.substring(servletPath.indexOf('/', 16)).substring(1).split("/"));
		List<String> resspath = Arrays.asList(queryRequest.getPath().substring(1).split("/"));

		int index = 0;
		for (String part : resspath) {
			if (part.contains("{")) {
				map.put(part.replace("{", "").replace("}", ""), receivedPath.get(index));
			}
			index++;
		}
	}

	public String parseRequestBody(HttpServletRequest req) {
		String body = "";

		try {
			body = IOUtils.toString(req.getReader());

		} catch (IOException e) {
			log.debug(e.getMessage());
		}
		return body;
	}

	private Object convertToReturnType(RuntimeQueryRequest req, List<Document> obj) {
		if (req.getReturnType().toLowerCase().contains("xml")) {
			try {
				return XML.toString(new JSONArray(springMvcJacksonConverter.getObjectMapper().writeValueAsString(obj)),
						req.getCollectionName());
			} catch (JSONException | JsonProcessingException e) {
				return obj;
			}
		} else {
			return obj;
		}
	}

	private Object convertToReturnType(RuntimeQueryRequest req, Object obj) {
		if (req.getReturnType().toLowerCase().contains("xml")) {
			try {
				return XML
						.toString(new JSONObject(springMvcJacksonConverter.getObjectMapper().writeValueAsString(obj)));
			} catch (JSONException | JsonProcessingException e) {
				return obj;
			}
		} else {
			return obj;
		}

	}

	public void setSessionParamsHandler(SessionParamsHandler sessionParamsHandler) {
		this.sessionParamsHandler = sessionParamsHandler;
	}

	/**
	 * Parse the query and fill the current user members if they exist
	 * 
	 * @param req
	 * @param query
	 * @param mCliet
	 * @param dbName
	 * @return the query after members fill
	 */
	public String parseSessionParams(RuntimeQueryRequest req, String query, MongoClient mCliet, String dbName) {
		return this.sessionParamsHandler.handle(req.getUsername(), query, mCliet, dbName);
	}

}
