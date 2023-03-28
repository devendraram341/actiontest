package com.attunedlabs.applicationservicehandlers.handler;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;
import java.util.concurrent.Future;

import org.apache.camel.Exchange;
import org.apache.kafka.clients.producer.BufferExhaustedException;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.errors.InterruptException;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.persistence.dao.LeapConstants;
import com.attunedlabs.config.util.LeapConfigUtil;
import com.attunedlabs.config.util.LeapDefaultConstants;
import com.attunedlabs.config.util.PropertiesConfigException;
import com.attunedlabs.eventframework.config.EventFrameworkConstants;
import com.attunedlabs.eventframework.dispatcher.channel.KafkaTopicDispatchChannel;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.LeapServiceContext;
import com.attunedlabs.leap.context.constant.LeapDataContextConstant;
import com.attunedlabs.leap.selflink.SelfLinkDataProcessor;
import com.attunedlabs.servicehandlers.AbstractServiceHandler;

public class SelfLinkDataProducer extends AbstractServiceHandler {

	private static String KAFKA_PRODUCER_CONFIGS = "globalAppDeploymentConfig.properties";
	final static Logger logger = LoggerFactory.getLogger(SelfLinkDataProducer.class);

	private static Properties props = new Properties();
	private String topicName;
	private KafkaProducer<String, Serializable> producer;
	private SelfLinkDataProcessor selfLinkDataProcessor = new SelfLinkDataProcessor();

	static {
		InputStream inputStream;
		try {
			inputStream = KafkaTopicDispatchChannel.class.getClassLoader().getResourceAsStream(KAFKA_PRODUCER_CONFIGS);
			props.load(inputStream);
		} catch (IOException e) {
		}
	}// ..end of static block to load the ProducerProperties

