package com.attunedlabs.leap.testsuites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.attunedlabs.leap.eventsubscription.lifecylce.bean.JMSSubscriptionFailureHandlerBeanTest;
import com.attunedlabs.leap.eventsubscription.lifecylce.bean.JMSSubscriptionPerProcessHandlerBeanTest;
import com.attunedlabs.leap.eventsubscription.lifecylce.bean.JMSSubscriptionSuccessHandlerBeanTest;
import com.attunedlabs.leap.eventsubscription.lifecylce.bean.SubscriptionFailureHandlerBeanTest;
import com.attunedlabs.leap.eventsubscription.lifecylce.bean.SubscriptionPerProcessHandlerBeanTest;
import com.attunedlabs.leap.eventsubscription.lifecylce.bean.SubscriptionSuccessHandlerBeanTest;

@RunWith(Suite.class)
@SuiteClasses({ 
		JMSSubscriptionPerProcessHandlerBeanTest.class, 
		JMSSubscriptionFailureHandlerBeanTest.class,
		JMSSubscriptionSuccessHandlerBeanTest.class ,
		SubscriptionPerProcessHandlerBeanTest.class,
		SubscriptionFailureHandlerBeanTest.class,
		SubscriptionSuccessHandlerBeanTest.class
	})
public class EventSubscriptionLifecycleTestSuites {

}
