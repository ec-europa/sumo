package org.geoimage.viewer.core.batch;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.geoimage.def.GeoImageReader;
import org.geoimage.viewer.core.SumoPlatform;
import org.slf4j.LoggerFactory;








public class Sumo {
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(Sumo.class);

	//status
	protected final int PARAM_ERROR=-1;
	protected final int SINGLE_IMG_ANALYSIS=1;
	protected final int MULTI_IMG_ANALYSIS=2;

	private int status=SINGLE_IMG_ANALYSIS;
	private String msg="";

	private final  String TOO_MUCH_PARAMS="-d and -f are exsclusive params";
	private final  String FOLDER_ERROR="Folder not exist, check path";
	private final  String FILE_ERROR="File not exist, check path";

	//starting params for the command line
	private static final String TRESH_HH_PARAM="-thh";
	private static  final String TRESH_HV_PARAM="-thv";
	private static  final String TRESH_VH_PARAM="-tvh";
	private static  final String TRESH_VV_PARAM="-tvv";
	private static  final String[] TRESH_PARAMS={TRESH_HH_PARAM, TRESH_VH_PARAM , TRESH_HV_PARAM ,TRESH_VV_PARAM };

	//for a single image
	private static  final String IMG_PARAM="-i";
	//for multiple images
	private static  final String IMG_FOLD_PARAM="-d";

	private static  final String BUFFER_PARAM="-b";
	private static  final String OUTPUT_FOLD_PARAM="-o";
	private static  final String SHP_FILE="-sh";
	private static  final String GLOBAL_CONF_FILE_PARAM="-gconf";
	private static  final String LOCAL_CONF_FILE_PARAM="-uselocalconf"; // Y/N

	//


	private AnalysisParams params;
	private ConfigurationFile conf;
	private static AbstractBatchAnalysis batch=null;
	private Date startDate;


	public Sumo(){
		params=new AnalysisParams();
		SumoPlatform.setInBatchMode();
		startDate=new Date();
		params.startDate=startDate;
	}

	public static GeoImageReader getCurrentReader(){
		return batch.getCurrentReader();
	}

	/**
	 *
	 */
	public void execAnalysis(){

		if(status==SINGLE_IMG_ANALYSIS){
			batch=new SingleBatchAnalysis(params);

		}else{
			batch=new MultipleBatchAnalysis(params,conf);
		}
		batch.runProcess();
	}

	/**
	 *
	 * @param params
	 * @return
	 */
	protected int parseParams(List<String> inputParams){
		this.status=0; //OK

		//for  the moment, the configuration file is ONLY FOR THE MULTIPLE ANALYSIS
		int idx=inputParams.indexOf(GLOBAL_CONF_FILE_PARAM);
		if(idx!=-1){
			String confFile=inputParams.get(idx+1);
			try {
				conf=new ConfigurationFile(confFile);
				params.shapeFile=conf.getShapeFile();
				params.buffer=conf.getBuffer();
				params.enl=conf.getENL();
				params.thresholdArrayValues=conf.getThresholdArray();
				params.pathImg=conf.getInputFolder();
				params.outputFolder=conf.getOutputFolder();
				params.maxDetections=conf.getMaxDetections();
				params.xmlOutputFolder=conf.getXMLOutputFolder();
				status=MULTI_IMG_ANALYSIS;
			} catch (IOException e) {
				e.printStackTrace();
				status=PARAM_ERROR;
			}
		}else{

			//XOR se i parametri non contengono o contengono entrambi
	        if (!(inputParams.contains(IMG_FOLD_PARAM)  ^ inputParams.contains(IMG_PARAM) )){
	        	status=PARAM_ERROR;
	        	msg=TOO_MUCH_PARAMS;
	        }else if(inputParams.contains(IMG_FOLD_PARAM)){
	        	idx=inputParams.indexOf(IMG_FOLD_PARAM);
	        	String dir=inputParams.get(idx+1);
	        	File f=new File(dir);
	        	if(f.exists()&&f.isDirectory()){
	        		status=MULTI_IMG_ANALYSIS;
	        		params.pathImg=new String[]{dir};
	        	}else{
	        		status=PARAM_ERROR;
	        		msg=FOLDER_ERROR;
	        	}
	        }else if(inputParams.contains(IMG_PARAM)){
	        	idx=inputParams.indexOf(IMG_PARAM);
	        	String dir=inputParams.get(idx+1);
	        	dir=dir.replace("\"", "");
	        	File f=new File(dir);
	        	if(f.exists()&&f.isFile()){
	        		status=SINGLE_IMG_ANALYSIS;
	        		params.pathImg=new String[]{dir};
	        	}else{
	        		status=PARAM_ERROR;
	        		msg=FILE_ERROR;
	        	}
	        }

	        //parameters are OK
	        if(status!=PARAM_ERROR){
	        	int index=inputParams.indexOf(BUFFER_PARAM);
	        	if(index!=-1){
	        		params.buffer=Integer.parseInt(inputParams.get(index+1));
	        	}else{
	        		params.buffer=Integer.parseInt(SumoPlatform.getApplication().getConfiguration().getBufferingDistance());
	        	}

	        	//set the treshold params
	        	for(int i=0;i<TRESH_PARAMS.length;i++){
	        		//leggo i valori di threshold se li trovo
	        		index=inputParams.indexOf(TRESH_PARAMS[i]);
	        		if(index!=-1){
	        			//li setto nell'array dei threshold con questo ordine HH HV VH VV
	        			float val=Float.parseFloat(inputParams.get(index+1));
	        			params.thresholdArrayValues[i]=val;
	        		}
	        	}

	        	//check for the output folder
	        	index=inputParams.indexOf(OUTPUT_FOLD_PARAM);
	        	if(index!=-1){
	        		params.outputFolder=inputParams.get(index+1);
	        	}else{
	        		//if the output folder is not setted we create an output folder under the current folder
	        		File f=new File("."+File.separator+"outupt_"+System.currentTimeMillis()+File.separator);
	        		logger.debug("Setting output folder:"+f.getAbsolutePath());
	        		f.mkdir();
	        		params.outputFolder=f.getAbsolutePath();
	        	}

	        	//check for shp file
	        	index=inputParams.indexOf(SHP_FILE);
	        	if(index!=-1){
	        		params.shapeFile=inputParams.get(index+1);
	        	}else{
	        		//search default shp file
	        		params.shapeFile=SumoPlatform.getApplication().getConfiguration().getDefaultLandMask();
	        	}
	        }
        }

		return status;
	}


	public static void main(String[] args) {
		/**@todo
         * arguments
         * -i : path to single image folder
         * -d : path to the directory that contains the image folders
         *
         *
         * -sh: specify shape file (if is not passed , use land mask for the analysis)
         *
         * -gf: specify global file configuration
         *
         *
         * -enl : equinvalent number of looks
         * -thh threshold HH
         * -thv threshold HV
         * -tvh threshold VH
         * -tvv threshold HV
         *
         * -o output dir to store files if no ddbb storage
         *
         *
         */
		long start=System.currentTimeMillis();
		System.out.println("Start SUMO in batch mode");
		List<String> params=Arrays.asList(args);
		Sumo s=new Sumo();
		int  st = s.parseParams(params);
		if(st!=s.PARAM_ERROR){
			System.out.println("Run Analysis");
			s.execAnalysis();
			System.out.println("Save results");
		}
		long end=System.currentTimeMillis();
		long time=end-start;
		System.out.println("Execution time:"+time);
		///logger.debug("Execution time:"+time);
		System.out.println("Exit");
		System.exit(0);
	}

}
