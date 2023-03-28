package com.attunedlabs.integrationfwk.activities.bean;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.LEAP_DATA_CONTEXT;

import java.util.TimeZone;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.LeapHeaderConstant;
import com.attunedlabs.leap.LeapServiceContext;
import com.attunedlabs.security.service.IAccountRegistryService;
import com.attunedlabs.security.service.impl.AccountRegistryServiceImpl;

public class PipelineExchangeInitializer {

	private Logger logger = (Logger) LoggerFactory.getLogger(PipelineExchangeInitializer.class.getName());

	/**
	 * bean method called from the IntegrationPipeactivity, to boot the pipe configs
	 * to the exchange headers, with its size
	 * 
	 * @param exchange
	 */
	public void processPipelineInit(Exchange exchange) {
		String methodName = "processPipelineInit";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		logger.trace("{} The exchange headers: {} , exchangeId : {} ", LEAP_LOG_KEY, exchange.getIn().getHeaders(),
				exchange.getExchangeId());
		if (exchange.getIn().getHeader(LeapHeaderConstant.TIMEZONE) == null)
			setTimeZone(exchange);

	}// ..end of the method

	private void setTimeZone(Exchange exchange) {
		// getting utc based on tenant and site
		String methodName = "setTimeZone";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext serviceDataContext = leapDataContext.getServiceDataContext();
		String tenantId = serviceDataContext.getTenant();
		String siteId = serviceDataContext.getSite();
		if (tenantId == null || siteId == null)
			exchange.getIn().setHeader(LeapHeaderConstant.TIMEZONE, TimeZone.getDefault().getID());
		else {
			IAccountRegistryService accountRegistryService = new AccountRegistryServiceImpl();
			String accountId = null;
			String timeZoneId = TimeZone.getDefault().getID();
			try {
				accountId = accountRegistryService.getAccountIdByTenant(tenantId);
				timeZoneId = accountRegistryService.getTimeZoneBySite(accountId, siteId);
				if (timeZoneId == null || timeZoneId.isEmpty()) {
					logger.trace("{} timezone not found for tenant: {},  site: {}", LEAP_LOG_KEY, tenantId, siteId);
					timeZoneId = TimeZone.getDefault().getID();
				}
			} catch (Exception e) {
				logger.error("{} timezone not found for tenant: {}, site: {} due to {} ", LEAP_LOG_KEY, tenantId,
						siteId, e.getMessage());
				e.printStackTrace();
			}
			exchange.getIn().setHeader(LeapHeaderConstant.TIMEZONE, timeZoneId);

		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

}
