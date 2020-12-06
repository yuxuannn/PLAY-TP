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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.text.ParseException;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.kiang.chinese.pinyin.MandarinTone;
import org.kiang.chinese.pinyin.PinyinSyllable;
import org.kiang.chinese.pinyin.PinyinUnit;
import org.kiang.util.MutableInteger;


/**
 * Contains static methods for serializing a PinyinTree into
 * a compiled byte form.  The byte form can be written and
 * read through the methods in this class.  The serialized
 * format is also designed to be streamable on demand if necessary
 * via separate code rather that deserialized into memeory.
 * 
 * The format data read/written for a node as represented
 * in the stream is as follows:
 * 
 * short x indicates the number of children nodes rooted at the current position
 * for each of the x nodes
 * 		short for the ordinal of the child node's PinyinSyllable in the enum
 * 		byte indicates the tone of the Pinyin (1-5)
 * 		int indicates how many bytes compose the contents of the node from here on
 * 		short y indicates how many values are stored at this position
 * 		for each of the y values
 * 			(value format/length is determined by the ValueSerializer)
 * 		short z indicates how many child nodes are rooted here
 * 		for each of the z nodes
 * 			recursively repeat the node format for the child node
 * 
 * Uses a "MutableInteger" when reading to keep track of the position
 * in the stream.  A bit clumsy, but it's easy to implement and allows
 * a way for the ValueSerializers to report how many bytes they read.
 * 
 * @author Jordan Kiang
 */
public class PinyinTreeByteStreamSerializer {

	// use the below when updating the byte stream Position
	
	/** size of an int in bytes */
	static public final int INT_SIZE	= 4;
	
	/** size of a short in bytes */
	static public final int SHORT_SIZE = 2;
	
	/** size of a byte in bytes (duh...) */
	static public final int BYTE_SIZE 	= 1;
	
	/**
	 * Write the given tree out to the given stream, serializing
	 * values with the given ValueSerializer.  The passed in
	 * stream is not closed.
	 * 
	 * @param <V> the parameterized type of value contained in the tree
	 * @param tree
	 * @param valueSerializer for serializing values
	 * @param out the stream to write to
	 * @throws IOException
	 */
	static public <V> void writeTree(PinyinTree<V> tree, ValueSerializer<V> valueSerializer, OutputStream out) throws IOException {
		PinyinTreeNode<V> rootNode = tree.getRoot();
		
		DataOutputStream dataOut = new DataOutputStream(new BufferedOutputStream(out));
		writeNodeChildren(rootNode, valueSerializer, dataOut);
		
		dataOut.flush();
	}
	
	/**
	 * Read a PinyinTree out of the given InputStream.
	 * Values are deserialized with the given ValueSerializer.
	 * 
	 * @param <V> the parameterized value type contained in the tree
	 * @param valueSerializer for deserializing the contained values
	 * @param in
	 * @return the deserialized tree
	 * @throws IOException
	 * @throws ParseException
	 */
	static public <V> PinyinTree<V> readTree(ValueSerializer<V> valueSerializer, InputStream in) throws IOException {
		PinyinTree<V> tree = new PinyinTree<V>();
		PinyinTreeNode<V> rootNode = tree.getRoot();
		
		MutableInteger position = new MutableInteger();
		DataInput dataIn = new DataInputStream(new BufferedInputStream(in));
		
		// the root node doesn't contain values or represent
		// a PinyinUnit, it's just contains children.
		// so we kick things off by reading the root children.
		readNodeChildren(rootNode, valueSerializer, dataIn, position);
		
		return tree;
	}
	
	/**
	 * Write an individual node of a PinyinTree.
	 * 
	 * @param <V> parameterized value type
	 * @param pinyin the pinyin unit that this node represents
	 * @param node
	 * @param valueSerializer
	 * @param out
	 * @throws IOException
	 */
	static private <V> void writeNode(PinyinUnit pinyin, PinyinTreeNode<V> node, ValueSerializer<V> valueSerializer, DataOutput out) throws IOException {
		PinyinSyllable syllable = pinyin.getSyllable();
		MandarinTone tone = pinyin.getTone();
		
		// write pinyin
		writeSyllable(syllable, out);
		writeTone(tone, out);
	
		// write out the contents of the node, its values and child nodes.
		// in order to be able to skip over nodes we don't care about,
		// we need to now how many of the next bytes at the current
		// position represent the content of the node.  since we need
		// to precede the contents with the size, we have to write
		// out the contents to a temp buffer here so we can capture
		// the size first before we write the contents out.
		ByteArrayOutputStream nodeByteBuffer = new ByteArrayOutputStream();
		DataOutput nodeOut = new DataOutputStream(nodeByteBuffer);
		
		writeValues(node.values, valueSerializer, nodeOut);
		writeNodeChildren(node, valueSerializer, nodeOut);
		
		byte[] nodeBytes = nodeByteBuffer.toByteArray();
		
		// first write out the size of the node contents, then the contents
		out.writeInt(nodeBytes.length);
		out.write(nodeBytes);
	}
	
