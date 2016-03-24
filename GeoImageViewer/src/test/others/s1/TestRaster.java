/*
 * 
 */
package others.s1;


import java.awt.Rectangle;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;



public class TestRaster {
	final static String file="C://tmp//sumo_images//S1//IW//S1A_IW_SLC__1SDH_20140502T170314_20140502T170344_000421_0004CC_1A90.SAFE//measurement//s1a-iw1-slc-hh-20140502t170314-20140502t170342-000421-0004cc-001.tiff";
	
	public static double[] readSLCRasterBand(int x, int y, int width, int height) throws IOException {
		
		final double[] srcArray;
		
		final ImageReader reader = ImageIO.getImageReadersByFormatName("tiff").next();
		ImageInputStream iis = ImageIO.createImageInputStream(file);
		reader.setInput(iis);
		
		final ImageReadParam param = reader.getDefaultReadParam();
		param.setSourceSubsampling(width,height,0,0);

		final RenderedImage image = reader.readAsRenderedImage(0,param);
		
		
		final Raster data = image.getData(new Rectangle(0,0, width, height));

		final SampleModel sampleModel = data.getSampleModel();

		srcArray = new double[width * height];
		double[] samples = sampleModel.getSamples(0, 0, width, height, 0, srcArray,data.getDataBuffer());
		
		/*if (oneOfTwo)
			copyLine1Of2(srcArray, (short[]) destBuffer.getElems(), sourceStepX);
		else
			copyLine2Of2(srcArray, (short[]) destBuffer.getElems(), sourceStepX);*/
		
		return samples;
	}

	public static void copyLine1Of2(final double[] srcArray,
			final short[] destArray, final int sourceStepX) {
		final int length = srcArray.length;
		for (int i = 0; i < length; i += sourceStepX) {
			destArray[i] = (short) srcArray[i];
		}
	}

	public static void copyLine2Of2(final double[] srcArray,
			final short[] destArray, final int sourceStepX) {
		final int length = srcArray.length;
		for (int i = 0; i < length; i += sourceStepX) {
			destArray[i] = (short) ((int) srcArray[i] >> 16);
		}
	}
	
	
	public static void main(String[] args){
		try {
			double[] samples=TestRaster.readSLCRasterBand(0,0,16,16);
			System.out.println(samples);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
