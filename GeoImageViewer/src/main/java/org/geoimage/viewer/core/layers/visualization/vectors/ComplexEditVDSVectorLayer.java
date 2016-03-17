/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.core.layers.visualization.vectors;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import org.geoimage.analysis.VDSSchema;
import org.geoimage.def.GeoImageReader;
import org.geoimage.def.GeoTransform;
import org.geoimage.def.SarImageReader;
import org.geoimage.exception.GeoTransformException;
import org.geoimage.opengl.OpenGLContext;
import org.geoimage.viewer.core.GeometryImage;
import org.geoimage.viewer.core.SumoPlatform;
import org.geoimage.viewer.core.api.ISave;
import org.geoimage.viewer.core.api.ilayer.ILayer;
import org.geoimage.viewer.core.factory.FactoryLayer;
import org.geoimage.viewer.core.gui.manager.LayerManager;
import org.geoimage.viewer.core.io.GmlIO;
import org.geoimage.viewer.core.io.KmlIO;
import org.geoimage.viewer.core.io.PostgisIO;
import org.geoimage.viewer.core.io.SumoXMLWriter;
import org.geoimage.viewer.core.io.SumoXmlIOOld;
import org.geoimage.viewer.core.layers.image.ImageLayer;
import org.geoimage.viewer.core.layers.thumbnails.ThumbnailsManager;
import org.geoimage.viewer.core.layers.visualization.AttributesGeometry;
import org.geoimage.viewer.widget.AttributesEditor;
import org.geoimage.viewer.widget.PostgisSettingsDialog;
import org.geoimage.viewer.widget.VDSAnalysisVersionDialog;
import org.jrc.sumo.configuration.PlatformConfiguration;
import org.jrc.sumo.util.files.FileTypes;
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
	//private String landMask;
	private String band="";

	public ComplexEditVDSVectorLayer(ILayer parent,String layername, String type, GeometryImage layer) {
        super(parent,layername, type, layer);
        //this.landMask=landMask;

    }

	public ComplexEditVDSVectorLayer(ILayer parent,String layername, String type, GeometryImage layer,
			String[] thresholds,double enl,int buffer,String landMask,String band) {
        super(parent,layername, type, layer);
        this.thresholds=thresholds;
        this.enl=enl;
        this.buffer=buffer;
        //this.landMask=landMask;
        this.band=band;

        // set the color and symbol values for the VDS layer
   	 	int widthstring=SumoPlatform.getApplication().getConfiguration().getTargetsSizeBand(""+band);
   	 	String colorString=SumoPlatform.getApplication().getConfiguration().getTargetsColorStringBand(""+band);
   	 	String symbolString=SumoPlatform.getApplication().getConfiguration().getTargetsSymbolBand(""+band);

        setWidth(widthstring);
        Color colordisplay = new Color(Integer.decode(colorString));
        setColor(colordisplay);
        setDisplaysymbol(MaskVectorLayer.symbol.valueOf(symbolString));
    }

    public boolean anyDections(){
    	return (glayer!=null && !glayer.getGeometries().isEmpty());
    }


    public void addAzimuthAmbiguities(List<Geometry> azimuthGeoms,boolean display){
    	super.addGeometries(AZIMUTH_AMBIGUITY_TAG, Color.RED,5, GeometryImage.POINT, azimuthGeoms, display);

    }

    public void addArtefactsAmbiguities(List<Geometry> artGeoms,boolean display){
    	super.addGeometries(ARTEFACTS_AMBIGUITY_TAG, Color.CYAN,5, GeometryImage.POINT, artGeoms, display);

    }

    public void addDetectedPixels(List<Geometry> pixgeoms,boolean display){
    	super.addGeometries(DETECTED_PIXELS_TAG,  new Color(0x00FF00),1, GeometryImage.POINT, pixgeoms, display);
    }

    public void addThreshAggPixels(List<Geometry> threshAgg,boolean display){
    	super.addGeometries(TRESHOLD_PIXELS_AGG_TAG,  new Color(0x0000FF),1, GeometryImage.POINT, threshAgg, display);
    }

    public void addThresholdPixels(List<Geometry> pixgeoms,boolean display){
    	super.addGeometries(TRESHOLD_PIXELS_TAG,  new Color(0x00FFFF),1, GeometryImage.POINT, pixgeoms, display);
    }

    public void saveNewXML(String file, int formattype, String projection,String runVersion,Integer runVersionNumber) {
    	try{
        	SarImageReader sar=((SarImageReader)SumoPlatform.getApplication().getCurrentImageReader());
	    	if (!file.endsWith(".xml")) {
	            file = file.concat(".xml");
	        }
	        float[] ts=new float[thresholds.length];
	        for(int i=0;i<thresholds.length;i++){
	        	ts[i]=Float.parseFloat(thresholds[i]);
	        }
	        ILayer current=SumoPlatform.getApplication().getLayerManager().getCurrentImageLayer();
	        MaskVectorLayer mask=LayerManager.getIstanceManager().getChildMaskLayer(current);

	        SumoXMLWriter.saveNewXML(new File(file),this,
	        		//FactoryLayer.createThresholdedLayer(glayer,currentThresh,threshable),
	        		projection,
	        		sar,ts,buffer,new Float(enl),mask.getName(),runVersion,runVersionNumber);

		    if(!SumoPlatform.isBatchMode())
		    	JOptionPane.showMessageDialog(null,"The VDS has been correctly saved into Sumo XML format","XML Saved", JOptionPane.INFORMATION_MESSAGE);
		    else
		    	logger.info("The VDS has been correctly saved into Sumo XML format");
    	}catch(Exception e){
    			JOptionPane.showMessageDialog(null,"Error saving XML:"+e.getMessage(),"Error", JOptionPane.INFORMATION_MESSAGE);
    			logger.error(e.getMessage());
    	}
    }

    @Override
    public void save(String file, int formattype, String projection) {
    	GeoImageReader reader=SumoPlatform.getApplication().getCurrentImageReader();
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
	        	/*
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
	            msgResult[0]="The VDS has been correctly saved into Sumo XML format";*/

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
	            tm.createThumbnailsDir(FactoryLayer.createThresholdedLayer(glayer,currentThresh,threshable), "id",reader, -1,((ImageLayer)super.parent).getActiveBand());
	            msgResult[0]="The thumnails have been successfully saved";
	            break;
	        }
	    }
        if(msgResult[0]==null||msgResult[0].equals(""))
        		msgResult[0]=file+" created";
        if(!SumoPlatform.isBatchMode())
        	JOptionPane.showMessageDialog(null,msgResult[0], msgResult[1],JOptionPane.INFORMATION_MESSAGE);
        else
        	logger.info(msgResult[0]);
    }



    public String getBand() {
		return band;
	}

	public void setBand(String band) {
		this.band = band;
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
    public FileTypes[] getFileFormatTypes() {
    	FileTypes[] opts=new FileTypes[8];
    	opts[0]=new FileTypes(ISave.OPT_EXPORT_XML_SUMO,ISave.STR_EXPORT_XML_SUMO,"xml");
    	opts[1]=new FileTypes(ISave.OPT_EXPORT_XML_SUMO_OLD,ISave.STR_EXPORT_XML_SUMO_OLD,"xml");
    	opts[2]=new FileTypes(ISave.OPT_EXPORT_CSV,ISave.STR_EXPORT_CSV,"csv");
    	opts[3]=new FileTypes(ISave.OPT_EXPORT_SHP,ISave.STR_EXPORT_SHP,"shp");
    	opts[4]=new FileTypes(ISave.OPT_EXPORT_GML,ISave.STR_EXPORT_GML);
    	opts[5]=new FileTypes(ISave.OPT_EXPORT_KMZ,ISave.STR_EXPORT_KMZ);
    	opts[6]=new FileTypes(ISave.OPT_EXPORT_POSTGIS,ISave.STR_EXPORT_POSTGIS);
    	opts[7]=new FileTypes(ISave.OPT_EXPORT_THUMBS,ISave.STR_EXPORT_THUMBS);

        return opts;
    }

    private GeometryImage postgisLayer(GeometryImage glayer,String timeStampStart) {
        // id counter for the postgis database
        int id = 0;
        // date object for postgis database
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = dateformat.format(new Date());
        // create new layer
        GeometryImage layer = new GeometryImage("POINT");
        layer.setName(glayer.getName());
        // change the layer fields to match the vds table layout
        for (Geometry geom : glayer.getGeometries()) {
            AttributesGeometry attributes = glayer.getAttributes(geom);
            AttributesGeometry tableattributes = new AttributesGeometry(
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
                        "comment"});/*,
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
                        */
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



    private ArrayList<String> postgisCommands(GeometryImage glayer, String table, String version, GeoTransform geotransform, String projection,String timeStampStart,String name) throws GeoTransformException {
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
            AttributesGeometry attributes = glayer.getAttributes(geom);
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

    protected void performAdd(java.awt.Point imagePosition, OpenGLContext context) {
        if (type.equals(GeometryImage.POINT)) {
            selectedGeometry = gf.createPoint(new Coordinate(imagePosition.x, imagePosition.y));
            //final AttributesGeometry atts = new AttributesGeometry(glayer.getSchema());//, glayer.getSchemaTypes());
            int size=glayer.getGeometries().size();

            Integer maxVal=0;
            for(int i=0;i<size;i++){
            	Integer val=(Integer) ((AttributesGeometry)glayer.getGeometries().get(i).getUserData()).get(VDSSchema.ID);

            	if(val>maxVal)
            		maxVal=val;
            }
            maxVal++;
            AttributesGeometry atts=null;
            /*if(glayer.getGeometries().size()>0){
            	AttributesGeometry source=(AttributesGeometry) glayer.getGeometries().get(0).getUserData();
            	atts=source.emptyAttributes(VDSSchema.schema,VDSSchema.types);
            }else{
            	atts=new AttributesGeometry(VDSSchema.schema,VDSSchema.types);
            }	*/
            atts=new AttributesGeometry(VDSSchema.schema,VDSSchema.types);
            atts.set(VDSSchema.ID,maxVal);
            final AttributesEditor ae = new AttributesEditor(new java.awt.Frame(), true);
            ae.setAttributes(atts);
            ae.setVisible(true);
        	HashMap<String,Object>newMapVals=ae.getAttributesValues();
            atts=setNewAttributeValues(atts,newMapVals);
            glayer.put(selectedGeometry,atts);

            ae.addWindowListener(new WindowAdapter() {
            	public void windowClosed(WindowEvent e) {
                    try {
                    	/*HashMap<String,Object>newMapVals=ae.getAttributesValues();
                        atts=setNewAttributeValues(atts,newMapVals);
                        glayer.put(selectedGeometry,atts);*/
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
			});



        }
    }



   protected AttributesGeometry setNewAttributeValues(AttributesGeometry attr,HashMap<String,Object> map){
    Collection<String>keys=map.keySet();
    SimpleDateFormat df=new SimpleDateFormat("dd-MM-YYYY");
    for (int i = 0; i <keys.size() ; i++) {
          String att = attr.getSchema()[i];
          Class<?> type = attr.getType(attr.getSchema()[i]);
          String val=((String)map.get(att));

          if(val!=null&&!"".equals(val)){
        	  try{
		          if (type == Double.class){
		        	  attr.set(att, Double.parseDouble(val));
		          } else if (type == String.class){
		        	  attr.set(att, val);
		          } else if(type == Date.class){
		        	  attr.set(att, df.parse(val));
		          } else if(type == Timestamp.class){
		          		Date d= df.parse(val);
						attr.set(att, new Timestamp(d.getTime()));
		          } else if (type == Integer.class){
		        	  attr.set(att, Integer.parseInt(val));
		          } else if (type == Boolean.class){
		        	  attr.set(att, Boolean.parseBoolean(val));
		          }else if (type.isArray()){
		        	//TODO: cambiare questa m...a
		        	int bb=Integer.parseInt(band);
		          	if(type==int[].class||type==Integer[].class){
		          		int[] a=new int[4];
		          		int id=PlatformConfiguration.getConfigurationInstance().getIdPolarization(SumoPlatform.getApplication().getCurrentImageReader().getBandName(bb));
		          		a[id]=Integer.parseInt(val);
		          		attr.set(att,a);
		          	}
		          	if(type==double[].class||type==Double[].class){
		          		double[] a=new double[4];
		          		int id=PlatformConfiguration.getConfigurationInstance().getIdPolarization(SumoPlatform.getApplication().getCurrentImageReader().getBandName(bb));
		          		a[id]=Double.parseDouble(val);
		          		attr.set(att,a);
		          	}
		          }else{
		        	  attr.set(att, val);
		          }
        	  }catch(Exception e){
        		  logger.error("Error setting attribute:"+att,e.getMessage());
        	  }
          }
      }
      return attr;
   }
};
