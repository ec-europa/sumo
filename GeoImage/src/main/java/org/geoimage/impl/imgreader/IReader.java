/*
 * 
 */
package org.geoimage.impl.imgreader;

import java.awt.Rectangle;
import java.io.File;

public interface IReader {

	int getxSize();

	void setxSize(int xSize);

	int getySize();

	void setySize(int ySize);

	Rectangle getBounds();

	void setBounds(Rectangle bounds);

	void refreshBounds();

	void dispose();

	File getImageFile();

	void setImageFile(File imageFile);
	
	
	

}