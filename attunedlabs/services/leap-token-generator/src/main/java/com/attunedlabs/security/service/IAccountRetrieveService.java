package com.attunedlabs.security.service;

import java.util.List;

import com.attunedlabs.security.exception.AccountFetchException;
import com.attunedlabs.security.pojo.AccountDetails;
import com.attunedlabs.security.pojo.AccountConfiguration;

/**
 * IAccountRetrieveService gets the account details from the database.
 * 
 * @author Reactiveworks
 *
 */
public interface IAccountRetrieveService {
	/**
	 * This method is used to get all the {@link AccountDetails} along with the
	 * associated sites.
	 * 
	 * @return {@link List} of {@link AccountDetails}
	 * @throws AccountFetchException
	 */
	public List<AccountDetails> getAllAccountDetails() throws AccountFetchException;

	/**
	 * This method is used to get all the {@link AccountConfiguration} along with
	 * the associated sites.
	 * 
	 * @return {@link List} of {@link AccountConfiguration}
	 * @throws AccountFetchException
	 */
	public List<AccountConfiguration> getAllAccountDetailConfiguration() throws AccountFetchException;

	/**
	 * This method is used to get a {@link AccountDetails} along with the associated
	 * sites for the given account.
	 * 
	 * @param accountName
	 * @return {@link AccountDetails}
	 * @throws AccountFetchException
	 */
	public AccountDetails getAccountDetails(String accountName) throws AccountFetchException;

	/**
	 * This method is used to get a {@link AccountConfiguration} along with the
	 * associated sites for the given account.
	 * 
	 * @param accountName
	 * @return {@link AccountConfiguration}
	 * @throws AccountFetchException
	 */
	public AccountConfiguration getAccountDetailConfiguration(String accountName) throws AccountFetchException;
}
