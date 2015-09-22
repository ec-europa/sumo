package org.geoimage.viewer.actions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

import org.geoimage.def.GeoImageReader;
import org.geoimage.utils.IMask;
import org.geoimage.viewer.core.SumoPlatform;
import org.geoimage.viewer.core.api.Argument;
import org.geoimage.viewer.core.api.iactions.AbstractAction;
import org.geoimage.viewer.core.factory.FactoryLayer;
import org.geoimage.viewer.core.gui.manager.LayerManager;
import org.geoimage.viewer.core.layers.visualization.vectors.MaskVectorLayer;
import org.geotools.geometry.jts.JTS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;

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
			if(args[0].equalsIgnoreCase("test")){
				FileWriter fw=null;
				BufferedWriter bw=null;
				try{
					GeoImageReader reader=SumoPlatform.getApplication().getCurrentImageReader();
					
					File f=new File(reader.getFilesList()[0]);
					fw = new FileWriter(f.getParent()+"\\invalidgeom.txt");
					bw = new BufferedWriter(fw);
					
					MaskVectorLayer mask=LayerManager.getIstanceManager().getChildMaskLayer(LayerManager.getIstanceManager().getCurrentImageLayer());
					List<Geometry> geoms=mask.getGeometries();
					for(int i=0;i<geoms.size();i++){
						Geometry g=geoms.get(i);
						if(!g.isValid()){
							System.out.println("Invalid geometry");
							
							fw.write(g.toText()+'\n');
							System.out.println("Corrected?:"+g.isValid());
						}	
					}
				}catch(Exception e){
					e.printStackTrace();
				}finally{
					try{
						bw.close();
						fw.close();
					}catch(Exception e){
					}	
				}	
				
			}else{
				Integer bufferSize=Integer.parseInt(args[0]);
				MaskVectorLayer mask=LayerManager.getIstanceManager().getChildMaskLayer(LayerManager.getIstanceManager().getCurrentImageLayer());
				// create new buffered mask with bufferingDistance using the mask in parameters
	            final IMask[] bufferedMask = new IMask[1];
	      		bufferedMask[0]=FactoryLayer.createMaskLayer("mask buffer_"+bufferSize, mask.getType(), bufferSize, ((MaskVectorLayer)mask).getGeometriclayer());
				LayerManager.getIstanceManager().addLayer(bufferedMask[0]);
			}	
		}
		return true;
	}

	@Override
	public List<Argument> getArgumentTypes() {
		// TODO Auto-generated method stub
		return null;
	}

}
