/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.analysis;

import java.awt.image.Raster;
import java.io.IOException;
import java.net.URL;

import org.geoimage.analysis.BlackBorderAnalysis.TileAnalysis;
import org.geoimage.def.SarImageReader;
import org.slf4j.LoggerFactory;

/**
 * the class applying the algorithm based on k-distribution
 */
public class KDistributionEstimation {
	// the image data for estimation
	protected SarImageReader gir = null;
	/** the processed image */
	protected byte[] imageData = null;
	/** the size of the processed image */
	protected int[] startTile = { 0, 0 };
	/** the size of the tile */
	protected int sizeTileX = 0;
	protected int sizeTileY = 0;
	/** */
	protected int N;
	/** the stats of subtiles */
	protected double[] statData = { 1., 1., 1., 1., 1. };
	/** the flag to know if the image is log-scale encoded */
	protected int logScaling = 0;
	/** parameter to decode log-scale images */
	protected double lk8c1 = 0;
	/** parameter to decode log-scale images */
	protected double lk8c2 = 0;

	// the lookup table to get the thresholds from estimated means and standard
	// deviation
	/** the client to get the lookup table through the webservice */
	protected LookUpTable lookUpTable = null;
	private double standardDeviation = -1.;

	// flag for external initialisation
	private boolean initialisation = false;


	// number of iteration for the detect threshold estimation
	int iteration = 2;

	// clipping thresh to compute the mean and the standard deviation
	double clippingThresh;

	// detect Thresh
	// [tileX][tileY][field]
	// with field = 0 -> mormalized detect Thresh
	// with field = 1 -> detect Thresh for subTile 1
	// with field = 2 -> detect Thresh for subTile 2
	// with field = 3 -> detect Thresh for subTile 3
	// with field = 4 -> detect Thresh for subTile 4
	private static String dbname = "Positions";
	private static String dbuser = "vms-vds-user";
	private static String dbpass = "";
	private static String dbhost = "localhost";
	private static String dbport = "5432";
	private double[][][] detectThresh = null;
	private double[][][] tileStat = null;

	  //------------------REMOVED AFTER THE BLACK BAND ANALYSIS-----------------------------
/*	private int xMarginCheck = 0;
	private int yMarginCheck = 0;
	private int minPixelVal = 0;*/

	private BlackBorderAnalysis borderAnalysis;
	private int rowTile;
	private int colTile;
	private int band=0;
	
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(KDistributionEstimation.class);
	
	class SupportStats{
		public double std;
		public double tempN;
		public double mu;
	}
	

	// CONSTRUCTOR
	/** the cinstructor */
	public KDistributionEstimation(float enlf) {
		String enl = "" + (int) (enlf * 10);
		if (enl.length() == 2) {
			enl = "0" + enl;
		}
		System.out.println("ktables/TabK" + enl + "17.r8");
		URL lut = VDSAnalysis.class.getClassLoader().getResource("ktables/TabK" + enl + "17.r8");
		System.out.println(lut.getPath());
		loadLookUpTable(lut);

	}

	// load the lookup table for thresholds estimation from a file
	/**
	 * the method to load a lookup table from afile
	 *
	 * @param filePath
	 *            the absolute path file of the lookup table
	 */
	public void loadLookUpTable(URL filePath) {
		try {
			lookUpTable = new LookUpTable(filePath.openStream());
		} catch (IOException ex) {
			logger.error(ex.getMessage(),ex);
		}
	}

	/**
	 * method to load the lookup table trough the web-service
	 *
	 * @param enl
	 *            the number of looks (only on digit after coma is used)
	 * @param pfm
	 *            the theorical false alarms rate
	 * @param pfn
	 *            the theorical false alarms rate
	 */
	public void loadLookUpTable(double enl, int pfm, int pfn) {
		lookUpTable = new LookUpTable();
		lookUpTable.initConnection(dbname, dbuser, dbpass, dbhost, dbport);
		lookUpTable.getLUT(enl, pfm, pfn);
		return;
	}

	// get the lookup Table
	/**
	 * return the lookup table client
	 *
	 * @return the lookuptable class
	 */
	public LookUpTable getLookUpTable() {
		return lookUpTable;
	}

