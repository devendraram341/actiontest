//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.03.01 at 04:08:32 PM IST 
//

package com.attunedlabs.eventframework.jaxb;

import java.io.Serializable;
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
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="SubscribeTo" minOccurs="0"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *               &lt;minLength value="1"/&gt;
 *               &lt;pattern value=".*[^\s].*"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="EventSubscriptionCriteria" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="EventRoutingRules" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="EventRoutingRule" maxOccurs="unbounded"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;sequence&gt;
 *                             &lt;element name="Rule" minOccurs="0"&gt;
 *                               &lt;simpleType&gt;
 *                                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                                   &lt;minLength value="1"/&gt;
 *                                   &lt;pattern value=".*[^\s].*"/&gt;
 *                                 &lt;/restriction&gt;
 *                               &lt;/simpleType&gt;
 *                             &lt;/element&gt;
 *                             &lt;element name="InvokeCamelRoute" minOccurs="0"&gt;
 *                               &lt;complexType&gt;
 *                                 &lt;simpleContent&gt;
 *                                   &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;string"&gt;
 *                                     &lt;attribute name="featureGroup"&gt;
 *                                       &lt;simpleType&gt;
 *                                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                                           &lt;minLength value="1"/&gt;
 *                                           &lt;pattern value=".*[^\s].*"/&gt;
 *                                         &lt;/restriction&gt;
 *                                       &lt;/simpleType&gt;
 *                                     &lt;/attribute&gt;
 *                                     &lt;attribute name="featureName"&gt;
 *                                       &lt;simpleType&gt;
 *                                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                                           &lt;minLength value="1"/&gt;
 *                                           &lt;pattern value=".*[^\s].*"/&gt;
 *                                         &lt;/restriction&gt;
 *                                       &lt;/simpleType&gt;
 *                                     &lt;/attribute&gt;
 *                                     &lt;attribute name="serviceName"&gt;
 *                                       &lt;simpleType&gt;
 *                                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                                           &lt;minLength value="1"/&gt;
 *                                           &lt;pattern value=".*[^\s].*"/&gt;
 *                                         &lt;/restriction&gt;
 *                                       &lt;/simpleType&gt;
 *                                     &lt;/attribute&gt;
 *                                   &lt;/extension&gt;
 *                                 &lt;/simpleContent&gt;
 *                               &lt;/complexType&gt;
 *                             &lt;/element&gt;
 *                             &lt;element name="Pipeline" minOccurs="0"&gt;
 *                               &lt;complexType&gt;
 *                                 &lt;complexContent&gt;
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                                     &lt;sequence&gt;
 *                                       &lt;element name="IntegrationPipeName" maxOccurs="unbounded"&gt;
 *                                         &lt;simpleType&gt;
 *                                           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                                             &lt;minLength value="1"/&gt;
 *                                             &lt;pattern value=".*[^\s].*"/&gt;
 *                                           &lt;/restriction&gt;
 *                                         &lt;/simpleType&gt;
 *                                       &lt;/element&gt;
 *                                     &lt;/sequence&gt;
 *                                   &lt;/restriction&gt;
 *                                 &lt;/complexContent&gt;
 *                               &lt;/complexType&gt;
 *                             &lt;/element&gt;
 *                             &lt;element name="HttpPostRequest" minOccurs="0"&gt;
 *                               &lt;complexType&gt;
 *                                 &lt;complexContent&gt;
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                                     &lt;sequence&gt;
 *                                       &lt;element name="header-params" minOccurs="0"&gt;
 *                                         &lt;complexType&gt;
 *                                           &lt;complexContent&gt;
 *                                             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                                               &lt;sequence&gt;
 *                                                 &lt;element name="header-param" maxOccurs="unbounded" minOccurs="0"&gt;
 *                                                   &lt;complexType&gt;
 *                                                     &lt;complexContent&gt;
 *                                                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                                                         &lt;sequence&gt;
 *                                                           &lt;element name="param-name"&gt;
 *                                                             &lt;simpleType&gt;
 *                                                               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                                                                 &lt;minLength value="1"/&gt;
 *                                                                 &lt;pattern value=".*[^\s].*"/&gt;
 *                                                               &lt;/restriction&gt;
 *                                                             &lt;/simpleType&gt;
 *                                                           &lt;/element&gt;
 *                                                           &lt;element name="param-value"&gt;
 *                                                             &lt;simpleType&gt;
 *                                                               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                                                                 &lt;minLength value="1"/&gt;
 *                                                                 &lt;pattern value=".*[^\s].*"/&gt;
 *                                                               &lt;/restriction&gt;
 *                                                             &lt;/simpleType&gt;
 *                                                           &lt;/element&gt;
 *                                                         &lt;/sequence&gt;
 *                                                       &lt;/restriction&gt;
 *                                                     &lt;/complexContent&gt;
 *                                                   &lt;/complexType&gt;
 *                                                 &lt;/element&gt;
 *                                               &lt;/sequence&gt;
 *                                             &lt;/restriction&gt;
 *                                           &lt;/complexContent&gt;
 *                                         &lt;/complexType&gt;
 *                                       &lt;/element&gt;
 *                                     &lt;/sequence&gt;
 *                                     &lt;attribute name="featureGroup" use="required"&gt;
 *                                       &lt;simpleType&gt;
 *                                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                                           &lt;minLength value="1"/&gt;
 *                                           &lt;pattern value=".*[^\s].*"/&gt;
 *                                         &lt;/restriction&gt;
 *                                       &lt;/simpleType&gt;
 *                                     &lt;/attribute&gt;
 *                                     &lt;attribute name="featureName" use="required"&gt;
 *                                       &lt;simpleType&gt;
 *                                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                                           &lt;minLength value="1"/&gt;
 *                                           &lt;pattern value=".*[^\s].*"/&gt;
 *                                         &lt;/restriction&gt;
 *                                       &lt;/simpleType&gt;
 *                                     &lt;/attribute&gt;
 *                                     &lt;attribute name="serviceName" use="required"&gt;
 *                                       &lt;simpleType&gt;
 *                                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                                           &lt;minLength value="1"/&gt;
 *                                           &lt;pattern value=".*[^\s].*"/&gt;
 *                                         &lt;/restriction&gt;
 *                                       &lt;/simpleType&gt;
 *                                     &lt;/attribute&gt;
 *                                     &lt;attribute name="hostName" use="required"&gt;
 *                                       &lt;simpleType&gt;
 *                                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                                           &lt;minLength value="1"/&gt;
 *                                           &lt;pattern value=".*[^\s].*"/&gt;
 *                                         &lt;/restriction&gt;
 *                                       &lt;/simpleType&gt;
 *                                     &lt;/attribute&gt;
 *                                     &lt;attribute name="port" use="required" type="{http://www.w3.org/2001/XMLSchema}short" /&gt;
 *                                   &lt;/restriction&gt;
 *                                 &lt;/complexContent&gt;
 *                               &lt;/complexType&gt;
 *                             &lt;/element&gt;
 *                           &lt;/sequence&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="FailureHandlingStrategy" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="FailureStrategyName"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;simpleContent&gt;
 *                         &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;string"&gt;
 *                           &lt;attribute name="handlerQualifiedClass"&gt;
 *                             &lt;simpleType&gt;
 *                               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                                 &lt;minLength value="1"/&gt;
 *                                 &lt;pattern value=".*[^\s].*"/&gt;
 *                               &lt;/restriction&gt;
 *                             &lt;/simpleType&gt;
 *                           &lt;/attribute&gt;
 *                         &lt;/extension&gt;
 *                       &lt;/simpleContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="FailureStrategyConfig" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="subscriptionId" use="required"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;minLength value="1"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="isEnabled" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" /&gt;
 *       &lt;attribute name="loadBalance" default="auto"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;enumeration value="auto"/&gt;
 *             &lt;enumeration value="manual"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="parallelProcessing" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "subscribeTo", "eventSubscriptionCriteria", "eventRoutingRules", "channelConfig",
		"failureHandlingStrategy" })
