package com.attunedlabs.policy.config.impl;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mvel2.MVEL;
import org.mvel2.PropertyAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.RequestContext;
import com.attunedlabs.permastore.config.IPermaStoreConfigurationService;
import com.attunedlabs.permastore.config.PermaStoreConfigRequestException;
import com.attunedlabs.permastore.config.impl.PermaStoreConfigurationService;
import com.attunedlabs.policy.config.PolicyConfigurationException;
import com.attunedlabs.policy.config.PolicyConfigurationUnit;
import com.attunedlabs.policy.config.PolicyEvaluationConfigurationUnit;
import com.attunedlabs.policy.config.PolicyRequestContext;
import com.attunedlabs.policy.config.PolicyRequestException;
import com.attunedlabs.policy.jaxb.PolicyResponseType;

/**
 * This class is reponsible for evaluating a policy and sending the response
 * back based on the Policy Response Configuration
 * 
 * @author bizruntime
 */
public class PolicyEvaluationRequestHandler {
	final Logger logger = LoggerFactory.getLogger(PolicyEvaluationRequestHandler.class);

	/**
	 * Evaluates the policy
	 * 
	 * @param polConfigUnit
	 * @return boolean True-if policy evaluation Expression returns true.
	 * @throws PolicyInvalidRegexExpception
	 * @throws PolicyConfigurationException
	 */
	public boolean evaluatePolicy(PolicyConfigurationUnit polConfigUnit, PolicyRequestContext policyRequestContext)
			throws PolicyRequestException, PolicyInvalidRegexExpception {
		String methodName = "evaluatePolicy";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		String policyName = polConfigUnit.getKey();
		Map inputVars = policyRequestContext.getRequestVariable();
		logger.debug("{} PolicyName= {} ", LEAP_LOG_KEY, policyName);
		List<PolicyEvaluationConfigurationUnit> evaluationExpList = polConfigUnit.getEvaluationUnitList();
		for (PolicyEvaluationConfigurationUnit polevalUnit : evaluationExpList) {
			String exp = polevalUnit.getExpression();

			if (polevalUnit.getDialect().equalsIgnoreCase("SQL")) {
				addPermaStoreVariableAsInput(policyRequestContext, polevalUnit);
				boolean istrue = evaluateMvelExpression(exp, policyRequestContext.getRequestVariable());
				if (istrue) {
					logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
					return true;
				}
			} else if (polevalUnit.getDialect().equalsIgnoreCase("REGEX")) {
				addPermaStoreVariableAsInput(policyRequestContext, polevalUnit);

				boolean istrue = evaluateRegexExpression(exp, policyRequestContext.getRequestVariable(), polevalUnit);
				if (istrue) {
					logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
					return true;
				}
			}

		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return false;
	}

	public Object getPolicyResponseData(PolicyConfigurationUnit polConfigUnit, PolicyRequestContext polReq)
			throws PolicyRequestException {
		String methodName = "getPolicyResponseData";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		String responseType = polConfigUnit.getResponseType();
		Object policyResponse = null;
		if (PolicyResponseType.POLICY_DEFINED_FACT.value().equalsIgnoreCase(responseType)) {

			policyResponse = polConfigUnit.getConfigData();

		} else if (PolicyResponseType.MAPPED_FACT.value().equalsIgnoreCase(responseType)) {

			String permaStoreVar = (String) polConfigUnit.getConfigData();
			logger.debug("{} permastore response type by mapped fact : {} ", LEAP_LOG_KEY, permaStoreVar);
			try {
				// added vendor and version support to get permastore
				policyResponse = getPermaStoreObject(polReq.getTenantId(), polReq.getSiteId(), polReq.getFeatureGroup(),
						polReq.getFeatureName(), polReq.getImplementationName(), polReq.getVendor(),
						polReq.getVersion(), permaStoreVar);
			} catch (PermaStoreConfigRequestException e) {
				throw new PolicyRequestException("PermaStore variable {" + permaStoreVar + "} referd by policy {"
						+ polConfigUnit.getKey() + "} not found");
			}
		} else {
			throw new PolicyRequestException("Invalid Policy State. Policy Response type not supported");
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return policyResponse;
	}

	private void addPermaStoreVariableAsInput(PolicyRequestContext polReq,
			PolicyEvaluationConfigurationUnit polevalUnit) throws PolicyRequestException {
		List<String> psVarList = polevalUnit.getPsVarList();
		if (psVarList != null) {
			for (String psVar : psVarList) {
				logger.trace("{} addPermaStoreVariableAsInput()--PermaStoreVariable is {}", LEAP_LOG_KEY, psVar);
				Serializable psObj;
				try {
					// added vendor and version support to get permastore
					psObj = getPermaStoreObject(polReq.getTenantId(), polReq.getSiteId(), polReq.getFeatureGroup(),
							polReq.getFeatureName(), polReq.getImplementationName(), polReq.getVendor(),
							polReq.getVersion(), psVar);

					logger.trace("{} added Request Varible of permastore : {} ", LEAP_LOG_KEY, psObj);
					polReq.addRequestVariable("$" + psVar, psObj);
					if (psObj == null)
						throw new PolicyRequestException("Variable from PermaStore with name=" + psVar + " is null");
				} catch (PermaStoreConfigRequestException e) {
					// failed to get data from PermaStore.Throw exception
					throw new PolicyRequestException("Failed to get variable from PermaStore with name=" + psVar, e);
				}

			}
		}
	}

	private Serializable getPermaStoreObject(String tenant, String site, String featureGrp, String feature,
			String implementationName, String vendor, String version, String varName)
			throws PermaStoreConfigRequestException {
		IPermaStoreConfigurationService psConfigService = new PermaStoreConfigurationService();
		RequestContext requestContext = new RequestContext(tenant, site, featureGrp, feature, implementationName,
				vendor, version);
		Serializable cachedPS = psConfigService.getPermaStoreCachedObject(requestContext, varName);
		return cachedPS;
	}

	private boolean evaluateMvelExpression(String mvelExp, Map<String, Object> requestFactMap) {
		try {
			logger.trace("{} evaluateMvelExpression(), MVELeXP={}, reqMap={}", LEAP_LOG_KEY, mvelExp, requestFactMap);
			// Object mvelResult = MVEL.evalToBoolean (mvelExp ,
			// requestFactMap);
			Object mvelResult = MVEL.eval((String) mvelExp, requestFactMap);
			logger.debug("{} evaluateMvelExpression()...Result= {} ", LEAP_LOG_KEY, mvelResult);
			if (mvelResult instanceof Boolean) {
				return ((Boolean) mvelResult).booleanValue();
			}
		} catch (PropertyAccessException exp) {
			// Its normal for mvel evaluation to fail.. Log it and eating is ok.
			logger.error("{} MVEL Evaluation failed with error", LEAP_LOG_KEY, exp);
		}
		return false;
	}

	private boolean evaluateRegexExpression(String regexExp, Map<String, Object> requestFactMap,
			PolicyEvaluationConfigurationUnit polevalUnit) throws PolicyInvalidRegexExpception {

		return splitRegexByANDandORCondtion(regexExp, requestFactMap, polevalUnit);
	}

	/**
	 * to split regexExpressions by space find out regex , requestVarible ,psVarible
	 * and check for Opartor if exist evalaute Right and Expression check lefthand
	 * and Righthand value is true false
	 * 
	 * 
	 * @param regularExpression
	 * @param requestFactMap
	 * @return boolen value if EXpressions evaluted to true or false
	 * @throws PolicyInvalidRegexExpception
	 */
	private boolean splitRegexByANDandORCondtion(String regularExpression, Map<String, Object> requestFactMap,
			PolicyEvaluationConfigurationUnit polevalUnit) throws PolicyInvalidRegexExpception {
		String methodName = "splitRegexByANDandORCondtion";
		logger.debug("{} entered into the method {}, with given RegexExp ={}", LEAP_LOG_KEY, methodName,
				regularExpression);
		boolean oldBooleanValue = false;
		// replicing regex$values with Requested Input values
		regularExpression = replaceRegexExpressionValues(regularExpression, polevalUnit, requestFactMap);
		String requestedValue = "";
		if (regularExpression != null) {

			// Split whole RegexEXpressions into words by space to get
			// Conditional oparetor and Evalute Regex
			String arrayOfSplitedvalue[] = regularExpression.split("\\s+");
			if (arrayOfSplitedvalue != null) {
				for (int i = 0; i < arrayOfSplitedvalue.length; i++) {
					if (!arrayOfSplitedvalue[i].isEmpty()) {
						try {

							// checking Increamented I lessthan splitedArray
							// Length

							if (arrayOfSplitedvalue[i].trim().equalsIgnoreCase("&&")) {
								requestedValue = checkForEmptyString(arrayOfSplitedvalue, i + 3);
								// checking AND(&&) Righthand side expression is
								// true or not
								boolean isMatched = evaluateRegex(arrayOfSplitedvalue[i + 1], requestedValue);
								logger.trace("{} isMatched : {}, oldBooleanValue {}", LEAP_LOG_KEY, isMatched,
										oldBooleanValue);
								// Evaluting right handside and left handside
								// boolean value
								oldBooleanValue = isMatched && oldBooleanValue;

								logger.trace("{} in ANd condtion oldBooleanValue {}", LEAP_LOG_KEY, oldBooleanValue);

								// increment i to get Next RegexExpression
								if (requestedValue.isEmpty()) {
									i = i + 2;
								} else {
									i = i + 3;
								}
							} else if (arrayOfSplitedvalue[i].equalsIgnoreCase("||")) {
								// check wether given Input is Empty or not
								requestedValue = checkForEmptyString(arrayOfSplitedvalue, i + 3);
								// checking OR(||) Righthand side expression is
								// true or not
								boolean isMatched = evaluateRegex(arrayOfSplitedvalue[i + 1], requestedValue);
								logger.trace("{} isMatched : {}, oldBooleanValue : {} ", LEAP_LOG_KEY, isMatched,
										oldBooleanValue);

								// Evaluting right handside and left handside
								// boolean value
								oldBooleanValue = isMatched || oldBooleanValue;
								logger.trace("{} in OR condtion oldBooleanValue :{}", LEAP_LOG_KEY, oldBooleanValue);
								// increment i to get Next RegexExpression
								if (requestedValue.isEmpty()) {
									i = i + 2;
								} else {
									i = i + 3;
								}

							} else {
								requestedValue = checkForEmptyString(arrayOfSplitedvalue, i + 2);
								// Evaluting First Regex Expression or Left side
								// Expression is true or not
								oldBooleanValue = evaluateRegex(arrayOfSplitedvalue[i], requestedValue);
								logger.trace("{} single EXpression oldBooleanValue  {} ", LEAP_LOG_KEY,
										oldBooleanValue);

								// if given Input is Empty increment I By 2 else
								// 3 to get Next Expression
								if (requestedValue.isEmpty()) {
									i = i + 1;
								} else {
									i = i + 2;
								}
							}

						} catch (Exception e) {
							throw new PolicyInvalidRegexExpception("Given Invalid Regex " + e);
						}
					}
				}

			}

		}
		logger.trace("{} final oldBooleanValue {}", LEAP_LOG_KEY, oldBooleanValue);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);

		return oldBooleanValue;
	}

	/**
	 * check regex method to check ture or false based on give value and regex
	 * 
	 * @param regularExpression
	 * @param request           Value
	 * @return boolean value
	 * @throws PolicyInvalidRegexExpception
	 */
	private boolean evaluateRegex(String regularExpression, String rquestedValue) throws PolicyInvalidRegexExpception {

		logger.debug("{} regularExpression :{} and requestedValue: {} ", LEAP_LOG_KEY, regularExpression,
				rquestedValue);
		// if requesstVariable only in Expression

		boolean isMatched = false;
		if (regularExpression != null && rquestedValue != null) {

			try {
				isMatched = Pattern.matches(regularExpression.trim(), rquestedValue);
				// if both requestVariable and psVariable Exist in Expression

				if (!isMatched) {
					Pattern pattern = Pattern.compile(regularExpression);
					Matcher matcher = pattern.matcher(rquestedValue);
					return matcher.find();
				}
			} catch (Exception e) {
				throw new PolicyInvalidRegexExpception("Invalid RegexXpression  ", e);
			}
		}

		return isMatched;
	}

	/**
	 * Replacing Expression request and ps pS value by given Requested Value
	 * 
	 * @param regularExpression
	 * @param polevalUnit
	 * @param requestFactMap
	 * @return Regex Expression with repalced Input values
	 */
	private String replaceRegexExpressionValues(String regularExpression, PolicyEvaluationConfigurationUnit polevalUnit,
			Map<String, Object> requestFactMap) {

		String methodName = "replaceRegexExpressionValues";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		String requestVarible = "";
		String requestedReplaceValueFromMAp = "";
		try {
			// getting RequestVariable from Requseted map ,requsetVariable
			// always start with $
			if (polevalUnit.getReqVarList() != null) {

				for (String psVarName : polevalUnit.getReqVarList()) {
					requestVarible = "$" + psVarName;
					logger.trace("{} request value :{} ", LEAP_LOG_KEY, requestVarible);
					requestedReplaceValueFromMAp = (String) requestFactMap.get(requestVarible.trim());
					logger.debug("{} requestedReplaceValueFromMAp value :{} ", LEAP_LOG_KEY,
							requestedReplaceValueFromMAp);

					try {
						regularExpression = regularExpression.replace(requestVarible, requestedReplaceValueFromMAp);
					} catch (NullPointerException e) {
						logger.error("{} rquest  value not Exsistin Regex Expression ", LEAP_LOG_KEY);
					}
				}

				logger.trace("{} finel REgex after Repalcing request value :{} ", LEAP_LOG_KEY, regularExpression);
			}
		} catch (NullPointerException e) {
			logger.error("{} error in getting request variable from PolicyEvaluationConfigurationUnit : {}",
					LEAP_LOG_KEY, e);
		}

		String psValueFromMAp = "";
		String psValue = "";
		try {
			if (polevalUnit.getPsVarList() != null) {

				for (String psVarName : polevalUnit.getPsVarList()) {
					psValue = "$" + psVarName;
					logger.trace("{} psValue {}", LEAP_LOG_KEY, psValue);
					psValueFromMAp = (String) requestFactMap.get(psValue);

					String psValureArray[] = psValueFromMAp.split(" ");
					psValueFromMAp = "";
					for (int i = 0; i < psValureArray.length; i++) {

						psValueFromMAp = psValueFromMAp + psValureArray[i];
					}
					if (psValueFromMAp.isEmpty()) {
						psValueFromMAp = (String) requestFactMap.get(psValue);
					}
					logger.trace("{} psValueFromMAp value {}", LEAP_LOG_KEY, psValueFromMAp);
					try {
						regularExpression = regularExpression.replace(psValue, psValueFromMAp.trim());
					} catch (NullPointerException e) {
						logger.error("{} rquest  value not Exsistin Regex Expression ", LEAP_LOG_KEY);
					}
				}
			}
			logger.trace("{} finel REgex after Repalcing ps value  {} ", LEAP_LOG_KEY, regularExpression);

		} catch (NullPointerException e) {
			logger.error("{} error in getting ps variable from Requested map {} ", LEAP_LOG_KEY, e);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return regularExpression;
	}

	/**
	 * to check wether expression Request value is empty is or not
	 * 
	 * @param requestedValue
	 * @param i
	 * @return request Value
	 */
	private String checkForEmptyString(String[] requestedValue, int i) {
		String emptyValue = "";

		if (i < requestedValue.length) {
			if (requestedValue[i] != null && requestedValue[i].equalsIgnoreCase("&&")) {
				emptyValue = "";
			} else if (requestedValue[i] != null && requestedValue[i].equalsIgnoreCase("||")) {
				emptyValue = "";
			} else {
				emptyValue = requestedValue[i];
			}
		}
		return emptyValue;
	}

	public static void main(String[] args) throws PolicyInvalidRegexExpception {
		String regex = " ^\\s*$  matches  $dstare && ^\\s*$  matches  $dstare || ^\\s*$  matches  $dstare   &&  ([1-2][0-9])+  matches   $platform || $dstare$dgood  matches $GetStagingAreasOne";
		// String regex="$dstare$dgood matches $GetStagingAreasOne";
		Map<String, Object> val = new HashMap<String, Object>();

		val.put("$dstare", "ST");
		val.put("$dgood", "A1");
		val.put("$dgood", "A1");

		val.put("$GetStagingAreasOne", "STA1, STA2, STA3, STA4, STA5");

		PolicyEvaluationConfigurationUnit polevalUnit = new PolicyEvaluationConfigurationUnit();
		polevalUnit.addReqVar("dstare");
		polevalUnit.addReqVar("dgood");
		polevalUnit.addReqVar("platform");
		polevalUnit.addPSVar("GetStagingAreasOne");

		PolicyRequestContext policyRequestContext = new PolicyRequestContext("", "", "");
		policyRequestContext.addRequestVariable("$dstare", "");
		policyRequestContext.addRequestVariable("$dgood", "hello");
		policyRequestContext.addRequestVariable("$platform", "29");
		policyRequestContext.addRequestVariable("$GetStagingAreasOne", "hello welcome ");

		System.out.println("polevalUnit " + polevalUnit);
		System.out.println(new PolicyEvaluationRequestHandler().evaluateRegexExpression(regex,
				policyRequestContext.getRequestVariable(), polevalUnit));

	}
}
