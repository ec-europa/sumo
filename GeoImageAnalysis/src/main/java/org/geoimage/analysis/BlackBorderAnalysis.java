package org.geoimage.analysis;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.geoimage.def.GeoImageReader;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

public class BlackBorderAnalysis {
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(BlackBorderAnalysis.class);

	
	
	public class TileAnalysis{
		public int[] horizLeftCutOffArray=null;
		public int[] horizRightCutOffArray=null;
		public int[] verTopCutOffArray=null;
		public int[] verBottomOffArray=null;
		public boolean bIsBorder=false;
		public boolean fullAnalysis=false;

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
	private List<Geometry> land;
	
	private int bandAnalysis=0;
	
	
	public BlackBorderAnalysis(GeoImageReader gir,int tSize,List<Geometry> land) {
		this.gir=gir;
		
		//if there is an image with cross-pol (HV or VH) we use it 
		int nb=gir.getNBand();
		if(nb>1){
			for(int i=0;i<nb;i++){
				if(gir.getBandName(i).equalsIgnoreCase("HV")||gir.getBandName(i).equalsIgnoreCase("VH")){
					bandAnalysis=i;
					break;
				}
			}
		}
		
		if(tSize==0){
			//define the size of the tiles
			tileSize = (int)(ConstantVDSAnalysis.TILESIZE / gir.getPixelsize()[0]);
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
        
        this.land=land;
	}
		
	public BlackBorderAnalysis(GeoImageReader gir,List<Geometry> land) {
		this(gir,0,land);
	}
	
	/**
	 * analyze single tile
	 * @param row
	 * @param col
	 */
	public void analyse(int row,int col,boolean horizontalAnalysis) {
		int iniX = (col-1) * sizeX;
		int iniY = (row-1) * sizeY;
		boolean normalDirection=true; //from left to right or from top to bottom
		
		if((iniX>(gir.getWidth()/2)&&horizontalAnalysis)){
			normalDirection=false;
		}
		if((iniY>(gir.getWidth()/2)&&!horizontalAnalysis)){
			normalDirection=false;
		}
		analyzeTile(iniX,iniY,horizontalAnalysis,normalDirection);
		
	}
	
	/**
	 *  check if the tile is on land
	 *  
	 * @param top
	 * @param left
	 * @param bottom
	 * @param right
	 * @return
	 */
	public boolean checkIfTileIsOnLand(double top,double left,double bottom,double right){
		boolean isOnLand=false;
		if(land!=null){
			GeometryFactory fact = new GeometryFactory();
			Coordinate[] cs=new Coordinate[5];
			cs[0]=new Coordinate(top, left);
			cs[1]=new Coordinate(bottom, left);
			cs[2]=new Coordinate(top,right);
			cs[3]=new Coordinate(bottom,right);
			cs[4]=new Coordinate(top,left);
			Polygon tile=fact.createPolygon(cs);

			for (Geometry p : land) {
	            if (p.contains(tile)) {
	            	isOnLand=true;
	            	break;
	            }
 	        }


		}
		
		return isOnLand;
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
		vertAnalysis(true);
		//bottom five row tiles
		vertAnalysis(false);
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
        if(!left){
        	//start from last tile
        	startX=horTiles-1;
        }
        int countTilesAnalyzed=0;
        int countFullTilesAnalyzed=0;
        
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
        			
        			boolean isOnLand=checkIfTileIsOnLand(startXPixelTile,startYPixelTile,startXPixelTile+sx,startYPixelTile+sy);
        			if(!isOnLand){
	        			TileAnalysis result=getAnalysisTile(rowIdx, colIdx);
	        			//if is not already analyzed or if is not completely on the black margin
	        			if(result==null||result.bIsBorder==false){
	        				if(result==null)
	        					result=new TileAnalysis();
	        				
	        				analyzeTile(startXPixelTile,startYPixelTile,true,left,sx,sy,result);
		            		putAnalysisTile(rowIdx,colIdx,result);
		            		countTilesAnalyzed++;
		            		if(result.fullAnalysis)countFullTilesAnalyzed++;
	        			}
		            	
		            	if(result.bIsBorder){
	            			previous=result;
	            		}else{
	            			stop=true;
	            		}
        			}else{
        				stop=true;
        				logger.debug("Tile is On Land  X:"+startXPixelTile+"  Y:"+startYPixelTile);
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
        logger.debug("Tiles Analized H:"+countTilesAnalyzed + "Full Analyzed:"+countFullTilesAnalyzed);
	}  
	
	
	/**
	 * 	
	 * @param top true start from top margin , false start from bottom  margin 
	 *  analyze first/last numTilesMargin(default 5) tiles
	 **/
	public void vertAnalysis(boolean top) {
		int startYPixelTile=0;
        int startXPixelTile=0;
        
        int startTileY=0;
        
        if(!top){
        	//start from last tile
        	startTileY=verTiles-1;
        }
        int countTilesAnalyzed=0;
        int countFullTilesAnalyzed=0;
        
        for (int colIdx = 0; colIdx < horTiles; colIdx++) {
        	
        	TileAnalysis previous=null;
        	boolean stop=false;
        	startYPixelTile=startTileY*sizeY;
        	
        	int iteration=0;
        	for (int rowIdx = startTileY; iteration < numTilesMargin&&!stop;iteration++ ) {
        		//entro se sono sulla prima colonna oppure se previous!=null
        		if(rowIdx==startTileY||previous!=null){
        			//check if we are on the last tile to correct the size
        			int sx=sizeX;
        			if(colIdx==horTiles-1){
        				sx=sizeX+correctionXForLastTile;
        			}
        			int sy=sizeY;
        			if(rowIdx==verTiles-1){
        				sy=sizeY+correctionYForLastTile;
        			}
        			boolean isOnLand=checkIfTileIsOnLand(startXPixelTile,startYPixelTile,startXPixelTile+sx,startYPixelTile+sy);
        			if(!isOnLand){
        			
	        			TileAnalysis result=getAnalysisTile(rowIdx, colIdx);
	        			//if is not already analyzed or if is not completely on the black margin
	        			if(result==null||result.bIsBorder==false){
	        				if(result==null)
	        					result=new TileAnalysis();
	        					analyzeTile(startXPixelTile,startYPixelTile,false,top,sx,sy,result);
	        					putAnalysisTile(rowIdx,colIdx,result);
	        					countTilesAnalyzed++;
	        					if(result.fullAnalysis)countFullTilesAnalyzed++;
	        			}
	            	
		            	if(result.bIsBorder){
	            			previous=result;
	            		}else{
	            			stop=true;
	            		}
        			}else{
        				stop=true;
        				logger.debug("Tile is On Land  X:"+startXPixelTile+"  Y:"+startYPixelTile);
        			}	
        		}
        		if(top){
        			rowIdx++;
        			startYPixelTile=startYPixelTile+sizeY;
        		}else{
        			rowIdx--;
        			if(colIdx==verTiles-1){
        				startYPixelTile=startYPixelTile-sizeY-correctionYForLastTile;
        			}
        			startYPixelTile=startYPixelTile-sizeY;
        		}
        	}
        	startXPixelTile=startXPixelTile+sizeX;
        }
        logger.debug("Tiles Analized V:"+countTilesAnalyzed + "Full Analyzed:"+countFullTilesAnalyzed);
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
	private int[] analyzeTile(int iniX,int iniY,boolean horizontal,boolean normalDirection){
		//correctionXForLastTile=gir.getWidth()-sizeX*horTiles;
		TileAnalysis tile=new TileAnalysis();
		analyzeTile(iniX, iniY,horizontal, normalDirection, sizeX+correctionXForLastTile, sizeY,tile);
		
		return tile.horizLeftCutOffArray;
	}
	
	
	/**
	 * 
	 * @param lineMatrix
	 * @param numberOfCols
	 * @param numberOfRows
	 * @return
	 */
	public int[] traspone(int lineMatrix[],int numberOfCols,int numberOfRows){
	    int matrix[][]=new int[numberOfRows][numberOfCols];
	    
	    // da array a matrice
	    int col=0;
	    int row=0;
	    for(int id=0;id<lineMatrix.length&&row<numberOfRows;id++){
	    	int value = lineMatrix[id];
	    	
	    	matrix[row][col]= value;
	    	
	    	if(id%(numberOfCols-1)==0){
	    		row++;
	    		col=0;
	    	}
	    	col++;
	    	
	    	//System.out.println("row:"+row+"  col:"+col);
	    }	
	    //trasposta della matrice
		int[][] trasposta = new int[numberOfCols][numberOfRows];
        for (int i = 0; i < numberOfRows; i++)
            for (int j = 0; j < numberOfCols; j++)
            	trasposta[j][i] = matrix[i][j];
        
        //serializzo la matrice
        int traspostaInline[]=new int[lineMatrix.length];
        int x=0;
        for(col=0;col<numberOfCols;col++){
          for(row=0;row<numberOfRows;row++){
        	  traspostaInline[x]=trasposta[col][row];
	    	x++;	
	    	//System.out.println("x:"+x);
          }	
        }
        
        
		return traspostaInline;
	} 
	
	/**
	 * 
	 * @param iniX	
	 * @param iniY
	 * @param horizontalAnalysis   if true the tile is analyzed horizontally
	 * @param normalDirection if true= mean from left to right if horizontalAnalysis==true else from top to bottom 
	 * @param tileWidth real tile width 
	 * @param tileHeight real tile height
	 * 
	 * @return
	 */
	private TileAnalysis analyzeTile(int iniX,int iniY,boolean horizontalAnalysis,boolean normalDirection,int tileWidth,int tileHeight,TileAnalysis result){
		//TileAnalysis result=new TileAnalysis();
		int vCutOffSize=0;
		
		//array che contine un boolean per ogni riga analizzata se true=la linea contiene un'area di transizione da un "black border" ad un'area valida
		boolean bBtoWBorderV[]=new boolean[3];
		
		int[][] dataRow=new int[nRowSamples][];
		
		for(int idxSamples=0;idxSamples<nRowSamples;idxSamples++){
			
			
			//leggo i dati di 1 riga o di 1 colonna
			if(horizontalAnalysis){
				//riga di campionamento 1 al 10% al 50% e al 90%
				int row=iniY+((ConstantVDSAnalysis.ROW_TILE_SAMPLES_ARRAY[idxSamples]*sizeY)/100);

				dataRow[idxSamples]=gir.readTile(iniX, row, tileWidth,1,bandAnalysis);
				vCutOffSize=tileHeight;
			}else{
				//riga di campionamento 1 al 10% al 50% e al 90%
				int col=iniX+((ConstantVDSAnalysis.ROW_TILE_SAMPLES_ARRAY[idxSamples]*sizeX)/100);
				dataRow[idxSamples]=gir.readTile(col, iniY, 1,tileHeight,bandAnalysis);
				vCutOffSize=tileWidth;
			}	
				
			if(!normalDirection){
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
		boolean bIsBorder=false;
		
		if(bBtoWBorder==false){
			bIsBorderV[0]=dAvEtreme2[0]<ConstantVDSAnalysis.THRESH_IS_BORDER&&dAvEtreme1[0]<ConstantVDSAnalysis.THRESH_IS_BORDER;
			bIsBorderV[1]=dAvEtreme2[1]<ConstantVDSAnalysis.THRESH_IS_BORDER&&dAvEtreme1[1]<ConstantVDSAnalysis.THRESH_IS_BORDER;
			bIsBorderV[2]=dAvEtreme2[2]<ConstantVDSAnalysis.THRESH_IS_BORDER&&dAvEtreme1[2]<ConstantVDSAnalysis.THRESH_IS_BORDER;
			
			//is true if all elements in BIsBorderV are true
			bIsBorder=bIsBorderV[0]&&bIsBorderV[1]&&bIsBorderV[2];
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
						if(normalDirection)
							viCutOffColTemp[arraySamplesId]=idCutOffElement;
						else
							viCutOffColTemp[arraySamplesId]=tileWidth-idCutOffElement;
						stop=true;	
					}
				}
			}
			
			//if the difference is too high we calculate the Cutoffpoint for each row
			if(NumberUtils.max(viCutOffColTemp)-NumberUtils.min(viCutOffColTemp)>3){
				result.fullAnalysis=true;
				
				//read complete tile
				int dataTile[]=gir.readTile(iniX, iniY, tileWidth,tileHeight,bandAnalysis);
				viCutOffColTemp=new int[vCutOffSize];

				if(!horizontalAnalysis){
					dataTile=traspone(dataTile, tileWidth,tileHeight);
				}
				
				//loop on each element of the data readed if stop is false
				for(int arraySamplesId=0;arraySamplesId<vCutOffSize;arraySamplesId++){
					try{
						//calcolo la posizione sul vettore come se fosse una matrice
						int posStart=arraySamplesId*tileWidth;
						//leggo la riga del tile (il tile e' mem come array)
						int[] singleRow=ArrayUtils.subarray(dataTile,posStart,posStart+tileWidth);
						
						if(!normalDirection){
							ArrayUtils.reverse(singleRow);
						}
						
						if(singleRow.length>0){
							int maxValue=NumberUtils.max(singleRow);
							double thres=maxValue*ConstantVDSAnalysis.THRESH_MAX_FACTOR;
							
							boolean stop=false;
							
							//loop on row elements
							for(int idCutOffElement=0;idCutOffElement<singleRow.length&&!stop;idCutOffElement++){
								
								if((singleRow[idCutOffElement]>=ConstantVDSAnalysis.THRESH_VALUE_SAFE||
										(singleRow[idCutOffElement]>thres&&singleRow[idCutOffElement]>=ConstantVDSAnalysis.THRESH_IS_BORDER))||
										(idCutOffElement==singleRow.length)){
										if(normalDirection){
											viCutOffColTemp[arraySamplesId]=idCutOffElement;
										}else{
											viCutOffColTemp[arraySamplesId]=tileWidth-idCutOffElement;
										}	
										stop=true;
								}
							}
							if(stop==false){
								if(normalDirection)
									viCutOffColTemp[arraySamplesId]=singleRow.length;
								else
									viCutOffColTemp[arraySamplesId]=0;
							}
						}	
					}catch(Exception e ){
						logger.error(e.getMessage());
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
			if(bIsBorder){
				//viCutOffColTemp=null;
				if(normalDirection){
					viCutOffColTemp=new int[vCutOffSize];
					Arrays.fill(viCutOffColTemp, tileWidth);
				}else{
					viCutOffColTemp=new int[vCutOffSize];
					Arrays.fill(viCutOffColTemp, 0);
				}	
			}else{
				viCutOffColTemp=new int[vCutOffSize];
				if(normalDirection)
					Arrays.fill(viCutOffColTemp, 0);
				else
					Arrays.fill(viCutOffColTemp, tileWidth-1);
			}
		}
		/*if(viCutOffColTemp!=null)
			for(int i=0;i<viCutOffColTemp.length;i++){
				System.out.println(viCutOffColTemp[i]);
			}*/
		
		if(horizontalAnalysis){
			if(normalDirection){
				result.horizLeftCutOffArray=viCutOffColTemp;
			}else{
				result.horizRightCutOffArray=viCutOffColTemp;
			}	
		}else{
			if(normalDirection){
				result.verTopCutOffArray=viCutOffColTemp;
			}else{
				result.verBottomOffArray=viCutOffColTemp;
			}
		}
		result.bIsBorder=bIsBorder;
		String direction=normalDirection==true?"L":"R";
		if(logger.isDebugEnabled()){
			logger.debug("X:"+iniX+"  Y:"+iniY+"  bIsBorder:"+bIsBorder+"  horizontalAnalysis:"+horizontalAnalysis+"  Direction:"+direction);
			logger.debug(Arrays.toString(viCutOffColTemp));
		}
		
		return result;
	}
	
	
}
