/*-
 * ========================LICENSE_START=================================
 * grizzly-api-core-domain
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
package fr.codeonce.grizzly.core.domain.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

@Configuration
public class MongoConfiguration extends AbstractMongoConfiguration {

	@Autowired
	private MongoProperties mongoProperties;

	@Override
	@Primary
	@Bean
	public MongoClient mongoClient() {

		// URI Connection
		if (mongoProperties.getUri() != null) {
			return new MongoClient(new MongoClientURI(mongoProperties.getUri()));
		}
		// REMOTE SERVER
		// SERVER INFO
		ServerAddress serverAddress = new ServerAddress(mongoProperties.getHost(), mongoProperties.getPort());
		if (StringUtils.isNotBlank(mongoProperties.getUsername())) {
			// CREDENTIAL
			MongoCredential credential = MongoCredential.createCredential(mongoProperties.getUsername(),
					mongoProperties.getAuthenticationDatabase(), mongoProperties.getPassword());

			// CREATE MONGO CLIENT
			return new MongoClient(serverAddress, credential, MongoClientOptions.builder().build());
		}

		// LOCAL HOST
		return new MongoClient(serverAddress);

	}

	@Override
	protected String getDatabaseName() {
		return mongoProperties.getDatabase();
	}

	@Bean
	public GridFsTemplate gridFsTemplate() throws Exception {
		return new GridFsTemplate(mongoDbFactory(), mappingMongoConverter());
	}

}
