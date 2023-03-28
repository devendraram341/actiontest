package com.attunedlabs.integrationfwk.activities.bean;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.LEAP_DATA_CONTEXT;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.integrationfwk.config.jaxb.PipeActivity;
import com.attunedlabs.integrationfwk.pipeline.service.PipelineServiceConstant;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.LeapServiceContext;

public class RouteActivity {
	final Logger logger = LoggerFactory.getLogger(RouteActivity.class);
	private String featureGroup;
	private String featureName;
	private String vendorName;
	private String version;
	private String serviceName;
	private String executionRoute;

	/**
	 * method to configure the leapheader contraints and setting the fetched route
	 * endpoint from the pipeline configuration in the header
	 * 
	 * @param exchange
	 * @throws RouteActivityException
	 */
	public void routeDecidingActivity(Exchange exchange) throws RouteActivityException {

		// fetched the pipeactivity from the header defined before
		PipeActivity pipeactivity = (PipeActivity) exchange.getIn().getHeader(ActivityConstant.PIPEACTIVITY_HEADER_KEY);

		// Checking if the route defined belongs to the same feature or not
		boolean samefeature = pipeactivity.getCamelRouteEndPoint().getCamelRoute().isIsSameFeature();
		// based on whether the current route belongs to the same feature or not, it
		// will be decided which
		// contraints to be changed in the leap header
		if (samefeature) {
			configForSameFeature(pipeactivity, exchange);
		} else {
			// #TODO need to test with different feature
			configForDifferentFeature(pipeactivity, exchange);
		}
	}

	/**
	 * Method to configure the leap header when the defined route is of same Feature
	 * and Feature group
	 * 
	 * @param pipeactivity
	 * @param leapHeader
	 * @param exchange
	 */
	public void configForSameFeature(PipeActivity pipeactivity, Exchange exchange) {
		String methodName = "configForSameFeature";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		serviceName = pipeactivity.getCamelRouteEndPoint().getCamelRoute().getServiceName();
		executionRoute = pipeactivity.getCamelRouteEndPoint().getCamelRoute().getExecutionRoute();
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext serviceDataContext = leapDataContext.getServiceDataContext();
		serviceDataContext.SetRunningContextServiceName(serviceName);
		// Cannot move this header to LDC , because it is used in camel route.
		exchange.getIn().setHeader(PipelineServiceConstant.EXE_ROUTE, serviceName);
		logger.trace("{} serviceName : {}", LEAP_LOG_KEY, serviceDataContext.getRunningContextServiceName());
		// setting the route endpoint in the header
		// Cannot move this header to LDC , because it is used in camel route.
		exchange.getIn().setHeader("executionRoute", executionRoute);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * Method to configure the leap header when the defined route is of Different
	 * Feature and Feature group
	 * 
	 * @param pipeactivity
	 * @param leapHeader
	 * @param exchange
	 * @throws RouteActivityException
	 */
	public void configForDifferentFeature(PipeActivity pipeactivity, Exchange exchange) throws RouteActivityException {
		// when feature and feature group is different

		String methodName = "configForDifferentFeature";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		try {
			featureGroup = pipeactivity.getCamelRouteEndPoint().getCamelRoute().getFeatureContext().getFeatureGroup();
			featureName = pipeactivity.getCamelRouteEndPoint().getCamelRoute().getFeatureContext().getFeatureName();
			vendorName = pipeactivity.getCamelRouteEndPoint().getCamelRoute().getFeatureContext().getVendorName();
			version = pipeactivity.getCamelRouteEndPoint().getCamelRoute().getFeatureContext().getVersion();
			serviceName = pipeactivity.getCamelRouteEndPoint().getCamelRoute().getServiceName();
			executionRoute = pipeactivity.getCamelRouteEndPoint().getCamelRoute().getExecutionRoute();
			LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn().getHeader(LEAP_DATA_CONTEXT);
			LeapServiceContext serviceDataContext = leapDataContext.getServiceDataContext();
			serviceDataContext.setFeatureGroup(featureGroup);
			serviceDataContext.setFeatureName(featureName);
			serviceDataContext.setVendor(vendorName);
			serviceDataContext.setVersion(version);
			serviceDataContext.SetRunningContextServiceName(serviceName);
			// setting the route endpoint in the header. Cannot move this header because it
			// is used in camel route.
			exchange.getIn().setHeader("executionRoute", executionRoute);

			if (featureGroup.equals("") || featureName.equals("") || vendorName.equals("") || version.equals("")
					|| serviceName.equals("") || executionRoute.equals("")) {
				throw new RouteActivityException("Data cannot be empty");
			}
		} catch (NullPointerException e) {
			throw new RouteActivityException("Data Cannot be Null");
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}
}