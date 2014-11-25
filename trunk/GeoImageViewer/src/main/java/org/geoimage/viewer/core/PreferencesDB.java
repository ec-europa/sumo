/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.core;

import static org.geoimage.viewer.util.Constant.PREF_AGGLOMERATION_METHODOLOGY;
import static org.geoimage.viewer.util.Constant.PREF_BUFFERING_DISTANCE;
import static org.geoimage.viewer.util.Constant.PREF_DISPLAY_BANDS;
import static org.geoimage.viewer.util.Constant.PREF_DISPLAY_PIXELS;
import static org.geoimage.viewer.util.Constant.PREF_NEIGHBOUR_DISTANCE;
import static org.geoimage.viewer.util.Constant.PREF_NEIGHBOUR_TILESIZE;
import static org.geoimage.viewer.util.Constant.PREF_REMOVE_LANDCONNECTEDPIXELS;
import static org.geoimage.viewer.util.Constant.PREF_TARGETS_COLOR_BAND_0;
import static org.geoimage.viewer.util.Constant.PREF_TARGETS_COLOR_BAND_1;
import static org.geoimage.viewer.util.Constant.PREF_TARGETS_COLOR_BAND_2;
import static org.geoimage.viewer.util.Constant.PREF_TARGETS_COLOR_BAND_3;
import static org.geoimage.viewer.util.Constant.PREF_TARGETS_COLOR_BAND_MERGED;
import static org.geoimage.viewer.util.Constant.PREF_TARGETS_SIZE_BAND_0;
import static org.geoimage.viewer.util.Constant.PREF_TARGETS_SIZE_BAND_1;
import static org.geoimage.viewer.util.Constant.PREF_TARGETS_SIZE_BAND_2;
import static org.geoimage.viewer.util.Constant.PREF_TARGETS_SIZE_BAND_3;
import static org.geoimage.viewer.util.Constant.PREF_TARGETS_SIZE_BAND_MERGED;
import static org.geoimage.viewer.util.Constant.PREF_TARGETS_SYMBOL_BAND_0;
import static org.geoimage.viewer.util.Constant.PREF_TARGETS_SYMBOL_BAND_1;
import static org.geoimage.viewer.util.Constant.PREF_TARGETS_SYMBOL_BAND_2;
import static org.geoimage.viewer.util.Constant.PREF_TARGETS_SYMBOL_BAND_3;
import static org.geoimage.viewer.util.Constant.PREF_TARGETS_SYMBOL_BAND_MERGED;

import java.awt.Color;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.geoimage.viewer.util.Constant;

/**
 * A class that deals with environment variables, they are stored on a local h2 database
 * @author gabbaan + leforth + thoorfr
 */
public class PreferencesDB {
    EntityManagerFactory emf;

    public PreferencesDB() {
        emf=Persistence.createEntityManagerFactory("GeoImageViewerPU");
        initializePreferences();
    }

    //for each name it is possible to save only one value
    public boolean updateRow(String name, String value) {
        EntityManager em=emf.createEntityManager();
        em.getTransaction().begin();
        Preferences p=new Preferences(name);
        p.setValue(value);
        try{
            em.persist(em.merge(p));
        }catch(Exception e){
            em.getTransaction().rollback();
            em.close();
            return false;
        }
        em.getTransaction().commit();
        em.close();
        return true;
    }

    public String readRow(String name) {
        EntityManager em=emf.createEntityManager();
        Query q=em.createNamedQuery("Preferences.findByName");
        q.setParameter("name", name);
        List<Preferences> l=q.getResultList();
        if(l.size()==0) return null;
        Preferences p=l.get(0);
        em.close();
        return p.getValue();

    }

    public boolean insertIfNotExistRow(String name, String value) {
        if(readRow(name)!=null){
            return false;
        }
        EntityManager em=emf.createEntityManager();
        em.getTransaction().begin();
        Preferences p=new Preferences(name);
        p.setValue(value);
        try{
            em.persist(p);
        }catch(Exception e){
            em.getTransaction().rollback();
            em.close();
            return false;
        }
        em.getTransaction().commit();
        em.close();
        return true;
    }
    
