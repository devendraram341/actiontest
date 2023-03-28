package com.attunedlabs.leap.testsuites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.attunedlabs.leap.eventsubscription.processor.InvokeCamelRouteProcessorTest;
import com.attunedlabs.leap.eventsubscription.processor.JMSSubscriberActionIndentificationProcessorTest;
import com.attunedlabs.leap.eventsubscription.processor.JMSSubscriberRoutingRuleCalculationProcessorTest;
import com.attunedlabs.leap.eventsubscription.processor.SubscriberActionIndentificationProcessorTest;
import com.attunedlabs.leap.eventsubscription.processor.SubscriberKafkaCommitProcessorTest;
import com.attunedlabs.leap.eventsubscription.processor.SubscriberRoutingRuleCalculationProcessorTest;

@RunWith(Suite.class)
@SuiteClasses({ 
		InvokeCamelRouteProcessorTest.class, 
		JMSSubscriberActionIndentificationProcessorTest.class,
		JMSSubscriberRoutingRuleCalculationProcessorTest.class,
		SubscriberActionIndentificationProcessorTest.class, 
		SubscriberKafkaCommitProcessorTest.class,
		SubscriberRoutingRuleCalculationProcessorTest.class

})
public class EventSubscriptionProcessorTestSuites {

}
