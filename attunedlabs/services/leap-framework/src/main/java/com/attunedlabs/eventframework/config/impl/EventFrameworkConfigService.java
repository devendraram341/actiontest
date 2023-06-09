package com.attunedlabs.eventframework.config.impl;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.eventframework.camel.eventproducer.AbstractCamelEventBuilder;
import com.attunedlabs.eventframework.camel.eventproducer.ServiceCompletionFailureEventBuilder;
import com.attunedlabs.eventframework.camel.eventproducer.ServiceCompletionSuccessEventBuilder;
import com.attunedlabs.eventframework.camel.eventproducer.ServicePerformanceLoggingEventBuilder;
import com.attunedlabs.eventframework.config.EventFrameworkConfigurationException;
import com.attunedlabs.eventframework.config.IEventFrameworkConfigService;
import com.attunedlabs.eventframework.jaxb.CamelEventProducer;
import com.attunedlabs.eventframework.jaxb.DispatchChannel;
import com.attunedlabs.eventframework.jaxb.Event;
import com.attunedlabs.eventframework.jaxb.JMSSubscribeEvent;
import com.attunedlabs.eventframework.jaxb.SubscribeEvent;
import com.attunedlabs.eventframework.jaxb.SystemEvent;

public class EventFrameworkConfigService implements IEventFrameworkConfigService {
	final Logger logger = LoggerFactory.getLogger(IEventFrameworkConfigService.class);
	static final String SERVICE_COMPLETION_SUCCESS = "success";
	static final String SERVICE_COMPLETION_FAILURE = "failure";
	static final String SERVICE_COMPLETION_ALL = "all";

	private EventFrameworkConfigHelper configHelper = new EventFrameworkConfigHelper();

	public void addEventFrameworkConfiguration(ConfigurationContext configContext, Event evtFwkConfig)
			throws EventFrameworkConfigurationException {
		configHelper.addEventFrameworkConfiguration(configContext, evtFwkConfig);
	}// end of method

	public void addEventFrameworkConfiguration(ConfigurationContext configContext, SystemEvent sysevtFwkConfig)
			throws EventFrameworkConfigurationException {
		configHelper.addEventFrameworkConfiguration(configContext, sysevtFwkConfig);
	}

	public void addEventFrameworkConfiguration(ConfigurationContext configContext, DispatchChannel dispatchChannelConfig)
			throws EventFrameworkConfigurationException {
		configHelper.addEventFrameworkConfiguration(configContext, dispatchChannelConfig);
	}

	public void addEventFrameworkConfiguration(ConfigurationContext configContext,
			SubscribeEvent eventSubscriptionConfig) throws EventFrameworkConfigurationException {
		configHelper.addEventFrameworkConfiguration(configContext, eventSubscriptionConfig);
	}

	public Event getEventConfiguration(ConfigurationContext configContext, String forEventId)
			throws EventFrameworkConfigurationException {
		return configHelper.getEventConfiguration(configContext, forEventId);
	}

	public DispatchChannel getDispatchChannelConfiguration(ConfigurationContext configContext, String dispatchChannelId)
			throws EventFrameworkConfigurationException {
		return configHelper.getDispatchChannelConfiguration(configContext, dispatchChannelId);
	}

	public SubscribeEvent getEventSubscriptionConfiguration(ConfigurationContext configContext,
			String eventSubscriptionId) throws EventFrameworkConfigurationException {
		return configHelper.getEventSubscriptionConfiguration(configContext, eventSubscriptionId);
	}

	public JMSSubscribeEvent getJMSEventSubscriptionConfiguration(ConfigurationContext configContext,
			String eventSubscriptionId) throws EventFrameworkConfigurationException {
		return (JMSSubscribeEvent) configHelper.getJMSEventSubscriptionConfiguration(configContext,
				eventSubscriptionId);
	}

	@Override
	public String getAllSubscriberTopicNames() throws EventFrameworkConfigurationException {
		return configHelper.getAllSubscriberTopicNames();
	}

	@Override
	public String getSubscriptionTopicsbySubscriptionId(String subscriptionId)
			throws EventFrameworkConfigurationException {
		return configHelper.getSubscriptionTopicsbySubscriptionId(subscriptionId);
	}

