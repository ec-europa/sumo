/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.core.io;

import java.io.File;

import org.geoimage.def.GeoTransform;
import org.geoimage.viewer.core.GeometryImage;

/**
 *
 * @author thoorfr
 */
public abstract class AbstractVectorIO {
	public static final int SIMPLE_SHAPEFILE = 0;//"SimpleShapeFile";
    public static final int GML = 1;//"GML";
    public static final int POSTGIS = 2;//"Postgis";
    public static final int SUMO_OLD = 3;//"Sumo";
    public static final int CSV = 4;//"Csv";
    public static final int GENERIC_CSV = 5;//"Generic Csv";
    public static final int KML = 6;//"Kml";
    public static final int GEORSS = 7;//"GeoRss";
    
    public static final int SUMO = 8;//"New Sumo XML";
	
    protected String layername;
    
    

	public String getLayername() {
		return layername;
	}

	public void setLayername(String layername) {
		this.layername = layername;
	}

    protected AbstractVectorIO() {
    }

    public void setLayerName(String layername) {
        this.layername = layername;
    }

    public abstract GeometryImage read();//File shpInput,GeoTransform transform);
	public abstract void save(File output, String projection,GeoTransform transform) ;
    
}
