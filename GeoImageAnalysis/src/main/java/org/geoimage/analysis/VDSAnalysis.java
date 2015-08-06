/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.analysis;

import java.awt.image.Raster;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.geoimage.def.SarImageReader;
import org.geoimage.utils.IMask;




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

	/**
     *
     * @param gir
     * @param mask
     * @param enlf
     * @param threshold
     * @param progressBar
     *
    public VDSAnalysis(SarImageReader gir, IMask[] mask, float enlf,  float thresholdHH, float thresholdHV, float thresholdVH, float thresholdVV ){
        this(gir,mask, enlf, thresholdHH, thresholdHV, thresholdVH, thresholdVV,null);
    }*/

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
     * @param significance
     * @return
     * @throws IOException 
     */
    private DetectedPixels analyse(KDistributionEstimation kdist, float significance,int band, BlackBorderAnalysis blackBorderAnalysis ) throws IOException {
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
            xRightTile = gir.getWidth(); 
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
                boolean minPixelAvailable=false;
                
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
                    int pixelcount = 0;
                    double firstMaskLength=maskdata.length;
                    for(int count = 0; count < maskdata.length; count++)
                        pixelcount += maskdata[count];
                    
                    minPixelAvailable=((double)pixelcount / maskdata.length) <= MIN_TRESH_FOR_ANALYSIS;
                    if(!minPixelAvailable){
                    	//try to read more pixels (out of the current tile) to have more pixels for the statistics
                    	rastermask = (mask[0].rasterize(xLeftTile, yTopTile, sizeX+dx+20, sizeY+dy+20, -xLeftTile-20, -yTopTile-20, 1.0)).getData();
                        //Read pixels for the area and check there are enough sea pixels
                        maskdata = rastermask.getPixels(0, 0, rastermask.getWidth(), rastermask.getHeight(), (int[])null);
                        
                        //for(int count = 0; count < maskdata.length; count++)
                          //  pixelcount += maskdata[count];
                        minPixelAvailable=((double)pixelcount / maskdata.length) <= MIN_TRESH_FOR_ANALYSIS;
                    }
                }	
                //check if we have the min pixels avalaible for the analysis else we try to "enlarge" the tile
                if(minPixelAvailable||rastermask==null){
                    // if there are pixels to estimate, calculate statistics using the mask
                    kdist.setImageData(gir, xLeftTile, yTopTile,sizeX+dx, sizeY+dy,rowIndex,colIndex,band,blackBorderAnalysis);
                    int[] data = gir.readTile(xLeftTile, yTopTile, sizeX+dx, sizeY+dy,band);
                    
                    kdist.estimate(rastermask,data);
                    
                    double[][][] thresh = kdist.getDetectThresh();
                    tileStat[rowIndex] = kdist.getTileStat()[0];
                    
                    double threshWindowsVals[]=calcThreshWindowVals(significance, thresh[0][0]);

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
                                    dpixels.add(h + xLeftTile, k + yTopTile, pix, thresh[0][0][subwindow] / thresh[0][0][5], thresh[0][0][0] * thresh[0][0][subwindow] / thresh[0][0][5], thresh[0][0][5], band);
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

    
    
    public int getVerTiles() {
		return verTiles;
	}

	public void setVerTiles(int verTiles) {
		this.verTiles = verTiles;
	}

	/**
     * 
     * @param significance
     * @param thresh
     * @return
     */
    private double[] calcThreshWindowVals(double significance,double[] thresh){
    	double threshWindowsVals[]=new double[4];
    	threshWindowsVals[0]=(significance * (thresh[5] - 1.0) + 1.0) * thresh[1] / thresh[5];
    	threshWindowsVals[1]=(significance * (thresh[5] - 1.0) + 1.0) * thresh[2] / thresh[5];
    	threshWindowsVals[2]=(significance * (thresh[5] - 1.0) + 1.0) * thresh[3] / thresh[5];
    	threshWindowsVals[3]=(significance * (thresh[5] - 1.0) + 1.0) * thresh[4] / thresh[5];
    	
    	return threshWindowsVals;
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
