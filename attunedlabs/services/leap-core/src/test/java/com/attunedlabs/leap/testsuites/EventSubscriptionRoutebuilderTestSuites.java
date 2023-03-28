package com.attunedlabs.leap.testsuites;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.attunedlabs.LeapCoreTestConstant;
import com.attunedlabs.configdbtest.DeleteConfigNodeForTesting;
import com.attunedlabs.configdbtest.InsertConfigNodeForTesting;
import com.attunedlabs.leap.eventsubscription.routebuilder.GenericJMSRetryRouteBuilderTest;
import com.attunedlabs.leap.eventsubscription.routebuilder.GenericRetryRouteBuilderTest;
import com.attunedlabs.leap.eventsubscription.routebuilder.JMSMessageProcessingWayTest;
import com.attunedlabs.leap.eventsubscription.routebuilder.MessageProcessingWayDeciderTest;
import com.attunedlabs.leap.eventsubscription.routebuilder.SubscriberEvaluationRouteBuilderTest;
import com.attunedlabs.leap.eventsubscription.routebuilder.SubscriberRouteBuilderTest;
@RunWith(Suite.class)
@SuiteClasses({
	GenericJMSRetryRouteBuilderTest.class,
	GenericRetryRouteBuilderTest.class,
	JMSMessageProcessingWayTest.class,
	MessageProcessingWayDeciderTest.class,
	SubscriberEvaluationRouteBuilderTest.class, 
	SubscriberRouteBuilderTest.class
	
})
public class EventSubscriptionRoutebuilderTestSuites {
	@BeforeClass
	public static void init() {
		System.setProperty(LeapCoreTestConstant.PROFILE_ID, LeapCoreTestConstant.LOCAL);
		new InsertConfigNodeForTesting();
	}

	@AfterClass
	public static void clean() {
		new DeleteConfigNodeForTesting();
	}
}
