package com.attunedlabs.integrationfwk.groovyactivity.config;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.LEAP_DATA_CONTEXT;

import java.security.MessageDigest;

import org.apache.camel.Exchange;
import org.apache.commons.codec.digest.DigestUtils;

import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.util.LeapConfigUtil;
import com.attunedlabs.config.util.LeapDefaultConstants;
import com.attunedlabs.config.util.PropertiesConfigException;
import com.attunedlabs.integrationfwk.activities.bean.ActivityConstant;
import com.attunedlabs.integrationfwk.activities.bean.GroovyScriptActivityException;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.LeapServiceContext;
import com.attunedlabs.leap.LeapServiceRuntimeContext;

/**
 * @author Reactiveworks
 *
 */
public class GroovyScriptUtil {
	private static String path;
	private static final String STATIC_CONFIG_DIC_KEY = "staticConfigDirectory";

	static {
		try {
			path = LeapConfigUtil.getGlobalPropertyValue(STATIC_CONFIG_DIC_KEY,LeapDefaultConstants.DEFAULT_STATICCONFIG_DIC_KEY);
		} catch (PropertiesConfigException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param groovyScript
	 * @return
	 */
	public static String getCheckSumValue(String groovyScript) {
		MessageDigest md = DigestUtils.getMd5Digest();
		md.update((groovyScript).getBytes());
		byte[] byteData = md.digest();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < byteData.length; i++) {
			sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
		}
		return sb.toString();
	}

	/**
	 * @param exchange
	 * @return
	 */
	public static String getStaticConfigCompletePath(Exchange exchange) {
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext serviceDataContext = leapDataContext.getServiceDataContext();
		LeapServiceRuntimeContext currentLeapServiceRuntimeContext = serviceDataContext.getCurrentLeapServiceRuntimeContext();
		String constructedPath = getStaticConfigCompletePath(serviceDataContext.getTenant(), serviceDataContext.getSite(),
				serviceDataContext.getFeatureGroup(), serviceDataContext.getFeatureName(), currentLeapServiceRuntimeContext.getImplementationName(),
				currentLeapServiceRuntimeContext.getVendor(), currentLeapServiceRuntimeContext.getVersion());
		return constructedPath;
	}

	/**
	 * @param tenant
	 * @param site
	 * @param featureGroup
	 * @param featureName
	 * @param implementation
	 * @param vendor
	 * @param version
	 * @return
	 */
	private static String getStaticConfigCompletePath(String tenant, String site, String featureGroup,
			String featureName, String implementation, String vendor, String version) {
		String constructedPath = path + ActivityConstant.BACKWORD_SLASH + tenant + ActivityConstant.BACKWORD_SLASH
				+ site + ActivityConstant.BACKWORD_SLASH + featureGroup + ActivityConstant.BACKWORD_SLASH + featureName
				+ ActivityConstant.BACKWORD_SLASH + implementation + ActivityConstant.BACKWORD_SLASH + vendor
				+ ActivityConstant.BACKWORD_SLASH + version + ActivityConstant.BACKWORD_SLASH + ActivityConstant.GROOVY_FOLDER;
		return constructedPath;
	}

	/**
	 * @return
	 */
	public static String getStaticConfigPath() {
		return path;
	}

	/**
	 * @param configurationContext
	 * @return
	 * @throws GroovyScriptActivityException
	 */
	public static String getStaticConfigCompletePath(ConfigurationContext configurationContext)
			throws GroovyScriptActivityException {
		String constructedPath = getStaticConfigCompletePath(configurationContext.getTenantId(),
				configurationContext.getSiteId(), configurationContext.getFeatureGroup(),
				configurationContext.getFeatureName(), configurationContext.getImplementationName(),
				configurationContext.getVendorName(), configurationContext.getVersion());
		return constructedPath;
	}// ..end of the method

}
