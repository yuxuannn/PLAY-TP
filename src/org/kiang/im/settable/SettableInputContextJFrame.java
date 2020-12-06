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

package org.kiang.im.settable;

import java.awt.im.InputContext;

import javax.swing.JFrame;

/**
 * A JFrame class with a settable InputContext.
 * Instantiated by a SettableInputMethodContext.
 * 
 * @author Jordan Kiang
 */
public class SettableInputContextJFrame extends JFrame
{
    private InputContext inputContext;
    
    /**
     * Generate a new frame with the given title.
     * @param title
     */
    public SettableInputContextJFrame(String title) {
        super(title);
    }
    
    /**
     * @see java.awt.Window#getInputContext()
     */
    @Override
    public InputContext getInputContext() {
        if(null != this.inputContext) {
            // if this window has an explicitly set InputContext,
        	// then we return that context.
            return this.inputContext;
        }
        
        // otherwise return the context as normal
        return super.getInputContext();
    }
    
    /**
     * Manually set the InputContext.
     * @param inputContext
     */
    void setInputContext(InputContext inputContext) {
    	this.inputContext = inputContext;
    }
}

