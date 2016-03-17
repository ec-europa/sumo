/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.core.io;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.geoimage.def.GeoTransform;
import org.geoimage.viewer.core.GeometryImage;
import org.geoimage.viewer.core.layers.visualization.AttributesGeometry;

import com.sun.syndication.feed.module.georss.SimpleModuleImpl;
import com.sun.syndication.feed.module.georss.geometries.Position;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.SyndFeedOutput;
import com.vividsolutions.jts.geom.Geometry;

/**
 *
 * @author thoorfr
 */
public class GeoRSSIO extends AbstractVectorIO {

    public static String CONFIG_FILE = "file";
    public static GeometryImage glayer=null;

    public void save(File outputFile, String projection,	GeoTransform transform){
    	export(outputFile, glayer, projection, transform);
    }
    public static void export(File outputFile, GeometryImage glayer, String projection,	GeoTransform transform){
    		
    	try {
	
	        SyndFeed feed = new SyndFeedImpl();
	        feed.setFeedType("atom_1.0");
	        feed.setTitle(glayer.getName() + "");
	        feed.setLink("http://masure.jrc.it");
	        feed.setDescription("This is an output from SUMO");
	
	        List entries = new ArrayList();
	        int id = 0;
	
	        for (Geometry geom : glayer.getGeometries()) {
	            SyndEntry entry;
	            SyndContent description;
	            AttributesGeometry att = glayer.getAttributes(geom);
	            entry = new SyndEntryImpl();
	            entry.setAuthor("geoimage");
	            entry.setTitle("" + id++);
	            entry.setPublishedDate(new Date());
	            description = new SyndContentImpl();
	            description.setType("text/plain");
	            description.setValue(att.toString());
	            entry.setDescription(description);
	            SimpleModuleImpl geoRSSModule = new SimpleModuleImpl();
	            if (projection == null) {
	                geoRSSModule.setPosition(new Position(geom.getCoordinate().y, geom.getCoordinate().x));
	            } else {
	                double[] newPos = transform.getGeoFromPixel(geom.getCoordinate().x, geom.getCoordinate().y);
	                geoRSSModule.setPosition(new Position(newPos[1], newPos[0]));
	            }
	            entry.getModules().add(geoRSSModule);
	            entries.add(entry);
	        }
	        feed.setEntries(entries);
	        SyndFeedOutput output = new SyndFeedOutput();
            output.output(feed,outputFile);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
       /* GeometricLayer glayer = new GeometricLayer("point");
        glayer.put(new GeometryFactory().createPoint(new Coordinate(10 * Math.random(), 10 * Math.random())), Attributes.createAttributes(new String[]{"id", "name"}, new String[]{"String", "String"}));
        glayer.put(new GeometryFactory().createPoint(new Coordinate(10 * Math.random(), 10 * Math.random())), Attributes.createAttributes(new String[]{"id", "name"}, new String[]{"String", "String"}));
        glayer.put(new GeometryFactory().createPoint(new Coordinate(10 * Math.random(), 10 * Math.random())), Attributes.createAttributes(new String[]{"id", "name"}, new String[]{"String", "String"}));
        glayer.put(new GeometryFactory().createPoint(new Coordinate(10 * Math.random(), 10 * Math.random())), Attributes.createAttributes(new String[]{"id", "name"}, new String[]{"String", "String"}));
        glayer.put(new GeometryFactory().createPoint(new Coordinate(10 * Math.random(), 10 * Math.random())), Attributes.createAttributes(new String[]{"id", "name"}, new String[]{"String", "String"}));

        new GeoRSSIO().save(glayer, null,null);*/
    }

    

	@Override
	public void read() {
	}

	

	
}
