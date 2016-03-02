package org.geoimage.viewer.util.files;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class FileHttpClient {
	private static final int BUFFER_SIZE = 1024;


	public FileHttpClient(){
	}

	public File download(String strurl,String outputFile) {
		File f=null;
		try {
			URL url = new URL(strurl);

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

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

				int count;
				byte buffer[] = new byte[BUFFER_SIZE];

				while ((count = br.read(buffer, 0, buffer.length)) != -1)
				  out.write(buffer, 0, count);

				System.out.println("Download complete \n File:"+f.getAbsolutePath());

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
}
