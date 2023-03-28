package com.attunedlabs.eventframework.dispatcher.channel;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;

import java.io.Serializable;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.RequestContext;
import com.attunedlabs.core.datagrid.DataGridService;
import com.attunedlabs.eventframework.dispatchchannel.exception.MessageDispatchingException;
import com.attunedlabs.eventframework.dispatchchannel.exception.RetryableMessageDispatchingException;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;

public class HazelcastTopicDispatchChannel extends AbstractDispatchChannel {
	final static Logger logger = LoggerFactory.getLogger(HazelcastTopicDispatchChannel.class);
	private String hcQueueName;

	public HazelcastTopicDispatchChannel() {

	}

	public HazelcastTopicDispatchChannel(String channeljsonconfig) throws DispatchChannelInitializationException {
		super.channeljsonconfig = channeljsonconfig;
		initializeFromConfig();
	}

	public void dispatchMsg(Serializable msg, RequestContext requestContext, String eventId)
			throws MessageDispatchingException {
		try {
			String methodName = "dispatchMsg";
			logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
			HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
			logger.trace("{} Got Hazelcast Instance", LEAP_LOG_KEY);
			ITopic<Object> topic = hazelcastInstance.getTopic(hcQueueName);
			logger.trace("{} Got Queue from Hazelcast Instance", LEAP_LOG_KEY);
			topic.publish(msg);
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		} catch (Exception e) {
			throw new RetryableMessageDispatchingException(
					"HazelcastQueueDispatchChannel Failed to dispatch eventMsg to Hazelcast queue {" + hcQueueName
							+ "}",
					e);
		}

	}

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
