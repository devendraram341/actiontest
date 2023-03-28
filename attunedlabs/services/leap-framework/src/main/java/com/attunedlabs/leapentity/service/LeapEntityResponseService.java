package com.attunedlabs.leapentity.service;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import org.apache.camel.Exchange;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.featuredeployment.FeatureDeploymentServiceException;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.context.bean.LeapDataElement;
import com.attunedlabs.leap.context.constant.LeapDataContextConstant;
import com.attunedlabs.leap.context.exception.UnableToApplyTemplateException;
import com.attunedlabs.leapentity.autoIncrement.LeapEntityAutoIncrementFactoryBean;

public class LeapEntityResponseService {
	private static final Logger logger = LoggerFactory.getLogger(LeapEntityResponseService.class);

	public Object createEntityFinalResponse(Exchange exchange) {
		String methodName = "createEntityFinalResponse";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Object entityResponseObject = null;
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn()
				.getHeader(LeapDataContextConstant.LEAP_DATA_CONTEXT);
		boolean applyLDC = exchange.getIn().getHeader(LeapEntityServiceConstant.APPLY_LDC, Boolean.class);
		if (applyLDC) {
			entityResponseObject = exchange.getIn().getBody();

		} else {
			LeapDataElement dataElement = leapDataContext
					.getContextElement(LeapEntityServiceConstant.ENTITY_RESULT_SET);
			entityResponseObject = dataElement.getData().getItems().getData();
			logger.info("{} final Entity Response :- {} " ,LEAP_LOG_KEY, entityResponseObject.toString());
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return entityResponseObject;
		}
		if (entityResponseObject instanceof JSONObject) {
			JSONObject jsonRequest = (JSONObject) entityResponseObject;
			if (jsonRequest.has("root")) {
				entityResponseObject = jsonRequest.get("root");
			}
		} else if (entityResponseObject instanceof JSONArray) {
			JSONArray jsonArray = (JSONArray) entityResponseObject;
			JSONObject jsonRequest = (JSONObject) jsonArray.get(0);
			if (jsonRequest.has("root")) {
				entityResponseObject = jsonRequest.get("root");
			}
		}
		logger.info("{} final Entity Response :- {}" ,LEAP_LOG_KEY, entityResponseObject.toString());
		logger.debug("{} response Time :- {}",LEAP_LOG_KEY, System.currentTimeMillis());
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return entityResponseObject;
	}

	public void applyLDCConfiguration(Exchange exchange)
			throws FeatureDeploymentServiceException, UnableToApplyTemplateException {
		String methodName = "applyLDCConfiguration";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		String taxonomyId = null;
		Boolean taxonomyRequired = exchange.getIn().getHeader(LeapEntityServiceConstant.TAXONOMY_REQUIRED,
				Boolean.class);
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn()
				.getHeader(LeapDataContextConstant.LEAP_DATA_CONTEXT);
		logger.debug("{} taxonomyRequired :: {}" ,LEAP_LOG_KEY, taxonomyRequired);
		if (taxonomyRequired) {
			Object taxonomyFileObj = exchange.getIn().getHeader(LeapEntityServiceConstant.TAXONOMY_FILE_NAME);
			if (taxonomyFileObj != null)
				taxonomyId = taxonomyFileObj.toString();
			else {
				leapDataContext.getVendorTaxonomyId(exchange);
				taxonomyId = exchange.getIn().getHeader(LeapDataContextConstant.TAXONOMY_ID_INHEADER, String.class);
			}
		}
		Boolean schemaRequired = exchange.getIn().getHeader(LeapEntityServiceConstant.SCHEMA_REQUIRED, Boolean.class);
		Boolean projectTionRequired = exchange.getIn().getHeader(LeapEntityServiceConstant.PROJECTION_REQUIRED,
				Boolean.class);
		String schemaFileName = exchange.getIn().getHeader(LeapEntityServiceConstant.SCHEMA_FILE_NAME, String.class);
		String projectFileName = exchange.getIn().getHeader(LeapEntityServiceConstant.PROJECTION_FILE_NAME,
				String.class);
		String projectionSource = exchange.getIn().getHeader(LeapEntityServiceConstant.PROJECTION_SOURCE, String.class);

		Object response = leapDataContext.applyTemplate(schemaRequired, projectTionRequired, schemaFileName,
				projectFileName, projectionSource, taxonomyId);
		logger.info("{} final response of after apply template {}",LEAP_LOG_KEY,response);
		exchange.getIn().setBody(response);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}
}
