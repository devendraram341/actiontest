package com.attunedlabs.testsuites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.attunedlabs.embedded.EmbeddedActiveMQTest;
import com.attunedlabs.embedded.EmbeddedKafkaTest;
import com.attunedlabs.embedded.EmbeddedRabbitMQTest;

@RunWith(Suite.class)
@SuiteClasses({
	EmbeddedRabbitMQTest.class,
	EmbeddedActiveMQTest.class,
	EmbeddedKafkaTest.class
	
})
public class EmbeddedTestSuites {

}
