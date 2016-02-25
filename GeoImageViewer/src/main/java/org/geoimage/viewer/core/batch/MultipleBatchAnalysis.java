package org.geoimage.viewer.core.batch;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

import org.geoimage.def.GeoImageReader;
import org.geoimage.def.SarImageReader;
import org.geoimage.factory.GeoImageReaderFactory;
import org.geoimage.viewer.core.api.ilayer.IMask;
import org.geoimage.viewer.core.factory.FactoryLayer;
import org.geoimage.viewer.core.layers.GeometricLayer;
import org.geoimage.viewer.core.layers.visualization.vectors.MaskVectorLayer;
import org.geoimage.viewer.util.files.SarFileUtil;
import org.jrc.sumo.configuration.PlatformConfiguration;
import org.jrc.sumo.util.Constant;
import org.slf4j.LoggerFactory;

public class MultipleBatchAnalysis extends AbstractBatchAnalysis{
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(MultipleBatchAnalysis.class);
	private ConfigurationFile confFile;

	public MultipleBatchAnalysis(AnalysisParams analysisParams,ConfigurationFile conf) {
		super(analysisParams);
		confFile=conf;
		super.setRunVersion(conf.getRunVersion());
		super.setRunVersionNumber(conf.getRunVersionNumber());
	}

	/**
	 *
	 */
	public void startAnalysis(){
			List<File>filesImg=SarFileUtil.scanFoldersForImages(super.params.pathImg, confFile.getFilterFolder(), false);

			for (File image:filesImg){
				try{
					logger.info("Start analyzing:"+image.getParent());

					AnalysisParams activeParams=params;

					String folderName=image.getParentFile().getName();

					//output folder have the same name of the input folder
					String imagePathFolder=new StringBuilder(params.outputFolder)
									.append(File.separator)
									.append(folderName).toString();


					boolean forceAnalysis=confFile.forceNewAnalysis();

					//check if already analized
					boolean alreadyAnalyzed=checkAlreadyAnalized(imagePathFolder);

					if(!alreadyAnalyzed || forceAnalysis){
						//check for use local configuration file
						if(confFile.useLocalConfigurationFiles()){
							//path to the single image conf file
							String localImageConfFilePath=new StringBuilder(imagePathFolder)
											.append(File.separator)
											.append(Constant.CONF_FILE).toString();

							activeParams=readLocalConfFile(params,localImageConfFilePath);
						}

						//crate the reader
						List<GeoImageReader> readers =  GeoImageReaderFactory.createReaderForName(image.getAbsolutePath(),PlatformConfiguration.getConfigurationInstance().getS1GeolocationAlgorithm());
						for(GeoImageReader r:readers){
							super.currentReader=r;
							SarImageReader reader=(SarImageReader) r;

							if(confFile.getENL()==0){
								String enl=reader.getENL();
								activeParams.enl=Float.parseFloat(enl);
							}else{
								activeParams.enl=confFile.getENL();
							}

							GeometricLayer gl=null;
							if(activeParams.shapeFile!=null)
								gl=readShapeFile(reader);

							IMask[] masks = null;
							if(gl!=null){
								masks=new IMask[1];
								masks[0]=FactoryLayer.createMaskLayer("buffered",gl.getGeometryType(),activeParams.buffer,  gl,MaskVectorLayer.COASTLINE_MASK);
							}
							analizeImage(reader,masks,activeParams);
							String name=image.getParentFile().getName();
							saveResults(name,masks,reader);
						}
					}
					logger.info("Image processed:"+image.getAbsolutePath());
				}catch(Exception e){
					logger.error("Problem working this image:"+image.getAbsolutePath(),e);
				}
			}
	}

	/**
	 *
	 * @param global
	 * @param image
	 * @return
	 */
	private AnalysisParams readLocalConfFile(final AnalysisParams global,String localImageConfFilePath){
		AnalysisParams local=global;

		//check the local file name in the output folder
		try{
			if(!new File(localImageConfFilePath).exists())
				throw new Exception("Local file not found");
			ConfigurationFile localConf=new ConfigurationFile(localImageConfFilePath);

			//if find the local conf then replace the configuration params
			if(!localConf.getShapeFile().isEmpty())
				params.shapeFile=localConf.getShapeFile();
			if(localConf.getBuffer()!=-1)
				params.buffer=localConf.getBuffer();
			if(localConf.getThresholdArray()!=null){
				float[] array=localConf.getThresholdArray();
				for(int i=0;i<array.length;i++){
					float v=array[i];
					if(v!=-1)
						params.thresholdArrayValues[i]=v;
				}
			}
			if(localConf.getENL()!=0){
				params.enl=localConf.getENL();
			}

		}catch(Exception e){
			logger.warn("Local configuration file:"+localImageConfFilePath +" not loaded. ", e.getMessage());
		}
		return local;
	}

	/**
	 *
	 * @param imagePathFolder
	 * @return
	 */
	private boolean checkAlreadyAnalized(String imagePathFolder){
		boolean analyzed=false;

		File f=new File(imagePathFolder);
		if(f.exists()&&f.isDirectory()){
			String[] xmls=f.list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.contains("VDS analysis");
				}
			});
			if(xmls.length>0)
				analyzed=true;
		}

		return analyzed;
	}




}
