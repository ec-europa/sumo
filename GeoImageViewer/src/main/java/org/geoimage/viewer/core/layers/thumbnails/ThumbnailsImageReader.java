/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.core.layers.thumbnails;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import org.geoimage.def.GeoTransform;
import org.geoimage.def.SarImageReader;
import org.geoimage.impl.Gcp;
import org.geoimage.impl.geoop.GcpsGeoTransform;
import org.slf4j.LoggerFactory;

/**
 *
 * @author thoorfr
 */
public class ThumbnailsImageReader  {
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(ThumbnailsImageReader.class);

	protected int xSize = -1;
	protected int ySize = -1;
	
    private ThumbnailsManager tm;
    private BufferedImage overview=null;
    private int[] size=null;
    private float[] ratio=null;
    private ArrayList<Gcp> gcps=null;
    private GeoTransform transformation=null;
    
    public ThumbnailsImageReader(ThumbnailsManager tm) {
        this.tm = tm;
        parsePTS(tm.getPTS());
        transformation=new GcpsGeoTransform(gcps, "EPSG:4326");
        overview = tm.getOverview();
        size = tm.getImageSize();
        ratio = new float[]{overview.getWidth()/(1f*size[0]),overview.getHeight()/(1f*size[1])};
        xSize = size[0];
        ySize = size[1];
    }


    
    public GeoTransform getTransformation() {
		return transformation;
	}



	public void setTransformation(GeoTransform transformation) {
		this.transformation = transformation;
	}



	private void parsePTS(File pts) {
        try {
            RandomAccessFile temp = new RandomAccessFile(pts, "r");
            String line = null;
            this.gcps = new ArrayList<Gcp>();
            while ((line = temp.readLine()) != null) {
                String[] nums = line.split(" ");
                if (nums.length == 4) {
                    Gcp gcp = new Gcp();
                    gcp.setXgeo(Double.parseDouble(nums[0]));
                    gcp.setYgeo(Double.parseDouble(nums[1]));
                    gcp.setXpix(Double.parseDouble(nums[2]));
                    gcp.setYpix(Double.parseDouble(nums[3]));
                    gcps.add(gcp);
                }
            }
            temp.close();
        } catch (Exception ex) {
        	logger.error(ex.getMessage(),ex);
        }
    }


    public int getBand() {
        return 0;
    }

    public List<double[]> getFrameLatLon() {
        return null;
    }

    public String getInternalImage() {
  		return null;
  	}

	public int getWidth() {
		return xSize;
	}

	public int getHeight() {
		return ySize;
	}

}

