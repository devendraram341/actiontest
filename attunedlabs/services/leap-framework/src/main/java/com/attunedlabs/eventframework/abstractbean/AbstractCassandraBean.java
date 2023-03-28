package com.attunedlabs.eventframework.abstractbean;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import static com.attunedlabs.eventframework.abstractbean.util.CassandraUtil.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.camel.Exchange;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.DataContextFactory;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.schema.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.util.LeapConfigUtil;
import com.attunedlabs.config.util.LeapDefaultConstants;
import com.attunedlabs.config.util.PropertiesConfigException;
import com.attunedlabs.eventframework.abstractbean.util.CassandraClusterException;
import com.attunedlabs.eventframework.abstractbean.util.CassandraConnectionException;
import com.attunedlabs.eventframework.abstractbean.util.CassandraUtil;
import com.attunedlabs.eventframework.abstractbean.util.ConnectionConfigurationException;
import com.datastax.driver.core.Cluster;

public abstract class AbstractCassandraBean extends AbstractLeapCamelBean {
	Logger logger = LoggerFactory.getLogger(AbstractCassandraBean.class);
	Connection con = null;

	/**
	 * This method is used to get the cassandra connection
	 * 
	 * @return
	 * @throws CassandraConnectionException
	 */
	protected Connection getCassandraConnection() throws CassandraConnectionException {
		String methodName = "getCassandraConnection";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		try {

			String deployemntEnv = LeapConfigUtil.getGlobalPropertyValue(DEPLOYMENT_ENVIRONMENT_KEY,LeapDefaultConstants.DEFAULT_DEPLOYMENT_ENVIRONMENT_KEY);
			if (deployemntEnv != null && !(deployemntEnv.isEmpty()) && deployemntEnv.length() > 0
					&& deployemntEnv.equalsIgnoreCase(PAAS_CASSANDRA_DEPLOYMENT_ENVIRONMENT_KEY)) {
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return getPAASCassandraConnection();
			} else if (deployemntEnv != null && !(deployemntEnv.isEmpty()) && deployemntEnv.length() > 0
					&& deployemntEnv.equalsIgnoreCase(LOCAL_CASSANDRA_DEPLOYMENT_ENVIRONMENT_KEY)) {
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return getLocalCassandraConnection();
			} else {
				throw new CassandraConnectionException("Unsupportable deployment environment for cassandra "
						+ deployemntEnv + ". Please provide either PAAS or Local");
			}

		} catch (PropertiesConfigException e2) {
			throw new CassandraConnectionException("unable to get the connection object for cassandra ", e2);
		}

	}// end of method

	/**
	 * This method is used to get the cassandra cluster
	 * 
	 * @return
	 * @throws CassandraClusterException
	 */
	protected Cluster getCassandraCluster() throws CassandraClusterException {
		String methodName = "getCassandraCluster";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		try {

			String deployemntEnv = LeapConfigUtil.getGlobalPropertyValue(DEPLOYMENT_ENVIRONMENT_KEY,LeapDefaultConstants.DEFAULT_DEPLOYMENT_ENVIRONMENT_KEY);
			if (deployemntEnv != null && !(deployemntEnv.isEmpty()) && deployemntEnv.length() > 0
					&& deployemntEnv.equalsIgnoreCase(PAAS_CASSANDRA_DEPLOYMENT_ENVIRONMENT_KEY)) {
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return getPAASCassandraCluster();
			} else if (deployemntEnv != null && !(deployemntEnv.isEmpty()) && deployemntEnv.length() > 0
					&& deployemntEnv.equalsIgnoreCase(LOCAL_CASSANDRA_DEPLOYMENT_ENVIRONMENT_KEY)) {
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return getLocalCassandraCluster();
			} else {
				throw new CassandraClusterException(
						"Unsupportable deployment environment for cassandra " + deployemntEnv);
			}
		} catch (PropertiesConfigException e2) {
			throw new CassandraClusterException("unable to get the connection object for cassandra : ", e2);
		}

	}// end of method getCassandraCluster

