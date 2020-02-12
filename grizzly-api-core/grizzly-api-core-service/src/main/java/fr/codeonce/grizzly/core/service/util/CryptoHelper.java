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

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fr.codeonce.grizzly.core.domain.datasource.DBSource;

@Service
public class CryptoHelper {
	
	@Value("${encrypt.key}")
	private String secretkey;
	
	private Key key;

	public CryptoHelper() {
		
	}
	
	@PostConstruct
	public void initKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		KeySpec spec = new PBEKeySpec(secretkey.toCharArray(), "A".getBytes(), 65536, 256);
		SecretKey tmp = factory.generateSecret(spec);
		SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
		this.key = secret;
	}

	private static final Logger log = LoggerFactory.getLogger(CryptoHelper.class);

	public String encrypt(String plaintext) throws Exception {
		return encrypt(generateIV(), plaintext);
	}

	public String encrypt(byte[] iv, String plaintext) throws Exception {

		byte[] decrypted = plaintext.getBytes();
		byte[] encrypted = encrypt(iv, decrypted);

		StringBuilder ciphertext = new StringBuilder();

		ciphertext.append(Base64.encodeBase64String(iv));
		ciphertext.append(":");
		ciphertext.append(Base64.encodeBase64String(encrypted));

		return ciphertext.toString();

	}

	public String decrypt(String ciphertext) throws Exception {
		String[] parts = ciphertext.split(":");
		byte[] iv = Base64.decodeBase64(parts[0]);
		byte[] encrypted = Base64.decodeBase64(parts[1]);
		byte[] decrypted = decrypt(iv, encrypted);
		return new String(decrypted);
	}

	

	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public static byte[] generateIV() {
		SecureRandom random = new SecureRandom();
		byte[] iv = new byte[16];
		random.nextBytes(iv);
		return iv;
	}

	public Key generateSymmetricKey() throws Exception {
		KeyGenerator generator = KeyGenerator.getInstance("AES");
		return generator.generateKey();
	}

	public byte[] encrypt(byte[] iv, byte[] plaintext) throws Exception {
		Cipher cipher = Cipher.getInstance(key.getAlgorithm() + "/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
		return cipher.doFinal(plaintext);
	}

	public byte[] decrypt(byte[] iv, byte[] ciphertext) throws Exception {
		Cipher cipher = Cipher.getInstance(key.getAlgorithm() + "/CBC/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
		return cipher.doFinal(ciphertext);
	}

	public void decrypt(DBSource db) {
		try {
			if (db.getHost() != null) {
				db.setHost(decrypt(db.getHost()));
			}
			if (db.getUri() != null) {
				db.setUri(decrypt(db.getUri()));
			}
			if (db.getPassword() != null) {
				db.setPassword(decrypt(new String(db.getPassword())).toCharArray());
			}
		} catch (Exception e) {
			log.debug(e.getMessage());
		}

	}

	public void encrypt(DBSource db) {
		try {
			if (db.getHost() != null) {
				db.setHost(encrypt(db.getHost()));
			}
			if (db.getUri() != null) {
				db.setUri(encrypt(db.getUri()));
			}
			if (db.getPassword() != null) {
				db.setPassword(encrypt(new String(db.getPassword())).toCharArray());
			}
		} catch (Exception e) {
			log.debug(e.getMessage());
		}
	}

}
