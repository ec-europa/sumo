/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.impl;

import java.awt.Rectangle;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.HashMap;
import java.util.List;

import org.geoimage.def.GeoImageReader;
import org.geoimage.def.GeoTransform;
import org.geoimage.exception.GeoTransformException;
import org.geoimage.factory.GeoImageReaderFactory;
import org.geoimage.utils.Constant;
import org.geoimage.utils.IProgress;
import org.slf4j.LoggerFactory;

/**
 * In order to save RAM, this Object manage the raster you may change or do whatever with it
 * @author thoorfr
 */
public class TiledBufferedImage implements GeoImageReader {
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(TiledBufferedImage.class);

    private int[] preloadedInterval;
    private final int xSize;
    private final int ySize;
    private int[] preloadedData;
    private final Rectangle bounds;
    private final File root;
    private static int tilesize = 512;
    private HashMap<String, File> tiles = new HashMap<String, File>();
    private final int nXTiles;
    private final int nYTiles;
    private GeoImageReader gir;
    private MappedByteBuffer writingTile = null;
    private File writingFile = null;
    private RandomAccessFile writingFis;

    public TiledBufferedImage(File rootDirectory, GeoImageReader gir) {
        this.xSize = gir.getWidth();
        this.ySize = gir.getHeight();
        this.bounds = new Rectangle(0, 0, xSize, ySize);
        this.root = rootDirectory;
        this.root.mkdirs();
        this.nXTiles = xSize / tilesize + 1;
        this.nYTiles = ySize / tilesize + 1;
        this.gir = gir;
        preloadedInterval = new int[]{-1, -1};
        mapExistingTiles();
    }

    public int[] readTile(int x, int y, int width, int height,int band) {
        return readTile(x, y, width, height, new int[width * height],band);
    }

    public int[] readTile(int x, int y, int width, int height, int[] tile,int band) {
        Rectangle rect = new Rectangle(x, y, width, height);
        rect = rect.intersection(bounds);
        if (rect.isEmpty()) {
            return tile;
        }
        if (rect.y != preloadedInterval[0] || rect.y + rect.height != preloadedInterval[1]) {
            preloadLineTile(rect.y, rect.height,band);
        }
        int xinit = rect.x - x;
        int yinit = rect.y - y;
        for (int i = 0; i < rect.height; i++) {
            for (int j = 0; j < rect.width; j++) {
                int temp = i * xSize + j + rect.x;
                tile[(i + yinit) * width + j + xinit] = preloadedData[temp];
            }
        }
        return tile;
    }

    private ByteBuffer writeTileFile(int xx, int yy, int[] data,int band) {
        FileOutputStream fos = null;
        ByteBuffer out = null;
        try {
            File f = new File(root, band + "_" + xx + "_" + yy);
            f.createNewFile();
            fos = new FileOutputStream(f);
            out = ByteBuffer.allocate(4 * data.length);
            IntBuffer ib = out.asIntBuffer();
            ib.put(data);
            fos.getChannel().write(out);
            tiles.put(band + "_" + xx + "_" + yy, f);
        } catch (Exception ex) {
        	logger.error(ex.getMessage(),ex);
        } finally {
            try {
                fos.close();
            } catch (IOException ex) {
            	logger.error(ex.getMessage(),ex);
            }
        }
        out.rewind();
        return out;
    }

    public void writeTile(int x, int y, int width, int height, int[] data,int band) throws IOException {
        for (int j = y; j < y + height; j++) {
            for (int i = x; i < x + width; i++) {
                write(i, j, data[j * width + i],band);
            }
        }
    }

    public void applyScaleFactor(double scale, double offset,int band) {
        File f = null;
        RandomAccessFile fis = null;
        MappedByteBuffer tile = null;
        for (int y = 0; y < gir.getHeight() / tilesize; y++) {
            for (int x = 0; x < gir.getHeight() / tilesize; x++) {
                try {
                    f = tiles.get(band + "_" + x + "_" + y);
                    fis = new RandomAccessFile(writingFile, "rwd");
                    tile = fis.getChannel().map(MapMode.READ_WRITE, 0, writingFile.length()).load();
                    for (int yt = 0; yt < tilesize; yt++) {
                        for (int xt = 0; xt < tilesize; xt++) {
                            tile.mark();
                            int val=(int) (scale * tile.getInt() + offset);
                            tile.flip();
                            tile.putInt(val);
                        }
                    }
                    tile.force();
                    fis.close();
                } catch (Exception ex) {
                	logger.error(ex.getMessage(),ex);                
                }
            }
        }
    }

