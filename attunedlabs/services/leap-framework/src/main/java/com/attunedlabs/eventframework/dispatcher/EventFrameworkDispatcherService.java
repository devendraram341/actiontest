package com.attunedlabs.eventframework.dispatcher;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeoutException;
import org.json.JSONObject;
import org.osgi.framework.InvalidSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.ConfigurationConstant;
import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.core.BeanDependencyResolveException;
import com.attunedlabs.core.BeanDependencyResolverFactory;
import com.attunedlabs.core.IBeanDependencyResolver;
import com.attunedlabs.core.datagrid.DataGridService;
import com.attunedlabs.eventframework.config.EventFrameworkConfigurationException;
import com.attunedlabs.eventframework.config.EventFrameworkConstants;
import com.attunedlabs.eventframework.config.IEventFrameworkConfigService;
import com.attunedlabs.eventframework.config.impl.EventFrameworkConfigHelper;
import com.attunedlabs.eventframework.config.impl.EventFrameworkConfigService;
import com.attunedlabs.eventframework.dispatchchannel.exception.MessageDispatchingException;
import com.attunedlabs.eventframework.dispatchchannel.exception.NonRetryableMessageDispatchingException;
import com.attunedlabs.eventframework.dispatcher.transformer.GenericLeapEventJsonTransformer;
import com.attunedlabs.eventframework.dispatcher.transformer.ILeapEventTransformer;
import com.attunedlabs.eventframework.dispatcher.transformer.LeapEventTransformationException;
import com.attunedlabs.eventframework.dispatcher.transformer.LeapEventXmlTransformer;
import com.attunedlabs.eventframework.event.LeapEvent;
import com.attunedlabs.eventframework.eventtracker.EventDispatcherTracker;
import com.attunedlabs.eventframework.eventtracker.IEventDispatcherTrackerService;
import com.attunedlabs.eventframework.eventtracker.impl.EventDispatcherTrackerException;
import com.attunedlabs.eventframework.eventtracker.impl.EventDispatcherTrackerImpl;
import com.attunedlabs.eventframework.eventtracker.impl.EventTrackerTableConstants;
import com.attunedlabs.eventframework.eventtracker.util.EventTrackerUtil;
import com.attunedlabs.eventframework.jaxb.DispatchChannel;
import com.attunedlabs.eventframework.jaxb.Event;
import com.attunedlabs.eventframework.jaxb.EventDispatcher;
import com.attunedlabs.eventframework.jaxb.SystemEvent;
import com.attunedlabs.osgi.helper.BeanResolutionHelper;
import com.attunedlabs.osgi.helper.OSGIEnvironmentHelper;
import com.hazelcast.durableexecutor.DurableExecutorService;

/**
 * 
 * @author bizruntime
 *
 */
public class EventFrameworkDispatcherService implements IEventFrameworkIDispatcherService {
	protected static final Logger logger = LoggerFactory.getLogger(EventFrameworkDispatcherService.class);
	private IEventFrameworkConfigService eventFrameworkConfigService;
	private IEventDispatcherTrackerService eventDispatcherTrackerService = new EventDispatcherTrackerImpl();

	public EventFrameworkDispatcherService() {
		eventFrameworkConfigService = new EventFrameworkConfigService();
	}

