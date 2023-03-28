/**
 * 
 */
package com.attunedlabs.leap.eventsubscription.routebuilder;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;

import java.util.Set;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.server.LeapConfigurationServer;
import com.attunedlabs.eventframework.config.IEventFrameworkConfigService;
import com.attunedlabs.eventframework.config.impl.EventFrameworkConfigService;
import com.attunedlabs.eventframework.jaxb.JMSSubscribeEvent;
import com.attunedlabs.eventsubscription.exception.MissingConfigurationException;
import com.attunedlabs.eventsubscription.util.SubscriptionConstant;
import com.attunedlabs.eventsubscription.util.SubscriptionUtil;
import com.attunedlabs.eventsubscriptiontracker.IEventSubscriptionTrackerService;
import com.attunedlabs.eventsubscriptiontracker.impl.EventSubscriptionTrackerImpl;
import com.attunedlabs.leap.context.constant.LeapDataContextConstant;
import com.attunedlabs.leap.eventsubscription.lifecylce.bean.JMSSubscriptionFailureHandlerBean;
import com.attunedlabs.leap.eventsubscription.lifecylce.bean.JMSSubscriptionPerProcessHandlerBean;
import com.attunedlabs.leap.eventsubscription.lifecylce.bean.JMSSubscriptionSuccessHandlerBean;
import com.attunedlabs.leap.eventsubscription.processor.HttpPostRequestProcessor;
import com.attunedlabs.leap.eventsubscription.processor.InvokeCamelRouteProcessor;
import com.attunedlabs.leap.eventsubscription.processor.JMSSubscriberActionIndentificationProcessor;
import com.attunedlabs.leap.eventsubscription.processor.JMSSubscriberRoutingRuleCalculationProcessor;
import com.attunedlabs.leap.eventsubscription.processor.JMSSubscriptionCriteriaEvaluationProcessor;
import com.attunedlabs.leap.eventsubscription.processor.PipelineProcessor;

/**
 * @author reactiveworks
 *
 */
public class JMSSubscriberEvaluationRouteBuilder extends RouteBuilder {

	final static Logger log = LoggerFactory.getLogger(JMSSubscriberEvaluationRouteBuilder.class);
	protected final IEventFrameworkConfigService eventFrameworkConfigService = new EventFrameworkConfigService();
	protected final static SubscriptionUtil subscriptionUtil = new SubscriptionUtil();

