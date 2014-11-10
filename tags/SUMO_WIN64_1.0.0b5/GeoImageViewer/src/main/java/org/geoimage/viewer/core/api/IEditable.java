/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoimage.viewer.core.api;

/**
 *
 * @author thoorfr
 */
public interface IEditable {
    public void setEditable(boolean editable);
    public boolean isEditable();
    public void setAddAction(boolean add);
    public boolean isAddAction();
    public void setDeleteAction(boolean delete);
    public boolean isDeleteAction();
    public void setMoveAction(boolean move);
    public boolean isMoveAction();
    public void setChangeAttributesAction(boolean move);
    public boolean isChangeAttributesAction();
}
