/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jrc.sumo.configuration;

import static org.jrc.sumo.util.Constant.PREF_AGGLOMERATION_METHODOLOGY;
import static org.jrc.sumo.util.Constant.PREF_BUFFERING_DISTANCE;
import static org.jrc.sumo.util.Constant.PREF_DISPLAY_BANDS;
import static org.jrc.sumo.util.Constant.PREF_DISPLAY_PIXELS;
import static org.jrc.sumo.util.Constant.PREF_LAND_MASK_MARGIN;
import static org.jrc.sumo.util.Constant.PREF_NEIGHBOUR_DISTANCE;
import static org.jrc.sumo.util.Constant.PREF_NEIGHBOUR_TILESIZE;
import static org.jrc.sumo.util.Constant.PREF_REMOVE_LANDCONNECTEDPIXELS;
import static org.jrc.sumo.util.Constant.PREF_S1_GEOLOCATION;
import static org.jrc.sumo.util.Constant.PREF_TARGETS_COLOR_BAND_0;
import static org.jrc.sumo.util.Constant.PREF_TARGETS_COLOR_BAND_1;
import static org.jrc.sumo.util.Constant.PREF_TARGETS_COLOR_BAND_2;
import static org.jrc.sumo.util.Constant.PREF_TARGETS_COLOR_BAND_3;
import static org.jrc.sumo.util.Constant.PREF_TARGETS_COLOR_BAND_MERGED;
import static org.jrc.sumo.util.Constant.PREF_TARGETS_SIZE_BAND_0;
import static org.jrc.sumo.util.Constant.PREF_TARGETS_SIZE_BAND_1;
import static org.jrc.sumo.util.Constant.PREF_TARGETS_SIZE_BAND_2;
import static org.jrc.sumo.util.Constant.PREF_TARGETS_SIZE_BAND_3;
import static org.jrc.sumo.util.Constant.PREF_TARGETS_SIZE_BAND_MERGED;
import static org.jrc.sumo.util.Constant.PREF_TARGETS_SYMBOL_BAND_0;
import static org.jrc.sumo.util.Constant.PREF_TARGETS_SYMBOL_BAND_1;
import static org.jrc.sumo.util.Constant.PREF_TARGETS_SYMBOL_BAND_2;
import static org.jrc.sumo.util.Constant.PREF_TARGETS_SYMBOL_BAND_3;
import static org.jrc.sumo.util.Constant.PREF_TARGETS_SYMBOL_BAND_MERGED;

