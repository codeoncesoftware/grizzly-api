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
package fr.codeonce.grizzly.core.service.analytics;

import java.util.ArrayList;
import java.util.List;

public class AnalyticsDto {
	
	private long msCount;

	private long requestCount;
	
	private List<AnalyticsApiCountDTO> apiCounts = new ArrayList<>();
	
	private AnalyticsDataDto data = new AnalyticsDataDto();
	
	public long getMsCount() {
		return msCount;
	}

	public void setMsCount(long msCount) {
		this.msCount = msCount;
	}

	public long getRequestCount() {
		return requestCount;
	}

	public void setRequestCount(long requestCount) {
		this.requestCount = requestCount;
	}

	public AnalyticsDataDto getData() {
		return data;
	}

	public void setData(AnalyticsDataDto data) {
		this.data = data;
	}

	public List<AnalyticsApiCountDTO> getApiCounts() {
		return apiCounts;
	}

	public void setApiCounts(List<AnalyticsApiCountDTO> apiCounts) {
		this.apiCounts = apiCounts;
	}

}
