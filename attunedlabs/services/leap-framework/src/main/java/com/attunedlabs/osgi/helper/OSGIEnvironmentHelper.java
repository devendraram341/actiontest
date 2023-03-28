package com.attunedlabs.osgi.helper;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.util.LeapConfigUtil;
import com.attunedlabs.config.util.LeapDefaultConstants;
import com.attunedlabs.config.util.PropertiesConfigException;




public class OSGIEnvironmentHelper {
	final static Logger logger = LoggerFactory.getLogger(OSGIEnvironmentHelper.class);
	
	public static final String OSGIENV_FILE="globalAppDeploymentConfig.properties";
	public static final String OSGIENV_ISENABLE_KEY="isOSGIEnabled";

	public static boolean isOSGIEnabled=false;

	public void setOSGIEnabled(BundleContext context) throws UnableToLoadOSGIPropertiesException {
		try {
			String methodName = "setOSGIEnabled";
			logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
			Bundle bundle=context.getBundle();
			logger.debug("{} bundle id : {},  bundle name : {}",LEAP_LOG_KEY,bundle.getBundleId(),bundle.getSymbolicName());
			String isOSGIEnabledAsString=LeapConfigUtil.getGlobalPropertyValue(OSGIENV_ISENABLE_KEY,LeapDefaultConstants.DEFAULT_OSGIENV_ISENABLE_KEY);
			if(isOSGIEnabledAsString.equalsIgnoreCase("true"))
				isOSGIEnabled=true;
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		} catch (PropertiesConfigException e) {
			throw new UnableToLoadOSGIPropertiesException("unable to load property  = "+OSGIENV_ISENABLE_KEY, e);

		}

	}
	

	
}
