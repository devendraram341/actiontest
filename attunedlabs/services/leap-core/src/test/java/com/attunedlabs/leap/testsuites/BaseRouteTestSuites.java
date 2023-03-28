package com.attunedlabs.leap.testsuites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.attunedlabs.baseroute.AccessTokenValidationRouteTest;
import com.attunedlabs.baseroute.BaseEntryRouteTest;
import com.attunedlabs.baseroute.BaseTransactionRouteTest;
import com.attunedlabs.baseroute.EntryRouteTest;
import com.attunedlabs.baseroute.ExitRouteTest;
import com.attunedlabs.baseroute.IdentityServiceRouteTest;
import com.attunedlabs.baseroute.LeapEventDispatcherServiceRouteTest;
import com.attunedlabs.baseroute.LeapGetPostRouteTest;
import com.attunedlabs.baseroute.LeapResourceRetentionRouteTest;
import com.attunedlabs.baseroute.ResourceClosingAndDispatcherRouteTest;
import com.attunedlabs.baseroute.RestCallJsonRouteTest;
import com.attunedlabs.baseroute.RestCallXmlRouteTest;
import com.attunedlabs.baseroute.RestGetPostRouteTest;
import com.attunedlabs.baseroute.RestPrettyUriRouteTest;
import com.attunedlabs.baseroute.SubscriberExecutionRouteTest;
import com.attunedlabs.baseroute.SubscriberTransactionRouteTest;
import com.attunedlabs.baseroute.TenantTokenValidationRouteTest;

@RunWith(Suite.class)
@SuiteClasses({
		AccessTokenValidationRouteTest.class,
		BaseEntryRouteTest.class,
		BaseTransactionRouteTest.class,
	 	EntryRouteTest.class,
		ExitRouteTest.class,
		IdentityServiceRouteTest.class,
		LeapEventDispatcherServiceRouteTest.class,
		LeapGetPostRouteTest.class,
		LeapResourceRetentionRouteTest.class,
		ResourceClosingAndDispatcherRouteTest.class,
		RestCallJsonRouteTest.class,
		RestCallXmlRouteTest.class,
		RestGetPostRouteTest.class,
		RestPrettyUriRouteTest.class,
		SubscriberExecutionRouteTest.class,
		SubscriberTransactionRouteTest.class,
		TenantTokenValidationRouteTest.class
	})
public class BaseRouteTestSuites {

}
