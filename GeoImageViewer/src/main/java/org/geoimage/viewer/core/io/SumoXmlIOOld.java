/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.core.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.geoimage.analysis.VDSSchema;
import org.geoimage.def.GeoImageReader;
import org.geoimage.def.GeoTransform;
import org.geoimage.def.SarImageReader;
import org.geoimage.exception.GeoTransformException;
import org.geoimage.viewer.core.GeometryImage;
import org.geoimage.viewer.core.layers.visualization.AttributesGeometry;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

/**
 *
 * @author thoorfr
 */
public class SumoXmlIOOld extends AbstractVectorIO {
    public static String CONFIG_FILE = "file";
    final Logger logger = LogManager.getLogger(SumoXmlIOOld.class);
    private File input=null;
    private GeometryImage glayer=null;
    
    
    public SumoXmlIOOld(File file){
    	this.input=file;
	}
    
    public void read() {
    	glayer=readLayer();
    }
    
    public GeometryImage readLayer() {
        try {
            GeometryImage layer = new GeometryImage(GeometryImage.POINT);
            
            //create xml doc
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(input);

            
            GeometryFactory gf = new GeometryFactory();
            Element root = doc.getRootElement().getChild("image");
            if (root != null) {
                layer.setGeometryType(GeometryImage.MIXED);
                Element gcps = root.getChild("gcps");
                if (gcps != null) {
                    Coordinate[] coords = new Coordinate[gcps.getChildren("gcp").size() + 1];
                    int i = 0;
                    for (Object gcp : gcps.getChildren("gcp")) {
                        if (gcp instanceof Element) {
                            double lon = Double.parseDouble(((Element) gcp).getChild("lon").getValue());
                            double lat = Double.parseDouble(((Element) gcp).getChild("lat").getValue());
                            coords[i] = new Coordinate(lon, lat);
                            //close the ring
                            if (i == 0) {
                                coords[gcps.getChildren("gcp").size()] = new Coordinate(lon, lat);
                            }
                            i++;
                        }
                    }
                    Polygon frame = gf.createPolygon(gf.createLinearRing(coords), null);
                    AttributesGeometry atts = new AttributesGeometry(VDSSchema.schema);
                    layer.put(frame.convexHull(), atts);
                }
            }
            root = doc.getRootElement().getChild("boatlist");
            if (root != null) {

                layer.setProjection("EPSG:4326");
                layer.setName(input.getName());
                for (Object obj : root.getChildren()) {
                    if (obj instanceof Element) {
                        Element boat = (Element) obj;
                        if (boat.getName().equals("boat")) {
                            AttributesGeometry atts = new AttributesGeometry(VDSSchema.schema);
                            double lon = Double.parseDouble(boat.getChild("lon").getValue());
                            double lat = Double.parseDouble(boat.getChild("lat").getValue());
                            Geometry geom = gf.createPoint(new Coordinate(lon, lat));
                            layer.put(geom, atts);
                            try {
                                atts.set(VDSSchema.ID, Double.parseDouble(boat.getChild("id").getValue()));
                                atts.set(VDSSchema.MAXIMUM_VALUE, Double.parseDouble(boat.getChild("maxValue").getValue()));
                                atts.set(VDSSchema.TILE_AVERAGE, Double.parseDouble(boat.getChild("averageTile").getValue()));
                                atts.set(VDSSchema.TILE_STANDARD_DEVIATION, Double.parseDouble(boat.getChild("tileSTD").getValue()));
                                atts.set(VDSSchema.THRESHOLD, Double.parseDouble(boat.getChild("maxValue").getValue()));
                                atts.set(VDSSchema.NUMBER_OF_AGGREGATED_PIXELS, Double.parseDouble(boat.getChild("subObjs").getValue()));
                                atts.set(VDSSchema.RUN_ID, boat.getChild("runid").getValue());
                                atts.set(VDSSchema.ESTIMATED_LENGTH, Double.parseDouble(boat.getChild("length").getValue()));
                                atts.set(VDSSchema.ESTIMATED_WIDTH, Double.parseDouble(boat.getChild("width").getValue()));
                                atts.set(VDSSchema.ESTIMATED_HEADING, Double.parseDouble(boat.getChild("heading").getValue()));
                            } catch (Exception ex) {
                            	logger.warn(ex.getMessage(),ex);
                            }
                        }
                    }
                }
            }
            return layer;
        } catch (Exception ex) {
            logger.error(ex.getMessage(),ex);
        }
        return null;
    }

