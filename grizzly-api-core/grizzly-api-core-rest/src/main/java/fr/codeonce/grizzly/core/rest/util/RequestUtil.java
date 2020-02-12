/*-
 * ========================LICENSE_START=================================
 * grizzly-api-core-rest
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
package fr.codeonce.grizzly.core.rest.util;

import javax.servlet.http.HttpServletRequest;

/**
 * 
 * {@link HttpServletRequest} Method Utils
 */
public class RequestUtil {

	private RequestUtil() {
	}

	/**
	 * Extracts Base URL from Http request
	 * 
	 * @param req
	 * @return
	 */
	public static String getBaseUrl(HttpServletRequest req) {
		if (req != null) {
			return req.getRequestURL().substring(0, req.getRequestURL().length() - req.getRequestURI().length())
					+ req.getContextPath();
		}
		throw new IllegalArgumentException("Http servlet Request could not be null");
	}

}