	@Override
	public Set<String> getAllTopicSubscribersbyTopicName(String topicName) throws EventFrameworkConfigurationException {
		return configHelper.getAllTopicSubscribersbyTopicName(topicName);
	}

	public CamelEventProducer getEventProducerForBean(ConfigurationContext configContext, String serviceName,
			String beanFQCN) throws EventFrameworkConfigurationException {
		return configHelper.getEventProducerForBean(configContext, serviceName, beanFQCN);

	}

	public List<CamelEventProducer> getEventProducerForServiceSuccessCompletion(ConfigurationContext configContext,
			String serviceName) throws EventFrameworkConfigurationException {
		return configHelper.getEventProducerForServiceSuccessCompletion(configContext, serviceName,
				SERVICE_COMPLETION_SUCCESS);
	}

	public List<CamelEventProducer> getEventProducerForServiceFailedCompletion(ConfigurationContext configContext,
			String serviceName) throws EventFrameworkConfigurationException {
		return configHelper.getEventProducerForServiceSuccessCompletion(configContext, serviceName,
				SERVICE_COMPLETION_FAILURE);
	}

	public List<CamelEventProducer> getEventProducerForServiceFailedAndSucessCompletion(
			ConfigurationContext configContext, String serviceName) throws EventFrameworkConfigurationException {
		return configHelper.getEventProducerForServiceSuccessCompletion(configContext, serviceName,
				SERVICE_COMPLETION_ALL);
	}

	public List<Event> getEventConfigProducerForServiceSuccessCompletion(ConfigurationContext configContext,
			String serviceName) throws EventFrameworkConfigurationException {
		return configHelper.getEventConfigProducerForServiceSuccessCompletion(configContext, serviceName,
				SERVICE_COMPLETION_SUCCESS);
	}

	public List<Event> getEventConfigProducerForServiceFailedCompletion(ConfigurationContext configContext,
			String serviceName) throws EventFrameworkConfigurationException {
		return configHelper.getEventConfigProducerForServiceSuccessCompletion(configContext, serviceName,
				SERVICE_COMPLETION_FAILURE);
	}

	public List<Event> getEventConfigProducerForServiceFailedAndSucessCompletion(ConfigurationContext configContext,
			String serviceName) throws EventFrameworkConfigurationException {
		return configHelper.getEventConfigProducerForServiceSuccessCompletion(configContext, serviceName,
				SERVICE_COMPLETION_ALL);
	}

	public Event getEventConfigProducerForBean(ConfigurationContext configContext, String serviceName, String beanFQCN)
			throws EventFrameworkConfigurationException {
		return configHelper.getEventConfigProducerForBean(configContext, serviceName, beanFQCN);
	}

	public SystemEvent getSystemEventConfiguration(ConfigurationContext configContext, String systemEventId)
			throws EventFrameworkConfigurationException {
		return configHelper.getSystemEventConfiguration(configContext, systemEventId);
	}

	/**
	 * Standard EventBuilder for building ServiceCompletionSuccessEvent. Builder
	 * is configured once for each tenant
	 * 
	 * @param tenantId
	 * @return AbstractCamelEventBuilder
	 */
	public AbstractCamelEventBuilder getServiceCompletionSuccessEventBuilder(ConfigurationContext configContext) {
		return new ServiceCompletionSuccessEventBuilder();
	}

	/**
	 * Standard EventBuilder for building ServiceCompletionFailureEvent. Builder
	 * is configured once for each tenant
	 * 
	 * @param tenantId
	 * @return AbstractCamelEventBuilder
	 */
	public AbstractCamelEventBuilder getServiceCompletionFailureEventBuilder(ConfigurationContext configContext) {
		return new ServiceCompletionFailureEventBuilder();
	}

	/**
	 * Standard EventBuilder for building ServicePerformanceLoggingEventBuilder.
	 * Builder is configured once for each tenant
	 * 
	 * @param tenantId
	 * @return AbstractCamelEventBuilder
	 */
	public AbstractCamelEventBuilder getServicePerformanceLoggingEventBuilder(ConfigurationContext configContext) {
		return new ServicePerformanceLoggingEventBuilder();
	}

