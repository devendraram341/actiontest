package com.attunedlabs.leap.eventsubscription.routebuilder;

import static com.attunedlabs.LeapCoreTestConstant.*;

import java.util.Map;

import javax.jms.Connection;
import javax.jms.JMSException;

import org.apache.camel.Exchange;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.attunedlabs.LeapCoreTestFileRead;
import com.attunedlabs.LeapCoreTestUtils;
import com.attunedlabs.config.server.ConfigServerInitializationException;
import com.attunedlabs.config.server.LeapConfigurationServer;
import com.attunedlabs.eventframework.config.EventFrameworkConfigurationUnit;
import com.attunedlabs.eventframework.config.IEventFrameworkConfigService;
import com.attunedlabs.eventframework.config.impl.EventFrameworkConfigService;
import com.attunedlabs.eventframework.jaxb.EventFramework;
import com.attunedlabs.eventframework.jaxb.JMSSubscribeEvent;
import com.attunedlabs.leap.LeapHeaderConstant;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JMSMessageProcessingWayTest {
	private IEventFrameworkConfigService configService = new EventFrameworkConfigService();
	private JMSMessageProcessingWay processingWay;
	private Exchange exchange;

	@Before
	public void init() {
		System.setProperty(PROFILE_ID, LOCAL);
		if (processingWay == null)
			processingWay = new JMSMessageProcessingWay(configService, LeapCoreTestUtils.setSubscriptionUtil());
		if (exchange == null)
			exchange = LeapCoreTestUtils.createJmsExchange();
	}

	/**
	 * this method use for message processing way with subscriptionId
	 * 
	 * @throws Exception
	 */
	@Test
	public void testBProcessWithSubId() throws Exception {
		addConfigurationUtil();
		LeapHeaderConstant.tenant = TEST_TENANT;
		LeapHeaderConstant.site = TEST_SITE;
		exchange.getIn().setHeader(SUBSCRIPTION_ID, setSubscriptionId());

		processingWay.process(exchange);

		Map<String, Object> headers = exchange.getIn().getHeaders();
		Assert.assertNotNull("Exchange Header Should not be null ::", headers);
		Assert.assertTrue("Exchange header should be contain processingDecision key ",
				headers.containsKey("processingDecision"));
		Assert.assertNotNull("processingDecision key value should not be null ::", headers.get("processingDecision"));
	}

	@After
	public void close() throws JMSException {
		Connection connection = ConnectionFactoryConfiguration.connection;
		if (connection == null)
			connection = LeapCoreTestUtils.connection;
		if (connection != null)
			connection.close();
	}

	private String setSubscriptionId() {
		return TEST_FEATUREGROUP + "-" + TEST_FEATURE + "-" + TEST_IMPL + "-" + TEST_VENDOR + "-" + TEST_VERSION
				+ "-TestJMS";
	}

	private void addConfigurationUtil() {
		EventFramework jmsSubscription = LeapCoreTestFileRead.getJmsSubscription();
		JMSSubscribeEvent jmsSubscribeEvent = jmsSubscription.getEventSubscription().getJmsSubscribeEvent().get(0);

		EventFrameworkConfigurationUnit configurationUnit = new EventFrameworkConfigurationUnit(TEST_TENANT, TEST_SITE,
				TEST_VENDOR_NODEID, false, jmsSubscribeEvent);

		try {
			LeapConfigurationServer.getConfigurationService().addConfiguration(configurationUnit);
		} catch (ConfigServerInitializationException e) {
			e.printStackTrace();
		}
	}

}
