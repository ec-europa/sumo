/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.analysis;

import java.awt.Rectangle;
import java.awt.image.Raster;
import java.util.ArrayList;
import java.util.List;

import org.geoimage.def.SarImageReader;
import org.geoimage.utils.IMask;
import org.geoimage.utils.IProgress;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 *
 * @author thoorfr
 */
public class VDSAnalysis {

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
    // batch mode flag
    private boolean isBatch = false;
    // application progress bar
    private IProgress progressBar = null;


    /**
     *
     * @param gir
     * @param mask
     * @param enlf
     * @param threshold
     * @param progressBar
     */
    public VDSAnalysis(SarImageReader gir, IMask[] mask, float enlf,  float thresholdHH, float thresholdHV, float thresholdVH, float thresholdVV , boolean batch){
        this(gir,mask, enlf, thresholdHH, thresholdHV, thresholdVH, thresholdVV, null);
        this.isBatch = batch;
    }

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
    public VDSAnalysis(SarImageReader gir, IMask[] mask, float enlf, float thresholdHH, float thresholdHV, float thresholdVH, float thresholdVV, IProgress progressBar) {
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
        
        // progress bar for progress monitoring
        this.progressBar = progressBar;
        
    }

    public DetectedPixels getPixels() {
        return pixels;
    }

    /*support of different thresholds for different bands
     * 
     * 
     */
    public void run(KDistributionEstimation kdist) {
        if(gir.getBandName(gir.getBand()).equals("HH")||gir.getBandName(gir.getBand()).equals("H/H")){
            pixels = analyse(kdist, thresholdHH);
        }else if(gir.getBandName(gir.getBand()).equals("HV")||gir.getBandName(gir.getBand()).equals("H/V")){
            pixels = analyse(kdist, thresholdHV);
        }else if(gir.getBandName(gir.getBand()).equals("VH")||gir.getBandName(gir.getBand()).equals("V/H")){
            pixels = analyse(kdist, thresholdVH);
        }else if(gir.getBandName(gir.getBand()).equals("VV")||gir.getBandName(gir.getBand()).equals("V/V")){
            pixels = analyse(kdist, thresholdVV);
        }else{
            pixels = analyse(kdist, threshold);
        }
        
    }
    
