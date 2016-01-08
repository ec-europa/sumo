package org.geoimage.opengl.control;

/*
 */


/**
 */
public class Dimension
{
	
    int width=0;
    int height=0;
    
    /**
     * Creates a new Dimension object.
     * 
     * @param w the width
     * @param h the height
     */
    public Dimension(int w, int h) {
        width = w;
        height = h;
    }

    /**
     * 
     * @param d the <code>Dimenion</code> object to copy
     */
    public Dimension(Dimension d) {
        width = d.getWidth();
        height = d.getHeight();
    }

    /**
     * Returns if the given point is "in" the dimension.
     * @param x the x coordinate of the point
     * @param y the y coordinat of the point
     * @return true if inside, false otherwise
     */
    public boolean contains(int x, int y) {
        return x >=0 && x < width && y >=0 && y < height;
    }
    
    /**
     * Returns the height of the dimension.
     * @return height
     */
    public int getHeight() {
        return height;
    }
    
    /**
     * Returns the width of the dimension
     * @return width
     */
    public int getWidth() {
        return width;
    }
    
	/**
     * Returns a string with the width and height of this dimension.
     * @return string
     */
    public String toString() {
        return "("+width+", "+height+")";
    }
    
	public void setWidth(int width)
	{
		this.width = width;
	}

	public void setSize(int width, int height)
	{
		this.width = width;
		this.height = height;
	}

	public void setSize(Dimension d)
	{
		this.width = d.getWidth();
		this.height = d.getHeight();
	}

	public void setHeight(int height)
	{
		this.height = height;
	}    
}
