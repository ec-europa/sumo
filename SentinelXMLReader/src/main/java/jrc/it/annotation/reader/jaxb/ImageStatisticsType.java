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


/**
 * Annotation record for image statistics information.
 * 
 * <p>Java class for imageStatisticsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="imageStatisticsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="outputDataMean" type="{}complex"/>
 *         &lt;element name="outputDataStdDev" type="{}complex"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "imageStatisticsType", propOrder = {
    "outputDataMean",
    "outputDataStdDev"
})
public class ImageStatisticsType {

    @XmlElement(required = true)
    protected Complex outputDataMean;
    @XmlElement(required = true)
    protected Complex outputDataStdDev;

    /**
     * Gets the value of the outputDataMean property.
     * 
     * @return
     *     possible object is
     *     {@link Complex }
     *     
     */
    public Complex getOutputDataMean() {
        return outputDataMean;
    }

    /**
     * Sets the value of the outputDataMean property.
     * 
     * @param value
     *     allowed object is
     *     {@link Complex }
     *     
     */
    public void setOutputDataMean(Complex value) {
        this.outputDataMean = value;
    }

    /**
     * Gets the value of the outputDataStdDev property.
     * 
     * @return
     *     possible object is
     *     {@link Complex }
     *     
     */
    public Complex getOutputDataStdDev() {
        return outputDataStdDev;
    }

    /**
     * Sets the value of the outputDataStdDev property.
     * 
     * @param value
     *     allowed object is
     *     {@link Complex }
     *     
     */
    public void setOutputDataStdDev(Complex value) {
        this.outputDataStdDev = value;
    }

}
