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

import java.awt.Component;
import java.awt.Font;

import org.kiang.chinese.pinyin.im.PinyinInputContext;

/**
 * An interface for Swing Pinyin text components to implement.
 * It will override the getInputContext with a covariant
 * return of the correct type.
 * @author jkiang
 */
public interface PinyinInputComponent {
	
	/**
	 * @return
	 * @see Component#getInputContext()
	 */
	public PinyinInputContext getInputContext();

	/**
	 * Comes for free with any Component.
	 * Useful to have accsssible on the interface.
	 * @return the Font
	 * @see Component#getFont
	 */
	public Font getFont();
}
