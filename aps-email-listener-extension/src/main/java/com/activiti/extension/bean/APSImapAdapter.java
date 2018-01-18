package com.activiti.extension.bean;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.mail.BodyPart;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMultipart;

import org.activiti.engine.RepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.mail.ImapMailReceiver;
import org.springframework.integration.mail.MailReceiver;
import org.springframework.integration.mail.MailReceivingMessageSource;
import org.springframework.stereotype.Component;

import com.activiti.service.api.UserService;
import com.activiti.service.runtime.ActivitiService;
import com.activiti.service.runtime.AlfrescoProcessInstanceService;
import com.activiti.service.runtime.RelatedContentService;
import com.activiti.domain.idm.User;
import com.activiti.domain.runtime.RelatedContent;
import com.activiti.model.runtime.CreateProcessInstanceRepresentation;
import com.activiti.model.runtime.ProcessInstanceRepresentation;
import com.activiti.security.SecurityUtils;

import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.repository.ProcessDefinition;

@Component("apsImapAdapter")
public class APSImapAdapter {

	private static Logger logger = LoggerFactory.getLogger(APSImapAdapter.class);

	@Autowired
	AlfrescoProcessInstanceService processInstanceService;

	@Autowired
	RepositoryService repositoryService;

	@Autowired
	UserService userService;

	@Autowired
	private Environment env;

	@Autowired
	private RelatedContentService relatedContentService;

	public static final String EMAIL_LISTENER_IMAP_USER = "email.listener.imap.username";
	public static final String EMAIL_LISTENER_IMAP_PW = "email.listener.imap.pw";
	public static final String EMAIL_LISTENER_IMAP_HOST = "email.listener.imap.host";
	public static final String EMAIL_LISTENER_IMAP_PORT = "email.listener.imap.port";
	public static final String EMAIL_LISTENER_IMAP_INBOX = "email.listener.imap.inbox";
	public static final String EMAIL_LISTENER_PROCESS_USER = "email.listener.process.user";
	public static final String EMAIL_LISTENER_PROCESS_KEY = "email.listener.process.key";
	public static final String EMAIL_LISTENER_DELETE_MESSAGES_FLAG = "email.listener.delete.messages.flag";
	public static final String EMAIL_LISTENER_MARK_READ_FLAG = "email.listener.mark.read.flag";

	@Bean
	@InboundChannelAdapter(value = "emailInboudChannel", poller = @Poller(fixedDelay = "1000"))
	public MailReceivingMessageSource mailMessageSource(MailReceiver imapMailReceiver) {
		return new MailReceivingMessageSource(imapMailReceiver);
	}

	@Bean
	public MailReceiver imapMailReceiver() {
		String imapUrl = "imaps://" + env.getProperty(EMAIL_LISTENER_IMAP_USER) + ":"
				+ env.getProperty(EMAIL_LISTENER_IMAP_PW) + "@" + env.getProperty(EMAIL_LISTENER_IMAP_HOST) + ":"
				+ env.getProperty(EMAIL_LISTENER_IMAP_PORT) + "/" + env.getProperty(EMAIL_LISTENER_IMAP_INBOX, "inbox");
		ImapMailReceiver imapMailReceiver = new ImapMailReceiver(imapUrl);
		imapMailReceiver.setShouldMarkMessagesAsRead(env.getProperty(EMAIL_LISTENER_MARK_READ_FLAG, Boolean.class, true));
		imapMailReceiver.setShouldDeleteMessages(env.getProperty(EMAIL_LISTENER_DELETE_MESSAGES_FLAG, Boolean.class, false));
		return imapMailReceiver;
	}

