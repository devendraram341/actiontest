package com.attunedlabs.applicationservicehandlers.handler;

import java.util.HashMap;

import org.apache.camel.Exchange;
import org.apache.commons.lang3.time.StopWatch;
import org.json.JSONObject;

import com.attunedlabs.servicehandlers.AbstractServiceHandler;

@SuppressWarnings("unchecked")
public class TimeProfilerHandler extends AbstractServiceHandler {
	static final String BASE = "baseRouteMs";
	static final String BASE_EXEC = "executionMs";
	static final String BASE_EXEC_IMPL = "implementationMs";
	static final String STOPWATCH_KEY = "profiling";

	@Override
	public void preService(Exchange exchange) {
		StopWatch preServiceStopWatch = new StopWatch();
		HashMap<String, StopWatch> profingData = new HashMap<>();
		profingData.put(BASE, preServiceStopWatch);
		preServiceStopWatch.start();
		exchange.getIn().setHeader(STOPWATCH_KEY, profingData);
	}

	@Override
	public void preExec(Exchange exchange) {
		StopWatch preExecStopWatch = new StopWatch();
		HashMap<String, StopWatch> profingData = exchange.getIn().getHeader(STOPWATCH_KEY, HashMap.class);
		profingData.put(BASE_EXEC, preExecStopWatch);
		preExecStopWatch.start();
	}

	@Override
	public void preImpl(Exchange exchange) {
		StopWatch preImplStopWatch = new StopWatch();
		HashMap<String, StopWatch> profingData = exchange.getIn().getHeader(STOPWATCH_KEY, HashMap.class);
		profingData.put(BASE_EXEC_IMPL, preImplStopWatch);
		preImplStopWatch.start();
	}

	@Override
	public void postExec(Exchange exchange) {
		HashMap<String, StopWatch> profingData = exchange.getIn().getHeader(STOPWATCH_KEY, HashMap.class);
		StopWatch stopWatch = profingData.get(BASE_EXEC_IMPL);
		stopWatch.stop();
		StopWatch stopWatchExec = profingData.get(BASE_EXEC);
		stopWatchExec.stop();
	}

	@Override
	public void postService(Exchange exchange) {
		HashMap<String, StopWatch> profingData = exchange.getIn().getHeader(STOPWATCH_KEY, HashMap.class);
		StopWatch stopWatch = profingData.get(BASE);
		stopWatch.stop();

	}

	public JSONObject getProfilingData(Exchange exchange) {
		HashMap<String, StopWatch> profingData = exchange.getIn().getHeader(STOPWATCH_KEY, HashMap.class);
		JSONObject profiling = new JSONObject();
		try {
			JSONObject baseRouteTimings = new JSONObject();
			baseRouteTimings.put(BASE, profingData.get(BASE).getTime());
			baseRouteTimings.put(BASE_EXEC, profingData.get(BASE_EXEC).getTime());
			baseRouteTimings.put(BASE_EXEC_IMPL, profingData.get(BASE_EXEC_IMPL).getTime());
			profiling.put("leapInfraProfiling", baseRouteTimings);
		} catch (Exception e) {
		}
		return profiling;
	}

}
