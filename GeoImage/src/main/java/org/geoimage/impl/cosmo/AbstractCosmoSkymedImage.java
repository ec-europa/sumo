package org.geoimage.impl.cosmo;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.math3.util.FastMath;
import org.geoimage.def.GeoImageReader;
import org.geoimage.def.SarImageReader;
import org.geoimage.factory.GeoTransformFactory;
import org.geoimage.impl.Gcp;
import org.slf4j.LoggerFactory;

import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;
import ncsa.hdf.object.Attribute;
import ncsa.hdf.object.h5.H5File;
import ncsa.hdf.object.h5.H5ScalarDS;

/**
 * Class to read Cosmo Skymed Images in hdf5 format.
 * To be able to read it the HDF5 DLLs or SOs files should be in the classpath
 * @author thoorfr
 */
public abstract class AbstractCosmoSkymedImage extends SarImageReader {
	protected String group=null;
	protected int xSize = -1;
	protected int ySize = -1;

	protected H5File h5file;
	protected H5ScalarDS imagedata;
	protected short[] preloadedData;
	protected Rectangle bounds;
    protected int[] preloadedInterval = new int[]{0, 0};
    protected long[] stride;
    protected long[] dims;
    protected long[] starts;
    protected boolean complex = false;
    protected String internalImage;
    protected String overview ;

    private static org.slf4j.Logger logger=LoggerFactory.getLogger(AbstractCosmoSkymedImage.class);
    
    public AbstractCosmoSkymedImage(File file,String pathImg,String group){
    	super(file);
    	internalImage=pathImg;
    	this.group=group;
    }

    @SuppressWarnings("unchecked")
	public abstract boolean initialise();

    
    @Override
    public void dispose(){
    	super.dispose();
    	if(h5file!=null){
    		try {
				h5file.close();
	    		h5file=null;
			} catch (HDF5Exception e) {
			}
    	}
    	if(imagedata!=null){
    		imagedata.close(0);
    		imagedata=null;
    	}
    }
    

    public H5File getH5file() {
		return h5file;
	}
    public void setH5file(H5File h5file) {
		this.h5file = h5file;
	}

    public int getNBand() {
        return 1;
    }

    public String getFormat() {
        return "CosmoSkymedImage";
    }

    public int getNumberOfBytes() {
        return 2;
    }

    public int getType(boolean oneBand) {
        return BufferedImage.TYPE_USHORT_GRAY;
    }

    public String getAccessRights() {
        return "r";
    }

    public String[] getFilesList() {
        return new String[]{h5file.getFilePath()};
    }
    
    


    public void setFile(File imageFile) {
        h5file = new H5File(imageFile.getAbsolutePath(), H5File.READ);
    }

    public int[] readTile(int x, int y, int width, int height,int band) {
        Rectangle rect = new Rectangle(x, y, width, height);
        rect = rect.intersection(bounds);
        int[] tile = new int[height * width];
        if (rect.isEmpty()) {
            return tile;
        }
        if (rect.y != preloadedInterval[0] || rect.y + rect.height != preloadedInterval[1]) {
            preloadLineTile(rect.y, rect.height,band);
        }

        if (complex) {
            int yOffset =2 * xSize;//xSize/ 200;//
            int xinit = rect.x - x;
            int yinit = rect.y - y;
            int i = 0;
            int j = 0;
            long temp1 =0;
            long temp2 =0;
            try {

                for (i = 0; i < rect.height; i++) {
                    for (j = 0; j < rect.width; j++) {
                        int temp = i * yOffset + 2 * j + 2 * rect.x;
                        temp1 = preloadedData[temp];
                        temp2 = preloadedData[temp + 1];
                        tile[(i + yinit) * width + j + xinit] = (int) Math.sqrt(temp1 * temp1 + temp2 * temp2);
                    }
                }
            } catch (Exception ex) {
            	logger.error(null, ex);
            }finally{
            }
        } else {
             int yOffset = xSize;
            int xinit = rect.x - x;
            int yinit = rect.y - y;
            try {
                for (int i = 0; i < rect.height; i++) {
                    for (int j = 0; j < rect.width; j++) {
                        int temp = i * yOffset + j + rect.x;
                        tile[(i + yinit) * width + j + xinit] = preloadedData[temp] & 0xFFFF;
                    }
                }
            } catch (Exception ex) {
            	logger.error(null, ex);
            }
        }
        return tile;
    }
   

    public int readPixel(int x, int y,int band) {
        if (x < 0 || y < 0 || x > xSize || y > ySize) {
            return -1;
        }
        stride[1] = 1;
        stride[0] = 1;
        dims[1] = 1;
        dims[0] = 1;
        starts[1] = x;
        starts[0] = y;
        try {
            Object o2 = imagedata.read();
            return Array.getInt(o2, 0) & 0xFFFF;
        } catch (HDF5Exception ex) {
        	logger.error(null, ex);
            return 0;
        }
    }

