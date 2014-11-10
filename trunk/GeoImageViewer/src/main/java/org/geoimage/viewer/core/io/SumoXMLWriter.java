package org.geoimage.viewer.core.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.geoimage.analysis.VDSSchema;
import org.geoimage.def.GeoMetadata;
import org.geoimage.def.SarImageReader;
import org.geoimage.utils.Corners;
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

	@Override
	public GeometricLayer read() {
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

	@Override
	public void save(GeometricLayer gLayer, String projection) {

		int targetNumber = 0;

		VdsAnalysis vdsA = new VdsAnalysis();
		vdsA.setAlgorithm("");
		//vdsA.setAnyDetections(true);
		vdsA.setBuffer(0);
		vdsA.setDetectorVersion("");
		vdsA.setNboat(0);

		vdsA.setThreshHH(new Double(0));
		vdsA.setThreshHV(new Double(0));
		vdsA.setThreshVH(new Double(0));
		vdsA.setThreshVV(new Double(0));
		
		vdsA.setMatrixratio(new Double(0));
		
		vdsA.setEnl(0);
		vdsA.setSumoRunid(0);
		vdsA.setParameters("");
		vdsA.setRunTime("");
		vdsA.setRunVersion("");
		vdsA.setRunVersionOri("");
		
		
		
		VdsTarget target = new VdsTarget();
		for (Geometry geom : gLayer.getGeometries()) {

			Attributes att = gLayer.getAttributes(geom);

			// create new boat
			Boat b = new Boat();
			// set boat target number
			targetNumber++;
			b.setTargetNumber(targetNumber);
			// position pos[0]=lon pos[1] =lat
			double[] pos = gir.getGeoTransform().getGeoFromPixel(geom.getCoordinate().x,geom.getCoordinate().y, "EPSG:4326");
			b.setLat(pos[1]);
			b.setLon(pos[0]);
			b.setXpixel(Math.floor(geom.getCoordinate().x));
			b.setYpixel(Math.floor(geom.getCoordinate().y));
			b.setMaxValue(att.get(VDSSchema.MAXIMUM_VALUE).toString());
			b.setLength((Double) att.get(VDSSchema.ESTIMATED_LENGTH));
			b.setWidth((Double) att.get(VDSSchema.ESTIMATED_WIDTH));
			b.setHeadingNorth((Double) att.get(VDSSchema.ESTIMATED_HEADING));
			target.getBoat().add(b);

			// TODO ask for this field: RUNID
			// Element runid = new Element("runid");
			// runid.setText(""+att.get(VDSSchema.RUN_ID));

			// TODO fields not used in new xml, to verify
			// Element typeAnalysis = new Element("type");
			// typeAnalysis.setText("VDS");
			// boat.addContent(typeAnalysis);
			// Element averageTile = new Element("averageTile");
			// averageTile.setText(""+att.get(VDSSchema.TILE_AVERAGE));
			// boat.addContent(averageTile);

			// Element tileSTD = new Element("tileSTD");
			// tileSTD.setText(""+att.get(VDSSchema.TILE_STANDARD_DEVIATION));
			// boat.addContent(tileSTD);
			// Element subObjs = new Element("subObjs");
			// subObjs.setText(""+att.get(VDSSchema.NUMBER_OF_AGGREGATED_PIXELS));
			// boat.addContent(subObjs);
			// Element mySize = new Element("sizeClassification");
			// mySize.setText(att.get(VDSSchema.ESTIMATED_LENGTH) + "");
			// boat.addContent(mySize);
		}
		
		SatImageMetadata imageMeta = new SatImageMetadata();
		imageMeta.setGcps(getCorners());
		
		
		//TODO set the correct date
		GregorianCalendar c = new GregorianCalendar();
		try {
			Timestamp start=(Timestamp)gir.getMetadata(GeoMetadata.TIMESTAMP_START);
			Timestamp stop=(Timestamp)gir.getMetadata(GeoMetadata.TIMESTAMP_STOP);

			c.setTimeInMillis(start.getTime());
			XMLGregorianCalendar startCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
			c.setTimeInMillis(stop.getTime());
			XMLGregorianCalendar stopCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
			
			imageMeta.setTimeStart(startCalendar);
			imageMeta.setTimeStop(stopCalendar);
			imageMeta.setTimestampStart(startCalendar);

		} catch (DatatypeConfigurationException e) {
			e.printStackTrace();
		}
		
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
	public Gcps getCorners() {
		Corners corners=((SarImageReader)gir).getOriginalCorners();
		
		
		
		/*double[] topLeft = gir.getGeoTransform().getGeoFromPixel(0, 0,"EPSG:4326");
		double[] topRight = gir.getGeoTransform().getGeoFromPixel(gir.getWidth(), 0, "EPSG:4326");
		double[] bottomLeft = gir.getGeoTransform().getGeoFromPixel(0,gir.getHeight(), "EPSG:4326");
		double[] bottomRight = gir.getGeoTransform().getGeoFromPixel(gir.getWidth(), gir.getHeight(), "EPSG:4326");
		*/
		Gcp gcpTopL = new Gcp();
		gcpTopL.setColumn(corners.getTopLeft().getOriginalXpix());
		gcpTopL.setRow(corners.getTopLeft().getYpix());
		gcpTopL.setLon(corners.getTopLeft().getXgeo());
		gcpTopL.setLat(corners.getTopLeft().getYgeo());

		Gcp gcpTopR = new Gcp();
		gcpTopR.setColumn(corners.getTopRight().getOriginalXpix());
		gcpTopR.setRow(corners.getTopRight().getYpix());
		gcpTopR.setLon(corners.getTopRight().getXgeo());
		gcpTopR.setLat(corners.getTopRight().getYgeo());

		Gcp gcpBottomL = new Gcp();
		gcpBottomL.setColumn(corners.getBottomLeft().getOriginalXpix());
		gcpBottomL.setRow(corners.getBottomLeft().getYpix());
		gcpBottomL.setLon(corners.getBottomLeft().getXgeo());
		gcpBottomL.setLat(corners.getBottomLeft().getYgeo());

		Gcp gcpBottomR = new Gcp();
		gcpBottomR.setColumn(corners.getBottomRight().getOriginalXpix());
		gcpBottomR.setRow(corners.getBottomRight().getYpix());
		gcpBottomR.setLon(corners.getBottomRight().getXgeo());
		gcpBottomR.setLat(corners.getBottomRight().getYgeo());

		Gcps gcps = new Gcps();
		gcps.getGcp().add(gcpTopL);
		gcps.getGcp().add(gcpTopR);
		gcps.getGcp().add(gcpBottomL);
		gcps.getGcp().add(gcpBottomR);
		
		return gcps;
	}

}
