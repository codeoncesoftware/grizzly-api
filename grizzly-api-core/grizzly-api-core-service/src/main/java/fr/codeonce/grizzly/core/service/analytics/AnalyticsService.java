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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fr.codeonce.grizzly.core.domain.analytics.Analytics;
import fr.codeonce.grizzly.core.domain.analytics.AnalyticsRepository;
import fr.codeonce.grizzly.core.domain.analytics.ApiCount;
import fr.codeonce.grizzly.core.domain.container.Container;
import fr.codeonce.grizzly.core.domain.datasource.DBSource;
import fr.codeonce.grizzly.core.domain.datasource.DBSourceRepository;
import fr.codeonce.grizzly.core.domain.project.ProjectRepository;
import fr.codeonce.grizzly.core.domain.resource.Resource;
import fr.codeonce.grizzly.core.domain.user.AccountType;
import fr.codeonce.grizzly.core.domain.user.UserRepository;
import fr.codeonce.grizzly.core.service.datasource.DBSourceStatsService;
import fr.codeonce.grizzly.core.service.util.SecurityContextUtil;

@Service
public class AnalyticsService {

	private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

	@Autowired
	private AnalyticsRepository analyticsRepository;

	@Autowired
	private DBSourceRepository dbSourceRepository;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private DBSourceStatsService dbSourceStatsService;

	@Autowired
	private UserRepository userRepository;
	
	@Value("${offres.free.msCount}")
	private long freeMsCount;
	
	@Value("${offres.premium.msCount}")
	private long premiumMsCount;
	
	@Value("${offres.free.maxStorage}")
	private long freeMaxStorage;
	
	@Value("${offres.free.maxStorage}")
	private long premiumMaxStorage;

	public AnalyticsDto getAnalytics() {
		AnalyticsDto analyticsDto = new AnalyticsDto();

		// CURRENT USER
		String currentUsername = SecurityContextUtil.getCurrentUsername();

		// API COUNTS
		List<Analytics> allAnalytics = analyticsRepository.findByUsername(currentUsername);
		List<AnalyticsApiCountDTO> apiCounts = allAnalytics.stream()//
				.flatMap(a -> a.getApiCounts().stream())//
				.collect(Collectors.groupingBy(ApiCount::getName, Collectors.summingLong(ApiCount::getCount)))//
				.entrySet().stream()//
				.map(e -> new AnalyticsApiCountDTO(e.getKey(), e.getValue())).collect(Collectors.toList());

		analyticsDto.setApiCounts(apiCounts);

		// REQUEST COUNT
		Long requestCount = allAnalytics.stream()//
				.map(Analytics::getRequestCount)//
				.reduce(0L, Long::sum);
		analyticsDto.setRequestCount(requestCount);

		// MS COUNT
		Long msCount = projectRepository.countByUsername(currentUsername);
		analyticsDto.setMsCount(msCount);

		// COMPUTE DATA METRICS
		List<DBSource> dbSourceList = dbSourceRepository.findAllByUserName(currentUsername);
		AnalyticsDataDto data = analyticsDto.getData();
		data.setTotalStored(computeDataSize(dbSourceList));
		data.setStoredFile(computeStoredFile(dbSourceList));
		data.setStoredContent(Math.round(data.getTotalStored() - data.getStoredFile()));

		// RETURN RESULT
		return analyticsDto;

	}

	private double computeStoredFile(List<DBSource> dbSourceList) {
		Double totalFiles = (double) dbSourceList.stream()
				.map(d -> dbSourceStatsService.getCollectionStats(d, d.getDatabase(), "fs.files"))
				.map(d -> d.get("size")).filter(Objects::nonNull).map(d -> (int) d).reduce(0, Integer::sum);
		Double totalChunks = (double) dbSourceList.stream()
				.map(d -> dbSourceStatsService.getCollectionStats(d, d.getDatabase(), "fs.chunks"))
				.map(d -> d.get("size")).filter(Objects::nonNull).map(d -> (int) d).reduce(0, Integer::sum);
		return totalFiles + totalChunks;
	}

	private double computeDataSize(List<DBSource> dbSourceList) {
		return Math.round(dbSourceList.stream()//
				.map(d -> dbSourceStatsService.getDbStats(d, d.getDatabase()))//
				.map(d -> Double.valueOf(String.valueOf(d.get("dataSize"))))//
				.reduce(0D, Double::sum));
	}

	public void updateContainerMetrics(Container container) {
		String currentUsername = SecurityContextUtil.getCurrentUsername();

		CompletableFuture.runAsync(() -> {
			Map<String, Long> apiCounts = container.getResources().stream()
					.collect(Collectors.groupingBy(Resource::getExecutionType, Collectors.counting()));

			Optional<Analytics> analytics = analyticsRepository.findByContainerId(container.getId());

			if (!analytics.isPresent()) {
				analytics = Optional.of(new Analytics());
			}

			analytics.ifPresent(a -> {
				a.setContainerId(container.getId());
				a.setUsername(currentUsername);
				a.setApiCounts(apiCounts.entrySet().stream().map(e -> new ApiCount(e.getKey(), e.getValue()))
						.collect(Collectors.toList()));
				analyticsRepository.save(a);
			});
		});
	}

	public void removeContainerAnalytics(String containerId) {
		analyticsRepository.deleteByContainerId(containerId);
	}

	public void updateRequestCount(String containerId) {
		Optional<Analytics> analytics = analyticsRepository.findByContainerId(containerId);

		if (!analytics.isPresent()) {
			analytics = Optional.of(new Analytics());
		}

		analytics.ifPresent(a -> {
			a.setContainerId(containerId);
			a.setRequestCount(a.getRequestCount() + 1);
			analyticsRepository.save(a);
		});
	}

	public boolean isUserTypeAuthorized() {
		
		AnalyticsDto analytics = getAnalytics();

		// CURRENT USER Email
		String currentUsername = SecurityContextUtil.getCurrentUsername();

		return this.userRepository.findByUsername(currentUsername).map(user ->  {
			
				if (user.getAccountType().equals(AccountType.FREE)) {
					if ((analytics.getMsCount() > freeMsCount) || (analytics.getData().getTotalStored() > freeMaxStorage)) {
						return false;
					}
				} else if (user.getAccountType().equals(AccountType.PREMIUM) || user.getAccountType().equals(AccountType.BUSINESS)) {
					if ((analytics.getMsCount() > premiumMsCount) || (analytics.getData().getTotalStored() > premiumMaxStorage)) {
						return false;
					}
				}
				
				return true;
		}).orElseThrow();
	}

}
