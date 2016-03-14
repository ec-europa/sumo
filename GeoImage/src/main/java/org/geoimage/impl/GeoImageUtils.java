/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoimage.impl;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import org.geoimage.def.GeoImageReader;
import org.geoimage.utils.Constant;

/**
 * A bunch of useful tools for
 * @author thoorfr
 */
public class GeoImageUtils {
    /**
     * create a quicklook image from the GeoImageReader
     * @param gir the source image
     * @param width the output width
     * @param height the output height
     * @return a BufferedImage representing the quicklook
     */
    public static BufferedImage createOverview(GeoImageReader gir, int width, int height,int band){
        int nPass=gir.getHeight()/Constant.GEOIMAGE_TILE_SIZE;
        int xstep=gir.getWidth()/width;
        int ystep=gir.getHeight()/height;
        width=gir.getWidth()/xstep;
        height= gir.getHeight()/ystep;
        BufferedImage out=new BufferedImage(width,height, BufferedImage.TYPE_USHORT_GRAY);
        WritableRaster raster=out.getRaster();
        for(int i=0;i<nPass;i++){
            int[] t=gir.readTile(0, i*Constant.GEOIMAGE_TILE_SIZE, gir.getWidth(), Constant.GEOIMAGE_TILE_SIZE,band);
            for(int x=0;x<width;x++){
                for(int y=i*height/nPass;y<(i+1)*height/nPass;y++){
                    raster.setSample(x, y, 0, t[x*xstep*(y-i*height/nPass)*ystep]);
                }
            }
        }
        return out;
    }
}