	@Override
	public void configure() throws Exception {

		final LeapConfigurationServer leapConfigurationServer = LeapConfigurationServer.getConfigurationService();

		// get the topics names from the subscription configured by
		// feature developer and subscribe to all the topics.
		Set<String> subscribers = leapConfigurationServer.getAllJMSSubscribersAvailable();
		if (!subscribers.isEmpty()) {
			boolean isRetryRouteBuild = false;
			for (final String subscriptionId : subscribers) {
				log.info("{} in for loop subscriptionId: {}", LEAP_LOG_KEY, subscriptionId);
				// for getting the jms event configuration from cache
				ConfigurationContext configCtx = subscriptionUtil.buildConfigContext(
						LeapDataContextConstant.GLOBAL_TENANT_ID, LeapDataContextConstant.GLOBAL_SITE_ID,
						subscriptionId);
				log.trace("{} ConfigurationContext formed {}", LEAP_LOG_KEY, configCtx);
				if (configCtx == null)
					throw new MissingConfigurationException(
							"MISSING CONFIGURATION PARAMETERS: for subscriber with Id : " + subscriptionId
									+ " ==> expected parameters are not present to build ConfigurationContext!");
				JMSSubscribeEvent jmsEventSubscriptionConfiguration = eventFrameworkConfigService
						.getJMSEventSubscriptionConfiguration(configCtx,
								subscriptionUtil.getActualSubscriberId(subscriptionId));
				RouteDefinition startSubscriberRouteEndpoint = null;
				/**********
				 * PER SUBSCRIBER DIRECT and RETRY ROUTE
				 ***********/
				if (!isRetryRouteBuild) {
					startSubscriberRouteEndpoint = from(
							SubscriptionConstant.SIMPLE_PROCESSING_ROUTE_ENDPOINT + subscriptionId)
									.from(SubscriptionUtil.constructJMSRetryConsumeQueueURI(subscriptionId,
											jmsEventSubscriptionConfiguration));
					isRetryRouteBuild = true;
				} else
					startSubscriberRouteEndpoint = from(
							SubscriptionConstant.SIMPLE_PROCESSING_ROUTE_ENDPOINT + subscriptionId)
									.from(SubscriptionUtil.constructJMSRetryConsumeQueueURI(subscriptionId,
											jmsEventSubscriptionConfiguration));

				// write the exception handling logic to retry here
				// for the failed subscribers.
				// continue to the next iterations on exception type by writing
				// own predicates.
				startSubscriberRouteEndpoint.onException(Exception.class).continued(true)
						.process(new JMSSubscriptionFailureHandlerBean()).end()

						.log("start the jms subscription process for queue ...")
						.setProperty(SubscriptionConstant.IS_JMS_SUBSCRIPTION_INVOCATION_KEY).constant(true)

						// do this stuff if invoked via retry Thread
						.choice().when(header(SubscriptionConstant.JMS_CALL).isNull())

						// Subscription criteria evaluation
						.process(new JMSSubscriptionCriteriaEvaluationProcessor(eventFrameworkConfigService,
								subscriptionUtil))
						// invoking pre-process activity from retry
						// lifecycle
						.process(new JMSSubscriptionPerProcessHandlerBean()).endChoice().end()

						// calculating the number of event routing rules
						// configured for subscriber.
						.process(new JMSSubscriberRoutingRuleCalculationProcessor(eventFrameworkConfigService,
								subscriptionUtil))

						// get the count of rules
						.loop(header(SubscriptionConstant.ROUTING_RULES_PER_SUBSCIBER_LOOP_COUNT_KEY))

						// and clone the exchange for every routing
						// rule.
						.copy()

						// exception handling to skip the iteration if
						// the
						// routing rules specified by subscriber fails
						// to
						// evaluate.
						.doTry()

						/** ACTION IDENTIFICATION MECHANISM **/
						.process(new JMSSubscriberActionIndentificationProcessor(subscriptionUtil)).choice()

						/** INVOKE CAMEL ROUTE ACTION **/
						.when(header(SubscriptionConstant.ACTION_KEY)
								.isEqualTo(SubscriptionConstant.INVOKE_CAMEL_ROUTE_KEY))
						// invoke the camel route..
						.process(new InvokeCamelRouteProcessor(subscriptionUtil))

						// new exchange is created and forwarded to
						// respective route.
						.choice().when(header(SubscriptionConstant.ROUTE_ENDPOINT_KEY).isNotNull())
						.toD("${header." + SubscriptionConstant.ROUTE_ENDPOINT_KEY + "}").process(new Processor() {

							@Override
							public void process(Exchange exchange) throws Exception {
								log.debug(
										"{} processor invocation after INVOKE_CAMEL_ROUTE in JMSSubscriberEvaluationRouteBuilder",
										LEAP_LOG_KEY);
								Message outMessage = exchange.getIn();
								log.info(
										"{} CamelExchange after invocation: Headers in JMSSubscriberEvaluationRouteBuilder => {}",
										LEAP_LOG_KEY, outMessage.getHeaders());
								log.info(
										"{} CamelExchange after invocation: BODY in JMSSubscriberEvaluationRouteBuilder => {}",
										LEAP_LOG_KEY, outMessage.getBody());
							}
						}).endChoice().endChoice()

						/** HTTP POST REQUEST ACTION **/
						.when(header(SubscriptionConstant.ACTION_KEY)
								.isEqualTo(SubscriptionConstant.HTTP_POST_REQUEST_KEY))
						// invoke service
						.process(new HttpPostRequestProcessor(subscriptionUtil)).process(new Processor() {

							@Override
							public void process(Exchange exchange) throws Exception {
								log.debug(
										"{} processor invocation after  HTTP_POST_REQUEST in JMSSubscriberEvaluationRouteBuilder",
										LEAP_LOG_KEY);
								Message outMessage = exchange.getIn();
								log.info(
										"{} CamelExchange after call: HTTP_POST_REQUEST Headers in JMSSubscriberEvaluationRouteBuilder => {}",
										LEAP_LOG_KEY, outMessage.getHeaders());
								log.info(
										"{} CamelExchange after call: HTTP_POST_REQUEST BODY in JMSSubscriberEvaluationRouteBuilder=> {}",
										LEAP_LOG_KEY, outMessage.getBody());
							}
						}).endChoice()

						/** PIPELINE ACTION **/
						.when(header(SubscriptionConstant.ACTION_KEY).isEqualTo(SubscriptionConstant.PIPELINE_KEY))
						.process(new PipelineProcessor(subscriptionUtil))
						.toD("${header." + SubscriptionConstant.ROUTE_ENDPOINT_KEY + "}").process(new Processor() {
							@Override
							public void process(Exchange exchange) throws Exception {
								log.debug(
										"{} processor invocation for evaluating PIPELINE in JMSSubscriberEvaluationRouteBuilder",
										LEAP_LOG_KEY);
								Message outMessage = exchange.getIn();
								log.info(
										"{} CamelExchange after PIPELINE: Headers in JMSSubscriberEvaluationRouteBuilder => {}",
										LEAP_LOG_KEY, outMessage.getHeaders());
								log.info(
										"{} CamelExchange after PIPELINE: BODY in JMSSubscriberEvaluationRouteBuilder=> {}",
										LEAP_LOG_KEY, outMessage.getBody());
							}
						}).endChoice().

						/** DEFAULT ACTION **/
						otherwise().process(new Processor() {
							@Override
							public void process(Exchange exchange) throws Exception {
								log.debug(
										"{} processor invocation for evaluating UNSUPPORTED_ACTIONS in JMSSubscriberEvaluationRouteBuilder", LEAP_LOG_KEY);
								Message outMessage = exchange.getIn();
								log.info(
										"{} CamelExchange after UNSUPPORTED_ACTIONS: Headers in JMSSubscriberEvaluationRouteBuilder=> {}", LEAP_LOG_KEY,
										outMessage.getHeaders());
								log.info(
										"{} CamelExchange after UNSUPPORTED_ACTIONS: BODY in JMSSubscriberEvaluationRouteBuilder=> {}", LEAP_LOG_KEY,
										outMessage.getBody());
							}
						})

						.endChoice().end()

						// do some task on the exchange once subscriber
						// gets the message.
						.process(new JMSSubscriptionSuccessHandlerBean())

						// end of both the try blocks
						.endDoTry()

						// catch the exception raised.
						.doCatch(Exception.class)

						// warp the exception and rethrow it the route
						// level onException clause specified.
						.process(new Processor() {
							public void process(Exchange exchange) throws Exception {
								Exception exception = (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
								exception.printStackTrace();
								log.debug("{} wrap and rethrow routing rule loop exception {}", exception.getMessage());
								throw exception;
							}
						})

						// end of inner-most try-catch clause for
						// routing
						// rule loop.
						.end()
						// end of inner loop for event routing rule.
						.end();

			}
		} else
			log.info("{} There is no subsciber feature found to load...", LEAP_LOG_KEY);

	}

}
