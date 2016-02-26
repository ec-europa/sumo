package org.geoimage.viewer.util.files;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

	public NoaaClient(){}



	public static void download(Date date) {

		try {
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

			URL url = new URL(strurl);

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");

			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
			String tmpPath=SumoPlatform.getApplication().getCachePath()+File.separator+System.currentTimeMillis()+File.separator+fileName;
			File f=new File(tmpPath);




			FileWriter writer=new FileWriter(f);
			BufferedWriter out = new BufferedWriter(writer);
			String output;
			System.out.println("Output from Server .... \n");
			while ((output = br.readLine()) != null) {
				out.write(output);
			}

			conn.disconnect();
			out.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();

		}

	}

}