import java.awt.Color;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.jrc.sumo.util.Constant;

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
        insertIfNotExistRow(PREF_DISPLAY_PIXELS, "false");
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
        insertIfNotExistRow(PREF_BUFFERING_DISTANCE, "0");			
        insertIfNotExistRow(PREF_AGGLOMERATION_METHODOLOGY, "neighbours");
        insertIfNotExistRow(PREF_NEIGHBOUR_DISTANCE, "2.0");
        insertIfNotExistRow(PREF_NEIGHBOUR_TILESIZE, "200");
        insertIfNotExistRow(PREF_REMOVE_LANDCONNECTEDPIXELS, "true");
        insertIfNotExistRow(PREF_LAND_MASK_MARGIN, "100");
        insertIfNotExistRow(Constant.PREF_MAX_TILE_BUFFER, "512");
        insertIfNotExistRow(Constant.PREF_MAX_NUM_OF_TILES, "7");
        insertIfNotExistRow(Constant.PREF_VERSION, "NRT");
        
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
        
        insertIfNotExistRow(Constant.PREF_NUM_TILES_BB_ANALYSIS, "10");
        
        
        //------------------REMOVED AFTER THE BLACK BAND ANALYSIS-----------------------------
        //check pixel for analysis
        /*insertIfNotExistRow(Constant.PREF_XMARGIN_EXCLUSION_PIXEL_ANALYSIS, "0");
        insertIfNotExistRow(Constant.PREF_YMARGIN_EXCLUSION_PIXEL_ANALYSIS, "0");
        insertIfNotExistRow(Constant.PREF_MIN_PIXEL_VALUE_FOR_ANALYSIS,"5");*/
        
        
        insertIfNotExistRow(PREF_S1_GEOLOCATION, Constant.GEOLOCATION_ORBIT);
        
        
        insertIfNotExistRow(Constant.PREF_HOST, "fishdb-copy.jrc.org");
        insertIfNotExistRow(Constant.PREF_DATABASE, "Positions");
        insertIfNotExistRow(Constant.PREF_TABLE, "vms");
        insertIfNotExistRow(Constant.PREF_USER, "");
        insertIfNotExistRow(Constant.PREF_PASSWORD, "");
        insertIfNotExistRow(Constant.PREF_PORT, "5432");
        
        insertIfNotExistRow(Constant.PREF_CACHE, System.getProperty("user.dir") + "/sumocache/");
        insertIfNotExistRow(Constant.PREF_LASTIMAGE, "");
        
        insertIfNotExistRow(Constant.PREF_NOISE_FLOOR, "0");

    }
    
    
    public Object getConfiguration(String name){
    	return readRow(name);
    }
    
    /**
     * 
     * @param defaultValue
     * @return
     */
    public double getNeighbourDistance(){
        double neighbouringDistance = Double.parseDouble(readRow(PREF_NEIGHBOUR_DISTANCE));
    	return neighbouringDistance;
    }
    
    
    /**
     * 
     * @param defaultValue
     * @return
     */
    public int getTileSize(int defaultValue){
        return Integer.parseInt(readRow(PREF_NEIGHBOUR_TILESIZE));
    }
    
    /**
     * 
     * @param defaultValue
     * @return
     */
    public int getNumTileForBBAnalysis(int defaultValue){
        return Integer.parseInt(readRow(Constant.PREF_NUM_TILES_BB_ANALYSIS));
    }
    
    /**
     * 
     * @param defaultValue
     * @return
     */
    public int getLandMaskMargin(int defaultValue){
        return Integer.parseInt(readRow(PREF_LAND_MASK_MARGIN));
    }
    
    
    public String getS1GeolocationAlgorithm(){
         return readRow(PREF_S1_GEOLOCATION);
    }

    /**
     * 
     * @param band 0,1,2,3,4,M M=merged
     * @return
     */
    public int getPrefSizeBand(String band){
    	Double d=Double.parseDouble(readRow(Constant.PREF_TARGETS_SIZE_BAND+band));
    	return d.intValue();
    	
    }
    /**
     * 
     * @param band 0,1,2,3,4,M M=merged
     * @return
     */

    public String getPrefTargetsColorBand(String band){
    	return readRow(Constant.PREF_TARGETS_COLOR_BAND+band);
    }
    /**
     * 
     * @param band 0,1,2,3,4,M M=merged
     * @return
     */

    public String getPrefTargetsSymbolBand(String band){
    	return readRow(Constant.PREF_TARGETS_SYMBOL_BAND+band);
    }
    
    public String getPrefCache(){
    	return readRow(Constant.PREF_CACHE);
    }
    
    public String getPrefDisplayPixel(){
    	return readRow(PREF_DISPLAY_PIXELS);
    }
    
    public String getPrefRemoveLandConnectedPixels(){
    	return readRow(PREF_REMOVE_LANDCONNECTEDPIXELS);
    }	
    
    public String getPrefDisplayBandAnalysis(){
    	return readRow(PREF_DISPLAY_BANDS);
    }
    
    public String getPrefLastImage(){
    	return readRow(Constant.PREF_LASTIMAGE);
    }
    
    public String getPrefImageFolder(){
    	return readRow(Constant.PREF_IMAGE_FOLDER);
    }
    
    public String getPrefBufferingDistance(){
    	return readRow(Constant.PREF_BUFFERING_DISTANCE);
    }
    
    public String getPrefAgglomerationAlg(){
    	return readRow(PREF_AGGLOMERATION_METHODOLOGY);
    }
    
    public String getPrefDefaultLandMask(){
    	return readRow(Constant.PREF_COASTLINE_DEFAULT_LAND_MASK);
    }
    
    public String getPrefMaxTileBuffer(){
    	return readRow(Constant.PREF_MAX_TILE_BUFFER);
    }
    public String getPrefMaxNumOfTiles(){
    	return readRow(Constant.PREF_MAX_NUM_OF_TILES);
    }
    public String getPrefCoastlinesFolder(){
    	return readRow(Constant.PREF_COASTLINES_FOLDER);
    }
    
    
    public String getPrefAzimuthGeometryColor(){
    	return readRow(Constant.PREF_AZIMUTH_GEOMETRYCOLOR);
    }
    public String getPrefAzimuthLineWidth(){
    	return readRow(Constant.PREF_AZIMUTH_GEOMETRYLINEWIDTH);
    }
    
    public String getPrefVersion(){
    	return readRow(Constant.PREF_VERSION);
    }
    
    /**
     * 
     * @return
     */
    public int getPrefNoiseFloor(){
        int noise= Integer.parseInt(readRow(Constant.PREF_NOISE_FLOOR));
    	return noise;
    }
    
    
}
    
    
    
