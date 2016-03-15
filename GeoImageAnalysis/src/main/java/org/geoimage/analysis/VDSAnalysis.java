/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.analysis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.util.Precision;
import org.geoimage.analysis.BlackBorderAnalysis.TileAnalysis;
import org.geoimage.analysis.BoatConnectedPixelMap.ConPixel;
import org.geoimage.def.SarImageReader;
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
    private MaskGeometries coastMask;
    private MaskGeometries iceMask;
 //   private int tileSize;
    private int verTilesImage=0;
    private int horTilesImage=0;
    private int realSizeX=0;
	private int realSizeY=0;

	private final double MIN_TRESH_FOR_ANALYSIS=0.7;

    private List<ProgressListener>progressListener=null;
    private Logger logger= LoggerFactory.getLogger(VDSAnalysis.class);
    private BlackBorderAnalysis blackBorderAnalysis;

    private boolean analyseSingleTile=false;
    private int xTileToAnalyze=0;
    private int yTileToAnalyze=0;

	public VDSAnalysis(SarImageReader gir, MaskGeometries coastMask,
			MaskGeometries iceMask, float enlf, Float[] trhreshold
			,int realSizeTileX,int realSizeTileY,int horTilesImage,int verTilesImage ) {

		HashMap<String,Float>thresholdsBandParams=new HashMap<>();
		thresholdsBandParams.put("HH",trhreshold[0]);
		thresholdsBandParams.put("HV",trhreshold[1]);
		thresholdsBandParams.put("VH",trhreshold[2]);
		thresholdsBandParams.put("VV",trhreshold[3]);
		this.thresholdsBandParams=thresholdsBandParams;
        this.gir = gir;
        this.coastMask = coastMask;
        this.iceMask=iceMask;
        this.realSizeX=realSizeTileX;
        this.realSizeY=realSizeTileY;
        this.horTilesImage=horTilesImage;
        this.verTilesImage=verTilesImage;



        init(enlf);
	}
	/**
	 *
	 * @param gir
	 * @param mask
	 * @param enlf
	 * @param trhresholdMap
	 */
    public VDSAnalysis(SarImageReader gir, MaskGeometries mask,
    		MaskGeometries iceMask, float enlf, Map<String, Float> trhresholdMap,int realSizeTileX,int realSizeTileY
    		,int horTilesImage,int verTilesImage ) {

    	this.thresholdsBandParams=trhresholdMap;
        this.gir = gir;
        this.coastMask = mask;
        this.iceMask=iceMask;
        this.realSizeX=realSizeTileX;
        this.realSizeY=realSizeTileY;
        this.horTilesImage=horTilesImage;
        this.verTilesImage=verTilesImage;

        init(enlf);
    }

    private void init(float enlf ){
    	this.enl = "" + (int) (enlf * 10);
        if (this.enl.length() == 2) {
            this.enl = "0" + this.enl;
        }
        progressListener=new ArrayList<ProgressListener>();
    }


	public int getVerTilesImage() {
		return verTilesImage;
	}
	public int getHorTilesImage() {
		return horTilesImage;
	}
	public boolean isAnalyseSingleTile() {
		return analyseSingleTile;
	}
	public void setAnalyseSingleTile(boolean analyseSingleTile) {
		this.analyseSingleTile = analyseSingleTile;
	}
	public int getxTileToAnalyze() {
		return xTileToAnalyze;
	}
	public void setxTileToAnalyze(int xTileToAnalyze) {
		this.xTileToAnalyze = xTileToAnalyze;
	}
	public int getyTileToAnalyze() {
		return yTileToAnalyze;
	}
	public void setyTileToAnalyze(int yTileToAnalyze) {
		this.yTileToAnalyze = yTileToAnalyze;
	}

	 public MaskGeometries getCoastMask() {
		return coastMask;
	}
	public void setCoastMask(MaskGeometries coastMask) {
		this.coastMask = coastMask;
	}
	public MaskGeometries getIceMask() {
		return iceMask;
	}
	public void setIceMask(MaskGeometries iceMask) {
		this.iceMask = iceMask;
	}
	public BlackBorderAnalysis getBlackBorderAnalysis() {
			return blackBorderAnalysis;
	}

	public void setBlackBorderAnalysis(BlackBorderAnalysis blackBorderAnalysis) {
		this.blackBorderAnalysis = blackBorderAnalysis;
	}

	public int getHorTiles() {
		return horTilesImage;
	}
	public void setHorTiles(int horTiles) {
		this.horTilesImage = horTiles;
	}

	 public int getVerTiles() {
			return verTilesImage;
		}

		public void setVerTiles(int verTiles) {
			this.verTilesImage = verTiles;
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
    	return new Float[]{thresholdsBandParams.get("HH"),
    			thresholdsBandParams.get("HV"),
    			thresholdsBandParams.get("VH"),
    			thresholdsBandParams.get("VV")};
    }

    /**
     *
     * @param polarization
     * @return
     */
    public Float getThresholdParam(final String polarization){
    	return thresholdsBandParams.get(polarization);
    }

    /**
     *
     * @param kdist
     * @param thresholdBandParams
     * @return
     * @throws IOException
     */
    public DetectedPixels analyse(final KDistributionEstimation kdist,final int band) throws IOException {

        DetectedPixels dpixels = new DetectedPixels(gir.getRangeSpacing(),gir.getAzimuthSpacing());
        String bb=((SarImageReader)gir).getBands()[band];
        float thresholdBand=this.thresholdsBandParams.get(bb);

        int xLeftTile=0;
        int xRightTile=0;
        int yTopTile=0;
        int yBottomTile=0;

        double[][][] tileStat = new double[verTilesImage][horTilesImage][5];

        int dy=0;

        for (int rowIndex = 0; rowIndex < verTilesImage; rowIndex++) {
        	if(isAnalyseSingleTile()){
        		if(rowIndex!=yTileToAnalyze)
        			continue;
        	}

            notifyStartNextRowProcessing(rowIndex);

        	if(rowIndex==verTilesImage-1){
            	//the last tiles have more pixels so we need to calculate the real size
            	dy=gir.getHeight()-((verTilesImage-1)*realSizeY)-realSizeY;
            }

            xLeftTile = 0;
            xRightTile = 0;//gir.getWidth();
            yTopTile = rowIndex * realSizeY;
            yBottomTile = yTopTile + realSizeY+dy; //dx is always 0 except on the last tile

            int dx=0;

            for (int colIndex = 0; colIndex < horTilesImage; colIndex++) {
            	if(isAnalyseSingleTile()){
            		if(colIndex!=xTileToAnalyze)
            			continue;
            	}
            	if(colIndex==horTilesImage-1){
            		//the last tiles have more pixels so we need to calculate the real size
                	dx=(gir.getWidth()-((horTilesImage-1)*realSizeX))-realSizeX;
                }

                xLeftTile = colIndex * realSizeX;   //x start tile
                xRightTile = xLeftTile + realSizeX+dx; //dx is always 0 except on the last tile

                boolean containsMinPixelValid=false;

                int[] maskdata =null;
                if (coastMask == null || !intersects(xLeftTile,xRightTile,yTopTile,yBottomTile)) {
                	maskdata =null;
                }else{
                	// compute different statistics if the tile intersects the land mask
                    // check if there is sea pixels in the tile area
                    if(includes(xLeftTile,xRightTile,yTopTile,yBottomTile))
                        continue;


                    maskdata=createDataMask(xLeftTile, yTopTile, realSizeX, realSizeY, dx, dy);
                    //count invalid pixel (land)
                    int inValidPixelCount = 0;
                    for(int count = 0; count < maskdata.length; count++)
                        inValidPixelCount += maskdata[count];
                    //double oldInValidPixelCount=inValidPixelCount;
                    containsMinPixelValid=((double)inValidPixelCount / maskdata.length) <= MIN_TRESH_FOR_ANALYSIS;

                    if(!containsMinPixelValid){
                    	maskdata=createDataMask(xLeftTile-30, yTopTile-30, realSizeX+30, realSizeY+30, dx, dy);
                        //count invalid pixel (land)
                        inValidPixelCount = 0;
                        for(int count = 0; count < maskdata.length; count++)
                            inValidPixelCount += maskdata[count];

                        containsMinPixelValid=((double)inValidPixelCount / maskdata.length) <= MIN_TRESH_FOR_ANALYSIS;
                    }
                }
                //check if we have the min pixels avalaible for the analysis else we try to "enlarge" the tile
                if(containsMinPixelValid||maskdata==null){
                    // if there are pixels to estimate, calculate statistics using the mask
                	TileAnalysis bbAnalysis=null;

                	//check if it is avalaible the bb analysis for this tile
            		if(blackBorderAnalysis!=null)bbAnalysis=blackBorderAnalysis.getAnalysisTile(rowIndex,colIndex);

                    int[] data = gir.readTile(xLeftTile, yTopTile, realSizeX+dx, realSizeY+dy,band);
                    kdist.setImageData(xLeftTile, yTopTile,realSizeX+dx, realSizeY+dy,band,bbAnalysis);
                    kdist.estimate(maskdata,data);

                    double[] thresh = kdist.getDetectThresh();
                    tileStat[rowIndex][0] = kdist.getTileStat();

                    double threshWindowsVals[]=AnalysisUtil.calcThreshWindowVals(thresholdBand, thresh);

                    for (int k = 0; k < (realSizeY+dy); k++) {
                        for (int h = 0; h < (realSizeX+dx); h++) {
                            // check pixel is in the sea
                            if(maskdata==null||(maskdata[h*k]==0)){//rastermask.getSample(h, k, 0) == 0)){
                                int subwindow = 1;
                                if (h < (realSizeX+dx) / 2) {
                                    if (k < (realSizeY+dy) / 2) {
                                        subwindow = 1;
                                    } else {
                                        subwindow = 3;
                                    }
                                } else {
                                    if (k < (realSizeY +dy)/ 2) {
                                        subwindow = 2;
                                    } else {
                                        subwindow = 4;
                                    }
                                }
                                int pix = data[k * (realSizeX+dx) + h];
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
            System.out.println(rowIndex + "/" + verTilesImage);
        }
        System.out.println("Detected Pixels Total :"+dpixels.getAllDetectedPixels().size());
        return dpixels;
    }


    /**
     *
     * @param xLeftTile
     * @param yTopTile
     * @param realSizeX
     * @param realSizeY
     * @param dx
     * @param dy
     * @return
     */
    public int[] createDataMask(int xLeftTile, int yTopTile, int realSizeX, int realSizeY,int dx,int dy){
    	int[] maskdata =null;
    	int size=0;
    	if(coastMask!=null){
	    	// create raster mask //dx and dy are for tile on the border that have different dimensions
	        //Read pixels for the area and check there are enough sea pixels
	        maskdata=coastMask.getRasterDataMask(xLeftTile, yTopTile, realSizeX+dx, realSizeY+dy, -xLeftTile, -yTopTile, 1.0);
	        size=maskdata.length;
    	}
        int[] iceMaskdata = null;
        if(iceMask!=null){
        	//Read pixels for ice
        	iceMaskdata=iceMask.getRasterDataMask(xLeftTile, yTopTile, realSizeX+dx, realSizeY+dy, -xLeftTile, -yTopTile, 1.0);
        	size=maskdata.length;
        }

        if(iceMaskdata==null)
        	return maskdata;
        if(maskdata==null)
        	return iceMaskdata;

        //merge the maskdata
        for(int count = 0; count < size; count++){
            //if the pixel is valid check if this pixel is ice
            if(maskdata[count]==0){
            	if(iceMaskdata[count]==1){
            		maskdata[count]=1;
            	}
            }
        }
        return maskdata;
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
    public Boat[] agglomerateNeighbours(DetectedPixels detPixels,
    		double distance,
    		int tilesize,
    		boolean removelandconnectedpixels,
    		MaskGeometries mask,
    		KDistributionEstimation kdist,
    		String polarization,
    		int... bands)throws IOException {

    	aggregate(detPixels,(int) distance, tilesize, removelandconnectedpixels, bands, mask, kdist);
        // generate statistics and values for boats
        return computeBoatsAttributesAndStatistics(detPixels.listboatneighbours,polarization);
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
    private void aggregate(DetectedPixels detPixels,int neighboursdistance, int tilesize, boolean removelandconnectedpixels, int[] bands, MaskGeometries mask, KDistributionEstimation kdist)throws IOException {
        int id = 0;
        // scan through list of detected pixels
        DetectedPixels.BoatPixel pixels[]=detPixels.getAllDetectedPixelsValues().toArray(new DetectedPixels.BoatPixel[0]);
        int count=0;

        //loop on all detected pixel
        for (DetectedPixels.BoatPixel detectedPix: pixels) {
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

            for (int bandcounter = 0; bandcounter < numberbands; bandcounter++) {
           		data[bandcounter] = gir.read(cornerx, cornery, tilesize, tilesize,bands[bandcounter]);
            }

            // calculate thresholds
            int row=(cornery+1)/this.verTilesImage;
        	int col=(cornerx+1)/this.horTilesImage;

        	TileAnalysis ta=null;
        	if(this.blackBorderAnalysis!=null)
        		ta=this.blackBorderAnalysis.getAnalysisTile(row, col);

        	double[][] statistics = AnalysisUtil.calculateImagemapStatistics(cornerx, cornery, tilesize, tilesize,row,col, bands, data, kdist,ta);

            double[][] thresholdvalues = new double[numberbands][2];
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
            }

            // add pixel only if above new threshold
            if (pixelabove) {
                int[] dataMask=createDataMask(cornerx, cornery, tilesize, tilesize, 0,0);
                // add pixel to the list
                BoatConnectedPixelMap boatpixel = null;
                try{
                	boatpixel = new BoatConnectedPixelMap(cornerx, cornery,xx, yy, id++, data[0][boatx + boaty * tilesize]);
                }catch(Exception e){
                	boatpixel = new BoatConnectedPixelMap(cornerx, cornery,xx, yy, id++, data[0][(boatx + boaty * tilesize)-1]);
                }

                for(int iBand=0;iBand<numberbands;iBand++){
                	//String bb=((SarImageReader)gir).getBands()[iBand];
                	String bb=gir.getBandName(bands[iBand]);
                    float thresholdBand=this.thresholdsBandParams.get(bb);

                    TileAnalysis bbAnalysis=null;
            		if(blackBorderAnalysis!=null)bbAnalysis=blackBorderAnalysis.getAnalysisTile(row,col);

                	kdist.setImageData(cornerx, cornery, tilesize, tilesize, iBand,bbAnalysis);
                	int[] newdata = gir.read(cornerx, cornery, tilesize, tilesize,bands[iBand]);
                	kdist.estimate(newdata, newdata);

                	double[] treshTile=kdist.getDetectThresh();
                	double threshTotal=treshTile[0]+treshTile[1]+treshTile[2]+treshTile[3];

                	double threshWindowsVals[]=AnalysisUtil.calcThreshWindowVals(thresholdBand, treshTile);

                	double tileAvg=0;
                	int i=0;
                	int y=-1;
                	for(;i<data[iBand].length;i++){
                		int x=i%200; //??? //TODO check this value , why it
                		if(x==0)
                			y++;

                		try{
                			if(dataMask==null||dataMask[x*y]==0){//.getSample(x, y, 0)==0){
                				tileAvg=tileAvg+data[iBand][i];
                			}
                		}catch(Exception e ){
                			System.out.println("X:"+x+"Y:"+y);
                		}
                	}
                	tileAvg=tileAvg/i;

                	double tileStdDev=treshTile[0] * threshTotal / treshTile[5];
                	boatpixel.getStatMap().setTreshold(Precision.round((threshWindowsVals[0]+threshWindowsVals[1]+threshWindowsVals[2]+threshWindowsVals[3])/4,3),bb);
                	boatpixel.getStatMap().setTileStd(Precision.round(tileStdDev,3),bb);
                	boatpixel.getStatMap().setTileAvg(Precision.round(tileAvg,3),bb);
                }
                detPixels.listboatneighbours.add(boatpixel);

                // start list of aggregated pixels
                List<int[]> boataggregatedpixels = new ArrayList<int[]>();


                boolean result = detPixels.checkNeighbours(boataggregatedpixels, data,
                		thresholdvalues, new int[]{boatx, boaty},
                		neighboursdistance, tilesize, dataMask);
                // set flag for touching land
                boatpixel.setTouchesLandMask(result);

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
    }

    /**
     * AG inserted a test to filter boats by length
     * @param list
     */
    protected Boat[] computeBoatsAttributesAndStatistics(List<BoatConnectedPixelMap> list,String band) {
    	 List <Boat> boatsTemp=new ArrayList<Boat>();

        // compute attributes and statistics values on boats
        for (BoatConnectedPixelMap boatPxMap : list) {
            boatPxMap.computeValues(gir.getRangeSpacing(),gir.getAzimuthSpacing());

            if(boatPxMap.getBoatlength()>ConstantVDSAnalysis.filterminSize && boatPxMap.getBoatlength()<ConstantVDSAnalysis.filtermaxSize){

            	Boat b=new Boat(boatPxMap.getId()						//id
            			,(int)boatPxMap.getBoatposition()[0]			//x
            			,(int)boatPxMap.getBoatposition()[1]			//y
            			,(int)boatPxMap.getBoatnumberofpixels()			//size
            			,(int)boatPxMap.getBoatlength()					//length
            			,(int)boatPxMap.getBoatwidth()					//width
            			,(int)boatPxMap.getBoatheading());				//heading

            	b.setStatMap(boatPxMap.getStatMap());
            	//code for single band
            	if(!band.equalsIgnoreCase("merge")){
	            	b.getStatMap().setMaxValue(boatPxMap.getMaxValue(),band);
	            	double significance=Precision.round((boatPxMap.getMaxValue()-boatPxMap.getStatMap().getTileAvg(band))/boatPxMap.getStatMap().getTileStd(band),3);
	            	b.getStatMap().setSignificance(significance,band);
	    			boatsTemp.add(b);
            	}else{
            		Collection<ConPixel> pixels=boatPxMap.getConnectedpixels();
            		List<Integer> pixelValues=null;
            		String[] bb=gir.getBands();
            		for(int i=0;i<bb.length;i++){
            			pixelValues=new ArrayList<>();
            			for(ConPixel pixel:pixels){
            				int x=pixel.x;
            				int y=pixel.y;
            				int[] val;
							try {
								val = gir.read(x, y, 1, 1,i);
								pixelValues.add(val[0]);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
            			}
            			int max=Collections.max(pixelValues);
            			b.getStatMap().setMaxValue(max,bb[i]);
                    	double significance=Precision.round((max-b.getStatMap().getTileAvg(bb[i]))/b.getStatMap().getTileStd(bb[i]),3);
                    	b.getStatMap().setSignificance( significance,bb[i]);
            		}
            		boatsTemp.add(b);
            	}
            }
        }
        boatsTemp=sortBoats(boatsTemp);
        Boat[] boatArray=boatsTemp.toArray(new Boat[0]);
        return boatArray;
    }
    /**
     *
     * @param boats
     * @return
     */
    private List<Boat> sortBoats(List <Boat> boats){
    	ArrayList<Boat> sorted=new ArrayList<>();
    	// sort the boats array by positions to facilitate navigation
        HashMap<Integer, List<Boat>> hashtableBoat = new HashMap<Integer, List<Boat>>();
        // list for keys
        List<Integer> keys = new ArrayList<Integer>();

        // sort the list by position
        for (Boat boat : boats) {
        	Integer posyKey=new Integer((int) (boat.getPosy() / 512));

        	// if entry does not exist in the hashtable create it
            if (hashtableBoat.get(posyKey) == null) {
                hashtableBoat.put(posyKey, new ArrayList<Boat>());
                keys.add((int) posyKey);
            }
            // sort boat positions by stripes of 512
            hashtableBoat.get(posyKey).add(boat);
        }

        // for each stripe sort the boats by columns within each stripe
        Collections.sort(keys);
        for (Integer key : keys) {
            // get the boats in the stripe
            List<Boat> boatsinStripe = hashtableBoat.get(key);
            if (boatsinStripe != null) {
                for (int i = 1; i < boatsinStripe.size(); i++) {
                    double positionX = boatsinStripe.get(i).getPosx();
                    for (int j = 0; j < i; j++) {
                        if (positionX < boatsinStripe.get(j).getPosy()) {
                            // insert element at the right position
                            boatsinStripe.add(j, boatsinStripe.get(i));
                            // remove element from its previous position increased by one position
                            boatsinStripe.remove(i + 1);
                            break;
                        }
                    }
                }
            }

            int id = 0;

            // add the sorted boats to the table
            for (Boat boat : boatsinStripe) {
                // update the ID
                boat.setId(id++);
                sorted.add(boat);
            }
        }
        return sorted;
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
        if (coastMask == null&&iceMask==null) {
            return false;
        }
        if ((coastMask!=null && coastMask.intersects(xLeftTile, yTopTile, xRightTile - xLeftTile, yBottomTile - yTopTile))||
        		(iceMask!=null&&iceMask.intersects(xLeftTile, yTopTile, xRightTile - xLeftTile, yBottomTile - yTopTile))) {
            return true;
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
         if (coastMask == null&&iceMask==null) {
            return false;
        }

        if ((coastMask!=null && coastMask.includes(xLeftTile, yTopTile, xRightTile - xLeftTile, yBottomTile - yTopTile))||
        		(iceMask!=null&&iceMask.includes(xLeftTile, yTopTile, xRightTile - xLeftTile, yBottomTile - yTopTile))) {
            return true;
        }

        return false;
    }


    public void dispose(){
    	try {
			if(blackBorderAnalysis!=null){
	    		blackBorderAnalysis=null;
	    	}
	    	if(this.progressListener.size()>0){
	    		this.progressListener.clear();
	    		this.progressListener=null;
	    	}
	    	if(this.pixels!=null)
	    		this.pixels=null;
	    	if(this.gir!=null)
	    		gir=null;
		} catch (Throwable e) {
			logger.error(e.getMessage());
		}
    }
}
