/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.widget.panels;

import java.awt.Color;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

import org.geoimage.analysis.VDSSchema;
import org.geoimage.def.SarImageReader;
import org.geoimage.viewer.core.Platform;
import org.geoimage.viewer.core.api.Attributes;
import org.geoimage.viewer.core.api.IComplexVectorLayer;
import org.geoimage.viewer.core.api.IImageLayer;
import org.geoimage.viewer.core.api.ILayer;
import org.geoimage.viewer.core.configuration.PlatformConfiguration;
import org.geoimage.viewer.core.layers.GeometricLayer;
import org.geoimage.viewer.core.layers.vectors.MaskVectorLayer;
import org.geoimage.viewer.core.layers.vectors.SimpleEditVectorLayer;
import org.geoimage.viewer.util.Constant;
import org.geoimage.viewer.widget.AttributesEditor;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 *
 * @author leforth
 */
public class GeometricInteractiveVDSLayerModel extends DefaultTableModel {

    private GeometricLayer gl;
    private IImageLayer il;
    private IComplexVectorLayer vdslayer;
    
    private Color azimuthGeometrycolor = null;
    private int azimuthGeometrylinewidth;

    private static org.slf4j.Logger logger=LoggerFactory.getLogger(GeometricInteractiveVDSLayerModel.class);
    
    public GeometricInteractiveVDSLayerModel(ILayer layer) {
        this.gl = ((SimpleEditVectorLayer) layer).getGeometriclayer();
        this.il = null;

        for (ILayer l : Platform.getLayerManager().getLayers().keySet()) {
            if (l instanceof IImageLayer && l.isActive()) {
                il = (IImageLayer) l;
                break;
            }
        }

        vdslayer = (IComplexVectorLayer) layer;

        PlatformConfiguration configuration = Platform.getConfiguration();
        // set the preferences values
        try {
            String colorString = configuration.getAzimuthGeometryColor();
            this.azimuthGeometrycolor = new Color(Integer.parseInt(colorString.equals("") ? Color.ORANGE.getRGB() + "" : colorString));
            this.azimuthGeometrylinewidth = configuration.getAzimuthLineWidth();
        } catch (NumberFormatException e) {
            //Logger.getLogger(GeometricInteractiveVDSLayerModel.class.getName()).log(Level.SEVERE, null, e);
            JOptionPane.showMessageDialog(null, "Wrong format with the preference settings", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public int getRowCount() {
    	if(gl==null)
    		return 0;
        return gl.getGeometries().size();
    }

    public int getColumnCount() {
    	if(gl==null)
    		return 0;
        return gl.getSchema().length + 1;
    }

    public String getColumnName(int columnIndex) {
        if (columnIndex == 0) {
            return "Geometry";
        } else {
            return gl.getSchema()[columnIndex - 1];
        }
    }

    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0) {
            return Geometry.class;
        } else {
            String c = gl.getSchemaTypes()[columnIndex - 1];
            if (c.contains("Double")) {
                return Double.class;
            }
            if (c.contains("String")) {
                return String.class;
            }
            if (c.contains("Integer")) {
                return Integer.class;
            }
            if (c.contains("Long")) {
                return Long.class;
            }
            if (c.contains("Date")) {
                return Date.class;
            } else {
                return String.class;
            }
        }
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex > 7) {
            return true;
        }
        return false;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        Geometry geom = gl.getGeometries().get(rowIndex);
        if (columnIndex == 0) {
            return geom;
        } else {
            return gl.getAttributes(geom).get(gl.getSchema()[columnIndex - 1]);
        }
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Geometry geom = gl.getGeometries().get(rowIndex);
        gl.getAttributes(geom).set(gl.getSchema()[columnIndex - 1], aValue);
        Platform.getGeoContext().setDirty(true);
    }
/*
    public void addTableModelListener(TableModelListener l) {
    }

    public void removeTableModelListener(TableModelListener l) {
    }*/

