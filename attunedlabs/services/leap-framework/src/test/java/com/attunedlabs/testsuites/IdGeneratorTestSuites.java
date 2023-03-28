package com.attunedlabs.testsuites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.attunedlabs.idgenerator.service.LeapJavaRandomIdGeneratorTest;
import com.attunedlabs.idgenerator.service.LeapJavaUUIDGeneratorTest;

@RunWith(Suite.class)
@SuiteClasses({
	LeapJavaRandomIdGeneratorTest.class,
	LeapJavaUUIDGeneratorTest.class
})
public class IdGeneratorTestSuites {

}