    public void write(int x, int y, int value,int band) throws IOException {
        int xx = x / tilesize;
        int yy = y / tilesize;
        if (writingFile == null || !writingFile.getAbsolutePath().endsWith(band + "_" + xx + "_" + yy)) {
            if (writingTile != null) {
                //writingTile.force();
                writingFis.close();
            }
            writingFile = tiles.get(band + "_" + xx + "_" + yy);
            if (writingFile == null) {
                writeTileFile(xx, yy, gir.readTile(xx * tilesize, yy * tilesize, tilesize, tilesize, band),band);
                writingFile = tiles.get(band + "_" + xx + "_" + yy);
                if (writingFile == null) {
                    throw new IOException("can't write the tile");
                }
            }
            writingFis = new RandomAccessFile(writingFile, "rwd");
            writingTile = writingFis.getChannel().map(MapMode.READ_WRITE, 0, writingFile.length()).load();
            writingTile.limit((int) writingFile.length());
        }
        int position = 4 * ((x % tilesize) + (y % tilesize) * tilesize);
        //System.out.println(writingTile.limit());
        writingTile.putInt(position, value);
    }

    public void flush() {
        if (writingTile != null) {
            writingTile.force();
        }
    }

    public void preloadLineTile(int y, int height,int band) {
        preloadedInterval = new int[]{y, y + height};
        preloadedData = new int[xSize * height];
        int[] tile = new int[tilesize * tilesize];
        MappedByteBuffer bb;
        try {
            int col = 0;
            for (int yy = y / tilesize; yy < ((y + height) / tilesize) + 1; yy++) {
                if (yy < 0) {
                    continue;
                }
                for (int xx = 0; xx < nXTiles; xx++) {
                    File f = tiles.get(band + "_" + xx + "_" + yy);
                    if (f == null) {
                        int[] data = gir.readTile(xx * tilesize, yy * tilesize, tilesize, tilesize,band);
                        writeTileFile(xx, yy, data,band);
                        f = tiles.get(band + "_" + xx + "_" + yy);
                    }

                    //System.out.println(f.getAbsolutePath());

                    if (col == 0) {
                        FileInputStream fis = new FileInputStream(f);
                        long startpointer = (y % tilesize) * tilesize * 4;
                        bb = fis.getChannel().map(MapMode.READ_ONLY, startpointer, f.length() - startpointer).load();
                        fis.close();
                        bb.rewind();
                        int aa = 0;
                        while (bb.hasRemaining()) {
                            tile[aa] = bb.getInt();
                            aa++;
                        }
                        for (int j = 0; j < tilesize - y % tilesize; j++) {
                            int temp = (col + j) * xSize + xx * tilesize;
                            for (int i = 0; i < tilesize; i++) {
                                try {
                                    preloadedData[temp + i] = tile[j * tilesize + i];
                                } catch (Exception e) {
                                    //System.out.println(e);
                                }
                            }
                        }
                    } else {
                        FileInputStream fis = new FileInputStream(f);
                        long endpointer = ((y + height) % tilesize) * tilesize * 4;
                        bb = fis.getChannel().map(MapMode.READ_ONLY, 0, endpointer).load();
                        fis.close();
                        bb.rewind();
                        int aa = 0;
                        while (bb.hasRemaining()) {
                            tile[aa] = bb.getInt();
                            aa++;
                        }
                        for (int j = 0; j < (y + height) % tilesize; j++) {
                            int temp = (col + j - (y % tilesize)) * xSize + xx * tilesize;
                            for (int i = 0; i < tilesize; i++) {
                                try {
                                    preloadedData[temp + i] = tile[j * tilesize + i];
                                } catch (Exception e) {
                                    //System.out.println(e);
                                }
                            }
                        }
                    }
                }
                col += tilesize;
            }
        } catch (IOException e) {
        	logger.error("cannot preload the line tile: ",e);
        }

    }

    private void mapExistingTiles() {
        for (int yy = 0; yy < nYTiles; yy++) {
            for (int xx = 0; xx < nXTiles; xx++) {
                for (int b = 0; b < getNBand(); b++) {
                    File f = new File(root, b + "_" + xx + "_" + yy);
                    if (f.exists()) {
                        tiles.put(b + "_" + xx + "_" + yy, f);
                    }
                }
            }
        }
    }

