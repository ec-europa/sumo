package org.fenggui.example;

import org.fenggui.Container;
import org.fenggui.Display;
import org.fenggui.GameMenuButton;
import org.fenggui.background.PlainBackground;
import org.fenggui.composites.MessageWindow;
import org.fenggui.event.ButtonPressedEvent;
import org.fenggui.event.IButtonPressedListener;
import org.fenggui.layout.RowLayout;
import org.fenggui.layout.StaticLayout;
import org.fenggui.util.Color;
import org.fenggui.util.Spacing;

public class GameMenuExample implements IExample
{

	private GameMenuButton play, credits, options, quit;
	private GameMenuButton sound, graphics, back, network;

	public void buildGUI(Display display)
	{
		final Container c = new Container();
		c.getAppearance().add(new PlainBackground(Color.BLACK));
		display.addWidget(c);
		c.getAppearance().setPadding(new Spacing(10, 10));
		c.setLayoutManager(new RowLayout(false));

		initButtons(c, display);

		buildMainMenu(c, display);
	}

	private void initButtons(final Container c, final Display display)
	{
		play = new GameMenuButton("data/images/GameButtons/play0.png", "data/images/GameButtons/play1.png");
		options = new GameMenuButton("data/images/GameButtons/options0.png", "data/images/GameButtons/options1.png");
		credits = new GameMenuButton("data/images/GameButtons/credits0.png", "data/images/GameButtons/credits1.png");
		quit = new GameMenuButton("data/images/GameButtons/quit0.png", "data/images/GameButtons/quit1.png");
		
		play.addButtonPressedListener(new IButtonPressedListener()
		{
			public void buttonPressed(ButtonPressedEvent e)
			{
				MessageWindow mw = new MessageWindow("Nothing to play. Just a demo.");
				mw.pack();
				display.addWidget(mw);
				StaticLayout.center(mw, display);
			}
		});

		credits.addButtonPressedListener(new IButtonPressedListener()
		{
			public void buttonPressed(ButtonPressedEvent e)
			{
				MessageWindow mw = new MessageWindow("We dont take credit for FengGUI :)");
				mw.pack();
				display.addWidget(mw);
				StaticLayout.center(mw, display);
			}
		});

		options.addButtonPressedListener(new IButtonPressedListener()
		{
			public void buttonPressed(ButtonPressedEvent e)
			{
				buildOptionsMenu(c, display);
			}
		});

		quit.addButtonPressedListener(new IButtonPressedListener()
		{

			public void buttonPressed(ButtonPressedEvent e)
			{
				display.removeWidget(c);
			}
		});
		
		sound = new GameMenuButton("data/images/GameButtons/sound0.png", "data/images/GameButtons/sound1.png");
		graphics = new GameMenuButton("data/images/GameButtons/graphics0.png", "data/images/GameButtons/graphics1.png");
		back = new GameMenuButton("data/images/GameButtons/back0.png", "data/images/GameButtons/back1.png");
		network = new GameMenuButton("data/images/GameButtons/network0.png", "data/images/GameButtons/network1.png");
		
		back.addButtonPressedListener(new IButtonPressedListener()
		{

			public void buttonPressed(ButtonPressedEvent e)
			{
				buildMainMenu(c, display);
			}
		});
	}
	
	private void buildMainMenu(final Container c, final Display display)
	{
		c.removeAllWidgets();
		
		c.addWidget(play);
		c.addWidget(credits);
		c.addWidget(options);
		c.addWidget(quit);
		
		c.pack();
		StaticLayout.center(c, display);
		

	}
	
	private void buildOptionsMenu(final Container c, final Display display)
	{
		c.removeAllWidgets();
		
		c.addWidget(graphics);
		c.addWidget(sound);
		c.addWidget(network);
		c.addWidget(back);
		
		c.pack();
		StaticLayout.center(c, display);
	}

	public String getExampleName()
	{
		return "Simple Game Menu";
	}

	public String getExampleDescription()
	{
		return "Game Menu";
	}

}
