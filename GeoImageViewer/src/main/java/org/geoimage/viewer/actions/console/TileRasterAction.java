package org.geoimage.viewer.actions.console;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.geoimage.analysis.MaskGeometries;
import org.geoimage.def.SarImageReader;
import org.geoimage.utils.PolygonOp;
import org.geoimage.viewer.core.SumoPlatform;
import org.geoimage.viewer.core.analysisproc.AnalysisProcess;
import org.geoimage.viewer.core.api.ilayer.ILayer;
import org.geoimage.viewer.core.api.ilayer.IMask;
import org.geoimage.viewer.core.gui.manager.LayerManager;
import org.geoimage.viewer.core.layers.GenericLayer;
import org.geoimage.viewer.core.layers.GeometricLayer;
import org.geoimage.viewer.core.layers.SumoActionEvent;
import org.geoimage.viewer.core.layers.image.ImageLayer;
import org.geoimage.viewer.core.layers.visualization.vectors.ComplexEditGeometryVectorLayer;
import org.geoimage.viewer.core.layers.visualization.vectors.MaskVectorLayer;
import org.geoimage.viewer.core.layers.visualization.vectors.SimpleGeometryLayer;
import org.geoimage.viewer.widget.dialog.ActionDialog.Argument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;

public class TileRasterAction extends AbstractConsoleAction {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private Logger logger = LoggerFactory.getLogger(TileRasterAction.class);
	boolean done = false;
	private AnalysisProcess proc = null;

	public TileRasterAction() {
		super("raster", "None");
	}

	@Override
	public String getDescription() {
		return "Create rasters for a tile ";
	}

	@Override
	public boolean executeFromConsole() {
		try {
			SarImageReader sar=(SarImageReader) SumoPlatform.getApplication().getCurrentImageReader();
			ImageLayer layer=LayerManager.getIstanceManager().getCurrentImageLayer();

			if(layer!=null && commandLine.length>=3){
				String arg0=commandLine[0];
				//run vds analysis on a single tile
				if(arg0.equalsIgnoreCase("raster")){
					String arg1=commandLine[1];
					String arg2=commandLine[2];
					String folderOut=null;
					if(commandLine.length==4){
						String arg3=commandLine[3];
						folderOut=arg3;
					}
					int row=Integer.parseInt(arg1);
					int col=Integer.parseInt(arg2);

					int[] mergedDataMask=null;
					//read the land mask
					List<ILayer> childs=LayerManager.getIstanceManager().getChilds(layer);
					for (ILayer l : childs) {
						if (l instanceof IMask ) {
							MaskVectorLayer mask=(MaskVectorLayer) l;
							MaskGeometries	mg=new MaskGeometries(mask.getName(), mask.getGeometries());


							if(folderOut==null)
								folderOut=SumoPlatform.getApplication().getCachePath();

							File output=new File(folderOut+File.separator+mask.getName()+row+"_"+col+".bmp");

							int y=layer.getRealTileSizeY()*row;
							int x=layer.getRealTileSizeX()*col;

							boolean saved=mg.saveRaster(x, y, layer.getRealTileSizeX(),layer.getRealTileSizeY(), 0, 0, 1, output);
							if(mergedDataMask==null){
								mergedDataMask=mg.getRasterDataMask(x, y, layer.getRealTileSizeX(),layer.getRealTileSizeY(), 0, 0, 1);//new int [ layer.getRealTileSizeX()*layer.getRealTileSizeY()];
							}else{
								int[] dataMaskTmp=mg.getRasterDataMask(x, y, layer.getRealTileSizeX(),layer.getRealTileSizeY(), 0, 0, 1);
								for (int i=0;i<dataMaskTmp.length;i++){
									if(mergedDataMask[i]==0){
										if(dataMaskTmp[i]==1)
											mergedDataMask[i]=1;
									}
								}
							}

						}
					}
					if(mergedDataMask!=null){
						//save merged data mask to bitmap
						// Let's create a BufferedImage for a binary image.
						BufferedImage im = new BufferedImage(layer.getRealTileSizeX(),layer.getRealTileSizeY(),BufferedImage.TYPE_BYTE_BINARY);
						// We need its raster to set the pixels' values.
						WritableRaster raster = im.getRaster();
						// Put the pixels on the raster. Note that only values 0 and 1 are used for the pixels. 1=white
						int colS=0;
						int rowS=0;
						for(int h=0;h<mergedDataMask.length;h++){
							raster.setSample(colS,rowS,0,mergedDataMask[h]); // checkerboard pattern.
							colS++;
							if(colS%layer.getRealTileSizeX()==0){
								colS=0;
								rowS++;
							}
						}
						// Store the image using the PNG format.
						ImageIO.write(im,"PNG",new File(folderOut+File.separator+"merged_"+row+"_"+col+".png"));
					}

					int yy=layer.getRealTileSizeY()*row;
					int xx=layer.getRealTileSizeX()*col;
					try {
						com.vividsolutions.jts.geom.Polygon box;
						Coordinate cc[]=new Coordinate[5];
						cc[0]=new Coordinate(xx,yy);
						cc[1]=new Coordinate(xx+layer.getRealTileSizeX(),yy);
						cc[2]=new Coordinate(xx+layer.getRealTileSizeY(),yy+layer.getRealTileSizeX());
						cc[3]=new Coordinate(xx,yy+layer.getRealTileSizeY());
						cc[4]=new Coordinate(xx,yy);
						box = PolygonOp.createPolygon(cc);

						//GeometricLayer gml=new GeometricLayer("test raster",GeometricLayer.POLYGON,Arrays.asList(cc));
						List< Geometry> geoms=new ArrayList<>();
						geoms.add(box);
						SimpleGeometryLayer gl=new SimpleGeometryLayer(layer, "test raster", geoms,GeometricLayer.POLYGON);
						LayerManager.getIstanceManager().addLayer(gl);
					} catch (Exception e) {
						logger.warn("layer not added");
					}

				}
			}
		}catch(

				Exception e)
		{
			logger.error(e.getMessage());
			return false;
		}finally
		{
		}return true;
	}

	@Override
	public List<Argument> getArgumentTypes() {
		List <Argument> args=new  ArrayList<Argument>();
		return args;
	}

	public void nextVDSAnalysisStep(int numSteps) {
		super.notifyEvent(new SumoActionEvent(SumoActionEvent.UPDATE_STATUS, "", numSteps));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (proc != null && e.getActionCommand().equals("STOP")) {
			this.proc.setStop(true);

			SumoPlatform.getApplication().getMain().removeStopListener(this);
			this.proc = null;
		}
	}

	@Override
	public String getCommand() {
		return "raster";
	}

	@Override
	public boolean execute() {
		return executeFromConsole();
	}

}
