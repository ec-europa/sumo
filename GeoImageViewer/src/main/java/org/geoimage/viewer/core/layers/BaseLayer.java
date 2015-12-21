
package org.geoimage.viewer.core.layers;

import org.geoimage.viewer.core.api.ilayer.ILayer;

public class BaseLayer extends GenericLayer {
	


	public BaseLayer(ILayer parent) {
		super(parent,"",null,null);
		init(parent);
	}

	@Override
	public void render(Object c) {

	}

	@Override
	public String getDescription() {
		return "Base Layer";
	}

	@Override
	public void dispose() {

	}

	
}
