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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.codeonce.grizzly.core.service.user.UserDto;
import fr.codeonce.grizzly.core.service.user.UserService;

@RestController
@CrossOrigin(origins = { "*" }, allowedHeaders = { "*" })
@RequestMapping("/api/user")
public class UserController {

	private static final Logger log = LoggerFactory.getLogger(UserController.class);

	@Autowired
	private UserService userService;

	@GetMapping("/{username}")
	public UserDto getUser(@PathVariable String username) {
		log.info("Request to fetch user with username : {}", username);
		return userService.getUser(username);
	}

	@PutMapping("/update")
	public UserDto updateUser(@RequestBody UserDto userDto) {
		log.info("Request to update user with username : {}", userDto.getUsername());
		return userService.updateUser(userDto);
	}

//	@PutMapping("/update/pwd")
//	public boolean updateUserPwd(@RequestParam String oldPwd, @RequestParam String newPwd) {
//		log.info("Request to update password");
//		return userService.updateUserPwd(oldPwd, newPwd);
//	}

}