	public boolean changeStatusOfDispactherChannelConfiguration(ConfigurationContext configurationContext,
			String dispatchChannelId, boolean isEnable) throws EventFrameworkConfigurationException {
		return configHelper.changeStatusOfDispactherChannelConfiguration(configurationContext, dispatchChannelId,
				isEnable);

	}

	public boolean changeStatusOfEventSubscriptionConfiguration(ConfigurationContext configurationContext,
			String eventSubscriptionId, boolean isEnable) throws EventFrameworkConfigurationException {
		return configHelper.changeStatusOfEventSubscriptionConfiguraion(configurationContext, eventSubscriptionId,
				isEnable);

	}

	public boolean changeStatusOfEventSubscriber(ConfigurationContext configurationContext, String eventSubscriptionId,
			String subscriberId, boolean isEnable) throws EventFrameworkConfigurationException {
		return configHelper.changeStatusOfEventSubscriber(configurationContext, eventSubscriptionId, subscriberId,
				isEnable);

	}

	public boolean changeStatusOfSystemEventConfiguration(ConfigurationContext configurationContext,
			String systemEventId, boolean isEnable) throws EventFrameworkConfigurationException {
		return configHelper.changeStatusOfSystemEventConfiguration(configurationContext, systemEventId, isEnable);
	}

	@Override
	public boolean deleteDipatcherChannelConfiguration(ConfigurationContext configContext, String dispatchChannelId)
			throws EventFrameworkConfigurationException {
		return configHelper.deleteDipatcherChannelConfiguration(configContext, dispatchChannelId);
	}

	@Override
	public boolean deleteSystemEventConfiguration(ConfigurationContext configContext, String systemEventId)
			throws EventFrameworkConfigurationException {
		return configHelper.deleteSystemEventConfiguration(configContext, systemEventId);
	}

	@Override
	public boolean deleteEventSubscriptionConfiguration(ConfigurationContext configContext, String subscriptionEventId)
			throws EventFrameworkConfigurationException {
		return configHelper.deleteEventSubscriptionConfiguration(configContext, subscriptionEventId);
	}

	@Override
	public boolean changeStatusOfEventConfiguration(ConfigurationContext configContext, String eventId,
			boolean isEnable) throws EventFrameworkConfigurationException {

		return configHelper.changeStatusOfEventConfiguration(configContext, eventId, isEnable);
	}

	@Override
	public boolean deleteEventConfiguration(ConfigurationContext configContext, String eventId)
			throws EventFrameworkConfigurationException {

		return configHelper.deleteEventConfiguration(configContext, eventId);
	}

	@Override
	public boolean reloadSystemEventCacheObject(RequestContext requestContext, String systemEventId)
			throws EventFrameworkConfigurationException {
		return configHelper.reloadSystemEventCacheObject(requestContext, systemEventId);

	}

	@Override
	public boolean reloadEventCacheObject(RequestContext requestContext, String eventId)
			throws EventFrameworkConfigurationException {
		return configHelper.reloadEventCacheObject(requestContext, eventId);
	}

	@Override
	public boolean reloadDispatchChannelCacheObject(RequestContext requestContext, String dispatchChannelId)
			throws EventFrameworkConfigurationException {
		return configHelper.reloadDispatchChannelCacheObject(requestContext, dispatchChannelId);
	}

	@Override
	public boolean reloadSubscriptionEventCacheObject(RequestContext requestContext, String dispatchChannelId)
			throws EventFrameworkConfigurationException {
		return configHelper.reloadSubscriptionEventCacheObject(requestContext, dispatchChannelId);
	}

	@Override
	public void addEventFrameworkConfiguration(ConfigurationContext configContext,
			JMSSubscribeEvent jmsEventSubscriptionConfig) throws EventFrameworkConfigurationException {
		configHelper.addEventFrameworkConfiguration(configContext, jmsEventSubscriptionConfig);
	}

	@Override
	public String getSubscriptionQueuebySubscriptionId(String subscriptionId)
			throws EventFrameworkConfigurationException {
		return configHelper.getSubscriptionQueuebySubscriptionId(subscriptionId);
	}

}