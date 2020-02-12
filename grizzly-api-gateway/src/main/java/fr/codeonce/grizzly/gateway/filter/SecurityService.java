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

import java.security.Key;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class SecurityService {

	private final Logger log = LoggerFactory.getLogger(SecurityService.class);

	public static Claims parseClaims(String authToken, String secretKey) {
		Key key = toSecretKey(secretKey);
		String authTokenValue = StringUtils.removeStart(authToken, "Bearer ");
		return Jwts.parser().setSigningKey(key).parseClaimsJws(authTokenValue).getBody();
	}

	@SuppressWarnings("unchecked")
	public String validateToken(String authToken, String secretKey, List<String> securityLevel) {
		try {
			Claims claims = parseClaims(authToken, secretKey);
			List<String> roles = claims.get("auth", List.class);

			if (checkRoles(securityLevel, roles) || securityLevel.contains("all")) {
				return "authorized";
			} else {
				return "You don't have the permission to use this secured API.";
			}

		} catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
			log.trace("Invalid JWT signature trace: {}", e);
			return "Invalid JWT signature.";
		} catch (ExpiredJwtException e) {
			log.trace("Expired JWT token trace: {}", e);
			return "Expired JWT token.";
		} catch (UnsupportedJwtException e) {
			log.trace("Unsupported JWT token trace: {}", e);
			return "Unsupported JWT token.";
		} catch (IllegalArgumentException e) {
			log.trace("JWT token compact of handler are invalid trace: {}", e);
			return "This API requires a JWT. Please enter a valid one in the headers.";
		}
	}

	private static Key toSecretKey(String secretKey) {
		byte[] keyBytes = Decoders.BASE64.decode(secretKey);
		return Keys.hmacShaKeyFor(keyBytes);
	}

	private boolean checkRoles(List<String> securityLevels, List<String> roles) {
		for (String securityLevel : securityLevels) {
			for (String role : roles) {
				if (securityLevel.equals(role)) {
					return true;
				}
			}
		}
		return false;
	}

}
