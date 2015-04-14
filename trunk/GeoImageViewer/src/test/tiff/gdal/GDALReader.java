package tiff.gdal;

import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;

public class GDALReader {
	
	public GDALReader() {
		
	}
	
	public void initGDAL(){
		
		//System.load("C:/chilkatJava/chilkat.dll");
	}
	
	public void readImage(){
		
		gdal.SetConfigOption("GDAL_DRIVER_PATH","C:/Workspaces_MyEclipse Professional 2014/Sumo/trunk/libs/gdal_lib_win64");
		gdal.SetConfigOption("GDAL_DATA", "C:/Workspaces_MyEclipse Professional 2014/Sumo/trunk/libs/gdal_lib_win64/gdal/gdal-data");
		Dataset data=gdal.Open("C:\\tmp\\sumo_images\\S1A_IW_GRDH_1SDV_20150326T174051_20150326T174120_005206_00692A_F72A.SAFE\\measurement\\s1a-iw-grd-vv-20150326t174051-20150326t174120-005206-00692a-001.tiff",
				gdalconstConstants.GA_ReadOnly);
		
		String projection=data.GetProjectionRef();
		System.out.println(projection);
		
		
		
		
		
	}
	
	
	
	public static void main(String args[]){
		GDALReader reader=new GDALReader();
		reader.readImage();
	}
	
	
}