	// set the imageData
	/**
	 * set the image to analyse
	 *
	 * @param gir
	 *            the image to analyse
	 * @param sizeX
	 *            the x size of the image
	 * @param sizeY
	 *            the y size of the image
	 * @param tileX
	 *            the x tile
	 * @param tileY
	 *            the y tile
	 * @param row
	 * 			  vertical index for the tile
	 * @param col
	 * 			  horiz index for the tile
	 * @param useBlackBorderAnalysis
	 * 			  true = use the black border analysis to analize the tiles
	 * 
	 *  
	 */
	public void setImageData(SarImageReader gir, int sizeX, int sizeY,
			int sizeTileX, int sizeTileY,
			int row,int col,int band,BlackBorderAnalysis blackBorderAnalysis) {
		this.gir = gir;
		startTile[0] = sizeX;
		startTile[1] = sizeY;
		this.sizeTileX = sizeTileX;
		this.sizeTileY = sizeTileY;
		N = sizeTileX * sizeTileY / 2;
		this.borderAnalysis=blackBorderAnalysis;
		this.rowTile=row;
		this.colTile=col;
		this.band=band;
	}

	/**
	 * set the number of iteratios for the stat calculations
	 *
	 * @param iter
	 *            number of iterations
	 */
	public void setIteration(int iter) {
		iteration = iter;
	}

	// get the imageData
	/**
	 * return the image processed data
	 *
	 * @return the image data
	 */
	public byte[] getImageData() {
		return imageData;
	}

	// initialise the parameters if you know a estimation
	/**
	 * initialise the stat
	 *
	 * @param mean
	 *            th mean of the tile
	 * @param standardDeviation
	 *            the standard deviation of the tile
	 */
	public void initialise(double mean, double std) {
		standardDeviation = std;
		clippingThresh = 0.0;
		initialisation = true;
	}

	//method to estimate the mean and the standard deviation with iteration
	/**
	 * nTileX number of x tiles to analyze
	 * nTileY number of y tiles to analyze
	 * @param mask
	 */
	public void estimate(Raster mask,int nTileX,int nTileY) {

		detectThresh = new double[nTileX][nTileY][6];
		tileStat = new double[nTileY][nTileX][5];
		if (!initialisation) {
			initialise(0.0, 0.0);
		}

		for (int j = 0; j < nTileY; j++) {
			for (int i = 0; i < nTileX; i++) {

				statData = new double[] { 1, 1, 1, 1, 1 };

				int iniX = startTile[0] + i * sizeTileX;
				int iniY = startTile[1] + j * sizeTileY;
				int[] data = gir.readTile(iniX, iniY, sizeTileX, sizeTileY,band);
				double[] result = computeStat(256 * 256, i, j, mask, data);

				for (int k = 0; k < 5; k++) {
					tileStat[j][i][k] = result[k];
				}
				clippingThresh = lookUpTable.getClippingThreshFromStd(result[0]);
				// System.out.print("->>"+clippingThresh);

				for (int iter = 0; iter < iteration; iter++) {
					result = computeStat(clippingThresh, i, j, mask, data);
					if (iter != iteration - 1) {
						clippingThresh = lookUpTable.getClippingThreshFromClippedStd(result[0]);
					} /*
					 * if(new String().valueOf(clippingThresh).equals("NaN")){
					 * clippingThresh=256.*256.; System.out.println("pouet"); }
					 */else {
						double threshTemp = lookUpTable.getDetectThreshFromClippedStd(result[0]);
						for (int k = 1; k < 5; k++) {
							detectThresh[i][j][k] = threshTemp * result[k];
						}
						detectThresh[i][j][0] = result[0];
						detectThresh[i][j][5] = threshTemp;
					}
				}
			}
		}
	}

