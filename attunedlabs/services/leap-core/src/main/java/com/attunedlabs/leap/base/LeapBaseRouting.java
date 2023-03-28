package com.attunedlabs.leap.base;

import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.LEAP_DATA_CONTEXT;
import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;

import org.apache.camel.Exchange;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.featuredeployment.FeatureDeployment;
import com.attunedlabs.featuredeployment.FeatureDeploymentServiceException;
import com.attunedlabs.featuredeployment.IFeatureDeployment;
import com.attunedlabs.featuredeployment.impl.FeatureDeploymentService;
import com.attunedlabs.integrationfwk.pipeline.service.PipelineServiceConstant;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.LeapServiceContext;
import com.attunedlabs.leap.LeapServiceRuntimeContext;
import com.attunedlabs.leap.feature.routing.DynamicallyImplRoutingFailedException;
import com.attunedlabs.leap.util.LeapConfigurationUtil;

/**
 * This class is for routing to specific execution route based of service type
 * 
 * @author Reactiveworks
 *
 */
public class LeapBaseRouting {
	private static final Logger logger = LoggerFactory.getLogger(LeapBaseRouting.class);

	public static final String EXECUTIONROUTE_BASEDON_SERVICENAME_FILE = "routesendpoints.properties";

	/**
	 * This method is used to route to execution route based on service name
	 * 
	 * @param exchange
	 *            : Exchange object to get leap header
	 * @return : execution route to send in string
	 * @throws DynamicallyTRRoutingFailedException
	 * @throws JSONException
	 * @throws FeatureDeploymentServiceException
	 * @throws DynamicallyImplRoutingFailedException
	 */
	public void route(Exchange exchange) throws DynamicallyTRRoutingFailedException, JSONException,
			FeatureDeploymentServiceException, DynamicallyImplRoutingFailedException {
		String methodName = "route";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		// get the leapDataContext, leapServiceContext and leapServiceRuntimeContext
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext leapServiceContext=leapDataContext.getServiceDataContext();
		LeapServiceRuntimeContext leapServiceRuntimeContext=leapServiceContext.getCurrentLeapServiceRuntimeContext();
		String featureName = leapServiceContext.getFeatureName();
		String serviceName = leapServiceRuntimeContext.getServiceName();

		if (featureName != null && serviceName != null && !serviceName.isEmpty()) {
			/*if (serviceName.trim().equals(LeapDataConstant.DATA)) {
				exchange.getIn().setHeader(PipelineServiceConstant.EXE_ROUTE,
						LeapDataConstant.DATA_ENTITY_SERVICE_ROUTE);
			}*/  if (serviceName.trim().equals(PipelineServiceConstant.EXECUTE_PIPELINE)) {
				exchange.getIn().setHeader(PipelineServiceConstant.EXE_ROUTE,
						PipelineServiceConstant.PIPELINE_SERVICE_ROUTE);
				IFeatureDeployment featureDeploymentservice = new FeatureDeploymentService();
				FeatureDeployment featureDeployment = featureDeploymentservice
						.getActiveAndPrimaryFeatureDeployedFromCache(leapServiceContext.getTenant(), leapServiceContext.getSite(),
								featureName, leapServiceContext);
				if (featureDeployment != null) {
					// providing implementation, vendor and version support
					leapServiceRuntimeContext.setImplementationName(featureDeployment.getImplementationName());
					leapServiceRuntimeContext.setVendor(featureDeployment.getVendorName());
					leapServiceRuntimeContext.setVersion(featureDeployment.getFeatureVersion());
				} else {
					throw new DynamicallyImplRoutingFailedException(
							"unable to get the Implementation name : " + featureDeployment);
				}
			} else {
				//defining exec route based on <featureName>-<ServiceName>-executionEnrichmentRoute
				String executionroute = featureName.trim() + "-" + serviceName.trim() + "-executionEnrichmentRoute";

				logger.trace("{} execution route to send based on serviceName is :{} ", LEAP_LOG_KEY, executionroute.trim());
				exchange.getIn().setHeader(PipelineServiceConstant.EXE_ROUTE, executionroute.trim());

				logger.trace("{} =============================================================================", LEAP_LOG_KEY);
				logger.trace("{} LeapBaseRouting [Stop] : {}" , LEAP_LOG_KEY, System.currentTimeMillis());
			}
		} else {
			LeapConfigurationUtil.setResponseCode(404, exchange,
					"No transformation route name is configured for the service in feature  :" + serviceName);
			throw new DynamicallyTRRoutingFailedException(
					"No transformation route name is configured for the service in feature  :" + serviceName);
		}

	}// end of route method
}
