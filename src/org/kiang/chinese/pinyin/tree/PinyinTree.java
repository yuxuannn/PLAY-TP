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

package org.kiang.chinese.pinyin.tree;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import org.kiang.chinese.pinyin.PinyinUnit;


/**
 * A specialized tree structure for storing values
 * according to sequences of Pinyin.  Multiple values
 * can be stored for a particular sequence.
 * 
 * Values are parameterized so that instances of these
 * can store generic value types.  Designed for use with an IME.
 * 
 * @author Jordan Kiang
 *
 * @param <V> the parameterized value type
 */
public class PinyinTree<V> {

	private PinyinTreeNode<V> root = new PinyinTreeNode<V>();
	
	/**
	 * Add the given value for the given sequence of Pinyin.
	 * 
	 * @param pinyin the pinyin
	 * @param value the value to add
	 */
	public void add(List<PinyinUnit> pinyin, V value) {
		// copy the List so we aren't modifying the passed in reference
		Queue<PinyinUnit> pinyinQueue = new LinkedList<PinyinUnit>(pinyin);
		this.root.drop(pinyinQueue, value);
	}
	
	/**
	 * Retrieve all values stored for the given sequence of Pinyin.
	 * Optionally additionally retrieve all values stored beginning
	 * with that sequence.
	 * 
	 * @param pinyin the pinyin
	 * @param anticipate true if values stored with sequences that begin with the given
	 * 			should also be returned
	 * @return the values
	 */
	public Set<V> get(List<PinyinUnit> pinyin, boolean anticipate) {
		// copy the List so we aren't modifying it
		Stack<PinyinUnit> pinyinStack = new Stack<PinyinUnit>();
		for(int i = pinyin.size() - 1; i >= 0; i--) {
			pinyinStack.push(pinyin.get(i));
		}
	
		Set<V> addTo = new HashSet<V>();
		this.root.collect(pinyinStack, anticipate, addTo);
		
		return addTo;
	}
	
	/**
	 * Obtain the root node.
	 * Package protected, not part of the public API.
	 * @return the root node
	 */
	PinyinTreeNode<V> getRoot() {
		return this.root;
	}
}
