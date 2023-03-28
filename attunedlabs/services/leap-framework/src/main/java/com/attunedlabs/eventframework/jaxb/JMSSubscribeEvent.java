/**
 * 
 */
package com.attunedlabs.eventframework.jaxb;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author reactiveworks
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "jMSConsumerQueue", "eventSubscriptionCriteria", "eventRoutingRules",
		"failureHandlingStrategy" })
public class JMSSubscribeEvent implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@XmlElement(name = "JMSConsumerQueue")
	protected String jMSConsumerQueue;
	@XmlElement(name = "EventSubscriptionCriteria")
	protected String eventSubscriptionCriteria;
	@XmlElement(name = "EventRoutingRules")
	protected EventRoutingRules eventRoutingRules;
	@XmlElement(name = "FailureHandlingStrategy")
	protected FailureHandlingStrategy failureHandlingStrategy;
	@XmlAttribute(name = "subscriptionId", required = true)
	protected String subscriptionId;
	@XmlAttribute(name = "isEnabled")
	protected Boolean isEnabled;
	@XmlAttribute(name = "concurrentConsumers")
	protected String concurrentConsumers;
	@XmlAttribute(name = "connectionFactoryBeanId")
	protected String connectionFactoryBeanId;
	@XmlAttribute(name = "destinationResolverBeanId")
	protected String destinationResolverBeanId;
	@XmlAttribute(name = "acknowledge")
	protected String acknowledge;

	/**
	 * @return the jMSConsumerQueue
	 */
	public String getjMSConsumerQueue() {
		return jMSConsumerQueue;
	}

	/**
	 * @param jMSConsumerQueue
	 *            the jMSConsumerQueue to set
	 */
	public void setjMSConsumerQueue(String jMSConsumerQueue) {
		this.jMSConsumerQueue = jMSConsumerQueue;
	}

	/**
	 * @return the eventSubscriptionCriteria
	 */
	public String getEventSubscriptionCriteria() {
		return eventSubscriptionCriteria;
	}

	/**
	 * @param eventSubscriptionCriteria
	 *            the eventSubscriptionCriteria to set
	 */
	public void setEventSubscriptionCriteria(String eventSubscriptionCriteria) {
		this.eventSubscriptionCriteria = eventSubscriptionCriteria;
	}

	/**
	 * @return the eventRoutingRules
	 */
	public EventRoutingRules getEventRoutingRules() {
		return eventRoutingRules;
	}

	/**
	 * @param eventRoutingRules
	 *            the eventRoutingRules to set
	 */
	public void setEventRoutingRules(EventRoutingRules eventRoutingRules) {
		this.eventRoutingRules = eventRoutingRules;
	}

	/**
	 * @return the failureHandlingStrategy
	 */
	public FailureHandlingStrategy getFailureHandlingStrategy() {
		return failureHandlingStrategy;
	}

	/**
	 * @param failureHandlingStrategy
	 *            the failureHandlingStrategy to set
	 */
	public void setFailureHandlingStrategy(FailureHandlingStrategy failureHandlingStrategy) {
		this.failureHandlingStrategy = failureHandlingStrategy;
	}

	/**
	 * @return the subscriptionId
	 */
	public String getSubscriptionId() {
		return subscriptionId;
	}

	/**
	 * @param subscriptionId
	 *            the subscriptionId to set
	 */
	public void setSubscriptionId(String subscriptionId) {
		this.subscriptionId = subscriptionId;
	}

	/**
	 * @return the isEnabled
	 */
	public Boolean getIsEnabled() {
		return isEnabled;
	}

	/**
	 * @param isEnabled
	 *            the isEnabled to set
	 */
	public void setIsEnabled(Boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	/**
	 * @return the concurrentConsumer
	 */
	public String getConcurrentConsumer() {
		return concurrentConsumers;
	}

	/**
	 * @param concurrentConsumer
	 *            the concurrentConsumer to set
	 */
	public void setConcurrentConsumer(String concurrentConsumer) {
		this.concurrentConsumers = concurrentConsumer;
	}

	/**
	 * @return the acknowledgement
	 */
	public String getAcknowledge() {
		return acknowledge;
	}

	/**
	 * @param acknowledgement
	 *            the acknowledgement to set
	 */
	public void setAcknowledge(String acknowledge) {
		this.acknowledge = acknowledge;
	}

	/**
	 * @return the serialversionuid
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getConnectionFactoryBeanId() {
		return connectionFactoryBeanId;
	}

	public void setConnectionFactoryBeanId(String connectionFactoryBeanId) {
		this.connectionFactoryBeanId = connectionFactoryBeanId;
	}

	public String getConcurrentConsumers() {
		return concurrentConsumers;
	}

	public void setConcurrentConsumers(String concurrentConsumers) {
		this.concurrentConsumers = concurrentConsumers;
	}

	public String getDestinationResolverBeanId() {
		return destinationResolverBeanId;
	}

	public void setDestinationResolverBeanId(String destinationResolverBeanId) {
		this.destinationResolverBeanId = destinationResolverBeanId;
	}
}
