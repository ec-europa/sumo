package org.geoimage.analysis;

import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.geoimage.def.GeoImageReader;

public class BlackBorderAnalysis {
	public class TileAnalysis{
		int[] horizLeftCutOffArray=null;
		int[] horizRightCutOffArray=null;
		int[] verTopCutOffArray=null;
		int[] verBottomOffArray=null;
		boolean bIsBorder=false;
	//	boolean bBtoWBorder=false;

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
	private int sizeX,sizeY=0;
	private int iNPixExtremes=0;
	private int correctionXForLastTile=0;
	private int correctionYForLastTile=0;
	private int numTilesMargin=5;
	
	
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
        sizeX = gir.getWidth() / horTiles;     //x step
        sizeY = gir.getHeight() / verTiles;	   //y step
        
        iNPixExtremes=tileSize/10;
	}
		
	public BlackBorderAnalysis(GeoImageReader gir) {
		this.gir=gir;
		
		//define the size of the tiles
		tileSize = (int)(ConstantVDSAnalysis.TILESIZE / gir.getGeoTransform().getPixelSize()[0]);
		if(tileSize < ConstantVDSAnalysis.TILESIZEPIXELS) tileSize = ConstantVDSAnalysis.TILESIZEPIXELS;
			
		horTiles = gir.getWidth() / tileSize;
		verTiles = gir.getHeight() / tileSize;
		
		 // the real size of tiles
        sizeX = gir.getWidth() / horTiles;     //x step
        sizeY = gir.getHeight() / verTiles;	//y step
       
        correctionXForLastTile=gir.getWidth()-sizeX*horTiles;
        correctionYForLastTile=gir.getHeight()-sizeY*verTiles;
        iNPixExtremes=tileSize/10;
	}
	
	/**
	 * analyze single tile
	 * @param row
	 * @param col
	 */
	public void analyse(int row,int col) {
		int iniY = (col-1) * sizeY;
		int iniX = (row-1) * sizeX;
		analyzeTile(iniX,iniY,true,true);
		
	}
	
	/**
	 * 
	 */
	public void analyse(int numTilesMargin) {
		this.numTilesMargin=numTilesMargin;
		//first five tiles
		horizAnalysis(true);
		//last five tiles
		horizAnalysis(false);
		
		//top five row tiles
		//vertAnalysis(true);
		//bottom five row tiles
		//vertAnalysis(false);
	}

