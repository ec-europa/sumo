/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.core.layers.thumbnails;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.Vector;

import javax.imageio.ImageIO;

import org.geoimage.def.GeoImageReader;
import org.geoimage.def.SarImageReader;
import org.geoimage.exception.GeoTransformException;
import org.geoimage.impl.Gcp;
import org.geoimage.utils.IProgress;
import org.geoimage.viewer.core.io.SumoXmlIOOld;
import org.geoimage.viewer.core.layers.GeometricLayer;
import org.geoimage.viewer.util.ImageTiler;
import org.jrc.sumo.util.Constant;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/**
 *
 * @author thoorfr
 */
public class ThumbnailsManager {
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(ThumbnailsManager.class);

    private File path;

    public ThumbnailsManager(String rootpath) {
        this.path = new File(rootpath);
        this.path.mkdirs();
    }

    public BufferedImage get(String id) {
        File f = new File(path, id + ".png");
        if (f.exists()) {
            try {
                return ImageIO.read(f);
            } catch (IOException ex) {
            	logger.error(ex.getMessage(),ex);
            }
        }
        return null;
    }

    public BufferedImage getOverview() {
        for (File f : path.listFiles()) {
            if (f.getName().startsWith("overview")) {
                try {
                    return ImageIO.read(f);
                } catch (IOException ex) {
                	logger.error(ex.getMessage(),ex);
                }
            }
        }
        return null;

    }

    public int[] getImageSize() {
        for (File f : path.listFiles()) {
            if (f.getName().startsWith("overview")) {
                String temp = f.getName().replace("overview", "").replace(".png", "");
                String[] size = temp.split("x");
                int[] out = new int[2];
                out[0] = Integer.parseInt(size[0]);
                out[1] = Integer.parseInt(size[1]);
                return out;
            }
        }
        return null;
    }

    public void createThumbnailsDir(GeometricLayer glayer, String idColumnName, GeoImageReader gir, IProgress ip,int band) {
        try {
            if (ip != null) {
                ip.setCurrent(0);
                ip.setIndeterminate(false);
                ip.setMessage("building thumbnails...");
                ip.setMaximum(glayer.getGeometries().size());
            }
            for (Geometry geom : glayer.getGeometries()) {
                try {
                    if (ip != null) {
                        ip.setCurrent(ip.getCurrent() + 1);
                    }
                    if (!new File(path, glayer.getAttributes(geom).get(idColumnName).toString() + ".png").exists()) {
                        Coordinate p = geom.getCoordinate();
                        BufferedImage image = ImageTiler.createImage(gir.readTile((int) p.x - (Constant.OVERVIEW_SIZE/2), (int) p.y - (Constant.OVERVIEW_SIZE/2), 
                        		Constant.OVERVIEW_SIZE, Constant.OVERVIEW_SIZE,band), Constant.OVERVIEW_SIZE, Constant.OVERVIEW_SIZE, gir);
                        ImageIO.write(image, "png", new File(path, glayer.getAttributes(geom).get(idColumnName).toString() + ".png"));
                    }
                } catch (IOException ex) {
                	logger.error(ex.getMessage(),ex);
                }
            }
            if (!new File(path, "overview" + gir.getWidth() + "x" + gir.getHeight() + ".png").exists()) {
                if (ip != null) {
                    ip.setMessage("building overview image...");
                }
                double scale=1;
                if(gir.getWidth()<gir.getHeight()){
                    scale=gir.getHeight()/1024.;
                }
                else{
                    scale=gir.getWidth()/1024.;
                }
                BufferedImage overview = new BufferedImage((int)(gir.getWidth()*(1.0/scale)), (int)(gir.getHeight()*(1.0/scale)), gir.getType(true));
                overview.getRaster().setSamples(0, 0, overview.getWidth(), overview.getHeight(), 0, 
                		gir.readAndDecimateTile(0, 0, gir.getWidth(), gir.getHeight(),1/scale, false, null,band));
                ImageIO.write(overview, "png", new File(path, "overview" + gir.getWidth() + "x" + gir.getHeight() + ".png"));
            }
        } catch (IOException ex) {
        	logger.error(ex.getMessage(),ex);
        }
        try {
            RandomAccessFile temp = new RandomAccessFile(new File(path, "gcps.pts"), "rw");
            List<Gcp> gcps = gir.getGcps();
            if (gcps == null || gcps.size() < 10) {
                gcps = createGcps(gir, 10, 10);
            }
            for (Gcp gcp : gcps) {
                temp.writeBytes(gcp.getXgeo() + " ");
                temp.writeBytes(gcp.getYgeo() + " ");
                temp.writeBytes(gcp.getXpix() + " ");
                temp.writeBytes(gcp.getYpix() + "\n");
            }
            temp.close();
        } catch (Exception ex) {
        	logger.error(ex.getMessage(),ex);
        }
        File out=new File(path, glayer.getName().replaceAll(" ", "_") + ".sumo.xml");
        
        SumoXmlIOOld.export(out, glayer, "EPSG:4326", (SarImageReader)gir);
        if (ip != null) {
            ip.setDone(true);
            ip.setMessage(null);
        }

    /*
    KSATVessel vesselFile = new KSATVessel(path.getAbsolutePath() + "vessel.gml");
    for (Geometry geom : glayer.getGeometries()) {
    Coordinate p = geom.getCoordinate();
    vesselFile.AddNewVessel(gir.getGeoTransform().getGeoFromPixel(p.x, p.y, "EPSG:4326")[0], gir.getGeoTransform().getGeoFromPixel(p.x, p.y, "EPSG:4326")[1], new BigDecimal(0));
    }
    vesselFile.generateFile();
     */

    }

    public File getPTS() {
        File f = new File(path, "gcps.pts");
        if (f.exists()) {
            return f;
        }
        return null;
    }

    public static void main(String[] args) {
    }

    private List<Gcp> createGcps(GeoImageReader gir, int nRows, int nCols) throws GeoTransformException {
        Vector<Gcp> out = new Vector<Gcp>();
        int stepx = gir.getWidth() / nCols;
        int stepy = gir.getHeight() / nRows;
        for (int j = 0; j < nRows; j++) {
            for (int i = 0; i < nCols; i++) {
                Gcp gcp = new Gcp();
                gcp.setXpix(i * stepx);
                gcp.setYpix(j * stepy);
                double[] geo = gir.getGeoTransform().getGeoFromPixel(gcp.getXpix(), gcp.getYpix());
                gcp.setXgeo(geo[0]);
                gcp.setYgeo(geo[1]);
                out.add(gcp);
            }
        }
        return out;
    }
}
