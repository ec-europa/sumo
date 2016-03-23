/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.JMenuItem;

import org.geoimage.analysis.BlackBorderAnalysis;
import org.geoimage.analysis.MaskGeometries;
import org.geoimage.analysis.VDSAnalysis;
import org.geoimage.def.GeoImageReader;
import org.geoimage.def.SarImageReader;
import org.geoimage.impl.ENL;
import org.geoimage.impl.TiledBufferedImage;
import org.geoimage.impl.s1.Sentinel1;
import org.geoimage.viewer.core.GeometryImage;
import org.geoimage.viewer.core.SumoPlatform;
import org.geoimage.viewer.core.analysisproc.AnalysisProcess;
import org.geoimage.viewer.core.analysisproc.VDSAnalysisProcessListener;
import org.geoimage.viewer.core.api.ilayer.ILayer;
import org.geoimage.viewer.core.api.ilayer.IMask;
import org.geoimage.viewer.core.factory.FactoryLayer;
import org.geoimage.viewer.core.gui.manager.LayerManager;
import org.geoimage.viewer.core.layers.image.ImageLayer;
import org.geoimage.viewer.core.layers.visualization.vectors.MaskVectorLayer;
import org.geoimage.viewer.widget.dialog.ActionDialog;

/**
 *
 * @author
 */
public class VDSAnalysisAction extends SumoAbstractAction implements  VDSAnalysisProcessListener{
    /**
	 *
	 */
	private static final long serialVersionUID = 3649669297745164880L;

    private GeoImageReader gir = null;
    private MaskVectorLayer coastlineMask = null;
    private MaskVectorLayer iceMasks = null;
    private AnalysisProcess proc=null;

    public VDSAnalysisAction() {
    	super("vds","Analysis/VDS");
    }


    public String getDescription() {
        return "Compute a VDS (Vessel Detection System) analysis.\n"
                + "Use \"vds k-dist 1.5 GSHHS\" to run a analysis with k-distribuion clutter model with a threshold of 1.5 using the land mask \"GSHHS...\"";
    }

