/*
 * 
 */
package org.geoimage.viewer.util.files;

import java.io.File;

public class SarImageFileFilter  implements java.io.FileFilter{
		private final String[] patternRec={".*\\.h5",".*manifest\\.safe.*",".*product\\.xml.*",".*\\.N1",".*TS.*xml.*",".*TDX.*xml.*"};
		
		@Override
		public boolean accept(File pathname) {
			boolean ok=false;
			for(int i=0;i<patternRec.length && ok==false;i++){
				String fileName=pathname.getName();
				if(fileName.matches(patternRec[i])){
					ok= true;
				}	
			}
			return ok;
		}
		
		

		
		
		
}
