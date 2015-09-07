/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.analysis;

import java.awt.image.Raster;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.geoimage.analysis.DetectedPixels.Pixel;
import org.geoimage.def.SarImageReader;
import org.geoimage.utils.IMask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pietro Argentieri
 */
public class VDSAnalysis{
	public interface ProgressListener{
		public void startRowProcesseing(int row);
		public void endRowProcesseing(int row);
	}
	
	
    private SarImageReader gir;
    private String enl = "010";
    private Map<String, Float> thresholdsBandParams=new HashMap<String, Float>();
    private DetectedPixels pixels;
    private IMask[] mask;
    private int tileSize;
    private int verTiles=0;
    private int horTiles=0;
    private final double MIN_TRESH_FOR_ANALYSIS=0.7;
    
    private List<ProgressListener>progressListener=null;
    private Logger logger= LoggerFactory.getLogger(VDSAnalysis.class);

    /**
     *
     * @param gir
     * @param mask
     * @param enlf
     * @param thresholdHH
     * @param thresholdHV
     * @param thresholdVH
     * @param thresholdVV
     * @param progressBar
     */
    public VDSAnalysis(SarImageReader gir, IMask[] mask, float enlf, Map<String, Float> trhresholdMap) {
        this.enl = "" + (int) (enlf * 10);
        if (this.enl.length() == 2) {
            this.enl = "0" + this.enl;
        }
                
        this.thresholdsBandParams=trhresholdMap;
        this.gir = gir;
        this.mask = mask;
        this.tileSize = (int)(ConstantVDSAnalysis.TILESIZE / gir.getPixelsize()[0]);
        if(this.tileSize < ConstantVDSAnalysis.TILESIZEPIXELS) this.tileSize = ConstantVDSAnalysis.TILESIZEPIXELS;
        
        this.verTiles = gir.getHeight() / this.tileSize;
        this.horTiles = gir.getWidth() / this.tileSize;
        
        progressListener=new ArrayList<ProgressListener>();
    }

    public DetectedPixels getPixels() {
        return pixels;
    }

    public void addProgressListener(ProgressListener listener){
    	if(!this.progressListener.contains(listener))
    		this.progressListener.add(listener);
    }
    public void removeProgressListener(ProgressListener listener){
    	this.progressListener.remove(listener);
    }
    public void clearProgressListener(ProgressListener listener){
    	this.progressListener.clear();
    }
    /**
     * notify to all listener new row start analyzing
     * @param row
     */
    public void notifyStartNextRowProcessing(int row){
    	for(ProgressListener pl:progressListener)
    		pl.startRowProcesseing(row);
    }
    public Float[] getThresholdsParams(){
    	return (Float[])thresholdsBandParams.values().toArray(new Float[0]);
    }
     
