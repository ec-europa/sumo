/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;

import org.geoimage.def.GeoImageReader;
import org.geoimage.factory.GeoImageReaderFactory;
import org.geoimage.impl.TiledBufferedImage;
import org.geoimage.viewer.core.SumoPlatform;
import org.geoimage.viewer.core.io.SumoXmlIOOld;
import org.geoimage.viewer.core.layers.GeometricLayer;
import org.geoimage.viewer.core.layers.image.CacheManager;
import org.geoimage.viewer.core.layers.image.ImageLayer;
import org.geoimage.viewer.core.layers.thumbnails.ThumbnailsLayer;
import org.geoimage.viewer.core.layers.thumbnails.ThumbnailsManager;
import org.geoimage.viewer.util.IProgress;
import org.geoimage.viewer.widget.dialog.ActionDialog.Argument;
import org.jrc.sumo.configuration.PlatformConfiguration;
import org.jrc.sumo.util.Constant;
import org.slf4j.LoggerFactory;

/**
 *
 * @author thoorfr
 * this class is called whenever we want to open an image. A new image consists in a new layer (SimpleVectorLayer).
 * thumbnails part need to be revised
 */
public class AddImageAction extends SumoAbstractAction implements IProgress {
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(AddImageAction.class);

    private JFileChooser fileChooser;
    private String lastDirectory;
    boolean done = false;
    private String message = "Adding Image. Please wait...";




    public AddImageAction() {
    	super("image","Import/Image");
        if(SumoPlatform.getApplication().getConfiguration().getLastImage().equals("")){
            //AG set the default directory if no images have been opened before
            lastDirectory = SumoPlatform.getApplication().getConfiguration().getImageFolder();
        }else{
            lastDirectory = new File(SumoPlatform.getApplication().getConfiguration().getLastImage()).getAbsolutePath();
        }
        fileChooser = new JFileChooser(lastDirectory);
    }


    @Override
    public String getDescription() {
        return " Add a vector layer, using geotools connection.\n"
                + " Use \"add image [file=/home/data/image.tiff]\" to add image.tiff\n";
    }

