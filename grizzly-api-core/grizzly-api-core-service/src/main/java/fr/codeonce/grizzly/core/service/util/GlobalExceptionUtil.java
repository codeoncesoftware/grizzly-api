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
package fr.codeonce.grizzly.core.service.util;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

import org.springframework.dao.DuplicateKeyException;

public class GlobalExceptionUtil {

	public static Supplier<NoSuchElementException> notFoundException(Class<?> clazz, String id) {
		String message = clazz.getSimpleName() + " not found with id : %s";
		return () -> new NoSuchElementException(String.format(message, id));
	}

	public static Supplier<DuplicateKeyException> duplicateNameFound(Class<?> clazz, String name) {
		String message = clazz.getSimpleName() + " already exists with : %s";
		return () -> new DuplicateKeyException(String.format(message, name));
	}

	public static Supplier<IOException> fileNotFoundException(String id) {
		String message = "File not found in container with id : %s";
		return () -> new IOException(String.format(message, id));
	}

	public static Supplier<IOException> fileNotValid() {
		String message = "File is not valid";
		return () -> new IOException(String.format(message));
	}

	private GlobalExceptionUtil() {

	}

}