    // specific VDS rendering
    public void changeSelection(int selectionLine, boolean display) {
        if (selectionLine != -1&&gl.getGeometries().size()>0) {
            Geometry geom = gl.getGeometries().get(selectionLine);
            int posX = (int) geom.getCoordinate().x;
            int posY = (int) geom.getCoordinate().y;
            Platform.getGeoContext().setX((int) (posX - Platform.getGeoContext().getWidth() / 2));
            Platform.getGeoContext().setY((int) (posY - Platform.getGeoContext().getHeight() / 2));
            Platform.getGeoContext().setZoom((float) 1.0);
            GeometryFactory gf = new GeometryFactory();
            // generate the geometry for the target shape
            Vector<Geometry> winGeom = new Vector<Geometry>();
            int size = 100;
            Coordinate[] coordinates = new Coordinate[5];
            coordinates[0] = new Coordinate(posX - size / 2, posY - size / 2);
            coordinates[1] = new Coordinate(posX + size / 2, posY - size / 2);
            coordinates[2] = new Coordinate(posX + size / 2, posY + size / 2);
            coordinates[3] = new Coordinate(posX - size / 2, posY + size / 2);
            coordinates[4] = new Coordinate(posX - size / 2, posY - size / 2);
            winGeom.add(gf.createLinearRing(coordinates));
            // generate the geometry for the boat shape
            Vector<Geometry> boatGeom = new Vector<Geometry>();
            Attributes boatattributes = gl.getAttributes(geom);
            double[] pixelsize = il.getImageReader().getGeoTransform().getPixelSize();
            double boatwidth = (Double) boatattributes.get(VDSSchema.ESTIMATED_WIDTH) / pixelsize[0];
            double boatlength = (Double) boatattributes.get(VDSSchema.ESTIMATED_LENGTH) / pixelsize[0];
            Double boatheading = -(Double) boatattributes.get(VDSSchema.ESTIMATED_HEADING);
            //get the image azimuth
            double imageAz = ((SarImageReader)il.getImageReader()).getImageAzimuth();
            boatheading = boatheading + 90 + imageAz;
            Coordinate[] boatcoordinates = new Coordinate[5];
            boatcoordinates[0] = new Coordinate(posX + boatlength / 2 * Math.cos(Math.PI * boatheading / 180.0) + boatwidth / 2 * Math.sin(Math.PI * boatheading / 180.0), posY - boatlength / 2 * Math.sin(Math.PI * boatheading / 180.0) + boatwidth / 2 * Math.cos(Math.PI * boatheading / 180.0));
            boatcoordinates[1] = new Coordinate(posX - boatlength / 2 * Math.cos(Math.PI * boatheading / 180.0) + boatwidth / 2 * Math.sin(Math.PI * boatheading / 180.0), posY + boatlength / 2 * Math.sin(Math.PI * boatheading / 180.0) + boatwidth / 2 * Math.cos(Math.PI * boatheading / 180.0));
            boatcoordinates[2] = new Coordinate(posX - boatlength / 2 * Math.cos(Math.PI * boatheading / 180.0) - boatwidth / 2 * Math.sin(Math.PI * boatheading / 180.0), posY + boatlength / 2 * Math.sin(Math.PI * boatheading / 180.0) - boatwidth / 2 * Math.cos(Math.PI * boatheading / 180.0));
            boatcoordinates[3] = new Coordinate(posX + boatlength / 2 * Math.cos(Math.PI * boatheading / 180.0) - boatwidth / 2 * Math.sin(Math.PI * boatheading / 180.0), posY - boatlength / 2 * Math.sin(Math.PI * boatheading / 180.0) - boatwidth / 2 * Math.cos(Math.PI * boatheading / 180.0));
            boatcoordinates[4] = boatcoordinates[0];
            boatGeom.add(gf.createLinearRing(boatcoordinates));
            // remove previous geometries
            vdslayer.removeGeometriesByTag("boatshape");
            vdslayer.removeGeometriesByTag("target");
            // add new geometries
            vdslayer.addGeometries("target", new Color(0xFF2200), 1, MaskVectorLayer.LINESTRING, winGeom, display);
            vdslayer.addGeometries("boatshape", new Color(0xFF2200), 2, MaskVectorLayer.LINESTRING, boatGeom, display);
            Platform.getGeoContext().setDirty(true);
            System.out.println(selectionLine + " " + geom.getCoordinate().x + " " + geom.getCoordinate().y);
        } else {
            vdslayer.removeGeometriesByTag("boatshape");
            vdslayer.removeGeometriesByTag("target");
            Platform.getGeoContext().setDirty(true);
        }
    }

