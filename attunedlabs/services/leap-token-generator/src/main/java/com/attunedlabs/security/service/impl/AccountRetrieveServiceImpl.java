package com.attunedlabs.security.service.impl;

import java.util.ArrayList;
import java.util.List;

import com.attunedlabs.security.exception.AccountFetchException;
import com.attunedlabs.security.pojo.AccountDetails;
import com.attunedlabs.security.pojo.AccountConfiguration;
import com.attunedlabs.security.pojo.SiteDetatils;
import com.attunedlabs.security.service.IAccountRetrieveService;
import com.attunedlabs.security.service.dao.AccountRetrieveDao;

public class AccountRetrieveServiceImpl implements IAccountRetrieveService {
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.attunedlabs.security.service.IAccountRetrieveService#getAllAccountDetails
	 * ()
	 */
	@Override
	public List<AccountDetails> getAllAccountDetails() throws AccountFetchException {
		return AccountRetrieveDao.getInstance().getAllAccountDetails();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.attunedlabs.security.service.IAccountRetrieveService#
	 * getAllAccountDetailConfiguration()
	 */
	@Override
	public List<AccountConfiguration> getAllAccountDetailConfiguration() throws AccountFetchException {
		List<AccountConfiguration> detailsConfigurations = new ArrayList<>();
		AccountConfiguration configuration;
		for (AccountDetails accountDetails : getAllAccountDetails())
			for (SiteDetatils siteDetatils : accountDetails.getSiteDetails()) {
				configuration = new AccountConfiguration(accountDetails.getAccountName(),
						accountDetails.getInternalTenantId(), siteDetatils.getInternalSiteId(),
						siteDetatils.getDomain(), siteDetatils.getTimezone(), accountDetails.getSecretKey(), accountDetails.getExpirationCount());
				detailsConfigurations.add(configuration);
			}
		return detailsConfigurations;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.attunedlabs.security.service.IAccountRetrieveService#getAccountDetails(
	 * java.lang.String)
	 */
	@Override
	public AccountDetails getAccountDetails(String accountName) throws AccountFetchException {
		return AccountRetrieveDao.getInstance().getAccountDetails(accountName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.attunedlabs.security.service.IAccountRetrieveService#
	 * getAccountDetailConfiguration(java.lang.String)
	 */
	@Override
	public AccountConfiguration getAccountDetailConfiguration(String accountName) throws AccountFetchException {
		AccountDetails accountDetails = getAccountDetails(accountName);
		AccountConfiguration configuration = null;
		for (SiteDetatils siteDetatils : accountDetails.getSiteDetails()) {
			configuration = new AccountConfiguration(accountDetails.getAccountName(),
					accountDetails.getInternalTenantId(), siteDetatils.getInternalSiteId(), siteDetatils.getDomain(),
					siteDetatils.getTimezone(), accountDetails.getSecretKey(), accountDetails.getExpirationCount());
		}
		return configuration;
	}

}
