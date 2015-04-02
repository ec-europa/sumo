package org.geoimage.viewer.java2d.util;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.slf4j.LoggerFactory;


public class ScaleTransformation {
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(ScaleTransformation.class);
	
	public ScaleTransformation(){
	}
	
	
	public static BufferedImage scale(File image,int scale){
		BufferedImage temp=null;
		try {
            temp= ImageIO.read(image);
            double scaleFactor = computeScale(temp.getWidth(), temp.getHeight(), scale, scale);
            AffineTransformOp op = new AffineTransformOp(AffineTransform.getScaleInstance(scaleFactor,scaleFactor), AffineTransformOp.TYPE_NEAREST_NEIGHBOR);//TYPE_BILINEAR);
            temp = op.filter(temp, null);
            
        } catch (IOException ex) {
            logger.error(ex.getMessage(),ex);
        }
		return temp;
	}
	
	
	/**
	 * Calcola il rapporto di scala sulla base della dimensione maggiore (tenuto conto
	 * del rapporto finale desiderato).
	 * Il fattore di scala restituito non sar� comunque superiore ad 1.
	 * @param width Dimensione attuale dell'immagine
	 * @param height Dimensione attuale dell'immagine
	 * @param finalWidth Dimensione finale dell'immagine
	 * @param finalHeight Dimensione finale dell'immagine
	 * @return Il fattore di scala da applicare all'immagine
	 */
	private static double computeScale(int width, int height, int finalWidth, int finalHeight){
		double scale;
		if(((double) width / (double) height) >= ((double) finalWidth / (double) finalHeight)){
			scale = (double) finalWidth / width;
		} else {
			scale = (double) finalHeight / height;
		}
		if(scale > 1) {
			scale = 1;
		}
		return scale;
	}
	
}
