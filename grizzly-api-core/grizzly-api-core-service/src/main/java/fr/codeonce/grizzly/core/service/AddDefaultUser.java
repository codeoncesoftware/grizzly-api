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
package fr.codeonce.grizzly.core.service;

import java.util.Optional;

import javax.annotation.PostConstruct;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.codeonce.grizzly.core.domain.user.AccountType;
import fr.codeonce.grizzly.core.domain.user.User;
import fr.codeonce.grizzly.core.domain.user.UserRepository;

@Component
public class AddDefaultUser {

	private static final Logger log = LoggerFactory.getLogger(AddDefaultUser.class);
	
	@Autowired
	private UserRepository userRepository;

	@PostConstruct
	public void init() {
		log.info("works on start");
		Optional<User> u = userRepository.findByUsername("admin");
		if(!u.isPresent()) {
			User user = new User();
			user.setFirstName("Admin");
			user.setLastName("Admin");
			user.setUsername("admin");
			user.setPassword(DigestUtils.sha256Hex("admin123" + DigestUtils.sha256Hex("co%de01/")));
			user.setPhone("fr#33#628474722");
			user.setOrganization("Code Once");
			user.setEnabled(true);
			user.setFirstTime(true);
			user.setAccountType(AccountType.PREMIUM);
			userRepository.save(user);
		}
	}
}
