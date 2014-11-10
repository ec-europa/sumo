package org.fenggui;

/**
 * Widgets that can not compute their inner size without knowing
 * their actual size are not layoutable widgets. They need a
 * hint about their size to become layoutable again.<br/>
 * Currently only concidered by <code>ScrollContainer</code>.
 * 
 * @author Johannes, last edited by $Author: schabby $, $Date: 2006-10-05 03:37:07 +0200 (Thu, 05 Oct 2006) $
 * @version $Revision: 28 $
 */
public interface INotLayoutableWidget {

	public void heightHint(int height);
	public void widthHint(int width);
	
}
