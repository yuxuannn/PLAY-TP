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

import java.io.BufferedInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.kiang.chinese.pinyin.MandarinTone;
import org.kiang.chinese.pinyin.PinyinSyllable;
import org.kiang.chinese.pinyin.PinyinUnit;
import org.kiang.chinese.pinyin.tree.PinyinTreeByteStreamSerializer;
import org.kiang.io.InputStreamSource;
import org.kiang.util.MutableInteger;


/**
 * An implementation of PinyinDictionary that reads its values
 * from a stream.  Every lookup causes it to reopen an InputStream
 * from the start and look for the requested entries.  As a consequence,
 * it is slower, but has hardly any memory footprint.
 * 
 * @author Jordan Kiang
 */
public class PinyinIMEStreamDictionary implements PinyinIMEDictionary {

	private InputStreamSource streamSource;
	
	/**
	 * Generate a new dictionary that streams its data on demand
	 * from a stream obtained from the given stream source.
	 * 
	 * @param streamSource
	 */
	public PinyinIMEStreamDictionary(InputStreamSource streamSource) {
		if(null == streamSource) {
			throw new NullPointerException("streamSource cannot be null!");
		}
		
		this.streamSource = streamSource;
	}
	
	/**
	 * Blocks until the given number of bytes are skipped from
	 * the given stream.  Okay to do this because we know what
	 * we're expecting out of the stream.
	 * @param bytes number of bytes to skip
	 * @param in
	 * @throws IOException
	 */
	static private void skipFully(int bytes, DataInput in) throws IOException {
		int skipped = 0;
		while(skipped < bytes) {
			skipped += in.skipBytes(bytes - skipped);
		}
	}
	
	/**
	 * @see org.kiang.chinese.pinyin.im.PinyinIMEDictionary#lookup(java.util.List, boolean)
	 */
	public List<PinyinIMEEntry> lookup(List<PinyinUnit> input, boolean anticipate) {
		
		Stack<PinyinUnit> pinyinStack = new Stack<PinyinUnit>();
		for(int i = input.size() - 1; i >= 0; i--) {
			pinyinStack.push(input.get(i));
		}
		
		Set<PinyinIMETraditionalEntry> entries = new HashSet<PinyinIMETraditionalEntry>();
		
		try {
			DataInputStream inStream = new DataInputStream(new BufferedInputStream(this.streamSource.openStream()));
			readFromPosition(pinyinStack, anticipate, inStream, entries);
			inStream.close();
			
		} catch(IOException ioe) {
			// TODO pick a Runtime
			throw new RuntimeException(ioe);
		}
		
		List<PinyinIMEEntry> sortedEntries = new ArrayList<PinyinIMEEntry>(entries);
		Collections.sort(sortedEntries);
		
		return sortedEntries;
	}
	
	private int readFromPosition(Stack<PinyinUnit> pinyinStack, boolean anticipate, DataInput in, Set<PinyinIMETraditionalEntry> addTo) throws IOException {
		int bytesRead = 0;
		
		PinyinUnit unit = null;
		PinyinSyllable inputSyllable = null;
		MandarinTone inputTone = null;
		
		if(!pinyinStack.isEmpty()) {
			unit = pinyinStack.pop();
			
			inputSyllable = unit.getSyllable();
			inputTone = unit.getTone();
		}
		
		PinyinSyllable[] syllables = PinyinSyllable.values();
		
		short childCount = in.readShort();
		for(short i = 0; i < childCount; i++) {
			
			short syllableOrdinal = in.readShort();
			byte toneNum = in.readByte();
			int nodeByteCount = in.readInt();
			
			bytesRead += PinyinTreeByteStreamSerializer.SHORT_SIZE;
			bytesRead += PinyinTreeByteStreamSerializer.BYTE_SIZE;
			bytesRead += nodeByteCount;
			
			PinyinSyllable readSyllable = syllables[syllableOrdinal];
			MandarinTone readTone = MandarinTone.valueOf(toneNum);
			
			if((null == inputSyllable || inputSyllable.equals(readSyllable)) && (null == inputTone || inputTone.equals(readTone))) {
				// we need to explore the node represented by this position in the stream
			
				if(pinyinStack.isEmpty()) {
					// we collect the values from this node by
					// reading them in from the stream.
					
					int valueByteCount = this.readValues(in, addTo);
				
					if(anticipate) {
						// we're out of PinyinUnits, but we're anticipating longer words,
						// so we additionally read the values from the child node.
						readFromPosition(pinyinStack, anticipate, in, addTo);
					
					} else {
						// we've reached the end of this pinyin chain and we're not anticipating,
						// so we just skip the child node bytes 
						int childNodesByteCount = nodeByteCount - valueByteCount;
						skipFully(childNodesByteCount, in);	
					}
					
				} else {
					// we still have PinyinUnits to peel off to get to the
					// appropriate depth to start reading values.
					// so we read the values out of the stream to skip
					// them, and then continue reading.
					this.readValues(in, null);
					this.readFromPosition(pinyinStack, anticipate, in, addTo);
				}
				
			} else {
				// this node doesn't match, ignore it
				skipFully(nodeByteCount, in);
			}
		}
		
		if(null != unit) {
			pinyinStack.push(unit);
		}
		
		return bytesRead;
	}
	
	private int readValues(DataInput in, Set<PinyinIMETraditionalEntry> addTo) throws IOException {
		MutableInteger byteCount = new MutableInteger();
		
		// first short indicates how many values are stored here
		short valueCount = in.readShort();
		byteCount.value = PinyinTreeByteStreamSerializer.SHORT_SIZE;
		
		for(short i = 0; i < valueCount; i++) {
			PinyinIMETraditionalEntry value = PinyinIMEEntryByteSerializer.readValueStatic(in, byteCount);
			if(null != addTo) {
				addTo.add(value);
			}
		}
		
		return byteCount.value;
	}
}
