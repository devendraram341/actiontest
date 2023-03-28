package com.attunedlabs.resourcemanagement.jaxb;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "ConnectionInfo")
public class ConnectionInfo  implements Serializable{
	 private final static long serialVersionUID = 1L; 
	 
		@XmlAttribute(name = "host")
	    protected String host;
	    @XmlAttribute(name = "port")
	    protected String port;
	    @XmlAttribute(name = "baseURI")
	    protected String baseURI;
		@XmlAttribute(name = "isURLSecured")
	    protected Boolean isURLSecured;
		public String getHost() {
			return host;
		}
		public void setHost(String host) {
			this.host = host;
		}
		public String getPort() {
			return port;
		}
		public void setPort(String port) {
			this.port = port;
		}
		public String getBaseURI() {
			return baseURI;
		}
		public void setBaseURI(String baseURI) {
			this.baseURI = baseURI;
		}
		public Boolean getIsURLSecured() {
			return isURLSecured;
		}
		public void setIsURLSecured(Boolean isURLSecured) {
			this.isURLSecured = isURLSecured;
		}

}
