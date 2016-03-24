/*
 * 
 */
package org.geoimage.impl;

/**
 * A simple Bean class to register gcps. the Angle and the Zgeo coordinate (altitude) are facultative
 * @author thoorfr
 */
public class Gcp {

    private float angle;

    private double xpix;

    private double ypix;

    private double xgeo;

    private double ygeo;

    private Double originalXpix;
    

	private double zgeo;

    public Gcp(double xpix, double ypix, double xgeo, double ygeo){
        this.xgeo=xgeo;
        this.xpix=xpix;
        this.ygeo=ygeo;
        this.ypix=ypix;
    }

    public Gcp(){
        
    }

    public double getXgeo () {
        return xgeo;
    }

    public void setXgeo (double val) {
        this.xgeo = val;
    }

    public float getAngle () {
        return angle;
    }

    public void setAngle (float val) {
        this.angle = val;
    }

    public double getXpix () {
        return xpix;
    }

    public void setXpix (double val) {
        this.xpix = val;
    }

    public double getYgeo () {
        return ygeo;
    }

    public void setYgeo (double val) {
        this.ygeo = val;
    }

    public double getYpix () {
        return ypix;
    }

    public void setYpix (double val) {
        this.ypix = val;
    }

    public double getZgeo () {
        return zgeo;
    }

    public void setZgeo (double val) {
        this.zgeo = val;
    }
    
    public Double getOriginalXpix() {
		return originalXpix;
	}

	public void setOriginalXpix(Double originalXpix) {
		this.originalXpix = originalXpix;
	}

}