    public void editSelection(int selectionLine) {
        Attributes atts = gl.getAttributes(gl.getGeometries().get(selectionLine));
        AttributesEditor ae = new AttributesEditor(new java.awt.Frame(), true);
        ae.setAttributes(atts);
        ae.setVisible(true);
    }

    public Geometry removeSelection(int selectionLine) {
        Geometry geom = gl.getGeometries().get(selectionLine);
        gl.remove(geom);
        return geom;
    }

    public void toggleRulers(int selectionLine) {
        if (selectionLine != -1) {
            if (vdslayer.tagExists(Constant.PREF_AZIMUTH_GEOMETRYTAG)) {
                vdslayer.removeGeometriesByTag(Constant.PREF_AZIMUTH_GEOMETRYTAG);
            } else {
            	try {
	                // get the position of the boat
	                Geometry geom = gl.getGeometries().get(selectionLine);
	                int posX = (int) geom.getCoordinate().x;
	                int posY = (int) geom.getCoordinate().y;
	                // calculate satellite speed
	                String tstart=((SarImageReader)il.getImageReader()).getTimeStampStart();
	                String tstop=((SarImageReader)il.getImageReader()).getTimeStampStop();
	                double seconds = ((Timestamp.valueOf(tstart).getTime() - (Timestamp.valueOf(tstop).getTime()))) / 1000;
	                // calculate satellite speed in azimuth pixels / seconds
	                double azimuthpixelspeed = (double) il.getImageReader().getHeight() / seconds;
	                // calculate the earth angular speed
	                double earthangularSpeed = 2 * Math.PI / 24 / 3600;
	                // calculate earth radius at target point lattitude
	                double radius;
				
					radius = 6400 * 1000 * Math.cos(2 * Math.PI * il.getImageReader().getGeoTransform().getGeoFromPixel(posX, posY)[1] / 360.0);
	                // calculate the range azimuth pixel speed due to the rotation of the earth
	                double rangepixelspeed = earthangularSpeed * radius / il.getImageReader().getGeoTransform().getPixelSize()[0];
	                // calculate the pixels delta value
	                double azi=((SarImageReader)il.getImageReader()).getImageAzimuth();
	                double pixeldelta = 1 / (Math.cos(azi * 2 * Math.PI / 360.0) / (azimuthpixelspeed / rangepixelspeed - Math.sin(azi * 2 * Math.PI / 360.0)));
	
	                // get the mode
	                int direction = Math.abs(azi) > 90 ? -1 : 1;
	                // create new geometry
	                GeometryFactory gf = new GeometryFactory();
	                // generate the geometry for the target shape
	                Vector<Geometry> winGeom = new Vector<Geometry>();
	                Coordinate[] coordinatesvertical = new Coordinate[2];
	                coordinatesvertical[0] = new Coordinate(posX + direction * (0 - posY) / pixeldelta, 0);
	                coordinatesvertical[1] = new Coordinate(posX + direction * (il.getImageReader().getHeight() - posY) / pixeldelta, il.getImageReader().getHeight());
	                winGeom.add(gf.createLineString(coordinatesvertical));
	                Coordinate[] coordinateshorizontal = new Coordinate[2];
	                coordinateshorizontal[0] = new Coordinate(0, posY);
	                coordinateshorizontal[1] = new Coordinate(il.getImageReader().getWidth(), posY);
	                winGeom.add(gf.createLineString(coordinateshorizontal));
	                vdslayer.addGeometries(Constant.PREF_AZIMUTH_GEOMETRYTAG, this.azimuthGeometrycolor, this.azimuthGeometrylinewidth, SimpleEditVectorLayer.LINESTRING, winGeom, true);
				} catch (Exception e) {
					logger.error(e.getMessage());
				}   
            }
        }
        Platform.getGeoContext().setDirty(true);
    }
}
