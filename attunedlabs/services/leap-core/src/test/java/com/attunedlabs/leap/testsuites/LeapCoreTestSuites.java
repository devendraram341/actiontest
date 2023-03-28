package com.attunedlabs.leap.testsuites;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.attunedlabs.LeapCoreTestConstant;
import com.attunedlabs.LeapCoreTestUtils;
import com.attunedlabs.config.util.HSQLDBConnectionException;
import com.attunedlabs.config.util.EmbeddedHSQLDBConnectionUtil;
import com.attunedlabs.leap.base.LeapBaseRoutingTest;
import com.attunedlabs.leap.context.bean.helper.LeapDataContextConfigurationHelperTest;
import com.attunedlabs.leap.context.initializer.LeapDataContextInitializerTest;
import com.attunedlabs.leap.corsheaders.CORSHeadersInitializerTest;
import com.attunedlabs.leap.randomuuid.RandomStringUUIDTest;
import com.attunedlabs.leap.selflink.SelfLinkDataProcessorTest;
import com.attunedlabs.leap.transform.TransformDataTest;
import com.attunedlabs.leap.util.LeapConfigurationUtilTest;

@RunWith(Suite.class)
@SuiteClasses({ 
		EventSubscriptionLifecycleTestSuites.class,
		EventSubscriptionRoutebuilderTestSuites.class,	
		CORSHeadersInitializerTest.class,
		BaseRouteTestSuites.class,
		EventTrackerInitializerTestSuites.class,
		LeapBaseRoutingTest.class,
		LeapDataContextInitializerTest.class,
		LeapDataContextConfigurationHelperTest.class,
		TransformDataTest.class,
		LeapConfigurationUtilTest.class,
		HeaderInitializerTestSuites.class,
		RandomStringUUIDTest.class,
		IdentityServiceTestSuites.class,
		SelfLinkDataProcessorTest.class,
		EventSubscriptionProcessorTestSuites.class
	})
public class LeapCoreTestSuites {
	

	@BeforeClass
	public static void init() throws HSQLDBConnectionException{
		System.setProperty(LeapCoreTestConstant.PROFILE_ID, LeapCoreTestConstant.LOCAL);
		EmbeddedHSQLDBConnectionUtil.readFileAndExecute(LeapCoreTestUtils.getRequestContext(),null);
	}

	@AfterClass
	public static void clearDB() throws HSQLDBConnectionException{
		EmbeddedHSQLDBConnectionUtil.revertGlobalAppDeploymentProperties();
	}	
}
