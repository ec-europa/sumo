//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.08.28 at 09:48:18 AM CEST 
//


package org.geoimage.viewer.core.io.sumoxml;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.geoimage.viewer.core.io.sumoxml package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Sensor_QNAME = new QName("", "sensor");
    private final static QName _Algorithm_QNAME = new QName("", "algorithm");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.geoimage.viewer.core.io.sumoxml
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link VdsTarget }
     * 
     */
    public VdsTarget createVdsTarget() {
        return new VdsTarget();
    }

    /**
     * Create an instance of {@link Boat }
     * 
     */
    public Boat createBoat() {
        return new Boat();
    }

    /**
     * Create an instance of {@link Gcp }
     * 
     */
    public Gcp createGcp() {
        return new Gcp();
    }

    /**
     * Create an instance of {@link Gcps }
     * 
     */
    public Gcps createGcps() {
        return new Gcps();
    }

    /**
     * Create an instance of {@link SatImageMetadata }
     * 
     */
    public SatImageMetadata createSatImageMetadata() {
        return new SatImageMetadata();
    }

    /**
     * Create an instance of {@link VdsAnalysis }
     * 
     */
    public VdsAnalysis createVdsAnalysis() {
        return new VdsAnalysis();
    }

    /**
     * Create an instance of {@link Analysis }
     * 
     */
    public Analysis createAnalysis() {
        return new Analysis();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "sensor")
    public JAXBElement<String> createSensor(String value) {
        return new JAXBElement<String>(_Sensor_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "algorithm")
    public JAXBElement<String> createAlgorithm(String value) {
        return new JAXBElement<String>(_Algorithm_QNAME, String.class, null, value);
    }

}
