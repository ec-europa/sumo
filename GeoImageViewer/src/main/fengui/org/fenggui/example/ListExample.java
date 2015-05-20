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
 * Created on Jul 15, 2005
 * $Id: ListExample.java 28 2006-10-05 01:37:07Z schabby $
 */
package org.fenggui.example;

import org.fenggui.Display;
import org.fenggui.FengGUI;
import org.fenggui.List;
import org.fenggui.ScrollContainer;
import org.fenggui.composites.Window;

/**
 * Demonstrates the usage of lists.
 * 
 * @author Johannes Schaback 
 */
public class ListExample implements IExample {


    /**
	 * Not FengGUI related
	 */
	private static final long serialVersionUID = 1L;

    Window listFrame = null;
    
    Display desk;

    private void buildFrame() {

        listFrame = FengGUI.createDialog(desk);
        listFrame.setX(150);
        listFrame.setY(150);
        listFrame.setSize(150, 200);
        
        listFrame.setTitle("Fun with Primes");
        
        ScrollContainer sc = FengGUI.createScrollContainer(listFrame.getContentContainer());
        
        List list = FengGUI.createList(sc);
                
        list.setSize(100, 100);

        int[] primes = getPrimeNumbers(20);
        
        for(int i=0;i< primes.length;i++)
        	FengGUI.createListItem(list).setText((i+1)+". prime: "+primes[i]);

        listFrame.layout();
    }

	private int[] getPrimeNumbers(int amount) {
		
		int[] primes = new int[amount];
		
		int i = 0;
		int number = 2;
		
		while(i < amount) {
			
			boolean isPrime = true;
			for(int j=0; j  < i;j++) {
				if(number % primes[j]== 0) {
					isPrime = false;
					break;
				} 
			}
			if(isPrime) {
				primes[i++] = number;
			}
			
			number++;
		}
		
		return primes;
	}

	public void buildGUI(Display display) {

		this.desk = display;
		
		buildFrame();
	}

	public String getExampleName() {
		return "List Example";
	}

	public String getExampleDescription() {
		return "Demonstrates the usage of List Containers";
	}
	
}
