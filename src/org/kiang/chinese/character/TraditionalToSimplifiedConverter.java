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

package org.kiang.chinese.character;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * Class w/ some static methods to convert Strings/characters
 * in traditional Chinese to simplified Chinese.  Traditional
 * to simplified only, the reverse is more complicated..
 * 
 * Translation table is loaded from the included hcutf8.txt file.
 * Copyright (C) 1988,1989,1990,1993 by Fung F. Lee & Ricky Yeung
 * Assumed to be free based on its usage in other free software.
 * 
 * @author Jordan Kiang
 */
public class TraditionalToSimplifiedConverter {

	// mapping file
	static private final String RESOURCE_NAME = "hcutf8.txt";
	
	// CJK unified range.
	// this range means about 21k characters.
	// at 2 bytes per, this means about 42k statically stored
	// when this class is loaded
    static private final char MIN_CHAR = '\u4e00';	// lowest supported character
    static private final char MAX_CHAR = '\u9fff';	// highest supported character

    // the mapping table.
    // indices are the traditional ints - the min char.
    // stored value is the simplified
    static private final char[] table = initTable();

    /**
     * Parse a mapping table from a resource file.
     * The returned table maps traditional character indices
     * to simplified characters.  The indices are the traditional
     * character as an int minus the MIN_CHAR.
     * 
     * @return mapping table
     */
    static private char[] initTable() {
    	// mapping file is defined in the accompanying resource
    	InputStream hcutf8Stream = TraditionalToSimplifiedConverter.class.getResourceAsStream(RESOURCE_NAME);    	
    	
    	// table is dimensioned according to specified range
    	char[] table = new char[MAX_CHAR - MIN_CHAR + 1];
    	
    	// read in the file line by line
    	String line;
    	BufferedReader reader = new BufferedReader(new InputStreamReader(hcutf8Stream, Charset.forName("UTF-8")));
    	int lineNum = 1;
    	try {
    		// format is two chars per mapping line.
    		// first char is the simplified, next char
    		// is the traditional.
	    	for(; null != (line = reader.readLine()); lineNum++) {
	    		if(!line.startsWith("#") && line.length() == 2) {
	    			char simplified = line.charAt(0);
	    			char traditional = line.charAt(1);
	    			
	    			if(traditional >= MIN_CHAR && traditional <= MAX_CHAR) {
	    				table[traditional - MIN_CHAR] = simplified;			
	    			}
	    		}
	    	}
	    	
	    	hcutf8Stream.close();
	    	
	    	return table;
    	
    	} catch(IOException ioe) {
    		// throw a runtime rather than requiring an annoying
    		// IOException catch with every use.
    		throw new RuntimeException("Error reading line " + lineNum + " from " + RESOURCE_NAME + "!", ioe);
    	}
    }
    
    /**
     * Obtain the simplified character for the given traditional.
     * @param traditional
     * @return simplified
     */
    static public char toSimplified(char traditional) {
    	if(traditional < MIN_CHAR || traditional > MAX_CHAR) {
    		throw new IllegalArgumentException("traditional character out of range!");
    	}
    	
    	char simplified = table[traditional - MIN_CHAR];
    	if(simplified > 0) {
    		// > 0 means value initialized,
    		// so there is a mapping
    		return simplified;
    	}
    	
    	// simplified uninitialized, no mapping.
    	// just return the traditional
    	return traditional;
    }
    
    /**
     * Obtain the simplified version of the given String.
     * @param traditional
     * @return simplified
     */
    static public String toSimplified(String traditional) {
    	char[] simplified = new char[traditional.length()];
    	
    	for(int i = 0; i < simplified.length; i++) {
    		simplified[i] = toSimplified(traditional.charAt(i));
    	}
    	
    	return new String(simplified);
    }	
}
