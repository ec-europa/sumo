package org.geoimage.viewer.core.batch;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.geoimage.analysis.VDSAnalysis;
import org.geoimage.def.GeoImageReader;
import org.geoimage.def.SarImageReader;
import org.geoimage.impl.cosmo.CosmoSkymedImage;
import org.geoimage.utils.IMask;
import org.geoimage.viewer.core.analysisproc.AnalysisProcess;
import org.geoimage.viewer.core.configuration.PlatformConfiguration;
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
	public String xmlOutputFolder="";
	public float enl=0;
	public int buffer=0;
	public String epsg="EPSG:4326";	
	public Date startDate;
	public int maxDetections=0;
	
}

public abstract class AbstractBatchAnalysis {
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(AbstractBatchAnalysis.class);
	public AnalysisParams params;
	private  List<ComplexEditVDSVectorLayer>layerResults=null;
	private SimpleDateFormat dFormat=new SimpleDateFormat("ddMMyy_hhmmss");//"dd-MM-yy hh-mm-ss");
	private VDSAnalysis analysis;
	protected GeoImageReader currentReader;
	
	private int runVersionNumber=1;
	private String runVersion="BATCH";
	
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
	
	
	public int getRunVersionNumber() {
		return runVersionNumber;
	}


	public void setRunVersionNumber(int runVersionNumber) {
		this.runVersionNumber = runVersionNumber;
	}


	public String getRunVersion() {
		return runVersion;
	}


	public void setRunVersion(String runVersion) {
		this.runVersion = runVersion;
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
		
        java.util.HashMap<String,Float> thresholdsMap = new java.util.HashMap<>();

        thresholdsMap.put("HH", params.thresholdArrayValues[0]);
        thresholdsMap.put("HV", params.thresholdArrayValues[1]);
        thresholdsMap.put("VH", params.thresholdArrayValues[2]);
        thresholdsMap.put("VV", params.thresholdArrayValues[3]);
		
        analysis = new VDSAnalysis(reader,
        		masks, 
        		params.enl, 
        		thresholdsMap
        		);
  
        
        final String[] thresholds={""+params.thresholdArrayValues[0], 
        		""+params.thresholdArrayValues[1], 
        		""+params.thresholdArrayValues[2], 
        		""+params.thresholdArrayValues[3]};
        		//Utils.getStringThresholdsArray(reader, params.thresholdArrayValues);
        
            layerResults=runBatchAnalysis(reader,params.enl,analysis,masks,thresholds,params.buffer);
        
	}	
	
