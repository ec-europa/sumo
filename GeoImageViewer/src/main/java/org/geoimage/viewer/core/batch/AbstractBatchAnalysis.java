package org.geoimage.viewer.core.batch;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.geoimage.analysis.VDSAnalysis;
import org.geoimage.def.GeoImageReader;
import org.geoimage.def.SarImageReader;
import org.geoimage.impl.cosmo.CosmoSkymedImage;
import org.geoimage.utils.IMask;
import org.geoimage.viewer.actions.VDSAnalysisConsoleAction;
import org.geoimage.viewer.core.configuration.PlatformConfiguration;
import org.geoimage.viewer.core.factory.FactoryLayer;
import org.geoimage.viewer.core.io.GenericCSVIO;
import org.geoimage.viewer.core.io.SimpleShapefile;
import org.geoimage.viewer.core.io.SumoXMLWriter;
import org.geoimage.viewer.core.layers.GeometricLayer;
import org.geoimage.viewer.core.layers.visualization.vectors.ComplexEditVDSVectorLayer;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;


class AnalysisParams {
	//HH HV VH VV
	public float[] thresholdArrayValues={0,0,0,0};
	public String pathImg[]=null;
	public String shapeFile="";
	public String outputFolder="";
	public float enl=1;
	public int buffer=0;
	public String epsg="EPSG:4326";	
	public Date startDate;
	
}

public abstract class AbstractBatchAnalysis {
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(AbstractBatchAnalysis.class);
	public AnalysisParams params;
	private  List<ComplexEditVDSVectorLayer>layerResults=null;
	private SimpleDateFormat dFormat=new SimpleDateFormat("ddMMyy_hhmmss");//"dd-MM-yy hh-mm-ss");
	private VDSAnalysis analysis;
	protected GeoImageReader currentReader;
	
	public AbstractBatchAnalysis(AnalysisParams analysisParams){
		params= analysisParams;
		layerResults=null;//new ArrayList<ComplexEditVDSVectorLayer>();
	}
	
		
	protected abstract void startAnalysis();
	protected GeoImageReader getCurrentReader(){
		return currentReader;
	}
	
	/**
	 * 
	 */
	protected void runProcess(){
		startAnalysis();
	}
	
	
	/**
	 * 
	 * @param reader
	 * @return
	 */
	protected GeometricLayer readShapeFile(SarImageReader reader){
		GeometricLayer gl=null;
	    try {
	    	Polygon imageP=(reader).getBbox(PlatformConfiguration.getConfigurationInstance().getLandMaskMargin(0));
            gl = SimpleShapefile.createIntersectedLayer(new File(params.shapeFile),imageP,reader.getGeoTransform());
        } catch (Exception e) {
        	logger.error(e.getMessage(),e);
        }
        
        return gl;
        
	}
		
	/**
	 * run analysis for 1 image
	 */
	public void analizeImage(SarImageReader reader,IMask[] masks,AnalysisParams params){
		
		
        analysis = new VDSAnalysis(reader,
        		masks, 
        		params.enl, 
        		params.thresholdArrayValues[0], 
        		params.thresholdArrayValues[1], 
        		params.thresholdArrayValues[2], 
        		params.thresholdArrayValues[3]);
  
        
        final String[] thresholds={""+params.thresholdArrayValues[0], 
        		""+params.thresholdArrayValues[1], 
        		""+params.thresholdArrayValues[2], 
        		""+params.thresholdArrayValues[3]};
        		//Utils.getStringThresholdsArray(reader, params.thresholdArrayValues);
        
        VDSAnalysisConsoleAction action= new VDSAnalysisConsoleAction();
        layerResults=action.runBatchAnalysis(reader,params.enl,analysis,masks,thresholds,params.buffer);
        
	}	
	
	/**
	 * 
	 */
	protected void saveResults(String imageName,IMask[] masks,SarImageReader reader){
		if(layerResults!=null){
    	   for(ComplexEditVDSVectorLayer l:layerResults){
    		   String outfolder=new StringBuilder(params.outputFolder)
    		   					.append(File.separator)
    		   					.append(imageName).toString();
    		   
    		   //create folder if not exist
    		   File folder=new File(outfolder);
    		   if(!folder.exists())
    			   folder.mkdirs();
    		   
    		   StringBuilder outfile=new StringBuilder(outfolder).append(File.separator)
    				            .append(l.getName()).append("_")
    				   			.append(dFormat.format(params.startDate));
    		   
				if(reader.isContainsMultipleImage() && reader instanceof CosmoSkymedImage){
					outfile=outfile.append("_").append(((CosmoSkymedImage)reader).getGroup());
				}

    		   l.save(outfile.toString(),ComplexEditVDSVectorLayer.OPT_EXPORT_XML_SUMO_OLD,params.epsg);
    		   
    		   
    		   String file=new String(outfile.append("_new").toString());
    		   if (!file.endsWith(".xml")) {
	                file = file.concat(".xml");
	            }
           
    		   //GeometricLayer threshLayer=FactoryLayer.createThresholdedLayer(l.getGeometriclayer(),l.getThresh(),l.isThreshable());
    		   SumoXMLWriter.saveNewXML(new File(file),l,params.epsg,reader,params.thresholdArrayValues,params.buffer,params.enl,params.shapeFile);
    		   
    		   String bbox=outfolder+"\\bbox.shp";
    		   List<Geometry> ggBbox=new ArrayList<Geometry>();

    		   //save the bound box as shape file
    		   try {
        		  ggBbox.add(reader.getBbox(PlatformConfiguration.getConfigurationInstance().getLandMaskMargin(0)));
				  SimpleShapefile.exportGeometriesToShapeFile(ggBbox, new File(bbox),"Polygon",null);
			   } catch (Exception e) {
				  logger.error("Problem exporting the bounding box:"+e.getLocalizedMessage(),e); 
			   }
    		   
    		   //save targets as shape file
    		   try{
    			   String targets=outfolder+"\\"+l.getName()+".shp";
    			  // l.save(targets, ISave.OPT_EXPORT_SHP, "EPSG:4326");
    			   SimpleShapefile.exportGeometriesToShapeFile(l.getGeometriclayer().getGeometries(), new File(targets),"Point",reader.getGeoTransform());
    		   } catch (Exception e) {
 				  logger.error("Problem exporting the bounding box:"+e.getLocalizedMessage(),e); 
 			   }
    		   
    		   try{
    			   String targetscsv=params.outputFolder+"\\targets.csv";
    			   List<Geometry> targets=l.getGeometriclayer().getGeometries();
    			   List<Geometry> ambi=l.getGeometriesByTag(ComplexEditVDSVectorLayer.AMBIGUITY_TAG).getGeometries();
    			   //remove ambiguities
    			   if(ambi!=null){
	    			   for(Geometry geom:targets){
		    			   if(ambi.contains(geom)){
		    				   targets.remove(geom);
		    			   }
	    			   }	   
    			   }	   
    			   GenericCSVIO.geomCsv(new File(targetscsv),targets,reader.getGeoTransform(),imageName,true);
    		   }catch(Exception e ){
    			   logger.error("Problem saving targets in csv:"+imageName,e);
    		   }
    		   try{
    			   String bboxcsv=params.outputFolder+"\\bbox.csv";
    			   GenericCSVIO.geomCsv(new File(bboxcsv),ggBbox,null,imageName,true);
    		   }catch(Exception e ){
    			   logger.error("Problem saving bbox in csv:"+imageName,e);
    		   }
    	   }
        }
	}
}
