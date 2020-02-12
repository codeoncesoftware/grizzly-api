/*-
 * ========================LICENSE_START=================================
 * grizzly-api-core-domain
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
package fr.codeonce.grizzly.core.domain.enums;

public enum Const {


	 INSERT("Insert"),
	 UPDATE("Udpate"),
	 GET("GET"),
	 POST("POST"),
	 PUT("PUT"),
	 DELETE("DELETE"),
	 QUERY("Query"),
	 @Deprecated
	 //use SecurityLevel enum
	 ADMIN("admin"),
	 VALUE("value"),
	 USERNAME("username"),
	 @Deprecated
	//use SecurityLevel enum
	 PUBLIC ("public"),
	 STRING ("String"),
	 @Deprecated
	 //use PredefinedQuery enum
	 USERNAME_USERNAME("{\"username\":\"%username\"}"),
	 FIELD_VALUE("{\"field\":\"%value\"}");
	  
	 private String value = "";
	  
	 //Constructeur
	 Const(String value){
	   this.value = value;
	 }
	  
	 public String getValue(){
	   return value;
	 }
}
