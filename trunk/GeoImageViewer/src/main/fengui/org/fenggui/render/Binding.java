/*
 * FengGUI - Java GUIs in OpenGL (http://www.fenggui.org)
 * 
 * Copyright (c) 2005, 2006 FengGUI Project
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details:
 * http://www.gnu.org/copyleft/lesser.html#TOC3
 * 
 * Created on Apr 18, 2005
 * $Id: Binding.java 311 2007-07-04 22:44:08Z Schabby $
 */
package org.fenggui.render;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.fenggui.event.DisplayResizedEvent;
import org.fenggui.event.IDisplayResizedListener;

/**
 * Defines the basic methods required to let FengGUI talk
 * to an OpenGL binding.
 * 
 * @todo Comment this class... #
 * 
 * @author Johannes Schaback, last edited by $Author: Schabby $, $Date: 2007-07-05 00:44:08 +0200 (Do, 05 Jul 2007) $
 * @version $Revision: 311 $
 */
public abstract class Binding
{
	private ArrayList<IDisplayResizedListener> displayResizedHook = new ArrayList<IDisplayResizedListener>();

	private static Binding instance = null;
	private Graphics graphics = null;
	private IOpenGL openGL = null;
	private boolean useClassLoader = false;

	public Binding(IOpenGL gl)
	{
		openGL = gl;
		graphics = new Graphics(gl);

		instance = this;
	}

	public abstract ITexture getTexture(InputStream inputStream) throws IOException;

	public abstract ITexture getTexture(BufferedImage bi);

	/**
	 * Returns whether FengGUI loads all resources through the class loader.
	 * @return true if FengGUI uses the class loader, false otherwise
	 */
	public boolean isUsingClassLoader()
	{
		return useClassLoader;
	}

	/**
	 * Sets whether FengGUI loads all resources through the class loader or by 
	 * accessing them by opening a file.
	 * @param useClassLoader true if FengGUI shall use the class loader, false otherwise
	 */
	public void setUseClassLoader(boolean useClassLoader)
	{
		this.useClassLoader = useClassLoader;
	}

	public IOpenGL getOpenGL()
	{
		return openGL;
	}

	public Graphics getGraphics()
	{
		return graphics;
	}

	/**
	 * Loads a resource either via the class loader or through a file input stream. The flag
	 * <code>useClassLoader</code> can be set to change the behaviour. The file input stream
	 * is used per default.
	 * @param filename the name of resource to load (including path)
	 * @return returns the input stream of the resource
	 * @throws FileNotFoundException thrown in case that the resource can not be found
	 */
	public InputStream getResource(String filename) throws FileNotFoundException
	{
		if (useClassLoader)
		{
			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
			if (is == null) throw new FileNotFoundException(
					"The method call Thread.currentThread().getContextClassLoader().getResourceAsStream(\"" + filename
							+ "\") returned null!");
			return is;
		}
		else
		{
			return new FileInputStream(filename);
		}
	}

	/**
	 * Loads a texture from a specified URL. Note that this method does obviously not recognize the <code>useClassLoader</code> flag.
	 * @param url the url to load the texture from
	 * @return the texture
	 * @throws IOException IOException thrown on problems during loading the data
	 */
	public ITexture getTexture(URL url) throws IOException
	{
	  return getTexture(getResource(url));
	}
		
	/**
	 * Loads a resource from the specified URL. Note that this method does obviously not recognize the <code>useClassLoader</code> flag.
	 * @param url of the resource to load
	 * @return returns the input stream to the resource
	 * @throws MalformedURLException, IOException thrown in case that the resource can not be loaded (can not be found)
	 */
	public InputStream getResource(URL url) throws MalformedURLException, IOException
	{
		InputStream is = url.openStream();
		if (is == null) throw new MalformedURLException("A problem occured while loading the resource (url.openStream() returned null)");
		return is;
	}
	
	
	/**
	 * Convenience method that nests a <code>getResource</code> call to load a texture.
	 * @param filename the file name of the texture (including path)
	 * @return returns the uploaded texture.
	 * @throws IOException thrown in case the texture loader encounters a problem.
	 */
	public ITexture getTexture(String filename) throws IOException
	{
		return getTexture(getResource(filename));
	}

	public abstract int getCanvasWidth();

	public abstract int getCanvasHeight();

	public abstract CursorFactory getCursorFactory();

	public static Binding getInstance()
	{
		if (instance == null) throw new IllegalStateException("The Binding has not been initiated yet!");

		return instance;
	}

	/**
	 * Add a {@link IDisplayResizedListener} to the widget. The listener can be added only once.
	 * @param l Listener
	 */
	public void addDisplayResizedListener(IDisplayResizedListener l)
	{
		if (!displayResizedHook.contains(l))
		{
			displayResizedHook.add(l);
		}
	}

	/**
	 * Add the {@link IDisplayResizedListener} from the widget
	 * @param l Listener
	 */
	public void removeDisplayResizedListener(IDisplayResizedListener l)
	{
		displayResizedHook.remove(l);
	}

	/**
	 * Fire a {@link DisplayResizedEvent} 
	 * @param source
	 * @param t
	 * @param s
	 */
	protected void fireDisplayResizedEvent(int width, int height)
	{
		DisplayResizedEvent e = new DisplayResizedEvent(width, height);
		IDisplayResizedListener[] listeners = new IDisplayResizedListener[displayResizedHook.size()];
		displayResizedHook.toArray(listeners);
		for (IDisplayResizedListener l : listeners)
		{
			l.displayResized(e);
		}
	}

}
