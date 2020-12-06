package org.kiang.chinese.dictionary.cedict;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.kiang.chinese.pinyin.PinyinUnit;


/**
 * A Parser for UTF-8 CEDICT.
 * http://www.mandarintools.com/cedict.html
 * 
 * Reads all the data into memory, returning
 * a List of CEDICTParser.Entry objects.
 * 
 * @author Jordan Kiang
 */
public class CEDICTParser {

	/**
	 * Regex pattern for a line in CEDICT.
	 * First group represents the traditional.
	 * Second group represents the simplified.
	 * Third group is the pinyin.
	 * Fourth group is the definition(s), delimited by /
	 */
	static private final Pattern CEDICT_LINE_PATTERN = Pattern.compile("(.*) (.*) \\[(.*)\\] /(.*)/");
	
	/**
	 * Parse an InputStream of UTF-8 CEDICT data.
	 * Reads the InputStream fully but does not close the stream.
	 * 
	 * @param inputStream
	 * @return List of data Entry objects
	 * @throws IOException
	 */
	static public List<Entry> parse(InputStream inputStream) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
		
		List<Entry> entries = new ArrayList<Entry>();
		String line;
		for(int lineNum = 1; null != (line = reader.readLine()); lineNum++) {
			if(line.startsWith("#")) {
				// it's a comment, ignore
				continue;
			}
			
			try {
				Entry entry = parseLine(line);
				entries.add(entry);
				
			} catch(RuntimeException e) {
				// couldn't parse a particular line
				//System.err.println("Can't parse line " + lineNum + ": " + line);
			
				// ignore for now, there's bound to be some...
			}
		}
		
		return entries;
	}
	
	/**
	 * Parse the CEDICT data for the given line.
	 * 
	 * @param line
	 * @return an Entry of CEDICT data parsed from the given line
	 * @throws IllegalArgumentException if the line cannot be parsed
	 */
	static private Entry parseLine(String line) {
		Matcher lineMatcher = CEDICT_LINE_PATTERN.matcher(line);
		if(!lineMatcher.matches()) {
			throw new IllegalArgumentException("can't parse line");
		}
		
		// first and second groups are traditional and simplified
		// characters, respectively.
		String traditional = lineMatcher.group(1);
		String simplified = lineMatcher.group(2);
		
		// third group in the pinyin.
		// units of pinyin delimited by spaces
		String pinyinGroup = lineMatcher.group(3);
		String[] pinyinSplit = pinyinGroup.split(" ");
		List<PinyinUnit> pinyinUnits = new ArrayList<PinyinUnit>(pinyinSplit.length);
		for(String pinyinStr : pinyinSplit) {
			PinyinUnit pinyinUnit = PinyinUnit.parseValue(pinyinStr); 
			pinyinUnits.add(pinyinUnit);
		}
		
		// fourth group is the definitions, delimited by /
		String definitionGroup = lineMatcher.group(4);
		String[] definitionSplit = definitionGroup.split("/");
		List<String> definitions = new ArrayList<String>(definitionSplit.length);
		for(String definition : definitionSplit) {
			definitions.add(definition);
		}
	
		return new Entry(traditional, simplified, pinyinUnits, definitions);
	}
	
	/**
	 * An immutable entry containing CEDICT data.
	 */
	static public class Entry {
		
		private String traditional;
		private String simplified;
		
		private List<PinyinUnit> pinyin;
		private List<String> definitions;
	
		/**
		 * @param traditional characters
		 * @param simplified characters
		 * @param pinyin
		 * @param definitions
		 */
		Entry(String traditional, String simplified, List<PinyinUnit> pinyin, List<String> definitions) {
			if(null == traditional) {
				throw new NullPointerException("traditional cannot be null!");
			} else if(null == simplified) {
				throw new NullPointerException("simplified cannot be null!");
			} else if(null == pinyin) {
				throw new NullPointerException("pinyin cannot be null!");
			} else if(null == definitions) {
				throw new NullPointerException("definitions cannot be null!");
			}
			
			this.traditional = traditional;
			this.simplified = simplified;
			
			this.pinyin = Collections.unmodifiableList(pinyin);
			this.definitions = Collections.unmodifiableList(definitions);
		}
		
		/**
		 * @return traditional characters
		 */
		public String getTraditional() {
			return this.traditional;
		}
		
		/**
		 * @return simplified characters
		 */
		public String getSimplified() {
			return this.simplified;
		}
		
		/**
		 * @return the pinyin
		 */
		public List<PinyinUnit> getPinyin() {
			return this.pinyin;
		}
		
		/**
		 * @return definitions
		 */
		public List<String> getDefinitions() {
			return this.definitions;
		}
		
		/**
		 * Returns data in the CEDICT format
		 * @return string representation
		 */
		public String toString() {
			StringBuilder sbuf = new StringBuilder();
			
			// append characters
			sbuf.append(this.traditional).append(" ").append(this.simplified);

			// append pinyin, enclosed in braces, spaces between units only
			sbuf.append(" [");
			Iterator<PinyinUnit> pinyinIter = this.pinyin.iterator();
			sbuf.append(pinyinIter.next().toString().toLowerCase());
			while(pinyinIter.hasNext()) {
				sbuf.append(" ").append(pinyinIter.next().toString().toLowerCase());
			}
			sbuf.append("]");
			
			// append definitions, "/" between each definition + preceding and trailing
			sbuf.append(" /");
			for(String definition : this.definitions) {
				sbuf.append(definition).append("/");
			}
			
			return sbuf.toString();
		}
	}
	
	static public void main(String[] args) throws IOException {
		InputStream in = CEDICTParser.class.getResourceAsStream("cedict_ts.u8");
		List<Entry> entries = CEDICTParser.parse(in);
	
		for(Entry entry : entries) {
			System.out.println(entry);
		}
		
		return;
	}
}
