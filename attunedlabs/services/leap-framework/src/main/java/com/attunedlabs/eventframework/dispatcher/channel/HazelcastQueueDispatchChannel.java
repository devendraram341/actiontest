package com.attunedlabs.eventframework.dispatcher.channel;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;

import java.io.Serializable;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.core.datagrid.DataGridService;
import com.attunedlabs.eventframework.config.EventFrameworkConfigurationException;
import com.attunedlabs.eventframework.config.IEventFrameworkConfigService;
import com.attunedlabs.eventframework.config.impl.EventFrameworkConfigService;
import com.attunedlabs.eventframework.dispatchchannel.exception.MessageDispatchingException;
import com.attunedlabs.eventframework.dispatchchannel.exception.NonRetryableMessageDispatchingException;
import com.attunedlabs.eventframework.dispatchchannel.exception.RetryableMessageDispatchingException;
import com.attunedlabs.eventframework.jaxb.SubscribeEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;

public class HazelcastQueueDispatchChannel extends AbstractDispatchChannel {
	final static Logger logger = LoggerFactory.getLogger(HazelcastQueueDispatchChannel.class);
	private String hcQueueName;

	public HazelcastQueueDispatchChannel() {

	}

	public HazelcastQueueDispatchChannel(String channeljsonconfig) throws DispatchChannelInitializationException {
		super.channeljsonconfig = channeljsonconfig;
		initializeFromConfig();
	}

	public void dispatchMsg(Serializable msg, RequestContext rqCtx, String eventId) throws MessageDispatchingException {
		try {
			String methodName = "dispatchMsg";
			logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
			HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
			logger.trace("{} Got Hazelcast Instance", LEAP_LOG_KEY);
			ConfigurationContext configContext = null;

			if (rqCtx.getVendor() != null) {
				configContext = new ConfigurationContext(rqCtx.getTenantId(), rqCtx.getSiteId(),
						rqCtx.getFeatureGroup(), rqCtx.getFeatureName(), rqCtx.getImplementationName(),
						rqCtx.getVendor(), rqCtx.getVersion());
			} else
				configContext = new ConfigurationContext(rqCtx.getTenantId(), rqCtx.getSiteId(), null, null, null);
			IEventFrameworkConfigService evtFwkConfiService = new EventFrameworkConfigService();
			try {
				// check event subscription for
				SubscribeEvent eventSubscription = evtFwkConfiService.getEventSubscriptionConfiguration(configContext,
						eventId);
				if (eventSubscription.isIsEnabled()) {
					IQueue<Object> queue = hazelcastInstance
							.getQueue(eventId.trim() + "-" + eventSubscription.getSubscriptionId().trim());
					logger.trace("{} Got Queue from Hazelcast Instance", LEAP_LOG_KEY);
					queue.put(msg);
					logger.trace("{} end msg added to queue={}", LEAP_LOG_KEY, hcQueueName);
				} // end of for
			} catch (EventFrameworkConfigurationException e) {
				throw new NonRetryableMessageDispatchingException("Unable to get the EventSubscription for event : "
						+ eventId + ", configuration context : " + configContext);
			}
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		} catch (InterruptedException e) {
			throw new RetryableMessageDispatchingException(
					"HazelcastQueueDispatchChannel Failed to dispatch eventMsg to Hazelcast queue {" + hcQueueName
							+ "}",
					e);
		}

	}// end of method

	@Override
	public void initializeFromConfig() throws DispatchChannelInitializationException {
		try {
			String methodName = "initializeFromConfig";
			logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
			parseConfiguration(this.channeljsonconfig);
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		} catch (ParseException e) {
			throw new DispatchChannelInitializationException(
					"Failed to Parse configuration for HazelcastQueueDispatch Channel", e);
		}

	}

	/**
	 * This method is to parse json configuration {queueName=}
	 * 
	 * @param channeljsonconfig
	 * @throws ParseException
	 */
	private void parseConfiguration(String channeljsonconfig) throws ParseException {
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(channeljsonconfig);
		JSONObject jsonObject = (JSONObject) obj;
		this.hcQueueName = (String) jsonObject.get("queueName");
	}
}
