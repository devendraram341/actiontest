package com.attunedlabs.eventsubscription.defaultretrystrategy;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.json.simple.JSONObject;

import com.attunedlabs.eventsubscription.abstractretrystrategy.AbstractSubscriptionRetryStrategy;
import com.attunedlabs.eventsubscription.exception.ConfigurationValidationFailedException;
import com.attunedlabs.eventsubscription.retrypolicy.SubscriptionNoRetryPolicy;
import com.attunedlabs.eventsubscription.util.EventSubscriptionTrackerConstants;
import com.attunedlabs.eventsubscription.util.SubscriptionConstant;
import com.attunedlabs.eventsubscriptiontracker.EventSubscriptionTracker;

/**
 * Default implementation provided for {@link AbstractSubscriptionRetryStrategy}
 * class. This class will store the record in EventSubscriptionTracker table and
 * also take care of the status at each stage.Retry attempt will not be
 * performed and also whether to log or not to the table also can be decided
 * with configuration.
 * 
 * @see : {@link SubscriptionNoRetryPolicy}
 * @author Reactiveworks42
 *
 */
public class LeapNoRetryStrategy extends AbstractSubscriptionRetryStrategy {

	public LeapNoRetryStrategy(String strategyConfig) throws ConfigurationValidationFailedException {
		super(strategyConfig);
		initializeRetryConfig(getRetryConfiguration());

	}

	@Override
	public boolean validatePassedStrategyConfiguration() {
		return true;
	}

	@Override
	public List<String> getAllStrategyConfigurationKeys() {
		return Arrays.asList(new String[] { SubscriptionNoRetryPolicy.MSG_LOGGING_ENABLED_KEY });
	}

	@Override
	public void preProcessing(Exchange exchange, Map<String, Object> metaData) {
		String methodName = "preProcessing";
		log.debug("{} entered into the method {} of NonRetryableStrategy...", LEAP_LOG_KEY, methodName);
		if (SubscriptionNoRetryPolicy.assertMessageLogEnabled(this.getRetryConfiguration())) {
			if (eventSubscriptionLogService.recordIsNotAlreadyPresent(exchange, metaData)) {
				eventSubscriptionLogService.addNewSubscriptionRecord(exchange, metaData);
//				eventSubscriptionLogService.updateSubscriptionRecordStatus(exchange, metaData,
//						EventSubscriptionTrackerConstants.STATUS_IN_PROCESS, null, this.getRetryConfiguration());
			}
		} else
			log.debug("{} PRE_PROCESS {}", LEAP_LOG_KEY,
					(EventSubscriptionTracker) metaData.get(SubscriptionConstant.EVENT_SUBSCRIPTION_TRACKER_CLASS));
		log.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	@Override
	public void onSuccess(Exchange exchange, Map<String, Object> metaData) {
		String methodName = "onSuccess";
		log.debug("{} entered into the method {} of NonRetryableStrategy...", LEAP_LOG_KEY, methodName);
		if (SubscriptionNoRetryPolicy.assertMessageLogEnabled(getRetryConfiguration())) {
			eventSubscriptionLogService.updateSubscriptionRecordStatus(exchange, metaData,
					EventSubscriptionTrackerConstants.STATUS_COMPLETE, null, this.getRetryConfiguration());
		} else
			log.debug("{} SUCCESS {}", LEAP_LOG_KEY,
					(EventSubscriptionTracker) metaData.get(SubscriptionConstant.EVENT_SUBSCRIPTION_TRACKER_CLASS));
		log.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	@Override
	public void onFailure(Exchange exchange, Map<String, Object> metaData, Exception exception) {
		String methodName = "onFailure";
		log.debug("{} entered into the method {} of NonRetryableStrategy...", LEAP_LOG_KEY, methodName);
		if (SubscriptionNoRetryPolicy.assertMessageLogEnabled(getRetryConfiguration())) {
			eventSubscriptionLogService.updateSubscriptionRecordStatus(exchange, metaData,
					EventSubscriptionTrackerConstants.STATUS_FAILED, exception, this.getRetryConfiguration());
		} else
			log.debug("{} FAILED {}", LEAP_LOG_KEY,
					(EventSubscriptionTracker) metaData.get(SubscriptionConstant.EVENT_SUBSCRIPTION_TRACKER_CLASS));
		log.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	@Override
	public List<EventSubscriptionTracker> getRetryableRecords(Exchange exchange, String tenantId, String siteId,
			String subscriptionId) throws Exception {
		log.debug("{} inside getRetryableRecords() of NonRetryableStrategy...", LEAP_LOG_KEY);
		return new ArrayList<>();
	}

	/**
	 * initialize in jsonConfig for specific default no strategy.
	 * 
	 * @param retryConfigurationJSON
	 */
	private void initializeRetryConfig(JSONObject retryConfigurationJSON) {
		SubscriptionNoRetryPolicy.assertMessageLogEnabled(retryConfigurationJSON);
		SubscriptionNoRetryPolicy.assertParallelProcessingEnabled(retryConfigurationJSON);
	}

}
