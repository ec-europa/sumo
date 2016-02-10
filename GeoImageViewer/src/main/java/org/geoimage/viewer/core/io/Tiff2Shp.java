package org.geoimage.viewer.core.io;
import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;
import org.gdal.ogr.*;
import org.gdal.osr.SpatialReference;


public class Tiff2Shp {



	    public static void main(String[] args) {
	        // TODO Auto-generated method stub
	        String[] temp = {"C:\\Users\\Michael\\Desktop\\result\\abia.tif"};
	        gdalPolygonize(temp);
	    }

	    public static void gdalPolygonize(String[] args)
	    {
	        gdal.AllRegister();
	        ogr.RegisterAll();
	        args = gdal.GeneralCmdLineProcessor(args);

	        //Open source file
	        Dataset hDataset = gdal.Open(args[0], gdalconstConstants.GA_ReadOnly);        
	        Band rasterBand = hDataset.GetRasterBand(1);
	        Band maskBand = rasterBand.GetMaskBand();

	        Driver driver = ogr.GetDriverByName("ESRI Shapefile");
	        DataSource dataSource =  driver.CreateDataSource("C:\\Users\\Michael\\Desktop\\result\\bio11.shp");

	        SpatialReference srs = null;
	        if(!hDataset.GetProjectionRef().isEmpty())
	            srs = new SpatialReference(hDataset.GetProjectionRef());

	        Layer outputLayer = dataSource.CreateLayer("destination", srs);
	        FieldDefn field_def = new FieldDefn("DN",ogr.OFTInteger);
	        outputLayer.CreateField(field_def);
	        gdal.Polygonize(rasterBand, maskBand, outputLayer, 0);
	    }
}
