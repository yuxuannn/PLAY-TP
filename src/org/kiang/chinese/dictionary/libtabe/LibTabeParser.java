/*
 * Created on Feb 23, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.kiang.chinese.dictionary.libtabe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.kiang.chinese.pinyin.PinyinUnit;
import org.kiang.chinese.pinyin.im.PinyinIMETraditionalEntry;
import org.kiang.chinese.pinyin.tree.PinyinTree;
import org.kiang.chinese.zhuyin.PinyinZhuyinConverter;


/**
 * 
 * 
 * @author Jordan Kiang
 */
public class LibTabeParser {

	/**
	 * Matches a libtabe line.
	 * Group 1 is the traditional characters.
	 * Group 2 is the frequency.
	 * Group 3 is the zhuyin.
	 */
    static private final Pattern LIBTABE_LINE_PATTERN = Pattern.compile("^(\\p{InCJKUnifiedIdeographs}+)\\s+(\\d+)\\s+(.*)");
    
    /**
     * Some Zhuyin units can be represented w/ multiple pronunciations.
     * Such cases are comma delimited within braces [].
     */
    static private final Pattern ZHUYIN_MULTIPLE_MATCH_PATTERN = Pattern.compile("\\[(.+?)\\]");
    
    /**
     * Zhuyin pattern is zhuyin characters with an optional tone.
     */
    static private final Pattern ZHUYIN_PATTERN = Pattern.compile("(\\p{InBopomofo}+)(\\d?)");
 