	/**
	 * This method is to dispatch event to executor service
	 * 
	 * @param : LeapEvent
	 * @throws EventFrameworkConfigurationException
	 * @throws EventFrameworkDispatcherException
	 * @throws MessageDispatchingException
	 * @throws LeapEventTransformationException
	 */
	public void dispatchforEvent(LeapEvent leapEvent, String tenant, String site, String requestId, boolean retryStatus)
			throws EventFrameworkConfigurationException, LeapEventTransformationException, MessageDispatchingException {
		String methodName = "dispatchforEvent";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		String eventId = leapEvent.getId();
		logger.debug("{} Event id={}", LEAP_LOG_KEY, eventId);
		ConfigurationContext configContext = null;
		configContext = getConfigurationContext(leapEvent.getRequestContext());
		logger.debug("{} config context :{} ", LEAP_LOG_KEY, configContext);

		Event eventConfig = null;
		try {
			eventConfig = eventFrameworkConfigService.getEventConfiguration(configContext, eventId);
		} catch (EventFrameworkConfigurationException e) {
			throw new EventFrameworkConfigurationException(
					"DispatcherService failed to get EventConfiguration for EventId{" + eventId + "} and context="
							+ configContext,
					e);
		}

		if (eventConfig == null)
			throw new EventFrameworkConfigurationException(
					"DispatcherService failed to get EventConfiguration for EventId{" + eventId + "} and context="
							+ configContext);

		DurableExecutorService executorService = getDurableDispatcherExecutorService(leapEvent.getRequestContext());
		boolean msgDispatcher = false;
		// Event can have more than One Dispatchers
		List<EventDispatcher> evtDispatcherConfigList = eventConfig.getEventDispatchers().getEventDispatcher();
		for (EventDispatcher evtDispatchConfig : evtDispatcherConfigList) {
			String dispatchChannelName = evtDispatchConfig.getDispatchChannelId();
			String dispatchChannelId = leapEvent.getDispatchChannelId();
			if (dispatchChannelId.equals(dispatchChannelName)) {
				DispatchChannel disChannel = eventFrameworkConfigService.getDispatchChannelConfiguration(
						getConfigurationContext(leapEvent.getRequestContext()),
						evtDispatchConfig.getDispatchChannelId());
				if (disChannel == null) {
					String failureMsg = "DispatchChannel is not enable for DispatchChannelId :" + dispatchChannelId;
					NonRetryableMessageDispatchingException error = new NonRetryableMessageDispatchingException(
							failureMsg);
					logger.error(failureMsg, error);
					JSONObject failureJson = new JSONObject();
					EventTrackerUtil.setFailureJSONString(failureJson, error, failureMsg);
					try {
						eventDispatcherTrackerService.updateEventStatus(tenant, site, requestId, requestId,
								EventTrackerTableConstants.STATUS_FAILED, leapEvent.getId(), true,
								failureJson.toString(), true, false, leapEvent.getDispatchChannelId());
					} catch (EventDispatcherTrackerException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						logger.error("{} error ::{} ", LEAP_LOG_KEY, e);
					}
					msgDispatcher = true;
					break;
				}
				String processingWay = disChannel.getProcessingWay();

				LeapEventDispatchTask task = getDispatcherTask(evtDispatchConfig, leapEvent, tenant, site, requestId,
						retryStatus, null);
				task.setProcessingWay(processingWay);
				logger.debug("{} processingWay is {}", LEAP_LOG_KEY, processingWay);
				if (processingWay.equals(EventTrackerTableConstants.PROCESSING_WAY_SYNC)) {
					task.run();
				} else {
					executorService.submit(task);
				}
				msgDispatcher = true;
				break;
			}
		}
		// end of For
		if (!msgDispatcher) {
			String failureMsg = " Unable to get dispatcher channel configuration of " + leapEvent.getDispatchChannelId()
					+ " for leap Event ::" + leapEvent.getId();
			NonRetryableMessageDispatchingException error = new NonRetryableMessageDispatchingException(failureMsg);
			logger.error("{} {} {}", LEAP_LOG_KEY, failureMsg, error);
			JSONObject failureJson = new JSONObject();
			EventTrackerUtil.setFailureJSONString(failureJson, error, failureMsg);
			try {
				eventDispatcherTrackerService.updateEventStatus(tenant, site, requestId, requestId,
						EventTrackerTableConstants.STATUS_FAILED, leapEvent.getId(), true, failureJson.toString(), true,
						false, leapEvent.getDispatchChannelId());
			} catch (EventDispatcherTrackerException e) {
				// TODO Auto-generated catch block
				logger.error("{} error ::{}", LEAP_LOG_KEY, e);
				throw new MessageDispatchingException("event status not updated in DB ",e);
			}
		}
	}// end of method

