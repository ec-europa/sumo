/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoimage.viewer.widget;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.slf4j.LoggerFactory;

/**
 *
 * @author thoorfr
 */
class DatabaseModel implements TableModel{
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(DatabaseModel.class);

    private ResultSet rs;
    private String[] cname=new String[0];
    private int nrow=0;

    DatabaseModel(ResultSet rs) {
        try {
            this.rs = rs;
            if(rs==null) return; 
            cname = new String[rs.getMetaData().getColumnCount()];
            for (int i = 0; i < cname.length; i++) {
                cname[i] = rs.getMetaData().getColumnName(i+1);
            }
            rs.last();
            nrow=rs.getRow();
        } catch (SQLException ex) {
        	logger.error(ex.getMessage(),ex);
        }
    }

    public int getRowCount() {
        return nrow;
    }

    public int getColumnCount() {
        return cname.length;
    }

    public String getColumnName(int columnIndex) {
        return cname[columnIndex];
    }

    public Class<?> getColumnClass(int columnIndex) {
        try {
            return Class.forName(rs.getMetaData().getColumnClassName(columnIndex+1));
        } catch (Exception ex) {
        	logger.error(ex.getMessage(),ex);
        }
        return null;
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        try {
            rs.first();
            rs.relative(rowIndex);
            return rs.getObject(columnIndex+1);
        } catch (SQLException ex) {
        	logger.error(ex.getMessage(),ex);
        }
        return null;
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    }

    public void addTableModelListener(TableModelListener l) {
    }

    public void removeTableModelListener(TableModelListener l) {
    }

}
