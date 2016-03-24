/*
 * 
 */
package org.geoimage.viewer.core.batch;

import java.util.List;

import org.geoimage.def.GeoImageReader;
import org.geoimage.def.SarImageReader;
import org.geoimage.factory.GeoImageReaderFactory;
import org.geoimage.viewer.core.GeometryImage;
import org.geoimage.viewer.core.analysisproc.AnalysisProcess;
import org.geoimage.viewer.core.api.ilayer.IMask;
import org.geoimage.viewer.core.factory.FactoryLayer;
import org.geoimage.viewer.core.layers.visualization.vectors.ComplexEditVDSVectorLayer;
import org.geoimage.viewer.core.layers.visualization.vectors.MaskVectorLayer;
import org.jrc.sumo.configuration.PlatformConfiguration;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Polygon;


public class SingleBatchAnalysis extends AbstractBatchAnalysis {
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(SingleBatchAnalysis.class);


	public SingleBatchAnalysis(AnalysisParams analysisParams) {
		super(analysisParams);
	}

	/**
	 *
	 */
	protected void runAnalysis(){
		try{
			//crate the reader
			List<GeoImageReader> readers =  GeoImageReaderFactory.createReaderForName(params.pathImg[0],PlatformConfiguration.getConfigurationInstance().getS1GeolocationAlgorithm());

			for(GeoImageReader r:readers){
				//currentReader=r;
				SarImageReader reader=(SarImageReader) r;
				String enl=reader.getENL();
				params.enl=Float.parseFloat(enl);

				GeometryImage gl=null;
				if(params.shapeFile!=null){
			    	Polygon imageP=(reader).getBbox(PlatformConfiguration.getConfigurationInstance().getLandMaskMargin(0));
					gl=readShapeFile(params.shapeFile,imageP,reader.getGeoTransform());
				}
				IMask masks = null;
				if(gl!=null){
					masks=FactoryLayer.createMaskLayer("buffered", gl.getGeometryType(), params.buffer, gl,MaskVectorLayer.COASTLINE_MASK);
				}

				AnalysisProcess ap=prepareBatchAnalysis(reader,masks,null,params);
				AnalysisProcess.Results rs=ap.call();
				List<ComplexEditVDSVectorLayer> results=rs.getLayerResults();
				saveResults(reader.getImgName(),reader,results);
			}
		}catch(Exception e){
			logger.error(e.getMessage(),e);
		}
	}
}
