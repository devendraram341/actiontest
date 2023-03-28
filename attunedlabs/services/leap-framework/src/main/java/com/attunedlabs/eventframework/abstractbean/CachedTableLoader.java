package com.attunedlabs.eventframework.abstractbean;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.schema.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CachedTableLoader {
	public static final String SELECT_OPERATION = "SELECT";
	public static final String CASSANDRA_DB_TYPE = "CASSANDRA";
	private static CachedTableLoader cachedTableLoader;
	public final Map<String, Map<String, Table>> _cachedDataContextTables;

	private CachedTableLoader() {
		_cachedDataContextTables = new ConcurrentHashMap<>();
	}

	private static void instantiate() {
		if (cachedTableLoader == null)
			cachedTableLoader = new CachedTableLoader();
	}

	public static Table getTableByQualifiedName(String tableName, String datasourceKey, DataContext dataContext) {
		instantiate();
		if (cachedTableLoader._cachedDataContextTables.containsKey(datasourceKey)) {

			Map<String, Table> tablesDataMap = cachedTableLoader._cachedDataContextTables.get(datasourceKey);
			if (tablesDataMap.containsKey(tableName)) {
				return tablesDataMap.get(tableName);
			} else {
				Table table = dataContext.getTableByQualifiedLabel(tableName);
				if (table != null) {
					tablesDataMap.put(tableName, table);
					cachedTableLoader._cachedDataContextTables.put(datasourceKey, tablesDataMap);
				}
				return table;
			}

		}
		Table table = dataContext.getTableByQualifiedLabel(tableName);
		Map<String, Table> tablesDataMap = new HashMap<>();
		if (table != null) {
			tablesDataMap.put(tableName, table);
			cachedTableLoader._cachedDataContextTables.put(datasourceKey, tablesDataMap);
		}
		return table;
	}

	public static String generateCachedKey(com.attunedlabs.datacontext.jaxb.DataContext dataContext, String operation) {
		String dbHost = dataContext.getDbHost();
		String dbPort = dataContext.getDbPort();
		String dbSchema = dataContext.getDbSchema();
		String dbUser = dataContext.getDbUser();
		String dbPassword = dataContext.getDbPassword();
		String dbUrl = dataContext.getDbUrl();
		String dbDriver = dataContext.getDbDriver();
		return checkDBProperties(dataContext, operation) + dbHost + dbPort + dbUrl + dbDriver + dbUser + dbPassword
				+ dbSchema;
	}

	public static String generateCachedKey(com.attunedlabs.datacontext.jaxb.DataContext dataContext) {
		String dbHost = dataContext.getDbHost();
		String dbPort = dataContext.getDbPort();
		String dbSchema = dataContext.getDbSchema();
		String dbUser = dataContext.getDbUser();
		String dbPassword = dataContext.getDbPassword();
		String dbUrl = dataContext.getDbUrl();
		String dbDriver = dataContext.getDbDriver();
		String dbType = checkDBProperties(dataContext);
		return getCacheGeneratedKey(dbType, dbHost, dbPort, dbUrl, dbDriver, dbUser, dbPassword, dbSchema);
	}

	public static String checkDBProperties(com.attunedlabs.datacontext.jaxb.DataContext dataContext) {
		instantiate();
		String dbHost = dataContext.getDbHost();
		String dbPort = dataContext.getDbPort();
		String dbSchema = dataContext.getDbSchema();
		String dbType = dataContext.getDbType().toUpperCase();
		String dbUrl = dataContext.getDbUrl();
		String dbDriver = dataContext.getDbDriver();
		String userName = dataContext.getDbUser();
		String password = dataContext.getDbPassword();

		if (checkNonEmptyString(dbHost) && checkNonEmptyString(dbPort) && checkNonEmptyString(dbSchema)
				&& dbType.equals(CASSANDRA_DB_TYPE)) {
			return "cassandra";
		} else if (checkNonEmptyString(dbUrl) && checkNonEmptyString(dbDriver))
			return "jdbc";
		return null;
	}

	private static String getCacheGeneratedKey(String dbType, String host, String port, String url, String driver,
			String username, String password, String schema) {
		return (dbType + host + port + url + driver + username + password + schema);
	}

	public static String checkDBProperties(com.attunedlabs.datacontext.jaxb.DataContext dataContext, String operation) {
		instantiate();
		String dbHost = dataContext.getDbHost();
		String dbPort = dataContext.getDbPort();
		String dbSchema = dataContext.getDbSchema();
		String dbType = dataContext.getDbType().toUpperCase();
		String dbUrl = dataContext.getDbUrl();
		String dbDriver = dataContext.getDbDriver();

		if (checkNonEmptyString(dbHost) && checkNonEmptyString(dbPort) && checkNonEmptyString(dbSchema)
				&& dbType.equals(CASSANDRA_DB_TYPE) && operation.equals(SELECT_OPERATION))
			return "cassandra";
		else if (checkNonEmptyString(dbUrl) && checkNonEmptyString(dbDriver) && !operation.equals(SELECT_OPERATION))
			return "jdbc";
		return null;
	}

	public static boolean checkNonEmptyString(String checkString) {
		return checkString != null && !checkString.isEmpty();
	}

	public static boolean checkEmptyString(String checkString) {
		if (checkString == null)
			return true;
		return checkString.trim().isEmpty();
	}

}