    public static void export(File output,GeometryImage glayer, String projection,SarImageReader gir) {
    	 try {
	    	GeoTransform transform=gir.getGeoTransform();
	    	
	        Element root = new Element("analysis");
	        Element image = new Element("image_information");
	        Element name = new Element("name");
	        String filename = gir.getFilesList()[0].replaceAll("\\\\", "/");
	        filename = filename.substring(filename.lastIndexOf("/") + 1);
	        name.setText(filename);
	        image.addContent(name);
	        Element type = new Element("type");
	        type.setText(gir.getFormat());
	        image.addContent(type);
	        Element mode = new Element("mode");
	        mode.setText(gir.getMode());
	        image.addContent(mode);
	        Element polarisation = new Element("polarisation");
	        polarisation.setText("" + gir.getBandName(0));
	        image.addContent(polarisation);
	        Element time = new Element("timestampStart");
	        time.setText(gir.getTimeStampStart());
	        image.addContent(time);
	        time = new Element("timestampStop");
	        time.setText(gir.getTimeStampStop());
	        image.addContent(time);
	        Element resolutionRange = new Element("resolutionRange");
	        resolutionRange.setText(gir.getRangeSpacing().toString());
	        image.addContent(resolutionRange);
	        Element resolutionAzimuth = new Element("resolutionAzimuth");
	        resolutionAzimuth.setText(gir.getAzimuthSpacing().toString());
	        image.addContent(resolutionAzimuth);
	        Element sizeX = new Element("width");
	        sizeX.setText(""+gir.getWidth());
	        image.addContent(sizeX);
	        Element sizeY = new Element("height");
	        sizeY.setText("" + gir.getHeight());
	        image.addContent(sizeY);
	        Element heading = new Element("heading");
	        Object o=gir.getHeadingAngle();
	        if(o!=null)
	        	heading.setText(gir.getHeadingAngle().toString());
	        image.addContent(heading);
	        image.addContent(getCorners(gir));
	        root.addContent(image);
	
	
	        Element boatlist = new Element("boatlist");
	        int number=0;
	        for (Geometry geom : glayer.getGeometries()) {
	
	            AttributesGeometry att = glayer.getAttributes(geom);
	            String[] atts = att.getSchema();
	            Element boat = new Element("boat");
	            Element xPixel = new Element("xPixel");
	            xPixel.setText(Math.floor(geom.getCoordinate().x) + "");
	            boat.addContent(xPixel);
	            Element yPixel = new Element("yPixel");
	            yPixel.setText(Math.floor(geom.getCoordinate().y) + "");
	            boat.addContent(yPixel);
	            double[] pos = gir.getGeoTransform().getGeoFromPixel(geom.getCoordinate().x, geom.getCoordinate().y);
	            Element lon = new Element("lon");
	            lon.setText(pos[0] + "");
	            boat.addContent(lon);
	            Element lat = new Element("lat");
	            lat.setText(pos[1] + "");
	            boat.addContent(lat);
	            Element id = new Element("id");
	            id.setText(""+number++);
	            boat.addContent(id);
	            Element runid = new Element("runid");
	            runid.setText(""+att.get(VDSSchema.RUN_ID));
	            boat.addContent(runid);
	
	            Element typeAnalysis = new Element("type");
	            typeAnalysis.setText("VDS");
	            boat.addContent(typeAnalysis);
	
	            Element maxValue = new Element("maxValue");
	            maxValue.setText(""+att.get(VDSSchema.MAXIMUM_VALUE));
	            boat.addContent(maxValue);
	
	            Element averageTile = new Element("averageTile");
	            averageTile.setText(""+att.get(VDSSchema.TILE_AVERAGE));
	            boat.addContent(averageTile);
	
	            Element tileSTD = new Element("tileSTD");
	            tileSTD.setText(""+att.get(VDSSchema.TILE_STANDARD_DEVIATION));
	            boat.addContent(tileSTD);
	
	            Element subObjs = new Element("subObjs");
	            subObjs.setText(""+att.get(VDSSchema.NUMBER_OF_AGGREGATED_PIXELS));
	            boat.addContent(subObjs);
	
	
	            Element lenght = new Element("length");
	            lenght.setText(att.get(VDSSchema.ESTIMATED_LENGTH) + "");
	            boat.addContent(lenght);
	
	            Element width = new Element("width");
	            width.setText(att.get(VDSSchema.ESTIMATED_WIDTH) + "");
	            boat.addContent(width);
	
	            Element course = new Element("heading");
	            course.setText(att.get(VDSSchema.ESTIMATED_HEADING) + "");
	            boat.addContent(course);
	
	            Element mySize = new Element("sizeClassification");
	            mySize.setText(att.get(VDSSchema.ESTIMATED_LENGTH) + "");
	            boat.addContent(mySize);
	            boatlist.addContent(boat);
	        }
	        root.addContent(boatlist);
	
	
	        org.jdom.Document doc = new org.jdom.Document(root);
	        XMLOutputter serializer = new XMLOutputter();
	        serializer.setFormat(Format.getPrettyFormat());
	        // System.out.println(serializer.outputString(doc));
       
            BufferedWriter out = new BufferedWriter(new FileWriter(output));
            out.write(serializer.outputString(doc));
            out.close();
        } catch (IOException |GeoTransformException e) {
        }
    }

