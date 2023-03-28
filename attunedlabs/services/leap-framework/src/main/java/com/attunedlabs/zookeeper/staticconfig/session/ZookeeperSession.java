package com.attunedlabs.zookeeper.staticconfig.session;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.util.LeapConfigUtil;
import com.attunedlabs.config.util.LeapDefaultConstants;
import com.attunedlabs.config.util.PropertiesConfigException;
import com.attunedlabs.eventframework.abstractbean.util.CassandraUtil;
import com.attunedlabs.eventframework.abstractbean.util.ConnectionConfigurationException;
import com.attunedlabs.zookeeper.staticconfig.service.impl.ZookeeperNodeDataWatcherService;
import com.attunedlabs.zookeeper.staticconfig.watcher.service.IZookeeperDataWatcherListner;

/**
 * To instantiate watchers and Zookeeper Session..
 * 
 * @author Bizruntime
 *
 */
public class ZookeeperSession implements Watcher, Runnable, IZookeeperDataWatcherListner {
	final static Logger log = LoggerFactory.getLogger(ZookeeperSession.class);
	final CountDownLatch connectedSignal = new CountDownLatch(1);
	static Process child;
	private String[] exec;
	static ZookeeperNodeDataWatcherService zkDwatcher;
	private String filename;
	private static ZooKeeper zk;
	static final String ZOOKEEPER_HOST_KEY = "host";
	static final String ZOOKEEPER_PORT_KEY = "port";
	static final String ZOOKEEPER_TIMEOUT_KEY = "timeout";
	static ZookeeperSession zookeeperSession;
	static String pathToWatch;
	static final String ZOOKEEPER_CONNECTON_PROPS = "globalAppDeploymentConfig.properties";
	private static Logger logger = (Logger) LoggerFactory.getLogger(ZookeeperSession.class.getName());

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getFilename() {
		return filename;
	}

	public ZookeeperSession(ZooKeeper zk) {
		ZookeeperSession.zk = zk;
	}

	public static ZooKeeper getZk() {
		return zk;
	}

	public static String getPathToWatch() {
		return pathToWatch;
	}

	public static void setPathToWatch(String pathToWatch) {
		ZookeeperSession.pathToWatch = pathToWatch;
	}

	public ZookeeperSession() {

	}

	/**
	 * single instance getter for the ZookeeperConnection
	 * 
	 * @return
	 * @throws IOException
	 * @throws PropertiesConfigException
	 * @throws ConnectionConfigurationException
	 */
	public static ZookeeperSession getZookeeperSession() throws IOException {
		String methodName = "getZookeeperSession";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		if (zookeeperSession == null) {
			synchronized (ZookeeperSession.class) {
				try {
					String deployemntEnv;
					deployemntEnv = LeapConfigUtil.getGlobalPropertyValue(CassandraUtil.DEPLOYMENT_ENVIRONMENT_KEY,LeapDefaultConstants.DEFAULT_DEPLOYMENT_ENVIRONMENT_KEY);
					if (deployemntEnv != null && !(deployemntEnv.isEmpty()) && deployemntEnv.length() > 0
							&& deployemntEnv
									.equalsIgnoreCase(CassandraUtil.PAAS_CASSANDRA_DEPLOYMENT_ENVIRONMENT_KEY)) {
						String host = LeapConfigUtil.getGlobalPropertyValue(ZOOKEEPER_HOST_KEY,LeapDefaultConstants.DEFAULT_ZOOKEEPER_HOST_KEY);
						logger.debug("{} zookeper host {} and port {}", LEAP_LOG_KEY, host);
						if ((host != null && !(host.isEmpty()) && host.length() > 0)) {
							String zookeeperConnectingString = host.trim();
							logger.trace("{} zookeeperConnectingString {}", LEAP_LOG_KEY, zookeeperConnectingString);
							zk = new ZooKeeper(zookeeperConnectingString.trim(),
									Integer.parseInt(LeapConfigUtil.getGlobalPropertyValue(ZOOKEEPER_TIMEOUT_KEY,LeapDefaultConstants.DEFAULT_ZOOKEEPER_TIMEOUT_KEY)),
									new ZookeeperSession());
						} else {
							throw new IOException("Unable to create zookeeper session because connection is  is : " + host
									+  " from system environment");
						}
					} else {
						zk = new ZooKeeper(LeapConfigUtil.getGlobalPropertyValue(ZOOKEEPER_HOST_KEY,LeapDefaultConstants.DEFAULT_ZOOKEEPER_HOST_KEY),
								Integer.parseInt(LeapConfigUtil.getGlobalPropertyValue(ZOOKEEPER_TIMEOUT_KEY,LeapDefaultConstants.DEFAULT_ZOOKEEPER_TIMEOUT_KEY)),
								new ZookeeperSession());
					}
					zkDwatcher = new ZookeeperNodeDataWatcherService(zk, getPathToWatch(), null,
							new ZookeeperSession());
					zookeeperSession = new ZookeeperSession(zk);
				} catch (PropertiesConfigException e) {
					log.error("{} Problem in getting the deloyment config {} ", LEAP_LOG_KEY, e);
				}

			}
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return zookeeperSession;
	}// ..end of the method

	/**
	 * Optional constructor can be used if needed extension of session
	 * 
	 * @param hostPort
	 * @param znode
	 * @param filename
	 * @param exec
	 * @throws KeeperException
	 * @throws IOException
	 */
	public ZookeeperSession(String hostPort, String znode, String filename, String[] exec)
			throws KeeperException, IOException {
		this.filename = filename;
		this.exec = exec;
		if (zk == null) {
			zk = new ZooKeeper(hostPort, 3000, this);
		}
		zkDwatcher = new ZookeeperNodeDataWatcherService(zk, znode, null, this);
	}// ..end of parameterized constructor

	@Override
	public void process(WatchedEvent event) {
		zkDwatcher.process(event);
	}

	/**
	 * When exists then keep listening by initiating the thread runtime
	 */
	@Override
	public void exists(byte[] data) {
		if (data == null) {
			if (child != null) {
				child.destroy();
				try {
					child.waitFor();
				} catch (InterruptedException e) {
					logger.error("{} Exception in zookeeper session wait {} ", LEAP_LOG_KEY, e);
				}
			}
			child = null;
		}
		try {
			child = Runtime.getRuntime().exec(exec);
		} catch (IOException e) {
			logger.error("{} Exception in zookeeper session thread execution {} ", LEAP_LOG_KEY, e);
		}
	}// ..end of the method

	@Override
	public void closing(int reasonCode) {
		synchronized (this) {
			notifyAll();
		}
	}// ..end of the method

	@Override
	public void run() {
		try {
			synchronized (this) {
				while (!zkDwatcher.dead) {
					wait();
				}
			}
		} catch (InterruptedException e) {
		}
	}

	/**
	 * compulsory close of the zookeeperSession
	 * 
	 * @throws InterruptedException
	 */
	public void close() throws InterruptedException {
		zk.close();
	}// ..end of the method

}
