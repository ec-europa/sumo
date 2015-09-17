package org.geoimage.viewer.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import org.geoimage.analysis.BlackBorderAnalysis;
import org.geoimage.analysis.VDSAnalysis;
import org.geoimage.def.GeoImageReader;
import org.geoimage.def.SarImageReader;
import org.geoimage.impl.s1.Sentinel1;
import org.geoimage.utils.IMask;
import org.geoimage.utils.IProgress;
import org.geoimage.viewer.core.SumoPlatform;
import org.geoimage.viewer.core.analysisproc.AnalysisProcess;
import org.geoimage.viewer.core.analysisproc.VDSAnalysisProcessListener;
import org.geoimage.viewer.core.api.Argument;
import org.geoimage.viewer.core.api.ILayer;
import org.geoimage.viewer.core.api.iactions.AbstractAction;
import org.geoimage.viewer.core.factory.FactoryLayer;
import org.geoimage.viewer.core.gui.manager.LayerManager;
import org.geoimage.viewer.core.layers.GeometricLayer;
import org.geoimage.viewer.core.layers.image.ImageLayer;
import org.geoimage.viewer.core.layers.visualization.vectors.MaskVectorLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;


public class TileAnalysisAction extends AbstractAction implements VDSAnalysisProcessListener,IProgress,ActionListener{
	private Logger logger = LoggerFactory.getLogger(TileAnalysisAction.class);
	boolean done=false;
	private int current = 0;
    private int maximum = 3;
    private boolean indeterminate;
    private String message = "";
    private boolean stopping=false;
    private AnalysisProcess proc=null;
    
    
	@Override
	public String getName() {
		return "chktile";
	}

	@Override
	public String getDescription() {
		return "Analyze Tile Values";
	}

	@Override
	public String getPath() {
		return "Tools/CheckTile";
	}

