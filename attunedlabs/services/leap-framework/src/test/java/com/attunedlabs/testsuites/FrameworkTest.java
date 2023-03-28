package com.attunedlabs.testsuites;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.attunedlabs.ContextData;
import com.attunedlabs.GenericTestConstant;
import com.attunedlabs.config.persistence.confignode.DeleteConfigNode;
import com.attunedlabs.config.persistence.confignode.InsertConfigNode;
import com.attunedlabs.config.util.EmbeddedHSQLDBConnectionUtil;
import com.attunedlabs.config.util.HSQLDBConnectionException;
import com.attunedlabs.ddlutils.config.DbConfigurationServiceTest;

@RunWith(Suite.class)
@SuiteClasses({ 
	EmbeddedTestSuites.class,
	DbConfigurationServiceTest.class,
	PersistenceTestSuites.class, 
	EventSubscriptionTestSuites.class, 
	EventTestSuites.class,
	FeatureMasterAndDeploymentTestSuites.class, 
	FeatureMetaInfoTestSuites.class, 
	FeatureServiceTestSuites.class,
	IdGeneratorTestSuites.class,
	DataContextTestSuites.class,
	PermaStoreTestSuites.class,
	ServiceHandlerTestSuites.class,
	LeapTestSuites.class
})

public class FrameworkTest {

	@BeforeClass
	public static void init() throws HSQLDBConnectionException{
		System.setProperty(GenericTestConstant.PROFILE_ID, GenericTestConstant.LOCAL);
		EmbeddedHSQLDBConnectionUtil.readFileAndExecute(ContextData.getRequestContext(),"leapdump.sql");
		new InsertConfigNode();
	}

	@AfterClass
	public static void clearDB() throws HSQLDBConnectionException{
		new DeleteConfigNode();
		EmbeddedHSQLDBConnectionUtil.revertGlobalAppDeploymentProperties();
	}	
}
