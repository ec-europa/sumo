package org.geoimage.viewer.core.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.math3.util.Precision;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.geoimage.analysis.VDSSchema;
import org.geoimage.def.GeoImageReader;
import org.geoimage.def.SarImageReader;
import org.geoimage.viewer.core.api.Attributes;
import org.geoimage.viewer.core.api.GeometricLayer;
import org.geoimage.viewer.core.api.VDSFields;
import org.geoimage.viewer.core.io.sumoxml.Analysis;
import org.geoimage.viewer.core.io.sumoxml.Boat;
import org.geoimage.viewer.core.io.sumoxml.Gcp;
import org.geoimage.viewer.core.io.sumoxml.Gcps;
import org.geoimage.viewer.core.io.sumoxml.SatImageMetadata;
import org.geoimage.viewer.core.io.sumoxml.VdsAnalysis;
import org.geoimage.viewer.core.io.sumoxml.VdsTarget;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

public class SumoXMLWriter extends AbstractVectorIO {
	public static String CONFIG_FILE = "file";
	final Logger logger = Logger.getLogger(SumoXMLWriter.class);
	
	
	public SumoXMLWriter(){
	}
	
	@Override
	public GeometricLayer read(GeoImageReader gir) {
		GeometricLayer layer = null;
		try {
			layer = new GeometricLayer(GeometricLayer.POINT);

			// create xml doc
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(new File((String) config.get(CONFIG_FILE)));

			GeometryFactory gf = new GeometryFactory();
			String[] schema = VDSFields.getSchema();
			String[] types = VDSFields.getTypes();
			Element root = doc.getRootElement().getChild("image");
			if (root != null) {
				layer.setGeometryType(GeometricLayer.MIXED);
				Element gcps = root.getChild("gcps");
				if (gcps != null) {
					Coordinate[] coords = new Coordinate[gcps.getChildren("gcp").size() + 1];
					int i = 0;
					for (Object gcp : gcps.getChildren("gcp")) {
						if (gcp instanceof Element) {
							double lon = Double.parseDouble(((Element) gcp).getChild("lon").getValue());
							double lat = Double.parseDouble(((Element) gcp).getChild("lat").getValue());
							coords[i] = new Coordinate(lon, lat);
							// close the ring
							if (i == 0) {
								coords[gcps.getChildren("gcp").size()] = new Coordinate(lon, lat);
							}
							i++;
						}
					}
					Polygon frame = gf.createPolygon(gf.createLinearRing(coords), null);
					Attributes atts = Attributes.createAttributes(schema, types);
					layer.put(frame.convexHull(), atts);
				}
			}
			root = doc.getRootElement().getChild("boatlist");
			if (root != null) {

				layer.setProjection("EPSG:4326");
				layer.setName(new File((String) config.get(CONFIG_FILE))
						.getName());
				for (Object obj : root.getChildren()) {
					if (obj instanceof Element) {
						Element boat = (Element) obj;
						if (boat.getName().equals("boat")) {
							Attributes atts = Attributes.createAttributes(
									schema, types);
							double lon = Double.parseDouble(boat
									.getChild("lon").getValue());
							double lat = Double.parseDouble(boat
									.getChild("lat").getValue());
							Geometry geom = gf.createPoint(new Coordinate(lon,
									lat));
							layer.put(geom, atts);
							try {
								atts.set(VDSSchema.ID, Double.parseDouble(boat.getChild("id").getValue()));
								atts.set(VDSSchema.MAXIMUM_VALUE,Double.parseDouble(boat.getChild("maxValue").getValue()));
								atts.set(VDSSchema.TILE_AVERAGE,Double.parseDouble(boat.getChild("averageTile").getValue()));
								atts.set(VDSSchema.TILE_STANDARD_DEVIATION,Double.parseDouble(boat.getChild("tileSTD").getValue()));
								atts.set(VDSSchema.THRESHOLD,Double.parseDouble(boat.getChild("maxValue").getValue()));
								atts.set(VDSSchema.NUMBER_OF_AGGREGATED_PIXELS,Double.parseDouble(boat.getChild("subObjs").getValue()));
								atts.set(VDSSchema.RUN_ID,boat.getChild("runid").getValue());
								atts.set(VDSSchema.ESTIMATED_LENGTH,Double.parseDouble(boat.getChild("length").getValue()));
								atts.set(VDSSchema.ESTIMATED_WIDTH,Double.parseDouble(boat.getChild("width").getValue()));
								atts.set(VDSSchema.ESTIMATED_HEADING,Double.parseDouble(boat.getChild("heading").getValue()));
							} catch (Exception ex) {
								logger.warn(ex.getMessage(), ex);
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return layer;
	}
	
	/**
	 * trunc and round off the milliseconds in a date using 3 digits
	 * 
	 * @param date
	 * @return
	 */
	private String roundedMillis(String date){
		String millis=date.substring(date.indexOf(".")+1,date.length());
		if(millis!=null&&millis.length()>0){
			double val=Double.parseDouble("0."+millis);
			double finalValue = Math.round( val * 1000.0 ) / 1000.0;
			String strFinalVal=""+finalValue;
			String milli=strFinalVal.length()>5?(""+finalValue).substring(2,5):strFinalVal.substring(2);
			
			date=date.substring(0, date.indexOf(".")+1)+milli;
			
			//System.out.println(millis+"   "+val+ "  "+finalValue+"  "+date);
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
	public void saveNewXML(GeometricLayer gLayer, String projection,SarImageReader gir,float[] thresholds,int buffer,double enl,String landmask) {
		SimpleDateFormat format=new SimpleDateFormat("YYYY-MM-dd HH:mm:ss.SSS");
		
		String start=gir.getTimeStampStart();
		Timestamp tStart=Timestamp.valueOf(start);
		
		start=start.replace("Z","");
		start=roundedMillis(start);
		
		String stop=gir.getTimeStampStop();
		stop=stop.replace("Z","");
		stop=roundedMillis(stop);
		
		
		
		/**** VDS ANALYSIS ***********/
		VdsAnalysis vdsA = new VdsAnalysis();
		vdsA.setAlgorithm("k-dist");
		
		vdsA.setBuffer(buffer);
		vdsA.setDetectorVersion("");
		
		//add thresholds in order
    	vdsA.setThreshHH(thresholds[0]);
    	vdsA.setThreshHV(thresholds[1]);
    	vdsA.setThreshVH(thresholds[2]);
    	vdsA.setThreshVV(thresholds[3]);
    	
		vdsA.setMatrixratio(new Double(0));
		double enlround=Precision.round(enl,2); 
		vdsA.setEnl(enlround);
		vdsA.setSumoRunid(0);
		
		StringBuilder params=new StringBuilder("").append(enlround).append(",");
		if(thresholds!=null && thresholds.length>0){
			String th=Arrays.toString(thresholds);
			th=th.substring(1, th.length()-1);
			
			vdsA.setThreshOrderChans(th);
			params=params.append(th).append(",0.00");
			
		}
		vdsA.setParameters(params.toString());
		vdsA.setRunTime(format.format(new Date()));

		vdsA.setRunVersion("");
		vdsA.setRunVersionOri("");
		
		vdsA.setLandMaskRead(landmask);
		
		
		/**** VDS TARGETS ***********/
		int targetNumber = 0;
		VdsTarget target = new VdsTarget();
		for (Geometry geom : gLayer.getGeometries()) {

			Attributes att = gLayer.getAttributes(geom);

			/**Boat section **/
			// create new boat
			Boat b = new Boat();
			// set boat target number
			targetNumber++;
			b.setTargetNumber(targetNumber);
			// position pos[0]=lon pos[1] =lat
			double[] pos = gir.getGeoTransform().getGeoFromPixel(geom.getCoordinate().x,geom.getCoordinate().y);
			
			//lat and lon with 6 decimals
			b.setLat(Precision.round(pos[1],6));
			b.setLon(Precision.round(pos[0],6));
			//x,y without decimal
			b.setXpixel(Precision.round(geom.getCoordinate().x,0));
			b.setYpixel(Precision.round(geom.getCoordinate().y,0));
			
			//for the moment we leave 
			b.setDetecttime(format.format(tStart));
			
			b.setMaxValue(att.get(VDSSchema.MAXIMUM_VALUE).toString());
			
			double lenght=Precision.round((Double) att.get(VDSSchema.ESTIMATED_LENGTH),1);
			b.setLength(lenght);
			b.setWidth(Precision.round((Double) att.get(VDSSchema.ESTIMATED_WIDTH),1));
			
			b.setSizeClass("S");//lenght<70
			if(lenght>70 && lenght<=120)
				b.setSizeClass("M");
			else if(lenght>120)
				b.setSizeClass("L");
			
			b.setNrPixels(null);
			b.setHeadingRange(null);

			b.setNoise(null);
			
			b.setHeadingNorth((Double) att.get(VDSSchema.ESTIMATED_HEADING));
			target.getBoat().add(b);
		}
		vdsA.setNboat(targetNumber);
		vdsA.setAnyDetections(targetNumber>0);
		
		
		/**** IMAGE METADATA ***********/
		SatImageMetadata imageMeta = new SatImageMetadata();
		imageMeta.setGcps(getCorners(gir));
		
		try {
			
			
			
			imageMeta.setTimestampStart(tStart.toString());
			
			imageMeta.setTimeStart(format.format(tStart));
			
			Timestamp tStop=Timestamp.valueOf(stop);
			imageMeta.setTimeStop(format.format(tStop));
			
			String sensor=gir.getSensor();
			format=new SimpleDateFormat("yyyyMMdd_HHmmss");
			
			imageMeta.setImId(sensor+"_"+format.format(tStart));
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
			e.printStackTrace();
		}
		
		/**** SAVING ***********/
		Analysis an = new Analysis();
		an.setSatImageMetadata(imageMeta);
		an.setVdsAnalysis(vdsA);
		an.setVdsTarget(target);
		
		try {            
            javax.xml.bind.JAXBContext jaxbCtx = javax.xml.bind.JAXBContext.newInstance("org.geoimage.viewer.core.io.sumoxml");
            javax.xml.bind.Marshaller marshaller = jaxbCtx.createMarshaller();
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_ENCODING, "UTF-8"); //NOI18N
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            File fout=new File((String) config.get(CONFIG_FILE) );
            OutputStream os = new FileOutputStream(fout );
            //marshaller.marshal(an, System.out);
            marshaller.marshal( an, os );
            os.close();
        } catch (javax.xml.bind.JAXBException ex) {
        	logger.log(Level.ERROR, null, ex);
        } catch (FileNotFoundException e) {
        	logger.log(Level.ERROR, null, e);
		} catch (IOException e) {
			logger.log(Level.ERROR, null, e);

		}
	}
	
	/**
	 * Get Gpcs for corners
	 * @return
	 */
	public Gcps getCorners(GeoImageReader gir) {
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
		gcpTopL.setColumn(0);
		gcpTopL.setRow(0);
		gcpTopL.setLon(topLeft[0]);
		gcpTopL.setLat(topLeft[1]);

		Gcp gcpTopR = new Gcp();
		gcpTopR.setColumn(gir.getWidth());
		gcpTopR.setRow(0);
		gcpTopR.setLon(topRight[0]);
		gcpTopR.setLat( topRight[1]);

		Gcp gcpBottomL = new Gcp();
		gcpBottomL.setColumn(0);
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
	
	
	
	public static void main(String[] args){
		SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		SimpleDateFormat format2=new SimpleDateFormat("yyyyMMdd_HHmmSSS");
		String dd="2014-12-12 13:00:44.123";
		try {
			Date d=format.parse(dd);
			String dd2=format2.format(d);
			System.out.println(dd2);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void save(GeometricLayer layer, String projection, SarImageReader gir) {
		
	}

	
	
}
