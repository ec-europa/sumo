package org.geoimage.viewer.util.files;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.geoimage.viewer.core.SumoPlatform;

public class NoaaClient {
	public static final String TOKEN_YEAR="%year%";
	public static final String TOKEN_DATE="%date%"; //year+dayofyear example: 2016046

	public static String baseUrl="http://www.natice.noaa.gov/pub/daily/arctic/"+ TOKEN_YEAR +"/ice_edge/";
	public static String fName="nic_autoc"+TOKEN_DATE+"n_pl_a.zip";
	
	private static final int BUFFER_SIZE = 4096;
	
	public NoaaClient(){}

	public static File download(Date date,String outputFile){
		GregorianCalendar gc=new GregorianCalendar();
		gc.setTimeInMillis(date.getTime());

		int year=gc.get(Calendar.YEAR);
		int numberofDaysPassed=gc.get(GregorianCalendar.DAY_OF_YEAR);
		String dateStr=""+year+numberofDaysPassed;
		if(numberofDaysPassed<=99){
			dateStr=""+year+"0"+numberofDaysPassed;
		}
		String fileName=fName.replace(TOKEN_DATE,dateStr);
		String strurl=baseUrl.replace(TOKEN_YEAR,""+year)+fileName;
		return download(strurl,outputFile);
	}

	public static File download(String strurl,String outputFile) {
		File f=null;
		try {
			URL url = new URL(strurl);

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			//conn.setRequestMethod("GET");
			//conn.setRequestProperty("Accept", "application/json");
			
			int responseCode = conn.getResponseCode();

			// always check HTTP response code first
			if (responseCode == HttpURLConnection.HTTP_OK) {

				if (conn.getResponseCode() != 200) {
					throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
				}
	
				BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));

				f=new File(outputFile);
					
				BufferedWriter  out = new BufferedWriter(new FileWriter(f)) ;
				String output;
				System.out.println("Downloading from Server .... \n");
				while ((output = br.readLine()) != null) {
					out.write(output);
				}
				out.flush();
				conn.disconnect();
				out.close();
			}	
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();

		}
		return f;
	}
	
	public static void main(String[]args){
		String url="http://www.natice.noaa.gov/pub/daily/arctic/2016//ice_edge/nic_autoc2016048n_pl_a.zip";
				//"http://www.natice.noaa.gov/pub/daily/arctic/2015/ice_edge/nic_autoc2015333n_pl_a.zip";
		String cachePath=SumoPlatform.getApplication().getCachePath();
		File cache=new File(cachePath+File.separator+System.currentTimeMillis());
		if(!cache.exists())
			cache.mkdirs();
		//String tmpPath=cache.getAbsolutePath()+File.separator+tmp.zip;
		String tmp_path="H:\\tmp\\tmp.zip";
		NoaaClient.download(url,tmp_path);
		System.exit(0);
	}

}