	 /**
     * 
     * @param ENL
     * @param analysis
     * @param bufferedMask
     * @param thresholds
     * @return
     */
    public List<ComplexEditVDSVectorLayer> runBatchAnalysis(GeoImageReader reader,float ENL, VDSAnalysis analysis,IMask[] bufferedMask, String[] thresholds,int buffer){
    	AnalysisProcess ap=new AnalysisProcess(reader,ENL,analysis, bufferedMask, buffer,1250000);
        ap.run();
        return ap.getResultLayers();
    }
    
	
	
	
	/**
	 * 
	 */
	protected void saveResults(String imageName,IMask[] masks,SarImageReader reader){
		if(layerResults!=null){
			String outfolder=new StringBuilder(params.outputFolder)
   					.append(File.separator)
   					.append(imageName).toString();
   
		   //create folder if not exist
		   File folder=new File(outfolder);
		   if(!folder.exists())
			   folder.mkdirs();
		
		   File xmlOutFolder=null;
		   
		    xmlOutFolder=new File(params.xmlOutputFolder);
		    if(!xmlOutFolder.exists())
		    	xmlOutFolder.mkdirs();
		   
    	   for(ComplexEditVDSVectorLayer l:layerResults){
    		   StringBuilder outfileName=new StringBuilder(outfolder).append(File.separator)
    				            .append(reader.getImId()).append("_");
    		   if(l.getBand().equalsIgnoreCase("Merged")){
    			   outfileName.append(l.getBand()); 
    		   }else{
    			   outfileName.append(reader.getBandName(Integer.valueOf(l.getBand())));
    		   }
    		   
    			
    		   
				if(reader.isContainsMultipleImage() && reader instanceof CosmoSkymedImage){
					outfileName=outfileName.append("_").append(((CosmoSkymedImage)reader).getGroup());
				}

    		   l.save(outfileName.toString()+"_OLD",ComplexEditVDSVectorLayer.OPT_EXPORT_XML_SUMO_OLD,params.epsg);
    		   
    		   
    		   File outFile=new File(outfileName.toString()+".xml");
    		   SumoXMLWriter.saveNewXML(outFile,l,
    				   params.epsg,reader,params.thresholdArrayValues,
    				   params.buffer,params.enl,params.shapeFile,runVersion,runVersionNumber);
    		   
    		   //copy the xml in the folder for ingestion => copy the merged for multiple band or the single xml
    		   try{
	    		   if(layerResults.size()==1||l.getBand().equals("Merged")){
	    			   FileUtils.copyFile(outFile,new File( params.xmlOutputFolder+"/"+reader.getImId()+".xml"));
	    		   }
    	   	   }catch(Exception e){
    	   		   logger.error("File xml not saved in the xmloutputfolder:",e);
    	   	   }
    		   
    		   //save the bound box as shape file
    		   try{
    			   String bbox=outfolder+"\\bbox.shp";
    			   List<Geometry> ggBbox=new ArrayList<Geometry>();
    			
    			   try {
    	    		  ggBbox.add(reader.getBbox(PlatformConfiguration.getConfigurationInstance().getLandMaskMargin(0)));
    				  SimpleShapefile.exportGeometriesToShapeFile(ggBbox, new File(bbox),"Polygon",null,null,null);
    			   } catch (Exception e) {
    				  logger.error("Problem exporting the bounding box:"+e.getLocalizedMessage(),e); 
    			   }
    			   
    			   String bboxcsv=params.outputFolder+"\\bbox.csv";
    			   GenericCSVIO.geomCsv(new File(bboxcsv),ggBbox,null,imageName,true);
    		   }catch(Exception e ){
    			   logger.error("Problem saving bbox in csv:"+imageName,e);
    		   }
    		   
    		   //save targets as shape file
    		   try{
    			   String targets=outfileName.append(".shp").toString();
    			  // l.save(targets, ISave.OPT_EXPORT_SHP, "EPSG:4326");
    			   SimpleShapefile.exportGeometriesToShapeFile(l.getGeometriclayer().getGeometries(), new File(targets),"Point",reader.getGeoTransform(),null,null);
    		   } catch (Exception e) {
 				  logger.error("Problem exporting the bounding box:"+e.getLocalizedMessage(),e); 
 			   }
    		   
    		   if(l.getBand().equals("Merged")||layerResults.size()==1){
	    		   try{
	    			   String targetscsv=params.outputFolder+"\\targets.csv";
	    			   List<Geometry> targets=new ArrayList<Geometry>(l.getGeometriclayer().getGeometries());
	    			   List<Geometry> ambi=new ArrayList<>();
	    			   
	    			   if(l.getGeometriesByTag(ComplexEditVDSVectorLayer.AZIMUTH_AMBIGUITY_TAG)!=null)
	    				   ambi.addAll(l.getGeometriesByTag(ComplexEditVDSVectorLayer.AZIMUTH_AMBIGUITY_TAG).getGeometries());
	    			   
	    			   if(l.getGeometriesByTag(ComplexEditVDSVectorLayer.ARTEFACTS_AMBIGUITY_TAG)!=null)
		    			   ambi.addAll(l.getGeometriesByTag(ComplexEditVDSVectorLayer.ARTEFACTS_AMBIGUITY_TAG).getGeometries());	   
	    			  
		    		   //remove ambiguities
	    			   if(!ambi.isEmpty()){
		    			   for(Geometry geom:targets){
			    			   if(ambi.contains(geom)){
			    				   targets.remove(geom);
			    			   }
		    			   }	   
	    			   }	   
	    			   GenericCSVIO.geomCsv(new File(targetscsv),targets,null,imageName,true);
	    		   }catch(Exception e ){
	    			   logger.error("Problem saving targets in csv:"+imageName,e);
	    		   }
    		   }	   
    	   }
		   //save the bound box as shape file
		   try{
			   String bbox=outfolder+"\\bbox.shp";
			   List<Geometry> ggBbox=new ArrayList<Geometry>();
			
			   try {
	    		  ggBbox.add(reader.getBbox(PlatformConfiguration.getConfigurationInstance().getLandMaskMargin(0)));
				  SimpleShapefile.exportGeometriesToShapeFile(ggBbox, new File(bbox),"Polygon",null,null,null);
			   } catch (Exception e) {
				  logger.error("Problem exporting the bounding box:"+e.getLocalizedMessage(),e); 
			   }
			   
			   String bboxcsv=params.outputFolder+"\\bbox.csv";
			   GenericCSVIO.geomCsv(new File(bboxcsv),ggBbox,null,imageName,true);
		   }catch(Exception e ){
			   logger.error("Problem saving bbox in csv:"+imageName,e);
		   }
		}   
	}
}
