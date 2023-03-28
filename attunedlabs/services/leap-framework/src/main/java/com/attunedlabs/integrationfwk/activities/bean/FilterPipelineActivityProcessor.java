package com.attunedlabs.integrationfwk.activities.bean;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.LEAP_DATA_CONTEXT;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.attunedlabs.integrationfwk.config.jaxb.Condition;
import com.attunedlabs.integrationfwk.config.jaxb.ConditionFaliure;
import com.attunedlabs.integrationfwk.config.jaxb.ConditionSuccess;
import com.attunedlabs.integrationfwk.config.jaxb.Conditions;
import com.attunedlabs.integrationfwk.config.jaxb.Drop;
import com.attunedlabs.integrationfwk.config.jaxb.FilterPipelineActivity;
import com.attunedlabs.integrationfwk.config.jaxb.FilterResponse;
import com.attunedlabs.integrationfwk.config.jaxb.PipeActivity;
import com.attunedlabs.integrationfwk.config.jaxb.Send;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.LeapServiceContext;

/**
 * This class is used to process Filter pipeline activity defined in pipeline
 * config xml. Purpose is to check of request contain specified data as method
 * in condition using the operator specified or not.If, yes then perform
 * conditionSuccess else condition faliure.
 * 
 * @author bizruntime
 *
 */
public class FilterPipelineActivityProcessor {
	Logger logger = LoggerFactory.getLogger(FilterPipelineActivityProcessor.class);

