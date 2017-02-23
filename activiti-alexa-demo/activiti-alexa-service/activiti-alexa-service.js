var http = require('http');

/**
 * This sample illustrates how to use Alexa to integrate with Activiti using your voice.
 * 
 * author: Ciju Joseph
 * Also, Thanks to Greg's code at https://github.com/melahn/alexa-alfresco which I used as reference/template when building this! 
 **/
var host = ""; //Activiti host
var port = "8080"; //Activiti port
var tenant = "tenant_1";
var userId = ""; //Activiti userId
var password = ""; //Activiti password
var alfrescoActivitiVersion = "1.5.3.2+" //If you are using version 1.5.3.2 or above leave it this way. Else change it to something else. Used in scheduleServiceResponse() below
var baseActivitiUrl = "http://" + userId + ":" + password + "@" + host + ":" + port + "/activiti-app/api/"
var versionUrl = "http://" + userId + ":" + password + "@" + host + ":" + port + "/activiti-app/api/enterprise/app-version";
var startProcessUrl = baseActivitiUrl + "enterprise/process-instances";
var executionQueryUrl = baseActivitiUrl + "query/executions?tenantId="+tenant;
var executionBaseUrl = baseActivitiUrl + "runtime/executions";
var variablesBaseUrl = baseActivitiUrl + "enterprise/process-instances";
var appId = "flzfklasjfk"; /* use an Alexa appid here */ // TODO get this value from alexa skill configuration
var serviceBookingProcessDefinitionKey = "VehicleServiceBooking"
var serviceBookingProcessDefinitionId = "VehicleServiceBooking:49:18271" //This is not required to be set if your Activiti version is 1.5.3.2+ 
var defaultUser = "Ciju Joseph"
var defaultContactNumber = "+1XXXXXXXXXX"//Please make sure that you are using a valid phone number and this number will be used to send text messages via your twilio account
var contactEmail = "demo@example.com" //Used for customer notification

/**
 *  Route the incoming request based on the event type
 */

exports.handler = function (event, context) {
    try {
        console.log("event.session.application.applicationId=" + event.session.application.applicationId);
        /**
         * This prevents someone else from configuring a skill that sends requests to this function.
         */
     
        if (event.session.application.applicationId !== appId) {
             context.fail("Invalid Application ID");
        }
       

        if (event.session.new) {
            onSessionStarted({requestId: event.request.requestId}, event.session);
        }

        if (event.request.type === "LaunchRequest") {
            onLaunch(event.request,
                event.session,
                function callback(sessionAttributes, speechletResponse) {
                    context.succeed(buildResponse(sessionAttributes, speechletResponse));
                });
        } else if (event.request.type === "IntentRequest") {
             console.log("about to call onIntent");
             onIntent(event.request,
                event.session,
                function callback(sessionAttributes, speechletResponse) {
                    context.succeed(buildResponse(sessionAttributes, speechletResponse));
                });
        } else if (event.request.type === "SessionEndedRequest") {
            onSessionEnded(event.request, event.session);
            context.succeed();
        }
    } catch (e) {
        context.fail("Exception: " + e);
    }
};

/**
 *  Called on Session start
 *
 */

function onSessionStarted(sessionStartedRequest, session) {
    console.log("onSessionStarted requestId=" + sessionStartedRequest.requestId +
        ", sessionId=" + session.sessionId);
}

/**
 * Called when the user launches the skill without specifying what they want.
 */
function onLaunch(launchRequest, session, callback) {
    console.log("onLaunch requestId=" + launchRequest.requestId +
        ", sessionId=" + session.sessionId);

    // Dispatch to your skill's launch.
    getWelcomeResponse(callback);
}

/**
 * Called when the user specifies an intent for the Alfresco skill.
 */
