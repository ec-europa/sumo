package jrc.it.geolocation.geo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import jrc.it.geolocation.common.GeoUtils;
import jrc.it.geolocation.common.MathUtil;
import jrc.it.geolocation.exception.GeoLocationException;
import jrc.it.geolocation.exception.MathException;
import jrc.it.geolocation.interpolation.OrbitInterpolation;
import jrc.it.geolocation.metadata.IMetadata;
import jrc.it.geolocation.metadata.S1Metadata;
import jrc.it.geolocation.metadata.IMetadata.OrbitStatePosVelox;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.util.FastMath;
import org.slf4j.LoggerFactory;

public class S1GeoCodingImpl implements GeoCoding {
	
	
	private S1Metadata meta=null;
	private OrbitInterpolation orbitInterpolation=null;
	private IMetadata.CoordinateConversion[] coordConv=null;
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(S1GeoCodingImpl.class);
	
	private final int  iSafetyBufferAz=500;
	
	
	/**
	 * 
	 * @param metaFile
	 * @throws MathException
	 */
	public S1GeoCodingImpl(String metaFile) throws MathException{
		meta =new S1Metadata();
		meta.initMetaData(metaFile);
		
		double zTimeFirstInSeconds=meta.getZeroDopplerTimeFirstLineSeconds().getTimeInMillis()/1000.0;
		double zTimeLastInSeconds=meta.getZeroDopplerTimeLastLineSeconds().getTimeInMillis()/1000.0;
		
		orbitInterpolation=new OrbitInterpolation();
		List<OrbitStatePosVelox> l=new ArrayList<>();
		l.addAll( meta.getOrbitStatePosVelox());
		orbitInterpolation.orbitInterpolation(l,zTimeFirstInSeconds,zTimeLastInSeconds,meta.getSamplingf(),iSafetyBufferAz);
		coordConv=meta.getCoordinateConversion();
	}
	
