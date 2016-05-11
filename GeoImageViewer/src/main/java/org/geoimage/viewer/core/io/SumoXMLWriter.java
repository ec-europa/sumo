/*
 *
 */
package org.geoimage.viewer.core.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Precision;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.geoimage.analysis.VDSSchema;
import org.geoimage.def.GeoImageReader;
import org.geoimage.def.GeoTransform;
import org.geoimage.def.SarImageReader;
import org.geoimage.exception.GeoTransformException;
import org.geoimage.viewer.core.GeometryImage;
import org.geoimage.viewer.core.io.sumoxml.Analysis;
import org.geoimage.viewer.core.io.sumoxml.Boat;
import org.geoimage.viewer.core.io.sumoxml.Gcp;
import org.geoimage.viewer.core.io.sumoxml.Gcps;
import org.geoimage.viewer.core.io.sumoxml.SatImageMetadata;
import org.geoimage.viewer.core.io.sumoxml.VdsAnalysis;
import org.geoimage.viewer.core.io.sumoxml.VdsTarget;
import org.geoimage.viewer.core.layers.visualization.AttributesGeometry;
import org.geoimage.viewer.core.layers.visualization.vectors.ComplexEditGeometryVectorLayer.Additionalgeometries;
import org.geoimage.viewer.core.layers.visualization.vectors.ComplexEditVDSVectorLayer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public class SumoXMLWriter extends AbstractVectorIO {
	public static String CONFIG_FILE = "file";
	public static Logger logger = LogManager.getLogger(SumoXMLWriter.class);
	private File input=null;
	//private static javax.xml.bind.JAXBContext jaxbCtx;

	public SumoXMLWriter(File input){
		this.input=input;
	}

	public SumoXMLWriter(){

	}


	@Override
	public ComplexEditVDSVectorLayer read() {
		GeometryImage layer = new GeometryImage(GeometryImage.POINT);
        ComplexEditVDSVectorLayer cev=new ComplexEditVDSVectorLayer(null, input.getName(), layer.getGeometryType(), layer);

		try {
			layer.setProjection("EPSG:4326");
			layer.setName(input.getName());
			
			javax.xml.bind.JAXBContext jaxbCtx = javax.xml.bind.JAXBContext.newInstance("org.geoimage.viewer.core.io.sumoxml");
            InputStream is = new FileInputStream(input );
            javax.xml.bind.Unmarshaller  uMarshaller = jaxbCtx.createUnmarshaller();
            Object o=uMarshaller.unmarshal(is);
            Analysis analysis=(Analysis)o;
            
            SatImageMetadata imgMeta=analysis.getSatImageMetadata();
            VdsAnalysis vds=analysis.getVdsAnalysis();
            VdsTarget targets=analysis.getVdsTarget();
                        
            List<Geometry>boatsAA=new ArrayList<>();
            List<Geometry>boatsPA=new ArrayList<>();
            List<Boat>boats=targets.getBoat();
            GeometryFactory factory=new GeometryFactory();
            int i=0;
            for(Boat b:boats){
            		i++;
        			AttributesGeometry att = new AttributesGeometry(new String[]{"x","y"});
        			Geometry geom=factory.createPoint(new Coordinate(b.getXpixel(),b.getYpixel()));
        			att.set(VDSSchema.ID, i);
					att.set(VDSSchema.MAXIMUM_VALUE,b.getMaxValue());
					att.set(VDSSchema.SIGNIFICANCE,b.getSignificance());
					
					att.set(VDSSchema.THRESHOLD,b.getThresholdTile());
					att.set(VDSSchema.NUMBER_OF_AGGREGATED_PIXELS,b.getNrPixels());
					att.set(VDSSchema.ESTIMATED_LENGTH,b.getLength());
					att.set(VDSSchema.ESTIMATED_WIDTH,b.getWidth());
					att.set(VDSSchema.ESTIMATED_HEADING,b.getHeadingNorth());
					geom.setUserData(att);
            		if(b.getReliability()==3){
            			if(b.getFalseAlarmCause().equals("AA"))
            				boatsAA.add(geom);
            			else
            				boatsPA.add(geom);
            		}else
            			layer.put(geom, att);
            }
    		cev.addArtefactsAmbiguities(boatsPA, true);
    		cev.addAzimuthAmbiguities(boatsAA, true);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		
		
        return cev;

	}

	/**
	 * trunc and round off the milliseconds in a date using 3 digits
	 *
	 * @param date
	 * @return
	 */
	public static String roundedMillis(String date){
		String millis=date.substring(date.indexOf(".")+1,date.length());
		if(millis!=null&&millis.length()>0){
			double val=Double.parseDouble("0."+millis);
			double finalValue = Math.round( val * 1000.0 ) / 1000.0;
			String strFinalVal=""+finalValue;
			String milli=strFinalVal.length()>5?(""+finalValue).substring(2,5):strFinalVal.substring(2);
			date=date.substring(0, date.indexOf(".")+1)+milli;
		}



		return date;
	}



	/**
	 *
	 * @param gLayer
	 * @param projection
	 * @param gir
	 * @param thresholds
	 * @param buffer
	 * @param enl
	 * @param landmask
	 */
	public static void saveNewXML(File output,ComplexEditVDSVectorLayer layer,
			String projection,SarImageReader gir,
			float[] thresholds,int buffer,float enl,
			String landmask,String runVersion,Integer runVersionNumber) {

		SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

		String start=gir.getTimeStampStart();
		start=start.replace("Z","");
		Timestamp tStart=Timestamp.valueOf(start);

		start=roundedMillis(start);

		String stop=gir.getTimeStampStop();
		stop=stop.replace("Z","");
		stop=roundedMillis(stop);



		/**** VDS ANALYSIS ***********/
		VdsAnalysis vdsA = new VdsAnalysis();
		vdsA.setAlgorithm("k-dist");

		vdsA.setBuffer(buffer);
		//TODO: remove the version as constant from here!!!
		vdsA.setDetectorVersion("SUMO_1.3.4");

		/// fields removed in the last xml version
		//add thresholds in order
    	/*vdsA.setThreshHH(thresholds[0]);
    	vdsA.setThreshHV(thresholds[1]);
    	vdsA.setThreshVH(thresholds[2]);
    	vdsA.setThreshVV(thresholds[3]);*/
		//vdsA.setMatrixratio(new Double(0));
		//vdsA.setEnl(enlround);
		//		vdsA.setSumoRunid(0);

		double enlround=Precision.round(enl,2);


		StringBuilder params=new StringBuilder("").append(enlround).append(",");
		if(thresholds!=null && thresholds.length>0){
			String th=Arrays.toString(thresholds);
			th=th.substring(1, th.length()-1);
			params=params.append(th).append(",0.00");

		}
		vdsA.setParameters(params.toString());
		vdsA.setRunTime(format.format(new Date()));

		//TODO: modify the gui to add this fields
		vdsA.setRunVersion(runVersion);
		vdsA.setRunVersionNum(runVersionNumber);

		vdsA.setLandMaskRead(landmask);

		List<Geometry> ambiguity=new ArrayList<>();
		Additionalgeometries amb=layer.getGeometriesByTag(ComplexEditVDSVectorLayer.AZIMUTH_AMBIGUITY_TAG);
		if(amb!=null)
			ambiguity=amb.getGeometries();

		Additionalgeometries art=layer.getGeometriesByTag(ComplexEditVDSVectorLayer.ARTEFACTS_AMBIGUITY_TAG);
		List<Geometry> ambiguityArt=new ArrayList<>();
		if(art!=null)
			ambiguityArt=art.getGeometries();

		/**** VDS TARGETS ***********/
		int targetNumber = 0;
		VdsTarget target = new VdsTarget();

		List<Geometry> gg=layer.getGeometriclayer().getGeometries();
		vdsA.setNrDetections(gg.size());
		for (Geometry geom : gg) {
			AttributesGeometry att = layer.getGeometriclayer().getAttributes(geom);

			/**Boat section **/
			// create new boat
			Boat b = new Boat();
			// set boat target number
			targetNumber++;
			b.setTargetNumber(targetNumber);

			try{
				double[] pos = gir.getGeoTransform().getGeoFromPixel(geom.getCoordinate().x,geom.getCoordinate().y);
				//lat and lon with 6 decimals
				b.setLat(Precision.round(pos[1],6));
				b.setLon(Precision.round(pos[0],6));
			}catch(GeoTransformException e){
				logger.warn(e);
			}
			double incAngle=gir.getIncidence((int)geom.getCoordinate().x);
			incAngle=Math.toDegrees(incAngle);
			b.setIncAng(Precision.round(incAngle,3));

			//x,y without decimal
			b.setXpixel(Precision.round(geom.getCoordinate().x,0));
			b.setYpixel(Precision.round(geom.getCoordinate().y,0));

			//for the moment we leave
			b.setDetecttime(format.format(tStart));

			if(ambiguity.contains(geom)||ambiguityArt.contains(geom)){
				//is an ambiguity
				b.setReliability(3);
				if(ambiguity.contains(geom))
					b.setFalseAlarmCause("AA");//AA is for azimuth ambiguity
				else
					b.setFalseAlarmCause("TP");//TP is for Twin peaks artefacts ambiguity
			}else{
				//is a target
				b.setReliability(0);
			}
			int[] max=(int[])att.get(VDSSchema.MAXIMUM_VALUE);
			if(max!=null){
				String s=StringUtils.join(max,',');
				b.setMaxValue(s);
			}
			if(att.get(VDSSchema.ESTIMATED_LENGTH)!=null){
				double lenght=Precision.round((Double) att.get(VDSSchema.ESTIMATED_LENGTH),1);
				b.setLength(lenght);
				b.setWidth(Precision.round((Double) att.get(VDSSchema.ESTIMATED_WIDTH),1));

				b.setSizeClass("S");//lenght<70
				if(lenght>70 && lenght<=120)
					b.setSizeClass("M");
				else if(lenght>120)
					b.setSizeClass("L");
			}
			if(att.get(VDSSchema.NUMBER_OF_AGGREGATED_PIXELS)!=null)
				b.setNrPixels(((Double)att.get(VDSSchema.NUMBER_OF_AGGREGATED_PIXELS)).intValue());

			if(att.get(VDSSchema.ESTIMATED_HEADING)!=null)
				b.setHeadingNorth(Precision.round((Double)att.get(VDSSchema.ESTIMATED_HEADING),2,BigDecimal.ROUND_FLOOR));

			if(att.get(VDSSchema.SIGNIFICANCE)!=null){
				double[] significance=(double[])att.get(VDSSchema.SIGNIFICANCE);
				if(significance!=null)
					b.setSignificance(StringUtils.join(significance,','));
			}
			if(att.get(VDSSchema.THRESHOLD)!=null){
				double[] trhtile=(double[])att.get(VDSSchema.THRESHOLD);
				if(trhtile!=null)
					b.setThresholdTile(StringUtils.join(trhtile,','));
			}
			target.getBoat().add(b);
		}


		/**** IMAGE METADATA ***********/
		SatImageMetadata imageMeta = new SatImageMetadata();


		try {
			imageMeta.setGcps(getCorners(gir));
			imageMeta.setTimestampStart(tStart.toString());
			imageMeta.setTimeStart(format.format(tStart));

			Timestamp tStop=Timestamp.valueOf(stop);
			imageMeta.setTimeStop(format.format(tStop));

			String sensor=gir.getSensor();
			format=new SimpleDateFormat("yyyyMMdd_HHmmss");
			String imId=sensor+"_"+format.format(tStart);

			imageMeta.setImId(imId);
			imageMeta.setImageName(((SarImageReader)gir).getImgName());

			imageMeta.setSensor(sensor);

			String pol=gir.getPolarization();
			imageMeta.setPol(pol.trim());
			String polNumeric=pol.replace("HH","1");
			polNumeric=polNumeric.replace("HV","2");
			polNumeric=polNumeric.replace("VH","3");
			polNumeric=polNumeric.replace("VV","4");
			if(polNumeric.endsWith(" "))
				polNumeric=polNumeric.substring(0, polNumeric.length()-1);
			polNumeric=polNumeric.replace(" ",",").trim();
			imageMeta.setPolnumeric(polNumeric);
		} catch (Exception e) {
			logger.error(e);
		}


		Analysis an = new Analysis();
		an.setSatImageMetadata(imageMeta);
		an.setVdsAnalysis(vdsA);
		an.setVdsTarget(target);

		/**** SAVING ***********/
		try {
			javax.xml.bind.JAXBContext jaxbCtx = javax.xml.bind.JAXBContext.newInstance("org.geoimage.viewer.core.io.sumoxml");
            OutputStream os = new FileOutputStream(output );
            javax.xml.bind.Marshaller  marshaller = jaxbCtx.createMarshaller();
	        marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_ENCODING, "UTF-8"); //NOI18N
	        marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal( an, os );
            os.close();
        } catch (javax.xml.bind.JAXBException|IOException ex) {
        	logger.error(ex.getMessage(), ex);
		}
	}

	/**
	 * Get Gpcs for corners
	 * @return
	 * @throws GeoTransformException
	 */
	public static Gcps getCorners(GeoImageReader gir) throws GeoTransformException {
		//Corners corners=((SarImageReader)gir).getOriginalCorners();
        double[] topLeft = gir.getGeoTransform().getGeoFromPixel(0, 0);
        double[] topRight = gir.getGeoTransform().getGeoFromPixel(gir.getWidth(), 0);
        double[] bottomLeft = gir.getGeoTransform().getGeoFromPixel(0, gir.getHeight());
        double[] bottomRight = gir.getGeoTransform().getGeoFromPixel(gir.getWidth(), gir.getHeight());

		/*double[] topLeft = gir.getGeoTransform().getGeoFromPixel(0, 0,"EPSG:4326");
		double[] topRight = gir.getGeoTransform().getGeoFromPixel(gir.getWidth(), 0);
		double[] bottomLeft = gir.getGeoTransform().getGeoFromPixel(0,gir.getHeight());
		double[] bottomRight = gir.getGeoTransform().getGeoFromPixel(gir.getWidth(), gir.getHeight());
		*/
		Gcp gcpTopL = new Gcp();
		gcpTopL.setColumn(1);
		gcpTopL.setRow(1);
		gcpTopL.setLon(topLeft[0]);
		gcpTopL.setLat(topLeft[1]);

		Gcp gcpTopR = new Gcp();
		gcpTopR.setColumn(gir.getWidth());
		gcpTopR.setRow(1);
		gcpTopR.setLon(topRight[0]);
		gcpTopR.setLat( topRight[1]);

		Gcp gcpBottomL = new Gcp();
		gcpBottomL.setColumn(1);
		gcpBottomL.setRow(gir.getHeight());
		gcpBottomL.setLon(bottomLeft[0]);
		gcpBottomL.setLat(bottomLeft[1]);

		Gcp gcpBottomR = new Gcp();
		gcpBottomR.setColumn(gir.getWidth());
		gcpBottomR.setRow(gir.getHeight());
		gcpBottomR.setLon(bottomRight[0]);
		gcpBottomR.setLat(bottomRight[1]);

		Gcps gcps = new Gcps();
		gcps.getGcp().add(gcpTopL);
		gcps.getGcp().add(gcpTopR);
		gcps.getGcp().add(gcpBottomL);
		gcps.getGcp().add(gcpBottomR);

		return gcps;
	}




	@Override
	public void save(File output, String projection, GeoTransform transform) {
		// TODO Auto-generated method stub
	}

	public static void main(String[] args){
		SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		SimpleDateFormat format2=new SimpleDateFormat("yyyyMMdd_HHmmSSS");
		String dd="2014-12-12 13:00:44.123";
		try {
			Date d=format.parse(dd);
			String dd2=format2.format(d);
			System.out.println(dd2);

			/**** VDS ANALYSIS ***********/
			VdsAnalysis vdsA = new VdsAnalysis();
			vdsA.setAlgorithm("k-dist");

			vdsA.setBuffer(0);
			vdsA.setDetectorVersion("SUMO_1.2.0");


			vdsA.setParameters("");
			vdsA.setRunTime(format.format(new Date()));

			vdsA.setRunVersion("");
			vdsA.setRunVersionNum(1);


			vdsA.setLandMaskRead("");

			/**** VDS TARGETS ***********/
			int targetNumber = 0;
			VdsTarget target = new VdsTarget();
			for (int i=0;i<3;i++) {
				/**Boat section **/
				// create new boat
				Boat b = new Boat();
				// set boat target number
				targetNumber++;
				b.setTargetNumber(targetNumber);

				b.setLat(0);
				b.setLon(0);

				b.setIncAng(0);

				//x,y without decimal
				b.setXpixel(0);
				b.setYpixel(0);

				//for the moment we leave
				b.setDetecttime(dd);

				b.setReliability(3);
				b.setFalseAlarmCause("AA");//AA is for azimuth ambiguity
				//TODO:change in array
				//b.setMaxValue(0);

				double lenght=0;
				b.setLength(lenght);
				b.setWidth(0);

				b.setSizeClass("S");//lenght<70

				double hn=0;
				b.setHeadingNorth(hn);

				//TODO: add significance and thresholdtile
				b.setSignificance("");
				b.setThresholdTile("");

				target.getBoat().add(b);
			}


			/**** IMAGE METADATA ***********/
			SatImageMetadata imageMeta = new SatImageMetadata();


			try {
				imageMeta.setGcps(new Gcps());
				imageMeta.setTimestampStart(dd);
				imageMeta.setTimeStart(format.format(dd));

				Timestamp tStop=Timestamp.valueOf(dd);
				imageMeta.setTimeStop(format.format(tStop));

				format=new SimpleDateFormat("yyyyMMdd_HHmmss");

				imageMeta.setImId("XX_"+format.format(dd));
				imageMeta.setImageName("TEST");

				imageMeta.setSensor("XX");

				String pol="HH";
				imageMeta.setPol(pol.trim());
				String polNumeric=pol.replace("HH","1");
				polNumeric=polNumeric.replace("HV","2");
				polNumeric=polNumeric.replace("VH","3");
				polNumeric=polNumeric.replace("VV","4");
				if(polNumeric.endsWith(" "))
					polNumeric=polNumeric.substring(0, polNumeric.length()-1);
				polNumeric=polNumeric.replace(" ",",").trim();
				imageMeta.setPolnumeric(polNumeric);
			} catch (Exception e) {
				logger.error(e);
			}


			Analysis an = new Analysis();
			an.setSatImageMetadata(imageMeta);
			an.setVdsAnalysis(vdsA);
			an.setVdsTarget(target);

			/**** SAVING ***********/
			try {
	            javax.xml.bind.JAXBContext jaxbCtx = javax.xml.bind.JAXBContext.newInstance("org.geoimage.viewer.core.io.sumoxml");
	            javax.xml.bind.Marshaller marshaller = jaxbCtx.createMarshaller();
	            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_ENCODING, "UTF-8"); //NOI18N
	            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
	            File output=new File("./test.xml");
	            OutputStream os = new FileOutputStream(output );
	            //marshaller.marshal(an, System.out);
	            marshaller.marshal( an, os );
	            os.close();
	        } catch (javax.xml.bind.JAXBException|IOException ex) {
	        	logger.error(ex.getMessage(), ex);
	        }


		} catch (ParseException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}



}
