package com.attunedlabs.eventframework.abstractbean.util;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.metamodel.DataContext;
import org.apache.metamodel.DataContextFactory;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.schema.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.util.LeapConfigUtil;
import com.attunedlabs.config.util.LeapDefaultConstants;
import com.attunedlabs.config.util.PropertiesConfigException;
import com.datastax.driver.core.Cluster;

public class CassandraUtil {
	static Logger logger = LoggerFactory.getLogger(CassandraUtil.class);
//	private static Properties prop = null;
	private static Properties paasProp = null;
	private static Properties appsDeploymentEnvProp = null;
	public static final String PAAS_CONFIG_PROPERTY_FILE = "paasDeploymentCassandraDBConfig.properties";
	public static final String APPS_DEPLOYEMENT_ENVIRONMENT_CONFIG_PROPERTY_FILE = "globalAppDeploymentConfig.properties";
	public static final String DRIVER_CLASS_KEY = "cassandra_driver_class";
	public static final String URL_KEY = "cassandra_url";
	public static final String HOST_KEY = "cassandra_host";
	public static final String PORT_KEY = "cassandra_port";
	public static final String KEYSPACE_KEY = "cassandra_keyspace";
	public static final String DEPLOYMENT_ENVIRONMENT_KEY = "deploymentEnvConfig";
	public static final String CASSANDRA_IP = "CASSANDRA_IP";
	public static final String JDBC_CASSANDRA_PROTOCOL = "jdbc:cassandra://";
	public static final String JDBC_CASSANDRA_PROTOCOL_COLON_SEPERATED = ":";
	public static final String JDBC_CASSANDRA_PROTOCOL_SEPERATED = "/";
	public static final String LOCAL_CASSANDRA_DEPLOYMENT_ENVIRONMENT_KEY = "Local";
	public static final String PAAS_CASSANDRA_DEPLOYMENT_ENVIRONMENT_KEY = "PAAS";

	/**
	 * This static block is used to load the cassandra config properties
	 */

	/**
	 * This method is used to load the paas cassandra config
	 * 
	 * @return Properties Object
	 * @throws ConnectionConfigurationException
	 */
	public static Properties getPAASCassandraConfigProperties() throws ConnectionConfigurationException {
		if (paasProp != null) {
			return paasProp;
		} else {
			throw new ConnectionConfigurationException("unable to load property file = " + PAAS_CONFIG_PROPERTY_FILE);
		}
	}

	/**
	 * This method is used to get the cassandra connection
	 * 
	 * @return
	 * @throws CassandraConnectionException
	 */
	public Connection getCassandraConnection() throws CassandraConnectionException {
		String methodName = "getCassandraConnection";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Connection con = null;
		try {
			Class.forName(LeapConfigUtil.getGlobalPropertyValue(DRIVER_CLASS_KEY,LeapDefaultConstants.DEFAULT_DRIVER_CLASS_KEY));
			try {
				con = DriverManager.getConnection(LeapConfigUtil.getGlobalPropertyValue(URL_KEY,LeapDefaultConstants.DEFAULT_URL_KEY));
				logger.debug("{} Connection Object :{} ", LEAP_LOG_KEY, con);
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return con;
			} catch (SQLException e) {
				throw new CassandraConnectionException("unable to get the connection object for cassandra : ", e);
			}
		} catch (ClassNotFoundException | PropertiesConfigException e) {
			throw new CassandraConnectionException("unable to load the driver name for cassandra : ", e);
		}

	}// end of method

	public Cluster getCassandraCluster() throws CassandraClusterException {
		String methodName = "getCassandraCluster";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Cluster cluster = null;
//		Properties prop = null;
		try {
//			prop = CassandraUtil.getCassandraConfigProperties();
			String host = LeapConfigUtil.getGlobalPropertyValue(CassandraUtil.HOST_KEY,LeapDefaultConstants.DEFAULT_HOST_KEY);
			int port = Integer.parseInt(LeapConfigUtil.getGlobalPropertyValue(CassandraUtil.PORT_KEY,LeapDefaultConstants.DEFAULT_PORT_KEY));
			try {
				cluster = Cluster.builder().addContactPoint(host).withPort(port).build();
				logger.debug("{} Connection Object :{} ", LEAP_LOG_KEY, cluster);
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return cluster;
			} catch (Exception e) {
				throw new CassandraClusterException("unable to connect to the host for cassandra : ", e);
			}
		} catch (NumberFormatException | PropertiesConfigException e1) {
			throw new CassandraClusterException("unable to get the connection object for cassandra : ", e1);
		}

	}// end of method

	public UpdateableDataContext getUpdateableDataContextForCassandra(Connection connection) {
		UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
		return dataContext;
	}

	public DataContext getDataContextForCassandraByCluster(Cluster cluster, String keyspace) {
		DataContext dataContext = DataContextFactory.createCassandraDataContext(cluster, keyspace);
		return dataContext;
	}

	protected Table getTableForDataContext(DataContext datacontext, String tableName) {
		Table table = datacontext.getTableByQualifiedLabel(tableName);
		return table;

	}
}
