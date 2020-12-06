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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.kiang.chinese.pinyin.tree.PinyinTreeTextSerializer.ValueSerializer;


/**
 * Implementation of ValueSerializer for serializing/deserializing to human
 * readable Strings.
 */
class PinyinIMEEntryTextValueSerializer implements ValueSerializer<PinyinIMETraditionalEntry> {

	static private final Pattern VALUE_PATTERN = Pattern.compile("\\s*(\\S+)\\s*,\\s*(\\d+)\\s*");
	
	/**
	 * @return String form of the IMEEntry
	 */
	public String toString(PinyinIMETraditionalEntry value) {
		StringBuilder sbuf = new StringBuilder(16);
		sbuf.append(value.getTraditional()).append(",").append(value.getFrequency());
		return sbuf.toString();
	}

	/**
	 * @param valueStr the String form of the IMEEntry
	 * @return the parsed IMEEntry
	 */
	public PinyinIMETraditionalEntry parseValue(String valueStr) {
		Matcher matcher = VALUE_PATTERN.matcher(valueStr);
		if(!matcher.matches()) {
			throw new IllegalArgumentException(valueStr + " is not a value!");
		}
		
		String word = matcher.group(1);
		String frequencyStr = matcher.group(2);
		int frequency = Integer.parseInt(frequencyStr);
		
		return new PinyinIMETraditionalEntry(word, frequency);
	}
}
