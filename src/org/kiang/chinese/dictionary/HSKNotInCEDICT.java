package org.kiang.chinese.dictionary;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kiang.chinese.dictionary.cedict.CEDICTParser;
import org.kiang.chinese.dictionary.hsk.HSKParser;


public class HSKNotInCEDICT {

	static public void main(String[] args) throws IOException {
		InputStream cedictStream = CEDICTParser.class.getResourceAsStream("cedict_ts.u8");
		InputStream hskStream = HSKParser.class.getResourceAsStream("hsk_parsed.txt");
		
		List<CEDICTParser.Entry> cedictEntries = CEDICTParser.parse(cedictStream);
		Map<String, CEDICTParser.Entry> cedictMap = new HashMap<String, CEDICTParser.Entry>(cedictEntries.size());
		for(CEDICTParser.Entry cedictEntry : cedictEntries) {
			cedictMap.put(cedictEntry.getSimplified(), cedictEntry);
		}
		
		int count = 0;
		List<HSKParser.Entry> hskEntries = HSKParser.parse(hskStream);
		for(HSKParser.Entry hskEntry : hskEntries) {
			if(!cedictMap.containsKey(hskEntry.getWord())) {
				System.out.println(hskEntry.getWord());
				count++;
			}
		}
		
		System.out.println(count);
		
		cedictStream.close();
		hskStream.close();
	}
}
