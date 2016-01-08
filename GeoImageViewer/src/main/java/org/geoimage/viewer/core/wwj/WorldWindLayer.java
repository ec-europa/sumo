/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.core.wwj;

import java.awt.Color;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geoimage.def.GeoImageReader;
import org.geoimage.def.SarImageReader;
import org.geoimage.viewer.core.api.ITime;
import org.geoimage.viewer.core.api.ilayer.ILayer;
import org.geoimage.viewer.core.gui.manager.LayerManager;
import org.geoimage.viewer.core.layers.image.ImageLayer;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Polyline;
import gov.nasa.worldwind.view.orbit.BasicOrbitView;

/**
 *
 * @author thoorfr
 */
public class WorldWindLayer extends RenderableLayer implements ITime {

    private LayerManager lm;
    private Map<GeoImageReader, Polyline> geoms = new HashMap<GeoImageReader, Polyline>();
    private Date minDate = Timestamp.valueOf("9999-01-01 00:00:00");
    private Date maxDate = Timestamp.valueOf("1970-01-01 00:00:00");
    private Date curminDate = Timestamp.valueOf("9999-01-01 00:00:00");
    private Date curmaxDate = Timestamp.valueOf("1970-01-01 00:00:00");

    public WorldWindLayer(LayerManager lm) {
        this.lm = lm;
    }

    @Override
    public void render(DrawContext dc) {
        super.render(dc);
        boolean done = false;
        for (ILayer layer : lm.getLayers().keySet()) {
            if (layer instanceof ImageLayer) {
                GeoImageReader gir = ((ImageLayer) layer).getImageReader();
                Polyline pol = geoms.get(gir);
                Timestamp ts = Timestamp.valueOf(((SarImageReader)gir).getTimeStampStop());
                if (pol == null) {
                    List<LatLon> ll = new ArrayList<LatLon>();
           /*         for (Coordinate c : GeometryExtractor.getFrame(gir).getCoordinates()) {
                        ll.add(new LatLon(Angle.fromDegreesLatitude(c.y), Angle.fromDegreesLongitude(c.x)));
                    }*/
                    pol = new Polyline(ll, 1000);
                    pol.setHighlightColor(Color.GREEN);
                    geoms.put(gir, pol);
                    if (ts != null) {
                        if (ts.before(minDate)) {
                            minDate = ts;
                        }
                        if (ts.after(maxDate)) {
                            maxDate = ts;
                        }
                    }
                    BasicOrbitView bow=((BasicOrbitView) dc.getView());
                    bow.addPanToAnimator(new Position(ll.get(0), 1000000), Angle.ZERO, Angle.ZERO, 1000000);
                    //OLD VERSION Iterator vst =FlyToOrbitViewStateIterator.createPanToIterator((OrbitView) dc.getView(), dc.getGlobe(), new Position(ll.get(0), 1000000), Angle.ZERO, Angle.ZERO, 1000000);
                                  //dc.getView().applyStateIterator(vst);
                }

                if (ts != null) {
                    if (ts.before(curminDate) || ts.after(curmaxDate)) {
                        continue;
                    }
                }
                if (layer.isActive() && !done) {
                    //!dc.getView().hasStateIterator() &&
                    if (!dc.getVisibleSector().contains(new LatLon(pol.getPositions().iterator().next().getLatitude(),pol.getPositions().iterator().next().getLongitude()))) {
                        BasicOrbitView bow=((BasicOrbitView) dc.getView());
                        bow.addPanToAnimator(pol.getPositions().iterator().next(), Angle.ZERO, Angle.ZERO, 1000000);
                        //Iterator vst = FlyToOrbitViewStateIterator.createPanToIterator((OrbitView) dc.getView(), dc.getGlobe(), pol.getPositions().iterator().next(), Angle.ZERO, Angle.ZERO, 1000000);
                        //dc.getView().applyStateIterator(vst);
                    }

                    pol.setHighlighted(true);
                    done = true;
                } else {
                    pol.setHighlighted(false);
                }
                pol.render(dc);
            }
        }
    }

    public Date getMaximumDate() {
        return curmaxDate;
    }

    public Date getMinimumDate() {
        return curminDate;
    }

    public String getTimeColumn() {
        return "id";
    }

    public void setMaximumDate(Date maximumDate) {
        curmaxDate = maximumDate;
    }

    public void setMinimumDate(Date minimumDate) {
        curminDate = minimumDate;
    }

    public void setTimeColumn(String timeColumn) {
    }

    public Date[] getDates() {
        return new Date[]{minDate, maxDate};
    }
}
