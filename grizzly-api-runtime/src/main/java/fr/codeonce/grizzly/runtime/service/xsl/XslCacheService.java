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
package fr.codeonce.grizzly.runtime.service.xsl;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.trans.CompilerInfo;

@Service
public class XslCacheService {

	private static final Logger log = LoggerFactory.getLogger(XslCacheService.class);
	
	@Value("${app.saxon.hasLicense}")
	private boolean hasLicense;

	//@Cacheable(value = "xslTransformer", key = "#containerId.concat('#').concat(#xsltPath)", sync = true)
	public XsltExecutable compileXslt(String containerId, String xsltPath)
			throws SaxonApiException, TransformerException {

		log.info("Create xslTransformer with version : '{}' for the file : '{}' ", containerId, xsltPath);

		StopWatch stopWatch = new StopWatch();

		stopWatch.start(String.format("Create xslTransformer with : version : '%s' for the file : '%s' ",
				containerId, xsltPath));

		Processor processor = createProcessor(containerId, xsltPath);

	
		// XSLT
		Configuration config = processor.getUnderlyingConfiguration();
		Source styleSource = config.getURIResolver().resolve(xsltPath, null);
		if (styleSource == null) {
			styleSource = config.getSystemURIResolver().resolve(xsltPath, null);
		}

		// XSLT TRANSFORMER
		XsltCompiler comp = processor.newXsltCompiler();

		XsltExecutable exec = comp.compile(styleSource);

		stopWatch.stop();

		log.debug(stopWatch.prettyPrint());

		return exec;

	}

	private Processor createProcessor(String containerId, String xsltPath) throws TransformerException {

		log.info("Create processor with version : '{}' for the file : '{}' ", containerId, xsltPath);

		Processor processor = new Processor(true);

		// CONFIG
		Configuration config = processor.getUnderlyingConfiguration();

		// VERSION
		config.setVersionWarning(true);

		// SCHEMA AWARE
		CompilerInfo defaultCompilerInfo = config.getDefaultXsltCompilerInfo();
		defaultCompilerInfo.setSchemaAware(hasLicense);

		// XML RESOLVER
		config.setURIResolver(config.makeURIResolver("org.apache.xml.resolver.tools.CatalogResolver"));
		return processor;
	}

}
