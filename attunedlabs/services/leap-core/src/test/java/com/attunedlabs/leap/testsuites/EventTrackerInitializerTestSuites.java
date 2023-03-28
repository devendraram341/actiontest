package com.attunedlabs.leap.testsuites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.attunedlabs.leap.eventtracker.initializer.PersistEventDetailsTest;
import com.attunedlabs.leap.eventtracker.initializer.RetryFailedEventTaskTest;

@RunWith(Suite.class)
@SuiteClasses({ 
		PersistEventDetailsTest.class, 
		RetryFailedEventTaskTest.class 
	})
public class EventTrackerInitializerTestSuites {

}
