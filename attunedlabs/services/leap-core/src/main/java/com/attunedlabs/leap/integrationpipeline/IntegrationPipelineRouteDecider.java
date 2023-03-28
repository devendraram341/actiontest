package com.attunedlabs.leap.integrationpipeline;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.LEAP_DATA_CONTEXT;

import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.pipeline.PipelineContext;
import com.attunedlabs.config.pipeline.PipelineContextConfig;
import com.attunedlabs.integrationfwk.activities.bean.ActivityConstant;
import com.attunedlabs.integrationfwk.config.IntegrationPipelineConfigUnit;
import com.attunedlabs.integrationfwk.config.jaxb.IntegrationPipe;
import com.attunedlabs.integrationfwk.config.jaxb.PipeActivity;
import com.attunedlabs.integrationfwk.pipeline.service.PipeLineExecutionHelper;
import com.attunedlabs.integrationfwk.pipeline.service.PipelineServiceConstant;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.LeapHeaderConstant;
import com.attunedlabs.leap.LeapServiceContext;

public class IntegrationPipelineRouteDecider {
	final static Logger logger = LoggerFactory.getLogger(IntegrationPipelineRouteDecider.class);

	/**
	 * To load all the pipeActivity from the given integration pipeline.
	 * 
	 * @param configName
	 * @param exchange
	 * @throws InitializingPipelineException
	 */
	public void processAllPipeActivity(Exchange exchange) throws InitializingPipelineException {
		String methodName = "processAllPipeActivity";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		/*
		 * IntegrationPipelineConfigUnit pipelineConfigUnit =
		 * (IntegrationPipelineConfigUnit) exchange.getIn()
		 * .getHeader(PipelineServiceConstant.PIPE_ACTIVITY_KEY_HEADER_KEY);
		 */
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext serviceDataContext = leapDataContext.getServiceDataContext();
		Map<String, Object> integrationCahedObject = serviceDataContext.getIntegrationPipelineFromServiceContext();
		logger.debug("{} pipeline data map is :{} ", LEAP_LOG_KEY, integrationCahedObject);
		IntegrationPipelineConfigUnit pipelineConfigUnit = (IntegrationPipelineConfigUnit) integrationCahedObject
				.get(LeapHeaderConstant.PIPELINE_CONFIG_KEY);
		IntegrationPipe integrationPipe = pipelineConfigUnit.getIntegrationPipe();
		try {
			PipeLineExecutionHelper.updatePipelineContext(exchange, integrationPipe, integrationPipe.getName());
		} catch (Exception e) {
			throw new InitializingPipelineException(e.getMessage(), e);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}// end of method processAllPipeActivity

	/**
	 * Loads the PipeActity one by one that are present in the pipeActivities.
	 * 
	 * @param exchange
	 * @throws Exception
	 */
	public void loadPipeActivity(Exchange exchange) throws Exception {
		String methodName = "loadPipeActivity";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext serviceDataContext = leapDataContext.getServiceDataContext();
		PipelineContext pipelineContext = serviceDataContext.getPipelineContextFromServiceContext();
		int size = pipelineContext.getPipelineContextConfigs().size();
		if (size == 0) {
			logger.debug("pipelineContext data is empty {}", LEAP_LOG_KEY);
			setNullActivity(exchange, pipelineContext, serviceDataContext);
			return;
		}
		int index = size - 1;
		Map<String, Object> serviceRequestData = serviceDataContext.getServiceRequestDataFromServiceContext();
		String sendvalue = (String) serviceRequestData.get(ActivityConstant.FILTER_RESULT_SEND_KEY);
		String dropvalue = (String) serviceRequestData.get(ActivityConstant.FILTER_RESULT_DROP_KEY);
		logger.trace("{} send value : {}, drop value :{}", LEAP_LOG_KEY, sendvalue, dropvalue);
		if (sendvalue == null && dropvalue == null) {
			setActualPipeActivity(exchange, index);
		} else if (sendvalue != null && !(sendvalue.isEmpty()) && sendvalue.equalsIgnoreCase("false")) {
			setNullActivity(exchange, pipelineContext, index, serviceDataContext);
		} else if (dropvalue != null && !(dropvalue.isEmpty()) && dropvalue.equalsIgnoreCase("true")) {
			setNullActivity(exchange, pipelineContext, index, serviceDataContext);
		} else {
			setActualPipeActivity(exchange, index);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * Sets the next pipeActivity in the header.
	 * 
	 * @param exchange
	 * @param pipeActivities
	 * @param pipelineContext
	 * @param index
	 * @param leapHeader
	 * @param pipelineContextConfig
	 * @param pipelineConfigList
	 * @throws Exception
	 */
	private void setActualPipeActivity(Exchange exchange, int index) throws Exception {
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext serviceDataContext = leapDataContext.getServiceDataContext();
		PipelineContext pipelineContext = serviceDataContext.getPipelineContextFromServiceContext();
		List<PipelineContextConfig> pipelineContextConfigs = pipelineContext.getPipelineContextConfigs();
		PipelineContextConfig pipelineContextConfig = pipelineContextConfigs.get(index);
		IntegrationPipe integrationPipe = pipelineContextConfig.getIntegrationPipe();
		List<PipeActivity> pipeActivities = integrationPipe.getPipeActivity();
		PipeActivity pipeactivity = pipeActivities.get(0);
		String key = PipeLineExecutionHelper.getRouteKey(pipeactivity);
		// cannot move this headers to LDC , because these are used in camel route.
		exchange.getIn().setHeader(PipelineServiceConstant.ROUTE_DECIDER_KEY, key);
		exchange.getIn().setHeader(PipelineServiceConstant.PIPE_ACTIVITY_HEADER_KEY, pipeactivity);
		// exchange.getIn().setHeader(PipelineServiceConstant.PIPELINE_NAME,
		// pipelineContextConfig.getName());
		pipeActivities.remove(0);
		if (pipeActivities.isEmpty())
			pipelineContextConfigs.remove(index);
		pipelineContext.setPipelineContextConfigs(pipelineContextConfigs);
		pipelineContext.setPipeActivity(pipeactivity);
		pipelineContext.setPipelineName(pipelineContextConfig.getName());
		serviceDataContext.setPipelineContextInServiceContext(pipelineContext);
		// exchange.getIn().setHeader(LeapHeaderConstant.LEAP_HEADER_KEY, leapHeader);
	}

	/**
	 * Sets the null value in the header to stop the execution.
	 * 
	 * @param exchange
	 */
	private void setNullActivity(Exchange exchange) {
		exchange.getIn().setHeader(PipelineServiceConstant.ROUTE_DECIDER_KEY, null);
		exchange.getIn().setHeader(PipelineServiceConstant.PIPE_ACTIVITY_HEADER_KEY, null);
	}

	/**
	 * Sets the null value in the header to stop the execution.
	 * 
	 * @param exchange
	 * @param leapHeader
	 * @param index
	 * @param pipelineContext
	 * @param pipeActivities
	 */
	private void setNullActivity(Exchange exchange, PipelineContext pipelineContext, int index,
			LeapServiceContext serviceDataContext) {
		setNullActivity(exchange);
		pipelineContext.getPipelineContextConfigs().clear();
		serviceDataContext.setPipelineContextInServiceContext(pipelineContext);
		// exchange.getIn().setHeader(LeapHeaderConstant.LEAP_HEADER_KEY, leapHeader);
	}

	/**
	 * Sets the null value in the header to stop the execution.
	 * 
	 * @param exchange
	 * @param leapHeader
	 * @param index
	 * @param pipelineContext
	 * @param pipeActivities
	 */
	private void setNullActivity(Exchange exchange, PipelineContext pipelineContext,
			LeapServiceContext leapServiceContext) {
		setNullActivity(exchange);
		pipelineContext.getPipelineContextConfigs().clear();
		leapServiceContext.setPipelineContextInServiceContext(pipelineContext);
		// exchange.getIn().setHeader(LeapHeaderConstant.LEAP_HEADER_KEY, leapHeader);
	}
}
