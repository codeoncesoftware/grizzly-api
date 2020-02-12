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
package fr.codeonce.grizzly.runtime.service.query.authentication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bson.Document;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.mongodb.MongoClient;

import fr.codeonce.grizzly.common.runtime.RuntimeQueryRequest;
import fr.codeonce.grizzly.runtime.service.query.QueryHandler;
import fr.codeonce.grizzly.runtime.util.DocumentHexIdHandler;

@Service
public class SignUpHandler {

	private static final String MESSAGE = "message";

	private static final String USERNAME = "username";
	
	@Value("${frontUrl}")
	private String url;
	
	@Autowired
	private QueryHandler queryHandler;
	
	@Autowired
	private EmailServiceAuth emailService;

	@SuppressWarnings("unchecked")
	public Object handleSignUp(RuntimeQueryRequest queryRequest, String databaseName, MongoClient mClient,
			HttpServletRequest req, HttpServletResponse res, String parsedBody)
			throws ParseException, IOException {

		String body = parsedBody;
		JSONParser parser = new JSONParser();
		JSONObject bodyJson = (JSONObject) parser.parse(body);
		if(verifyFields(bodyJson) != null) {
			res.setStatus(400);
			return verifyFields(bodyJson);
		}
		String username = (String) bodyJson.get(USERNAME);
		JSONObject query = new JSONObject();
		query.put(USERNAME, username);
		queryRequest.setQuery(query.toString());
		queryRequest.setHttpMethod("GET");
		Object findResult = queryHandler.handleFindQuery(queryRequest, databaseName, mClient, req, res);
		// verify unicity
		if (!findResult.toString().equals("[]")) {
			res.setStatus(302);
			return new Document(MESSAGE,"User with current username already exists");
		} else {
			return insertRequest(queryRequest, databaseName, mClient, bodyJson);
		}

	}

	@SuppressWarnings("unchecked")
	private Object insertRequest(RuntimeQueryRequest queryRequest, String databaseName, MongoClient mClient,
			JSONObject bodyJson) {
		List<Document> result = new ArrayList<>();
		if (mClient != null) {
			bodyJson.put("roles", Arrays.asList("user"));
			bodyJson.put("enabled", false);
			String body = bodyJson.toString();
			MongoTemplate mongoTemplate = new MongoTemplate(mClient, databaseName);
			// Test if the Given Query have Multiple Objects
			if (body.length() > 1) {
				if (body.substring(0, 1).equals("[")) {
					JsonArray jsonArray = new JsonParser().parse(body).getAsJsonArray();
					jsonArray.forEach(element -> {
						String jsonToInsert = element.toString();
						Document document = mongoTemplate.insert(Document.parse(jsonToInsert),
								queryRequest.getCollectionName());
						result.add(DocumentHexIdHandler.transformMongoHexID(document));
					});
				} else {
					Document document = mongoTemplate.insert(Document.parse(body),
							queryRequest.getCollectionName());
					result.add(DocumentHexIdHandler.transformMongoHexID(document));
				}
			}

		}

		return result.size() == 1 ? result.get(0) : result;
	}
	
	Document verifyFields(JSONObject bodyJson) {
		
		if(bodyJson.get(USERNAME) == null) {
			return new Document(MESSAGE, "The username field is required");
		} else if(bodyJson.get("password") == null) {
			return new Document(MESSAGE, "The password field is required");
		} else if(bodyJson.get("email") == null) {
			return new Document(MESSAGE, "The email field is required");
		} else if(bodyJson.get("firstname") == null) {
			return new Document(MESSAGE,"The firstname field is required");
		} else if(bodyJson.get("lastname") == null) {
			 return new Document(MESSAGE,"The lastname field is required");
		} else if(bodyJson.get("phone") == null) {
			return new Document(MESSAGE, "The phone field is required");
		} 
		return null;
	}
	
	public void confirmRegistration(String userEmail) {

		String token = "";
		String subject = "Email confirmation TEST";
		String confirmUrl = url + "/confirm/email/" + token;
		// Get HTML After Processing the THYMELEAF File
		String content = "Activate your account with this link : "  + confirmUrl;

		emailService.send(content, subject, userEmail);

	}

}
