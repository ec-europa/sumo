/*
 *
 */
package org.geoimage.viewer.util.files;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.geoimage.viewer.core.SumoPlatform;

public class IceHttpClient extends FileHttpClient{
	public final static String TOKEN_YEAR="yyyy";
	public final static String TOKEN_DATE="yyyyDDD"; //year+dayofyear example: 2016046

	public final String noaaBaseUrl="http://www.natice.noaa.gov/pub/daily/arctic/"+ TOKEN_YEAR +"/ice_edge/";
	public final String noaaFileName="nic_autoc"+TOKEN_DATE+"n_pl_a.zip";

	public final String masieBaseUrl="ftp://sidads.colorado.edu/DATASETS/NOAA/G02186/shapefiles/1km/"+TOKEN_YEAR+"/";
	public final String masieFileName="masie_ice_r00_v01_"+TOKEN_DATE+"_1km.zip";



	public IceHttpClient(){}


	/**
	 *
	 * @param date 				date of the shape file to download
	 * @param outputFolder 		ouput folder destination
	 * @return
	 */
	public File downloadFromNoaa(Date date,String outputFolder){
		SimpleDateFormat df=new SimpleDateFormat();
		df.applyPattern(TOKEN_DATE);
		String fileName=noaaFileName.replace(TOKEN_DATE,df.format(date));

		df.applyPattern(TOKEN_YEAR);
		String strurl=noaaBaseUrl.replace(TOKEN_YEAR,df.format(date))+fileName;
		return download(strurl,outputFolder+File.separator+fileName);
	}


	/**
	 *
	 * @param date 				date of the shape file to download
	 * @param outputFolder 		ouput folder destination
	 * @return
	 */
	public File downloadFromMasie(Date date,String outputFolder){
		SimpleDateFormat df=new SimpleDateFormat();
		df.applyPattern(TOKEN_DATE);
		String fileName=masieFileName.replace(TOKEN_DATE,df.format(date));

		df.applyPattern(TOKEN_YEAR);
		String strurl=masieBaseUrl.replace(TOKEN_YEAR,df.format(date))+fileName;
		return download(strurl,outputFolder+File.separator+fileName);
	}




	public static void main(String[]args){
		String url="http://www.natice.noaa.gov/pub/daily/arctic/2016//ice_edge/nic_autoc2016048n_pl_a.zip";
				//"http://www.natice.noaa.gov/pub/daily/arctic/2015/ice_edge/nic_autoc2015333n_pl_a.zip";
		String cachePath=SumoPlatform.getApplication().getCachePath();
		File cache=new File(cachePath+File.separator+System.currentTimeMillis());
		if(!cache.exists())
			cache.mkdirs();

		//String tmp_path="H:\\tmp\\tmp.zip";
		//new IceHttpClient().download(url,tmp_path);

		SimpleDateFormat df=new SimpleDateFormat("yyyyDDD");
		try {
			new IceHttpClient().downloadFromNoaa(df.parse("2016048"), cache.getAbsolutePath());
		} catch (ParseException e) {
			e.printStackTrace();
		}


		System.exit(0);
	}

}