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

import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import fr.codeonce.grizzly.core.domain.user.User;
import fr.codeonce.grizzly.core.service.project.ProjectExample;
import fr.codeonce.grizzly.core.service.user.UserDto;
import fr.codeonce.grizzly.core.service.user.UserService;

public class UserServiceTest extends AbstractServiceTest {

	private static final String MY_FIRSTNAME = "MY_FIRSTNAME";
	private static final String MY_LASTNAME = "MY_LASTNAME";
	private static final String MY_USERNAME = "MY_USERNAME";
	private static final String MY_PASSWORD = "myPass";

	@Autowired
	private UserService userService;

	private List<User> testUsers = new ArrayList<>();
	private User testUser = new User();
	private UserDto testUserDto = new UserDto();

	@MockBean
	private ProjectExample projectExample;

	@Before
	public void init() {
		testUsers = new ArrayList<User>();
		testUser = new User();
		testUser.setUsername(MY_USERNAME);
		testUser.setFirstName(MY_FIRSTNAME);
		testUser.setLastName(MY_LASTNAME);
		testUser.setPassword(MY_PASSWORD);
		testUsers.add(testUser);

		testUserDto = new UserDto();
		testUserDto.setUsername(MY_USERNAME);
		testUser.setFirstName(MY_FIRSTNAME);
		testUser.setLastName(MY_LASTNAME);
		testUserDto.setPassword(MY_PASSWORD);

	}

	
	@Test
	public void testGetUser() {
		// Call
		given(userRepository.findByUsername(MY_USERNAME)).willReturn(Optional.of(testUser));

		// Test

		UserDto user = userService.getUser(MY_USERNAME);
		Assert.assertFalse(user.getFirstName() == null);
		
	}


}
