//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.02.08 at 12:39:14 PM IST 
//


package com.attunedlabs.dynastore.config.jaxb;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
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
 *         &lt;element name="DynastoreName">
 *           &lt;complexType>
 *             &lt;simpleContent>
 *               &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *                 &lt;attribute name="version" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/extension>
 *             &lt;/simpleContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="DynastoreInitializer" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="CustomBuilder" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="builder" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="SQLBuilder" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="SQLQuery">
 *                               &lt;complexType>
 *                                 &lt;simpleContent>
 *                                   &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *                                     &lt;attribute name="mappedClass" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                     &lt;attribute name="uniqueColumn" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                   &lt;/extension>
 *                                 &lt;/simpleContent>
 *                               &lt;/complexType>
 *                             &lt;/element>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="InlineBuilder" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;simpleContent>
 *                         &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *                           &lt;attribute name="type" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                         &lt;/extension>
 *                       &lt;/simpleContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *                 &lt;attribute name="required" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *                 &lt;attribute name="type" type="{}DynastoreInitializerType" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="AccessScope" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="Getter">
 *                     &lt;simpleType>
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                         &lt;enumeration value="Feature"/>
 *                         &lt;enumeration value="FeatureGroup"/>
 *                         &lt;enumeration value="Site"/>
 *                         &lt;minLength value="1"/>
 *                       &lt;/restriction>
 *                     &lt;/simpleType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="PublishEvent">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="onReload" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="onTermination" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="onEntryAdded" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="onEntryDeleted" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="onEntryUpdated" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *       &lt;attribute name="isEnabled" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "dynastoreName",
    "dynastoreInitializer",
    "accessScope",
    "publishEvent"
})
public class DynastoreConfiguration
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlElement(name = "DynastoreName", required = true)
    protected DynastoreName dynastoreName;
    @XmlElement(name = "DynastoreInitializer")
    protected DynastoreInitializer dynastoreInitializer;
    @XmlElement(name = "AccessScope")
    protected AccessScope accessScope;
    @XmlElement(name = "PublishEvent", required = true)
    protected PublishEvent publishEvent;
    @XmlAttribute(name = "isEnabled", required = true)
    protected boolean isEnabled;

    /**
     * Gets the value of the dynastoreName property.
     * 
     * @return
     *     possible object is
     *     {@link DynastoreName }
     *     
     */
    public DynastoreName getDynastoreName() {
        return dynastoreName;
    }

    /**
     * Sets the value of the dynastoreName property.
     * 
     * @param value
     *     allowed object is
     *     {@link DynastoreName }
     *     
     */
    public void setDynastoreName(DynastoreName value) {
        this.dynastoreName = value;
    }

    /**
     * Gets the value of the dynastoreInitializer property.
     * 
     * @return
     *     possible object is
     *     {@link DynastoreInitializer }
     *     
     */
    public DynastoreInitializer getDynastoreInitializer() {
        return dynastoreInitializer;
    }

    /**
     * Sets the value of the dynastoreInitializer property.
     * 
     * @param value
     *     allowed object is
     *     {@link DynastoreInitializer }
     *     
     */
    public void setDynastoreInitializer(DynastoreInitializer value) {
        this.dynastoreInitializer = value;
    }

    /**
     * Gets the value of the accessScope property.
     * 
     * @return
     *     possible object is
     *     {@link AccessScope }
     *     
     */
    public AccessScope getAccessScope() {
        return accessScope;
    }

    /**
     * Sets the value of the accessScope property.
     * 
     * @param value
     *     allowed object is
     *     {@link AccessScope }
     *     
     */
    public void setAccessScope(AccessScope value) {
        this.accessScope = value;
    }

    /**
     * Gets the value of the publishEvent property.
     * 
     * @return
     *     possible object is
     *     {@link PublishEvent }
     *     
     */
    public PublishEvent getPublishEvent() {
        return publishEvent;
    }

    /**
     * Sets the value of the publishEvent property.
     * 
     * @param value
     *     allowed object is
     *     {@link PublishEvent }
     *     
     */
    public void setPublishEvent(PublishEvent value) {
        this.publishEvent = value;
    }

    /**
     * Gets the value of the isEnabled property.
     * 
     */
    public boolean isIsEnabled() {
        return isEnabled;
    }

    /**
     * Sets the value of the isEnabled property.
     * 
     */
    public void setIsEnabled(boolean value) {
        this.isEnabled = value;
    }

}