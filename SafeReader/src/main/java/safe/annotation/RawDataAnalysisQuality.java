//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.06.24 at 04:43:09 PM CEST 
//


package safe.annotation;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
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
 *         &lt;element ref="{}iBias"/>
 *         &lt;element ref="{}iBiasSignificanceFlag"/>
 *         &lt;element ref="{}qBias"/>
 *         &lt;element ref="{}qBiasSignificanceFlag"/>
 *         &lt;element ref="{}iqGainImbalance"/>
 *         &lt;element ref="{}iqGainSignificanceFlag"/>
 *         &lt;element ref="{}iqQuadratureDeparture"/>
 *         &lt;element ref="{}iqQuadratureDepartureSignificanceFlag"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "iBias",
    "iBiasSignificanceFlag",
    "qBias",
    "qBiasSignificanceFlag",
    "iqGainImbalance",
    "iqGainSignificanceFlag",
    "iqQuadratureDeparture",
    "iqQuadratureDepartureSignificanceFlag"
})
@XmlRootElement(name = "rawDataAnalysisQuality")
public class RawDataAnalysisQuality {

    protected double iBias;
    protected boolean iBiasSignificanceFlag;
    protected double qBias;
    protected boolean qBiasSignificanceFlag;
    protected double iqGainImbalance;
    protected boolean iqGainSignificanceFlag;
    protected double iqQuadratureDeparture;
    protected boolean iqQuadratureDepartureSignificanceFlag;

    /**
     * Gets the value of the iBias property.
     * 
     */
    public double getIBias() {
        return iBias;
    }

    /**
     * Sets the value of the iBias property.
     * 
     */
    public void setIBias(double value) {
        this.iBias = value;
    }

    /**
     * Gets the value of the iBiasSignificanceFlag property.
     * 
     */
    public boolean isIBiasSignificanceFlag() {
        return iBiasSignificanceFlag;
    }

    /**
     * Sets the value of the iBiasSignificanceFlag property.
     * 
     */
    public void setIBiasSignificanceFlag(boolean value) {
        this.iBiasSignificanceFlag = value;
    }

    /**
     * Gets the value of the qBias property.
     * 
     */
    public double getQBias() {
        return qBias;
    }

    /**
     * Sets the value of the qBias property.
     * 
     */
    public void setQBias(double value) {
        this.qBias = value;
    }

    /**
     * Gets the value of the qBiasSignificanceFlag property.
     * 
     */
    public boolean isQBiasSignificanceFlag() {
        return qBiasSignificanceFlag;
    }

    /**
     * Sets the value of the qBiasSignificanceFlag property.
     * 
     */
    public void setQBiasSignificanceFlag(boolean value) {
        this.qBiasSignificanceFlag = value;
    }

    /**
     * Gets the value of the iqGainImbalance property.
     * 
     */
    public double getIqGainImbalance() {
        return iqGainImbalance;
    }

    /**
     * Sets the value of the iqGainImbalance property.
     * 
     */
    public void setIqGainImbalance(double value) {
        this.iqGainImbalance = value;
    }

    /**
     * Gets the value of the iqGainSignificanceFlag property.
     * 
     */
    public boolean isIqGainSignificanceFlag() {
        return iqGainSignificanceFlag;
    }

    /**
     * Sets the value of the iqGainSignificanceFlag property.
     * 
     */
    public void setIqGainSignificanceFlag(boolean value) {
        this.iqGainSignificanceFlag = value;
    }

    /**
     * Gets the value of the iqQuadratureDeparture property.
     * 
     */
    public double getIqQuadratureDeparture() {
        return iqQuadratureDeparture;
    }

    /**
     * Sets the value of the iqQuadratureDeparture property.
     * 
     */
    public void setIqQuadratureDeparture(double value) {
        this.iqQuadratureDeparture = value;
    }

    /**
     * Gets the value of the iqQuadratureDepartureSignificanceFlag property.
     * 
     */
    public boolean isIqQuadratureDepartureSignificanceFlag() {
        return iqQuadratureDepartureSignificanceFlag;
    }

    /**
     * Sets the value of the iqQuadratureDepartureSignificanceFlag property.
     * 
     */
    public void setIqQuadratureDepartureSignificanceFlag(boolean value) {
        this.iqQuadratureDepartureSignificanceFlag = value;
    }

}
