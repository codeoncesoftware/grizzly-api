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
package fr.codeonce.grizzly.runtime.config;

import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import fr.codeonce.grizzly.core.domain.config.AppProperties;
import fr.codeonce.grizzly.core.domain.config.GrizzlyCoreProperties;

@Configuration
@ComponentScan("fr.codeonce.grizzly.core")
@EnableConfigurationProperties(value = { AppProperties.class, MongoProperties.class, GrizzlyCoreProperties.class })
@EnableMongoRepositories("fr.codeonce.grizzly.core.domain")
@Profile("!test")
public class GrizzlyCoreConfig {

}
