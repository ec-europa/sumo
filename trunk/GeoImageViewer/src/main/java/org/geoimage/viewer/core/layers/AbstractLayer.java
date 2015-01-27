package org.geoimage.viewer.core.layers;

import org.geoimage.viewer.core.api.ILayer;

public abstract class AbstractLayer implements ILayer{
	private String name = "";
	private boolean active = true;
	protected boolean isRadio = false;
	    
	
	
	public boolean isActive() {
        return active;
    }

	public void setActive(boolean active) {
        this.active=active;
    }
	
	public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
	
	
    public boolean isRadio() {
        return isRadio;
    }

    public void setIsRadio(boolean radio) {
        isRadio = radio;
    }
    
   

	
}
