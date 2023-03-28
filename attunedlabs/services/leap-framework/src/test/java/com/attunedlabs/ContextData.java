package com.attunedlabs;

import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.RequestContext;

public class ContextData {

	public static RequestContext getRequestContext() {
		RequestContext requestContext = new RequestContext(GenericTestConstant.TEST_TENANT,
				GenericTestConstant.TEST_SITE, GenericTestConstant.TEST_FEATUREGROUP, GenericTestConstant.TEST_FEATURE,
				GenericTestConstant.TEST_IMPL, GenericTestConstant.TEST_VENDOR, GenericTestConstant.TEST_VERSION);
		return requestContext;
	}

	public static ConfigurationContext getConfigContext() {
		ConfigurationContext configurationContext = new ConfigurationContext(GenericTestConstant.TEST_TENANT,
				GenericTestConstant.TEST_SITE, GenericTestConstant.TEST_FEATUREGROUP, GenericTestConstant.TEST_FEATURE,
				GenericTestConstant.TEST_IMPL, GenericTestConstant.TEST_VENDOR, GenericTestConstant.TEST_VERSION);
		return configurationContext;
	}

}
