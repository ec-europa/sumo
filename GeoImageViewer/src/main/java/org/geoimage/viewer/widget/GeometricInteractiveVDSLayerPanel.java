/*
 * GeometricInteractiveVDSLayerPanel.java
 *
 * Created on June 18, 2008, 5:12 PM
 */

package org.geoimage.viewer.widget;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.geoimage.viewer.core.Platform;
import org.geoimage.viewer.core.api.Attributes;
import org.geoimage.viewer.core.api.GeoContext;
import org.geoimage.viewer.core.api.IComplexVDSVectorLayer;
import org.geoimage.viewer.core.api.ILayer;
import org.geoimage.viewer.core.layers.GeometricLayer;
import org.geoimage.viewer.core.layers.vectors.ComplexEditVDSVectorLayer;
import org.geoimage.viewer.util.GeometricInteractiveVDSLayerModel;

import com.vividsolutions.jts.geom.Geometry;

/**
 *
 * @author  leforth
 */
public class GeometricInteractiveVDSLayerPanel extends javax.swing.JPanel {
	private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2 = new jTable();
	
    private GeometricInteractiveVDSLayerModel glm;
    private boolean display = true;
    private List<Geometry> deleted = new ArrayList<Geometry>();
    private List<Attributes> attrDeleted = new ArrayList<Attributes>();
    private ILayer layer;

    private class jTable extends JTable  implements KeyListener
    {
        @Override
        public void valueChanged(ListSelectionEvent e)
        {
            super.valueChanged(e);
            if(getSelectedRow()>=0){
            	if(dataModel instanceof GeometricInteractiveVDSLayerModel)
            		((GeometricInteractiveVDSLayerModel)dataModel).changeSelection(getSelectedRow(), display);
            }	
        }
        
        public void keyTyped(KeyEvent e) {
        }

        public void keyPressed(KeyEvent e) {
            switch(e.getKeyCode())
            {
            	
                // toggle the display of the additional geometries
                case 'D':
                case 'd':
                {
                    display = !display;
                    ((GeometricInteractiveVDSLayerModel)dataModel).changeSelection(getSelectedRow(), display);
                } break;
                // toggle bands
                case 'B':
                case 'b':
                {
                    Platform.getConsoleLayer().execute("bs");
                } break;
                // increase the contrast
                case KeyEvent.VK_C:
                {
                    if(e.isShiftDown())
                        Platform.getConsoleLayer().execute("contrast +10");
                    else
                        Platform.getConsoleLayer().execute("contrast -10");
                } break;
                // move image up
                case KeyEvent.VK_UP:
                {
                    if(e.isAltDown())
                        Platform.getGeoContext().setY(Platform.getGeoContext().getY() - Platform.getGeoContext().getHeight() / 3);
                } break;
                // move image down
                case KeyEvent.VK_DOWN:
                {
                    if(e.isAltDown())
                        Platform.getGeoContext().setY(Platform.getGeoContext().getY() + Platform.getGeoContext().getHeight() / 3);
                } break;
                // move image left
                case KeyEvent.VK_LEFT:
                {
                    if(e.isAltDown())
                        Platform.getGeoContext().setX(Platform.getGeoContext().getX() - Platform.getGeoContext().getWidth() / 3);
                } break;
                // move image right
                case KeyEvent.VK_RIGHT:
                {
                    if(e.isAltDown())
                        Platform.getGeoContext().setX(Platform.getGeoContext().getX() + Platform.getGeoContext().getWidth() / 3);
                } break;
                // zoom out image
                case KeyEvent.VK_Z:
                {	//undo operation
                	if (e.isControlDown()){  
                		if(deleted.size()>0){
                			Geometry geom=deleted.remove(0);
                			Attributes attr=attrDeleted.remove(0);
                			GeometricLayer gl=((ComplexEditVDSVectorLayer)layer).getGeometriclayer();
                			gl.put(geom,attr);
                			((ComplexEditVDSVectorLayer)layer).setSelectedGeometry(geom);
                			glm.fireTableDataChanged();
                		}	
                		
                	}else{
	                    GeoContext geoContext = Platform.getGeoContext();
	                    float zoom = (float) geoContext.getZoom();
	                    if(e.isShiftDown())
	                    {
	                        zoom = zoom * 2;
	                    } else {
	                        zoom = zoom / 2;
	                    }
	                    int x = (int) (geoContext.getX() + geoContext.getWidth() * geoContext.getZoom() / 2);
	                    int y = (int) (geoContext.getY() + geoContext.getHeight() * geoContext.getZoom() / 2);
	                    geoContext.setZoom(zoom);
	                    geoContext.setX((int) (x - geoContext.getWidth() * zoom / 2));
	                    geoContext.setY((int) (y - geoContext.getHeight() * zoom / 2));
                	}    
                } break;
                // toggle the display of Azimuth rulers
                case KeyEvent.VK_A:
                {
                    ((GeometricInteractiveVDSLayerModel)dataModel).toggleRulers(getSelectedRow());
                } break;
                case 'E':
                case 'e':
                {
                   ((GeometricInteractiveVDSLayerModel)dataModel).editSelection(getSelectedRow());
                } break;
                default:
                    break;
            }
        }

