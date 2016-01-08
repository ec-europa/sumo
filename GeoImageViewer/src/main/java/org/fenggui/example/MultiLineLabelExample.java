/**
 * 
 */
package org.fenggui.example;

import org.fenggui.Display;
import org.fenggui.MultiLineLabel;

/**
 * @author charlie
 *
 */
public class MultiLineLabelExample implements IExample {


	public void buildGUI(Display display) {
		String text = "This is an example showing the behavior of the multiline label.";
		
		MultiLineLabel l = new MultiLineLabel();
		display.addWidget(l);
		l.setMaxCharactersPerLine(20);
		l.setText(text);
		l.setX(100);
		l.setY(100);
		l.setSizeToMinSize();
		
		display.layout();
	}


	public String getExampleDescription() {
		return "Shows a multiline label";
	}

	public String getExampleName() {
		return "MultiLineLabel Example";
	}

}
