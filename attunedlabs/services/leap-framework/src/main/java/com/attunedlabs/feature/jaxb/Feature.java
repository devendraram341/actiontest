//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.09.19 at 05:34:04 PM IST 
//

package com.attunedlabs.feature.jaxb;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for anonymous complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="KafkaEndpointConfig" minOccurs="0">
 *           &lt;complexType>
 *             &lt;simpleContent>
 *               &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *                 &lt;attribute name="brokerHostPort" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="topicName" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="groupId" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="clientId" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="sslTruststoreLocation" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="sslTruststorePassword" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/extension>
 *             &lt;/simpleContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="Service" maxOccurs="unbounded">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="GenericRestEndpoint" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;simpleContent>
 *                         &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *                           &lt;attribute name="httpMethod" use="required">
 *                             &lt;simpleType>
 *                               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                                 &lt;enumeration value="POST"/>
 *                                 &lt;enumeration value="GET"/>
 *                                 &lt;enumeration value="PUT"/>
 *                                 &lt;enumeration value="DELETE"/>
 *                               &lt;/restriction>
 *                             &lt;/simpleType>
 *                           &lt;/attribute>
 *                           &lt;attribute name="urlMappingScheme" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                         &lt;/extension>
 *                       &lt;/simpleContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="ConcreteSoapEndpoint" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;simpleContent>
 *                         &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *                           &lt;attribute name="wsdl" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                         &lt;/extension>
 *                       &lt;/simpleContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *                 &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="enabled" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *                 &lt;attribute name="vendorName" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="Description" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *       &lt;attribute name="featureName" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="version" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="enabled" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="interfaceName" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "kafkaEndpointConfig", "service" })
public class Feature implements Serializable {

	private final static long serialVersionUID = 1L;
	@XmlElement(name = "KafkaEndpointConfig")
	protected KafkaEndpointConfig kafkaEndpointConfig;
	@XmlElement(name = "Service", required = true)
	protected List<Service> service;
	@XmlAttribute(name = "featureName", required = true)
	protected String featureName;
	@XmlAttribute(name = "version", required = true)
	protected String version;
	@XmlAttribute(name = "enabled", required = true)
	protected boolean enabled;
	@XmlAttribute(name = "interfaceName", required = true)
	protected String interfaceName;
	@XmlAttribute(name = "implementationName", required = false)
	protected String implementationName;
	@XmlAttribute(name = "provider", required = false)
	protected String provider;
	@XmlAttribute(name = "vendorTaxonomyId", required = false)
	protected String vendorTaxonomyId;

	/**
	 * Gets the value of the kafkaEndpointConfig property.
	 * 
	 * @return possible object is {@link KafkaEndpointConfig }
	 * 
	 */
	public KafkaEndpointConfig getKafkaEndpointConfig() {
		return kafkaEndpointConfig;
	}

	/**
	 * Sets the value of the kafkaEndpointConfig property.
	 * 
	 * @param value
	 *            allowed object is {@link KafkaEndpointConfig }
	 * 
	 */
	public void setKafkaEndpointConfig(KafkaEndpointConfig value) {
		this.kafkaEndpointConfig = value;
	}

	/**
	 * Gets the value of the service property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the service property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getService().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Service }
	 * 
	 * 
	 */
	public List<Service> getService() {
		if (service == null) {
			service = new ArrayList<Service>();
		}
		return this.service;
	}

	/**
	 * Gets the value of the featureName property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getFeatureName() {
		return featureName;
	}

	/**
	 * Sets the value of the featureName property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setFeatureName(String value) {
		this.featureName = value;
	}

	/**
	 * Gets the value of the version property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Sets the value of the version property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setVersion(String value) {
		this.version = value;
	}

	/**
	 * Gets the value of the enabled property.
	 * 
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Sets the value of the enabled property.
	 * 
	 */
	public void setEnabled(boolean value) {
		this.enabled = value;
	}

	/**
	 * Gets the value of the interfaceName property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getInterfaceName() {
		return interfaceName;
	}

	/**
	 * Sets the value of the interfaceName property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setInterfaceName(String value) {
		this.interfaceName = value;
	}

	public String getImplementationName() {
		return implementationName;
	}

	public void setImplementationName(String implementationName) {
		this.implementationName = implementationName;
	}

	/**
	 * Sets the value of the provider property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setProvider(String provider) {
		this.provider = provider;
	}

	/**
	 * Gets the value of the provider property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */

	public String getProvider() {
		return provider;
	}

	/**
	 * Gets the value of the vendorTaxonomyId property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getVendorTaxonomyId() {
		return vendorTaxonomyId;
	}

	/**
	 * Sets the value of the vendorTaxonomyId property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setVendorTaxonomyId(String vendorTaxonomyId) {
		this.vendorTaxonomyId = vendorTaxonomyId;
	}

}
