package com.attunedlabs.leap.i18n.dao;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.metamodel.DataContextFactory;
import org.apache.metamodel.DefaultUpdateSummary;
import org.apache.metamodel.UpdateCallback;
import org.apache.metamodel.UpdateScript;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.insert.RowInsertionBuilder;
import org.apache.metamodel.schema.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.util.DataSourceInstance;
import com.attunedlabs.leap.i18n.LeapI18nConstant;
import com.attunedlabs.leap.i18n.entity.LeapI18nText;
import com.attunedlabs.leap.i18n.exception.LocaleRegistryException;
import com.attunedlabs.leap.i18n.exception.LocaleResolverException;

public class I18nTextDao {
	private static Logger logger = LoggerFactory.getLogger(I18nTextDao.class);

	/**
	 * Insert new Message for new tenant & new site
	 * 
	 * @param tenantId
	 * @param siteId
	 * @param locale
	 * @return
	 * @throws LocaleRegistryException
	 */
	public int insertNewMessage(final String tenantId, final String siteId, final String feature,
			final String resourceType, final String msgVariant, final String localeId, final String usage,
			final String i18nId, final String textValue) throws LocaleRegistryException {
		String methodName = "insertNewMessage";
		logger.debug(
				"{} entered into the method {}, tenantId: {}  siteId: {} feature: {} resourceType: {} msgVariant: {} localeId: {} usage: {} I18nId: {} textValue: {}",
				LEAP_LOG_KEY, methodName, tenantId, siteId, feature, resourceType, msgVariant, localeId, usage, i18nId,
				textValue);
		Integer generatedId = 0;
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext.getTableByQualifiedLabel(LeapI18nConstant.I18N_TEXT_TABLE);
			DefaultUpdateSummary insertSummary = (DefaultUpdateSummary) dataContext.executeUpdate(new UpdateScript() {
				@Override
				public void run(UpdateCallback callback) {
					final RowInsertionBuilder insert = callback.insertInto(table);
					insert.value(LeapI18nConstant.TENANT_ID, tenantId).value(LeapI18nConstant.SITE_ID, siteId)
							.value(LeapI18nConstant.FEATURE, feature)
							.value(LeapI18nConstant.RESOURCE_TYPE, resourceType)
							.value(LeapI18nConstant.MSG_VARIANT, msgVariant).value(LeapI18nConstant.LOCALE_ID, localeId)
							.value(LeapI18nConstant.ELEMENTID, usage).value(LeapI18nConstant.I18NID, i18nId)
							.value(LeapI18nConstant.TEXT_VALUE, textValue);
					insert.execute();

				}
			});

			if (insertSummary.getGeneratedKeys().isPresent()) {
				generatedId = Integer
						.parseInt(insertSummary.getGeneratedKeys().get().iterator().next().toString().trim());
				logger.trace("{} insertNewMessage key:{} ", LEAP_LOG_KEY, generatedId);
			} else {
				logger.trace("{} insertNewMessage key not found {}", LEAP_LOG_KEY,
						insertSummary.getGeneratedKeys().get().iterator().next());
			}
		} catch (Exception e) {
			throw new LocaleRegistryException("Unable to register the new Message! --> " + e.getMessage(), e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return generatedId;
	}

	/**
	 * get the tenant-specific list of all the locale message.
	 * 
	 * @param tenantId
	 * @param siteId
	 * @param localeId
	 * @param applicableNodeId
	 * @return
	 * @throws LocaleResolverException
	 */
	public List<LeapI18nText> selectAllTenantMessage(String tenantId, String siteId, String feature)
			throws LocaleResolverException {
		String methodName = "selectAllTenantMessage";
		logger.debug("{} entered into the method {} tenantId: {},  siteId:{}  feature:{}", LEAP_LOG_KEY, methodName,
				tenantId, siteId, feature);
		List<LeapI18nText> listMessageContext = new ArrayList<>();
		LeapI18nText messageContext = null;
		DataSet dataSet = null;
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext.getTableByQualifiedLabel(LeapI18nConstant.I18N_TEXT_TABLE);
			dataSet = dataContext.query().from(table).selectAll().where(LeapI18nConstant.TENANT_ID).eq(tenantId)
					.and(LeapI18nConstant.SITE_ID).eq(siteId).and(LeapI18nConstant.FEATURE).eq(feature).execute();
			if (dataSet == null)
				throw new LocaleResolverException("Empty dataSet returned on get all available messages! ");
			Iterator<Row> itr = dataSet.iterator();
			while (itr.hasNext()) {
				Row row = itr.next();
				logger.info("{} fetched config node row: {}", LEAP_LOG_KEY, row);
				messageContext = parseROW(messageContext, row, table);
				logger.info("{} parsed LeapI18nMessage from row retrieved: {}", LEAP_LOG_KEY, messageContext);
				listMessageContext.add(messageContext);
			}
			logger.info("{} list of all parsed LeapI18nMessage from rows retrieved: {}", LEAP_LOG_KEY,
					listMessageContext);
		} catch (Exception e) {
			throw new LocaleResolverException("Unable to get all messages details --> " + e.getMessage(), e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return listMessageContext;
	}// ..end of the method

	/**
	 * get specific locale message.
	 * 
	 * @param tenantId
	 * @param siteId
	 * @param localeId
	 * @param applicableNodeId
	 * @return
	 * @throws LocaleResolverException
	 */
	public LeapI18nText selectMessage(String tenantId, String siteId, String localeId, String feature)
			throws LocaleResolverException {
		String methodName = "selectMessage";
		logger.debug("{} entered into the method {} tenantId: {},  siteId:{} feature:{} localeId:{}", LEAP_LOG_KEY,
				methodName, tenantId, siteId, feature, localeId);
		LeapI18nText messageContext = null;
		DataSet dataSet = null;
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext.getTableByQualifiedLabel(LeapI18nConstant.I18N_TEXT_TABLE);
			dataSet = dataContext.query().from(table).selectAll().where(LeapI18nConstant.TENANT_ID).eq(tenantId)
					.and(LeapI18nConstant.SITE_ID).eq(siteId).and(LeapI18nConstant.FEATURE).eq(feature)
					.and(LeapI18nConstant.LOCALE_ID).eq(localeId).execute();
			if (dataSet == null)
				throw new LocaleResolverException("Empty dataSet returned on get all available messages! ");
			Iterator<Row> itr = dataSet.iterator();
			if (itr.hasNext()) {
				Row row = itr.next();
				logger.info("{} fetched config node row:{} ", LEAP_LOG_KEY, row);
				messageContext = parseROW(messageContext, row, table);
				logger.info("{} parsed LeapI18nMessage from row retrieved:{} ", LEAP_LOG_KEY, messageContext);
			}
		} catch (Exception e) {
			throw new LocaleResolverException("Unable to get messages details --> " + e.getMessage(), e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return messageContext;
	}// ..end of the method

	/**
	 * get the list of all locale message details.
	 * 
	 * @return
	 * @throws LocaleResolverException
	 */
	public List<LeapI18nText> selectAllMessage() throws LocaleResolverException {
		String methodName = "selectAllMessage";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		List<LeapI18nText> listMessageContext = new ArrayList<>();
		LeapI18nText messageContext = null;
		DataSet dataSet = null;
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext.getTableByQualifiedLabel(LeapI18nConstant.I18N_TEXT_TABLE);
			dataSet = dataContext.query().from(table).selectAll().execute();
			if (dataSet == null)
				throw new LocaleResolverException("Empty dataSet returned on get all available messages! ");
			Iterator<Row> itr = dataSet.iterator();
			while (itr.hasNext()) {
				Row row = itr.next();
				logger.info("{} fetched config node row: {}", LEAP_LOG_KEY, row);
				messageContext = parseROW(messageContext, row, table);
				logger.info("{} parsed LeapI18nMessage from row retrieved:{} ", LEAP_LOG_KEY, messageContext);
				listMessageContext.add(messageContext);
			}
			logger.info("{} list of all parsed LeapI18nMessage from rows retrieved:{}", LEAP_LOG_KEY,
					listMessageContext);
		} catch (Exception e) {
			throw new LocaleResolverException("Unable to get all messages details --> " + e.getMessage(), e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return listMessageContext;
	}// ..end of the method

	/**
	 * 
	 * @param messageContext
	 * @param row
	 * @param table
	 * @return
	 */
	private LeapI18nText parseROW(LeapI18nText messageContext, Row row, Table table) {
		return messageContext = new LeapI18nText(
				// ConfigUtil.conversionOfLongToIntSetup(row.getValue(table.getColumnByName(LeapI18nConstant.MSG_ID))),
				Integer.parseInt(row.getValue(table.getColumnByName(LeapI18nConstant.MSG_ID)).toString()),
				row.getValue(table.getColumnByName(LeapI18nConstant.TENANT_ID)).toString(),
				row.getValue(table.getColumnByName(LeapI18nConstant.SITE_ID)).toString(),
				row.getValue(table.getColumnByName(LeapI18nConstant.FEATURE)).toString(),
				row.getValue(table.getColumnByName(LeapI18nConstant.RESOURCE_TYPE)).toString(),
				row.getValue(table.getColumnByName(LeapI18nConstant.MSG_VARIANT)).toString(),
				row.getValue(table.getColumnByName(LeapI18nConstant.LOCALE_ID)).toString(),
				row.getValue(table.getColumnByName(LeapI18nConstant.ELEMENTID)).toString(),
				row.getValue(table.getColumnByName(LeapI18nConstant.I18NID)).toString(),
				row.getValue(table.getColumnByName(LeapI18nConstant.TEXT_VALUE)).toString());
	}
}
