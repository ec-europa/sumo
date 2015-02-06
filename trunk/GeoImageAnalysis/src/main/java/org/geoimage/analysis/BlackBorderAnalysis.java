package org.geoimage.analysis;

import org.geoimage.def.GeoImageReader;

public class BlackBorderAnalysis {
	
	
	private int numOfTiles=200;
	private final int nRowSamples=3;
	
	private double dAvEtreme1[]=new double[nRowSamples];
	private double dAvEtreme2[]=new double[nRowSamples];
	
	private double dAvEtremeRatio[]=new double[nRowSamples];
	private double dAvEtremeDiff[]=new double[nRowSamples];
	
	
	private GeoImageReader gir;
	
	
	
	public BlackBorderAnalysis(GeoImageReader gir) {
		this.gir=gir;
	}
		
	
	public void analyse(int row,int col) {
		
	}
	
	public void analyse() {
		
		//define the size of the tiles
		int tileSize = (int)(ConstantVDSAnalysis.TILESIZE / gir.getGeoTransform().getPixelSize()[0]);
		if(tileSize < ConstantVDSAnalysis.TILESIZEPIXELS) tileSize = ConstantVDSAnalysis.TILESIZEPIXELS;
		
		int horTiles = gir.getWidth() / tileSize, verTiles = gir.getHeight() / tileSize;
		
		
        // the real size of tiles
        int dx = gir.getWidth() / horTiles;     //x step
        int dy = gir.getHeight() / verTiles;	//y step
        
        int startTileY=0;
        int startTileX=0;
        
        for (int j = 0; j < 1; j++) {
        	int iniY = startTileY+j * dy;
        	
        	for (int i = 0; i < 5; i++) {
        		int iniX = startTileX+i * dx;
        		
        		analyzeTile(iniX,iniY,dx,dy);
        		
        		
        		startTileX=iniX;
        	}
        	startTileY=iniY;
        }
	}    
	
	private void analyzeTile(int iniX,int iniY,int dx,int dy){
		//riga di campionamento 1 al 10%
		int row1=iniY+(ConstantVDSAnalysis.ROW_TILE_SAMPLES_ARRAY[0]*(iniY+dy))/100;
		//leggo i dati di 1 riga
		int[] dataRow1=gir.readTile(iniX, row1, dx,1);
		
		//riga di campionamento 2 al 50%
		int row2=iniY+(ConstantVDSAnalysis.ROW_TILE_SAMPLES_ARRAY[1]*(iniY+dy))/100;
		int[] dataRow2=gir.readTile(iniX, row2, dx,1);
		
		//riga di campionamento 3 al 90%
		int row3=iniY+(ConstantVDSAnalysis.ROW_TILE_SAMPLES_ARRAY[2]*(iniY+dy))/100;
		int[] dataRow3=gir.readTile(iniX, row3, dx,1);
		
		
		
		
		
		
		
	}
	
}
