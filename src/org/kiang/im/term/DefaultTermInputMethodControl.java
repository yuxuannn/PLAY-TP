/* Copyright (c) 2007 Jordan Kiang
 * jordan-at-kiang.org
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.kiang.im.term;

import java.awt.Font;

/**
 * An base implementation of TermInputMethod.Control
 * that supports the base control options:
 * raw window usage (raw below the spot or inline)
 * chooser orientation (vertical or horizontal)
 * 
 * @author Jordan Kiang
 */
public class DefaultTermInputMethodControl implements TermInputMethod.Control {
	
	private boolean enabled = true;
	private boolean usingRawWindow = true;
	private boolean chooserOrientation = true;
	private Font font;

	/**
	 * @see org.kiang.im.term.TermInputMethod.Control#isUsingRawWindow()
	 */
	public boolean isUsingRawWindow() {
		return this.usingRawWindow;
	}

	/**
	 * @see org.kiang.im.term.TermInputMethod.Control#setUsingRawWindow(boolean)
	 */
	public void setUsingRawWindow(boolean usingRawWindow) {
		this.usingRawWindow = usingRawWindow;
	}
	
	/**
	 * @see org.kiang.im.term.TermInputMethod.Control#getChooserOrientation()
	 */
	public boolean getChooserOrientation() {
		return this.chooserOrientation;
	}
	
	/**
	 * @see org.kiang.im.term.TermInputMethod.Control#setChooserOrientation(boolean)
	 */
	public void setChooserOrientation(boolean vertical) {
		this.chooserOrientation = vertical;
	}

	/**
	 * @see org.kiang.im.term.TermInputMethod.Control#getFont()
	 */
	public Font getFont() {
		return this.font;
	}
	
	/**
	 * @see org.kiang.im.term.TermInputMethod.Control#setFont(java.awt.Font)
	 */
	public void setFont(Font font) {
		this.font = font;
	}

	/**
	 * @return true if the InputMethod is enabled, false if input passes through
	 */
	public boolean isEnabled() {
		return this.enabled;
	}

	/**
	 * @param enabled true if the InputMethod is enabled, false if input passes through
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
