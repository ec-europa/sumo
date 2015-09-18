package org.geoimage.viewer.actions;

import java.util.List;

import org.geoimage.utils.IMask;
import org.geoimage.viewer.core.api.Argument;
import org.geoimage.viewer.core.api.iactions.AbstractAction;
import org.geoimage.viewer.core.factory.FactoryLayer;
import org.geoimage.viewer.core.gui.manager.LayerManager;
import org.geoimage.viewer.core.layers.visualization.vectors.MaskVectorLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateBufferedMask extends AbstractAction{
	private Logger logger = LoggerFactory.getLogger(CreateBufferedMask.class);

	@Override
	public String getName() {
		return "buffer";
	}

	@Override
	public String getDescription() {
		return "---";
	}

	@Override
	public String getPath() {
		return "Tools/buffer";
	}

	@Override
	public boolean execute(String[] args) {
		if(args.length>=1){
			Integer bufferSize=Integer.parseInt(args[0]);
			
			MaskVectorLayer mask=LayerManager.getIstanceManager().getChildMaskLayer(LayerManager.getIstanceManager().getCurrentImageLayer());
			// create new buffered mask with bufferingDistance using the mask in parameters
            final IMask[] bufferedMask = new IMask[1];
            
      		bufferedMask[0]=FactoryLayer.createMaskLayer("mask buffer_"+bufferSize, mask.getType(), bufferSize, ((MaskVectorLayer)mask).getGeometriclayer());

			LayerManager.getIstanceManager().addLayer(bufferedMask[0]);
		}
		return true;
	}

	@Override
	public List<Argument> getArgumentTypes() {
		// TODO Auto-generated method stub
		return null;
	}

}
