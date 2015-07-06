package org.geoimage.viewer.util;

public class Constant {
  public static final int OVERVIEW_SIZE=256;
  public static final double OVERVIEW_SIZE_DOUBLE=256.d;
  public static final double TILE_SIZE_DOUBLE=256.d;
  
  public static final int TILE_SIZE_IMG_LAYER = 256;
 
  
//vds analysis preferences
  public static final String PREF_DISPLAY_PIXELS = "VDS Analaysis - Display detected pixels";
  public static final String PREF_DISPLAY_BANDS = "VDS Analaysis - Display all bands detection results";
  
  public static final String PREF_TARGETS_COLOR_BAND = "VDS Analaysis - Target Color - Band ";
  public static final String PREF_TARGETS_SIZE_BAND  = "VDS Analaysis - Target Size - Band ";
  public static final String PREF_TARGETS_SYMBOL_BAND= "VDS Analaysis - Target Symbol - Band ";
  
  public static final String PREF_TARGETS_COLOR_BAND_0 = "VDS Analaysis - Target Color - Band 0";
  public static final String PREF_TARGETS_COLOR_BAND_1 = "VDS Analaysis - Target Color - Band 1";
  public static final String PREF_TARGETS_COLOR_BAND_2 = "VDS Analaysis - Target Color - Band 2";
  public static final String PREF_TARGETS_COLOR_BAND_3 = "VDS Analaysis - Target Color - Band 3";
  public static final String PREF_TARGETS_COLOR_BAND_MERGED = "VDS Analaysis - Target Color - Band Merged";
  
  public static final String PREF_TARGETS_SIZE_BAND_0  = "VDS Analaysis - Target Size - Band 0";
  public static final String PREF_TARGETS_SIZE_BAND_1  = "VDS Analaysis - Target Size - Band 1";
  public static final String PREF_TARGETS_SIZE_BAND_2  = "VDS Analaysis - Target Size - Band 2";
  public static final String PREF_TARGETS_SIZE_BAND_3  = "VDS Analaysis - Target Size - Band 3";
  public static final String PREF_TARGETS_SIZE_BAND_MERGED = "VDS Analaysis - Target Size - Band Merged";
  
  public static final String PREF_TARGETS_SYMBOL_BAND_0= "VDS Analaysis - Target Symbol - Band 0";
  public static final String PREF_TARGETS_SYMBOL_BAND_1= "VDS Analaysis - Target Symbol - Band 1";
  public static final String PREF_TARGETS_SYMBOL_BAND_2= "VDS Analaysis - Target Symbol - Band 2";
  public static final String PREF_TARGETS_SYMBOL_BAND_3= "VDS Analaysis - Target Symbol - Band 3";
  public static final String PREF_TARGETS_SYMBOL_BAND_MERGED = "VDS Analaysis - Target Symbol - Band Merged";
  
  
  
  
  public static final String PREF_BUFFERING_DISTANCE = "VDS Analaysis - Buffering Distance in pixels";
  public static final String PREF_AGGLOMERATION_METHODOLOGY = "VDS Analaysis - Agglomeration Method";
  public static final String PREF_NEIGHBOUR_DISTANCE = "VDS Analaysis - Neighbours distance";
  public static final String PREF_NEIGHBOUR_TILESIZE = "VDS Analaysis - Neighbours tile size";
  public static final String PREF_REMOVE_LANDCONNECTEDPIXELS = "VDS Analaysis - Remove pixels connected to land in neighbour mode";
  public static final String PREF_LAND_MASK_MARGIN="Land mask margin pixel";
  

  //preferences for import image and vector actions
  public static final String PREF_LASTIMAGE = "Last Image";
  public static final String PREF_LASTVECTOR = "Last Vector";
  public static final String PREF_IMAGE_FOLDER = "Image Default Folder";
  public static final String PREF_USE_GEOLOCATIONCORRECTION = "Image - use geolocation correction file";
  
  
  //cache folder preference 
  public final static String PREF_CACHE = "Cache Folder";
  
  public static final String GOOGLE_CHART_API="Google Chart format";
  public static final String NUM_HISTOGRAM_CLASSES="Number of Histogram Classes";
  
  //VDSAnalysisVersionDialog
  public static final String PREF_VERSION = "PostGIS - VDS Analysis Version";
  
  //Geometric interactivelayer
  public static final String PREF_AZIMUTH_GEOMETRYTAG = "Azimuth Geometry";
  public static final String PREF_AZIMUTH_GEOMETRYCOLOR = "VDS Analysis - Azimuth Geometry Color";
  public static final String PREF_AZIMUTH_GEOMETRYLINEWIDTH = "VDS Analysis - Azimuth Geometry Line Width";

  
  public static final String PREF_COASTLINE_DEFAULT_LAND_MASK = "Default land mask";
  public static final String PREF_COASTLINES_FOLDER = "Coastline Shape folders";
  
  public static final String CONF_FILE="analysis.conf";
  
  
  public static final String PREF_S1_GEOLOCATION = "S1 Geolocation (ORB=Orbit / GRID=Grid Points)";
  public static final String GEOLOCATION_GRID = "GRID";
  public static final String GEOLOCATION_ORBIT = "ORB";
  
  public static final String PREF_MAX_TILE_BUFFER="Maximum Tile Buffer Size";
  public static final String PREF_MAX_NUM_OF_TILES="Max Number Of Tiles";
  
  public static final String PREF_HOST = "PostGIS - Host for Database";
  public static final String PREF_DATABASE = "PostGIS - Database Name";
  public static final String PREF_TABLE = "PostGIS - Database Table";
  public static final String PREF_USER = "PostGIS - User";
  public static final String PREF_PASSWORD = "PostGIS - Password";
  public static final String PREF_PORT = "PostGIS - Port";

		  
  
  
  //------------------REMOVED AFTER THE BLACK BAND ANALYSIS-----------------------------
  //constants to define the margin x,y in which we need to check the value of the pixel 
  /*public static final String PREF_XMARGIN_EXCLUSION_PIXEL_ANALYSIS="Horizontal exclusion margin pixel";
  public static final String PREF_YMARGIN_EXCLUSION_PIXEL_ANALYSIS="Vertical exclusion margin pixel";
  public static final String PREF_MIN_PIXEL_VALUE_FOR_ANALYSIS="Min pixel exclusion value";*/
  
  
  
  
}
