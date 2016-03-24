/*
 * 
 */
package org.geoimage.viewer.util.files;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

public class SarFileUtil {

	/**
	 *
	 * @param imagesFolder
	 * @return
	 * @throws Exception
	 */
	public static File findManifest(File imagesFolder) throws Exception{
		//list only folders
		File[] manifest=imagesFolder.listFiles(new SarImageFileFilter());

		if(manifest.length!=1)
			throw new Exception("Problem reading manifest for this image:"+imagesFolder.getPath());

		return manifest[0];

	}

	/**
	 *
	 * @param imagesFolder
	 * @return
	 */
	public static List<File> scanFolderForImages(File imagesFolder,String patternFilterFolder){
		List<File> imgFiles=new ArrayList<File>();
		final String pattFilterFolder=patternFilterFolder.replace("*",".*")+".*";
		//list only folders
		File[] childs=imagesFolder.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				boolean match=true;
				match=pathname.getName().matches(pattFilterFolder);
				return (pathname.isDirectory()&&match);
			}
		});

		if(childs!=null){
			for(File f:childs){
				File[] imgFile=f.listFiles(new SarImageFileFilter());
				if(imgFile.length==1){
					imgFiles.add(imgFile[0]);
				}
			}
		}
		return imgFiles;
	}

	/**
	 *
	 * @param imagesFolder
	 * @return
	 */
	public static List<File> scanFoldersForImages(String[] inputFolders,String patternFilterFolder,boolean recursive){
		List<File> imgFiles=new ArrayList<File>();

		for(int i=0;i<inputFolders.length;i++){
			imgFiles.addAll(scanFolderForImages(new File(inputFolders[i]), patternFilterFolder));
		}

		return imgFiles;
	}

}
