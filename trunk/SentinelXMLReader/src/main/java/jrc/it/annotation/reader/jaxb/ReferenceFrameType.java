//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.03.05 at 04:36:39 PM CET 
//


package jrc.it.annotation.reader.jaxb;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for referenceFrameType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="referenceFrameType">
 *   &lt;restriction base="{}string">
 *     &lt;enumeration value="Undefined"/>
 *     &lt;enumeration value="Galactic"/>
 *     &lt;enumeration value="BM1950"/>
 *     &lt;enumeration value="BM2000"/>
 *     &lt;enumeration value="HM2000"/>
 *     &lt;enumeration value="GM2000"/>
 *     &lt;enumeration value="Mean Of Date"/>
 *     &lt;enumeration value="True Of Date"/>
 *     &lt;enumeration value="Pseudo True Of Date"/>
 *     &lt;enumeration value="Earth Fixed"/>
 *     &lt;enumeration value="Topocentric"/>
 *     &lt;enumeration value="Satellite Orbital"/>
 *     &lt;enumeration value="Satellite Nominal"/>
 *     &lt;enumeration value="Satellite Attitude"/>
 *     &lt;enumeration value="Instrument Attitude"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "referenceFrameType")
@XmlEnum
public enum ReferenceFrameType {

    @XmlEnumValue("Undefined")
    UNDEFINED("Undefined"),
    @XmlEnumValue("Galactic")
    GALACTIC("Galactic"),
    @XmlEnumValue("BM1950")
    BM_1950("BM1950"),
    @XmlEnumValue("BM2000")
    BM_2000("BM2000"),
    @XmlEnumValue("HM2000")
    HM_2000("HM2000"),
    @XmlEnumValue("GM2000")
    GM_2000("GM2000"),
    @XmlEnumValue("Mean Of Date")
    MEAN_OF_DATE("Mean Of Date"),
    @XmlEnumValue("True Of Date")
    TRUE_OF_DATE("True Of Date"),
    @XmlEnumValue("Pseudo True Of Date")
    PSEUDO_TRUE_OF_DATE("Pseudo True Of Date"),
    @XmlEnumValue("Earth Fixed")
    EARTH_FIXED("Earth Fixed"),
    @XmlEnumValue("Topocentric")
    TOPOCENTRIC("Topocentric"),
    @XmlEnumValue("Satellite Orbital")
    SATELLITE_ORBITAL("Satellite Orbital"),
    @XmlEnumValue("Satellite Nominal")
    SATELLITE_NOMINAL("Satellite Nominal"),
    @XmlEnumValue("Satellite Attitude")
    SATELLITE_ATTITUDE("Satellite Attitude"),
    @XmlEnumValue("Instrument Attitude")
    INSTRUMENT_ATTITUDE("Instrument Attitude");
    private final String value;

    ReferenceFrameType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ReferenceFrameType fromValue(String v) {
        for (ReferenceFrameType c: ReferenceFrameType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}