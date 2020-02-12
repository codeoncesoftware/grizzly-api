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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;

import fr.codeonce.grizzly.common.runtime.RuntimeQueryRequest;
import fr.codeonce.grizzly.core.service.container.ContainerService;
import fr.codeonce.grizzly.core.service.project.ProjectDto;
import fr.codeonce.grizzly.core.service.project.ProjectService;
import fr.codeonce.grizzly.core.service.project.SecurityApiConfigDto;
import fr.codeonce.grizzly.runtime.service.query.QueryHandler;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class SignInHandler {

	@Autowired
	private ProjectService projectService;

	@Autowired
	private ContainerService containerService;

	@Autowired
	private QueryHandler queryHandler;

	private final ObjectMapper mapper = new ObjectMapper();

	@SuppressWarnings("unchecked")
	public Object createToken(SecurityApiConfigDto securityApiConfig, String username, List<String> roles) {
		long now = (new Date()).getTime();
		Date validity;
		validity = new Date(now + (securityApiConfig.getTokenExpiration() * 1000));
		Key key = setKey(securityApiConfig.getSecretKey());

		String token = Jwts.builder().setSubject(username).setHeader((Map<String, Object>) Jwts.header().setType("JWT"))
				.claim("auth", roles)
				.claim("iss", securityApiConfig.getClientId()).signWith(key, SignatureAlgorithm.HS256)
				.setExpiration(validity).compact();
		return new Document("token", token);

	}

	private Key setKey(String secretKey) {
		byte[] keyBytes = Decoders.BASE64.decode(secretKey);
		return Keys.hmacShaKeyFor(keyBytes);
	}

	@SuppressWarnings("unchecked")
	public Object handleSignIn(RuntimeQueryRequest resource, String databaseName, MongoClient mongoClient,
			HttpServletRequest req, HttpServletResponse res, String containerId, String parsedBody)
			throws FileNotFoundException {

		String projectId = containerService.get(containerId).getProjectId();
		ProjectDto p = projectService.get(projectId);

		resource.setQuery(parsedBody);
		espaceSignInInjection(resource.getQuery());

		resource.setHttpMethod("GET");
		Object result = queryHandler.handleFindQuery(resource, databaseName, mongoClient, req, res);
		// verify they match
		if (result.toString().equals("[]")) {
			res.setStatus(401);
			return new Document("message", "Wrong Credentials");
		} else {
			Document userDocument = (Document) result;
			String username = userDocument.get("username").toString();
			List<String> roles = (List<String>) userDocument.get("roles");
			boolean enabled = (boolean) userDocument.get("enabled");
			if (!enabled) {
				res.setStatus(201);
				return new Document("message", "Your account will be activated soon by the admin");
			}
			return createToken(p.getSecurityConfig(), username, roles);
		}
	}

	/**
	 * Parse the query and check if it is malformed or contains a MONGO operator
	 * that starts with '${'
	 * 
	 * @param query that is going to be executed
	 * @throws IOException, IllegalArgumentException
	 */
	private void espaceSignInInjection(String query) {
		try {
			mapper.enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
			JsonNode node = this.mapper.readTree(query);
			if (node.get("password") != null && node.get("password").asText().indexOf("{$") == 0) {
				throw new IllegalArgumentException();
			}
		} catch (IOException e) {
			throw new IllegalArgumentException();
		}
	}

}
