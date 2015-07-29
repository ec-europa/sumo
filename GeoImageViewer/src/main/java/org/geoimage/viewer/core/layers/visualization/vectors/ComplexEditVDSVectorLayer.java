/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.core.layers.visualization.vectors;

import java.awt.Color;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import org.geoimage.analysis.VDSSchema;
import org.geoimage.def.GeoImageReader;
import org.geoimage.def.GeoTransform;
import org.geoimage.def.SarImageReader;
import org.geoimage.exception.GeoTransformException;
import org.geoimage.viewer.common.OptionMenu;
import org.geoimage.viewer.core.Platform;
import org.geoimage.viewer.core.api.Attributes;
import org.geoimage.viewer.core.api.GeoContext;
import org.geoimage.viewer.core.api.ILayer;
import org.geoimage.viewer.core.api.ISave;
import org.geoimage.viewer.core.factory.FactoryLayer;
import org.geoimage.viewer.core.io.GmlIO;
import org.geoimage.viewer.core.io.KmlIO;
import org.geoimage.viewer.core.io.PostgisIO;
import org.geoimage.viewer.core.io.SumoXMLWriter;
import org.geoimage.viewer.core.io.SumoXmlIOOld;
import org.geoimage.viewer.core.layers.GeometricLayer;
import org.geoimage.viewer.core.layers.image.ImageLayer;
import org.geoimage.viewer.core.layers.thumbnails.ThumbnailsManager;
import org.geoimage.viewer.widget.AttributesEditor;
import org.geoimage.viewer.widget.PostgisSettingsDialog;
import org.geoimage.viewer.widget.VDSAnalysisVersionDialog;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/**
 *
 * @author Pietro Argentieri
 *  The main class to visualize the result of the analysis
 * 
 * 
 */
public class ComplexEditVDSVectorLayer extends ComplexEditGeometryVectorLayer  {
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(ComplexEditVDSVectorLayer.class);
	private String[] thresholds={};
	private double enl=0;
	private int buffer=0;
	private String landMask;
	
	
	public ComplexEditVDSVectorLayer(ILayer parent,String layername, String type, GeometricLayer layer,String landMask) {
        super(parent,layername, type, layer);
        this.landMask=landMask;
        
    }
	
	public ComplexEditVDSVectorLayer(ILayer parent,String layername, String type, GeometricLayer layer,String[] thresholds,double enl,int buffer,String landMask,String band) {
        super(parent,layername, type, layer);
        this.thresholds=thresholds;
        this.enl=enl;
        this.buffer=buffer;
        this.landMask=landMask;
        
        // set the color and symbol values for the VDS layer
   	 	int widthstring=Platform.getConfiguration().getTargetsSizeBand(""+band);
   	 	String colorString=Platform.getConfiguration().getTargetsColorStringBand(""+band);
   	 	String symbolString=Platform.getConfiguration().getTargetsSymbolBand(""+band);
        
        setWidth(widthstring);
        Color colordisplay = new Color(Integer.decode(colorString));
        setColor(colordisplay);
        setDisplaysymbol(MaskVectorLayer.symbol.valueOf(symbolString));
    }
    
    public boolean anyDections(){
    	return (glayer!=null && !glayer.getGeometries().isEmpty());
    }
        