	/**
	 * 
	 */
	@Override
	public double[] pixelFromGeo(double lon,double lat)throws GeoLocationException{
		double[] resultReverse=new double[2];
		
		double[] pXYZ =GeoUtils.convertFromGeoToEarthCentred(lat, lon);
		List<double[]> statepVecInterp=new ArrayList<>(orbitInterpolation.getStatepVecInterp());
		double dd[][]=(double[][])statepVecInterp.toArray(new double[0][]);
		Double dd2[]=(Double[])orbitInterpolation.getTimeStampInterp().toArray(new Double[0]);
		double[] results=findZeroDoppler(dd, pXYZ, ArrayUtils.toPrimitive(dd2));
		
		double zeroDopplerTime=results[0];
		logger.debug("Zero Doppler Time:"+zeroDopplerTime);
		
		double srdist=results[1];
		logger.debug("srdist:"+srdist);
		
		//Convert zero Doppler azimuth time to image line number
		double lNominatore=(zeroDopplerTime - orbitInterpolation.getZeroDopplerTimeFirstRef()+iSafetyBufferAz/meta.getSamplingf());
		double lDenom=(orbitInterpolation.getZeroDopplerTimeLastRef() - orbitInterpolation.getZeroDopplerTimeFirstRef() -2*(iSafetyBufferAz /meta.getSamplingf())) ;
		double l = (lNominatore/lDenom )* (meta.getNlines() - 1);

		//******************* this part is only Sentinel 1 'SLC 'IW' and 'EW'
		    // Need to take the bursts into account
		/*if(meta.getMode().equalsIgnoreCase("IW")||meta.getMode().equalsIgnoreCase("EW")){
			double timeRef = meta.getOrbitStatePosVelox().get(0).timeStampInitSeconds - orbitInterpolation.getSecondsDiffFromRefTime()[0];
			double[] az0TimBSeconds = meta.az0TimBSeconds - timeRef;
		    double azLTimBSeconds = az0TimBSeconds + meta.getLinesPerBurst()*meta.getAzimuthTimeInterval();
		    
		    int idx=0;
		    do{
		    	idx++;
		    }while(az0TimBSeconds<zeroDopplerTime&&azLTimBSeconds > zeroDopplerTime);
			    
		    
		    idx = find(az0TimBSeconds < zeroDopplerTime & azLTimBSeconds > zeroDopplerTime);
		    l = meta.linesPerBurst * (idx-1) + (zeroDopplerTime - az0TimBSeconds(idx))/meta.azimuthTimeInterval;
		
		}	*/
		//******************* End part only for Sentinel 1 'SLC 'IW' and 'EW'
		
		
		//************************************Sentinel 1 non SLC a********************//
		// Interpolate the GR to SR coefficients at the zero Doppler time.  This is only needed in non-SLC with multiple GR to SR polynomials

		if (meta.getCoordinateConversion()!=null&&meta.getCoordinateConversion().length>0){
			
			
			//TODO check if the time ref is correct and if the 2 values are calculated in the correct way
																			//getSecondsDiffFromRefTime is timeStampInitSecondsRef in Matlab
			double timeRef = meta.getOrbitStatePosVelox().get(0).timeStampInitSeconds - orbitInterpolation.getSecondsDiffFromRefTime()[0];
			
			double[]groundToSlantRangePolyTimesSeconds=new double[coordConv.length];
			int index=0;
			for(S1Metadata.CoordinateConversion cc:coordConv){
				groundToSlantRangePolyTimesSeconds[index] = cc.groundToSlantRangePolyTimesSeconds - timeRef;
				index++;
			}
			
		    int idx=0;
		    if(zeroDopplerTime < groundToSlantRangePolyTimesSeconds[0]){
		        idx = 0;
			}else if (zeroDopplerTime > groundToSlantRangePolyTimesSeconds[groundToSlantRangePolyTimesSeconds.length-1]){
		        idx = groundToSlantRangePolyTimesSeconds.length - 2;
			}else{
				for(idx=0;idx<groundToSlantRangePolyTimesSeconds.length;idx++){
					if(groundToSlantRangePolyTimesSeconds[idx] > zeroDopplerTime)
						break;
				}
				if(idx==groundToSlantRangePolyTimesSeconds.length-1){
					idx=idx-1;
				}
				if(idx==groundToSlantRangePolyTimesSeconds.length){
					idx=idx-2;
				}
		    }
		    
		    
		    double factor1 = (groundToSlantRangePolyTimesSeconds[idx+1] - zeroDopplerTime) / (groundToSlantRangePolyTimesSeconds[idx+1] - groundToSlantRangePolyTimesSeconds[idx]);
		    double factor2 = (zeroDopplerTime - groundToSlantRangePolyTimesSeconds[idx]) / (groundToSlantRangePolyTimesSeconds[idx+1] - groundToSlantRangePolyTimesSeconds[idx]);

		    double[]groundToSlantRangeCoefficientsInterp=new double[coordConv[idx].groundToSlantRangeCoefficients.length];
	    	for(int idCoeff=0;idCoeff<groundToSlantRangeCoefficientsInterp.length;idCoeff++){
	    		groundToSlantRangeCoefficientsInterp[idCoeff]=factor1*coordConv[idx].groundToSlantRangeCoefficients[idCoeff]+factor2*coordConv[idx+1].groundToSlantRangeCoefficients[idCoeff];
	    	}
	    	
		    double[]slantToGroundRangeCoefficientsInterp=new double[coordConv[idx].slantToGroundRangeCoefficients.length];
	    	for(int idCoeff=0;idCoeff<slantToGroundRangeCoefficientsInterp.length;idCoeff++){
	    		slantToGroundRangeCoefficientsInterp[idCoeff]=factor1*coordConv[idx].slantToGroundRangeCoefficients[idCoeff]+factor2*coordConv[idx+1].slantToGroundRangeCoefficients[idCoeff];
	    	}

	    	//TODO ADDING THIS PART FOR SLC 
			// Convert the slant range into the distance D, from the near-range edge of the scene.
	    	/*	if strncmp(meta.productType,'SLC',3) || strncmp(meta.productType,'SCS',3) || strncmp(meta.productType,'SSC',3)
			    D = SRdist - meta.slantRangeNearEdge;
			else*/
		
	       double newD=0;
           // Using the groundToSlantRangeCoefficients
           double oldD = 0;
           int nCoeffs = groundToSlantRangeCoefficientsInterp.length;
           
          //initialize the Arrays with the coefficients
           int[] numCoeffs =new int[nCoeffs];
           int[] numExps =new int[nCoeffs];
           int[] denomCoeffs =new int[nCoeffs];
           int[] denomExps =new int[nCoeffs];
           
           for(int i=0;i<nCoeffs;i++){
        	   numCoeffs[i]=i-1;
        	   numExps[i]=i;
        	   
        	   denomCoeffs[i]=i;
        	   denomExps[i]=i-1;
           }
           denomExps[0]=0;
           
           double deltaD=0;
           do{
        	 //calcolo dExp e denommExp come prodotti vettoriali => il risultato è uno scalare
        	   double numCoeffsXGround[]=new double[nCoeffs];
        	   double denomCoeffsXGround[]=new double[nCoeffs];
        	   double scalNum=0;
        	   double scalDen=0;

        	   for(int i=0;i<groundToSlantRangeCoefficientsInterp.length;i++){
        		   double dExp=FastMath.pow(oldD,numExps[i]);
        		   double denomExp=FastMath.pow(oldD,denomExps[i]);
        		   
        		   numCoeffsXGround[i]=groundToSlantRangeCoefficientsInterp[i]*numCoeffs[i];
        		   denomCoeffsXGround[i]=groundToSlantRangeCoefficientsInterp[i]*denomCoeffs[i];
        		   
             	  //calcolo lo scalare risultante dal prodotto di 2 matrici (1xn)x(nx1) 
        		   scalNum=scalNum+dExp*numCoeffsXGround[i];
        		   scalDen=scalDen+denomExp*denomCoeffsXGround[i];
        	   }
        	   
        	   double num=srdist+scalNum;
        	   newD = num/scalDen;
               
               deltaD = FastMath.abs(newD-oldD);
               oldD=newD;
               
           }while(deltaD > 1);
		           
			// Convert slant range distance to image pixel (column)
			double p=0;// (newD + meta.getGroundRangeOrigin())/meta.getSamplePixelSpacing();
			
			if (meta.isPixelTimeOrderingAscending()){
			    p = (newD + meta.getGroundRangeOrigin())/meta.getSamplePixelSpacing();
			 }else{
			    p = meta.getNumberOfSamplesPerLine() - 1 - (newD + meta.getGroundRangeOrigin())/meta.getSamplePixelSpacing();
			}
			resultReverse[0]=p;
			resultReverse[1]=l;
			logger.debug("line:"+l+"  "+"col:"+p);
		}
		return resultReverse;
	}
	
