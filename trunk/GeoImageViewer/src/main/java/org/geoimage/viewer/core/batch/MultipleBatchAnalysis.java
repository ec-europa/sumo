package org.geoimage.viewer.core.batch;

import java.io.File;
import java.util.List;

import org.geoimage.def.GeoImageReader;
import org.geoimage.def.SarImageReader;
import org.geoimage.factory.GeoImageReaderFactory;
import org.geoimage.utils.IMask;
import org.geoimage.viewer.core.api.GeometricLayer;
import org.geoimage.viewer.core.factory.FactoryLayer;
import org.geoimage.viewer.util.files.SarFileUtil;
import org.slf4j.LoggerFactory;

public class MultipleBatchAnalysis extends AbstractBatchAnalysis{
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(MultipleBatchAnalysis.class);

	
	public MultipleBatchAnalysis(AnalysisParams analysisParams) {
		super(analysisParams);
	}

	/**
	 * 
	 */
	public void startAnalysis(){
			File mainFolder=new File(super.params.pathImg);
			File outputFolder=new File(super.params.outputFolder);
			
			List<File>filesImg=SarFileUtil.scanFolderForImages(mainFolder);
			
			
			for (File image:filesImg){
				try{
					//crate the reader
					List<GeoImageReader> readers =  GeoImageReaderFactory.createReaderForName(image.getAbsolutePath());
					SarImageReader reader=(SarImageReader) readers.get(0);
					
					GeometricLayer gl=null;
					if(params.shapeFile!=null)
						gl=readShapeFile(reader);
					
					IMask[] masks = null;
					if(params.buffer!=0&&gl!=null){
						masks=new IMask[1];
						masks[0]=FactoryLayer.createBufferedLayer("buffered", FactoryLayer.TYPE_COMPLEX, params.buffer, reader, gl);
					}	
					analizeImage(reader,masks);
					
					saveResults(image.getParentFile().getName());
				}catch(Exception e){
					logger.error(getClass().getName(),e);
				}	
			}
	}
	
	
	
	
	
	
}
