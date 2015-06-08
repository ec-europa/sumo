/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.util;

import com.vividsolutions.jts.geom.Geometry;

import java.util.Date;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.geoimage.viewer.core.layers.GeometricLayer;

/**
 *
 * @author thoorfr
 */
public class GeometricLayerModel implements TableModel {

    private GeometricLayer gl;

    public GeometricLayerModel(GeometricLayer gl) {
        this.gl = gl;
    }

    public int getRowCount() {
        return gl.getGeometries().size();
    }

    public int getColumnCount() {
        return gl.getSchema().length + 1;
    }

    public String getColumnName(int columnIndex) {
        if (columnIndex == 0) {
            return "Geometry";
        } else {
            return gl.getSchema()[columnIndex - 1];
        }
    }

    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0) {
            return Geometry.class;
        } else {
            String c = gl.getSchemaTypes()[columnIndex - 1];
            if (c.contains("Double")) {
                return Double.class;
            }
            if (c.contains("String")) {
                return String.class;
            }
            if (c.contains("Integer")) {
                return Integer.class;
            }
            if (c.contains("Long")) {
                return Long.class;
            }
            if (c.contains("Date")) {
                return Date.class;
            }
            else{
                return String.class;
            }
        }
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        Geometry geom = gl.getGeometries().get(rowIndex);
        if (columnIndex == 0) {
            return geom;
        } else {
            return gl.getAttributes(geom).get(gl.getSchema()[columnIndex - 1]);
        }
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    }

    public void addTableModelListener(TableModelListener l) {
    }

    public void removeTableModelListener(TableModelListener l) {
    }
}
