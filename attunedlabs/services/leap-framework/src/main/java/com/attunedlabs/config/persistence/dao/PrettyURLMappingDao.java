package com.attunedlabs.config.persistence.dao;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.metamodel.DataContext;
import org.apache.metamodel.DataContextFactory;
import org.apache.metamodel.DefaultUpdateSummary;
import org.apache.metamodel.UpdateCallback;
import org.apache.metamodel.UpdateScript;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.insert.RowInsertionBuilder;
import org.apache.metamodel.schema.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.persistence.PrettyUrlMapping;
import com.attunedlabs.config.persistence.exception.PrettyUrlMappingDaoException;
import com.attunedlabs.config.util.DataSourceInstance;

public class PrettyURLMappingDao {
	final Logger logger = LoggerFactory.getLogger(PrettyURLMappingDao.class);

	public void addPrettyUrlMapping(PrettyUrlMapping prettyUrlMapping) throws PrettyUrlMappingDaoException {
		String methodName = "addPrettyUrlMapping";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		int insertedKey;
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext.getTableByQualifiedLabel(LeapConstants.PRETTY_URL_MAPPING_TABLE_NAME);
			DefaultUpdateSummary insertSummary = (DefaultUpdateSummary) dataContext.executeUpdate(new UpdateScript() {
				@Override
				public void run(UpdateCallback callback) {
					final RowInsertionBuilder insert = callback.insertInto(table);
					insert.value(LeapConstants.PRETTY_STRING_COL, prettyUrlMapping.getPrettyString())
							.value(LeapConstants.TENANT_ID, prettyUrlMapping.getTenantId())
							.value(LeapConstants.SITE_ID, prettyUrlMapping.getSiteId())
							.value(LeapConstants.ACTUAL_STRING_COL, prettyUrlMapping.getActualString()).execute();

				}
			});

			if (insertSummary.getGeneratedKeys().isPresent()) {
				insertedKey = Integer
						.parseInt(insertSummary.getGeneratedKeys().get().iterator().next().toString().trim());
				logger.debug("{} addPrettyUrlMapping - generatedKey:{}", LEAP_LOG_KEY,insertedKey);
			} else
				logger.warn("{} addPrettyUrlMapping-generatedKey not found", LEAP_LOG_KEY);

		} catch (Exception e) {
			throw new PrettyUrlMappingDaoException(
					"Failed to insert into prettyUrlMapping:  due to -- > " + e.getMessage(), e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return;
	}

	public PrettyUrlMapping getPrettyUrlMappingByPrettyUrlString(PrettyUrlMapping prettryString)
			throws PrettyUrlMappingDaoException {
		String methodName = "getPrettyUrlMappingByPrettyUrlString";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		PrettyUrlMapping prettyUrlMapping = null;
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			DataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext.getTableByQualifiedLabel(LeapConstants.PRETTY_URL_MAPPING_TABLE_NAME);
			DataSet dataSet = dataContext.query().from(table).selectAll().where(LeapConstants.PRETTY_STRING_COL)
					.eq(prettryString.getPrettyString()).and(LeapConstants.TENANT_ID).eq(prettryString.getTenantId())
					.and(LeapConstants.SITE_ID).eq(prettryString.getSiteId()).execute();
			if (dataSet.next()) {
				prettyUrlMapping = buildPrettyUrlMappingFromdataset(dataSet, table);
			}
		} catch (Exception e) {
			throw new PrettyUrlMappingDaoException(
					"Failed to get prettyUrlMapping by prettryString --> " + e.getMessage(), e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return prettyUrlMapping;
	}

	public List<PrettyUrlMapping> getAllPrettyUrlMappings() throws PrettyUrlMappingDaoException {
		String methodName = "getAllPrettyUrlMappings";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		List<PrettyUrlMapping> listOfPrettyMappings = new ArrayList<>();
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			DataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext.getTableByQualifiedLabel(LeapConstants.PRETTY_URL_MAPPING_TABLE_NAME);
			DataSet dataSet = dataContext.query().from(table).selectAll().execute();
			while (dataSet.next()) {
				PrettyUrlMapping prettyUrlMapping = buildPrettyUrlMappingFromdataset(dataSet, table);
				listOfPrettyMappings.add(prettyUrlMapping);
			}
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return listOfPrettyMappings;
		} catch (Exception e) {
			throw new PrettyUrlMappingDaoException("Failed to get list of prettyUrlMapping  --> " + e.getMessage(), e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}
	}

	private PrettyUrlMapping buildPrettyUrlMappingFromdataset(DataSet dataSet, Table table) {
		PrettyUrlMapping prettyUrlMapping = new PrettyUrlMapping();
		prettyUrlMapping
				.setTenantId(dataSet.getRow().getValue(table.getColumnByName(LeapConstants.TENANT_ID)).toString());
		prettyUrlMapping.setSiteId(dataSet.getRow().getValue(table.getColumnByName(LeapConstants.SITE_ID)).toString());
		prettyUrlMapping.setPrettyString(
				dataSet.getRow().getValue(table.getColumnByName(LeapConstants.PRETTY_STRING_COL)).toString());
		prettyUrlMapping.setActualString(
				dataSet.getRow().getValue(table.getColumnByName(LeapConstants.ACTUAL_STRING_COL)).toString());
		prettyUrlMapping.setId(
				Integer.parseInt(dataSet.getRow().getValue(table.getColumnByName(LeapConstants.ID_COL)).toString()));
		return prettyUrlMapping;
	}
}
