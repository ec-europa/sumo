/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.impl;

import java.io.File;
import java.util.Hashtable;

import org.geoimage.def.SarImageReader;
import org.geoimage.impl.envi.EnvisatImage;
import org.geoimage.impl.radarsat.Radarsat1Image;
import org.geoimage.impl.radarsat.Radarsat2Image;
import org.geoimage.impl.s1.Sentinel1GRD;
import org.geoimage.impl.s1.Sentinel1SLC;

/**
 * Convenience class to calculate the ENL (Equivalent Number of Looks)
 * from a SarImageReader when the meta data does not contain it.
 * This is based on the work of Harm Greidanus
 * @author thoorfr
 */
public class ENL  {

    private static Hashtable<String, String> envisatENLMap = new Hashtable<String, String>();
    private static float[][] radarsatENLArray = {
        {4.5f, 4}, // mode s(tandard)
        {1, 1}, // mode f(ine)
        {4.5f, 4}, // mode n(arrow)
        {4.7f, 3.8f} // product scn
    };

    static {
        envisatENLMap.put("IS1", "1.8");
        envisatENLMap.put("IS2", "2.1");
        envisatENLMap.put("IS3", "2.6");
        envisatENLMap.put("IS4", "3.1");
        envisatENLMap.put("IS5", "3.6");
        envisatENLMap.put("IS6", "4.1");
        envisatENLMap.put("IS7", "4.6");
        envisatENLMap.put("WS", "1.8");
    }

    public static String whatIsThis() {
        return "ENL = the Equivalent Number of Looks";
    }

    public static float getFromGeoImageReader(SarImageReader gir) {
        float enl = 1;
        if (gir instanceof Radarsat1Image) {
            int ENLColumn = 0, ENLLine = 0;
            String processor = gir.getProcessor();
            String mode = gir.getMode();
            if (processor.charAt(0) == 'K' || processor.charAt(0) == 'S') {
                ENLColumn = 0;
            } else {
                ENLColumn = 1;
            }
            if (mode.equalsIgnoreCase("s")) {
                ENLLine = 0;
            }
            if (mode.equalsIgnoreCase("f")) {
                ENLLine = 1;
            }
            if (mode.equalsIgnoreCase("w")) {
                ENLLine = 2;
            }
            if (((String)gir.getProduct()).equalsIgnoreCase("scn")) {
                ENLLine = 3;
            }
            enl = radarsatENLArray[ENLLine][ENLColumn];
        }else if(gir instanceof Sentinel1SLC){
        	enl=1;
        }else if(gir instanceof Sentinel1GRD){
        	String name=((Sentinel1GRD) gir).getSafeFilePath();
        	String folder=new File(name).getParentFile().getAbsolutePath();
        	if(folder.contains("GRDF")){
        		enl=3.5F;
        	}else if(folder.contains("_GRDH")){
        		if(folder.matches(".*S(.){1}_GRDH.*")){
        			enl=27F;
        		}else if(folder.contains("IW")){
        			enl=4.4F;
        		}else if(folder.contains("EW")){
        			enl=2.7F;
        		}
        	}else if(folder.contains("GRDM")){
        		if(folder.matches(".*S(.){1}_GRDM.*")){
        			enl=50F;
        		}else if(folder.contains("IW")){
        			enl=50.0F;
        		}else if(folder.contains("EW")){
        			enl=9.6F;
        		}
        	}
        }else if (gir instanceof EnvisatImage) {
            String swath = gir.getSwath();
            enl = Float.parseFloat(envisatENLMap.get(swath));
        } else if (gir instanceof Radarsat2Image) {
            String temp = gir.getENL();
            enl = Float.parseFloat(temp);
        } else if (gir instanceof GeotiffImage) {
            enl = 1;
        }
        return enl;
    }
}
