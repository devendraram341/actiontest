package com.attunedlabs.leap.selflink;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.ParseException;
import org.restlet.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.applicationservicehandlers.handler.TimeProfilerHandler;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.LeapServiceContext;
import com.attunedlabs.leap.context.bean.LeapDataElement;
import com.attunedlabs.leap.context.constant.LeapDataContextConstant;
import com.attunedlabs.leap.context.initializer.LeapDataContextInitializer;
import com.attunedlabs.leap.selflink.exception.SelfLinkException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SelfLinkDataProcessor implements Processor {
	static Logger logger = LoggerFactory.getLogger(SelfLinkDataProcessor.class);

	@Override
	public void process(Exchange exchange) throws Exception {
		long start = System.currentTimeMillis();
		try {
			logger.debug("{} inside self link data processor...", LEAP_LOG_KEY);
			LeapDataContext leapDataContext = exchange.getIn().getHeader("leapDataContext", LeapDataContext.class);
			SelfLinkResponse selfLinkResponse = buildSelfLinkResponse(exchange, leapDataContext);
			ObjectMapper mapper = new ObjectMapper();
			String selfLinkStr = mapper.writeValueAsString(selfLinkResponse);
			JSONObject selfLinkJson = new JSONObject(selfLinkStr);
			exchange.getIn().setHeader("selfLink", selfLinkJson);
			System.out.println("selfLinkJson : " + selfLinkJson.toString());
			long stop = System.currentTimeMillis();
			System.out.println("SelfLink Doc JSON Time in ms: " + (stop - start));
		} catch (Exception e) {
			logger.error("error ::" + e);
			throw new SelfLinkException("error :: ", e);
		}
	}

	private SelfLinkResponse buildSelfLinkResponse(Exchange exchange, LeapDataContext leapDataContext)
			throws JSONException, ParseException {

		logger.debug("{} build self link response structure...",LEAP_LOG_KEY);
		long start = System.currentTimeMillis();
		LeapServiceContext leapServiceContext = leapDataContext.getServiceDataContext();

		SelfLinkResponse selfLinkResponse = new SelfLinkResponse();
		LeapDataElement dataElement = leapDataContext.getContextElement(LeapDataContextConstant.INITIAL_CONTEXT);
		JSONObject privateHeaders = leapDataContext.getPrivateHeaders(LeapDataContextConstant.PRIVATE_HEADERS);
		selfLinkResponse.setApiVersion(LeapDataContextConstant.API_VERSION_VAL);
		selfLinkResponse.setContext(LeapDataContextConstant.CONTEXT);
		selfLinkResponse.setCreated(new Date().toString());
		selfLinkResponse.setSelfLink("/" + leapServiceContext.getRunningContextServiceName() + "/" 
					+ leapServiceContext.getRequestUUID());
		selfLinkResponse.setId(leapServiceContext.getRequestUUID());
		selfLinkResponse.setUserId(privateHeaders.getString(LeapDataContextConstant.ACCOUNTID));
		Response response = exchange.getIn().getHeader("CamelRestletResponse", Response.class);
		selfLinkResponse.setStatusCode(response.getStatus().getCode());
		SelfLinkData selfLinkData = new SelfLinkData();
		selfLinkData.setKind("selfLink");
		selfLinkData.setUpdated(new Date().toString());
		ItemData itemData = new ItemData();
		JSONObject constructLeapDataJsonForRequest = LeapDataContextInitializer
				.constructLeapDataJsonForRequest(dataElement, exchange);
		JSONObject constructLeapDataJsonForResponse = LeapDataContextInitializer
				.constructLeapDataJsonForResponse(exchange);
		itemData.setRequest(constructLeapDataJsonForRequest.toMap());
		itemData.setResponse(constructLeapDataJsonForResponse.toMap());
		itemData.setProfiling(new TimeProfilerHandler().getProfilingData(exchange).toMap());

		List<ItemData> asList = Arrays.asList(itemData);
		selfLinkData.setTotalItems(asList.size());
		selfLinkData.setItems(asList);

		selfLinkResponse.setData(selfLinkData);
		long stop = System.currentTimeMillis();
		System.out.println("SelfLink Doc Obj Time in ms: " + (stop - start));
		return selfLinkResponse;
	}

}
