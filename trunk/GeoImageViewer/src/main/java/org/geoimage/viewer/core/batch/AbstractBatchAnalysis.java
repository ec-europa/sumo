package org.geoimage.viewer.core.batch;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geoimage.analysis.VDSAnalysis;
import org.geoimage.def.SarImageReader;
import org.geoimage.impl.cosmo.CosmoSkymedImage;
import org.geoimage.utils.IMask;
import org.geoimage.viewer.actions.VDSAnalysisConsoleAction;
import org.geoimage.viewer.core.api.GeometricLayer;
import org.geoimage.viewer.core.factory.FactoryLayer;
import org.geoimage.viewer.core.factory.VectorIOFactory;
import org.geoimage.viewer.core.io.AbstractVectorIO;
import org.geoimage.viewer.core.io.SumoXMLWriter;
import org.geoimage.viewer.core.layers.vectors.ComplexEditVDSVectorLayer;
import org.geoimage.viewer.util.Utils;
import org.slf4j.LoggerFactory;


class AnalysisParams {
	//HH HV VH VV
	public float[] thresholdArrayValues={};
	public String pathImg="";
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
	
	
	public AbstractBatchAnalysis(AnalysisParams analysisParams){
		params= analysisParams;
		layerResults=null;//new ArrayList<ComplexEditVDSVectorLayer>();
	}
	
		
	protected abstract void startAnalysis();
	
	
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
  	    Map<String,Object> config = new HashMap<String,Object>();
	    try {
            config.put("url", new File(params.shapeFile).toURI().toURL());
            AbstractVectorIO shpio = VectorIOFactory.createVectorIO(VectorIOFactory.SIMPLE_SHAPEFILE, config);
            gl = shpio.read(reader);
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
        		params.thresholdArrayValues[3], 
        		true);
  
        
        final String[] thresholds=Utils.getStringThresholdsArray(reader, params.thresholdArrayValues);
        
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

    		   
    		   System.out.println("Writing:"+outfile.toString());
    		   l.save(outfile.toString(),ComplexEditVDSVectorLayer.OPT_EXPORT_XML_SUMO_OLD,params.epsg);
    		   
    		   
    		   SumoXMLWriter newWriter=new SumoXMLWriter();
    		   
    		   String file=new String(outfile.append("_new").toString());
    		   if (!file.endsWith(".xml")) {
	                file = file.concat(".xml");
	            }
	           HashMap<String,Object> config=new HashMap<String,Object>(); 
	           config.put(SumoXMLWriter.CONFIG_FILE, file);
    		   newWriter.setConfig(config);
    		   newWriter.saveNewXML(FactoryLayer.createThresholdedLayer(l.getGeometriclayer(),l.getThresh(),l.isThreshable()), 
    				   params.epsg,reader,params.thresholdArrayValues,params.buffer,params.enl);
    	   }
        }
	}
}
