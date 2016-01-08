package org.fenggui.example;

import org.fenggui.Button;
import org.fenggui.Container;
import org.fenggui.Display;
import org.fenggui.Label;
import org.fenggui.TabContainer;
import org.fenggui.background.PlainBackground;
import org.fenggui.composites.Window;
import org.fenggui.layout.Alignment;
import org.fenggui.layout.BorderLayoutData;
import org.fenggui.layout.GridLayout;
import org.fenggui.layout.RowLayout;
import org.fenggui.util.Color;
import org.fenggui.util.Spacing;

public class TabContainerExample implements IExample
{

	public void buildGUI(Display display)
	{
		Window w = new Window(true, false, false, true);
		
		TabContainer tabContainer = new TabContainer();
		
		w.getContentContainer().addWidget(tabContainer);
			
		Label l2 = new Label("Other tab (2)");
		l2.getAppearance().add(new PlainBackground(Color.GREEN));
		l2.getAppearance().setAlignment(Alignment.MIDDLE);
		
		
		Label l3 = new Label("Yen another tab (3)");
		l3.getAppearance().add(new PlainBackground(Color.CYAN));
		l3.getAppearance().setAlignment(Alignment.MIDDLE);
		
		tabContainer.addTab("Login", null, buildLoginTab());
		tabContainer.addTab("Buttons", null, buildButtonTab());
		tabContainer.addTab("Tab 3", null, l3);
		
		w.setXY(10, 10);
		display.addWidget(w);
		w.setSize(200, 200);
		w.layout();
	}
	
	private Container buildButtonTab()
	{
		Container c = new Container(new GridLayout(2, 2));
		c.addWidget(new Button("B1"));
		c.addWidget(new Button("B2"));
		c.addWidget(new Button("B3"));
		c.addWidget(new Button("B4"));
		
		return c;
	}
	
	private Container buildLoginTab()
	{
		Container c = new Container(new GridLayout(2, 1));
		c.addWidget(new Label("Name:"));
		//c.addWidget(new TextEditor(false));
		c.addWidget(new Label("Password:"));
		//c.addWidget(new TextEditor(false));
		c.setLayoutData(BorderLayoutData.CENTER);
		
		Container k = new Container(new RowLayout(false));
		k.addWidget(c);
		Button b = new Button(" Login ");
		b.getAppearance().setMargin(new Spacing(5, 5));
		b.setSizeToMinSize();
		b.setExpandable(false);
		b.setShrinkable(false);

		b.setLayoutData(BorderLayoutData.SOUTH);
		k.addWidget(b);
		
		return k;
	}

	public String getExampleName()
	{
		return "TabContainer Example";
	}

	public String getExampleDescription()
	{
		return "Shows the TabContainer in action";
	}

}
