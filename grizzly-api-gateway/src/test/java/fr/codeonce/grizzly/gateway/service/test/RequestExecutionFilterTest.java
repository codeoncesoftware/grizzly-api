/*-
 * ========================LICENSE_START=================================
 * grizzly-api-gateway
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
package fr.codeonce.grizzly.gateway.service.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

import org.junit.After;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.netflix.zuul.context.RequestContext;

import fr.codeonce.grizzly.common.runtime.RuntimeRequest;
import fr.codeonce.grizzly.common.runtime.resource.RuntimeResource;
import fr.codeonce.grizzly.gateway.filter.RequestExecutionFilter;
import fr.codeonce.grizzly.gateway.service.APIService;
import fr.codeonce.grizzly.gateway.service.FeignDiscovery;

//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = DemoApplicationTests.class)
public class RequestExecutionFilterTest {

	private MockHttpServletResponse response;
	private MockHttpServletRequest request;

	@MockBean
	private APIService apiService;

	@Autowired
	private RequestExecutionFilter filter;

	@Mock
	private FeignDiscovery resourceDiscovery;

	@Before
	public void setTestRequestcontext() {
		filter = new RequestExecutionFilter();
		MockitoAnnotations.initMocks(this);
		RequestContext context = new RequestContext();
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
		context.setRequest(request);
		context.setResponse(response);

		filter = new RequestExecutionFilter();

		RequestContext.testSetCurrentContext(context);
	}

	@After
	public void reset() {
		RequestContext.getCurrentContext().clear();
	}

	// @Test
	public void shouldFilterQueryRequest() {

		assertTrue("shouldFilter returned false", filter.shouldFilter());
		request.setServletPath("/runtime/5cf8ca5be8ccbb5958ca90ff/test");
		RuntimeResource resource = new RuntimeResource();
		resource.setExecutionType("Query");
		given(apiService.getResource("5cf8ca5be8ccbb5958ca90ff", "/test")).willReturn(resource);
		request.setMethod("get");
		assertNull(filter.run());
	}

	// @Test
	public void shouldFilterTransformRequest() {
		RuntimeRequest<String> runtimeRequest = new RuntimeRequest<>();
		runtimeRequest.setExecutionType("xsl");
		request.setServletPath("/runtime/xsl/5cf8ca5be8ccbb5958ca90ff/5cf623c56b6b9f4ff059303e/test");
		request.addHeader("Authorization", "authorization");
//		doReturn(runtimeRequest).when(apiService).getRuntimeRequest("5cf8ca5be8ccbb5958ca90ff", "/test");
//		given(runtimeRequestDiscovery.getRuntimeRequest("5cf8ca5be8ccbb5958ca90ff", "/test")).willReturn(runtimeRequest);
//		Mockito.when(apiService.getRuntimeRequest("5cf8ca5be8ccbb5958ca90ff", "/test")).thenReturn(runtimeRequest); //my chnage here
		assertNotNull(filter.run());
	}

}
