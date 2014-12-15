/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.core.io;

import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.RescaleOp;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import org.geoimage.def.GeoImageReader;
import org.geoimage.def.GeoTransform;
import org.geoimage.viewer.core.api.Attributes;
import org.geoimage.viewer.core.api.GeometricLayer;

import com.keithpower.gekmlib.Document;
import com.keithpower.gekmlib.Folder;
import com.keithpower.gekmlib.GroundOverlay;
import com.keithpower.gekmlib.Icon;
import com.keithpower.gekmlib.IconStyle;
import com.keithpower.gekmlib.Kml;
import com.keithpower.gekmlib.LatLonBox;
import com.keithpower.gekmlib.LineStyle;
import com.keithpower.gekmlib.LinearRing;
import com.keithpower.gekmlib.MultiGeometry;
import com.keithpower.gekmlib.Placemark;
import com.keithpower.gekmlib.Point;
import com.keithpower.gekmlib.PolyStyle;
import com.keithpower.gekmlib.Polygon;
import com.keithpower.gekmlib.Style;
import com.keithpower.gekmlib.TimeStamp;
import com.keithpower.gekmlib.outerBoundaryIs;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/**
 *
 * @author thoorfr+AG
 *
 * this class manages the Google kml files. (note: there is a commented code inside which permits to have thumbnails of VDS in the result.
 * However, it is necessary to better implement the thumbnails orientation to take into account of the satellite orbit and looking direction).
 *
 */
public class KmlIO extends AbstractVectorIO {
	
    public static String CONFIG_FILE = "file";

    public KmlIO(){
    } 
    
    public GeometricLayer read(GeoImageReader gir) {
        return null;
    }

    public void save(GeometricLayer glayer, String projection,GeoImageReader gir) {
    	GeoTransform gt=gir.getGeoTransform();
        boolean toFlip = false;
        int t = 0; //thumbnails id name
        try {
            String file = ((String) config.get(CONFIG_FILE)).replace('\\', '/');

            if (!glayer.getGeometryType().equals(GeometricLayer.POINT)) {
                return;
            }

            if (projection == null) {
                //better to return a error message
                return;
            }
            //name of the temporary kml file
            String tempFile = file.replace("kmz", "kml");

            //creation of the kmz file
            byte[] buffer = new byte[1024];
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file));

            Kml kml = new Kml();
            Document doc = new Document();
            kml.addDocument(doc);
            //create the main folder
            Folder rootFolder = new Folder();
            rootFolder.setName("VDS");
            doc.addFolder(rootFolder);
            TimeStamp time = new TimeStamp();
            time.setWhen(gir.getMetadata(GeoImageReader.TIMESTAMP_START).toString().replace(" ", "T") + "Z");
            rootFolder.addTimeStamp(time);

            //extraction of the corners of the image
            double[] a = gt.getGeoFromPixel((double) 0, (double) 0, "EPSG:4326");
            double[] b = gt.getGeoFromPixel((double) gir.getWidth(), (double) 0, "EPSG:4326");
            double[] c = gt.getGeoFromPixel((double) gir.getWidth(), (double) gir.getHeight(), "EPSG:4326");
            double[] d = gt.getGeoFromPixel((double) 0, (double) gir.getHeight(), "EPSG:4326");

            //create folder for the image border
            Folder border = new Folder();
            border.setName("Image");
            rootFolder.addFolder(border);
            //add the border
            Placemark pm = new Placemark();
            pm.setName("Outline");
            Style st = new Style();
            PolyStyle pst = new PolyStyle();
            //pst.setColor("36ff0000");//set the transparency to 30% and color to blue
            pst.setFill(false);
            pst.setOutline(true);
            LineStyle lst = new LineStyle(); //set the style of the border            
            lst.setWidth(2);
            st.addLineStyle(lst);
            st.addPolyStyle(pst);
            pm.addStyle(st);
            MultiGeometry mg = new MultiGeometry();//create the border line
            Polygon poly = new Polygon();
            outerBoundaryIs ob = new outerBoundaryIs();
            LinearRing lr = new LinearRing();
            String coords = a[0] + "," + a[1] + ",0 " + b[0] + "," + b[1] + ",0 " + c[0] + "," + c[1] + ",0 " + d[0] + "," + d[1] + ",0 " + a[0] + "," + a[1] + ",0 ";
            lr.setCoordinates(coords);
            ob.addLinearRing(lr);
            poly.addOuterBoundaryIs(ob);
            mg.addPolygon(poly);
            pm.addMultiGeometry(mg);
            border.addPlacemark(pm);

            //Add ground overlay
            GroundOverlay go = new GroundOverlay();
            go.setName("Overview");
            LatLonBox llb = new LatLonBox();
            AffineTransform at = new AffineTransform();


