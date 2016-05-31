/*
 * 
 */
package org.geoimage.impl.radarsat;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geoimage.impl.imgreader.GeoToolsGDALReader;
import org.geoimage.impl.imgreader.IReader;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.slf4j.LoggerFactory;

/**
 * A class that reads Radarsat 2 images (geotiffs + xml). simple to quadri polarisation are supported
 * @author thoorfr
 */
public class Radarsat2ImageGDAL extends Radarsat2Image {
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(Radarsat2ImageGDAL.class);


    public Radarsat2ImageGDAL(File f) {
    	super(f);
    }


	@Override
	public int[] read(int x, int y, int w, int h, int band) throws IOException {
		return readTile(x, y, w, h, band);
	}

	 @Override
	    public boolean initialise() {
	    	try {
	    		super.imgName=manifestFile.getParentFile().getName();

	    		SAXBuilder builder = new SAXBuilder();
	    		setFile(manifestFile);
	    		doc = builder.build(productxml);
	    		tiffImages = getImages();
	    		if(tiffImages==null) return false;

	    		IReader image = tiffImages.values().iterator().next();

	            this.displayName=super.imgName;//+ "  " +image.getImageFile().getName();

	            super.parseProductXML(productxml);

	            bounds = new Rectangle(0, 0, image.getxSize(), image.getySize());
	            readPixel(0,0,0);
	        } catch (Exception ex) {
	            dispose();
	            logger.error(ex.getMessage(),ex);
	            return false;
	        }
	        return true;
	    }
	
	private Map<String, IReader> getImages() {
        List<?> elements = doc.getRootElement().getChild("imageAttributes", ns).getChildren("fullResolutionImageData", ns);
        Map<String, IReader> tiffs = new HashMap<String, IReader>();

        for (Object o : elements) {
            if (o instanceof Element) {
                File f = new File(productxml.getParent(), ((Element) o).getText());
                String polarisation = ((Element) o).getAttribute("pole").getValue();
                tiffs.put(polarisation, new GeoToolsGDALReader(f,1));
                //bands.add(polarisation);
            }
        }
        bands=tiffs.keySet().toArray(new String[0]);
        return tiffs;
    }
	
	
    @Override
    public void preloadLineTile(int y, int length,int band) {
        if (y < 0) {
            return;
        }
        GeoToolsGDALReader  tiff=(GeoToolsGDALReader)getImage(band);
        preloadedInterval = new int[]{y, y + length};
        Rectangle rect = new Rectangle(0, y, getImage(band).getxSize(), length);
        rect=tiff.getBounds().intersection(rect);
        try {
        	preloadedData=tiff.readPixValues(0, y, rect.width, rect.height);
        } catch (Exception ex) {
           //logger.warn(ex.getMessage(),ex);
        }
    }

   
    @Override
    public int[] readTile(int x, int y, int width, int height,int band) {
        Rectangle rect = new Rectangle(x, y, width, height);
        rect = rect.intersection(bounds);
        int[] tile= new int[height*width];
        if (rect.isEmpty()) {
            return tile;
        }
        if (rect.y != preloadedInterval[0] | rect.y + rect.height != preloadedInterval[1]) {
            preloadLineTile(rect.y, rect.height,band);
        }
        int yOffset = getImage(band).getxSize();
        int xinit = rect.x - x;
        int yinit = rect.y - y;
        for (int i = 0; i < rect.height; i++) {
            for (int j = 0; j < rect.width; j++) {
                int temp = i * yOffset + j + rect.x;
                	if(preloadedData.length>=temp){
                		tile[(i + yinit) * width + j + xinit] = preloadedData[temp];
                	}else{
                		
                    	//logger.debug("");
                	}	
            }
        }
        return tile;
    }

    @Override
    public synchronized long readPixel(int x, int y,int band) {
    	GeoToolsGDALReader tiff=null;
        try {
        	String b=getBandName(band);
        	tiff=(GeoToolsGDALReader)tiffImages.get(b);
            return tiff.readPixValues(x, y,1,1)[0];
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }finally{
        }
       
        return -1;
    }


}

