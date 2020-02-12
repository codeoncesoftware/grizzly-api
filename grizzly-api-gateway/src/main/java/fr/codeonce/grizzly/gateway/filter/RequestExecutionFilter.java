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
package fr.codeonce.grizzly.gateway.filter;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.REQUEST_URI_KEY;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.util.StopWatch;
import org.springframework.util.StreamUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

import fr.codeonce.grizzly.common.runtime.RuntimeQueryRequest;
import fr.codeonce.grizzly.common.runtime.RuntimeRequest;
import fr.codeonce.grizzly.common.runtime.resource.RuntimeResource;
import fr.codeonce.grizzly.gateway.service.APIService;
import io.jsonwebtoken.Claims;

/**
 * A ZUUL Filter to Intercept the HTTP Request and Forward it to the
 * Corresponding MicroService Instance Based on the Request's PATH
 * 
 * @author rayen
 *
 */
public class RequestExecutionFilter extends ZuulFilter {

	private static Logger log = LoggerFactory.getLogger(RequestExecutionFilter.class);

	@Autowired
	private APIService apiService;

	@Autowired
	private SecurityService securityFilter;

	/**
	 * Filter Should run Before Forwarding the Request to the MicroService
	 */
	@Override
	public String filterType() {
		return FilterConstants.PRE_TYPE;
	}

	@Override
	public int filterOrder() {
		return FilterConstants.SEND_FORWARD_FILTER_ORDER;
	}

	@Override
	public boolean shouldFilter() {
		RequestContext context = RequestContext.getCurrentContext();
		HttpServletRequest request = context.getRequest();
		return !request.getServletPath().startsWith("/api");
	}

	@Override
	public Object run() {

		String authorizedMessage = "authorized";
		log.debug("run method is EXECUTED");
		
		StopWatch stopWatch= new StopWatch();
		stopWatch.start("handle execution request");

		RequestContext context = RequestContext.getCurrentContext();
		HttpServletRequest request = context.getRequest();

		// Log Request
		log.debug("Request Recieved : Method {} with path {}", request.getMethod(), request.getServletPath());

		try {
			String requestType = request.getServletPath().substring(1,
					request.getServletPath().indexOf('/', request.getServletPath().indexOf('/') + 1));

			if (request.getServletPath().indexOf("/runtime/static") == 0) {
				forwardStaticResource(context, request.getServletPath());
			} else if (requestType.equals("runtime")) {
				// Transformation API
				String containerId = request.getServletPath().substring(requestType.length() + 2).substring(0,
						request.getServletPath().substring(requestType.length() + 2).indexOf('/', 0));
				String resourcePath = request.getServletPath().substring(requestType.length() + containerId.length() + 2);
				RuntimeResource resource = apiService.getResource(containerId, resourcePath);
				if (resource.getPath() != null) {

					// Check if API secured
					String key = resource.getSecurityKey();
					List<String> securityLevel = resource.getSecurityLevel();
					if (!securityLevel.contains("public")) {
						String token = request.getHeader("Authorization");
						authorizedMessage = securityFilter.validateToken(token, key, securityLevel);
					}

					if (authorizedMessage.equals("authorized")) {
						if (resource.getExecutionType() != null && (resource.getExecutionType().equalsIgnoreCase("Query")
								|| resource.getExecutionType().equalsIgnoreCase("FILE"))) {
							forwardDBQuery(resource, context, containerId, resourcePath);
						} else {
							forwardTransformationQuery(resource, context, containerId, resourcePath);
						}
					} else {
						forwardSecurityError(context, authorizedMessage, "401");
					}
				} else {
					authorizedMessage = "This API URL is not valid or no longer exists.";
					forwardSecurityError(context, authorizedMessage, "404");
				}
			}
		} catch (RuntimeException e) {
			log.error("Url malformed", e);
			authorizedMessage = "This API URL is not valid or no longer exists.";
			forwardSecurityError(context, authorizedMessage, "404");
		} 
		stopWatch.stop();
		log.debug(stopWatch.prettyPrint());

		return null;
	}

	private void forwardSecurityError(RequestContext context, String authorizedMessage, String code) {
		HttpServletRequest request = context.getRequest();
		HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(request);

		context.addZuulRequestHeader("code", code);
		context.addZuulRequestHeader("authorizedMessage", authorizedMessage);
		context.put(REQUEST_URI_KEY, "/runtime/error");
		context.setRequest(wrapper);

	}
	
	private void forwardStaticResource(RequestContext context, String path) {
		HttpServletRequest request = context.getRequest();
		HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(request);
		
		System.out.println(path);
		context.put(REQUEST_URI_KEY, path);
		context.setRequest(wrapper);
	}

	private void forwardDBQuery(RuntimeResource resource, RequestContext context, String containerId,
			String resourcePath)  {

		HttpServletRequest request = context.getRequest();
		
		HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(request);
		context.put(REQUEST_URI_KEY, "/runtime/query/" + containerId.toLowerCase() + '/' + resourcePath);

		RuntimeQueryRequest runtimeRequest = apiService.getRuntimeQueryRequest(resource);

		// ADD USERNAME TO RUNTIME REQUEST
		String authToken = request.getHeader("Authorization");
		if (!resource.getSecurityLevel().contains("public") && StringUtils.isNotBlank(authToken)) {
			Claims claims = SecurityService.parseClaims(authToken, resource.getSecurityKey());
			runtimeRequest.setUsername(claims.getSubject());
		}

		try {
			context.addZuulRequestHeader("query", new ObjectMapper().writeValueAsString(runtimeRequest));
		} catch (JsonProcessingException e) {
			log.debug("An error has been occured while parsing object {}", e.getMessage());
		}
		context.setRequest(wrapper);
		/**
		 * Add the Received Runtime Request to the Request Body
		 */

	}

	private void forwardTransformationQuery(RuntimeResource resource, RequestContext context, String containerId,
			String resourcePath) {

		HttpServletRequest request = context.getRequest();
		HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(request);

		context.setRequest(wrapper);
		/**
		 * Get the Runtime Request from the API Manager
		 */
		RuntimeRequest<?> runtimeRequest = apiService.getRuntimeRequest(containerId, resource);
		context.put(REQUEST_URI_KEY, "/runtime/" + runtimeRequest.getExecutionType().toLowerCase());
		/**
		 * Add the Received Runtime Request to the Request Body
		 */
		try {
			InputStream in = (InputStream) context.get("requestEntity");
			if (in == null) {
				in = context.getRequest().getInputStream();
			}
			String body = StreamUtils.copyToString(in, Charset.forName(StandardCharsets.UTF_8.toString()));
			body = new ObjectMapper().writeValueAsString(runtimeRequest) + body;
			byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
			/**
			 * Build a new HttpServletRequest
			 */
			context.setRequest(apiService.makeHttpServletRequest(request, bytes));
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

}
