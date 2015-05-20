/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.core.wwj;

import gov.nasa.worldwind.Disposable;
import gov.nasa.worldwind.Locatable;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.pick.PickSupport;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import org.geoimage.viewer.core.api.ITime;

/**
 *
 * @author thoorfr
 */
public class TimeRenderableLayer extends AbstractLayer implements ITime {

    private Date mindate = Timestamp.valueOf("1970-01-01 00:00:00");
    private Date maxdate = Timestamp.valueOf("9999-01-01 00:00:00");
    private Date _mindate = Timestamp.valueOf("9999-05-05 00:00:00");
    private Date _maxdate = Timestamp.valueOf("1970-01-01 00:00:00");
    private HashMap<Renderable, Date> map = new HashMap<Renderable, Date>();
    private java.util.Collection<Renderable> renderables = new java.util.ArrayList<Renderable>();
    private Iterable<Renderable> renderablesOverride;
    private final PickSupport pickSupport = new PickSupport();

    /**
     * Adds the specified <code>renderable</code> to this layer's internal collection.
     * If this layer's internal collection has been overriden with a call to {@link #setRenderables},
     * this will throw an exception.
     *
     * @param renderable Renderable to add.
     * @throws IllegalArgumentException If <code>renderable</code> is null.
     * @throws IllegalStateException If a custom Iterable has been specified by a call to <code>setRenderables</code>.
     */
    public void addRenderable(Renderable renderable) {
        if (renderable == null) {
            throw new IllegalArgumentException("");
        }
        if (this.renderablesOverride != null) {
            throw new IllegalStateException("");
        }

        this.renderables.add(renderable);
    }

    /**
     * Adds the contents of the specified <code>renderables</code> to this layer's internal collection.
     * If this layer's internal collection has been overriden with a call to {@link #setRenderables},
     * this will throw an exception.
     *
     * @param renderables Renderables to add.
     * @throws IllegalArgumentException If <code>renderables</code> is null.
     * @throws IllegalStateException If a custom Iterable has been specified by a call to <code>setRenderables</code>.
     */
    public void addRenderables(Iterable<Renderable> renderables) {
        if (renderables == null) {
            throw new IllegalArgumentException("");
        }
        if (this.renderablesOverride != null) {
            throw new IllegalStateException("");
        }

        for (Renderable renderable : renderables) {
            // Internal list of renderables does not accept null values.
            if (renderable != null) {
                this.renderables.add(renderable);
            }
        }
    }

    /**
     * Removes the specified <code>renderable</code> from this layer's internal collection, if it exists.
     * If this layer's internal collection has been overriden with a call to {@link #setRenderables},
     * this will throw an exception.
     *
     * @param renderable Renderable to remove.
     * @throws IllegalArgumentException If <code>renderable</code> is null.
     * @throws IllegalStateException If a custom Iterable has been specified by a call to <code>setRenderables</code>.
     */
    public void removeRenderable(Renderable renderable) {
        if (renderable == null) {
            throw new IllegalArgumentException("");
        }
        if (this.renderablesOverride != null) {
            throw new IllegalStateException("");
        }

        this.renderables.remove(renderable);
    }

    /**
     * Clears the contents of this layer's internal Renderable collection.
     * If this layer's internal collection has been overriden with a call to {@link #setRenderables},
     * this will throw an exception.
     *
     * @throws IllegalStateException If a custom Iterable has been specified by a call to <code>setRenderables</code>.
     */
    public void removeAllRenderables() {
        if (this.renderablesOverride != null) {
            throw new IllegalStateException("");
        }

        clearRenderables();
    }

    private void clearRenderables() {
        if (this.renderables != null && this.renderables.size() > 0) {
            this.renderables.clear();
        }
    }

    /**
     * Returns the Iterable of Renderables currently in use by this layer.
     * If the caller has specified a custom Iterable via {@link #setRenderables}, this will returns a reference
     * to that Iterable. If the caller passed <code>setRenderables</code> a null parameter,
     * or if <code>setRenderables</code> has not been called, this returns a view of this layer's internal
     * collection of Renderables.
     *
     * @return Iterable of currently active Renderables.
     */
    public Iterable<Renderable> getRenderables() {
        Vector<Renderable> out = new Vector<Renderable>();
        for (Renderable renderable : this.renderables) {
            if (renderable != null) {
                Date date = map.get(renderable);
                if (date != null && (date.before(mindate) || date.after(maxdate))) {
                    continue;
                }
                out.add(renderable);
            }
        }
        return out;
    }



