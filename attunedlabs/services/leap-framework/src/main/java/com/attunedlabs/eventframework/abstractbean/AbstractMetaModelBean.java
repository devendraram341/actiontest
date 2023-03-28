package com.attunedlabs.eventframework.abstractbean;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.LEAP_DATA_CONTEXT;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.sql.DataSource;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.metamodel.CompositeDataContext;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.DataContextFactory;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.jdbc.JdbcDataContext;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.schema.TableType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.CannotGetJdbcConnectionException;

import com.attunedlabs.config.RequestContext;
import com.attunedlabs.config.persistence.ConfigurationTreeNode;
import com.attunedlabs.config.persistence.ITenantConfigTreeService;
import com.attunedlabs.config.persistence.UndefinedPrimaryVendorForFeature;
import com.attunedlabs.config.persistence.impl.TenantConfigTreeServiceImpl;
import com.attunedlabs.datacontext.config.DataContextConfigurationException;
import com.attunedlabs.datacontext.config.DataContextConfigurationUnit;
import com.attunedlabs.datacontext.config.IDataContextConfigurationService;
import com.attunedlabs.datacontext.config.impl.DataContextConfigurationService;
import com.attunedlabs.datacontext.jaxb.FeatureDataContext;
import com.attunedlabs.datacontext.jaxb.RefDataContext;
import com.attunedlabs.datacontext.jaxb.RefDataContexts;
import com.attunedlabs.featuredeployment.FeatureDeployment;
import com.attunedlabs.featuredeployment.FeatureDeploymentServiceException;
import com.attunedlabs.featuredeployment.IFeatureDeployment;
import com.attunedlabs.featuredeployment.impl.FeatureDeploymentService;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.LeapServiceContext;

public abstract class AbstractMetaModelBean extends AbstractLeapCamelBean {

	protected static final Logger logger = LoggerFactory.getLogger(AbstractLeapCamelBean.class);
	private DataSource dataSource = null;
	private List<DataSource> dataSourceList;
	private static final String INSERT = "insert";
	private static final String UPDATE = "update";
	private static final String SELECT = "select";
	private static final String DELETE = "delete";

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void setDataSourceList(List<DataSource> dataSourceList) {
		this.dataSourceList = dataSourceList;
	}

