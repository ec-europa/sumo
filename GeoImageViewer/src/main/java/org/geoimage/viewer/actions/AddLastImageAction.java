/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.actions;

import java.util.List;

import org.geoimage.def.GeoImageReader;
import org.geoimage.factory.GeoImageReaderFactory;
import org.geoimage.viewer.core.SumoPlatform;
import org.geoimage.viewer.core.layers.image.ImageLayer;
import org.geoimage.viewer.util.IProgress;
import org.geoimage.viewer.widget.dialog.ActionDialog.Argument;
import org.jrc.sumo.configuration.PlatformConfiguration;
import org.jrc.sumo.util.Constant;
import org.slf4j.LoggerFactory;

/**
 *
 * @author thoorfr this class is called whenever we want to open an image. A new
 *         image consists in a new layer (SimpleVectorLayer). thumbnails part
 *         need to be revised
 */
public class AddLastImageAction extends SumoAbstractAction implements IProgress {
	private static org.slf4j.Logger logger = LoggerFactory.getLogger(AddLastImageAction.class);

	public AddLastImageAction() {
		super("Last Image", "Import/Last Image");
		super.message = "Adding Image. Please wait...";

	}

	@Override
	public String getDescription() {
		return " Add a vector layer, using geotools connection.\n";
	}

	@Override
	public boolean execute() {
		done = false;
		try {
			addImage();
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

		// **** menu open last image ********//
		final String imagename = SumoPlatform.getApplication().getConfiguration().getLastImage();
		GeoImageReader temp = null;
		List<GeoImageReader> tempList = null;

		tempList = GeoImageReaderFactory.createReaderForName(imagename,
				PlatformConfiguration.getConfigurationInstance().getS1GeolocationAlgorithm());
		temp = tempList.get(0);
		// save the file name in the preferences
		SumoPlatform.getApplication().getConfiguration().updateConfiguration(Constant.PREF_LASTIMAGE,
				temp.getFilesList()[0]);
		SumoPlatform.getApplication().getGeoContext().setX(0);
		SumoPlatform.getApplication().getGeoContext().setY(0);

		if (tempList == null || tempList.isEmpty()) {
			this.done = true;
			this.setMessage("Could not open image file");
			final String filename = paramsAction.get("image_name").split("=")[1];
			errorWindow("Could not open image file\n" + filename);
		} else {
			for (int i = 0; i < tempList.size(); i++) {
				temp = tempList.get(i);

				ImageLayer newImage = new ImageLayer(temp);
				SumoPlatform.getApplication().getLayerManager().addLayer(newImage, i == 0);
				try {
					Thread.sleep(5000);
				} catch (InterruptedException ex) {
					logger.error(ex.getMessage(), ex);
				}
			}
			try {
				SumoPlatform.getApplication().refresh();
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
			SumoPlatform.getApplication().getConsoleLayer().executeCommand("home=home");
		}
		done = true;
	}

	public boolean isIndeterminate() {
		return true;
	}

	public boolean isDone() {
		return done;
	}

	public int getMaximum() {
		return 100;
	}

	public int getCurrent() {
		return 1;
	}

	public List<Argument> getArgumentTypes() {
		return null;
	}

	public void setCurrent(int i) {
	}

	public void setMaximum(int size) {
	}

	public void setIndeterminate(boolean value) {
	}

	public void setDone(boolean value) {
		this.done=value;
	}

}
