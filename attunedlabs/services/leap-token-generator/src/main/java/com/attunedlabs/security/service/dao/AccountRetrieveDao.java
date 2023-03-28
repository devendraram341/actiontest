package com.attunedlabs.security.service.dao;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.jdbc.JdbcDataContext;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.security.TenantSecurityConstant;
import com.attunedlabs.security.pojo.AccountDetails;
import com.attunedlabs.security.pojo.SiteDetatils;
import com.attunedlabs.security.utils.TenantSecurityUtil;

public class AccountRetrieveDao {

	protected static final Logger logger = LoggerFactory.getLogger(AccountRetrieveDao.class);

	private static AccountRetrieveDao retrieveDao;

	private static Table customerAccountTable;
	private static Column accountIdColumn;
	private static Column accountNameColumn;
	private static Column saltSecretKeyColumn;
	private static Column internalTenantIdColumn;
	private static Column accountDescriptionColumn;
	private static Column tenantTokenExpirationColumn;

	private static Table customerSiteTable;
	private static Column siteTableAccountIdColumn;
	private static Column siteIdColumn;
	private static Column domainColumn;
	private static Column siteDescriptionColumn;
	static {
		retrieveDao = new AccountRetrieveDao();
	}

	private AccountRetrieveDao() {
	}

	/**
	 * 
	 * 
	 * @return
	 */
	public static AccountRetrieveDao getInstance() {
		intializeTableAndColumn();
		return retrieveDao;
	}// ..end of the method

	private static void intializeTableAndColumn() {
		Connection dbConnection = TenantSecurityUtil.getDBConnection();
		JdbcDataContext dataContext;
		try {
			dataContext = new JdbcDataContext(dbConnection);
			Schema defaultSchema = dataContext.getDefaultSchema();
			customerAccountTable = defaultSchema.getTableByName(TenantSecurityConstant.TABLE_CUSTOMER_ACCOUNT);
			accountIdColumn = customerAccountTable.getColumnByName(TenantSecurityConstant.ACCOUNT_ID);
			accountNameColumn = customerAccountTable.getColumnByName(TenantSecurityConstant.ACCOUNT_NAME);
			saltSecretKeyColumn = customerAccountTable.getColumnByName(TenantSecurityConstant.SALT_SECRET_KEY);
			internalTenantIdColumn = customerAccountTable.getColumnByName(TenantSecurityConstant.INTERNAL_TENANT);
			accountDescriptionColumn = customerAccountTable.getColumnByName(TenantSecurityConstant.DESCRIPTION);
			tenantTokenExpirationColumn = customerAccountTable
					.getColumnByName(TenantSecurityConstant.TENANT_TOKEN_EXPIRATION);

			customerSiteTable = defaultSchema.getTableByName(TenantSecurityConstant.TABLE_CUSTOMER_SITE);
			siteTableAccountIdColumn = customerSiteTable.getColumnByName(TenantSecurityConstant.ACCOUNT_ID);
			siteIdColumn = customerSiteTable.getColumnByName(TenantSecurityConstant.SITE_ID);
			domainColumn = customerSiteTable.getColumnByName(TenantSecurityConstant.DOMAIN);
			siteDescriptionColumn = customerSiteTable.getColumnByName(TenantSecurityConstant.DESCRIPTION);

		} finally {
			TenantSecurityUtil.dbCleanUp(dbConnection, null);
		}
	}

	public List<AccountDetails> getAllAccountDetails() {
		Connection dbConnection = TenantSecurityUtil.getDBConnection();
		JdbcDataContext dataContext;
		try {
			dataContext = new JdbcDataContext(dbConnection);
			List<AccountDetails> accountDetails = new ArrayList<>();
			DataSet dataSet = dataContext.query().from(customerAccountTable).selectAll().execute();
			Iterator<Row> iterator = dataSet.iterator();
			while (iterator.hasNext()) {
				Row row = (Row) iterator.next();
				if (row != null)
					accountDetails.add(getAccountDetails(row));
			}
			return accountDetails;
		} finally {
			TenantSecurityUtil.dbCleanUp(dbConnection, null);
		}
	}

	private AccountDetails getAccountDetails(Row row) {
		AccountDetails accountDetails = new AccountDetails();
		Object accountId = row.getValue(accountIdColumn);
		Object accountName = row.getValue(accountNameColumn);
		Object saltSecretKey = row.getValue(saltSecretKeyColumn);
		Object internalTenantId = row.getValue(internalTenantIdColumn);
		Object description = row.getValue(accountDescriptionColumn);
		Object tenantTokenExpiration = row.getValue(tenantTokenExpirationColumn);
		if (accountName != null)
			accountDetails.setAccountName(accountName.toString());
		if (saltSecretKey != null)
			accountDetails.setSecretKey(saltSecretKey.toString());
		if (internalTenantId != null)
			accountDetails.setInternalTenantId(internalTenantId.toString());
		if (description != null)
			accountDetails.setDescription(description.toString());
		if (tenantTokenExpiration != null) {
			int expirationCount = (int) tenantTokenExpiration;
			accountDetails.setExpirationCount(expirationCount);
		}
		List<SiteDetatils> allSiteDetatils = getAllSiteDetatils((int) accountId);
		accountDetails.setSiteDetails(allSiteDetatils);
		return accountDetails;
	}

	public List<SiteDetatils> getAllSiteDetatils(final int accountId) {
		Connection dbConnection = TenantSecurityUtil.getDBConnection();
		JdbcDataContext dataContext;
		try {
			dataContext = new JdbcDataContext(dbConnection);
			List<SiteDetatils> detatils = new ArrayList<>();
			DataSet dataSet = dataContext.query().from(customerSiteTable).selectAll()
					.where(TenantSecurityConstant.ACCOUNT_ID).eq(accountId).execute();
			Iterator<Row> iterator = dataSet.iterator();
			while (iterator.hasNext()) {
				Row row = (Row) iterator.next();
				if (row != null)
					detatils.add(getSiteDetails(row));
			}
			return detatils;
		} finally {
			TenantSecurityUtil.dbCleanUp(dbConnection, null);
		}
	}

	private SiteDetatils getSiteDetails(Row row) {
		SiteDetatils siteDetatils = new SiteDetatils();
		Object siteTableAccountId = row.getValue(siteTableAccountIdColumn);
		Object siteId = row.getValue(siteIdColumn);
		Object domain = row.getValue(domainColumn);
		Object description = row.getValue(siteDescriptionColumn);
		if (siteTableAccountId != null)
			siteDetatils.setSiteAccountId((int) siteTableAccountId);
		if (siteId != null)
			siteDetatils.setInternalSiteId(siteId.toString());
		if (domain != null)
			siteDetatils.setDomain(domain.toString());
		if (description != null)
			siteDetatils.setDescription(description.toString());
		return siteDetatils;
	}

	public AccountDetails getAccountDetails(String accountName) {
		Connection dbConnection = TenantSecurityUtil.getDBConnection();
		JdbcDataContext dataContext;
		try {
			dataContext = new JdbcDataContext(dbConnection);
			AccountDetails accountDetails = null;
			DataSet dataSet = dataContext.query().from(customerAccountTable).selectAll().where(accountNameColumn)
					.eq(accountName).execute();
			Iterator<Row> iterator = dataSet.iterator();
			while (iterator.hasNext()) {
				Row row = (Row) iterator.next();
				if (row != null)
					accountDetails = getAccountDetails(row);
			}
			return accountDetails;
		} finally {
			TenantSecurityUtil.dbCleanUp(dbConnection, null);
		}
	}
}
