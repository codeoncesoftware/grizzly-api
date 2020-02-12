/*-
 * ========================LICENSE_START=================================
 * grizzly-api-core-rest
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
package fr.codeonce.grizzly.core.rest.config;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfiguration {

	@Autowired
	private BuildProperties buildProperties;
	

	@Bean
	public Docket api() {
		
		return new Docket(DocumentationType.SWAGGER_2).select().apis(RequestHandlerSelectors.any())
				.paths(PathSelectors.any()).build()
                .globalOperationParameters(parameters())
				.apiInfo(apiInfo());
	}

	private ApiInfo apiInfo() {
		String buildTime = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(Locale.UK)
				.withZone(ZoneOffset.UTC).format(buildProperties.getTime());

		return new ApiInfoBuilder().title("Grizzly Core Api")//
				.version(buildProperties.getVersion())//
				.description(String.format("Build Time: %s", buildTime))//
				.build();
	}
	

	
	List<Parameter> parameters() {
        ArrayList<Parameter> parameters = new ArrayList<>();
        Parameter parameter = new ParameterBuilder()
                .name("Authorization")
                .description("Bearer {genereated_token}")
                .parameterType("header")
                .modelRef(new ModelRef("string"))
                .required(false)
                .defaultValue("")
                .build();
        parameters.add(parameter);
        return parameters;
    }
	
}
