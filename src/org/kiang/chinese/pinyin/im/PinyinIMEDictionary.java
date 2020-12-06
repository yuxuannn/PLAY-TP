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

import java.util.List;

import org.kiang.chinese.pinyin.PinyinUnit;


/**
 * A dictionary that supplies word matches for a Pinyin IME.
 * 
 * @author Jordan Kiang
 */
public interface PinyinIMEDictionary {

	/**
	 * Find all the words in the dictionary whose Pinyin matches
	 * the given input.  For input units with no specified tone,
	 * the tone should be considered a wildcard and matches
	 * for all tones should be returned.
	 * 
	 * Returned entries will have at least as many characters as
	 * units in the specified input.  If anticipating, then
	 * longer words whose first characters match the input
	 * can additionally be returned.
	 * 
	 * Returned entries should be in order relative to their
	 * frequency, with the most frequent words at the head
	 * of the List.
	 * 
	 * @param input the units of pinyin input
	 * @param anticipate true if words that begin with the input should also be returned
	 * @return words matched by the input
	 */
	public List<PinyinIMEEntry> lookup(List<PinyinUnit> input, boolean anticipate);
}
