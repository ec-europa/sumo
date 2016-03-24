/*
 * 
 */
package org.fenggui.render.lwjgl;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import org.fenggui.render.Cursor;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Mouse;

public class LWJGLCursor extends Cursor
{
	org.lwjgl.input.Cursor cursor = null;
	
	
	public LWJGLCursor(org.lwjgl.input.Cursor c)
	{
		cursor = c;
	}
	
	public LWJGLCursor(int xHotspot, int yHotspot, BufferedImage image) 
	{
		BufferedImage bi = null;
		
		int width = image.getWidth();
		int height = image.getHeight();
		
		if(image.getType() != BufferedImage.TYPE_INT_ARGB)
		{
			bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			bi.getGraphics().drawImage(image, 0, 0, null);
		} 
		else bi = image;
		
		ByteBuffer bb = ByteBuffer.allocateDirect(bi.getWidth()*bi.getHeight()*4);
		bb.order(ByteOrder.nativeOrder());
		IntBuffer ib = bb.asIntBuffer();
		
		for(int y = bi.getHeight()-1; y >=0; y--)
		{
			for(int x = 0; x < bi.getWidth(); x++)
			{
				ib.put(bi.getRGB(x,y));
			}
		}
		
		ib.flip();
		
		try 
		{
			cursor = new org.lwjgl.input.Cursor(width, height, xHotspot, yHotspot, 1, ib, null);
		} 
		catch (LWJGLException e) {
			
			e.printStackTrace();
		}
	}

	@Override
	public void show() 
	{
		try 
		{
			Mouse.setNativeCursor(cursor);
		} 
		catch (LWJGLException e) 
		{
			e.printStackTrace();
		}
	}

}
