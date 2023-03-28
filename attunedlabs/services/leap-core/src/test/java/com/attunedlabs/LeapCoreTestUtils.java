package com.attunedlabs;

import static com.attunedlabs.LeapCoreTestConstant.*;

import java.util.HashMap;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.jms.JmsBinding;
import org.apache.camel.component.jms.JmsMessage;
import org.apache.camel.component.kafka.DefaultKafkaManualCommit;
import org.apache.camel.component.kafka.KafkaManualCommit;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.json.JSONObject;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import com.attunedlabs.baseroute.util.BaseRouteTestUtil;
import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.eventframework.jaxb.EventFramework;
import com.attunedlabs.eventframework.jaxb.JMSSubscribeEvent;
import com.attunedlabs.eventframework.jaxb.SubscribeEvent;
import com.attunedlabs.eventsubscription.abstractretrystrategy.AbstractSubscriptionRetryStrategy;
import com.attunedlabs.eventsubscription.defaultretrystrategy.JMSLeapDefaultRetryStrategy;
import com.attunedlabs.eventsubscription.defaultretrystrategy.LeapDefaultRetryStrategy;
import com.attunedlabs.eventsubscription.exception.ConfigurationValidationFailedException;
import com.attunedlabs.eventsubscription.util.SubscriptionUtil;
import com.attunedlabs.eventsubscriptiontracker.EventSubscriptionTracker;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.LeapServiceContext;
import com.attunedlabs.leap.context.bean.InitialLeapDataContextElement;
import com.attunedlabs.leap.context.bean.LeapData;
import com.attunedlabs.leap.context.bean.LeapDataContextElement;
import com.attunedlabs.leap.context.bean.LeapJSONResultSet;
import com.attunedlabs.leap.context.bean.LeapResultSet;
import com.attunedlabs.leap.context.exception.LeapDataContextInitialzerException;

public class LeapCoreTestUtils {

	private static Session session;
	public static Connection connection = null;

	/**
	 * This method use for addContextElement with the required header and push to
	 * LDC with #header tag.
	 * 
	 * @param leapDataCtx
	 */
	public static void addContextElementWithSetRequestHeader(LeapDataContext leapDataCtx) {
		setServiceContext(leapDataCtx);
		Map<String, Object> reaquiredHeadersMap = new HashMap<String, Object>();
		reaquiredHeadersMap.put(TENANTID, TEST_TENANT);
		reaquiredHeadersMap.put(SITEID, TEST_SITE);
		reaquiredHeadersMap.put(FEATURE_GROUP, TEST_FEATUREGROUP);
		reaquiredHeadersMap.put(FEATURE, TEST_FEATURE);
		reaquiredHeadersMap.put(SERVICENAME, TEST_SERVICE);
		reaquiredHeadersMap.put(REQUEST_METHOD, HTTP_POST.toLowerCase());

		Map<String, Object> privateHeadersMap = new HashMap<String, Object>();
		privateHeadersMap.put("privateTest", "PrivateTestData");

		InitialLeapDataContextElement initialRequestHeaderElement = new InitialLeapDataContextElement();
		initialRequestHeaderElement.setApiVersion("1.0");
		initialRequestHeaderElement.setContext("contextString");
		initialRequestHeaderElement.setData(getLeapDataWithOutLeapJson());
		initialRequestHeaderElement.setRequestHeaderElement(reaquiredHeadersMap);
		initialRequestHeaderElement.setPrivateHeaderElement(privateHeadersMap);

		LeapDataContextElement leapDataContextElement = new LeapDataContextElement(TAG_NAME_HEADER,
				initialRequestHeaderElement);
		leapDataCtx.addContextElement(leapDataContextElement, TAG_NAME_HEADER);
	}

	/**
	 * This method use for addContextElemnt with required LeapDataContextElement and
	 * push to LDC with #leap_initial tag.
	 * 
	 * @param leapDataCtx
	 */
	public static void addContextElementForEntryRoute(LeapDataContext leapDataCtx) {

		setServiceContext(leapDataCtx);
		InitialLeapDataContextElement initialRequestHeaderElement = new InitialLeapDataContextElement();
		initialRequestHeaderElement.setApiVersion("1.0");
		initialRequestHeaderElement.setContext("contextString");
		initialRequestHeaderElement.setData(getLeapData());
		LeapDataContextElement leapDataContextElement = new LeapDataContextElement(TAG_NAME_LEAP_INITIAL,
				initialRequestHeaderElement);
		leapDataCtx.addContextElement(leapDataContextElement, TAG_NAME_LEAP_INITIAL);
	}