    @Override
    public boolean execute() {
        if (paramsAction.size() == 0) {
            return true;
        }
        done = false;
        try {
        	String img=getParamValue("image_type");
            if (img.equals("image")) {
                addImage();
            } else if (img.startsWith("thumb")) {
                addThumbnails();
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorWindow("Problem opening file");
        }
        done = true;

        return true;
    }

    private void addImage() {
        // set the done flag to false
        this.done = false;
        boolean tileBuff=false;

        String imagefile = "";
        
    	if (lastDirectory.equals(SumoPlatform.getApplication().getConfiguration().getImageFolder())) {
            if (fileChooser == null) {
                fileChooser = new JFileChooser(lastDirectory);
            }
        } else {
            fileChooser = new JFileChooser(lastDirectory);
        }
        int returnVal = fileChooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            lastDirectory = fileChooser.getSelectedFile().getAbsolutePath();
            imagefile=fileChooser.getSelectedFile().getAbsolutePath();
        }
        tileBuff=paramsAction.get("Local_Buffer").equals("true");
        
        GeoImageReader temp = null;
        List<GeoImageReader> tempList = null;

        if (tileBuff) {
            GeoImageReader gir1 = GeoImageReaderFactory.createReaderForName(imagefile,PlatformConfiguration.getConfigurationInstance().getS1GeolocationAlgorithm()).get(0);
            temp = new TiledBufferedImage(new File(CacheManager.getRootCacheInstance().getPath(), gir1.getFilesList()[0] + "/data"), gir1);
            if(temp!=null){
            	tempList=new ArrayList<GeoImageReader>();
            	tempList.add(temp);
            }
        } else {
        	tempList = GeoImageReaderFactory.createReaderForName(imagefile,PlatformConfiguration.getConfigurationInstance().getS1GeolocationAlgorithm());
        	temp=tempList.get(0);
        }
        // save the file name in the preferences
        SumoPlatform.getApplication().getConfiguration().updateConfiguration(Constant.PREF_LASTIMAGE, temp.getFilesList()[0]);
        SumoPlatform.getApplication().getGeoContext().setX(0);
        SumoPlatform.getApplication().getGeoContext().setY(0);

        if (tempList==null||tempList.isEmpty()) {
            this.done = true;
            this.setMessage("Could not open image file");
            final String filename = paramsAction.get("image_name").split("=")[1];
            errorWindow("Could not open image file\n" + filename);
        } else {
        	for(int i=0;i<tempList.size();i++){
        		temp=tempList.get(i);

        		ImageLayer newImage = new ImageLayer(temp);
                SumoPlatform.getApplication().getLayerManager().addLayer(newImage,i==0);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                	logger.error(ex.getMessage(),ex);
                }
        	}
        	try {
                SumoPlatform.getApplication().refresh();
            } catch (Exception ex) {
            	logger.error(ex.getMessage(),ex);
            }
        	SumoPlatform.getApplication().getConsoleLayer().executeCommand("home=home");
        }
        done = true;
    }


    private void addThumbnails() {
    	 SumoXmlIOOld old=null;
        if (paramsAction.size() == 2) {
            //csv = new SimpleCSVIO(args[1].split("=")[1]);
        } else {
            int returnVal = fileChooser.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                lastDirectory = fileChooser.getSelectedFile().getParent();
                old=new SumoXmlIOOld(fileChooser.getSelectedFile());
            }
	        if(old!=null){
	        	try{
			        GeometricLayer positions = old.readLayer();
			        if (positions.getProjection() == null) {
			            SumoPlatform.getApplication().getLayerManager().addLayer(new ThumbnailsLayer(null, positions, null, "id", new ThumbnailsManager(lastDirectory)));
			        } else {
			            ThumbnailsLayer tm = new ThumbnailsLayer(null, positions, positions.getProjection(), "id", new ThumbnailsManager(lastDirectory));
			            SumoPlatform.getApplication().getLayerManager().addLayer(tm);
			        }
	        	}catch(Exception e){
	        		logger.error(e.getMessage());
	        	}

	        }
	        try {
	            SumoPlatform.getApplication().refresh();
	        } catch (Exception ex) {
	        	logger.error(ex.getMessage(),ex);
	        }
        }

    }

    public boolean isIndeterminate() {
        return true;
    }

    public boolean isDone() {
        return done;
    }

    public int getMaximum() {
        return 1;
    }

    public int getCurrent() {
        return 1;
    }

    public String getMessage() {
        return this.message;
    }

    public List<Argument> getArgumentTypes() {
        Argument a1 = new Argument("image_type", Argument.STRING, false, "image","data type");
        //AG import thumbnails removed
        //a1.setPossibleValues(new String[]{"image", "thumbnails"});
        a1.setPossibleValues(new String[]{"image"});
        List<Argument> out = new ArrayList<Argument>();
        out.add(a1);

        Argument a2 = new Argument("Local_Buffer", Argument.BOOLEAN, false, "buffer","Local Buffer the raster data");
        out.add(a2);

        return out;
    }

    public void setCurrent(int i) {
    }

    public void setMaximum(int size) {
    }

    public void setMessage(String string) {
    }

    public void setIndeterminate(boolean value) {
    }

    public void setDone(boolean value) {
    }

    public File listFiles(File file, String filter) {
        File[] entries = file.listFiles();

        for (File listfile : entries) {
            if (listfile.isDirectory()) {
                File filteredfile = null;
                filteredfile = listFiles(listfile, filter);
                if (filteredfile != null) {
                    return filteredfile;
                }
            } else {
                String listfilename = listfile.getName();
                String[] strings = filter.split("\\*");
                int found = 0;
                int index = 0;
                for (String string : strings) {
                    if (index == 0) {
                        if (listfilename.startsWith(string)) {
                            found++;
                            listfilename = listfilename.substring(string.length());
                        } else {
                            break;
                        }
                    } else {
                        if (index == strings.length) {
                            if (listfilename.endsWith(string)) {
                                found++;
                            } else {
                                break;
                            }
                        } else {
                            if (listfile.getName().contains(string)) {
                                found++;
                                listfilename = listfilename.substring(listfilename.indexOf(string) + string.length());
                            } else {
                                break;
                            }
                        }
                    }
                    index++;
                }
                if (found == strings.length) {
                    return listfile;
                }
            }
        }
        return null;
    }


}
