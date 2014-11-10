package org.fenggui.example;

import java.io.File;

import org.fenggui.*;


public class VerticalListExample implements IExample
{
	public void buildGUI(Display display) 
	{
		VerticalList list = new VerticalList();

		list.setSize(300, 200);
		
		File[] files = (new File(".")).listFiles();
		
		for(File f: files)
		{
			if(f.isDirectory())
				list.addItem(f.getName().toUpperCase()+"/");
			else
				list.addItem(f.getName());
		}
		
		ScrollContainer sc = new ScrollContainer();
		sc.setSize(300, 200);
		sc.setInnerWidget(list);
		sc.setXY(100, 100);
		
		display.addWidget(sc);
		display.layout();
	}

	public String getExampleName() 
	{
		return "Vertical List Example";
	}

	public String getExampleDescription() {
		return "Gnagnagnagnaga";
	}

}
