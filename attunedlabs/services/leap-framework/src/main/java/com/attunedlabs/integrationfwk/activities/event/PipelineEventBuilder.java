package com.attunedlabs.integrationfwk.activities.event;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.LEAP_DATA_CONTEXT;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.camel.Exchange;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.attunedlabs.config.RequestContext;
import com.attunedlabs.eventframework.camel.eventproducer.AbstractCamelEventBuilder;
import com.attunedlabs.eventframework.config.EventFrameworkConfigurationException;
import com.attunedlabs.eventframework.config.EventFrameworkConstants;
import com.attunedlabs.eventframework.dispatcher.EventFrameworkDispatcherException;
import com.attunedlabs.eventframework.event.LeapEvent;
import com.attunedlabs.eventframework.jaxb.Event;
import com.attunedlabs.integrationfwk.activities.bean.ActivityConstant;
import com.attunedlabs.integrationfwk.config.jaxb.EventData;
import com.attunedlabs.integrationfwk.config.jaxb.EventPublishActivity;
import com.attunedlabs.integrationfwk.config.jaxb.PipeActivity;
import com.attunedlabs.integrationfwk.jdbcIntactivity.config.helper.JdbcIntActivityConfigHelper;
import com.attunedlabs.integrationfwk.jdbcIntactivity.config.helper.JdbcIntActivityConfigurationException;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.LeapServiceContext;

public class PipelineEventBuilder extends AbstractCamelEventBuilder {
	Logger logger = LoggerFactory.getLogger(PipelineEventBuilder.class);

	/**
	 * This method is used to build event for pipeline Activity
	 * 
	 * @param camelExchange : Camel Exchange Object
	 * @param eventConfig:  Event Object
	 */
	@Override
	public LeapEvent buildEvent(Exchange camelExchange, Event eventConfig) {
		String methodName = "buildEvent";
		logger.debug("{} entered into the method {}.{}()", LEAP_LOG_KEY, getClass().getName(), methodName);

		LeapDataContext leapDataContext = (LeapDataContext) camelExchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext serviceDataContext = leapDataContext.getServiceDataContext();
		PipeActivity pipeactivity = (PipeActivity) camelExchange.getIn()
				.getHeader(ActivityConstant.PIPEACTIVITY_HEADER_KEY);
		EventPublishActivity eventPublishActivity = pipeactivity.getEventPublishActivity();
		JdbcIntActivityConfigHelper activityConfigHelper = new JdbcIntActivityConfigHelper();
		String inBodyData = camelExchange.getIn().getBody(String.class);
		PipelineEvent pipelineEvent = null;
		try {
			Document document = activityConfigHelper.generateDocumentFromString(inBodyData);
			logger.info("{} In Body Data : {}", LEAP_LOG_KEY, inBodyData);
			pipelineEvent = createPipeLineEvent(eventPublishActivity, document, serviceDataContext);
		} catch (JdbcIntActivityConfigurationException e) {
			// #TODO, build event doesnot throw exception so catching it
			logger.error("{} error in geting the document object for incoming exchange body: {}", LEAP_LOG_KEY,
					inBodyData, e);
		}
		logger.debug("{} exiting from the {}.{}()", LEAP_LOG_KEY, getClass().getName(), methodName);
		return pipelineEvent;
	}

	/**
	 * This method is used to create pipeline event
	 * 
	 * @param eventPublishActivity : EventPublishActivity Object
	 * @param document             : DOcument Object
	 * @param leapHeader           : LeapHeader Object
	 * @return PipelineEvent Object
	 * @throws EventFrameworkConfigurationException
	 * @throws EventFrameworkDispatcherException
	 */
	private PipelineEvent createPipeLineEvent(EventPublishActivity eventPublishActivity, Document document,
			LeapServiceContext serviceDataContext) {
		String methodName = "createPipeLineEvent";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		RequestContext requestContext = serviceDataContext.getRequestContext();
		// creating pipeline event object
		logger.trace("{} EventName : {}", LEAP_LOG_KEY, eventPublishActivity.getEventName().trim());
		PipelineEvent pipelineEvent = new PipelineEvent(eventPublishActivity.getEventName().trim(), requestContext);
		Map<String, Serializable> eventParam = pipelineEvent.getObject();
		logger.trace("{} eventParam : {} ", LEAP_LOG_KEY, eventParam.keySet().toString());
		List<EventData> eventDataList = eventPublishActivity.getEventActivityParams().getEventData();
		XPath xPath = XPathFactory.newInstance().newXPath();
		JSONObject eventJObj = new JSONObject();
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		for (EventData eventData : eventDataList) {
			try {
				logger.debug("eventData :- " + eventData.getValue() + ":: eventExpressionValue : "
						+ eventData.getExpressionValue());
				XPathExpression expr = xPath.compile(eventData.getXpathExpression());
				String eventName = eventData.getExpressionValue().toString();
				NodeList nodeList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
				if (nodeList != null && nodeList.getLength() > 0) {
					try {
						if (nodeList.item(0).getNodeName().contains("text") && eventName != null) {
							eventJObj.put(eventName, nodeList.item(0).getTextContent());
							logger.trace("{} added {} into the eventName ", LEAP_LOG_KEY,
									nodeList.item(0).getTextContent());
						} else if (eventName != null) {
							eventJObj.put(eventName, nodeList.item(0).getTextContent());
							logger.trace("{} added {} into the eventName ", LEAP_LOG_KEY,
									nodeList.item(0).getTextContent());
						}
					} catch (DOMException | JSONException e) {
						logger.error("{} Unable to add {} into {}", nodeList.item(0).getNodeName(),
								EventFrameworkConstants.OBJECT_KEY);
					}
				}
			} catch (XPathExpressionException e) {
				logger.error("{} error in processing the xpath : {}" ,LEAP_LOG_KEY, eventData.getXpathExpression(), e);
			}
			Calendar cal = Calendar.getInstance();
			String eventRaisedDateTime = dateFormat.format(cal.getTime());
			try {
				logger.trace("{} EventCreatedOn : = > {}" ,LEAP_LOG_KEY, eventRaisedDateTime);
				eventJObj.put("EventCreatedOn", eventRaisedDateTime);
			} catch (JSONException e) {
				logger.error("{} Unable to add EventCreatedOn into {}" ,LEAP_LOG_KEY, EventFrameworkConstants.OBJECT_KEY);
			}
		}
		pipelineEvent.addObject(EventFrameworkConstants.OBJECT_KEY, eventJObj.toString());
		logger.info("{} Final Event BuildUp : {}" ,LEAP_LOG_KEY, pipelineEvent.getObject().toString());
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return pipelineEvent;
	}// end of method createPipeLineEvent

}
