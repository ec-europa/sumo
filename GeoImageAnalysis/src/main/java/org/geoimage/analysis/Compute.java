/*
 * 
 */
package org.geoimage.analysis;

import java.util.ArrayList;
import java.util.List;

public class Compute {
	//
    // Compute Length (m), Width (m) and Heading (deg) of target cluster
    //   Clust     Cluster obtained e.g. by ClustGrow, i.e. Clust( 1, :) are
    //             sample locations of cluster and Clust( 2, :) record locations
    //   PixszSam  Sample pixel size in m
    //   PixszRec  Record pixel size in m
    //   Tlen      Length of cluster in m
    //   Twid      Width of custer in m
    //   Thed      Heading (orientation) of cluster long axis
    // Heading is positive (anti-clockwise) wrt sample axis, with the record axis
    // at +90 deg wrt sample axis, 180 deg ambigue, between [-90..+90].
    // Calculated by least-squares fit of a line through the pixels in the cluster.
    // (Each pixel is treated as (x,y) pair, assuming errors in both x and y.)
    // Based on CFAR08.m
    //
    // See also LenWidHed.m (script)
    // (c) H. Greidanus 2004
    public static double[] fLenWidHead(List<int[]> clust, double pixszSam, double pixszRec) {
        double Tlen = 0.0;
        double Twid = 0.0;
        double Thed = 0.0;
        double aa = 0.0;
        // Make sums of pixel coordinates
        int icpt = clust.size();
        double tt[] = matlabSumRow(clust, 1);
        double Sjs = tt[0];
        double Sjr = tt[1];
        tt = matlabSumRow(clust, 2);
        double Sjs2 = tt[0];
        double Sjr2 = tt[1];
        double Sjsjr = matlabSumCross(clust);

        // Quadratic sums and cross-sum in units of meters
        double s2x = (Sjs2 - Sjs * Sjs / icpt) * pixszSam * pixszSam;
        double s2y = (Sjr2 - Sjr * Sjr / icpt) * pixszRec * pixszRec;
        double r2 = (Sjsjr - Sjs * Sjr / icpt) * pixszSam * pixszRec;

        if (r2 != 0) {
            // Elongated shape
            double QQ = (s2y - s2x) / (2 * r2);
            aa = QQ + Math.sqrt(QQ * QQ + 1);
            double a2 = QQ - Math.sqrt(QQ * QQ + 1);
            double dd = (aa * (aa * s2x - 2 * r2) + s2y) / (aa * aa + 1);
            double d2 = (a2 * (a2 * s2x - 2 * r2) + s2y) / (a2 * a2 + 1);
            if (d2 < dd) {
                aa = a2;
            }
            a2 = -1 / aa;
            // The next is to find min and max of d1 and d2 (in units of m)
            double d1mn = aa * clust.get(0)[0] * pixszSam - clust.get(0)[1] * pixszRec;
            double d1mx = d1mn;
            double d2mn = a2 * clust.get(0)[0] * pixszSam - clust.get(0)[1] * pixszRec;
            double d2mx = d2mn;
            for (int icp = 2; icp < icpt; icp++) {
                dd = aa * clust.get(icp)[0] * pixszSam - clust.get(icp)[1] * pixszRec;
                if (dd < d1mn) {
                    d1mn = dd;
                } else {
                    if (dd > d1mx) {
                        d1mx = dd;
                    }
                }
                dd = a2 * clust.get(icp)[0] * pixszSam - clust.get(icp)[1] * pixszRec;
                if (dd < d2mn) {
                    d2mn = dd;
                } else {
                    if (dd > d2mx) {
                        d2mx = dd;
                    }
                }
            }

            Twid = (d1mx - d1mn) / Math.sqrt(aa * aa + 1);
            Tlen = (d2mx - d2mn) / Math.sqrt(a2 * a2 + 1);
        } else {
            // Round shape or line on sample axis or on record axis
            double[] srmn = matlabMinRow(clust);
            double[] srmx = matlabMaxRow(clust);
            Twid = (srmx[1] - srmn[1]) * pixszRec;
            Tlen = (srmx[0] - srmn[0]) * pixszSam;
            if (s2x == s2y) {
                // Round shape
                aa = 0.0;
                Twid = (Twid + Tlen) / 2;
                Tlen = Twid;
            } else {
                aa = 0;
                // Line on sample axis
                if (s2x < s2y) {
                    // No, line on record axis
                    aa = Double.POSITIVE_INFINITY;
                    double a2 = Tlen;
                    Tlen = Twid;
                    Twid = a2;
                }
            }
        }

        // Deconvolve
        Tlen = Math.max(Tlen - Math.sqrt(pixszSam * pixszRec), 0);
        Twid = Math.max(Twid - Math.sqrt(pixszSam * pixszRec), 0);
        // Size calculated is difference between the extreme pixel centers,
        // therefore 0 if 1 pixel wide or long; give minimum size (not perfect)
        Tlen = Math.sqrt(Tlen * Tlen + pixszSam * pixszRec);
        Twid = Math.sqrt(Twid * Twid + pixszSam * pixszRec);

        // Heading in degrees [-90..90]
        Thed = 180.0 / Math.PI * Math.atan(aa);

        // calculate centre using min and max
        double[] centre = new double[2];
        centre[0] = matlabMinRow(clust)[0] + (matlabMaxRow(clust)[0] - matlabMinRow(clust)[0]) / 2;
        centre[1] = matlabMinRow(clust)[1] + (matlabMaxRow(clust)[1] - matlabMinRow(clust)[1]) / 2;

        return new double[]{icpt, centre[0], centre[1], Tlen, Twid, Thed};

    }