    @Override
    public void save(String file, int formattype, String projection) {
    	GeoImageReader reader=Platform.getCurrentImageReader();//((IImageLayer)super.parent).getImageReader();
    	SarImageReader sar=((SarImageReader)reader);
        super.save(file, formattype, projection);
        
        
        String[] msgResult={"","Succefull"};
        switch (formattype) {
	        case ISave.OPT_EXPORT_POSTGIS: {
	            try {
	                String table = "";
	                PostgisSettingsDialog ps = new PostgisSettingsDialog(null, true);
	                ps.setVisible(true);
	                table = ps.getTable();
	                Map <String,Object>config = new HashMap<String,Object>();
	                config = ps.getConfig();
	                // get the version
	                VDSAnalysisVersionDialog versiondialog = new VDSAnalysisVersionDialog(null, true);
	                versiondialog.setVisible(true);
	                String version = versiondialog.getVersion();
	
	                // generate SQL commands
	                ArrayList<String> postgiscommands = postgisCommands(FactoryLayer.createThresholdedLayer(glayer,currentThresh,threshable), table, version, 
	                		sar.getGeoTransform(), projection,sar.getTimeStampStart(),sar.getDisplayName(0));
	                // save the new layer in database
	                PostgisIO vio=new PostgisIO(reader, config);
	                vio.setLayerName(table);
	                vio.executeCommands(postgiscommands);
	                msgResult[0]="The VDS has been correctly uploaded to the database";
	            } catch (Exception ex) {
	            	logger.error(ex.getMessage(), ex);
	            	msgResult[0]=ex.getMessage();
	            	msgResult[1]="Error";
	            }
	            break;
	        }
	        case ISave.OPT_EXPORT_GML:{
	            if (!file.endsWith(".gml")) {
	                file = file.concat(".gml");
	            }
	            GmlIO gml=new GmlIO(new File(file), sar);
	            ((GmlIO)gml).save(new File(file),FactoryLayer.createThresholdedLayer(glayer,currentThresh,threshable), projection,reader.getGeoTransform(),sar);
	            msgResult[0]="The GML file has been succesfully created";
	            break;
	        }
	        case ISave.OPT_EXPORT_XML_SUMO_OLD: {
	
	            if (!file.endsWith(".xml")) {
	                file = file.concat(".xml");
	            }
	            SumoXmlIOOld.export(new File(file),FactoryLayer.createThresholdedLayer(glayer,currentThresh,threshable), projection,(SarImageReader)reader);
	            msgResult[0]="The VDS has been correctly saved into Sumo XML format";
	            break;
	        }
	        case ISave.OPT_EXPORT_XML_SUMO: {
	
	            if (!file.endsWith(".xml")) {
	                file = file.concat(".xml");
	            }
	            float[] ts=new float[thresholds.length];
	            for(int i=0;i<thresholds.length;i++){
	            	ts[i]=Float.parseFloat(thresholds[i]);
	            }
	            SumoXMLWriter.saveNewXML(new File(file),this,
	            		//FactoryLayer.createThresholdedLayer(glayer,currentThresh,threshable),
	            		projection,
	            		sar,ts,buffer,new Float(enl),landMask);
	            msgResult[0]="The VDS has been correctly saved into Sumo XML format";
	            
	            break;
	        }
	        case ISave.OPT_EXPORT_KMZ: {
	            try {
	                if (!file.endsWith(".kmz")) {
	                    file = file.concat(".kmz");
	                }
	                KmlIO.export(new File(file),FactoryLayer.createThresholdedLayer(glayer,currentThresh,threshable), projection,sar,sar.getGeoTransform());
	                msgResult[0]="The KMZ file is succesfully created";
	            } catch (Exception ex) {
	            	logger.error(ex.getMessage(), ex);
	            	msgResult[0]=ex.getMessage();
	            	msgResult[1]="Error";
	            }
	            break;
	        }
	        case ISave.OPT_EXPORT_THUMBS:{
	            ThumbnailsManager tm = new ThumbnailsManager(file);
	            tm.createThumbnailsDir(FactoryLayer.createThresholdedLayer(glayer,currentThresh,threshable), "id",reader, null,((ImageLayer)super.parent).getActiveBand());
	            msgResult[0]="The thumnails have been successfully saved";
	            break;
	        }
	    }
        if(msgResult[0]==null||msgResult[0].equals(""))
        		msgResult[0]=file+" created";
        if(!Platform.isBatchMode())
        	JOptionPane.showMessageDialog(null,msgResult[0], msgResult[1],JOptionPane.INFORMATION_MESSAGE);
        else 
        	logger.info(msgResult[0]);
    }

    
    
    public String[] getThresholds() {
		return thresholds;
	}

	public void setThresholds(String[] thresholds) {
		this.thresholds = thresholds;
	}
    