	/**
	 * In Matlab forwardgeolocation
	 */
	@Override
	public double[] geoFromPixel(double p, double l) throws GeoLocationException{
		try{
			double[] results=new double[2];
			
			double lon=0;
			double lat=0;
			//Convert the line number into zero Doppler time
			/*if strcmp(meta.ImTyp,'TX') && ~strncmp(meta.productType,'SSC',3) && strcmp(meta.pass,'ASCENDING') //This is to make it compatible with SUMO
			    l = meta.nLines - l;
			end*/ 
	
			double t0=0;
			int idxStartT0=0;
			//TODO blocco da inserire solo per immagini complesse
			if(meta.getType().equalsIgnoreCase("S1") && meta.getProductType().equalsIgnoreCase("SLC") && (meta.getMode().equalsIgnoreCase("IW") ||meta.getMode().equalsIgnoreCase("EW"))){
			    // Need to take the bursts into account 
				double timeRef = meta.getOrbitStatePosVelox().get(0).timeStampInitSeconds - orbitInterpolation.getSecondsDiffFromRefTime()[0];
			    
				//TODO COMPLETE THIS PART FOR THE EW
			  /*  double az0TimBSeconds = meta.az0TimBSeconds - timeRef;
			    double azLTimBSeconds = az0TimBSeconds + meta.linesPerBurst*meta.azimuthTimeInterval;
			    
			    double idxBurst = ceil(l / meta.linesPerBurst) - 1;
			    double lBurst = l - idxBurst * meta.linesPerBurst;
			    
			    t0 = az0TimBSeconds(idxBurst+1) + lBurst * meta.azimuthTimeInterval;
			    
			    for(idxStartT0=0;idxStartT0<orbitInterpolation.getTimeStampInterp().length;idxStartT0++){
			    	if(orbitInterpolation.getTimeStampInterp()[idxStartT0]>=t0){
			    		break;
			    	}
			    }     */
			
			}else{
				double t01=(orbitInterpolation.getZeroDopplerTimeFirstRef()+ iSafetyBufferAz / meta.getSamplingf()) * (meta.getNlines()-1-l);
				double t02=(orbitInterpolation.getZeroDopplerTimeLastRef()- iSafetyBufferAz / meta.getSamplingf())*l;
				t0 =  (t01+t02) / (meta.getNlines()-1); //In seconds
			    for(idxStartT0=0;idxStartT0<orbitInterpolation.getTimeStampInterp().size();idxStartT0++){
			    	if(orbitInterpolation.getTimeStampInterp().get(idxStartT0)>t0){
			    		break;
			    	}
			    }
			    if(idxStartT0==orbitInterpolation.getStatepVecInterp().size())//MATLAB if isempty(idx_t0) idx_t0 = length(timeStampInterpSecondsRef); end  %20150608
			    	idxStartT0--;
			}
			//Using the orbit propagation model, find the sensor position p(t0) and sensor velocity v(t0) at zero Doppler time
			double[] pT0 = orbitInterpolation.getStatepVecInterp().get(idxStartT0);
			double[] vT0 = orbitInterpolation.getStatevVecInterp().get(idxStartT0);
	
	
			double distance=0;  //D
			// Convert the pixel number into a distance from the near-range edge of the image
			if(meta.isPixelTimeOrderingAscending()){
			    distance = p * meta.getSamplePixelSpacing() - meta.getGroundRangeOrigin();
			}else{
			    distance = (meta.getNumberOfSamplesPerLine() - 1 - p) * meta.getSamplePixelSpacing() - meta.getGroundRangeOrigin();
			}
	
			// Interpolate the GR to SR coefficients at the zero Doppler time.  This is only needed in non-SLC with multiple GR to SR polynomials
			if (coordConv!=null){
			    double zeroDopplerTime = t0;
			    double timeRef = meta.getOrbitStatePosVelox().get(0).timeStampInitSeconds - orbitInterpolation.getSecondsDiffFromRefTime()[0];
			    
			    double[]groundToSlantRangePolyTimesSeconds=new double[coordConv.length];
				int index=0;
				for(S1Metadata.CoordinateConversion cc:coordConv){
					groundToSlantRangePolyTimesSeconds[index] = cc.groundToSlantRangePolyTimesSeconds - timeRef;
					index++;
				}
				
				int idx=0;
			    if(zeroDopplerTime < groundToSlantRangePolyTimesSeconds[0]){
			        idx = 1;
				}else if (zeroDopplerTime > groundToSlantRangePolyTimesSeconds[groundToSlantRangePolyTimesSeconds.length-1]){
			        idx = groundToSlantRangePolyTimesSeconds.length - 2;
				}else{
					for(idx=0;idx<groundToSlantRangePolyTimesSeconds.length;idx++){
						if(groundToSlantRangePolyTimesSeconds[idx] < zeroDopplerTime)
							break;
					}
			    }
			    double factor1 = (groundToSlantRangePolyTimesSeconds[idx+1] - zeroDopplerTime) / (groundToSlantRangePolyTimesSeconds[idx+1] - groundToSlantRangePolyTimesSeconds[idx]);
			    double factor2 = (zeroDopplerTime - groundToSlantRangePolyTimesSeconds[idx]) / (groundToSlantRangePolyTimesSeconds[idx+1] - groundToSlantRangePolyTimesSeconds[idx]);
	
			    double[]groundToSlantRangeCoefficientsInterp=ArrayUtils.clone(coordConv[idx].groundToSlantRangeCoefficients);
			    double[]groundToSlantRangeCoefficientsInterp2=coordConv[idx+1].groundToSlantRangeCoefficients;
		    	for(int idCoeff=0;idCoeff<groundToSlantRangeCoefficientsInterp.length;idCoeff++){
		    		groundToSlantRangeCoefficientsInterp[idCoeff]=factor1*groundToSlantRangeCoefficientsInterp[idCoeff]+factor2*groundToSlantRangeCoefficientsInterp2[idCoeff];
		    	}
		    	
	
			    double[]slantToGroundRangeCoefficientsInterp=ArrayUtils.clone(coordConv[idx].slantToGroundRangeCoefficients);
			    double[]slantToGroundRangeCoefficientsInterp2=coordConv[idx+1].slantToGroundRangeCoefficients;
		    	for(int idCoeff=0;idCoeff<slantToGroundRangeCoefficientsInterp.length;idCoeff++){
		    		slantToGroundRangeCoefficientsInterp[idCoeff]=factor1*slantToGroundRangeCoefficientsInterp[idCoeff]+factor2*slantToGroundRangeCoefficientsInterp2[idCoeff];
		    	}
		        
				// Convert the distance from the near-range edge of the image into zero Doppler slant range
				/*TODO ADDING THIS PART FOR COMPLEX IMAGE
					//(strncmp(meta.productType,'SLC',3) || strncmp(meta.productType,'SCS',3) || strncmp(meta.productType,'SSC',3)){
				    //SRdist = D + meta.slantRangeNearEdge;
				}else{*/
				int nCoeffs = groundToSlantRangeCoefficientsInterp.length;
				int[] vExps =new int[nCoeffs];
				//create the Array with the coefficients
				for(int i=0;i<nCoeffs;i++){
			     	   vExps[i]=i;
			    }
			    double tmpD = distance - meta.getGroundRangeOrigin();
			    double[] pows=MathUtil.powValue2Coeffs(tmpD,vExps);
			    double sRdist = MathUtil.vectorProd1XN(groundToSlantRangeCoefficientsInterp, pows); // This polynomial transforms from slant range (in metres) to ground range (in metres)       
			    logger.debug("sRdist:"+sRdist);
	
			    //norma for pt0 vector
			    double normPt0=MathUtil.norm(pT0);
			    
				// Find the tangent of the angle ? between the zero Doppler plane and the vertical direction, which is the same as the ratio between the radial and tangential components of the sensor velocity
				double vRadial = MathUtil.vectorProd1XN(vT0, pT0) / normPt0;
				double vTangential = FastMath.sqrt(FastMath.pow(MathUtil.norm(vT0),2) - FastMath.pow(vRadial,2));
				double tanPsi = vRadial/vTangential;
	
	
				// Define a satellite coordinate system centred on the sensor position, where the Z axis points towards the Earth centre, the X axis points along the tangential component of the sensor velocity, and the Y axis completes the right-handed coordinate system
				double[] zsUnit =MathUtil.divVectByVal(pT0,-normPt0);
				double[] vTmp =MathUtil.crossProd3x3(zsUnit,vT0);// cross(zs_unit,v_t0);
				double[] ysUnit = MathUtil.divVectByVal(vTmp, MathUtil.norm(vTmp));
				double[] xsUnit = MathUtil.crossProd3x3(ysUnit,zsUnit);
	
				
				double pH=0;
				for(int iidx=0; iidx<2;iidx++){ //First iteration with a default pH, second with a more accurate pH
					// Find the approximate Earth radius at a point directly below the sensor position.
					double rEarth = normPt0/FastMath.sqrt((pT0[0]*pT0[0] + pT0[1]*pT0[1])/GeoUtils.semiMajorAxis2 + (pT0[2]*pT0[2])/GeoUtils.semiMinorAxis2) + pH;
		
					//iteration
					double rEarthOld = rEarth;
					double rEarthChange = 100000;
					
					double[]q=new double[3];
					
					for(;rEarthChange > 0.1;){
					    // Form a triangle whose sides are the distance from the centre of the earth to the sensor, the slant range and the local earth radius and using the cosine law find the intersection point between the slant range vector and the Earth surface in the satellite coordinate system
					    double Rz = (normPt0*normPt0 + sRdist*sRdist - rEarth*rEarth) / (2*normPt0);
					    double Rx = Rz * tanPsi;
					    double Ry = FastMath.sqrt(sRdist*sRdist - Rz*Rz - Rx*Rx);
					    
					    if (!meta.getAntennaPointing().equalsIgnoreCase("Right")){
					        Ry = -Ry;
					    }
		
					    // Transform the coordinates of the intersection point from satellite to target ECR coordinates
					    double[][] sRvect = new double[3][];
					    sRvect[0]=new double[]{Rx};
					    sRvect[1]=new double[]{Ry};
					    sRvect[2]=new double[]{Rz-normPt0};
					    
					    double[][] qbig = new double[3][];
					    qbig[0]=xsUnit;
					    qbig[1]=ysUnit;
					    qbig[2]=zsUnit;
					    RealMatrix m=MatrixUtils.createRealMatrix(qbig);
					    qbig=m.transpose().getData();
					    
					    double[][] qmat =MathUtil.multiplyMatrix(qbig,sRvect);
					    q[0]=qmat[0][0];
					    q[1]=qmat[1][0];
					    q[2]=qmat[2][0];
		
					    // Find the Earth radius at a point directly below the intersection point (this approximation becomes more and more precise as the intersection point becomes closer to the surface)
					    rEarth = MathUtil.norm(q) / FastMath.sqrt((q[0]*q[0]+q[1]*q[1])/GeoUtils.semiMajorAxis2 + (q[2]*q[2]/GeoUtils.semiMinorAxis2)) + pH;
					    
					    rEarthChange = FastMath.abs(rEarth-rEarthOld);
					    rEarthOld = rEarth;
					}
	
					// Convert the target ECR coordinates (x,y,z) into geographic coordinates (lat,lon,h) based on the WGS84 ellipsoid Earth model
					double x = q[0]; 
					double y = q[1]; 
					double z = q[2];
					
					lon = FastMath.atan2(y,x) * 180/FastMath.PI;
					double lattmp = FastMath.asin(z/FastMath.sqrt(x*x+y*y+z*z));
					lat=FastMath.atan(FastMath.tan(lattmp) * FastMath.pow((GeoUtils.semiMajorAxis+pH),2)/FastMath.pow((GeoUtils.semiMinorAxis+pH),2)) * 180/FastMath.PI;
					pH = GeoUtils.getGeoidH(lon,lat);
				}
			}
			logger.debug("lat:"+lat+ "  lon:"+lon);
			results[0]=lon;
			results[1]=lat;
			return results;
		}catch(MathException me){
			throw new GeoLocationException(GeoLocationException.MSG_CONV_FROM_PIXEL_TO_GEO + "  " +me.getMessage());
		}	
		
	}
	
	
	 

