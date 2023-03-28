package com.attunedlabs.servicehandlers.constant;

public class SeviceHandlerConstant {
	
	//List Of Handlers
	public final static String PRE_SERVICE_SYNC_HANDLERS = "preServiceSyncHandlers";
	public final static String PRE_SERVICE_ASYNC_HANDLERS = "preServiceAsyncHandlers";

	public final static String PRE_EXEC_SYNC_HANDLERS = "preExecSyncHandlers";
	public final static String PRE_EXEC_ASYNC_HANDLERS = "preExecAsyncHandlers";

	public final static String PRE_EXEC_ENRICHMENT_SYNC_HANDLERS = "preExecEnricherSyncHandlers";
	public final static String PRE_EXEC_ENRICHMENT_ASYNC_HANDLERS = "preExecEnricherAsyncHandlers";

	public final static String PRE_IMPL_SELECTION_SYNC_HANDLERS = "preImplSelectionSyncHandlers";
	public final static String PRE_IMPL_SELECTION_ASYNC_HANDLERS = "preImplSelectionAsyncHandlers";

	public final static String PRE_IMPL_ENRICHMENT_SYNC_HANDLERS = "preImplEnricherSyncHandlers";
	public final static String PRE_IMPL_ENRICHMENT_ASYNC_HANDLERS = "preImplEnricherAsyncHandlers";

	public final static String PRE_IMPL_SYNC_HANDLERS = "preImplSyncHandlers";
	public final static String PRE_IMPL_ASYNC_HANDLERS = "preImplAsyncHandlers";

	public final static String POST_EXEC_SYNC_HANDLERS = "postExecSyncHandlers";
	public final static String POST_EXEC_ASYNC_HANDLERS = "postExecAsyncHandlers";

	public final static String POST_SERVICE_SYNC_HANDLERS = "postServiceSyncHandlers";
	public final static String POST_SERVICE_ASYNC_HANDLERS = "postServiceAsyncHandlers";
	
	
	//Service Handler execute type
	public final static String SYNC_EXECUTE = "sync";
	public final static String ASYNC_EXECUTE = "async";
	
	
	//Service handler invocation method
	public final static String INSTANCE_HANDLER_INVOCATION = "instance";
	public final static String LOOKUP_HANDLER_INVOCATION = "lookup";
	
	//Service Handler Type
	public final static String PRE_SERVICE_INVOCATION_TYPE = "pre-service";
	public final static String POST_SERVICE_INVOCATION_TYPE = "post-service";
	public final static String PRE_EXEC_INVOCATION_TYPE = "pre-exec";
	public final static String POST_EXEC_INVOCATION_TYPE = "post-exec";
	public final static String PRE_EXEC_ENRICHMENT_INVOCATION_TYPE = "pre-exec-enrichment";
	public final static String PRE_IMPL_SELECTION_INVOCATION_TYPE = "pre-impl-selection";
	public final static String PRE_IMPL_ENRICHMENT_INVOCATION_TYPE = "pre-impl-enrichment";
	public final static String PRE_IMPL_INVOCATION_TYPE = "pre-impl";
	public final static String ALL_INVOCATION_TYPE = "*";
	
	
	// Service Handler Type async
	public final static String PRE_SERVICE_INVOCATION_ASYNC_TYPE = "pre-service-async";
	public final static String POST_SERVICE_INVOCATION_ASYNC_TYPE = "post-service-async";
	public final static String PRE_EXEC_INVOCATION_ASYNC_TYPE = "pre-exec-async";
	public final static String POST_EXEC_INVOCATION_ASYNC_TYPE = "post-exec-async";
	public final static String PRE_EXEC_ENRICHMENT_INVOCATION_ASYNC_TYPE = "pre-exec-enrichment-async";
	public final static String PRE_IMPL_SELECTION_INVOCATION_ASYNC_TYPE = "pre-impl-selection-async";
	public final static String PRE_IMPL_ENRICHMENT_INVOCATION_ASYNC_TYPE = "pre-impl-enrichment-async";
	public final static String PRE_IMPL_INVOCATION_ASYNC_TYPE = "pre-impl-async";
	
	//Async route decider key and route key
	public final static String ASYNC_ROUTE_NAME_KEY = "asyncRouteName";
	public final static String ASYNC_ROUTE_NAME_VALUE = "asyncHandlerRoute";
	
	//camel invoke header
	public final static String INVOKE_TYPE_KEY = "invokeType";
	

}
