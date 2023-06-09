//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.06.04 at 11:57:45 AM IST 
//


package com.attunedlabs.eventframework.jaxb;

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
 *         &lt;element name="DispatchChannelId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="EventTransformation">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="XSLTName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="CustomTransformer" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;simpleContent>
 *                         &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *                           &lt;attribute name="fqcn" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                         &lt;/extension>
 *                       &lt;/simpleContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *                 &lt;attribute name="Type" use="required">
 *                   &lt;simpleType>
 *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                       &lt;enumeration value="XML-XSLT"/>
 *                       &lt;enumeration value="JSON"/>
 *                       &lt;enumeration value="CUSTOM"/>
 *                     &lt;/restriction>
 *                   &lt;/simpleType>
 *                 &lt;/attribute>
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
    "dispatchChannelId",
    "eventTransformation"
})
public class EventDispatcher
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlElement(name = "DispatchChannelId", namespace = "http://attunedlabs.com/internalevents/Dispatcher", required = true)
    protected String dispatchChannelId;
    @XmlElement(name = "EventTransformation", namespace = "http://attunedlabs.com/internalevents/Dispatcher", required = true)
    protected EventTransformation eventTransformation;

    /**
     * Gets the value of the dispatchChannelId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDispatchChannelId() {
        return dispatchChannelId;
    }

    /**
     * Sets the value of the dispatchChannelId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDispatchChannelId(String value) {
        this.dispatchChannelId = value;
    }

    /**
     * Gets the value of the eventTransformation property.
     * 
     * @return
     *     possible object is
     *     {@link EventTransformation }
     *     
     */
    public EventTransformation getEventTransformation() {
        return eventTransformation;
    }

    /**
     * Sets the value of the eventTransformation property.
     * 
     * @param value
     *     allowed object is
     *     {@link EventTransformation }
     *     
     */
    public void setEventTransformation(EventTransformation value) {
        this.eventTransformation = value;
    }

}
