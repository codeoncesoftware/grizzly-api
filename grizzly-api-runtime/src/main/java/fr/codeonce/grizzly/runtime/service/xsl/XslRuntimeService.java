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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.codeonce.grizzly.common.runtime.RuntimeRequest;
import fr.codeonce.grizzly.common.runtime.RuntimeRequestParam;
import fr.codeonce.grizzly.runtime.service.resolver.CloudURLStreamHandler;
import fr.codeonce.grizzly.runtime.service.shared.PostTreatment;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

@Service
public class XslRuntimeService {

	@Autowired
	private XslCacheService xslCacheService;
	
	@Autowired
	private PostTreatment postTreatment;

	private static final Logger log = LoggerFactory.getLogger(XslRuntimeService.class);

	public Optional<String> handle(RuntimeRequest<?> request, String baseUrl) {
		String result = transformXml(request, baseUrl);
		return Optional.ofNullable(result);
	}

	private Map<String, String> convert(List<RuntimeRequestParam<?>> params) {
		return params.stream().collect(Collectors.toMap(RuntimeRequestParam::getName, RuntimeRequestParam::getValue));
	}

	protected String transformXml(RuntimeRequest<?> request, String baseUrl) {
		XsltTransformer transformer = null;
		try {

			String xmlContent = (String) request.getBody();
			String containerId = request.getContainerId();
			String xsltPath = CloudURLStreamHandler.CLOUD_PROTOCOL + "://" + request.getExecutablePath();

			// Transformer
			XsltExecutable xsltExecutable = xslCacheService.compileXslt(containerId, xsltPath);

			Processor processor = xsltExecutable.getProcessor();

			transformer = xsltExecutable.load();

			// DYNAMIC PARAMS
			// addDynamicParams(transformer, convert(request.getParams()));

			// SOURCE
//			if (xmlContent == null) {
//				throw new IllegalArgumentException("No XML Content to transform in the Body");
//			}
			Source xmlSource = getXmlStreamSource(xmlContent);
			XdmNode source = processor.newDocumentBuilder().build(xmlSource);
			transformer.setInitialContextNode(source);

			// DESTINATION
			ByteArrayOutputStream result = new ByteArrayOutputStream();
			Serializer out = processor.newSerializer(result);
			transformer.setDestination(out);

			// DO TRANSFORM
			transformer.transform();

			// IMPORT STATIC RESOURCES and RETURN HTML OUTPUT
			return postTreatment.htmlTreatment(result.toString(StandardCharsets.UTF_8.name()), xsltPath, containerId,
					request.getSecondaryFilePaths());
		} catch (Exception ex) {
			throw new IllegalArgumentException("Error during Transformation");
		}
	}

	private void addDynamicParams(XsltTransformer transformer, Map<String, String> parameters) {
		parameters.entrySet().forEach(
				e -> transformer.setParameter(QName.fromClarkName(e.getKey()), new XdmAtomicValue(e.getValue())));
	}

	private Source getXmlStreamSource(String xmlContent) {
		InputStream xmlInputStream = new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8));
		return new StreamSource(xmlInputStream);
	}

}
