package org.geoimage.viewer.core.batch;

import java.util.List;

import org.geoimage.def.GeoImageReader;
import org.geoimage.def.SarImageReader;
import org.geoimage.factory.GeoImageReaderFactory;
import org.geoimage.viewer.core.GeometryCollection;
import org.geoimage.viewer.core.api.ilayer.IMask;
import org.geoimage.viewer.core.factory.FactoryLayer;
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
	protected void startAnalysis(){
		try{
			//crate the reader
			List<GeoImageReader> readers =  GeoImageReaderFactory.createReaderForName(params.pathImg[0],PlatformConfiguration.getConfigurationInstance().getS1GeolocationAlgorithm());

			for(GeoImageReader r:readers){
				//currentReader=r;
				SarImageReader reader=(SarImageReader) r;
				String enl=reader.getENL();
				params.enl=Float.parseFloat(enl);

				GeometryCollection gl=null;
				if(params.shapeFile!=null){
			    	Polygon imageP=(reader).getBbox(PlatformConfiguration.getConfigurationInstance().getLandMaskMargin(0));
					gl=readShapeFile(imageP,reader.getGeoTransform());
				}
				IMask masks = null;
				if(gl!=null){
					masks=FactoryLayer.createMaskLayer("buffered", gl.getGeometryType(), params.buffer, gl,MaskVectorLayer.COASTLINE_MASK);
				}

				analizeImage(reader,masks,null,params);
				saveResults(reader.getImgName(),reader);
			}
		}catch(Exception e){
			logger.error(e.getMessage(),e);
		}
	}
}
