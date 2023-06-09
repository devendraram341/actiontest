package com.attunedlabs.integrationfwk.jdbcIntactivity.config.persistence.impl;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.camel.Exchange;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.UpdateSummary;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.delete.DeleteFrom;
import org.apache.metamodel.insert.InsertInto;
import org.apache.metamodel.jdbc.JdbcDataContext;
import org.apache.metamodel.query.FilterItem;
import org.apache.metamodel.query.Query;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.update.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.attunedlabs.integrationfwk.config.jaxb.FieldMapper;
import com.attunedlabs.integrationfwk.config.jaxb.JDBCIntActivity;
import com.attunedlabs.integrationfwk.jdbcIntactivity.config.helper.JdbcIntActivityConfigHelper;
import com.attunedlabs.integrationfwk.jdbcIntactivity.config.helper.JdbcIntActivityConfigurationException;
import com.attunedlabs.integrationfwk.jdbcIntactivity.config.helper.JdbcIntActivityExecutionException;
import com.attunedlabs.integrationfwk.jdbcIntactivity.config.persistence.IJdbcIntActivityService;
import com.attunedlabs.integrationfwk.jdbcIntactivity.config.persistence.JdbcIntActivityPersistenceException;

public class JdbcIntActivityConfigDAO implements IJdbcIntActivityService {

	private Logger logger = LoggerFactory.getLogger(JdbcIntActivityConfigDAO.class.getName());
	private static final String WHERE_COLKEY = "constraintOne";
	private static final String ASTERIKS = "*";
	private static final String CONDITION_PATTERN = " AND | and | OR | or ";

	/**
	 * insert operation DAO method to process the configured query
	 * 
	 * @param datacontext
	 * @param table
	 * @param insertColumnKeySet
	 * @param insertListOfValues
	 * @return //#TODO have to figure out success and failure returns
	 * @throws JdbcIntActivityExecutionException
	 */
	@Override
	public int insertActivityConfigParams(JdbcDataContext datacontext, Table table, Set<String> insertColumnKeySet,
			List<Object> insertListOfValues) throws JdbcIntActivityExecutionException {
		String methodName = "insertActivityConfigParams";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		logger.debug("{}  dataContext: {}, table: {}, ColumnKey :{}, ColumnValues:{} ", LEAP_LOG_KEY, datacontext,
				table, insertColumnKeySet, insertListOfValues);
		InsertInto valuesInsertObject = new InsertInto(table);
		UpdateSummary executeUpdate;
		if (insertColumnKeySet.isEmpty()) {
			int valuesCount = insertListOfValues.size();
			for (int i = 0; i < valuesCount; i++) {
				insertListOfValues.set(i,
						insertListOfValues.get(i).toString().replaceAll(JdbcIntActivityConfigHelper.REPLACER, ","));
			}
			for (int i = 0; i < valuesCount; i++) {
				valuesInsertObject.value(i, insertListOfValues.get(i));
			}
			try {
				executeUpdate = datacontext.executeUpdate(valuesInsertObject);
			} catch (Exception e) {
				throw new JdbcIntActivityExecutionException("Unable to insert values into table - " + table.getName(),
						e);
			}
		} // .. end of if, processing for non empty columnNames
		else if (!insertColumnKeySet.isEmpty() && insertColumnKeySet.size() == insertListOfValues.size()) {
			int count = 0;
			for (String columnNames : insertColumnKeySet) {
				valuesInsertObject.value(columnNames, insertListOfValues.get(count));
				count++;
			}
			try {
				executeUpdate = datacontext.executeUpdate(valuesInsertObject);
			} catch (Exception e) {
				throw new JdbcIntActivityExecutionException("Unable to insert values into table - " + table.getName(),
						e);
			}
		} // .. end of the else-if, giving support for the queries without
			// column names
		else {
			throw new JdbcIntActivityExecutionException("Unable to Insert into the columns -" + insertColumnKeySet);
		} // .. as the executeUpdate is or returnType Void, manually returning
			// an integer(1), always
		Integer integer = executeUpdate.getInsertedRows().get();
		logger.info("{} inserted rows total count : {} ", LEAP_LOG_KEY, integer);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return integer;
	}// ..end of the method