	/**
	 * 	
	 * @param left true start from left margin , false start from right  margin 
	 *  analyze first/last numTilesMargin(default 5) tiles
	 */
	public void horizAnalysis(boolean left) {
        int startYPixelTile=0;
        int startXPixelTile=0;
        
        int startX=0;
        
        if(left){
        	startX=0;
        }else{
        	//start from last tile
        	startX=horTiles-1;
        }
        for (int rowIdx = 0; rowIdx < verTiles; rowIdx++) {
        	
        	TileAnalysis previous=null;
        	boolean stop=false;
        	startXPixelTile=startX*sizeX;
        	
        	
        	for (int colIdx = startX,iteration=0; iteration < numTilesMargin&&!stop;iteration++ ) {
        		//entro se sono sulla prima colonna oppure se previous!=null
        		if(colIdx==startX||previous!=null){
        			//check if we are on the last tile to correct the size
        			int sx=sizeX;
        			if(colIdx==horTiles-1){
        				sx=sizeX+correctionXForLastTile;
        			}
        			int sy=sizeY;
        			if(rowIdx==verTiles-1){
        				sy=sizeY+correctionYForLastTile;
        			}
        			TileAnalysis result=getAnalysisTile(rowIdx, colIdx);
        			//if is not already analyzed or if is not completely on the black margin
        			if(result==null||result.bIsBorder==false){
        				if(result==null)
        					result=new TileAnalysis();
        				int[] resultArray=analyzeTile(startXPixelTile,startYPixelTile,true,left,sx,sy);
        			
        				result.bIsBorder=resultArray==null; //if resultArray is null the tile is all on the black border
        				if(left){
        					result.horizLeftCutOffArray=resultArray;
        					result.horizRightCutOffArray=null;
        				}else{
        					result.horizRightCutOffArray=resultArray;
        					result.horizLeftCutOffArray=null;
        				}	
	            		putAnalysisTile(rowIdx,colIdx,result);
        			}
	            	
	            	if(result.bIsBorder){
            			previous=result;
            		}else{
            			stop=true;
            		}
        		}
        		
        		if(left){
        			colIdx++;
        			startXPixelTile=startXPixelTile+sizeX;
        		}else{
        			colIdx--;
        			if(colIdx==horTiles-1){
        				startXPixelTile=startXPixelTile-sizeX-correctionXForLastTile;
        			}
        			startXPixelTile=startXPixelTile-sizeX;
        		}
        	}
        	startYPixelTile=startYPixelTile+sizeY;
        }
	}  
	
	
	/**
	 * 	
	 * @param top true start from left margin , false start from right  margin 
	 *  analyze first/last numTilesMargin(default 5) tiles
	 */
	public void vertAnalysis(boolean top) {
        int startYPixelTile=0;
        int startXPixelTile=0;
        
        int startY=0;
        
        if(top){
        	startY=0;
        }else{
        	startY=verTiles;
        }
        for (int colIdx = 0; colIdx < horTiles; colIdx++) {
        	
        	TileAnalysis previous=null;
        	boolean stop=false;
        	startYPixelTile=startY*sizeY;
        	for (int rowIdx = startY,iteration=0; iteration < numTilesMargin&&!stop; iteration++) {
        		
        		if(rowIdx==startY||colIdx==0||previous!=null){
        			//check if we are on the last tile to correct the size
        			int sy=sizeY;
        			if(colIdx==verTiles-1){
        				sy=sizeY+correctionYForLastTile;
        			}
        			TileAnalysis result=getAnalysisTile(rowIdx, colIdx);
        			//if is not already analyzed or if is not completely on the black margin
        			if(result==null||result.bIsBorder==false){
        				if(result==null)
        					result=new TileAnalysis();
        				int resultArray[]=analyzeTile(startXPixelTile,startYPixelTile,false,top,sy,sizeX);
        				result.bIsBorder=resultArray==null; //if resultArray is null the tile is all on the black border
        				result.verTopCutOffArray=resultArray;
        				putAnalysisTile(rowIdx,colIdx,result);
        			}	
            		if(result.bIsBorder){
            			previous=result;
            		}else{
            			stop=true;
            		}
        		}
        		startYPixelTile=startYPixelTile+sizeY;
        		if(top){
        			rowIdx++;
        		}else{
        			rowIdx--;
        		}
        	}
        	startXPixelTile=startXPixelTile+sizeX;
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
	 *  used for analyze single tile
	 * @param iniX
	 * @param iniY
	 * @param startFromLeft
	 * @return
	 */
	private int[] analyzeTile(int iniX,int iniY,boolean horizontal,boolean startFromLeft){
		//correctionXForLastTile=gir.getWidth()-sizeX*horTiles;
		int[]cutOff=analyzeTile(iniX, iniY,horizontal, startFromLeft, sizeX+correctionXForLastTile, sizeY);
		
		return cutOff;
	}
	
	
	/*
	public int[] traspone(int lineMatrix[],int rowSize,int colSize){
	    int trasposta[]=new int[lineMatrix.lenght];
		for(int idx=0;idx<lineMatrix.lenght;idx++){
		    int idTrasposta=(idx*rowSize)%lineMatrix.lenght;
			trasposta[idTrasposta]=
			
		}
		
	} 
	
	*/
	/**
	 * 
	 * @param iniX
	 * @param iniY
	 * @param horizontal
	 * @param invert
	 * @param tileWidth
	 * @param tileHeight
	 * @return
	 */
	private int[] analyzeTile(int iniX,int iniY,boolean horizontal,boolean startFromLeft,int tileWidth,int tileHeight){
		TileAnalysis result=new TileAnalysis();
		int vCutOffSize=0;
		
		//array che contine un boolean per ogni riga analizzata se true=la linea contiene un'area di transizione da un "black border" ad un'area valida
		boolean bBtoWBorderV[]=new boolean[3];
		
		int[][] dataRow=new int[nRowSamples][];
		
		for(int idxSamples=0;idxSamples<nRowSamples;idxSamples++){
			
			//riga di campionamento 1 al 10% al 50% e al 90%
			int row=iniY+((ConstantVDSAnalysis.ROW_TILE_SAMPLES_ARRAY[idxSamples]*sizeY)/100);
			
			//leggo i dati di 1 riga o di 1 colonna
			if(horizontal){
				dataRow[idxSamples]=gir.readTile(iniX, row, tileWidth,1);
				vCutOffSize=tileHeight;
			}else{
				dataRow[idxSamples]=gir.readTile(iniX, row, 1,sizeY);
				vCutOffSize=tileWidth;
			}	
				
			if(!startFromLeft){
				ArrayUtils.reverse(dataRow[idxSamples]);
			}
			
			
			double sStart=0;
			double sEnd=0;
			for(int i=0;i<iNPixExtremes;i++){
				sStart=sStart+dataRow[idxSamples][i];
				sEnd=sEnd+dataRow[idxSamples][dataRow[idxSamples].length-i-1];
			}
			dAvEtreme1[idxSamples]=sStart/iNPixExtremes;
			dAvEtreme2[idxSamples]=sEnd/iNPixExtremes;
			dAvEtremeRatio[idxSamples]=sEnd/sStart;
			dAvEtremeDiff[idxSamples]=(sEnd-sStart)/iNPixExtremes;
	
			bBtoWBorderV[idxSamples]=dAvEtremeRatio[idxSamples]>ConstantVDSAnalysis.THRESH_D_EXTREMES_RATIO 
					&& dAvEtremeDiff[idxSamples]>ConstantVDSAnalysis.THRESH_D_EXTREMES_DIFF
					&& dAvEtreme2[idxSamples]>=ConstantVDSAnalysis.THRESH_IS_BORDER
					&& dAvEtreme1[idxSamples]<ConstantVDSAnalysis.THRESH_VALUE_SAFE;
			
			
		}	
		

		boolean bBtoWBorder=bBtoWBorderV[0]||bBtoWBorderV[1]||bBtoWBorderV[2];
		
		boolean bIsBorderV[]={false,false,false};
		
		
		if(bBtoWBorder==false){
			bIsBorderV[0]=dAvEtreme2[0]<ConstantVDSAnalysis.THRESH_IS_BORDER&&dAvEtreme1[0]<ConstantVDSAnalysis.THRESH_IS_BORDER;
			bIsBorderV[1]=dAvEtreme2[1]<ConstantVDSAnalysis.THRESH_IS_BORDER&&dAvEtreme1[1]<ConstantVDSAnalysis.THRESH_IS_BORDER;
			bIsBorderV[2]=dAvEtreme2[2]<ConstantVDSAnalysis.THRESH_IS_BORDER&&dAvEtreme1[2]<ConstantVDSAnalysis.THRESH_IS_BORDER;
			
			//is true if all elements in BIsBorderV are true
			result.bIsBorder=bIsBorderV[0]&&bIsBorderV[1]&&bIsBorderV[2];
		}
		
		int[] viCutOffColTemp={0,0,0};
		if(bBtoWBorder){
			int max1=NumberUtils.max(dataRow[0]);
			int max2=NumberUtils.max(dataRow[1]);
			int max3=NumberUtils.max(dataRow[2]);
			
			double dThresValue[]={max1*ConstantVDSAnalysis.THRESH_MAX_FACTOR,max2*ConstantVDSAnalysis.THRESH_MAX_FACTOR,max3*ConstantVDSAnalysis.THRESH_MAX_FACTOR};
			
			
			//loop for each array of samples
			for(int arraySamplesId=0;arraySamplesId<nRowSamples;arraySamplesId++){
				boolean stop=false;
				//loop on each element of the data readed if stop is false
				for(int idCutOffElement=0;idCutOffElement<dataRow[arraySamplesId].length&&!stop;idCutOffElement++){
					if(dataRow[arraySamplesId][idCutOffElement]>=ConstantVDSAnalysis.THRESH_VALUE_SAFE||
					  (dataRow[arraySamplesId][idCutOffElement]>dThresValue[arraySamplesId]&&dataRow[arraySamplesId][idCutOffElement]>=ConstantVDSAnalysis.THRESH_IS_BORDER)){
						//save the cutoff and stop
						if(startFromLeft)
							viCutOffColTemp[arraySamplesId]=idCutOffElement;
						else
							viCutOffColTemp[arraySamplesId]=tileWidth-idCutOffElement;
						stop=true;	
					}
				}
			}
			
			//if the difference is too high we calculate the Cutoffpoint for each row
			if(NumberUtils.max(viCutOffColTemp)-NumberUtils.min(viCutOffColTemp)>3){
				//read complete tile
				int dataTile[]=gir.readTile(iniX, iniY, tileWidth,sizeY);
				viCutOffColTemp=new int[vCutOffSize];
				

				//loop on each element of the data readed if stop is false
				for(int arraySamplesId=0;arraySamplesId<vCutOffSize;arraySamplesId++){
					try{
						int posStart=arraySamplesId*tileWidth;
						
						int[] singleRow=ArrayUtils.subarray(dataTile,posStart,posStart+tileWidth);
						int maxValue=NumberUtils.max(singleRow);
						double thres=maxValue*ConstantVDSAnalysis.THRESH_MAX_FACTOR;
						
						boolean stop=false;
						for(int idCutOffElement=0;idCutOffElement<singleRow.length&&!stop;idCutOffElement++){
							
							if((singleRow[idCutOffElement]>=ConstantVDSAnalysis.THRESH_VALUE_SAFE||
									(singleRow[idCutOffElement]>thres&&singleRow[idCutOffElement]>=ConstantVDSAnalysis.THRESH_IS_BORDER))||
									(idCutOffElement==singleRow.length)){
									if(startFromLeft)
										viCutOffColTemp[arraySamplesId]=idCutOffElement;
									else
										viCutOffColTemp[arraySamplesId]=tileWidth-idCutOffElement;
									stop=true;
							}
						}
					}catch(Exception e ){
						    
						   
						
						
						
					}	
				}
			}else{
				//calcolo la media e riempio l'array viCutOffCol
				int m=0;
				for(int i=0;i<nRowSamples;i++){
					m+=(viCutOffColTemp[i]);
				}
				m=m/nRowSamples;
				viCutOffColTemp=new int[vCutOffSize];
				Arrays.fill(viCutOffColTemp, m);
			}
		}else{
			if(result.bIsBorder){
				viCutOffColTemp=null;
				//viCutOffColTemp=new int[vCutOffSize];
				//Arrays.fill(viCutOffColTemp, vCutOffSize);
			}else{
				viCutOffColTemp=new int[vCutOffSize];
				if(startFromLeft)
					Arrays.fill(viCutOffColTemp, 0);
				else
					Arrays.fill(viCutOffColTemp, tileWidth-1);
			}
		}
		if(viCutOffColTemp!=null)
			for(int i=0;i<viCutOffColTemp.length;i++){
				System.out.println(viCutOffColTemp[i]);
			}
		
		return viCutOffColTemp;
	}
	
	
}