	/**
	 * @param mask
	 */
	public void estimate(Raster mask,int data[]) {

		detectThresh = new double[1][1][6];
		tileStat = new double[1][1][5];
		if (!initialisation) {
			initialise(0.0, 0.0);
		}
		statData = new double[] { 1, 1, 1, 1, 1 };

		double[] result = computeStat(256 * 256, 1, 1, mask, data);

		for (int k = 0; k < 5; k++) {
			tileStat[0][0][k] = result[k];
		}
		clippingThresh = lookUpTable.getClippingThreshFromStd(result[0]);
		// System.out.print("->>"+clippingThresh);

		for (int iter = 0; iter < iteration; iter++) {
			result = computeStat(clippingThresh, 1, 1, mask, data);
			if (iter != iteration - 1) {
				clippingThresh = lookUpTable.getClippingThreshFromClippedStd(result[0]);
			} /*
			 * if(new String().valueOf(clippingThresh).equals("NaN")){
			 * clippingThresh=256.*256.; System.out.println("pouet"); }
			 */else {
				double threshTemp = lookUpTable.getDetectThreshFromClippedStd(result[0]);
				for (int k = 1; k < 5; k++) {
					detectThresh[0][0][k] = threshTemp * result[k];
				}
				detectThresh[0][0][0] = result[0];
				detectThresh[0][0][5] = threshTemp;
			}
		}
	}
	/**
	 * the tile is divided in 4 parts, this function analize each part
	 * 
	 * @param startx  origin x in tile
	 * @param starty  origin y in tile
	 * @param endx    end x pos of the part of the tile that we want analize
	 * @param endy    end y of the part of the tile that we want analize
	 * @param mask
	 * @param data    pixels values
	 * @param thresholdpixels      threshold 
	 * @param clipx
	 * @param blackAn              result of the black border analysis (null if the bb analysis is not used)
	 * @return
	 */
	private SupportStats calcStatValues(int startx,int starty,int endx,int endy,Raster mask,int sizeTileX, int sizeTileY,int[] data,int thresholdpixels,double clipx,TileAnalysis blackAn){
		double val = 0.;
		double std = 0.0;
		double tempN=0.0;
		double mux=0.0;
		
		boolean exit=false;
		
		if(blackAn!=null&&blackAn.verTopCutOffArray!=null){
			if(starty==0){ //we are in the first or second part of the tile
				int firstCutOffY=blackAn.verTopCutOffArray[0];
			
				if(firstCutOffY>endy){//verify if the first cutoff is > of the endy
					int count=0;
					for(int v:blackAn.verTopCutOffArray){
						if(v==firstCutOffY)
							count++;
					}
					if (count==blackAn.verTopCutOffArray.length){//tutte le soglie sono uguali
						if(endy<=firstCutOffY)
							exit=true; //tutte le soglie sono maggiori di endy
					}
				}
			}
		}	
		if(blackAn!=null&&blackAn.verBottomOffArray!=null){
			if(starty>0){
				int firstCutOffY=blackAn.verBottomOffArray[0];
				
				if(firstCutOffY<=starty){//verify if the first cutoff is <= of the starty
					int count=0;
					for(int v:blackAn.verBottomOffArray){
						if(v==firstCutOffY)
							count++;
					}
					if (count==blackAn.verBottomOffArray.length){//tutte le soglie sono uguali
						if(starty>=firstCutOffY)
							exit=true; //tutte le soglie sono maggiori di endy e quindi non serve analizzare questa parte
					}
				}
			}
		}
		if(!exit){
			for (int y = starty; y <endy; y += 2) {
				if(blackAn!=null&&blackAn.verTopCutOffArray!=null){
						if(y==blackAn.verTopCutOffArray.length||y<=meanThresh(blackAn.verTopCutOffArray))continue;//use the mean
				}	
				if(blackAn!=null&&blackAn.verBottomOffArray!=null){
						if(y==blackAn.verBottomOffArray.length||y>=meanThresh(blackAn.verBottomOffArray))continue;//use the mean
				}
				
				int newStart=startx;
				int newEnd=endx;
				
				if(blackAn!=null){
					if(blackAn.horizLeftCutOffArray!=null&&startx<blackAn.horizLeftCutOffArray[y])
						newStart=startx+blackAn.horizLeftCutOffArray[y];
					if(blackAn.horizRightCutOffArray!=null&&endx>blackAn.horizRightCutOffArray[y])
						newEnd=blackAn.horizRightCutOffArray[y];
				}	
				for (int x = newStart; x < newEnd ; x += 2) {
					
					if ((mask == null) || (mask.getSample(x, y, 0) == 0)) {
						val = data[y * sizeTileX + x];
	
						if (val > 0 && val < clipx) {
							mux += val;
							std += val * val;
							tempN++;
						}
					}
				}
			}
		}	
		SupportStats result=new SupportStats();
		result.mu=mux;
		result.tempN=tempN;
		result.std=std;
		return result;
	}
	/**
	 * 
	 * @param thres
	 * @return
	 */
	private int meanThresh(int[]thres){
		int tot=0;
		for(int t:thres)
			tot=tot+t;
		return tot/thres.length;
	}
	