    /**
     * run the analysis called from ActionDialog.
     *
     * @param
     * @return true, if successful
     */
    public boolean execute() {
	    	status=STATUS_RUNNING;
	    	iceMasks=null;
	    	coastlineMask=null;
	        // initialise the buffering distance value
	        int bufferingDistance = Double.valueOf((SumoPlatform.getApplication().getConfiguration()).getBufferingDistance()).intValue();
	        ImageLayer currentImgLayer=LayerManager.getIstanceManager().getCurrentImageLayer();

	        if (paramsAction.size() < 2) {
	            return true;
	        } else {
	        	try{
	        		if (paramsAction.get("algorithm").equals("k-dist")) {
	        			done = false;


	        			GeoImageReader reader = ((ImageLayer) currentImgLayer).getImageReader();
	        			if (reader instanceof SarImageReader || reader instanceof TiledBufferedImage) {
	        				gir = reader;
	        			}
	        			if (gir == null) {
	        				done = true;
	        				return false;
	        			}

	        			//this part mange the different thresholds for different bands
	        			//in particular is also looking for which band is available and leave the threshold to 0 for the not available bands
	        			java.util.HashMap<String,Float> thresholds = new java.util.HashMap<>();

	        			int numberofbands = gir.getNBand();
	        			thresholds.put("HH",0.0f);
	        			thresholds.put("HV",0.0f);
	        			thresholds.put("VH",0.0f);
	        			thresholds.put("VV",0.0f);
	        			for (int bb = 0; bb < numberofbands; bb++) {
	        				if (gir.getBandName(bb).equals("HH") || gir.getBandName(bb).equals("H/H")) {
	        					thresholds.put("HH", Float.parseFloat(paramsAction.get("thresholdHH")));
	        				} else if (gir.getBandName(bb).equals("HV") || gir.getBandName(bb).equals("H/V")) {
	        					thresholds.put("HV", Float.parseFloat(paramsAction.get("thresholdHV")));
	        				} else if (gir.getBandName(bb).equals("VH") || gir.getBandName(bb).equals("V/H")) {
	        					thresholds.put("VH", Float.parseFloat(paramsAction.get("thresholdVH")));
	        				} else if (gir.getBandName(bb).equals("VV") || gir.getBandName(bb).equals("V/V")) {
	        					thresholds.put("VV", Float.parseFloat(paramsAction.get("thresholdVV")));
	        				}
	        			}
	        			//read the land mask
	        			for (ILayer l : LayerManager.getIstanceManager().getChilds(currentImgLayer)) {
	        				if (l instanceof IMask ) {
	        					if( ((MaskVectorLayer) l).getMaskType()==MaskVectorLayer.COASTLINE_MASK){
	        						if( !"".equals(paramsAction.get("coastline"))&&l.getName().startsWith(paramsAction.get("coastline")))
	        							coastlineMask=(MaskVectorLayer) l;
	        					}else if( ((MaskVectorLayer) l).getMaskType()==MaskVectorLayer.ICE_MASK){
	        						if(!"".equals(paramsAction.get("ice"))&& l.getName().startsWith(paramsAction.get("ice")))
	        							iceMasks=(MaskVectorLayer) l;
	        					}else{

	        					}
	        				}
	        			}
	        			//read the buffer distance
	        			bufferingDistance = Integer.parseInt(paramsAction.get("Buffer"));
	        			final float ENL = Float.parseFloat(paramsAction.get("ENL"));

	        			IMask bufferedMask=null;
	        			if(coastlineMask!=null)
	        				bufferedMask=FactoryLayer.createMaskLayer(coastlineMask.getName(),
	        						coastlineMask.getType(),
	        						bufferingDistance,
	        						((MaskVectorLayer)coastlineMask).getGeometriclayer(),
	        						coastlineMask.getMaskType());

	        			IMask iceMask=null;
	        			if(iceMasks!=null)
	        				iceMask=FactoryLayer.createMaskLayer(iceMasks.getName(),
	        						iceMasks.getType(),
	        						bufferingDistance,
	        						((MaskVectorLayer)iceMasks).getGeometriclayer(),
	        						iceMasks.getMaskType());

	        			MaskGeometries mg=null;
	        			if(bufferedMask!=null)
	        				mg=new MaskGeometries(bufferedMask.getName(),bufferedMask.getGeometries());

	        			MaskGeometries icemg=null;
	        			if(iceMask!=null)
	        				icemg=new MaskGeometries(iceMask.getName(),iceMask.getGeometries());

	        			VDSAnalysis vdsanalysis = new VDSAnalysis((SarImageReader) gir, mg,icemg, ENL, thresholds,
	        					currentImgLayer.getRealTileSizeX(),currentImgLayer.getRealTileSizeY(),
	        					currentImgLayer.getHorizontalTilesImage(),currentImgLayer.getVerticalTilesImage());


	        			BlackBorderAnalysis blackBorderAnalysis=null;
	        			if(gir instanceof Sentinel1){
	        				if(vdsanalysis.getCoastMask()!=null)
	        					blackBorderAnalysis= new BlackBorderAnalysis(gir,currentImgLayer.getRealTileSizeX(),
	        							currentImgLayer.getRealTileSizeY(),vdsanalysis.getCoastMask().getMaskGeometries());
	        				else
	        					blackBorderAnalysis= new BlackBorderAnalysis(gir,currentImgLayer.getRealTileSizeX(),
	        							currentImgLayer.getRealTileSizeY(),null);
	        			}

	        			proc=new AnalysisProcess(reader,ENL, vdsanalysis, bufferingDistance,0,blackBorderAnalysis);
	        			proc.addProcessListener(this);

	        			SumoPlatform.getApplication().getConsoleLayer().setCurrentAction(this);


	        			//Thread t=new Thread(proc);
	        			//t.setName("VDS_analysis_"+gir.getDisplayName(0));
	        			//t.start();

	        			Thread.currentThread().setName("VDS_analysis_"+gir.getDisplayName(0));
	        			proc.call();
	        		}
	        	}finally{
	        		proc.dispose();
	        		if(status!=STATUS_END){
	        			endAnalysis(gir.getDisplayName(0));
	        		}
	        	}
            return true;
        }
    }


