package org.geoimage.viewer.util.files;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

	private static final int BUFFER_SIZE = 1024;

	public NoaaClient(){}

	public static File download(Date date,String outputFolder){
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
		return download(strurl,outputFolder+File.separator+fileName);
	}

	public static File download(String strurl,String outputFile) {
		File f=null;
		try {
			URL url = new URL(strurl);

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			//conn.setRequestMethod("GET");
			//conn.setRequestProperty("Accept", "application/json");

			int responseCode = conn.getResponseCode();
			Long lenght=conn.getContentLengthLong();
			System.out.println("Content lenght:"+lenght);
			// always check HTTP response code first
			if (responseCode == HttpURLConnection.HTTP_OK) {

				if (conn.getResponseCode() != 200) {
					throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
				}

				BufferedInputStream br = new BufferedInputStream(url.openStream());

				f=new File(outputFile);
				System.out.println("Downloading from Server .... \n");

				FileOutputStream  out = new FileOutputStream(f) ;
				//String output;
				/*while ((output = br.readLine()) != null) {
					out.write(output);
				}*/
				int count;
				byte buffer[] = new byte[BUFFER_SIZE];

				while ((count = br.read(buffer, 0, buffer.length)) != -1)
				  out.write(buffer, 0, count);

				System.out.println("Download complete \n");

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