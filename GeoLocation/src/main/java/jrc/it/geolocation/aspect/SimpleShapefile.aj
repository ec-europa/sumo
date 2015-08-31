package jrc.it.geolocation.aspect;

import org.geotools.data.DataStore;
import org.geotools.feature.FeatureCollection;

import com.vividsolutions.jts.geom.Polygon;

public aspect SimpleShapefile {
	private long startTime=0;
	
	pointcut read(): execution (GeometricLayer org.geoimage.viewer.core.io.SimpleShapefileIO.read(GeoImageReader));
	
	pointcut createLayer(): execution(GeometricLayer org.geoimage.viewer.core.io.SimpleShapefileIO.createFromSimpleGeometry(Polygon,String,DataStore,FeatureCollection,String[],String[]));
	
	before(): read(){
		startTime=System.currentTimeMillis();
	}
	after(): read(){
		long endTime=System.currentTimeMillis();
		System.out.println("Read Shapefile:"+(endTime-startTime));
	}
	
	before(): createLayer(){
		startTime=System.currentTimeMillis();
	}
	after(): createLayer(){
		long endTime=System.currentTimeMillis();
		System.out.println("createFromSimpleGeometry:"+(endTime-startTime));
	}
}
