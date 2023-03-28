package com.attunedlabs.leap.context.bean.helper;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import com.attunedlabs.featuredeployment.FeatureDeploymentServiceException;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.context.constant.LeapDataContextConstant;
import com.attunedlabs.leap.context.exception.UnableToApplyTaxonomyException;
import com.attunedlabs.leap.context.exception.UnableToApplyTemplateException;
import com.attunedlabs.leap.context.exception.UnableToGetLeapDataContextInstanceException;
import com.attunedlabs.leap.util.LeapConfigurationUtil;

/**
 * To apply leapDataContext configuration inside spring dsl routes
 * 
 * @author Reactiveworks42
 *
 */
public class LeapDataContextConfigurationHelper {

	private static Logger logger = LoggerFactory.getLogger(LeapDataContextConfigurationHelper.class);

	/**
	 * this method is used to apply Template, projection and taxonomy with file name
	 * 
	 * @param isTemplateRequired
	 * @param isProjectionRequired
	 * @param templateFileName
	 * @param projectionFileName
	 * @param projectionSource
	 * @param taxonomyId
	 * @param exchange
	 * @return {@link Object}
	 * @throws UnableToApplyTemplateException
	 */
	public Object applyTemplate(boolean isTemplateRequired, boolean isProjectionRequired, String projectionSource,
			String taxonomyId, String templateFileName, String projectionFileName, Exchange exchange)
			throws UnableToApplyTemplateException {
		String methodName = "applyTemplate";
		logger.debug(
				"{} entered into the method {}, isTemplateRequired {}, isProjectionRequired {},  projectionSource {},  taxonomyId{},  templateFileName{},  projectionFileName {}",
				LEAP_LOG_KEY, methodName, isTemplateRequired, isProjectionRequired, projectionSource, taxonomyId,
				templateFileName, projectionFileName);
		Object leapDataContextObject = exchange.getIn().getHeader(LeapDataContextConstant.LEAP_DATA_CONTEXT);
		if (leapDataContextObject == null || !(leapDataContextObject instanceof LeapDataContext)) {
			throw new UnableToGetLeapDataContextInstanceException(" unable to get LeapDataContext instance ");
		}
		Object ldcResponse = null;
		LeapDataContext leapDataContext = (LeapDataContext) leapDataContextObject;
		if (templateFileName == null && projectionFileName == null) {
			logger.trace("{} calling applyTemplate method of LDC without resources Name", LEAP_LOG_KEY);
			ldcResponse = leapDataContext.applyTemplate(isTemplateRequired, isProjectionRequired, taxonomyId,
					projectionSource);
		} else {
			logger.trace("{} calling applyTemplate method of LDC with resources Name", LEAP_LOG_KEY);
			ldcResponse = leapDataContext.applyTemplate(isTemplateRequired, isProjectionRequired, templateFileName,
					projectionFileName, projectionSource, taxonomyId);
		}
		logger.trace("{} applyTemplate response :: ", LEAP_LOG_KEY, ldcResponse);
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
		return ldcResponse;
	}// end of the applyTemplate..

	/**
	 * this method is used to get the vendorTaxonomyId. If vendor taxonomy is null,
	 * return taxonomyId present in request. If it is null return default
	 * taxonomyId.
	 * 
	 * @param exchange
	 * @throws FeatureDeploymentServiceException
	 */
	public void getVendorTaxonomyId(Exchange exchange) throws FeatureDeploymentServiceException {
		String methodName = "getVendorTaxonomyId";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Object leapDataContextObject = exchange.getIn().getHeader(LeapDataContextConstant.LEAP_DATA_CONTEXT);
		if (leapDataContextObject == null || !(leapDataContextObject instanceof LeapDataContext)) {
			throw new UnableToGetLeapDataContextInstanceException(" unable to get LeapDataContext instance ");
		}
		LeapDataContext leapDataContext = (LeapDataContext) leapDataContextObject;
		leapDataContext.getVendorTaxonomyId(exchange);
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
	}// end of getVendorTaxonomyId..