	/**
	 * This method is to dispatch event to executor service
	 * 
	 * @param : LeapEvent
	 * @throws EventFrameworkConfigurationException
	 * @throws EventFrameworkDispatcherException
	 * @throws MessageDispatchingException
	 * @throws LeapEventTransformationException
	 */
	public void dispatchforEvent(LeapEvent leapEvent, String tenant, String site, String requestId, boolean retryStatus,
			EventDispatcherTracker tracker)
			throws EventFrameworkConfigurationException, LeapEventTransformationException, MessageDispatchingException {
		logger.debug("{} inside dispatchforEvent() in LeapDispachterService : ", LEAP_LOG_KEY);
		String eventId = leapEvent.getId();

		ConfigurationContext configContext = getConfigurationContext(leapEvent.getRequestContext());
		logger.debug("{} config context :{} ", LEAP_LOG_KEY, configContext);

		Event eventConfig = null;
		try {
			eventConfig = eventFrameworkConfigService.getEventConfiguration(configContext, eventId);
		} catch (EventFrameworkConfigurationException e) {
			throw new EventFrameworkConfigurationException(
					"DispatcherService failed to get EventConfiguration for EventId{" + eventId + "} and context="
							+ configContext,
					e);
		}
		if (eventConfig == null)
			throw new EventFrameworkConfigurationException(
					"DispatcherService failed to get EventConfiguration for EventId{" + eventId + "} and context="
							+ configContext);
		DurableExecutorService executorService = getDurableDispatcherExecutorService(leapEvent.getRequestContext());

		// Event can have more than One Dispatchers
		List<EventDispatcher> evtDispatcherConfigList = eventConfig.getEventDispatchers().getEventDispatcher();
		for (EventDispatcher evtDispatchConfig : evtDispatcherConfigList) {
			try {
				String dispatchChannelName = evtDispatchConfig.getDispatchChannelId();
				String dispatchChannelId = leapEvent.getDispatchChannelId();
				if (dispatchChannelId.equals(dispatchChannelName)) {

					LeapEventDispatchTask task = getDispatcherTask(evtDispatchConfig, leapEvent, tenant, site,
							requestId, retryStatus, tracker);
					executorService.submit(task);

					break;
				}
			} catch (CancellationException e) {
				// closing the future gracefully.
			}
		} // end of For
	}// end of method

	/**
	 * This method is to dispatch event to executor service
	 * 
	 * @param : LeapEvent
	 * @throws MessageDispatchingException
	 * @throws LeapEventTransformationException
	 * @throws EventFrameworkConfigurationException
	 * @throws TimeoutException
	 */
	public void dispatchforSystemEvent(LeapEvent leapEvent, String tenant, String site, String requestId)
			throws LeapEventTransformationException, MessageDispatchingException, EventFrameworkConfigurationException {
		logger.debug("{} inside dispatchforEvent() in LeapDispachterService : ", LEAP_LOG_KEY);
		String eventId = leapEvent.getId();
		logger.debug("{} event in EventFramework dispatcher service :{} ", LEAP_LOG_KEY, leapEvent.toString());
		logger.debug("{} request context in EventFrameworkDispatcherService :{} ", LEAP_LOG_KEY,
				leapEvent.getRequestContext());
		ConfigurationContext configContext = getConfigurationContext(leapEvent.getRequestContext());
		logger.debug("{} config context : {}", LEAP_LOG_KEY, configContext);

		SystemEvent systemEventConfig = null;
		try {
			systemEventConfig = eventFrameworkConfigService.getSystemEventConfiguration(configContext, eventId);
			if (systemEventConfig == null) {
				logger.warn("{} DispatcherService failed to get SystemEventConfiguration for EventId{} and context=",
						LEAP_LOG_KEY, eventId, configContext);
			}
		} catch (EventFrameworkConfigurationException e) {
			throw new EventFrameworkConfigurationException(
					"DispatcherService failed to get EventConfiguration for EventId{" + eventId + "} and context="
							+ configContext,
					e);
		}
		DurableExecutorService executorService = getDurableDispatcherExecutorService(leapEvent.getRequestContext());
		// Event can have more than One Dispatchers
		if (systemEventConfig != null)
			if (systemEventConfig.getEventDispatchers() != null) {
				List<EventDispatcher> evtDispatcherConfigList = systemEventConfig.getEventDispatchers()
						.getEventDispatcher();
				for (EventDispatcher evtDispatchConfig : evtDispatcherConfigList) {
					try {
						String dispatchChannelName = evtDispatchConfig.getDispatchChannelId();
						String dispatchChannelId = leapEvent.getDispatchChannelId();
						if (dispatchChannelId.equals(dispatchChannelName)) {
							LeapEventDispatchTask dispatcherTask = getDispatcherTask(evtDispatchConfig, leapEvent,
									tenant, site, requestId, false, null);
							executorService.submit(dispatcherTask);
							break;
						}
					} catch (CancellationException e) {
						// closing the future gracefully.
					}
				}
			} // end of For
	}// end of method

