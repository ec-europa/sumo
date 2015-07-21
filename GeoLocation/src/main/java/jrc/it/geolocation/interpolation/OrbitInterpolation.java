package jrc.it.geolocation.interpolation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.util.FastMath;
import org.slf4j.LoggerFactory;

import jrc.it.geolocation.exception.MathException;
import jrc.it.geolocation.metadata.IMetadata;
import jrc.it.geolocation.metadata.S1Metadata;
 



public class OrbitInterpolation {
	// points (Beaulne2005 suggests nOrbPointsInterp=4)
	private final int N_ORB_POINT_INTERP=4;
	private double zeroDopplerTimeFirstRef=0;
	private double zeroDopplerTimeLastRef=0;
	List<double[]> statevVecInterp;
	List<double[]> statepVecInterp;
	List<Double> timeStampInterp;
	double[] secondsDiffFromRefTime;
	double iSafetyBufferAz=0;
	
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(OrbitInterpolation.class);

	public void orbitInterpolation(List<S1Metadata.OrbitStatePosVelox> vpList,
			double zeroDopplerTimeFirstLineSeconds,
			double zeroDopplerTimeLastLineSeconds,
			double samplingf,double iSafetyBufferAz) throws MathException{
			
		this.iSafetyBufferAz=iSafetyBufferAz;
		
		double deltaT = 1/samplingf;
		double deltaTinv = 1/deltaT;
		int nPoints=vpList.size();
		
		double reftime=0;
		double firstTime=vpList.get(0).time;
		double lastTime=vpList.get(vpList.size()-1).time;
		
		// set the reference time
		if(firstTime<(lastTime)){
			 reftime=firstTime;
		}else{
			 reftime=lastTime;
		}
		
		// Refer all the times to the same "origin" (refTime)
		int idx=0;
		
		//differences in seconds between the ref time and the positions time
		secondsDiffFromRefTime=new double[vpList.size()]; // in matlab code is timeStampInitSecondsRef
		for(S1Metadata.OrbitStatePosVelox vp:vpList){
			secondsDiffFromRefTime[idx]= (vp.time-reftime);
			idx++;
		}
		
		
		
		 // Modifiche di Carlos del 20150703 //
		//zeroDopplerTimeFirstRef=(zeroDopplerTimeFirstLineSeconds-reftime)-iSafetyBufferAz*deltaT;
		//zeroDopplerTimeLastRef=(zeroDopplerTimeLastLineSeconds-reftime)+iSafetyBufferAz*deltaT;
		//
		zeroDopplerTimeFirstRef=(zeroDopplerTimeFirstLineSeconds-reftime);
		zeroDopplerTimeLastRef=(zeroDopplerTimeLastLineSeconds-reftime);

		double minT=0;
		double maxT=0;
		
		double zeroDopplerTimeFirstLineWSafetyBufSecondsRef=0;
		double zeroDopplerTimeLastLineWSafetyBufSecondsRef=0;
		if(zeroDopplerTimeFirstRef<zeroDopplerTimeLastRef){
			zeroDopplerTimeFirstLineWSafetyBufSecondsRef = zeroDopplerTimeFirstRef - iSafetyBufferAz * deltaT; //20150703
	        zeroDopplerTimeLastLineWSafetyBufSecondsRef = zeroDopplerTimeLastRef + iSafetyBufferAz * deltaT;   //20150703
	        
	        minT = FastMath.min(0, zeroDopplerTimeFirstLineWSafetyBufSecondsRef); //20150703
	        maxT = FastMath.max(secondsDiffFromRefTime[secondsDiffFromRefTime.length-1], zeroDopplerTimeLastLineWSafetyBufSecondsRef); //20150703
		}else{
			zeroDopplerTimeFirstLineWSafetyBufSecondsRef = zeroDopplerTimeFirstRef + iSafetyBufferAz * deltaT; //20150703
	        zeroDopplerTimeLastLineWSafetyBufSecondsRef = zeroDopplerTimeLastRef - iSafetyBufferAz * deltaT; //20150703
	        
			if(firstTime<(lastTime)){
				minT=FastMath.min(secondsDiffFromRefTime[0], zeroDopplerTimeLastLineWSafetyBufSecondsRef);
				maxT=FastMath.max(secondsDiffFromRefTime[secondsDiffFromRefTime.length-1],zeroDopplerTimeFirstLineWSafetyBufSecondsRef);
			}else{
				minT=FastMath.min(secondsDiffFromRefTime[secondsDiffFromRefTime.length-1], zeroDopplerTimeLastLineWSafetyBufSecondsRef);
				maxT=FastMath.max(secondsDiffFromRefTime[0],zeroDopplerTimeFirstLineWSafetyBufSecondsRef);
			}	
		}
		// Fine Modifiche di Carlos del 20150703 //
		
		
		int limit=((Double)((maxT-minT)/deltaT)).intValue();
		
		Double[] timeStampInterpSecondsRef=new Double[limit+1];
		double nextVal=0;
		for(int j=0;j<=limit;j++){
			timeStampInterpSecondsRef[j]=nextVal;
			nextVal=deltaT+nextVal;
		}
		
		double zeroDopplerTimeInitRef=zeroDopplerTimeFirstLineWSafetyBufSecondsRef;
		double zeroDopplerTimeEndRef=zeroDopplerTimeLastLineWSafetyBufSecondsRef;
		
		if( zeroDopplerTimeFirstLineWSafetyBufSecondsRef > zeroDopplerTimeLastLineWSafetyBufSecondsRef){
				zeroDopplerTimeInitRef = zeroDopplerTimeLastLineWSafetyBufSecondsRef;
				zeroDopplerTimeEndRef = zeroDopplerTimeFirstLineWSafetyBufSecondsRef;
		}
		
		/////////////////////////Hermite interpolation/////////////////////////////////
		//For each section of orbit (ie time interval between two points in timeStampInitSecondsRef),
		// do the interpolation using the 'nOrbPointsInterp' nearest orbit points (Beaulne2005 suggests nOrbPointsInterp=4)
		if(zeroDopplerTimeInitRef<timeStampInterpSecondsRef[1]){
			int size=Math.min(nPoints,N_ORB_POINT_INTERP);
			
			double[] subTimesDiffRef= Arrays.copyOfRange(secondsDiffFromRefTime, 0, nPoints);
			
			//Prepare the state vectors that will be used in the interpolation
			List<Double> timeStampInitSecondsRefPointsInterp=new ArrayList<Double>();
			for(int i=0;i<size;i++){
				double valMax=timeStampInterpSecondsRef[size-1];//vOrbsPoints[size-1]];
				if(timeStampInterpSecondsRef[i]<valMax){
					timeStampInitSecondsRefPointsInterp.add(timeStampInterpSecondsRef[i]);
				}
			}
			List<IMetadata.OrbitStatePosVelox>subList=vpList.subList(0,size);
			
	        //Do the interpolation only in the portion of the section that is between zeroDopplerTimeInitSecondsRef and timeStampInitSecondsRef(1)
			double initTime=zeroDopplerTimeInitRef;
			int idxInitTime =-1;
			boolean finded=false;
			double endTime = secondsDiffFromRefTime[0];
			
			int idxEndTime =-1;
			for(int i=0;i<timeStampInterpSecondsRef.length;i++){
				//search the first
				if(timeStampInterpSecondsRef[i]>=initTime&&!finded){
					idxInitTime=i;
					finded=true; //stop at the first
				}
				//search the last that verify the condition
				if(timeStampInterpSecondsRef[i]<endTime){
					idxEndTime=i;
				}
			}
			
			logger.debug("idxInitTime:"+idxInitTime+"	idxEndTime:"+idxEndTime);
			
			
			Double[] t0=(Double[])timeStampInitSecondsRefPointsInterp.toArray(new Double[0]);
			if (idxInitTime!=-1 && idxEndTime!=-1 && initTime < endTime){
				 List<double[]>vPoints=new ArrayList<double[]>();
				 List<double[]>pPoints=new ArrayList<double[]>();
				 List<Double>timeStampOutput=new ArrayList<Double>();
				 HermiteInterpolation.interpolation(
						 subTimesDiffRef,subList, t0, 
						 idxInitTime, idxEndTime, deltaTinv,
						 vPoints,pPoints,timeStampOutput);
				 
				 addPointsToResult(vPoints,pPoints);
				 
				 if(timeStampInterp==null)
					 timeStampInterp=new ArrayList<>(timeStampOutput);
				 else
					 timeStampInterp.addAll(timeStampOutput);
			}
		}
		//start from 2 because we need 2 points to start interpolation
		for(int orbPoint=1;orbPoint<nPoints;orbPoint++){
		//Interpolation is to be done in this section					//in carlos code secondsDiffFromRefTime=timeStampInitSecondsRef
		    if(secondsDiffFromRefTime[orbPoint-1]<zeroDopplerTimeEndRef||secondsDiffFromRefTime[orbPoint]>zeroDopplerTimeEndRef){
		    	
		    	//Find the orbit points that will be used for the interpolation
		    	int idxStart=0;
		    	int idxEnd=0;
	            if( N_ORB_POINT_INTERP > nPoints){
	            	idxEnd = nPoints;// in matlab array da 1-31 con step =1
	            }else{
	                idxStart=orbPoint - new Double(Math.floor(N_ORB_POINT_INTERP/2)).intValue();
	                idxEnd=idxStart + N_ORB_POINT_INTERP -1;
	                
	                //vOrbPoints = (idxStart:idxEnd);// in matlab array da idxstart a idxEnd con step =1
	                if( idxStart<1){
	                	//0-1-2-3..  N_ORB_POINT_INTERP-1
	                	idxStart=0;
	                	idxEnd=N_ORB_POINT_INTERP-1;
	                }
	                if( idxEnd>=nPoints){//27-28-29-30 
	                    idxStart=nPoints-N_ORB_POINT_INTERP;
	                	idxEnd=nPoints-1;
	                }
	            }
	            
	            //Prepare the state vectors that will be used in the interpolation
	            // prendo i valori delle differenze tra i tempi e il timeref per i punti che mi interessano indicati da vOrbitPoins
	            double[] timeStampInitSecondsRefPoints = Arrays.copyOfRange(secondsDiffFromRefTime, idxStart, idxEnd+1);//matlab -->timeStampInitSecondsRef(vOrbPoints);
	            
	            List<Double> timeStampInitSecondsRefPointsInterp = new ArrayList<Double>();

	            for(int i=0;i<timeStampInterpSecondsRef.length;i++){
	            	if(timeStampInterpSecondsRef[i]>=secondsDiffFromRefTime[idxStart]&& timeStampInterpSecondsRef[i] < secondsDiffFromRefTime[idxEnd]){
	            		timeStampInitSecondsRefPointsInterp.add(timeStampInterpSecondsRef[i]);
	            	}
	            }				
	            
	            List<S1Metadata.OrbitStatePosVelox> stateVecPoints=vpList.subList(idxStart,idxEnd+1);
	             

	            //Do the interpolation only in the portion of the section that is between zeroDopplerTimeInitSecondsRef and zeroDopplerTimeEndSecondsRef
	            double initTime = Math.max(secondsDiffFromRefTime[orbPoint-1], zeroDopplerTimeInitRef);
	            int idxInitTime=0;
	            boolean findIdxInitTime=false;
	            //cerco l'indice del primo elemento che soddisfa questa condizione--> Matlab code  //int  idxInitTime = find(timeStampInitSecondsRefPointsInterp >= initTime,1,'first');
	            for (int i=0;i<timeStampInitSecondsRefPointsInterp.size()&&!findIdxInitTime;i++){
	            	if(timeStampInitSecondsRefPointsInterp.get(i)>=initTime){
	            		idxInitTime=i;//timeStampInitSecondsRefPointsInterp.get(i);
	            		findIdxInitTime=true;
	            	}
	            }
	            
	            double endTime=Math.min(secondsDiffFromRefTime[orbPoint], zeroDopplerTimeEndRef);
	            int idxEndTime=0;
	            boolean findIdxEndTime=false;
	            //cerco l'indice dell'ultimo elemento che soddisfa questa condizione--> Matlab code  //int  idxInitTime = find(timeStampInitSecondsRefPointsInterp >= initTime,1,'first');
	            for (int i=timeStampInitSecondsRefPointsInterp.size()-1;i>=0&&!findIdxEndTime;i--){
	            	if(timeStampInitSecondsRefPointsInterp.get(i)<endTime){
	            		idxEndTime=i;//timeStampInitSecondsRefPointsInterp.get(i);
	            		findIdxEndTime=true;
	            	}
	            }
	            	            
	            double timeRef = timeStampInitSecondsRefPointsInterp.get(0);
	            Double[] timeStampInitSecondsRefPointsInterp0 =new Double[timeStampInitSecondsRefPointsInterp.size()];
	            for(int i=0;i<timeStampInitSecondsRefPointsInterp.size();i++){
	            	timeStampInitSecondsRefPointsInterp0[i]= timeStampInitSecondsRefPointsInterp.get(i)- timeRef;        
	            }		
	            
	            
	            if (findIdxEndTime && findIdxInitTime && initTime < endTime && idxInitTime < idxEndTime){
					//HermiteInterpolation hermite=new HermiteInterpolation();
					List<double[]>vPoints=new ArrayList<double[]>();
					List<double[]>pPoints=new ArrayList<double[]>();
					List<Double>timeStampOutput=new ArrayList<Double>();
					HermiteInterpolation.interpolation(timeStampInitSecondsRefPoints,stateVecPoints, 
							timeStampInitSecondsRefPointsInterp0,idxInitTime, idxEndTime, deltaTinv,
							pPoints,vPoints,timeStampOutput);
	            	addPointsToResult(vPoints,pPoints);					
					
	            	List<Double> timeStampInterpSecondsRefOutput=new ArrayList<Double>();//hermite.getTimeStampInterpSecondsRefOutput();
	            	for(int i=0;i<timeStampOutput.size();i++){
	            		timeStampInterpSecondsRefOutput.add(timeStampOutput.get(i)+timeRef);
	            	}
	            	if(timeStampInterp==null)
						 timeStampInterp=new ArrayList<>(timeStampInterpSecondsRefOutput);
					 else
						 timeStampInterp.addAll(timeStampInterpSecondsRefOutput);
	            }
		    }
		}
		
		 if( zeroDopplerTimeEndRef > secondsDiffFromRefTime[secondsDiffFromRefTime.length-1]){
	        int vOrbPointsInit = (nPoints - Math.min(nPoints,N_ORB_POINT_INTERP));
	        int vOrbPointsEnd=nPoints-1;

	        //Prepare the state vectors that will be used in the interpolation
            double[] timeStampInitSecondsRefPoints = Arrays.copyOfRange(secondsDiffFromRefTime, vOrbPointsInit, vOrbPointsEnd+1);
	        
	        List<Double> timeStampInitSecondsRefPointsInterp = new ArrayList<Double>();
            for(int i=0;i<timeStampInterpSecondsRef.length;i++){
            	if(timeStampInterpSecondsRef[i]>=secondsDiffFromRefTime[vOrbPointsInit]){
            		timeStampInitSecondsRefPointsInterp.add(timeStampInterpSecondsRef[i]);
            	}
            }				
            
            List<S1Metadata.OrbitStatePosVelox> stateVecPoints=vpList.subList(vOrbPointsInit,vOrbPointsEnd+1);
	        

	       ///////Do the interpolation only in the portion of the section that is between timeStampInitSecondsRef(vOrbPoints(end)) and zeroDopplerTimeEndSecondsRef   /////
            
	        double initTime = secondsDiffFromRefTime[vOrbPointsEnd];
	        int idxInitTime=0;
            boolean findIdxInitTime=false;
            //cerco l'indice del primo elemento che soddisfa questa condizione--> Matlab code  //int  idxInitTime = find(timeStampInitSecondsRefPointsInterp >= initTime,1,'first');
            for (int i=0;i<timeStampInitSecondsRefPointsInterp.size()&&!findIdxInitTime;i++){
            	if(timeStampInitSecondsRefPointsInterp.get(i)>=initTime){
            		idxInitTime=i;//timeStampInitSecondsRefPointsInterp.get(i);
            		findIdxInitTime=true;
            	}
            }
            
	        
            double endTime = zeroDopplerTimeEndRef;
	        int idxEndTime=0;
            boolean findIdxEndTime=false;
            //cerco l'indice dell'ultimo elemento che soddisfa questa condizione--> Matlab code  //int  idxInitTime = find(timeStampInitSecondsRefPointsInterp >= initTime,1,'first');
            for (int i=timeStampInitSecondsRefPointsInterp.size()-1;i>=0&&!findIdxEndTime;i--){
            	if(timeStampInitSecondsRefPointsInterp.get(i)<endTime){
            		idxEndTime=i;//timeStampInitSecondsRefPointsInterp.get(i);
            		findIdxEndTime=true;
            	}
            }

            double timeRef = timeStampInitSecondsRefPointsInterp.get(0);
            Double[] timeStampInitSecondsRefPointsInterp0 =new Double[timeStampInitSecondsRefPointsInterp.size()];
            for(int i=0;i<timeStampInitSecondsRefPointsInterp.size();i++){
            	timeStampInitSecondsRefPointsInterp0[i]= timeStampInitSecondsRefPointsInterp.get(i)- timeRef;        
            }	

	        if(findIdxEndTime && findIdxInitTime && initTime < endTime){
	        	List<double[]>vPoints=new ArrayList<double[]>();
				List<double[]>pPoints=new ArrayList<double[]>();
				List<Double>timeStampOutput=new ArrayList<Double>();
	        	HermiteInterpolation.interpolation(timeStampInitSecondsRefPoints,stateVecPoints, 
	        			timeStampInitSecondsRefPointsInterp0, idxInitTime, idxEndTime, deltaTinv,
	        			pPoints,vPoints,timeStampOutput);
	        	
	        	addPointsToResult(vPoints,pPoints);
	        	
				/*double [] timeStampInterpSecondsRefOutput=new double[timeStampOutput.size()];;
	        	for(int i=0;i<timeStampInterpSecondsRefOutput.length;i++){
	        		timeStampInterpSecondsRefOutput[i]=timeStampOutput.get(i)+timeRef;
            	}
	        	if(timeStampInterp==null)
					 timeStampInterp=new ArrayList<>(timeStampOutput);
				 else
					 timeStampInterp.addAll(timeStampOutput);
	        	*/
	        	
	        	List<Double> timeStampInterpSecondsRefOutput=new ArrayList<Double>();//hermite.getTimeStampInterpSecondsRefOutput();
            	for(int i=0;i<timeStampOutput.size();i++){
            		timeStampInterpSecondsRefOutput.add(timeStampOutput.get(i)+timeRef);
            	}
            	if(timeStampInterp==null)
					 timeStampInterp=new ArrayList<>(timeStampInterpSecondsRefOutput);
				 else
					 timeStampInterp.addAll(timeStampInterpSecondsRefOutput);
	        }
		 }   
		
	}
	