	/**
	 * This helper method is used to generate the LDC response to the client
	 * 
	 * @param exchange :: {@link Exchange}
	 * @return {@link Object}
	 */
	public Object generateResponse(Exchange exchange) {
		String methodName = "generateResponse";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Object entityResponseObject = null;
		entityResponseObject = exchange.getIn().getBody();
		logger.trace("{} final Entity Response {} ", LEAP_LOG_KEY, entityResponseObject.toString());
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
		logger.debug("{} final Entity Response :- {}", LEAP_LOG_KEY, entityResponseObject.toString());
		logger.debug("{}  response Time :- {} ", LEAP_LOG_KEY, System.currentTimeMillis());
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
		return entityResponseObject;
	}// ..end of the method of generateResponse

	/**
	 * Method used to set LeapDataElement's data to exchange body for the given
	 * 'tagName'
	 * 
	 * @param tagName
	 * @param exchange
	 */
	public void setLeapDataElementIntoExchange(String tagName, Exchange exchange) {
		LeapDataContext leapDataCtx = (LeapDataContext) exchange.getIn()
				.getHeader(LeapDataContextConstant.LEAP_DATA_CONTEXT);
		if (tagName != null) {
			if (tagName.equals("initial")) {
				exchange.getIn().setBody(leapDataCtx.getInitialRequestData().getData().toString());
				return;
			} else {
				exchange.getIn().setBody(leapDataCtx.getDataByTag(tagName).getData().toString());
				return;
			}
		}
	}