function onIntent(intentRequest, session, callback) {
    console.log("onIntent requestId=" + intentRequest.requestId +
        ", sessionId=" + session.sessionId);
    var intent = intentRequest.intent,
        intentName = intentRequest.intent.name;
    console.log("intentName = " + intentName);
 
    if ("Available" === intentName) {
        getAvailableResponse(intent, session, callback);
    } else if ("Hello" === intentName) {
        getHelloResponse(intent, session, callback);
    } else if ("NeedHelp" === intentName) {
        getHelpResponse(intent, session, callback);
    } else if ("ScheduleService" === intentName) {
        scheduleServiceResponse(intent, session, callback);
    } else if ("CheckApmntDate" === intentName) {
        getApmntDateResponse(intent, session, callback);
    } else if ("RescheduleApmt" === intentName) {
        rescheduleApmtResponse(intent, session, callback);
    } else if ("CancelApmt" === intentName) {
        cancelApmtResponse(intent, session, callback);
    } else if ("Goodbye" === intentName) {
        getGoodbyeResponse(intent, session, callback);
    } else if ("AMAZON.HelpIntent" === intentName) {
        getWelcomeResponse(callback);
    } else {
        getWelcomeResponse(intent, session, callback);
    }
}

/**
 * Called when the user ends the session.
 * Is not called when the skill returns shouldEndSession=false.
 */
function onSessionEnded(sessionEndedRequest, session) {
    console.log("onSessionEnded requestId=" + sessionEndedRequest.requestId +
        ", sessionId=" + session.sessionId);
    // Nothing else to do at the moment
}

/**
 * --------------- Functions that control the skill's behavior -----------------------
 */

/**
 * Provide a friendly welcome for the user
 */

function getWelcomeResponse(callback) {
    console.log("getWelcomeResponse");
    var sessionAttributes = {"name":defaultUser};
    var cardTitle = "Welcome";
    var speechOutput = "Welcome to the Alfresco Process and Content Services. What can I do for you today? If you need help, please say I need help.";
    // If the user either does not reply to the welcome message or says something that is not
    // understood, they will be prompted again with this text.
    var repromptText = "I'm not sure I understand that.  Come again?";
    var shouldEndSession = false;

    callback(sessionAttributes,
        buildSpeechletResponse(cardTitle, speechOutput, repromptText, shouldEndSession));
}
/**
 * Determines if Alfresco is available and provides a response.
 */
function getAvailableResponse(intent, session, callback) {
    console.log("getAvailableResponse");
    var repromptText = null;
    var shouldEndSession = false;
    var name = getUserName(session.attributes);
    var speechOutputDown = "Alfresco is down";
    var speechOutputUp = "Alfresco is up";
    var speechOutput="";
    var request = intent.slots.status.value;
    if (request !== null && request !== undefined && (request === "up" || request === "down")) {
       speechOutput = name + "  You asked whether Alfresco is " + request + ".    ";  
    } else {
       speechOutput = name + "  I am not sure what you want to know but I can tell you that ";    
    }
    isAlfrescoAvailable(intent.name, session.attributes, callback, speechOutput, repromptText, shouldEndSession);
}
/* 
 * Gets the user name from the slot and saves it in the session
  
   Also provides a friendly greeting to that user.
*/
function getHelloResponse(intent, session, callback) {
    console.log("getHelloResponse");
    var repromptText = null;
    var shouldEndSession = false;
    var name = intent.slots.name.value;
    var sessionAttributes = {"name":name.toUpperCase()};
    console.log("sessionAttributes = " + sessionAttributes);
    var speechOutput = "Hello " + name + ".  What can I do for you today?";
    callback(sessionAttributes,
         buildSpeechletResponse(intent.name, speechOutput, repromptText, shouldEndSession));
}
/**
 *   Provides a goodbye response and ends the session
 */
function getGoodbyeResponse(intent, session, callback) {
    console.log("getGoodbyeResponse");
    var repromptText = null;
    var name = getUserName(session.attributes);
    var sessionAttributes = {};
    var shouldEndSession = true;
    var speechOutput = "Goodbye " + name + "  See you soon.";
    callback(sessionAttributes,
         buildSpeechletResponse(intent.name, speechOutput, repromptText, shouldEndSession));
}
/**
 *   Provides a list of supported utterances
 */