    public Float getThresholdParam(String polarization){
    	return thresholdsBandParams.get(polarization);
    }
    /**
     * 
     * @param kdist
     * @param thresholdBandParams
     * @return
     * @throws IOException 
     */
    public DetectedPixels analyse(KDistributionEstimation kdist,int band, BlackBorderAnalysis blackBorderAnalysis ) throws IOException {
        DetectedPixels dpixels = new DetectedPixels(gir);
        String bb=((SarImageReader)gir).getBands()[band];
        float thresholdBand=this.thresholdsBandParams.get(bb);
        
        // the real size of tiles
        int sizeX = gir.getWidth() / horTiles;
        int sizeY = gir.getHeight() / verTiles;
        
        
        int xLeftTile=0;
        int xRightTile=0;
        int yTopTile=0;
        int yBottomTile=0;
        
        double[][][] tileStat = new double[verTiles][horTiles][5];

        int dy=0;
        
        for (int rowIndex = 0; rowIndex < verTiles; rowIndex++) {
            notifyStartNextRowProcessing(rowIndex);

        	if(rowIndex==verTiles-1){
            	//the last tiles have more pixels so we need to calculate the real size
            	dy=gir.getHeight()-((verTiles-1)*sizeY)-sizeY;
            }
            
            xLeftTile = 0;				 
            xRightTile = 0;//gir.getWidth(); 
            yTopTile = rowIndex * sizeY;
            yBottomTile = yTopTile + sizeY+dy; //dx is always 0 except on the last tile
            
            int dx=0;
            
            for (int colIndex = 0; colIndex < horTiles; colIndex++) {
            	if(colIndex==horTiles-1){
            		//the last tiles have more pixels so we need to calculate the real size
                	dx=(gir.getWidth()-((horTiles-1)*sizeX))-sizeX;
                }
            	
                xLeftTile = colIndex * sizeX;   //x start tile 
                xRightTile = xLeftTile + sizeX+dx; //dx is always 0 except on the last tile

                Raster rastermask =null;
                boolean containsMinPixelValid=false;
                
                if (mask == null || mask.length == 0 || mask[0] == null || !intersects(xLeftTile,xRightTile,yTopTile,yBottomTile)) {
                	rastermask =null;
                }else{
                	// compute different statistics if the tile intersects the land mask
                    // check if there is sea pixels in the tile area
                    if(includes(xLeftTile,xRightTile,yTopTile,yBottomTile))
                        continue;
                    
                    // create raster mask //dx and dy are for tile on the border that have different dimensions
                    rastermask = (mask[0].rasterize(xLeftTile, yTopTile, sizeX+dx, sizeY+dy, -xLeftTile, -yTopTile, 1.0)).getData();

                    //Read pixels for the area and check there are enough sea pixels
                    int[] maskdata = rastermask.getPixels(0, 0, rastermask.getWidth(), rastermask.getHeight(), (int[])null);
                    //float[] maskdata2 = rastermask.getPixels(0, 0, rastermask.getWidth(), rastermask.getHeight(), (float[])null);
                    int inValidPixelCount = 0;
                    
                    for(int count = 0; count < maskdata.length; count++)
                        inValidPixelCount += maskdata[count];
                    
                    double oldInValidPixelCount=inValidPixelCount;
                    containsMinPixelValid=((double)inValidPixelCount / maskdata.length) <= MIN_TRESH_FOR_ANALYSIS;
                    if(!containsMinPixelValid){
                    	//try to read more pixels (out of the current tile) to have more pixels for the statistics
                    	rastermask = (mask[0].rasterize(xLeftTile-30, yTopTile-30, sizeX+dx+30, sizeY+dy+30, -xLeftTile, -yTopTile, 1.0)).getData();
                        //Read pixels for the area and check there are enough sea pixels
                        maskdata = rastermask.getPixels(0, 0, rastermask.getWidth(), rastermask.getHeight(), (int[])null);
                        
                        containsMinPixelValid=((double)oldInValidPixelCount / maskdata.length) <= MIN_TRESH_FOR_ANALYSIS;
                    }
                }	
                //check if we have the min pixels avalaible for the analysis else we try to "enlarge" the tile
                if(containsMinPixelValid||rastermask==null){
                    // if there are pixels to estimate, calculate statistics using the mask
                    kdist.setImageData(xLeftTile, yTopTile,sizeX+dx, sizeY+dy,rowIndex,colIndex,band);
                    int[] data = gir.readTile(xLeftTile, yTopTile, sizeX+dx, sizeY+dy,band);
                    
                    kdist.estimate(rastermask,data);
                    
                    double[] thresh = kdist.getDetectThresh();
                    tileStat[rowIndex][0] = kdist.getTileStat();
                    
                    double threshWindowsVals[]=AnalysisUtil.calcThreshWindowVals(thresholdBand, thresh);

                    for (int k = 0; k < (sizeY+dy); k++) {
                        for (int h = 0; h < (sizeX+dx); h++) {
                            // check pixel is in the sea
                            if(rastermask==null||(rastermask.getSample(h, k, 0) == 0)){
                                int subwindow = 1;
                                if (h < (sizeX+dx) / 2) {
                                    if (k < (sizeY+dy) / 2) {
                                        subwindow = 1;
                                    } else {
                                        subwindow = 3;
                                    }
                                } else {
                                    if (k < (sizeY +dy)/ 2) {
                                        subwindow = 2;
                                    } else {
                                        subwindow = 4;
                                    }
                                }
                                int pix = data[k * (sizeX+dx) + h];
                                // if (pix > thresh[i][0][subwindow] * (significance - (significance - 1.)	/ thresh[i][0][5])) {

                                // Modified condition from S = ((pix/mean) - 1)/(t_p - 1) where T_window = t_p * mean
                                if (pix > threshWindowsVals[subwindow-1]) {
                                	
                                	double tileAvg=thresh[subwindow] / thresh[5];
                                	
                                	double tileStdDev=thresh[0] * thresh[subwindow] / thresh[5];

                                	dpixels.add(h + xLeftTile,//x
                                    		    k + yTopTile, //y
                                    		    pix,//pixelvalue 
                                    		    tileAvg,
                                    		    tileStdDev,//tile standard deviation normalized 
                                    		    thresh[5], band);
                                }
                            }
                        }
                    }
	            }
	        }
            System.out.println(rowIndex + "/" + verTiles);
        }
        System.out.println("Detected Pixels Total :"+dpixels.getAllDetectedPixels().size());
        return dpixels;
    }