    /**
     *
     */
    public List<ActionDialog.Argument> getArgumentTypes() {
        List<ActionDialog.Argument> out = new ArrayList<ActionDialog.Argument>();

        final ActionDialog.Argument a1 = new ActionDialog.Argument("algorithm", ActionDialog.Argument.STRING, false, "k-dist","algorithm");
        a1.setPossibleValues(new Object[]{"k-dist"});
        final ActionDialog.Argument a2 = new ActionDialog.Argument("thresholdHH", ActionDialog.Argument.FLOAT, false, 1.5,"thresholdHH");
        final ActionDialog.Argument a21 = new ActionDialog.Argument("thresholdHV", ActionDialog.Argument.FLOAT, false, 1.2,"thresholdHV");
        final ActionDialog.Argument a22 = new ActionDialog.Argument("thresholdVH", ActionDialog.Argument.FLOAT, false, 1.5,"thresholdVH");
        final ActionDialog.Argument a23 = new ActionDialog.Argument("thresholdVV", ActionDialog.Argument.FLOAT, false, 1.2,"thresholdVV");

        final ActionDialog.Argument a3 = new ActionDialog.Argument("coastline", ActionDialog.Argument.STRING, true, "no mask choosen","coastline");
        final ActionDialog.Argument a31= new ActionDialog.Argument("ice", ActionDialog.Argument.STRING, true, "no mask choosen","ice");


        final ArrayList<String> coasts = new ArrayList<String>();
        coasts.add("");
        final ArrayList<String> ice = new ArrayList<String>();
        ice.add("");
        final ImageLayer il=LayerManager.getIstanceManager().getCurrentImageLayer();

        if (il != null) {
          //  for (ILayer l : il.getLayers()) {
            for (ILayer l : LayerManager.getIstanceManager().getChilds(il)) {
                if (l instanceof MaskVectorLayer && !((MaskVectorLayer) l).getType().equals(GeometryImage.POINT)) {
                	if(((MaskVectorLayer )l).getMaskType()==MaskVectorLayer.COASTLINE_MASK){
                		coasts.add(l.getName());
                	}else if(((MaskVectorLayer )l).getMaskType()==MaskVectorLayer.ICE_MASK){
                		ice.add(l.getName());
                	}
                }
            }
        }
        a3.setPossibleValues(coasts.toArray());
        a31.setPossibleValues(ice.toArray());
        out.add(a3);
        out.add(a31);


        ActionDialog.Argument a4 = new ActionDialog.Argument("Buffer",
        		ActionDialog.Argument.FLOAT, false,
        		SumoPlatform.getApplication().getConfiguration().getBufferingDistance(),"Buffer (pixels)");

        //management of the different threshold in the VDS parameters panel
        out.add(a1);
        int numberofbands = il.getImageReader().getNBand();
        for (int bb = 0; bb < numberofbands; bb++) {
            if (il.getImageReader().getBandName(bb).equals("HH") || il.getImageReader().getBandName(bb).equals("H/H")) {
                out.add(a2);
            } else if (il.getImageReader().getBandName(bb).equals("HV") || il.getImageReader().getBandName(bb).equals("H/V")) {
                out.add(a21);
            } else if (il.getImageReader().getBandName(bb).equals("VH") || il.getImageReader().getBandName(bb).equals("V/H")) {
                out.add(a22);
            } else if (il.getImageReader().getBandName(bb).equals("VV") || il.getImageReader().getBandName(bb).equals("V/V")) {
                out.add(a23);
            }
        }


        out.add(a4);
        if (il.getImageReader() instanceof SarImageReader) {
        	ActionDialog.Argument aEnl = new ActionDialog.Argument("ENL", ActionDialog.Argument.FLOAT, false,
        			ENL.getFromGeoImageReader((SarImageReader) il.getImageReader()),"ENL");
            out.add(aEnl);
        }

        return out;
    }




	@Override
	public void startAnalysis(String imgName) {
		String message="Starting VDS Analysis:"+imgName;
		super.notifyEvent(new SumoActionEvent(SumoActionEvent.STARTACTION,message ,0,5));
	}
	@Override
	public void performVDSAnalysis(String message,int numSteps) {
			this.actionSteps=numSteps;
			super.notifyEvent(new SumoActionEvent(SumoActionEvent.UPDATE_STATUS,message ,1,numSteps));
	}
	@Override
	public void startBlackBorederAnalysis(String message) {
			super.notifyEvent(new SumoActionEvent(SumoActionEvent.UPDATE_STATUS,message ,2,5));
	}
	@Override
	public void startAnalysisBand(String message) {
			super.notifyEvent(new SumoActionEvent(SumoActionEvent.UPDATE_STATUS,message ,3,5));
	}

	@Override
	public void calcAzimuthAmbiguity(String message) {
			super.notifyEvent(new SumoActionEvent(SumoActionEvent.UPDATE_STATUS,message ,4,5));
	}

	@Override
	public void agglomerating(String message) {
			super.notifyEvent(new SumoActionEvent(SumoActionEvent.UPDATE_STATUS,message ,5,5));
	}

	@Override
	public void nextVDSAnalysisStep(int numSteps){
			super.notifyEvent(new SumoActionEvent(SumoActionEvent.UPDATE_STATUS,null,numSteps,actionSteps));
	}


	@Override
	public void endAnalysis(String imageName) {
		super.notifyEvent(new SumoActionEvent(SumoActionEvent.ENDACTION,"End Analysis",-1));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() instanceof JMenuItem){
			super.actionPerformed(e);
		}else{
			if(proc!=null&&e.getActionCommand().equals("STOP")){
				this.proc.setStop(true);
				status=STATUS_CANCELLED;
				super.notifyEvent(new SumoActionEvent(SumoActionEvent.STOP_ACTION,"ANALYSIS CANCELLED",-1));
			}
		}
	}

	@Override
	public void layerReady(ILayer layer) {
		if(!SumoPlatform.isBatchMode()){
			LayerManager.getIstanceManager().addLayer(layer);
		}

	}



}
