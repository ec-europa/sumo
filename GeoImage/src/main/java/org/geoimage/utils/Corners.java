/*
 * 
 */
package org.geoimage.utils;

import org.geoimage.impl.Gcp;

public class Corners {
	 
	private Gcp topLeft;
	private Gcp topRight;
	private Gcp bottomLeft;
	private Gcp bottomRight;
	
	
	public Gcp getTopLeft() {
		return topLeft;
	}
	public void setTopLeft(Gcp topLeft) {
		this.topLeft = topLeft;
	}
	public Gcp getTopRight() {
		return topRight;
	}
	public void setTopRight(Gcp topRight) {
		this.topRight = topRight;
	}
	public Gcp getBottomLeft() {
		return bottomLeft;
	}
	public void setBottomLeft(Gcp bottomLeft) {
		this.bottomLeft = bottomLeft;
	}
	public Gcp getBottomRight() {
		return bottomRight;
	}
	public void setBottomRight(Gcp bottomRight) {
		this.bottomRight = bottomRight;
	}
	
	
	
}
