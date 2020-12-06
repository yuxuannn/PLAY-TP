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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.kiang.chinese.pinyin.tree.PinyinTreeByteStreamSerializer;
import org.kiang.chinese.pinyin.tree.PinyinTreeByteStreamSerializer.ValueSerializer;
import org.kiang.util.MutableInteger;


/**
 * An implementation of ValueSerializer for reading/writing
 * IMEEntry instances.
 */
class PinyinIMEEntryByteSerializer implements ValueSerializer<PinyinIMETraditionalEntry> {
	
	/**
	 * Read an IMEEntry from the input.
	 * @see ValueSerializer#readValue(DataInput, MutableInteger)
	 */
	public PinyinIMETraditionalEntry readValue(DataInput in, MutableInteger position) throws IOException {
		return readValueStatic(in, position);
	}

	/**
	 * Write the IMEEntry to the output stream.
	 * @see ValueSerializer#writeValue(Object, DataOutput)
	 */
	public void writeValue(PinyinIMETraditionalEntry value, DataOutput out) throws IOException {
		writeValueStatic(value, out);
	}
	
	/**
	 * Static method read helper.
	 * Can use without instantiating an instance..
	 * @see ValueSerializer#readValue(DataInput, MutableInteger)
	 */
	static PinyinIMETraditionalEntry readValueStatic(DataInput in, MutableInteger position) throws IOException {
		// the number of bytes in the word part is
		// first in the stream.  read that so we
		// know how many bytes to read for the word.
		short wordByteCount = in.readShort();
		
		// read the word
		byte[] wordBytes = new byte[wordByteCount];
		in.readFully(wordBytes);
		String word = new String(wordBytes, "UTF-8");
		
		// read the frequency
		int frequency = in.readInt();
		
		// increment the position to reflect
		// having read the word and frequency
		if(null != position) {
			position.value += PinyinTreeByteStreamSerializer.SHORT_SIZE;
			position.value += wordBytes.length;
			position.value += PinyinTreeByteStreamSerializer.INT_SIZE;
		}
		
		return new PinyinIMETraditionalEntry(word, frequency);
	}
	
	/**
	 * Static method write helper.
	 * Can use without instanting an instance.
	 * @see ValueSerializer#writeValue(Object, DataOutput)
	 */
	static void writeValueStatic(PinyinIMETraditionalEntry value, DataOutput out) throws IOException {
		String traditional = value.getTraditional();
		byte[] traditionalBytes = traditional.getBytes();
		
		// write out the number of bytes in the
		// word so we'll know how much to read
		// back in when we deserialize
		out.writeShort(traditionalBytes.length);
		
		// write the word
		out.write(traditionalBytes);
		
		// write the frequency
		out.writeInt(value.getFrequency());		
	}
}