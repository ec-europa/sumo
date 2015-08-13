/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoimage.viewer.core.api;

import java.awt.Point;
import java.awt.event.MouseEvent;

import org.geoimage.opengl.OpenGLContext;

/**
 *
 * @author thoorfr
 */
public interface IClickable {
    public static int BUTTON1=MouseEvent.BUTTON1;
    public static int BUTTON2=MouseEvent.BUTTON2;
    public static int BUTTON3=MouseEvent.BUTTON3;
    /**
     * Interface to receive the mouse clicked event
     * @param imagePosition the position clicked in the image
     * @param button the button clicked: BUTTON1 (left click), BUTTON2 or BUTTON3 (right click)
     * @param context the geographic GeoContext of the current display
     */
    public void mouseClicked(Point imagePosition, int button, OpenGLContext context);
}
