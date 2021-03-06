//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.03.05 at 04:36:39 PM CET 
//


package jrc.it.annotation.reader.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * Annotation record for slice information
 * 
 * <p>Java class for sliceType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="sliceType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="sliceNumber" type="{}uint32"/>
 *         &lt;element name="sensingStartTime" type="{}timeType"/>
 *         &lt;element name="sensingStopTime" type="{}timeType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sliceType", propOrder = {
    "sliceNumber",
    "sensingStartTime",
    "sensingStopTime"
})
public class SliceType {

    @XmlElement(required = true)
    protected Uint32 sliceNumber;
    @XmlElement(required = true)
    protected XMLGregorianCalendar sensingStartTime;
    @XmlElement(required = true)
    protected XMLGregorianCalendar sensingStopTime;

    /**
     * Gets the value of the sliceNumber property.
     * 
     * @return
     *     possible object is
     *     {@link Uint32 }
     *     
     */
    public Uint32 getSliceNumber() {
        return sliceNumber;
    }

    /**
     * Sets the value of the sliceNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link Uint32 }
     *     
     */
    public void setSliceNumber(Uint32 value) {
        this.sliceNumber = value;
    }

    /**
     * Gets the value of the sensingStartTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getSensingStartTime() {
        return sensingStartTime;
    }

    /**
     * Sets the value of the sensingStartTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setSensingStartTime(XMLGregorianCalendar value) {
        this.sensingStartTime = value;
    }

    /**
     * Gets the value of the sensingStopTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getSensingStopTime() {
        return sensingStopTime;
    }

    /**
     * Sets the value of the sensingStopTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setSensingStopTime(XMLGregorianCalendar value) {
        this.sensingStopTime = value;
    }

}