	/**
	 * this method use for addConetxtElement with jsonObject and Push to LDC with
	 * #leap_initial Tag.
	 * 
	 * @param leapDataCtx
	 */
	public static void addContextElementWithLeapTag(LeapDataContext leapDataCtx) {

		setServiceContext(leapDataCtx);
		JSONObject jsonObject = new JSONObject(DEMO_JSON_DATA);
		try {
			leapDataCtx.addContextElement(jsonObject, "TestKind", TAG_NAME_LEAP_INITIAL, null);
		} catch (LeapDataContextInitialzerException e) {
			e.printStackTrace();
		}
	}

	/**
	 * this method use for addContextElement with LeapJSonObject and push to LDC
	 * with external Tag.
	 * 
	 * @param leapDataCtx
	 */
	public static void addContextElementWithTag(LeapDataContext leapDataCtx) {

		setServiceContext(leapDataCtx);
		JSONObject jsonObject = new JSONObject(DEMO_LEAP_DATA);
		try {
			leapDataCtx.addContextElement(jsonObject, "TestKind", TAG_NAME, null);
		} catch (LeapDataContextInitialzerException e) {
			e.printStackTrace();
		}
	}

	/**
	 * this method use for setRequestHeader and setServiceName is testService
	 * 
	 * @param leapDataCtx
	 */
	public static void setRequestHeaderWithoutPipeLine(LeapDataContext leapDataCtx) {
		LeapServiceContext serviceDataContext = leapDataCtx.getServiceDataContext(TEST_TENANT, TEST_SITE,
				TEST_FEATUREGROUP, TEST_FEATURE);
		serviceDataContext.initializeLeapRuntimeServiceContext(TEST_SERVICE);
		serviceDataContext.setRequestUUID("123456789");
		serviceDataContext.setRequestContext(new RequestContext(TEST_TENANT, TEST_SITE, TEST_FEATUREGROUP, TEST_FEATURE,
				TEST_IMPL, TEST_VENDOR, TEST_VERSION));
		serviceDataContext.SetRunningContextServiceName(TEST_SERVICE);
	}

	/**
	 * this method use for setRequestHeader and setServiceName is executePipeline
	 * 
	 * @param leapDataCtx
	 */
	public static void setRequestHeaderWithPipeLine(LeapDataContext leapDataCtx) {
		LeapServiceContext serviceDataContext = leapDataCtx.getServiceDataContext(TEST_TENANT, TEST_SITE,
				TEST_FEATUREGROUP, TEST_FEATURE);
		serviceDataContext.initializeLeapRuntimeServiceContext("executePipeline");
		serviceDataContext.setRequestUUID("123456789");
		serviceDataContext.setRequestContext(new RequestContext(TEST_TENANT, TEST_SITE, TEST_FEATUREGROUP, TEST_FEATURE,
				TEST_IMPL, TEST_VENDOR, TEST_VERSION));
		serviceDataContext.SetRunningContextServiceName("executePipeline");
	}

	/**
	 * this method set Request context and Return RequestContext.
	 * 
	 * @return
	 */
	public static RequestContext getRequestContext() {
		return new RequestContext(TEST_TENANT, TEST_SITE, TEST_FEATUREGROUP, TEST_FEATURE, TEST_IMPL, TEST_VENDOR,
				TEST_VERSION);
	}

	/**
	 * this method set Request context and Return RequestContext.
	 * 
	 * @return
	 */
	public static ConfigurationContext getConfigContext() {
		return new ConfigurationContext(TEST_TENANT, TEST_SITE, TEST_FEATUREGROUP, TEST_FEATURE, TEST_IMPL, TEST_VENDOR,
				TEST_VERSION);
	}

