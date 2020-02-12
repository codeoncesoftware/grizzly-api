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
package fr.codeonce.grizzly.runtime.service.schematron;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.ValidationDriver;
import com.thaiopensource.validate.auto.AutoSchemaReader;
import com.thaiopensource.xml.sax.ErrorHandlerImpl;

/**
 * A Schematron Runtime service to validate xml resource based on schematron
 * file .sh
 *
 */
@Service
public class SchematronRuntimeService {

	private static final Logger log = LoggerFactory.getLogger(SchematronRuntimeService.class);

	/**
	 * Validate xml file based on the given schemaURL
	 * 
	 * @param schemaUrl
	 * @param xml
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 */
	public AutoSchemaReportDTO validate(String schemaUrl, String xml) throws SAXException, IOException {

		InputSource entrySource = new InputSource(new StringReader(xml));
		if (entrySource.getSystemId() == null) {
			entrySource.setSystemId("app:id");
		}

		// ERROR HANDLER
		ByteArrayOutputStream reportStream = new ByteArrayOutputStream();

		ValidationDriver driver = loadSchemaSource(schemaUrl, reportStream);

		// VALIDATE ENTRY
		boolean valid = driver.validate(entrySource);

		// RETURN REPORT
		AutoSchemaReportDTO report = new AutoSchemaReportDTO();
		report.setValid(valid);
		report.setTextReport(reportStream.toString("UTF-8"));
		return report;
	}

	private ValidationDriver loadSchemaSource(String url, ByteArrayOutputStream reportStream)
			throws IOException, SAXException {

		InputSource schemaSource = new InputSource(url);

		// ERROR HANDLER
		ErrorHandlerImpl eh = new ErrorHandlerImpl(reportStream);

		// PROPERTIES
		PropertyMapBuilder properties = new PropertyMapBuilder();
		properties.put(ValidateProperty.ERROR_HANDLER, eh);
		properties.put(ValidateProperty.URI_RESOLVER, new org.apache.xml.resolver.tools.CatalogResolver());

		// VALIDATION ENGINE : AUTO (SCHEMATRON/RNG)
		ValidationDriver driver = new ValidationDriver(properties.toPropertyMap(), new AutoSchemaReader());

		// LOAD SCHEMA
		if (driver.loadSchema(schemaSource)) {
			return driver;
		} else {
			log.error("validation failed for url : '{}'", url);
			throw new SAXException(String.format("Cannot load schema for url: %s", url));
		}
	}

}
