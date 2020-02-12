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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.stereotype.Service;

import com.mongodb.MongoClient;

import fr.codeonce.grizzly.common.runtime.RuntimeQueryRequest;
import fr.codeonce.grizzly.runtime.service.query.QueryHandler;
import fr.codeonce.grizzly.runtime.util.DocumentHexIdHandler;

@Service
public class AllUsersHandler {
	@Autowired
	private QueryHandler queryHandler;

	public List<Document> handleAllUsers(RuntimeQueryRequest queryRequest, String databaseName, MongoClient mClient,
			HttpServletRequest req) {

		// The Query String as a MongoQuery Object
		Map<String, String> finalQuery = queryHandler.parseQuery(queryRequest, queryRequest.getQuery(), mClient, databaseName, req);
		// List for the Result Elements
		List<Document> result = new ArrayList<>();

		if (mClient != null) {
			MongoTemplate mongoTemplate = new MongoTemplate(mClient, databaseName);
			// Get DB Query
			BasicQuery query = new BasicQuery(finalQuery.get("Query"));
			mongoTemplate.executeQuery(query, queryRequest.getCollectionName(),
					document -> result.add(DocumentHexIdHandler.transformMongoHexID(document)));
		}

		result.forEach(element -> element.remove("password"));
		queryRequest.setReturnType("application/json");

		return result;
	}
}
