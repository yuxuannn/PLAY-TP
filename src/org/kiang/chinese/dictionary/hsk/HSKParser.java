package org.kiang.chinese.dictionary.hsk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.kiang.chinese.pinyin.PinyinUnit;


public class HSKParser {

	static public List<Entry> parse(InputStream hskStream) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(hskStream, Charset.forName("UTF-8")));
		
		List<Entry> entries = new ArrayList<Entry>();
		String line;
		for(int lineNum = 1; null != (line = reader.readLine()); lineNum++) {
			try {
				entries.add(parseEntry(line));
			} catch(IllegalArgumentException iae) {
				System.err.println("unable to parse line " + lineNum + ": " + line);
				iae.printStackTrace();
			}
		}
		
		return entries;
	}
	
	static private Entry parseEntry(String line) {
		String[] split = line.split(",");
		
		String word = split[0].trim();
		List<PinyinUnit> pinyin = parsePinyin(split[1]);
		int level = Integer.parseInt(split[2].trim());
		
		return new Entry(word, pinyin, level);
	}
	
	static private List<PinyinUnit> parsePinyin(String pinyin) {
		String[] pinyinUnitStrs = pinyin.trim().split("\\s+");
		List<PinyinUnit> pinyinUnits = new ArrayList<PinyinUnit>(pinyinUnitStrs.length);
		
		for(String unitStr : pinyinUnitStrs) {
			pinyinUnits.add(PinyinUnit.parseValue(unitStr));
		}
		
		return pinyinUnits;
	}
	
	static public class Entry {
		private List<PinyinUnit> pinyin;
		private String word;
		private int level;
		
		Entry(String word, List<PinyinUnit> pinyin, int level) {
			if(null == word) {
				throw new NullPointerException("word cannot be null!");
			} else if(null == pinyin) {
				throw new NullPointerException("pinyin cannot be null!");
			}
			
			this.word = word;
			this.pinyin = Collections.unmodifiableList(pinyin);
			this.level = level;
		}
		
		/**
		 * @return the word
		 */
		public String getWord() {
			return this.word;
		}
		
		/**
		 * @return the pinyin
		 */
		public List<PinyinUnit> getPinyin() {
			return this.pinyin;
		}
		
		/**
		 * @return the HSK word level
		 */
		public int getLevel() {
			return this.level;
		}
	}
	
	static public void main(String[] args) throws IOException {
		InputStream hskStream = HSKParser.class.getResourceAsStream("hsk_parsed.txt");
		List<Entry> entries = parse(hskStream);
		
		for(Entry entry : entries) {
			System.out.println(entry.getWord() + " " + entry.getPinyin() + " " + entry.getLevel());
		}
	}
}
