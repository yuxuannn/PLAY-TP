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

package org.kiang.chinese.pinyin.im.swing;

import java.awt.Font;

import javax.swing.JTextArea;

import org.kiang.chinese.pinyin.im.PinyinInputContext;


/**
 * A JTextArea that is hard-wired to use a PinyinInputMethod.
 * 
 * @author Jordan Kiang
 */
public class PinyinInputTextArea extends JTextArea implements PinyinInputComponent {

	private PinyinInputContext inputContext = new PinyinInputContext(this);
		
	/**
	 * @see java.awt.Component#getInputContext()
	 */
	@Override
	public PinyinInputContext getInputContext() {
		return this.inputContext;
	}
	
	/**
	 * Additionally applies the font to the input method windows.
	 * 
	 * @see javax.swing.JTextArea#setFont(java.awt.Font)
	 */
	@Override
	public void setFont(Font font) {
		super.setFont(font);
		
		if(null != this.inputContext) {
			// setFont is invoked in a super constructor before the overiding
			// InputContext is set, so check if it's non-null before applying
			// it to the input method
			this.inputContext.getInputMethodControlObject().setFont(font);
		}
	}
}