	/**
	 * 
	 * @param statepVecInterp
	 * @param pXYZ
	 * @param timeStampInterp
	 * @return double array with 2 elements [0]=zeroDopplerTimeSmooth  [1]=sRdistSmooth
	 */
	public double[] findZeroDoppler(double[][] statepVecInterp,double[] pXYZ,double[] timeStampInterp){
			int iOptFactor = 10;
		    int nPointsAroundMin = 100;//100;//50;
		    int nWindowLength = 7;//5;
		    double nWindowLengthSize = FastMath.floor(nWindowLength/2);

		///**********Optimization************    
		    double [][]diffSubsample = new double[statepVecInterp.length/iOptFactor][statepVecInterp[0].length];
		    double []vDistSubsample = new double[statepVecInterp.length/iOptFactor];
		    
		    for(int i=0;i<statepVecInterp.length/iOptFactor;i++){
		    	int pos=i*10;
		    	diffSubsample[i][0]=(statepVecInterp[pos][0]-pXYZ[0])*(statepVecInterp[pos][0]-pXYZ[0]);
		    	diffSubsample[i][1]=(statepVecInterp[pos][1]-pXYZ[1])*(statepVecInterp[pos][1]-pXYZ[1]);
		    	diffSubsample[i][2]=(statepVecInterp[pos][2]-pXYZ[2])*(statepVecInterp[pos][2]-pXYZ[2]);
		    	vDistSubsample[i]=diffSubsample[i][0]+diffSubsample[i][1]+diffSubsample[i][2];
		    }
		    int idxMinSubsample=0;
		    double distMinSubsample=vDistSubsample[0];
		    for(int i=0;i<vDistSubsample.length;i++){
		    	if(distMinSubsample>vDistSubsample[i]){
		    		distMinSubsample=vDistSubsample[i];
		    		idxMinSubsample=i;
		    	}
		    }
		    double idxMin = idxMinSubsample * iOptFactor;
		    
		    double[] vDist=new double[statepVecInterp.length];
		    for(int i=0;i<vDistSubsample.length;i++){
		    	for(int j=0;j<iOptFactor;j++){
		    		vDist[(i*iOptFactor)+j]=vDistSubsample[i];
		    	}			
		    }
		///**********End Optimization************
		    
	   ////
	   // vDist is spiky, especially if a high order Hermite polynomial has been used (ie degree 12 or higher) (maybe because of precision issues??) -> smooth it with
	   // a moving average. This is done to limit the maximum potential error in zero Doppler azimuth time (ie pixel line coordinate).
	   // The smoothing may reduce the zero Doppler distance by a very small amount (millimetres), so it will not alter the pixel column coordinate
		   
		    double iDistWInit = idxMin-nPointsAroundMin;
		    if( iDistWInit <= 0) 
		    	iDistWInit = 1; 
		    double iDistWEnd = idxMin+nPointsAroundMin;
		    if (iDistWEnd > vDist.length)
		    	iDistWEnd = vDist.length;

		    // Refine the distance calculation around the minimum found with optimisation
		    int start=((Double)iDistWInit).intValue();
		    double[] vDistOptimization=new double[((Double)iDistWEnd).intValue()-start];
		    for(int i=start;i<iDistWEnd;i++){
		    	double v1=(statepVecInterp[i][0]-pXYZ[0])*(statepVecInterp[i][0]-pXYZ[0]);
		    	double v2=(statepVecInterp[i][1]-pXYZ[1])*(statepVecInterp[i][1]-pXYZ[1]);
		    	double v3=(statepVecInterp[i][2]-pXYZ[2])*(statepVecInterp[i][2]-pXYZ[2]);
		    	vDistOptimization[i-start]=v1+v2+v3;
		    }
		    //matlab code w = ones(1,nWindowLength) / nWindowLength;
		    double w[]=new double[nWindowLength];
		    Arrays.fill(w,1.0/nWindowLength);

		    double[] vdistSmooth=MathUtil.linearConvolutionMatlabValid(vDistOptimization, w);//MathArrays.convolve(vDistOptimization, w);
		    double distMinSmooth=vdistSmooth[0];
		    int idxMinSmoothW=0;
		    for(int i=0;i<vdistSmooth.length;i++){
		    	if(distMinSmooth>vdistSmooth[i]){
		    		distMinSmooth=vdistSmooth[i];
		    		idxMinSmoothW=i;
		    	}
		    }
		    int idxMinSmooth=new Double(idxMinSmoothW+iDistWInit+nWindowLengthSize-1).intValue();

		    double sRdistSmooth = FastMath.sqrt(distMinSmooth);
		    double zeroDopplerTimeSmooth = timeStampInterp[idxMinSmooth];
		    
		    double res[]={zeroDopplerTimeSmooth,sRdistSmooth};
		    return res; 
	}
	
	
	