    private void initializePreferences(){
    	// creates preferences fields
    	
    	// vds analysis preferences
        insertIfNotExistRow(PREF_DISPLAY_PIXELS, "true");
        insertIfNotExistRow(PREF_DISPLAY_BANDS, "true");
        insertIfNotExistRow(PREF_TARGETS_COLOR_BAND_0, "0x0000FF");
        insertIfNotExistRow(PREF_TARGETS_SIZE_BAND_0, "1.0");
        insertIfNotExistRow(PREF_TARGETS_SYMBOL_BAND_0, "square");
        insertIfNotExistRow(PREF_TARGETS_SYMBOL_BAND_1, "triangle");
        insertIfNotExistRow(PREF_TARGETS_COLOR_BAND_1, "0x00FF00");
        insertIfNotExistRow(PREF_TARGETS_SIZE_BAND_1, "1.0");
        insertIfNotExistRow(PREF_TARGETS_COLOR_BAND_2, "0xFF0000");
        insertIfNotExistRow(PREF_TARGETS_SIZE_BAND_2, "1.0");
        insertIfNotExistRow(PREF_TARGETS_SYMBOL_BAND_2, "square");
        insertIfNotExistRow(PREF_TARGETS_COLOR_BAND_3, "0xFFFF00");
        insertIfNotExistRow(PREF_TARGETS_SIZE_BAND_3, "1.0");
        insertIfNotExistRow(PREF_TARGETS_SYMBOL_BAND_3, "triangle");
        insertIfNotExistRow(PREF_TARGETS_SYMBOL_BAND_MERGED, "cross");
        insertIfNotExistRow(PREF_TARGETS_COLOR_BAND_MERGED, "0xFFAA00");
        insertIfNotExistRow(PREF_TARGETS_SIZE_BAND_MERGED, "1.0");
        insertIfNotExistRow(PREF_BUFFERING_DISTANCE, "15.0");			
        insertIfNotExistRow(PREF_AGGLOMERATION_METHODOLOGY, "neighbours");
        insertIfNotExistRow(PREF_NEIGHBOUR_DISTANCE, "2.0");
        insertIfNotExistRow(PREF_NEIGHBOUR_TILESIZE, "200");
        insertIfNotExistRow(PREF_REMOVE_LANDCONNECTEDPIXELS, "true");
        
        //default pref folder for import vector option
        insertIfNotExistRow(Constant.PREF_LASTVECTOR, "");
        
        // default pref folders for import image option
        insertIfNotExistRow(Constant.PREF_IMAGE_FOLDER, java.util.ResourceBundle.getBundle("GeoImageViewer").getString("image_directory"));
        insertIfNotExistRow(Constant.PREF_LASTIMAGE, "");
        //default pref for use geo location
        insertIfNotExistRow(Constant.PREF_USE_GEOLOCATIONCORRECTION, "true");
        
        insertIfNotExistRow(Constant.PREF_CACHE, java.util.ResourceBundle.getBundle("GeoImageViewer").getString("cache"));
     
        
        insertIfNotExistRow(Constant.GOOGLE_CHART_API,"http://chart.apis.google.com/chart?chf=c,lg,45,CCCCCC,0,999999,0.75|bg,s,CCCCCC&chm=B,76A4FB,0,0,0&chxt=x&chtt=Histogram&chs=300x200&cht=lc&chco=0077CC");
        insertIfNotExistRow(Constant.NUM_HISTOGRAM_CLASSES,"20");

        insertIfNotExistRow(Constant.PREF_AZIMUTH_GEOMETRYCOLOR, Color.ORANGE.getRGB() + "");
        insertIfNotExistRow(Constant.PREF_AZIMUTH_GEOMETRYLINEWIDTH, "1");
        
        //screenshot
        insertIfNotExistRow("Screenshot file", "~/screenshot.jpg");

        insertIfNotExistRow(Constant.PREF_COASTLINE_DEFAULT_LAND_MASK,"./resources/coastline/default/Global GSHHS Land Mask.shp");
        insertIfNotExistRow(Constant.PREF_COASTLINES_FOLDER, "./resources/coastline");
        
    }
}
