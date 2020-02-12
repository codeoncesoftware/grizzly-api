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
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.mongodb.MongoClient;
import com.mongodb.client.result.UpdateResult;

import fr.codeonce.grizzly.common.runtime.RuntimeQueryRequest;
import fr.codeonce.grizzly.runtime.service.query.QueryHandler;

@Service
public class UpdateUserHandler {

	@Autowired
	private QueryHandler queryHandler;

	public Object handleUpdateUser(RuntimeQueryRequest queryRequest, String databaseName, MongoClient mClient,
			HttpServletRequest req, String containerId, String parsedBody) {

		queryRequest.getParameters().get(0).setValue(queryRequest.getUsername());

		Document userDocument = Document.parse(parsedBody);

		UpdateResult updateResult = null;

		if (mClient != null) {
			MongoTemplate mongoTemplate = new MongoTemplate(mClient, databaseName);

			Map<String, String> finalQuery = queryHandler.parseQuery(queryRequest, queryRequest.getQuery(), mClient, databaseName, req);
			BasicQuery basicQuery = new BasicQuery(finalQuery.get("query"));
			// Apply Action on One Or Many
			if (!queryRequest.isMany()) {
				basicQuery.limit(1);
			}

			Update update = new Update();
			userDocument.forEach(update::set);

			updateResult = mongoTemplate.updateMulti(basicQuery, update, queryRequest.getCollectionName());

		}
		return updateResult;
	}

	public Object activate(RuntimeQueryRequest queryRequest, String databaseName, MongoClient mClient,
			HttpServletRequest req, HttpServletResponse res) throws FileNotFoundException {
		
		String username = "Username";
		
		Object findResult = queryHandler.handleFindQuery(queryRequest, databaseName, mClient, req, res);
		if (findResult == null || findResult.toString().equals("[]")) {
			res.setStatus(404);
			return new Document("message", "Username does not exist");
		}
		Document user = (Document) findResult;
		user.put("enabled", true);

		if (mClient != null) {
			MongoTemplate mongoTemplate = new MongoTemplate(mClient, databaseName);

			Map<String, String> finalQuery = queryHandler.parseQuery(queryRequest, queryRequest.getQuery(), mClient, databaseName, req);
			BasicQuery basicQuery = new BasicQuery(finalQuery.get("query"));
			// Apply Action on One Or Many
			if (!queryRequest.isMany()) {
				basicQuery.limit(1);
			}

			Update update = new Update();
			user.forEach(update::set);
			
			username = basicQuery.getQueryObject().getString("username");

			mongoTemplate.updateMulti(basicQuery, update, queryRequest.getCollectionName());

		}
		return new Document("message", "The account for " + username + " has been activated");

	}

}
