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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geoimage.def.SarImageReader;
import org.geoimage.impl.Gcp;
import org.geoimage.impl.GcpsGeoTransform;

/**
 *
 * @author thoorfr
 */
public class ThumbnailsImageReader extends SarImageReader {
	protected int xSize = -1;
	protected int ySize = -1;
	
    private ThumbnailsManager tm;
    private GcpsGeoTransform transform;
    private BufferedImage overview=null;
    private int[] size=null;
    private float[] ratio=null;

    public ThumbnailsImageReader(ThumbnailsManager tm) {
        this.tm = tm;
        parsePTS(tm.getPTS());
        geotransform = new GcpsGeoTransform(gcps, "EPSG:4326");
        overview = tm.getOverview();
        size = tm.getImageSize();
        ratio = new float[]{overview.getWidth()/(1f*size[0]),overview.getHeight()/(1f*size[1])};
        displayName = "Thumbnails";
        xSize = size[0];
        ySize = size[1];
    }

    @Override
    public int getNBand() {
        return 1;
    }

    @Override
    public int getNumberOfBytes() {
        return 2;
    }

    @Override
    public int getType(boolean oneBand) {
        return BufferedImage.TYPE_USHORT_GRAY;
    }

    public  double getPRF(int x,int y){
        return getPRF();
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
            Logger.getLogger(ThumbnailsImageReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String getAccessRights() {
        return "r";
    }

    @Override
    public String[] getFilesList() {
        return new String[]{tm.getPTS().getAbsolutePath()};
    }

    @Override
    public boolean initialise(File f) {

        return true;
    }


    @Override
    public int[] readTile(int x, int y, int width, int height) {
        return null;
    }

    @Override
    public int[] readAndDecimateTile(int x, int y, int width, int height, int outWidth, int outLength, int xSize, int ySize,boolean filter) {
        return null;
    }

    @Override
    public int read(int x, int y) {
        return overview.getRaster().getSample((int)(x*ratio[0]), (int)(y*ratio[1]), 0);
    }

    @Override
    public String getBandName(int band) {
        return "N/A";
    }

    @Override
    public void setBand(int band) {
    }

    @Override
    public void preloadLineTile(int y, int height) {
    }

    @Override
    public String getFormat() {
        return "Dummy";
    }

    @Override
    public double getImageAzimuth() {
        return 0;
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

	@Override
	public int getWidth() {
		return xSize;
	}

	@Override
	public int getHeight() {
		return ySize;
	}

	@Override
	public File getOverviewFile() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean supportAzimuthAmbiguity() {
		return false;
	}

	@Override
	public String getImgName() {
		return displayName;
	}
	@Override
	public String getDisplayName() {
		return displayName;
	}
}

