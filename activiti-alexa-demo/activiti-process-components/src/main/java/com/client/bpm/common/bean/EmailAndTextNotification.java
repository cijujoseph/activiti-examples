package com.client.bpm.common.bean;

import java.util.HashMap;
import java.util.Map;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component("emailAndTextNotification")
public class EmailAndTextNotification implements JavaDelegate {

	private static final Logger log = LoggerFactory.getLogger(EmailAndTextNotification.class);

	@Autowired
	private ContentUtils contentUtils;

	@Autowired
	private EmailUtils emailUtils;	
	
	@Autowired
	private Environment env;
	
	@Autowired
	private TwilioSMSSend twilioSMSSend;

	public void execute(DelegateExecution execution) throws Exception {
			//Send SMS to Customer
			if(env.getProperty("twilio.sms.enabled")!=null && env.getProperty("twilio.sms.enabled").equals("true")){
				twilioSMSSend.execute(execution);
			}
			//Email the report file to Customer using the email templates in Activiti
			if(env.getProperty("customer.email.enabled")!=null && env.getProperty("customer.email.enabled").equals("true")){
				// Freemarker template variable map so that the variables in the map can be used for email content substitution
				Map<String, Object> templateVariables = new HashMap<String, Object>();
				// Add all process variables to freemarker template variable map
				templateVariables.putAll(execution.getVariables());
				String template = env.getProperty("customer.email.template", "custom-email-template.ftl");
				String subject = emailUtils.getEmailTemplateSubject(template);
				String emailAddress = execution.getVariable("contactEmail")!=null ? execution.getVariable("contactEmail").toString() : env.getProperty("customer.email.address");
				String[] toList = { emailAddress };
				try {
					// send email
					emailUtils.sendEmail(toList, subject, emailUtils.evaluateTemplate(template, templateVariables),
							contentUtils.getContents(execution.getProcessInstanceId()));
				} catch (Exception e) {
					log.error("Unable to send email");
				}
			}
		}
}
