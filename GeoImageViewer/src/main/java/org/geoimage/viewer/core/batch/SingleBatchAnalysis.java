package org.geoimage.viewer.core.batch;

import java.util.List;

import org.geoimage.def.GeoImageReader;
import org.geoimage.def.SarImageReader;
import org.geoimage.factory.GeoImageReaderFactory;
import org.geoimage.utils.IMask;
import org.geoimage.viewer.core.factory.FactoryLayer;
import org.geoimage.viewer.core.layers.GeometricLayer;


public class SingleBatchAnalysis extends AbstractBatchAnalysis {

	
	public SingleBatchAnalysis(AnalysisParams analysisParams) {
		super(analysisParams);
	}

	/**
	 * 
	 */
	protected void startAnalysis(){
		//crate the reader
		List<GeoImageReader> readers =  GeoImageReaderFactory.createReaderForName(params.pathImg);
		
		for(GeoImageReader r:readers){
			currentReader=r;
			SarImageReader reader=(SarImageReader) r;
			String enl=reader.getENL();
			params.enl=Float.parseFloat(enl);
			
			GeometricLayer gl=null;
			if(params.shapeFile!=null)
				gl=readShapeFile(reader);
			
			IMask[] masks = null;
			if(params.buffer!=0&&gl!=null){
				masks=new IMask[1];
				masks[0]=FactoryLayer.createMaskLayer("buffered", FactoryLayer.TYPE_COMPLEX, params.buffer, reader, gl);
			}	
			
			analizeImage(reader,masks,params);
			saveResults(reader.getImgName(),masks,reader);
		}	
	}	
}