	/**
	 * compute the stats of thesubtiles
	 *
	 * @param clip
	 *            the ciping thresh
	 * @param i
	 *            the line of the tile
	 * @param j
	 *            the column of the tile
	 * @return the stats of each subtiles
	 */
	protected double[] computeStat(double clip, int iniX, int iniY,	Raster mask, int[] data) {
		
		double clip1 = statData[1] * clip, clip2 = statData[2] * clip, clip3 = statData[3]* clip, clip4 = statData[4] * clip;
		
		// used to fill in the zero values for the means
		
		int thresholdpixels = Math.min(sizeTileX * sizeTileY / 4 / 4, 500);
		standardDeviation = 0.0;

		
		boolean estimate=true;
		//check the black border analysis for the first 5 tile on rows
		TileAnalysis black=null;
		if(this.borderAnalysis!=null)black=this.borderAnalysis.getAnalysisTile(rowTile,colTile);
		
		if(black!=null){
			if(black.bIsBorder)
				estimate=false;//the tile is completely on the black border
		}
	  
		try{
			if(estimate){
				double mean = 0.0;
				int meancounter = 0;
				double tempTileN = 0.;
				SupportStats[] result=new SupportStats[4];
				
				result[0]=calcStatValues(0,0,sizeTileX/2,sizeTileY/2,mask,sizeTileX,sizeTileY,data,thresholdpixels,clip1,black);
				// make sure we have enough points
				if (result[0].tempN > thresholdpixels) {
					result[0].mu /= result[0].tempN;
					standardDeviation += result[0].std / (result[0].mu * result[0].mu);
					tempTileN += result[0].tempN;
					mean += result[0].mu;
					meancounter++;
				}
				
				result[1]=calcStatValues(sizeTileX/2,0,sizeTileX,sizeTileY/2,mask,sizeTileX,sizeTileY,data,thresholdpixels,clip2,black);
				// make sure we have enough points
				if (result[1].tempN > thresholdpixels) {
					result[1].mu /= result[1].tempN;
					standardDeviation += result[1].std / (result[1].mu * result[1].mu);
					tempTileN += result[1].tempN;
					mean += result[1].mu;
					meancounter++;
				}
				result[2]=calcStatValues(0,sizeTileY/2,sizeTileX/2,sizeTileY,mask,sizeTileX,sizeTileY,data,thresholdpixels,clip3,black);
				// make sure we have enough points
				if (result[2].tempN > thresholdpixels) {
					result[2].mu /= result[2].tempN;
					standardDeviation += result[2].std / (result[2].mu * result[2].mu);
					tempTileN += result[2].tempN;
					mean += result[2].mu;
					meancounter++;
				}
				result[3]=calcStatValues(sizeTileX/2,sizeTileY/2,sizeTileX,sizeTileY,mask,sizeTileX,sizeTileY,data,thresholdpixels,clip4,black);
				// make sure we have enough points
				if (result[3].tempN > thresholdpixels) {
					result[3].mu /= result[3].tempN;
					standardDeviation += result[3].std / (result[3].mu * result[3].mu);
					tempTileN += result[3].tempN;
					mean += result[3].mu;
					meancounter++;
				}
				
				// at least one mean was set to zero
				if ((meancounter != 4) && (meancounter > 0)) {
					mean = mean / meancounter;
					if (result[0].tempN < thresholdpixels)
						result[0].mu = mean;
					if (result[1].tempN < thresholdpixels)
						result[1].mu = mean;
					if (result[2].tempN < thresholdpixels)
						result[2].mu = mean;
					if (result[3].tempN < thresholdpixels)
						result[3].mu = mean;
				}
		
				if (meancounter != 0) {
					standardDeviation = Math.sqrt((standardDeviation - tempTileN)
							/ (tempTileN - 1.));
					statData[0] = standardDeviation;
					statData[1] = result[0].mu;
					statData[2] = result[1].mu;
					statData[3] = result[2].mu;
					statData[4] = result[3].mu;
				} else {
					statData[0] = 0.001;
					statData[1] = 100000;
					statData[2] = 100000;
					statData[3] = 100000;
					statData[4] = 100000;
				}
			}else{
					statData[0] = 0.001;
					statData[1] = 100000;
					statData[2] = 100000;
					statData[3] = 100000;
					statData[4] = 100000;
			}	
		}catch(Exception e){
			logger.error("Error computing statistics for iniX:"+iniX+"   iniY:"+iniY,e);
			statData[0] = 0.001;
			statData[1] = 100000;
			statData[2] = 100000;
			statData[3] = 100000;
			statData[4] = 100000;
		}	
		return statData;
	}
	
	
	// return [normalized standardDeviation, mean1, mean2, mean3, mean4] of each
	// sub-window
	/**
	 * return the stat of each subtiles
	 *
	 * @return the stats
	 */
	public double[][][] getTileStat() {
		return tileStat;
	}

	/**
	 * return the normalized thresh of each subtiles
	 *
	 * @return the threshs
	 */
	public double[][][] getDetectThresh() {
		return detectThresh;
	}

	public static void setDatabaseSettings(String k_host, String k_port,
			String k_dbname, String k_user, String k_password) {
		dbhost = k_host;
		dbport = k_port;
		dbname = k_dbname;
		dbuser = k_user;
		dbpass = k_password;

	}
}
