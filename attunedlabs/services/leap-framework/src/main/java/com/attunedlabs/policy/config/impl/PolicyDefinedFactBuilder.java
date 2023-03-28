package com.attunedlabs.policy.config.impl;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.policy.jaxb.FactAttribute;
import com.attunedlabs.policy.jaxb.FactDescription;
import com.attunedlabs.policy.jaxb.PolicyDefinedFact;

/**
 * Helper class for building the PolicyDefinedFacts from the Policy
 * Configuration
 * 
 * @author bizruntime
 *
 */
public class PolicyDefinedFactBuilder {

	final Logger logger = LoggerFactory.getLogger(PolicyDefinedFactBuilder.class);

	// constants required for the class
	private static final String STRING_TYPE = "String";
	private static final String LIST_TYPE = "List";
	private static final String INTEGER_TYPE = "Integer";
	private static final String LONG_TYPE = "Long";
	private static final String DOUBLE_TYPE = "Double";
	private static final String FLOAT_TYPE = "Float";
	private static final String MAP_TYPE = "Map";
	private static final String BOOLEAN_TYPE = "Boolean";
	private static final String DATE_TYPE = "Date";

	/**
	 * This method is to build the object for policy defined fact
	 * 
	 * @param polDefinedFact : PolicyDefinedFact object
	 * @return Serializable
	 * @throws PolicyFactBuilderException
	 */
	public Serializable buildPolicyDefinedFact(PolicyDefinedFact polDefinedFact) throws PolicyFactBuilderException {
		String methodName = "buildPolicyDefinedFact";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		FactDescription factDesc = polDefinedFact.getFactDescription();
		List<FactAttribute> factAttributeList = polDefinedFact.getFactDescription().getFactAttribute();

		String factTyp = factDesc.getType();
		logger.debug("{} policy defined fact type : {} ", LEAP_LOG_KEY, factTyp);

		switch (factTyp) {
		case PolicyDefinedFactBuilder.MAP_TYPE: {
			logger.trace("{} factTyp is of Map Type", LEAP_LOG_KEY);
			HashMap factDiscriptionMap = new HashMap();
			storeFactAttributeDataInMap(factDiscriptionMap, factAttributeList);
			logger.trace("{} fact attribute data : {}", LEAP_LOG_KEY, factDiscriptionMap);
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return factDiscriptionMap;
		}

		case PolicyDefinedFactBuilder.LIST_TYPE: {
			logger.trace("{} factTyp is of List Type", LEAP_LOG_KEY);
			ArrayList factDiscriptionList = new ArrayList();
			storeFactAttributeDataInList(factDiscriptionList, factAttributeList);
			logger.trace("{} Fact attribute data : {} ", LEAP_LOG_KEY, factDiscriptionList);
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return factDiscriptionList;
		}

		default: {
			logger.error("{} Exception while building the PolicyDefinedFact due to unsupported FactType", LEAP_LOG_KEY);
			throw new PolicyFactBuilderException("FactType for PolicyDefinedFact is Unsupported for : " + factTyp);
		}
		}// end of switch case
	}// end of method

	/**
	 * This method is used to store fact attributes in HashMap
	 * 
	 * @param factDiscriptionMap : HashMap object to store fact attribute
	 * @param factAttributeList  : List of all the Fact Attribute
	 * @throws PolicyFactBuilderException
	 */
	private void storeFactAttributeDataInMap(HashMap factDiscriptionMap, List<FactAttribute> factAttributeList)
			throws PolicyFactBuilderException {
		String methodName = "storeFactAttributeDataInMap";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		for (FactAttribute factAttribute : factAttributeList) {

			String factAttributeName = factAttribute.getName();
			String factAttributeType = factAttribute.getType();

			if (factAttributeType != null) {
				switch (factAttributeType) {
				case PolicyDefinedFactBuilder.STRING_TYPE: {
					logger.debug("{} fact attribute type is String", LEAP_LOG_KEY);
					factDiscriptionMap.put(factAttributeName, factAttribute.getValue());
					break;

				}
				case PolicyDefinedFactBuilder.LIST_TYPE: {
					logger.debug("{} fact attribute type is List", LEAP_LOG_KEY);
					List<String> validoperationList = new ArrayList();
					String factAttributeData = factAttribute.getValue();
					String factAttributeDataArray[] = factAttributeData.split(",");
					for (String facts : factAttributeDataArray) {
						validoperationList.add(facts.trim());
					}

					factDiscriptionMap.put(factAttributeName, validoperationList);
					break;
				}

				case PolicyDefinedFactBuilder.INTEGER_TYPE: {
					logger.debug("{} fact attribute type is Integer", LEAP_LOG_KEY);
					int intvalue = Integer.parseInt(factAttribute.getValue());
					factDiscriptionMap.put(factAttributeName, intvalue);
					break;
				}

				case PolicyDefinedFactBuilder.LONG_TYPE: {
					logger.debug("{} fact attribute type is Long", LEAP_LOG_KEY);
					long longvalue = Long.parseLong(factAttribute.getValue());
					factDiscriptionMap.put(factAttributeName, longvalue);
					break;
				}

				case PolicyDefinedFactBuilder.BOOLEAN_TYPE: {
					logger.debug("{} fact attribute type is Boolean", LEAP_LOG_KEY);
					boolean boolvalue = Boolean.parseBoolean(factAttribute.getValue());
					factDiscriptionMap.put(factAttributeName, boolvalue);
					break;
				}
				case PolicyDefinedFactBuilder.DOUBLE_TYPE: {
					logger.debug("{} fact attribute type is Double", LEAP_LOG_KEY);
					double doblevalue = Double.parseDouble(factAttribute.getValue());
					factDiscriptionMap.put(factAttributeName, doblevalue);
					break;
				}
				case PolicyDefinedFactBuilder.FLOAT_TYPE: {
					logger.debug("{} fact attribute type is float", LEAP_LOG_KEY);
					float floatvalue = Float.parseFloat(factAttribute.getValue());
					factDiscriptionMap.put(factAttributeName, floatvalue);
					break;
				}

				case PolicyDefinedFactBuilder.DATE_TYPE: {
					logger.debug("{} factTyp is of Date Type", LEAP_LOG_KEY);
					DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
					Date date = null;
					try {
						date = dateFormat.parse(factAttribute.getValue());
					} catch (ParseException e) {
						throw new PolicyFactBuilderException("Unable to parse the date format= "
								+ factAttribute.getValue() + " Expecting MM/dd/yyyy format");
					}
					logger.trace("{} Date : {} ", LEAP_LOG_KEY, date);
					factDiscriptionMap.put(factAttributeName, dateFormat.format(date));
					break;
				}
				default:
					throw new PolicyFactBuilderException("Fact Attribute type is Undefined for : " + factAttributeType);
				}// end of switch
			} else {
				logger.debug("{} fact attribute type is restrictive", LEAP_LOG_KEY);

				factDiscriptionMap.put(factAttributeName, factAttribute.getValue());
			}

		} // end of for(FactAttribute FactAttribute:factAttributeList)
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}// end of method

