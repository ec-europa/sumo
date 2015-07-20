
package org.geoimage.viewer.core.layers;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import org.geoimage.viewer.core.api.GeoContext;
import org.geoimage.viewer.core.api.ILayer;

public class BaseLayer extends GenericLayer {
	


	public BaseLayer(ILayer parent) {
		super(parent,"",null,null);
		init(parent);
	}

	@Override
	public void render(GeoContext context) {

	}

	@Override
	public String getDescription() {
		return "Base Layer";
	}

	@Override
	public void dispose() {

	}

	
}