            double rotation = gir.getImageAzimuth();
            at.setToRotation(rotation * Math.PI / 180, (a[0] + b[0] + c[0] + d[0]) / 4, (a[1] + b[1] + c[1] + d[1]) / 4);
            at.transform(a, 0, a, 0, 1);
            at.transform(b, 0, b, 0, 1);
            at.transform(c, 0, c, 0, 1);
            at.transform(d, 0, d, 0, 1);
            double north = Math.max(Math.max(Math.max(a[1], b[1]), c[1]), d[1]);
            double south = Math.min(Math.min(Math.min(a[1], b[1]), c[1]), d[1]);
            double west = Math.min(Math.min(Math.min(a[0], b[0]), c[0]), d[0]);
            double east = Math.max(Math.max(Math.max(a[0], b[0]), c[0]), d[0]);
            llb.setNorth(north);
            llb.setEast(east);
            llb.setSouth(south);
            llb.setWest(west);
            llb.setRotation(-rotation);
            go.addLatLonBox(llb);

            //check if the overview image has to be flipped or not
            if (b[0] < a[0]) {
                toFlip = true;
            }

            Icon im = new Icon();
            im.setHref("overview.png");
            go.addIcon(im);
            border.addGroundOverlay(go);

            //create folders to group boats by size
            Folder lFolder = new Folder();
            lFolder.setName("up to 20m");
            lFolder.setDescription("list of detected vessels with estimated length lower than 20 meters");
            rootFolder.addFolder(lFolder);

            Folder mFolder = new Folder();
            mFolder.setName("20m to 100m");
            mFolder.setDescription("list of detected vessels with estimated length between 20 and 100 meters");
            rootFolder.addFolder(mFolder);

            Folder hFolder = new Folder();
            hFolder.setName("more than 100m");
            hFolder.setDescription("list of detected vessels with estimated length bigger than 100 meters");
            rootFolder.addFolder(hFolder);

            String[] schema = glayer.getSchema();
            Icon icon = new Icon();
            icon.setHref("boat.png");
            Icon iconNR = new Icon();//icon for not reliable heading, if width/length>0.8
            iconNR.setHref("NRboat.png");
            double imageAzimuth = gir.getImageAzimuth();
            at.rotate(gir.getImageAzimuth() * Math.PI / 180, 32, 32);
            //extraction of boat attributes
            for (Geometry geom : glayer.getGeometries()) {
                Attributes atts = glayer.getAttributes(geom);
                pm = new Placemark();
                //pm.setName(atts.get(schema[0]).toString());
                /*Float length = new Float(atts.get(schema[7]).toString());
                Float width = new Float(atts.get(schema[8]).toString());
                Float ratio = width / length;
                Float heading = new Float(atts.get(schema[9]).toString());*/
                Float length = new Float(""+atts.get("length"));
                Float width = new Float(""+atts.get("width"));
                Float ratio = width / length;
                Float heading = new Float(""+atts.get("heading"));
                boolean azDirection = false;
                if (Math.abs(heading - imageAzimuth) < 5) {
                    azDirection = true;//if there are less than 5degree of difference then the image heading is not reliable
                }
                boolean nr = false;
                if (azDirection || ratio > 0.8) {
                    nr = true;//heading not reliable if one of those is true
                }
                String description = "";
                for (int i = 0; i < schema.length; i++) {
                    if (schema[i].equals("heading") & nr) {//heading not reliable
                        description = description + schema[i] + ": NR " + (i == schema.length - 1 ? "" : "<br/>");
                    } else {
                        description = description + schema[i] + ": " + atts.get(schema[i]) + (i == schema.length - 1 ? "" : "<br/>");
                    }
                }

                /*AG uncomment if you want to add thumbnails in the kml
                 * problems: delete temp thumbnails files
                 *           insert double thumbs if dual pol
                 *           check behaviour when image mirrored*/
                //create thumbnails
                /*Coordinate p = geom.getCoordinate();
                BufferedImage image = ImageTiler.createImage(gir.readTile((int) p.x - 64, (int) p.y - 64, 128, 128), 128, 128, gir);
                //AG rotate the thumbnail according to the image                
                BufferedImageOp bio;
                bio = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
                BufferedImage outImage = bio.filter(image, null);
                File fTemp = new File("D:/temp/"+t+".png");
                ImageIO.write(outImage, "png", fTemp);
                URL thumbURL = fTemp.toURI().toURL();
                InputStream is = thumbURL.openStream(); 
                out.putNextEntry(new ZipEntry(t+".png"));                
                int len;
                while ((len = is.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
                out.closeEntry();
                is.close();
                pm.setDescription(description+"<br/><img src=\""+t+".png\" />");
                t++;*/
                pm.setDescription(description);

                
                Coordinate pos = geom.getCoordinate();
                double[] temp = gt.getGeoFromPixel(pos.x, pos.y, projection);
                Point pt = new Point();
                //pt.setId(schema[0]);
                pt.setNumericalCoordinates(temp);
                pm.addPoint(pt);
                //check the length                
                if (length <= 20) {
                    IconStyle lStyle = new IconStyle();
                    lStyle.setScale((float) 0.5);
                    if (nr) {
                        //heading not reliable
                        lStyle.setScale((float) 0.3);
                        lStyle.addIcon(iconNR);
                    } else {
                        lStyle.addIcon(icon);
                    }
                    lStyle.setColor("cf00ff00");
                    Style lstyle = new Style();
                    lstyle.addIconStyle(lStyle);
                    lStyle.setHeading(heading);
                    pm.addStyle(lstyle);
                    lFolder.addPlacemark(pm);
                } else if (length > 100) {
                    IconStyle hStyle = new IconStyle();
                    hStyle.setScale((float) 0.5);
                    if (nr) {
                        //heading not reliable
                        hStyle.setScale((float) 0.3);
                        hStyle.addIcon(iconNR);
                    } else {
                        hStyle.addIcon(icon);
                    }
                    hStyle.setColor("cf0000ff");
                    Style hstyle = new Style();
                    hstyle.addIconStyle(hStyle);
                    hStyle.setHeading(heading);
                    pm.addStyle(hstyle);
                    hFolder.addPlacemark(pm);
                } else {
                    IconStyle mStyle = new IconStyle();
                    mStyle.setScale((float) 0.5);
                    if (nr) {
                        //heading not reliable
                        mStyle.setScale((float) 0.3);
                        mStyle.addIcon(iconNR);
                    } else {
                        mStyle.addIcon(icon);
                    }
                    mStyle.setColor("cfff0000");
                    Style mstyle = new Style();
                    mstyle.addIconStyle(mStyle);
                    mStyle.setHeading(heading);
                    pm.addStyle(mstyle);
                    mFolder.addPlacemark(pm);
                }

            }
            //write the temp kml file
            FileWriter fis = new FileWriter(tempFile);
            fis.write(kml.toKML());
            fis.flush();
            fis.close();

