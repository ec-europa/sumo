package org.geoimage.analysis;

import java.util.HashMap;
import java.util.Map;

import org.geoimage.analysis.Boat;
import org.geoimage.def.SarImageReader;
import org.geoimage.impl.s1.Sentinel1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class S1ArtefactsAmbiguity extends Ambiguity{
    private Logger logger= LoggerFactory.getLogger(S1ArtefactsAmbiguity.class);

	private static Map<String, Integer> DISTANCE_LOOKUP_TABLE_AMBIGUITY=new HashMap<String, Integer>(); 
	
	static{
		//key = IW + N=swath  +ipf version
		DISTANCE_LOOKUP_TABLE_AMBIGUITY.put("IW1_236", 3817); //Swath 1 IPF=236
		DISTANCE_LOOKUP_TABLE_AMBIGUITY.put("IW2_236", 4310);
		DISTANCE_LOOKUP_TABLE_AMBIGUITY.put("IW3_236", 4162);
		
		DISTANCE_LOOKUP_TABLE_AMBIGUITY.put("IW1_243", 2417); //IPF=243
		DISTANCE_LOOKUP_TABLE_AMBIGUITY.put("IW2_243", 2951);
		DISTANCE_LOOKUP_TABLE_AMBIGUITY.put("IW3_243", 2624);
		
		DISTANCE_LOOKUP_TABLE_AMBIGUITY.put("IW1_245", 2417); //IPF=245
		DISTANCE_LOOKUP_TABLE_AMBIGUITY.put("IW2_245", 2951);
		DISTANCE_LOOKUP_TABLE_AMBIGUITY.put("IW3_245", 2624);
		
		DISTANCE_LOOKUP_TABLE_AMBIGUITY.put("IW1_252", 2417); //IPF=252
		DISTANCE_LOOKUP_TABLE_AMBIGUITY.put("IW2_252", 2951);
		DISTANCE_LOOKUP_TABLE_AMBIGUITY.put("IW3_252", 2624);
		
		DISTANCE_LOOKUP_TABLE_AMBIGUITY.put("IW1_253", 2417); //IPF=253
		DISTANCE_LOOKUP_TABLE_AMBIGUITY.put("IW2_253", 2951);
		DISTANCE_LOOKUP_TABLE_AMBIGUITY.put("IW3_253", 2624);

	}
	
	public S1ArtefactsAmbiguity(Boat[] boatList, SarImageReader image,int band) {
    	super(boatList,image,band);
    }
    
    public S1ArtefactsAmbiguity(Boat[]boatList, SarImageReader image, int windowSize, int numSteps,int band) {
    	super(boatList,image,windowSize,numSteps,band);
    }
    

	@Override
	protected void process() {
	    int deltaAzimuth=0;
	    Sentinel1 s1=(Sentinel1)sumoImage;
        try{
	        // scan through the list of boats
	        for (int i = 0; i < boatArray.length; i++) {
	            Boat myBoat = boatArray[i];
	            if(!myBoat.isAmbiguity()){
	            
		            int xPos = (int) myBoat.getPosx();
		            int yPos = (int) myBoat.getPosy();
		            
		            //is already in pixel
		            int ipfVersion =s1.getIpfVersion();
		            if(ipfVersion!=-1){
			            String swath=s1.getSwathName(xPos, yPos);
			            swath=swath.replace("_","");
			            Object o=DISTANCE_LOOKUP_TABLE_AMBIGUITY.get(swath+"_"+ipfVersion);
			            if(o==null){
			            	//default value for the moment
			            	deltaAzimuth=DISTANCE_LOOKUP_TABLE_AMBIGUITY.get("IW1_243");
			            }else{
			            	deltaAzimuth=(int)o;
			            }
			            
			            double[] pixSize=sumoImage.getPixelsize();
			            deltaAzimuth=(int)(deltaAzimuth/pixSize[1]);
			            if(isAmbiguity(myBoat,xPos, yPos, deltaAzimuth,pixSize[0] ,pixSize[1])){
			            	ambiguityboatlist.add(myBoat);
			            	myBoat.setAmbiguity(true);
			            	myBoat.setAmbiguityType(Boat.AMBIGUITY_TYPE_ARTEFACTS);
			            	//second check to dAzimuth*2
			            }else if(isAmbiguity(myBoat,xPos, yPos, (deltaAzimuth*2),pixSize[0] ,pixSize[1])){
			            	ambiguityboatlist.add(myBoat);
			            	myBoat.setAmbiguity(true);
			            	myBoat.setAmbiguityType(Boat.AMBIGUITY_TYPE_ARTEFACTS);
			            }else{
			            	myBoat.setAmbiguity(false);
			            }  
		            }   
	            }     
	        }
        }catch(Exception e){
        	logger.error("Error processing Azimuth Ambiguity",e);
        }
		
	}
	
	
}
