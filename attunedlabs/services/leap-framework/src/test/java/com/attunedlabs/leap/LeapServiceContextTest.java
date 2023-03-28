package com.attunedlabs.leap;

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.ContextData;
import com.attunedlabs.GenericTestConstant;
import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.leapentity.config.LeapDataServiceConfigurationException;
import com.attunedlabs.permastore.config.IPermaStoreConfigurationService;
import com.attunedlabs.permastore.config.PermaStoreConfigParserException;
import com.attunedlabs.permastore.config.PermaStoreConfigRequestException;
import com.attunedlabs.permastore.config.PermaStoreConfigurationException;
import com.attunedlabs.permastore.config.impl.PermaStoreConfigurationService;
import com.attunedlabs.permastore.config.jaxb.PermaStoreConfiguration;
import com.attunedlabs.permastore.config.util.PermastoreFileRead;

public class LeapServiceContextTest {

	Logger log = LoggerFactory.getLogger(LeapServiceContextTest.class);
	private LeapServiceContext leapServiceCtx;
	private final String SERVICE_NAME = "TestService";
	private final String CONFIG_NAME = "TestName";

	@Before
	public void setUp() {
		if (leapServiceCtx == null)
			leapServiceCtx = new LeapServiceContext(GenericTestConstant.TEST_TENANT, GenericTestConstant.TEST_SITE,
					GenericTestConstant.TEST_FEATUREGROUP, GenericTestConstant.TEST_FEATURE);
		leapServiceCtx.initializeLeapRuntimeServiceContext(SERVICE_NAME);
		leapServiceCtx.SetRunningContextServiceName(SERVICE_NAME);
		leapServiceCtx.addLeapServiceRuntimeContext(new LeapServiceRuntimeContext(SERVICE_NAME,
				GenericTestConstant.TEST_IMPL, GenericTestConstant.TEST_VENDOR, GenericTestConstant.TEST_VERSION));
	}

	@Test
	public void testGetPermastoreByNameFromServiceContext() throws PermaStoreConfigRequestException {
		Object permastoreByNameFromServiceContext = leapServiceCtx.getPermastoreByNameFromServiceContext(CONFIG_NAME);
		Assert.assertNull("permastore By name service data should be null ::", permastoreByNameFromServiceContext);

		addPermastoreConfig();
		
		Object permastoreByNameFromServiceContext1 = leapServiceCtx.getPermastoreByNameFromServiceContext(CONFIG_NAME);
		Assert.assertNotNull("permastore By name service data should not be null ::",
				permastoreByNameFromServiceContext1);
		Assert.assertEquals("{\"third\":101,\"four\":102}", permastoreByNameFromServiceContext1.toString());
	}

	@Test
	public void testGetRunningContextServiceName() {
		String runningContextServiceName = leapServiceCtx.getRunningContextServiceName();
		Assert.assertNotNull("running context service name should not be null ::", runningContextServiceName);
		Assert.assertEquals("Service Name Should be same as TestService ::", SERVICE_NAME, runningContextServiceName);
	}

	@Test
	public void testGetEntitesConfigFromServiceContext() throws LeapDataServiceConfigurationException {
		leapServiceCtx.storeEntityConfigToServiceContext(CONFIG_NAME);
		Map<String, Object> entitesConfigFromServiceContext = leapServiceCtx.getEntitesConfigFromServiceContext();

		log.debug("entitesConfigFromServiceContextentitesConfigFromServiceContext " + entitesConfigFromServiceContext);
	}

	private void addPermastoreConfig() {
		PermaStoreConfiguration permaStoreConfiguration;
		try {
			permaStoreConfiguration = PermastoreFileRead.getPermaStoreConfigurations().getPermaStoreConfiguration()
					.get(0);
			Assert.assertNotNull("permastore configuration Should not be null ::", permaStoreConfiguration);

			IPermaStoreConfigurationService permastoreConfigService = new PermaStoreConfigurationService();
			ConfigurationContext configurationContext = ContextData.getConfigContext();
			Assert.assertNotNull("configuration context should not be null ::", configurationContext);

			permastoreConfigService.addPermaStoreConfiguration(configurationContext, permaStoreConfiguration);
			
			RequestContext context = ContextData.getRequestContext();
			leapServiceCtx.setRequestContext(context);
			leapServiceCtx.storePermastoreConfigToServiceContext(CONFIG_NAME);
			
		} catch (PermaStoreConfigParserException e) {
			e.printStackTrace();
		} catch (PermaStoreConfigurationException e) {
			e.printStackTrace();
		} catch (PermaStoreConfigRequestException e) {
			e.printStackTrace();
		}

	}
}