function getHelpResponse(intent, session, callback) {
    console.log("getGoodbyeResponse");
    var repromptText = null;
    var name = getUserName(session.attributes);
    var sessionAttributes = {};
    var shouldEndSession = false;
    var speechOutput = "Hello " + name + "  Here are some options available for you <break time=\"1s\"/>" 
        +"To check if the system is available, please say, Is Alfresco up? <break time=\"1s\"/>" 
        +"To schedule a service, please say, Schedule a service <break time=\"1s\"/>" 
        +"To check the date of my appointment, please say, Checking my appointment date <break time=\"1s\"/>" 
        +"To reschedule an appointment by 2 days, please say, Change my appointment by 2 days <break time=\"1s\"/>" 
         +"To cancel an appointment, please say, Cancel my appointment <break time=\"1s\"/>";
    callback(sessionAttributes,
         buildSpeechletResponse(intent.name, speechOutput, repromptText, shouldEndSession));
}

/**
 *   Tests whether the Alfresco Activiti endpoint is available
 */
function isAlfrescoAvailable(intentName, attributes, callback, speechOutput, repromptText, shouldEndSession) {
   var speechOutputDown = "Alfresco is down";
   var speechOutputUp = "Alfresco is up";
   
   var request = require("request");
   console.log("about to try request to get " + versionUrl);
   request(versionUrl, function(error, response, body) {
        if(response){
            console.log("HTTP statusCode=" + response.statusCode);        
            if (response.statusCode === 200)
               {
                  speechOutput += speechOutputUp;
               } else {
                  speechOutput += speechOutputDown;
               }
               callback(attributes,
                   buildSpeechletResponse(intentName, speechOutput, repromptText, shouldEndSession));
        }
        if(error){
             callback(attributes,
                   buildSpeechletResponse(intentName, speechOutputDown, repromptText, shouldEndSession));
        }
       
   })
}

/**
 *   Schedules a service which is basically creating a process instance in Activiti.
 */
function scheduleServiceResponse(intentName, session, callback) {
    console.log("scheduleServiceResponse");
    var name = getUserName(session.attributes).toUpperCase();
    var speechOutput = "";
    var repromptText = null;
    var shouldEndSession = false;
    var request = require("request");
    var request1 = require("request");
    //This if/else is due to a defect that was fixed in 1.5.3.2 https://issues.alfresco.com/jira/browse/ACTIVITI-619
    if(alfrescoActivitiVersion === "1.5.3.2+"){
        var reqObj = {"values":{"customerName":name, "contactNumber": defaultContactNumber, "contactEmail": contactEmail},"processDefinitionKey":serviceBookingProcessDefinitionKey, "name": "Vehicle Service Booking - Alexa"}
    } else{
        var reqObj = {"values":{"customerName":name, "contactNumber": defaultContactNumber, "contactEmail": contactEmail},"processDefinitionId":serviceBookingProcessDefinitionId, "name": "Vehicle Service Booking - Alexa"}
    }
    
    body = JSON.stringify(reqObj)
    request({
        url: startProcessUrl,
        method: 'POST',
        headers: {
            'Content-Type': 'application/json; charset=UTF-8'
        },
        body: body
    }, function (error, response, body) {
          if (error) {
              console.log(error);
              speechOutput = "An appointment could not be booked, Please try again after sometime";
              callback(session.attributes,
              buildSpeechletResponse(intentName, speechOutput, repromptText, shouldEndSession));
          } 
          else {
             console.log(response.statusCode, body);
             console.log("body = " + body);
             var respObj = eval("(" + body + ')');
             var appointmentDate = getVariableValue(respObj.variables, 'appointmentDate') 
             speechOutput = "Appointment booking successful. Your appoitnment is on " + simplifyDate(appointmentDate);
             callback(session.attributes,
                      buildSpeechletResponse(intentName, speechOutput, repromptText, shouldEndSession));
         }
    });
}