	/**
	 * overloaded insert operation DAO method to process the configured query for
	 * cassandra
	 * 
	 * @param updateableDatacontext : UpdateableDataContext Object of apache
	 *                              metamodel
	 * @param table                 : Table Object of apache metamodel
	 * @param insertColumnKeySet
	 * @param insertListOfValues
	 * @return //#TODO have to figure out success and failure returns
	 * @throws JdbcIntActivityExecutionException
	 * @throws JdbcIntActivityConfigurationException
	 */

	public int insertActivityConfigParams(String dbType, Exchange exchange, JDBCIntActivity configObject,
			UpdateableDataContext updateableDatacontext, Table table, Set<String> insertColumnKeySet,
			List<Object> insertListOfValues, Map<String, String> setOfValuesProcessed, Document xmlDocument)
			throws JdbcIntActivityExecutionException, JdbcIntActivityConfigurationException {
		String methodName = "insertActivityConfigParams";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		logger.debug("{} dataContext: {},table: {}, ColumnKey: {}, ColumnValues: {},  ", LEAP_LOG_KEY,
				updateableDatacontext, table, insertColumnKeySet, insertListOfValues);
		logger.trace("{} insertCOlumnKeySet size : {}", LEAP_LOG_KEY, insertColumnKeySet.size());
		logger.trace("{} insertListOfValues size : {}", LEAP_LOG_KEY, insertListOfValues.size());

		List<FieldMapper> fieldMappers = new ArrayList<>();
		if (configObject != null) {
			if (configObject.getDbmsMapper() != null)
				fieldMappers = configObject.getDbmsMapper().getFieldMapper();
		}

		JdbcIntActivityConfigHelper configHelper = new JdbcIntActivityConfigHelper();
		InsertInto valuesInsertObject = new InsertInto(table);
		if (insertColumnKeySet.isEmpty()) {
			int valuesCount = insertListOfValues.size();
			for (int i = 0; i < valuesCount; i++) {
				insertListOfValues.set(i, insertListOfValues.get(i).toString());
			}
			logger.debug("{} updated InserListOfValues : {}", LEAP_LOG_KEY, insertListOfValues);
			for (int i = 0; i < valuesCount; i++) {
				valuesInsertObject.value(i, insertListOfValues.get(i));
			}
			try {
				updateableDatacontext.executeUpdate(valuesInsertObject);
			} catch (Exception e) {
				throw new JdbcIntActivityExecutionException("Unable to insert values into table - " + table.getName(),
						e);
			}
		} // .. end of if, processing for non empty columnNames
		else if (!insertColumnKeySet.isEmpty() /*
												 * && insertColumnKeySet.size() == insertListOfValues.size()
												 */ ) {
			int count = 0;
			for (int i = 0; i < insertListOfValues.size(); i++) {
				insertListOfValues.set(i,
						insertListOfValues.get(i).toString().replaceAll(JdbcIntActivityConfigHelper.REPLACER, ","));
			}
			logger.debug("{} listOfValues : {} ", LEAP_LOG_KEY, insertListOfValues);
			for (String columnNames : insertColumnKeySet) {
				logger.trace("{} columnName : {}", LEAP_LOG_KEY, columnNames);
				String field = (String) insertListOfValues.get(count);
				logger.trace("{} field : {}", LEAP_LOG_KEY, field);
				// setOfValuesProcessed;
				if (setOfValuesProcessed.containsKey(field)) {
					try {
						String val = configHelper.xpathProcessingOnInputXml(setOfValuesProcessed.get(field),
								xmlDocument);
						boolean declared = false;
						for (FieldMapper fieldMapper : fieldMappers) {
							if (fieldMapper.getField().equals(field) && fieldMapper.getDataType().equals("timestamp")) {
								valuesInsertObject.value(columnNames, JdbcIntActivityConfigHelper
										.parseIfDateexists(dbType, exchange, val, fieldMapper.getDateFormat()));
								declared = true;
							}

						}
						if (!declared)
							valuesInsertObject.value(columnNames, val.trim());

						logger.trace("{} setOfValueProcessed : {}", LEAP_LOG_KEY, setOfValuesProcessed.get(field));
					} catch (JdbcIntActivityConfigurationException e) {
						throw new JdbcIntActivityConfigurationException("Unable to get the value using the Xpath");
					}

				} else {
					valuesInsertObject.value(columnNames, new Long(insertListOfValues.get(count).toString()));
					logger.debug("{} insertListOfValues : {}", LEAP_LOG_KEY, insertListOfValues.get(count));
				}
				count++;
			}
			try {
				updateableDatacontext.executeUpdate(valuesInsertObject);
			} catch (Exception e) {
				e.printStackTrace();
				throw new JdbcIntActivityExecutionException("Unable to insert values into table - " + table.getName(),
						e);
			}
		} // .. end of the else-if, giving support for the queries without
			// column names
		else {
			throw new JdbcIntActivityExecutionException("Unable to Insert into the columns -" + insertColumnKeySet);
		} // .. as the executeUpdate is or returnType Void, manually returning
			// an integer(1), always
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return 1;
	}// ..end of the method

