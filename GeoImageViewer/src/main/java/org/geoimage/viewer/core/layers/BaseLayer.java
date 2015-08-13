
package org.geoimage.viewer.core.layers;

import org.geoimage.opengl.OpenGLContext;
import org.geoimage.viewer.core.api.ILayer;

public class BaseLayer extends GenericLayer {
	


	public BaseLayer(ILayer parent) {
		super(parent,"",null,null);
		init(parent);
	}

	@Override
	public void render(OpenGLContext context) {

	}

	@Override
	public String getDescription() {
		return "Base Layer";
	}

	@Override
	public void dispose() {

	}

	
}