	/**
	 * Read a PinyinTreeNode from the input stream.
	 * The node will be automatically attached to the
	 * passed in parent node.
	 * 
	 * @param <V> parameterized value type
	 * @param parentNode
	 * @param valueSerializer
	 * @param in
	 * @param position
	 * @throws IOException
	 * @throws ParseException
	 */
	static private <V> void readNode(PinyinTreeNode<V> parentNode, ValueSerializer<V> valueSerializer, DataInput in, MutableInteger position) throws IOException {
		
		PinyinSyllable syllable = readSyllable(in, position);
		MandarinTone tone = readTone(in, position);
		
		PinyinUnit pinyin = new PinyinUnit(syllable, tone);
		
		// read the length of the content out
		// we don't use it, but need to consume it to get beyond
		in.readInt();
		position.value += INT_SIZE;
		
		PinyinTreeNode<V> node = new PinyinTreeNode<V>();
		SortedSet<V> values = readValues(valueSerializer, in, position);
		if(values.size() > 0) {
			// don't bother holding onto a reference and leave null
			// if there are no values at this node
			node.values = values;
		}
		
		// attach the newly created node to its parent
		parentNode.getChildNodeMap().put(pinyin, node);
		
		readNodeChildren(node, valueSerializer, in, position);
	}
	
	/**
	 * Read in the children of the given node, attaching them
	 * to the passed in parent node.
	 * 
	 * @param <V> parameterized value type
	 * @param parentNode
	 * @param valueSerializer
	 * @param in
	 * @param position
	 * @throws IOException
	 * @throws ParseException
	 */
	static private <V> void readNodeChildren(PinyinTreeNode<V> parentNode, ValueSerializer<V> valueSerializer, DataInput in, MutableInteger position) throws IOException {
		// first we read in how many children there are
		short childCount = in.readShort();
		position.value += SHORT_SIZE;
		
		// now reach in each of the children
		for(short i = 0; i < childCount; i++) {
			readNode(parentNode, valueSerializer, in, position);
		}
	}
	
	/**
	 * Write the values to the given stream, preceded by the
	 * total number of bytes for all the values.
	 * 
	 * @param <V> parameterized value type
	 * @param values
	 * @param valueSerializer
	 * @param out
	 * @throws IOException
	 */
	static private <V> void writeValues(Set<V> values, ValueSerializer<V> valueSerializer, DataOutput out) throws IOException {
		// in order to be able to stream values quickly, we want to be
		// able to skip over the values if need be, so we need to know
		// how many bytes to skip, so we write the number of bytes
		// before we write out the values.  to do this we need to
		// collect the bytes first so we know how many bytes there are.
		ByteArrayOutputStream valueBytes = new ByteArrayOutputStream();
		DataOutput valuesOut = new DataOutputStream(valueBytes);
		
		int valueCount = 0;
		if(null != values) {
			for(V value : values) {
				valueSerializer.writeValue(value, valuesOut);
			}
		
			valueCount = values.size();
		}
		
		// precede the actual values with the number of values
		out.writeShort(valueCount);
		
		byte[] bytes = valueBytes.toByteArray();
		out.write(bytes);
	}
	
	/**
	 * Read the values stored at the node.
	 * Use the passed in ValueSerializer to parse the values.
	 * 
	 * @param <V> the parameterized type
	 * @param valueSerializer
	 * @param in
	 * @param position
	 * @return the Set of values
	 * @throws IOException
	 * @throws ParseException
	 */
	static private <V> SortedSet<V> readValues(ValueSerializer<V> valueSerializer, DataInput in, MutableInteger position) throws IOException {
		// first read in the number of values stored at the current position
		short valueCount = in.readShort();
		position.value += SHORT_SIZE;
		
		// read in each value
		SortedSet<V> values = new TreeSet<V>();
		for(short i = 0; i < valueCount; i++) {
			V value = valueSerializer.readValue(in, position);
			values.add(value);
		}
		
		return values;
	}
	