    /**
     * 
     * @param distance
     * @param tilesize
     * @param removelandconnectedpixels
     * @param bands
     * @param mask
     * @param kdist
     * @throws IOException
     */
    public void agglomerateNeighbours(DetectedPixels detPixels,double distance, int tilesize, boolean removelandconnectedpixels, IMask mask, KDistributionEstimation kdist,int... bands)throws IOException {
        aggregate(detPixels,(int) distance, tilesize, removelandconnectedpixels, bands, mask, kdist);
    }
    
    /**
     *  aggregate using the neighbours within tilesize
     * @param neighboursdistance
     * @param tilesize
     * @param removelandconnectedpixels
     * @param bands
     * @param mask
     * @param kdist
     * @throws IOException
     */
    private void aggregate(DetectedPixels detPixels,int neighboursdistance, int tilesize, boolean removelandconnectedpixels, int[] bands, IMask mask, KDistributionEstimation kdist)throws IOException {
        int id = 0;
        // scan through list of detected pixels
        Pixel pixels[]=detPixels.getAllDetectedPixelsValues().toArray(new Pixel[0]);
        int count=0;
        
        //loop on all detected pixel
        for (Pixel detectedPix: pixels) {
        	count++;
        	
            int xx = detectedPix.x;
            int yy = detectedPix.y;
            
            if((count % 100)==0)
            	logger.info(new StringBuilder().append("Aggregating pixel Num:").append(count).append("  x:").append(xx).append("   y:").append(yy).toString() );
            
            // check pixels is not aggregated
            boolean checked = false;
            for (BoatConnectedPixelMap boatpixel : detPixels.listboatneighbours) {
                if (boatpixel.containsPixel(xx, yy)) {
                    checked = true;
                    break;
                }
            }
            if (checked) {
                continue;
            }
            // get image data in tile
            int cornerx = Math.min(Math.max(0, xx - tilesize / 2), gir.getWidth() - tilesize);
            int cornery = Math.min(Math.max(0, yy - tilesize / 2), gir.getHeight() - tilesize);
            
            // boat relative coordinates
            int boatx = xx - cornerx;
            int boaty = yy - cornery;
            int numberbands = bands.length;

            //read the area for the bands
            int[][] data = new int[numberbands][];
            try{
	            for (int bandcounter = 0; bandcounter < numberbands; bandcounter++) {
	            	data[bandcounter] = gir.read(cornerx, cornery, tilesize, tilesize,bands[bandcounter]);
	            }	
            }catch(IOException e){
        		logger.error(e.getMessage());
        		throw e;
        	}
            
            // calculate thresholds
            int row=(cornery+1)/this.verTiles;
        	int col=(cornerx+1)/this.horTiles;
            double[][] statistics = AnalysisUtil.calculateImagemapStatistics(cornerx, cornery, tilesize, tilesize,row,col, bands, data, kdist);
            
            double[][] thresholdvalues = new double[numberbands][2];
            int[] maxValue = new int[numberbands];
            boolean pixelabove = false;
            
            for (int bandcounter = 0; bandcounter < numberbands; bandcounter++) {
                // average the tile mean values
                double mean = (statistics[bandcounter][1] + statistics[bandcounter][2] + statistics[bandcounter][3] + statistics[bandcounter][4]) / 4;
                // aggregate value is mean + 3 * std
                thresholdvalues[bandcounter][0] = mean + 3 * mean * statistics[bandcounter][0];
                // clip value is mean + 5 * std
                thresholdvalues[bandcounter][1] = mean + 5 * mean * statistics[bandcounter][0];
                // check the pixel is still above the new threshold
                int value = data[bandcounter][boatx + boaty * tilesize];
                if (value > thresholdvalues[bandcounter][1]) {
                    pixelabove = true;
                }
                // find maximum value amongst bands
                if (value > maxValue[bandcounter]) {
                    maxValue[bandcounter] = data[bandcounter][boatx + boaty * tilesize];
                }
            }
            
            // add pixel only if above new threshold
            if (pixelabove) {
            	// check if there is land in tile
                Raster rastermask = null;
                if (mask != null) {
                    // check if land in tile
                    if (mask.intersects(cornerx, cornery, tilesize, tilesize)) {
                        // create raster mask
                        rastermask = (mask.rasterize(cornerx, cornery, tilesize, tilesize, -cornerx, -cornery, 1.0)).getData();
                    }
                }
                // add pixel to the list
                BoatConnectedPixelMap boatpixel = new BoatConnectedPixelMap(cornerx, cornery,xx, yy, id++, data[0][boatx + boaty * tilesize]);
                
                for(int iBand=0;iBand<numberbands;iBand++){
                	String bb=((SarImageReader)gir).getBands()[iBand];
                    float thresholdBand=this.thresholdsBandParams.get(bb);
                	
                	kdist.setImageData(cornerx, cornery, tilesize, tilesize, row, col, iBand);
                	int[] newdata = gir.readTile(cornerx, cornery, tilesize, tilesize,iBand);
                	kdist.estimate(rastermask, newdata);
 
                	double[] treshTile=kdist.getDetectThresh();
                	double threshTotal=treshTile[0]+treshTile[1]+treshTile[2]+treshTile[3];
                	
                	double threshWindowsVals[]=AnalysisUtil.calcThreshWindowVals(thresholdBand, treshTile);

                	double tileAvg=threshTotal / treshTile[5];
                	double tileStdDev=treshTile[0] * threshTotal / treshTile[5];
                	
                	int idxBand=idxOn4Band(bb);
                	boatpixel.putMeanValue(idxBand,(statistics[iBand][1] + statistics[iBand][2] + statistics[iBand][3] + statistics[iBand][4]) / 4);
                	boatpixel.putThresholdValue(idxBand,(threshWindowsVals[0]+threshWindowsVals[1]+threshWindowsVals[2]+threshWindowsVals[3])/4);
                	boatpixel.putMaxValue(idxBand, maxValue[iBand]);
                	
                	boatpixel.putStDevValue(idxBand,tileStdDev );
                	double significance=(maxValue[iBand]-tileAvg)/tileStdDev;
                	boatpixel.putSignificanceValue(idxBand, significance);
                	boatpixel.putAvgValue(idxBand, tileAvg);
                }	
                detPixels.listboatneighbours.add(boatpixel);
                
                // start list of aggregated pixels
                List<int[]> boataggregatedpixels = new ArrayList<int[]>();
                int[] imagemap = new int[tilesize * tilesize];
                for (int i = 0; i < tilesize * tilesize; i++) {
                    imagemap[i] = 0;
                }
                
                boolean result = detPixels.checkNeighbours(boataggregatedpixels, imagemap, data, thresholdvalues, new int[]{boatx, boaty}, neighboursdistance, tilesize, rastermask);
                // set flag for touching land
                boatpixel.setTouchesLandMask(result);
              
                //if (!result || !removelandconnectedpixels)
                //	detPixels.listboatneighbours.add(boatpixel);
                
                // shift pixels by cornerx and cornery and store in boat list
                for (int[] pixel : boataggregatedpixels) {
                    boatpixel.addConnectedPixel(pixel[0] + cornerx, pixel[1] + cornery, pixel[2], pixel[3] == 1 ? true : false);
                }
            } 
        }

        // if remove connected to land pixels flag
        if (removelandconnectedpixels) {
            // remove all boats connecting to land
        	List<BoatConnectedPixelMap> toRemove=new ArrayList<BoatConnectedPixelMap>();
            for (int i=0;i<detPixels.listboatneighbours.size();i++) {
            	BoatConnectedPixelMap boat = detPixels.listboatneighbours.get(i);
                if (boat.touchesLand()) {
                    toRemove.add(boat);
                }
            }
            detPixels.listboatneighbours.removeAll(toRemove);
        }
        // generate statistics and values for boats
        detPixels.computeBoatsAttributesAndStatistics(detPixels.listboatneighbours);
    }
    
