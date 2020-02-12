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
package fr.codeonce.grizzly.runtime.test.query;

import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequestWrapper;

import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartHttpServletRequest;

import com.mongodb.MongoClient;

import fr.codeonce.grizzly.common.runtime.RuntimeQueryRequest;
import fr.codeonce.grizzly.common.runtime.resource.RuntimeResourceParameter;
import fr.codeonce.grizzly.core.service.container.ContainerResourceService;
import fr.codeonce.grizzly.core.service.container.ContainerService;
import fr.codeonce.grizzly.core.service.datasource.DBSourceDto;
import fr.codeonce.grizzly.core.service.datasource.DBSourceService;
import fr.codeonce.grizzly.core.service.datasource.cache.ConnectionCacheService;
import fr.codeonce.grizzly.runtime.AbstractRuntimeTest;
import fr.codeonce.grizzly.runtime.service.query.QueryHandler;
import fr.codeonce.grizzly.runtime.service.query.SessionParamsHandler;

public class QueryHandlerTest extends AbstractRuntimeTest {

	@Spy
	@InjectMocks
	private QueryHandler queryHandler = new QueryHandler();

	@MockBean
	private ContainerService containerService;

	@MockBean
	private DBSourceService dbSourceService;

	@MockBean
	private ConnectionCacheService connectionCacheService;

	@MockBean
	private ContainerResourceService containerResourceService;

	@Autowired
	private SessionParamsHandler sessionParamsHandler;

	@Before
	public void init() {
		queryHandler.setSessionParamsHandler(sessionParamsHandler);

	}

	// @Test
	@SuppressWarnings("deprecation")
	public void shouldSelectTheRightMethod() throws IOException, ServletException, ParseException {
		MockitoAnnotations.initMocks(this);
		// INIT
		MockMultipartHttpServletRequest req = new MockMultipartHttpServletRequest();
		RuntimeQueryRequest rQueryRequest = new RuntimeQueryRequest();
		fr.codeonce.grizzly.core.domain.resource.Resource resource = new fr.codeonce.grizzly.core.domain.resource.Resource();
		fr.codeonce.grizzly.core.domain.resource.CustomQuery customQuery = new fr.codeonce.grizzly.core.domain.resource.CustomQuery();
		customQuery.setDatabase("testDB");
		customQuery.setDatasource("dbsourceId");
		customQuery.setCollectionName("testCollectionName");
		resource.setCustomQuery(customQuery);
		doReturn(resource).when(containerResourceService).getResource("testId", "testPath");
		Mockito.when(containerResourceService.getResource("testId", "testPath")).thenReturn(resource);
		DBSourceDto dbsourceDto = new DBSourceDto();
		doReturn(dbsourceDto).when(dbSourceService).getDbSourceById("dbsourceId");
		MongoClient mongoClient = new MongoClient();
		doReturn(mongoClient).when(connectionCacheService).getMongoClient(dbsourceDto);

		req.setMethod("get");
		queryHandler.handleQuery(rQueryRequest, "testId", req, null);

		verify(queryHandler, times(1)).handleFindQuery(rQueryRequest, "testDB", null, req, null);

		req.setMethod("delete");
		try {
			queryHandler.handleQuery(rQueryRequest, "testId", req, null);
		} catch (Exception e) {
			assertThatIllegalStateException();
		}

		verify(queryHandler, times(1)).handleDeleteQuery(rQueryRequest, "testDB", null, req);

		req.setMethod("put");
		resource.getCustomQuery().setType("insert");
		queryHandler.handleQuery(rQueryRequest, "testId", req, null);
		String parsedBody = new HttpServletRequestWrapper(req).getReader().lines().reduce("",
				(accumulator, actual) -> accumulator + actual);

		verify(queryHandler, times(1)).handleInsertQuery(rQueryRequest, "testDB", null, null, req, parsedBody);

		resource.getCustomQuery().setType("update");
		req.setContent("{\"test\":\"test\"}".getBytes());
		req.setContentType("application/json");
		try {
			queryHandler.handleQuery(rQueryRequest, "testId", req, null);
		} catch (Exception e) {
			assertThatIllegalStateException();
		}

		verify(queryHandler, times(1)).handleUpdateQuery(rQueryRequest, "testDB", null, req, parsedBody);

	}

	@Test
	public void shouldParseQuery() {
		RuntimeQueryRequest rQueryRequest = new RuntimeQueryRequest();
		rQueryRequest.setPath("/");
		rQueryRequest.setQuery("{\"name\":\"%name\", \"enabled\":\"%enabled\"}");

		// Preparing Test DATA
		// For String Values Test
		RuntimeResourceParameter param1 = new RuntimeResourceParameter();
		param1.setIn("query");
		param1.setName("name");
		param1.setType("String");
		// For Non String Values Test
		RuntimeResourceParameter param2 = new RuntimeResourceParameter();
		param2.setIn("query");
		param2.setName("enabled");
		param2.setType("Boolean");
		List<RuntimeResourceParameter> paramsList = new ArrayList<>();
		paramsList.add(param1);
		paramsList.add(param2);
		rQueryRequest.setParameters(paramsList);
		MockMultipartHttpServletRequest req = new MockMultipartHttpServletRequest();
		req.setParameter("name", "codeonce");
		req.setParameter("enabled", "true");
		// Test Method that Should Parse Query And Replace Parameters
		assertEquals("{\"name\":\"codeonce\", \"enabled\":true}",
				queryHandler.parseQuery(rQueryRequest, rQueryRequest.getQuery(), null, null, req).get("query"));
	}

}
