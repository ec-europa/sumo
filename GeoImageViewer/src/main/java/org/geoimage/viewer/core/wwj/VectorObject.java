/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoimage.viewer.core.wwj;

import org.geoimage.viewer.core.layers.visualization.AttributesGeometry;

import com.vividsolutions.jts.geom.Geometry;

/**
 *
 * @author thoorfr
 */
public class VectorObject {
    public Geometry geom;
    public AttributesGeometry attributes;

    public VectorObject(Geometry geom, AttributesGeometry att) {
        this.geom=geom;
        this.attributes=att;
    }
}
