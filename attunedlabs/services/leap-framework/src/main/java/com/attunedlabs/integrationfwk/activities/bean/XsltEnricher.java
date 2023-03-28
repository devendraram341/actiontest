package com.attunedlabs.integrationfwk.activities.bean;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.LEAP_DATA_CONTEXT;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.camel.Exchange;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.attunedlabs.config.RequestContext;
import com.attunedlabs.integrationfwk.config.jaxb.PipeActivity;
import com.attunedlabs.integrationfwk.config.jaxb.XsltPathMap;
import com.attunedlabs.integrationfwk.pipeline.service.PipelineServiceConstant;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.LeapServiceContext;
import com.attunedlabs.staticconfig.IStaticConfigurationService;
import com.attunedlabs.staticconfig.StaticConfigFetchException;
import com.attunedlabs.staticconfig.StaticConfigInitializationException;
import com.attunedlabs.staticconfig.factory.StaticConfigurationFactory;
import com.attunedlabs.staticconfig.impl.AccessProtectionException;

public class XsltEnricher {
	private Logger logger = LoggerFactory.getLogger(XsltEnricher.class.getName());
	private IStaticConfigurationService iStaticConfigurationService;

	/**
	 * Bean to process get the data to process and is called only when the
	 * configuration exists
	 * 
	 * @param exchange
	 * @throws ActivityEnricherException
	 * @throws StaticConfigInitializationException
	 * @throws StaticConfigFetchException
	 * @throws AccessProtectionException
	 * @throws FileNotFoundException
	 */
	public void processorBean(Exchange exchange) throws ActivityEnricherException, StaticConfigFetchException,
			StaticConfigInitializationException, AccessProtectionException {
		String methodName = "processorBean";
		logger.debug("{} entered into the method {}.{}()", LEAP_LOG_KEY,getClass().getName(), methodName);
		PipeActivity pipeactivity = (PipeActivity) exchange.getIn().getHeader(ActivityConstant.PIPEACTIVITY_HEADER_KEY);
		logger.trace("{} pipelineActivity in XSLTEnricher : {}",LEAP_LOG_KEY, pipeactivity.getXSLTEnricherActivity().getName());
		List<XsltPathMap> xsltPathList = pipeactivity.getXSLTEnricherActivity().getXsltpathMapper().getXsltPathMap();
		logger.trace("{} xsltPathList size in XSLTEnricher : {}" ,LEAP_LOG_KEY, xsltPathList.size());
		String xmlData = exchange.getIn().getBody(String.class);
		String temp;
		for (XsltPathMap pathMapper : xsltPathList) {
			String xsltNamePath = pathMapper.getFilePath();
			temp = xmlData;
			String xmlOut;
			try {
				xmlOut = xsltTransformFactory(temp, xsltNamePath, exchange);
				xmlData = xmlOut;
				logger.trace("{} Check-in for-each: {}" ,LEAP_LOG_KEY, xmlData);
			} catch (FileNotFoundException e) {
				throw new StaticConfigFetchException("File not found");
			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			}
		}
		logger.info("{} ExchangeBodySet from Xslt-Enricher: {}" ,LEAP_LOG_KEY, xmlData);
		exchange.getIn().setBody(xmlData);
		logger.debug("{} exiting from the {}.{}()", LEAP_LOG_KEY,getClass().getName(), methodName);
	}// ..end of the method

