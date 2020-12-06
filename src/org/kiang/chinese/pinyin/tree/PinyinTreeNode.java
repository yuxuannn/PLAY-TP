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

import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

import org.kiang.chinese.pinyin.MandarinTone;
import org.kiang.chinese.pinyin.PinyinUnit;


/**
 * A node in a PinyinTree.
 * 
 * Package protected contents, only meant to 
 * be used in the context of a PinyinTree,
 * in which case the nodes aren't publicly
 * accessible.
 * 
 * @author Jordan Kiang
 * @param <V> parameterized value type
 */
class PinyinTreeNode<V> {
	
	// the children keyed by the Pinyin
	SortedMap<PinyinUnit, PinyinTreeNode<V>> children;
	
	// the values stored at the node
	SortedSet<V> values;
	
	/**
	 * Drop the given value at this node.
	 * One PinyinUnit will stripped off the head
	 * which represents the Pinyin at this node.
	 * The value is then passed on down the appropiate
	 * child node, and deposited when the Pinyin
	 * units run out.
	 * 
	 * @param pinyin
	 * @param value
	 */
	void drop(Queue<PinyinUnit> pinyin, V value) {
		
		if(pinyin.isEmpty()) {
			// the value should be stored at this node.
			this.getValues().add(value);
			
		} else {
			// value is to be stored at a child of this node.
			
			// pick off the head which is the pinyin for this node.
			PinyinUnit unit = pinyin.poll();
			
			// obtain the child node for the pinyin,
			// creating the node if necessary.
			Map<PinyinUnit, PinyinTreeNode<V>> children = this.getChildNodeMap();
			PinyinTreeNode<V> childNode = children.get(unit);
			if(null == childNode) {
				childNode = new PinyinTreeNode<V>();
				children.put(unit, childNode);
			}
			
			// drop the value down
			childNode.drop(pinyin, value);
		}
	}
	
	/**
	 * Collect the values at this node, adding them to the passed in Set.
	 * Optionally additionally retrieve values that are stored under
	 * Pinyin sequences that begin with the given sequence.
	 * 
	 * @param pinyin the pinyin
	 * @param anticipate true if values at child nodes should be collected to
	 * @param addTo add values to this Set
	 */
	void collect(Stack<PinyinUnit> pinyin, boolean anticipate, Set<V> addTo) {
		
		if(pinyin.isEmpty()) {
			
			// if the sequence is empty then this is the node
			// whose value we want to collect (if any).
			if(null != this.values) {
				addTo.addAll(this.values);
			}
			
			// if we're additionally anticipating, then collect
			// the values from all the children as well
			if(anticipate) {
				if(null != this.children) {
					for(PinyinTreeNode<V> childNode : this.children.values()) {
						childNode.collect(pinyin, anticipate, addTo);
					}
				}
			}
		
		} else {
			// otherwise the values we want are stored at a child
			// node of this node.  pull off the head Pinyin and
			// then collect from the appropriate child.
			PinyinUnit unit = pinyin.pop();
			
			if(null != this.children) {
				// obtain the values from the appropriate child
				// if it exists...
				
				if(null != unit.getTone()) {
					// if there is a tone specified, then we need
					// to collect the values from the specific
					// toned child node
					this.collectFromChild(unit, pinyin, anticipate, addTo);
				
				} else {
					// if there is no tone, then we wild card the tone
					// and collect the values from any of the tones.
					for(MandarinTone tone : MandarinTone.values()) {		
						PinyinUnit toneUnit = new PinyinUnit(unit.getSyllable(), tone);
						this.collectFromChild(toneUnit, pinyin, anticipate, addTo);
					}
				}
			}
			
			pinyin.push(unit);
		}
	}
	
	/**
	 * Helper method pulls the values from the given child.
	 * @param childUnit
	 * @param pinyin
	 * @param anticipate
	 * @param addTo
	 */
	private void collectFromChild(PinyinUnit childUnit, Stack<PinyinUnit> pinyin, boolean anticipate, Set<V> addTo) {
		PinyinTreeNode<V> childNode = this.children.get(childUnit);
		if(null != childNode) {
			childNode.collect(pinyin, anticipate, addTo);
		}
	}
	
	/**
	 * Get the values stored at this node, creating
	 * the collection if necessary.
	 * @return values
	 */
	Set<V> getValues() {
		if(null == this.values) {
			this.values = new TreeSet<V>();
		}
		
		return this.values;
	}
	
	/**
	 * Get the pinyin to child node Map at this node,
	 * creating it if necessary.
	 * @return the Map.
	 */
	Map<PinyinUnit, PinyinTreeNode<V>> getChildNodeMap() {
		if(null == this.children) {
			this.children = new TreeMap<PinyinUnit, PinyinTreeNode<V>>();
		}
		
		return this.children;
	}
}
