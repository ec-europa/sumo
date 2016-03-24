/*
 * 
 */
package org.fenggui.render.dummy;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.fenggui.render.ITexture;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOutputStream;

public class DummyTexture implements ITexture {

	public void bind() {
		// does nothing! It's a dummy implementation
	
	}

	public void dispose() {
		// does nothing! It's a dummy implementation
		
	}

	public int getTextureWidth() {
		// does nothing! It's a dummy implementation
		return 200;
	}

	public int getTextureHeight() {
		// does nothing! It's a dummy implementation
		return 100;
	}

	public int getImageWidth() {
		// does nothing! It's a dummy implementation
		return 82;
	}

	public int getImageHeight() {
		// does nothing! It's a dummy implementation
		return 50;
	}

	public void setAlpha(boolean alpha) {
		// does nothing! It's a dummy implementation
		
	}

	public boolean hasAlpha() {
		// does nothing! It's a dummy implementation
		return false;
	}

	public void process(InputOutputStream stream) throws IOException, IXMLStreamableException
	{
	}

	public String getUniqueName() {
		return null;
	}

	public void texSubImage2D(int xOffset, int yOffset, int width, int height, ByteBuffer buffer)
	{
	}

	public int getID()
	{
		return 0;
	}
	
		
}