	/**
	 * this method use for setServiceContext data.
	 * 
	 * @param leapDataCtx
	 */
	public static void setServiceContext(LeapDataContext leapDataCtx) {

		LeapServiceContext serviceDataContext = leapDataCtx.getServiceDataContext(TEST_TENANT, TEST_SITE,
				TEST_FEATUREGROUP, TEST_FEATURE);
		serviceDataContext.setVendor(TEST_VENDOR);
		serviceDataContext.setVersion(TEST_VERSION);
		serviceDataContext.setEndpointType("HTTP-JSON");
		serviceDataContext.initializeLeapRuntimeServiceContext(TEST_SERVICE);
		serviceDataContext.setRequestUUID("123456789");

		serviceDataContext.setRequestContext(new RequestContext(TEST_TENANT, TEST_SITE, TEST_FEATUREGROUP, TEST_FEATURE,
				TEST_IMPL, TEST_VENDOR, TEST_VERSION));
		serviceDataContext.SetRunningContextServiceName(TEST_SERVICE);
	}

	/**
	 * this method set Service Context with subscriberData for jms
	 * 
	 * @param leapDataCtx
	 */
	public static void setServiceContextWithJmsSubscribeData(LeapDataContext leapDataCtx) {

		LeapServiceContext serviceDataContext = leapDataCtx.getServiceDataContext(TEST_TENANT, TEST_SITE,
				TEST_FEATUREGROUP, TEST_FEATURE);
		serviceDataContext.setVendor(TEST_VENDOR);
		serviceDataContext.setVersion(TEST_VERSION);
		serviceDataContext.setEndpointType("HTTP-JSON");
		serviceDataContext.initializeLeapRuntimeServiceContext(TEST_SERVICE);
		serviceDataContext.setRequestUUID("123456789");
		serviceDataContext.setSubscriberDataInServiceContext(setJMSSubscriber());

		serviceDataContext.setRequestContext(new RequestContext(TEST_TENANT, TEST_SITE, TEST_FEATUREGROUP, TEST_FEATURE,
				TEST_IMPL, TEST_VENDOR, TEST_VERSION));
		serviceDataContext.SetRunningContextServiceName(TEST_SERVICE);
	}

	/**
	 * this method set Service Context with subscriberData for kafka
	 * 
	 * @param leapDataCtx
	 */
	public static void setServiceContextWithSubscribeData(LeapDataContext leapDataCtx) {

		LeapServiceContext serviceDataContext = leapDataCtx.getServiceDataContext(TEST_TENANT, TEST_SITE,
				TEST_FEATUREGROUP, TEST_FEATURE);
		serviceDataContext.setVendor(TEST_VENDOR);
		serviceDataContext.setVersion(TEST_VERSION);
		serviceDataContext.setEndpointType("HTTP-JSON");
		serviceDataContext.initializeLeapRuntimeServiceContext(TEST_SERVICE);
		serviceDataContext.setRequestUUID("123456789");
		serviceDataContext.setSubscriberDataInServiceContext(setSubscriber());

		serviceDataContext.setRequestContext(new RequestContext(TEST_TENANT, TEST_SITE, TEST_FEATUREGROUP, TEST_FEATURE,
				TEST_IMPL, TEST_VENDOR, TEST_VERSION));
		serviceDataContext.SetRunningContextServiceName(TEST_SERVICE);
	}

	/**
	 * This method Use for create exchange.
	 * 
	 * @return
	 * @throws Exception
	 */
	public static Exchange createExchange() {
		CamelContext context = new DefaultCamelContext();
		Exchange exchange = new DefaultExchange(context);
		return exchange;
	}

	/**
	 * This exchange Use for jms Subscription only
	 * 
	 * @return
	 * @throws Exception
	 */
	public static Exchange createJmsExchange() {
		CamelContext context = new DefaultCamelContext();
		Exchange exchange = new DefaultExchange(context);
		exchange.setIn(getJmsMessage());
		return exchange;
	}

	/**
	 * This method Use for create exchange with set camelContext With registry..
	 * 
	 * @return
	 * @throws Exception
	 */
	public static Exchange createExchangeWithRegistry() {
		SimpleRegistry registry = new SimpleRegistry();
		registry.put("dataSourceSQL", setDataSource());
		CamelContext context = new DefaultCamelContext(registry);
		Exchange exchange = new DefaultExchange(context);
		return exchange;
	}