    /**
     * Overrides the collection of currently active Renderables with the specified <code>renderableIterable</code>.
     * This layer will maintain a reference to <code>renderableIterable</code> strictly for picking and rendering.
     * This layer will not modify the reference, or dispose of its contents. This will also clear and dispose of
     * the internal collection of Renderables, and will prevent any modification to its contents via
     * <code>addRenderable, addRenderables, removeRenderables, or dispose</code>.
     *
     * If the specified <code>renderableIterable</code> is null, this layer will revert to maintaining its internal
     * collection.
     *
     * @param renderableIterable Iterable to use instead of this layer's internal collection, or null to use this
     *                           layer's internal collection.
     */
    public void setRenderables(Iterable<Renderable> renderableIterable) {
        this.renderablesOverride = renderableIterable;
        // Dispose of the internal collection of Renderables.
        disposeRenderables();
        // Clear the internal collection of Renderables.
        clearRenderables();
    }

    /**
     * Disposes the contents of this layer's internal Renderable collection, but does not remove any elements from
     * that collection.
     *
     * @throws IllegalStateException If a custom Iterable has been specified by a call to <code>setRenderables</code>.
     */
    public void dispose() {
        if (this.renderablesOverride != null) {
            throw new IllegalStateException("");
        }

        disposeRenderables();
    }

    private void disposeRenderables() {
        if (this.renderables != null && this.renderables.size() > 0) {
            for (Renderable renderable : this.renderables) {
                if (renderable instanceof Disposable) {
                    ((Disposable) renderable).dispose();
                }
            }
        }
    }

    @Override
    protected void doPick(DrawContext dc, java.awt.Point pickPoint) {
        this.pickSupport.clearPickList();
        this.pickSupport.beginPicking(dc);

        for (Renderable renderable : getRenderables()) {
            // If the caller has specified their own Iterable,
            // then we cannot make any guarantees about its contents.
            if (renderable != null) {
                float[] inColor = new float[4];
                dc.getGL().glGetFloatv(GL2.GL_CURRENT_COLOR, inColor, 0);
                java.awt.Color color = dc.getUniquePickColor();
                dc.getGL().getGL2().glColor3ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue());

                renderable.render(dc);

                dc.getGL().getGL2().glColor4fv(inColor, 0);

                if (renderable instanceof Locatable) {
                    this.pickSupport.addPickableObject(color.getRGB(), renderable,
                            ((Locatable) renderable).getPosition(), false);
                } else {
                    this.pickSupport.addPickableObject(color.getRGB(), renderable);
                }
            }
        }

        this.pickSupport.resolvePick(dc, pickPoint, this);
        this.pickSupport.endPicking(dc);
    }


    public void setMinTime(Date date) {
        this.mindate = date;
    }

    public void setMaxTime(Date date) {
        this.maxdate = date;
    }

    @Override
    protected void doRender(DrawContext dc) {
        for (Renderable renderable : getRenderables()) {
            if (renderable != null) {
                Date date = map.get(renderable);
                if (date != null && (date.before(mindate) || date.after(maxdate))) {
                    continue;
                }
                renderable.render(dc);
            }
        }
    }

    public void addRenderable(Renderable renderable, Date date) {
        if (date.after(new Date())) {
            return;
        }
        map.put(renderable, date);
        if (date.before(_mindate)) {
            _mindate = date;
        }
        if (date.after(_maxdate)) {
            _maxdate = date;
        }
        addRenderable(renderable);
    }

    public Date getMaximumDate() {
        return maxdate;
    }

    public Date getMinimumDate() {
        return mindate;
    }

    public String getTimeColumn() {
        return "id";
    }

    public void setMaximumDate(Date maximumDate) {
        maxdate = maximumDate;
    }

    public void setMinimumDate(Date minimumDate) {
        mindate = minimumDate;
    }

    public void setTimeColumn(String timeColumn) {
    }

    public Date[] getDates() {
        return new Date[]{_mindate, _maxdate};
    }

    public Date[] calculateMinMaxDates()
    {
        Date[] dates = new Date[]{maxdate, mindate};
        for (Renderable renderable : this.renderables) {
            if (renderable != null) {
                Date date = map.get(renderable);
                if (date.before(dates[0]))
                    dates[0] = date;
                if(date.after(dates[1]))
                    dates[1] = date;
            }
        }
        if(dates[0].before(dates[1]))
            return dates;
        else
            return new Date[]{mindate, maxdate};
    }
}
