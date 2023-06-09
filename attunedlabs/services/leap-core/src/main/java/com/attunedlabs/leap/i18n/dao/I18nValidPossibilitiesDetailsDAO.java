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
import com.attunedlabs.leap.i18n.entity.LeapValidPossibilitiesDetail;
import com.attunedlabs.leap.i18n.exception.LocaleRegistryException;
import com.attunedlabs.leap.i18n.exception.LocaleResolverException;

public class I18nValidPossibilitiesDetailsDAO {

	static Logger logger = LoggerFactory.getLogger(I18nValidPossibilitiesDetailsDAO.class);

	/**
	 * Insert new Valid Possibilities List for new tenant & new site
	 * 
	 * @param tenantId
	 * @param siteId
	 * @param locale
	 * @return
	 * @throws LocaleRegistryException
	 */
	public int insertNewVpDetail(final int vpListId, final String tenantId, final String siteId, final String feature,
			final String localeId, final int seqNumber, final String vpCode, final String textValue)
			throws LocaleRegistryException {
		String methodName = "insertNewVpDetail";
		logger.debug(
				"{} entered into the method {}, tenantId: {}  siteId: {} feature: {} seqNumber: {} vpCode: {} localeId: {} textValue: {}",
				LEAP_LOG_KEY, methodName, tenantId, siteId, feature, seqNumber, vpCode, localeId, textValue);

		Integer generatedId = 0;
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext
					.getTableByQualifiedLabel(LeapI18nConstant.I18N_VALIDPOSSIBILITIESDETAIL_TABLE);
			DefaultUpdateSummary insertSummary = (DefaultUpdateSummary) dataContext.executeUpdate(new UpdateScript() {
				@Override
				public void run(UpdateCallback callback) {
					final RowInsertionBuilder insert = callback.insertInto(table);
					insert.value(LeapI18nConstant.VPLISTID, vpListId).value(LeapI18nConstant.TENANT_ID, tenantId)
							.value(LeapI18nConstant.SITE_ID, siteId).value(LeapI18nConstant.LOCALE_ID, localeId)
							.value(LeapI18nConstant.SEQNUMBER, seqNumber).value(LeapI18nConstant.VPCODE, vpCode)
							.value(LeapI18nConstant.TEXT_VALUE, textValue);
					insert.execute();

				}
			});

			if (insertSummary.getGeneratedKeys().isPresent()) {
				generatedId = Integer
						.parseInt(insertSummary.getGeneratedKeys().get().iterator().next().toString().trim());
				logger.debug("{} insertNewMessage key: {}",LEAP_LOG_KEY, generatedId);
			} else {
				logger.debug("{} insertNewMessage key not found {}",LEAP_LOG_KEY,
						insertSummary.getGeneratedKeys().get().iterator().next());
			}
		} catch (Exception e) {
			throw new LocaleRegistryException("Unable to register the new Message! --> " + e.getMessage(), e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}
		logger.debug("{} exiting from the {}",LEAP_LOG_KEY, methodName);
		return generatedId;
	}

	/**
	 * get the tenant-specific list of all the locale Valid Possibilities Details.
	 * 
	 * @param tenantId
	 * @param siteId
	 * @param localeId
	 * @return
	 * @throws LocaleResolverException
	 */
	public List<LeapValidPossibilitiesDetail> selectAllTenantVpDetail(String tenantId, String siteId, String localeId)
			throws LocaleResolverException {
		String methodName = "selectAllTenantVpDetail";
		logger.debug("{} entered into the method {} tenantId: {},  siteId:{}  localeId:{} ",LEAP_LOG_KEY, methodName, tenantId, siteId,
				localeId);
		List<LeapValidPossibilitiesDetail> listMessageContext = new ArrayList<>();
		LeapValidPossibilitiesDetail messageContext = null;
		DataSet dataSet = null;
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext
					.getTableByQualifiedLabel(LeapI18nConstant.I18N_VALIDPOSSIBILITIESDETAIL_TABLE);
			dataSet = dataContext.query().from(table).selectAll().where(LeapI18nConstant.TENANT_ID).eq(tenantId)
					.and(LeapI18nConstant.SITE_ID).eq(siteId).and(LeapI18nConstant.LOCALE_ID).eq(localeId).execute();
			if (dataSet == null)
				throw new LocaleResolverException("Empty dataSet returned on get all available messages! ");
			Iterator<Row> itr = dataSet.iterator();
			while (itr.hasNext()) {
				Row row = itr.next();
				logger.info("{} fetched config node row: {}",LEAP_LOG_KEY, row);
				messageContext = parseROW(messageContext, row, table);
				logger.info("{} parsed LeapI18nMessage from row retrieved: {}",LEAP_LOG_KEY, messageContext);
				listMessageContext.add(messageContext);
			}
			logger.info("{} list of all parsed LeapI18nMessage from rows retrieved:{} ",LEAP_LOG_KEY, listMessageContext);
		} catch (Exception e) {
			throw new LocaleResolverException("Unable to get all messages details --> " + e.getMessage(), e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}
		logger.debug("{} exiting from the {}",LEAP_LOG_KEY, methodName);
		return listMessageContext;
	}// ..end of the method

