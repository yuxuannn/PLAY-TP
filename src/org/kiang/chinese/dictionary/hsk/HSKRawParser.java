package org.kiang.chinese.dictionary.hsk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.kiang.chinese.pinyin.PinyinSyllable;



public class HSKRawParser {

	static private Set<String> pinyinSyllables = initPinyinPartials();
	
	static public void main(String[] args) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(HSKRawParser.class.getResourceAsStream("hsk.u8"), Charset.forName("UTF-8")));
		Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/home/jkiang/eclipse-workspaces/ime/ime2/src/java/kiang/chinese/dictionary/hsk/hsk_parsed.txt"), Charset.forName("UTF-8"))); 
		
		Appendable writeTo = writer;
		String line;
		while(null != (line = reader.readLine())) {
			String[] split = line.split(",");
			if(split.length >= 3) {
				int frequency = Integer.parseInt(split[0].trim());
				String word = split[1].trim();
				String pinyin = split[2].trim();
			
				List<String> pinyinUnits = parsePinyinUnits(pinyin.toUpperCase());
				
				writeTo.append(word);
				writeTo.append(", ");
				
				Iterator<String> unitIter = pinyinUnits.iterator();
				String unit = unitIter.next();
				writeTo.append(unit);
				
				while(unitIter.hasNext()) {
					writeTo.append(" " + unitIter.next());
				}
				
				writeTo.append(", ");
				writeTo.append("" + frequency);
				writeTo.append("\n");
			}
		}
		
		reader.close();
		writer.close();
	}
	
	static private Set<String> initPinyinPartials() {
		PinyinSyllable[] syllables = PinyinSyllable.values();
		Set<String> partials = new HashSet<String>();
		
		for(PinyinSyllable syllable : syllables) {
			String name = syllable.name();
			
			for(int i = 1; i <= name.length(); i++) {
				partials.add(name.substring(0, i));
			}
		}
		
		return partials;
	}
	
	static private List<String> parsePinyinUnits(String pinyin) {
		//pinyin = pinyin.replace('V', 'Ü');
		pinyin = pinyin.replace('V', 'V');
		
		String unit = "";
		
		List<String> units = new ArrayList<String>();
		for(int i = 0; i < pinyin.length(); i++) {
			char c = pinyin.charAt(i);
			
			if(pinyinSyllables.contains(unit + c)) {
				unit = unit + c;
			} else if(c >= '1' && c <= '4') {
				units.add(unit + c);
				unit = "";
			} else if(unit.length() > 0) {
				units.add(unit + '5');
				unit = "" + c;
			}
		}
		
		if(unit.length() > 0) {
			char lastChar = unit.charAt(unit.length() - 1);
			if(lastChar < '1' || lastChar > '4') {
				units.add(unit + "5");
			}
		}
		
		return units;
	}
}
