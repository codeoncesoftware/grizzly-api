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
package fr.codeonce.grizzly.core.service.swagger.utils;

import fr.codeonce.grizzly.core.domain.resource.ResourceParameter;
import io.swagger.models.ModelImpl;
import io.swagger.models.RefModel;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.FormParameter;
import io.swagger.models.parameters.HeaderParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.PropertyBuilder;

/**
 * This Parameter Factory is a singleton Responsible for Parameter and
 * ResourceParameter instantiation
 * 
 * @author rayen
 *
 */
public class ParameterFactory {

	private ParameterFactory() {
	}

	/**
	 * Holder Class
	 * 
	 * @author rayen
	 *
	 */
	private static class ParameterFactoryHolder {
		static final ParameterFactory FACTORY = new ParameterFactory();
	}

	public static ParameterFactory getInstance() {
		return ParameterFactoryHolder.FACTORY;
	}

	/**
	 * Return a Parameter Object and set it's Fields from the given
	 * ResourceParameter object
	 * 
	 * @param param
	 * @return
	 */
	public Parameter makeParameter(ResourceParameter param) {
		if (param != null) {
			switch (param.getIn().toLowerCase()) {
			case "body":
				BodyParameter bodyParameter = new BodyParameter();
				bodyParameter.setName(param.getName());
				RefModel schema = new RefModel();
				schema.set$ref("text");
				Property property = PropertyBuilder.build("string", null, null);
				ModelImpl model = new ModelImpl();
				model.setType("object");
				model.addProperty("text", property);
				model.getProperties().get("text").setDescription(param.getValue());
				bodyParameter.setSchema(model);
				return bodyParameter;
			case "header":
				HeaderParameter headerParam = new HeaderParameter();
				headerParam.setName(param.getName());
				headerParam.setType(param.getType().toLowerCase());
				headerParam.setDefaultValue(param.getValue());
				return headerParam;
			case "formdata":
				FormParameter formParam = new FormParameter();
				formParam.setName(param.getName());
				formParam.setType(param.getType().toLowerCase());
				formParam.setDefaultValue(param.getValue());
				return formParam;
			case "query":
				QueryParameter queryParam = new QueryParameter();
				queryParam.setName(param.getName());
				queryParam.setType(param.getType().toLowerCase());
				queryParam.setDefaultValue(param.getValue());
				return queryParam;
			// Default is Path Parameter
			default:
				PathParameter pathParam = new PathParameter();
				pathParam.setName(param.getName());
				pathParam.setType(param.getType().toLowerCase());
				pathParam.setDefaultValue(param.getValue());
				return pathParam;
			}
		}
		return null;
	}

	/**
	 * Prepare Models Definitions for Sign-Up and Sign-In 
	 * 
	 * @param type
	 * @return Parameter of type RefModel
	 */
	public Parameter getModelDef(String type) {

		BodyParameter bodyParameter = new BodyParameter();
		bodyParameter.setName("body");
		bodyParameter.setRequired(true);
		
		if (type.equals("signup")) {
			bodyParameter.setDescription("SignUp Model");
			bodyParameter.setSchema(new RefModel("signUp"));
		} else if (type.equals("signin")) {
			bodyParameter.setDescription("SignIn Model");
			bodyParameter.setSchema(new RefModel("signIn"));
		}
		
		return bodyParameter;
	}

	/**
	 * Return a valid ResourceParameter object and set it's fields from the given
	 * Parameter Object
	 * 
	 * @param param
	 * @return
	 */
	public ResourceParameter makeResourceParameter(Parameter param) {
		if (param instanceof BodyParameter) {
			BodyParameter bodyParam = (BodyParameter) param;
			String desc = null;
			if (bodyParam.getSchema() != null && bodyParam.getSchema().getProperties() != null
					&& bodyParam.getSchema().getProperties().get("text") != null) {
				desc = bodyParam.getSchema().getProperties().get("text").getDescription();
			}
			return new ResourceParameter(safeStringReturn(safeStringReturn(bodyParam.getName())), "String", desc,
					bodyParam.getIn());
		} else if (param instanceof HeaderParameter) {
			HeaderParameter headerParam = (HeaderParameter) param;
			return new ResourceParameter(safeStringReturn(headerParam.getName()), headerParam.getType(),
					safeStringReturn(headerParam.getDefaultValue()), headerParam.getIn());
		} else if (param instanceof FormParameter) {
			FormParameter formParam = (FormParameter) param;
			return new ResourceParameter(safeStringReturn(formParam.getName()), formParam.getType(),
					safeStringReturn(formParam.getDefaultValue()), formParam.getIn());
		} else if (param instanceof QueryParameter) {
			QueryParameter queryParam = (QueryParameter) param;
			return new ResourceParameter(safeStringReturn(queryParam.getName()), queryParam.getType(),
					safeStringReturn(queryParam.getDefaultValue()), queryParam.getIn());
		} else {
			PathParameter pathParam = (PathParameter) param;
			return new ResourceParameter(safeStringReturn(pathParam.getName()), pathParam.getType(),
					safeStringReturn(pathParam.getDefaultValue()), pathParam.getIn());
		}
	}

	public String safeStringReturn(Object obj) {
		return obj != null ? obj.toString().toLowerCase() : null;
	}

}
