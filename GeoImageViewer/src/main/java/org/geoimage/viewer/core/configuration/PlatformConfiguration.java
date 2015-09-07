package org.geoimage.viewer.core.configuration;


import org.geoimage.viewer.core.layers.visualization.vectors.MaskVectorLayer;
import org.geoimage.viewer.util.Constant;

public class PlatformConfiguration {
	private static PlatformConfiguration instance=null;
	private static PreferencesDB prefDB=null;
	
	public static PlatformConfiguration getConfigurationInstance(){
    	if (instance == null) {
            instance = new PlatformConfiguration();
        }
    	
    	return instance;
    }
	
	private PlatformConfiguration(){
		prefDB=new PreferencesDB();
		String cache=prefDB.getPrefCache();
		if (cache.equals("")) {
        	cache = java.util.ResourceBundle.getBundle("GeoImageViewer").getString("cache");
            prefDB.updateRow(Constant.PREF_CACHE, cache);
        }
	}
	
	public boolean updateConfiguration(String option,String val){
		try{
			prefDB.updateRow(option, val);
			return true;
		}catch(Exception e){
			return false;
		}	
	}
	
	
	public String getCachePrefFolder(){
		return prefDB.getPrefCache();
	}
	
	/**
	 * 
	 * @param band
	 * @return
	 */
	public int getTargetsSizeBand(String band){
		try{
			return prefDB.getPrefSizeBand(band);
		}catch(Exception e){
			return 1;
		}	
	}
	/**
	 * 
	 * @param band
	 * @return
	 */
	public String getTargetsColorStringBand(String band){
		try{
			return prefDB.getPrefTargetsColorBand(band);
		}catch(Exception e){
			return "0x0000FF";
		}	
	}
	/**
	 * 
	 * @param band
	 * @return
	 */
	public String getTargetsSymbolBand(String band){
		try{
			return prefDB.getPrefTargetsSymbolBand(band);
		}catch(Exception e){
			return MaskVectorLayer.symbol.square.toString();
		}	
	}
	
	/**
     * 
     * @param defaultValue
     * @return
     */
    public double getNeighbourDistance(double defaultValue){
    	double neighbouringDistance=defaultValue;
    	try {
            neighbouringDistance = prefDB.getNeighbourDistance();
        } catch (NumberFormatException e) {
            neighbouringDistance = defaultValue;
        }
    	return neighbouringDistance;
    }
    
    
    /**
     * 
     * @param defaultValue
     * @return
     */
    public int getTileSize(int defaultValue){
        try {
            return prefDB.getTileSize(defaultValue);
        } catch (NumberFormatException e) {
            return defaultValue;
        }

    }
    /**
     * 
     * @param defaultValue
     * @return
     */
    public int getNumTileBBAnalysis(){
        try {
            return prefDB.getNumTileForBBAnalysis(10);
        } catch (NumberFormatException e) {
            return 10;
        }

    }
    
    
    /**
     * 
     * @param defaultValue
     * @return
     */
    public int getLandMaskMargin(int defaultValue){
        try {
            return prefDB.getLandMaskMargin(defaultValue);
        } catch (NumberFormatException e) {
            return defaultValue;
        }

    }
    
    
    public String getS1GeolocationAlgorithm(){
    	try{
            return prefDB.getS1GeolocationAlgorithm();
    	}catch(Exception e){
    		return Constant.GEOLOCATION_GRID;
    	}    
    }
    
    public boolean getDisplayPixel(){
    	return prefDB.getPrefDisplayPixel().equalsIgnoreCase("true");
    }
    
    public boolean removeLandConnectedPixel(){
    	return prefDB.getPrefRemoveLandConnectedPixels().equalsIgnoreCase("true");
    }
    
    public boolean getDisplayBandAnalysis(){
    	return prefDB.getPrefDisplayBandAnalysis().equalsIgnoreCase("true");
    }
    
    public String getLastImage(){
    	return prefDB.getPrefLastImage();
    }
    
    
    public String getImageFolder(){
    	return prefDB.getPrefImageFolder();
    }
    
    public String getBufferingDistance(){
    	return prefDB.getPrefBufferingDistance();
    }
    
    public String getAgglomerationAlg(){
    	return prefDB.getPrefAgglomerationAlg();
    }
    
    public String getDefaultLandMask(){
    	return prefDB.getPrefDefaultLandMask();
    }
    
    public String getMaxTileBuffer(){
    	return prefDB.getPrefMaxTileBuffer();
    }
    public int getMaxNumOfTiles(){
    	try{
    		return Integer.parseInt(prefDB.getPrefMaxNumOfTiles());
    	}catch(Exception e){
    		return 7; 
    	}
    }
    public String getCoastlinesFolder(){
    	return prefDB.getPrefCoastlinesFolder();
    }
    
    public String getAzimuthGeometryColor(){
   		return prefDB.getPrefAzimuthGeometryColor();
    }
    
    public int getAzimuthLineWidth(){
    	try{
    		return Integer.parseInt(prefDB.getPrefAzimuthLineWidth());
    	}catch(Exception e){
    		return 1; 
    	}
    }
    
    public String getVersion(){
    	return prefDB.getPrefVersion();
    }
    
    
    public String getHost(){
    	return (String)prefDB.getConfiguration(Constant.PREF_HOST);
    }
    
    public String getPort(){
    	return (String)prefDB.getConfiguration(Constant.PREF_PORT);
    }
    
    public String getPassword(){
    	return (String)prefDB.getConfiguration(Constant.PREF_PASSWORD);
    }
    
    public String getUserName(){
    	return (String)prefDB.getConfiguration(Constant.PREF_USER);
    }
    
    public String getTableName(){
    	return (String)prefDB.getConfiguration(Constant.PREF_TABLE);
    }
    public String getDatabase(){
    	return (String)prefDB.getConfiguration(Constant.PREF_DATABASE);
    }
    public String getNumHistgramClasses(){
    	return (String)prefDB.getConfiguration(Constant.NUM_HISTOGRAM_CLASSES);
    }
    public String getGoogleChartApi(){
    	return (String)prefDB.getConfiguration(Constant.GOOGLE_CHART_API);
    }
    public String getLastVector(){
    	return (String)prefDB.getConfiguration(Constant.PREF_LASTVECTOR);
    }
    
    
}
