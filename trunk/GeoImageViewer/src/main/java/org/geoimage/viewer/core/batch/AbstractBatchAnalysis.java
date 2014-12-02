package org.geoimage.viewer.core.batch;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geoimage.analysis.VDSAnalysis;
import org.geoimage.def.SarImageReader;
import org.geoimage.utils.IMask;
import org.geoimage.viewer.actions.VDSAnalysisConsoleAction;
import org.geoimage.viewer.core.api.GeometricLayer;
import org.geoimage.viewer.core.factory.VectorIOFactory;
import org.geoimage.viewer.core.io.AbstractVectorIO;
import org.geoimage.viewer.core.layers.vectors.ComplexEditVDSVectorLayer;
import org.slf4j.LoggerFactory;

class AnalysisParams{
	
	//HH HV VH VV
	public int[] thresholdArrayValues={1,1,1,1};
	public String pathImg="";
	public String shapeFile="";
	public String outputFolder="";
	public float enl=1;
	public double buffer=0.0;
	public String epsg="EPSG:4326";	
	public Date startDate;
	public boolean useLocalConfigurationFiles=false;
	
}


public abstract class AbstractBatchAnalysis {
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(AbstractBatchAnalysis.class);
	protected AnalysisParams params;
	private  List<ComplexEditVDSVectorLayer>layerResults=null;
	private SimpleDateFormat dFormat=new SimpleDateFormat("dd-MM-yy");
	
	
	
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
	public void analizeImage(SarImageReader reader,IMask[] masks){
		
		
        VDSAnalysis analysis = new VDSAnalysis(reader,
        		masks, 
        		params.enl, 
        		params.thresholdArrayValues[0], 
        		params.thresholdArrayValues[1], 
        		params.thresholdArrayValues[2], 
        		params.thresholdArrayValues[3], 
        		true);
  
        
        int numberofbands = reader.getNBand();
        final String[] thresholds = new String[numberofbands];
        //management of the strings added at the end of the layer name in order to remember the used threshold
        for (int bb = 0; bb < numberofbands; bb++) {
            if (reader.getBandName(bb).equals("HH") || reader.getBandName(bb).equals("H/H")) {
                thresholds[bb] = "" + params.thresholdArrayValues[0];
            } else if (reader.getBandName(bb).equals("HV") || reader.getBandName(bb).equals("H/V")) {
                thresholds[bb] = "" + params.thresholdArrayValues[1];
            } else if (reader.getBandName(bb).equals("VH") || reader.getBandName(bb).equals("V/H")) {
                thresholds[bb] = "" + params.thresholdArrayValues[2];
            } else if (reader.getBandName(bb).equals("VV") || reader.getBandName(bb).equals("V/V")) {
                thresholds[bb] = "" + params.thresholdArrayValues[3];
            }
        }
        VDSAnalysisConsoleAction action= new VDSAnalysisConsoleAction();
        layerResults=action.runBatchAnalysis(reader,params.enl,analysis,masks,thresholds);
        
	}	
	
	/**
	 * 
	 */
	protected void saveResults(String imageName){
		if(layerResults!=null){
    	   for(ComplexEditVDSVectorLayer l:layerResults){
    		   String outfolder=new StringBuilder(params.outputFolder)
    		   					.append(File.separator)
    		   					.append(imageName).toString();
    		   
    		   //create folder if not exist
    		   File folder=new File(outfolder);
    		   if(!folder.exists())
    			   folder.mkdirs();
    		   
    		   String outfile=new StringBuilder(outfolder).append(File.separator)
    				            .append(l.getName()).append("_")
    				   			.append(dFormat.format(params.startDate)).toString();
    		   
    		   System.out.println("Writing:"+outfile);
    		   l.save(outfile,ComplexEditVDSVectorLayer.OPT_EXPORT_XML_SUMO_OLD,params.epsg);
    		   l.save(outfile+"_new",ComplexEditVDSVectorLayer.OPT_EXPORT_XML_SUMO,params.epsg);
    	   }
        }
	}
}