	/**
	 * get specific locale Valid Possibilities Details.
	 * 
	 * @param tenantId
	 * @param siteId
	 * @param localeId
	 * @return
	 * @throws LocaleResolverException
	 */
	public LeapValidPossibilitiesDetail selectVpDetail(String tenantId, String siteId, String localeId, String vpCode)
			throws LocaleResolverException {
		String methodName = "selectVpDetail";
		logger.debug("{} entered into the method {} tenantId: {},  siteId:{}  localeId:{} vpCode:{} ",LEAP_LOG_KEY, methodName,
				tenantId, siteId, localeId, vpCode);
		LeapValidPossibilitiesDetail messageContext = null;
		DataSet dataSet = null;
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext
					.getTableByQualifiedLabel(LeapI18nConstant.I18N_VALIDPOSSIBILITIESDETAIL_TABLE);
			dataSet = dataContext.query().from(table).selectAll().where(LeapI18nConstant.TENANT_ID).eq(tenantId)
					.and(LeapI18nConstant.SITE_ID).eq(siteId).and(LeapI18nConstant.LOCALE_ID).eq(localeId)
					.and(LeapI18nConstant.VPCODE).eq(vpCode).execute();
			if (dataSet == null)
				throw new LocaleResolverException("Empty dataSet returned on get all available messages! ");
			Iterator<Row> itr = dataSet.iterator();
			if (itr.hasNext()) {
				Row row = itr.next();
				logger.info("{} fetched config node row: {}",LEAP_LOG_KEY, row);
				messageContext = parseROW(messageContext, row, table);
				logger.info("{} parsed LeapI18nMessage from row retrieved:{} ",LEAP_LOG_KEY, messageContext);
			}
		} catch (Exception e) {
			throw new LocaleResolverException("Unable to get messages details --> " + e.getMessage(), e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}
		logger.debug("{} exiting from the {}",LEAP_LOG_KEY, methodName);
		return messageContext;
	}// ..end of the method

	/**
	 * get the list of all locale Valid Possibilities Detail.
	 * 
	 * @return
	 * @throws LocaleResolverException
	 */
	public List<LeapValidPossibilitiesDetail> selectAllVpDetail() throws LocaleResolverException {
		String methodName = "selectAllVpDetail";
		logger.debug("{} entered into the method {}",LEAP_LOG_KEY, methodName);
		List<LeapValidPossibilitiesDetail> listMessageContext = new ArrayList<>();
		LeapValidPossibilitiesDetail messageContext = null;
		DataSet dataSet = null;
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext
					.getTableByQualifiedLabel(LeapI18nConstant.I18N_VALIDPOSSIBILITIESDETAIL_TABLE);
			dataSet = dataContext.query().from(table).selectAll().execute();
			if (dataSet == null)
				throw new LocaleResolverException("Empty dataSet returned on get all available messages! ");
			Iterator<Row> itr = dataSet.iterator();
			while (itr.hasNext()) {
				Row row = itr.next();
				logger.info("{} fetched config node row: " + row);
				messageContext = parseROW(messageContext, row, table);
				logger.info("{} parsed LeapI18nMessage from row retrieved: " + messageContext);
				listMessageContext.add(messageContext);
			}
			logger.info("{} list of all parsed LeapI18nMessage from rows retrieved: " + listMessageContext);
		} catch (Exception e) {
			throw new LocaleResolverException("Unable to get all messages details --> " + e.getMessage(), e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}
		logger.debug("{} exiting from the {}",LEAP_LOG_KEY, methodName);
		return listMessageContext;
	}// ..end of the method

	/**
	 * 
	 * @param messageContext
	 * @param row
	 * @param table
	 * @return
	 */
	private LeapValidPossibilitiesDetail parseROW(LeapValidPossibilitiesDetail messageContext, Row row, Table table) {
		return messageContext = new LeapValidPossibilitiesDetail(
				// ConfigUtil.conversionOfLongToIntSetup(row.getValue(table.getColumnByName(LeapI18nConstant.MSG_ID))),
				Integer.parseInt(row.getValue(table.getColumnByName(LeapI18nConstant.VPLISTID)).toString()),
				row.getValue(table.getColumnByName(LeapI18nConstant.TENANT_ID)).toString(),
				row.getValue(table.getColumnByName(LeapI18nConstant.SITE_ID)).toString(),
				row.getValue(table.getColumnByName(LeapI18nConstant.LOCALE_ID)).toString(),
				Integer.parseInt(row.getValue(table.getColumnByName(LeapI18nConstant.SEQNUMBER)).toString()),
				row.getValue(table.getColumnByName(LeapI18nConstant.VPCODE)).toString(),
				row.getValue(table.getColumnByName(LeapI18nConstant.TEXT_VALUE)).toString());
	}

}
