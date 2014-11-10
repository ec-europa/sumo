/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.core.layers;

import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.awt.image.WritableRaster;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import org.geoimage.def.GeoImageReader;
import org.geoimage.impl.GeotiffImage;
import org.geoimage.impl.SarImageReader;
import org.geoimage.impl.TiledBufferedImage;
import org.geoimage.viewer.core.Platform;
import org.geoimage.viewer.core.api.GeoContext;
import org.geoimage.viewer.core.api.IImageLayer;
import org.geoimage.viewer.core.api.ILayerManager;
import org.geoimage.viewer.core.layers.image.CacheManager;
import org.geoimage.viewer.core.layers.image.ImagePool;
import org.geoimage.viewer.core.layers.image.TextureCacheManager;
import org.geoimage.viewer.util.Constant;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureCoords;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;

/**
 *
 * @author thoorfr
 */
public class FastImageLayer extends LayerManager implements IImageLayer {

    private GeoImageReader activeGir;
    private HashMap<String, Float> contrast = new HashMap<String, Float>();
    private float brightness = 0;
    private int[] innerBands;
    private int xpadding;
    private int ypadding;
    private TextureCacheManager tcm;
    
    
    private Vector<String> submitedTiles;
    private ImagePool imagePool;
    private List<Future<Object[]>> futures = new Vector<Future<Object[]>>();
    private int mylevel = -1;
    private RescaleOp rescale = new RescaleOp(1f, brightness, null);
    private boolean disposed = false;
    private int maxCut = 1;
    private int il = 0;
    private int currentSize = 0;
    private int curlevel;
    private int levels;
    private boolean torescale = false;
    private int maxlevels;
    
    //for cuncurrency reader
    private ExecutorService pool;
    private int poolSize = 4;
    
    
    
    private int maxnumberoftiles = 7;
    static String MaxNumberOfTiles = "Max Number Of Tiles";

    static {
        Platform.getPreferences().insertIfNotExistRow(MaxNumberOfTiles, "7");
        Platform.getPreferences().insertIfNotExistRow("Maximum Tile Buffer Size", "128");
    }

    public FastImageLayer(ILayerManager parent, GeoImageReader gir) {
        super(parent);
        this.activeGir = gir;
        poolSize = Integer.parseInt(ResourceBundle.getBundle("GeoImageViewer").getString("maxthreads"));
        pool = Executors.newFixedThreadPool(poolSize);
        submitedTiles = new Vector<String>();
        imagePool = new ImagePool(gir, poolSize);
        

        setName(gir);
        
        description = gir.getName();
        innerBands = new int[1];
        innerBands[0] = 0;
        levels = (int) (Math.sqrt(Math.max(gir.getWidth() / Constant.OVERVIEW_SIZE_DOUBLE, gir.getHeight() / Constant.OVERVIEW_SIZE_DOUBLE))) + 1;
        maxlevels = (int) (Math.sqrt(Math.max(gir.getWidth() / (Constant.OVERVIEW_SIZE_DOUBLE*2), gir.getHeight() / (Constant.OVERVIEW_SIZE_DOUBLE*2)))) +1;  //massimo livello di zoom
        curlevel = levels;
        //TODO Understand the meaning of this values for padding
        xpadding = (((1 << levels) << 8) - gir.getWidth()) / 2;
        ypadding = (((1 << levels) << 8) - gir.getHeight()) / 2;
        
        String temp = Platform.getPreferences().readRow("Maximum Tile Buffer Size");
        int maxBuffer = Integer.parseInt(temp);
        tcm = new TextureCacheManager(maxBuffer);
        setInitialContrast();
        maxnumberoftiles = Integer.parseInt(Platform.getPreferences().readRow(MaxNumberOfTiles));
    }

    @Override
    public String getDescription() {
        if (activeGir.getMetadata() != null) {
            return activeGir.getDescription();
        } else {
            return activeGir.getName();
        }
    }