	public double getEnl() {
		return enl;
	}

	public void setEnl(double enl) {
		this.enl = enl;
	}
	
	public int getBuffer() {
		return buffer;
	}

	public void setBuffer(int buffer) {
		this.buffer = buffer;
	}
	
    @Override
    public OptionMenu[] getFileFormatTypes() {
    	OptionMenu[] opts=new OptionMenu[8];
    	opts[0]=new OptionMenu(ISave.OPT_EXPORT_CSV,ISave.STR_EXPORT_CSV); 
    	opts[1]=new OptionMenu(ISave.OPT_EXPORT_SHP,ISave.STR_EXPORT_SHP);
    	opts[2]=new OptionMenu(ISave.OPT_EXPORT_XML_SUMO_OLD,ISave.STR_EXPORT_XML_SUMO_OLD);
    	opts[3]=new OptionMenu(ISave.OPT_EXPORT_XML_SUMO,ISave.STR_EXPORT_XML_SUMO);
    	opts[4]=new OptionMenu(ISave.OPT_EXPORT_GML,ISave.STR_EXPORT_GML);
    	opts[5]=new OptionMenu(ISave.OPT_EXPORT_KMZ,ISave.STR_EXPORT_KMZ);
    	opts[6]=new OptionMenu(ISave.OPT_EXPORT_POSTGIS,ISave.STR_EXPORT_POSTGIS);
    	opts[7]=new OptionMenu(ISave.OPT_EXPORT_THUMBS,ISave.STR_EXPORT_THUMBS);
    	
        return opts; 
    }

    private GeometricLayer postgisLayer(GeometricLayer glayer,String timeStampStart) {
        // id counter for the postgis database
        int id = 0;
        // date object for postgis database
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = dateformat.format(new Date());
        // create new layer
        GeometricLayer layer = new GeometricLayer("POINT");
        layer.setName(glayer.getName());
        // change the layer fields to match the vds table layout
        for (Geometry geom : glayer.getGeometries()) {
            Attributes attributes = glayer.getAttributes(geom);
            Attributes tableattributes = Attributes.createAttributes(
                    new String[]{
                        "id",
                        "detectime",
                        "geom",
                        "image_id",
                        "run_id",
                        "io",
                        "xp",
                        "yp",
                        "vs",
                        "calc_length",
                        "calc_width",
                        "calc_course",
                        "sub_objects",
                        "version",
                        "related_obj",
                        "size_classification",
                        "target_time",
                        "reliability",
                        "comment"},
                    new String[]{
                        "Integer",
                        "String",
                        "String",
                        "String",
                        "String",
                        "Integer",
                        "Integer",
                        "Integer",
                        "Integer",
                        "Double",
                        "Double",
                        "Double",
                        "Integer",
                        "String",
                        "Integer",
                        "String",
                        "Integer",
                        "Integer",
                        "String"}); // new String[]{"integer", "time stamp without time zone", "character varying(32)", "character varying(32)", "smallint", "integer", "real", "real", "real", "character varying(12)", "smallint"}
            //tableattributes.set("id", new Integer(270100 + id));
            tableattributes.set("detectime", date);
            
            String image_id = timeStampStart + "0";
            image_id.replaceAll(":", "");
            image_id.replaceAll("-", "");
            image_id.replaceAll(" ", "");
            tableattributes.set("image_id", image_id);
            tableattributes.set("run_id", attributes.get(VDSSchema.RUN_ID));
            tableattributes.set("io", new Integer(id++));
            tableattributes.set("xp", new Double((double) geom.getCoordinate().x));
            tableattributes.set("yp", new Double((double) geom.getCoordinate().y));
            tableattributes.set("calc_length", (Double) attributes.get(VDSSchema.ESTIMATED_LENGTH));
            tableattributes.set("calc_width", (Double) attributes.get(VDSSchema.ESTIMATED_WIDTH));
            tableattributes.set("calc_course", (Double) attributes.get(VDSSchema.ESTIMATED_HEADING));
            tableattributes.set("version", "");
            tableattributes.set("related_obj", new Integer(0));
            tableattributes.set("size_classification", (Double) attributes.get(VDSSchema.ESTIMATED_LENGTH) < 15 ? "small" : ((Double) attributes.get(VDSSchema.ESTIMATED_LENGTH) > 150 ? "large" : "medium"));
            tableattributes.set("target_time", "");
            tableattributes.set("reliability", new Integer(0));
            tableattributes.set("comment", "");
            layer.put(geom, tableattributes);
        }
        return layer;
    }

    
    