	/**
	 * getDispatcherTask will built transformationType for specified
	 * <code>EventDispatcher</code> and form the LeapEventDispatchTask for
	 * dispatching events to channel.
	 * 
	 * @param evtDispatchConfig configuration to get transformation type and
	 *                          dispatchId.
	 * @param leapEvent
	 * @param requestId
	 * @param site
	 * @param tenant
	 * @param retryStatus
	 * @param processingWay
	 * @return instance of LeapEventDispatchTask.
	 * @throws LeapEventTransformationException
	 */
	private LeapEventDispatchTask getDispatcherTask(EventDispatcher evtDispatchConfig, LeapEvent leapEvent,
			String tenant, String site, String requestId, boolean retryStatus, EventDispatcherTracker tracker)
			throws LeapEventTransformationException {
		String channelId = evtDispatchConfig.getDispatchChannelId();
		String transformationType = evtDispatchConfig.getEventTransformation().getType();
		String tranformationBeanFQCN = null;
		String xslname = null;
		String xsltAsString = null;

		switch (transformationType) {
		case "CUSTOM":
			logger.debug("{} transformation type is CUSTOM", LEAP_LOG_KEY);
			tranformationBeanFQCN = evtDispatchConfig.getEventTransformation().getCustomTransformer().getFqcn().trim();
			break;

		case "XML-XSLT":
			logger.debug("{} transformation type is XML-XSLT", LEAP_LOG_KEY);
			xslname = evtDispatchConfig.getEventTransformation().getXSLTName().trim();
			xsltAsString = evtDispatchConfig.getEventTransformation().getXsltAsString().trim();

			break;

		case "JSON":
			logger.debug("{} transformation type is JSON", LEAP_LOG_KEY);
			// setting as null because JSON is default transformer
			tranformationBeanFQCN = null;
			break;
		default:
			logger.debug("{} no matching transformation type found in the system setting JSON as Default",
					LEAP_LOG_KEY);
			// JSON is deFAULT TRANSFORMER
			tranformationBeanFQCN = null;
			break;
		}// end of switch
		String value = "EventId=" + leapEvent.getId() + "--ChannelId=" + channelId + ", transformation bean : "
				+ tranformationBeanFQCN + ", xslname : " + xslname;
		logger.debug("{} LeapEventDispatchTask sending for execution for {}", LEAP_LOG_KEY, value);
		Serializable transformedMsg = transformLeapEvent(tranformationBeanFQCN, xslname, channelId, xsltAsString,
				leapEvent, tenant, site, requestId, requestId);
		logger.debug("{} transformedMsg ::{} ", LEAP_LOG_KEY, transformedMsg);

		LeapEventDispatchTask task = new LeapEventDispatchTask(leapEvent, channelId, transformedMsg,
				new LeapExchangeHeader(tenant, site, requestId, retryStatus));
		if (tracker != null)
			task.setTracker(tracker);
		return task;
	}

	private ConfigurationContext getConfigurationContext(RequestContext rqCtx) {
		ConfigurationContext configContext = new ConfigurationContext(rqCtx);
		logger.debug("{} config context before return :{} ", LEAP_LOG_KEY, configContext);
		return configContext;
	}

	private DurableExecutorService getDurableDispatcherExecutorService(RequestContext requestContext) {
		String tenantId = requestContext.getTenantId();
		String siteId = requestContext.getSiteId();
		logger.debug("{} getting DurableDispatcherExecutor for {}", LEAP_LOG_KEY, requestContext);
		DurableExecutorService dispExecutor = DataGridService.getDataGridInstance()
				.getDurableDispatcherExecutor(tenantId + "-" + siteId);
		return dispExecutor;
	}

