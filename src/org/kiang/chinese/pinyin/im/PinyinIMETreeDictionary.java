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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.kiang.chinese.pinyin.PinyinUnit;
import org.kiang.chinese.pinyin.tree.PinyinTree;


/**
 * An implementation of PinyinDictionary that is backed by
 * a PinyinTree, a custom in-memory tree data structure.
 * 
 * @author Jordan Kiang
 */
public class PinyinIMETreeDictionary implements PinyinIMEDictionary {

	private PinyinTree<PinyinIMETraditionalEntry> tree;
	
	/**
	 * Generate a new instance using the given PinyinTree as its data source.
	 * @param tree
	 */
	public PinyinIMETreeDictionary(PinyinTree<PinyinIMETraditionalEntry> tree) {
		if(null == tree) {
			throw new NullPointerException("tree cannot be null!");
		}
		
		this.tree = tree;
	}
	
	/**
	 * @see PinyinIMEDictionary#lookup(List, boolean)
	 */
	public List<PinyinIMEEntry> lookup(List<PinyinUnit> input, boolean anticipate) {
		Set<PinyinIMETraditionalEntry> entries = this.tree.get(input, anticipate);
		
		List<PinyinIMEEntry> sortedEntries = new ArrayList<PinyinIMEEntry>(entries);
		Collections.sort(sortedEntries);
		
		return sortedEntries;
	}
}
