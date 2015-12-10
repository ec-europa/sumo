/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jrc.sumo.core.api;

import org.jrc.sumo.util.files.FileTypes;

/**
 *
 * @author thoorfr
 */
public interface ISave {
   //{csv, shp, sumoXML,matlabXML, gml, kmz, postgis, thumbnails};
  
   public static final int OPT_EXPORT_CSV=0;
   public static final int OPT_EXPORT_SHP=1;
   public static final int OPT_EXPORT_XML_SUMO_OLD=2;
   public static final int OPT_EXPORT_XML_SUMO=3;
   public static final int OPT_EXPORT_GML=4;
   public static final int OPT_EXPORT_KMZ=5;
   public static final int OPT_EXPORT_POSTGIS=6;
   public static final int OPT_EXPORT_THUMBS=7;
   
   public static final String STR_EXPORT_CSV="csv";
   public static final String STR_EXPORT_SHP="shp";
   public static final String STR_EXPORT_XML_SUMO_OLD="sumoXML Old";
   public static final String STR_EXPORT_XML_SUMO="sumoXML";
   public static final String STR_EXPORT_GML="gml";
   public static final String STR_EXPORT_KMZ="kmz";
   public static final String STR_EXPORT_POSTGIS="postgis";
   public static final String STR_EXPORT_THUMBS="thumbnails";
	  
    public void save(String file, int type, String projection);
    public FileTypes[] getFileFormatTypes();

}
