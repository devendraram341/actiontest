package com.attunedlabs.leap.testsuites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.attunedlabs.leap.identityservice.IdentityServiceBeanTest;
import com.attunedlabs.leap.identityservice.IdentityServiceUtilTest;

@RunWith(Suite.class)
@SuiteClasses({
		IdentityServiceBeanTest.class,
		IdentityServiceUtilTest.class
	})
public class IdentityServiceTestSuites {

}
