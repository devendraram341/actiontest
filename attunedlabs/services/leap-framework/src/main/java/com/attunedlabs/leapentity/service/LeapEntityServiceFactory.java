package com.attunedlabs.leapentity.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import com.attunedlabs.leapentity.service.impl.LeapEntityServiceRestTypeImpl;
import com.attunedlabs.leapentity.service.impl.LeapEntityServiceSqlTypeImpl;

/**
 * To find the Impl Of EntityService based on the configuration
 * 
 * @author Reactiveworks42
 *
 */
public class LeapEntityServiceFactory {

	private static final Logger logger = LoggerFactory.getLogger(LeapEntityServiceFactory.class);

	private LeapEntityServiceFactory() {
		super();
	}

	/**
	 * to get the Instance of EntityServiceImpl
	 * 
	 * @param type
	 * @return
	 * @throws LeapEntityServiceException
	 */
	public static ILeapEntityService getInstance(String type) throws LeapEntityServiceException {
		String methodName = "getInstance";
		logger.debug("{} entered into the method {}, Type : {}", LEAP_LOG_KEY, methodName,type);
		ILeapEntityService entityService = null;
		if (type.equalsIgnoreCase("sql")) {
			entityService = new LeapEntityServiceSqlTypeImpl();
		} else if (type.equalsIgnoreCase("WS-Rest")) {
            entityService = new LeapEntityServiceRestTypeImpl();
		} else {
			throw new LeapEntityServiceException(new Throwable(), "Unable to Perform Entity ",
					"EntityAccessConfig Type :  " + type + " Done not exits", 404);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return entityService;
	}

}
