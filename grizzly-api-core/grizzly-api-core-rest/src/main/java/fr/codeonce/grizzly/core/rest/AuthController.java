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
package fr.codeonce.grizzly.core.rest;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import fr.codeonce.grizzly.core.service.auth.AuthService;
import fr.codeonce.grizzly.core.service.auth.LoginDto;
import fr.codeonce.grizzly.core.service.project.ProjectExample;
import fr.codeonce.grizzly.core.service.user.UserDto;
import fr.codeonce.grizzly.core.service.user.UserService;
import fr.codeonce.grizzly.core.service.util.SecurityContextUtil;

@RestController
@CrossOrigin
@RequestMapping("/api/auth")
public class AuthController {

	private static final Logger log = LoggerFactory.getLogger(AuthController.class);

	@Autowired
	private AuthService authService;

	@Autowired
	private UserService userService;

	@Autowired
	private ProjectExample projectExample;

	@Autowired
	ApplicationEventPublisher eventPublisher;

	/**
	 * Authenticate the user using email and password
	 * 
	 * @param user
	 * @return token
	 * @throws IOException
	 */
	@PostMapping("/login")
	public String login(@RequestBody LoginDto login, HttpServletRequest req) throws IOException {
		log.info("start login with : {}", login.getUsername());
		String username = login.getUsername();
		String auth = authService.login(username, login.getPassword());
		SecurityContextUtil.setUpSecurityContext(username);
		UserDto currentUser = userService.getUser(username);
		log.info("current user : {}", login.getUsername());
		if (currentUser.isFirstTime()) {
			projectExample.createProjectExample();
			currentUser.setFirstTime(false);
			currentUser.setPassword(null);
			userService.updateUser(currentUser);
		}
		return auth;
	}


	@GetMapping("/logout")
	public void logout(HttpServletRequest req) {
		log.info("request logout");
		userService.logout(req);
	}





}
