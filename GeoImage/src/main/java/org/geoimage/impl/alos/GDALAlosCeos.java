/*
 *
 */
package org.geoimage.impl.alos;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.util.FastMath;
import org.gdal.gdal.GCP;
import org.geoimage.def.GeoMetadata;
import org.geoimage.factory.GeoTransformFactory;
import org.geoimage.impl.Gcp;
import org.geoimage.impl.alos.prop.CeosAlosProperties;
import org.geoimage.impl.imgreader.GeoToolsGDALReader;
import org.geoimage.impl.imgreader.IReader;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeocentricCRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GDALAlosCeos extends Alos {
	private Logger logger = LoggerFactory.getLogger(GDALAlosCeos.class);

	public GDALAlosCeos(File manifest) {
		super(manifest);
		prop = new CeosAlosProperties(manifest.getParent());
	}

	@Override
	public int getNBand() {
		return polarizations.size();
	}

	@Override
	public String getFormat() {
		return getClass().getCanonicalName();
	}

	@Override
	public int getType(boolean oneBand) {
		if (oneBand || polarizations.size() < 2)
			return BufferedImage.TYPE_USHORT_GRAY;
		else
			return BufferedImage.TYPE_INT_RGB;
	}

	@Override
	public String[] getFilesList() {
		return new String[] { manifestFile.getAbsolutePath() };
	}


	@Override
	public boolean initialise() {
		try {
			File mainFolder = manifestFile.getParentFile();

			polarizations = prop.getPolarizations();

			// set image properties
			alosImages = new HashMap<>();
			List<String> imgNames = prop.getImageNames();

			for (int i = 0; i < imgNames.size(); i++) {
				String img = imgNames.get(i);
				File imgFile = new File(mainFolder.getAbsolutePath() + File.separator + img);
				GeoToolsGDALReader t = new GeoToolsGDALReader(imgFile, 1);
				alosImages.put(img.substring(4, 6), t);
			}

			// String
			// nameFirstFile=alosImages.get(bandName).getImageFile().getName();
			super.pixelsize[0] = prop.getPixelSpacing();
			super.pixelsize[1] = prop.getPixelSpacing();

			// read and set the metadata from the manifest and the annotation
			setXMLMetaData();

			// we have only the corners
			gcps = new ArrayList<>();
			GeoToolsGDALReader image=(GeoToolsGDALReader)alosImages.values().iterator().next();
			List<GCP> gcpsGDAL=image.getGCPS();
			for(GCP gcp:gcpsGDAL){
				Gcp g=new Gcp(gcp.getGCPPixel(),gcp.getGCPLine(),gcp.getGCPX(),gcp.getGCPY());
				gcps.add(g);
			}
			String epsg = "EPSG:4326";
			geotransform = GeoTransformFactory.createFromGcps(gcps, epsg);

			double[] latlon = geotransform.getGeoFromPixel(0, 0);
			double[] position = new double[3];
			MathTransform convert = CRS.findMathTransform(DefaultGeographicCRS.WGS84, DefaultGeocentricCRS.CARTESIAN);
			convert.transform(latlon, 0, position, 0, 1);

			// get incidence angles from gcps
			float firstIncidenceangle = (float) (this.gcps.get(0).getAngle());
			float lastIncidenceAngle = (float) (this.gcps.get(this.gcps.size() - 1).getAngle());
			setIncidenceNear(firstIncidenceangle < lastIncidenceAngle ? firstIncidenceangle : lastIncidenceAngle);
			setIncidenceFar(firstIncidenceangle > lastIncidenceAngle ? firstIncidenceangle : lastIncidenceAngle);
			
			
			return true;
		} catch (TransformException ex) {
			logger.error(ex.getMessage(), ex);
		} catch (FactoryException ex) {
			logger.error(ex.getMessage(), ex);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return false;
	}




	@Override
	public String getDisplayName(int band) {
		try {
			return alosImages.get(getBandName(band)).getImageFile().getName();
		} catch (Exception e) {
			return "Alos-IMG-" + System.currentTimeMillis();
		}
	}

	@Override
	public int getWidth() {
		return getImage(0).getxSize();
	}

	@Override
	public int getHeight() {
		return getImage(0).getySize();
	}

	@Override
	public double getPRF(int x, int y) {
		try {
			return prop.getPrf();
		} catch (Exception e) {
			logger.warn("Error reading PRF",e);
			return 0;
		}
	}

	@Override
	public File getOverviewFile() {
		File folder = manifestFile.getParentFile();
		File[] files = folder.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if (pathname.getName().startsWith("BRS") && pathname.getName().endsWith("jpg"))
					return true;
				return false;
			}
		});
		return files[0];
	}

	@Override
	public String getSensor() {
		return "ALOS";
	}

	public IReader getImage(int band) {
		IReader img = null;
		try {
			img = alosImages.get(getBandName(band));
		} catch (Exception e) {
			logger.error(this.getClass().getName() + ":getImage function  " + e.getMessage());
		}
		return img;
	}

	// ----------------------------------------------

	@Override
	public int[] read(int x, int y, int width, int height, int band)  {
	      int data[]=null;

	      GeoToolsGDALReader tiff=(GeoToolsGDALReader)getImage(band);
	       try {

		   		int[] b=tiff.readPixValues(x, y, width,height);
		   		data=new int[b.length];
		       	for(int i=0;i<b.length;i++)
		       		data[i]=b[i];

		       } catch (Exception ex) {
		           logger.warn(ex.getMessage());
		       }finally{
		       }

	      return data;
	}

	@Override
	public int[] readTile(int x, int y, int width, int height, int band) {
		Rectangle rect = new Rectangle(x, y, width, height);
		rect = rect.intersection(getImage(band).getBounds());
		int[] tile = new int[height * width];
		if (rect.isEmpty()) {
			return tile;
		}

		if (rect.y != preloadedInterval[0] || rect.y + rect.height != preloadedInterval[1]
				|| preloadedData.length < (rect.y * rect.height - 1)) {
			preloadLineTile(rect.y, rect.height, band);
		} else {
			 //logger.debug("using preloaded data");
		}

		int yOffset = getImage(band).getxSize();
		int xinit = rect.x - x;
		int yinit = rect.y - y;
		for (int i = 0; i < rect.height; i++) {
			for (int j = 0; j < rect.width; j++) {
				int temp = i * yOffset + j + rect.x;
				try {
					tile[(i + yinit) * width + j + xinit] =(int) preloadedData[temp];
				} catch (ArrayIndexOutOfBoundsException e) {
					logger.warn("readTile function:" + e.getMessage());
				}
			}
		}
		return tile;
	}

	@Override
	public long readPixel(int x, int y, int band) {
		Rectangle rect = new Rectangle(x, y, 1, 1);
		rect = rect.intersection(getImage(band).getBounds());
		int data[] = null;

		GeoToolsGDALReader img = (GeoToolsGDALReader)getImage(band);
		try {
			data=img.readPixValues(x, y,1,1);
		} finally {
		}

		return data[0];

	}

	@Override
	public void preloadLineTile(int y, int length, int band) {
		if (y < 0) {
			return;
		}
		GeoToolsGDALReader tiff = (GeoToolsGDALReader)getImage(band);

		preloadedInterval = new int[] { y, y + length };
		Rectangle rect = new Rectangle(0, y, tiff.getxSize(), length);

		rect = tiff.getBounds().intersection(rect);

		try {
			int[] bi = null;
			try {
				bi = tiff.readPixValues(0, y, rect.width, rect.height);
				//bi = tiff.readShortValues(0, y, tiff.xSize, length);
			} catch (Exception e) {
				logger.warn("Problem reading image POS x:" + 0 + "  y: " + y + "   try to read again");
				try {
					Thread.sleep(100);
				} catch (InterruptedException exx) {
					Thread.currentThread().interrupt();
				}
				bi = tiff.readPixValues(0, y, rect.width, rect.height);
			}
			preloadedData =ArrayUtils.toObject(bi);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		} finally {

		}

	}

	public void setXMLMetaData() {
		super.setXMLMetaData();
		try {
			setMetadata(GeoMetadata.SATELLITE_ALTITUDE,prop.getSatelliteAltitude());
		} catch (Exception e) {
			logger.warn(e.getMessage(),e);
		}
	}

	@Override
    public int[] getAmbiguityCorrection(final int xPos,final int yPos) {
    	float prf=0;
    	if(isScanSar())
    		prf=NOMINAL_PRF; //Nominal PRF
		else
			try {
				prf=prop.getPrf();
			} catch (Exception e) {
				logger.warn("Error reading PRF",e);
				prf=0;
			}
    	
	    satelliteSpeed = calcSatelliteSpeed();

        double temp, deltaAzimuth, deltaRange;
        int[] output = new int[2];

        try {
        	// already in radian
            double incidenceAngle = getIncidence(xPos);
            double slantRange = ((CeosAlosProperties)prop).getSlantRange();

            double sampleDistAzim = getPixelsize()[0];
            double sampleDistRange =getPixelsize()[1];

            temp = (((CeosAlosProperties)prop).getWaveLength() * slantRange * prf) /
                    (2 * satelliteSpeed * (1 - FastMath.cos(ORBIT_INCLINATION) / REV_PER_DAY));

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


	public static void main(String args[]) {
		File f = new File(
				"F:/SumoImgs/AlosTrialTmp/SM/0000054534_001001_ALOS2049273700-150422/IMG-HH-ALOS2049273700-150422-FBDR1.5RUD");
		GeoToolsGDALReader gg = new GeoToolsGDALReader(f, 0);

		System.out.println("x:" + gg.xSize + " - y:" + gg.ySize);

	}
}
