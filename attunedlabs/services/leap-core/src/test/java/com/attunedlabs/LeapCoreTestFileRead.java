package com.attunedlabs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import com.attunedlabs.eventframework.config.EventFrameworkConfigParserException;
import com.attunedlabs.eventframework.config.EventFrameworkXmlHandler;
import com.attunedlabs.eventframework.jaxb.DispatchChannel;
import com.attunedlabs.eventframework.jaxb.Event;
import com.attunedlabs.eventframework.jaxb.EventFramework;
import com.attunedlabs.eventframework.jaxb.SystemEvent;

public class LeapCoreTestFileRead {

	private final static String featureServiceFile = "Feature/feature-service.xml";
	private final static String systemEventFile = "Event/SystemEvent.xml";
	private final static String dispatchChannel = "Event/DispatchChannel.xml";
	private final static String eventFile = "Event/Event.xml";
	private final static String kafkaEventSubscription = "Event/EventSubscription/KafkaSubscription.xml";
	private final static String jmsEventSubscription = "Event/EventSubscription/JMSSubscription.xml";

	/**
	 * read file from test resources.
	 * 
	 * @return
	 */
	public static String getFeatureServiceXMLFileToString() {
		InputStream inputstream = LeapCoreTestFileRead.class.getClassLoader()
				.getResourceAsStream(featureServiceFile);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				out1.append(line);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return out1.toString();
	}

	/**
	 * this method read list of SystemEvent data.
	 * 
	 * @return
	 */
	public static List<SystemEvent> getSystemEventFramework() {
		EventFrameworkXmlHandler parser = new EventFrameworkXmlHandler();
		EventFramework evtFramework = null;
		InputStream inputstream = LeapCoreTestFileRead.class.getClassLoader().getResourceAsStream(systemEventFile);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				out1.append(line);
			}
			reader.close();
			evtFramework = parser.marshallConfigXMLtoObject(out1.toString());
		} catch (IOException | EventFrameworkConfigParserException e) {
			e.printStackTrace();
		}
		return evtFramework.getSystemEvents().getSystemEvent();
	}

	/**
	 * this method read list of DispatchChannel data.
	 * 
	 * @return
	 */
	public static List<DispatchChannel> getDispatchChannel() {
		EventFrameworkXmlHandler parser = new EventFrameworkXmlHandler();
		EventFramework evtFramework = null;
		InputStream inputstream = LeapCoreTestFileRead.class.getClassLoader().getResourceAsStream(dispatchChannel);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				out1.append(line);
			}
			reader.close();
			evtFramework = parser.marshallConfigXMLtoObject(out1.toString());
		} catch (IOException | EventFrameworkConfigParserException e) {
			e.printStackTrace();
		}
		return evtFramework.getDispatchChannels().getDispatchChannel();
	}

	/**
	 * this method read list of Event data.
	 * 
	 * @return
	 */
	public static List<Event> getEventFramework() {
		EventFrameworkXmlHandler parser = new EventFrameworkXmlHandler();
		EventFramework evtFramework = null;
		InputStream inputstream = LeapCoreTestFileRead.class.getClassLoader().getResourceAsStream(eventFile);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				out1.append(line);
			}
			reader.close();
			evtFramework = parser.marshallConfigXMLtoObject(out1.toString());
		} catch (IOException | EventFrameworkConfigParserException e) {
			e.printStackTrace();
		}
		return evtFramework.getEvents().getEvent();
	}

	/**
	 * read kafkaSubscription file
	 * 
	 * @return
	 */
	public static EventFramework getKafkaSubscription() {
		EventFrameworkXmlHandler parser = new EventFrameworkXmlHandler();
		InputStream inputstream = LeapCoreTestFileRead.class.getClassLoader()
				.getResourceAsStream(kafkaEventSubscription);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line = null;
		EventFramework evtFramework = null;
		try {
			while ((line = reader.readLine()) != null) {
				out1.append(line);
			}
			reader.close();
			evtFramework = parser.marshallConfigXMLtoObject(out1.toString());
		} catch (IOException | EventFrameworkConfigParserException e) {
			e.printStackTrace();
		}
		return evtFramework;
	}

	/**
	 * Read JMS Configuration File.
	 * 
	 * @return
	 */
	public static EventFramework getJmsSubscription() {
		EventFrameworkXmlHandler parser = new EventFrameworkXmlHandler();
		InputStream inputstream = LeapCoreTestFileRead.class.getClassLoader()
				.getResourceAsStream(jmsEventSubscription);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line = null;
		EventFramework evtFramework = null;
		try {
			while ((line = reader.readLine()) != null) {
				out1.append(line);
			}
			reader.close();
			evtFramework = parser.marshallConfigXMLtoObject(out1.toString());
		} catch (IOException | EventFrameworkConfigParserException e) {
			e.printStackTrace();
		}
		return evtFramework;
	}

	/**
	 * read logFileDeispatcher Channel file.
	 * 
	 * @return
	 */
	public static String readDispatcherFile() {
		File file = new File("${user.dir}\\..\\..\\..\\config\\local\\TestLog\\TestEventLog.txt");
		StringBuilder out1 = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = reader.readLine()) != null) {
				out1.append(line);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return out1.toString();
	}
}
