package com.attunedlabs.embedded.rabbitmq;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.qpid.server.SystemLauncher;
import org.apache.qpid.server.configuration.IllegalConfigurationException;
import org.apache.qpid.server.model.SystemConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmbeddedInMemoryQpidBroker {
	public static final Logger logger = LoggerFactory.getLogger(EmbeddedInMemoryQpidBroker.class);

	private static final String DEFAULT_INITIAL_CONFIGURATION_LOCATION = "qpid-embedded.json";

	private SystemLauncher systemLauncher;

	public EmbeddedInMemoryQpidBroker() {
		this.systemLauncher = new SystemLauncher();
	}

	public void start() throws Exception {
		this.systemLauncher.startup(createSystemConfig());
	}

	public void shutdown() {
		this.systemLauncher.shutdown();
	}

	private Map<String, Object> createSystemConfig() throws IllegalConfigurationException {
		Map<String, Object> attributes = new HashMap<String, Object>();
		URL initialConfigUrl = EmbeddedInMemoryQpidBroker.class.getClassLoader()
				.getResource(DEFAULT_INITIAL_CONFIGURATION_LOCATION);
		if (initialConfigUrl == null)
			throw new IllegalConfigurationException(
					"Configuration location '" + DEFAULT_INITIAL_CONFIGURATION_LOCATION + "' not found");
		
		attributes.put(SystemConfig.TYPE, "Memory");
		attributes.put(SystemConfig.INITIAL_CONFIGURATION_LOCATION, initialConfigUrl.toExternalForm());
		attributes.put(SystemConfig.STARTUP_LOGGED_TO_SYSTEM_OUT, true);
		return attributes;
	}
}