    // LenWidHedd Length, width, heading and centre of pixel cluster
    // 
    //    Clust  Cluster obtained e.g. by ClustGrow, i.e. Clust( 1, :) are
    //           sample locations of cluster and Clust( 2, :) record locations
    //    Pixsz  [PixszSam PixszRec] Sample, record pixel size in m
    //    Len    Length of cluster in m
    //    Wid    Width of custer in m
    //    Hed    Heading (orientation) of cluster long axis in rad
    //           heading in coordinate frame in m (different from heading in
    //           coordinate frame in pixels in case Pixsz(1)~=Pixsz(2))
    //           In case of round cluster, NaN
    //    Cen    Centre of clustre [sam; rec] in pixels (real)
    //  Heading is positive (anti-clockwise) wrt sample (x-)axis, with the record (y-)
    //  axis at +90 deg wrt sample axis, 180 deg ambiguous, between [-pi/2..+pi/2].
    //  Calculated by least-squares fit of a line through the pixels in the cluster
    //  as plotted in a frame in meters using Pixsz.
    //  (Each pixel is treated as (x,y) pair, assuming errors in both x and y.)
    //  Based on CFAR08.m
    //  Improved version of LenWidHedm; uses cluster size based on average pixel
    //  location instead of extreme pixel location, and better treatment for non-
    //  square pixels and for nearly-round clusters.
    //  Recursively discards outlying pixels from cluster.
    // 
    //  See also LenWidHedBox, LenWidHedm, fLenWidHead, LenWidHead.m
    //  (c) H. Greidanus 2008

