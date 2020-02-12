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
package fr.codeonce.grizzly.runtime.service.query.authentication;

import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceAuth {

	private static final Logger log = LoggerFactory.getLogger(EmailServiceAuth.class);

	@Autowired
	private JavaMailSender mailSender;

	@Value("${spring.mail.from}")
	private String userFrom;
	@Value("${spring.mail.personal}")
	private String personal;

	public boolean send(String content, String subject, String email) {
		log.info("send email with subject: {} to email: {}", subject, email);
		
		MimeMessage message = mailSender.createMimeMessage();
		try {

			MimeMessageHelper helper = new MimeMessageHelper(message, false, "utf-8");
			helper.setFrom(userFrom,personal);
			helper.setTo(email);
			helper.setSubject(subject);
			helper.setText(content, true);
			message.setContent(content, "text/html; charset=utf-8");
			mailSender.send(message);
			return true;
		} catch (Exception e) {
			log.error("could not send email", e);
		}
		return false;
	}

}
