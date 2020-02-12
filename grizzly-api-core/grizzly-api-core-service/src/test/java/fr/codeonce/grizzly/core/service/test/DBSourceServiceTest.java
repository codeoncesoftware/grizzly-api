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

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;

import java.util.Collections;
import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;

import fr.codeonce.grizzly.core.domain.datasource.DBSource;
import fr.codeonce.grizzly.core.domain.user.User;
import fr.codeonce.grizzly.core.service.datasource.DBSourceDto;
import fr.codeonce.grizzly.core.service.datasource.DBSourceService;
import fr.codeonce.grizzly.core.service.datasource.cache.ConnectionCacheService;

public class DBSourceServiceTest extends AbstractServiceTest {

	@Autowired
	private DBSourceService dBSourceService;

	@MockBean
	private ConnectionCacheService connectionCacheService;

	private Authentication auth;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);

		User user = new User();
		user.setUsername("testcodeonce");
		auth = new UsernamePasswordAuthenticationToken(user, null);
		SecurityContextHolder.getContext().setAuthentication(auth);
	}

	@Test
	public void testCheckUnicity() {
		DBSourceDto dbSourceDto = new DBSourceDto();
		DBSource dbSource = new DBSource();
		dbSource.setHost("127.0.0.999");
		dbSourceDto.setHost("127.0.0.999");
		given(dbSourceRepository.findAllByUserName(auth.getName())).willReturn(Collections.singletonList(dbSource));
		assertEquals(dbSource.getHost(), dBSourceService.getAll().get(0).getHost());
	}

	@Test(expected = NoSuchElementException.class)
	public void testGetDBSourceByIdException() {
		dBSourceService.getDbSourceById("dbsourceId");
	}

	@Test
	public void testCheckConnection() {
		ServerAddress serverAddress = new ServerAddress("127.0.0.0", 27272);
		MongoClient mongoClient = new MongoClient(serverAddress, new MongoClientOptions.Builder()
				.serverSelectionTimeout(2000).connectTimeout(1000).socketTimeout(1000).build());
		DBSourceDto dbsourceDto = new DBSourceDto();
		dbsourceDto.setConnectionMode("FREE");
		dbsourceDto.setDatabase("testDB");
		doReturn(mongoClient).when(connectionCacheService).getMongoClient(dbsourceDto);

		assertThatIllegalArgumentException();

	}

}
