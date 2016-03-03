package org.geoimage.viewer.util.files;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.LoggerFactory;

public class ArchiveUtil {
	private static final int BUFFER = 2048;
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(ArchiveUtil.class);
	private static String[] exts=new String[]{"zip","gz","rar"};
	
	public static boolean isArchive(File f){
		String ext=FilenameUtils.getExtension(f.getName());
		return (Arrays.asList(exts).contains(ext.toLowerCase()));
	}
	
	
	/**
	 * 
	 * @param zipPath
	 * @return
	 * @throws Exception 
	 */
	public static boolean unZip(String zipPath) throws Exception {
		try {
			BufferedOutputStream dest = null;
			FileInputStream fis = new FileInputStream(zipPath);
			ZipInputStream zis = new ZipInputStream(fis);//new BufferedInputStream(fis));
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				System.out.println("Extracting: " + entry);
				int count;
				byte data[] = new byte[BUFFER];
				
				File newFile = new File(new File(zipPath).getParent(), entry.getName());
	            if (entry.isDirectory()) {
	                newFile.mkdirs();
	                entry = zis.getNextEntry();
	                continue;
	            }

	            /*if (newFile.exists()) {
	                newFile.delete();
	            }*/

	            FileOutputStream fos = new FileOutputStream(newFile);
				
				dest = new BufferedOutputStream(fos, BUFFER);
				while ((count = zis.read(data, 0, BUFFER)) != -1) {
					dest.write(data, 0, count);
				}
				dest.flush();
				dest.close();
			}
			zis.close();
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			throw e;
		}
		return true;
	}
	
	/**
	 * 
	 * @param pathGz
	 * @return
	 * @throws Exception 
	 */
	public boolean unZipGz(String pathGz) throws Exception {
		try {
			BufferedOutputStream dest = null;
			BufferedInputStream is = null;
			ZipEntry entry;
			ZipFile zipfile = new ZipFile(pathGz);
			Enumeration e = zipfile.entries();
			while (e.hasMoreElements()) {
				entry = (ZipEntry) e.nextElement();
				System.out.println("Extracting: " + entry);
				is = new BufferedInputStream(zipfile.getInputStream(entry));
				int count;
				byte data[] = new byte[BUFFER];
				FileOutputStream fos = new FileOutputStream(entry.getName());
				dest = new BufferedOutputStream(fos, BUFFER);
				while ((count = is.read(data, 0, BUFFER)) != -1) {
					dest.write(data, 0, count);
				}
				dest.flush();
				dest.close();
				is.close();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			throw e;
		}
		return true;
	}

}
