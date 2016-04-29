/*
 *
 */
package org.geoimage.viewer.core.batch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FilenameUtils;
import org.geoimage.def.GeoImageReader;
import org.geoimage.def.SarImageReader;
import org.geoimage.factory.GeoImageReaderFactory;
import org.geoimage.viewer.core.GeometryImage;
import org.geoimage.viewer.core.SumoPlatform;
import org.geoimage.viewer.core.analysisproc.AnalysisProcess;
import org.geoimage.viewer.core.api.ilayer.IMask;
import org.geoimage.viewer.core.factory.FactoryLayer;
import org.geoimage.viewer.core.layers.visualization.vectors.ComplexEditVDSVectorLayer;
import org.geoimage.viewer.core.layers.visualization.vectors.MaskVectorLayer;
import org.geoimage.viewer.util.files.ArchiveUtil;
import org.geoimage.viewer.util.files.IceHttpClient;
import org.geoimage.viewer.util.files.SarFileUtil;
import org.jrc.sumo.configuration.PlatformConfiguration;
import org.jrc.sumo.util.Constant;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Polygon;

public class MultipleBatchAnalysis extends AbstractBatchAnalysis {
	private static org.slf4j.Logger logger = LoggerFactory.getLogger(MultipleBatchAnalysis.class);
	private ConfigurationFile confFile;
	private int NTHREDS = 4;

	public MultipleBatchAnalysis(AnalysisParams analysisParams, ConfigurationFile conf) {
		super(analysisParams);
		confFile = conf;
		super.setRunVersion(conf.getRunVersion());
		super.setRunVersionNumber(conf.getRunVersionNumber());
		NTHREDS = Runtime.getRuntime().availableProcessors();
	}

