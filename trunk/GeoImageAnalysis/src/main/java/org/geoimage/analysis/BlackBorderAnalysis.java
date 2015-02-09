package org.geoimage.analysis;

import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.geoimage.def.GeoImageReader;

public class BlackBorderAnalysis {
	public class TileAnalysis{
		int[] cutOffArray=null;
		boolean bIsBorder=false;
		boolean bBtoWBorder=false;

	}
	
	HashMap<String,BlackBorderAnalysis.TileAnalysis> mapAnalysis=new HashMap<String,BlackBorderAnalysis.TileAnalysis>();
	
	private final int nRowSamples=3;
	
	private double dAvEtreme1[]=new double[nRowSamples];
	private double dAvEtreme2[]=new double[nRowSamples];
	private double dAvEtremeRatio[]=new double[nRowSamples];
	private double dAvEtremeDiff[]=new double[nRowSamples];


	
	private GeoImageReader gir;
	private int horTiles;
	private int verTiles;
	private int tileSize =0;
	private int dx,dy=0;
	private int iNPixExtremes=0;
	
	public BlackBorderAnalysis(GeoImageReader gir,int tSize) {
		this.gir=gir;
		
		if(tSize==0){
			//define the size of the tiles
			tileSize = (int)(ConstantVDSAnalysis.TILESIZE / gir.getGeoTransform().getPixelSize()[0]);
			if(tileSize < ConstantVDSAnalysis.TILESIZEPIXELS) tileSize = ConstantVDSAnalysis.TILESIZEPIXELS;
		}else{
			this.tileSize=tSize;
		}		
			
		horTiles = gir.getWidth() / tileSize;
		verTiles = gir.getHeight() / tileSize;
		
		
		 // the real size of tiles
        dx = gir.getWidth() / horTiles;     //x step
        dy = gir.getHeight() / verTiles;	//y step
        
        iNPixExtremes=tileSize/10;
	}
		
	
	public void analyse(int row,int col) {
		int iniY = (col-1) * dy;
		int iniX = (row-1) * dx;
		analyzeTile(iniX,iniY);
		
	}
	
	public void analyse() {
        int startTileY=0;
        int startTileX=0;
        
        
        for (int j = 0; j < 5; j++) {
        	int iniY = startTileY+j * dy;
        	
        	TileAnalysis previous=null;
        	boolean stop=false;
        	for (int i = 0; i < 5&&!stop; i++) {
        		int iniX = startTileX+i * dx;
        		
        		if(i==0||j==0||previous!=null){
        			TileAnalysis result=analyzeTile(iniX,iniY);
            		putAnalysisTile(j,i,result);
            		if(result.bIsBorder){
            			previous=result;
            		}else{
            			stop=true;
            		}
        		}
        		startTileX=iniX;
        	}
        	startTileY=iniY;
        }
	}  
	
	public void putAnalysisTile(int row,int col,TileAnalysis result){
		String key=""+row+"_"+col;
		mapAnalysis.put(key, result);
	}
	
	public TileAnalysis getAnalysisTile(int row,int col){
		String key=""+row+"_"+col;
		return mapAnalysis.get(key);
	}
	
