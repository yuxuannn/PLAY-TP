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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.kiang.chinese.pinyin.MandarinTone;
import org.kiang.chinese.pinyin.PinyinSyllable;
import org.kiang.chinese.pinyin.PinyinUnit;


/**
 * Contains static methods for serializing a PinyinTree into
 * a textual, human readable form.  Makes use of a passed
 * in ValueSerializer for specifying how the generic
 * typed values are serialized.
 * 
 * Grammar follows the following compact example.
 * The nodes can be arbitrarily long.
 * 
 * {ZHONG1, [(中,7), (鐘,9)],
 *		{WEN2, [(中文,9)]}}
 * 
 * Everything from a # on to the end of a line is an ignored comment.
 * 
 * The format of the values contained within the parentheses ()
 * are determined by the passed in ValueSerializer.  Above
 * is an example where the value serialization is
 * "word, frequency".
 * 
 * Note this isn't a Java Serializable.  Other Serializable
 * classes can use this with a concrete parameterized value type
 * by passing in a ValueSerializer.
 * 
 * @author Jordan Kiang
 */
public class PinyinTreeTextSerializer {

	/**
	 * Write out the given PinyinTree to the given OutputStream,
	 * using the given ValueSerializer to serialize the
	 * values of the parameterized value type.
	 * 
	 * @param <V> parameterized value type
	 * @param tree the PinyinTree
	 * @param valueSerializer serializes values
	 * @param out the stream to write to
	 * @throws IOException
	 */
	static public <V> void writeTree(PinyinTree<V> tree, ValueSerializer<V> valueSerializer, OutputStream out) throws IOException {
		PinyinTreeNode<V> rootNode = tree.getRoot();
				
		// append the characters to the live OutputStream so that
		// we don't have to collect the whole serialized output
		// into memory.
		Writer writer = new OutputStreamWriter(out, Charset.forName("UTF-8"));
		
		// the root node is a special case since it doesn't have
		// any Pinyin associated with it.  we start by outputting
		// it's children rather than writing the node itself because
		// of this.
		writeNodeChildren(rootNode, valueSerializer, 0, writer);
		
		// newline at the end.
		writer.append("\n");
		
		// flush the streams.
		// writer is buffered, so explicitly flush it.
		writer.flush();
		out.flush();
	}
	
	/**
	 * Write the node located at the given PinyinUnit
	 * into the given StringBuilder.  Use the ValueSerializer
	 * to serialize values, and use the comparator to order
	 * child nodes into the output
	 * 
	 * @param <V> parameterized type
	 * @param unit pinyin that the node represents
	 * @param node the node
	 * @param valueSerializer
	 * @param depth the depth of the node
	 * @param appendTo append serialized text to
	 * @param childEntryComparator for ordering nodes in the output
	 */
	static private <V> void writeNode(PinyinUnit unit, PinyinTreeNode<V> node,
										ValueSerializer<V> valueSerializer,
										int depth, Appendable appendTo) throws IOException {
		
		// write some tabs for formatting
		writeTabs(appendTo, depth - 1);
		
		// open the node with an opening curly and the pinyin
		appendTo.append("{").append(unit.toString()).append(", ");
		
		// open the values with a brace
		appendTo.append("[");	// open values comma delimited w/n braces []
		
		// if there are values in this node, write them out
		if(null != node.values && !node.values.isEmpty()) {
			Iterator<V> valueIter = node.values.iterator();
			while(true) {
				// break within the loop to handle commas	
				V value = valueIter.next();
				
				// each individual value wrapped in parens
				appendTo.append("(").append(valueSerializer.toString(value)).append(")");
				
				if(valueIter.hasNext()) {
					appendTo.append(", ");
				} else {
					// no comma after the last
					// break out of the loop
					break;
				}
			}
		}
		
		// close values contained w/n braces
		appendTo.append("]");
		
		// write the children of this node
		writeNodeChildren(node, valueSerializer, depth, appendTo);
		
		// close the node
		appendTo.append("}");
	}
	
