package com.attunedlabs.baseroute.util;

import static com.attunedlabs.LeapCoreTestConstant.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.test.spring.CamelSpringTestSupport;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class BaseRouteTestUtil extends CamelSpringTestSupport {

	@Override
	protected AbstractApplicationContext createApplicationContext() {
		System.setProperty(PROFILE_ID, LOCAL);
		String userDir = System.getProperty(USER);
		Path path = Paths.get(userDir).getParent().getParent();
		System.setProperty(BASE_CONFIG_PATH,
				path.toString() + File.separator + CONFIG + File.separator + System.getProperty(PROFILE_ID));
		AbstractApplicationContext abstractApplicationContext = null;
		abstractApplicationContext = new ClassPathXmlApplicationContext(BASEROUTE_XML_NAME);
		return abstractApplicationContext;
	}
	
	
	public AbstractApplicationContext applicationContext() {
		return createApplicationContext();
	}

	public Map<String, Object> setHeader() {
		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put(FEATUREGROUP, TEST_FEATUREGROUP);
		headers.put(FEATURE_GROUP, TEST_FEATUREGROUP);
		headers.put(FEATURE, TEST_FEATURE);
		headers.put(SERVICENAME, TEST_SERVICE);
		headers.put(ACCOUNTID, TEST_ACCOUNT);
		headers.put(SITEID, TEST_SITE);
		headers.put("prettyUri", PRETTY_URI_TEST);
		return headers;
	}
}