    /**
     * 
     * @param kdist
     * @param significance
     * @return
     */
    private DetectedPixels analyse(KDistributionEstimation kdist, float significance) {
        DetectedPixels dpixels = new DetectedPixels(gir);
        int horTiles = gir.getWidth() / this.tileSize, verTiles = gir.getHeight() / this.tileSize;
        //int[] sizeTile = new int[2];
        // the real size of tiles
        int sizeX = gir.getWidth() / horTiles;
        int sizeY = gir.getHeight() / verTiles;
        
        
        
        int xLeftTile=0;
        int xRightTile=0;
        int yTopTile=0;
        int yBottomTile=0;
        
        
        double[][][] tileStat = new double[verTiles][horTiles][5];

        // get band name
        int bandname = gir.getBand();

        // set values for progress bar
        if(!this.isBatch )
        {
            this.progressBar.setMessage("Performing VDS Analysis");
            this.progressBar.setMaximum(verTiles);
        }
        
        int dy=sizeY;
        
        for (int j = 0; j < verTiles; j++) {
            
            // update the progress bar value
            if(this.progressBar != null)
            {
                this.progressBar.setCurrent(j);
            }
            if(j==verTiles-1){
            	//the last tiles have more pixels so we need to calculate the real size
            	dy=gir.getHeight()-((verTiles-1)*sizeY);
            }
            
            
            xLeftTile = 0;				 //old tile[0][0]
            xRightTile = gir.getWidth(); //old tile[0][1]
            yTopTile = j * sizeY;			 //old tile[1][0]
            yBottomTile = yTopTile + sizeY; //old tile[1][1]
            
            int dx=sizeX;
            
            for (int i = 0; i < horTiles; i++) {
            	if(i==horTiles-1){
            		//the last tiles have more pixels so we need to calculate the real size
                	dx=gir.getWidth()-((horTiles-1)*dx);
                }
            	
                xLeftTile = i * dx;
                xRightTile = xLeftTile + dx;
                if (mask == null || mask.length == 0 || mask[0] == null || !intersects(xLeftTile,xRightTile,yTopTile,yBottomTile)) {
                	kdist.setImageData(gir, xLeftTile, yTopTile, 1, 1, dx, dy);
                	kdist.estimate(null);

                	double[][][] thresh = kdist.getDetectThresh();
                    tileStat[j] = kdist.getTileStat()[0];
                    
                    
                    int[] data = gir.readTile(xLeftTile, yTopTile, dx, dy);
                    
                    double threshWindowsVals[]=calcThreshWindowVals(significance, thresh[0][0]);

                    for (int k = 0; k < dy; k++) {
                        for (int h = 0; h < dx; h++) {
                            int subwindow = 1;
                            if (h < dx / 2) {
                                if (k < dy / 2) {
                                    subwindow = 1;
                                } else {
                                    subwindow = 3;
                                }
                            } else {
                                if (k < dy / 2) {
                                    subwindow = 2;
                                } else {
                                    subwindow = 4;
                                }
                            }
                            int pix = data[k * dx + h];
                            // Modified condition from S = ((pix/mean) - 1)/(t_p - 1) where T_window = t_p * mean
                            if (pix >threshWindowsVals[subwindow-1] ) {
                                dpixels.add(h + xLeftTile, k + yTopTile, pix, thresh[0][0][subwindow] / thresh[0][0][5], thresh[0][0][0] * thresh[0][0][subwindow] / thresh[0][0][5], thresh[0][0][5], bandname);
                            }

                        }
                    }
                } else {
                    // compute different statistics if the tile intersects the land mask
                    // check if there is sea pixels in the tile area
                    if(includes(xLeftTile,xRightTile,yTopTile,yBottomTile))
                        continue;
                    // create raster mask
                    Raster rastermask = null;
                    rastermask = (mask[0].rasterize(new Rectangle(xLeftTile, yTopTile, dx, dy), -xLeftTile, -yTopTile, 1.0)).getData();
                    //Read pixels for the area and check there are enough sea pixels
                    int[] maskdata = rastermask.getPixels(0, 0, rastermask.getWidth(), rastermask.getHeight(), (int[])null);
                    int pixelcount = 0;
                    for(int count = 0; count < maskdata.length; count++)
                        pixelcount += maskdata[count];
                    //System.out.println("Mask pixels at " + (new Rectangle(xLeftTile, yTopTile, dx, dy)).toString() + ":" + pixelcount);
                    if(((double)pixelcount / maskdata.length) < 0.7)
                    {
                        // if there are pixels to estimate, calculate statistics using the mask
                        kdist.setImageData(gir, xLeftTile, yTopTile, 1, 1, dx, dy);
                        kdist.estimate(rastermask);
                        double[][][] thresh = kdist.getDetectThresh();
                        tileStat[j] = kdist.getTileStat()[0];
                        
                        double threshWindowsVals[]=calcThreshWindowVals(significance, thresh[0][0]);
                        
                        int[] data = gir.readTile(xLeftTile, yTopTile, dx, dy);

                        for (int k = 0; k < dy; k++) {
                            for (int h = 0; h < dx; h++) {
                                // check pixel is in the sea
                                if(rastermask.getSample(h, k, 0) == 0)
                                {
                                    int subwindow = 1;
                                    if (h < dx / 2) {
                                        if (k < dy / 2) {
                                            subwindow = 1;
                                        } else {
                                            subwindow = 3;
                                        }
                                    } else {
                                        if (k < dy / 2) {
                                            subwindow = 2;
                                        } else {
                                            subwindow = 4;
                                        }
                                    }
                                    int pix = data[k * dx + h];
                                    // if (pix > thresh[i][0][subwindow] * (significance - (significance - 1.)	/ thresh[i][0][5])) {
                                    // Modified condition from S = ((pix/mean) - 1)/(t_p - 1) where T_window = t_p * mean
                                    if (pix > threshWindowsVals[subwindow-1]) {
                                        dpixels.add(h + xLeftTile, k + yTopTile, pix, thresh[0][0][subwindow] / thresh[0][0][5], thresh[0][0][0] * thresh[0][0][subwindow] / thresh[0][0][5], thresh[0][0][5], bandname);
                                    }
                                }

                            }
                        }
                    }
                }
            }
            System.out.println(j + "/" + verTiles);
           // System.out.println("Detected Pixels for Tile "+j+ ":"+dpixels.getAllDetectedPixels().size());
        }
        System.out.println("Detected Pixels Total :"+dpixels.getAllDetectedPixels().size());
        return dpixels;
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

    // return a geometry of grid of Tiles
    public List<Geometry> getTiles() {
        
        int horTiles = gir.getWidth() / this.tileSize;
        int verTiles = gir.getHeight() / this.tileSize;
        
        List<Geometry> tiles = new ArrayList<Geometry>(horTiles*verTiles*8);
        
        int[] sizeTile = new int[2];
        // the real size of tiles
        sizeTile[0] = gir.getWidth() / horTiles;
        sizeTile[1] = gir.getHeight() / verTiles;
        GeometryFactory geomFactory = new GeometryFactory();
        Coordinate[] coo=null;
        for (int j = 0; j < verTiles; j++) {
        	coo=new Coordinate[2];
        	coo[0]=new Coordinate(0, j * sizeTile[1]);
        	coo[1]=new Coordinate((double)gir.getWidth(), (double)j * sizeTile[1]);
            tiles.add(geomFactory.createLineString(coo));
        }
        for (int i = 0; i < horTiles; i++) {
        	coo=new Coordinate[2];
        	coo[0]=new Coordinate(i * sizeTile[0], 0);
        	coo[1]=new Coordinate((double)i * sizeTile[0], (double)gir.getHeight());
            tiles.add(geomFactory.createLineString(coo));
        }
        return tiles;

    }
    
    
    /*
    // return a geometry of grid of Tiles
    public Vector<Geometry> getTiles() {
        Vector<Geometry> tiles = new Vector<Geometry>();
        int horTiles = gir.getWidth() / this.tileSize;
        int verTiles = gir.getHeight() / this.tileSize;
        int[] sizeTile = new int[2];
        // the real size of tiles
        sizeTile[0] = gir.getWidth() / horTiles;
        sizeTile[1] = gir.getHeight() / verTiles;
        GeometryFactory geomFactory = new GeometryFactory();
        for (int j = 0; j < verTiles; j++) {
            tiles.add(geomFactory.createLineString(new Coordinate[]{new Coordinate(0, j * sizeTile[1]), new Coordinate((double)gir.getWidth(), (double)j * sizeTile[1])}));
        }
        for (int i = 0; i < horTiles; i++) {
            tiles.add(geomFactory.createLineString(new Coordinate[]{new Coordinate(i * sizeTile[0], 0), new Coordinate((double)i * sizeTile[0], (double)gir.getHeight())}));
        }
        return tiles;

    }*/
    
}
