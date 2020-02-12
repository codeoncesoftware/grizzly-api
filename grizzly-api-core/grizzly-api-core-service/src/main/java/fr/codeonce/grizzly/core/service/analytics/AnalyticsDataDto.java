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

public class AnalyticsDataDto {
	private double totalStored;
	private double downloaded;
	private double uploaded;
	private double storedContent;
	private double storedFile;

	public double getTotalStored() {
		return totalStored;
	}

	public void setTotalStored(double totalStored) {
		this.totalStored = totalStored;
	}

	public double getDownloaded() {
		return downloaded;
	}

	public void setDownloaded(double downloaded) {
		this.downloaded = downloaded;
	}

	public double getUploaded() {
		return uploaded;
	}

	public void setUploaded(double uploaded) {
		this.uploaded = uploaded;
	}

	public double getStoredContent() {
		return storedContent;
	}

	public void setStoredContent(double storedContent) {
		this.storedContent = storedContent;
	}

	public double getStoredFile() {
		return storedFile;
	}

	public void setStoredFile(double storedFile) {
		this.storedFile = storedFile;
	}

}
