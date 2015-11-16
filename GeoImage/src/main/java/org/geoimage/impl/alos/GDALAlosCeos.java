package org.geoimage.impl.alos;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferUShort;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geoimage.def.SarImageReader;
import org.geoimage.factory.GeoTransformFactory;
import org.geoimage.impl.GDALTIFF;
import org.geoimage.impl.Gcp;
import org.geoimage.impl.TIFF;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeocentricCRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.media.imageio.plugins.tiff.TIFFImageReadParam;
import com.sun.media.imageioimpl.plugins.tiff.TIFFImageReader;
import com.vividsolutions.jts.geom.Coordinate;

public class GDALAlosCeos extends SarImageReader {
	private Logger logger = LoggerFactory.getLogger(GDALAlosCeos.class);

	protected int[] preloadedInterval = new int[] { 0, 0 };
	protected short[] preloadedData;

	private AlosProperties props = null;
	private List<String> polarizations = null;
	protected Map<String, GDALTIFF> alosImages;

	public GDALAlosCeos(File manifest) {
		super(manifest);
		props = new AlosProperties(manifest);
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

			polarizations = props.getPolarizations();

			// set image properties
			alosImages = new HashMap<>();
			List<String> imgNames = props.getImageNames();

			for (int i = 0; i < imgNames.size(); i++) {
				String img = imgNames.get(i);
				File imgFile = new File(mainFolder.getAbsolutePath() + File.separator + img);
				GDALTIFF t = new GDALTIFF(imgFile, 0);
				alosImages.put(img.substring(4, 6), t);
			}

			String bandName = getBandName(0);
			// String
			// nameFirstFile=alosImages.get(bandName).getImageFile().getName();
			super.pixelsize[0] = props.getPixelSpacing();
			super.pixelsize[1] = props.getPixelSpacing();

			// read and set the metadata from the manifest and the annotation
			setXMLMetaData();

			Coordinate[] corners = props.getCorners();
			int lines = props.getNumberOfLines();
			int pix = props.getNumberOfPixels();
			// we have only the corners
			gcps = new ArrayList<>();
			gcps.add(new Gcp(corners[0].x, corners[0].y, 0, 0));
			gcps.add(new Gcp(corners[1].x, corners[1].y, pix, 0));
			gcps.add(new Gcp(corners[2].x, corners[2].y, pix, lines));
			gcps.add(new Gcp(corners[3].x, corners[3].y, 0, lines));

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

	/**
	 * 
	 * @param productxml
	 * @param safeReader
	 * @param annotationReader
	 * @throws TransformException
	 */
	private void setXMLMetaData() {
		setSatellite(new String("ALOS"));

		// polarizations string
		List<String> pols = props.getPolarizations();
		String strPol = "";
		for (String p : pols) {
			strPol = strPol.concat(p).concat(" ");
		}
		setPolarization(strPol);
		setSensor("ALOS");

		setSatelliteOrbitInclination(98.18);

		setRangeSpacing(new Float(props.getPixelSpacing()));
		setAzimuthSpacing(new Float(props.getPixelSpacing()));
		setMetaHeight(props.getNumberOfLines());
		setMetaWidth(props.getNumberOfPixels());
		setNumberBands(pols.size());
		setNumberOfBytes(16);

		// TODO:check this value
		// float enl=org.geoimage.impl.ENL.getFromGeoImageReader(this);
		setENL("2.3");// String.valueOf(enl));

		/*
		 * String start=header.getStartTime().toString().replace('T', ' ');
		 * String stop=header.getStopTime().toString().replace('T', ' ');
		 * setTimeStampStart(start);//Timestamp.valueOf(start));
		 * setTimeStampStop(stop);//Timestamp.valueOf(stop));
		 */

	}

	@Override
	public String getBandName(int band) {
		return "HH";// polarizations.get(band);
	}

	@Override
	public String[] getBands() {
		return polarizations.toArray(new String[0]);
	}

	@Override
	public String getImgName() {
		// TODO Auto-generated method stub
		return null;
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
		return getImage(0).xSize;
	}

	@Override
	public int getHeight() {
		return getImage(0).ySize;
	}

	@Override
	public double getPRF(int x, int y) {
		return 0;
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

	public GDALTIFF getImage(int band) {
		GDALTIFF img = null;
		try {
			img = alosImages.get(getBandName(band));
		} catch (Exception e) {
			logger.error(this.getClass().getName() + ":getImage function  " + e.getMessage());
		}
		return img;
	}

	// ----------------------------------------------

	@Override
	public int[] read(int x, int y, int width, int height, int band) throws IOException {
		return null;
	}

	@Override
	public int[] readTile(int x, int y, int width, int height, int band) {
		Rectangle rect = new Rectangle(x, y, width, height);
		rect = rect.intersection(getImage(band).bounds);
		int[] tile = new int[height * width];
		if (rect.isEmpty()) {
			return tile;
		}

		if (rect.y != preloadedInterval[0] || rect.y + rect.height != preloadedInterval[1]
				|| preloadedData.length < (rect.y * rect.height - 1)) {
			preloadLineTile(rect.y, rect.height, band);
		} else {
			// logger.debug("using preloaded data");
		}

		int yOffset = getImage(band).xSize;
		int xinit = rect.x - x;
		int yinit = rect.y - y;
		for (int i = 0; i < rect.height; i++) {
			for (int j = 0; j < rect.width; j++) {
				int temp = i * yOffset + j + rect.x;
				try {
					tile[(i + yinit) * width + j + xinit] = preloadedData[temp];
				} catch (ArrayIndexOutOfBoundsException e) {
					logger.warn("readTile function:" + e.getMessage());
				}
			}
		}
		return tile;
	}

	@Override
	public int readPixel(int x, int y, int band) {
		Rectangle rect = new Rectangle(x, y, 1, 1);
		rect = rect.intersection(getImage(band).bounds);
		short data[] = null;

		GDALTIFF tiff = getImage(band);
		try {
			data=tiff.readShortValues(x, y,1,1);
		} finally {
		}

		return data[0];

	}

	@Override
	public void preloadLineTile(int y, int length, int band) {
		if (y < 0) {
			return;
		}
		preloadedInterval = new int[] { y, y + length };
		Rectangle rect = new Rectangle(0, y, getImage(band).xSize, length);

		GDALTIFF tiff = getImage(band);
		rect = tiff.bounds.intersection(rect);

		try {
			short[] bi = null;
			try {
				bi = tiff.readShortValues(0, y, getImage(band).xSize, length);
			} catch (Exception e) {
				logger.warn("Problem reading image POS x:" + 0 + "  y: " + y + "   try to read again");
				try {
					Thread.sleep(100);
				} catch (InterruptedException exx) {
					Thread.currentThread().interrupt();
				}
				bi = tiff.readShortValues(0, y, getImage(band).xSize, length);
			}
			preloadedData = bi;
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		} finally {

		}

	}

	@Override
	public String getInternalImage() {
		return null;
	}

	public static void main(String args[]) {
		File f = new File(
				"F:/SumoImgs/AlosTrialTmp/SM/0000054534_001001_ALOS2049273700-150422/IMG-HH-ALOS2049273700-150422-FBDR1.5RUD");
		GDALTIFF gg = new GDALTIFF(f, 0);

		System.out.println("x:" + gg.xSize + " - y:" + gg.ySize);

	}
}