	public static void main(String args[]){
		//String metaF="C:/tmp/sumo_images/S1_PRF_SWATH_DEVEL/S1A_IW_GRDH_1SDV_20150219T053530_20150219T053555_004688_005CB5_3904.SAFE/annotation/s1a-iw-grd-vv-20150219t053530-20150219t053555-004688-005cb5-001.xml";
		//String metaF="C:\\tmp\\sumo_images\\carlos tests\\pixel analysis\\S1A_IW_GRDH_1SDV_20150215T171331_20150215T171356_004637_005B75_CFE1.SAFE\\annotation\\s1a-iw-grd-vv-20150215t171331-20150215t171356-004637-005b75-001.xml";
		//String metaF="H:/sat/S1A_IW_GRDH_1SDH_20140607T205125_20140607T205150_000949_000EC8_CDCE.SAFE/annotation/s1a-iw-grd-hh-20140607t205125-20140607t205150-000949-000ec8-001.xml";
		//String metaF="C:\\tmp\\sumo_images\\carlos tests\\geoloc\\S1A_EW_GRDH_1SDV_20141020T055155_20141020T055259_002909_0034C1_F8D5.SAFE\\annotation\\s1a-ew-grd-vv-20141020t055155-20141020t055259-002909-0034c1-001.xml";
		String metaF="H://Radar-Images//S1Med//S1//EW//S1A_EW_GRDH_1SDV_20141020T055155_20141020T055259_002909_0034C1_F8D5.SAFE//annotation//s1a-ew-grd-vv-20141020t055155-20141020t055259-002909-0034c1-001.xml";
		
		/*
		 * The geographic coordinates of this point are:
			- Google Earth: lat=41.31735  lon=2.17263
			- OSM 0m buffer: 41.31744 2.17258
			
			So, Google Earth and OSM very closely agree (the fifth decimal number in lat lon in degrees roughly represents meters).
			
			In the Sentinel-1 image, the coordinates of the point are:
			- S1 image:  x=14165  y=15766
			
			
			When running reverse geocoding using orbit SVs for that point (lat=41.31735  lon=2.17263), I am getting:
			- Matlab code:  x=14167.6  y=15764.7
			- SUMO (orbit SVs geocoding):  x=14054  y=15763
			
			So a big discrepancy in the x direction (113 pixes = 2825 meters) .  Matlab gives the correct results.*/
		
		
		GeoCoding gc;
		try {
			gc = new S1GeoCodingImpl(metaF);
			double lat = 41.31735;//43.13935;//42.81202;
			double lon = 2.17263;//3.35876;//10.32972;
			double r[];
			try {
				//r = gc.pixelFromGeo(lon, lat);
				r = gc.geoFromPixel(-100, 16831);
				System.out.println("Line:"+r[1]+"--- Col:"+r[0]);
			} catch (GeoLocationException e) {
				e.printStackTrace();
			}
			
			
			
			//double r[]=gc.forward(-100.0,11104.0);
			//logger.debug("lon:"+r[0]+"---  lat:"+r[1]);
		} catch (MathException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
	
	
	
}
