package com.attunedlabs.testsuites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.attunedlabs.leap.LeapDataContextTest;
import com.attunedlabs.leap.context.helper.LeapDataContextHelperTest;
import com.attunedlabs.leap.context.helper.LeapRequestHelperTest;

@RunWith(Suite.class)
@SuiteClasses({ 
	LeapDataContextTest.class, 
	LeapDataContextHelperTest.class, 
	LeapRequestHelperTest.class })
public class LeapTestSuites {

}