/**
 *   Check an existing appointment date. 
 * If an active appointment can be found, the appointment will be returned back
 */
 function getApmntDateResponse(intentName, session, callback) {
    console.log("scheduleServiceResponse");
    var name = getUserName(session.attributes).toUpperCase();
    var speechOutput = "";
    var repromptText = null;
    var shouldEndSession = false;
    var request = require("request");
    var request1 = require("request");
    var reqObj = {"processDefinitionKey":serviceBookingProcessDefinitionKey, "processInstanceVariables": [{"name":"customerName", "value":name, "operation": "equals", "type":"string"},{"name":"contactNumber", "value":defaultContactNumber, "operation": "equals", "type":"string"}]}
    body = JSON.stringify(reqObj)
    request({
        url: executionQueryUrl,
        method: 'POST',
        headers: {
            'Content-Type': 'application/json; charset=UTF-8'
        },
        body: body
    }, function (error, response, body) {
        if (error) {
            console.log(error);
            speechOutput = "We are experiencing technical difficulties in serving you, Please try again after sometime";
            callback(session.attributes,
                        buildSpeechletResponse(intentName, speechOutput, repromptText, shouldEndSession));
        } else {
            console.log(response.statusCode, body);
            console.log("body = " + body);
            var respObj = eval("(" + body + ')');
            if(respObj.size>0){
                var varResponse;
                request1({
                    url: variablesBaseUrl+"/"+respObj.data[0].processInstanceId+"/variables",
                    method: 'GET'
                }, function (error, response, body) {
                    if (error) {
                        console.log(error);
                        varResponse = null;
                    } else {
                        console.log(response.statusCode, body);
                        console.log("body = " + body);
                        varResponse = eval("(" + body + ')');
                    }
                    if(varResponse!=null){
                    var appointmentDate = getVariableValue(varResponse, 'appointmentDate') 
                    speechOutput = "You have your appointment coming up next on " + simplifyDate(appointmentDate);
                    } else{
                        speechOutput = "We are experiencing technical difficulties in serving you, Please try again after sometime";
                    }
                    callback(session.attributes,
                        buildSpeechletResponse(intentName, speechOutput, repromptText, shouldEndSession));
                });
                
            } else{
                speechOutput = "We are unable to locate an active appointment of your car"; 
                callback(session.attributes,
                        buildSpeechletResponse(intentName, speechOutput, repromptText, shouldEndSession));
            }
           
        }
    });
}
 
/**
*   Change an existing appointment date. If the "days" value can be extracted from the utterance,
* the appointment will be resheduled by that many "days" else the default process behaviour which is push by 1 minute
*/ 

function rescheduleApmtResponse(intent, session, callback) {
    var name = getUserName(session.attributes).toUpperCase();
    var speechOutput = "";
    var repromptText = null;
    var shouldEndSession = false;
    var request = require("request");
    var request1 = require("request");
    var days = 0
    if(intent.slots.days && intent.slots.days.value){
        days = parseInt(intent.slots.days.value)
    } 
    var reqObj = {"processDefinitionKey":serviceBookingProcessDefinitionKey, "messageEventSubscriptionName":"reschedule", "processInstanceVariables": [{"name":"customerName", "value":name, "operation": "equals", "type":"string"},{"name":"contactNumber", "value":defaultContactNumber, "operation": "equals", "type":"string"}]}
    body = JSON.stringify(reqObj)
    var execObj = {"action":"messageEventReceived", "messageName":"reschedule", "variables": [{"name":"changeAppointmentBy", "value":days}]}
    executebody = JSON.stringify(execObj)
    console.log(executebody)
    request({
        url: executionQueryUrl,
        method: 'POST',
        headers: {
            'Content-Type': 'application/json; charset=UTF-8'
        },
        body: body
    }, function (error, response, body) {
        if (error) {
            console.log(error);
            speechOutput = "We are experiencing technical difficulties in serving you, Please try again after sometime";
            callback(session.attributes,
                        buildSpeechletResponse(intent, speechOutput, repromptText, shouldEndSession));
        } else {
            console.log(response.statusCode, body);
            console.log("body = " + body);
            var respObj = eval("(" + body + ')');
            if(respObj.size>0){
                var varResponse;
                request1({
                    url: executionBaseUrl+"/"+respObj.data[0].id,
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json; charset=UTF-8'
                    },
                    body: executebody
                }, function (error, response, body) {
                    if (error) {
                        console.log(error);
                    } else {
                        console.log(response.statusCode, body);
                    }
                    speechOutput = "You have successfully rescheduled your appointment";
                    callback(session.attributes,
                        buildSpeechletResponse(intent, speechOutput, repromptText, shouldEndSession));
                });
            } else{
                speechOutput = "We are unable to locate an active appointment of your car"; 
                callback(session.attributes,
                        buildSpeechletResponse(intent, speechOutput, repromptText, shouldEndSession));
            }
           
        }

    });
   
}

/**
 *   Cancel an existing appointment associated with the caller
 */ 

