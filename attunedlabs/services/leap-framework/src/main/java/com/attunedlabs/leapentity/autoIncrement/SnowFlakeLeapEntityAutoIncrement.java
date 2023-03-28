package com.attunedlabs.leapentity.autoIncrement;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.relops.snowflake.Snowflake;
/**
 * This class is type of autoIncrement snowflake type impl for Entity
 * @author Nishant
 *
 */
public class SnowFlakeLeapEntityAutoIncrement implements ILeapEntityAutoIncrement {

	private static Map<Integer, SnowFlakeLeapEntityAutoIncrement> snowFlakeMap = new ConcurrentHashMap<Integer, SnowFlakeLeapEntityAutoIncrement>();
	private Snowflake snowflake;
   
	/**
	 * singletone 
	 * @param nodeId 
	 */
	private SnowFlakeLeapEntityAutoIncrement(Integer nodeId) {
		snowflake = new Snowflake(nodeId);
	};
    
	/**
	 * to get the instance based on the nodeId
	 * @param nodeId
	 * @return
	 */
	public static SnowFlakeLeapEntityAutoIncrement getInstance(Integer nodeId) {
		if (snowFlakeMap.containsKey(nodeId)) {
			return snowFlakeMap.get(nodeId);
		} else {
			SnowFlakeLeapEntityAutoIncrement autoIncrement = new SnowFlakeLeapEntityAutoIncrement(nodeId);
			snowFlakeMap.put(nodeId, autoIncrement);
			return autoIncrement;
		}
	}
    
	/**
	 * it generate the value based on the snowflake impl 
	 */
	@Override
	public Object getAutoIncrementValue(String columnType) {
		long autoIncrementValue = snowflake.next();
		if (columnType.equalsIgnoreCase("string")) {
			return "" + autoIncrementValue;
		}
		return autoIncrementValue;
	}

}
