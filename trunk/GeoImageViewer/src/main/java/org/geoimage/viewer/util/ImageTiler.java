package org.geoimage.viewer.util;

import org.geoimage.def.GeoImageReader;
import org.geoimage.factory.GeoImageReaderFactory;
import org.geoimage.viewer.core.layers.image.CacheManager;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

/**
 *
 * @author thoorfr
 */
public class ImageTiler {
    private GeoImageReader gir;
    
    private String cachePath;
    private int xpadding;
    private int ypadding;
    private int levels;

    public ImageTiler(GeoImageReader gir) {
        this.gir = gir;
        this.cachePath = gir.getFilesList()[0];
        levels = (int) (Math.log(Math.max(gir.getWidth() / Constant.TILE_SIZE, gir.getHeight() / Constant.TILE_SIZE)) / Math.log(2)) + 1;
        xpadding = ((1 << levels) * Constant.OVERVIEW_SIZE - gir.getWidth()) / 2;
        ypadding = ((1 << levels) * Constant.OVERVIEW_SIZE - gir.getHeight()) / 2;
    }

    public void generateTiles(int band) {
        gir.setBand(band);
        System.out.println((1 << levels) + ";" + xpadding + "--" + ypadding);
        int powerI = 1;
        int numI = 1 << levels;
        for (int i = levels - 1; i > -1; i--) {
            for (int h = 0; h < powerI; h++) {
                for (int w = 0; w < powerI; w++) {
                    String file = cachePath + "/" + i + "/" + band + "/" + w + "_" + h + ".png";
                    if (!CacheManager.getCacheInstance(gir.getName()).contains(file)) {
                        try {
                            int[] t = gir.readAndDecimateTile(w * numI * Constant.TILE_SIZE - xpadding, h * numI * Constant.TILE_SIZE - ypadding, numI * Constant.TILE_SIZE, numI * Constant.TILE_SIZE, Constant.TILE_SIZE, Constant.TILE_SIZE,gir.getWidth(),gir.getHeight(), true);
                            File f = CacheManager.getCacheInstance(gir.getName()).newFile(cachePath + "/" + i + "/" + band + "/" + w + "_" + h + ".png");
                            ImageIO.write(createImage(t, Constant.TILE_SIZE, Constant.TILE_SIZE, gir), "png", f);

                        } catch (IOException ex) {
                            Logger.getLogger(ImageTiler.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
            numI >>= 1;
            powerI <<= 1;
        }
    }

    public static BufferedImage createImage(int[]tile, int width, int height, GeoImageReader gimage) {

        BufferedImage bufferedImage = new BufferedImage(width, height, gimage.getType(true));


        WritableRaster raster = bufferedImage.getRaster();

        // Put the pixels on the raster.
        if(tile!=null){
            raster.setPixels(0, 0, width, height, tile);
        }
        return bufferedImage;
    }

    private int[] maxmin(int[] data) {
        int max = Integer.MAX_VALUE;
        int min = Integer.MIN_VALUE;
        for (int i = 0; i < data.length; i++) {
            if (max < data[i]) {
                max = data[i];
            }
            if (min > data[i]) {
                min = data[i];
            }
        }
        return new int[]{max, min};
    }

    public void generateSmartTiles(int band) {
        gir.setBand(band);
        for (int line = 0; line < gir.getHeight(); line++) {
            readLine(new HashMap<String, int[]>(), line, band);
        }
    }

    private void readLine(Map<String, int[]> tiles, int line, int band) {
        int[] data = gir.readTile(0, line, gir.getWidth(), line + 1);
        int L = Integer.numberOfTrailingZeros(line);
        if(L==32) L=levels;
        updateLevels(tiles, line, L, data);
        if (L == 0) {
            int L2 = Integer.numberOfTrailingZeros(line + 1);
            saveLevels(tiles, L2, line, band);
        }
    }

    private void saveLevels(Map<String, int[]> tiles, int L2, int line, int band) {
        for (int x = 0; x < L2 + 1; x++) {
            for (int l = 0; l < L2 + 1; l++) {
                int posx = (x + xpadding) >> l;
                int posy = (line + ypadding) >> l;
                int[] temp = tiles.remove(l + "/" + posx + "/" + posy);
                BufferedImage bi = createImage(temp, Constant.OVERVIEW_SIZE, Constant.OVERVIEW_SIZE, gir);
                File f = new File(cachePath + "/" + l + "/" + band + "/" + posx + "_" + posy + ".png");
                try {
                    ImageIO.write(bi, "png", f);
                } catch (IOException ex) {
                    Logger.getLogger(ImageTiler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private void updateLevels(Map<String, int[]> tiles, int line, int maxL, int[] data) {
        for (int i = 0; i < gir.getWidth(); i++) {
            int X = Integer.numberOfTrailingZeros(i);
            if(X==32) X=levels;
            for (int x = 0; x < X + 1; x++) {
                    int posx = (i + xpadding) >> x;
                    int posy = (line + ypadding) >> x;
                    int posx2 = i >> x;
                    int posy2 = line >> x;
                    int[] temp = tiles.get(x + "/" + posx + "/" + posy);
                    if (temp == null) {
                        temp = new int[Constant.OVERVIEW_SIZE * Constant.OVERVIEW_SIZE];
                        tiles.put(x + "/" + posx + "/" + posy, temp);
                    }
                    temp[posy2 * Constant.OVERVIEW_SIZE + posx2] = data[i];

            }
        }
    }
    
    /*
    public static void main(String[] args) {
        GeoImageReader image = GeoImageReaderFactory.createReaderForName(args[0]).get(0);
        //GeoImageReader image = GeoImageReaderFactory.create("/home/fish/Radar-Images/Caribbean/ENVISAT/IMAGE_HH_SRA_strip_008.tif");

        CacheManager cman = new CacheManager("/home/thoorfr/cache/");
        ImageTiler it = new ImageTiler(image, cman);
        it.generateSmartTiles(0);

    }*/
}