	@SuppressWarnings("unchecked")
	@Override
	public boolean initializeConfiguration(JSONObject jsonObject) {
		String methodName = "initializeConfiguration";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		try {
			topicName = (String) jsonObject.getOrDefault("topic", "selflinkdata");
			Properties propsKafka = new Properties();
			propsKafka.setProperty("bootstrap.servers", (String) jsonObject.get("brokerconfig"));
			propsKafka.put("zookeeper.connect", LeapConfigUtil.getGlobalPropertyValue(LeapConstants.ZOOKEEPER_CONNECT,LeapDefaultConstants.DEFAULT_ZOOKEEPER_CONNECT));
			propsKafka.put("acks", LeapConfigUtil.getGlobalPropertyValue(LeapConstants.ACKS,LeapDefaultConstants.DEFAULT_ACKS));
			propsKafka.put("retries", LeapConfigUtil.getGlobalPropertyValue(LeapConstants.RETRIES,LeapDefaultConstants.DEFAULT_RETRIES));
			propsKafka.put("batch.size", LeapConfigUtil.getGlobalPropertyValue(LeapConstants.BATCH_SIZE,LeapDefaultConstants.DEFAULT_BATCH_SIZE));
			propsKafka.put("linger.ms", LeapConfigUtil.getGlobalPropertyValue(LeapConstants.LINGER_MS,LeapDefaultConstants.DEFAULT_LINGER_MS));
			propsKafka.put("buffer.memory", LeapConfigUtil.getGlobalPropertyValue(LeapConstants.BUFFER_MEMORY,LeapDefaultConstants.DEFAULT_BUFFER_MEMORY));
			propsKafka.put("key.serializer", LeapConfigUtil.getGlobalPropertyValue(LeapConstants.KEY_SERIALIZER,LeapDefaultConstants.DEFAULT_KEY_SERIALIZER));
			propsKafka.put("value.serializer", LeapConfigUtil.getGlobalPropertyValue(LeapConstants.VALUE_SERIALIZER,LeapDefaultConstants.DEFAULT_VALUE_SERIALIZER));
			propsKafka.put("producer.type", LeapConfigUtil.getGlobalPropertyValue(LeapConstants.PRODUCER_TYPE,LeapDefaultConstants.DEFAULT_PRODUCER_TYPE));
			propsKafka.put("buffer.size", LeapConfigUtil.getGlobalPropertyValue(LeapConstants.BUFFER_SIZE,LeapDefaultConstants.DEFAULT_BUFFER_SIZE));
			propsKafka.put("reconnect.interval", LeapConfigUtil.getGlobalPropertyValue(LeapConstants.RECONNECT_INTERVAL,LeapDefaultConstants.DEFAULT_RECONNECT_INTERVAL));
			propsKafka.put("request.required.acks", LeapConfigUtil.getGlobalPropertyValue(LeapConstants.REQUEST_REQUIRED_ACKS,LeapDefaultConstants.DEFAULT_REQUEST_REQUIRED_ACKS));
			propsKafka.put("rebalance.retries.max", LeapConfigUtil.getGlobalPropertyValue(LeapConstants.REBALANCE_RETRIES_MAX,LeapDefaultConstants.DEFAULT_REBALANCE_RETRIES_MAX));
			propsKafka.put("mirror.consumer.numthreads",LeapConfigUtil.getGlobalPropertyValue(LeapConstants.MIRROR_CONSUMER_NUMTHREADS,LeapDefaultConstants.DEFAULT_MIRROR_CONSUMER_NUMTHREADS));
			propsKafka.put("metadata.max.age.ms", LeapConfigUtil.getGlobalPropertyValue(LeapConstants.METADATA_MAX_AGE_MS,LeapDefaultConstants.DEFAULT_METADATA_MAX_AGE_MS));
			String globalPropertyValue = LeapConfigUtil.getGlobalPropertyValue(LeapConstants.SECURITY_PROTOCOL,LeapDefaultConstants.DEFAULT_SECURITY_PROTOCOL);
			if(globalPropertyValue != null) {
				propsKafka.put("security.protocol", LeapConfigUtil.getGlobalPropertyValue(LeapConstants.SECURITY_PROTOCOL,LeapDefaultConstants.DEFAULT_SECURITY_PROTOCOL));
				propsKafka.put("ssl.truststore.location", LeapConfigUtil.getGlobalPropertyValue(LeapConstants.SSL_TRUSTSTORE_LOCATION,LeapDefaultConstants.DEFAULT_SSL_TRUSTSTORE_LOCATION));
				propsKafka.put("ssl.truststore.password", LeapConfigUtil.getGlobalPropertyValue(LeapConstants.SSL_TRUSTSTORE_PASSWORD,LeapDefaultConstants.DEFAULT_SSL_TRUSTSTORE_PASSWORD));
			}	

			this.producer = new KafkaProducer<>(propsKafka);

		} catch (PropertiesConfigException e) {
			logger.error("{} Problem in getting the deloyment config file {}", LEAP_LOG_KEY, methodName);
		}
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void postService(Exchange exchange) {
		String methodName = "postService";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		long startTime = System.currentTimeMillis();
		JSONObject json = new JSONObject();
		try {
			LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn()
					.getHeader(LeapDataContextConstant.LEAP_DATA_CONTEXT);
			LeapServiceContext leapServiceContext = leapDataContext.getServiceDataContext();
			try {
				selfLinkDataProcessor.process(exchange);
			} catch (Exception e) {
				e.printStackTrace();
			}

			org.json.JSONObject requestHeaders = leapDataContext.getRequestHeaders(LeapDataContextConstant.HEADER);
			json.put(EventFrameworkConstants.EVENT_ID_KEY, "SELF_LINK_EVT");
			JSONObject header = new JSONObject();
			header.put(LeapDataContextConstant.TENANTID, requestHeaders.get(LeapDataContextConstant.TENANTID));
			header.put(LeapDataContextConstant.SITEID, requestHeaders.get(LeapDataContextConstant.SITEID));
			header.put("requestId", leapServiceContext.getRequestUUID());
			json.put(EventFrameworkConstants.METADATA_KEY, header);
			json.put(EventFrameworkConstants.OBJECT_KEY,
					exchange.getIn().getHeader("selfLink", org.json.JSONObject.class));

			try {
				Future<RecordMetadata> future = producer.send(
						new ProducerRecord<String, Serializable>(topicName, "selfLink", json.toString()),
						new Callback() {
							public void onCompletion(RecordMetadata metadata, Exception e) {
								if (e != null) {
									logger.error("{} Unable to produce to the topic..{} ", LEAP_LOG_KEY,
											e.getMessage());
								}
							}
						});
			} catch (InterruptException | BufferExhaustedException e) {
				logger.error("{} Failed to produce to the topic..{} ", LEAP_LOG_KEY, e.getMessage());
			} catch (Exception e) {
				logger.error("{} Failed to produce to the topic..{} ", LEAP_LOG_KEY, e.getMessage());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		long stopTime = System.currentTimeMillis();
		logger.debug("{} postService time ms :{}", LEAP_LOG_KEY, (stopTime - startTime));
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);

	}

}