	/**
	 * update operation DAO method to process the configured query
	 * 
	 * @param datacontext
	 * @param table
	 * @param updateColumnKeySet
	 * @param updateListOfValues
	 * @return //#TODO have to figure out the successful and error returns
	 * @throws JdbcIntActivityExecutionException
	 * @throws JdbcIntActivityPersistenceException
	 */
	@Override
	public int updateActivityConfigParams(JdbcDataContext datacontext, Table table, Set<String> updateColumnKeySet,
			List<Object> updateListOfValues, Map<String, Map<String, Object>> mapOfConstraints)
			throws JdbcIntActivityPersistenceException {
		String methodName = "updateActivityConfigParams";
		logger.debug("{} entered into the method {}, ColumnKey-ColumnValue: {}-{} ", LEAP_LOG_KEY, methodName,
				updateColumnKeySet, updateListOfValues);

		Update valueUpdateObject = new Update(table);
		int counter = 0;
		if (!updateColumnKeySet.isEmpty()) {
			for (String updtcolumnNames : updateColumnKeySet) {
				valueUpdateObject.value(updtcolumnNames, updateListOfValues.get(counter));
				counter++;
			}
			try {
				String frstCol = null;
				for (String key : mapOfConstraints.get(WHERE_COLKEY).keySet()) {
					frstCol = key;
				}
				datacontext.executeUpdate(
						valueUpdateObject.where(frstCol).eq(mapOfConstraints.get(WHERE_COLKEY).get(frstCol)));
			} catch (Exception e) {
				throw new JdbcIntActivityPersistenceException(
						"Unable to update the value for the table - " + table.getName(), e);
			}
		} else {
			throw new JdbcIntActivityPersistenceException("Unable to update the columns -" + updateColumnKeySet);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return 1;
	}// ..end of method

	/**
	 * update operation DAO method to process the configured query
	 * 
	 * @param datacontext
	 * @param table
	 * @param updateColumnKeySet
	 * @param updateListOfValues
	 * @return //#TODO have to figure out the successful and error returns
	 * @throws JdbcIntActivityExecutionException
	 * @throws JdbcIntActivityPersistenceException
	 * @throws JdbcIntActivityConfigurationException
	 */
	public int updateActivityConfigParamsForCassandra(UpdateableDataContext datacontext, Table table,
			Set<String> updateColumnKeySet, List<Object> updateListOfValues, String whereConstraints,
			Map<String, String> setOfValuesProcessed, Document xmlDocument)
			throws JdbcIntActivityPersistenceException, JdbcIntActivityConfigurationException {
		String methodName = "updateActivityConfigParamsForCassandra";
		logger.debug("{} entered into the method {}, columeKey-columnValue = {}-{} ", LEAP_LOG_KEY, methodName,
				updateColumnKeySet, updateListOfValues);
		JdbcIntActivityConfigHelper configHelper = new JdbcIntActivityConfigHelper();
		Update valueUpdateObject = new Update(table);
		logger.trace("{} setOfValuesProcessed ::: {}", LEAP_LOG_KEY, setOfValuesProcessed.toString());
		int counter = 0;
		if (!updateColumnKeySet.isEmpty()) {
			for (String updtcolumnNames : updateColumnKeySet) {
				String field = (String) updateListOfValues.get(counter);
				logger.trace("{} field : {}", LEAP_LOG_KEY, field);
				// setOfValuesProcessed;
				if (setOfValuesProcessed.containsKey(field)) {
					try {
						String val = configHelper.xpathProcessingOnInputXml(setOfValuesProcessed.get(field),
								xmlDocument);
						logger.trace("{} val : {}", LEAP_LOG_KEY, val);
						valueUpdateObject.value(updtcolumnNames, val);
						logger.debug("{} setOfValueProcessed : {}", LEAP_LOG_KEY, setOfValuesProcessed.get(field));
						logger.debug("{} valueUpdateObject : {}", LEAP_LOG_KEY, valueUpdateObject.toString());
					} catch (JdbcIntActivityConfigurationException e) {
						throw new JdbcIntActivityConfigurationException("Unable to get the value using the Xpath");
					}
				} else {
					// logger.debug("!!!!!!!");
					valueUpdateObject.value(updtcolumnNames, removeQuoteFromQuery(field));
					// logger.debug("updateList of values : " + );
				}

				/*
				 * valueUpdateObject.value(updtcolumnNames,
				 * removeQuoteFromQuery(updateListOfValues.get(counter).toString ()));
				 */
				counter++;
			}
			try {
				// whereConstraints=createWhereConditionForFilterItem(whereConstraints);
				logger.debug("{} Before where constaints : {}" + whereConstraints);
				String arr[] = whereConstraints.trim().split(" ");
				StringBuffer mapValue = new StringBuffer();
				for (int i = 0; i < arr.length; i += 2) {
					String[] str = arr[i].split("=");
					mapValue.append(str[0]);
					mapValue.append("=");
					mapValue.append(
							"'" + configHelper.xpathProcessingOnInputXml(setOfValuesProcessed.get(str[1]), xmlDocument)
									+ "'");
					if ((i + 2) < arr.length) {
						mapValue.append(" AND ");
					}
				}

				whereConstraints = mapValue.toString();
				logger.debug("{} After where Constraints : {}", LEAP_LOG_KEY, whereConstraints);

				/*
				 * String[] arr = whereConstraints.split(" AND ");
				 * 
				 * logger.debug("arr : "+arr[0]); if (arr.length == 1) { arr =
				 * arr[0].split(" and "); } StringBuffer strBfr = new StringBuffer(); int i=0;
				 * do{ String str[] = arr[i].split("="); if(str.length>2){ throw new
				 * JdbcIntActivityPersistenceException("Malformed Query, contains multiple = in where clause"
				 * ); }else{ str[1] =
				 * configHelper.xpathProcessingOnInputXml(setOfValuesProcessed. get(str[0]),
				 * xmlDocument); logger.debug("value [1] : "+str[1]); strBfr.append(str[0]);
				 * strBfr.append("="); strBfr.append("'"+str[1]+"'"); if(i<arr.length)
				 * strBfr.append(" AND "); } }while(i<arr.length); whereConstraints =
				 * strBfr.toString();
				 */
				FilterItem fitem = new FilterItem(whereConstraints.trim());
				datacontext.executeUpdate(valueUpdateObject.where(fitem));
			} catch (Exception e) {
				throw new JdbcIntActivityPersistenceException(
						"Unable to update the value for the table - " + e + " " + table.getName(), e);
			}
		} else {
			throw new JdbcIntActivityPersistenceException("Unable to update the columns -" + updateColumnKeySet);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return 1;
	}// ..end of method

	/**
	 * To replace qutoe in query which come before where condition
	 * 
	 * @param value
	 * @return
	 */
	private String removeQuoteFromQuery(String value) {

		if (value != null) {
			if (!value.isEmpty()) {
				value = value.replace("'", "");
			}
		}
		return value;
	}

	/**
	 * select operation DAO method to process the configured query
	 * 
	 * @param datacontext
	 * @param selectQuery SELECT (amount,dateconfigured) FROM testtable WHERE amount
	 *                    = ""
	 * @return //#TODO have to figure out the successful and error response
	 * @throws JdbcIntActivityPersistenceException
	 */
	@Override
	public Row selectActivityConfigParams(DataContext datacontext, Table table1, Table table2,
			List<String> columnSelectKeySet, Map<String, Map<String, Object>> mapOfConstraints, boolean isJoin,
			String joinType) throws JdbcIntActivityPersistenceException {

		if (!isJoin) {
			return performSelectWhenNotJoin(datacontext, table1, columnSelectKeySet, mapOfConstraints);
		} else {
			String[] colKeyArr = new String[columnSelectKeySet.size()];
			String frstCol = null;
			for (String key : mapOfConstraints.get(WHERE_COLKEY).keySet()) {
				frstCol = key;
			}
			colKeyArr = columnSelectKeySet.toArray(colKeyArr);
			Query q;
			switch (joinType) {
			case "INNER JOIN":
				q = datacontext.query().from(table1).innerJoin(table2)
						.on(frstCol, (String) mapOfConstraints.get(WHERE_COLKEY).get(frstCol)).select(colKeyArr)
						.toQuery();

				return performSelectForJoinQueries(datacontext, q);
			case "LEFT OUTER JOIN":
				q = datacontext.query().from(table1).leftJoin(table2)
						.on(frstCol, (String) mapOfConstraints.get(WHERE_COLKEY).get(frstCol)).select(colKeyArr)
						.toQuery();
				return performSelectForJoinQueries(datacontext, q);
			case "RIGHT OUTER JOIN":
				q = datacontext.query().from(table1).rightJoin(table2)
						.on(frstCol, (String) mapOfConstraints.get(WHERE_COLKEY).get(frstCol)).select(colKeyArr)
						.toQuery();
				return performSelectForJoinQueries(datacontext, q);
			default:
				throw new JdbcIntActivityPersistenceException(
						"Unable to perform the operation specifed, which doesn't belongs to an of the join operations..");
			}
		}
	}// ..end of the method

	/**
	 * delete operation DAO method to process the configured query
	 * 
	 * @param datacontext
	 * @param table
	 * @return //#TODO have to figure out successful and error responses to return
	 * @throws JdbcIntActivityPersistenceException
	 */
	@Override
	public int deleteActivityConfigParams(JdbcDataContext datacontext, Table table,
			Map<String, Map<String, Object>> mapOfConstraints) throws JdbcIntActivityPersistenceException {
		String frstCol = null;
		try {
			for (String key : mapOfConstraints.get(WHERE_COLKEY).keySet()) {
				frstCol = key;
			}

			Object whereColVal = mapOfConstraints.get(WHERE_COLKEY).get(frstCol);
			if (whereColVal != null) {
				datacontext.executeUpdate(new DeleteFrom(table).where(frstCol).eq(whereColVal));
				return 1;
			} else {
				datacontext.executeUpdate(new DeleteFrom(table));
				return 1;
			}

		} catch (Exception e) {
			throw new JdbcIntActivityPersistenceException(
					"Unable to delete the values from the table - " + table.getName(), e);
		}
	}// ..end of the method

	/**
	 * delete operation DAO method to process the configured query
	 * 
	 * @param datacontext
	 * @param table
	 * @return //#TODO have to figure out successful and error responses to return
	 * @throws JdbcIntActivityPersistenceException
	 * @throws JdbcIntActivityConfigurationException
	 */
	public int deleteActivityConfigParamsForCassandra(UpdateableDataContext datacontext, Table table,
			String whereConstraints, Map<String, String> setOfValuesProcessed, Document xmlDocument)
			throws JdbcIntActivityPersistenceException, JdbcIntActivityConfigurationException {
		if (whereConstraints != null) {
			whereConstraints = createWhereConditionForFilterItem(whereConstraints, setOfValuesProcessed, xmlDocument);
			logger.trace("{} where condition after replacement : {}", LEAP_LOG_KEY, whereConstraints);
			FilterItem fitem = new FilterItem(whereConstraints);
			datacontext.executeUpdate(new DeleteFrom(table).where(fitem));
			return 1;

		} else {
			datacontext.executeUpdate(new DeleteFrom(table));
			return 1;
		}

	}// ..end of the method

	private String createWhereConditionForFilterItem(String whereConstraints, Map<String, String> setOfValuesProcessed,
			Document xmlDocument) throws JdbcIntActivityPersistenceException, JdbcIntActivityConfigurationException {
		String methodName = "createWhereConditionForFilterItem";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		StringBuffer mapValue = new StringBuffer();
		JdbcIntActivityConfigHelper configHelper = new JdbcIntActivityConfigHelper();
		String[] arrWhereCondition = whereConstraints.split(CONDITION_PATTERN);
		if (arrWhereCondition != null && arrWhereCondition.length > 0) {
			String key = null;
			String oldValue = null;
			int i = 0;
			for (String whereCondition : arrWhereCondition) {
				String[] arrCondition = whereCondition.split("=");
				if (arrCondition != null && arrCondition.length > 0) {
					logger.trace("{}  where Constraints before adding single quotes :  {}" ,LEAP_LOG_KEY, whereConstraints);
					i++;
					key = arrCondition[0].trim();
					oldValue = arrCondition[1].trim();
					// String
					// newValue=substituteQuotesWithWhereConditionValue(oldValue);

					// whereConstraints=whereConstraints.replace(oldValue,
					// newValue);

					logger.trace("{} oldvalue : {}, after generating  where Constraints : {}", LEAP_LOG_KEY, oldValue,
							whereConstraints);
					logger.trace("{} where constaints : {}" + whereConstraints);
					logger.trace("{} setOfValuesProcessed {}" + setOfValuesProcessed.toString());
					mapValue.append(key);
					mapValue.append("=");
					mapValue.append("'" + configHelper
							.xpathProcessingOnInputXml(setOfValuesProcessed.get(oldValue.trim()), xmlDocument) + "'");
					if (i <= arrCondition.length)
						mapValue.append(" AND ");
				} else {
					throw new JdbcIntActivityPersistenceException(
							"where condition for delete operation is not proper : " + whereConstraints);
				}
			}
		} else {
			logger.trace("{} only one where condition", LEAP_LOG_KEY);
			String[] arrCondition = whereConstraints.split("=");
			if (arrCondition != null && arrCondition.length > 0) {
				String key = arrCondition[0].trim();
				String oldValue = arrCondition[1].trim();
				logger.trace("{} oldvalue : {},after generating  where Constraints : {}", LEAP_LOG_KEY, oldValue,
						whereConstraints);
				logger.trace("{} where constaints : {} ", whereConstraints);
				logger.trace("{} Service request : {} ", setOfValuesProcessed.get(oldValue));
				// String arr[] = whereConstraints.trim().split(" ");
				// String[] str = arrCondition[i].split("=");
				mapValue.append(key);
				mapValue.append("=");
				mapValue.append("'"
						+ configHelper.xpathProcessingOnInputXml(setOfValuesProcessed.get(oldValue.trim()), xmlDocument)
						+ "'");
				// whereConstraints = mapValue.toString();
				// logger.debug("whereConstraints : " + whereConstraints);

				// whereConstraints = mapValue.toString();
				/*
				 * String newValue=substituteQuotesWithWhereConditionValue(oldValue);
				 * whereConstraints=whereConstraints.replace(oldValue, newValue);
				 */
			} else {
				throw new JdbcIntActivityPersistenceException(
						"where condition for delete operation is not proper : " + whereConstraints);
			}
		}
		logger.trace("{} string generated after adding quotes to where constraintes : {}" ,LEAP_LOG_KEY, whereConstraints);
		whereConstraints = mapValue.toString();
		return whereConstraints;
	}

	@SuppressWarnings("unused")
	private String substituteQuotesWithWhereConditionValue(String value) {
		logger.debug("{} substituteQuotesWithWhereConditionValue method of JDBCIntActivityConfigDAO", LEAP_LOG_KEY);

		return "'" + value.trim() + "'";
	}

	/**
	 * called after checking the Query , if its not a join based
	 * 
	 * @param datacontext
	 * @param table
	 * @param columnSelectKeySet
	 * @param mapOfConstraints
	 * @return
	 * @throws JdbcIntActivityPersistenceException
	 */
	private Row performSelectWhenNotJoin(DataContext datacontext, Table table, List<String> columnSelectKeySet,
			Map<String, Map<String, Object>> mapOfConstraints) throws JdbcIntActivityPersistenceException {

		logger.trace("{} selectActivityConfigParams().. - {} - {}", LEAP_LOG_KEY, columnSelectKeySet, mapOfConstraints);
		DataSet dataSet = null;
		String[] colKeyArr = new String[columnSelectKeySet.size()];
		colKeyArr = columnSelectKeySet.toArray(colKeyArr);
		if (!ASTERIKS.equals(colKeyArr[1]) || colKeyArr[1] != ASTERIKS) {
			return performSelectColumnsWithConstraints(datacontext, table, colKeyArr, mapOfConstraints, dataSet);
		} // ..end of if, condition check when column names are given

		else if ((ASTERIKS.equals(colKeyArr[1])
				&& (mapOfConstraints != null && mapOfConstraints.get(WHERE_COLKEY) != null))
				|| (colKeyArr[1] == ASTERIKS
						&& (mapOfConstraints != null && mapOfConstraints.get(WHERE_COLKEY) != null))) {
			return performSelectAllwithConstraints(datacontext, table, colKeyArr, mapOfConstraints, dataSet);
		} // ..end of else-if , condition where checking for '*' with column

		else {
			return performSelectAll(datacontext, table);
		} // ..end of else, condition check for '*' and no column names
	}// ..end of the method

	/**
	 * called when select columns from table with constraint
	 * 
	 * @param datacontext
	 * @param table
	 * @param colKeyArr
	 * @param mapOfConstraints
	 * @param dataSet
	 * @return rowsetString
	 * @throws JdbcIntActivityPersistenceException
	 */
	private Row performSelectColumnsWithConstraints(DataContext datacontext, Table table, String[] colKeyArr,
			Map<String, Map<String, Object>> mapOfConstraints, DataSet dataSet)
			throws JdbcIntActivityPersistenceException {
		String methodName = "performSelectColumnsWithConstraints";
		logger.debug("{} entered into the method {}, colKeyArr={}, mapOfConstraints={}", LEAP_LOG_KEY, methodName,
				colKeyArr, mapOfConstraints);
		String frstCol = null;
		for (String key : mapOfConstraints.get(WHERE_COLKEY).keySet()) {
			frstCol = key;
		}
		try {
			DataSet ds = dataSet;
			Query q = datacontext.query().from(table).select(colKeyArr).where(frstCol)
					.eq(mapOfConstraints.get(WHERE_COLKEY).get(frstCol)).toQuery();
			ds = datacontext.executeQuery(q);
			Row row = null;
			try {
				while (ds.next()) {
					row = ds.getRow();
					logger.trace("{} checking row obj in select with constraint: {}", LEAP_LOG_KEY, row.getValues()[0]);
				}
			} finally {
				ds.close();
			}
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return row;
		} catch (Exception e) {
			throw new JdbcIntActivityPersistenceException(
					"Unable to retreive DataSet from the table - " + table.getName(), e);
		}
	}// ..end of the method

	/**
	 * called when select * with constraints
	 * 
	 * @param datacontext
	 * @param table
	 * @param colKeyArr
	 * @param mapOfConstraints
	 * @param dataSet
	 * @return rowsetString
	 * @throws JdbcIntActivityPersistenceException
	 */
	private Row performSelectAllwithConstraints(DataContext datacontext, Table table, String[] colKeyArr,
			Map<String, Map<String, Object>> mapOfConstraints, DataSet dataSet)
			throws JdbcIntActivityPersistenceException {
		String frstCol = null;
		for (String key : mapOfConstraints.get(WHERE_COLKEY).keySet()) {
			frstCol = key;
		}
		try {
			DataSet ds = dataSet;
			Query q = datacontext.query().from(table).select(colKeyArr).where(frstCol)
					.eq(mapOfConstraints.get(WHERE_COLKEY).get(frstCol)).toQuery();
			ds = datacontext.executeQuery(q);
			Row row = null;
			try {
				while (ds.next()) {
					row = ds.getRow();
				}
			} finally {
				ds.close();
			}
			return row;
		} catch (Exception e) {
			throw new JdbcIntActivityPersistenceException(
					"Unable to retreive DataSet from the table - " + table.getName(), e);
		}
	}// ..end of the method

	/**
	 * called when select * from table is configured
	 * 
	 * @param datacontext
	 * @param table
	 * @param dataSet
	 * @return rowsetString
	 */
	private Row performSelectAll(DataContext datacontext, Table table) {
		logger.trace("{} routed when there is no column names..", LEAP_LOG_KEY);
		Query q = datacontext.query().from(table).selectAll().toQuery();
		DataSet ds = datacontext.executeQuery(q);
		Row row = null;
		try {
			while (ds.next()) {
				row = ds.getRow();
			}
		} finally {
			ds.close();
		}
		return row;
	}// ..end of the method

	/**
	 * to get the result-set with INNER_JOIN operation on tables eg: SELECT gid,
	 * first_name, last_name, pid, gardener_id, plant_name FROM Gardners INNER JOIN
	 * Plantings ON gid = gardener_id
	 * 
	 * @param datacontext
	 * @param table1
	 * @param table2
	 * @param columnSelectKeySet
	 * @param mapOfConstraints
	 * @return row string from ineerJoin operation
	 */
	private Row performSelectForJoinQueries(DataContext datacontext, Query q) {
		DataSet dataSet = null;
		dataSet = datacontext.executeQuery(q);
		Row row = null;
		try {
			while (dataSet.next()) {
				row = dataSet.getRow();
			}
		} finally {
			dataSet.close();
		}
		return row;
	}// ..end of the method
}
