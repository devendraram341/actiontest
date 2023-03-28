package com.attunedlabs.resourcemanagement.jaxb;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType( propOrder = {
    "resourceName"
})
public class Resources implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@XmlElement(name = "ResourceName")
	protected List<String> resourceName;

	public List<String> getResourceName() {
		return resourceName;
	}

	public void setResourceName(List<String> resourceName) {
		this.resourceName = resourceName;
	}
	

}
