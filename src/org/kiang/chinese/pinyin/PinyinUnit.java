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

package org.kiang.chinese.pinyin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Wraps a syllable and a tone into a single object
 * that represents one unit of Pinyin input.
 * 
 * @author Jordan Kiang
 */
public class PinyinUnit implements Comparable<PinyinUnit> {

	private PinyinSyllable syllable;
	private MandarinTone tone;
	
	/**
	 * Instantiate a new Pinyin unit.
	 * 
	 * @param syllable the syllable, cannot be null
	 * @param tone the tone, null if the tone is unspecified
	 */
	public PinyinUnit(PinyinSyllable syllable, MandarinTone tone) {
		if(null == syllable) {
			throw new NullPointerException("syllable required!");
		}
		
		this.syllable = syllable;
		this.tone = tone;
	}
	
	/**
	 * @return the Pinyin syllable of this input unit.
	 */
	public PinyinSyllable getSyllable() {
		return this.syllable;
	}
	
	/**
	 * @return the tone of the input unit
	 */
	public MandarinTone getTone() {
		return this.tone;
	}
	
	static private final Pattern PINYIN_PATTERN = Pattern.compile("([a-zA-ZüÜ:]*+)(\\d?)");
	
	/**
	 * Parse the value from the given String.
	 * Accepts Strings of the form [syllable][tone int], i.e. "zhong1"
	 * 
	 * @param pinyinStr
	 * @return parsed value
	 */
	static public PinyinUnit parseValue(String pinyinStr) {
		Matcher matcher = PINYIN_PATTERN.matcher(pinyinStr);
		if(!matcher.matches()) {
			throw new IllegalArgumentException("Cannot parse PinyinUnit from input: " + pinyinStr);
		}
		
		String syllableStr = matcher.group(1);
		PinyinSyllable syllable = PinyinSyllable.parseValue(syllableStr);
		
		String toneNumStr = matcher.group(2);
		MandarinTone tone = null;
		if(null != toneNumStr && toneNumStr.length() > 0) {
			int toneNum = Integer.parseInt(toneNumStr);
			tone = MandarinTone.valueOf(toneNum);
		}
		return new PinyinUnit(syllable, tone);
	}
	
	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		String toneStr = null != this.tone ? "" + Integer.toString(this.tone.getToneNum()) : "";
		return this.syllable + toneStr;
	}
	
	/**
	 * @see Comparable#compareTo(Object)
	 */
	public int compareTo(PinyinUnit that) {
		PinyinSyllable thisSyllable = this.getSyllable();
		PinyinSyllable thatSyllable = that.getSyllable();
		
		int compareTo = thisSyllable.compareTo(thatSyllable);
		
		if(compareTo == 0) {
			// equal syllables, compare tones.
			// tones can be null, so watch out...
			
			MandarinTone tone1 = this.getTone();
			MandarinTone tone2 = that.getTone();
		
			if(null != tone1) {
				if(null != tone2) {
					// both not null
					compareTo = tone1.compareTo(tone2);
				} else {
					// tone1 not null, tone 2 null, null comes first
					compareTo = 1;
				}
			
			} else if(null != tone2) {
				// tone 1 null, tone2 not null, null comes first
				compareTo = -1;
			
			} else {
				// both null
				compareTo = 0;
			}
		}
		
		return compareTo;
	}
	
	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object o) {
		if(o instanceof PinyinUnit) {
			PinyinUnit that = (PinyinUnit)o;
			
			return this.syllable == that.syllable &&
				this.tone == that.tone;
		}
		
		return false;
	}
	
	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}
}
