/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.impl;

import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.geoimage.def.GeoImageReader;
import org.geoimage.exception.GeoTransformException;
import org.geoimage.utils.Constant;
import org.geoimage.utils.IProgress;
import org.jaitools.tiledimage.DiskMemImage;

import com.sun.media.imageio.plugins.tiff.GeoTIFFTagSet;
import com.sun.media.jai.codec.TIFFEncodeParam;
import com.sun.media.jai.codec.TIFFField;
import com.sun.media.jai.codecimpl.TIFFImageEncoder;

/**
 *
 * @author thoorfr
 */
public class GeotiffWriter {

    public static void create(final GeoImageReader gir, String filepath,int band) throws FileNotFoundException, IOException, GeoTransformException {
      //  GeoImageReader mygir = gir.clone();
        ColorModel cm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY), false, false, Transparency.OPAQUE, DataBuffer.TYPE_USHORT);
        SampleModel sm = cm.createCompatibleSampleModel(Constant.TILE_SIZE, Constant.TILE_SIZE);
        DiskMemImage ti = new DiskMemImage(gir.getWidth(), gir.getHeight(), sm);
        ti.setUseCommonCache(true);
        System.out.println(ti.getNumXTiles() + "-" + ti.getNumYTiles());
        for (int j = 0; j < ti.getNumYTiles(); j++) {
            for (int i = 0; i < ti.getNumXTiles(); i++) {
                System.out.println(i + "-" + j);
                WritableRaster r = ti.getWritableTile(i, j);
                Rectangle bounds = r.getBounds();
                r.setPixels(bounds.x, bounds.y, bounds.width, bounds.height, gir.readTile(bounds.x, bounds.y, bounds.width, bounds.height,band));
                ti.releaseWritableTile(i, j);

            }
        }
        TIFFEncodeParam encodep = new TIFFEncodeParam();
        TIFFField[] geotiffmeta = new TIFFField[1];
        List<Gcp> gcps = gir.getGcps();
        double[] data = new double[gcps.size() * 6];
        int i = 0;
        for (Gcp gcp : gcps) {
            data[6 * i] = gcp.getXpix();
            data[6 * i + 1] = gcp.getYpix();
            data[6 * i + 2] = 0;
            data[6 * i + 3] = gcp.getXgeo();
            data[6 * i + 4] = gcp.getYgeo();
            data[6 * i + 5] = gcp.getZgeo();
            i++;
        }
        geotiffmeta[0] = new TIFFField(GeoTIFFTagSet.TAG_MODEL_TIE_POINT, TIFFField.TIFF_DOUBLE, data.length, data);
        encodep.setExtraFields(geotiffmeta);
        FileOutputStream fos = new FileOutputStream(filepath);
        TIFFImageEncoder w = new TIFFImageEncoder(fos, encodep);
        w.encode(ti);
        fos.close();
        ti.dispose();
      //  mygir.dispose();
    }

    public static void create(final GeoImageReader gir, int band,String filepath, IProgress progress) throws FileNotFoundException, IOException, GeoTransformException {
        progress.setIndeterminate(true);
        progress.setMessage("Starting export...");
       // GeoImageReader mygir = gir.clone();
        ColorModel cm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY), false, false, Transparency.OPAQUE, DataBuffer.TYPE_USHORT);
        SampleModel sm = cm.createCompatibleSampleModel(Constant.TILE_SIZE, Constant.TILE_SIZE);
        DiskMemImage ti = new DiskMemImage(gir.getWidth(), gir.getHeight(), sm);
        ti.setUseCommonCache(true);
        System.out.println(ti.getNumXTiles() + "-" + ti.getNumYTiles());
        progress.setIndeterminate(false);
        progress.setMaximum(ti.getNumXTiles() * ti.getNumYTiles());
        progress.setCurrent(0);
        progress.setMessage("Writing data to the cache...");
        int cur = 0;
        for (int j = 0; j < ti.getNumYTiles(); j++) {
            for (int i = 0; i < ti.getNumXTiles(); i++) {
                WritableRaster r = ti.getWritableTile(i, j);
                Rectangle bounds = r.getBounds();
                r.setPixels(bounds.x, bounds.y, bounds.width, bounds.height, gir.readTile(bounds.x, bounds.y, bounds.width, bounds.height,band));
                ti.releaseWritableTile(i, j);
                progress.setCurrent(cur++);
            }
        }
        progress.setMessage("Writing metadata...");
        TIFFEncodeParam encodep = new TIFFEncodeParam();
        TIFFField[] geotiffmeta = new TIFFField[1];
        List<Gcp> gcps = gir.getGcps();
        double[] data = new double[gcps.size() * 6];
        int i = 0;
        for (Gcp gcp : gcps) {
            data[6 * i] = gcp.getXpix();
            data[6 * i + 1] = gcp.getYpix();
            data[6 * i + 2] = 0;
            data[6 * i + 3] = gcp.getXgeo();
            data[6 * i + 4] = gcp.getYgeo();
            data[6 * i + 5] = gcp.getZgeo();
            i++;
        }
        geotiffmeta[0] = new TIFFField(GeoTIFFTagSet.TAG_MODEL_TIE_POINT, TIFFField.TIFF_DOUBLE, data.length, data);
        encodep.setExtraFields(geotiffmeta);
        FileOutputStream fos = new FileOutputStream(filepath);
        progress.setIndeterminate(true);
        progress.setMessage("Wrtiting to the file system, please wait...");
        TIFFImageEncoder w = new TIFFImageEncoder(fos, encodep);
        w.encode(ti);
        fos.close();
        ti.dispose();
     //   mygir.dispose();
        progress.setMessage("");
        progress.setDone(true);
    }

    public static void main(String[] args) throws IOException {
      /*  GeoImageReader test = GeoImageReaderFactory.create("/media/fish/Radar-Images/AIS4FISH/TheNetherlands/Rsat-1/Standard/2005-06-10-std5-desc/20050610055417.DAT");
        File output = new File("/home/thoorfr/Desktop/test.tif");
        output.createNewFile();
        create(test, output.getAbsolutePath());*/
    }
}