	/**
	 * Write the children of the node into the Appendable.
	 * Use the given ValueSerializer to serialize values
	 * of the paramterized type, and use the given Comparator
	 * to order the nodes in the output.
	 * 
	 * @param <V> parameterized value type
	 * @param node the node whose children are being written
	 * @param valueSerializer serializes the values
	 * @param depth the depth of the node
	 * @param appendTo write output into it
	 * @param childEntryComparator orders the nodes in the output
	 */
	static private <V> void writeNodeChildren(PinyinTreeNode<V> node,
												ValueSerializer<V> valueSerializer,
												int depth, Appendable appendTo) throws IOException {
	
		if(null != node.children && !node.children.isEmpty()) {
			// don't blow up if there's no children
			
			// useful to keep track of to pretty print
			boolean firstChild = true;
			
			// write out each child node.
			// it's a SortedMap, so the Set is already sorted
			Set<Map.Entry<PinyinUnit, PinyinTreeNode<V>>> childNodeEntryList = node.children.entrySet();
			Iterator<Map.Entry<PinyinUnit, PinyinTreeNode<V>>> childNodeEntryIter = childNodeEntryList.iterator();
			while(true) {
				Map.Entry<PinyinUnit, PinyinTreeNode<V>> childNodeEntry = childNodeEntryIter.next();
				
				PinyinUnit unit = childNodeEntry.getKey();
				PinyinTreeNode<V> childNode = childNodeEntry.getValue();
				
				if(firstChild) {						
					if(depth > 0) {
						// if it's the first child of a node then we know there is
						// at least one child and we're currently positioned
						// after the values [] and we append a comma before
						// the first child, except if it's the first child
						// of the root (the very first node).

						appendTo.append(",\n");
					}
					
					firstChild = false;
					
				} else {
					// newline before opening each node
					appendTo.append("\n");
				}
				
				writeNode(unit, childNode, valueSerializer, depth + 1, appendTo);
				
				// no comma after the last, break out when done
				if(childNodeEntryIter.hasNext()) {
					appendTo.append(",");
				} else {
					break;
				}
			}
		}
	}
	
	/**
	 * Helper method writes tabs into the buffer
	 * for pretty printed output.
	 * @param sbuf buffer to write to
	 * @param tabs # of tabs to write
	 */
	static private void writeTabs(Appendable appendTo, int tabs) throws IOException {
		for(int i = 0; i < tabs; i++) {
			appendTo.append("\t");
		}
	}
	
	/**
	 * Reads a serialized PinyinTree from the given InputStream.
	 * The format is that written out by this class.
	 * 
	 * @param <V> the parameterized value type
	 * @param valueSerializer for deserializing values of the parameterized type
	 * @param in InputStream
	 * @return the tree
	 * @throws IOException
	 * @throws ParseException
	 */
	static public <V> PinyinTree<V> readTree(ValueSerializer<V> valueSerializer, InputStream in) throws IOException, ParseException {
		PinyinTree<V> tree = new PinyinTree<V>();
		PinyinTreeNode<V> rootNode = tree.getRoot();

		Reader reader = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")));
		readNodeChildren(0, rootNode, valueSerializer, reader);
		
		return tree;
	}

	/**
	 * Parse the children of the given parent node and a
	 * and attach the deserialized children nodes to the parent.
	 * 
	 * @param <V> the parameterized value type
	 * @param position the position in in the stream where the node begins
	 * @param parentNode the parent node
	 * @param valueSerializer for deserializing values
	 * @param reader read the serialized text from here
	 * @throws IOException
	 * @throws ParseException
	 */
	static private <V> void readNodeChildren(int position, PinyinTreeNode<V> parentNode, ValueSerializer<V> valueSerializer, Reader reader) throws IOException, ParseException {
		// count of opening/closing node braces
		// when close count matches the opening
		// count, we know that we've finished
		// reading a node.
		int nodeOpenCount = 0;
		int nodeCloseCount = 0;
		
		int nodePosition = position;
		
		boolean inComment = false;
		boolean inValue = false;
		
		// read and buffer the text for a node
		StringBuilder nodeBuffer = new StringBuilder();
		for(int reads = 1, val; (val = reader.read()) > -1; reads++) {
			char c = (char)val;
						
			if(inComment) {
				// inside of a comment
				
				if(c == '\n') {
					// a newline concludes a comment
					inComment = false;
				}
				
			} else if(inValue) {
				// in the middle of reading in a value.
				// just accept the value since it's supposed
				// to be interpeted by the ValueSerializer.
				nodeBuffer.append(c);
				
				if(c == ')') {
					// end of the value
					inValue = false;
				}			
				
			} else if(c == '#') {
				// inside of a comment.
				// ignore the read character.
				inComment = true;
				
			} else if(c == '{') {
				// an opening brace, increment the count
				nodeOpenCount++;
				nodeBuffer.append(c);
				
			} else if(c == '}') {
				nodeCloseCount++;
				nodeBuffer.append(c);
				
				if(nodeCloseCount >= nodeOpenCount) {
					// it's the closing curly brace that
					// concludes the node
					
					String nodeText = nodeBuffer.toString();
					readNodeContents(nodePosition, parentNode, nodeText, valueSerializer);
					
					// after wrapping up the node, set the offset to the next
					// node to the offset incremented by the number of reads
					nodePosition = position + reads;
					
					// reset the loop args
					nodeBuffer = new StringBuilder();
					
					nodeOpenCount = 0;
					nodeCloseCount = 0;
				}
				
			} else if(nodeOpenCount > 0) {
				// else it's part of the contents of the node
				nodeBuffer.append(c);
				
				if(c == '(') {
					// it's the beginning of reading in a value
					inValue = true;	
				}
			
			} else {
				// we are in between nodes (commas, newlines, etc.)
				// increment the offset so that it will be positioned
				// correctly at the start of the next node
				nodePosition++;
			}
		}
	}
	