public class SubscribeEvent implements Serializable {

	private final static long serialVersionUID = 1L;
	@XmlElement(name = "SubscribeTo")
	protected String subscribeTo;
	@XmlElement(name = "EventSubscriptionCriteria")
	protected String eventSubscriptionCriteria;
	@XmlElement(name = "EventRoutingRules")
	protected EventRoutingRules eventRoutingRules;
	@XmlElement(name = "channelConfig")
	protected String channelConfig;
	@XmlElement(name = "FailureHandlingStrategy")
	protected FailureHandlingStrategy failureHandlingStrategy;
	@XmlAttribute(name = "subscriptionId", required = true)
	protected String subscriptionId;
	@XmlAttribute(name = "isEnabled")
	protected Boolean isEnabled;
	@XmlAttribute(name = "loadBalance")
	protected String loadBalance;
	@XmlAttribute(name = "parallelProcessing")
	protected Boolean parallelProcessing;

	/**
	 * Gets the value of the subscribeTo property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getSubscribeTo() {
		return subscribeTo;
	}

	/**
	 * Sets the value of the subscribeTo property.
	 * 
	 * @param value allowed object is {@link String }
	 * 
	 */
	public void setSubscribeTo(String value) {
		this.subscribeTo = value;
	}

	/**
	 * Gets the value of the eventSubscriptionCriteria property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getEventSubscriptionCriteria() {
		return eventSubscriptionCriteria;
	}

	/**
	 * Sets the value of the eventSubscriptionCriteria property.
	 * 
	 * @param value allowed object is {@link String }
	 * 
	 */
	public void setEventSubscriptionCriteria(String value) {
		this.eventSubscriptionCriteria = value;
	}

