//this class is almost a plain copy from www.cokeandcode.com
package org.fenggui.render.lwjgl;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import org.fenggui.render.Binding;
import org.fenggui.render.ITexture;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOnlyStream;
import org.fenggui.theme.xml.InputOutputStream;
import org.fenggui.util.ImageConverter;
import org.lwjgl.opengl.GL11;


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
public class LWJGLTexture implements ITexture{
	
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

    /**
     * Create a new texture
     *
     * @param target The GL target 
     * @param textureID The GL texture ID
     */
    public LWJGLTexture(int textureID, int imageWidth, int imageHeight, int textureWidth, int textureHeight) 
    {
        this.textureID = textureID;
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
    public LWJGLTexture(InputOnlyStream stream)
    	throws IOException, IXMLStreamableException
    {
    	process(stream);
    }
    
    private void set(LWJGLTexture t)
    {
    	this.imgHeight = t.imgHeight;
    	this.texHeight = t.texHeight;
    	this.textureID = t.textureID;
    	this.texWidth = t.texWidth;
    	this.imgWidth = t.imgWidth;
    }
    
	private void assertCorrectTextureID()
	{
		if(textureID == -1)
			throw new IllegalStateException("The texture seems not yet uploaded (maybe it has been disposed)");
	}    
    
    /**
     * Bind the specified GL context to a texture
     */
    public void bind() 
    {
    	assertCorrectTextureID();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
    }
    
    public void dispose() 
    {
    	assertCorrectTextureID();
        GL11.glDeleteTextures(IntBuffer.wrap(new int[] {textureID}));
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
			
			set((LWJGLTexture)Binding.getInstance().getTexture(filename));
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
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, xOffset, yOffset, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
	}
	
    /**
     * Creates an integer buffer to hold specified ints
     * - strictly a utility method
     *
     * @param size how many int to contain
     * @return created IntBuffer
     */
    public static IntBuffer createIntBuffer(int size) 
    {
      ByteBuffer temp = ByteBuffer.allocateDirect(4 * size);
      temp.order(ByteOrder.nativeOrder());

      return temp.asIntBuffer();
    }    
    

    private static int createTextureID()
    {
        IntBuffer tmp = createIntBuffer(1); 
        GL11.glGenTextures(tmp); 
        return tmp.get(0);
    }	
	
	public static LWJGLTexture createTexture(BufferedImage awtImage)
	{
		int textureID = createTextureID();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
		
		// convert that image into a byte buffer of texture data
        ByteBuffer textureBuffer = ImageConverter.convertPowerOf2(awtImage);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
        
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        
        int texWidth = ImageConverter.powerOf2(awtImage.getWidth());
        int texHeight = ImageConverter.powerOf2(awtImage.getHeight());
        
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, 
        	texWidth, texHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, textureBuffer);
        
        GL11.glDisable(GL11.GL_TEXTURE_2D);
		return new LWJGLTexture(textureID, awtImage.getWidth(), awtImage.getHeight(), texWidth, texHeight);
	}


	public int getID()
	{
		return textureID;
	}
	
}