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

import java.util.NoSuchElementException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import fr.codeonce.grizzly.core.service.util.CustomGitAPIException;

/**
 * Handles / Catches all APP exceptions and returns the suitable message
 *
 */
@ControllerAdvice
public class GlobalExceptionHandler {
	
	
	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);


	@ResponseStatus(HttpStatus.FOUND)
	@ExceptionHandler(DuplicateKeyException.class)
	@ResponseBody
	public String handleIllegalException(DuplicateKeyException e) {
		logException(e);
		return ExceptionUtils.getRootCause(e).getMessage();
	}
	
	@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseBody
	public String handleIllegalException(IllegalArgumentException e) {
		logException(e);
		return ExceptionUtils.getRootCause(e).getMessage();
	}
	
	
	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(NoSuchElementException.class)
	@ResponseBody
	public String handleIllegalException(NoSuchElementException e) {
		logException(e);
		return ExceptionUtils.getRootCause(e).getMessage();
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(Exception.class)
	@ResponseBody
	public String handleIllegalException(Exception e) {
		logException(e);
		return ExceptionUtils.getRootCause(e).getMessage();
	}
	
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	@ExceptionHandler(IllegalStateException.class)
	@ResponseBody
	public String handleHttpClientErrorException(IllegalStateException e) {
		logException(e);
		return ExceptionUtils.getRootCause(e).getMessage();
	}
	
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	@ExceptionHandler(BadCredentialsException.class)
	@ResponseBody
	public String handleHttpClientErrorException(BadCredentialsException e) {
		logException(e);
		return ExceptionUtils.getRootCause(e).getMessage();
	}
	
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	@ExceptionHandler(CustomGitAPIException.class)
	@ResponseBody
	public String handleUnauthorizedGitClone(CustomGitAPIException e) {
		logException(e);
		return ExceptionUtils.getRootCause(e).getMessage();
	}
	
	private void logException(Exception e) {
		log.error("an error occurred", e);
	}

}
