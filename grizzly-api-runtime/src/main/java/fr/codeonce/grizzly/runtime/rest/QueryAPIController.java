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
package fr.codeonce.grizzly.runtime.rest;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.codeonce.grizzly.common.runtime.RuntimeQueryRequest;
import fr.codeonce.grizzly.runtime.service.query.QueryHandler;

@RestController
@CrossOrigin(origins = { "*" })
public class QueryAPIController {

	private static final Logger log = LoggerFactory.getLogger(QueryAPIController.class);

	@Autowired
	private QueryHandler queryHandler;

	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
			RequestMethod.DELETE }, path = "/runtime/query/{containerId}/**")
	public Object executeGetQuery(@PathVariable("containerId") String containerId, HttpServletRequest req,
			HttpServletResponse res) throws IOException, ServletException {
		log.debug("controller runtime : {}", containerId);

		Object result = null;

		StopWatch stopWatch = new StopWatch();
		stopWatch.start("handle Query");
		String runtimeQueryString = req.getHeader("query");
		try {
			RuntimeQueryRequest runtimeReq = new ObjectMapper().readValue(runtimeQueryString,
					RuntimeQueryRequest.class);
			result = this.queryHandler.handleQuery(runtimeReq, containerId, req, res);
			res.setContentType(runtimeReq.getReturnType());
		} catch (Exception e) {
			log.error("an error occurred while handling query", e);
		}
		stopWatch.stop();
		log.debug(stopWatch.prettyPrint());
		
		

		return result;
	}

}
