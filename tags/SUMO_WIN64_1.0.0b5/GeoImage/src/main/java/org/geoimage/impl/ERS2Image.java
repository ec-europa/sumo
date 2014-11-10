/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoimage.impl;

import java.io.File;
import java.io.RandomAccessFile;

/**
 * Simple warper of Envisat Image (same format) to access specifically ERS2
 * @author leforth
 */
public class ERS2Image extends EnvisatImage {

    @Override
    public boolean initialise(File f) {
        boolean result = super.initialise(f);
        
        // change name of satellite and instrument
        setMetadata(SATELLITE, "ERS2");
        setMetadata(SENSOR, "ERS2");
        setMetadata(TYPE, "ERS2");
        
        return result;
    }

    @Override
    protected void extractGcps(RandomAccessFile fss) {
        
        int pointer = 3679;
        float sourceP[][] = new float[8][2];
        float destP[][] = new float[8][2];
        float sizeImage[] = new float[]{xSize, ySize};
        
        sourceP[0] = new float[]{1, 1};
        sourceP[3] = new float[]{1, sizeImage[1]};
        sourceP[4] = new float[]{1, sizeImage[1] / 2};
        sourceP[2] = new float[]{sizeImage[0], sizeImage[1]};
        sourceP[5] = new float[]{sizeImage[0] / 2, sizeImage[1]};
        sourceP[6] = new float[]{sizeImage[0], sizeImage[1] / 2};
        sourceP[1] = new float[]{sizeImage[0], 1};
        sourceP[7] = new float[]{sizeImage[0] / 2, 1};

        try {
            for (int i = 0; i < 4; i++) {
                float lat = getFloatValue(fss, pointer, 16);
                pointer += 16;
                float lon = getFloatValue(fss, pointer, 16);
                if (lon > 180)
                        lon -= 360;
                pointer += 16;
                destP[i] = new float[]{lon, lat};
            }

            destP[4] = new float[]{(float) (destP[3][0] + destP[0][0]) / 2, (float) (destP[3][1] + destP[0][1]) / 2};
            destP[6] = new float[]{((float) destP[2][0] + (float) destP[1][0]) / 2, (float) (destP[2][1] + destP[1][1]) / 2};
            destP[5] = new float[]{((float) destP[2][0] + (float) destP[3][0]) / 2, (float) (destP[2][1] + destP[3][1]) / 2};
            destP[7] = new float[]{(float) (destP[1][0] + destP[0][0]) / 2, (float) (destP[1][1] + destP[0][1]) / 2};

            gcps = new java.util.Vector<Gcp>();
            for (int i = 0; i < 8; i++) {
                // create gcps from lat lon values
                Gcp gcp = new Gcp();
                gcp.setXpix(sourceP[i][0]);
                gcp.setOriginalXpix(new Double(sourceP[i][0]));
                gcp.setXgeo(destP[i][0]);
                gcp.setYpix(sourceP[i][1]);
                gcp.setYgeo(destP[i][1]);
                gcps.add(gcp);
            }

            for (int i = 0; i < 8; i++) {
                    System.out.println(sourceP[i].toString() + "    "
                                    + destP[i].toString());
            }

        } catch (Exception e) {

        }
    }

}
