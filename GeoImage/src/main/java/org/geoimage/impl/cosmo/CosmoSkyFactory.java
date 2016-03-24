/*
 * 
 */
package org.geoimage.impl.cosmo;

import ncsa.hdf.object.Attribute;
import ncsa.hdf.object.h5.H5File;

public class CosmoSkyFactory {

	
	public static AbstractCosmoSkymedImage instanceCosmoSkymed(H5File h5file,String pathImg,String group) throws Exception{
		Attribute data= (Attribute) h5file.get("/").getMetadata().get(2);
		String[] acquisionMode=(String[]) data.getValue();

			
		if(acquisionMode[0].equalsIgnoreCase("WIDEREGION")||acquisionMode[0].equalsIgnoreCase("HUGEREGION")){
			//scansar images
			return new CosmoSkymedScanSar(h5file, pathImg, group);
		}else{
			//streetmap images
			return new CosmoSkymedStreetMap(h5file, pathImg, group);
		}
	}
}
