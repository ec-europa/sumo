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
		String s="S1A_IW_GRDH_1SDH_20141030T061035_20141030T061104_003055_0037E5_D3D1.SAFE";
		s.matches("S1");
		
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
