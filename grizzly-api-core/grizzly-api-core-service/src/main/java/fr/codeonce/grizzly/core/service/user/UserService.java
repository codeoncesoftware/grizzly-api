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
package fr.codeonce.grizzly.core.service.user;

import java.util.NoSuchElementException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.mongodb.MongoClient;

import fr.codeonce.grizzly.core.domain.datasource.DBSource;
import fr.codeonce.grizzly.core.domain.datasource.DBSourceRepository;
import fr.codeonce.grizzly.core.domain.user.User;
import fr.codeonce.grizzly.core.domain.user.UserRepository;
import fr.codeonce.grizzly.core.service.datasource.cache.ConnectionCacheService;
import fr.codeonce.grizzly.core.service.util.GlobalExceptionUtil;

@PropertySource("classpath:application.yml")
@Service
public class UserService {

	private static final Logger log = LoggerFactory.getLogger(UserService.class);

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private DBSourceRepository dbSourceRepository;

	@Autowired
	private UserMapper userMapper;

	@Autowired
	private ConnectionCacheService cacheService;

	@Value("${frontUrl}")
	private String url;

	public UserDto updateUser(UserDto userDto) {
		String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

		if (userDto.getUsername().equals(currentUsername)) {
			return this.userRepository.findByUsername(userDto.getUsername()).map(user -> {
				user.setFirstName(userDto.getFirstName());
				user.setLastName(userDto.getLastName());
				user.setOrganization(userDto.getOrganization());
				user.setPhone(userDto.getPhone());
				user.setFirstTime(userDto.isFirstTime());
				return this.userMapper.mapToDto(this.userRepository.save(user));
			}).orElseThrow();
		} else {
			throw new NoSuchElementException("User is not registered. Please contact us to resolve this issue.");
		}

	}

	/**
	 * Returns a specific user using his email
	 * 
	 * @param email
	 * @return
	 */
	public UserDto getUser(String username) {
		return userMapper.mapToDto(userRepository.findByUsername(username)
				.orElseThrow(GlobalExceptionUtil.notFoundException(User.class, username)));
	}

	// NEEDS TO BE REFACTORED
	public void logout(HttpServletRequest req) {
		this.dbSourceRepository.findAllByUserName(SecurityContextHolder.getContext().getAuthentication().getName())
				.parallelStream().forEach(db -> evictAndLogout(db, req));
		try {
			req.logout();
		} catch (ServletException e) {
			log.error("an error occurred!", e);
		}
	}

	private Object evictAndLogout(DBSource db, HttpServletRequest req) {
		if (!db.getConnectionMode().equalsIgnoreCase("FREE")) {
			MongoClient mongoClient = this.cacheService.getMongoClient(db.getId());
			mongoClient.close();
			this.cacheService.evictMongoClient(db.getId());
		}
		return null;
	}

	/**
	 * Verifies if the password matches the email
	 * 
	 * @param email
	 * @param oldPassword
	 * @return
	 */
	public boolean verifyOldPassword(String username, String oldPassword) {
		return this.userRepository.existsByUsernameAndPassword(username,
				DigestUtils.sha256Hex(oldPassword + DigestUtils.sha256Hex("co%de01/")));
	}

	public boolean updateUserPwd(String oldPwd, String newPwd) {
		return this.userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName())
				.map(user -> {
					if (verifyOldPassword(user.getUsername(), oldPwd)) {
						setUserPwd(user, newPwd);
						return true;
					}
					return false;
				}).orElse(false);
	}

//
//	private boolean isValidToken(VerificationToken verifToken) {
//		Date date = new Date();
//		Date tokenDate = verifToken.getCreatedDate();
//		return TimeUnit.MILLISECONDS.toMinutes((date.getTime() - tokenDate.getTime())) <= 3600;
//	}
//
	private void setUserPwd(User user, String newPwd) {
		user.setPassword(DigestUtils.sha256Hex(newPwd + DigestUtils.sha256Hex("co%de01/")));
		userRepository.save(user);
	}

}