	/**
	 * locally used to instantiate the transformation factory and to process the
	 * transformation
	 * 
	 * @param inputXml
	 * @param xsltName
	 * @return transformed xml
	 * @throws ActivityEnricherException
	 * @throws StaticConfigInitializationException
	 * @throws StaticConfigFetchException
	 * @throws AccessProtectionException
	 * @throws IOException
	 * @throws SAXException
	 */
	private String xsltTransformFactory(String inputXml, String xsltName, Exchange exchange)
			throws ActivityEnricherException, StaticConfigFetchException, StaticConfigInitializationException,
			FileNotFoundException, AccessProtectionException {
		String methodName = "xsltTransformFactory";
		logger.debug("{} entered into the method {}, xsltName :{}", LEAP_LOG_KEY, methodName,xsltName);
		TransformerFactory factory = TransformerFactory.newInstance();

		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext serviceDataContext = leapDataContext.getServiceDataContext();
		RequestContext requestContext = serviceDataContext.getRequestContext();
		logger.trace("{} request context :: {}" ,LEAP_LOG_KEY, requestContext);
		// fetching the filepath from the xsltTransformFactory
		try {
			iStaticConfigurationService = StaticConfigurationFactory.getFilemanagerInstance();
			logger.trace ("{} Static Configuration Service {}",LEAP_LOG_KEY, iStaticConfigurationService);
		} catch (InstantiationException | IllegalAccessException e1) {
			throw new ActivityEnricherException("Unable to initialize file-manager for configurations.." + xsltName,
					e1);
		}
		String xsltFile = null;
		try {
			xsltFile = iStaticConfigurationService.getStaticConfiguration(requestContext, xsltName);
			logger.info("{} .XsltPath from specific tenant: {}",LEAP_LOG_KEY, xsltFile);
		} catch (StaticConfigFetchException | StaticConfigInitializationException | AccessProtectionException e) {
			RequestContext reCntx = new RequestContext(ActivityConstant.GLOBAL_TENANT_ID,
					ActivityConstant.GLOBAL_SITE_ID, requestContext.getFeatureGroup(), requestContext.getFeatureName(),
					requestContext.getImplementationName(), requestContext.getVendor(), requestContext.getVersion());
			xsltFile = iStaticConfigurationService.getStaticConfiguration(requestContext, xsltName);
			logger.debug("{} xslt File Fetch from Global :  {}" ,LEAP_LOG_KEY, xsltFile);
		} catch (Exception e) {

			RequestContext reCntx = new RequestContext(ActivityConstant.GLOBAL_TENANT_ID,
					ActivityConstant.GLOBAL_SITE_ID, requestContext.getFeatureGroup(), requestContext.getFeatureName(),
					requestContext.getImplementationName(), requestContext.getVendor(), requestContext.getVersion());
			xsltFile = iStaticConfigurationService.getStaticConfiguration(reCntx, xsltName);
			logger.debug("{} xslt File Fetch from Global After Exception : {} " ,LEAP_LOG_KEY, xsltFile);

		}
		if (xsltFile == null) {
			RequestContext reCntx = new RequestContext(ActivityConstant.GLOBAL_TENANT_ID,
					ActivityConstant.GLOBAL_SITE_ID, requestContext.getFeatureGroup(), requestContext.getFeatureName(),
					requestContext.getImplementationName(), requestContext.getVendor(), requestContext.getVersion());
			xsltFile = iStaticConfigurationService.getStaticConfiguration(reCntx, xsltName);
			logger.info("{} .XsltPath Fetch from Global : {} ",LEAP_LOG_KEY, xsltFile);

		}
		InputStream in;
			
		in = IOUtils.toInputStream(xsltFile, "UTF-8");
		
		logger.trace("{} input stream in xsltTransformFactor : {}",LEAP_LOG_KEY, in);
		Source xslt;
		xslt = new StreamSource(in);
		logger.info("{} xslt data: {}",LEAP_LOG_KEY, xslt);
		Transformer transformer = null;
		try {
			transformer = factory.newTransformer(xslt);
		} catch (TransformerConfigurationException e) {
			throw new ActivityEnricherException(
					"Unable to instantiate xsltTransformation..withe the current configuration", e);
		}
		Source text = new StreamSource(new StringReader(inputXml));
		logger.trace("{} xml data to be transformed : {}" ,LEAP_LOG_KEY, text);
		StringWriter outWriter = new StringWriter();
		StreamResult result = new StreamResult(outWriter);
		try {
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, PipelineServiceConstant.YES);
			transformer.transform(text, result);
		} catch (TransformerException e) {
			throw new ActivityEnricherException(
					"Unable to perform xsltEnrichment..an exception occured in the transformation", e);
		}
		StringBuffer sb = outWriter.getBuffer();
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return sb.toString();
	}// ..end of the method

}