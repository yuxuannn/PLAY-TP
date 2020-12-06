/*
 * Copyright (C) 2005 Jordan Kiang
 * jordan-at-kiang.org
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.kiang.chinese.zhuyin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts Pinyin Strings to Zhuyin Strings and vice versa.
 * Depends on the PINYIN_ZHUYIN_MAP_FILE which should be in the same location the class file.
 * 
 * u: is represted by the '\u00fc' char.
 */
public class PinyinZhuyinConverter {
    
    // File contains all the mappings, one per line.
    static private final String PINYIN_ZHUYIN_MAP_RESOURCE = "pinyin_to_zhuyin.u8";
    
    // Lines in the PINYIN_ZHUYIN_MAP_FILE should conform this pattern: Pinyin whitespace Zhuyin
    static private final Pattern LINE_PATTERN = Pattern.compile("^([a-uw-z\\\u00fc]+)\\s(\\p{InBopomofo}+)$");
        
    private Map<String, String> pinyinToZhuyinMap;
    private Map<String, String> zhuyinToPinyinMap;
    
    /**
     * Create a new converter.
     * @throws IOException if there's an error reading the mapping resource
     */
    public PinyinZhuyinConverter() throws IOException {
        this.initMappings();
    }
    
    /*
     * Initialize the pinyinToZhuyinMap and zhuyinToPinyinMap.
     * Reads in the mappings from the PINYIN_ZHUYIN_MAP_FILE.
     */
    private void initMappings() throws IOException {
        final Map<String, String> pinyinToZhuyinMap = new HashMap<String, String>();
        final Map<String, String> zhuyinToPinyinMap = new HashMap<String, String>();
        
        InputStream resourceStream = PinyinZhuyinConverter.class.getResourceAsStream(PINYIN_ZHUYIN_MAP_RESOURCE);
        if(null == resourceStream) {
        	throw new IOException("mapping resource " + PINYIN_ZHUYIN_MAP_RESOURCE + " not found");
        }
        
        int lineNum = 1;
        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(resourceStream, Charset.forName("UTF-8")));
        for(; null != (line = reader.readLine()); lineNum++) {
        	Matcher lineMatcher = LINE_PATTERN.matcher(line);
        	
        	if(lineMatcher.matches()) {
        		// line should contain a mapping
                String pinyin = lineMatcher.group(1);
                String zhuyin = lineMatcher.group(2);
             
                pinyinToZhuyinMap.put(pinyin, zhuyin);
                zhuyinToPinyinMap.put(zhuyin, pinyin);
                   
            } else if(!line.matches("^\\s*$") && !line.matches("^\\s*#.*")) {
            	// not a mapping, and it's non-empty but not a comment
            	System.err.println("line " + lineNum + " unrecognizable.");
            }
        }
        
        // Initialize the instance maps.
        // These maps will retrievable so we make them unmodifiable.
        this.pinyinToZhuyinMap = Collections.unmodifiableMap(pinyinToZhuyinMap);
        this.zhuyinToPinyinMap = Collections.unmodifiableMap(zhuyinToPinyinMap);
    }
    
    /////////////////
    
    /**
     * Get the Pinyin to Zhuyin Map.  Returned Map is unmodifiable.
     * @return the Map
     */
    public Map<String, String> getPinyinToZhuyinMap() {
        return Collections.unmodifiableMap(this.pinyinToZhuyinMap);
    }
    
    /**
     * Get the Zhuyin to Pinyin Map.  Returned Map is unmodifiable.
     * @return the Map
     */
    public Map<String, String> getZhuyinToPinyinMap() {
        return Collections.unmodifiableMap(this.zhuyinToPinyinMap);
    }

    /**
     * Get the Pinyin for the given Zhuyin String.
     * @param zhuyin
     * @return the Pinyin, null if no mapping found
     */
    public String toPinyin(String zhuyin) {
        return this.zhuyinToPinyinMap.get(zhuyin);
    }
    
    /**
     * Get the Zhuyin for the given Pinyin String.
     * @param pinyin
     * @return the Zhuyin, null if no mapping found
     */
    public String toZhuyin(String pinyin) {
        return this.pinyinToZhuyinMap.get(pinyin);
    }
    
    /**
     * Test main method
     * @param args
     * @throws IOException
     */
    static public void main(String[] args) throws IOException {
    	String jiangPinyin = "jiang";
    	String jiangZhuyin = "\u3110\u3127\u3124";
    	
    	PinyinZhuyinConverter converter = new PinyinZhuyinConverter();
    	System.out.println(jiangPinyin + ":" + converter.toZhuyin(jiangPinyin));
    	System.out.println(jiangZhuyin = ":" + converter.toPinyin(jiangZhuyin));
    }
}
