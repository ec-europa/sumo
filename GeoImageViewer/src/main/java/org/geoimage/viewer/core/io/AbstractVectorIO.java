/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.core.io;

import java.util.List;
import java.util.Map;

import org.geoimage.def.GeoImageReader;
import org.geoimage.def.SarImageReader;
import org.geoimage.viewer.core.layers.GeometricLayer;

/**
 *
 * @author thoorfr
 */
public abstract class AbstractVectorIO {
	protected Map<String,Object> config;
    //protected GeoImageReader gir;
    protected String layername;
    
    
	public Map<String,Object>  getConfig() {
		return config;
	}

	public void setConfig(Map<String,Object>  config) {
		this.config = config;
	}

	public String getLayername() {
		return layername;
	}

	public void setLayername(String layername) {
		this.layername = layername;
	}

    protected AbstractVectorIO() {
    }

    public void setLayerName(String layername) {
        this.layername = layername;
    }

    

    public abstract GeometricLayer read(GeoImageReader reader);

    public abstract void save(GeometricLayer layer, String projection,SarImageReader gir);
    
    /**
     * For some reasons, you may execute some commands for some special datastore (clening database, ensure existence, or manual upload...)
     * @param commands
     * @return List of  Objects that describe the results of the associated commands
     */
    public List<? extends Object> executeCommands(List<String> commands) {
        return null;
    }
}
