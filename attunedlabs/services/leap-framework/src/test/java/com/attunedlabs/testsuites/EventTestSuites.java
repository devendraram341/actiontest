package com.attunedlabs.testsuites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.attunedlabs.eventframework.config.EventFrameworkConfigServiceTest;
import com.attunedlabs.eventframework.config.EventFrameworkConfigXMLParserTest;
import com.attunedlabs.eventframework.dispatcher.EventFrameworkDispatcherServiceTest;
import com.attunedlabs.eventframework.dispatcher.channel.FileStoreDispatchChannelTest;
import com.attunedlabs.eventframework.dispatcher.channel.HazelcastQueueDispatchChannelTest;
import com.attunedlabs.eventframework.dispatcher.channel.HazelcastTopicDispatchChannelTest;
import com.attunedlabs.eventframework.dispatcher.channel.KafkaQueueDispatchChannelTest;
import com.attunedlabs.eventframework.dispatcher.channel.KafkaTopicDispatchChannelTest;
import com.attunedlabs.eventframework.dispatcher.channel.RestClientPostDispatchChannelTest;
import com.attunedlabs.eventframework.dispatcher.transformer.GenericLeapEventJsonTransformerTest;
import com.attunedlabs.eventframework.dispatcher.transformer.LeapEventXmlTransformerTest;
import com.attunedlabs.eventframework.dispatcher.transformer.XmlTransformerHelperTest;
import com.attunedlabs.eventframework.event.LeapEventServiceTest;
import com.attunedlabs.eventframework.eventtracker.EventDispatcherTrackerServiceTest;
import com.attunedlabs.eventframework.retrypolicy.RetryPolicyTest;

@RunWith(Suite.class)
@SuiteClasses({
	EventFrameworkConfigServiceTest.class, 
	EventFrameworkConfigXMLParserTest.class,
	EventFrameworkDispatcherServiceTest.class, 
	FileStoreDispatchChannelTest.class,
	HazelcastTopicDispatchChannelTest.class, 
	HazelcastQueueDispatchChannelTest.class,
	KafkaQueueDispatchChannelTest.class,
	KafkaTopicDispatchChannelTest.class, 
	RestClientPostDispatchChannelTest.class,
	GenericLeapEventJsonTransformerTest.class, 
	LeapEventXmlTransformerTest.class, 
	XmlTransformerHelperTest.class,
	LeapEventServiceTest.class, 
	EventDispatcherTrackerServiceTest.class, 
	RetryPolicyTest.class
	})
public class EventTestSuites {

	
}