    private int idxOn4Band(String polar){
    	int idx=0;//HH
    	if(polar.equalsIgnoreCase("HV")){
    		idx=1;
    	}else if(polar.equalsIgnoreCase("VH")){
    		idx=2;
    	}else if(polar.equalsIgnoreCase("VV")){
    		idx=3;
    	}
    	return idx;
    }
    
    public int getVerTiles() {
		return verTiles;
	}

	public void setVerTiles(int verTiles) {
		this.verTiles = verTiles;
	}

	
    
    /**
     * 
     * @param xLeftTile
     * @param xRightTile
     * @param yTopTile
     * @param yBottomTile
     * @return
     */
    public boolean intersects(int xLeftTile,  int xRightTile,int yTopTile, int yBottomTile) {
        if (mask == null) {
            return false;
        }
        for (IMask m : mask) {
            if (m.intersects(xLeftTile, yTopTile, xRightTile - xLeftTile, yBottomTile - yTopTile)) {
                return true;
            }

        }
        return false;
    }
    
    /**
     * 
     * @param xLeftTile
     * @param xRightTile
     * @param yTopTile
     * @param yBottomTile
     * @return
     */
    private boolean includes(int xLeftTile,  int xRightTile,int yTopTile, int yBottomTile) {
         if (mask == null) {
            return false;
        }
        for (IMask m : mask) {
            if (m.includes(xLeftTile, yTopTile, xRightTile - xLeftTile, yBottomTile - yTopTile)) {
                return true;
            }

        }
        return false;
    }

  
    public int getTileSize() {
		return tileSize;
	}

	
    
}
