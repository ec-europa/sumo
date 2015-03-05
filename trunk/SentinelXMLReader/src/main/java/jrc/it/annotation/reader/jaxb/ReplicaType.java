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
 * Annotation record for reconstructed replicas.
 * 
 * <p>Java class for replicaType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="replicaType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="azimuthTime" type="{}timeType"/>
 *         &lt;element name="crossCorrelationBandwidth" type="{}float"/>
 *         &lt;element name="crossCorrelationPslr" type="{}float"/>
 *         &lt;element name="crossCorrelationIslr" type="{}float"/>
 *         &lt;element name="crossCorrelationPeakLocation" type="{}float"/>
 *         &lt;element name="reconstructedReplicaValidFlag" type="{}bool"/>
 *         &lt;element name="pgProductAmplitude" type="{}float"/>
 *         &lt;element name="pgProductPhase" type="{}float"/>
 *         &lt;element name="modelPgProductAmplitude" type="{}float"/>
 *         &lt;element name="modelPgProductPhase" type="{}float"/>
 *         &lt;element name="relativePgProductValidFlag" type="{}bool"/>
 *         &lt;element name="absolutePgProductValidFlag" type="{}bool"/>
 *         &lt;element name="internalTimeDelay" type="{}float"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "replicaType", propOrder = {
    "azimuthTime",
    "crossCorrelationBandwidth",
    "crossCorrelationPslr",
    "crossCorrelationIslr",
    "crossCorrelationPeakLocation",
    "reconstructedReplicaValidFlag",
    "pgProductAmplitude",
    "pgProductPhase",
    "modelPgProductAmplitude",
    "modelPgProductPhase",
    "relativePgProductValidFlag",
    "absolutePgProductValidFlag",
    "internalTimeDelay"
})
public class ReplicaType {

    @XmlElement(required = true)
    protected XMLGregorianCalendar azimuthTime;
    @XmlElement(required = true)
    protected Float crossCorrelationBandwidth;
    @XmlElement(required = true)
    protected Float crossCorrelationPslr;
    @XmlElement(required = true)
    protected Float crossCorrelationIslr;
    @XmlElement(required = true)
    protected Float crossCorrelationPeakLocation;
    protected boolean reconstructedReplicaValidFlag;
    @XmlElement(required = true)
    protected Float pgProductAmplitude;
    @XmlElement(required = true)
    protected Float pgProductPhase;
    @XmlElement(required = true)
    protected Float modelPgProductAmplitude;
    @XmlElement(required = true)
    protected Float modelPgProductPhase;
    protected boolean relativePgProductValidFlag;
    protected boolean absolutePgProductValidFlag;
    @XmlElement(required = true)
    protected Float internalTimeDelay;

    /**
     * Gets the value of the azimuthTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getAzimuthTime() {
        return azimuthTime;
    }

    /**
     * Sets the value of the azimuthTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setAzimuthTime(XMLGregorianCalendar value) {
        this.azimuthTime = value;
    }

    /**
     * Gets the value of the crossCorrelationBandwidth property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getCrossCorrelationBandwidth() {
        return crossCorrelationBandwidth;
    }

    /**
     * Sets the value of the crossCorrelationBandwidth property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setCrossCorrelationBandwidth(Float value) {
        this.crossCorrelationBandwidth = value;
    }

    /**
     * Gets the value of the crossCorrelationPslr property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getCrossCorrelationPslr() {
        return crossCorrelationPslr;
    }

    /**
     * Sets the value of the crossCorrelationPslr property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setCrossCorrelationPslr(Float value) {
        this.crossCorrelationPslr = value;
    }

    /**
     * Gets the value of the crossCorrelationIslr property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getCrossCorrelationIslr() {
        return crossCorrelationIslr;
    }

    /**
     * Sets the value of the crossCorrelationIslr property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setCrossCorrelationIslr(Float value) {
        this.crossCorrelationIslr = value;
    }

    /**
     * Gets the value of the crossCorrelationPeakLocation property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getCrossCorrelationPeakLocation() {
        return crossCorrelationPeakLocation;
    }

    /**
     * Sets the value of the crossCorrelationPeakLocation property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setCrossCorrelationPeakLocation(Float value) {
        this.crossCorrelationPeakLocation = value;
    }

    /**
     * Gets the value of the reconstructedReplicaValidFlag property.
     * 
     */
    public boolean isReconstructedReplicaValidFlag() {
        return reconstructedReplicaValidFlag;
    }

    /**
     * Sets the value of the reconstructedReplicaValidFlag property.
     * 
     */
    public void setReconstructedReplicaValidFlag(boolean value) {
        this.reconstructedReplicaValidFlag = value;
    }

    /**
     * Gets the value of the pgProductAmplitude property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getPgProductAmplitude() {
        return pgProductAmplitude;
    }

    /**
     * Sets the value of the pgProductAmplitude property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setPgProductAmplitude(Float value) {
        this.pgProductAmplitude = value;
    }

    /**
     * Gets the value of the pgProductPhase property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getPgProductPhase() {
        return pgProductPhase;
    }

    /**
     * Sets the value of the pgProductPhase property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setPgProductPhase(Float value) {
        this.pgProductPhase = value;
    }

    /**
     * Gets the value of the modelPgProductAmplitude property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getModelPgProductAmplitude() {
        return modelPgProductAmplitude;
    }

    /**
     * Sets the value of the modelPgProductAmplitude property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setModelPgProductAmplitude(Float value) {
        this.modelPgProductAmplitude = value;
    }

    /**
     * Gets the value of the modelPgProductPhase property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getModelPgProductPhase() {
        return modelPgProductPhase;
    }

    /**
     * Sets the value of the modelPgProductPhase property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setModelPgProductPhase(Float value) {
        this.modelPgProductPhase = value;
    }

    /**
     * Gets the value of the relativePgProductValidFlag property.
     * 
     */
    public boolean isRelativePgProductValidFlag() {
        return relativePgProductValidFlag;
    }

    /**
     * Sets the value of the relativePgProductValidFlag property.
     * 
     */
    public void setRelativePgProductValidFlag(boolean value) {
        this.relativePgProductValidFlag = value;
    }

    /**
     * Gets the value of the absolutePgProductValidFlag property.
     * 
     */
    public boolean isAbsolutePgProductValidFlag() {
        return absolutePgProductValidFlag;
    }

    /**
     * Sets the value of the absolutePgProductValidFlag property.
     * 
     */
    public void setAbsolutePgProductValidFlag(boolean value) {
        this.absolutePgProductValidFlag = value;
    }

    /**
     * Gets the value of the internalTimeDelay property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getInternalTimeDelay() {
        return internalTimeDelay;
    }

    /**
     * Sets the value of the internalTimeDelay property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setInternalTimeDelay(Float value) {
        this.internalTimeDelay = value;
    }

}
