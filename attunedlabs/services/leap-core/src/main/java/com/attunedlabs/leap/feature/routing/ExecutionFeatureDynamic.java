package com.attunedlabs.leap.feature.routing;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.FEATURE_DEPLOYMENT;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.LEAP_DATA_CONTEXT;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.PROVIDER;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.apache.camel.Exchange;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.persistence.InvalidNodeTreeException;
import com.attunedlabs.feature.config.FeatureConfigRequestContext;
import com.attunedlabs.feature.config.FeatureConfigRequestException;
import com.attunedlabs.feature.config.FeatureConfigurationException;
import com.attunedlabs.feature.config.FeatureConfigurationUnit;
import com.attunedlabs.feature.config.impl.FeatureConfigurationService;
import com.attunedlabs.feature.jaxb.Feature;
import com.attunedlabs.feature.jaxb.Service;
import com.attunedlabs.featuredeployment.FeatureDeployment;
import com.attunedlabs.featuredeployment.IFeatureDeployment;
import com.attunedlabs.featuredeployment.impl.FeatureDeploymentService;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.LeapHeaderConstant;
import com.attunedlabs.leap.LeapServiceContext;
import com.attunedlabs.leap.LeapServiceRuntimeContext;
import com.attunedlabs.leap.feature.FeatureErrorPropertyConstant;
import com.attunedlabs.leap.generic.UnableToLoadPropertiesException;
import com.attunedlabs.leap.util.LeapConfigurationUtil;

public class ExecutionFeatureDynamic {
	private static final Logger logger = LoggerFactory.getLogger(ExecutionFeatureDynamic.class);

	private static Properties errorcodeprop = null;
	private static Properties errormessageprop = null;

	private static final String IMPL_ROUTE = "implroute";

	private IFeatureDeployment featureDeploymentservice = new FeatureDeploymentService();

	static {
		try {
			errorcodeprop = loadingPropertiesFile(FeatureErrorPropertyConstant.ERROR_CODE_FILE);
			errormessageprop = loadingPropertiesFile(FeatureErrorPropertyConstant.ERROR_MESSAGE_FILE);
		} catch (UnableToLoadPropertiesException e) {
			logger.error("{} Unable to read error code and error message property file", LEAP_LOG_KEY);
		}
	}

