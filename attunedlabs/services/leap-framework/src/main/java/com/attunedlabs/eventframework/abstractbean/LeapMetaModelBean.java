package com.attunedlabs.eventframework.abstractbean;

import static com.attunedlabs.eventframework.abstractbean.CachedTableLoader.generateCachedKey;
import static com.attunedlabs.eventframework.abstractbean.CachedTableLoader.getTableByQualifiedName;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.UpdateSummary;
import org.apache.metamodel.cassandra.CassandraCustomDataContext;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.delete.DeleteFrom;
import org.apache.metamodel.factory.DataContextFactoryRegistryImpl;
import org.apache.metamodel.factory.DataContextPropertiesImpl;
import org.apache.metamodel.insert.InsertInto;
import org.apache.metamodel.jdbc.JdbcDataContext;
import org.apache.metamodel.query.FunctionType;
import org.apache.metamodel.query.Query;
import org.apache.metamodel.query.builder.FunctionSelectBuilder;
import org.apache.metamodel.query.builder.SatisfiedQueryBuilder;
import org.apache.metamodel.query.builder.TableFromBuilder;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.update.Update;
import org.springframework.jdbc.CannotGetJdbcConnectionException;

import com.attunedlabs.datacontext.config.DataContextConfigurationException;
import com.attunedlabs.featuredeployment.FeatureDeploymentServiceException;
import com.attunedlabs.leap.LeapServiceContext;
import com.attunedlabs.leap.loggingfeature.bean.LoggingFeatureUtilitiesBean;
import com.attunedlabs.security.TenantSecurityConstant;
import com.attunedlabs.security.exception.AccountFetchException;
import com.attunedlabs.security.service.impl.AccountRegistryServiceImpl;
import com.attunedlabs.security.utils.TenantSecurityUtil;
import com.datastax.driver.core.ResultSet;

/**
 * Wrapper for the MetaModel CRUD
 * 
 * @author GetUsRoi
 *
 */
public class LeapMetaModelBean extends AbstractMetaModelBean {
	public static final String SELECT_OPERATION = "SELECT";
	public static final String INSERT_OPERATION = "INSERT";
	public static final String UPDATE_OPERATION = "UPDATE";
	public static final String DELETE_OPERATION = "DELETE";
	private static final String MYSQL_DB_TYPE = "MYSQL";
	private static final String POSTGRESQL_DB_TYPE = "POSTGRESQL";
	private static final String ORACLE_DB_TYPE = "ORACLE";
	private static final String SQLSERVER_DB_TYPE = "SQLSERVER";
	private static final String CASSANDRA_DB_TYPE = "CASSANDRA";
	private static final String TRANSACTIONAL_DB_SCHEMA_NAME = "leap";

	/**
	 * This method is use to add data into the specified table using leap-metaModel
	 * 
	 * @param tableName          : Name of the table
	 * @param insertableColumns  : Name of columns whose value going to be inserted
	 * @param insertableValues   : Values for the specified column
	 * @param dataContextAndDef  : {@link List<Object>}- contain datacontext on
	 *                           index 0
	 * @param leapServiceContext : {@link LeapServiceContext}
	 * @throws InvalidQueryObjectException
	 * @throws InvalidSqlParamsCountException
	 * @throws AccountFetchException
	 * @throws TableDoesNotExistException
	 */
	public void doLeapInsert(String tableName, List<String> insertableColumns, List<Object> insertableValues,
			List<Object> dataContextAndDef, LeapServiceContext leapServiceContext)
			throws InvalidSqlParamsCountException, InvalidQueryObjectException, AccountFetchException,
			TableDoesNotExistException {
		logger.debug(".doLeapInsert method of LeapDataModel class.");
		if (tableName == null || tableName.isEmpty()) {
			throw new InvalidQueryObjectException("Invalid empty table-object requested.! " + tableName);
		}
		insertableColumns = LeapMetaModelHelper.validateListGeneric(insertableColumns);
		logger.debug("insertableColumns ::" + insertableColumns);
		insertableValues = LeapMetaModelHelper.validateListGeneric(insertableValues);
		logger.debug("insertableValues ::" + insertableValues);
		CassandraCustomDataContext cassandraCustomDataContext = null;
		// we always know at index 0 we will get jdbc datacontext object
		Object obj = dataContextAndDef.get(0);
		if (obj instanceof JdbcDataContext) {
			JdbcDataContext jdbcDataContext = (JdbcDataContext) obj;
			doJdbcLeapInsert(tableName, insertableColumns, insertableValues, jdbcDataContext, leapServiceContext);

		} else if (obj instanceof CassandraCustomDataContext) {
			cassandraCustomDataContext = (CassandraCustomDataContext) obj;
			doCassandraLeapInsert(tableName, insertableColumns, insertableValues, cassandraCustomDataContext,
					leapServiceContext);
		}
	} // ..end of the method doLeapInsert

	public void doLeapCreate(String tableName, List<ColumnMetaData> columnsMetaData, List<String> primaryKey,
			List<List<String>> uniqueKey, List<Object> dataContextAndDef, LeapServiceContext leapServiceContext)
			throws InvalidQueryObjectException {
		logger.debug(".doLeapCreate method of LeapDataModel class.");
		if (tableName == null || tableName.isEmpty()) {
			throw new InvalidQueryObjectException("Invalid empty table-object requested.! " + tableName);
		}

		Object obj = dataContextAndDef.get(0);
		if (obj instanceof JdbcDataContext) {
			JdbcDataContext jdbcDataContext = (JdbcDataContext) obj;
			doJdbcLeapCreate(tableName, columnsMetaData, primaryKey, uniqueKey, jdbcDataContext, leapServiceContext);

		} else if (obj instanceof CassandraCustomDataContext) {
			CassandraCustomDataContext cassandraCustomDataContext = (CassandraCustomDataContext) obj;
			// doCassandraLeapCreate(tableName,insertableColumns,insertableValues,cassandraCustomDataContext,leapServiceContext);
		}
	}