    //  Subroutines: none
    public static double[] lenWidHedd(List<int[]> clust, double[] pixsz) {
        double Len = 0.0;
        double Wid = 0.0;
        double Hed = Double.NaN;
        double[] Cen = {Double.NaN, Double.NaN};
        double ap = 0.0;

        //  Parameter
        double fRecl = 0.6; //  # Pixels limit for outlier removal in length
        double fRecw = 0.4; //  # Pixels limit for outlier removal in width
        //  (the higher the less outliers removed)

        double Nptc = (double) clust.size(); //  # Pixels in cluster

        if (Nptc == 0) {
            //  Empty cluster
            return new double[]{Len, Wid, Hed, Cen[0], Cen[1]};
        }

        //  (Note, Clust is [x; y] for each pixel in the cluster)
        //  Sums, quadratic sums and cross-sum in units of pixels
        final  double[] sJ = matlabSumRow(clust, 1); //  Sigma( x), Sigma( y)
        final  double[] sJ2 = matlabSumRow(clust, 2); //  Sigma( x2), Sigma( y2)
        final  double sJx = matlabSumCross(clust); //  Sigma( xy)

        Cen[0] = sJ[0] / Nptc; //  [centre-x; centre-y]
        Cen[1] = sJ[1] / Nptc; //  [centre-x; centre-y]

        //System.out.println("\nCen: " + Cen[0] + " " + Cen[1] + " Nptc: " + Nptc + " Sj2: " + Sj2[0] + " " + Sj2[1] + " Sjx: " + Sjx);

        // create new cluster
        List<double[]> Clust0 = new ArrayList<double[]>();
        for (int[] element : clust) {
            Clust0.add(new double[]{(double) element[0] - Cen[0], (double) element[1] - Cen[1]}); //  Cluster pixels wrt centre
        }
        //  Stdev and correlation in units of meters
        final double s2x = (sJ2[0] - sJ[0] * sJ[0] / Nptc) * pixsz[0] * pixsz[0]; //  Sigma( x2)- Sigma( x)^2/ N
        final double s2y = (sJ2[1] - sJ[1] * sJ[1] / Nptc) * pixsz[1] * pixsz[1]; //  Sigma( y2)- Sigma( y)^2/ N
        final double r2 = (sJx - sJ[0] * sJ[1] / Nptc) * pixsz[0] * pixsz[1]; //  Sigma( xy)- Sigma( x).Sigma( y)/ N
        //System.out.println("Cen: " + Cen[0] + " " + Cen[1] + " r2 " + r2 + " s2x " + s2x + " s2y " + s2y);

        if ((Math.abs(r2) / (pixsz[0] * pixsz[1])) > 1 / 40.0) // (if r2~= 0)
        {
            //  Elongated shape (as seen in meters frame)
            double QQ = (s2y - s2x) / (2.0 * r2);
            ap = QQ + Math.sqrt(QQ * QQ + 1.0); //  RC of best fitting line (in meters coord frame)
            //  RC of line perpendicular to best fitting line:
            double a2 = QQ - Math.sqrt(QQ * QQ + 1.0);
            //  (However, ap and a2 may be interchanged, not known here)
            double w1 = ap * ap + 1.0;
            double w2 = a2 * a2 + 1.0;
            //  Distances of all cluster pixels to best fitting line (in meters)
            //  times sqrt( w1):
            double[] d1 = matlabSumProdRegmat2D(new double[]{ap * pixsz[0], -1.0 * pixsz[1]}, Clust0);
            //  Average (absolute) distance (m) of cluster pixels to line (times 2):
            double ma1 = 2.0 * matlabMeanAbs1D(d1) / Math.sqrt(w1);
            //  One pixel distance along direction ap is dl1 meters:
            double dl1 = Math.sqrt((pixsz[0] * pixsz[0] + ap * ap * pixsz[1] * pixsz[1]) / w1);
            // Wid= max( ma1+ realsqrt( 0.5* dl1* dl1+ ma1* ma1)- dl1, dl1); //  Width (meters)
            // Wid= max( ma1+ realsqrt( 0.5* dl1* dl1+ ma1* ma1), dl1); //  Width (meters)
            //  (The above formula approximates the relation between "ma1" and "Wid"
            //  for a rectangular noise-free cluster, width an accuracy of +/- 0.5
            //  pixel and a minimum value of 1 pixel for an unresolved target.)
            Wid = Math.max(2.0 * ma1, dl1); //  Width (meters)
            //  Same for perpendicular line:
            double[] d2 = matlabSumProdRegmat2D(new double[]{a2 * pixsz[0], -1.0 * pixsz[1]}, Clust0);
            double ma2 = 2 * matlabMeanAbs1D(d2) / Math.sqrt(w2);
            double dl2 = Math.sqrt((pixsz[0] * pixsz[0] + a2 * a2 * pixsz[1] * pixsz[1]) / w2);
            // Len= max( ma2+ realsqrt( 0.5* dl2* dl2+ ma2* ma2)- dl2, dl2); //  Length (meters)
            // Len= max( ma2+ realsqrt( 0.5* dl2* dl2+ ma2* ma2), dl2); //  Length (meters)
            Len = Math.max(2.0 * ma2, dl2); //  Length (meters)
            //  At this point, it is still not know which of Len and Wid is actually
            //  the longer or shorter dimension
            //System.out.println(" Len: " + Len + " Wid: " + Wid + " ma1 " + ma1 + " dl1 " + dl1 + " ma2 " + ma2 + " dl2 " + dl2 + " ap " + ap + " a2 " + a2);

            //  Which points are outliers (further than fRec pixels out of box):
            List<int[]> clust1 = new ArrayList<int[]>();
            // find( abs( d1)/ realsqrt( w1)- Wid/ 2> fRecw* dl1 | abs( d2)/ realsqrt( w2)- Len/ 2> fRecl* dl2);
            for (int i = 0; i < d1.length; i++) {
                if ((Math.abs(d1[i]) / Math.sqrt(w1) - Wid / 2.0 > fRecw * dl1) || (Math.abs(d2[i]) / Math.sqrt(w2) - Len / 2.0 > fRecl * dl2)) {
                    //  Remove outliers and call recursively
                } else {
                    clust1.add(clust.get(i));
                }
            }

            //System.out.println("Clust size : " + Clust.size() + " clust1 size: " + clust1.size());
            if (clust1.size() < clust.size()) {
                //System.out.println(clust1.size());
                double[] result = lenWidHedd(clust1, pixsz);
                return result;
            }

            //  Change length and width if needed to get length as the longest (in m):
            if (Len < Wid) {
                double tmp = Len;
                Len = Wid;
                Wid = tmp;
                ap = a2;
            }
        } else {
            //  Round shape or line on sample axis or on record axis
            //System.out.println("Round shape or line on sample axis or on record axis Cen: " + Cen[0] + " " + Cen[1] + " Len: " + Len + " Wid: " + Wid);

            //  Average (absolute) distance of cluster pixels [to x-axis; to y-axis]
            //  (times 2) (in units of pixels):
            double[] ma = matlabMeanAbs2D(Clust0);
            ma[0] = ma[0] * 2.0;
            ma[1] = ma[1] * 2.0;
            //  Cluster size along [x-axis; y-axis] (meters):
            // Siz= max( ma+ realsqrt( 0.5+ ma.* ma)- 1, 1).* Pixsz;
            double[] Siz = new double[]{Math.max(ma[0] + Math.sqrt(0.5 + ma[0] * ma[0]), 1) * pixsz[0], Math.max(ma[1] + Math.sqrt(0.5 + ma[1] * ma[1]), 1) * pixsz[1]};
            if (Math.abs(Math.sqrt(s2x / Nptc) - Math.sqrt(s2y / Nptc)) < (Math.max(pixsz[0], pixsz[1]) / 40.0)) //  (if s2x== s2y)
            {
                //  Round shape
                Len = (Siz[0] + Siz[1]) / 2.0;
                Wid = Len;
                ap = 0.0;
            } else {
                if (s2x > s2y) {
                    //  Line on sample axis
                    Len = Siz[0];
                    Wid = Siz[1];
                    ap = 0.0;
                } else {
                    //  Line on record axis
                    Len = Siz[1];
                    Wid = Siz[0];
                    ap = Double.POSITIVE_INFINITY;
                }
            }
        }

        Hed = Math.atan(ap); //  Heading in rad [-pi/2..pi/2]

        return new double[]{clust.size(), Cen[0], Cen[1], Len, Wid, Math.toDegrees(Hed)};
    }
    
