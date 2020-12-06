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

/**
 * Represents a tone in standard Mandarin Chinese.
 * @author Jordan Kiang
 */
public enum MandarinTone {

	/** first tone */
	FIRST(1),
	
	/** second tone */
	SECOND(2),
	
	/** third tone */
	THIRD(3),
	
	/** fourth tone */
	FOURTH(4),
	
	/** fifth tone */
	NEUTRAL(5);
	
	private int toneNum;
	
	private MandarinTone(int toneNum) {
		this.toneNum = toneNum;
	}
	
	/**
	 * Obtain the tone # of the Tone.
	 * @return tone #
	 */
	public int getToneNum() {
		return this.toneNum;
	}
	
	/**
	 * Obtain the Tone instance for the given tone #
	 * @param toneNum
	 * @return the Tone
	 * @throws IllegalArgumentException if there is no such tone
	 */
	static public MandarinTone valueOf(int toneNum) throws IllegalArgumentException {
		// cycle through the tones, return the
		// one that matches the given #
		MandarinTone[] values = MandarinTone.values();
		for(MandarinTone tone : values) {
			if(tone.toneNum == toneNum) {
				return tone;
			}
		}
		
		// prefer 5, but additionally accept 0
		// to indicate the neutral tone.
		if(toneNum == 0) {
			return NEUTRAL;
		}
		
		throw new IllegalArgumentException("tone " + toneNum + " does not exist!");
	}
}