	private void runBBAnalysis(){
		//run the blackborder analysis for the s1 images
		BlackBorderAnalysis blackBorderAnalysis=null;
		GeoImageReader gir=SumoPlatform.getApplication().getCurrentImageReader();
        if(gir instanceof Sentinel1){
                /*MaskVectorLayer mv=null;
           	 	if(bufferedMask!=null&&bufferedMask.length>0)
           	 		mv=(MaskVectorLayer)bufferedMask[0];
           	 	if(mv!=null)
           	 		blackBorderAnalysis= new BlackBorderAnalysis(gir,mv.getGeometries());
           	 	else*/ 
           	blackBorderAnalysis= new BlackBorderAnalysis(gir,null);
         } 	
         if(blackBorderAnalysis!=null){
        	 int nTile=SumoPlatform.getApplication().getConfiguration().getNumTileBBAnalysis();
        	 blackBorderAnalysis.analyse(nTile,BlackBorderAnalysis.ANALYSE_ALL);
        	 List<Coordinate>cc=blackBorderAnalysis.getCoordinatesThresholds();
        	 GeometricLayer bbanal=new GeometricLayer("BBAnalysis",
        			 GeometricLayer.POINT,
        			 cc
        			 );
        	 LayerManager.addLayerInThread(
        			 GeometricLayer.POINT, 
        			 bbanal, 
        			 LayerManager.getIstanceManager().getCurrentImageLayer());
        	 
         }
         //end blackborder analysis
	}
	
	
	@Override
	public boolean execute(String[] args) {
		try {
			SarImageReader sar=(SarImageReader) SumoPlatform.getApplication().getCurrentImageReader();
			ImageLayer layer=LayerManager.getIstanceManager().getCurrentImageLayer();
			
			if(layer!=null && args.length>=2){
				if(args[0].equalsIgnoreCase("bb")){
					if(args.length==2 && args[1].equalsIgnoreCase("test")){
						runBBAnalysis();
					}else{
						int row=Integer.parseInt(args[1]);
						int col=Integer.parseInt(args[2]);
						String direction="H"; //h= horizontal v=vertical
						if(args.length==4)
							direction=args[2];
						BlackBorderAnalysis borderAn=new BlackBorderAnalysis(sar,0,null);
						borderAn.analyse(row,col,direction.equalsIgnoreCase("H"));
					}	
				}else if(args[0].equalsIgnoreCase("vds")){
					
					int row=Integer.parseInt(args[1]);
					int col=Integer.parseInt(args[2]);
					Float hh=Float.parseFloat(args[3]);
					Float hv=Float.parseFloat(args[4]);
					Float vh=Float.parseFloat(args[5]);
					Float vv=Float.parseFloat(args[6]);
					
					//read the land mask
					ArrayList<IMask> mask = new ArrayList<IMask>();
	                for (ILayer l : LayerManager.getIstanceManager().getChilds(layer)) {
	                    if (l instanceof IMask ) {
	                        mask.add((IMask) l);
	                    }
	                }
					// create new buffered mask with bufferingDistance using the mask in parameters
	                final IMask[] bufferedMask = new IMask[mask.size()];
	                for (int i=0;i<mask.size();i++) {
	                	IMask maskList = mask.get(i);
	               		bufferedMask[i]=FactoryLayer.createMaskLayer(maskList.getName(), maskList.getType(), 0, ((MaskVectorLayer)maskList).getGeometriclayer());
	                }
					
	                VDSAnalysis analysis = new VDSAnalysis(sar, null, Float.parseFloat(sar.getENL()), new Float[]{hh,hv,vh,vv});
					analysis.setAnalyseSingleTile(true);
					analysis.setxTileToAnalyze(col);
					analysis.setyTileToAnalyze(row);
					proc=new AnalysisProcess(sar,Float.parseFloat(sar.getENL()), analysis, bufferedMask,0,0);
					proc.addProcessListener(this);
					Thread t=new Thread(proc);
	                t.setName("VDS_analysis_"+sar.getDisplayName(0));
	                t.start();
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			proc.removeProcessListener(this);
			proc.dispose();
			
			return false;
		}finally{
		}
		return true;
	}

	@Override
	public List<Argument> getArgumentTypes() {
		List <Argument> args=new  ArrayList<Argument>();
		return args;
	}

	 public void setCurrent(int i) {
	        current = i;
	    }

	    public void setMaximum(int size) {
	        maximum = size;
	    }

	    public void setMessage(String string) {
	        message =  string;
	    }

	    public void setIndeterminate(boolean value) {
	        indeterminate = value;
	    }

	    public void setDone(boolean value) {
	        done = value;
	    }
	
	
	@Override
	public void startAnalysis() {
		setCurrent(1);
		message="Starting VDS Analysis";
	}
	@Override
	public void performVDSAnalysis(String message,int numSteps) {
		if(!stopping){
			setMaximum(numSteps);
			setCurrent(1);
			this.message=message;
		}	
	}

	@Override
	public void startAnalysisBand(String message) {
		if(!stopping){
			setCurrent(2);
			this.message=message;
		}	
	}

	@Override
	public void calcAzimuthAmbiguity(String message) {
		if(!stopping){
			setCurrent(3);
			this.message=message;
		}	
	}

	@Override
	public void agglomerating(String message) {
		if(!stopping){
			setCurrent(4);
			this.message=message;
		}	
	}

	public void nextVDSAnalysisStep(int numSteps){
		//setMessage(numSteps+"/"+maximum);
		setCurrent(numSteps);
	}


	@Override
	public void endAnalysis() {
		setDone(true);
		SumoPlatform.getApplication().getMain().removeStopListener(this);
		
		if(proc!=null)
			proc.dispose();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(proc!=null&&e.getActionCommand().equals("STOP")){
			this.proc.setStop(true);
			this.message="stopping";
			SumoPlatform.getApplication().getMain().removeStopListener(this);
			this.proc=null;
		}	
	}

	@Override
	public void layerReady(ILayer layer) {
		if(!SumoPlatform.isBatchMode()){
			LayerManager.getIstanceManager().addLayer(layer);
		}
	}

	public boolean isIndeterminate() {
        return this.indeterminate;
    }

    public boolean isDone() {
        return this.done;
    }

    public int getMaximum() {
        return this.maximum;
    }

    public int getCurrent() {
        return this.current;
    }

    public String getMessage() {
        return this.message;
    }

	@Override
	public void startBlackBorederAnalysis(String message) {
		if(!stopping){
			setCurrent(1);
			this.message=message;
		}	
		
	}


}
