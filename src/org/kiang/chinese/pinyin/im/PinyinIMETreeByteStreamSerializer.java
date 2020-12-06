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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URL;
import java.text.ParseException;

import org.kiang.chinese.pinyin.tree.PinyinTree;
import org.kiang.chinese.pinyin.tree.PinyinTreeByteStreamSerializer;


/**
 * Serialization/deserialization methods for a reading/writing
 * a PinyinTree containing IMEEntrys into a compact streamable
 * byte format.  Can wrap instances PinyinTree<IMEEntry> in these
 * to serialize via Java's standard serialize mechanisms.  Or
 * can just read/write directly to streams using the static methods.
 * 
 * @author Jordan Kiang
 */
public class PinyinIMETreeByteStreamSerializer implements Serializable {
	
	private PinyinTree<PinyinIMETraditionalEntry> tree;
	
	/**
	 * Static serializer helper.  Write the tree out to the given stream.
	 * @param tree
	 * @param out
	 * @throws IOException
	 */
	static public void writeTree(PinyinTree<PinyinIMETraditionalEntry> tree, OutputStream out) throws IOException {
		PinyinTreeByteStreamSerializer.writeTree(tree, new PinyinIMEEntryByteSerializer(), out);
	}
	
	/**
	 * Static deserializer helper.  Read a PinyinTree<IMEEntry> from the given stream.
	 * @param in
	 * @return tree
	 * @throws IOException
	 * @throws ParseException
	 */
	static public PinyinTree<PinyinIMETraditionalEntry> readTree(InputStream in) throws IOException {
		return PinyinTreeByteStreamSerializer.readTree(new PinyinIMEEntryByteSerializer(), in);
	}
	
	/**
	 * Create a new instance for use with Java serialization.
	 * You can wrap a PinyinTree in one of these and write it out
	 * to via the JDK serialization mechanisms.  Note that if you
	 * just want to write a tree out yourself and don't need
	 * Java's serialization, you can use the static read/write methods.
	 * 
	 * @param tree the tree to serialize
	 */
	public PinyinIMETreeByteStreamSerializer(PinyinTree<PinyinIMETraditionalEntry> tree) {
		if(null == tree) {
			throw new NullPointerException("tree cannot be null!");
		}
		
		this.tree = tree;
	}
	
	/**
	 * Obtain the tree wrapped by this Serializer instance.
	 * @return tree
	 */
	public PinyinTree<PinyinIMETraditionalEntry> getPinyinTree() {
		return this.tree;
	}
	
	/**
	 * Java custom serializer method.
	 * @param out
	 * @throws IOException
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		writeTree(this.tree, out);
	}
	
	/**
	 * Java custom deserializer method.
	 * @param in
	 * @throws IOException
	 */
	private void readObject(ObjectInputStream in) throws IOException {
		this.tree = readTree(in);
	}
	
	/**
	 * Compile the text dictionary and overwrite the binary copy.
	 * @param args
	 * @throws IOException
	 * @throws ParseException
	 */
	static public void main(String[] args) throws IOException, ParseException {
		URL textResource = PinyinIMETreeTextSerializer.class.getResource("dict.txt");
		
		InputStream dictStream = textResource.openStream();
		PinyinTree<PinyinIMETraditionalEntry> pyTree = PinyinIMETreeTextSerializer.readTree(dictStream);
		dictStream.close();
		
		FileOutputStream fileOut = new FileOutputStream("/home/jkiang/eclipse-workspaces/ime/ime2/src/java/org/kiang/chinese/pinyin/im/dict.dat", false);
		writeTree(pyTree, fileOut);
		fileOut.close();
	}
}
