//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.10.28 at 10:47:27 AM IST 
//


package com.attunedlabs.leapentity.config.jaxb;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="FeatureServiceRouteURI">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="endpoint" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="featureServiceName" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="serviceName" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "featureServiceRouteURI"
})
public class FeatureServiceConfig
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlElement(name = "FeatureServiceRouteURI", required = true)
    protected FeatureServiceRouteURI featureServiceRouteURI;

    /**
     * Gets the value of the featureServiceRouteURI property.
     * 
     * @return
     *     possible object is
     *     {@link FeatureServiceRouteURI }
     *     
     */
    public FeatureServiceRouteURI getFeatureServiceRouteURI() {
        return featureServiceRouteURI;
    }

    /**
     * Sets the value of the featureServiceRouteURI property.
     * 
     * @param value
     *     allowed object is
     *     {@link FeatureServiceRouteURI }
     *     
     */
    public void setFeatureServiceRouteURI(FeatureServiceRouteURI value) {
        this.featureServiceRouteURI = value;
    }

}
