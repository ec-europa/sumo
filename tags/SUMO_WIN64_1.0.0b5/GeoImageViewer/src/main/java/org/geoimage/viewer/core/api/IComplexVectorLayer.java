/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoimage.viewer.core.api;

import com.vividsolutions.jts.geom.Geometry;
import java.awt.Color;
import java.util.List;
import java.util.Vector;


/**
 *
 * @author leforth
 */
public interface IComplexVectorLayer extends IVectorLayer {
    public void addGeometries(String geometrytag, Color color, int lineWidth, String type, List<Geometry> geometries, boolean status);
    public boolean tagExists(String tag);
    public boolean removeGeometriesByTag(String geometrytag);
    public boolean getGeometriesDisplay(String geometrytag);
    public void toggleGeometriesByTag(String geometrytag, boolean status);
    public List<String> getGeometriestagList();
}
