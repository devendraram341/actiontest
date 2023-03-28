package com.attunedlabs.leap;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.util.Properties;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.util.LeapConfigUtil;
import com.attunedlabs.config.util.LeapDefaultConstants;
import com.attunedlabs.config.util.PropertiesConfigException;

public class TenantSitePropertiesLoader {
	final static Logger logger = LoggerFactory.getLogger(TenantSitePropertiesLoader.class);

	public static final String TENANT_SITE_LIST_FILE = "globalAppDeploymentConfig.properties";

	public void setTenantAndSite(BundleContext context) throws TenantSitePropertiesLoadingException {
		try {
			String methodName = "setTenantAndSite";
			logger.debug("{} entered into the method {}, bundleContext={}", LEAP_LOG_KEY, methodName, context);
			Bundle bundle = context.getBundle();
			logger.debug("{} bundle id : {}, bundle name : {} ", LEAP_LOG_KEY, bundle.getBundleId(),
					bundle.getSymbolicName());
			LeapHeaderConstant.tenant = LeapConfigUtil.getGlobalPropertyValue(LeapHeaderConstant.TENANT_KEY,LeapDefaultConstants.DEFAULT_TENANT_KEY).trim();
			LeapHeaderConstant.site = LeapConfigUtil.getGlobalPropertyValue(LeapHeaderConstant.SITE_KEY,LeapDefaultConstants.DEFAULT_SITE_KEY).trim();
			logger.debug("{} tenant : {}, site: {} ", LEAP_LOG_KEY, LeapHeaderConstant.tenant, LeapHeaderConstant.site);
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		} catch (PropertiesConfigException e) {
			throw new TenantSitePropertiesLoadingException("unable to set tenant and site :", e);
		}

	}

	public void setTenantAndSite() throws TenantSitePropertiesLoadingException {
		try {
			String methodName = "setTenantAndSite";
			logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
			LeapHeaderConstant.tenant = LeapConfigUtil.getGlobalPropertyValue(LeapHeaderConstant.TENANT_KEY,LeapDefaultConstants.DEFAULT_TENANT_KEY).trim();
			LeapHeaderConstant.site = LeapConfigUtil.getGlobalPropertyValue(LeapHeaderConstant.SITE_KEY,LeapDefaultConstants.DEFAULT_TENANT_KEY).trim();
			logger.debug("{} tenant : {}, site: {} ", LEAP_LOG_KEY, LeapHeaderConstant.tenant, LeapHeaderConstant.site);
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		} catch (PropertiesConfigException e) {
			throw new TenantSitePropertiesLoadingException("unable to set tenant and site :", e);
		}

	}

}
