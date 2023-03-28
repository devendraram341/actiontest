package com.attunedlabs.eventframework.camel.eventproducer;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;

import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.LEAP_DATA_CONTEXT;

import java.util.Date;
import java.util.Map;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.RequestContext;
import com.attunedlabs.eventframework.event.LeapEvent;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.LeapHeader;
import com.attunedlabs.leap.LeapHeaderConstant;
import com.attunedlabs.leap.LeapServiceContext;
import com.attunedlabs.leap.LeapServiceRuntimeContext;

/**
 * 
 * @author bizruntime
 *
 */
public abstract class AbstractCamelEventBuilder implements ICamelEventBuilder {

	protected static final Logger logger = LoggerFactory.getLogger(AbstractCamelEventBuilder.class);

	/**
	 * This method is to update Standard Camel header
	 * 
	 * @param fromCamelExchange : Camel exchange
	 * @param toLeapEvent       : LeapEvent
	 */
	protected void updateStandardCamelHeader(Exchange fromCamelExchange, LeapEvent toLeapEvent) {
		String routeId = fromCamelExchange.getFromRouteId();
		String contextStr = fromCamelExchange.getContext().getName();
		/* #TODO Match the tenantId. keeping default for now */
		LeapDataContext leapDataContext = (LeapDataContext) fromCamelExchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext serviceDataContext = leapDataContext.getServiceDataContext();
		if (serviceDataContext != null)
			toLeapEvent.addMetadata(LeapHeaderConstant.REQUEST_ID_KEY, serviceDataContext.getRequestUUID());
		toLeapEvent.addMetadata(LeapHeaderConstant.TENANT_KEY, getTenantId(fromCamelExchange));
		toLeapEvent.addMetadata(LeapHeaderConstant.SITE_KEY, serviceDataContext.getSite());
		toLeapEvent.addMetadata(CamelEventProducerConstant.CAMEL_CONTEXT_ID, contextStr);
		toLeapEvent.addMetadata(CamelEventProducerConstant.CAMEL_ROUTER_ID, routeId);
		toLeapEvent.addMetadata(CamelEventProducerConstant.CAMEL_TIMESTAMP, new Date());
	}

	protected LeapEvent updateStandardCamelHeader(String eventId, Exchange fromCamelExchange) {
		// LeapEvent toLeapEvent
		RequestContext reqCtx = getRequestContextFromCamelExchange(fromCamelExchange);
		LeapEvent toLeapEvent = new LeapEvent(eventId, reqCtx);
		updateStandardCamelHeader(fromCamelExchange, toLeapEvent);
		return toLeapEvent;
	}

	/**
	 * This method is to set tenantid
	 * 
	 * @param fromCamelExchange : Camel Exchange
	 * @return String :tenantid
	 */
	protected String getTenantId(Exchange fromCamelExchange) {
		String methodName = "getTenantId";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapDataContext leapDataContext = (LeapDataContext) fromCamelExchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext serviceDataContext = leapDataContext.getServiceDataContext();
		String tenantId = serviceDataContext.getTenant();
		logger.trace("{} tenantid :{} ", LEAP_LOG_KEY, tenantId);
		if (tenantId == null || tenantId.isEmpty())
			return "default";
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return tenantId;
	}

	/**
	 * This method is used to get service type
	 * 
	 * @param fromCamelExchange :Camel Exchange
	 * @return String : service type
	 */
	protected String getServiceName(Exchange fromCamelExchange) {
		String methodName = "getServiceName";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapDataContext leapDataContext = (LeapDataContext) fromCamelExchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext serviceDataContext = leapDataContext.getServiceDataContext();
		LeapServiceRuntimeContext currentLeapServiceRuntimeContext = serviceDataContext
				.getCurrentLeapServiceRuntimeContext();
		String serviceName = currentLeapServiceRuntimeContext.getServiceName();
		logger.debug("{} serviceName : {}", LEAP_LOG_KEY, serviceName);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return serviceName;
	}

	/**
	 * This method is to update failureCamelHeader
	 * 
	 * @param fromCamelExchange : camel exchange
	 * @param toLeapEvent       : LeapEvent
	 */
	protected void updateFailureCamelHeader(Exchange fromCamelExchange, LeapEvent toLeapEvent) {
		Map<String, Object> excProp = fromCamelExchange.getProperties();
		String failureEndPoint = (String) excProp.get("CamelFailureEndpoint");
		Exception expCaught = (Exception) excProp.get("CamelExceptionCaught");
		Boolean wasRollbacked = (Boolean) excProp.get("CamelRollbackOnly");

		toLeapEvent.addMetadata(CamelEventProducerConstant.CAMEL_FAILED_ENDPONT, failureEndPoint);
		toLeapEvent.addMetadata(CamelEventProducerConstant.CAMEL_FAILURE_MSG, expCaught);
		toLeapEvent.addMetadata(CamelEventProducerConstant.CAMEL_ROUTE_ROLLBACK, wasRollbacked);
	}

	/**
	 * This method is to get event name
	 * 
	 * @param fromCamelExchange
	 * @return String : event name
	 */
	protected String getEventId(Exchange fromCamelExchange) {
		String methodName = "getEventId";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapDataContext leapDataContext = (LeapDataContext) fromCamelExchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext serviceDataContext = leapDataContext.getServiceDataContext();
		String eventname = null;
		logger.trace("{} eventid name :{} ", LEAP_LOG_KEY, eventname);
		if (eventname == null || eventname.isEmpty())
			return "UNKOWN";
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return eventname;

	}

	protected RequestContext getRequestContextFromCamelExchange(Exchange fromCamelExchange) {
		LeapDataContext leapDataContext = (LeapDataContext) fromCamelExchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext serviceDataContext = leapDataContext.getServiceDataContext();
		return serviceDataContext.getRequestContext();
	}

}
