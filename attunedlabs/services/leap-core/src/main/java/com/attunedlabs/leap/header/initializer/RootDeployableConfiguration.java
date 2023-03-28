package com.attunedlabs.leap.header.initializer;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.util.LeapConfigUtil;
import com.attunedlabs.config.util.LeapDefaultConstants;
import com.attunedlabs.config.util.PropertiesConfigException;

public class RootDeployableConfiguration {
	protected static final Logger logger = LoggerFactory.getLogger(RootDeployableConfiguration.class);
	private static final String ROOT_DEPLOYABLE_FEATURES_KEY = "rootDeployableFeatures";
	private static final String FEATURE_LIST_SEPERATOR = ",";
	private static final String FEATURE_SEPERATOR = "-";

	public static boolean isRootDeployableFeature(String featureGroup, String featureName) {
		String methodName = "isRootDeployableFeature";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		try {
			String allFeatures =LeapConfigUtil.getGlobalPropertyValue(ROOT_DEPLOYABLE_FEATURES_KEY,LeapDefaultConstants.DEFAULT_ROOT_DEPLOYABLE_FEATURES_KEY);
			if (allFeatures != null) {
				boolean isRoot = false;
				String[] featureArray = allFeatures.trim().split(FEATURE_LIST_SEPERATOR);
				for (int i = 0; i < featureArray.length; i++) {
					String featureCombo = featureArray[i];
					String[] featureComboArray = featureCombo.trim().split(FEATURE_SEPERATOR);
					if (featureComboArray.length == 2 && !isRoot)
						isRoot = featureComboArray[0].trim().equalsIgnoreCase(featureGroup)
								&& featureComboArray[1].trim().equalsIgnoreCase(featureName);
				}
				return isRoot;
			}
		} catch (NullPointerException | PropertiesConfigException e) {
			logger.warn("{} failed to identify root deployable feature...", LEAP_LOG_KEY, e.getMessage());
			e.printStackTrace();
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return false;
	}

}
