//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.2-147 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.08.24 at 11:02:24 AM IST 
//


package com.attunedlabs.featuremetainfo.jaxb;

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
 *         &lt;element name="Name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element ref="{http://attunedlabs.com/internal/FeatureMetaInfoSupporting}EventResources" minOccurs="0"/>
 *         &lt;element ref="{http://attunedlabs.com/internal/FeatureMetaInfoSupporting}ServiceHandlerConfiguration" minOccurs="0"/>
 *         &lt;element ref="{http://attunedlabs.com/internal/FeatureMetaInfoSupporting}PermaStoreConfigurations" minOccurs="0"/>
 *         &lt;element ref="{http://attunedlabs.com/internal/FeatureMetaInfoSupporting}DynaStoreConfigurations" minOccurs="0"/>
 *         &lt;element ref="{http://attunedlabs.com/internal/FeatureMetaInfoSupporting}PolicyConfigurations" minOccurs="0"/>
 *         &lt;element ref="{http://attunedlabs.com/internal/FeatureMetaInfoSupporting}ScheduledJobConfigurations" minOccurs="0"/>
 *         &lt;element name="Features" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;choice>
 *                   &lt;element name="Feature" maxOccurs="unbounded" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;all>
 *                             &lt;element name="Name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                             &lt;element ref="{http://attunedlabs.com/internal/FeatureMetaInfoSupporting}EventResources" minOccurs="0"/>
 *                             &lt;element ref="{http://attunedlabs.com/internal/FeatureMetaInfoSupporting}ServiceHandlerConfiguration" minOccurs="0"/>
 *                             &lt;element ref="{http://attunedlabs.com/internal/FeatureMetaInfoSupporting}PermaStoreConfigurations" minOccurs="0"/>
 *                             &lt;element ref="{http://attunedlabs.com/internal/FeatureMetaInfoSupporting}DynaStoreConfigurations" minOccurs="0"/>
 *                             &lt;element ref="{http://attunedlabs.com/internal/FeatureMetaInfoSupporting}PolicyConfigurations" minOccurs="0"/>
 *                             &lt;element ref="{http://attunedlabs.com/internal/FeatureMetaInfoSupporting}ScheduledJobConfigurations" minOccurs="0"/>
 *                             &lt;element name="FeatureImplementations" minOccurs="0">
 *                               &lt;complexType>
 *                                 &lt;complexContent>
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     &lt;sequence>
 *                                       &lt;element name="FeatureImplementation" maxOccurs="unbounded" minOccurs="0">
 *                                         &lt;complexType>
 *                                           &lt;simpleContent>
 *                                             &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *                                               &lt;attribute name="resourceName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                             &lt;/extension>
 *                                           &lt;/simpleContent>
 *                                         &lt;/complexType>
 *                                       &lt;/element>
 *                                     &lt;/sequence>
 *                                   &lt;/restriction>
 *                                 &lt;/complexContent>
 *                               &lt;/complexType>
 *                             &lt;/element>
 *                             &lt;element name="FeatureDataContexts" minOccurs="0">
 *                               &lt;complexType>
 *                                 &lt;complexContent>
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     &lt;sequence>
 *                                       &lt;element name="DataContexts" maxOccurs="unbounded">
 *                                         &lt;complexType>
 *                                           &lt;simpleContent>
 *                                             &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *                                               &lt;attribute name="resourceName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                             &lt;/extension>
 *                                           &lt;/simpleContent>
 *                                         &lt;/complexType>
 *                                       &lt;/element>
 *                                     &lt;/sequence>
 *                                   &lt;/restriction>
 *                                 &lt;/complexContent>
 *                               &lt;/complexType>
 *                             &lt;/element>
 *                             &lt;element name="IntegrationPipeLineConfigurations" minOccurs="0">
 *                               &lt;complexType>
 *                                 &lt;complexContent>
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     &lt;sequence>
 *                                       &lt;element name="PipeConfiguration" maxOccurs="unbounded">
 *                                         &lt;complexType>
 *                                           &lt;simpleContent>
 *                                             &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *                                               &lt;attribute name="resourceName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                             &lt;/extension>
 *                                           &lt;/simpleContent>
 *                                         &lt;/complexType>
 *                                       &lt;/element>
 *                                     &lt;/sequence>
 *                                   &lt;/restriction>
 *                                 &lt;/complexContent>
 *                               &lt;/complexType>
 *                             &lt;/element>
 *                             &lt;element name="StaticFileConfiguration" minOccurs="0">
 *                               &lt;complexType>
 *                                 &lt;complexContent>
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     &lt;sequence>
 *                                       &lt;element name="ConfigFile" maxOccurs="unbounded" minOccurs="0">
 *                                         &lt;complexType>
 *                                           &lt;simpleContent>
 *                                             &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *                                               &lt;attribute name="filePath" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                               &lt;attribute name="fileName" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                             &lt;/extension>
 *                                           &lt;/simpleContent>
 *                                         &lt;/complexType>
 *                                       &lt;/element>
 *                                     &lt;/sequence>
 *                                   &lt;/restriction>
 *                                 &lt;/complexContent>
 *                               &lt;/complexType>
 *                             &lt;/element>
 *                             &lt;element name="DBConfiguration" minOccurs="0">
 *                               &lt;complexType>
 *                                 &lt;complexContent>
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     &lt;sequence>
 *                                       &lt;element name="database">
 *                                         &lt;complexType>
 *                                           &lt;complexContent>
 *                                             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                               &lt;sequence>
 *                                                 &lt;element name="table" maxOccurs="unbounded">
 *                                                   &lt;complexType>
 *                                                     &lt;complexContent>
 *                                                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                                         &lt;sequence>
 *                                                           &lt;element name="column" maxOccurs="unbounded">
 *                                                             &lt;complexType>
 *                                                               &lt;simpleContent>
 *                                                                 &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *                                                                   &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                                                   &lt;attribute name="type" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                                                   &lt;attribute name="primaryKey" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                                                   &lt;attribute name="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                                                   &lt;attribute name="autoIncrement" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                                                   &lt;attribute name="size" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                                                 &lt;/extension>
 *                                                               &lt;/simpleContent>
 *                                                             &lt;/complexType>
 *                                                           &lt;/element>
 *                                                           &lt;element name="partitionType" minOccurs="0">
 *                                                             &lt;complexType>
 *                                                               &lt;complexContent>
 *                                                                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                                                   &lt;sequence>
 *                                                                     &lt;element name="partition" maxOccurs="unbounded" minOccurs="0">
 *                                                                       &lt;complexType>
 *                                                                         &lt;simpleContent>
 *                                                                           &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *                                                                             &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                                                             &lt;attribute name="values" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                                                             &lt;attribute name="tablespace" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                                                           &lt;/extension>
 *                                                                         &lt;/simpleContent>
 *                                                                       &lt;/complexType>
 *                                                                     &lt;/element>
 *                                                                   &lt;/sequence>
 *                                                                   &lt;attribute name="type" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                                                   &lt;attribute name="columnName" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                                                   &lt;attribute name="partitionCount" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *                                                                   &lt;attribute name="schema" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                                                 &lt;/restriction>
 *                                                               &lt;/complexContent>
 *                                                             &lt;/complexType>
 *                                                           &lt;/element>
 *                                                           &lt;element name="tableSpaces" minOccurs="0">
 *                                                             &lt;complexType>
 *                                                               &lt;complexContent>
 *                                                                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                                                   &lt;sequence>
 *                                                                     &lt;element name="tableSpace" maxOccurs="unbounded" minOccurs="0">
 *                                                                       &lt;complexType>
 *                                                                         &lt;complexContent>
 *                                                                           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                                                             &lt;sequence>
 *                                                                               &lt;element name="file" maxOccurs="unbounded" minOccurs="0">
 *                                                                                 &lt;complexType>
 *                                                                                   &lt;simpleContent>
 *                                                                                     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *                                                                                       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                                                                       &lt;attribute name="directory" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                                                                       &lt;attribute name="size" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                                                                       &lt;attribute name="reuse" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                                                                       &lt;attribute name="autoExtendNextSize" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                                                                       &lt;attribute name="maxSize" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                                                                     &lt;/extension>
 *                                                                                   &lt;/simpleContent>
 *                                                                                 &lt;/complexType>
 *                                                                               &lt;/element>
 *                                                                             &lt;/sequence>
 *                                                                             &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                                                             &lt;attribute name="type" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                                                           &lt;/restriction>
 *                                                                         &lt;/complexContent>
 *                                                                       &lt;/complexType>
 *                                                                     &lt;/element>
 *                                                                   &lt;/sequence>
 *                                                                 &lt;/restriction>
 *                                                               &lt;/complexContent>
 *                                                             &lt;/complexType>
 *                                                           &lt;/element>
 *                                                           &lt;element name="partitionSchema" minOccurs="0">
 *                                                             &lt;complexType>
 *                                                               &lt;complexContent>
 *                                                                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                                                   &lt;sequence>
 *                                                                     &lt;element name="partitionFunction" minOccurs="0">
 *                                                                       &lt;complexType>
 *                                                                         &lt;complexContent>
 *                                                                           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                                                             &lt;sequence>
 *                                                                               &lt;element name="fileGroup" maxOccurs="unbounded" minOccurs="0">
 *                                                                                 &lt;complexType>
 *                                                                                   &lt;simpleContent>
 *                                                                                     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *                                                                                       &lt;attribute name="fileGroupName" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                                                                       &lt;attribute name="fileName" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                                                                       &lt;attribute name="location" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                                                                       &lt;attribute name="size" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                                                                       &lt;attribute name="maxSize" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                                                                       &lt;attribute name="fileGrowth" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                                                                     &lt;/extension>
 *                                                                                   &lt;/simpleContent>
 *                                                                                 &lt;/complexType>
 *                                                                               &lt;/element>
 *                                                                             &lt;/sequence>
 *                                                                             &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                                                             &lt;attribute name="columnType" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                                                             &lt;attribute name="rangeType" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                                                             &lt;attribute name="values" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                                                           &lt;/restriction>
 *                                                                         &lt;/complexContent>
 *                                                                       &lt;/complexType>
 *                                                                     &lt;/element>
 *                                                                   &lt;/sequence>
 *                                                                   &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                                                 &lt;/restriction>
 *                                                               &lt;/complexContent>
 *                                                             &lt;/complexType>
 *                                                           &lt;/element>
 *                                                         &lt;/sequence>
 *                                                         &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                                       &lt;/restriction>
 *                                                     &lt;/complexContent>
 *                                                   &lt;/complexType>
 *                                                 &lt;/element>
 *                                               &lt;/sequence>
 *                                               &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                             &lt;/restriction>
 *                                           &lt;/complexContent>
 *                                         &lt;/complexType>
 *                                       &lt;/element>
 *                                     &lt;/sequence>
 *                                   &lt;/restriction>
 *                                 &lt;/complexContent>
 *                               &lt;/complexType>
 *                             &lt;/element>
 *                             &lt;element name="UIConfig" minOccurs="0">
 *                               &lt;complexType>
 *                                 &lt;complexContent>
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     &lt;sequence>
 *                                       &lt;element name="FormsFlows">
 *                                         &lt;complexType>
 *                                           &lt;complexContent>
 *                                             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                               &lt;sequence>
 *                                                 &lt;element name="FormFlow" maxOccurs="unbounded" minOccurs="0">
 *                                                   &lt;complexType>
 *                                                     &lt;simpleContent>
 *                                                       &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *                                                         &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                                         &lt;attribute name="path" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                                                       &lt;/extension>
 *                                                     &lt;/simpleContent>
 *                                                   &lt;/complexType>
 *                                                 &lt;/element>
 *                                               &lt;/sequence>
 *                                             &lt;/restriction>
 *                                           &lt;/complexContent>
 *                                         &lt;/complexType>
 *                                       &lt;/element>
 *                                     &lt;/sequence>
 *                                   &lt;/restriction>
 *                                 &lt;/complexContent>
 *                               &lt;/complexType>
 *                             &lt;/element>
 *                           &lt;/all>
 *                           &lt;attribute name="vendorName" use="required">
 *                             &lt;simpleType>
 *                               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                                 &lt;minLength value="1"/>
 *                               &lt;/restriction>
 *                             &lt;/simpleType>
 *                           &lt;/attribute>
 *                           &lt;attribute name="vendorVersion" use="required">
 *                             &lt;simpleType>
 *                               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                                 &lt;minLength value="1"/>
 *                               &lt;/restriction>
 *                             &lt;/simpleType>
 *                           &lt;/attribute>
 *                           &lt;attribute name="implementationName" use="required">
 *                             &lt;simpleType>
 *                               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                                 &lt;minLength value="1"/>
 *                               &lt;/restriction>
 *                             &lt;/simpleType>
 *                           &lt;/attribute>
 *                           &lt;attribute name="provider">
 *                             &lt;simpleType>
 *                               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                                 &lt;minLength value="1"/>
 *                               &lt;/restriction>
 *                             &lt;/simpleType>
 *                           &lt;/attribute>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/choice>
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
    "name",
    "eventResources",
    "serviceHandlerConfiguration",
    "permaStoreConfigurations",
    "dynaStoreConfigurations",
    "policyConfigurations",
    "scheduledJobConfigurations",
    "features"
})
public class FeatureGroup
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlElement(name = "Name", required = true)
    protected String name;
    @XmlElement(name = "EventResources", namespace = "http://attunedlabs.com/internal/FeatureMetaInfoSupporting")
    protected EventResources eventResources;
    @XmlElement(name = "ServiceHandlerConfiguration", namespace = "http://attunedlabs.com/internal/FeatureMetaInfoSupporting")
    protected ServiceHandlerConfiguration serviceHandlerConfiguration;
    @XmlElement(name = "PermaStoreConfigurations", namespace = "http://attunedlabs.com/internal/FeatureMetaInfoSupporting")
    protected PermaStoreConfigurations permaStoreConfigurations;
    @XmlElement(name = "DynaStoreConfigurations", namespace = "http://attunedlabs.com/internal/FeatureMetaInfoSupporting")
    protected DynaStoreConfigurations dynaStoreConfigurations;
    @XmlElement(name = "PolicyConfigurations", namespace = "http://attunedlabs.com/internal/FeatureMetaInfoSupporting")
    protected PolicyConfigurations policyConfigurations;
    @XmlElement(name = "ScheduledJobConfigurations", namespace = "http://attunedlabs.com/internal/FeatureMetaInfoSupporting")
    protected ScheduledJobConfigurations scheduledJobConfigurations;
    @XmlElement(name = "Features")
    protected Features features;

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the eventResources property.
     * 
     * @return
     *     possible object is
     *     {@link EventResources }
     *     
     */
    public EventResources getEventResources() {
        return eventResources;
    }

    /**
     * Sets the value of the eventResources property.
     * 
     * @param value
     *     allowed object is
     *     {@link EventResources }
     *     
     */
    public void setEventResources(EventResources value) {
        this.eventResources = value;
    }

    /**
     * Gets the value of the serviceHandlerConfiguration property.
     * 
     * @return
     *     possible object is
     *     {@link ServiceHandlerConfiguration }
     *     
     */
    public ServiceHandlerConfiguration getServiceHandlerConfiguration() {
        return serviceHandlerConfiguration;
    }

    /**
     * Sets the value of the serviceHandlerConfiguration property.
     * 
     * @param value
     *     allowed object is
     *     {@link ServiceHandlerConfiguration }
     *     
     */
    public void setServiceHandlerConfiguration(ServiceHandlerConfiguration value) {
        this.serviceHandlerConfiguration = value;
    }

    /**
     * Gets the value of the permaStoreConfigurations property.
     * 
     * @return
     *     possible object is
     *     {@link PermaStoreConfigurations }
     *     
     */
    public PermaStoreConfigurations getPermaStoreConfigurations() {
        return permaStoreConfigurations;
    }

    /**
     * Sets the value of the permaStoreConfigurations property.
     * 
     * @param value
     *     allowed object is
     *     {@link PermaStoreConfigurations }
     *     
     */
    public void setPermaStoreConfigurations(PermaStoreConfigurations value) {
        this.permaStoreConfigurations = value;
    }

    /**
     * Gets the value of the dynaStoreConfigurations property.
     * 
     * @return
     *     possible object is
     *     {@link DynaStoreConfigurations }
     *     
     */
    public DynaStoreConfigurations getDynaStoreConfigurations() {
        return dynaStoreConfigurations;
    }

    /**
     * Sets the value of the dynaStoreConfigurations property.
     * 
     * @param value
     *     allowed object is
     *     {@link DynaStoreConfigurations }
     *     
     */
    public void setDynaStoreConfigurations(DynaStoreConfigurations value) {
        this.dynaStoreConfigurations = value;
    }

    /**
     * Gets the value of the policyConfigurations property.
     * 
     * @return
     *     possible object is
     *     {@link PolicyConfigurations }
     *     
     */
    public PolicyConfigurations getPolicyConfigurations() {
        return policyConfigurations;
    }

    /**
     * Sets the value of the policyConfigurations property.
     * 
     * @param value
     *     allowed object is
     *     {@link PolicyConfigurations }
     *     
     */
    public void setPolicyConfigurations(PolicyConfigurations value) {
        this.policyConfigurations = value;
    }

    /**
     * Gets the value of the scheduledJobConfigurations property.
     * 
     * @return
     *     possible object is
     *     {@link ScheduledJobConfigurations }
     *     
     */
    public ScheduledJobConfigurations getScheduledJobConfigurations() {
        return scheduledJobConfigurations;
    }

    /**
     * Sets the value of the scheduledJobConfigurations property.
     * 
     * @param value
     *     allowed object is
     *     {@link ScheduledJobConfigurations }
     *     
     */
    public void setScheduledJobConfigurations(ScheduledJobConfigurations value) {
        this.scheduledJobConfigurations = value;
    }

    /**
     * Gets the value of the features property.
     * 
     * @return
     *     possible object is
     *     {@link Features }
     *     
     */
    public Features getFeatures() {
        return features;
    }

    /**
     * Sets the value of the features property.
     * 
     * @param value
     *     allowed object is
     *     {@link Features }
     *     
     */
    public void setFeatures(Features value) {
        this.features = value;
    }

}