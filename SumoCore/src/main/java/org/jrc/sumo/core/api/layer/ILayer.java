/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jrc.sumo.core.api.layer;


/**
 *
 * @author thoorfr
 */
public interface ILayer {
	public String getName();
    public void setName(String name);
    public void render();
    public boolean isActive();
    public void setActive(boolean active);
    public void setParent(ILayer parent);
    public ILayer getParent();
    public boolean isRadio();
    public String getDescription();
    public void dispose();
    public void init(ILayer parent);
    public String getType();
    
}
