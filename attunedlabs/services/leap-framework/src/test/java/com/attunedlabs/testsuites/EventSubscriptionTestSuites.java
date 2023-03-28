package com.attunedlabs.testsuites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.attunedlabs.eventsubscription.retrypolicy.SubscriptionNoRetryPolicyTest;
import com.attunedlabs.eventsubscription.retrypolicy.SubscriptionRetryPolicyTest;
import com.attunedlabs.eventsubscription.util.SubscriptionUtilTest;
import com.attunedlabs.eventsubscriptiontracker.EventSubscriptionTrackerImplTest;
import com.attunedlabs.eventsubscriptiontracker.JMSEventSubscriptionTrackerImplTest;

@RunWith(Suite.class)
@SuiteClasses({
	SubscriptionNoRetryPolicyTest.class, 
	SubscriptionRetryPolicyTest.class, 
	SubscriptionUtilTest.class,
	EventSubscriptionTrackerImplTest.class, 
	JMSEventSubscriptionTrackerImplTest.class
	})
public class EventSubscriptionTestSuites {
	
}