	/**
	 * This method is used to apply taxonomy to exchange body and store it in
	 * exchange itself
	 * 
	 * @param exchange
	 * @throws UnableToApplyTaxonomyException
	 */
	public void applyTaxonomy(Exchange exchange) throws UnableToApplyTaxonomyException {
		String methodName = "applyTaxonomy";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapDataContext leapDataContext = LeapConfigurationUtil.getLDCFromExchange(exchange);
		String reqBody = exchange.getIn().getBody(String.class);
		String taxonomyId = (String) exchange.getIn().getHeader(LeapDataContextConstant.TAXONOMY_ID_INHEADER);
		try {
			if (taxonomyId == null) {
				leapDataContext.getVendorTaxonomyId(exchange);
				taxonomyId = exchange.getIn().getHeader(LeapDataContextConstant.TAXONOMY_ID_INHEADER, String.class);
			}
			leapDataContext.setTaxonomyJSON(taxonomyId);
			JSONObject afterApplyingTaxnomy = leapDataContext.applyTaxonomy(new JSONObject(reqBody), new JSONObject());
			exchange.getIn().setBody(afterApplyingTaxnomy.toString());
		} catch (Exception e) {
			logger.error("Unable to applyTaxonomy:: {}", LEAP_LOG_KEY, e.getMessage());
			throw new UnableToApplyTaxonomyException("Unable to applyTaxonomy:: ",
					"Unable to applyTaxonomy:: " + e.getMessage(), 500);
		}
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * This method is used to apply taxonomy to exchange body and store it in
	 * exchange itself
	 * 
	 * @param taxonomyId
	 * @param exchange
	 * @throws UnableToApplyTaxonomyException
	 */
	public void applyTaxonomyToExchangeBody(String taxonomyId, Exchange exchange)
			throws UnableToApplyTaxonomyException {
		String methodName = "applyTaxonomyToExchangeBody";
		logger.debug("{} entered into the method {}, taxonomyId {}", LEAP_LOG_KEY, methodName, taxonomyId);
		try {

			LeapDataContext leapDataContext = LeapConfigurationUtil.getLDCFromExchange(exchange);
			String reqBody = exchange.getIn().getBody(String.class);
			nullOrEmptyCheck(taxonomyId, "taxonomyId");
			leapDataContext.setTaxonomyJSON(taxonomyId);
			JSONObject afterApplyingTaxnomy = leapDataContext.applyTaxonomy(new JSONObject(reqBody), new JSONObject());
			exchange.getIn().setBody(afterApplyingTaxnomy.toString());
		} catch (UnableToApplyTaxonomyException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Unable to applyTaxonomy:: {}", LEAP_LOG_KEY, e.getMessage());
			throw new UnableToApplyTaxonomyException("Unable to applyTaxonomy:: ",
					"Unable to applyTaxonomy:: " + e.getMessage(), 500);
		}
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * This method get request body from Ldc and apply taxonomy. After that it will
	 * store applied taxonomy body into LDC by given push Tag.
	 * 
	 * @param taxonomyId - if it is empty or null. uses vendorTaxonomyId
	 * @param ldcGetTag  - used to get data from LDC by tag name. if you provide
	 *                   null or empty it will get data from "initial Request body"
	 * @param ldcPushTag - it is the tag name which help us to push data into LDC
	 *                   and it cannot be null or empty
	 * @param exchange
	 * @throws UnableToApplyTaxonomyException
	 */
	public void applyTaxonomyWithLDC(String taxonomyId, String ldcGetTag, String ldcPushTag, Exchange exchange)
			throws UnableToApplyTaxonomyException {
		String methodName = "applyTaxonomyWithLDC";
		logger.debug("{} entered into the method {} ldcGetTag:{}, ldcPushTag{} ", LEAP_LOG_KEY, methodName, ldcGetTag,
				ldcPushTag);
		try {
			LeapDataContext leapDataContext = LeapConfigurationUtil.getLDCFromExchange(exchange);
			String reqBody = LeapConfigurationUtil.getRequestBodyFromLDC(leapDataContext, ldcGetTag);
			logger.debug("{} request body Is:: {}", LEAP_LOG_KEY, reqBody);
			nullOrEmptyCheck(ldcPushTag, "ldc Push Tag");
			nullOrEmptyCheck(taxonomyId, "taxonomyId");
			logger.debug("{} taxonomyId in applyTaxonomy is:: {}", LEAP_LOG_KEY, taxonomyId);
			leapDataContext.setTaxonomyJSON(taxonomyId);
			JSONObject afterApplyingTaxnomy = leapDataContext.applyTaxonomy(new JSONObject(reqBody), new JSONObject());
			leapDataContext.addContextElement(afterApplyingTaxnomy, ldcPushTag, ldcPushTag, taxonomyId);
		} catch (UnableToApplyTaxonomyException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Unable to applyTaxonomy:: {}", LEAP_LOG_KEY, e.getMessage());
			throw new UnableToApplyTaxonomyException("Unable to applyTaxonomy:: ",
					"Unable to applyTaxonomy:: " + e.getMessage(), 500);
		}
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * This method get request body from Exchange body and apply taxonomy. After
	 * that it will store applied taxonomy body into LDC by given push Tag.
	 * 
	 * @param taxonomyId
	 * @param ldcPushTag - it is the tag name which help us to push data into LDC
	 * @param exchange
	 * @throws UnableToApplyTaxonomyException
	 */

	public void applyTaxonomyToExchangeBodyAndPushToLDC(String taxonomyId, String ldcPushTag, Exchange exchange)
			throws UnableToApplyTaxonomyException {
		String methodName = "applyTaxonomyToExchangeBodyAndPushToLDC";
		logger.debug("{} entered into the method {}, ldcPushTag{}", LEAP_LOG_KEY, methodName, ldcPushTag);
		try {
			LeapDataContext leapDataContext = LeapConfigurationUtil.getLDCFromExchange(exchange);
			String reqBody = exchange.getIn().getBody(String.class);
			nullOrEmptyCheck(ldcPushTag, "ldc Push Tag");
			nullOrEmptyCheck(taxonomyId, "taxonomyId");
			leapDataContext.setTaxonomyJSON(taxonomyId);
			JSONObject afterApplyingTaxnomy = leapDataContext.applyTaxonomy(new JSONObject(reqBody), new JSONObject());
			leapDataContext.addContextElement(afterApplyingTaxnomy, ldcPushTag, ldcPushTag, taxonomyId);
		} catch (UnableToApplyTaxonomyException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Unable to applyTaxonomy:: {}", LEAP_LOG_KEY, e.getMessage());
			throw new UnableToApplyTaxonomyException("Unable to applyTaxonomy:: ",
					"Unable to applyTaxonomy:: " + e.getMessage(), 500);
		}
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
	}

	private void nullOrEmptyCheck(String string, String text) throws UnableToApplyTaxonomyException {
		if (string == null || string.isEmpty()) {
			logger.error("Unable to applyTaxonomy, :: " + text + " cannot be null or empty");
			throw new UnableToApplyTaxonomyException("Unable to applyTaxonomy",
					"Unable to applyTaxonomy, " + text + " cannot be null or empty ", 500);
		}
	}
}
