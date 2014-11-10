/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.analysis;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import java.awt.Rectangle;
import java.awt.image.Raster;
import java.util.Vector;

import org.geoimage.def.GeoImageReader;
import org.geoimage.impl.SarImageReader;
import org.geoimage.utils.IMask;

import java.util.List;

import org.geoimage.utils.IProgress;

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
    private List<IMask> mask;
    // Minimum Tile Size for VDS analysis in meters
    private final int TILESIZE = 1000;
    // Minimum Tile Size for VDS analysis in pixels
    private final int TILESIZEPIXELS = 200;
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
    public VDSAnalysis(SarImageReader gir, List<IMask> mask, float enlf,  float thresholdHH, float thresholdHV, float thresholdVH, float thresholdVV , boolean batch){
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
    public VDSAnalysis(SarImageReader gir, List<IMask> mask, float enlf, float thresholdHH, float thresholdHV, float thresholdVH, float thresholdVV, IProgress progressBar) {
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
        this.tileSize = (int)(TILESIZE / gir.getGeoTransform().getPixelSize()[0]);
        if(this.tileSize < TILESIZEPIXELS) this.tileSize = TILESIZEPIXELS;
        
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

    private DetectedPixels analyse(KDistributionEstimation kdist, float significance) {
        DetectedPixels dpixels = new DetectedPixels(gir);
        int horTiles = gir.getWidth() / this.tileSize, verTiles = gir.getHeight() / this.tileSize;
        int[] sizeTile = new int[2];
        // the real size of tiles
        sizeTile[0] = gir.getWidth() / horTiles;
        sizeTile[1] = gir.getHeight() / verTiles;
        int[][] tile = new int[2][2];
        // warning array in [y][x][subwindow] !!!!!!!
        double[][][] tileStat = new double[verTiles][horTiles][5];
        // get band name
        int bandname = gir.getBand();

        // set values for progress bar
        if(!this.isBatch )
        {
            this.progressBar.setMessage("Performing VDS Analysis");
            this.progressBar.setMaximum(verTiles);
        }
            
        for (int j = 0; j < verTiles; j++) {
            
            // update the progress bar value
            if(this.progressBar != null)
            {
                this.progressBar.setCurrent(j);
            }
            
            tile[0][0] = 0;
            tile[0][1] = gir.getWidth();
            tile[1][0] = j * sizeTile[1];
            tile[1][1] = tile[1][0] + sizeTile[1];

            for (int i = 0; i < horTiles; i++) {

                tile[0][0] = i * sizeTile[0];
                tile[0][1] = tile[0][0] + sizeTile[0];
                if ((mask == null) || (mask.size() == 0) || (mask.get(0) == null) || (!intersects(tile))) {
                    kdist.setImageData(gir, tile[0][0], tile[1][0], 1, 1, sizeTile[0], sizeTile[1]);
                    kdist.estimate(null);
                    double[][][] thresh = kdist.getDetectThresh();
                    tileStat[j] = kdist.getTileStat()[0];
                    int[] data = gir.readTile(tile[0][0], tile[1][0], sizeTile[0], sizeTile[1]);

                    for (int k = 0; k < sizeTile[1]; k++) {
                        for (int h = 0; h < sizeTile[0]; h++) {
                            int subwindow = 1;
                            if (h < sizeTile[0] / 2) {
                                if (k < sizeTile[1] / 2) {
                                    subwindow = 1;
                                } else {
                                    subwindow = 3;
                                }
                            } else {
                                if (k < sizeTile[1] / 2) {
                                    subwindow = 2;
                                } else {
                                    subwindow = 4;
                                }
                            }
                            int pix = data[k * sizeTile[0] + h];
                            // if (pix > thresh[i][0][subwindow] * (significance - (significance - 1.)	/ thresh[i][0][5])) {
                            // Modified condition from S = ((pix/mean) - 1)/(t_p - 1) where T_window = t_p * mean
                            if (pix > (significance * (thresh[0][0][5] - 1.0) + 1.0) * thresh[0][0][subwindow] / thresh[0][0][5]) {
                                dpixels.add(h + tile[0][0], k + tile[1][0], pix, thresh[0][0][subwindow] / thresh[0][0][5], thresh[0][0][0] * thresh[0][0][subwindow] / thresh[0][0][5], thresh[0][0][5], bandname);
                            }

                        }
                    }
                } else {
                    // compute different statistics if the tile intersects the land mask
                    // check if there is sea pixels in the tile area
                    if(includes(tile))
                        continue;
                    // create raster mask
                    Raster rastermask = null;
                    rastermask = (mask.get(0).rasterize(new Rectangle(tile[0][0], tile[1][0], sizeTile[0], sizeTile[1]), -tile[0][0], -tile[1][0], 1.0)).getData();
                    // check there are enough sea pixels
                    int[] maskdata = rastermask.getPixels(0, 0, rastermask.getWidth(), rastermask.getHeight(), (int[])null);
                    int pixelcount = 0;
                    for(int count = 0; count < maskdata.length; count++)
                        pixelcount += maskdata[count];
                    //System.out.println("Mask pixels at " + (new Rectangle(tile[0][0], tile[1][0], sizeTile[0], sizeTile[1])).toString() + ":" + pixelcount);
                    if(((double)pixelcount / maskdata.length) < 0.7)
                    {
                        // if there are pixels to estimate, calculate statistics using the mask
                        kdist.setImageData(gir, tile[0][0], tile[1][0], 1, 1, sizeTile[0], sizeTile[1]);
                        kdist.estimate(rastermask);
                        double[][][] thresh = kdist.getDetectThresh();
                        tileStat[j] = kdist.getTileStat()[0];
                        int[] data = gir.readTile(tile[0][0], tile[1][0], sizeTile[0], sizeTile[1]);

                        for (int k = 0; k < sizeTile[1]; k++) {
                            for (int h = 0; h < sizeTile[0]; h++) {
                                // check pixel is in the sea
                                if(rastermask.getSample(h, k, 0) == 0)
                                {
                                    int subwindow = 1;
                                    if (h < sizeTile[0] / 2) {
                                        if (k < sizeTile[1] / 2) {
                                            subwindow = 1;
                                        } else {
                                            subwindow = 3;
                                        }
                                    } else {
                                        if (k < sizeTile[1] / 2) {
                                            subwindow = 2;
                                        } else {
                                            subwindow = 4;
                                        }
                                    }
                                    int pix = data[k * sizeTile[0] + h];
                                    // if (pix > thresh[i][0][subwindow] * (significance - (significance - 1.)	/ thresh[i][0][5])) {
                                    // Modified condition from S = ((pix/mean) - 1)/(t_p - 1) where T_window = t_p * mean
                                    if (pix > (significance * (thresh[0][0][5] - 1.0) + 1.0) * thresh[0][0][subwindow] / thresh[0][0][5]) {
                                        dpixels.add(h + tile[0][0], k + tile[1][0], pix, thresh[0][0][subwindow] / thresh[0][0][5], thresh[0][0][0] * thresh[0][0][subwindow] / thresh[0][0][5], thresh[0][0][5], bandname);
                                    }
                                }

                            }
                        }
                    }
                }
            }
            System.out.println(j + "/" + verTiles);
        }
        System.out.println();
        return dpixels;
    }

    public boolean intersects(int[][] tile) {
        if (mask == null) {
            return false;
        }
        for (IMask m : mask) {
            if (m.intersects(tile[0][0], tile[1][0], tile[0][1] - tile[0][0], tile[1][1] - tile[1][0])) {
                return true;
            }

        }
        return false;
    }

    private boolean includes(int[][] tile) {
         if (mask == null) {
            return false;
        }
        for (IMask m : mask) {
            if (m.includes(tile[0][0], tile[1][0], tile[0][1] - tile[0][0], tile[1][1] - tile[1][0])) {
                return true;
            }

        }
        return false;
    }

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

    }
}
