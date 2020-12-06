package org.kiang.chinese.dictionary.hsk;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.kiang.chinese.pinyin.PinyinUnit;
import org.kiang.chinese.pinyin.im.PinyinIMETraditionalEntry;
import org.kiang.chinese.pinyin.im.PinyinIMETreeByteStreamSerializer;
import org.kiang.chinese.pinyin.tree.PinyinTree;


/**
 * Build a PinyinTree<IMEEntry> from a data file of HSK data.
 * 
 * @author Jordan Kiang
 *
 */
public class HSKPinyinIMETreeBuilder {

	static public PinyinTree<PinyinIMETraditionalEntry> buildTree(InputStream hskStream) throws IOException {
		PinyinTree<PinyinIMETraditionalEntry> tree = new PinyinTree<PinyinIMETraditionalEntry>();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(hskStream, Charset.forName("UTF-8")));
		String line;
		for(int i = 1; null != (line = reader.readLine()); i++) {
			try {
				parseLine(line, tree);
			} catch(IllegalArgumentException iae) {
				System.err.println("error parsing line " + i + ": " + line);
			}
		}
		
		return tree;
	}
	
	static private void parseLine(String line, PinyinTree<PinyinIMETraditionalEntry> addTo) {
		String[] split = line.split(",");
		if(split.length != 3) {
			throw new IllegalArgumentException("Unable to parse line!");
		}
		
		String word = split[0].trim();
		
		String pinyinStr = split[1].trim();
		String frequencyStr = split[2].trim();
	
		List<PinyinUnit> pinyin = parsePinyin(pinyinStr);
		int frequency = Integer.parseInt(frequencyStr);
		
		PinyinIMETraditionalEntry entry = new PinyinIMETraditionalEntry(word, frequency);
		addTo.add(pinyin, entry);	
	}
	
	static List<PinyinUnit> parsePinyin(String pinyin) {
		String[] unitStrs = pinyin.split(" ");
		List<PinyinUnit> units = new ArrayList<PinyinUnit>(unitStrs.length);
		for(String unitStr : unitStrs) {
			units.add(PinyinUnit.parseValue(unitStr));	
		}
		
		return units;
	}
	
	static public void main(String[] args) throws IOException {
		InputStream hskStream = HSKPinyinIMETreeBuilder.class.getResourceAsStream("hsk_parsed.txt");
		PinyinTree<PinyinIMETraditionalEntry> tree = buildTree(hskStream);
		
		//FileOutputStream fileOut = new FileOutputStream("/home/jkiang/eclipse-workspaces/ime/ime2/src/java/kiang/chinese/dictionary/hsk/hsk_tree.txt");
		//PinyinIMETreeTextSerializer.writeTree(tree, fileOut);
		
		FileOutputStream fileOut = new FileOutputStream("/home/jkiang/eclipse-workspaces/ime/ime2/src/java/kiang/chinese/dictionary/hsk/hsk_tree.bytes");
		PinyinIMETreeByteStreamSerializer.writeTree(tree, fileOut);
		
		
		fileOut.close();
	}
}