        public void keyReleased(KeyEvent e) {
            int id = e.getID();
            if(id == e.KEY_RELEASED && e.getKeyChar() == e.VK_BACK_SPACE)
            {
                if(getSelectedRow() != -1)
                    for(int i:getSelectedRows()){
                    	GeometricLayer gl=((ComplexEditVDSVectorLayer)layer).getGeometriclayer();
                    	Geometry geom=gl.getGeometries().get(i);
                    	deleted.add(0,geom);
                        attrDeleted.add(0,(gl).getAttributes(geom));
                    	((GeometricInteractiveVDSLayerModel)dataModel).removeSelection(i);
                    	glm.fireTableDataChanged();
                    	if(gl.getGeometries().size()>0){
                    		int sel=0;
                    		if(i>0&&i<(gl.getGeometries().size()-1))
                    			sel=i;
                    		((GeometricInteractiveVDSLayerModel)dataModel).changeSelection(sel, display);
                    		glm.changeSelection(sel, true);
                    		((JTable)e.getSource()).setRowSelectionInterval(sel,sel);
                    	}
                    }
                this.repaint();
                ((GeometricInteractiveVDSLayerModel)dataModel).changeSelection(getSelectedRow(), display);
            }
        }

    };
        
    /** Creates new form GeometricInteractiveVDSLayerPanel */
    public GeometricInteractiveVDSLayerPanel(ILayer layer) {
        this.layer = layer;
        initComponents();
        glm=new GeometricInteractiveVDSLayerModel(layer);
        ((jTable)jTable1).setModel(glm);
        jTable1.addKeyListener((jTable)jTable1);
        
    }
    
    

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        javax.swing.JScrollPane jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new jTable();

        setName("Form"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jTable1.setName("jTable1"); // NOI18N
        jScrollPane1.setViewportView(jTable1);

        // add check boxes for debug information
        List<String> geometriestaglist = ((IComplexVDSVectorLayer)layer).getGeometriestagList();
        if(geometriestaglist != null)
        {
            for(final String tag : geometriestaglist)
            {
                String status = ((IComplexVDSVectorLayer)layer).getGeometriesDisplay(tag) ? "On" : "Off";
                Boolean statusCheckBoolean = new Boolean(status.equalsIgnoreCase("On"));
                
                debugTableModel.addRow(new Object[]{tag, statusCheckBoolean});
                debugTableModel.addTableModelListener(new TableModelListener() {

                    public void tableChanged(TableModelEvent e) {
                        int selectedrow = jTable2.getSelectedRow();
                        if((selectedrow != -1) && (selectedrow < jTable2.getRowCount()))
                        {
                            boolean status = ((Boolean)jTable2.getValueAt(selectedrow, 1));
                            String tag = ((String)jTable2.getValueAt(selectedrow, 0));
                            ((IComplexVDSVectorLayer)layer).toggleGeometriesByTag(tag, status);
                            repaint();
                        }
                    }
                });
            };
        }
        jTable2.setModel(debugTableModel);
        jTable2.setName("jTable2"); // NOI18N
        jTable2.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
/*
                String tag = (String)(debugTableModel.getValueAt(jTable2.getSelectedRow(), 1));
                String value = (String)(debugTableModel.getValueAt(jTable2.getSelectedRow(), 2));
                debugTableModel.setValueAt(value.equalsIgnoreCase("On") ? "Off" : "On", jTable2.getSelectedRow(), 2);
                ((IComplexVDSVectorLayer)layer).toggleGeometriesByTag(tag, !value.equalsIgnoreCase("On"));
*/
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }
        });
        jScrollPane2.setViewportView(jTable2);

        
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                    .addContainerGap()
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                    .addContainerGap())
        );
    }
    
    private class DebugTableModel extends javax.swing.table.DefaultTableModel {

        private DebugTableModel(Object[][] object, String[] string) {
            super(object, string);
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if(columnIndex == 0)
                return String.class;
            else
            	//checkbox
                return Boolean.class;
        }
        
    }
    private DebugTableModel debugTableModel = new DebugTableModel(
            new Object [][] {
            },
            new String [] {
                "Debug Layer", "Status"
            }
        );

}