	/**
	 * 
	 * @param resultV
	 * @param resultP
	 */
	private void addPointsToResult(List<double[]> resultV,List<double[]> resultP){
		if(this.statevVecInterp==null){
			statevVecInterp=new ArrayList<>(resultV);
		}else{
			statevVecInterp.addAll(resultV);
		}
		if(statepVecInterp==null){
			statepVecInterp=new ArrayList<>(resultP);
		}else{
			statepVecInterp.addAll(resultP);
		}
		//System.out.println("Added:"+resultP.size());
	}
		
	public double getZeroDopplerTimeFirstRef() {
		return zeroDopplerTimeFirstRef;
	}


	public double getZeroDopplerTimeLastRef() {
		return zeroDopplerTimeLastRef;
	}
	
	public double[] getSecondsDiffFromRefTime() {
		return secondsDiffFromRefTime;
	}

	public List<double[]> getStatevVecInterp() {
		return statevVecInterp;
	}


	public List<double[]> getStatepVecInterp() {
		return statepVecInterp;
	}


	public List<Double> getTimeStampInterp() {
		return timeStampInterp;
	}


	public static void main(String args[]){
		S1Metadata meta =new S1Metadata();
		//meta.initMetaData("C:\\\\tmp\\\\sumo_images\\\\S1_PRF_SWATH_DEVEL\\\\S1A_IW_GRDH_1SDV_20150219T053530_20150219T053555_004688_005CB5_3904.SAFE\\\\annotation\\\\s1a-iw-grd-vv-20150219t053530-20150219t053555-004688-005cb5-001.xml");
		//meta.initMetaData("C:\\\\tmp\\\\sumo_images\\\\test_interpolation\\\\S1A_IW_GRDH_1SDV_20141016T173306_20141016T173335_002858_0033AF_FA6D.SAFE\\\\annotation\\\\s1a-iw-grd-vv-20141016t173306-20141016t173335-002858-0033af-001.xml");
		//meta.initMetaData("G:\\\\sat\\\\S1A_IW_GRDH_1SDH_20140607T205125_20140607T205150_000949_000EC8_CDCE.SAFE\\\\annotation\\\\s1a-iw-grd-hh-20140607t205125-20140607t205150-000949-000ec8-001.xml");
		meta.initMetaData("F:\\SumoImgs\\test_geo_loc\\S1A_IW_GRDH_1SDV_20150428T171323_20150428T171348_005687_0074BD_5A2C.SAFE/annotation/s1a-iw-grd-vv-20150428t171323-20150428t171348-005687-0074bd-001.xml");
		//meta.initMetaData("H://Radar-Images//S1Med//S1//EW//S1A_EW_GRDH_1SDV_20141020T055155_20141020T055259_002909_0034C1_F8D5.SAFE//annotation//s1a-ew-grd-vv-20141020t055155-20141020t055259-002909-0034c1-001.xml");

		
		OrbitInterpolation orbitInterpolation=new OrbitInterpolation();
		try {
			
			double zTimeFirstInSeconds=meta.getZeroDopplerTimeFirstLineSeconds().getTimeInMillis()/1000.0;
			double zTimeLastInSeconds=meta.getZeroDopplerTimeLastLineSeconds().getTimeInMillis()/1000.0;
			
			orbitInterpolation.orbitInterpolation(meta.getOrbitStatePosVelox(),
					zTimeFirstInSeconds,
					zTimeLastInSeconds,
					meta.getSamplingf(),500);
			//System.out.println(""+orbitInterpolation.getStatepVecInterp().get(113315)[0]);
			System.out.println(""+orbitInterpolation.getStatepVecInterp().size());
		} catch (MathException e) {
			e.printStackTrace();
		}
	}
	
}