    public int[] readAndDecimateTile(int x, int y, int width, int height, int outWidth, int outHeight, boolean filter,int band) {
        if (height < 257) {
            int[] outData = new int[outWidth * outHeight];
            int[] data = readTile(x, y, width, height,band);
            int decX = Math.round(width / (1f * outWidth));
            int decY = Math.round(height / (1f * outHeight));
            if (data != null) {
                int index = 0;
                for (int j = 0; j < outHeight; j++) {
                    int temp = (int) (j * decY) * width;
                    for (int i = 0; i < outWidth; i++) {
                        if (filter) {
                            for (int h = 0; h < decY; h++) {
                                for (int w = 0; w < decX; w++) {
                                    outData[index] += data[temp + h * width + (int) (i * decX + w)];
                                }
                            }
                            if (decX > 1) {
                                outData[index] /= (int) decX;
                            }
                            if (decY > 1) {
                                outData[index] /= (int) decY;
                            }
                        } else {
                            outData[index] = data[temp + (int) (i * decX)];
                        }
                        index++;
                    }
                }
            }
            return outData;
        } else {
            float incy = height / 256f;
            int[] outData = new int[outWidth * outHeight];
            float decY = height / (1f * outHeight);
            int index = 0;
            for (int i = 0; i < Math.ceil(incy); i++) {
                int tileHeight = (int) Math.min(Constant.TILE_SIZE, height - i * Constant.TILE_SIZE);
                if (tileHeight > decY) {
                    int[] temp = readAndDecimateTile(x, y + i * Constant.TILE_SIZE, width, tileHeight, outWidth, Math.round(tileHeight / decY), filter,band);
                    if (temp != null) {
                        for (int j = 0; j < temp.length; j++) {
                            if (index < outData.length) {
                                outData[index++] = temp[j];
                            }
                        }
                    } else {
                        index += outWidth * (int) (Constant.TILE_SIZE / decY);
                    }
                }
            }
            return outData;
        }
    }


    public List<double[]> getFrameLatLon() throws GeoTransformException {
        return gir.getFrameLatLon(xSize,ySize);
    }

    public String getDisplayName() {
        return gir.getDisplayName(0);
    }

    public int getWidth() {
        return gir.getWidth();
    }

    public int getHeight() {
        return gir.getHeight();
    }

    public int getNBand() {
        return gir.getNBand();
    }

    public String getFormat() {
        return gir.getFormat();
    }

    public String getDescription() {
        return "" + this.hashCode();
    }

    public int getNumberOfBytes() {
        return gir.getNumberOfBytes();
    }

    public int getType(boolean oneBand) {
        return gir.getType(oneBand);
    }

    public GeoTransform getGeoTransform() {
        return gir.getGeoTransform();
    }

    public List<Gcp> getGcps() throws GeoTransformException {
        return gir.getGcps();
    }

    public String getAccessRights() {
        return gir.getAccessRights();
    }

    public String[] getFilesList() {
        return gir.getFilesList();
    }

    public boolean initialise() {
        return gir.initialise();
    }


    public int readPixel(int x, int y,int band) {
        return readTile(x, y, 1, 1,band)[0];
    }

    public String getBandName(int band) {
        return gir.getBandName(band);
    }

    public void dispose() {
        gir.dispose();
        gir = null;
        root.delete();
    }

    public int[] readAndDecimateTile(int x, int y, int width, int height, double scalingFactor, boolean filter, IProgress progressbar,int band) {
        return gir.readAndDecimateTile(x, y, width, height, scalingFactor, filter, progressbar,band);
    }

    @Override
    public GeoImageReader clone() {
        return new TiledBufferedImage(root, gir.clone());
    }

    public String getInternalImage() {
  		return null;
  	}
    
	@Override
	public List<double[]> getFrameLatLon(int xSize, int ySize) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int[] readAndDecimateTile(int x, int y, int width, int height,
			int outWidth, int outLength, int xSize, int ySize, boolean filter,int band) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public File getOverviewFile() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int[] getAmbiguityCorrection(int xPos, int yPos) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
 
    
    //test main
    public static void main(String[] args) {
        GeoImageReader gir = GeoImageReaderFactory.createReaderForName("/media/52f29c23-b0dd-44d5-ac24-2ca4871c7d46/09DEC05073305-S2AS_R2C1-052241789020_02_P001.TIF").get(0);
        TiledBufferedImage tbi = new TiledBufferedImage(new File("/home/thoorfr/cache/test/" + gir.getFilesList()[0]), gir);
        for (int i = 0; i < 8000; i += Constant.TILE_SIZE) {
            tbi.preloadLineTile(i, Constant.TILE_SIZE,0);
        }
    }

	@Override
	public String getDisplayName(int band) {
		return "";
	}

	@Override
	public double[] getPixelsize() {
		// TODO Auto-generated method stub
		return null;
	}

}
