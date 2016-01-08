/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoimage.viewer.core.api;

import java.awt.Point;

/**
 *
 * @author thoorfr
 */
public interface IMouseDrag {
    
    public void mouseDragged(Point initPosition, Point imagePosition, int button,Object glContext);

}
