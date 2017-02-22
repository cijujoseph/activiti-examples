package com.alfresco;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twilio.twiml.Gather;
import com.twilio.twiml.Method;
import com.twilio.twiml.Pause;
import com.twilio.twiml.Record;
import com.twilio.twiml.Say;
import com.twilio.twiml.TwiMLException;
import com.twilio.twiml.VoiceResponse;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class TwilioServlet extends HttpServlet {
	/**
	 * * This Twilio sample servlet illustrates how to use Twilio with Activiti
	 * APIs. This example is checking for active message wait components in a
	 * process and completing them with a phone call. Scenario is a customer
	 * calling back to provide his/her feedback after an appointment. If there
	 * is an active process, this will allow the customer to continue on the
	 * call and provide feedback using the dialpad of their phone. This also
	 * allows them to optionally leave a message if unsatisfied with the
	 * service; author: Ciju Joseph
	 */
	
	private static final Logger log = LoggerFactory
			.getLogger(TwilioServlet.class);
	private static final long serialVersionUID = 1L;

	// Activiti Execution API URI
	public static final String QUERY_EXECUTION_URL = "query/executions?tenantId=tenant_1";
	public static final String COMPLETE_EXECUTION_URL = "runtime/executions/{executionId}?tenantId=tenant_1";

	// Process Definition Key in Activiti
	public static final String PROCESS_KEY = "VehicleServiceBooking";

	// Activiti message subscription names
	public static final String MESSAGE_NAME = "twilio";
	public static final String TRANSCRIPTION_MESSAGE_NAME = "twilio-transcription";

	// Setting a few actions to route the call appropriately!
	public static final String ACTION_FEEDBACK = "feedback";
	public static final String ACTION_HELLO = "hello";
	public static final String ACTION_RECORD = "record";
	public static final String ACTION_TRANSCRIPTION = "transcription";

	// Setting various Twilio messages
	public static final String UNKNOWN_CALLER_MSG_LINE1 = "Hello there, Thank you for calling Alfresco Services. I couldn't recognise you!";
	public static final String GOOD_BYE_MSG = "Good Bye";
	public static final String NO_ACTIVE_CALL_WAIT_MSG = "Thank you for calling Alfresco Services. We already received your feedback and a customer service representatve will get back to you soon";
	public static final String CUSTOMER_FEEDBACK_OPTION_MSG = "Please press 1 to answer a short survey. If you do not wish to answer a survey, - please press 2 if you are satisfied with the service - or - press 3 if you are not satisfied.";
	public static final String SURVEY_QUESTION = "After the tone please provide your feedback";
	public static final String WRONG_CHOICE_FEEDBACK_MSG = "Sorry, I don\'t understand that choice.";
	public static final String VOICE_MSG_THANKYOU_MSG = "Thank you for leaving a message. Goodbye";
	public static final String UNEXPECTED_ERR_MSG = "There is an unxpected error while processing your request.";
	public static final String INITIAL_THANKYOU_FOR_CALL_MSG = "Thank you for calling Alfresco Services.";
	public static final String POST_FEEDBACK_MSG = "Thank you for feedback, Goodbye.";

	// Twilio URLS for subsequent actions
	public static final String EXECUTION_HANDLE_TWIL_URL = "/activiti-twilio-service/handle-key/executions/";
	public static final String RECORDING_TWIL_URL = "/activiti-twilio-service/handle-recording/recordings/";
	public static final String TRANSCRIPTION_TWIL_URL = "/activiti-twilio-service/handle-recording/transcriptions/";

	// Activiti Base url and Decooda Url read and set from properties
	private String activitiBaseUrl;
	private String decoodaUrl;
	private Boolean decoodaCallEnabled = false;
	private String decoodaModelNumber;

	@SuppressWarnings("unchecked")
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {

		// Read the property file and set some values
		InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("twilio-app.properties");
		Properties properties = new Properties();
		properties.load(inputStream);

		// Create a list of people we know.
		
		HashMap<String, String> callers = new HashMap<String, String>();
		if(properties.getProperty("twilio.caller.1.number")!=null){
			callers.put(properties.getProperty("twilio.caller.1.number"), properties.getProperty("twilio.caller.1.name"));
		}

		// Check & Set Decooda properties
		if (properties.getProperty("decooda.enabled") != null
				&& properties.getProperty("decooda.enabled").equals("true")) {
			setDecoodaCallEnabled(true);
			setDecoodaUrl(properties.getProperty("deccoda.api.url"));
			setDecoodaModelNumber(properties.getProperty("decooda.model"));
		}

		setActivitiBaseUrl(properties.getProperty("activiti.api.base.url"));

		// Get the caller.
		String fromNumber = request.getParameter("From");
		String knownCaller = callers.get(fromNumber)!=null? callers.get(fromNumber) :"";
		String executionId = null;
		String action = null;
		VoiceResponse.Builder builder = new VoiceResponse.Builder();
		String addressUser;

		if (properties.getProperty("twilio.callercheck.enabled") != null
				&& properties.getProperty("twilio.callercheck.enabled").equals("true") && knownCaller.equals("")) {
			
			builder.say(new Say.Builder(UNKNOWN_CALLER_MSG_LINE1).build()).pause(new Pause.Builder().length(1).build())
					.say(new Say.Builder(GOOD_BYE_MSG).build()).build();
			
		} else {
			String pathInfo = request.getPathInfo();
			log.info(request.getRequestURI());
			if (pathInfo != null) {
				log.info(pathInfo);
				if (pathInfo.startsWith("/executions/")) {
					String[] pathParts = pathInfo.split("/");
					executionId = pathParts[2];
					action = ACTION_FEEDBACK;
				} else if (pathInfo.startsWith("/recordings/")) {
					String[] pathParts = pathInfo.split("/");
					executionId = pathParts[2];
					action = ACTION_RECORD;
				} else if (pathInfo.startsWith("/transcriptions/")) {
					action = ACTION_TRANSCRIPTION;
				} else {
					action = ACTION_HELLO;
				}
			} else {
				action = ACTION_HELLO;
			}

			String recordingUrl;
			Map<String, Object> executions;

			switch (action) {

			case ACTION_HELLO:
				// Use the caller's name
				addressUser = "Hello " + knownCaller;
				executions = checkActiveTwilioCallWait(fromNumber);
				if ((Integer) executions.get("size") > 0) {
					executionId = ((Map<String, String>) ((List) (executions.get("data"))).get(0)).get("id");
					// Create a TwiML response and add our friendly message.
					builder.say(new Say.Builder(addressUser).build()).pause(new Pause.Builder().length(1).build())
							.say(new Say.Builder(INITIAL_THANKYOU_FOR_CALL_MSG).build()).build();

					appendGather(builder, EXECUTION_HANDLE_TWIL_URL + executionId, CUSTOMER_FEEDBACK_OPTION_MSG);

				} else {
					builder.say(new Say.Builder(addressUser).build()).pause(new Pause.Builder().length(1).build())
							.say(new Say.Builder(NO_ACTIVE_CALL_WAIT_MSG).build()).build();
				}
				break;

			case ACTION_FEEDBACK:

				String digits = request.getParameter("Digits");
				if (digits != null) {
					switch (digits) {
					case "1":
						// Record the caller's voice.
						Record record = new Record.Builder().maxLength(30).action(RECORDING_TWIL_URL + executionId)
								.transcribe(true).transcribeCallback(TRANSCRIPTION_TWIL_URL).build();
						builder.say(new Say.Builder(SURVEY_QUESTION).build()).record(record);
						break;
					case "2":
						builder.say(new Say.Builder(POST_FEEDBACK_MSG).build());
						provideFeedback(executionId, digits, null);
						break;
					case "3":
						builder.say(new Say.Builder(POST_FEEDBACK_MSG).build());
						provideFeedback(executionId, digits, null);
						break;
					default:
						builder.say(new Say.Builder(WRONG_CHOICE_FEEDBACK_MSG).build());
						appendGather(builder, EXECUTION_HANDLE_TWIL_URL + executionId, CUSTOMER_FEEDBACK_OPTION_MSG);
						break;
					}
				} else {
					builder.say(new Say.Builder("Please make a selection").build());
					appendGather(builder, EXECUTION_HANDLE_TWIL_URL + executionId, CUSTOMER_FEEDBACK_OPTION_MSG);
				}
				break;

			case ACTION_RECORD:

				recordingUrl = request.getParameter("RecordingUrl");
				log.info(recordingUrl);
				provideFeedback(executionId, null, recordingUrl);
				builder.say(new Say.Builder(VOICE_MSG_THANKYOU_MSG).build());
				break;

			case ACTION_TRANSCRIPTION:

				recordingUrl = request.getParameter("RecordingUrl");
				log.info(recordingUrl);
				executions = findExecutionAssociatedWithTranscription(recordingUrl);
				if ((Integer) executions.get("size") > 0) {
					String transcriptionStatus = request.getParameter("TranscriptionStatus");
					log.info(transcriptionStatus);
					String transcriptionText = null;
					if (transcriptionStatus != null && transcriptionStatus.equals("completed")) {
						transcriptionText = request.getParameter("TranscriptionText");
					}
					String decoodaResult = null;
					String primaryEmotion = null;
					if (transcriptionText != null) {
						// Call Decooda
						Map<String, Object> decoodaResponse = analyzeTranscription(transcriptionText);
						if ((Integer) decoodaResponse.get("status") == 200) {
							Map<String, Object> score = (Map<String, Object>) ((Map<String, Object>) decoodaResponse
									.get("data")).get("score");
							decoodaResult = (String) score.get("valence_direction");
							primaryEmotion = (String) score.get("primary_emotion");
						}
					}
					executionId = ((Map<String, String>) ((List) (executions.get("data"))).get(0)).get("id");
					executeTranscriptionWaitTask(executionId, transcriptionText, decoodaResult, primaryEmotion);
				} else {
					log.info("No active transcription message wait events found");
				}
				break;

			default:
				builder.say(new Say.Builder("UNEXPECTED_ERR_MSG").build()).build();
				break;

			}
		}

		response.setContentType("application/xml");

		try {
			response.getWriter().print(builder.build().toXml());
		} catch (TwiMLException e) {
			e.printStackTrace();
		}
	}
	
	

	// This method will check if there is an active Twilio call wait message
	// subscription in Activiti for the give caller
	// The lookup is performed based on the called number
	public Map<String, Object> checkActiveTwilioCallWait(String callerNumber)
			throws JsonParseException, JsonMappingException, IOException {
		Map<String, Object> payload = new HashMap<>();
		payload.put("processDefinitionKey", PROCESS_KEY);
		payload.put("messageEventSubscriptionName", MESSAGE_NAME);
		List<Map<String, Object>> varList = new ArrayList<Map<String, Object>>();
		Map<String, Object> variable1 = new HashMap<>();
		variable1.put("name", "contactNumber");
		variable1.put("value", callerNumber);
		variable1.put("operation", "equals");
		variable1.put("type", "string");
		varList.add(variable1);
		payload.put("processInstanceVariables", varList);
		return checkActiveExecutions(payload);

	}

	// This method will check if there is an active Twilio Transcription
	// callback message subscription in Activiti for the given recording
	public Map<String, Object> findExecutionAssociatedWithTranscription(String recordingUrl)
			throws JsonParseException, JsonMappingException, IOException {
		Map<String, Object> payload = new HashMap<>();
		payload.put("processDefinitionKey", PROCESS_KEY);
		payload.put("messageEventSubscriptionName", TRANSCRIPTION_MESSAGE_NAME);
		List<Map<String, Object>> varList = new ArrayList<Map<String, Object>>();
		Map<String, Object> variable1 = new HashMap<>();
		variable1.put("name", "recordingUrl");
		variable1.put("value", recordingUrl);
		variable1.put("operation", "equals");
		variable1.put("type", "string");
		varList.add(variable1);
		payload.put("processInstanceVariables", varList);
		return checkActiveExecutions(payload);

	}

	// A common execution query helper method
	private Map<String, Object> checkActiveExecutions(Map<String, Object> payload)
			throws JsonProcessingException, IOException, JsonParseException, JsonMappingException {
		String jsonString = new ObjectMapper().writeValueAsString(payload);
		log.info(jsonString);
		HttpClient httpClient = new HttpClient();
		String response = (String) httpClient.execute(this.activitiBaseUrl + QUERY_EXECUTION_URL, "POST", jsonString,
				ContentType.APPLICATION_JSON, null, true);

		Map<String, Object> responseMap = new HashMap<String, Object>();
		log.info(responseMap.toString());
		// convert JSON string to Map
		responseMap = new ObjectMapper().readValue(response, new TypeReference<Map<String, Object>>() {
		});
		return responseMap;
	}

	// The helper method which is invoked when user provides feedback.
	// This method will complete an active message subscription associated with
	// the given execution id in Activiti
	public void provideFeedback(String executionId, String digits, String recordingUrl)
			throws JsonParseException, JsonMappingException, IOException {
		Map<String, Object> payload = new HashMap<>();
		payload.put("action", "messageEventReceived");
		payload.put("messageName", MESSAGE_NAME);
		List<Map<String, Object>> varList = new ArrayList<Map<String, Object>>();
		if (digits != null) {
			Map<String, Object> variable1 = new HashMap<>();
			variable1.put("name", "levelOfSatisfaction");
			if (digits.equals("2")) {
				variable1.put("value", "satisfied");
			} else {
				variable1.put("value", "notSatisfied");
			}
			varList.add(variable1);
		}

		if (recordingUrl != null) {
			Map<String, Object> variable2 = new HashMap<>();
			variable2.put("name", "recordingUrl");
			variable2.put("value", recordingUrl);
			varList.add(variable2);
		}
		payload.put("variables", varList);
		completeExecution(executionId, payload);
	}

	// When twilio transcription is completed, this method will complete an
	// active twilio callback message subscription associated with the given
	// execution id in Activiti.
	public void executeTranscriptionWaitTask(String executionId, String transcriptionText, String decoodaResult,
			String primaryEmotion) throws JsonParseException, JsonMappingException, IOException {
		Map<String, Object> payload = new HashMap<>();
		payload.put("action", "messageEventReceived");
		payload.put("messageName", TRANSCRIPTION_MESSAGE_NAME);
		List<Map<String, Object>> varList = new ArrayList<Map<String, Object>>();

		// When transcription is successful
		if (transcriptionText != null) {
			Map<String, Object> variable1 = new HashMap<>();
			variable1.put("name", "transcriptionText");
			variable1.put("value", transcriptionText);
			varList.add(variable1);

			Map<String, Object> variable3 = new HashMap<>();
			variable3.put("name", "decoodaResult");
			variable3.put("value", decoodaResult);
			varList.add(variable3);

			Map<String, Object> variable4 = new HashMap<>();
			variable4.put("name", "primaryEmotion");
			variable4.put("value", primaryEmotion);
			varList.add(variable4);
		}

		Map<String, Object> variable2 = new HashMap<>();
		variable2.put("name", "transcriptionSuccess");
		variable2.put("value", transcriptionText != null ? true : false);
		variable2.put("type", "boolean");
		varList.add(variable2);

		payload.put("variables", varList);
		completeExecution(executionId, payload);
	}

	// When the twilio transcription is complete, the transcribed text is
	// analysed using Decooda platform and results are associated with the
	// process in Activiti when completing an active twilio callback message
	// subscription via executeTranscriptionWaitTask()
	public Map<String, Object> analyzeTranscription(String transcriptionText) throws IOException, RuntimeException {
		Map<String, Object> responseMap;
		if (getDecoodaCallEnabled().equals(true)) {
			Map<String, Object> payload = new HashMap<>();
			payload.put("content", transcriptionText);
			Map<String, Object> params = new HashMap<>();
			ArrayList<String> focus = new ArrayList<>();
			params.put("model", this.decoodaModelNumber);
			params.put("content_media_type", "social_media");
			params.put("focus", focus);
			payload.put("params", params);
			String jsonString = new ObjectMapper().writeValueAsString(payload);
			log.info(jsonString);
			HttpClient httpClient = new HttpClient();
			String response = (String) httpClient.execute(this.decoodaUrl, "POST", jsonString,
					ContentType.APPLICATION_JSON, null, true);
			log.info("Successfully executed: " + jsonString);
			responseMap = new ObjectMapper().readValue(response, new TypeReference<Map<String, Object>>() {
			});
		} else {
			// Mocked response from Decooda
			String mockedResponse = "{\"status\":200,\"data\":{\"score\":{\"valence_confidence\":\"low\",\"purchase_path_score\":{\"purch_intent_neg_v2\":1},\"primary_emotion\":\"frustration\",\"valence_direction\":\"negative\",\"content_published_date\":\"2017-02-20T17:38:21.998+0000\",\"persona_score\":{\"detractor\":1},\"standard_intensity\":\"2\",\"primary_emotion_confidence\":\"medium\",\"other_score\":{},\"topic_score\":{\"negative priority 1\":1,\"negative priority 2\":1},\"standard_ispersonal\":\"1\",\"document_word_count\":\"21\",\"performance_score\":{\"recommend_neg\":1},\"standard_isnarative\":\"4\",\"content_media_type\":\"social_media\"}},\"message\":\"Content is scored successfully\"}";
			responseMap = new ObjectMapper().readValue(mockedResponse, new TypeReference<Map<String, Object>>() {
			});
		}
		return responseMap;
	}

	// A common helper method to complete a given execution
	private void completeExecution(String executionId, Map<String, Object> payload)
			throws JsonProcessingException, IOException {
		String jsonString = new ObjectMapper().writeValueAsString(payload);
		log.info(jsonString);
		HttpClient httpClient = new HttpClient();
		httpClient.execute(this.activitiBaseUrl + COMPLETE_EXECUTION_URL.replace("{executionId}", executionId), "PUT",
				jsonString, ContentType.APPLICATION_JSON, null, true);
		log.info("Successfully executed: " + jsonString);
	}

	private static void appendGather(VoiceResponse.Builder builder, String action, String message) {
		builder.gather(new Gather.Builder().say(new Say.Builder(message).build()).action(action).method(Method.GET)
				.numDigits(1).build());
	}

	public String getActivitiBaseUrl() {
		return activitiBaseUrl;
	}

	public void setActivitiBaseUrl(String activitiBaseUrl) {
		this.activitiBaseUrl = activitiBaseUrl;
	}

	public String getDecoodaUrl() {
		return decoodaUrl;
	}

	public void setDecoodaUrl(String decoodaUrl) {
		this.decoodaUrl = decoodaUrl;
	}

	public Boolean getDecoodaCallEnabled() {
		return decoodaCallEnabled;
	}

	public void setDecoodaCallEnabled(Boolean decoodaCallEnabled) {
		this.decoodaCallEnabled = decoodaCallEnabled;
	}

	public String getDecoodaModelNumber() {
		return decoodaModelNumber;
	}

	public void setDecoodaModelNumber(String decoodaModelNumber) {
		this.decoodaModelNumber = decoodaModelNumber;
	}
}