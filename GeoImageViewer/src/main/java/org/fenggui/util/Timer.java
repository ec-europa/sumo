package org.fenggui.util;

/**
 * Low resolution timer.
 * 
 * @author Johannes, last edited by $Author: schabby $, $Date: 2006-10-05 03:37:07 +0200 (Thu, 05 Oct 2006) $
 * @version $Revision: 28 $
 */
public class Timer 
{
	private long delay = 100;
	private int numberOfStates = 2;
	private long start = System.currentTimeMillis();
	
	public Timer(int numberOfStates, long delay)
	{
		this.numberOfStates = numberOfStates;
		this.delay = delay;
	}
	
	public int getState()
	{
		long tmp = (System.currentTimeMillis() - start) / delay;
		
		return (int) (tmp % numberOfStates);
	}
	
	public void reset()
	{
		setState(0);
	}
	
	public void setState(int state)
	{
		start = System.currentTimeMillis() - state*delay;
	}
}

