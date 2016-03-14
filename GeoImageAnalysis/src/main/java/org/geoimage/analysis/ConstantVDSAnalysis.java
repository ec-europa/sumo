package org.geoimage.analysis;

public class ConstantVDSAnalysis {
	 //thresh for S1 vds analisys
	  public static int THRESH_D_EXTREMES_RATIO=3;
	  public static int THRESH_D_EXTREMES_DIFF=5;
	  public static int THRESH_IS_BORDER=10;
	  public static double THRESH_MAX_FACTOR=0.2;
	  public static int THRESH_VALUE_SAFE=50;


	  public static int ROW_TILE_SAMPLES_ARRAY[]={10,50,90}; //10% 50% 90% indicate how many and what rows we want to use as samples for each tile



	  // filter size of boats in meters
	  public static final double FILTERminSIZE = 3;
	  public static final double FILTERmaxSIZE = 1000;
	  public static final double filterminSize = FILTERminSIZE;
	  public static final double filtermaxSize = FILTERmaxSIZE;

}
