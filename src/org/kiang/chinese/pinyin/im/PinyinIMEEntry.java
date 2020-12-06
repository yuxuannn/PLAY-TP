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

package org.kiang.chinese.pinyin.im;

/**
 * Instances of these represent returned entries from a Pinyin
 * Input Method Editor.  They contain a word and a frequency of
 * that word.  In an IME the word can be displayed as an/the option.
 * The frequency can be used to order multiple possible entries.
 * 
 * @author Jordan Kiang
 */
public interface PinyinIMEEntry extends Comparable<PinyinIMEEntry> {

	/**
	 * @return the traditional form of the word
	 */
	public String getTraditional();
	
	/**
	 * @return the simplified form of the word
	 */
	public String getSimplified();
	
	/**
	 * The relative frequency.  The higher the number
	 * the higher the priority.  Entries should be sorted
	 * by this frequency before being returned to the IME.
	 * Use this in the compareTo implementation.
	 * @return frequency
	 */
	int getFrequency();	
}
