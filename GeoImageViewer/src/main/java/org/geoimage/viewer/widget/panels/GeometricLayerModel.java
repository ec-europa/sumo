/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.widget.panels;

import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import org.geoimage.viewer.core.GeometryCollection;

import com.vividsolutions.jts.geom.Geometry;

/**
 *
 * @author thoorfr
 */
public class GeometricLayerModel extends DefaultTableModel {

    private GeometryCollection gl;

    public GeometricLayerModel(GeometryCollection gl) {
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