    // return the smallest x and the smallest y in the Vector
    public static double[] matlabMinRow(List<int[]> table) {
        double[] result = {0.0, 0.0};
        if (table.size() > 0) {
            result[0] = table.get(0)[0];
            result[1] = table.get(0)[1];
            for (int i = 1; i < table.size(); i++) {
                if (result[0] > (double) table.get(i)[0]) {
                    result[0] = (double) table.get(i)[0];
                }
                if (result[1] > (double) table.get(i)[1]) {
                    result[1] = (double) table.get(i)[1];
                }
            }
        }
        return result;
    }
    
    // return the largest x and the largest y in the Vector
    public static double[] matlabMaxRow(List<int[]> table) {
        double[] result = {0.0, 0.0};
        if (table.size() > 0) {
            result[0] = table.get(0)[0];
            result[1] = table.get(0)[1];
            for (int i = 1; i < table.size(); i++) {
                if (result[0] < (double) table.get(i)[0]) {
                    result[0] = (double) table.get(i)[0];
                }
                if (result[1] < (double) table.get(i)[1]) {
                    result[1] = (double) table.get(i)[1];
                }
            }
        }
        return result;
    }

    public static double[] matlabSumRow(List<int[]> table, int power) {
        double[] result = {0.0, 0.0};
        for (int i = 0; i < table.size(); i++) {
            result[0] = result[0] + Math.pow((double) table.get(i)[0], power);
            result[1] = result[1] + Math.pow((double) table.get(i)[1], power);
        }
        return result;
    }

    public static double matlabSumCross(List<int[]> table) {
        double result = 0.0;
        for (int i = 0; i < table.size(); i++) {
            result = result + (double) table.get(i)[0] * (double) table.get(i)[1];
        }
        return result;
    }

    public static double[] matlabSumProdRegmat2D(double regmat[], List<double[]> table) {
        double[] result = new double[table.size()];
        for (int i = 0; i < table.size(); i++) {
            result[i] = regmat[0] * (double) table.get(i)[0] + regmat[1] * (double) table.get(i)[1];
        }
        return result;
    }

    public static double[] matlabMeanAbs2D(List<double[]> table) {
        double[] result = new double[]{0.0, 0.0};
        for (int i = 0; i < table.size(); i++) {
            result[0] = result[0] + (double) Math.abs(table.get(i)[0]);
            result[1] = result[1] + (double) Math.abs(table.get(i)[1]);
        }
        // calculate mean of the values
        result[0] = result[0] / table.size();
        result[1] = result[1] / table.size();

        return result;
    }

    public static double matlabMeanAbs1D(double[] table) {
        double result = 0.0;
        for (int i = 0; i < table.length; i++) {
            result += (double) Math.abs(table[i]);
        }
        // calculate mean of the values
        result = result / table.length;

        return result;
    }
}
