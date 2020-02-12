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
package fr.codeonce.grizzly.runtime.test.sch;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

import org.apache.catalina.webresources.TomcatURLStreamHandlerFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.codeonce.grizzly.runtime.AbstractRuntimeTest;
import fr.codeonce.grizzly.runtime.service.resolver.CloudURLStreamHandler;
import fr.codeonce.grizzly.runtime.service.schematron.SchematronRuntimeService;

public class SchematronRuntimeServiceTest extends AbstractRuntimeTest {
	
	@Autowired
	private CloudURLStreamHandler cloudURLStreamHandler;
	
	@Autowired
	private SchematronRuntimeService schematronRuntimeService;
	
	@Before
	public void init() {
		TomcatURLStreamHandlerFactory.getInstance().addUserFactory(new URLStreamHandlerFactory() {
			@Override
			public URLStreamHandler createURLStreamHandler(String protocol) {
				if (CloudURLStreamHandler.CLOUD_PROTOCOL.equals(protocol)) {
					return cloudURLStreamHandler;
				}
				return null;
			}

		});
	}
	
	@Test
	public void validate() throws Exception {
		schematronRuntimeService.validate("gridfs://sch/person.sch", "<person2></person2>");
	}

}
