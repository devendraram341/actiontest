package com.attunedlabs.eventframework.dispatcher.transformer;

import java.io.StringWriter;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.attunedlabs.ContextData;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.eventframework.event.LeapEvent;

public class XmlTransformerHelperTest {

	final Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * this method used for convert event Object to xml
	 */
	@Test
	public void testConvertEventObjectToXml() {
		RequestContext requestContext = ContextData.getRequestContext();

		LeapEvent leapEvent = new LeapEvent("demoData", requestContext);
		Assert.assertNotNull("Leap Event Value Should Not be null", leapEvent);
		Assert.assertTrue("leap event value shold not be empty ::", !leapEvent.toString().isEmpty());

		XmlTransformerHelper helper = new XmlTransformerHelper();
		String xmlData = helper.convertEventObjectToXml(leapEvent);
		Assert.assertNotNull("XML Data Should not be null ", xmlData);
		Assert.assertTrue("XML data SHould not be empty :: ", !xmlData.isEmpty());
	}

	/**
	 * this method used for test xml to document and document to xml
	 * 
	 * @throws TransformerException
	 */
	@Test
	public void testXMLTODocumentAndDocumentToXML() throws TransformerException {
		String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><Event>\r\n"
				+ "	<id>demoData</id>\r\n" + "	<metadata>\r\n" + "		<tenantId>TestTenant</tenantId>\r\n"
				+ "		<siteId>TestSite</siteId>\r\n" + "		<featuregroup>TestFeatureGroup</featuregroup>\r\n"
				+ "		<feature>TestFeature</feature>\r\n" + "		<EVT_CONTEXT>RequestContext</EVT_CONTEXT>\r\n"
				+ "	</metadata>\r\n" + "</Event>\r\n" + "";

		Document document = XmlTransformerHelper.parse(xmlString);
		Assert.assertNotNull("Document value should be exist ", document);
		Assert.assertEquals("Event", document.getFirstChild().getNodeName());

		String output = getEventXmlAsString(document);
		Assert.assertNotNull(output.trim());
		Assert.assertEquals(xmlString.trim(), output.trim());
	}

	/**
	 * this method used for convert document as string
	 * 
	 * @param document
	 * @return
	 * @throws TransformerException
	 */
	private String getEventXmlAsString(Document document) throws TransformerException {
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		StringWriter writer = new StringWriter();
		transformer.transform(new DOMSource(document), new StreamResult(writer));
		String output = writer.getBuffer().toString();
		return output;

	}

}
