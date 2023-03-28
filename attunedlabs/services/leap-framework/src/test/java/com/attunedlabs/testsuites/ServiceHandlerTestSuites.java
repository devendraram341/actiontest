package com.attunedlabs.testsuites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.attunedlabs.servicehandlers.config.ServiceHandlerConfigXMLParserTest;
import com.attunedlabs.servicehandlers.config.ServiceHandlerConfigurationServiceTest;

@RunWith(Suite.class)
@SuiteClasses({
	ServiceHandlerConfigurationServiceTest.class,
	ServiceHandlerConfigXMLParserTest.class
})
public class ServiceHandlerTestSuites {

}
