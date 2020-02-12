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

import java.util.concurrent.TimeUnit;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
@Validated
public class AppProperties {

	private Cache cache = new Cache();
	private Saxon saxon = new Saxon();

	public Cache getCache() {
		return cache;
	}

	public void setCache(Cache cache) {
		this.cache = cache;
	}

	public Saxon getSaxon() {
		return saxon;
	}

	public void setSaxon(Saxon saxon) {
		this.saxon = saxon;
	}

	public static class Saxon {
		private boolean hasLicense;

		public boolean isHasLicense() {
			return hasLicense;
		}

		public void setHasLicense(boolean hasLicense) {
			this.hasLicense = hasLicense;
		}

	}

	public static class Cache {

		private long maximumSize;

		private long expireAfterAccess;

		private TimeUnit timeUnit;

		private ContainerloadingPolicy containerloadingPolicy;

		public enum ContainerloadingPolicy {

			NONE, NEWEST, OLDEST;
		}

		public long getMaximumSize() {
			return maximumSize;
		}

		public void setMaximumSize(long maximumSize) {
			this.maximumSize = maximumSize;
		}

		public long getExpireAfterAccess() {
			return expireAfterAccess;
		}

		public void setExpireAfterAccess(long expireAfterAccess) {
			this.expireAfterAccess = expireAfterAccess;
		}

		public TimeUnit getTimeUnit() {
			return timeUnit;
		}

		public void setTimeUnit(TimeUnit timeUnit) {
			this.timeUnit = timeUnit;
		}

		public ContainerloadingPolicy getContainerloadingPolicy() {
			return containerloadingPolicy;
		}

		public void setContainerloadingPolicy(ContainerloadingPolicy containerloadingPolicy) {
			this.containerloadingPolicy = containerloadingPolicy;
		}

	}

}