	/**
	 * Write out the children of the given node to the output stream.
	 * 
	 * @param <V> the parameterized value type
	 * @param node
	 * @param valueSerializer
	 * @param out
	 * @throws IOException
	 */
	static private <V> void writeNodeChildren(PinyinTreeNode<V> node, ValueSerializer<V> valueSerializer, DataOutput out) throws IOException {
		if(null == node.children || node.children.isEmpty()) {
			// write out the count of child nodes (0).
			out.writeShort(0);
		
		} else {
			// some children nodes exist.
			// it's a SortedMap, so the set is ordered.
			Set<Map.Entry<PinyinUnit, PinyinTreeNode<V>>> childNodeEntryList = node.children.entrySet();
			
			// we precede the node bytes with the child node count
			out.writeShort(childNodeEntryList.size());
			
			// write out each of the children
			for(Map.Entry<PinyinUnit, PinyinTreeNode<V>> childNodeEntry : childNodeEntryList) {
				
				PinyinUnit pinyin = childNodeEntry.getKey();
				PinyinTreeNode<V> childNode = childNodeEntry.getValue();
				
				writeNode(pinyin, childNode, valueSerializer, out);
			}
		}
	}
	
	/**
	 * Write out the given Pinyin syllable to the stream.
	 * 
	 * @param syllable
	 * @param out
	 * @throws IOException
	 */
	static private void writeSyllable(PinyinSyllable syllable, DataOutput out) throws IOException {
		// write the syllable as its enum ordinal.
		// usually bad-form to use the ordinal,
		// but it's convenient here and the enum won't change.
		out.writeShort(syllable.ordinal());
	}
	
	/**
	 * Read in a PinyinSyllabel from the given stream.
	 * 
	 * @param in
	 * @param position
	 * @return the read syllable
	 * @throws IOException
	 * @throws ParseException
	 */
	static private PinyinSyllable readSyllable(DataInput in, MutableInteger position) throws IOException {
		PinyinSyllable[] syllables = PinyinSyllable.values();
		try {
			// the syllable is stored as it's ordinal in the enum array.
			PinyinSyllable syllable = syllables[in.readShort()];
			position.value += SHORT_SIZE;
			
			return syllable;
		
		} catch(IndexOutOfBoundsException ioobe) {
			throw new StreamCorruptedException("Invalid PinyinSyllable at position " + position.value);
		}
	}
	
	/**
	 * Write out the tone to the given output stream.
	 * 
	 * @param tone
	 * @param out
	 * @throws IOException
	 */
	static private void writeTone(MandarinTone tone, DataOutput out) throws IOException {
		// write the tone as a byte
		out.writeByte(tone.getToneNum());
	}
	
	/**
	 * Read in the tone from the given input stream.
	 * 
	 * @param in
	 * @param position
	 * @return the tone
	 * @throws IOException
	 * @throws ParseException
	 */
	static private MandarinTone readTone(DataInput in, MutableInteger position) throws IOException {
		int toneNum = in.readByte();
		try {
			MandarinTone tone = MandarinTone.valueOf(toneNum);
			position.value += BYTE_SIZE;
			
			return tone;
			
		} catch(IndexOutOfBoundsException ioobe) {
			throw new StreamCorruptedException("Invalid tone "  + toneNum + " at position " + position.value);
		}
	}
	
	/**
	 * An interface for serializing/deserializing parameterized values.
	 * PinyinTree's store values of a genericized type, and this class
	 * can work with generic value types using an instance of one of these.
	 * 
	 * @param <V>
	 */
	static public interface ValueSerializer<V> {
		
		/**
		 * Serialize the given value to a String
		 * @param value the value
		 * @param out write the value out
		 * @throws IOException
		 */
		public void writeValue(V value, DataOutput out) throws IOException;
		
		/**
		 * Parse a value from the given String.
		 * @param in
		 * @param position update the position with the number of bytes that were read
		 * @return V the read value
		 * @throws IOException
		 */
		public V readValue(DataInput in, MutableInteger position) throws IOException;
		
	}
}
