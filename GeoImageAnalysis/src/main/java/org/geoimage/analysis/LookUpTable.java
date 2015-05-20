/*****************************************************************************
 *                        SUMO Copyright (c) 2000
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 * Guillermo.Schwartz@jrc.it
 * Guillermo.Schwartz@
 ****************************************************************************/
package org.geoimage.analysis;

/**
 * 
 * @author thoorfr, lemoigu
 * 
 */
import java.sql.*;
import java.io.*;

import org.slf4j.LoggerFactory;

/** the client to access to look up tables through a web-service */
public class LookUpTable {

    /** Creates a new instance of PGLUTClient */
    // the look-up table to get the clipping thresh (0) or the detection thresh (1) from the evaluated clipped standard deviation
    protected double[][] clippedStandardDeviation2Threshs = null;
    protected final int CLIPPINGTRESH = 0;
    protected final int DETECTIONTRESH = 1;

    // the look-up table to get the clipping thresh from the evaluated unclip standard deviation
    /** the table giving the clipping thresh from non clipped standard deviation */
    protected double[] standardDeviation2ClippingThresh = null;

    // number of input elements for each table
    private int numberOfElements = 0;

    // util
    private double[] std = new double[2];
    private double[] clippedStd = new double[2];
    private double step = 0;
    private double clippedStep = 0;
    private double pcl = 0;
    private double nnu = 0;
    static String dbname = "algorithm_data";
    static String dbuser = "vms-vds-user";
    static String dbpass = "Vms-vds-user1";
    static String dbhost = "localhost";
    static String dbport = "5432";

    
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(LookUpTable.class);

    
    // The binary LUT
    private byte[] lut = null;

