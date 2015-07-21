/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoimage.viewer.core.wwj;

import org.geoimage.viewer.core.api.Attributes;

import com.vividsolutions.jts.geom.Geometry;

/**
 *
 * @author thoorfr
 */
public class VectorObject {
    public Geometry geom;
    public Attributes attributes;

    public VectorObject(Geometry geom, Attributes att) {
        this.geom=geom;
        this.attributes=att;
    }
}
