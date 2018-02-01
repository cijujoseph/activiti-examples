package com.activiti.extension.bean;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;

import org.activiti.engine.delegate.BpmnError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Component;
import com.activiti.domain.runtime.EmailTemplate;
import com.activiti.domain.runtime.RelatedContent;
import com.activiti.service.runtime.EmailTemplateService;
import com.activiti.service.runtime.EmailTemplateService.ProcessedEmailTemplate;
import com.activiti.service.runtime.RelatedContentStreamProvider;

import freemarker.template.Configuration;

@Component("emailUtils")
public class EmailUtils {

	@Autowired
	protected JavaMailSender mailSender;

	@Autowired
	private RelatedContentStreamProvider relatedContentStreamProvider;
	
	@Autowired
	protected Configuration freeMarkerConfig;
	
	
	@Autowired
	protected EmailTemplateService emailTemplateService;


	public static final String DEFAULT_EMAIL_FROM_ADDRESS_PROPERTY_NAME = "email.from.default";
	public static final String DEFAULT_EMAIL_DISPLAY_NAME_PROPERTY_NAME = "email.from.default.name";

	@Autowired
	private Environment env;

	private static final Logger log = LoggerFactory.getLogger(EmailUtils.class);

	public void sendEmail(final String[] to, final String subject,
			final String text, final List<RelatedContent> attachments) {

		MimeMessagePreparator preparator = new MimeMessagePreparator() {
			public void prepare(MimeMessage mimeMessage) throws Exception {
				MimeMessageHelper message;
				if (attachments != null) {
					message = new MimeMessageHelper(mimeMessage, true);
				} else {
					message = new MimeMessageHelper(mimeMessage);
				}

				String fromEmailVar = env.getProperty(DEFAULT_EMAIL_FROM_ADDRESS_PROPERTY_NAME, "no-reply@localhost.com");
				String fromNameVar = env.getProperty(DEFAULT_EMAIL_DISPLAY_NAME_PROPERTY_NAME, "Alfresco Process Services");
				message.setFrom(fromEmailVar, fromNameVar);
				if (to != null)
					message.setTo(to);
				else
					throw new IllegalArgumentException("To Address Not Provided");

				if (subject != null)
					message.setSubject(subject);
				else
					throw new IllegalArgumentException("Subject Not Provided");
				if (text != null)
					message.setText(text, true);
				else
					throw new IllegalArgumentException("Email Body Not Provided");
				if (attachments != null) {
					for (RelatedContent attachment : attachments) {
						InputStream inputStream = relatedContentStreamProvider.getContentStream(attachment);
						ByteArrayDataSource bds = new ByteArrayDataSource(inputStream, attachment.getMimeType());
						message.addAttachment(attachment.getName(), bds);
					}
				}

			}
		};
		try {
			this.mailSender.send(preparator);
		} catch (Exception e) {
			String errorMessage = "Error while sending mail" + e.getMessage();
			// log.error(errorMessage);
			log.error(e.getMessage(), e);
			throw new BpmnError("ERROR_SENDING_EMAIL", errorMessage);
		}

	}
	public String evaluateTemplate(String templateName, Map<String, Object>templateVariables) throws Exception  {

		EmailTemplate emailTemplate = emailTemplateService.findCustomEmailTemplate(1L, templateName);
		ProcessedEmailTemplate emailTemp = emailTemplateService.processCustomEmailTemplate(emailTemplate.getId(), templateVariables);
		return emailTemp.getBody();

	}
}