	/**
	 * Using the context lookup approach to set datasource bean instance. *
	 * 
	 * @param exchange  : {@link Exchange}
	 * @param dbRefName : name of the db
	 * @throws NoDataSourceFoundException
	 */
	public void setDataSource(Exchange exchange, String dbRefName) throws NoDataSourceFoundException {
		String methodName = "setDataSource";
		logger.debug("{} entered into the method {} dbRefName {}", LEAP_LOG_KEY, methodName, dbRefName);
		CamelContext camelContext = exchange.getContext();
		DataSource dataSourcegetting = (DataSource) camelContext.getRegistry().lookupByName(dbRefName);
		if (dataSourcegetting != null) {
			logger.debug("dataSourcegetting object: " + dataSourcegetting);
			/** setting DataSource **/
			setDataSource(dataSourcegetting);
		} else {
			throw new NoDataSourceFoundException(
					"Unable to fing the datasource by lookup... with beanRef" + dbRefName + " in CamelContext");
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * This method is used to get the datasource
	 * 
	 * 
	 * @return {@link DataSource}
	 */
	public DataSource getDataSource() {
		logger.debug("Logging getDataSource: ");
		if (dataSource != null) {
			return dataSource;
		}
		return dataSource;

	}

	/**
	 * This method is use to give the JDBCDataContext running in transaction
	 * 
	 * @param leapServiceContext :{@link LeapServiceContext}
	 * @return {@link JdbcDataContext}
	 * @throws CannotGetJdbcConnectionException
	 */
	public JdbcDataContext getTransactionalLocalDataContext(LeapServiceContext leapServiceContext)
			throws CannotGetJdbcConnectionException {
		String methodName = "getTransactionalLocalDataContext";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Connection con;
		try {
			con = getConnection(dataSource, leapServiceContext);
			JdbcDataContext metamodelJdbcContext = new JdbcDataContext(con);
			metamodelJdbcContext.setIsInTransaction(true);
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return metamodelJdbcContext;
		} catch (SQLException e) {
			throw new CannotGetJdbcConnectionException(
					"Unable to get the JDBC data context due because connection is unavailable", e);
		}

	}// end of the method getTransactionalLocalDataContext

	/**
	 * This method is use to give the JDBCDataContext running in transaction
	 * 
	 * @param leapServiceContext :{@link LeapServiceContext}
	 * @return {@link JdbcDataContext}
	 * @throws CannotGetJdbcConnectionException
	 */
	public JdbcDataContext getNonTransactionalLocalDataContext(LeapServiceContext leapServiceContext)
			throws CannotGetJdbcConnectionException {
		String methodName = "getNonTransactionalLocalDataContext";
		logger.debug("{} entered into the method {} ", LEAP_LOG_KEY, methodName);
		Connection con;
		try {
			con = getConnection(dataSource, leapServiceContext);
			JdbcDataContext metamodelJdbcContext = new JdbcDataContext(con);
			metamodelJdbcContext.setIsInTransaction(false);
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return metamodelJdbcContext;
		} catch (SQLException e) {
			throw new CannotGetJdbcConnectionException(
					"Unable to get the JDBC data context due because connection is unavailable", e);
		}

	}// end of the method getNonTransactionalLocalDataContext

	/**
	 * This method is used to get DataContext object of a feature
	 * 
	 * @param leapServiceContext : {@link LeapServiceContext}
	 * @return {@link com.attunedlabs.datacontext.jaxb.DataContext}
	 * @throws DataContextConfigurationException
	 * @throws FeatureDeploymentServiceException
	 */
	public com.attunedlabs.datacontext.jaxb.DataContext getFeatureDataContext(String dbRefName,
			LeapServiceContext leapServiceContext)
			throws DataContextConfigurationException, FeatureDeploymentServiceException {
		String methodName = "getFeatureDataContext";
		logger.debug("{} entered into the method {} dbRefName {}", LEAP_LOG_KEY, methodName, dbRefName);
		com.attunedlabs.datacontext.jaxb.DataContext datacontext = null;
		if (leapServiceContext != null) {
			RequestContext requestContext = leapServiceContext.getRequestContext();
			if (requestContext != null) {
				try {
					if (dbRefName == null) {
						datacontext = getDataContextObjectOfFeature(requestContext);
					} else {
						datacontext = getDataContextObjectOfFeature(requestContext, dbRefName);
					}
				} catch (DataContextConfigurationException e) {
					throw new DataContextConfigurationException(
							"Unable to fetch the data context object for the request context : " + requestContext);
				}

			} else {
				// If RequestContext is null then we are trying to get datacontext for active
				// and primary feature.
				// May be code given below is unnecessary
				IFeatureDeployment featureDeploymentservice = new FeatureDeploymentService();
				FeatureDeployment featureDeployment = featureDeploymentservice
						.getActiveAndPrimaryFeatureDeployedFromCache(leapServiceContext.getTenant(),
								leapServiceContext.getSite(), leapServiceContext.getFeatureName(), leapServiceContext);
				if (featureDeployment != null) {
					requestContext = new RequestContext(leapServiceContext.getTenant(), leapServiceContext.getSite(),
							leapServiceContext.getFeatureGroup(), leapServiceContext.getFeatureName(),
							featureDeployment.getImplementationName(), featureDeployment.getVendorName(),
							featureDeployment.getFeatureVersion());
					if (dbRefName == null && dbRefName.isEmpty()) {
						logger.debug("{} dbRefName is null, get 0th index DC", LEAP_LOG_KEY);
						datacontext = getDataContextObjectOfFeature(requestContext);
					} else {
						logger.debug("{} dbRefName is not null get DC {}", LEAP_LOG_KEY, dbRefName);
						datacontext = getDataContextObjectOfFeature(requestContext, dbRefName);
					}
					return datacontext;
				} else {
					throw new FeatureDeploymentServiceException(
							"feature deployment is null for ServiceContext " + leapServiceContext);
				}
			}
		} // end of if(leapServiceContext != null)
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return datacontext;
	}// end of method getFeatureDataContext

	/**
	 * This method is used to get DataContext object of a feature
	 * 
	 * @param dbname             : name of the db
	 * @param leapServiceContext : {@link LeapServiceContext}
	 * @return {@link com.attunedlabs.datacontext.jaxb.DataContext}
	 * @throws DataContextConfigurationException
	 * @throws FeatureDeploymentServiceException
	 */
	protected com.attunedlabs.datacontext.jaxb.DataContext getFeatureDataContextByDbName(String dbname,
			LeapServiceContext leapServiceContext)
			throws DataContextConfigurationException, FeatureDeploymentServiceException {
		String methodName = "getFeatureDataContextByDbName";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		if (leapServiceContext != null) {
			RequestContext requestContext = leapServiceContext.getRequestContext();
			com.attunedlabs.datacontext.jaxb.DataContext datacontext;
			if (requestContext != null) {
				try {
					datacontext = getDataContextObjectOfFeature(requestContext, dbname);
				} catch (DataContextConfigurationException e) {
					throw new DataContextConfigurationException(
							"Unable to fetch the data context object for the request context : " + requestContext);
				}

			} else {
				// If RequestContext is null then we are trying to get datacontext for active
				// and primary feature.
				// May be code given below is unnecessary
				IFeatureDeployment featureDeploymentservice = new FeatureDeploymentService();
				FeatureDeployment featureDeployment = featureDeploymentservice
						.getActiveAndPrimaryFeatureDeployedFromCache(leapServiceContext.getTenant(),
								leapServiceContext.getSite(), leapServiceContext.getFeatureName(), leapServiceContext);
				if (featureDeployment != null) {
					requestContext = new RequestContext(leapServiceContext.getTenant(), leapServiceContext.getSite(),
							leapServiceContext.getFeatureGroup(), leapServiceContext.getFeatureName(),
							featureDeployment.getImplementationName(), featureDeployment.getVendorName(),
							featureDeployment.getFeatureVersion());
					datacontext = getDataContextObjectOfFeature(requestContext, dbname);
					return datacontext;
				} else {
					throw new FeatureDeploymentServiceException(
							"feature deployment is null for ServiceContext " + leapServiceContext);
				}
			}
		} // end of if(leapServiceContext != null)
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return null;
	}// end of method getFeatureDataContextByDbName

	/**
	 * This method is use to get the UpdatebleDataContext Object
	 * 
	 * @param leapServiceContext
	 * @return
	 * @throws SQLException
	 */
	protected UpdateableDataContext getUpdateableDataContext(LeapServiceContext leapServiceContext)
			throws SQLException {
		Connection con = getConnection(dataSource, leapServiceContext);
		UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(con);
		// dataContext.setIsInTransaction(true);
		return dataContext;
	}

	/**
	 * This method is used to get the com.attunedlabs.datacontext.jaxb.DataContext
	 * of reference feature This method should only be call when we have only one
	 * feature data context is defined.
	 * 
	 * @param requestContext : Request Context Object of current feature
	 * @return com.attunedlabs.datacontext.jaxb.DataContext
	 * @throws DataContextConfigurationException
	 */
	private com.attunedlabs.datacontext.jaxb.DataContext getDataContextObjectOfFeature(RequestContext requestContext)
			throws DataContextConfigurationException {
		com.attunedlabs.datacontext.jaxb.DataContext dataContextforFeature = null;
		IDataContextConfigurationService dataContextConfigService = new DataContextConfigurationService();
		DataContextConfigurationUnit dataContextConfigurationUnit = dataContextConfigService
				.getDataContextConfiguration(requestContext);
		FeatureDataContext featureDataContext = (FeatureDataContext) dataContextConfigurationUnit.getConfigData();
		List<com.attunedlabs.datacontext.jaxb.DataContext> dataContextList = featureDataContext.getDataContexts()
				.getDataContext();
		if (dataContextList != null && !(dataContextList.isEmpty())) {
			// get the 0th index feature specific DataContext
			dataContextforFeature = dataContextList.get(0);
			return dataContextforFeature;
		} else
			throw new DataContextConfigurationException(
					"No datacontext defined for the request context: " + requestContext);

	}

	/**
	 * This method is used to get the com.attunedlabs.datacontext.jaxb.DataContext
	 * of reference feature by passing db reference name This method should be used
	 * to fetch datacontext for specified dbRefName
	 * 
	 * @param requestContext : Request Context Object of current feature
	 * @param name           : DB reference name defined in FDC
	 * @return com.attunedlabs.datacontext.jaxb.DataContext
	 * @throws DataContextConfigurationException
	 */

	private com.attunedlabs.datacontext.jaxb.DataContext getDataContextObjectOfFeature(RequestContext requestContext,
			String name) throws DataContextConfigurationException {
		IDataContextConfigurationService dataContextConfigService = new DataContextConfigurationService();
		DataContextConfigurationUnit dataContextConfigurationUnit = dataContextConfigService
				.getDataContextConfiguration(requestContext);

		FeatureDataContext featureDataContext = (FeatureDataContext) dataContextConfigurationUnit.getConfigData();
		List<com.attunedlabs.datacontext.jaxb.DataContext> dataContextList = featureDataContext.getDataContexts()
				.getDataContext();

		boolean dataContextPredicate = true;
		for (com.attunedlabs.datacontext.jaxb.DataContext dataContext : dataContextList) {
			if (dataContext.getDbBeanRefName().equalsIgnoreCase(name)) {
				dataContextPredicate = false;
				return dataContext;
			}
		}
		if (dataContextPredicate) {
			throw new DataContextConfigurationException(
					"Feature specific data context not found for the request context : " + requestContext
							+ " for dbRef name : " + name);
		}
		return null;
	}

	@Deprecated
	protected JdbcDataContext getLocalDataContext(Exchange exchange) throws Exception {
		Connection con = getConnection(dataSource, exchange);
		JdbcDataContext metamodelJdbcContext = new JdbcDataContext(con);
		metamodelJdbcContext.setIsInTransaction(true);
		return metamodelJdbcContext;
	}

	// #TODO This method is not getting used anywhere need to re-think about this,
	// and may require recoding
	protected DataContext getCompositeMetaModelDataContext(Exchange exchange) throws Exception {

		Collection<DataContext> coll = new ArrayList<>();
		logger.trace("{} DataSource List :{} ", LEAP_LOG_KEY, dataSourceList);
		if (dataSourceList != null && !(dataSourceList.isEmpty())) {
			for (DataSource datasource : dataSourceList) {
				Connection connection = getConnection(datasource, exchange);
				DataContext metamodelJdbcContext = new JdbcDataContext(connection);
				coll.add(metamodelJdbcContext);
			}
		}
		DataContext dataContextComposite = new CompositeDataContext(coll);
		return dataContextComposite;
	}

	// #TODO This method is not getting used anywhere need to re-think about this,
	// and may require recoding
	protected JdbcDataContext getMetaModelJdbcDataContext(TableType[] tableTypes, String catalogName, Exchange exchange)

			throws Exception {
		// #TODO Support is not yet Provided
		return null;
	}

	@Override
	protected abstract void processBean(Exchange exch) throws Exception;

	/**
	 * This method is used to get com.attunedlabs.datacontext.jaxb.DataContext
	 * Object
	 * 
	 * @param requestContext : RequestCOntext Object of a feature
	 * @return com.attunedlabs.datacontext.jaxb.FeatureDataContext
	 * @throws DataContextConfigurationException
	 * @throws UndefinedPrimaryVendorForFeature
	 * @throws FeatureDeploymentServiceException
	 */
	@Deprecated
	protected com.attunedlabs.datacontext.jaxb.DataContext getFeatureDataContext(Exchange exchange)
			throws DataContextConfigurationException, UndefinedPrimaryVendorForFeature,
			FeatureDeploymentServiceException {
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext leapServiceContext = leapDataContext.getServiceDataContext();
		RequestContext requestContext = null;
		/**
		 * In some cases the AbstractMetaModel bean getDataContext gets 'null' as
		 * implementation, hence below condition
		 **/
		if (leapServiceContext.getImplementationName() == null) {
			IFeatureDeployment featureDeploymentservice = new FeatureDeploymentService();
			FeatureDeployment featureDeployment = featureDeploymentservice.getActiveAndPrimaryFeatureDeployedFromCache(
					leapServiceContext.getTenant(), leapServiceContext.getSite(), leapServiceContext.getFeatureName(),
					leapServiceContext);
			if (featureDeployment != null) {
				requestContext = new RequestContext(leapServiceContext.getTenant(), leapServiceContext.getSite(),
						leapServiceContext.getFeatureGroup(), leapServiceContext.getFeatureName(),
						featureDeployment.getImplementationName(), featureDeployment.getVendorName(),
						featureDeployment.getFeatureVersion());
			} else {
				throw new UndefinedPrimaryVendorForFeature(
						"feature deployment is null for ServiceContext " + leapServiceContext);
			}
		} else {
			requestContext = leapServiceContext.getRequestContext();
		}

		com.attunedlabs.datacontext.jaxb.DataContext datacontext = getDataContextObjectOfFeature(requestContext);

		return datacontext;
	}

	/**
	 * This method is used to get com.attunedlabs.datacontext.jaxb.DataContext
	 * Object
	 * 
	 * @param requestContext : RequestCOntext Object of a feature
	 * @return com.attunedlabs.datacontext.jaxb.FeatureDataContext
	 * @throws DataContextConfigurationException
	 * @throws FeatureDeploymentServiceException
	 */
	@Deprecated
	protected com.attunedlabs.datacontext.jaxb.DataContext getFeatureDataContext(String dbname, Exchange exchange)
			throws DataContextConfigurationException, FeatureDeploymentServiceException {
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext leapServiceContext = leapDataContext.getServiceDataContext();
		RequestContext requestContext = null;
		/**
		 * In some cases the AbstractMetaModel bean getDataContext gets 'null' as
		 * implementation, hence below condition
		 **/
		if (leapServiceContext.getImplementationName() == null) {
			IFeatureDeployment featureDeploymentservice = new FeatureDeploymentService();
			FeatureDeployment featureDeployment = featureDeploymentservice.getActiveAndPrimaryFeatureDeployedFromCache(
					leapServiceContext.getTenant(), leapServiceContext.getSite(), leapServiceContext.getFeatureName(),
					leapServiceContext);
			if (featureDeployment != null) {
				requestContext = new RequestContext(leapServiceContext.getTenant(), leapServiceContext.getSite(),
						leapServiceContext.getFeatureGroup(), leapServiceContext.getFeatureName(),
						featureDeployment.getImplementationName(), featureDeployment.getVendorName(),
						featureDeployment.getFeatureVersion());
			} else {
				throw new FeatureDeploymentServiceException(
						"feature deployment is null for ServiceContext " + leapServiceContext);
			}
		} else {
			requestContext = leapServiceContext.getRequestContext();
		}

		com.attunedlabs.datacontext.jaxb.DataContext datacontext = getDataContextObjectOfFeature(requestContext,
				dbname);

		return datacontext;
	}

	/**
	 * This method is used to get
	 * com.attunedlabs.datacontext.jaxb.FeatureDataContext Object
	 * 
	 * @param requestContext : RequestCOntext Object of a feature
	 * @return com.attunedlabs.datacontext.jaxb.FeatureDataContext
	 * @throws DataContextConfigurationException
	 */
	// #TODO This method is not getting used anywhere need to re-think about this,
	// and may require recoding
	protected FeatureDataContext getFeatureDataContextObject(RequestContext requestContext)
			throws DataContextConfigurationException {
		String methodName = "getFeatureDataContextObject";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		IDataContextConfigurationService dataContextConfigService = new DataContextConfigurationService();
		DataContextConfigurationUnit dataContextConfigurationUnit = dataContextConfigService
				.getDataContextConfiguration(requestContext);
		FeatureDataContext featureDataContext = (FeatureDataContext) dataContextConfigurationUnit.getConfigData();
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return featureDataContext;
	}

	/**
	 * This method is used to compare datacontext for this feature and other
	 * feature. If same then create Apache metamodel datacontext else create
	 * composite datacontext
	 * 
	 * @param requestContext        : Feature Request Context Object
	 * @param featureDataContext    : FeatureDataContext Object of current feature
	 * @param refFeatureDataContext : FeatureDataContext Object of reference feature
	 * @return
	 */
	// #TODO This method is not getting used anywhere need to re-think about this,
	// and may require recoding
	protected boolean compareDataContext(RequestContext requestContext,

			com.attunedlabs.datacontext.jaxb.DataContext featureDataContext, FeatureDataContext refFeatureDataContext) {
		String methodName = "compareDataContext";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		boolean flag = false;
		String dbBeanRefName = featureDataContext.getDbBeanRefName();
		String dbType = featureDataContext.getDbType();
		String dbHost = featureDataContext.getDbHost();
		String dbPort = featureDataContext.getDbPort();
		String dbSchema = featureDataContext.getDbSchema();
		List<RefDataContexts> refDataContextsList = refFeatureDataContext.getRefDataContexts();
		for (RefDataContexts refDataContexts : refDataContextsList) {
			String featureGroup = refDataContexts.getFeatureGroup();
			String featureName = refDataContexts.getFeatureName();
			if (featureGroup.equalsIgnoreCase(requestContext.getFeatureGroup())
					&& featureName.equalsIgnoreCase(requestContext.getFeatureName())) {
				List<RefDataContext> refDataContextList = refDataContexts.getRefDataContext();
				for (RefDataContext refDataContext : refDataContextList) {
					if (refDataContext.getDbBeanRefName().equalsIgnoreCase(dbBeanRefName)
							&& refDataContext.getDbType().equalsIgnoreCase(dbType)
							&& refDataContext.getDbHost().equalsIgnoreCase(dbHost)
							&& refDataContext.getDbPort().equalsIgnoreCase(dbPort)
							&& refDataContext.getDbSchema().equalsIgnoreCase(dbSchema)) {
						flag = true;
					} else {
						flag = false;
					}
				}
			} // end of if matching fetaureGroup and featureName
		} // end of for(RefDataContexts refDataContexts:refDataContextsList)
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return flag;
	}

	/**
	 * This method is used to get the com.attunedlabs.datacontext.jaxb.DataContext
	 * Object based on feature group and feature name
	 * 
	 * @param featureGroup : Feature group in String
	 * @param feature      : Feature in String
	 * @param exchange     : Camel Exchange Object
	 * @return com.attunedlabs.datacontext.jaxb.DataContext Object
	 * @throws Exception
	 */
	// #TODO This method is not getting used anywhere need to re-think about this,
	// and may require recoding
	protected com.attunedlabs.datacontext.jaxb.DataContext getReferenceFeatureDataContext(String featureGroup,
			String feature, Exchange exchange) throws Exception {
		String methodName = "getReferenceFeatureDataContext";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext leapServiceContext = leapDataContext.getServiceDataContext();
		ITenantConfigTreeService tenantTreeService = TenantConfigTreeServiceImpl.getTenantConfigTreeServiceImpl();
		ConfigurationTreeNode fgconfigNodeTree = tenantTreeService.getPrimaryVendorForFeature(
				leapServiceContext.getTenant(), leapServiceContext.getSite(), featureGroup, feature);
		String vendorName = fgconfigNodeTree.getNodeName();
		String version = fgconfigNodeTree.getVersion();
		RequestContext requestContext = new RequestContext(leapServiceContext.getTenant(), leapServiceContext.getSite(),
				featureGroup, feature, leapServiceContext.getImplementationName(), vendorName, version);
		com.attunedlabs.datacontext.jaxb.DataContext dataContext = getDataContextObjectOfFeature(requestContext);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return dataContext;
	}

	/**
	 * This method is used to get the com.attunedlabs.datacontext.jaxb.DataContext
	 * Object based on feature group,feature name,vendor,version,db name
	 * 
	 * @param featureGroup : Feature group in String
	 * @param feature      : Feature in String
	 * @param vendor       : vendor in String
	 * @param version      : version in String
	 * @param db           name : dbname in String
	 * @param exchange     : Camel Exchange Object
	 * @return com.attunedlabs.datacontext.jaxb.DataContext Object
	 * @throws Exception
	 */
	// #TODO This method is not getting used anywhere need to re-think about this,
	// and may require recoding
	protected com.attunedlabs.datacontext.jaxb.DataContext getReferenceFeatureDataContext(String featureGroup,
			String feature, String vendor, String version, String dbName, Exchange exchange) throws Exception {
		String methodName = "getReferenceFeatureDataContext";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext leapServiceContext = leapDataContext.getServiceDataContext();
		RequestContext requestContext = new RequestContext(leapServiceContext.getTenant(), leapServiceContext.getSite(),
				featureGroup, feature, leapServiceContext.getImplementationName(), vendor, version);
		com.attunedlabs.datacontext.jaxb.DataContext dataContext = getDataContextObjectOfFeature(requestContext,
				dbName);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return dataContext;
	}

	// #TODO This method is not getting used anywhere need to re-think about this,
	// and may require recoding
	protected UpdateableDataContext getUpdateableDataContext(Exchange exchange) throws SQLException {
		Connection con = getConnection(dataSource, exchange);
		UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(con);
		// dataContext.setIsInTransaction(true);
		return dataContext;
	}

	// #TODO This method is not getting used anywhere need to re-think about this,
	// and may require recoding
	protected Table getTableForDataContext(DataContext datacontext, String tableName) {
		Table table = datacontext.getTableByQualifiedLabel(tableName);
		return table;

	}

}