	/**
	 * this method set header with leapJsonObject and set required header and push
	 * to LDC with #header tag .
	 * 
	 * @param leapDataCtx
	 * @param exchange
	 */
	public static void setHeaders(LeapDataContext leapDataCtx, Exchange exchange) {
		addContextElementWithLeapTag(leapDataCtx);
		addContextElementWithSetRequestHeader(leapDataCtx);
		exchange.getIn().setHeaders(new BaseRouteTestUtil().setHeader());
		exchange.getIn().setHeader(LEAP_DATA_CONTEXT, leapDataCtx);
	}

	/**
	 * this method use for setLeapPrivate header data and add contextElement with
	 * #leap_private
	 * 
	 * @param leapDataCtx
	 */
	public static void setLeapPrivateHeader(LeapDataContext leapDataCtx) {
		setServiceContext(leapDataCtx);

		Map<String, Object> privateHeadersMap = new HashMap<String, Object>();
		privateHeadersMap.put(ACCOUNTID, TEST_TENANT);

		InitialLeapDataContextElement initialRequestHeaderElement = new InitialLeapDataContextElement();
		initialRequestHeaderElement.setPrivateHeaderElement(privateHeadersMap);

		LeapDataContextElement leapDataContextElement = new LeapDataContextElement(TAG_NAME_HEADER,
				initialRequestHeaderElement);
		leapDataCtx.addContextElement(leapDataContextElement, "#leap_private");
	}

	/**
	 * leap Data without leap Json
	 * 
	 * @return
	 */
	private static LeapData getLeapDataWithOutLeapJson() {
		LeapResultSet leapResultSet = new LeapJSONResultSet();
		leapResultSet.setData(new JSONObject(DEMO_JSON_DATA));

		LeapData data = new LeapData();
		data.setItems(leapResultSet);
		return data;
	}

	/**
	 * leap Data with leap json.
	 * 
	 * @return
	 */
	private static LeapData getLeapData() {
		LeapResultSet leapResultSet = new LeapJSONResultSet();
		leapResultSet.setData(new JSONObject(DEMO_LEAP_DATA));

		LeapData data = new LeapData();
		data.setItems(leapResultSet);
		return data;
	}

	/**
	 * set dataSource
	 * 
	 * @return
	 */
	private static DriverManagerDataSource setDataSource() {
		BaseRouteTestUtil baseRouteTestUtil = new BaseRouteTestUtil();
		AbstractApplicationContext applicationContext = baseRouteTestUtil.applicationContext();
		DriverManagerDataSource bean = applicationContext.getBean("dataSourceSQL", DriverManagerDataSource.class);
		return bean;
	}

	/**
	 * set Subscriber data for jms
	 * 
	 * @return
	 */
	private static Map<String, Object> setJMSSubscriber() {
		Map<String, Object> subscriber = null;
		
		EventFramework jmsSubscription = LeapCoreTestFileRead.getJmsSubscription();
		JMSSubscribeEvent jmsSubscribeEvent = jmsSubscription.getEventSubscription().getJmsSubscribeEvent().get(0);

		String jmsRetry = jmsSubscribeEvent.getFailureHandlingStrategy().getFailureStrategyConfig();
		try {
			AbstractSubscriptionRetryStrategy retryStrategy = new JMSLeapDefaultRetryStrategy(jmsRetry);

			subscriber = new HashMap<String, Object>();
			subscriber.put("RetryStrategy", retryStrategy);
			subscriber.put("EventSubscriptionTracker", setEventSubscriptionTrackerForJMS());
			subscriber.put("subscriptionQuartzTrigger", false);

		} catch (ConfigurationValidationFailedException e) {
			e.printStackTrace();
		}
		return subscriber;
	}