    // CONSTRUCTOR
    public LookUpTable(InputStream is) {
        try {
            getFileLUT(is);
            parseLUT();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public LookUpTable(){
        
    }

    public void initConnection(String dbName, String dbUser, String dbPass,
            String dbHost, String dbPort) {
        dbname = dbName;
        dbuser = dbUser;
        dbpass = dbPass;
        dbhost = dbHost;
        dbport = dbPort;
    }

    public double getPcl() {
        return pcl;
    }

    public double getNnu() {
        return nnu;
    }

    public double[] getStd() {
        return std;
    }

    public double[] getClippedStd() {
        return clippedStd;
    }

    public double[][] getClippedStandardDeviation2Threshs() {
        return clippedStandardDeviation2Threshs;
    }

    public double[] getStandardDeviation2ClippingThresh() {
        return standardDeviation2ClippingThresh;
    }

    public double getStep() {
        return step;
    }

    public double getClippedStep() {
        return clippedStep;
    }

    public void getDatabaseLUT(double enl, int pfm, int pfn) {
        Connection conn;

        System.out.println("Using data base: " + dbname);

        try {
            String url = "jdbc:postgresql://" + dbhost + ":" + dbport + "/" + dbname;
            conn = DriverManager.getConnection(url, dbuser, dbpass);
            StringBuffer qryString = new StringBuffer(
                    "select lut from kdist_luts where enl = ? and pfm = ? and pfn = ?");
            PreparedStatement p = conn.prepareStatement(qryString.toString());

            p.setDouble(1, enl);
            p.setInt(2, pfm);
            p.setInt(3, pfn);
            System.out.println(enl);
            ResultSet r = p.executeQuery();
            r.next();
            lut = r.getBytes(1);
            p.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getLUT(double enl, int pfm, int pfn) {
        try {
            getDatabaseLUT(enl, pfm, pfn);
        } catch (Exception ex) {
            System.out.println("cannot access to database...");
            this.lut = null;
        }
        parseLUT();
        return;
    }

    public void getFileLUT(InputStream is) {
        try {

            DataInputStream lutFile = new DataInputStream(is);
            this.lut = new byte[lutFile.available()];
            lutFile.readFully(this.lut);

        } catch (IOException ex) {
        	logger.error(ex.getMessage(),ex);
        }
    }

    public void parseLUT() {
        try {
            DataInputStream lutdis = new DataInputStream(new ByteArrayInputStream(lut));
            numberOfElements = (int) lutdis.readDouble();
            pcl = lutdis.readDouble();
            nnu = lutdis.readDouble();
            std[0] = lutdis.readDouble();
            std[1] = lutdis.readDouble();
            standardDeviation2ClippingThresh = new double[numberOfElements];
            clippedStandardDeviation2Threshs = new double[2][numberOfElements];
            for (int i = 0; i < numberOfElements; i++) {
                standardDeviation2ClippingThresh[i] = lutdis.readDouble();
            }
            clippedStd[0] = lutdis.readDouble();
            clippedStd[1] = lutdis.readDouble();
            for (int i = 0; i < numberOfElements; i++) {
                clippedStandardDeviation2Threshs[CLIPPINGTRESH][i] = lutdis.readDouble();
            }
            for (int i = 0; i < numberOfElements; i++) {
                clippedStandardDeviation2Threshs[DETECTIONTRESH][i] = lutdis.readDouble();
            }
        } catch (java.io.IOException e) {
            System.out.println("Cannot read the tables");
        }

        this.step = (numberOfElements - 1) / (std[1] - std[0]);
        this.clippedStep = (numberOfElements - 1) / (clippedStd[1] - clippedStd[0]);

        return;
    }

    // public method to access to the look up tables by linear interpolation
    public double getClippingThreshFromStd(double sigma) {
        int n = (int) ((sigma - std[0]) * this.step);
        if (n < 0) {
            return this.standardDeviation2ClippingThresh[0];
        }
        if (n < this.standardDeviation2ClippingThresh.length - 1) {
            return this.standardDeviation2ClippingThresh[n] + (sigma - n) * (standardDeviation2ClippingThresh[n + 1] - standardDeviation2ClippingThresh[n]);
        } else {
            // System.out.println("WARNING: n="+n+" out of bound");
            double x0 = this.standardDeviation2ClippingThresh[this.standardDeviation2ClippingThresh.length - 1];
            double x1 = this.standardDeviation2ClippingThresh[this.standardDeviation2ClippingThresh.length - 2];
            return x0 + (n - this.standardDeviation2ClippingThresh.length - 1) * (x0 - x1);
        }
    }

    public double getClippingThreshFromClippedStd(double sigma) {
        int n = (int) ((sigma - this.clippedStd[0]) * this.clippedStep);
        // System.out.println("n: "+n);
        if (n < 0) {
            return this.clippedStandardDeviation2Threshs[this.CLIPPINGTRESH][0];
        }
        if (n < this.clippedStandardDeviation2Threshs[this.CLIPPINGTRESH].length - 1) {
            return this.clippedStandardDeviation2Threshs[this.CLIPPINGTRESH][n] + (sigma - n) * (clippedStandardDeviation2Threshs[this.CLIPPINGTRESH][n + 1] - clippedStandardDeviation2Threshs[this.CLIPPINGTRESH][n]);
        } else {
            double x0 = this.clippedStandardDeviation2Threshs[this.CLIPPINGTRESH][this.clippedStandardDeviation2Threshs[this.CLIPPINGTRESH].length - 1];
            double x1 = this.clippedStandardDeviation2Threshs[this.CLIPPINGTRESH][this.clippedStandardDeviation2Threshs[this.CLIPPINGTRESH].length - 2];
            // System.out.println("WARNING: n="+n+" out of bound");
            return x0 + (n - this.clippedStandardDeviation2Threshs[this.CLIPPINGTRESH].length - 1) * (x0 - x1);
        }
    }

    public double getDetectThreshFromClippedStd(double sigma) {
        int n = (int) ((sigma - this.clippedStd[0]) * this.clippedStep);
        if (n < 0) {
            return this.clippedStandardDeviation2Threshs[this.DETECTIONTRESH][0];
        }
        if (n < this.clippedStandardDeviation2Threshs[this.DETECTIONTRESH].length - 1) {
            return clippedStandardDeviation2Threshs[this.DETECTIONTRESH][n] + (sigma - n) * (clippedStandardDeviation2Threshs[this.DETECTIONTRESH][n + 1] - clippedStandardDeviation2Threshs[this.DETECTIONTRESH][n]);
        } else {
            // System.out.println("WARNING: n="+n+" out of bound for detecting
            // tresh");
            double x0 = this.clippedStandardDeviation2Threshs[this.DETECTIONTRESH][this.clippedStandardDeviation2Threshs[this.DETECTIONTRESH].length - 1];
            double x1 = this.clippedStandardDeviation2Threshs[this.DETECTIONTRESH][this.clippedStandardDeviation2Threshs[this.DETECTIONTRESH].length - 2];
            return x0 + (n - this.clippedStandardDeviation2Threshs[this.DETECTIONTRESH].length - 1) * (x0 - x1);
        }
    }
}