	private Serializable transformLeapEvent(String tranformationBeanFQCN, String xslname, String channelId,
			String xsltAsString, LeapEvent leapEvent, String tenant, String site, String requestId,
			String eventStoreKey) throws LeapEventTransformationException {
		ILeapEventTransformer transformer;
		Serializable transformedMsg = null;
		try {
			String value = "EventId=" + leapEvent.getId() + "--channelId=" + channelId + "--transformationBean="
					+ tranformationBeanFQCN + ", xslName : " + xslname;
			logger.debug("{} {} ", LEAP_LOG_KEY, value);

			// decide which transformation to call
			if (tranformationBeanFQCN != null) {
				logger.debug("{} tansformation bean is not null", LEAP_LOG_KEY);
				// Transform the Event msg
				transformer = getTransformerInstance(tranformationBeanFQCN);
			} else if (xslname != null) {
				logger.debug("{} xsl name is not null", LEAP_LOG_KEY);
				transformer = getXmlXsltTransformerInstance(xslname, xsltAsString);
			} else {
				logger.debug("{} its for json", LEAP_LOG_KEY);
				transformer = getJSONTransformerInstance();
			}

			transformedMsg = transformer.transformEvent(leapEvent);

		} catch (BeanDependencyResolveException | LeapEventTransformationException insExp) {
			try {
				String failureMsg = insExp.getMessage();
				JSONObject failureJson = new JSONObject();
				EventTrackerUtil.setFailureJSONString(failureJson, insExp, failureMsg);
				logger.error("{} Failed to publish event for eventId : {}", LEAP_LOG_KEY, leapEvent.getId());
				eventDispatcherTrackerService.updateEventStatus(tenant, site, requestId, eventStoreKey,
						EventTrackerTableConstants.STATUS_FAILED, leapEvent.getId(), true, failureJson.toString(), true,
						false, leapEvent.getDispatchChannelId());
			} catch (Exception e) {
				throw new LeapEventTransformationException(e.getMessage(), e.getCause());
			}
		}

		return transformedMsg;
	}

	/**
	 * This method is to generate XSL-XSLT transformer Instance
	 * 
	 * @param xslname
	 * @param xsltAsString
	 * @return
	 */
	private ILeapEventTransformer getXmlXsltTransformerInstance(String xslname, String xsltAsString) {
		logger.debug("{} inside getXmlXsltTransformationInstance ", LEAP_LOG_KEY);
		ILeapEventTransformer transformer = new LeapEventXmlTransformer(xslname, xsltAsString);
		return transformer;
	}

	private ILeapEventTransformer getJSONTransformerInstance() {
		logger.debug("{} inside getJSONTransformerInstance ", LEAP_LOG_KEY);
		ILeapEventTransformer transformer = new GenericLeapEventJsonTransformer();

		return transformer;
	}

	private ILeapEventTransformer getTransformerInstance(String fqcn) throws BeanDependencyResolveException {
		logger.debug("{} inside getTransformerInstance : {}", LEAP_LOG_KEY, fqcn);
		ILeapEventTransformer transformer = null;
		logger.debug("{} OSGIEnvironmentHelper.isOSGIEnabled : {}", LEAP_LOG_KEY, OSGIEnvironmentHelper.isOSGIEnabled);
		if (OSGIEnvironmentHelper.isOSGIEnabled) {
			BeanResolutionHelper beanResolutionHelper = new BeanResolutionHelper();
			try {
				transformer = (ILeapEventTransformer) beanResolutionHelper
						.resolveBean(ILeapEventTransformer.class.getName(), fqcn);
			} catch (InvalidSyntaxException e) {
				logger.error("{} Unable to Load/instantiate CustomEventTransformer={}", LEAP_LOG_KEY, fqcn);
				throw new BeanDependencyResolveException("Unable to Load/instantiate CustomEventTransformer=" + fqcn,
						e);
			}
		} else {

			IBeanDependencyResolver beanResolver = BeanDependencyResolverFactory.getBeanDependencyResolver();

			if (fqcn.equalsIgnoreCase(
					"com.attunedlabs.eventframework.dispatcher.transformer.GenericLeapEventJsonTransformer")) {
				return new GenericLeapEventJsonTransformer();
			}
			transformer = (ILeapEventTransformer) beanResolver.getBeanInstance(ILeapEventTransformer.class, fqcn);
		}
		return transformer;
	}

}