	/**
	 * 
	 * @param iniX
	 * @param iniY
	 */
	private TileAnalysis analyzeTile(int iniX,int iniY){
		TileAnalysis result=new TileAnalysis();
		
		//array che contine un boolean per ogni riga analizzata se true=la linea contiene un'area di transizione da un "black border" ad un'area valida
		boolean bBtoWBorderV[]=new boolean[3];
		
		int[][] dataRow=new int[nRowSamples][];
		
		for(int idxRow=0;idxRow<nRowSamples;idxRow++){
			//riga di campionamento 1 al 10% al 50% e al 90%
			int row=iniY+((ConstantVDSAnalysis.ROW_TILE_SAMPLES_ARRAY[idxRow]*dy)/100);
			//leggo i dati di 1 riga
			dataRow[idxRow]=gir.readTile(iniX, row, dx,1);
			
			double sStart=0;
			double sEnd=0;
			for(int i=0;i<iNPixExtremes;i++){
				sStart=sStart+dataRow[idxRow][i];
				sEnd=sEnd+dataRow[idxRow][dataRow[idxRow].length-i-1];
			}
			dAvEtreme1[idxRow]=sStart/iNPixExtremes;
			dAvEtreme2[idxRow]=sEnd/iNPixExtremes;
			dAvEtremeRatio[idxRow]=sEnd/sStart;
			dAvEtremeDiff[idxRow]=(sEnd-sStart)/iNPixExtremes;
	
			bBtoWBorderV[idxRow]=dAvEtremeRatio[idxRow]>ConstantVDSAnalysis.THRESH_D_EXTREMES_RATIO 
					&& dAvEtremeDiff[idxRow]>ConstantVDSAnalysis.THRESH_D_EXTREMES_DIFF
					&& dAvEtreme2[idxRow]>=ConstantVDSAnalysis.THRESH_IS_BORDER
					&& dAvEtreme1[idxRow]<ConstantVDSAnalysis.THRESH_VALUE_SAFE;
			
			
		}	
		

		result.bBtoWBorder=bBtoWBorderV[0]&&bBtoWBorderV[1]&&bBtoWBorderV[2];
		
		boolean bIsBorderV[]={false,false,false};
		
		
		if(result.bBtoWBorder==false){
			bIsBorderV[0]=dAvEtreme2[0]<ConstantVDSAnalysis.THRESH_IS_BORDER&&dAvEtreme1[0]<ConstantVDSAnalysis.THRESH_IS_BORDER;
			bIsBorderV[1]=dAvEtreme2[1]<ConstantVDSAnalysis.THRESH_IS_BORDER&&dAvEtreme1[1]<ConstantVDSAnalysis.THRESH_IS_BORDER;
			bIsBorderV[2]=dAvEtreme2[2]<ConstantVDSAnalysis.THRESH_IS_BORDER&&dAvEtreme1[2]<ConstantVDSAnalysis.THRESH_IS_BORDER;
			
			//is true if all elements in BIsBorderV are true
			result.bIsBorder=bIsBorderV[0]&&bIsBorderV[1]&&bIsBorderV[2];
		}
		
		int[] viCutOffColTemp={0,0,0};
		if(result.bBtoWBorder){
			int max1=NumberUtils.max(dataRow[0]);
			int max2=NumberUtils.max(dataRow[1]);
			int max3=NumberUtils.max(dataRow[2]);
			
			double dThresValue[]={max1*ConstantVDSAnalysis.THRESH_MAX_FACTOR,max2*ConstantVDSAnalysis.THRESH_MAX_FACTOR,max3*ConstantVDSAnalysis.THRESH_MAX_FACTOR};
			
			for(int i=0;i<nRowSamples;i++){
				boolean stop=false;
				for(int j=0;j<dataRow[i].length&&!stop;j++){
					if(dataRow[i][j]>=ConstantVDSAnalysis.THRESH_VALUE_SAFE||
						(dataRow[i][j]>dThresValue[0]&&dataRow[i][j]>=ConstantVDSAnalysis.THRESH_IS_BORDER)){
						viCutOffColTemp[i]=j;
							stop=true;
					}
				}
			}
			//if the difference is too high we calculate the Cutoffpoint for each row
			if(NumberUtils.max(viCutOffColTemp)-NumberUtils.min(viCutOffColTemp)>3){
				int dataTile[]=gir.readTile(iniX, iniY, dx,dy);
				viCutOffColTemp=new int[dy];
				for(int row=0;row<dy;row++){
					int posStart=row*dx;
					int[] singleRow=ArrayUtils.subarray(dataTile,posStart,posStart+dx-1);
					
					boolean stop=false;
					for(int col=0;col<singleRow.length&&!stop;col++){
						if(singleRow[col]>=ConstantVDSAnalysis.THRESH_VALUE_SAFE||
								(singleRow[col]>dThresValue[0]&&singleRow[col]>=ConstantVDSAnalysis.THRESH_IS_BORDER)){
								viCutOffColTemp[row]=col;
									stop=true;
						}
					}
				}
			}else{
				//calcolo la media e riempio l'array viCutOffCol
				int m=0;
				for(int i=0;i<nRowSamples;i++) m+=(viCutOffColTemp[i]);
				m=m/nRowSamples;
				viCutOffColTemp=new int[dy];
				Arrays.fill(viCutOffColTemp, m);
			}
		}else{
			if(result.bIsBorder){
				viCutOffColTemp=new int[dy];
				Arrays.fill(viCutOffColTemp, dy);
			}else{
				viCutOffColTemp=new int[dy];
				Arrays.fill(viCutOffColTemp, 0);
			}
		}
		for(int i=0;i<viCutOffColTemp.length;i++){
			System.out.println(viCutOffColTemp[i]);
		}
		
		result.cutOffArray= viCutOffColTemp;
		return result;
	}
	
	
}
