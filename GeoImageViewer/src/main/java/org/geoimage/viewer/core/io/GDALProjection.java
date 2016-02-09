package org.geoimage.viewer.core.io;

import org.gdal.ogr.DataSource;
import org.gdal.ogr.Feature;
import org.gdal.ogr.FeatureDefn;
import org.gdal.ogr.Geometry;
import org.gdal.ogr.Layer;
import org.gdal.ogr.ogr;
import org.gdal.osr.CoordinateTransformation;
import org.gdal.osr.SpatialReference;

public class GDALProjection {

	
	public static void test(String pathFile,String outPathFile){
		ogr.RegisterAll();
		
		
		DataSource vector=ogr.Open(pathFile);
		
		SpatialReference src=new SpatialReference(WKTString.WKT3995);
		SpatialReference dst=new SpatialReference(WKTString.WKTWGS84);
		CoordinateTransformation tr=new CoordinateTransformation(src, dst);
		
		DataSource outputDs = ogr.GetDriverByName("ESRI Shapefile").CreateDataSource(outPathFile);
		Layer outLayer = outputDs.CreateLayer(outPathFile,dst);

		
		//Get the output Layer's Feature Definition
	    FeatureDefn featureDefn = outLayer.GetLayerDefn();
	    
		Layer ll=vector.GetLayer(0);
		
		int n=ll.GetFeatureCount();
		
		outLayer.StartTransaction();
		for (int i=0;i<n;i++){
			Feature feat=ll.GetFeature(i);
			Geometry gg=feat.GetGeometryRef();
			gg.Transform(tr);
			
			// create a new feature
	        Feature f=new Feature(featureDefn);
			f.SetGeometry(gg);
			
			// Add new feature to output Layer
			int res=outLayer.CreateFeature(f);
			
			System.out.println("feature:"+res);

		}
		outLayer.CommitTransaction();
		//vector.delete();
	}
	
	public static void main(String[] args){
		GDALProjection.test("/home/argenpo/Desktop/script_ice/masie_ice_r00_v01_2016037_1km/masie_ice_r00_v01_2016037_1km.shp"
    				, "/home/argenpo/Desktop/script_ice/masie_ice_r00_v01_2016037_1km/masie_ice_r00_v01_2016037_1km_corrected.shp");
	}
	
	
	
}
