package org.geoimage.viewer.core.io;

import java.io.File;
import java.io.FileNotFoundException;

import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;
import org.gdal.ogr.DataSource;
import org.gdal.ogr.Driver;
import org.gdal.ogr.Feature;
import org.gdal.ogr.FeatureDefn;
import org.gdal.ogr.FieldDefn;
import org.gdal.ogr.Geometry;
import org.gdal.ogr.Layer;
import org.gdal.ogr.ogr;
import org.gdal.ogr.ogrConstants;
import org.gdal.osr.CoordinateTransformation;
import org.gdal.osr.SpatialReference;
import org.gdal.osr.osr;

import it.geosolutions.imageio.gdalframework.GDALUtilities;

public class GDALWarpReprojection {

	public static void reprojectShapeFile(String pathFile, String outPathFile) {
		ogr.RegisterAll();

		DataSource vector = ogr.Open(pathFile);

		SpatialReference src = new SpatialReference(WKTString.WKT3995);
		SpatialReference dst = new SpatialReference(WKTString.WKTWGS84);
		CoordinateTransformation tr = new CoordinateTransformation(src, dst);

		Driver drv = ogr.GetDriverByName("ESRI Shapefile");
		File file = new File(outPathFile);
		if (file.exists())
			drv.DeleteDataSource(outPathFile);

		DataSource outputDs = drv.CreateDataSource(outPathFile);
		Layer outLayer = outputDs.CreateLayer(outPathFile, dst);
		
		FieldDefn field_def = new FieldDefn("DN",ogr.OFTInteger);
		outLayer.CreateField(field_def);
		 
		Layer ll = vector.GetLayer(0);
		// Get the Layer's Feature Definition
		FeatureDefn featureDefn = ll.GetLayerDefn();
		
		int n = ll.GetFeatureCount();

		// outLayer.StartTransaction();
		for (int i = 0; i < n; i++) {
			Feature feat = ll.GetNextFeature();
			Geometry gg = feat.GetGeometryRef();
			Geometry newgg = new Geometry(ogr.wkbPolygon);

			int srcType = gg.GetGeometryType() ;//& (ogrConstants.wkb25DBit);
			if (srcType == ogr.wkbPolygon) {
				//int resg = gg.Transform(tr);
				newgg.AddGeometry(gg);
			} else if (srcType == ogr.wkbMultiPolygon) {
				int geomCount = gg.GetGeometryCount();
				for (int geomIndex = 0; geomIndex < geomCount; geomIndex++) {
					Geometry g=gg.GetGeometryRef(geomIndex);
					int resg = g.Transform(tr);
					newgg.AddGeometry(g);
				}
			}

			// create a new feature
			Feature f = new Feature(featureDefn);
			f.SetGeometry(newgg);
			// f.SetFID(i);
			for (int j = 0; j < feat.GetFieldCount(); j++) {
				String type = feat.GetFieldDefnRef(j).GetFieldTypeName(feat.GetFieldType(j));
				if (type.equals("String")) {
					f.SetField(feat.GetFieldDefnRef(j).GetName(), feat.GetFieldAsString(j));
				} else if (type.equals("StringList")) {

				} else if (type.equals("Integer")) {
					f.SetField(feat.GetFieldDefnRef(j).GetName(), feat.GetFieldAsInteger(j));
				} else if (type.equals("IntegerList")) {
				} else if (type.equals("Real")) {
					f.SetField(feat.GetFieldDefnRef(j).GetName(), feat.GetFieldAsDouble(j));
				} else if (type.equals("RealList")) {
				}
			}

			// Add new feature to output Layer
			int res = outLayer.CreateFeature(f);

			System.out.println("create feature:" + res);

			gg.delete();
			feat.delete();
		}
		// outLayer.CommitTransaction();

	}

	
	
	public static void reprojectTiffWithWarp(String pathFile, String outPathFile) throws FileNotFoundException {
		gdal.AllRegister();
		System.out.println(gdal.VersionInfo());
		
		File f=new File(pathFile);
		if(!f.exists())
			throw new FileNotFoundException();
		
		GDALUtilities.loadGDAL();
		
		// Source
		Dataset source=GDALUtilities.acquireDataSet(pathFile, gdalconstConstants.GA_ReadOnly);//gdal.Open(pathFile);
		
		// Output / destination
        Dataset out_ds = source.GetDriver().Create(outPathFile,source.getRasterXSize(), source.getRasterYSize(), source.getRasterCount());

        

		try{
		
			String srcProj = source.GetProjection();
			double[] trans = source.GetGeoTransform();
	
			// We want a section of source that matches this:
			SpatialReference dstRef = new SpatialReference();
	        dstRef.ImportFromEPSG(4326);
	
	
			
	
	        out_ds.SetProjection(dstRef.ExportToWkt());
	        out_ds.SetGeoTransform(source.GetGeoTransform());
	
	
	        Dataset warp = gdal.AutoCreateWarpedVRT(source, source.GetProjection(), dstRef.ExportToWkt());
	        
			// Do the work
			gdal.ReprojectImage(source, warp);

		}finally {
            source.delete();
            out_ds.delete();
		}
		
	}
	public static void main(String[] args) {
		 //GDALWarpReprojection.reprojectShapeFile("/home/argenpo/Desktop/script_ice/masie_ice_r00_v01_2016037_1km/masie_ice_r00_v01_2016037_1km.shp",
		 //"/home/argenpo/Desktop/script_ice/masie_ice_r00_v01_2016037_1km/masie_ice_r00_v01_2016037_1km_corrected.shp");

		try {
			 //GDALWarpReprojection.reprojectShapeFile("/home/argenpo/Desktop/script_ice/masie_ice_r00_v01_2016037_1km/masie_ice_r00_v01_2016037_1km.shp",
			 //"/home/argenpo/Desktop/script_ice/masie_ice_r00_v01_2016037_1km/masie_ice_r00_v01_2016037_1km_corrected.shp");

			GDALWarpReprojection.reprojectTiffWithWarp(""
					+ "/home/argenpo/Desktop/script_ice/masie_ice_r00_v01_2016037_1km/masie_ice_r00_v01_2016039_1km.tif",
			 "/home/argenpo/Desktop/script_ice/masie_ice_r00_v01_2016037_1km/masie_ice_r00_v01_2016039_1km_out_java.tif");
		} catch (Exception e) {
			e.printStackTrace();
		}

		
	}

}
