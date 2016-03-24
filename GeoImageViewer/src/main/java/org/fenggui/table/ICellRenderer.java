/*
 * 
 */
package org.fenggui.table;

import org.fenggui.render.Graphics;
import org.fenggui.util.Dimension;

public interface ICellRenderer
{

	public void paint(Graphics g, Object value, int x, int y, int width, int height);

	public Dimension getCellContentSize(Object value);
}
