package jrc.it.geolocation.geo;

import java.util.Arrays;
import java.util.List;

import jrc.it.geolocation.common.GeoUtils;
import jrc.it.geolocation.common.MathUtil;
import jrc.it.geolocation.interpolation.OrbitInterpolation;
import jrc.it.geolocation.metadata.IMetadata;
import jrc.it.geolocation.metadata.S1Metadata;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.slf4j.LoggerFactory;

public class S1GeoCodingImpl implements GeoCoding {
	
	
	private S1Metadata meta=null;
	private OrbitInterpolation orbitInterpolation=null;
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(S1GeoCodingImpl.class);
	private MathUtil mathUtil;
	
	/**
	 * 
	 * @param metaFile
	 */
	public S1GeoCodingImpl(String metaFile){
		meta =new S1Metadata();
		meta.initMetaData(metaFile);
		
		double zTimeFirstInSeconds=meta.getZeroDopplerTimeFirstLineSeconds().getTimeInMillis()/1000.0;
		double zTimeLastInSeconds=meta.getZeroDopplerTimeLastLineSeconds().getTimeInMillis()/1000.0;
		
		orbitInterpolation=new OrbitInterpolation();
		orbitInterpolation.orbitInterpolation(meta.getOrbitStatePosVelox(),zTimeFirstInSeconds,zTimeLastInSeconds,meta.getSamplingf());
		mathUtil=new MathUtil();
	}
	
