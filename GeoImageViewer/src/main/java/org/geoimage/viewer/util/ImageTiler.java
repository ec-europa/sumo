package org.geoimage.viewer.util;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.geoimage.def.GeoImageReader;
import org.geoimage.viewer.core.layers.image.Cache;
import org.geoimage.viewer.core.layers.image.CacheManager;
import org.jrc.sumo.util.Constant;
import org.slf4j.LoggerFactory;

/**
 *
 * @author thoorfr
 */
public class ImageTiler {
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(ImageTiler.class);

    private GeoImageReader gir;
    
    private int xpadding;
    private int ypadding;
    private int levels;

    public ImageTiler(GeoImageReader gir) {
        this.gir = gir;
        levels = (int) (Math.log(Math.max(gir.getWidth() / Constant.TILE_SIZE_IMG_LAYER, gir.getHeight() / Constant.TILE_SIZE_IMG_LAYER)) / Math.log(2)) + 1;
        xpadding = ((1 << levels) * Constant.OVERVIEW_SIZE - gir.getWidth()) / 2;
        ypadding = ((1 << levels) * Constant.OVERVIEW_SIZE - gir.getHeight()) / 2;
    }

    public void generateTiles(int band) {
        logger.info((1 << levels) + ";" + xpadding + "--" + ypadding);
        
        Cache cache=CacheManager.getCacheInstance(gir.getDisplayName(band));
        int powerI = 1;
        int numI = 1 << levels;
        for (int i = levels - 1; i > -1; i--) {
            for (int h = 0; h < powerI; h++) {
                for (int w = 0; w < powerI; w++) {
                    String file = cache.getPath() + "/" + i + "/" + band + "/" + w + "_" + h + ".png";
                    if (!CacheManager.getCacheInstance(gir.getDisplayName(band)).contains(file)) {
                        try {
                            int[] t = gir.readAndDecimateTile(w * numI * Constant.TILE_SIZE_IMG_LAYER - xpadding, h * numI * Constant.TILE_SIZE_IMG_LAYER - ypadding, 
                            		numI * Constant.TILE_SIZE_IMG_LAYER, numI * Constant.TILE_SIZE_IMG_LAYER, Constant.TILE_SIZE_IMG_LAYER, Constant.TILE_SIZE_IMG_LAYER,gir.getWidth(),gir.getHeight(), true,band);
                            File f = CacheManager.getCacheInstance(gir.getDisplayName(band)).newFile(cache.getPath() + "/" + i + "/" + band + "/" + w + "_" + h + ".png");
                            ImageIO.write(createImage(t, Constant.TILE_SIZE_IMG_LAYER, Constant.TILE_SIZE_IMG_LAYER, gir), "png", f);

                        } catch (IOException ex) {
                        	logger.error(ex.getMessage(),ex);
                        }
                    }
                }
            }
            numI >>= 1;
            powerI <<= 1;
        }
    }
    public void generateAllTiles() {
    	int nband=gir.getNBand();
    	for(int i=0;i<nband;i++){
    		generateTiles(i);
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
        for (int line = 0; line < gir.getHeight(); line++) {
            readLine(new HashMap<String, int[]>(), line, band);
        }
    }

    private void readLine(Map<String, int[]> tiles, int line, int band) {
        int[] data = gir.readTile(0, line, gir.getWidth(), line + 1,band);
        int L = Integer.numberOfTrailingZeros(line);
        if(L==32) L=levels;
        updateLevels(tiles, line, L, data);
        if (L == 0) {
            int L2 = Integer.numberOfTrailingZeros(line + 1);
            saveLevels(tiles, L2, line, band);
        }
    }

    private void saveLevels(Map<String, int[]> tiles, int L2, int line, int band) {
    	Cache cache=CacheManager.getCacheInstance(gir.getDisplayName(band));
        for (int x = 0; x < L2 + 1; x++) {
            for (int l = 0; l < L2 + 1; l++) {
                int posx = (x + xpadding) >> l;
                int posy = (line + ypadding) >> l;
                int[] temp = tiles.remove(l + "/" + posx + "/" + posy);
                BufferedImage bi = createImage(temp, Constant.OVERVIEW_SIZE, Constant.OVERVIEW_SIZE, gir);
                File f = new File(cache.getPath() + "/" + l + "/" + band + "/" + posx + "_" + posy + ".png");
                try {
                    ImageIO.write(bi, "png", f);
                } catch (IOException ex) {
                	logger.error(ex.getMessage(),ex);
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
