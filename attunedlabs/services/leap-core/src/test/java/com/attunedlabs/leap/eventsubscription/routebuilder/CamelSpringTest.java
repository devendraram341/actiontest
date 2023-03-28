package com.attunedlabs.leap.eventsubscription.routebuilder;

import org.apache.camel.test.spring.CamelSpringTestSupport;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

public class CamelSpringTest extends CamelSpringTestSupport {

	@Override
	protected AbstractApplicationContext createApplicationContext() {
		return new AnnotationConfigApplicationContext(ConnectionFactoryConfiguration.class);
	}
}
