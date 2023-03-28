package com.attunedlabs.leap.base;

import static com.attunedlabs.LeapCoreTestConstant.*;

import java.util.Map;

import org.apache.camel.Exchange;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.attunedlabs.LeapCoreTestUtils;
import com.attunedlabs.configdbtest.FeatureDeploymentTestConfigDB;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.LeapServiceRuntimeContext;
import com.attunedlabs.leap.feature.routing.DynamicallyImplRoutingFailedException;

public class LeapBaseRoutingTest {

	private Exchange exchange;
	private LeapBaseRouting baseRouting;
	private LeapDataContext leapDatacontext;
	private FeatureDeploymentTestConfigDB testConfigDB;

	@Before
	public void setUp() {
		if (baseRouting == null)
			baseRouting = new LeapBaseRouting();
		if (leapDatacontext == null)
			leapDatacontext = new LeapDataContext();
		if (exchange == null)
			exchange = LeapCoreTestUtils.createExchange();
		if (testConfigDB == null)
			testConfigDB = new FeatureDeploymentTestConfigDB();
		
	}

	/**
	 * This method is used to route to execution route based on service name without
	 * executePipeline
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRouteWithoutExecutePipeLineServiceName() throws Exception {
		LeapCoreTestUtils.setRequestHeaderWithoutPipeLine(leapDatacontext);
		exchange.getIn().setHeader(LEAP_DATA_CONTEXT, leapDatacontext);
		baseRouting.route(exchange);
		Map<String, Object> headers = exchange.getIn().getHeaders();
		Assert.assertNotNull("exchange header should not be null ::", headers);
		Assert.assertEquals("Executopn Route should be same as Expected data ::", getExecRoute(),
				headers.get(EXECUTION_ROUTE));

	}

	/**
	 * This method is used to route to execution route based on service name with
	 * executePipeline and featureDeployment data
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRouteWithExecutionPipelineServiceName() throws Exception {
		testConfigDB.addFeatureDeployement();
		LeapCoreTestUtils.setRequestHeaderWithPipeLine(leapDatacontext);
		exchange.getIn().setHeader(LEAP_DATA_CONTEXT, leapDatacontext);

		baseRouting.route(exchange);

		
		testConfigDB.deleteFeatureDeployement();
		Map<String, Object> afterHeaders = exchange.getIn().getHeaders();
		Assert.assertNotNull("exchange header should not be null ::", afterHeaders);
		Assert.assertEquals("Executopn Route should be same as Expected data ::", PIPELINE_SERVICE_ROUTE,
				afterHeaders.get(EXECUTION_ROUTE));

		LeapDataContext leapDataCtx = (LeapDataContext) afterHeaders.get(LEAP_DATA_CONTEXT);
		Assert.assertNotNull("LDC Should Not be null ::", leapDataCtx);

		LeapServiceRuntimeContext currentLeapServiceRuntimeContext = leapDataCtx.getServiceDataContext()
				.getCurrentLeapServiceRuntimeContext();
		Assert.assertNotNull("LeapServiceRuntimeContext Should not be null :", currentLeapServiceRuntimeContext);

		String implementationName = currentLeapServiceRuntimeContext.getImplementationName();
		Assert.assertNotNull("implementationName Should not be null :", implementationName);
		Assert.assertEquals(TEST_IMPL, implementationName);
	}
	
	/**
	 * 
	 * This method is used to route to execution route based on service name with
	 * executePipeline
	 *
	 * @throws Exception
	 */
	@Test(expected = DynamicallyImplRoutingFailedException.class)
	public void testRouteWithExecutionPipelineServiceNameWithoutFeatureDeploy() throws Exception {
		LeapCoreTestUtils.setRequestHeaderWithPipeLine(leapDatacontext);
		exchange.getIn().setHeader(LEAP_DATA_CONTEXT, leapDatacontext);

		baseRouting.route(exchange);
	}

	private String getExecRoute() {
		return TEST_FEATURE + "-" + TEST_SERVICE + "-executionEnrichmentRoute";
	}

}
