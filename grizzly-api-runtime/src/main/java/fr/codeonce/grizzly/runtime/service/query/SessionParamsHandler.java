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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.stereotype.Service;

import com.mongodb.MongoClient;

import fr.codeonce.grizzly.runtime.util.DocumentHexIdHandler;
import net.bytebuddy.implementation.bytecode.Throw;
/**
 * Replace the current user properties for the secured APIs
 */
@Service
public class SessionParamsHandler {

	public String handle(String username, String query, MongoClient mClient, String databaseName) {
		if (StringUtils.isNotBlank(query) && query.contains("$session_")) {

			if (query.contains("$session_username")) { // USERNAME
				query = StringUtils.replace(query, "$session_username", username);
			}
			// Check if the Query contains variables other than the $session_username
			if (query.contains("$session_")) {
				Map<String, String> attributs = getUserAttributs(databaseName, mClient, username);
				query = StringUtils.replace(query, "$session_firstname", getAttribut("firstname", attributs));
				query = StringUtils.replace(query, "$session_lastname", getAttribut("lastname", attributs));
				query = StringUtils.replace(query, "$session_phone", getAttribut("phone", attributs));
				query = StringUtils.replace(query, "$session_email", getAttribut("email", attributs));
			}
		}
		return query;
	}

	/**
	 * Return the user Fields depending on the given username
	 * 
	 * @param username
	 * @return Map<String, String> with all the retrieved properties
	 */
	public Map<String, String> getUserAttributs(String databaseName, MongoClient mClient, String username) {

		Map<String, String> attributs = new HashMap<>();

		MongoTemplate mongoTemplate = new MongoTemplate(mClient, databaseName);
		BasicQuery query = new BasicQuery("{ \"username\":\"" + username + "\"}");
		mongoTemplate.executeQuery(query, "authentication_user", document -> {
			Document result = DocumentHexIdHandler.transformMongoHexID(document);
			result.forEach((k, v) -> attributs.put(k, String.valueOf(v)));
		});

		return attributs;
	}

	/**
	 * Retrieve properties in safe mode from the given properties map
	 * 
	 * @param attribut,  to retrieve
	 * @param attributs, the user infos
	 * @return the value or {@link Throw IllegalArgumentException(String message)}
	 *         if not found
	 */
	public String getAttribut(String attribut, Map<String, String> attributs) {
		if (StringUtils.isNotBlank(attributs.get(attribut)))
			return attributs.get(attribut);
		else
			throw new IllegalArgumentException(attribut + " is not found");
	}

}