	/* (non-Javadoc)
	 * @see geo.GeoCoding#reverse(double, double)
	 */
	@Override
	public double[] reverse(double lat,double lon){
		double[] resultReverse=new double[2];
		
		double[] pXYZ =GeoUtils.convertFromGeoToEarthCentred(lat, lon);
		logger.debug(pXYZ[0]+"   "+pXYZ[1]+"   "+pXYZ[2]);
		
		final double[][] statepVecInterp=orbitInterpolation.getStatepVecInterp();
		
		double[] results=findZeroDoppler(statepVecInterp, pXYZ,orbitInterpolation.getTimeStampInterp() );
		//double last=orbitInterpolation.getTimeStampInterp()[orbitInterpolation.getTimeStampInterp().length-1 ]; 
		logger.debug("res[0]:"+results[0]+"    res[1]:"+results[1]);
		
		double zeroDopplerTime=results[0];
		logger.debug("Zero Doppler Time:"+zeroDopplerTime);
		
		double srdist=results[1];
		logger.debug("srdist:"+srdist);
		
		//Convert zero Doppler azimuth time to image line number
		double l = ((zeroDopplerTime - orbitInterpolation.getZeroDopplerTimeFirstRef()) / (orbitInterpolation.getZeroDopplerTimeLastRef() - orbitInterpolation.getZeroDopplerTimeFirstRef())) * (meta.getNlines() - 1);
		logger.debug("ZeroDopplerTimeFirstRef:"+orbitInterpolation.getZeroDopplerTimeFirstRef());
		logger.debug("ZeroDopplerTimeLastRef:"+orbitInterpolation.getZeroDopplerTimeLastRef());
		logger.debug("l:"+l);

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

		if (meta.getCoordinateConversion()!=null&&!meta.getCoordinateConversion().isEmpty()){
			List<S1Metadata.CoordinateConversion> coordConv=meta.getCoordinateConversion();
			
			//TODO check if the time ref is correct and if the 2 values are calculated in the correct way
																			//getSecondsDiffFromRefTime is timeStampInitSecondsRef
			double timeRef = meta.getOrbitStatePosVelox().get(0).timeStampInitSeconds - orbitInterpolation.getSecondsDiffFromRefTime()[0];
			
			double[]groundToSlantRangePolyTimesSeconds=new double[coordConv.size()];
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

		    double[]groundToSlantRangeCoefficientsInterp=ArrayUtils.clone(coordConv.get(idx).groundToSlantRangeCoefficients);
		    double[]groundToSlantRangeCoefficientsInterp2=ArrayUtils.clone(coordConv.get(idx+1).groundToSlantRangeCoefficients);
	    	for(int idCoeff=0;idCoeff<groundToSlantRangeCoefficientsInterp.length;idCoeff++){
	    		groundToSlantRangeCoefficientsInterp[idCoeff]=factor1*groundToSlantRangeCoefficientsInterp[idCoeff]+factor2*groundToSlantRangeCoefficientsInterp2[idCoeff];
	    	}
	    	

		    double[]slantToGroundRangeCoefficientsInterp=ArrayUtils.clone(coordConv.get(idx).slantToGroundRangeCoefficients);
		    double[]slantToGroundRangeCoefficientsInterp2=ArrayUtils.clone(coordConv.get(idx+1).slantToGroundRangeCoefficients);
	    	for(int idCoeff=0;idCoeff<slantToGroundRangeCoefficientsInterp.length;idCoeff++){
	    		slantToGroundRangeCoefficientsInterp[idCoeff]=factor1*slantToGroundRangeCoefficientsInterp[idCoeff]+factor2*slantToGroundRangeCoefficientsInterp2[idCoeff];
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
	           double[] dExp=new double[nCoeffs];
        	   double[] denomExp=new double[nCoeffs];
        	   for(int i=0;i<groundToSlantRangeCoefficientsInterp.length;i++){
        		   dExp[i]=Math.pow(oldD,numExps[i]);
        		   denomExp[i]=Math.pow(oldD,denomExps[i]);
        	   }
        	   double numCoeffsXGround[]=new double[nCoeffs];
        	   double denomCoeffsXGround[]=new double[nCoeffs];
        	   for(int i=0;i<groundToSlantRangeCoefficientsInterp.length;i++){
        		   numCoeffsXGround[i]=groundToSlantRangeCoefficientsInterp[i]*numCoeffs[i];
        		   denomCoeffsXGround[i]=groundToSlantRangeCoefficientsInterp[i]*denomCoeffs[i];
        	   }
        	   
        	   double scalNum=0;
        	   double scalDen=0;
        	  //calcolo lo scalare risultante dal prodotto di 2 matrici (1xn)x(nx1) 
        	   for(int i=0;i<groundToSlantRangeCoefficientsInterp.length;i++){
        		   scalNum=scalNum+dExp[i]*numCoeffsXGround[i];
        		   scalDen=scalDen+denomExp[i]*denomCoeffsXGround[i];
        	   }
        	   
        	   double num=srdist+scalNum;
        	   newD = num/scalDen;
               
               deltaD = Math.abs(newD-oldD);
               oldD=newD;
               
           }while(deltaD > 1);
		           
		    logger.debug("D:"+newD);
			// Convert slant range distance to image pixel (column)
			double p= (newD + meta.getGroundRangeOrigin())/meta.getSamplePixelSpacing();
			
			if (meta.isPixelTimeOrderingAscending()){
			    p = (newD + meta.getGroundRangeOrigin())/meta.getSamplePixelSpacing();
			 }else{
			    p = meta.getNumberOfSamplesPerLine() - 1 - (newD + meta.getGroundRangeOrigin())/meta.getSamplePixelSpacing();
			}
			logger.debug("image pixel result:"+p);
			resultReverse[0]=l;
			resultReverse[1]=p;
		}
		return resultReverse;
	}
	
	
	@Override
	public double[] forward(double p, double l){
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
		    t0 = (orbitInterpolation.getZeroDopplerTimeFirstRef() * (meta.getNlines()-1-l) + orbitInterpolation.getZeroDopplerTimeLastRef()*l) / (meta.getNlines()-1); //In seconds
		    for(idxStartT0=0;idxStartT0<orbitInterpolation.getTimeStampInterp().length;idxStartT0++){
		    	if(orbitInterpolation.getTimeStampInterp()[idxStartT0]>t0){
		    		break;
		    	}
		    }
		    if(idxStartT0==orbitInterpolation.getStatepVecInterp().length)
		    	idxStartT0--;
		}
		//Using the orbit propagation model, find the sensor position p(t0) and sensor velocity v(t0) at zero Doppler time
		double[] pT0 = orbitInterpolation.getStatepVecInterp()[idxStartT0];
		double[] vT0 = orbitInterpolation.getStatevVecInterp()[idxStartT0];


		double distance=0;  //D
		// Convert the pixel number into a distance from the near-range edge of the image
		if(meta.isPixelTimeOrderingAscending()){
		    distance = p * meta.getSamplePixelSpacing() - meta.getGroundRangeOrigin();
		}else{
		    distance = (meta.getNumberOfSamplesPerLine() - 1 - p) * meta.getSamplePixelSpacing() - meta.getGroundRangeOrigin();
		}

