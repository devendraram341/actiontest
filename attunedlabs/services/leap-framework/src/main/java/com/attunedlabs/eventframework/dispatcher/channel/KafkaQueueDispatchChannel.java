package com.attunedlabs.eventframework.dispatcher.channel;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.producer.BufferExhaustedException;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.errors.InterruptException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.RequestContext;
import com.attunedlabs.config.persistence.dao.LeapConstants;
import com.attunedlabs.config.util.LeapConfigUtil;
import com.attunedlabs.config.util.LeapDefaultConstants;
import com.attunedlabs.config.util.PropertiesConfigException;
import com.attunedlabs.eventframework.abstractbean.util.CassandraUtil;
import com.attunedlabs.eventframework.dispatchchannel.exception.MessageDispatchingException;
import com.attunedlabs.eventframework.dispatchchannel.exception.NonRetryableMessageDispatchingException;
import com.attunedlabs.eventframework.dispatchchannel.exception.RetryableMessageDispatchingException;

public class KafkaQueueDispatchChannel extends AbstractDispatchChannel {

	private static Properties props = null;
	final static Logger log = LoggerFactory.getLogger(KafkaQueueDispatchChannel.class);
	private String bootstrapservers;
	private String queue;
	private Boolean isTenantAware;
	private KafkaProducer<String, Serializable> producer;
	static {
		try {
			props = LeapConfigUtil.getGlobalAppDeploymentConfigProperties();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}// ..end of static block to load the ProducerProperties

	public KafkaQueueDispatchChannel(String channeljsonconfig) throws DispatchChannelInitializationException {
		this.channeljsonconfig = channeljsonconfig;
		initializeFromConfig();
	}

	public KafkaQueueDispatchChannel() {
	}

	/**
	 * 
	 * Method which is called for dispatching the event message to a Kafka topic
	 * "tenant-topic" - for tenantAware, else the topic name specified on json
	 * string. A Random-UUID is generated for each messages appended with the
	 * EventId.
	 * 
	 * @param msg
	 * @param reqContext
	 * @param evendId
	 * @throws MessageDispatchingException
	 * 
	 **/
	@Override
	public void dispatchMsg(Serializable msg, RequestContext reqContext, String evendId)
			throws MessageDispatchingException {
		String methodName = "dispatchMsg";
		log.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		String topicName = "";
		if (isTenantAware) {
			topicName = reqContext.getTenantId() + "-" + queue;
		} else {
			topicName = queue;
		}
		log.debug("{} Event message incoming:{} EventID:{}", LEAP_LOG_KEY, msg, evendId);
		List<ProducerRecord<String, Serializable>> buffer = new ArrayList<ProducerRecord<String, Serializable>>();
		ProducerRecord<String, Serializable> producerRecord = new ProducerRecord<String, Serializable>(topicName, 0,
				evendId + "-" + generateRecKey(), msg);
		buffer.add(producerRecord);
		log.debug("{} Event bufferSize:{} ", LEAP_LOG_KEY, buffer.size());
		AtomicBoolean booleanFlag = new AtomicBoolean(false);
		StringBuffer st = new StringBuffer();
		for (ProducerRecord<String, Serializable> record : buffer) {
			try {
				Future<RecordMetadata> future = producer.send(record, new Callback() {
					public void onCompletion(RecordMetadata metadata, Exception e) {
						if (e != null) {
							log.error("{} Unable to produce to the topic.. {}", LEAP_LOG_KEY, e.getMessage());
							booleanFlag.set(true);
							st.append("Unable to produce to the topic.. " + e.getMessage());
						}
					}
				});
			} catch (InterruptException | BufferExhaustedException e) {
				throw new RetryableMessageDispatchingException("Failed to produce to the topic.. " + e.getMessage());
			} catch (Exception e) {
				throw new NonRetryableMessageDispatchingException("Failed to produce to the topic.. " + e.getMessage());
			} finally {
				producer.close();
			}
			if (booleanFlag.get())
				throw new RetryableMessageDispatchingException("Failed to produce to the topic.. " + st);
			log.debug("{} -Data - {}", LEAP_LOG_KEY, record.toString());
		}
		log.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}// ..end
		// of
		// the
		// method

	/**
	 * to initialize few values before startup
	 * 
	 * @throws DispatchChannelInitializationException
	 */
	@Override
	public void initializeFromConfig() throws DispatchChannelInitializationException {
		String methodName = "initializeFromConfig";
		log.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		parseConfiguration(channeljsonconfig);
		try {
			Properties propsKafka = new Properties();
			propsKafka.setProperty("bootstrap.servers", bootstrapservers);
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
			log.error("Problem in getting the deloyment config  ", e);
		}

	}// ..end of the method

	/**
	 * This method is to parse json configuration eg:
	 * {"bootstrapservers":"host:port","queue":"topicName", "isTenantAware":true}
	 * 
	 * @param channeljsonconfig
	 * @throws ParseException
	 */
	private void parseConfiguration(String channeljsonconfig) {
		JSONParser parser = new JSONParser();
		Object obj = null;
		try {
			obj = parser.parse(channeljsonconfig);
		} catch (ParseException e) {

		}
		JSONObject jsonObject = (JSONObject) obj;
		this.bootstrapservers = (String) jsonObject.get("bootstrapservers");
		this.queue = (String) jsonObject.get("queue");
		this.isTenantAware = (Boolean) (jsonObject.get("isTenantAware"));
		log.debug("Queue configured: " + channeljsonconfig);
	}// .. end of the method

	/**
	 * generate random uuid
	 * 
	 * @return
	 */
	private String generateRecKey() {
		return UUID.randomUUID().toString();
	}// ..end of the method

}
