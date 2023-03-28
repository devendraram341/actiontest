package com.attunedlabs.embedded;

import java.util.Collections;
import java.util.Map;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.kafka.test.utils.KafkaTestUtils;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EmbeddedKafkaTest {

	@ClassRule
	public static EmbeddedKafkaRule embeddedKafka = new EmbeddedKafkaRule(1, true, "topic");

	private EmbeddedKafkaBroker embeddedKafkaBroker;

	@Before
	public void setUp() {
		if (embeddedKafkaBroker == null)
			embeddedKafkaBroker = embeddedKafka.getEmbeddedKafka();
	}

	/**
	 * method use for produce the some data for testing
	 */
	@Test
	public void testAProducer() {

		String data = "Embedded Testing";
		Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
		producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		Producer<String, String> producerTest = new KafkaProducer<String, String>(producerProps);
		ProducerRecord<String, String> record = new ProducerRecord<String, String>("topic", data);
		producerTest.send(record);
		producerTest.close();

	}

	/**
	 * method use for consume the data from the producer data.
	 */
	@Test
	public void testBConsumer() {
		Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("TestGroup", "true", embeddedKafkaBroker);

		consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

		Consumer<String, String> consumer = new KafkaConsumer<String, String>(consumerProps);
		consumer.subscribe(Collections.singletonList("topic"));

		while (true) {
			boolean flag = false;
			ConsumerRecords<String, String> messageConsumed = consumer.poll(10);
			for (ConsumerRecord<String, String> record : messageConsumed) {
				flag = true;
				Assert.assertEquals(1, messageConsumed.count());
				Assert.assertEquals("Embedded Testing", record.value());
			}
			if (flag) {
				break;
			}
		}
		consumer.close();
	}
}