	private void doJdbcLeapCreate(String tableName, List<ColumnMetaData> columnsMetaData, List<String> primaryKey,
			List<List<String>> uniqueKey, JdbcDataContext jdbcDataContext, LeapServiceContext leapServiceContext) {
		try {
			logger.debug(".doJdbcLeapCreate method of LeapDataModel class.");
			String createQuery = "CREATE TABLE " + tableName + " (";
			if (columnsMetaData != null) {
				for (ColumnMetaData columnMetaData : columnsMetaData) {
					String columnName = columnMetaData.getColumnName();
					String dataType = columnMetaData.getDataType();
					int length = columnMetaData.getLength();
					String defaultValue = columnMetaData.getDefaultValue();
					boolean notNull = columnMetaData.isNotNull();
					boolean autoIncrement = columnMetaData.isAutoIncreament();
					String autoIncrementString = "";
					if (defaultValue == null) {
						defaultValue = "NULL";
					}
					if (notNull == true) {
						defaultValue = "NOT NULL";
					}
					if (autoIncrement == true) {
						autoIncrementString = "AUTO_INCREMENT";
					}
					createQuery = createQuery + " " + columnName + " " + dataType + "(" + length + ")" + " "
							+ defaultValue + " " + autoIncrementString;

				}

				logger.debug("query string is : " + createQuery);

			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

	}

	/**
	 * This method is use to add data into the specified table using jdbc
	 * leap-metaModel
	 * 
	 * @param tableName          : Name of the table
	 * @param insertableColumns  : Name of columns whose value going to be inserted
	 * @param insertableValues   : Values for the specified column
	 * @param jdbcDataContext    : {@link JdbcDataContext}- contain jdbcDataContext
	 * @param leapServiceContext : {@link LeapServiceContext}
	 * @throws InvalidSqlParamsCountException
	 * @throws AccountFetchException
	 * @throws TableDoesNotExistException
	 * 
	 */
	private void doJdbcLeapInsert(String tableName, List<String> insertableColumns, List<Object> insertableValues,
			JdbcDataContext jdbcDataContext, LeapServiceContext leapServiceContext)
			throws InvalidSqlParamsCountException, AccountFetchException, TableDoesNotExistException {
		logger.debug(".doJdbcLeapInsert method of LeapDataModel class.");
		long startTime = System.currentTimeMillis();
		long endTime;
		DataSource dataSource = getDataSource();
		AccountRegistryServiceImpl accRegSerImpl = new AccountRegistryServiceImpl();
		String accountId = accRegSerImpl.getAccountIdByTenant(leapServiceContext.getTenant());
		String dataSourceInString = null;
		if (dataSource != null) {
			dataSourceInString = dataSource.toString();
			long startTime1 = System.currentTimeMillis();
			logger.debug("table Name: " + tableName);
			logger.debug("dataSourceInString ::" + dataSourceInString);
			final Table table = getTableByQualifiedName(tableName, dataSourceInString, jdbcDataContext);
			logger.debug(" table data ::" + table);
			if (table == null) {
				throw new TableDoesNotExistException("table " + tableName + " does not exist");
			}
			long endTime1 = System.currentTimeMillis();
			logger.debug("time taken to get the table name from ds - " + (endTime1 - startTime1));
			final int colCount = insertableColumns.size();
			insertableValues = LeapMetaModelHelper.getArraylistofList(insertableValues);
			final int valCount = insertableValues.size();
			logger.debug("DateChecking: " + insertableValues.get(1));
			/** New InsertInto MetaModel derived instance **/
			InsertInto insertInto = new InsertInto(table);
			Set<String> columnSet;
			if (colCount != 0) {
				/** check for equal params **/
				if (colCount == valCount) {
					/**
					 * Set of ColumnNames, from the String[].. to avoid duplicate names
					 **/
					logger.debug("InsertableCol name: " + insertableColumns.get(1));
					columnSet = LeapMetaModelHelper.getSetofColumns(insertableColumns);
					int index = 0;
					/** Iterating values, for the given number of Columns **/
					logger.debug("Column: " + columnSet + " Values: " + insertableValues);
					for (String column : columnSet) {
						insertInto.value(column.trim(), insertableValues.get(index));
						index++;
					} // ..end of for-loop
				} // ..end of if
				else {
					throw new InvalidSqlParamsCountException(
							"Colum count " + colCount + " & Values count " + valCount + " missmatched");
				} // ..end of else
			} // ..end of if
			else {
				/**
				 * Set of ColumnNames, from the getColumsbyTable[].. to avoid duplicate names
				 **/
				int index = 0;
				insertableColumns = LeapMetaModelHelper.getColumsbyTable(table);
				/** Iterate over values **/
				for (Object object : insertableValues) {
					insertInto.value(insertableColumns.get(index).toString().trim(), object);
					index = index + 1;
				} // ..end of for-each
			} // ..end of else
			insertInto.value(TenantSecurityConstant.ACCOUNT_ID, accountId);
			insertInto.value(TenantSecurityConstant.SITE_ID, leapServiceContext.getSite());
			try {
				logger.debug("jdbcDataContext ::" + jdbcDataContext.getConnection().getTransactionIsolation());
				logger.debug("jdbcDataContext ::" + jdbcDataContext.getConnection().getAutoCommit());

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			jdbcDataContext.executeUpdate(insertInto);

			logger.info("Value has been inserted succesfully..!");
			endTime = System.currentTimeMillis();
			if (LoggingFeatureUtilitiesBean.isPerformanceLoggingEnabled)
				LoggingFeatureUtilitiesBean.addInternalComponentCalls(leapServiceContext, "doLeapInsert",
						(endTime - startTime));
		}
	}// end of method doJdbcLeapInsert

	/**
	 * This method is use to add data into the specified table using Cassandra
	 * leap-metaModel
	 * 
	 * @param tableName                  : Name of the table
	 * @param insertableColumns          : Name of columns whose value going to be
	 *                                   inserted
	 * @param insertableValues           : Values for the specified column
	 * @param cassandraCustomDataContext : {@link CassandraCustomDataContext}-
	 *                                   contain CassandraCustomDataContext
	 * @param leapServiceContext         : {@link LeapServiceContext}
	 * @throws InvalidSqlParamsCountException
	 * @throws AccountFetchException
	 * 
	 */
	private void doCassandraLeapInsert(String tableName, List<String> insertableColumns, List<Object> insertableValues,
			CassandraCustomDataContext cassandraCustomDataContext, LeapServiceContext leapServiceContext)
			throws InvalidSqlParamsCountException, AccountFetchException {
		logger.debug(".doCassandraLeapInsert method of LeapDataModel class.");
		long startTime = System.currentTimeMillis();
		long endTime;
		Map<String, Object> dataToBeInserted = new HashMap<>();
		AccountRegistryServiceImpl accRegSerImpl = new AccountRegistryServiceImpl();
		String accountId = accRegSerImpl.getAccountIdByTenant(leapServiceContext.getTenant());
		if (insertableColumns != null && insertableValues != null
				&& (insertableColumns.size() == insertableValues.size())) {
			for (int i = 0; i < insertableColumns.size(); i++)
				dataToBeInserted.put(insertableColumns.get(i), insertableValues.get(i));

			dataToBeInserted.put(TenantSecurityConstant.SITE_ID, leapServiceContext.getSite());
			dataToBeInserted.put(TenantSecurityConstant.ACCOUNT_ID, accountId);
			cassandraCustomDataContext.insertIntoTable(tableName, dataToBeInserted);
			logger.info("Value has been inserted succesfully..!");
			endTime = System.currentTimeMillis();
			if (LoggingFeatureUtilitiesBean.isPerformanceLoggingEnabled)
				LoggingFeatureUtilitiesBean.addInternalComponentCalls(leapServiceContext, "doLeapInsert",
						(endTime - startTime));
		} // end of if
		else {
			throw new InvalidSqlParamsCountException(
					"Unable to insert data in the db as column count doesnt match with column value count");
		} // ..end of else

	}// end of method doCassandraLeapInsert

	/**
	 * This method is use to update the specified table using leap-metaModel
	 * 
	 * @param tableName           : Name of the table
	 * @param predicateString     : they are the conditional placeholder e.g 'id=?'
	 *                            for where (mostly used for SQL) clause
	 * @param predicatefieldList  : value for the conditional placeholder
	 * @param predicateColumnName : Name of the predicate column ( mostly used for
	 *                            cassandra)
	 * @param updatableColumns    : List of the name of the columns need to be
	 *                            updated
	 * @param values              : Updated values for the column
	 * @param dataContextAndDef   : {@link List<Object>}- contain datacontext on
	 *                            index 0
	 * @param leapServiceContext  : {@link LeapServiceContext
	 * @throws InvalidQueryObjectException
	 * @throws InvalidPredicateException
	 * @throws InvalidSqlParamsCountException
	 * @throws AccountFetchException
	 * @throws TableDoesNotExistException
	 */
	public void doLeapUpdate(String tableName, String predicateString, List<?> predicatefieldList,
			List<String> predicateColumnName, List<String> updatableColumns, List<?> values,
			List<Object> dataContextAndDef, LeapServiceContext leapServiceContext)
			throws InvalidQueryObjectException, InvalidPredicateException, InvalidSqlParamsCountException,
			AccountFetchException, TableDoesNotExistException {
		logger.debug(".doLeapUpdate method of LeapMetaModelBean class..");
		JdbcDataContext jdbcDataContext = null;
		CassandraCustomDataContext cassandraCustomDataContext = null;
		long startTime = System.currentTimeMillis();
		long endTime;
		AccountRegistryServiceImpl accRegSerImpl = new AccountRegistryServiceImpl();
		String accountId = accRegSerImpl.getAccountIdByTenant(leapServiceContext.getTenant());
		logger.debug(".processMetaModelUpdate().. tableName: " + tableName + " values: " + values);
		if (tableName == null || tableName.isEmpty()) {
			throw new InvalidQueryObjectException("Invalid empty table-object requested.! " + tableName);
		}
		updatableColumns = LeapMetaModelHelper.validateListGeneric(updatableColumns);
		values = LeapMetaModelHelper.validateListGeneric(values);
		// we always know at index 0 we will get jdbc datacontext object
		Object obj = dataContextAndDef.get(0);
		if (obj instanceof JdbcDataContext) {
			jdbcDataContext = (JdbcDataContext) obj;
			DataSource dataSource = getDataSource();
			String dataSourceInString = null;
			if (dataSource != null) {
				dataSourceInString = dataSource.toString();
				final Table table = getTableByQualifiedName(tableName, dataSourceInString, jdbcDataContext);
				if (table == null) {
					throw new TableDoesNotExistException("table " + tableName + " does not exist");
				}
				predicateString = LeapMetaModelHelper.validatePredicateList(predicateString);
				predicatefieldList = LeapMetaModelHelper.validatePredicateFieldList(predicatefieldList);
				final int colCount = updatableColumns.size();
				final int valCount = values.size();
				Set<String> columnSet;
				String predicateMapped = LeapMetaModelHelper.getMappedStringFromList(predicateString,
						predicatefieldList);
				/** Update object initialization **/
				Update updateObject = new Update(table);
				columnSet = LeapMetaModelHelper.getSetofColumns(updatableColumns);
				logger.debug("ColumnSet returned: " + columnSet);
				int index = 0;
				if (LeapMetaModelHelper.isColumnToValueCountEqual(colCount, valCount)) {
					/** if predicates are empty **/
					if (LeapMetaModelHelper.isPredicateEmpty(predicateMapped)) {
						logger.info("Updating all the matching columns, since no predicates are mentioned.");
						/** Looping if no predicates **/
						for (String column : columnSet) {
							logger.debug("Checking the columns to be updated: " + column + " & the values"
									+ values.get(index) + " respectively.");
							updateObject.value(column, values.get(index));
							index = index + 1;
						} // ..end of for-each
					} // ..end of if
					else {
						for (String column : columnSet) {
							updateObject.value(column, values.get(index));
							index = index + 1;
						} // ..end of for-each
						updateObject.where(LeapMetaModelHelper.getFilterItem(predicateMapped));
					} // ..end of else
					updateObject.where(TenantSecurityConstant.ACCOUNT_ID).eq(accountId)
							.where(TenantSecurityConstant.SITE_ID).eq(leapServiceContext.getSite());
					jdbcDataContext.executeUpdate(updateObject);
					logger.info("Value has been Updated succesfully..!");
				} else {
					throw new InvalidSqlParamsCountException(
							"Colum count " + colCount + " & Values count " + valCount + " missmatched");
				}
				endTime = System.currentTimeMillis();
				if (LoggingFeatureUtilitiesBean.isPerformanceLoggingEnabled)
					LoggingFeatureUtilitiesBean.addInternalComponentCalls(leapServiceContext, "doLeapUpdate",
							(endTime - startTime));
			}

		} else if (obj instanceof CassandraCustomDataContext) {
			cassandraCustomDataContext = (CassandraCustomDataContext) obj;
			doCassandraLeapUpdate(tableName, predicatefieldList, predicateColumnName, updatableColumns, values,
					cassandraCustomDataContext, leapServiceContext);
		}

	}// ..end of the method

	/**
	 * This method is used to update the data from the given table using Cassandra
	 * leap-metaModel
	 * 
	 * @param tableName                  : Name of the table
	 * @param predicatefieldList         : value for the conditional placeholder
	 * @param predicateColumnName        : Name of the column of predicate
	 * @param updatableColumns           : Name of the column need to be updated
	 * @param values                     : new values for the updating column
	 * @param cassandraCustomDataContext : {@link CassandraCustomDataContext} -
	 *                                   contain CassandraCustomDataContext Object
	 * @param leapServiceContext         : {@link LeapServiceContext}
	 * @throws InvalidSqlParamsCountException
	 */
	private void doCassandraLeapUpdate(String tableName, List<?> predicatefieldList, List<String> predicateColumnName,
			List<String> updatableColumns, List<?> values, CassandraCustomDataContext cassandraCustomDataContext,
			LeapServiceContext leapServiceContext) throws InvalidSqlParamsCountException {
		logger.debug(".doCassandraLeapUpdate method of LeapDataModel class.");
		long startTime = System.currentTimeMillis();
		long endTime;
		Map<String, Object> dataToBeUpdated = new HashMap<>();
		Map<String, Object> conditionForUpdate = new HashMap<>();
		if (updatableColumns != null && values != null && (updatableColumns.size() == values.size())) {
			for (int i = 0; i < updatableColumns.size(); i++)
				dataToBeUpdated.put(updatableColumns.get(i), values.get(i));
		}
		if (predicateColumnName != null && predicatefieldList != null
				&& (predicateColumnName.size() == predicatefieldList.size())) {
			for (int i = 0; i < predicateColumnName.size(); i++)
				conditionForUpdate.put(predicateColumnName.get(i), predicatefieldList.get(i));
		}

		// conditionForUpdate.put(TenantSecurityConstant.SITE_ID,leapServiceContext.getSite());
		// conditionForUpdate.put(TenantSecurityConstant.TENANT_ID,leapServiceContext.getTenant());
		cassandraCustomDataContext.updateRow(tableName, dataToBeUpdated, conditionForUpdate);
		logger.info("Value has been updated succesfully..!");
		endTime = System.currentTimeMillis();
		if (LoggingFeatureUtilitiesBean.isPerformanceLoggingEnabled)
			LoggingFeatureUtilitiesBean.addInternalComponentCalls(leapServiceContext, "doLeapUpdate",
					(endTime - startTime));

	}// end of method doCassandraLeapUpdate

	/**
	 * This method is used to delete the data from the given table using
	 * leap-metaModel
	 * 
	 * @param tableName          : Name of the table
	 * @param predicateString    : they are the conditional placeholder e.g 'id=?'
	 * @param predicatefieldList : value for the conditional placeholder
	 * @param dataContextAndDef  : {@link List<Object>}- contain datacontext on
	 *                           index 0
	 * @param leapServiceContext : {@link LeapServiceContext}
	 * @throws InvalidQueryObjectException
	 * @throws InvalidPredicateException
	 * @throws InvalidSqlParamsCountException
	 * @throws AccountFetchException
	 * @throws TableDoesNotExistException
	 */
	public int doLeapDelete(String tableName, String predicateString, List<?> predicatefieldList,
			List<String> predicateColumnName, List<Object> dataContextAndDef, LeapServiceContext leapServiceContext)
			throws InvalidQueryObjectException, InvalidPredicateException, InvalidSqlParamsCountException,
			AccountFetchException, TableDoesNotExistException {
		logger.debug(".doLeapDelete method of LeapMetaModelBean");
		if (tableName == null || tableName.isEmpty()) {
			throw new InvalidQueryObjectException("Invalid empty table-object requested.! " + tableName);
		}
		CassandraCustomDataContext cassandraCustomDataContext = null;
		// we always know at index 0 we will get datacontext object
		Object obj = dataContextAndDef.get(0);
		if (obj instanceof JdbcDataContext) {
			JdbcDataContext jdbcDataContext = (JdbcDataContext) obj;
			return doJdbcLeapDelete(tableName, predicateString, predicatefieldList, jdbcDataContext,
					leapServiceContext);
		} else if (obj instanceof CassandraCustomDataContext) {
			cassandraCustomDataContext = (CassandraCustomDataContext) obj;
			return doCassandraLeapDelete(tableName, predicatefieldList, predicateColumnName, cassandraCustomDataContext,
					leapServiceContext);
		} // end of else-if
		return 0;

	}// ..end of the method

	/**
	 * This method is used to delete the data from the given table using Jdbc
	 * leap-metaModel
	 * 
	 * @param tableName          : Name of the table
	 * @param predicateString    : they are the conditional placeholder e.g 'id=?'
	 * @param predicatefieldList : value for the conditional placeholder
	 * @param jdbcDataContext    : {@link JdbcDataContext}
	 * @param leapServiceContext : {@link LeapServiceContext}
	 * @throws InvalidPredicateException
	 * @throws AccountFetchException
	 * @throws TableDoesNotExistException
	 */
	private int doJdbcLeapDelete(String tableName, String predicateString, List<?> predicatefieldList,
			JdbcDataContext jdbcDataContext, LeapServiceContext leapServiceContext)
			throws InvalidPredicateException, AccountFetchException, TableDoesNotExistException {
		logger.debug(".doJdbcLeapDelete method of LeapDataModel class.");
		long startTime = System.currentTimeMillis();
		long endTime;
		int noOfRowsDeleted = 0;
		DataSource dataSource = getDataSource();
		AccountRegistryServiceImpl accRegSerImpl = new AccountRegistryServiceImpl();
		String accountId = accRegSerImpl.getAccountIdByTenant(leapServiceContext.getTenant());
		String dataSourceInString = null;
		if (dataSource != null) {
			dataSourceInString = dataSource.toString();
			final Table table = getTableByQualifiedName(tableName, dataSourceInString, jdbcDataContext);
			if (table == null) {
				throw new TableDoesNotExistException("table " + tableName + " does not exist");
			}
			predicateString = LeapMetaModelHelper.validatePredicateList(predicateString);
			predicatefieldList = LeapMetaModelHelper.validatePredicateFieldList(predicatefieldList);
			logger.debug("predicateSTring : " + predicateString + ", predicateFieldList : " + predicatefieldList);
			/* If no predicates then called for delete all from the table */
			if (LeapMetaModelHelper.isPredicateEmpty(predicateString)) {
				logger.debug("predicate string is empty : " + predicateString);
				UpdateSummary executeUpdate = jdbcDataContext
						.executeUpdate(new DeleteFrom(table).where(TenantSecurityConstant.ACCOUNT_ID).eq(accountId)
								.where(TenantSecurityConstant.SITE_ID).eq(leapServiceContext.getSite()));
				logger.info("Successfully deleted all data from the given table..");
				Optional<Integer> deletedRows = executeUpdate.getDeletedRows();
				noOfRowsDeleted = deletedRows.orElse(0);

			} // ..end of if, to delete all from table
			else {
				logger.debug("defined the predicate string : " + predicateString);
				String mapOfConstraint = LeapMetaModelHelper.getMappedStringFromList(predicateString,
						predicatefieldList);
				UpdateSummary executeUpdate = jdbcDataContext
						.executeUpdate(new DeleteFrom(table).where(LeapMetaModelHelper.getFilterItem(mapOfConstraint))
								.where(TenantSecurityConstant.ACCOUNT_ID).eq(accountId)
								.where(TenantSecurityConstant.SITE_ID).eq(leapServiceContext.getSite()));
				logger.info("Successfully deleted a row data from the given table,for the given predicate.");
				Optional<Integer> deletedRows = executeUpdate.getDeletedRows();
				noOfRowsDeleted = deletedRows.orElse(0);
			}

			endTime = System.currentTimeMillis();
			if (LoggingFeatureUtilitiesBean.isPerformanceLoggingEnabled)
				LoggingFeatureUtilitiesBean.addInternalComponentCalls(leapServiceContext, "doLeapDelete",
						(endTime - startTime));
		} else {
			logger.warn("DataSource is not set");
		}
		logger.debug("end of doJdbcLeapDelete method of LeapDataModel class.");
		return noOfRowsDeleted;
	}// end of method doJdbcLeapDelete

	/**
	 * This method is used to delete the data from the given table using cassandra
	 * leap-metaModel
	 * 
	 * @param tableName           : Name of the table
	 * @param predicatefieldList  : value for the conditional placeholder
	 * @param predicateColumnName : Name of the column of predicate
	 * @param jdbcDataContext     : {@link JdbcDataContext}
	 * @param leapServiceContext  : {@link LeapServiceContext}
	 * @throws InvalidPredicateException
	 */
	private int doCassandraLeapDelete(String tableName, List<?> predicatefieldList, List<String> predicateColumnName,
			CassandraCustomDataContext cassandraCustomDataContext, LeapServiceContext leapServiceContext)
			throws InvalidSqlParamsCountException {
		logger.debug(".doCassandraLeapDelete method of LeapDataModel class.");
		long startTime = System.currentTimeMillis();
		long endTime;
		Map<String, Object> dataToBeDeleted = new HashMap<>();
		if (predicateColumnName != null && predicatefieldList != null
				&& (predicateColumnName.size() == predicatefieldList.size())) {
			for (int i = 0; i < predicateColumnName.size(); i++)
				dataToBeDeleted.put(predicateColumnName.get(i).toLowerCase(), predicatefieldList.get(i));

			// dataToBeDeleted.put(TenantSecurityConstant.SITE_ID,leapServiceContext.getSite());
			// dataToBeDeleted.put(TenantSecurityConstant.TENANT_ID,leapServiceContext.getTenant());
			ResultSet deleteRow = cassandraCustomDataContext.deleteRow(tableName, dataToBeDeleted, null);

			logger.info("Value has been deleted succesfully..!");
			endTime = System.currentTimeMillis();
			if (LoggingFeatureUtilitiesBean.isPerformanceLoggingEnabled)
				LoggingFeatureUtilitiesBean.addInternalComponentCalls(leapServiceContext, "doLeapDelete",
						(endTime - startTime));
		} // end of if
		else {
			throw new InvalidSqlParamsCountException(
					"Unable to delete data in the db as column count doesnt match with column value count");
		} // ..end of else
			// need to write an logic to get the no of rows deleted.
		return 1;
	}// end of method doCassandraLeapDelete

	/**
	 * This method is used to select the data from specified table using
	 * leap-metaModel
	 * 
	 * @param tableName          : Name of the table
	 * @param predicateString    : they are the conditional placeholder e.g 'id=?'
	 *                           using in where clause
	 * @param predicatefieldList : value for the conditional placeholder
	 * @param selectableColumns  : List of the name of the column need to get in
	 *                           response
	 * @param dataContextAndDef  : {@link List<Object} - contain datacontext on
	 *                           index 0 and schema def at index 1
	 * @param leapServiceContext : {@link LeapServiceContext}
	 * @return {@link DataSet}
	 * @throws InvalidQueryObjectException
	 * @throws InvalidPredicateException
	 * @throws AccountFetchException
	 * @throws TableDoesNotExistException
	 */
	public DataSet doLeapSelect(String tableName, String predicateString, List<?> predicatefieldList,
			List<String> selectableColumns, List<Object> dataContextAndDef, LeapServiceContext leapServiceContext)
			throws InvalidQueryObjectException, InvalidPredicateException, AccountFetchException,
			TableDoesNotExistException {
		DataContext datacontext = null;
		long startTime = System.currentTimeMillis();
		long endTime;
		DataSet ds = null;
		AccountRegistryServiceImpl accRegSerImpl = new AccountRegistryServiceImpl();
		String accountId = accRegSerImpl.getAccountIdByTenant(leapServiceContext.getTenant());
		logger.debug("Select Api is called..");
		if (tableName == null || tableName.isEmpty()) {
			throw new InvalidQueryObjectException("Invalid empty table-object requested.! " + tableName);
		}
		if (selectableColumns == null) {
			selectableColumns = new ArrayList<>();
		}
		// we always know at index 0 we will get datacontext could be jdbc or cassandra
		// custom object
		Object obj = dataContextAndDef.get(0);
		if (obj instanceof CassandraCustomDataContext)
			datacontext = (CassandraCustomDataContext) obj;
		else
			datacontext = (JdbcDataContext) obj;

		logger.debug(" databaseName ::" + datacontext.getSchemaNames() + datacontext.getClass());
		DataSource dataSource = getDataSource();
		String dataSourceInString = null;
		if (dataSource != null)
			dataSourceInString = dataSource.toString();
		else
			dataSourceInString = (String) dataContextAndDef.get(1);

		long startTime1 = System.currentTimeMillis();
		final Table table = getTableByQualifiedName(tableName, dataSourceInString, datacontext);

		if (table == null) {
			throw new TableDoesNotExistException("table " + tableName + " does not exist");
		}

		long endTime1 = System.currentTimeMillis();
		logger.debug("time taken to get the table name from ds - " + (endTime1 - startTime1));

		predicatefieldList = LeapMetaModelHelper.validatePredicateFieldList(predicatefieldList);
		predicateString = LeapMetaModelHelper.validatePredicateList(predicateString);

		final boolean isEmptyPredicate = LeapMetaModelHelper.isPredicateEmpty(predicateString);
		final boolean isEmptyColumns = LeapMetaModelHelper.isEmptyList(selectableColumns);
		String substitutedPredicates = LeapMetaModelHelper.getMappedStringFromList(predicateString, predicatefieldList);

		Query q;
		if (isEmptyPredicate && isEmptyColumns) {
			logger.debug("Select all is called.");
			q = datacontext.query().from(table).selectAll().where(TenantSecurityConstant.ACCOUNT_ID).eq(accountId)
					.and(TenantSecurityConstant.SITE_ID).eq(leapServiceContext.getSite()).toQuery();
		} else if (isEmptyPredicate && !isEmptyColumns) {
			String[] columnNames = new String[selectableColumns.size()];
			columnNames = selectableColumns.toArray(columnNames);
			q = datacontext.query().from(table).select(columnNames).where(TenantSecurityConstant.ACCOUNT_ID)
					.eq(accountId).and(TenantSecurityConstant.SITE_ID).eq(leapServiceContext.getSite()).toQuery();
		} else if (!isEmptyPredicate && isEmptyColumns) {
			q = datacontext.query().from(table).selectAll().where(TenantSecurityConstant.ACCOUNT_ID).eq(accountId)
					.and(TenantSecurityConstant.SITE_ID).eq(leapServiceContext.getSite())
					.where(LeapMetaModelHelper.getFilterItem(substitutedPredicates)).toQuery();
		} else {
			String[] columnNames = new String[selectableColumns.size()];
			columnNames = selectableColumns.toArray(columnNames);
			q = datacontext.query().from(table).select(columnNames)
					.where(LeapMetaModelHelper.getFilterItem(substitutedPredicates))
					.where(TenantSecurityConstant.ACCOUNT_ID).eq(accountId).and(TenantSecurityConstant.SITE_ID)
					.eq(leapServiceContext.getSite()).toQuery();
		}

		ds = datacontext.executeQuery(q);
		endTime = System.currentTimeMillis();
		logger.debug("time taken to select operation - " + (endTime - startTime));
		if (LoggingFeatureUtilitiesBean.isPerformanceLoggingEnabled)
			LoggingFeatureUtilitiesBean.addInternalComponentCalls(leapServiceContext, "doLeapSelect",
					(endTime - startTime));

		return ds;
	}// ..end of the method

	/**
	 * to perform functionType select on Function
	 * 
	 * @param functionType
	 * @param columnOnFuncApplied
	 * @param tableName
	 * @param dataContextAndDef   : {@link List<Object} - contain datacontext on
	 *                            index 0 and schema def at index 1
	 * @param leapServiceContext
	 * @return MetaModel-DataSet Object
	 * @throws InvalidQueryObjectException
	 * @throws InvalidPredicateException
	 * @throws TableDoesNotExistException
	 * @throws Exception
	 */
	public DataSet doLeapSelectByFunction(FunctionType functionType, String columnOnFuncApplied, String tableName,
			String predicateString, List predicateFieldList, List<Object> dataContextAndDef,
			LeapServiceContext leapServiceContext)
			throws InvalidQueryObjectException, InvalidPredicateException, TableDoesNotExistException {
		DataContext datacontext = null;
		long startTime = System.currentTimeMillis();
		long endTime;
		DataSet ds = null;
		if (tableName == null || tableName.isEmpty()) {
			throw new InvalidQueryObjectException("Invalid empty table-object requested.! " + tableName);
		}
		// we always know at index 0 we will get datacontext could be jdbc or cassandra
		// custom object
		Object obj = dataContextAndDef.get(0);
		if (obj instanceof CassandraCustomDataContext)
			datacontext = (CassandraCustomDataContext) obj;
		else
			datacontext = (JdbcDataContext) obj;
		DataSource dataSource = getDataSource();
		String dataSourceInString = null;
		if (dataSource != null)
			dataSourceInString = dataSource.toString();
		else
			dataSourceInString = (String) dataContextAndDef.get(1);
		final Table table = getTableByQualifiedName(tableName, dataSourceInString, datacontext);
		if (table == null) {
			throw new TableDoesNotExistException("table " + tableName + " does not exist");
		}
		predicateString = LeapMetaModelHelper.validatePredicateList(predicateString);
		predicateFieldList = LeapMetaModelHelper.validatePredicateFieldList(predicateFieldList);
		Query q = null;
		TableFromBuilder tableFromBuilder = datacontext.query().from(table);
		final boolean isEmptypredicateString = LeapMetaModelHelper.isPredicateEmpty(predicateString);
		final boolean isEmptypredicateFieldList = LeapMetaModelHelper.isEmptyList(predicateFieldList);
		if (isEmptypredicateString && isEmptypredicateFieldList) {
			FunctionSelectBuilder<?> fSelectBuilder = tableFromBuilder.select(functionType, columnOnFuncApplied);
			q = fSelectBuilder.where(TenantSecurityConstant.TENANT_ID).eq(leapServiceContext.getTenant())
					.and(TenantSecurityConstant.SITE_ID).eq(leapServiceContext.getSite()).toQuery();
		} else if (!isEmptypredicateString && !isEmptypredicateFieldList) {
			String substitutedPredicates = LeapMetaModelHelper.getMappedStringFromList(predicateString,
					predicateFieldList);
			SatisfiedQueryBuilder<?> funcSelectSatisfiedBuilder = tableFromBuilder
					.select(functionType, columnOnFuncApplied)
					.where(LeapMetaModelHelper.getFilterItem(substitutedPredicates));
			q = funcSelectSatisfiedBuilder.where(TenantSecurityConstant.TENANT_ID).eq(leapServiceContext.getTenant())
					.and(TenantSecurityConstant.SITE_ID).eq(leapServiceContext.getSite()).toQuery();
		}

		ds = datacontext.executeQuery(q);
		endTime = System.currentTimeMillis();
		if (LoggingFeatureUtilitiesBean.isPerformanceLoggingEnabled)
			LoggingFeatureUtilitiesBean.addInternalComponentCalls(leapServiceContext, "doLeapSelectByFunction",
					(endTime - startTime));

		return ds;

	}// ..end of the method

	/**
	 * This method is used to send back the resultant by performing join operation
	 * on specified tables.
	 * 
	 * @param queryFromtableName : Name of the table
	 * @param joinObject         : Name of the another table
	 * @param columnsToCompare   : name of the joining columns
	 * @param columnsToquery     : other columns for conditional clause
	 * @param joinType           : type of join (e.g inner, outer, full)
	 * @param jdbcDataContext    : {@link JdbcDataContext}
	 * @param leapServiceContext : {@link LeapServiceContext}
	 * @return {@link DataSet}
	 * @throws InvalidOperationException
	 */
	public DataSet doLeapJoinSelect(String queryFromtableName, String joinObject, List<String> columnsToCompare,
			List<String> columnsToquery, String joinType, List<Object> dataContextAndDef,
			LeapServiceContext leapServiceContext) throws InvalidOperationException {
		DataContext datacontext = null;
		long startTime = System.currentTimeMillis();
		long endTime;
		DataSet ds = null;
		// we always know at index 0 we will get datacontext could be jdbc or cassandra
		// custom object
		Object obj = dataContextAndDef.get(0);
		if (obj instanceof CassandraCustomDataContext)
			datacontext = (CassandraCustomDataContext) obj;
		else
			datacontext = (JdbcDataContext) obj;
		DataSource dataSource = getDataSource();
		String dataSourceInString = null;
		if (dataSource != null)
			dataSourceInString = dataSource.toString();
		else
			dataSourceInString = (String) dataContextAndDef.get(1);
		final Table fromTable = getTableByQualifiedName(queryFromtableName, dataSourceInString, datacontext);
		final Table joinTable = getTableByQualifiedName(joinObject, dataSourceInString, datacontext);

		Query query = LeapMetaModelHelper.buildJoinQueries(datacontext, fromTable, joinTable, columnsToCompare,
				columnsToquery, joinType);
		ds = datacontext.executeQuery(query);
		endTime = System.currentTimeMillis();
		if (LoggingFeatureUtilitiesBean.isPerformanceLoggingEnabled)
			LoggingFeatureUtilitiesBean.addInternalComponentCalls(leapServiceContext, "doLeapJoinSelect",
					(endTime - startTime));

		return ds;

	}// ..end of the method

	public com.attunedlabs.datacontext.jaxb.DataContext loadDataContext(LeapServiceContext leapServiceContext)
			throws Exception {
		return (com.attunedlabs.datacontext.jaxb.DataContext) getFeatureDataContext(null, leapServiceContext);
	}

	/**
	 * This method is use to get the data context object by setting feature defined
	 * datasource.
	 * 
	 * @param leapServiceContext : {@link LeapServiceContext}
	 * @param exchange           : {@link Exchange}
	 * @return {@link List<Object>}
	 * @throws UnableToFetchAppropriateContext    : Unable to fetch the appropriate
	 *                                            data context for feature defined
	 *                                            datasource.
	 * @throws DataContextInitializationException
	 */
	public List<Object> getDataContext(String dbNameRef, LeapServiceContext leapServiceContext, Exchange exchange)
			throws UnableToFetchAppropriateContext, DataContextInitializationException {
		logger.debug(".getDataContext method of LeapMetaModel class");
		com.attunedlabs.datacontext.jaxb.DataContext dataContext;
		JdbcDataContext jdbcDataContext = null;
		List<Object> dataContextAndOperation = null;
		try {
			dataContext = getFeatureDataContext(dbNameRef, leapServiceContext);
			if (dataContext != null) {
				String dbRefName = dataContext.getDbBeanRefName();
				String dbType = dataContext.getDbType().toUpperCase();
				String dbSchema = dataContext.getDbSchema();
				logger.debug("Logging DbReferenceName: " + dbRefName + " dbType : " + dbType);
				if ((dbType.equals(MYSQL_DB_TYPE) || dbType.equals(POSTGRESQL_DB_TYPE)
						|| dbType.equals(SQLSERVER_DB_TYPE) || dbType.equals(ORACLE_DB_TYPE))
						&& checkNonEmptyString(dbRefName)) {
					try {
						setDataSource(exchange, dbRefName);
						if (dbSchema != null && dbSchema.equalsIgnoreCase(TRANSACTIONAL_DB_SCHEMA_NAME)) {
							jdbcDataContext = getTransactionalLocalDataContext(leapServiceContext);
						} else {
							jdbcDataContext = getNonTransactionalLocalDataContext(leapServiceContext);
						}
						dataContextAndOperation = new ArrayList<>();
						dataContextAndOperation.add(jdbcDataContext);
					} catch (NoDataSourceFoundException e) {
						throw new UnableToFetchAppropriateContext(
								"Unable to get JDBC datacontext object because datasource not found", e.getCause());
					}

				} else {
					dataContextAndOperation = getDataContextFromFeatureConfig(dataContext);
				}
			} else {
				throw new DataContextConfigurationException(
						"No feature speciifc Data context found for request context : "
								+ leapServiceContext.getRequestContext() + " for db ref name : " + dbNameRef);
			}
		} catch (DataContextConfigurationException | FeatureDeploymentServiceException e) {
			throw new UnableToFetchAppropriateContext(
					"Unable to get JDBC datacontext object because no data context found for feature", e.getCause());

		}
		return dataContextAndOperation;
	}// end of method getDataContext

	/**
	 * This method is used to get the datacontext and its ds in a list object for
	 * cassandra
	 * 
	 * @param dataContext : {@link com.attunedlabs.datacontext.jaxb.DataContext} -
	 *                    feature defining the datacontext to be used. (Its mostly
	 *                    going to use for cassandra)
	 * @return List<Object>
	 * @throws DataContextInitializationException
	 */
	private List<Object> getDataContextFromFeatureConfig(com.attunedlabs.datacontext.jaxb.DataContext dataContext)
			throws DataContextInitializationException {
		logger.debug(".getDataContextFromFeatureConfig method of LeapMetaModel");
		List<Object> keyAndDS = new ArrayList<>();
		String dbHost = dataContext.getDbHost();
		String dbPort = dataContext.getDbPort();
		String dbSchema = dataContext.getDbSchema();
		String dbUser = dataContext.getDbUser();
		String dbPassword = dataContext.getDbPassword();
		String dbUrl = dataContext.getDbUrl();
		String dbDriver = dataContext.getDbDriver();
		DataContextPropertiesImpl properties = new DataContextPropertiesImpl();
		// main factor for deciding implementation
		properties.setDataContextType(checkDBProperties(dataContext));
		properties.put(DataContextPropertiesImpl.PROPERTY_HOSTNAME, dbHost);
		properties.put(DataContextPropertiesImpl.PROPERTY_PORT, dbPort);
		properties.put(DataContextPropertiesImpl.PROPERTY_URL, dbUrl);
		properties.put(DataContextPropertiesImpl.PROPERTY_DRIVER_CLASS, dbDriver);
		properties.put(DataContextPropertiesImpl.PROPERTY_USERNAME, dbUser);
		properties.put(DataContextPropertiesImpl.PROPERTY_PASSWORD, dbPassword);
		properties.put(DataContextPropertiesImpl.PROPERTY_DATABASE, dbSchema);

		String generatedCahchedKey = generateCachedKey(dataContext);

		// cassandra custom data context object is expensive so caching it into map
		// based on generatedCacheKey
		CassandraDataContextHolder cassandraDataContextHolderObj = CassandraDataContextHolder
				.getCassandraDataContextObject();
		Map<String, DataContext> dataContextMap = cassandraDataContextHolderObj.getCassandraDataContextHolderMap();
		DataContext jdbcOrCassandraDataContext = dataContextMap.get(generatedCahchedKey);

		// if cassandra custom object is unavailable in map then create the new object
		// and add it
		if (jdbcOrCassandraDataContext == null) {
			jdbcOrCassandraDataContext = DataContextFactoryRegistryImpl.getDefaultInstance()
					.createDataContext(properties);
			keyAndDS.add(jdbcOrCassandraDataContext);
			keyAndDS.add(generatedCahchedKey);
			dataContextMap.put(generatedCahchedKey, jdbcOrCassandraDataContext);
			if (jdbcOrCassandraDataContext == null)
				throw new DataContextInitializationException(
						"Something went wrong to in ContextFactory while initializing  DataContext...datacontext returned :  "
								+ dataContext);
		} // If cassandra custom object is available then use existing data context object
		else {
			keyAndDS.add(jdbcOrCassandraDataContext);
			keyAndDS.add(generatedCahchedKey);
		}
		return keyAndDS;
	}// end of method getDataContextFromFeatureConfig

	private String checkDBProperties(com.attunedlabs.datacontext.jaxb.DataContext dataContext) {
		logger.debug("checkDBProperties : " + dataContext);
		String dbHost = dataContext.getDbHost();
		String dbPort = dataContext.getDbPort();
		String dbSchema = dataContext.getDbSchema();
		String dbType = dataContext.getDbType().toUpperCase();
		String dbUrl = dataContext.getDbUrl();
		String dbDriver = dataContext.getDbDriver();

		if (checkNonEmptyString(dbHost) && checkNonEmptyString(dbPort) && checkNonEmptyString(dbSchema)
				&& dbType.equals(CASSANDRA_DB_TYPE))
			return "cassandra";
		else if (checkNonEmptyString(dbUrl) && checkNonEmptyString(dbDriver))
			return "jdbc";
		return null;
	}

	private boolean checkNonEmptyString(String checkString) {
		return checkString != null && !checkString.isEmpty();
	}

	/**
	 * Not implementing , from parent
	 */
	@Override
	protected void processBean(Exchange exch) throws Exception {
		// TODO Auto-generated method stub

	}
}