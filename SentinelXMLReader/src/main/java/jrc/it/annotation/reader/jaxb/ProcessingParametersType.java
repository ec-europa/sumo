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
 * Annotation record for parameters used during range and azimuth processing.
 * 
 * <p>Java class for processingParametersType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="processingParametersType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="windowType" type="{}weightingWindowType"/>
 *         &lt;element name="windowCoefficient" type="{}double"/>
 *         &lt;element name="totalBandwidth" type="{}double"/>
 *         &lt;element name="processingBandwidth" type="{}double"/>
 *         &lt;element name="lookBandwidth" type="{}double"/>
 *         &lt;element name="numberOfLooks" type="{}uint32"/>
 *         &lt;element name="lookOverlap" type="{}double"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "processingParametersType", propOrder = {
    "windowType",
    "windowCoefficient",
    "totalBandwidth",
    "processingBandwidth",
    "lookBandwidth",
    "numberOfLooks",
    "lookOverlap"
})
public class ProcessingParametersType {

    @XmlElement(required = true)
    protected WeightingWindowType windowType;
    @XmlElement(required = true)
    protected Double windowCoefficient;
    @XmlElement(required = true)
    protected Double totalBandwidth;
    @XmlElement(required = true)
    protected Double processingBandwidth;
    @XmlElement(required = true)
    protected Double lookBandwidth;
    @XmlElement(required = true)
    protected Uint32 numberOfLooks;
    @XmlElement(required = true)
    protected Double lookOverlap;

    /**
     * Gets the value of the windowType property.
     * 
     * @return
     *     possible object is
     *     {@link WeightingWindowType }
     *     
     */
    public WeightingWindowType getWindowType() {
        return windowType;
    }

    /**
     * Sets the value of the windowType property.
     * 
     * @param value
     *     allowed object is
     *     {@link WeightingWindowType }
     *     
     */
    public void setWindowType(WeightingWindowType value) {
        this.windowType = value;
    }

    /**
     * Gets the value of the windowCoefficient property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getWindowCoefficient() {
        return windowCoefficient;
    }

    /**
     * Sets the value of the windowCoefficient property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setWindowCoefficient(Double value) {
        this.windowCoefficient = value;
    }

    /**
     * Gets the value of the totalBandwidth property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getTotalBandwidth() {
        return totalBandwidth;
    }

    /**
     * Sets the value of the totalBandwidth property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setTotalBandwidth(Double value) {
        this.totalBandwidth = value;
    }

    /**
     * Gets the value of the processingBandwidth property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getProcessingBandwidth() {
        return processingBandwidth;
    }

    /**
     * Sets the value of the processingBandwidth property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setProcessingBandwidth(Double value) {
        this.processingBandwidth = value;
    }

    /**
     * Gets the value of the lookBandwidth property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getLookBandwidth() {
        return lookBandwidth;
    }

    /**
     * Sets the value of the lookBandwidth property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setLookBandwidth(Double value) {
        this.lookBandwidth = value;
    }

    /**
     * Gets the value of the numberOfLooks property.
     * 
     * @return
     *     possible object is
     *     {@link Uint32 }
     *     
     */
    public Uint32 getNumberOfLooks() {
        return numberOfLooks;
    }

    /**
     * Sets the value of the numberOfLooks property.
     * 
     * @param value
     *     allowed object is
     *     {@link Uint32 }
     *     
     */
    public void setNumberOfLooks(Uint32 value) {
        this.numberOfLooks = value;
    }

    /**
     * Gets the value of the lookOverlap property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getLookOverlap() {
        return lookOverlap;
    }

    /**
     * Sets the value of the lookOverlap property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setLookOverlap(Double value) {
        this.lookOverlap = value;
    }

}