	@ServiceActivator(inputChannel = "emailInboudChannel")
	public void emailMessageSource(Message message) {
		try {
			// logger.info();
			User user = userService.findActiveUserByEmail(env.getProperty(EMAIL_LISTENER_PROCESS_USER));
			Authentication.setAuthenticatedUserId(Long.toString(user.getId()));
			ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
					.processDefinitionKey(env.getProperty(EMAIL_LISTENER_PROCESS_KEY)).latestVersion().singleResult();
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("initiator", user.getId());
			map.put("message", getTextFromMessage(message));
			map.put("from", ((InternetAddress[]) message.getFrom())[0].getAddress());
			if (message.getReplyTo() != null) {
				map.put("replyto", ((InternetAddress[]) message.getReplyTo())[0].getAddress());
			}

			InternetAddress[] ccAddresses = (InternetAddress[]) message.getRecipients(Message.RecipientType.CC);
			if (ccAddresses != null) {
				StringBuffer sb = new StringBuffer("");
				for (InternetAddress ccAddress : ccAddresses) {
					if (sb.length() != 0) {
						sb.append(",").append(ccAddress.getAddress());
					} else {
						sb.append(ccAddress.getAddress());
					}
				}
				map.put("cc", sb.toString());
			}
			map.put("subject", message.getSubject());
			map.put("receiveddate", message.getReceivedDate());

			// Print all headers
			Enumeration enumeration = message.getAllHeaders();
			while (enumeration.hasMoreElements()) {
				Header header = (Header) enumeration.nextElement();
				logger.debug(header.getName() + " - " + header.getValue());
			}

			if (message.isMimeType("multipart/*")) {
				MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
				String attachmentIds = processAttachments(mimeMultipart);
				map.put("attachments", attachmentIds);
			}
			SecurityUtils.assumeUser(user);
			CreateProcessInstanceRepresentation cpir = new CreateProcessInstanceRepresentation();
			cpir.setValues(map);
			cpir.setName(message.getSubject().toString());
			cpir.setProcessDefinitionId(pd.getId());
			ProcessInstanceRepresentation pi = processInstanceService.startNewProcessInstance(cpir);
			logger.info("process started: " + pi.getId());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String getTextFromMessage(Message message) throws MessagingException, IOException {
		String result = "";
		if (message.isMimeType("text/plain")) {
			result = message.getContent().toString();
		} else if (message.isMimeType("multipart/*")) {
			MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
			result = getTextFromMimeMultipart(mimeMultipart);
		}
		return result;
	}

	private String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws MessagingException, IOException {
		String result = "";
		int count = mimeMultipart.getCount();
		for (int i = 0; i < count; i++) {
			BodyPart bodyPart = mimeMultipart.getBodyPart(i);
			if (bodyPart.isMimeType("text/plain")) {
				result = result + "\n" + bodyPart.getContent();
				break;
			}
			if (bodyPart.isMimeType("text/html")) {
				result = (String) bodyPart.getContent();
				break;
			}
			if (bodyPart.getContent() instanceof MimeMultipart) {
				result = result + getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent());
				break;
			}
		}
		return result;
	}

	private String processAttachments(MimeMultipart mimeMultipart) throws MessagingException, IOException {
		int count = mimeMultipart.getCount();
		String contentIds = null;
		StringBuffer sb = new StringBuffer("");
		for (int i = 0; i < count; i++) {
			BodyPart bodyPart = mimeMultipart.getBodyPart(i);
			if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())
					|| (bodyPart.getFileName() != null && !bodyPart.getFileName().isEmpty())) {
				String mimeType = bodyPart.getContentType().contains(";") ? bodyPart.getContentType().split(";")[0]
						: bodyPart.getContentType();
				RelatedContent newRelatedContentObject = relatedContentService.createRelatedContent(
						userService.findActiveUserByEmail(env.getProperty(EMAIL_LISTENER_PROCESS_USER)), bodyPart.getFileName(), null, null,
						null, null, null, mimeType, bodyPart.getInputStream(), 0L);
				if (sb.length() != 0) {
					sb.append(",").append(Long.toString(newRelatedContentObject.getId()));
				} else {
					sb.append(Long.toString(newRelatedContentObject.getId()));
				}
			}
		}
		if (!sb.toString().isEmpty()) {
			contentIds = sb.toString();
			logger.info(contentIds);
		}
		return contentIds;
	}
}