	/**
	 *
	 * @return
	 */
	private List<File> readFileList() {
		try {
			List<File> files = new ArrayList<>();
			String fl = confFile.getInputFileList();
			BufferedReader br = new BufferedReader(new FileReader(fl));
			String line = null;
			while ((line = br.readLine()) != null) {
				if (!line.equals("")) {
					try {
						File manifest = SarFileUtil.findManifest(new File(line));
						files.add(manifest);
					} catch (Exception e) {
						logger.warn("Problem with this image:" + line);
						continue;
					}
				}
			}
			br.close();
			return files;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * 
	 * @param filesImg
	 */
	private void multiThreadingLoop(List<File> filesImg) {
		// Get the ThreadFactory implementation to use
		// creating the ThreadPoolExecutor
		int minT = NTHREDS > 1 ? 2 : 1;
		ThreadPoolExecutor executorPool = new ThreadPoolExecutor(minT, NTHREDS, 60, TimeUnit.SECONDS,new LinkedBlockingQueue<>(50));
		final List<Future<AnalysisProcess.Results>> tasks = new ArrayList<Future<AnalysisProcess.Results>>();
		ExecutorCompletionService<AnalysisProcess.Results> ecs = new ExecutorCompletionService<AnalysisProcess.Results>(executorPool);
		int count=0;
		int completati=0;
		try {
			for (File image : filesImg) {
				try {
					AnalysisProcess ap = singleAnalysisTask(image);
					tasks.add(ecs.submit(ap));
					count++;
				} catch (Exception e) {
					logger.error("Image not analyzed:" + image, e);
				}
				if(count%5==0||(filesImg.size()-(count+1))<=0){
					// retrieve and save the result
					while (!executorPool.getQueue().isEmpty()||((filesImg.size()-(count+1))<0)&&completati!=filesImg.size()){
						try {
							AnalysisProcess.Results res = (AnalysisProcess.Results) ecs.take().get();
							List<ComplexEditVDSVectorLayer> results = res.getLayerResults();
							SarImageReader gr = (SarImageReader) res.getReader();
							String name = (gr).getManifestFile().getParentFile().getName();
							saveResults(name, gr, results);
							completati++;
						} catch (Exception e) {
							logger.error(e.getMessage(), e);
						}finally{
							
						}
					}
				}
			}
			
		} finally {
			try {
				if(!executorPool.getQueue().isEmpty())
					executorPool.awaitTermination(180,TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				logger.warn(e.getMessage(), e);
			}
			executorPool.shutdown();
			logger.info("Analyzed:"+count+"  images");
		}
	}

	/**
	 * 
	 * @param filesImg
	 */
	private void seqLoop(List<File> filesImg) {
		for (File image : filesImg) {
			try {
				AnalysisProcess ap = singleAnalysisTask(image);
				AnalysisProcess.Results results=ap.call();
				SarImageReader gr = (SarImageReader) results.getReader();
				String name = gr.getManifestFile().getParentFile().getName();
				saveResults(name, gr, results.getLayerResults());
			} catch (Exception e) {
				logger.error("Image not analyzed:" + image, e);
			}
		}
	}
	
	/**
	 * 
	 * @param image
	 * @return
	 */
	public AnalysisProcess singleAnalysisTask(File image){
		AnalysisProcess ap=null;
			logger.info("Start analyzing IMAGE:"+image.getParent());
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
					//super.currentReader=r;
					SarImageReader reader=(SarImageReader) r;

					if(confFile.getENL()==0){
						String enl=reader.getENL();
						activeParams.enl=Float.parseFloat(enl);
					}else{
						activeParams.enl=confFile.getENL();
					}

					GeometryImage gl=null;
					Polygon imageP=null;
					try {
						imageP = (reader).getBbox(PlatformConfiguration.getConfigurationInstance().getLandMaskMargin(0));
					} catch (Exception e) {
						logger.info("Image not analyzed:"+reader.getImgName()+"  Error creating box:"+e.getMessage());
						continue;
					}

					if(activeParams.shapeFile!=null)
						gl=readShapeFile(params.shapeFile,imageP,reader.getGeoTransform());

					IMask mask = null;
					if(gl!=null){
						mask=FactoryLayer.createMaskLayer(params.shapeFile,gl.getGeometryType(),activeParams.buffer,  gl,MaskVectorLayer.COASTLINE_MASK);
					}

					IMask iceMask = null;
					if(confFile.useIceRepositoryShapeFile()){
						File ice=getIceShapeFile(r.getImageDate());
						if(ice!=null){
							activeParams.iceShapeFile=ice.getAbsolutePath();
							GeometryImage glIce=readShapeFile(params.iceShapeFile,imageP,reader.getGeoTransform());
							if(glIce!=null)
								iceMask=FactoryLayer.createMaskLayer(params.iceShapeFile,glIce.getType(),0,glIce,MaskVectorLayer.ICE_MASK);
						}
					}

					ap=prepareBatchAnalysis(reader, mask, iceMask, activeParams);
					ap.addProcessListener(this);
				}
			}
			return ap;
		}

		/**
		 *
		 */
		public void runAnalysis() {
			long start=System.currentTimeMillis()/1000;
			List<File> filesImg = null;
			if (!confFile.getUseFileList())
				filesImg = SarFileUtil.scanFoldersForImages(super.params.pathImg, confFile.getFilterFolder(), false);
			else
				filesImg = readFileList();

			if (filesImg != null) {
				if(this.confFile.isMultithread())
					multiThreadingLoop(filesImg);
				else 
					seqLoop(filesImg);

			} else {
				logger.error("No file found to analyze");
			}
			long end=System.currentTimeMillis()/1000;
			System.out.println("Process terminated in:"+(end-start)+" seconds");
		}

		/**
		 *
		 * @param imgDate
		 *            the date of the image. It is used to download the correct
		 *            shape file fron the repository
		 * @return the ice shp file
		 */
		private File getIceShapeFile(Date imgDate) {
			File ice = null;
			try {
				String icePatternName = confFile.getIceShapeFileName();
				boolean isRemote = confFile.getIsRemoteRepoIceFile();
				String iceRepoUrl = confFile.getIceRepositoryUrl();
				if (isRemote) {
					String tokenName = icePatternName.substring(icePatternName.indexOf("%") + 1,
							icePatternName.lastIndexOf("%"));
					String tokenUrl = iceRepoUrl.substring(iceRepoUrl.indexOf("%") + 1, iceRepoUrl.lastIndexOf("%"));

					SimpleDateFormat fd = new SimpleDateFormat(tokenName);
					String strImgDate=fd.format(imgDate);
					icePatternName = icePatternName.replace("%" + tokenName + "%",strImgDate );

					fd.applyPattern(tokenUrl);
					String strYearDate=fd.format(imgDate);
					iceRepoUrl = iceRepoUrl.replace("%" + tokenUrl + "%", strYearDate);

					if(!iceRepoUrl.endsWith(File.separator)||iceRepoUrl.endsWith("/"))
						iceRepoUrl.concat(File.separator);
					String completeUrl = iceRepoUrl.concat(icePatternName);
					String output = SumoPlatform.getApplication().getCachePath().concat(File.separator)
							.concat(icePatternName);
					ice = new IceHttpClient().download(completeUrl, output);
					if (ArchiveUtil.isArchive(ice)) {
						ArchiveUtil.unZip(ice.getAbsolutePath());
						File[] shpfiles = ice.getParentFile().listFiles((java.io.FileFilter) pathname ->( FilenameUtils
								.getExtension(pathname.getName()).equalsIgnoreCase("shp")&&pathname.getName().contains(strImgDate)));
						ice = shpfiles[0];
					}

				} else {

				}
			} catch (Exception e) {
				logger.error("Ice shape file not loaded:" + e.getMessage());
				ice = null;
			}
			return ice;
		}

		/**
		 *
		 * @param global
		 * @param image
		 * @return
		 */
		private AnalysisParams readLocalConfFile(final AnalysisParams global, String localImageConfFilePath) {
			AnalysisParams local = global;

			// check the local file name in the output folder
			try {
				if (!new File(localImageConfFilePath).exists())
					throw new Exception("Local file not found");
				ConfigurationFile localConf = new ConfigurationFile(localImageConfFilePath);

				// if find the local conf then replace the configuration params
				if (!localConf.getShapeFile().isEmpty())
					params.shapeFile = localConf.getShapeFile();
				if (localConf.getBuffer() != -1)
					params.buffer = localConf.getBuffer();
				if (localConf.getThresholdArray() != null) {
					float[] array = localConf.getThresholdArray();
					for (int i = 0; i < array.length; i++) {
						float v = array[i];
						if (v != -1)
							params.thresholdArrayValues[i] = v;
					}
				}
				if (localConf.getENL() != 0) {
					params.enl = localConf.getENL();
				}

			} catch (Exception e) {
				logger.warn("Local configuration file:" + localImageConfFilePath + " not loaded. ", e.getMessage());
			}
			return local;
		}

		/**
		 *
		 * @param imagePathFolder
		 * @return
		 */
		private boolean checkAlreadyAnalized(String imagePathFolder) {
			boolean analyzed = false;

			File f = new File(imagePathFolder);
			if (f.exists() && f.isDirectory()) {
				String[] xmls = f.list(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.contains("VDS analysis");
					}
				});
				if (xmls.length > 0)
					analyzed = true;
			}

			return analyzed;
		}

	}