    public String getBandName(int band) {
        return getPolarization();
    }

    public String[] getBands() {
        return new String[]{getPolarization()};
    }
    
	@Override
	public int[] read(int x, int y, int width, int height, int band) throws IOException {
		return readTile(x, y, width, height, band);
	}


    public void preloadLineTile(int y, int height,int band) {
        if (y < 0) {
            return;
        }
        long[] selected = imagedata.getSelectedDims(); // the selected size of the dataet

        //this is necessary because select size as default contains always "1" in the third value
        if(selected.length>2)
        	selected[2]=2;
        preloadedInterval = new int[]{y, y + height};
        stride[1] = 1;
        stride[0] = 1;
        dims[1] = xSize;
        dims[0] = height;
        starts[1] = 0;
        starts[0] = y;

        try {
            preloadedData = (short[]) imagedata.read();
        } catch (Exception ex) {
        	logger.error(null, ex);
        }
    }

    /**
     * For convenience the class try to extract the quiclook from the hdf5 file
     * and save it in the image folder for easier access as "preview.jpg"
     */
    protected void extractQuickLook() {
    	if(group==null){
    		overview = new StringBuilder(h5file.getParentFile().getAbsolutePath()).append(File.separator).append("preview_.jpg").toString();
    		if (new File(overview).exists()) {
    			return;
    		}
    	}else{
	        try {
	        	overview = new StringBuilder(h5file.getParentFile().getAbsolutePath()).append(File.separator).append("preview_").append(group).append(".jpg").toString();
	            H5ScalarDS ql = (H5ScalarDS) h5file.get(group+"/QLK");
	            if (ql == null) {
	                ql = (H5ScalarDS) h5file.get("QLK");
	                if (ql == null) {
	                    return;
	                }
	            }
	            Object data = ql.getData();
	            BufferedImage bi = new BufferedImage(ql.getWidth(), ql.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
	            bi.getRaster().setDataElements(0, 0, bi.getWidth(), bi.getHeight(), data);
	            ImageIO.write(bi, "jpg", new File(overview));
	        } catch (Exception ex) {
	        	logger.error(null, ex);
	        }
    	}
    }

	@Override
	public String getInternalImage() {
		return internalImage;
	}
	
	
	@Override
	public GeoImageReader clone(){
		AbstractCosmoSkymedImage geo=null;
		try {
			geo = CosmoSkyFactory.instanceCosmoSkymed(h5file,this.internalImage, group);
			geo.imagedata=imagedata;
			//geo.preloadedData=preloadedData;
			geo.bounds=bounds;
			geo.preloadedInterval=preloadedInterval;
			geo.stride=stride;
			geo.dims=dims;
			geo.starts=starts;
			geo.complex=complex;
			geo.internalImage=internalImage;
			geo.initialise();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return geo;
	}

	@Override
    public int[] getAmbiguityCorrection(final int xPos,final int yPos) {
    	if(satelliteSpeed==0){
	    	satelliteSpeed = calcSatelliteSpeed();
	        orbitInclination = FastMath.toRadians(getSatelliteOrbitInclination());
    	}    

        double temp, deltaAzimuth, deltaRange;
        int[] output = new int[2];

        try {

        	// already in radian
            double incidenceAngle = getIncidence(xPos);
            double slantRange = getSlantRange(xPos,incidenceAngle);
            double prf = getPRF(xPos,yPos);

            double sampleDistAzim = getPixelsize()[0];
            double sampleDistRange =getPixelsize()[1];

            temp = (getRadarWaveLenght() * slantRange * prf) /
                    (2 * satelliteSpeed * (1 - FastMath.cos(orbitInclination) / getRevolutionsPerday()));

            //azimuth and delta in number of pixels
            deltaAzimuth = temp / sampleDistAzim;
            deltaRange = (temp * temp) / (2 * slantRange * sampleDistRange * FastMath.sin(incidenceAngle));

            output[0] = (int) FastMath.floor(deltaAzimuth);
            output[1] = (int) FastMath.floor(deltaRange);

        } catch (Exception ex) {
        	logger.error("Problem calculating the Azimuth ambiguity:"+ex.getMessage());
        }
        return output;
    }
	public double[] getPixelsize(){
		return pixelsize;
	}
	
 	public String getGroup() {
		return group;
	}


	public void setGroup(String group) {
		this.group = group;
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
		return new File(this.overview);
	}


	@Override
	public String getImgName() {
		return imgName;
	}


	@Override
	public String getDisplayName(int band) {
		
		return displayName;
	}
	
	@Override
	public double getPRF(int x,int y){
        //for all the other cases with only one PRF
        return getPRF();

    }

	@Override
	public String getSensor() {
		return "CS";
	}

	
}
