package jrc.it.geolocation.geo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.LoggerFactory;

import jrc.it.geolocation.common.GeoUtils;
import jrc.it.geolocation.exception.GeoLocationException;
import jrc.it.geolocation.exception.MathException;
import jrc.it.geolocation.interpolation.OrbitInterpolation;
import jrc.it.geolocation.metadata.IMetadata.OrbitStatePosVelox;
import jrc.it.geolocation.metadata.impl.S1Metadata;

public class S1GeoCodingImpl extends AbstractGeoCoding {
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(S1GeoCodingImpl.class);
	
	private final int  iSafetyBufferAz=500;
	private S1Metadata s1Meta;
	
	/**
	 * 
	 * @param metaFile
	 * @throws MathException
	 * @throws JAXBException 
	 */
	public S1GeoCodingImpl(final S1Metadata metaData) throws MathException, JAXBException{
		super(metaData);
		this.s1Meta=(S1Metadata)super.meta;
		
		double zTimeFirstInSeconds=s1Meta.getZeroDopplerTimeFirstLineSeconds().getTimeInMillis()/1000.0;
		double zTimeLastInSeconds=s1Meta.getZeroDopplerTimeLastLineSeconds().getTimeInMillis()/1000.0;
		
		orbitInterpolation=new OrbitInterpolation();
		List<OrbitStatePosVelox> l=new ArrayList<>();
		l.addAll( s1Meta.getOrbitStatePosVelox());
		orbitInterpolation.orbitInterpolation(l,zTimeFirstInSeconds,zTimeLastInSeconds,s1Meta.getSamplingf(),iSafetyBufferAz);
		coordConv=s1Meta.getCoordinateConversion();
		statepVecInterp=Collections.unmodifiableList(orbitInterpolation.getStatepVecInterp());
		statevVecInterp=Collections.unmodifiableList(orbitInterpolation.getStatevVecInterp());
	}
	
	
	
	
	
	
	public static void main(String args[]){
		String metaF="F:/SumoImgs/carlos tests/new s1 problem with geoloc/"
				+ "S1A_IW_GRDH_1SSV_20151129T081958_20151129T082027_008817_00C94D_93C4.SAFE/annotation/s1a-iw-grd-vv-20151129t081958-20151129t082027-008817-00c94d-001.xml";
		//String metaF="F:/SumoImgs/carlos tests/analysis on land/S1A_EW_GRDH_1SDV_20141019T064549_20141019T064649_002895_003478_47B4.SAFE/annotation/s1a-ew-grd-vv-20141019t064549-20141019t064649-002895-003478-001.xml";
		//"Z://Radar-Images//S1PmarMase//S1//IW//S1A_IW_GRDH_1SDV_20150401T162928_20150401T162953_005292_006B1C_01C1.SAFE//annotation//s1a-iw-grd-vv-20150401t162928-20150401t162953-005292-006b1c-001.xml";
		//String metaF="H:/sat/S1A_IW_GRDH_1SDV_20150401T145242_20150401T145301_005291_006B16_EDD7.SAFE/annotation/s1a-iw-grd-vv-20150401t145242-20150401t145301-005291-006b16-001.xml";
		
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
			S1Metadata meta =new S1Metadata(metaF);
			meta.initMetaData();
			gc = new S1GeoCodingImpl(meta);
			
			double lat = 7.900084;
			double lon = 9.06636;
			double p=9342;
			double l=5000;
			
			try {

				System.out.println("Height:"+GeoUtils.getGeoidH(lon, lat));
				//double r[] = gc.pixelFromGeo(lon,lat); //r[o]=p r[1]=l
				//System.out.println("P:"+r[0]+" --- L:"+r[1]);
				
				/*double r2[] = gc.geoFromPixel(r[1],r[0]);//r[0]=lon r[1]=lat
				System.out.println("LON:"+r2[0]+" --- LAT:"+r2[1]);
				 */				
				
				double r3[] = gc.geoFromPixel(l,p);//r[0]=lon r[1]=lat
				System.out.println("LON:"+r3[0]+" --- LAT:"+r3[1]);
			
				//r = gc.geoFromPixel(line,pixel);
				//System.out.println(""+r[1]+" --- "+r[0]);
			} catch (GeoLocationException e) {
				e.printStackTrace();
			}
			//double r[]=gc.forward(-100.0,11104.0);
			//logger.debug("lon:"+r[0]+"---  lat:"+r[1]);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
	}
	
	
	
}
