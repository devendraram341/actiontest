package com.attunedlabs.leap.load.resource;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.io.File;

import org.apache.camel.spring.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.attunedlabs.config.ConfigurationConstant;
import com.attunedlabs.leap.eventsubscription.routebuilder.GenericJMSRetryRouteBuilder;
import com.attunedlabs.leap.eventsubscription.routebuilder.GenericRetryRouteBuilder;
import com.attunedlabs.leap.eventsubscription.routebuilder.JMSSubscriberEvaluationRouteBuilder;
import com.attunedlabs.leap.eventsubscription.routebuilder.JMSSubscriberRouteBuilder;
import com.attunedlabs.leap.eventsubscription.routebuilder.SubscriberEvaluationRouteBuilder;
import com.attunedlabs.leap.eventsubscription.routebuilder.SubscriberRouteBuilder;

public class CamelApplicationRun {
	private static final String BASEROUTE_XML_LOC = getLocationOfBaserouteXml();
	private static final String BASEROUTE_XML_NAME = "baseroute.xml";
	private static final String SPRING_DIR_NAME = "spring";
	private static final String FILE_COMPONENT = "file:";
	private Logger log = LoggerFactory.getLogger(CamelApplicationRun.class);

	@SuppressWarnings("resource")
	public void startCamelApplication() throws Exception {

		Main main = new Main();
		// configure the location of the Spring XML file
		File file = new File(BASEROUTE_XML_LOC);
		boolean exists = file.exists();
		if (exists) {
			log.info("{} baseroute.xml present at ::{} ", LEAP_LOG_KEY, BASEROUTE_XML_LOC);
			main.setApplicationContext(new FileSystemXmlApplicationContext(FILE_COMPONENT.concat(BASEROUTE_XML_LOC)));
		} else {
			log.error("{} baseroute.xml Not Found at {} ", LEAP_LOG_KEY, BASEROUTE_XML_LOC);
			throw new Exception("baseroute.xml Not Found at " + BASEROUTE_XML_LOC);
		}
		// enable hangup support allows Camel to detect when the JVM is
		// terminated
		// main.enableHangupSupport();

		main.addRouteBuilder(new SubscriberEvaluationRouteBuilder());
		main.addRouteBuilder(new JMSSubscriberEvaluationRouteBuilder());
		main.addRouteBuilder(new SubscriberRouteBuilder());
		main.addRouteBuilder(new JMSSubscriberRouteBuilder());
		main.addRouteBuilder(new GenericRetryRouteBuilder());
		main.addRouteBuilder(new GenericJMSRetryRouteBuilder());

		// run and block until Camel is stopped (or JVM terminated)
		main.run();
	}

	private static String getLocationOfBaserouteXml() {
		StringBuffer xmlLoc = new StringBuffer();
		xmlLoc.append(System.getProperty(ConfigurationConstant.BASE_CONFIG_PATH));
		xmlLoc.append(File.separator);
		xmlLoc.append(SPRING_DIR_NAME);
		xmlLoc.append(File.separator);
		xmlLoc.append(BASEROUTE_XML_NAME);
		return xmlLoc.toString();
	}

}
