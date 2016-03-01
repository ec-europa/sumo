/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuItem;

import org.geoimage.analysis.MaskGeometries;
import org.geoimage.analysis.VDSAnalysis;
import org.geoimage.def.GeoImageReader;
import org.geoimage.def.SarImageReader;
import org.geoimage.impl.ENL;
import org.geoimage.impl.TiledBufferedImage;
import org.geoimage.viewer.core.SumoPlatform;
import org.geoimage.viewer.core.analysisproc.AnalysisProcess;
import org.geoimage.viewer.core.analysisproc.VDSAnalysisProcessListener;
import org.geoimage.viewer.core.api.ilayer.ILayer;
import org.geoimage.viewer.core.api.ilayer.IMask;
import org.geoimage.viewer.core.factory.FactoryLayer;
import org.geoimage.viewer.core.gui.manager.LayerManager;
import org.geoimage.viewer.core.layers.GeometricLayer;
import org.geoimage.viewer.core.layers.image.ImageLayer;
import org.geoimage.viewer.core.layers.visualization.vectors.MaskVectorLayer;
import org.geoimage.viewer.util.IProgress;
import org.geoimage.viewer.widget.dialog.ActionDialog;

/**
 *
 * @author
 */
public class VDSAnalysisConsoleAction extends SumoAbstractAction implements  IProgress,VDSAnalysisProcessListener,ActionListener{
    private String message = "";


    private int current = 0;
    private int maximum = 3;
    private boolean done = false;
    private boolean indeterminate;
    private GeoImageReader gir = null;
    private List<MaskVectorLayer> coastlineMask = null;
    private List<MaskVectorLayer> iceMask = null;
    private AnalysisProcess proc=null;
    private boolean stopping=false;

    public VDSAnalysisConsoleAction() {
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
        // initialise the buffering distance value
        int bufferingDistance = Double.valueOf((SumoPlatform.getApplication().getConfiguration()).getBufferingDistance()).intValue();
        SumoPlatform.getApplication().getMain().addStopListener(this);

        if (paramsAction.size() < 2) {
            return true;
        } else {

            if (paramsAction.get("algorithm").equals("k-dist")) {
                done = false;

                ImageLayer cl=LayerManager.getIstanceManager().getCurrentImageLayer();
                GeoImageReader reader = ((ImageLayer) cl).getImageReader();
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
                coastlineMask = new ArrayList<MaskVectorLayer>();
                iceMask = new ArrayList<MaskVectorLayer>();
                for (ILayer l : LayerManager.getIstanceManager().getChilds(cl)) {
                    if (l instanceof IMask ) {
                    	if( ((MaskVectorLayer) l).getMaskType()==MaskVectorLayer.COASTLINE_MASK){
                    		if( l.getName().startsWith(paramsAction.get("coastline")))
                    			coastlineMask.add((MaskVectorLayer) l);
                    	}else if( ((MaskVectorLayer) l).getMaskType()==MaskVectorLayer.ICE_MASK){
                    		if( l.getName().startsWith(paramsAction.get("ice")))
                    			iceMask.add((MaskVectorLayer) l);
                    	}else{

                    	}
                    }
                }
                //read the buffer distance
                bufferingDistance = Integer.parseInt(paramsAction.get("Buffer"));
                final float ENL = Float.parseFloat(paramsAction.get("ENL"));

                // create new buffered mask with bufferingDistance using the mask in parameters
                final IMask[] bufferedMask = new IMask[coastlineMask.size()];

                for (int i=0;i<coastlineMask.size();i++) {
                	MaskVectorLayer maskList = coastlineMask.get(i);
               		bufferedMask[i]=FactoryLayer.createMaskLayer(maskList.getName(), maskList.getType(),
               				bufferingDistance,
               				((MaskVectorLayer)maskList).getGeometriclayer(),
               				maskList.getMaskType());
                }
                MaskGeometries mg=null;
                if(bufferedMask!=null&&bufferedMask.length>0)
                	mg=new MaskGeometries(bufferedMask[0].getGeometries());


                VDSAnalysis vdsanalysis = new VDSAnalysis((SarImageReader) gir, mg, ENL, thresholds);

                proc=new AnalysisProcess(reader,ENL, vdsanalysis, bufferedMask,bufferingDistance,0);
                proc.addProcessListener(this);

                Thread t=new Thread(proc);
                t.setName("VDS_analysis_"+gir.getDisplayName(0));
                t.start();
            }
            return true;
        }
    }


    /**
     *
     */
    public List<ActionDialog.Argument> getArgumentTypes() {
        List<ActionDialog.Argument> out = new ArrayList<ActionDialog.Argument>();

        ActionDialog.Argument a1 = new ActionDialog.Argument("algorithm", ActionDialog.Argument.STRING, false, "k-dist","algorithm");
        a1.setPossibleValues(new Object[]{"k-dist"});
        ActionDialog.Argument a2 = new ActionDialog.Argument("thresholdHH", ActionDialog.Argument.FLOAT, false, 1.5,"thresholdHH");
        ActionDialog.Argument a21 = new ActionDialog.Argument("thresholdHV", ActionDialog.Argument.FLOAT, false, 1.2,"thresholdHV");
        ActionDialog.Argument a22 = new ActionDialog.Argument("thresholdVH", ActionDialog.Argument.FLOAT, false, 1.5,"thresholdVH");
        ActionDialog.Argument a23 = new ActionDialog.Argument("thresholdVV", ActionDialog.Argument.FLOAT, false, 1.2,"thresholdVV");

        ActionDialog.Argument a3 = new ActionDialog.Argument("coastline", ActionDialog.Argument.STRING, true, "no mask choosen","coastline");
        ActionDialog.Argument a31= new ActionDialog.Argument("ice", ActionDialog.Argument.STRING, true, "no mask choosen","ice");


        ArrayList<String> coasts = new ArrayList<String>();
        coasts.add("");
        ArrayList<String> ice = new ArrayList<String>();
        ice.add("");
        ImageLayer il=LayerManager.getIstanceManager().getCurrentImageLayer();

        if (il != null) {
          //  for (ILayer l : il.getLayers()) {
            for (ILayer l : LayerManager.getIstanceManager().getChilds(il)) {
                if (l instanceof MaskVectorLayer && !((MaskVectorLayer) l).getType().equals(GeometricLayer.POINT)) {
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
	public void startBlackBorederAnalysis(String message) {
		if(!stopping){
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
		if(e.getSource() instanceof JMenuItem){
			super.actionPerformed(e);
		}else{
			if(proc!=null&&e.getActionCommand().equals("STOP")){
				this.proc.setStop(true);
				this.message="stopping";
				SumoPlatform.getApplication().getMain().removeStopListener(this);
				this.proc=null;
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