		// Interpolate the GR to SR coefficients at the zero Doppler time.  This is only needed in non-SLC with multiple GR to SR polynomials
		List<IMetadata.CoordinateConversion>coordConv=meta.getCoordinateConversion();
		if (coordConv!=null&&!coordConv.isEmpty()){
		    double zeroDopplerTime = t0;
		    double timeRef = meta.getOrbitStatePosVelox().get(0).timeStampInitSeconds - orbitInterpolation.getSecondsDiffFromRefTime()[0];
		    
		    double[]groundToSlantRangePolyTimesSeconds=new double[coordConv.size()];
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

		    final double[]groundToSlantRangeCoefficientsInterp=ArrayUtils.clone(coordConv.get(idx).groundToSlantRangeCoefficients);
		    final double[]groundToSlantRangeCoefficientsInterp2=ArrayUtils.clone(coordConv.get(idx+1).groundToSlantRangeCoefficients);
	    	for(int idCoeff=0;idCoeff<groundToSlantRangeCoefficientsInterp.length;idCoeff++){
	    		groundToSlantRangeCoefficientsInterp[idCoeff]=factor1*groundToSlantRangeCoefficientsInterp[idCoeff]+factor2*groundToSlantRangeCoefficientsInterp2[idCoeff];
	    	}
	    	

		    final double[]slantToGroundRangeCoefficientsInterp=ArrayUtils.clone(coordConv.get(idx).slantToGroundRangeCoefficients);
		    final double[]slantToGroundRangeCoefficientsInterp2=ArrayUtils.clone(coordConv.get(idx+1).slantToGroundRangeCoefficients);
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
			for(int i=0;i<nCoeffs;i++){
		     	   vExps[i]=i;
		    }
		    double tmpD = distance - meta.getGroundRangeOrigin();
		    double[] pows=mathUtil.powValue2Coeffs(tmpD,vExps);
		    double sRdist = mathUtil.vectorProd1XN(groundToSlantRangeCoefficientsInterp, pows); // This polynomial transforms from slant range (in metres) to ground range (in metres)       
		    logger.debug("sRdist:"+sRdist);

		    //norma for pt0 vector
		    double normPt0=mathUtil.norm(pT0);
		    
			// Find the tangent of the angle ? between the zero Doppler plane and the vertical direction, which is the same as the ratio between the radial and tangential components of the sensor velocity
			double vRadial = mathUtil.vectorProd1XN(vT0, pT0) / normPt0;
			logger.debug("vRadial:"+vRadial);
			double vTangential = Math.sqrt(Math.pow(mathUtil.norm(vT0),2) - Math.pow(vRadial,2));
			logger.debug("vTangential:"+vTangential);
			double tanPsi = vRadial/vTangential;


			// Define a satellite coordinate system centred on the sensor position, where the Z axis points towards the Earth centre, the X axis points along the tangential component of the sensor velocity, and the Y axis completes the right-handed coordinate system
			double[] zsUnit =mathUtil.divVectByVal(pT0,-normPt0);
			logger.debug("zsUnit[0]:"+zsUnit[0]);
			
			double[] vTmp =mathUtil.crossProd3x3(zsUnit,vT0);// cross(zs_unit,v_t0);
			
			double[] ysUnit = mathUtil.divVectByVal(vTmp, mathUtil.norm(vTmp));
			double[] xsUnit = mathUtil.crossProd3x3(ysUnit,zsUnit);

			
			double pH=0;
			for(int iidx=0; iidx<2;iidx++){ //First iteration with a default pH, second with a more accurate pH
				// Find the approximate Earth radius at a point directly below the sensor position.
				double rEarth = normPt0/Math.sqrt((pT0[0]*pT0[0] + pT0[1]*pT0[1])/(GeoUtils.semiMajorAxis*GeoUtils.semiMajorAxis) + (pT0[2]*pT0[2])/(GeoUtils.semiMinorAxis*GeoUtils.semiMinorAxis)) + pH;
	
				//iteration
				double rEarthOld = rEarth;
				double rEarthChange = 100000;
				
				double[]q=new double[3];
				
				for(;rEarthChange > 0.1;){
				    // Form a triangle whose sides are the distance from the centre of the earth to the sensor, the slant range and the local earth radius and using the cosine law find the intersection point between the slant range vector and the Earth surface in the satellite coordinate system
				    double Rz = (normPt0*normPt0 + sRdist*sRdist - rEarth*rEarth) / (2*normPt0);
				    double Rx = Rz * tanPsi;
				    double Ry = Math.sqrt(sRdist*sRdist - Rz*Rz - Rx*Rx);
				    logger.debug("Rx:"+Rx+ "  Rz:"+Rz+"  Ry:"+Ry);
				    
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
				    
				    double[][] qmat =mathUtil.multiplyMatrix(qbig,sRvect);
				    q[0]=qmat[0][0];
				    q[1]=qmat[1][0];
				    q[2]=qmat[2][0];
					logger.debug("q:["+q[0]+ "  "+q[1]+"  "+q[2]+"  ]");
	
				    // Find the Earth radius at a point directly below the intersection point (this approximation becomes more and more precise as the intersection point becomes closer to the surface)
				    rEarth = mathUtil.norm(q) / Math.sqrt((q[0]*q[0]+q[1]*q[1])/(GeoUtils.semiMajorAxis*GeoUtils.semiMajorAxis) + (q[2]*q[2]/(GeoUtils.semiMinorAxis*GeoUtils.semiMinorAxis))) + pH;
				    
				    rEarthChange = Math.abs(rEarth-rEarthOld);
				    rEarthOld = rEarth;
				}

				// Convert the target ECR coordinates (x,y,z) into geographic coordinates (lat,lon,h) based on the WGS84 ellipsoid Earth model
				double x = q[0]; 
				double y = q[1]; 
				double z = q[2];
				
				lon = Math.atan2(y,x) * 180/Math.PI;
				double lattmp = Math.asin(z/Math.sqrt(x*x+y*y+z*z));
				lat=Math.atan(Math.tan(lattmp) * Math.pow((GeoUtils.semiMajorAxis+pH),2)/Math.pow((GeoUtils.semiMinorAxis+pH),2)) * 180/Math.PI;
				pH = GeoUtils.getGeoidH(lon,lat);
			}
		}
		logger.debug("lat:"+lat+ "  lon:"+lon);
		results[0]=lon;
		results[1]=lat;
		return results;
		
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
		    double nWindowLengthSize = Math.floor(nWindowLength/2);