            //creation of the kmz file
            //byte[] buffer = new byte[1024];
            //ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file));

            //copy of the kml file inside the kmz
            FileInputStream in = new FileInputStream(tempFile);
            out.putNextEntry(new ZipEntry(tempFile.substring(tempFile.lastIndexOf('/') + 1)));
            int len;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            out.closeEntry();
            in.close();

            // create overview image
            URL overview = createOverview(gir, toFlip);
            InputStream inn = overview.openStream();
            out.putNextEntry(new ZipEntry("overview.png"));
            while ((len = inn.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            out.closeEntry();
            inn.close();


            //copy of the boat icon inside the kmz
            URL iconFile = getClass().getResource("/org/geoimage/viewer/core/resources/boat.PNG");
            InputStream in2 = iconFile.openStream();
            out.putNextEntry(new ZipEntry("boat.png"));
            while ((len = in2.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            out.closeEntry();
            in2.close();

            //copy of the not reliable boat icon inside the kmz
            URL iconFileNR = getClass().getResource("/org/geoimage/viewer/core/resources/NRboat.PNG");
            InputStream in3 = iconFileNR.openStream();
            out.putNextEntry(new ZipEntry("NRboat.png"));
            while ((len = in3.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            out.closeEntry();
            in3.close();

            //delete temporary kml file
            File f = new File(tempFile);
            f.delete();
            out.close();

        } catch (IOException ex) {
            Logger.getLogger(GenericCSVIO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private URL createOverview(GeoImageReader gir, boolean toFlip) throws IOException {
        File f = File.createTempFile("kmloverview", ".png");
        // generate a suitable size image
        double ratio = Math.max(((double) gir.getWidth()) / 1024., ((double) gir.getHeight()) / 1024.);
        // generate overview image
        BufferedImage temp = new BufferedImage((int) (gir.getWidth() * (1.0 / ratio)), (int) (gir.getHeight() * (1.0 / ratio)), gir.getType(true));
        // get a handle on the raster data
        WritableRaster raster = temp.getRaster();
        int[] data = gir.readAndDecimateTile(0, 0, gir.getWidth(), gir.getHeight(), 1.0 / ratio, true, null);
        raster.setSamples(0, 0, temp.getWidth(), temp.getHeight(), 0, data);

        float average = 0;
        for (int i = 0; i < data.length; i++) {
            average = average + data[i];
        }
        average = average / data.length;
        RescaleOp rescale = new RescaleOp(((1 << (8 * gir.getNumberOfBytes())) / 5f / average), 0, null);
        rescale.filter(temp, temp);

        ColorConvertOp bop = new ColorConvertOp(null);
        BufferedImage out = bop.createCompatibleDestImage(temp, new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY), false, false, ComponentColorModel.OPAQUE, DataBuffer.TYPE_BYTE));
        out = bop.filter(temp, out);

        //flip the image if necessary
        if (toFlip) {
            int w = out.getWidth();
            int h = out.getHeight();
            BufferedImage dimg = new BufferedImage(w, h, out.getType());
            Graphics2D g = dimg.createGraphics();
            g.drawImage(out, 0, 0, w, h, w, 0, 0, h, null);
            g.dispose();
            ImageIO.write(dimg, "png", f);
        } else {
            ImageIO.write(out, "png", f);
        }
        return f.toURI().toURL();

    }


}
