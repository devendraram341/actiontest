package com.attunedlabs.eventframework.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.attunedlabs.eventframework.jaxb.EventFramework;

public class EventAndDispatchFileRead {
	
	public static EventFramework getFileStore() throws EventFrameworkConfigParserException {
		EventFrameworkXmlHandler parser = new EventFrameworkXmlHandler();
		InputStream inputstream = EventFrameworkXmlHandler.class.getClassLoader()
				.getResourceAsStream(EventFrameworkTestConstant.FILE_STORE);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				out1.append(line);
			}
			reader.close();
		} catch (IOException e) {
			throw new EventFrameworkConfigParserException("eventFramework file doesnot exist in classpath", e);
		}
		String evtFrmeworkXmlStr = out1.toString();
		EventFramework evtFramework = parser.marshallConfigXMLtoObject(evtFrmeworkXmlStr);
		return evtFramework;
	}
	
	
	public static EventFramework getHazelCastQueue() throws EventFrameworkConfigParserException {
		EventFrameworkXmlHandler parser = new EventFrameworkXmlHandler();
		InputStream inputstream = EventFrameworkXmlHandler.class.getClassLoader()
				.getResourceAsStream(EventFrameworkTestConstant.HAZELCAST_QUEUE);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				out1.append(line);
			}
			reader.close();
		} catch (IOException e) {
			throw new EventFrameworkConfigParserException("eventFramework file doesnot exist in classpath", e);
		}
		String evtFrmeworkXmlStr = out1.toString();
		EventFramework evtFramework = parser.marshallConfigXMLtoObject(evtFrmeworkXmlStr);
		return evtFramework;
	}
	
	
	public static EventFramework getHazelCastTopic() throws EventFrameworkConfigParserException {
		EventFrameworkXmlHandler parser = new EventFrameworkXmlHandler();
		InputStream inputstream = EventFrameworkXmlHandler.class.getClassLoader()
				.getResourceAsStream(EventFrameworkTestConstant.HAZELCAST_TOPIC);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				out1.append(line);
			}
			reader.close();
		} catch (IOException e) {
			throw new EventFrameworkConfigParserException("eventFramework file doesnot exist in classpath", e);
		}
		String evtFrmeworkXmlStr = out1.toString();
		EventFramework evtFramework = parser.marshallConfigXMLtoObject(evtFrmeworkXmlStr);
		return evtFramework;
	}
	
	
	public static EventFramework getKafkaQueue() throws EventFrameworkConfigParserException {
		EventFrameworkXmlHandler parser = new EventFrameworkXmlHandler();
		InputStream inputstream = EventFrameworkXmlHandler.class.getClassLoader()
				.getResourceAsStream(EventFrameworkTestConstant.KAFKA_QUEUE);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				out1.append(line);
			}
			reader.close();
		} catch (IOException e) {
			throw new EventFrameworkConfigParserException("eventFramework file doesnot exist in classpath", e);
		}
		String evtFrmeworkXmlStr = out1.toString();
		EventFramework evtFramework = parser.marshallConfigXMLtoObject(evtFrmeworkXmlStr);
		return evtFramework;
	}
	
	public static EventFramework getKafkaTopic() throws EventFrameworkConfigParserException {
		EventFrameworkXmlHandler parser = new EventFrameworkXmlHandler();
		InputStream inputstream = EventFrameworkXmlHandler.class.getClassLoader()
				.getResourceAsStream(EventFrameworkTestConstant.KAFKA_TOPIC);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				out1.append(line);
			}
			reader.close();
		} catch (IOException e) {
			throw new EventFrameworkConfigParserException("eventFramework file doesnot exist in classpath", e);
		}
		String evtFrmeworkXmlStr = out1.toString();
		EventFramework evtFramework = parser.marshallConfigXMLtoObject(evtFrmeworkXmlStr);
		return evtFramework;
	}
	
	
	public static EventFramework getRestDispatch() throws EventFrameworkConfigParserException {
		EventFrameworkXmlHandler parser = new EventFrameworkXmlHandler();
		InputStream inputstream = EventFrameworkXmlHandler.class.getClassLoader()
				.getResourceAsStream(EventFrameworkTestConstant.REST_DISPATCHER);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				out1.append(line);
			}
			reader.close();
		} catch (IOException e) {
			throw new EventFrameworkConfigParserException("eventFramework file doesnot exist in classpath", e);
		}
		String evtFrmeworkXmlStr = out1.toString();
		EventFramework evtFramework = parser.marshallConfigXMLtoObject(evtFrmeworkXmlStr);
		return evtFramework;
	}
	
	
	public static EventFramework getJMSSubscription() throws EventFrameworkConfigParserException {
		EventFrameworkXmlHandler parser = new EventFrameworkXmlHandler();
		InputStream inputstream = EventFrameworkXmlHandler.class.getClassLoader()
				.getResourceAsStream(EventFrameworkTestConstant.JMS_SUBSCRIPTION);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				out1.append(line);
			}
			reader.close();
		} catch (IOException e) {
			throw new EventFrameworkConfigParserException("eventFramework file doesnot exist in classpath", e);
		}
		String evtFrmeworkXmlStr = out1.toString();
		EventFramework evtFramework = parser.marshallConfigXMLtoObject(evtFrmeworkXmlStr);
		return evtFramework;
	}
	
	
	public static EventFramework getKafkaSubscription() throws EventFrameworkConfigParserException {
		EventFrameworkXmlHandler parser = new EventFrameworkXmlHandler();
		InputStream inputstream = EventFrameworkXmlHandler.class.getClassLoader()
				.getResourceAsStream(EventFrameworkTestConstant.KAFKA_SUBSCRIPTION);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				out1.append(line);
			}
			reader.close();
		} catch (IOException e) {
			throw new EventFrameworkConfigParserException("eventFramework file doesnot exist in classpath", e);
		}
		String evtFrmeworkXmlStr = out1.toString();
		EventFramework evtFramework = parser.marshallConfigXMLtoObject(evtFrmeworkXmlStr);
		return evtFramework;
	}
	
	public static EventFramework getSystemEvent() throws EventFrameworkConfigParserException {
		EventFrameworkXmlHandler parser = new EventFrameworkXmlHandler();
		InputStream inputstream = EventFrameworkXmlHandler.class.getClassLoader()
				.getResourceAsStream(EventFrameworkTestConstant.SYSTEM_EVENT);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				out1.append(line);
			}
			reader.close();
		} catch (IOException e) {
			throw new EventFrameworkConfigParserException("eventFramework file doesnot exist in classpath", e);
		}
		String evtFrmeworkXmlStr = out1.toString();
		EventFramework evtFramework = parser.marshallConfigXMLtoObject(evtFrmeworkXmlStr);
		return evtFramework;
	}
	
	
	
	
}
