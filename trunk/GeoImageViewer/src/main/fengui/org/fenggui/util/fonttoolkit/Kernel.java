/*
 * FengGUI - Java GUIs in OpenGL (http://www.fenggui.org)
 * 
 * Copyright (c) 2005, 2006 FengGUI Project
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details:
 * http://www.gnu.org/copyleft/lesser.html#TOC3
 * 
 * Created on Jan 31, 2006
 * $Id: Kernel.java 28 2006-10-05 01:37:07Z schabby $
 */
package org.fenggui.util.fonttoolkit;

/**
 * A kernel is pretty much a 2D double array which values are used to
 * perform a convolution.
 * 
 * @author Johannes Schaback, last edited by $Author: schabby $, $Date: 2006-10-05 03:37:07 +0200 (Thu, 05 Oct 2006) $
 * @version $Revision: 28 $
 */
public class Kernel {

	private double[][] kernel = null;
	
	public Kernel(double[][] kernel) {
		this.kernel = kernel;
	}
	
	public Kernel(int size) {
		this.kernel = new double[size][size];
	}
	
	/**
	 * Creates a Kernel in which the values sum to one.
	 * @param size the square root of the size of the kernel (size x size)
	 * @return new kernel
	 */
	public static Kernel createDefaultKernel(int size) {
		double[][] array = new double[size][size];
		
		double value = 1.0/Math.pow(size, 2);
		
		for(int x = 0;x<size;x++) {
			for(int y = 0;y<size;y++) {
				array[x][y] = value;
			}
		}
		
		return new Kernel(array);
	}

	public static Kernel createCircularDilationKernel(int radius) {
		double[][] array = new double[radius*2+1][radius*2+1];
		
		for(int x = 0;x<array.length;x++) {
			for(int y = 0;y<array[0].length;y++) {
				if(Math.sqrt(Math.pow(radius-x, 2)+Math.pow(radius-y, 2))<= radius) {
					array[x][y] = 1;
				}
			}
		}
		
		return new Kernel(array);
	}

	
	public static Kernel createDilationKernel(int size) {
		double[][] array = new double[size][size];
		
		for(int x = 0;x<size;x++) {
			for(int y = 0;y<size;y++) {
				array[x][y] = 1;
			}
		}
		
		return new Kernel(array);
	}
	
	/**
	 * Creates a kernel with a Gaussian distribution.
	 * @param size the size of the kernel
	 * @param delta the delta value
	 * @return new kernel
	 */
	public static Kernel createGaussianKernel(int size, double delta) {
		double[][] array = new double[size][size];

		for(int x = 0;x<size;x++) {
			for(int y = 0;y<size;y++) {
				array[x][y] = gaussian(size/2-x, size/2-y, delta);
			}
		}
		
		return new Kernel(array);
	}
	
    private static double gaussian(int x, int y, double delta){
        
        double d = 1.0/(2.0*Math.PI*Math.pow(delta, 2));
        
        double exponent = (Math.pow(x, 2)+Math.pow(y, 2))/(2.0*Math.pow(delta, 2));
        
        //double d1 = Math.pow(Math.E, -((double)(x*x)+(double)(y*y))/(2.0*delta*delta));
        double d1 = Math.pow(Math.E, -exponent);
        return d*d1;
        
    }
    
    public double getValue(int x, int y) {
    	return kernel[x][y];
    }
    
    public int getWidth() {
    	return kernel.length;
    }
    
    public int getHeight() {
    	return kernel[0].length;
    }
    
    public String toString() {
    	String s = "";
    	
    	for(int y=0; y<getHeight(); y++) {
    		for(int x=0; x<getWidth(); x++) {
    			s += kernel[x][y]+" ";
    		}
    		s += '\n';
    	}
    	
    	return s;
    }
    
    public Kernel multiply(double value) {
    	for(int y=0; y<getHeight(); y++) {
    		for(int x=0; x<getWidth(); x++) {
    			kernel[x][y] *= value;
    		}
    	}
    	return this;
    }
    
    public Kernel add(double value) {
    	for(int y=0; y<getHeight(); y++) {
    		for(int x=0; x<getWidth(); x++) {
    			kernel[x][y] += value;
    		}
    	}
    	return this;
    }    

    public Kernel add(Kernel k) {
    	for(int y=0; y<getHeight(); y++) {
    		for(int x=0; x<getWidth(); x++) {
    			kernel[x][y] += k.getValue(x, y);
    		}
    	}
    	return this;
    }        
}