	// pattern of a node contents
	// group 1 is the pinyin syllable
	// group 2 is the tone
	// group 3 is the values at the node (possibly comma delimited)
	static private final Pattern NODE_PATTERN = Pattern.compile("\\{\\s*([A-ZÜ]+)(\\d)\\s*,\\s*\\[(.*?)\\].*?(\\{.*)?\\}", Pattern.DOTALL);
	
	/**
	 * Parse the contents of a node, the Pinyin, the values, and recursively the children nodes.
	 * The parsed node will be attached to the given parent, and so on recursively.
	 * 
	 * @param <V> the parameterized value type
	 * @param position the offset in the stream of the node
	 * @param parentNode the node that is the parent of the node that's being parsed
	 * @param nodeText the serialized node text
	 * @param valueSerializer for deserializing the values
	 * @throws IOException
	 * @throws ParseException
	 */
	static private <V> void readNodeContents(int position, PinyinTreeNode<V> parentNode, String nodeText, ValueSerializer<V> valueSerializer) throws IOException, ParseException {
		
		// attempt to pattern group the node.
		// throw a ParseException if unable.
		Matcher nodeMatcher = NODE_PATTERN.matcher(nodeText);
		if(!nodeMatcher.matches()) {
			throw new ParseException("unable to parse node at position: " + position, position);
		}
		
		// parse the pinyin.
		// throw a ParseException if unable.
		PinyinSyllable syllable;
		String syllableStr = nodeMatcher.group(1);
		try {
			syllable = PinyinSyllable.valueOf(syllableStr);
		} catch(IllegalArgumentException iae) {
			throw new ParseException("invalid PinyinSyllable: " + syllableStr + " at position: " + position, position);
		}
		
		// parse the tone.
		// throw a ParseException if unable.
		MandarinTone tone;
		String toneStr = nodeMatcher.group(2);
		try {
			tone = MandarinTone.valueOf(Integer.parseInt(toneStr));
		} catch(IllegalArgumentException iae) {
			throw new ParseException("invalid MandarinTone: " + toneStr + " at position: " + position, position);
		}
		
		// construct the PinyinUnit and generate,
		// generate a new child node, and attach
		// it to the parent.
		PinyinUnit unit = new PinyinUnit(syllable, tone);
		PinyinTreeNode<V> treeNode = new PinyinTreeNode<V>();
		parentNode.getChildNodeMap().put(unit, treeNode);
		
		// parse each of the values
		String valuesCSV = nodeMatcher.group(3);
		if(valuesCSV.length() > 0) {
			// values are comma delimited, each value
			// is enclosed in parens.  strip off the opening
			// parent of the first and the closing of the last
			int firstOpenParen = valuesCSV.indexOf("(");
			int lastCloseParen = valuesCSV.lastIndexOf(")");
			valuesCSV = valuesCSV.substring(firstOpenParen + 1, lastCloseParen);

			// now split the remainder and strip out the between parens
			String[] valueSplit = valuesCSV.split("\\),\\s*\\(");
			
			// deserialize each value
			Set<V> values = treeNode.getValues();
			for(String valueStr : valueSplit) {
				V value = valueSerializer.parseValue(valueStr);
				values.add(value);
			}
		}
		
		// the remainder in group four contains all the
		// children of the particular node
		String childNodesText = nodeMatcher.group(4);
		if(null != childNodesText) {
			int childNodeOffset = position + nodeMatcher.start(4);
			Reader childNodesReader = new StringReader(childNodesText);
			readNodeChildren(childNodeOffset, treeNode, valueSerializer, childNodesReader);
		}
	}
	
	
	/**
	 * Determines how the value contained in the PinyinTree
	 * is serialized and deserialized to/from String text.
	 * 
	 * @param <V> parameterized value type
	 */
	static public interface ValueSerializer<V> {
		
		/**
		 * Serialize the given value to a String
		 * @param value the value
		 * @return the serialized String form
		 */
		public String toString(V value);
		
		/**
		 * Parse a value from the given String.
		 * @param valueStr serialized form
		 * @return parsed value
		 */
		public V parseValue(String valueStr);
	}
}
