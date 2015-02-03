package org.geoimage.analysis;

import java.awt.Rectangle;
import java.awt.image.Raster;

import org.geoimage.def.GeoImageReader;

public class BlackBorderAnalysis {
	
	
	private int numOfTiles=200;
	private final int nRowSamples=3;
	
	private double dAvEtreme1[]=new double[nRowSamples];
	private double dAvEtreme2[]=new double[nRowSamples];
	private double dAvEtremeRation[]=new double[nRowSamples];
	private double dAvEtremeDiff[]=new double[nRowSamples];
	
	
	
	
	public BlackBorderAnalysis() {
	}
		
	
	
	private void analyse(GeoImageReader gir) {
	//	this.tileSize = (int)(ConstantVDSAnalysis.TILESIZE / gir.getGeoTransform().getPixelSize()[0]);
    //    if(this.tileSize < ConstantVDSAnalysis.TILESIZEPIXELS) this.tileSize = ConstantVDSAnalysis.TILESIZEPIXELS;
		
		/*int horTiles = gir.getWidth() / this.tileSize, verTiles = gir.getHeight() / this.tileSize;
        //int[] sizeTile = new int[2];
        // the real size of tiles
        int dx = gir.getWidth() / horTiles;
        int dy = gir.getHeight() / verTiles;
		
		
		
        int horTiles = gir.getWidth() / numOfTiles;
        int verTiles = gir.getHeight() / numOfTiles;
        
        
        for (int j = 0; j < verTiles; j++) {
        	int iniX = startTile[0]+i * sizeTile[0];
            int iniY = startTile[1]+j * sizeTile[1];
            int[] data=gir.readTile(iniX, iniY, sizeTile[0], sizeTile[1]);

                  
        }*/
    }
	
}
