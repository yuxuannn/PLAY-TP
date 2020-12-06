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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.ParseException;

import org.kiang.chinese.pinyin.tree.PinyinTree;
import org.kiang.chinese.pinyin.tree.PinyinTreeTextSerializer;


/**
 * Serialization/deserialization methods for a reading/writing
 * a PinyinTree containing IMEEntrys into a a textual, human-readable
 * form.  Can wrap instances PinyinTree<IMEEntry> in these
 * to serialize via Java's standard serialize mechanisms.  Or
 * can just read/write directly to streams using the static methods.
 * 
 * @author Jordan Kiang
 */
public class PinyinIMETreeTextSerializer implements Serializable  {

	private PinyinTree<PinyinIMETraditionalEntry> tree;
	
	/**
	 * Static helper for writing a tree out to the given stream.
	 * @param tree
	 * @param out
	 * @throws IOException
	 */
	static public void writeTree(PinyinTree<PinyinIMETraditionalEntry> tree, OutputStream out) throws IOException {
		PinyinTreeTextSerializer.writeTree(tree, new PinyinIMEEntryTextValueSerializer(), out);
	}
	
	/**
	 * Static helper for reading a tree in from the given stream.
	 * @param in
	 * @return read in tree
	 * @throws IOException
	 * @throws ParseException
	 */
	static public PinyinTree<PinyinIMETraditionalEntry> readTree(InputStream in) throws IOException, ParseException {
		return PinyinTreeTextSerializer.readTree(new PinyinIMEEntryTextValueSerializer(), in);
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
	public PinyinIMETreeTextSerializer(PinyinTree<PinyinIMETraditionalEntry> tree) {
		if(null == tree) {
			throw new NullPointerException("tree cannot be null!");	
		}
		this.tree = tree;
	}
	
	/**
	 * @return the tree wrapped by this Serializable instance.
	 */
	public PinyinTree<PinyinIMETraditionalEntry> getPinyinTree() {
		return this.tree;
	}
	
	/**
	 * Java serializer method.
	 * @param out
	 * @throws IOException
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		PinyinTreeTextSerializer.writeTree(this.tree, new PinyinIMEEntryTextValueSerializer(), out);
	}
	
	/**
	 * Java deserializer method.
	 * @param in
	 * @throws IOException
	 */
	private void readObject(ObjectInputStream in) throws IOException {
		try {
			this.tree = PinyinTreeTextSerializer.readTree(new PinyinIMEEntryTextValueSerializer(), in);	
		} catch(ParseException pe) {
			throw new IOException("deserialization failed!", pe);
		}
	}
}
