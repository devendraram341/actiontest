package com.attunedlabs.osgi.activator;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.core.BundleContextSingleton;
import com.attunedlabs.leap.LeapHeaderConstant;
import com.attunedlabs.leap.TenantSitePropertiesLoader;
import com.attunedlabs.osgi.helper.OSGIEnvironmentHelper;


public class RoiActivator implements BundleActivator {
	final static Logger logger = LoggerFactory.getLogger(RoiActivator.class);

	@Override
	public void start(BundleContext context) throws Exception {
		String methodName = "start";
		logger.debug("{} entered into the method {}.{}()", LEAP_LOG_KEY, getClass().getName(),methodName);
		BundleContextSingleton.getBundleContextSingleton(context);
		OSGIEnvironmentHelper osgiEnvHelper=new OSGIEnvironmentHelper();
		osgiEnvHelper.setOSGIEnabled(context);
		logger.debug("{} isOSGIENabled : {} ",LEAP_LOG_KEY, osgiEnvHelper.isOSGIEnabled);
		logger.trace("{} bundle context got initialized : {}",LEAP_LOG_KEY,BundleContextSingleton.getBundleContext());
		TenantSitePropertiesLoader tenantSitePropLoader=new TenantSitePropertiesLoader();
		tenantSitePropLoader.setTenantAndSite(context);
		logger.trace("{} tenant : {}, Site : {} ",LEAP_LOG_KEY,LeapHeaderConstant.tenant,LeapHeaderConstant.site);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);

	}

	@Override
	public void stop(BundleContext context) throws Exception {
		logger.debug("{} .stop() of RoiActivator",LEAP_LOG_KEY);

	}

}
