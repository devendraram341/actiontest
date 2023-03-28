package com.attunedlabs.baseroute;

import org.apache.camel.Message;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.RouteDefinition;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.attunedlabs.LeapCoreTestConstant.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.attunedlabs.baseroute.util.BaseRouteTestUtil;
import com.attunedlabs.baseroute.util.PropertiesForTesting;
import com.attunedlabs.config.persistence.PrettyUrlMapping;
import com.attunedlabs.config.persistence.exception.PrettyUrlMappingDaoException;
import com.attunedlabs.config.persistence.impl.PrettyUrlMappingService;
import com.attunedlabs.config.persistence.impl.PrettyUrlMappingServiceException;
import com.attunedlabs.core.datagrid.DataGridService;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class RestPrettyUriRouteTest extends BaseRouteTestUtil {

	private PrettyUrlMapping mapping;
	@Before
	public void addedPrettyUrl() throws PrettyUrlMappingServiceException, PrettyUrlMappingDaoException {
		mapping = new PrettyUrlMapping(TEST_TENANT, TEST_SITE, PRETTY_URI_TEST, ACTUAL_URL, 1);
		PrettyUrlMappingService service = new PrettyUrlMappingService();
		service.addPrettyUrlMappingInDBAndCache(mapping);
		addPrettyUrlMappingInCache(mapping);
	}

	@Test
	public void testRestPrettyUriRouteWithPost() throws Exception {
		
		RouteDefinition route = context.getRouteDefinition(REST_PRETTY_URI);
		route.adviceWith(context, new AdviceWithRouteBuilder() {

			@Override
			public void configure() throws Exception {
				weaveByToUri(ALL_DIRECT_URI).remove();
				weaveAddFirst().setHeader(CAMEL_HTTP_METHOD, constant(HTTP_POST));
				weaveAddLast().to(MOCK_FINISH);
			}
		});

		MockEndpoint mockEndpoint = context.getEndpoint(MOCK_FINISH, MockEndpoint.class);
		mockEndpoint.expectedMessageCount(1);

		template.sendBodyAndHeaders(PropertiesForTesting.getInstance().getValue(REST_PRETTY_URI), null, setHeader());

		mockEndpoint.assertIsSatisfied();

		Message message = mockEndpoint.getReceivedExchanges().get(0).getIn();
		Assert.assertNotNull(message);

		Map<String, Object> headers = message.getHeaders();
		Assert.assertNotNull("exchange Header should not be null :", headers);
		Assert.assertEquals("camelHtppMethod Should be same as POST ::", PRETTY_URI_TEST, headers.get("Prettyuri"));
	}

	@Test
	public void testRestPrettyUriRouteWithGet() throws Exception {
		RouteDefinition route = context.getRouteDefinition(REST_PRETTY_URI);
		route.adviceWith(context, new AdviceWithRouteBuilder() {

			@Override
			public void configure() throws Exception {
				weaveByToUri(ALL_DIRECT_URI).remove();
				weaveAddFirst().setHeader(CAMEL_HTTP_METHOD, constant(HTTP_GET)).setHeader("CamelHttpQuery",
						constant("demo=testing&demo1=testing1"));
				weaveAddLast().to(MOCK_FINISH);
			}
		});

		MockEndpoint mockEndpoint = context.getEndpoint(MOCK_FINISH, MockEndpoint.class);
		mockEndpoint.expectedMessageCount(1);

		template.sendBodyAndHeaders(PropertiesForTesting.getInstance().getValue(REST_PRETTY_URI), null, setHeader());

		mockEndpoint.assertIsSatisfied();

		Message message = mockEndpoint.getReceivedExchanges().get(0).getIn();
		Assert.assertNotNull(message);

		Map<String, Object> headers = message.getHeaders();
		Assert.assertNotNull("exchange Header should not be null :", headers);
		Assert.assertEquals("camelHtppMethod Should be same as POST ::", PRETTY_URI_TEST, headers.get("Prettyuri"));

		String body = message.getBody(String.class);
		Assert.assertNotNull("Exchange body should not be null ::", body);
		Assert.assertEquals("Expeceted And actual data should be same ::", DEMO_REST_GET_DATA, body);
	}
	
	
	private void addPrettyUrlMappingInCache(PrettyUrlMapping prettyUrlMapping) {
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		IMap<String, Serializable> map = hazelcastInstance.getMap(getGlobalFeatureServiceKey());
		Map<String, Serializable> siteMap = new HashMap<>();
		Map<String, String> prettyUriMap = new HashMap<>();
		prettyUriMap.put(prettyUrlMapping.getPrettyString(), prettyUrlMapping.getActualString());
		siteMap.put(prettyUrlMapping.getSiteId(), (Serializable) prettyUriMap);
		map.put(prettyUrlMapping.getTenantId(), (Serializable) siteMap);
	}
	
	private static String getGlobalFeatureServiceKey() {
		return "GlobalFeatureService".trim();
	}
}