    @Override
    /**
     * displays the tiles on screen
     */
    public void render(GeoContext context) {
        if (torescale) {
            torescale = false;
            tcm.clear();
        }
        if (!context.isDirty()) {
            super.render(context);
            return;
        }
        GL gl = context.getGL();
        
        updateFutures(gl);
        
        float zoom = context.getZoom();
        int width = context.getWidth();
        int height = context.getHeight();
        int x = context.getX(), y = context.getY();
        
        
        gl.getGL2().glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_REPLACE);
        if (zoom >= 1) {
        	
            curlevel = (int) Math.sqrt(zoom + 1);
            //cycle to avoid the black area when zooming in/out and tiles not in memory
            for (int lll = maxlevels; lll > curlevel - 1; lll--) {
                //stop when lll=max zoom level
            	if (lll > maxlevels) {
                    break;
                }

                lll += il;
                
                if (lll < 0) {
                    continue;
                }
                
                
                if (this.mylevel != curlevel) {
                    this.mylevel = curlevel;
                    pool.shutdown();
                    pool = Executors.newFixedThreadPool(poolSize);
                }
                
                int xx = (int) (x + xpadding);
                int yy = (int) (y + ypadding);
                int w0 = xx / ((1 << lll) << 8);
                
                //max tiles to compute time by time
                int max = maxnumberoftiles; //for the computation of the array is important to keep this number odd
                int h0 = yy / ((1 << lll) << 8);
                int array[][] = new int[max][max];//contain the weight for reading tile sequence, 0 most important
                for (int i = 0; i <= max / 2; i++) {
                    int k = max - i - 1;
                    for (int j = 0; j < max / 2; j++) {
                        array[i][j] = k--;
                    }
                    for (int j = max / 2; j < max; j++) {
                        array[i][j] = k++;
                    }
                }
                for (int i = max / 2 + 1; i < max; i++) {
                    int k = i;
                    for (int j = 0; j < max / 2; j++) {
                        array[i][j] = k--;
                    }
                    for (int j = max / 2; j < max; j++) {
                        array[i][j] = k++;
                    }
                }

                String root=CacheManager.getCacheInstance(activeGir.getName()).getPath()+"\\";//+activeGir.getFilesList()[0].substring(activeGir.getFilesList()[0].indexOf("\\")+1
                
                final String initfile = root+ "/" + (int) lll + "/"+(activeGir instanceof TiledBufferedImage?((TiledBufferedImage)activeGir).getDescription()+"/":"");

                //AG loads the different tiles, starting from the center (k=0)
                for (int k = 0; k < max; k++) {
                    for (int j = 0; j < max; j++) {
                        if (j + h0 < 0) {
                            continue;
                        }
                        for (int i = 0; i < max; i++) {
                            if (i + w0 < 0) {
                                continue;
                            }
                            if (array[i][j] == k) {
                                //start reading tiles in center of the image and go through the borders
                                float ymin = (float) (((j + h0) * Constant.OVERVIEW_SIZE_DOUBLE * (1 << lll) - yy) / (height * zoom));
                                float ymax = (float) (((j + h0 + 1) * Constant.OVERVIEW_SIZE_DOUBLE * (1 << lll) - yy) / (height * zoom));
                                float xmin = (float) (((i + w0) * Constant.OVERVIEW_SIZE_DOUBLE * (1 << lll) - xx) / (1d * width * zoom));
                                float xmax = (float) (((i + w0 + 1) * Constant.OVERVIEW_SIZE_DOUBLE * (1 << lll) - xx) / (1d * width * zoom));

                                //check if the tile is in or out, if is not visible then is not loaded
                                if (ymin > 1 || ymax < 0) {
                                    continue;
                                }
                                if (xmin > 1 || xmax < 0) {
                                    continue;
                                }
                                
                                String file = initfile + getBandFolder(innerBands) + "/" + (i + w0) + "_" + (j + h0) + ".png";

                                //checked if the tile is already in memory or in cache, otherwise required it
                                if (!tryMemoryCache(gl, file, xmin, xmax, ymin, ymax)) {
                                    if (!tryFileCache(gl, file, lll, (i + w0), (j + h0), xmin, xmax, ymin, ymax)) {
                                        if (curlevel == 0 && lll == 0) {
                                            addTileToQueue(initfile, lll, (i + w0), (j + h0));
                                        } else if (curlevel == lll) {
                                            addTileToQueue(initfile, lll, (i + w0), (j + h0));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (zoom > 0) {
        	
            curlevel = 0;
            int xx = (int) (x + xpadding);
            int yy = (int) (y + ypadding);
            int w0 = xx / Constant.OVERVIEW_SIZE;
            //max tiles to compute time by time
            int max = maxnumberoftiles; //for the computation of the array is important to keep this number odd
            int h0 = yy / Constant.OVERVIEW_SIZE;
            final String initfile = activeGir.getFilesList()[0] + "/0/"+(activeGir instanceof TiledBufferedImage?((TiledBufferedImage)activeGir).getDescription()+"/":"");
            for (int j = 0; j < max; j++) {
                if (j + h0 < 0) {
                    continue;
                }
                for (int i = 0; i < max; i++) {
                    if (i + w0 < 0) {
                        continue;
                    }
                    //start reading tiles in center of the image and go through the borders
                    float ymin = (float) (((j + h0) * Constant.OVERVIEW_SIZE_DOUBLE - yy) / (height * zoom));
                    float ymax = (float) (((j + h0 + 1) * Constant.OVERVIEW_SIZE_DOUBLE - yy) / (height * zoom));
                    float xmin = (float) (((i + w0) * Constant.OVERVIEW_SIZE_DOUBLE - xx) / (1d * width * zoom));
                    float xmax = (float) (((i + w0 + 1) * Constant.OVERVIEW_SIZE_DOUBLE - xx) / (1d * width * zoom));

                    //check if the tile is in or out, if is not visible then is not loaded
                    if (ymin > 1 || ymax < 0) {
                        continue;
                    }
                    if (xmin > 1 || xmax < 0) {
                        continue;
                    }
                    String file = initfile + getBandFolder(innerBands) + "/" + (i + w0) + "_" + (j + h0) + ".png";

                    //checked if the tile is already in memory or in cache, otherwise required it
                    if (!tryMemoryCache(gl, file, xmin, xmax, ymin, ymax)) {
                        if (!tryFileCache(gl, file, 0, (i + w0), (j + h0), xmin, xmax, ymin, ymax)) {
                            addTileToQueue(initfile, 0, (i + w0), (j + h0));
                        }
                    }
                }
            }

        }
        displayDownloading(futures.size());
        super.render(context);

        if (this.disposed) {

            disposeSync();
        }

    }

    private void displayDownloading(int size) {
        if (currentSize != size) {
            if (size == 0) {
                Platform.setInfo("", -1);
            } else {
                Platform.setInfo("loading " + size);
            }

            currentSize = size;
        }

    }

    private void setInitialContrast() {
        activeGir.setBand(innerBands[0]);

        // very rough calculation of a possible suitable contrast value
        int[] data = activeGir.readTile(activeGir.getWidth() / 2 - 100, activeGir.getHeight() / 2 - 100, 200, 200);
        float average = 0;
        for (int i = 0; i < data.length; i++) {
            average = average + data[i];
        }

        average = average / data.length;
        int factor=8 * activeGir.getNumberOfBytes();
        //if the factor is >16 the contrast will be too high 
        factor=(factor<16)?factor:16;
        	
        float contrastLevel=(1 << ((factor))) / 5 / average;
        if(contrastLevel==0)
        	contrastLevel=100;
        if(contrastLevel>255)
        	contrastLevel=128;
        setContrast(contrastLevel);
    }

    //search for tiles in the file cache
    private boolean tryFileCache(GL gl, String file, int level, int i, int j, float xmin, float xmax, float ymin, float ymax) {
        if (CacheManager.getCacheInstance(activeGir.getName()).contains(file) & !submitedTiles.contains(level + " " + getBandFolder(innerBands) + " " + i + " " + j)) {
            try {
            	BufferedImage temp =null;
            	try {
            		temp = ImageIO.read(CacheManager.getCacheInstance(activeGir.getName()).newFile(file));
            	} catch (Exception ex) {
                    Logger.getLogger(FastImageLayer.class.getName()).log(Level.SEVERE, null, ex);
                }	
                if (temp == null) {
                    return false;
                }

                if (temp.getColorModel().getNumComponents() == 1) {
                    temp = rescale.filter(temp, rescale.createCompatibleDestImage(temp, temp.getColorModel()));
                }
                Texture t = AWTTextureIO.newTexture(gl.getGLProfile(),temp, false);
                tcm.add(file, t);
                bindTexture(gl, t, xmin, xmax, ymin, ymax);
                return true;
            } catch (Exception ex) {
                Logger.getLogger(FastImageLayer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return false;
    }

    //search for the tiles on memory
    private boolean tryMemoryCache(GL gl, String file, float xmin, float xmax, float ymin, float ymax) {
        Texture t = tcm.getTexture(file);
        if (t != null) {
            bindTexture(gl, t, xmin, xmax, ymin, ymax);
            return true;
        }
        return false;
    }

    private void bindTexture(GL gl, Texture texture, float xmin, float xmax, float ymin, float ymax) {
        texture.enable(gl);
        texture.bind(gl);
        TextureCoords coords = texture.getImageTexCoords();
        gl.getGL2().glBegin(GL2.GL_QUADS);
        gl.getGL2().glTexCoord2f(coords.left(), coords.top());
        gl.getGL2().glVertex2f(xmin, 1 - ymin);
        gl.getGL2().glTexCoord2f(coords.right(), coords.top());
        gl.getGL2().glVertex2f(xmax, 1 - ymin);
        gl.getGL2().glTexCoord2f(coords.right(), coords.bottom());
        gl.getGL2().glVertex2f(xmax, 1 - ymax);
        gl.getGL2().glTexCoord2f(coords.left(), coords.bottom());
        gl.getGL2().glVertex2f(xmin, 1 - ymax);
        gl.getGL2().glEnd();
        texture.disable(gl);
    }

    private BufferedImage createImage(GeoImageReader gir, int x, int y, int width, int height, float zoom) {
        BufferedImage bufferedImage = new BufferedImage(width, height, gir.getType(innerBands.length == 1));
        int[] nat;
        //System.out.println(zoom);
        WritableRaster raster = bufferedImage.getRaster();

        // Put the pixels on the raster.
        if (innerBands.length == 1) {
            int band = innerBands[0];
            gir.setBand(band);
            nat = gir.readAndDecimateTile(x, y, (int) (width * zoom), (int) (height * zoom), width, height,((SarImageReader)gir).getWidth(),((SarImageReader)gir).getHeight() ,true);
            raster.setPixels(0, 0, width, height, nat);

        } else {
            int b = 0;
            for (int band : innerBands) {
                gir.setBand(band);
                nat = gir.readAndDecimateTile(x, y, (int) (width * zoom), (int) (height * zoom), width, height,((SarImageReader)gir).getWidth(),((SarImageReader)gir).getHeight(), true);
                ///if (zoom == 1) {
                if(maxCut>1){
                    for(int tt=0;tt<nat.length;tt++){
                        nat[tt]/=maxCut;
                    }
                }
                raster.setSamples(0, 0, width, height, b, nat);
        
                b++;
                if (b > raster.getNumBands()) {
                    break;
                }
            }

        }
        return bufferedImage;
    }

    private String getBandFolder(int[] band) {
        String out = "";
        if (band.length == 1) {
            out += band[0];
        } else if (band.length > 1) {
            out += band[0];
            for (int i = 1; i < band.length; i++) {
                out += "_" + band[i];
            }
        }

        return out;
    }

    public void addTileToQueue(final String initfile, final int level, final int i, final int j) {
        if (!submitedTiles.contains(level + " " + getBandFolder(innerBands) + " " + i + " " + j)) {
            submitedTiles.add(level + " " + getBandFolder(innerBands) + " " + i + " " + j);
            futures.add(0, pool.submit(new Callable<Object[]>() {

                public Object[] call() {
                    final File f = CacheManager.getCacheInstance(activeGir.getName()).newFile(initfile + getBandFolder(innerBands) + "/" + i + "_" + j + ".png");
                    if (f == null) {
                        return new Object[]{initfile + getBandFolder(innerBands) + "/" + i + "_" + j + ".png", level + " " + i + " " + j, null};
                    }

                    GeoImageReader gir2 = imagePool.get();
                    if (gir2 == null) {
                        return new Object[]{f.getAbsolutePath(), level + " " + getBandFolder(innerBands) + " " + i + " " + j, null};
                    }

                    try {
                        final BufferedImage out = createImage(gir2, i * (1 << level) * Constant.OVERVIEW_SIZE - xpadding, j * (1 << level) * Constant.OVERVIEW_SIZE - ypadding, Constant.OVERVIEW_SIZE, Constant.OVERVIEW_SIZE, (1 << level));
                        imagePool.release(gir2);
                        ImageIO.write(out, "png", f);
                        return new Object[]{f.getAbsolutePath(), level + " " + getBandFolder(innerBands) + " " + i + " " + j, out};
                    } catch (Exception ex) {
                        imagePool.release(gir2);
                        Logger.getLogger(FastImageLayer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return new Object[]{f.getAbsolutePath(), level + " " + getBandFolder(innerBands) + " " + i + " " + j, null};
                }
            }));
        }
    }
    
    /**
     * 
     * @param gl
     */
    private void updateFutures(GL gl) {
        Vector<Future<Object[]>> remove1 = new Vector<Future<Object[]>>();
        for (Future<Object[]> f : futures) {
            if (f.isDone() || f.isCancelled()) {
                remove1.add(f);
                try {
                    Object[] o = f.get();
                    submitedTiles.remove(o[1]);
                    if (o[2] != null) {
                        tcm.add((String) o[0], AWTTextureIO.newTexture(gl.getGLProfile(),(BufferedImage) o[2], false));
                    }


                } catch (Exception ex) {
                    Logger.getLogger(FastImageLayer.class.getName()).log(Level.SEVERE, null, ex);
                    //mcm.clear();
                    //tcm.clear();
                }
            }

        }
        futures.removeAll(remove1);
    }

    public void setContrast(float value) {
        contrast.put(createBandsString(innerBands), value); 
        torescale = true;
        rescale = new RescaleOp(value, brightness, null);
    }

    private String createBandsString(int[] b) {
        String out = "";
        for (int i = 0; i < b.length; i++) {
            out += b[i] + ",";
        }
        return out;
    }

    public void setBrightness(float value) {
        this.brightness = value;
        torescale = true;
        rescale = new RescaleOp(getContrast(), brightness, null);
    }

    public float getContrast() {
        return contrast.get(createBandsString(innerBands)) == null ? 1 : contrast.get(createBandsString(innerBands));
    }

    public float getBrightness() {
        return brightness;
    }

    public void setBand(int[] values) {
        if (futures.size() > 0) {
            return;
        }
        this.innerBands = values;
        if (contrast.get(createBandsString(innerBands)) == null) {
            setInitialContrast();
        } else {
            rescale = new RescaleOp(contrast.get(createBandsString(innerBands)), brightness, null);
        }
    }

    public int getNumberOfBands() {
        return activeGir.getNBand();
    }

    public int[] getBands() {
        return innerBands;
    }

    @Override
    public void dispose() {
        disposed = true;
        pool.shutdownNow();
        pool = null;
        super.dispose();
        activeGir.dispose();
        activeGir = null;
        tcm.clear();
        tcm = null;
        submitedTiles.clear();
        submitedTiles = null;
        imagePool.dispose();
        imagePool = null;
    }

    private void disposeSync() {

        pool.shutdownNow();
        pool = null;
        super.dispose();
        activeGir.dispose();
        activeGir = null;
        tcm.clear();
        tcm = null;
        submitedTiles.clear();
        submitedTiles = null;
        imagePool.dispose();
        imagePool = null;

    }

    public GeoImageReader getImageReader() {
        return activeGir;
    }

    public void setMaximumCut(float value) {
        maxCut = (int) value;
    }

    public float getMaximumCut() {
        return maxCut;
    }

    public void level(int levelIncrease) {
        this.il = levelIncrease;
    }
    
    
    public void setName(GeoImageReader gir){
    	if(gir.getName()!=null&&!gir.getName().equals(""))
        	setName(gir.getName());
        else{
        	String temp = gir.getFilesList()[0].replace("\\", "/");
        	String name=temp.substring(temp.lastIndexOf("/") + 1);
        	if(gir.getInternalImage()!=null)
        		name=name+"_"+gir.getInternalImage();
        	setName(name);
        }	
    }
    
    
}