	/**
	 * Gets the value of the eventRoutingRules property.
	 * 
	 * @return possible object is {@link EventRoutingRules }
	 * 
	 */
	public EventRoutingRules getEventRoutingRules() {
		return eventRoutingRules;
	}

	/**
	 * Sets the value of the eventRoutingRules property.
	 * 
	 * @param value allowed object is {@link EventRoutingRules }
	 * 
	 */
	public void setEventRoutingRules(EventRoutingRules value) {
		this.eventRoutingRules = value;
	}

	/**
	 * Gets the value of the failureHandlingStrategy property.
	 * 
	 * @return possible object is {@link FailureHandlingStrategy }
	 * 
	 */
	public FailureHandlingStrategy getFailureHandlingStrategy() {
		return failureHandlingStrategy;
	}

	/**
	 * Sets the value of the failureHandlingStrategy property.
	 * 
	 * @param value allowed object is {@link FailureHandlingStrategy }
	 * 
	 */
	public void setFailureHandlingStrategy(FailureHandlingStrategy value) {
		this.failureHandlingStrategy = value;
	}

	/**
	 * Gets the value of the subscriptionId property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getSubscriptionId() {
		return subscriptionId;
	}

	/**
	 * Sets the value of the subscriptionId property.
	 * 
	 * @param value allowed object is {@link String }
	 * 
	 */
	public void setSubscriptionId(String value) {
		this.subscriptionId = value;
	}

	/**
	 * Gets the value of the isEnabled property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public boolean isIsEnabled() {
		if (isEnabled == null) {
			return true;
		} else {
			return isEnabled;
		}
	}

	/**
	 * Sets the value of the isEnabled property.
	 * 
	 * @param value allowed object is {@link Boolean }
	 * 
	 */
	public void setIsEnabled(Boolean value) {
		this.isEnabled = value;
	}

	/**
	 * Gets the value of the loadBalance property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getLoadBalance() {
		if (loadBalance == null) {
			return "auto";
		} else {
			return loadBalance;
		}
	}

	/**
	 * Sets the value of the loadBalance property.
	 * 
	 * @param value allowed object is {@link String }
	 * 
	 */
	public void setLoadBalance(String value) {
		this.loadBalance = value;
	}

	/**
	 * Gets the value of the parallelProcessing property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public boolean isParallelProcessing() {
		if (parallelProcessing == null) {
			return false;
		} else {
			return parallelProcessing;
		}
	}

	/**
	 * Sets the value of the parallelProcessing property.
	 * 
	 * @param value allowed object is {@link Boolean }
	 * 
	 */
	public void setParallelProcessing(Boolean value) {
		this.parallelProcessing = value;
	}

	public String getChannelConfig() {
		return channelConfig;
	}

	public void setChannelConfig(String channelConfig) {
		this.channelConfig = channelConfig;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SubscribeEvent [subscribeTo=").append(subscribeTo).append(", eventSubscriptionCriteria=")
				.append(eventSubscriptionCriteria).append(", eventRoutingRules=").append(eventRoutingRules)
				.append(", channelConfig=").append(channelConfig).append(", failureHandlingStrategy=")
				.append(failureHandlingStrategy).append(", subscriptionId=").append(subscriptionId)
				.append(", isEnabled=").append(isEnabled).append(", loadBalance=").append(loadBalance)
				.append(", parallelProcessing=").append(parallelProcessing).append("]");
		return builder.toString();
	}

}
