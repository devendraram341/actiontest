//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.10.28 at 10:47:27 AM IST 
//


package com.attunedlabs.leapentity.config.jaxb;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
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
 *         &lt;element name="Attribute" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="value" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="attributeDatatype" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="sequence" type="{http://www.w3.org/2001/XMLSchema}byte" />
 *                 &lt;attribute name="size" type="{http://www.w3.org/2001/XMLSchema}byte" />
 *                 &lt;attribute name="type">
 *                   &lt;simpleType>
 *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                       &lt;enumeration value="NATIVE"/>
 *                       &lt;enumeration value="MANUAL"/>
 *                       &lt;enumeration value="JSON"/>
 *                       &lt;enumeration value="XML"/>
 *                       &lt;enumeration value="TEXT"/>
 *                     &lt;/restriction>
 *                   &lt;/simpleType>
 *                 &lt;/attribute>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="entityFieldNameRef" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="sequence" type="{http://www.w3.org/2001/XMLSchema}byte" />
 *       &lt;attribute name="hasAutoIncrement" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "attribute"
})
public class EntityColumn
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlElement(name = "Attribute")
    protected List<Attribute> attribute;
    @XmlAttribute(name = "name", required = true)
    protected String name;
    @XmlAttribute(name = "entityFieldNameRef", required = true)
    protected String entityFieldNameRef;
    @XmlAttribute(name = "sequence")
    protected Byte sequence;
    @XmlAttribute(name = "hasAutoIncrement")
    protected Boolean hasAutoIncrement;

    /**
     * Gets the value of the attribute property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the attribute property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAttribute().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Attribute }
     * 
     * 
     */
    public List<Attribute> getAttribute() {
        if (attribute == null) {
            attribute = new ArrayList<Attribute>();
        }
        return this.attribute;
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

    /**
     * Gets the value of the entityFieldNameRef property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEntityFieldNameRef() {
        return entityFieldNameRef;
    }

    /**
     * Sets the value of the entityFieldNameRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEntityFieldNameRef(String value) {
        this.entityFieldNameRef = value;
    }

    /**
     * Gets the value of the sequence property.
     * 
     * @return
     *     possible object is
     *     {@link Byte }
     *     
     */
    public Byte getSequence() {
        return sequence;
    }

    /**
     * Sets the value of the sequence property.
     * 
     * @param value
     *     allowed object is
     *     {@link Byte }
     *     
     */
    public void setSequence(Byte value) {
        this.sequence = value;
    }

    /**
     * Gets the value of the hasAutoIncrement property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isHasAutoIncrement() {
        if (hasAutoIncrement == null) {
            return false;
        } else {
            return hasAutoIncrement;
        }
    }

    /**
     * Sets the value of the hasAutoIncrement property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setHasAutoIncrement(Boolean value) {
        this.hasAutoIncrement = value;
    }

}