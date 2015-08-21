/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.analysis;

import java.awt.image.Raster;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    private float thresholdHH = 1.5f;
    private float thresholdHV = 1.2f;
    private float thresholdVH = 1.5f;
    private float thresholdVV = 1.5f;
    private float threshold = 1.5f;    
    private DetectedPixels pixels;
    private IMask[] mask;
    private int tileSize;
    private int verTiles=0;
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
    public VDSAnalysis(SarImageReader gir, IMask[] mask, float enlf, float thresholdHH, float thresholdHV, float thresholdVH, float thresholdVV) {
        this.enl = "" + (int) (enlf * 10);
        if (this.enl.length() == 2) {
            this.enl = "0" + this.enl;
        }
        this.thresholdHH = thresholdHH;
        this.thresholdHV = thresholdHV;
        this.thresholdVV = thresholdVV;
        this.thresholdVH = thresholdVH;
        this.gir = gir;
        this.mask = mask;
        this.tileSize = (int)(ConstantVDSAnalysis.TILESIZE / gir.getGeoTransform().getPixelSize()[0]);
        if(this.tileSize < ConstantVDSAnalysis.TILESIZEPIXELS) this.tileSize = ConstantVDSAnalysis.TILESIZEPIXELS;
        
        this.verTiles = gir.getHeight() / this.tileSize;
        
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
    
    
    /*support of different thresholds for different bands
     * 
     * 
     */
    public DetectedPixels run(KDistributionEstimation kdist, BlackBorderAnalysis blackBorderAnalysis,int band) throws IOException {
        if(gir.getBandName(band).equals("HH")||gir.getBandName(band).equals("H/H")){
            pixels = analyse(kdist, thresholdHH,band,blackBorderAnalysis);
        }else if(gir.getBandName(band).equals("HV")||gir.getBandName(band).equals("H/V")){
            pixels = analyse(kdist, thresholdHV,band,blackBorderAnalysis);
        }else if(gir.getBandName(band).equals("VH")||gir.getBandName(band).equals("V/H")){
            pixels = analyse(kdist, thresholdVH,band,blackBorderAnalysis);
        }else if(gir.getBandName(band).equals("VV")||gir.getBandName(band).equals("V/V")){
            pixels = analyse(kdist, thresholdVV,band,blackBorderAnalysis);
        }else{
            pixels = analyse(kdist, threshold,band,blackBorderAnalysis);
        }
        return pixels;
    }
    
    /**
     * 
     * @param kdist
     * @param thresholdBandParams
     * @return
     * @throws IOException 
     */
    private DetectedPixels analyse(KDistributionEstimation kdist, float thresholdBandParams,int band, BlackBorderAnalysis blackBorderAnalysis ) throws IOException {
        DetectedPixels dpixels = new DetectedPixels(gir);
        
        int horTiles = gir.getWidth() / this.tileSize;

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
                    	rastermask = (mask[0].rasterize(xLeftTile, yTopTile, sizeX+dx+30, sizeY+dy+30, -xLeftTile-30, -yTopTile-30, 1.0)).getData();
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
                    
                    double threshWindowsVals[]=AnalysisUtil.calcThreshWindowVals(thresholdBandParams, thresh);

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
            for (BoatPixel boatpixel : detPixels.listboatneighbours) {
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
            double[][] statistics = AnalysisUtil.calculateImagemapStatistics(cornerx, cornery, tilesize, tilesize, bands, data, kdist);
            
            double[][] thresholdvalues = new double[numberbands][2];
            int maxvalue = 0;
            boolean pixelabove = false;
            
            for (int bandcounter = 0; bandcounter < numberbands; bandcounter++) {
                // average the tile mean values
                double mean = (statistics[bandcounter][1] + statistics[bandcounter][2] + statistics[bandcounter][3] + statistics[bandcounter][4]) / 4;
                

                //TODO CHEK if is true....this is the thresholds for the agglomeration. Change in the output with the trheshold calculated by the anlysis
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
                if (value > maxvalue) {
                    maxvalue = data[bandcounter][boatx + boaty * tilesize];
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
                BoatPixel boatpixel = new BoatPixel(xx, yy, id++, data[0][boatx + boaty * tilesize]);
                for(int i=0;i<numberbands;i++){
                //	Raster rastermask = (mask[0].rasterize(xLeftTile, yTopTile, sizeX+dx, sizeY+dy, -xLeftTile, -yTopTile, 1.0)).getData();
                //	kdist.setImageData(sizeX, sizeY, sizeTileX, sizeTileY, row, col, band);
                //	kdist.estimate(rastermask, data);
//TODO  for each band,calculate here the "trheshold tile " to put in the xml for the new tile 
                //	double threshWindowsVals[]=calcThreshWindowVals(thresholdAnalysisParams, thresh);
                	boatpixel.putMeanValue(i,(statistics[i][1] + statistics[i][2] + statistics[i][3] + statistics[i][4]) / 4);
                	boatpixel.putThresholdValue(i,detectedPix.threshold);
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
                boatpixel.setLandMask(result);
                
                // shift pixels by cornerx and cornery and store in boat list
                //Collection<int[]> aggregatedpixels = boataggregatedpixels.values();
                for (int[] pixel : boataggregatedpixels) {
                    boatpixel.addConnectedPixel(pixel[0] + cornerx, pixel[1] + cornery, pixel[2], pixel[3] == 1 ? true : false);
                }
            } else {
            }
            
        }

        // if remove connected to land pixels flag
        if (removelandconnectedpixels) {
            // remove all boats connecting to land
        	//List<BoatPixel> toRemove=new ArrayList<BoatPixel>();
            for (int i=0;i<detPixels.listboatneighbours.size();i++) {
            	BoatPixel boat = detPixels.listboatneighbours.get(i);
                if (boat.touchesLand()) {
                	detPixels.listboatneighbours.remove(boat);
                    //toRemove.add(boat);
                }
            }
        }
        // generate statistics and values for boats
        detPixels.computeBoatsAttributesAndStatistics(detPixels.listboatneighbours);
        
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