    /**
     * Parse the entries of libtabe's tsi.src from the given InputStream.
     * Reads the stream fully, but does not close the stream.
     * 
     * @param inputStream
     * @param minimumCharacterFrequency ignore 1-character words with frequencies below this
     * @param minimumWordFrequency ignore multiple character words with frequencies below this
     * @return a List of all entries in the given InputStream
     * @throws IOException
     */
    static public List<Entry> parse(InputStream inputStream, int minimumCharacterFrequency, int minimumWordFrequency) throws IOException {
    	BufferedReader lineReader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("Big5")));
   	
    	PinyinZhuyinConverter zhuyinConverter = new PinyinZhuyinConverter();
        
    	List<Entry> entries = new ArrayList<Entry>();
    	String line;
    	for(int lineNum = 1; null != (line = lineReader.readLine()); lineNum++) {
    		if(line.startsWith("#")) {
    			// it's a comment
    			continue;
    		}
    	
    		try {
    			Entry entry = parseLine(line, zhuyinConverter);
    			
    			String traditional = entry.getTraditional();
    			int frequency = entry.getFrequency();
    			
    			int wordLength = traditional.length();
    			
    			if((wordLength == 1 && frequency >= minimumCharacterFrequency) ||
    					wordLength > 1 && frequency >= minimumWordFrequency) {
    				entries.add(entry);
    			}
    					
    					
    		} catch(RuntimeException e) {
    			//System.err.println("Can't parse line " + lineNum + ": " + line);
    		}
    	}
    	
    	return entries;
    }
   
    /**
     * Parse the given line into an Entry.
     * @param line
     * @param zhuyinConverter for converting zhuyin to pinyin
     * @return Entry derived from the line
     */
    static private Entry parseLine(String line, PinyinZhuyinConverter zhuyinConverter) {
    	Matcher lineMatcher = LIBTABE_LINE_PATTERN.matcher(line);
    	if(!lineMatcher.matches()) {
    		throw new IllegalArgumentException("can't parse line!");
    	}
    	
    	String traditional = lineMatcher.group(1);
    	String frequencyStr = lineMatcher.group(2);
    	String zhuyinGroup = lineMatcher.group(3);
    	
    	int wordLength = traditional.length();
    	int frequency = Integer.parseInt(frequencyStr);
    	List<List<PinyinUnit>> pinyin = parseZhuyinGroup(wordLength, zhuyinGroup, zhuyinConverter);
    	
    	return new Entry(traditional, pinyin, frequency);
    }     

    /**
     * Parse the zhuyin.
     * Returns a List of List of PinyinUnits.
     * The inner lists contain alternatives where multiple pronunciations are possible for that syllable.
     * Most of the inner Lists will therefore only have one entry.
     * 
     * @param wordLength
     * @param zhuyinGroup
     * @param zhuyinConverter
     * @return pinyin
     */
    static private List<List<PinyinUnit>> parseZhuyinGroup(int wordLength, String zhuyinGroup, PinyinZhuyinConverter zhuyinConverter) {
    	

    	List<List<PinyinUnit>> pinyin;
    	
    	if(wordLength == 1) {
    		// word is only 1 char long
    		// slightly confusing format rule where if the word is only one char
    		// long then splitting on whitespace gives the alternative pronunciations
    		// of the first and only syllable, vs. the sequence of syllables for
    		// multi-char words (no [] braces used for 1 char words).
    		pinyin = new ArrayList<List<PinyinUnit>>(1);
    		
    		List<PinyinUnit> alternatives = parseAlternatives(zhuyinGroup, zhuyinConverter);	
    		pinyin.add(alternatives);
    		
    	} else {
    		// word is longer than 1 char.
    		
        	// split the zhuyin group on whitespace.
        	// each split item represents one of the syllables in the word,
        	// unless the word is only one character long, in which case
        	// the split represents multiple possible alternatives for the one syllable.
        	String[] zhuyinUnitsStrs = zhuyinGroup.split("\\s+");
        	pinyin = new ArrayList<List<PinyinUnit>>(zhuyinUnitsStrs.length);
    		
	    	for(String zhuyinUnitStr : zhuyinUnitsStrs) {
	    		
	    		List<PinyinUnit> alternatives;
	    		
	    		// check if the unit has multiple possible pronunciations.
	    		// if so it's in a comma-delimited format surrounded by braces [].
	    		Matcher multipleMatcher = ZHUYIN_MULTIPLE_MATCH_PATTERN.matcher(zhuyinUnitStr);
	        	if(multipleMatcher.matches()) {
	        		String alternativesStr = multipleMatcher.group(1);
	        		alternatives = parseAlternatives(alternativesStr, zhuyinConverter);
	        	
	        	} else {
	        		// only one possible pronuncation
	        		PinyinUnit pinyinUnit = parseZhuyin(zhuyinUnitStr, zhuyinConverter);
	        		alternatives = new ArrayList<PinyinUnit>(1);
	        		alternatives.add(pinyinUnit);	
	        	}
	        	
	        	pinyin.add(Collections.unmodifiableList(alternatives));
	    	}
    	}
    	return Collections.unmodifiableList(pinyin);
    }
    
    static private List<PinyinUnit> parseAlternatives(String alternativesStr, PinyinZhuyinConverter zhuyinConverter) {
		// split on comma or whitespace.  comma is used between alternatives
    	// contained in braces [], space used when word is only 1 char long.
    	String[] alternativeSplit = alternativesStr.split(",|\\s");
		List<PinyinUnit> alternatives = new ArrayList<PinyinUnit>(alternativeSplit.length);
		
		// add each possible alternative pronuncation to the list.
		for(String alternative : alternativeSplit) {
			PinyinUnit pinyinUnit = parseZhuyin(alternative, zhuyinConverter);
			alternatives.add(pinyinUnit);
		}   
		
		return alternatives;
    }
    
    /**
     * Parse a zhuyin string into a PinyinUnit.
     * Input String should be zhuyin possibly trailed by a tone integer.
     * 
     * @param zhuyinStr
     * @param zhuyinConverter
     * @return pinyin
     */
    static private PinyinUnit parseZhuyin(String zhuyinStr, PinyinZhuyinConverter zhuyinConverter) {
    	Matcher zhuyinMatcher = ZHUYIN_PATTERN.matcher(zhuyinStr);
    	if(zhuyinMatcher.matches()) {
    		String zhuyinSyllableStr = zhuyinMatcher.group(1);
    		String toneNumStr = zhuyinMatcher.group(2);
    		
    		// no explicit tone means first tone
    		// (5 == neutral tone)
    		if(null == toneNumStr || toneNumStr.length() == 0) {
    			toneNumStr = "1";
    		}
    		
    		String pinyinSyllableStr = zhuyinConverter.toPinyin(zhuyinSyllableStr);
    		return PinyinUnit.parseValue(pinyinSyllableStr + toneNumStr);
    	}
    	
    	throw new IllegalArgumentException("couldn't parse PinyinUnit from zhuyin: " + zhuyinStr);
    }
    
    /**
     * Wraps the data contained in a line of the LibTabe file.
     */
	static public class Entry implements Comparable<Entry> {
		private String traditional;
		private List<List<PinyinUnit>> pinyin;
		private int frequency;
		
		/**
		 * @param traditional traditional characters
		 * @param pinyin pinyin representation of the pronunciation
		 * @param frequency the frequency of the word, the higher the more frequent
		 */
		Entry(String traditional, List<List<PinyinUnit>> pinyin, int frequency) {
			if(null == traditional) {
				throw new NullPointerException("traditional cannot be null!");
			} else if(null == pinyin) {
				throw new NullPointerException("pinyin cannot be null!");
			}
			
			this.traditional = traditional;
			this.pinyin = pinyin;
			this.frequency = frequency;
		}
		
		/**
		 * @return the traditional characters
		 */
		public String getTraditional() {
			return this.traditional;
		}
		
		/**
		 * Obtain the pinyin pronuncation.
		 * Inner lists are possible alternative pronuncations.
		 * 
		 * @return pinyin
		 */
		public List<List<PinyinUnit>> getPinyin() {
			return this.pinyin;
		}
		
		/**
		 * Relative frequency of the word, the higher the more frequent.
		 * @return frequency
		 */
		public int getFrequency() {
			return this.frequency;
		}

		/**
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		public int compareTo(Entry o) {
			return o.frequency - this.frequency;
		}
		
		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object o) {
			if(o instanceof Entry) {
				Entry that = (Entry)o;
				
				return this.traditional.equals(that.traditional) &&
						this.pinyin.equals(that.pinyin) &&
						this.frequency == that.frequency;
			}	
		
			return false;
		}
		
		/**
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode() {
			return this.traditional.hashCode() + this.pinyin.hashCode() + this.frequency;
		}
	}

	static public void main(String[] args) throws IOException {
		/*
		String str = "[ㄧ,ㄧ4]";
		Matcher matcher = ZHUYIN_MULTIPLE_MATCH_PATTERN.matcher(str);
		matcher.matches();
		
		System.out.println(matcher.group(1));
		*/
		
		
		InputStream inputStream = LibTabeParser.class.getResourceAsStream("tsi.src");
		List<Entry> entries = LibTabeParser.parse(inputStream, Integer.MIN_VALUE, Integer.MIN_VALUE);
		
		int max = Integer.MIN_VALUE;
		for(Entry entry : entries) {
			max = Math.max(entry.getFrequency(), max);
		}
		
		System.out.println(max);
		return;
		
	}
}
