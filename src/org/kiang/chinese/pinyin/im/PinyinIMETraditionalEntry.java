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

import org.kiang.chinese.character.TraditionalToSimplifiedConverter;

/**
 * An implementation of PinyinIMEEntry that's instantiated
 * with only the traditional form of the word, and generates
 * its simplified form on the fly.  This works pretty well
 * since traditional characters are many to one to simplified,
 * and it allows us to get away with only storing one form
 * of the word in our dictionaries.
 * 
 * @author Jordan Kiang
 */
public class PinyinIMETraditionalEntry implements PinyinIMEEntry {

	private String traditional;
	private int frequency;
	
	/** 
	 * @param traditional the in traditional characters
	 * @param pinyin the pinyin
	 * @param frequency the frequency of that word, the higher the more frequent
	 */
	public PinyinIMETraditionalEntry(String traditional, int frequency) {
		if(null == traditional) {
			throw new NullPointerException("traditional cannot be null!");
		}
		
		this.traditional = traditional;
		this.frequency = frequency;
	}
	
	/**
	 * @see org.kiang.chinese.pinyin.im.PinyinIMEEntry#getTraditional()
	 */
	public String getTraditional() {
		return this.traditional;
	}
	
	/**
	 * @see org.kiang.chinese.pinyin.im.PinyinIMEEntry#getSimplified()
	 */
	public String getSimplified() {
		// convert traditional tp simplified on the fly
		return TraditionalToSimplifiedConverter.toSimplified(this.traditional);
	}
	
	/**
	 * The relative frequency of this word.
	 * The higher the number the more frequent.
	 * @return frequency
	 */
	public int getFrequency() {
		return this.frequency;
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(PinyinIMEEntry that) {
		int compareTo = that.getFrequency() - this.frequency;
		if(compareTo == 0) {
			// if the frequencies match, break the tie with the word
			compareTo = this.traditional.compareTo(that.getTraditional());
		}
		
		return compareTo;
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if(o instanceof PinyinIMETraditionalEntry) {
			PinyinIMETraditionalEntry that = (PinyinIMETraditionalEntry)o;
			
			return this.traditional.equals(that.traditional);
				//&& this.frequency == that.frequency;
			
		}
		
		return false;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.traditional.hashCode();// * (this.frequency + 1);
	}
	
	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return this.traditional;
	}
}