    private ArrayList<String> postgisCommands(GeometricLayer glayer, String table, String version, GeoTransform geotransform, String projection,String timeStampStart,String name) throws GeoTransformException {
        // id counter for the postgis database
        int id = 0;
        // list of postgis commands for database
        ArrayList<String> postgiscommands = new ArrayList<String>();
        // start with the delete command
        // date object for postgis database
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = dateformat.format(new Date());
        String detect_time = timeStampStart;
        String image_id = detect_time;
        image_id = image_id.replaceAll("-", "").replaceAll(" ", "").replaceAll("\\.", "").replaceAll(":", "");
        postgiscommands.add("delete from " + table + " where version = '" + version + "' and image_id = '" + image_id + "'");
        // now insert new vds positions
        StringBuilder qryString = new StringBuilder(
                "insert into "+table+" (detecttime,geom,image_id,run_id,io,xp,yp,vs,calc_length,calc_width,calc_course,sub_objects,version,related_obj,size_classification,reliability,comment) values ");
        // scan through the list of boats
        for (Geometry geom : glayer.getGeometries()) {
            Attributes attributes = glayer.getAttributes(geom);
            StringBuilder values = new StringBuilder(qryString.toString());
            values.append("('").append(detect_time).append("',");
            // convert pixel values to projected values
            double[] temp = geotransform.getGeoFromPixel(geom.getCoordinate().x, geom.getCoordinate().y);
            values.append("setsrid(geometryfromtext('POINT(").append(temp[0]).append(" ").append(temp[1]).append(")'),4326)" + ",");
            values.append("'").append(image_id).append("',");
            values.append("'").append((String) attributes.get(VDSSchema.RUN_ID)).append("',");
            values.append(id++).append(",");
            values.append((int) geom.getCoordinate().x).append(",");
            values.append((int) geom.getCoordinate().y).append(",");
            values.append(attributes.get(VDSSchema.VS)).append(",");
            values.append(attributes.get(VDSSchema.ESTIMATED_LENGTH)).append(",");
            values.append(attributes.get(VDSSchema.ESTIMATED_WIDTH)).append(",");
            values.append(attributes.get(VDSSchema.ESTIMATED_HEADING)).append(",");
            values.append((int) ((Double) attributes.get(VDSSchema.NUMBER_OF_AGGREGATED_PIXELS)).doubleValue()).append(",");
            values.append("'").append(version).append("',");
            values.append("null,");
            values.append((Double) attributes.get(VDSSchema.ESTIMATED_LENGTH) < 15 ? "'small'" : ((Double) attributes.get(VDSSchema.ESTIMATED_LENGTH) > 150 ? "'large'" : "'medium'")).append(",");
            values.append("0,");
            name = name.substring(name.lastIndexOf("/") + 1);
            values.append("'").append(glayer.getName()).append("')");
            postgiscommands.add(values.toString());
        }
        return postgiscommands;
    }

    protected void performAdd(java.awt.Point imagePosition, GeoContext context) {
        if (type.equals(GeometricLayer.POINT)) {
            selectedGeometry = gf.createPoint(new Coordinate(imagePosition.x, imagePosition.y));
            final Attributes atts = Attributes.createAttributes(glayer.getSchema(), glayer.getSchemaTypes());
            atts.set(VDSSchema.SIGNIFICANCE, 100.0d);
            final AttributesEditor ae = new AttributesEditor(new java.awt.Frame(), true);
            ae.setAttributes(atts);
            glayer.put(selectedGeometry,atts);
            ae.setVisible(true);
        }
    }
};