	/**
	 * set Subscriber data for kafka
	 * 
	 * @return
	 */
	private static Map<String, Object> setSubscriber() {
		Map<String, Object> subscriber = null;
		EventFramework subscription = LeapCoreTestFileRead.getKafkaSubscription();
		SubscribeEvent subscribeEvent = subscription.getEventSubscription().getSubscribeEvent().get(0);

		String FailureStrategy = subscribeEvent.getFailureHandlingStrategy().getFailureStrategyConfig();
		try {
			AbstractSubscriptionRetryStrategy retryStrategy = new LeapDefaultRetryStrategy(FailureStrategy);

			subscriber = new HashMap<String, Object>();
			subscriber.put("RetryStrategy", retryStrategy);
			subscriber.put("EventSubscriptionTracker", setEventSubscriptionTrackerForKafka());
			subscriber.put("subscriptionQuartzTrigger", false);

		} catch (ConfigurationValidationFailedException e) {
			e.printStackTrace();
		}
		return subscriber;
	}

	/**
	 * set event Subscription Tracker data for JMS subscription.
	 * 
	 * @return
	 */
	private static EventSubscriptionTracker setEventSubscriptionTrackerForJMS() {
		String eventData = "{\"Test\":\"demo\"}";

		EventSubscriptionTracker tracker = new EventSubscriptionTracker();
		tracker.setEventData(eventData);
		tracker.setTenantId(TEST_TENANT);
		tracker.setSiteId(TEST_SITE);
		tracker.setSubscriptionId("TestJmsSubId");
		tracker.setTopic("testJmsTopicName");
		tracker.setPartition("1");
		tracker.setOffset("1");
		return tracker;
	}

	/**
	 * set event Subscription Tracker data for Kafka subscription.
	 * 
	 * @return
	 */
	private static EventSubscriptionTracker setEventSubscriptionTrackerForKafka() {
		String eventData = "{\"Test\":\"demo\"}";

		EventSubscriptionTracker tracker = new EventSubscriptionTracker();
		tracker.setEventData(eventData);
		tracker.setTenantId(TEST_TENANT);
		tracker.setSiteId(TEST_SITE);
		tracker.setSubscriptionId("TestKafka");
		tracker.setTopic("testKafkaTopicName");
		tracker.setPartition("1");
		tracker.setOffset("1");
		tracker.setIsRetryable(true);
		return tracker;
	}

	private static Message getJmsMessage() {
		Message message = null;
		try {
			if(session==null)
				embeddedActiveMq();
			String payload = "Hi, I am text message";
			javax.jms.Message msg = session.createTextMessage(payload);
			JmsBinding binding = new JmsBinding();
			message = new JmsMessage(msg, session, binding);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return message;
	}

	/**
	 * configure the embedded ActiveMq.
	 * 
	 * @throws Exception
	 */
	private static void embeddedActiveMq() throws Exception {
		BrokerService service = new BrokerService();
		service.addConnector("tcp://localhost:6161");
		service.start();
		String URL = service.getTransportConnectors().get(0).toString();
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(URL);
		if (connection == null) {
			connection = connectionFactory.createConnection();
			connection.start();
		}
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	}

	/**
	 * configure the embedded kafka.
	 * 
	 * @param embeddedKafkaBroker
	 */
	private static void setEmbeddedKafka(EmbeddedKafkaBroker embeddedKafkaBroker) {
		String data = "Testing data";
		Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
		producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		Producer<String, String> producerTest = new KafkaProducer<String, String>(producerProps);
		ProducerRecord<String, String> recordData = new ProducerRecord<String, String>("testKafkaTopicName", data);
		producerTest.send(recordData);
		producerTest.close();
	}

	/**
	 * set Consumer kafka.
	 * 
	 * @param embeddedKafkaBroker
	 * @return
	 */
	public static KafkaManualCommit setConsumerKafka(EmbeddedKafkaBroker embeddedKafkaBroker) {
		setEmbeddedKafka(embeddedKafkaBroker);
		Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("TestKafkaGroup", "true", embeddedKafkaBroker);
		consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

		KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(consumerProps);
		TopicPartition partition = new TopicPartition("testKafkaTopicName", 0);
		KafkaManualCommit commit = new DefaultKafkaManualCommit(consumer, "testKafkaTopicName",
				Thread.currentThread().getName(), null, partition, 9999999L);
		return commit;
	}

	/**
	 * 
	 * @return
	 */
	public static SubscriptionUtil setSubscriptionUtil() {
		SubscriptionUtil subscriptionUtil = new SubscriptionUtil();
		return subscriptionUtil;
	}
}
