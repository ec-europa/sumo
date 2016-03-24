/*
 * 
 */
package org.geoimage.viewer.core.io;
import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;
import org.gdal.ogr.*;
import org.gdal.osr.SpatialReference;


public class TiffFile {




	    public static void gdalPolygonize(String input, String output)
	    {
	        gdal.AllRegister();
	        ogr.RegisterAll();
	        

	        //Open source file
	        Dataset hDataset = gdal.Open(input, gdalconstConstants.GA_ReadOnly);        
	        Band rasterBand = hDataset.GetRasterBand(1);
	        Band maskBand = rasterBand.GetMaskBand();

	        Driver driver = ogr.GetDriverByName("ESRI Shapefile");
	        DataSource dataSource =  driver.CreateDataSource(output);

	        SpatialReference srs = null;
	        if(!hDataset.GetProjectionRef().isEmpty())
	            srs = new SpatialReference(hDataset.GetProjectionRef());

	        Layer outputLayer = dataSource.CreateLayer("destination", srs);
	        FieldDefn field_def = new FieldDefn("DN",ogr.OFTInteger);
	        outputLayer.CreateField(field_def);
	        gdal.Polygonize(rasterBand, maskBand, outputLayer, 0);
	    }
	    
	    public static void gdalWarpReprojecting(String input, String output){
	    	gdal.AllRegister();
	        
	        Dataset hDataset = gdal.Open(input, gdalconstConstants.GA_ReadOnly);
	        
	        SpatialReference origRef = new SpatialReference();
	        origRef.ImportFromEPSG(3995);

	        
	        SpatialReference dstRef = new SpatialReference();
	        dstRef.ImportFromEPSG(4326);
	        
	        Dataset warp=gdal.AutoCreateWarpedVRT(hDataset,origRef.toString(),dstRef.toString());

	        double[] transformation= warp.GetGeoTransform();
     		org.gdal.gdal.Driver driver = gdal.GetDriverByName("GTiff");
	        Dataset result = driver.Create(output, warp.getRasterXSize(),warp.getRasterYSize(),warp.getRasterCount());
	        result.SetProjection(warp.GetProjection());
	        result.SetGeoTransform(transformation);
	        
	        int res=gdal.ReprojectImage(hDataset, warp);
	     
	       
	        
	        		
  
       		
       		
	        
	    }
	    
	    
	    

	    public static void main(String[] args) {
	    	String f1="/home/argenpo/Desktop/script_ice/masie_ice_r00_v01_2016037_1km/masie_ice_r00_v01_2016039_1km.tif";//args[0];
	    	String f2="/home/argenpo/Desktop/script_ice/masie_ice_r00_v01_2016037_1km/masie_ice_r00_v01_2016039_1km_out_java.tif";//args[1];
	    	gdalWarpReprojecting(f1,f2);
	    }
}
