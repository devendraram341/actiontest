package com.attunedlabs.baseroute;

import static com.attunedlabs.LeapCoreTestConstant.*;

import org.apache.camel.Message;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.RouteDefinition;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.attunedlabs.baseroute.util.BaseRouteTestUtil;
import com.attunedlabs.baseroute.util.PropertiesForTesting;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.context.bean.InitialLeapDataContextElement;
import com.attunedlabs.leap.context.bean.LeapDataContextElement;

public class ExitRouteTest extends BaseRouteTestUtil {

	private LeapDataContext dataContext;

	@Before
	public void init() {
		dataContext = new LeapDataContext();
		InitialLeapDataContextElement initialRequestHeaderElement = new InitialLeapDataContextElement();
		initialRequestHeaderElement.setApiVersion("1.0");
		initialRequestHeaderElement.setContext("contextString");
		initialRequestHeaderElement.setLang("EN");
		initialRequestHeaderElement.setData(null);
		LeapDataContextElement createShipHeaderElement = new LeapDataContextElement(TAG_NAME_LEAP_INITIAL,
				initialRequestHeaderElement);
		dataContext.addContextElement(createShipHeaderElement, TAG_NAME_LEAP_INITIAL);
	}

	/**
	 * This is exit route which will be called at last
	 * 
	 * @throws Exception
	 */
	@Test
	public void testExitRoute() throws Exception {
		RouteDefinition route = context.getRouteDefinition(EXIT_ROUTE);

		route.adviceWith(context, new AdviceWithRouteBuilder() {

			@Override
			public void configure() throws Exception {
				weaveAddFirst().setHeader(LEAP_DATA_CONTEXT, constant((Object) dataContext));
				weaveAddLast().to(MOCK_FINISH);
			}
		});
		template.sendBodyAndHeaders(PropertiesForTesting.getInstance().getValue(EXIT_ROUTE), DEMO_DATA, setHeader());

		MockEndpoint mockEndpoint = getMockEndpoint(MOCK_FINISH);
		mockEndpoint.expectedMessageCount(1);
		mockEndpoint.assertIsSatisfied();

		Message message = mockEndpoint.getReceivedExchanges().get(0).getIn();
		Assert.assertNotNull("Exchange message should not be null ::", message);
		Assert.assertNotNull("Exchange Mesaage Body Shoud not be null ::", message.getBody());
		Assert.assertNotNull("Exchange Header Should not be null ::", message.getHeaders());

		Object header = message.getHeader(LEAP_DATA_CONTEXT);
		Assert.assertNotNull("Header value should not be null ::", header);
		Assert.assertTrue("header should be InstanceOf LeapDataContext :: ", header instanceof LeapDataContext);

		Assert.assertEquals("body data should be same as Expected data ::", DEMO_DATA, message.getBody());
	}
}
