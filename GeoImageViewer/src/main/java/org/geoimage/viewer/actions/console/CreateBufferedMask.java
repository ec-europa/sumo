/*
 * 
 */
package org.geoimage.viewer.actions.console;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Iterator;
import java.util.List;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
import org.geoimage.def.GeoImageReader;
import org.geoimage.def.SarImageReader;
import org.geoimage.viewer.core.GeometryImage;
import org.geoimage.viewer.core.SumoPlatform;
import org.geoimage.viewer.core.api.ilayer.IMask;
import org.geoimage.viewer.core.factory.FactoryLayer;
import org.geoimage.viewer.core.gui.manager.LayerManager;
import org.geoimage.viewer.core.io.SimpleShapefile;
import org.geoimage.viewer.core.layers.visualization.vectors.MaskVectorLayer;
import org.geoimage.viewer.widget.dialog.ActionDialog.Argument;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;

public class CreateBufferedMask extends AbstractConsoleAction{
	//private Logger logger = LogManager.getLogger(CreateBufferedMask.class);

	public CreateBufferedMask() {
		super("buffer","none");
	}


	@Override
	public String getDescription() {
		return "---";
	}

	@Override
	public boolean execute() {
		if(paramsAction.size()>=1){
			super.commandLine=new String[1];
			Iterator<String> it=paramsAction.values().iterator();
			super.commandLine[0]=it.next();
			return executeFromConsole();
		}
		return false;
	}

	@Override
	public boolean executeFromConsole() {
			String arg=super.commandLine[0];
			final GeoImageReader reader=SumoPlatform.getApplication().getCurrentImageReader();
			if(arg.equalsIgnoreCase("test")){
				FileWriter fw=null;
				BufferedWriter bw=null;
				try{
					File f=new File(reader.getFilesList()[0]);
					fw = new FileWriter(f.getParent()+"\\invalidgeom.txt");
					bw = new BufferedWriter(fw);

					MaskVectorLayer mask=LayerManager.getIstanceManager().getChildMaskLayer(LayerManager.getIstanceManager().getCurrentImageLayer());
					List<Geometry> geoms=mask.getGeometries();
					for(int i=0;i<geoms.size();i++){
						Geometry g=geoms.get(i);
						if(!g.isValid()){
							System.out.println("Invalid geometry");
						//	Geometry geo=reader.getGeoTransform().transformGeometryGeoFromPixel(g);
							fw.write(g.toText()+'\n');
						//	fw.write(geo.toText()+'\n');
							fw.write('\n');
							//System.out.println("Corrected?:"+g.isValid());
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

			}else if(arg.equalsIgnoreCase("testreload")){
				String fname="F:\\SumoImgs\\coastline\\OSMLandPoly_20141001_250\\OSMLandPoly_20141001_250m.shp";
					GeometryImage shpLayer=null;
					try{
						 shpLayer=SimpleShapefile.createIntersectedLayer(new File(fname),((SarImageReader)reader).getBbox(100),reader.getGeoTransform());
						final List<Geometry> geoms=shpLayer.getGeometries();
						Thread check=new Thread(){
							public void run(){
								try {
									for(int i=0;i<geoms.size();i++){
										Geometry g=reader.getGeoTransform().transformGeometryPixelFromGeo(geoms.get(i));
										if(!g.isValid()){
											System.out.println("Invalid geometry");
											Geometry simplify=TopologyPreservingSimplifier.simplify(geoms.get(i),0.003);
											simplify=reader.getGeoTransform().transformGeometryPixelFromGeo(geoms.get(i));
											System.out.println("Simplify:"+simplify.isValid());
											if(!simplify.isValid()){
												simplify=TopologyPreservingSimplifier.simplify(geoms.get(i),0.005);
												simplify=reader.getGeoTransform().transformGeometryPixelFromGeo(geoms.get(i));
												System.out.println("Simplify2 0.005:"+simplify.isValid());
											}
											//Geometry geo=reader.getGeoTransform().transformGeometryGeoFromPixel(g);
											//fw.write(g.toText()+'\n');
											//fw.write('\n');
										}
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						};
						check.start();
					}catch(Exception e){e.printStackTrace();}

			}else{
				Integer bufferSize=Integer.parseInt(arg);
				MaskVectorLayer mask=LayerManager.getIstanceManager().getChildMaskLayer(LayerManager.getIstanceManager().getCurrentImageLayer());
				// create new buffered mask with bufferingDistance using the mask in parameters
	            final IMask[] bufferedMask = new IMask[1];
	      		bufferedMask[0]=FactoryLayer.createMaskLayer("mask buffer_"+bufferSize,mask.getType(),
	      				bufferSize,((MaskVectorLayer)mask).getGeometriclayer(),MaskVectorLayer.COASTLINE_MASK);
				LayerManager.getIstanceManager().addLayer(bufferedMask[0]);
			}
		return true;
	}

	@Override
	public List<Argument> getArgumentTypes() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String getCommand() {
		// TODO Auto-generated method stub
		return null;
	}




}