	/**
	 * This method is for dynamically routing to Implementation route
	 * 
	 * @param exchange : Exchange Object
	 * @return : String
	 * 
	 * @throws UnableToLoadPropertiesException
	 * @throws DynamicallyImplRoutingFailedException
	 * @throws JSONException
	 *
	 */
	public void route(Exchange exchange)
			throws UnableToLoadPropertiesException, DynamicallyImplRoutingFailedException, JSONException {
		logger.debug("{} ExecutionFeatureDynamic-route[start]: {}", LEAP_LOG_KEY, System.currentTimeMillis());
		String methodName = "route";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		String implName = null;
		String vendor = null;
		String version = null;
		// getting the leapData context, service context and runtime service context
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext leapserviceContext = leapDataContext.getServiceDataContext();
		LeapServiceRuntimeContext currentLeapServiceRuntimeContext = leapserviceContext
				.getCurrentLeapServiceRuntimeContext();
		FeatureConfigRequestContext featureRequestContext = null;

		// get Service context related data
		String tenant = leapserviceContext.getTenant();
		String siteId = leapserviceContext.getSite();
		String featureGroup = leapserviceContext.getFeatureGroup();
		String featureName = leapserviceContext.getFeatureName();
		String serviceName = currentLeapServiceRuntimeContext.getServiceName();
		String provider = null;

		if (exchange.getIn().getHeaders().containsKey(PROVIDER)) {
			provider = (String) exchange.getIn().getHeader(PROVIDER);
			logger.trace("{} provider in if : {}", LEAP_LOG_KEY, provider);
			currentLeapServiceRuntimeContext.setProvider(provider);
		}
		logger.trace("{} Service context in ExecutionFeatureDynamic:{}", LEAP_LOG_KEY, leapserviceContext);
		try {
			FeatureDeployment featureDeployment;
			if (provider != null) {
				logger.trace("{} provider is not null in ExecutionFeatureDynamic class",LEAP_LOG_KEY);
				featureDeployment = featureDeploymentservice.getActiveAndPrimaryFeatureDeployedFromCache(tenant, siteId,
						featureName, provider, leapserviceContext);
			} else {
				logger.trace("{} provider is Null in ExecutionFeatureDynamic class", LEAP_LOG_KEY);
				featureDeployment = (FeatureDeployment) exchange.getIn().getHeader(FEATURE_DEPLOYMENT);
			}

			if (featureDeployment != null) {
				// providing implementation, vendor and version support
				implName = featureDeployment.getImplementationName();
				vendor = featureDeployment.getVendorName();
				version = featureDeployment.getFeatureVersion();

				// updating runtime service context with impl, vendor, version
				updateLeapServiceRuntimeContext(implName, vendor, version, currentLeapServiceRuntimeContext);
				logger.trace("{} Runtime Service context in ExecutionFeatureDynamic after update:{} ", LEAP_LOG_KEY,
						currentLeapServiceRuntimeContext);
				if (provider != null) {
					featureRequestContext = new FeatureConfigRequestContext(tenant, siteId, featureGroup, featureName,
							implName, vendor, version, provider);
				} else {
					featureRequestContext = new FeatureConfigRequestContext(tenant, siteId, featureGroup, featureName,
							implName, vendor, version);
				}

			} else {
				throw new DynamicallyImplRoutingFailedException(
						"unable to get the active and primary feature deployement : " + featureDeployment);
			}
			// creating a featureConfigurationUnit
			FeatureConfigurationUnit featureConfigurationUnit;
			try {
				featureConfigurationUnit = getFeatureConfigurationUnit(featureRequestContext, featureName);
				logger.trace("{} feature config unit : {}", LEAP_LOG_KEY, featureConfigurationUnit);
				if (featureConfigurationUnit != null) {
					String routeToRedirect = routeToRedirect(featureConfigurationUnit, leapserviceContext, serviceName,
							errorcodeprop, errormessageprop, exchange);
					if (routeToRedirect != null && !(routeToRedirect.isEmpty())) {
						exchange.getIn().setHeader(IMPL_ROUTE, routeToRedirect.trim());
					} else {
						throw new DynamicallyImplRoutingFailedException(
								"No implementation route name is configured for the service in feature  : "
										+ featureRequestContext);
					}
				} else {
					// if you don't find feature under tenant specific then, search on global level
					logger.trace("{} searching for feature service on global level",LEAP_LOG_KEY);
					featureRequestContext = new FeatureConfigRequestContext(LeapHeaderConstant.tenant,
							LeapHeaderConstant.site, featureGroup, featureName, implName, vendor, version);
					featureConfigurationUnit = getFeatureConfigurationUnit(featureRequestContext, featureName);
					if (featureConfigurationUnit != null) {
						String routeToRedirect = routeToRedirect(featureConfigurationUnit, leapserviceContext,
								serviceName, errorcodeprop, errormessageprop, exchange);
						if (routeToRedirect != null && !(routeToRedirect.isEmpty())) {
							exchange.getIn().setHeader(IMPL_ROUTE, routeToRedirect.trim());
						} else {
							throw new DynamicallyImplRoutingFailedException(
									"No implementation route name is configured for the service in feature  : "
											+ featureRequestContext);
						}
					} else {
						throw new DynamicallyImplRoutingFailedException(
								"No feature is configured with request context : " + featureRequestContext);
					}
				}

			} catch (InvalidNodeTreeException | FeatureConfigRequestException | FeatureConfigurationException e) {
				LeapConfigurationUtil.setResponseCode(404, exchange,
						"Unable to load the configuraion feature with request conetxt : " + featureRequestContext + ":"
								+ e.getMessage());
				throw new DynamicallyImplRoutingFailedException(
						"Unable load the configuraion feature with request conetxt : " + featureRequestContext);
			}

		} catch (Exception e) {
			LeapConfigurationUtil.setResponseCode(404, exchange, e.getMessage());
			throw new DynamicallyImplRoutingFailedException(
					"Unable to route to Implementation route because no feature with request conetxt : "
							+ featureRequestContext + " is marked as primary");
		}
		logger.debug("{} ExecutionFeatureDynamic-route[stop]: {}", LEAP_LOG_KEY, System.currentTimeMillis());
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * This method is used to update the runtime service context with its impl,
	 * vendor and version.
	 * 
	 * @param implName                  : Name of the implementation
	 * @param vendor                    : Name of the vendor
	 * @param version                   : Name of the version
	 * @param leapServiceRuntimeContext : {@link LeapServiceRuntimeContext}
	 */
	private void updateLeapServiceRuntimeContext(String implName, String vendor, String version,
			LeapServiceRuntimeContext leapServiceRuntimeContext) {
		leapServiceRuntimeContext.setVendor(vendor);
		leapServiceRuntimeContext.setImplementationName(implName);
		leapServiceRuntimeContext.setVersion(version);
	}

	/**
	 * This method is to load the properties file from the classpath
	 * 
	 * @param filetoload : name of the file to be loaded
	 * @return Properties Object
	 * @throws UnableToLoadPropertiesException
	 */
	private static Properties loadingPropertiesFile(String filetoload) throws UnableToLoadPropertiesException {
		Properties prop = new Properties();
		InputStream input1 = ExecutionFeatureDynamic.class.getClassLoader().getResourceAsStream(filetoload);
		try {
			prop.load(input1);
		} catch (IOException e) {
			throw new UnableToLoadPropertiesException(
					"unable to load property file = " + FeatureErrorPropertyConstant.ERROR_CODE_FILE, e);
		}
		return prop;
	}

	/**
	 * This method is used to get the feature configuration unit
	 * 
	 * @param featureRequestContext : Request context object contain
	 *                              tenant,site,featuregroup and feature name
	 * @param configname            : configuration name
	 * @return FeatureConfigurationUnit Object
	 * @throws InvalidNodeTreeException
	 * @throws FeatureConfigRequestException
	 * @throws FeatureConfigurationException
	 */
	private FeatureConfigurationUnit getFeatureConfigurationUnit(FeatureConfigRequestContext featureRequestContext,
			String configname)
			throws InvalidNodeTreeException, FeatureConfigRequestException, FeatureConfigurationException {
		FeatureConfigurationService featureConfigurationService = new FeatureConfigurationService();
		FeatureConfigurationUnit featureConfigurationUnit = featureConfigurationService
				.getFeatureConfiguration(featureRequestContext, configname);
		return featureConfigurationUnit;
	}

	/**
	 * This method is to find the route for the implementation route
	 * 
	 * @param featureConfigurationUnit : FeatureCOnfigurationUnit Object
	 * @param leapHeader               : leapHeader Object
	 * @param errorcodeprop            : Properties file for error code
	 * @param errormessageprop         : Properties file for error message
	 * @param exchange                 : exchange to set body with error code and
	 *                                 error message
	 * @return String route to Implementation
	 * @throws DynamicallyImplRoutingFailedException
	 * @throws JSONException
	 */
	private String routeToRedirect(FeatureConfigurationUnit featureConfigurationUnit,
			LeapServiceContext leapserviceContext, String serviceName, Properties errorcodeprop,
			Properties errormessageprop, Exchange exchange)
			throws DynamicallyImplRoutingFailedException, JSONException {
		String methodName = "routeToRedirect";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		String implRouteEndpoint = null;
		boolean status = false;
		if (featureConfigurationUnit.getIsEnabled()) {
			status = true;
		}
		if (status) {
			Feature feature = (Feature) featureConfigurationUnit.getConfigData();
			List<Service> serviceList = feature.getService();
			logger.trace("{} no of feature service in list : {}" ,LEAP_LOG_KEY, serviceList.size());
			for (Service service : serviceList) {
				if (service.getName().equalsIgnoreCase(serviceName.trim())) {
					logger.trace("{} service name from cache :{} and service store in leap header :{} ", LEAP_LOG_KEY,
							service.getName(), serviceName.trim());
					if (service.isEnabled() == true) {
						implRouteEndpoint = setImplEndPointForService(service, leapserviceContext, exchange,
								errorcodeprop, errormessageprop);
						if (implRouteEndpoint != null && !(implRouteEndpoint.isEmpty())) {
							return implRouteEndpoint.trim();
						} else {
							throw new DynamicallyImplRoutingFailedException(
									"No implementation route name is configured service : " + service.getName());
						}
					} else {
						logger.trace("{} Not-supported :{} ", LEAP_LOG_KEY,
								FeatureErrorPropertyConstant.UNAVAILABLE_KEY);
						String errorcode = errorcodeprop.getProperty(FeatureErrorPropertyConstant.UNAVAILABLE_KEY);
						logger.trace("{} errorcode : {}", LEAP_LOG_KEY, errorcode);
						String errormsg = errormessageprop.getProperty(errorcode);
						logger.trace("{} error :{} ", LEAP_LOG_KEY, errormsg);
						LeapConfigurationUtil.setResponseCode(503, exchange,
								" for feature " + leapserviceContext.getFeatureName());
						exchange.getIn().setBody("status code : " + errorcode + " for feature "
								+ leapserviceContext.getFeatureName() + " \nReason : " + errormsg);
						implRouteEndpoint = "featureServiceNotExist";
					}
				} // end of if(servicename == leap core service name)

			} // end of for (Service service : serviceList)
		}
		return implRouteEndpoint;

	}// end of method

	/**
	 * This method is used to set the Implementation route from service
	 * 
	 * @param service          : Service Object specify the request for the type of
	 *                         service
	 * @param leapHeader       : LeapHeader Object
	 * @param exchange         : Camel Exchange
	 * @param errorcodeprop    : Get the error code
	 * @param errormessageprop : get the error message for the error code
	 * @return endpoint in String
	 * @throws JSONException
	 */
	private String setImplEndPointForService(Service service, LeapServiceContext leapserviceContext, Exchange exchange,
			Properties errorcodeprop, Properties errormessageprop) throws JSONException {
		// logger.error("before setting service endpoint
		// type"+System.currentTimeMillis());
		String implRouteEndpoint = null;
		String endpointtype = leapserviceContext.getEndpointType();
		switch (endpointtype) {
		case LeapHeaderConstant.HTTP_JSON_KEY:
			logger.trace("{} Endpoint type is HTTP JSON type",LEAP_LOG_KEY);
			implRouteEndpoint = service.getGenericRestEndpoint().getValue();
			break;
		case LeapHeaderConstant.HTTP_XML_KEY:
			logger.trace("{} Endpoint type is HTTP XML type",LEAP_LOG_KEY);
			implRouteEndpoint = service.getGenericRestEndpoint().getValue();
			break;
		case LeapHeaderConstant.CXF_ENDPOINT_KEY:
			logger.trace("{} Endpoint type is cxf type",LEAP_LOG_KEY);
			implRouteEndpoint = service.getConcreteSoapEndpoint().getValue();
			break;
		default: {
			logger.trace("{} Not-supported :{} " ,LEAP_LOG_KEY, FeatureErrorPropertyConstant.NOT_SUPPORTED_KEY);
			String errorcode = errorcodeprop.getProperty(FeatureErrorPropertyConstant.NOT_SUPPORTED_KEY);
			logger.trace("{} errorcode :{} " ,LEAP_LOG_KEY, errorcode);
			String errormsg = errormessageprop.getProperty(errorcode);
			logger.trace("{} error :{} ",LEAP_LOG_KEY, errormsg);
			LeapConfigurationUtil.setResponseCode(503, exchange,
					" for feature " + leapserviceContext.getFeatureName() + "with endpoint type : " + endpointtype);
			exchange.getIn()
					.setBody("status code : " + errorcode + " for feature " + leapserviceContext.getFeatureName()
							+ "with endpoint type : " + endpointtype + " \nReason : " + errormsg);
			implRouteEndpoint = "direct:noFeature";
		}
			break;
		}// end of switch
			// logger.error("after setting service endpoint
			// type"+System.currentTimeMillis());

		return implRouteEndpoint.trim();
	}// end of method

}
