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
package fr.codeonce.grizzly.runtime.service.shared;

import java.util.List;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PostTreatment {

	private static final Logger log = LoggerFactory.getLogger(PostTreatment.class);
	
	@Value("${frontUrl}")
	private String baseUrl;

	public String htmlTreatment(String html, String xslPath, String containerId,
			List<String> secondaryFiles) {

		String projectBasePath = xslPath.substring(0, xslPath.lastIndexOf('/') + 1).replaceFirst("gridfs://", "");

		Document doc = Jsoup.parse(html, "", Parser.xmlParser());

		String resourceUrl = baseUrl + "/runtime/static/" + containerId + "/path/";

		// FIXING RESOURCES USING XSL PATHS

		// FIXING IMG
		doc.getElementsByTag("img").stream()
				.map(s -> s.attr("src", resourceUrl + projectBasePath + s.attr("src").replaceFirst("./", "")))
				.collect(Collectors.toList());

		// FIXING CSS
//		doc.getElementsByTag("link").stream()
//				.map(s -> s.attr("href", resourceUrl + projectBasePath + s.attr("href").replaceFirst("./", "")))
//				.collect(Collectors.toList());
		

		// FIXING JS
//		doc.getElementsByTag("script").stream()
//				.map(s -> s.attr("src", resourceUrl + projectBasePath + s.attr("src").replaceFirst("./", "")))
//				.collect(Collectors.toList());

		// FIXING RESOURCES WITH CONFIG (PARAMS)

		// GET CSS & JS FILES
		try {
			List<String> cssFiles = secondaryFiles.stream().filter(x -> x.substring(x.lastIndexOf('.') + 1).equals("css"))
					.collect(Collectors.toList());
			List<String> jsFiles = secondaryFiles.stream().filter(x -> x.substring(x.lastIndexOf('.') + 1).equals("js"))
					.collect(Collectors.toList());

			if (doc.getElementsByTag("head").get(0) == null) {
				doc.getElementsByTag("html").get(0).append("<head></head>");
			}
			

			Element head = doc.getElementsByTag("head").get(0);
			Element body = doc.getElementsByTag("body").get(0);

			cssFiles.stream().map(cssFileUri -> head.append("<link rel='stylesheet' href='" + resourceUrl + cssFileUri + "'>"))
					.collect(Collectors.toList());
			jsFiles.stream().map(jsFileUri -> body.append("<script src='" + resourceUrl + jsFileUri + "'>"))
					.collect(Collectors.toList());
		}
		catch(Exception e) {
			log.debug("an exception {}", e);
		}
		

		return doc.toString();
	}
}