	/**
	 * This method is used to proccess the filter pipeline request
	 * 
	 * @param exchange : Camel Exchange object
	 * @throws ActivityEnricherException
	 */
	public void processAndFilterRequest(Exchange exchange) throws ActivityEnricherException {
		String methodName = "processAndFilterRequest";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		PipeActivity pipeactivity = (PipeActivity) exchange.getIn().getHeader(ActivityConstant.PIPEACTIVITY_HEADER_KEY);
		Conditions filterConditions = pipeactivity.getFilterPipelineActivity().getConditions();
		String incommingData = exchange.getIn().getBody(String.class);

		Document document = generateDocumentFromXMLString(incommingData);
		if (document != null) {
			LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn().getHeader(LEAP_DATA_CONTEXT);
			LeapServiceContext serviceDataContext = leapDataContext.getServiceDataContext();
			Map<String, Object> serviceRequestData = serviceDataContext.getServiceRequestDataFromServiceContext();
			boolean conditionEvalResponse = evaluvateConditionExpressionWithXML(filterConditions, document);
			if (conditionEvalResponse) {
				FilterPipelineActivity filterPipelineActivity = pipeactivity.getFilterPipelineActivity();
				ConditionSuccess conditionSuccess = filterPipelineActivity.getConditionSuccess();
				Send send = conditionSuccess.getSend();
				Drop drop = conditionSuccess.getDrop();
				FilterResponse filterResponse = conditionSuccess.getFilterResponse();
				if (send != null) {
					logger.trace("{} condition success send value is : {} ", LEAP_LOG_KEY, send.getValue());
					// exchange.getIn().setHeader(ActivityConstant.FILTER_RESULT_SEND_KEY,
					// send.getValue());
					serviceRequestData.put(ActivityConstant.FILTER_RESULT_SEND_KEY, send.getValue());
				}
				if (drop != null) {
					logger.trace("{} condition success drop value is : {}", LEAP_LOG_KEY, drop.getValue());
					// exchange.getIn().setHeader(ActivityConstant.FILTER_RESULT_DROP_KEY,
					// drop.getValue());
					serviceRequestData.put(ActivityConstant.FILTER_RESULT_DROP_KEY, drop.getValue());
				}
				if (filterResponse != null) {
					logger.info("{} filter response value is {} ", LEAP_LOG_KEY, filterResponse.getName());
					// exchange.getIn().setHeader(ActivityConstant.FILTER_RESPONSE_KEY,filterResponse.getName());
					serviceRequestData.put(ActivityConstant.FILTER_RESPONSE_KEY, filterResponse.getName());
				}
			} else {
				FilterPipelineActivity filterPipelineActivity = pipeactivity.getFilterPipelineActivity();
				ConditionFaliure conditionFaliure = filterPipelineActivity.getConditionFaliure();
				Send send = conditionFaliure.getSend();
				Drop drop = conditionFaliure.getDrop();
				FilterResponse filterResponse = conditionFaliure.getFilterResponse();
				if (send != null) {
					logger.trace("{} condition faliure send value is : {}", LEAP_LOG_KEY, send.getValue());
					// exchange.getIn().setHeader(ActivityConstant.FILTER_RESULT_SEND_KEY,
					// send.getValue());
					serviceRequestData.put(ActivityConstant.FILTER_RESULT_SEND_KEY, send.getValue());
				}
				if (drop != null) {
					logger.trace("{} condition faliure drop value is : {}", LEAP_LOG_KEY, drop.getValue());
					// exchange.getIn().setHeader(ActivityConstant.FILTER_RESULT_DROP_KEY,
					// drop.getValue());
					serviceRequestData.put(ActivityConstant.FILTER_RESULT_DROP_KEY, drop.getValue());
				}
				if (filterResponse != null) {
					logger.info("{} filter response value is {}", LEAP_LOG_KEY, filterResponse.getName());
					// exchange.getIn().setHeader(ActivityConstant.FILTER_RESPONSE_KEY,filterResponse.getName());
					serviceRequestData.put(ActivityConstant.FILTER_RESPONSE_KEY, filterResponse.getName());
				}
			}
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}// end of method

	/**
	 * This method is used evaluate the condition expression given in filter
	 * pipeline activity
	 * 
	 * @param ConditionsObj : Condition object of FilterPipelineActivity
	 * @param documentObj   : xml Document Object
	 * @return condResult : true/false
	 * @throws ActivityEnricherException
	 */
	private boolean evaluvateConditionExpressionWithXML(Conditions ConditionsObj, Document documentObj)
			throws ActivityEnricherException {
		String methodName = "evaluvateConditionExpressionWithXML";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		XPath xPath = XPathFactory.newInstance().newXPath();
		List<Condition> conditionList = ConditionsObj.getCondition();
		List<Boolean> conditionExpRespList = new ArrayList<>();
		boolean overAllConditionExpRes = false;
		Boolean conditionExpResp = false;
		for (Condition conditionObj : conditionList) {
			try {
				XPathExpression expr = xPath.compile(conditionObj.getExpression());
				logger.info("{} xPath Expression : {}", LEAP_LOG_KEY, expr);
				NodeList nodeList = (NodeList) expr.evaluate(documentObj, XPathConstants.NODESET);
				logger.trace("{} nodeList object in processConditionExpressionWithXML of filter: {}", LEAP_LOG_KEY,
						nodeList);
				logger.trace("{} The length of nodeList: {} ", LEAP_LOG_KEY, nodeList.getLength());
				if (nodeList != null && nodeList.getLength() > 0) {
					if ((conditionObj.getValue().isEmpty() && conditionObj.getValue().length() <= 0)
							|| conditionObj.getValue() == null) {
						logger.trace("{} value is not defined for xpath expression but node exist ", LEAP_LOG_KEY);
						conditionExpResp = true;
						conditionExpRespList.add(conditionExpResp);
					} else {
						for (int i = 0; i < nodeList.getLength(); i++) {
							String fieldVal = nodeList.item(i).getTextContent();
							logger.trace("{} The field values : {}", LEAP_LOG_KEY, fieldVal);
							logger.debug("{} pipeline expr value : {}", LEAP_LOG_KEY, conditionObj.getValue().trim());
							if (fieldVal.trim().equalsIgnoreCase(conditionObj.getValue().trim())) {
								logger.trace("{} field value matches with exp value", LEAP_LOG_KEY);
								conditionExpResp = true;
								conditionExpRespList.add(conditionExpResp);
							} else {
								logger.trace("{} field value didnot matched with exp value", LEAP_LOG_KEY);
								conditionExpResp = false;
								conditionExpRespList.add(conditionExpResp);
							}
						}
					}
				} else {
					// #TODO, not sure to throw custom exception or to return
					// false value.For now m setting false value.
					conditionExpResp = false;
					conditionExpRespList.add(conditionExpResp);
					// throw new ActivityEnricherException("Given condition
					// expression : "+conditionObj.getExpression()+" is not
					// available in requested xml. Please check the condition
					// expression.");
				}

			} catch (XPathExpressionException e) {
				throw new ActivityEnricherException(
						"Unable to evaluate xpath expression for the expression : " + conditionObj.getExpression());
			}
		}
		overAllConditionExpRes = applyOperatorOnConditionResponse(ConditionsObj.getOperator().trim(),
				conditionExpRespList);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return overAllConditionExpRes;
	}// end of method evaluvateConditionExpressionWithXML

	/**
	 * This method is used to check what operator to used for conditions and call
	 * required method to perform operator on condition response
	 * 
	 * @param operator             : operator(AND/OR) in String
	 * @param conditionExpRespList : List of response of each condtion evaluation
	 * @return
	 * @throws ActivityEnricherException
	 */
	private boolean applyOperatorOnConditionResponse(String operator, List<Boolean> conditionExpRespList)
			throws ActivityEnricherException {
		String methodName = "applyOperatorOnConditionResponse";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		boolean conditionExpRes = false;
		if (conditionExpRespList != null && !(conditionExpRespList.isEmpty())) {
			logger.trace("{} conditionExpRespList list values : {}, operator value:{} ",LEAP_LOG_KEY, conditionExpRespList, operator);
			switch (operator) {
			case "AND": {
				logger.trace("{} Applied AND Operator",LEAP_LOG_KEY);
				conditionExpRes = applyANDOperatorOnConditionResponse(conditionExpRespList);
				break;
			}
			case "OR": {
				logger.trace("{} Applied OR Operator",LEAP_LOG_KEY);
				conditionExpRes = applyOROperatorOnConditionResponse(conditionExpRespList);
				break;
			}
			default:
				throw new ActivityEnricherException(
						"operator : " + operator + " is undefined for overall condition evaluation");
			}
		} else {
			throw new ActivityEnricherException(
					"No condition specified in filter pipeline activity matches with incoming request");
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return conditionExpRes;
	}// end of method applyOperatorOnCondtion

	/**
	 * This method is used to perform OR operation on the response got from
	 * condition given in filterPipelineActivity
	 * 
	 * @param ConditionsObj        : Condition Object from FilterPipelineActivity
	 * @param conditionExpRespList : List of response(boolean value) based on the
	 *                             condition match
	 * @return condResult : overoll result of condition evaluation (true/false)
	 */
	private boolean applyANDOperatorOnConditionResponse(List<Boolean> conditionExpRespList) {
		String methodName = "applyANDOperatorOnConditionResponse";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		boolean condResult = true;
		if (conditionExpRespList != null && !(conditionExpRespList.isEmpty())) {
			logger.trace("{} conditionExpRespList is not null and not empty",LEAP_LOG_KEY);
			for (Boolean bool : conditionExpRespList) {
				logger.trace("{} boolean value for performing add : {}",LEAP_LOG_KEY, bool.booleanValue());
				if (bool.booleanValue() && condResult) {
					condResult = true;
				} else {
					condResult = false;
				}
			}
		}
		logger.info("{} Result of AND operation on conditions : {}" ,LEAP_LOG_KEY, condResult);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return condResult;

	}// end of method applyANDOperatorOnConditionResponse

	/**
	 * This method is used to perform OR operation on the response got from
	 * condition given in filterPipelineActivity
	 * 
	 * @param ConditionsObj        : Condition Object from FilterPipelineActivity
	 * @param conditionExpRespList : List of response(boolean value) based on the
	 *                             condition match
	 * @return condResult : overoll result of condition evaluation (true/false)
	 */
	private boolean applyOROperatorOnConditionResponse(List<Boolean> conditionExpRespList) {
		String methodName = "applyOROperatorOnConditionResponse";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		boolean condResult = false;
		if (conditionExpRespList != null && !(conditionExpRespList.isEmpty())) {
			logger.trace("{} conditionExpRespList is not null and not empty",LEAP_LOG_KEY);
			for (Boolean bool : conditionExpRespList) {
				logger.trace("{} boolean value for performing or : {}" ,LEAP_LOG_KEY, bool.booleanValue());
				if (bool.booleanValue() || condResult) {
					condResult = true;
				} else {
					condResult = false;
				}
			}
		}
		logger.info("{} Result of OR operation on conditions : {}" ,LEAP_LOG_KEY, condResult);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return condResult;

	}// end of method applyOROperatorOnConditionResponse

	/**
	 * to generate the document object once and all from the xml input which is of
	 * String
	 * 
	 * @param xmlInput : xml input in String format
	 * @return documentObject : Document Object for the xml
	 * @throws ActivityEnricherException
	 */
	private Document generateDocumentFromXMLString(String xmlInput) throws ActivityEnricherException {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		Document xmlDocument = null;
		try {
			builder = builderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new ActivityEnricherException("Unable to initiate the document builder..", e);
		}
		try {
			if (xmlInput != null && !(xmlInput.isEmpty())) {
				xmlDocument = builder.parse(new ByteArrayInputStream(xmlInput.getBytes("UTF-16")));
			} else {
				throw new ActivityEnricherException("input xml data is null for filter pipeline activity");
			}
		} catch (SAXException | IOException e) {
			throw new ActivityEnricherException("Unable to parse the xmlString into document..", e);
		}
		return xmlDocument;
	}// end of method generateDocumentFromXMLString

}