function cancelApmtResponse(intentName, session, callback) {
    var name = getUserName(session.attributes).toUpperCase();
    var speechOutput = "";
    var repromptText = null;
    var shouldEndSession = false;
    var request = require("request");
    var request1 = require("request");
    var reqObj = {"processDefinitionKey":serviceBookingProcessDefinitionKey, "messageEventSubscriptionName":"cancelAppointment", "processInstanceVariables": [{"name":"customerName", "value":name, "operation": "equals", "type":"string"},{"name":"contactNumber", "value":defaultContactNumber, "operation": "equals", "type":"string"}]}
    body = JSON.stringify(reqObj)
    var execObj = {"action":"messageEventReceived", "messageName":"cancelAppointment"}
    executebody = JSON.stringify(execObj)
    request({
        url: executionQueryUrl,
        method: 'POST',
        headers: {
            'Content-Type': 'application/json; charset=UTF-8'
        },
        body: body
    }, function (error, response, body) {
        if (error) {
            console.log(error);
            speechOutput = "We are experiencing technical difficulties in serving you, Please try again after sometime";
            callback(session.attributes,
                        buildSpeechletResponse(intentName, speechOutput, repromptText, shouldEndSession));
        } else {
            console.log(response.statusCode, body);
            console.log("body = " + body);
            var respObj = eval("(" + body + ')');
            if(respObj.size>0){
                var varResponse;
                request1({
                    url: executionBaseUrl+"/"+respObj.data[0].id,
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json; charset=UTF-8'
                    },
                    body: executebody
                }, function (error, response, body) {
                    if (error) {
                        console.log(error);
                    } else {
                        console.log(response.statusCode, body);
                    }
                    speechOutput = "You have successfully cancelled your appointment";
                    callback(session.attributes,
                        buildSpeechletResponse(intentName, speechOutput, repromptText, shouldEndSession));
                });
            } else{
                speechOutput = "We are unable to locate an active appointment of your car"; 
                callback(session.attributes,
                        buildSpeechletResponse(intentName, speechOutput, repromptText, shouldEndSession));
            }
           
        }

    });
}

/**
 *  --------------- Helpers that build all of the responses -----------------------
 */

/**
 *  Builds a speech response
 */
function buildSpeechletResponse(title, output, repromptText, shouldEndSession) {
    return {
        outputSpeech: {
            type: "SSML",
            ssml: "<speak>"+ output + "</speak>"
        },
        card: {
            type: "Simple",
            title: "SessionSpeechlet - " + title,
            content: "SessionSpeechlet - " + output
        },
        reprompt: {
            outputSpeech: {
                type: "SSML",
                ssml: "<speak>"+ repromptText + "</speak>"
            }
        },
        shouldEndSession: shouldEndSession
    };
}

/**
 *  Builds a response
 */
function buildResponse(sessionAttributes, speechletResponse) {
    return {
        version: "1.0",
        sessionAttributes: sessionAttributes,
        response: speechletResponse
    };
}

/**
 *  Returns the name of the user that is currently in the session.
 * 
 *  If no name is in the session, returns "Dave" (reference to 2001, A Space Odyssey)
 */
function getUserName(sessionAttributes) {
    console.log("getUserName");
    console.log("sessionAttributes = " + sessionAttributes);
    var name = defaultUser;  // The default user name 
    if (sessionAttributes !== undefined && sessionAttributes !== null && sessionAttributes.name !== null) {
        name = sessionAttributes.name;
        console.log("The user name in the session = " + name)
    }
    return name;
}

/**
 *  Answers a simple date.
 * 
 *  Given a date of the form "2016-01-15T00:00:00.000+0000" will answer "2016-01-15"
 * 
 */
function simplifyDate(completeDate) {
    console.log("completeDate: " + completeDate)
    var simpleDate = completeDate.split("T", 1)[0];
    console.log("simpleDate: " + simpleDate)
    return simpleDate;  
}

function getVariableValue(variableObj, varName) {
    console.log("getVariableValue");
    var count = variableObj.length;
    var response = "";
    for (i = 0; i < count; i++) { 
    if(variableObj[i].name == varName){
        response = variableObj[i].value;
        break;
    }
    }
    return response;
}