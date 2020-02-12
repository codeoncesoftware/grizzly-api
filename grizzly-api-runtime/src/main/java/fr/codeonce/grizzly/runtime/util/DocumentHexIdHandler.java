/*-
 * ========================LICENSE_START=================================
 * grizzly-api-runtime
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
package fr.codeonce.grizzly.runtime.util;

import org.bson.Document;
import org.bson.types.ObjectId;

public interface DocumentHexIdHandler {

	/**
	 * Transform MONGO ObjectID To HEX Format
	 * 
	 * @param document
	 * @return document
	 */
	public static Document transformMongoHexID(Document document) {
		
		if (document.get("_id") != null) {
			document.put("_id", ((ObjectId) document.get("_id")).toHexString());
		}
		
		return document;
	}
	
}
