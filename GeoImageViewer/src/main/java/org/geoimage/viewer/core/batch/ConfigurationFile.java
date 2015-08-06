package org.geoimage.viewer.core.batch;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;


/**
 * @author Pietro Argentieri
 * 
 */


public class ConfigurationFile {
	//starting params
	public static  final String TRESH_HH_PARAM="thh";
	public static  final String TRESH_HV_PARAM="thv";
	public static  final String TRESH_VH_PARAM="tvh";
	public static  final String TRESH_VV_PARAM="tvv";
	
	public static  final String SHP_FILE="shape_file";
	public static  final String BUFFER_PARAM="buffer";
	public static  final String INPUT_FOLD_PARAM="input_folder";
	public static  final String OUTPUT_FOLD_PARAM="output_folder";
	
	public static  final String USE_LOCAL_CONF="use_local_conf";
	//public static  final String REP_OLD_XML_ANALYSIS="replace_old_analysis";
	public static  final String FORCE_NEW_ANALYSIS="force_new_analysis";
	public static  final String FILTER_FOLDER="filter";
	public static final String MAX_DETECTIONS_ALLOWED="max_detections_allowed";
	
	private String confFile;
	private Properties prop = new Properties();
	
		/**
		 * 
		 * @param configurationFile
		 * @throws IOException
		 */
		public ConfigurationFile(String configurationFile) throws IOException{
			confFile=configurationFile;
			readConfiguration();
		}
	 
		/**
		 * 
		 * @throws IOException
		 */
		private void readConfiguration() throws IOException {
			InputStream inputStream = new FileInputStream(new File(confFile));
	 
			if (inputStream != null) {
				prop.load(inputStream);
			} 
		}
		
		public String getProperty(String property){
			return prop.getProperty(property);
		}
		
		
		/***
		 * 
		 * @return
		 */
		public float[] getThresholdArray(){
			float[] thresholds={0,0,0,0};
			
			//-1 not setted 
			String tmp=prop.getProperty(TRESH_HH_PARAM,"0");
			thresholds[0]=Float.parseFloat(tmp);
			
			tmp=prop.getProperty(TRESH_HV_PARAM,"0");
			thresholds[1]=Float.parseFloat(tmp);
						
			tmp=prop.getProperty(TRESH_VH_PARAM,"0");
			thresholds[2]=Float.parseFloat(tmp);
			
			tmp=prop.getProperty(TRESH_VV_PARAM,"0");
			thresholds[3]=Float.parseFloat(tmp);
			
			
			return thresholds;
		}
		
		/**
		 * 
		 * @return the buffer param or -1 if is not configured
		 */
		public int getBuffer(){
			int buffer=-1;
			String tmp=prop.getProperty(BUFFER_PARAM);
			if(tmp!=null&&!tmp.isEmpty()){
				buffer=Integer.parseInt(tmp);
			}
			return buffer;
		}
		
		/**
		 * 
		 * @return 
		 */
		public String getShapeFile(){
			return prop.getProperty(SHP_FILE,"");
		}
		/**
		 * 
		 * @return
		 */
		public String getOutputFolder(){
			return prop.getProperty(OUTPUT_FOLD_PARAM,"");
		}
		/**
		 * 
		 * @return
		 */
		public String[] getInputFolder(){
			List<String>inputs=new ArrayList<>();
			Set<Object> ks=prop.keySet();
			for(Object o:ks){
				if(((String)o).contains(INPUT_FOLD_PARAM)){
					inputs.add((String)prop.get(o));
				}
			}
			
			return inputs.toArray(new String[0]);
		}
		
		
		public String getFilterFolder(){
			return prop.getProperty(FILTER_FOLDER,"*");
		}
		
		public boolean useLocalConfigurationFiles(){
			String tmp= prop.getProperty(USE_LOCAL_CONF,"false");
			return Boolean.parseBoolean(tmp);
		}
		
		public boolean forceNewAnalysis(){
			String tmp= prop.getProperty(FORCE_NEW_ANALYSIS,"false");
			return Boolean.parseBoolean(tmp);
		}
		
		public int getMaxDetections(){
			String val=prop.getProperty(MAX_DETECTIONS_ALLOWED,"0");
			int max=0;
			try{
				max=Integer.parseInt(val);
			}catch(Exception e){}	
			return max;
		}
		
		/*public boolean replaceOldAnalysis(){
			String tmp= prop.getProperty(REP_OLD_XML_ANALYSIS,"false");
			return Boolean.parseBoolean(tmp);
		}*/
		
		public static void main(String[] args){
			try {
				ConfigurationFile f=new ConfigurationFile("C:\\tmp\\output\\analysis.conf");
				double b=f.getBuffer();
				System.out.println(b);
				float[] tt=f.getThresholdArray();
				System.out.println(tt[0]);
				System.out.println(tt[1]);
				System.out.println(tt[2]);
				System.out.println(tt[3]);
				System.out.println(f.getShapeFile());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
}




