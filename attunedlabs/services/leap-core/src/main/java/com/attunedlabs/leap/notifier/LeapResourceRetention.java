package com.attunedlabs.leap.notifier;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;

import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.LEAP_DATA_CONTEXT;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.leap.LeapDataConstant;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.LeapHeader;
import com.attunedlabs.leap.LeapHeaderConstant;
import com.attunedlabs.leap.LeapServiceContext;

/**
 * LeapResourceRetention will release all the resources that are opened during
 * the service execution.
 * 
 * @author Reactiveworks
 *
 */
public class LeapResourceRetention {

	final Logger logger = LoggerFactory.getLogger(LeapResourceRetention.class);

	/**
	 * Closes all the opened resources during the service execution.
	 * 
	 * @param exchange
	 * @throws SQLException
	 */
	public void closeAllDataSourceConnection(Exchange exchange) throws SQLException {

		// fetching the leap Service Context object
		Object leapDataContextObj = exchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		if (leapDataContextObj != null && leapDataContextObj instanceof LeapDataContext) {
			LeapDataContext leapDataContext = (LeapDataContext) leapDataContextObj;
			LeapServiceContext leapServiceContext = leapDataContext.getServiceDataContext();

			// fetching all the resources that are opened during the service
			// execution.
			Map<Object, Object> mapResourceHolder = leapServiceContext.getResourceHolder();
			logger.debug("{} Resource holder size :{} ", LEAP_LOG_KEY, mapResourceHolder.size());
			for (int idx = 0; idx < mapResourceHolder.size(); idx++) {
				Connection connection = (Connection) mapResourceHolder.get(idx);
				if (connection != null) {
					if (!(connection.isClosed())) {
						connection.close();
					}
				}
			} // end of for
		}
	}// end of method closeAllConnection
}
