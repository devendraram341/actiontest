//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.02.19 at 05:01:20 PM IST 
//


package com.attunedlabs.integrationfwk.config.jaxb;

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
 *         &lt;element name="DBConfig">
 *           &lt;complexType>
 *             &lt;simpleContent>
 *               &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *                 &lt;attribute name="operation" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="dbType" use="required">
 *                   &lt;simpleType>
 *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                       &lt;enumeration value="MYSQL"/>
 *                       &lt;enumeration value="CASSANDRA"/>
 *                     &lt;/restriction>
 *                   &lt;/simpleType>
 *                 &lt;/attribute>
 *               &lt;/extension>
 *             &lt;/simpleContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="SQL" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="dbmsMapper" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="fieldMapper" maxOccurs="unbounded" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;simpleContent>
 *                         &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *                           &lt;attribute name="xPath" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                           &lt;attribute name="Field" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                           &lt;attribute name="dataType" default="text">
 *                             &lt;simpleType>
 *                               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                                 &lt;enumeration value="timestamp"/>
 *                                 &lt;enumeration value="text"/>
 *                               &lt;/restriction>
 *                             &lt;/simpleType>
 *                           &lt;/attribute>
 *                           &lt;attribute name="dateFormat" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                         &lt;/extension>
 *                       &lt;/simpleContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="RowMapper" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="xmlFieldMapper" maxOccurs="unbounded" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;simpleContent>
 *                         &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *                           &lt;attribute name="xpath" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                           &lt;attribute name="columnKey" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                         &lt;/extension>
 *                       &lt;/simpleContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "dbConfig",
    "sql",
    "dbmsMapper",
    "rowMapper"
})
public class JDBCIntActivity
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlElement(name = "DBConfig", required = true)
    protected DBConfig dbConfig;
    @XmlElement(name = "SQL", required = true)
    protected String sql;
    protected DbmsMapper dbmsMapper;
    @XmlElement(name = "RowMapper")
    protected RowMapper rowMapper;
    @XmlAttribute(name = "name")
    protected String name;

    /**
     * Gets the value of the dbConfig property.
     * 
     * @return
     *     possible object is
     *     {@link DBConfig }
     *     
     */
    public DBConfig getDBConfig() {
        return dbConfig;
    }

    /**
     * Sets the value of the dbConfig property.
     * 
     * @param value
     *     allowed object is
     *     {@link DBConfig }
     *     
     */
    public void setDBConfig(DBConfig value) {
        this.dbConfig = value;
    }

    /**
     * Gets the value of the sql property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSQL() {
        return sql;
    }

    /**
     * Sets the value of the sql property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSQL(String value) {
        this.sql = value;
    }

    /**
     * Gets the value of the dbmsMapper property.
     * 
     * @return
     *     possible object is
     *     {@link DbmsMapper }
     *     
     */
    public DbmsMapper getDbmsMapper() {
        return dbmsMapper;
    }

    /**
     * Sets the value of the dbmsMapper property.
     * 
     * @param value
     *     allowed object is
     *     {@link DbmsMapper }
     *     
     */
    public void setDbmsMapper(DbmsMapper value) {
        this.dbmsMapper = value;
    }

    /**
     * Gets the value of the rowMapper property.
     * 
     * @return
     *     possible object is
     *     {@link RowMapper }
     *     
     */
    public RowMapper getRowMapper() {
        return rowMapper;
    }

    /**
     * Sets the value of the rowMapper property.
     * 
     * @param value
     *     allowed object is
     *     {@link RowMapper }
     *     
     */
    public void setRowMapper(RowMapper value) {
        this.rowMapper = value;
    }

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

}
