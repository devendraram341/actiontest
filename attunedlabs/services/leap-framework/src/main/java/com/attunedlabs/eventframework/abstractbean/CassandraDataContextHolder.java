package com.attunedlabs.eventframework.abstractbean;

import java.util.HashMap;
import java.util.Map;

import org.apache.metamodel.DataContext;
import org.apache.metamodel.cassandra.CassandraCustomDataContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a singleton class used to hold the cassandra custom data context
 * object
 * 
 * @author Reactiveworks
 *
 */
public class CassandraDataContextHolder {

	private Map<String, DataContext> cassandraCustomDataContextHolderMap = new HashMap<>();
	private static CassandraDataContextHolder cassandraDataContextHolder = null;

	private CassandraDataContextHolder() {

	}

	/**
	 * This method is used to give the CassandraDataContextHolder singleton Object
	 * 
	 * @return {@link CassandraDataContextHolder}
	 */
	public synchronized static CassandraDataContextHolder getCassandraDataContextObject() {
		if (cassandraDataContextHolder == null) {
			cassandraDataContextHolder = new CassandraDataContextHolder();
			return cassandraDataContextHolder;
		} else {
			return cassandraDataContextHolder;
		}

	}// end of CassandraDataContextHolder

	/**
	 * This method returns the map containing the cassandra custom datacontext
	 * object against a schema
	 * 
	 * @return Map<String, DataContext> - key contain the schema for which the
	 *         custom cassandra object is stored.
	 */
	public Map<String, DataContext> getCassandraDataContextHolderMap() {
		return cassandraCustomDataContextHolderMap;
	}// end of method getCassandraDataContextHolderMap

}
