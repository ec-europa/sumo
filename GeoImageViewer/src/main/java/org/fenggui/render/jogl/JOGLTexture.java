//this class is almost a plain copy from www.cokeandcode.com
package org.fenggui.render.jogl;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.media.opengl.GL;

import org.fenggui.render.Binding;
import org.fenggui.render.ITexture;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOnlyStream;
import org.fenggui.theme.xml.InputOutputStream;
import org.fenggui.util.ImageConverter;


/**
 * A texture to be bound within JOGL. This object is responsible for 
 * keeping track of a given OpenGL texture and for calculating the
 * texturing mapping coordinates of the full image.
 * 
 * Since textures need to be powers of 2 the actual texture may be
 * considerably bigged that the source image and hence the texture
 * mapping coordinates need to be adjusted to matchup drawing the
 * sprite against the texture.
 *
 */
public class JOGLTexture implements ITexture{
	
    /** The GL texture ID */
    private int textureID = -1;
    
    /** The height of the image */
    private int imgHeight;
    /** The width of the image */
    private int imgWidth;
    
    /** The width of the texture */
    private int texWidth;
    /** The height of the texture */
    private int texHeight;
    
    private GL gl;

    /**
     * Create a new texture
     *
     * @param target The GL target 
     * @param textureID The GL texture ID
     */
    public JOGLTexture(GL gl, int textureID, int imageWidth, int imageHeight, int textureWidth, int textureHeight) 
    {
        this.textureID = textureID;
        this.gl = gl;
        this.texWidth = textureWidth;
        this.texHeight = textureHeight;
        this.imgWidth = imageWidth;
        this.imgHeight = imageHeight;
    }
    
    
    /**
     * Loads a JOGLTexture from the InputOnlyStream
     * @throws IXMLStreamableException 
     * @throws IOException 
     */
    public JOGLTexture(InputOnlyStream stream)
    throws IOException, IXMLStreamableException
    {
    	process(stream);
    }
    
    private void set(JOGLTexture t)
    {
    	this.gl = t.gl;
    	this.imgHeight = t.imgHeight;
    	this.texHeight = t.texHeight;
    	this.textureID = t.textureID;
    	this.texWidth = t.texWidth;
    	this.imgWidth = t.imgWidth;
    }
    
    /**
	 * Bind the specified GL context to a texture
	 */
	public void bind()
	{
		assertCorrectTextureID();
		gl.glBindTexture(GL.GL_TEXTURE_2D, textureID);
	}

	private void assertCorrectTextureID()
	{
		if(textureID == -1)
			throw new IllegalStateException("The texture seems not yet uploaded (maybe it has been disposed)");
	}
	
	public void dispose()
	{
		assertCorrectTextureID();
		gl.glDeleteTextures(1, new int[] { textureID }, 0);
		textureID = -1;
	}
    
    /**
     * Get the height of the original image
     *
     * @return The height of the original image
     */
    public int getImageHeight() {
        return imgHeight;
    }
    
    /** 
     * Get the width of the original image
     *
     * @return The width of the original image
     */
    public int getImageWidth() {
        return imgWidth;
    }
    

    /* (non-Javadoc)
     * @see joglui.binding.Texture#getTextureWidth()
     */
    public int getTextureWidth() {
        return texWidth;
    }

    /* (non-Javadoc)
     * @see joglui.binding.Texture#getTextureHeight()
     */
    public int getTextureHeight() {
        return texHeight;
    }
    
    public boolean hasAlpha()
    {
        return true;
    }

	public void process(InputOutputStream stream) throws IOException, IXMLStreamableException
	{
		if(stream.isInputStream())
		{
			String filename = stream.processAttribute("filename", "filename");
			
			filename = ((InputOnlyStream)stream).getResourcePath() + filename;
			
			set((JOGLTexture)Binding.getInstance().getTexture(filename));
		}
	}

	
	/* (non-Javadoc)
	 * @see org.fenggui.io.IOStreamSaveable#getUniqueName()
	 */
	public String getUniqueName() {
		return GENERATE_NAME;
	}

	public void texSubImage2D(int xOffset, int yOffset, int width, int height, ByteBuffer buffer)
	{
		gl.glEnable(GL.GL_TEXTURE_2D);
		gl.glTexSubImage2D(GL.GL_TEXTURE_2D, 0, xOffset, yOffset, width, height, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, buffer);
		gl.glDisable(GL.GL_TEXTURE_2D);
	}
	

    private static int createTextureID(GL gl)
    {
        int[] tmp = new int[1];
        gl.glGenTextures(1, tmp,0);
        return tmp[0];
    }	
	
	public static JOGLTexture createTexture(GL gl, BufferedImage awtImage)
	{
		int textureID = createTextureID(gl);

		gl.glEnable(GL.GL_TEXTURE_2D);
		
		// convert that image into a byte buffer of texture data
        ByteBuffer textureBuffer = ImageConverter.convertPowerOf2(awtImage);
        gl.glBindTexture(GL.GL_TEXTURE_2D, textureID);

        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
        
        int texWidth = ImageConverter.powerOf2(awtImage.getWidth());
        int texHeight = ImageConverter.powerOf2(awtImage.getHeight());
        
        gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, 
        	texWidth, texHeight, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, textureBuffer);
        
        gl.glDisable(GL.GL_TEXTURE_2D);
        
		return new JOGLTexture(gl, textureID, awtImage.getWidth(), awtImage.getHeight(), texWidth, texHeight);
	}


	public int getID()
	{
		return textureID;
	}
	
}