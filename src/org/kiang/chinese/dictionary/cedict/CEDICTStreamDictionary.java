package org.kiang.chinese.dictionary.cedict;

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
import java.util.regex.PatternSyntaxException;


public class CEDICTStreamDictionary {

	//static private final Pattern LINE_PATTERN = Pattern.compile("(\\p{InCJKUnifiedIdeographs}+).*");	
	static private final Pattern LINE_PATTERN = Pattern.compile("(\\S+)\\s+(\\S+)\\s+\\[(.*)\\]\\s+/(.*)/");	
	//static private final Pattern LINE_PATTERN = Pattern.compile("(\\p{InCJKUnifiedIdeographs}+)\\s+(\\p{InCJKUnifiedIdeographs}+)\\s+\\[(.*)\\]\\s+/(.*)/");	
	
	static public List<Entry> lookup(InputStream cedictStream,
									 String simplifiedRegex,
									 String traditionalRegex,
									 String pinyinRegex,
									 String definitionRegex)
									 throws PatternSyntaxException,
									 		IOException {
		
		Pattern simplifiedPattern = null;
		Pattern traditionalPattern = null;
		Pattern pinyinPattern = null;
		Pattern definitionPattern = null;
		
		if(null != simplifiedRegex) {
			try {
				simplifiedPattern = Pattern.compile(simplifiedRegex);
			} catch(PatternSyntaxException pse) {
				throw new PatternSyntaxException("simplifiedRegex invalid!", pse.getPattern(), pse.getIndex());
			}
		}
		
		if(null != traditionalRegex) {
			try {
				traditionalPattern = Pattern.compile(traditionalRegex);
			} catch(PatternSyntaxException pse) {
				throw new PatternSyntaxException("traditionalRegex invalid!", pse.getPattern(), pse.getIndex());
			}
		}
		
		if(null != pinyinRegex) {
			try {
				pinyinPattern = Pattern.compile(pinyinRegex);
			} catch(PatternSyntaxException pse) {
				throw new PatternSyntaxException("pinyinRegex invalid!", pse.getPattern(), pse.getIndex());
			}
		}
		
		if(null != definitionRegex) {
			try {
				definitionPattern = Pattern.compile(definitionRegex);
			} catch(PatternSyntaxException pse) {
				throw new PatternSyntaxException("definitionRegex invalid!", pse.getPattern(), pse.getIndex());
			}
		}
		
		List<Entry> matches = new ArrayList<Entry>();
		String line;
		BufferedReader reader = new BufferedReader(new InputStreamReader(cedictStream, Charset.forName("UTF-8")));
		for(int lineNum = 1; null != (line = reader.readLine()); lineNum++) {
			if(line.startsWith("#")) {
				continue;
			}
			
			try {
				Entry match = matchNextLine(line, simplifiedPattern, traditionalPattern, pinyinPattern, definitionPattern);
				if(null != match) {
					matches.add(match);
				}
			} catch(IllegalArgumentException iae) {
				System.err.println("Couldn't parse line " + lineNum + ": " + line);
			}
		}
		
		return matches;
	}
	
	static private Entry matchNextLine(String line,
									Pattern simplifiedPattern,
									Pattern traditionalPattern,
									Pattern pinyinPattern,
									Pattern definitionPattern) {
		
		Matcher lineMatcher = LINE_PATTERN.matcher(line);
		if(!lineMatcher.matches()) {
			throw new IllegalArgumentException();
		}
		
		String simplified = lineMatcher.group(1);
		if(null != simplifiedPattern) {
			Matcher simplifiedMatcher = simplifiedPattern.matcher(simplified);
			if(!simplifiedMatcher.matches()) {
				return null;
			}
		}
		
		String traditional = lineMatcher.group(2);
		if(null != traditionalPattern) {
			Matcher traditionalMatcher = traditionalPattern.matcher(traditional);
			if(!traditionalMatcher.matches()) {
				return null;
			}
		}
		
		String pinyinStr = lineMatcher.group(3);
		if(null != pinyinPattern) {
			Matcher pinyinMatcher = pinyinPattern.matcher(pinyinStr);
			if(!pinyinMatcher.matches()) {
				return null;
			}
		}
		
		String definitionStr = lineMatcher.group(4);
		if(null != definitionPattern) {
			boolean matched = true;
			String[] definitions = definitionStr.split("/");
			for(String definition : definitions) {
				Matcher definitionMatcher = definitionPattern.matcher(definition);
				if(!definitionMatcher.matches()) {
					matched = false;
					break;
				}
			}
			
			if(!matched) {
				return null;
			}
		}
		
		String[] pinyinUnits = pinyinStr.split("\\s+");
		List<String> pinyin = new ArrayList<String>(pinyinUnits.length);
		for(String pinyinUnit : pinyinUnits) {
			pinyin.add(pinyinUnit);
		}
		
		String[] definitionUnits = definitionStr.split("/");
		List<String> definitions = new ArrayList<String>(definitionUnits.length);
		for(String definition : definitionUnits) {
			definitions.add(definition);
		}
		
		return new Entry(simplified, traditional, pinyin, definitions);
	}
	
	static public class Entry {
		private String simplified;
		private String traditional;
		
		private List<String> pinyin;
		private List<String> definitions;
		
		Entry(String simplified, String traditional, List<String> pinyin, List<String> definitions) {
			this.simplified = simplified;
			this.traditional = traditional;
		
			this.pinyin = Collections.unmodifiableList(pinyin);
			this.definitions = Collections.unmodifiableList(definitions);
		}
		
		public String toString() {
			StringBuilder sbuf = new StringBuilder();
			sbuf.append(this.simplified).append(" ").append(this.traditional).append(" [");
			
			for(int i = 0;; i++) {
				sbuf.append(this.pinyin.get(i));
			
				if(i < this.pinyin.size() - 1) {
					sbuf.append(" ");
				} else {
					break;
				}
			}
			sbuf.append("] /");
			
			for(int i = 0;; i++) {
				sbuf.append(this.definitions.get(i));
				
				if(i < this.definitions.size() - 1) {
					sbuf.append("/");
				} else {
					break;
				}
			}
			sbuf.append("/");
			
			return sbuf.toString();
		}
	}
	
	static public void main(String[] args) throws Exception {
		InputStream stream = CEDICTStreamDictionary.class.getResourceAsStream("cedict_ts.u8");
		
		String simplifiedRegex = null;
		String traditionalRegex = null;
		String pinyinRegex = null;
		String definitionRegex = ".*talk.*";
		
		List<Entry> matches = CEDICTStreamDictionary.lookup(stream, simplifiedRegex, traditionalRegex, pinyinRegex, definitionRegex);
		for(Entry match : matches) {
			System.out.println(match);
		}
	}
}
