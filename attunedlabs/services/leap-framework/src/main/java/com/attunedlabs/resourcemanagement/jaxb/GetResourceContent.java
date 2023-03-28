package com.attunedlabs.resourcemanagement.jaxb;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType( propOrder = {"requestMethod", "resourceURI", "resourceScope", "type", "subType", "resources"})
@XmlRootElement(name = "GetResourceContent")
public class GetResourceContent implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@XmlAttribute(name = "name")
    protected String name;
    @XmlAttribute(name = "serviceName")
    protected String serviceName;
	@XmlAttribute(name = "isURLSecured")
    protected Boolean isURLSecured;
	@XmlElement(name = "Type")
    protected String type;
	@XmlElement(name = "RequestMethod")
    protected String requestMethod;
	@XmlElement(name = "ResourceURI")
    protected String resourceURI;
	@XmlElement(name = "ResourceScope")
    protected String resourceScope;
	@XmlElement(name = "SubType")
    protected String subType;
	@XmlElement(name = "Resources")
	protected Resources resources;
	public Resources getResources() {
		return resources;
	}
	public void setResources(Resources resources) {
		this.resources = resources;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public Boolean getIsURLSecured() {
		return isURLSecured;
	}
	public void setIsURLSecured(Boolean isURLSecured) {
		this.isURLSecured = isURLSecured;
	}
	public String getRequestMethod() {
		return requestMethod;
	}
	public void setRequestMethod(String requestMethod) {
		this.requestMethod = requestMethod;
	}
	public String getResourceURI() {
		return resourceURI;
	}
	public void setResourceURI(String resourceURI) {
		this.resourceURI = resourceURI;
	}
	public String getResourceScope() {
		return resourceScope;
	}
	public void setResourceScope(String resourceScope) {
		this.resourceScope = resourceScope;
	}
	public String getSubType() {
		return subType;
	}
	public void setSubType(String subType) {
		this.subType = subType;
	}
}
