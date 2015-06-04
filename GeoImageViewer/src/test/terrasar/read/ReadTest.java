package terrasar.read;


import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;
import org.geoimage.analysis.BlackBorderAnalysis;
import org.geoimage.def.GeoImageReader;
import org.geoimage.impl.tsar.TerrasarXImage;
import org.geoimage.impl.tsar.TerrasarXImage_SLC;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.slf4j.LoggerFactory;


public class ReadTest {
	private static final String file="G:\\sat\\TSX1_SAR__SSC______SM_S_SRA_20130311T070032_20130311T070040\\TSX1_SAR__SSC______SM_S_SRA_20130311T070032_20130311T070040.xml";
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(ReadTest.class);

	byte[]preloadedDataSLC;
	private int xOffset = 8;
	GeoImageReader reader;
	Map<?, ?> image=null;
	Document doc;

	public ReadTest() {
		reader=new TerrasarXImage_SLC(new File(file));
		reader.initialise();
		parseProductXML(new File(file));
		image=getImages(new File(file));
	}

	 //set all the metadata available in the xml file
    public void parseProductXML(File productxml)  {
        try {
            SAXBuilder builder = new SAXBuilder();
            doc = builder.build(productxml);
            Element atts = doc.getRootElement().getChild("productInfo");

        } catch (JDOMException|IOException ex) {
        	logger.error(ex.getMessage(),ex);
        }
    }

	private Map<String, File> getImages(File productxml) {
        List elements = doc.getRootElement().getChild("productComponents").getChildren("imageData");
        Map<String, File> tiffs = new HashMap<String, File>();
        for (Object o : elements) {
            if (o instanceof Element) {
                File f = new File(productxml.getParent()+"\\"+((Element) o).getChild("file").getChild("location").getChild("path").getText()+"\\"+((Element) o).getChild("file").getChild("location").getChild("filename").getText());
                String polarisation = ((Element) o).getChild("polLayer").getValue();
                tiffs.put(polarisation, f);
                //tiffs.put(polarisation, new TIFF(f));
                //bands.add(polarisation);
            }
        }
        return tiffs;
    }
		@SuppressWarnings("unused")
		public void loadWithJavaFile() {
			int x=reader.getWidth() / 2 - 100;
			int y=reader.getHeight() / 2 - 100;
			int width=200;
			int height=200;
			int length=200;

			int xSize=reader.getWidth();

			int[] preloadedInterval = new int[]{y, y + length};
	        //y+4 skips the first 4 lines of offset
	        int tileOffset = (y+4) * (xSize * 4 + xOffset);

	        //preloadedDataSLC = new short[(xSize * 4 + xOffset) * length];
	        try {
	        	File f=(File)image.get("HH");
	        	RandomAccessFile fss = new RandomAccessFile(f.getAbsolutePath(), "r");

	        	int flen=((int)fss.length());

	            preloadedDataSLC = new byte[reader.getWidth()*reader.getHeight()];
	            //fss.seek(tileOffset);
	            /*int p=0;
	            for(int position=0;position<flen-16;position=position+16){
	            	fss.seek(position);
	            	p++;
	            	preloadedDataSLC[p]=fss.readShort();
	            }*/

	            fss.read(preloadedDataSLC);
	            fss.close();
	        } catch (Exception ex) {
	        	logger.error(ex.getMessage(),ex);
	        }

	        /*gdal.AllRegister();
	        Dataset dataset = gdal.OpenShared(files[0], gdalconstConstants.GA_ReadOnly);
	        Band band = dataset.GetRasterBand(1);
	        int xSize = band.getXSize();
	        int ySize = band.getYSize();
	        short[] realArray = new short[xSize * ySize];
	        short[] imgArray = new short[xSize * ySize];
	        if (band.getDataType() == gdal.GetDataTypeByName("GDT_CInt16"))
	        {
	            short[] tmpArray = new short[2 * xSize * ySize];
	            band.ReadRaster(0,0,o.getXSize(),o.getYSize(), tmpArray);
	            for (int i = 0; i < tmpArray.length;i++ )
	            {
	                realArray[i] = tmpArray[i / 2];
	                imgArray[i] = tmpArray[i / 2 + 1];
	            }
	            tmpArray = null;
	        }*/

		}

	    public void loadWithGdal() {
	        //gdal.SetConfigOption("GDAL_DATA", "E:/SUMO/workspace/Sumo/trunk/GeoImageViewer/lib/gdal_libl/");
	        /*SpatialReference sptRef=new SpatialReference(osr.SRS_WKT_WGS84);
	        SpatialReference layerProjection = new SpatialReference();
	        SpatialReference hLatLong = new SpatialReference(osr.SRS_WKT_WGS84);*/
	    	File f=(File)image.get("HH");
	        Dataset dataset=gdal.Open(f.getAbsolutePath() , gdalconstConstants.GA_ReadOnly);
	        Band o=(Band)dataset.GetRasterBand(1);

	        long flen=o.getXSize()*o.getYSize();//fss.length();

            byte[] raster=new byte[(o.getXSize()*o.getYSize())];
	        o.ReadRaster(0,0,o.getXSize(),o.getYSize(), raster);
	        byte[] preloadedDataSLC=raster;

	    }



	    public static void main(String[]args){
	    	ReadTest rTest=new ReadTest();
	    	rTest.loadWithJavaFile();
	    	//rTest.loadWithGdal();

	    }

}