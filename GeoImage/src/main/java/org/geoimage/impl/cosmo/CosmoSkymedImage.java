/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
public class CosmoSkymedImage extends SarImageReader {
	private String group=null;
	protected int xSize = -1;
	protected int ySize = -1;

	private H5File h5file;
	private H5ScalarDS imagedata;
    private short[] preloadedData;
    private Rectangle bounds;
    private int[] preloadedInterval = new int[]{0, 0};
    private long[] stride;
    private long[] dims;
    private long[] starts;
    private boolean complex = false;
    protected String internalImage;
    protected String overview ;

    private static org.slf4j.Logger logger=LoggerFactory.getLogger(CosmoSkymedImage.class);
    
    public CosmoSkymedImage(File file,String pathImg,String group){
    	super(file);
    	internalImage=pathImg;
    	this.group=group;
    }

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
    
    

    @SuppressWarnings("unchecked")
	public boolean initialise() {
        try {
        	this.imgName=super.manifestFile.getName();
        	this.imgName=this.imgName.substring(0, this.imgName.lastIndexOf("."));
        	this.displayName=super.manifestFile.getName();
        	if(group!=null&&!group.equalsIgnoreCase(""))
        		this.displayName=this.displayName+"_"+group;
        	
        	h5file = new H5File(super.manifestFile.getAbsolutePath(), H5File.READ);
        	imagedata = (H5ScalarDS) h5file.get(internalImage);
            extractQuickLook();
            List<Object> metadata = new ArrayList<Object>();
            metadata.addAll(h5file.get("/").getMetadata());
            metadata.addAll(h5file.get(internalImage.substring(0, 3)).getMetadata());

            if(imagedata==null)
            	return false;
        	metadata.addAll(imagedata.getMetadata());
        	
            long[] selected = imagedata.getSelectedDims(); // the selected size of the dataset
            selected[0]=1;
            if(selected.length>2)
            	selected[2]=2;

            //read image dimensions
            xSize = (int) imagedata.getDims()[1];
            ySize = (int) imagedata.getDims()[0];

            //
            stride = imagedata.getStride();
            //
            dims = imagedata.getSelectedDims();

            //
            starts = imagedata.getStartDims();

            
            setMetaWidth(xSize);
            setMetaHeight(ySize);
            
            bounds = new Rectangle(0, 0, xSize, ySize);
            
            gcps = new ArrayList<Gcp>();

            

           /* Object oo=CollectionUtils.find(metadata, new Predicate() {
				
				@Override
				public boolean evaluate(Object o) {
					 
					return ((Attribute)o).getName().contains("Speed");
				}
			});
            for (Object o : metadata) {
            	System.out.println(((Attribute)o).getName());
            }*/
            
            for (Object o : metadata) {
                Attribute a = (Attribute) o;
                //System.out.println(a.getName() + "=" + a.getValue().toString());
                if (a.getName().equals("Bottom Left Geodetic Coordinates")) {
                    double[] val = (double[]) a.getValue();
                    Gcp gcp = new Gcp();
                    gcp.setXpix(0);
                    gcp.setYpix(ySize);
                    gcp.setOriginalXpix(0.0);
                    gcp.setXgeo(val[1]);
                    gcp.setYgeo(val[0]);
                    gcps.add(gcp);
                } else if (a.getName().equals("Bottom Right Geodetic Coordinates")) {
                    double[] val = (double[]) a.getValue();
                    Gcp gcp = new Gcp();
                    gcp.setXpix(xSize);
                    gcp.setOriginalXpix(new Double(xSize));
                    gcp.setYpix(ySize);
                    gcp.setXgeo(val[1]);
                    gcp.setYgeo(val[0]);
                    gcps.add(gcp);
                } else if (a.getName().equals("Top Left Geodetic Coordinates")) {
                    double[] val = (double[]) a.getValue();
                    Gcp gcp = new Gcp();
                    gcp.setXpix(0);
                    gcp.setOriginalXpix(new Double(xSize));
                    gcp.setYpix(0);
                    gcp.setXgeo(val[1]);
                    gcp.setYgeo(val[0]);
                    gcps.add(gcp);
                } else if (a.getName().equals("Top Right Geodetic Coordinates")) {
                    double[] val = (double[]) a.getValue();
                    Gcp gcp = new Gcp();
                    gcp.setXpix(xSize);
                    gcp.setOriginalXpix(new Double(xSize));
                    gcp.setYpix(0);
                    gcp.setXgeo(val[1]);
                    gcp.setYgeo(val[0]);
                    gcps.add(gcp);
                } else if (a.getName().equals("Scene Sensing Start UTC")) {
                    String[] val = (String[]) a.getValue();
                    setTimeStampStart(val[0]);
                } else if (a.getName().equals("Scene Sensing Stop UTC")) {
                    String[] val = (String[]) a.getValue();
                    setTimeStampStop(val[0]);
                } else if (a.getName().equals("Equivalent Number of Looks")) {
                    double[] val = (double[]) a.getValue();
                    setENL(String.valueOf(val[0]));
                } else if (a.getName().equals("Column Spacing")) {
                    double[] val = (double[]) a.getValue();
                    setRangeSpacing(new Float(val[0]));
                    pixelsize[0]=getRangeSpacing();
                } else if (a.getName().equals("Far Incidence Angle")) {
                    double[] val = (double[]) a.getValue();
                    setIncidenceFar(new Float(val[0]));
                } else if (a.getName().equals("Near Incidence Angle")) {
                    double[] val = (double[]) a.getValue();
                    setIncidenceNear(new Float(val[0]));
                } else if (a.getName().equals("Line Spacing")) {
                    double[] val = (double[]) a.getValue();
                    setAzimuthSpacing(new Float(val[0]));
                    pixelsize[1]=getAzimuthSpacing();
                } else if (a.getName().equals("Look Side")) {
                    String[] val = (String[]) a.getValue();
                    setLookDirection(val[0]);
                } else if (a.getName().equals("Orbit Direction")) {
                    String[] val = (String[]) a.getValue();
                    setOrbitDirection(val[0]);
                } else if (a.getName().equals("Processing Centre")) {
                    String[] val = (String[]) a.getValue();
                    setProcessor(val[0]);
                } else if (a.getName().equals("Radar Wavelength")) {
                    double[] val = (double[]) a.getValue();
                    setRadarWaveLenght(val[0]);
                } else if (a.getName().equals("Product Type")) {
                    String[] val = (String[]) a.getValue();
                    setType(val[0]);
                } else if (a.getName().equals("Satellite ID")) {
                    String[] val = (String[]) a.getValue();
                    setSatellite(val[0]);
                } else if (a.getName().equals("Satellite Height")) {
                    double[] val = (double[]) a.getValue();
                    setSatelliteAltitude(val[0]);
                } else if (a.getName().equals("Polarisation")) {
                    String[] val = (String[]) a.getValue();
                    setPolarization(val[0]);
                } else if (a.getName().equals("Multi-Beam ID")) {
                    String[] val = (String[]) a.getValue();
                    setBeam(val[0]);
                }else if (a.getName().equals("PRF")) {
                	double[] val = (double[]) a.getValue();
                    setPRF(val[0]);
                }else if (a.getName().equals("")) {
                	double[] val = (double[]) a.getValue();
                    setPRF(val[0]);
                }
                
                setSatelliteOrbitInclination(97.86);
                setRevolutionsPerday(14.8125);
                
            }
            setSensor("CS");
            if (getType().startsWith("SCS")) {
                complex = true;
            }
            geotransform = GeoTransformFactory.createFromGcps(gcps, "EPSG:4326");

        } catch (Exception ex) {
        	logger.error(null, ex);
            return false;
        }
        return true;
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
    private void extractQuickLook() {
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
		CosmoSkymedImage geo=new CosmoSkymedImage(super.manifestFile,this.internalImage, group);
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
