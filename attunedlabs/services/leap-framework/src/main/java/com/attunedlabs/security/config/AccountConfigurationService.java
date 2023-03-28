package com.attunedlabs.security.config;

import java.util.ArrayList;
import java.util.List;

import com.attunedlabs.config.server.ConfigServerInitializationException;
import com.attunedlabs.config.server.LeapConfigurationServer;
import com.attunedlabs.security.pojo.AccountConfiguration;

/**
 * AccountConfigurationService is used for adding and removing the
 * {@link AccountConfiguration} in the dataGrid.
 * 
 * @author Reactiveworks
 *
 */
public class AccountConfigurationService {

	/**
	 * This method is used to add the {@link List} of {@link AccountConfiguration}
	 * to the dataGrid service.
	 * 
	 * @param configurations
	 * @throws ConfigServerInitializationException
	 */
	public void addAccountConfigurations(List<AccountConfiguration> configurations)
			throws ConfigServerInitializationException {
		LeapConfigurationServer configServer = LeapConfigurationServer.getConfigurationService();
		configServer.addAccountConfiguration(configurations);
	}// end of the method.

	/**
	 * This method is used to add a {@link AccountConfiguration} to the dataGrid
	 * service.
	 * 
	 * @param configuration
	 * @throws ConfigServerInitializationException
	 */
	public void addAccountConfiguration(AccountConfiguration configuration) throws ConfigServerInitializationException {
		LeapConfigurationServer configServer = LeapConfigurationServer.getConfigurationService();
		List<AccountConfiguration> configurations = new ArrayList<>();
		configurations.add(configuration);
		configServer.addAccountConfiguration(configurations);
	}// end of the method.

	/**
	 * This method is used to remove a {@link AccountConfiguration} to the dataGrid
	 * service.
	 * 
	 * @param configuration
	 * @throws ConfigServerInitializationException
	 */
	public void removeAccountConfiguration(AccountConfiguration configuration)
			throws ConfigServerInitializationException {
		LeapConfigurationServer configServer = LeapConfigurationServer.getConfigurationService();
		configServer.removeAccountConfiguration(configuration);
	}// end of the method.
}
