/*-
 * ========================LICENSE_START=================================
 * grizzly-api-core-domain
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
package fr.codeonce.grizzly.core.domain.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

import fr.codeonce.grizzly.core.domain.config.AppProperties.Cache;

@Configuration
@EnableCaching
public class CacheConfiguration {

	@Bean("cacheManagerBean")
	public CacheManager cacheManager(AppProperties appProperties) {

		// PROPS
		Cache cacheConfig = appProperties.getCache();

		// CACHE MANAGER
		CaffeineCacheManager cacheManager = new CaffeineCacheManager();

		// BUILD CACHE
		Caffeine<Object, Object> caffeine = Caffeine.newBuilder()/**/
				.maximumSize(cacheConfig.getMaximumSize())/**/
				.expireAfterAccess(cacheConfig.getExpireAfterAccess(), cacheConfig.getTimeUnit())/**/
				.recordStats();
		cacheManager.setCaffeine(caffeine);

		// RETURNS
		return cacheManager;
	}

}
