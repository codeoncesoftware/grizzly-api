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
package fr.codeonce.grizzly.core.service.test;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.junit4.SpringRunner;

import fr.codeonce.grizzly.core.ApplicationServiceTest;
import fr.codeonce.grizzly.core.domain.analytics.AnalyticsRepository;
import fr.codeonce.grizzly.core.domain.config.AppProperties;
import fr.codeonce.grizzly.core.domain.config.GrizzlyCoreProperties;
import fr.codeonce.grizzly.core.domain.container.ContainerRepository;
import fr.codeonce.grizzly.core.domain.container.hierarchy.ContainerHierarchyRepository;
import fr.codeonce.grizzly.core.domain.datasource.DBSourceRepository;
import fr.codeonce.grizzly.core.domain.project.ProjectRepository;
import fr.codeonce.grizzly.core.domain.user.UserRepository;
import fr.codeonce.grizzly.core.domain.user.token.TokenRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApplicationServiceTest.class)

public abstract class AbstractServiceTest {

	@MockBean
	protected ContainerRepository containerRepository;

	@MockBean
	protected ProjectRepository projectRepository;

	@MockBean
	protected UserRepository userRepository;

	@MockBean
	protected GrizzlyCoreProperties resourceManagerProperties;

	@MockBean
	protected AppProperties appProperties;

	@MockBean
	protected GridFsTemplate gridFsTemplate;

	@MockBean
	protected ContainerHierarchyRepository containerHierarchyRepository;

	@MockBean
	protected DBSourceRepository dbSourceRepository;

	@MockBean
	protected TokenRepository tokenRepository;

	@MockBean
	protected CacheManager cacheManager;

	@MockBean
	protected JavaMailSender javaMailSender;
	
	@MockBean
	protected AnalyticsRepository analyticsRepository;

}
