/*
 * Copyright (C) 2005 Jordan Kiang
 * jordan-at-kiang.org
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.kiang.swing;

import java.awt.Component;
import java.awt.Window;

/**
 * Some Swing utility methods.
 * @author Jordan Kiang
 */
public class SwingUtilities {

	/**
     * Obtain the root parent Window of the given component.
     * Throws an IllegalArgumentException if the component
     * has no parent Window.
     * 
     * Seems to duplicate some functionality in JOptionPane#getWindowForComponent
     * but it's not public...
     * 
     * @param component
     * @return root Window
     */
    static public Window getRootWindow(Component component) {
    	while(null != component.getParent()) {
    		component = component.getParent();
    	}
    	
    	if(component instanceof Window) {
    		// should be a Window...
    		return (Window)component;
    	}
    		
    	throw new IllegalArgumentException("component had no parent window");
    }
}
