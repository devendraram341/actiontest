package com.attunedlabs.resourcemanagement.jaxb;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType( propOrder = {
    "connectionInfo", "getResourceContent"
})
@XmlRootElement(name = "ResourceManagerConfig")
public class ResourceManagerConfig
implements Serializable
{

private final static long serialVersionUID = 1L;
@XmlElement(name = "ConnectionInfo")
protected ConnectionInfo connectionInfo;
@XmlElement(name = "GetResourceContent")
protected List<GetResourceContent> getResourceContent;
public ConnectionInfo getConnectionInfo() {
	return connectionInfo;
}
public void setConnectionInfo(ConnectionInfo connectionInfo) {
	this.connectionInfo = connectionInfo;
}
public List<GetResourceContent> getGetResourceContent() {
	return getResourceContent;
}
public void setGetResourceContent(List<GetResourceContent> getResourceContent) {
	this.getResourceContent = getResourceContent;
}

}
