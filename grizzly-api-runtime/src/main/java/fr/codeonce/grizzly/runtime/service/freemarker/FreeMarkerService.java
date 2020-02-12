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
package fr.codeonce.grizzly.runtime.service.freemarker;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.codeonce.grizzly.common.runtime.RuntimeRequest;
import fr.codeonce.grizzly.runtime.service.resolver.CloudURLStreamHandler;
import fr.codeonce.grizzly.runtime.service.shared.PostTreatment;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

@Service
public class FreeMarkerService {

	@Autowired
	private PostTreatment postTreatment;
	
	public String handle(RuntimeRequest<?> request, String baseUrl) throws IOException, TemplateException {

		
		// map objects from JSON body
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> variables = mapper.readValue(request.getBody().toString(),
				new TypeReference<Map<String, Object>>() {});

		Configuration cfg = new Configuration(Configuration.VERSION_2_3_27);

		cfg.setTemplateLoader(new CloudURLTemplateLoader()); // define customized template loader
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		cfg.setLocalizedLookup(false); // disable language
		
		String fileUri = CloudURLStreamHandler.CLOUD_PROTOCOL + "://" + request.getExecutablePath();
		Template template = cfg.getTemplate(fileUri);

		// Process variables from HashMap in template
		StringWriter stringWriter = new StringWriter();
		template.process(variables, stringWriter);
		
		// Get the HTML output from the StringWriter
		String htmlOutput = stringWriter.toString();
		// Post treatment to fix static files' paths
		return postTreatment.htmlTreatment(htmlOutput, fileUri, request.getContainerId(), request.getSecondaryFilePaths());
		

	}

}