		///**********Optimization************    
		    double [][]diffSubsample = new double[statepVecInterp.length/iOptFactor][statepVecInterp[0].length];
		    double []vDistSubsample = new double[statepVecInterp.length/iOptFactor];
		    
		    for(int i=0;i<statepVecInterp.length/iOptFactor;i++){
		    	diffSubsample[i][0]=Math.pow(statepVecInterp[i*10][0]-pXYZ[0],2);
		    	diffSubsample[i][1]=Math.pow(statepVecInterp[i*10][1]-pXYZ[1],2);
		    	diffSubsample[i][2]=Math.pow(statepVecInterp[i*10][2]-pXYZ[2],2);
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
		    	double v1=Math.pow(statepVecInterp[i][0]-pXYZ[0],2);
		    	double v2=Math.pow(statepVecInterp[i][1]-pXYZ[1],2);
		    	double v3=Math.pow(statepVecInterp[i][2]-pXYZ[2],2);
		    	vDistOptimization[i-start]=v1+v2+v3;
		    }
		    //matlab code w = ones(1,nWindowLength) / nWindowLength;
		    double w[]=new double[nWindowLength];
		    Arrays.fill(w,1.0/nWindowLength);

		    double[] vdistSmooth=mathUtil.linearConvolutionMatlabValid(vDistOptimization, w);//MathArrays.convolve(vDistOptimization, w);
		    double distMinSmooth=vdistSmooth[0];
		    int idxMinSmoothW=0;
		    for(int i=0;i<vdistSmooth.length;i++){
		    	if(distMinSmooth>vdistSmooth[i]){
		    		distMinSmooth=vdistSmooth[i];
		    		idxMinSmoothW=i;
		    	}
		    }
		    int idxMinSmooth=new Double(idxMinSmoothW+iDistWInit+nWindowLengthSize-1).intValue();

		    double sRdistSmooth = Math.sqrt(distMinSmooth);
		    double zeroDopplerTimeSmooth = timeStampInterp[idxMinSmooth];
		    
		    double res[]={zeroDopplerTimeSmooth,sRdistSmooth};
		    return res; 
	}
	
	
	
	public static void main(String args[]){
		//String metaF="C:/tmp/sumo_images/S1_PRF_SWATH_DEVEL/S1A_IW_GRDH_1SDV_20150219T053530_20150219T053555_004688_005CB5_3904.SAFE/annotation/s1a-iw-grd-vv-20150219t053530-20150219t053555-004688-005cb5-001.xml";
		//String metaF="C:\\tmp\\sumo_images\\carlos tests\\pixel analysis\\S1A_IW_GRDH_1SDV_20150215T171331_20150215T171356_004637_005B75_CFE1.SAFE\\annotation\\s1a-iw-grd-vv-20150215t171331-20150215t171356-004637-005b75-001.xml";
		String metaF="H:/sat/S1A_IW_GRDH_1SDH_20140607T205125_20140607T205150_000949_000EC8_CDCE.SAFE/annotation/s1a-iw-grd-hh-20140607t205125-20140607t205150-000949-000ec8-001.xml";

		GeoCoding gc=new S1GeoCodingImpl(metaF);
		/*double lat = 52.96606;
		double lon = 4.78491;
		
		
		double r[]=gc.reverse(lat, lon);
		logger.debug("R:"+r[0]+"---"+r[1]);*/
		
		
		double r[]=gc.forward(-100.0,11104.0);
		logger.debug("lon:"+r[0]+"---  lat:"+r[1]);
	}
	
	
	
}