    /*public void save(GeometricLayer glayer, String projection) {
    Element root = new Element("analysis");
    Element image = new Element("image");
    Element name = new Element("name");
    String filename = gir.getFilesList()[0].replaceAll("\\\\", "/");
    filename = filename.substring(filename.lastIndexOf("/") + 1);
    name.setText(filename);
    image.addContent(name);
    Element type = new Element("type");
    type.setText(gir.getFormat());
    image.addContent(type);
    Element mode = new Element("mode");
    mode.setText("");
    image.addContent(mode);
    Element polarisation = new Element("polarisation");
    polarisation.setText(gir.getBandName(0));
    image.addContent(polarisation);
    Element time = new Element("timestampStart");
    time.setText("" + gir.getMetadata(GeoMetadata.TIMESTAMP_START));
    image.addContent(time);
    time = new Element("timestampStop");
    time.setText("" + gir.getMetadata(GeoMetadata.TIMESTAMP_STOP));
    image.addContent(time);
    Element resolutionRange = new Element("resolutionRange");
    resolutionRange.setText("");
    image.addContent(resolutionRange);
    Element resolutionAzimuth = new Element("resolutionAzimuth");
    resolutionAzimuth.setText("");
    image.addContent(resolutionAzimuth);
    Element sampleDistanceRange = new Element("sampleDistanceRange");
    sampleDistanceRange.setText(Double.toString(gir.getGeoTransform().getPixelSize()[1]));
    image.addContent(sampleDistanceRange);
    Element sampleDistanceAzimuth = new Element("sampleDistanceAzimuth");
    sampleDistanceAzimuth.setText(Double.toString(gir.getGeoTransform().getPixelSize()[0]));
    image.addContent(sampleDistanceAzimuth);
    Element sizeX = new Element("sizeX");
    sizeX.setText(gir.getWidth() + "");
    image.addContent(sizeX);
    Element sizeY = new Element("sizeY");
    sizeY.setText(gir.getHeight() + "");
    image.addContent(sizeY);
    image.addContent(getCorners(gir));
    root.addContent(image);

    Element algorithm = new Element("algorithm");
    root.addContent(algorithm);

    Element boatlist = new Element("boatlist");
    for (Geometry geom : glayer.getGeometries()) {
    Attributes att = glayer.getAttributes(geom);
    Element boat = new Element("boat");
    Element id = new Element("id");
    id.setText("" + att.get("id"));
    boat.addContent(id);
    Element runid = new Element("runid");
    runid.setText(att.get("runid").toString());
    boat.addContent(runid);

    Element typeAnalysis = new Element("type");
    typeAnalysis.setText("VDS");
    boat.addContent(typeAnalysis);

    Element xPixel = new Element("xPixel");
    xPixel.setText(geom.getCoordinate().x + "");
    boat.addContent(xPixel);

    Element yPixel = new Element("yPixel");
    yPixel.setText(geom.getCoordinate().y + "");
    boat.addContent(yPixel);

    Element maxValue = new Element("maxValue");
    maxValue.setText(att.get("maximum value").toString());
    boat.addContent(maxValue);

    Element averageTile = new Element("averageTile");
    averageTile.setText(att.get("tile average").toString());
    boat.addContent(averageTile);

    Element tileSTD = new Element("tileSTD");
    tileSTD.setText(att.get("tile standard deviation").toString());
    boat.addContent(tileSTD);

    Element subObjs = new Element("subObjs");
    subObjs.setText(att.get("num pixels").toString());
    boat.addContent(subObjs);

    double[] pos = gir.getGeoTransform().getGeoFromPixel(geom.getCoordinate().x, geom.getCoordinate().y);

    Element lon = new Element("lon");
    lon.setText(pos[0] + "");
    boat.addContent(lon);

    Element lat = new Element("lat");
    lat.setText(pos[1] + "");
    boat.addContent(lat);

    Element lenght = new Element("length");
    lenght.setText(att.get("Estimated Length") + "");
    boat.addContent(lenght);

    Element width = new Element("width");
    width.setText(att.get("Estimated Width") + "");
    boat.addContent(width);

    Element heading = new Element("heading");
    heading.setText(att.get("Estimated Heading") + "");
    boat.addContent(heading);

    Element mySize = new Element("sizeClassification");
    mySize.setText(att.get("Estimated Length")+"");
    boat.addContent(mySize);

    boatlist.addContent(boat);
    }
    root.addContent(boatlist);


    org.jdom.Document doc = new org.jdom.Document(root);
    XMLOutputter serializer = new XMLOutputter();
    serializer.setFormat(Format.getPrettyFormat());
    // System.out.println(serializer.outputString(doc));
    try {
    BufferedWriter out = new BufferedWriter(new FileWriter((String) config.get(CONFIG_FILE)));
    out.write(serializer.outputString(doc));
    out.close();
    } catch (IOException e) {
    }
    }*/
    public static Element getCorners(GeoImageReader gir) throws GeoTransformException {
        Element gcps = new Element("gcps");
        double[] topLeft = gir.getGeoTransform().getGeoFromPixel(0, 0);
        double[] topRight = gir.getGeoTransform().getGeoFromPixel(gir.getWidth(), 0);
        double[] bottomLeft = gir.getGeoTransform().getGeoFromPixel(0, gir.getHeight());
        double[] bottomRight = gir.getGeoTransform().getGeoFromPixel(gir.getWidth(), gir.getHeight());
        gcps.addContent(createGcp(topLeft[0], topLeft[1], 0, 0));
        gcps.addContent(createGcp(topRight[0], topRight[1], gir.getWidth(), 0));
        gcps.addContent(createGcp(bottomLeft[0], bottomLeft[1], 0, gir.getHeight()));
        gcps.addContent(createGcp(bottomRight[0], bottomRight[1], gir.getWidth(), gir.getHeight()));
        return gcps;
    }

    public static Element createGcp(double lon, double lat, double x, double y) {
        Element gcp = new Element("gcp");

        Element row = new Element("row");
        row.setText((int) y + "");
        gcp.addContent(row);

        Element column = new Element("column");
        column.setText((int) x + "");
        gcp.addContent(column);

        Element lone = new Element("lon");
        lone.setText(lon + "");
        gcp.addContent(lone);

        Element late = new Element("lat");
        late.setText(lat + "");
        gcp.addContent(late);

        return gcp;
    }


	@Override
	public void save(File output, String projection,GeoTransform transform) {
		// TODO Auto-generated method stub
		
	}



}