	/**
	 * This method is used to store Fact Attribute in List object
	 * 
	 * @param factDiscriptionList : ArrayList object to store fact attribute data
	 * @param factAttributeList   : List of Fact Attribute Object
	 * @throws PolicyFactBuilderException
	 */
	private void storeFactAttributeDataInList(ArrayList factDiscriptionList, List<FactAttribute> factAttributeList)
			throws PolicyFactBuilderException {
		String methodName = "storeFactAttributeDataInList";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);

		for (FactAttribute factAttribute : factAttributeList) {

			String factAttributeName = factAttribute.getName();
			String factAttributeType = factAttribute.getType();

			if (factAttributeType != null) {

				switch (factAttributeType) {

				case PolicyDefinedFactBuilder.STRING_TYPE: {
					logger.debug("{} fact attribute type is String", LEAP_LOG_KEY);
					factDiscriptionList.add(factAttribute.getValue());
					break;

				}
				case PolicyDefinedFactBuilder.LIST_TYPE: {
					logger.debug("{} fact attribute type is List", LEAP_LOG_KEY);
					List<String> validoperationList = new ArrayList();
					String factAttributeData = factAttribute.getValue();
					String factAttributeDataArray[] = factAttributeData.split(",");
					for (String facts : factAttributeDataArray) {
						validoperationList.add(facts.trim());
					}

					factDiscriptionList.add(validoperationList);
					break;
				}

				case PolicyDefinedFactBuilder.INTEGER_TYPE: {
					logger.debug("{} fact attribute type is Integer", LEAP_LOG_KEY);
					int intvalue = Integer.parseInt(factAttribute.getValue());
					factDiscriptionList.add(intvalue);
					break;
				}

				case PolicyDefinedFactBuilder.LONG_TYPE: {
					logger.debug("{} fact attribute type is Long", LEAP_LOG_KEY);
					long longvalue = Long.parseLong(factAttribute.getValue());
					factDiscriptionList.add(longvalue);
					break;
				}

				case PolicyDefinedFactBuilder.BOOLEAN_TYPE: {
					logger.debug("{} fact attribute type is Boolean", LEAP_LOG_KEY);
					boolean boolvalue = Boolean.parseBoolean(factAttribute.getValue());
					factDiscriptionList.add(boolvalue);
					break;
				}
				case PolicyDefinedFactBuilder.DOUBLE_TYPE: {
					logger.debug("{} fact attribute type is Double", LEAP_LOG_KEY);
					double doblevalue = Double.parseDouble(factAttribute.getValue());
					factDiscriptionList.add(doblevalue);
					break;
				}
				case PolicyDefinedFactBuilder.FLOAT_TYPE: {
					logger.debug("{} fact attribute type is float", LEAP_LOG_KEY);
					float floatvalue = Float.parseFloat(factAttribute.getValue());
					factDiscriptionList.add(floatvalue);
					break;
				}
				case PolicyDefinedFactBuilder.DATE_TYPE: {
					logger.debug("{} factTyp is of Date Type", LEAP_LOG_KEY);
					DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
					Date date = null;
					try {
						date = dateFormat.parse(factAttribute.getValue());
					} catch (ParseException e) {
						throw new PolicyFactBuilderException("Unable to parse the date format= "
								+ factAttribute.getValue() + " Expecting MM/dd/yyyy format");
					}
					logger.trace("{} Date : {} ", LEAP_LOG_KEY, date);
					factDiscriptionList.add(dateFormat.format(date));
					break;
				}
				default:
					throw new PolicyFactBuilderException("Fact Attribute type is Undefined for : " + factAttributeType);
				}// end of switch

			} else {
				logger.debug("fact attribute type is restrictive");

				factDiscriptionList.add(factAttribute.getValue());
			}

		} // end of for(FactAttribute FactAttribute:factAttributeList)

		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}// end of method

}// end of class
