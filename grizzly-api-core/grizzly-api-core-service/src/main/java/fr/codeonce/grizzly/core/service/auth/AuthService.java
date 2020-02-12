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
package fr.codeonce.grizzly.core.service.auth;

import java.util.Optional;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import fr.codeonce.grizzly.core.domain.config.GrizzlyCoreProperties;
import fr.codeonce.grizzly.core.domain.user.User;
import fr.codeonce.grizzly.core.domain.user.UserRepository;

@Service
public class AuthService {

	private static final Logger log = LoggerFactory.getLogger(AuthService.class);

	@Autowired
	private GrizzlyCoreProperties properties;
	
	@Autowired
	private UserRepository userRepository;

	@Value("${server.port}")
	private String port;

	/**
	 * Authenticate the user using email and password
	 * 
	 * @param email
	 * @param password
	 * @return token
	 */
	public String login(String username, String password) {
		
		StopWatch stopWatch = new StopWatch();

		stopWatch.start("url construction");

		String clientId = this.properties.getOauth2().getClientId();
		String clientSecret = this.properties.getOauth2().getClientSecret();
		String grantType = this.properties.getOauth2().getGrantType();
		String jwtKey = this.properties.getOauth2().getJwtKey();

		String url = "http://localhost:" + port + "/oauth/token?client_id=" + clientId + "&client_secret="
				+ clientSecret + "&grant_type=" + grantType + "&username=" + username + "&password="
				+ DigestUtils.sha256Hex(password + DigestUtils.sha256Hex("co%de01/")) + "&jwt_key=" + jwtKey;

		RestTemplate restTamplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> request = new HttpEntity<>(headers);
		ResponseEntity<String> result;

		stopWatch.stop();

		try {
			stopWatch.start("The login process");
			result = restTamplate.exchange(url, HttpMethod.POST, request, String.class);
			stopWatch.stop();

			log.debug(stopWatch.prettyPrint());
				
			return result.getBody();

		} catch (HttpClientErrorException.BadRequest e) {
			String responseBodyAsString = e.getResponseBodyAsString();
			if (responseBodyAsString.contains("Bad credentials")) {
				throw new BadCredentialsException("4011"); //Code 1: invalid credentials
			}

		}

		throw new IllegalStateException("an error occurred with rest call : TODO");
	}

}
