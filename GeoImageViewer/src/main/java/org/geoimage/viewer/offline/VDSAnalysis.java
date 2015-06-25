/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.offline;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.geoimage.analysis.DetectedPixels;
import org.geoimage.def.GeoImageReader;
import org.geoimage.def.SarImageReader;
import org.geoimage.factory.GeoImageReaderFactory;
import org.geoimage.utils.IMask;
import org.geoimage.viewer.core.api.Attributes;
import org.geoimage.viewer.core.io.AbstractVectorIO;
import org.geoimage.viewer.core.io.GenericCSVIO;
import org.geoimage.viewer.core.io.SumoXmlIOOld;
import org.geoimage.viewer.core.layers.GeometricLayer;
import org.geoimage.viewer.core.layers.vectors.MaskVectorLayer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 *
 * @author thoorfr
 */
public class VDSAnalysis {

    public static void main(String[] args) throws Exception {
    /*    String image = args[0];
        GeoImageReader gir = GeoImageReaderFactory.createReaderForName(image).get(0);
        if (!(gir instanceof SarImageReader)) {
            return;
        }
        URL url = VDSAnalysis.class.getResource("/org/geoimage/viewer/core/resources/shapefile/Global GSHHS Land Mask.shp");
        System.out.println(url);
        Map config = new HashMap();
        config.put("url", url);
        AbstractVectorIO shpio = VectorIOFactory.createVectorIO(VectorIOFactory.SIMPLE_SHAPEFILE, config);
        GeometricLayer mask = shpio.read(gir);
        IMask[] masks = new IMask[1];
        masks[0]=(new MaskVectorLayer("mask", null, mask.getGeometryType(), mask));
        org.geoimage.analysis.VDSAnalysis vds = new org.geoimage.analysis.VDSAnalysis((SarImageReader) gir,masks, 5f, 15f, 15f,15f,15f, null);
        vds.run(null);
        DetectedPixels pixels = vds.getPixels();
        pixels.agglomerate();
        pixels.computeBoatsAttributes();
        GeometricLayer gl = createGeometricLayer(pixels);
        Map config2 = new HashMap();
        config2.put(GenericCSVIO.CONFIG_FILE, args[0] + ".csv");
        AbstractVectorIO csv = VectorIOFactory.createVectorIO(VectorIOFactory.GENERIC_CSV, config2);
        csv.save(gl, "EPSG:4326",gir);
        config.put(SumoXmlIOOld.CONFIG_FILE, args[0] + ".sumo.xml");
        AbstractVectorIO smio = VectorIOFactory.createVectorIO(VectorIOFactory.SUMO, config);
        smio.save(gl, "EPSG:4326",gir);
        //ThumbnailsManager tm=new ThumbnailsManager(args[0]+"dir");
        //tm.createThumbnailsDir(gl, "id", gir, null);
        gir.dispose();*/
    }
/*
    private static GeometricLayer createGeometricLayer(DetectedPixels pixels) {
        GeometricLayer out = new GeometricLayer("point");
        GeometryFactory gf = new GeometryFactory();
        for (double[] boat : pixels.getBoats()) {
            String[] schema = new String[]{"id", "maximum value", "tile average", "tile standard deviation", "threshold", "num pixels", "runid"};
            String[] types = new String[]{"Double", "Double", "Double", "Double", "Double", "Double", "String"};
            Attributes atts = Attributes.createAttributes(schema, types);
            long runid = System.currentTimeMillis();
            atts.set("id", boat[0]);
            atts.set("maximum value", boat[3]);
            atts.set("tile average", boat[4]);
            atts.set("tile standard deviation", boat[5]);
            atts.set("threshold", boat[6]);
            atts.set("runid", runid + "");
            atts.set("num pixels", boat[7]);
            out.put(gf.createPoint(new Coordinate(boat[1], boat[2])), atts);
            out.setName("VDS Analysis");
        }
        return out;
    }*/
}