	protected UpdateableDataContext getUpdateableDataContextForCassandra(Connection connection) {
		String methodName = "getUpdateableDataContextForCassandra";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return dataContext;
	}

	protected DataContext getDataContextForCassandraByCluster(Cluster cluster, String keyspace) {
		String methodName = "getDataContextForCassandraByCluster";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		DataContext dataContext = DataContextFactory.createCassandraDataContext(cluster, keyspace);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return dataContext;
	}

	protected Table getTableForDataContext(DataContext datacontext, String tableName) {
		String methodName = "getTableForDataContext";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Table table = datacontext.getTableByQualifiedLabel(tableName);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return table;

	}

	@Override
	abstract protected void processBean(Exchange exch) throws Exception;

	/**
	 * This method is used to get data require for Cassandra local
	 * 
	 * @return Connection Object
	 * @throws CassandraConnectionException
	 */
	private Connection getLocalCassandraConnection() throws CassandraConnectionException {
		String methodName = "getLocalCassandraConnection";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);

		try {
			if (con == null || con.isClosed()) {
				try {
					Class.forName(LeapConfigUtil.getGlobalPropertyValue(DRIVER_CLASS_KEY,LeapDefaultConstants.DEFAULT_DRIVER_CLASS_KEY));
					try {
						con = DriverManager.getConnection(LeapConfigUtil.getGlobalPropertyValue(URL_KEY,LeapDefaultConstants.DEFAULT_URL_KEY));
						logger.debug("{} Connection Object :{} ", LEAP_LOG_KEY, con);
						logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
						return con;
					} catch (SQLException e) {
						throw new CassandraConnectionException(
								"URL to get the connection object for cassandra is invalid : "
										+ LeapConfigUtil.getGlobalPropertyValue(URL_KEY,LeapDefaultConstants.DEFAULT_URL_KEY),
								e);
					}
				} catch (ClassNotFoundException e) {
					throw new CassandraConnectionException("unable to load the driver name for cassandra : "
							+ LeapConfigUtil.getGlobalPropertyValue(DRIVER_CLASS_KEY,LeapDefaultConstants.DEFAULT_DRIVER_CLASS_KEY), e);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new CassandraConnectionException("unable to get the connection object for cassandra : ", e);
		}
		return con;
	}// end
		// of
		// method
		// getLocalCassandraConnection

	/**
	 * This method is used to get data require for Cassandra PAAS
	 * 
	 * @return Connection Object
	 * @throws CassandraConnectionException
	 */
	private Connection getPAASCassandraConnection() throws CassandraConnectionException {
		String methodName = "getPAASCassandraConnection";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Connection con = null;
		try {
			Class.forName(LeapConfigUtil.getGlobalPropertyValue(DRIVER_CLASS_KEY,LeapDefaultConstants.DEFAULT_DRIVER_CLASS_KEY));
			String cassandraIP = System.getenv(CASSANDRA_IP);
			if (cassandraIP != null && !(cassandraIP.isEmpty()) && cassandraIP.length() > 0) {
				String cassandraPort = LeapConfigUtil.getGlobalPropertyValue(PORT_KEY,LeapDefaultConstants.DEFAULT_PORT_KEY);
				String cassandraKeysapce = LeapConfigUtil.getGlobalPropertyValue(KEYSPACE_KEY,LeapDefaultConstants.DEFAULT_KEYSPACE_KEY);
				if ((cassandraPort != null && !(cassandraPort.isEmpty()) && cassandraPort.length() > 0)
						&& (cassandraKeysapce != null && !(cassandraKeysapce.isEmpty())
								&& cassandraKeysapce.length() > 0)) {
					// create url
					String url = JDBC_CASSANDRA_PROTOCOL + cassandraIP + JDBC_CASSANDRA_PROTOCOL_COLON_SEPERATED
							+ cassandraPort + JDBC_CASSANDRA_PROTOCOL_SEPERATED + cassandraKeysapce;
					logger.trace("{} cassandra db url : {}", LEAP_LOG_KEY, url);
					try {
						con = DriverManager.getConnection(url);
						logger.debug("{} Connection Object :{} ", LEAP_LOG_KEY, con);
						logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
						return con;
					} catch (SQLException e) {
						throw new CassandraConnectionException(
								"unable to create cassandra connection object with url : " + url);
					}
				} else {
					throw new CassandraConnectionException(
							"Unable to create connection for cassandra with port and and keyspace : " + cassandraPort
									+ ", " + cassandraKeysapce);
				}
			} else {
				throw new CassandraConnectionException(
						"Unable to create connection for cassandra with an IP given in system evironment : "
								+ cassandraIP);
			}
		} catch (ClassNotFoundException | PropertiesConfigException e) {
			throw new CassandraConnectionException("unable to load properties for cassandra : ", e);
		}

	}// end of method getPAASCassandraConnection

	/**
	 * This method is used to get cluster object of cassandra from local environment
	 * 
	 * @return
	 * @throws CassandraClusterException
	 */
	private Cluster getLocalCassandraCluster() throws CassandraClusterException {
		String methodName = "getLocalCassandraCluster";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Cluster cluster = null;
		try {
			String host = LeapConfigUtil.getGlobalPropertyValue(CassandraUtil.HOST_KEY,LeapDefaultConstants.DEFAULT_HOST_KEY);
			int port = Integer.parseInt(LeapConfigUtil.getGlobalPropertyValue(CassandraUtil.PORT_KEY,LeapDefaultConstants.DEFAULT_PORT_KEY));
			try {
				cluster = Cluster.builder().addContactPoint(host).withPort(port).build();
				logger.debug("{} cluster Object :{} ", LEAP_LOG_KEY, cluster);
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return cluster;
			} catch (Exception e) {
				throw new CassandraClusterException("unable to connect to the host for cassandra : "
						+ LeapConfigUtil.getGlobalPropertyValue(CassandraUtil.HOST_KEY,LeapDefaultConstants.DEFAULT_HOST_KEY), e);
			}
		} catch (PropertiesConfigException e1) {
			throw new CassandraClusterException("unable to get the connection object for cassandra : ", e1);
		}
	}// end of method getLocalCassandraCluster

	/**
	 * This method is used to get cluster object of cassandra from PAAS environment
	 * 
	 * @return
	 * @throws CassandraClusterException
	 */
	private Cluster getPAASCassandraCluster() throws CassandraClusterException {
		String methodName = "getPAASCassandraCluster";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Cluster cluster = null;
		try {
			String host = LeapConfigUtil.getGlobalPropertyValue(CassandraUtil.HOST_KEY,LeapDefaultConstants.DEFAULT_HOST_KEY);
			if (host != null && !(host.isEmpty()) && host.length() > 0) {
				int port = Integer.parseInt(LeapConfigUtil.getGlobalPropertyValue(CassandraUtil.PORT_KEY,LeapDefaultConstants.DEFAULT_PORT_KEY));
				try {
					cluster = Cluster.builder().addContactPoint(host).withPort(port).build();
					logger.debug("{} cluster Object : {}", LEAP_LOG_KEY, cluster);
					logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
					return cluster;

				} catch (Exception e) {
					throw new CassandraClusterException(
							"unable to connect to the host for cassandra host and port : " + host + ", " + port, e);
				}
			} else {
				throw new CassandraClusterException(
						"Unable to create connection for cassandra with an IP given in system evironment : " + host);

			}
		} catch (PropertiesConfigException e1) {
			throw new CassandraClusterException("unable to get the connection object for cassandra : ", e1);
		}
	}// end of method getPAASCassandraCluster
}
