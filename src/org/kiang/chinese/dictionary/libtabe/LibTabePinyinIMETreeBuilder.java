package org.kiang.chinese.dictionary.libtabe;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kiang.chinese.pinyin.PinyinUnit;
import org.kiang.chinese.pinyin.im.PinyinIMETraditionalEntry;
import org.kiang.chinese.pinyin.tree.PinyinTree;


public class LibTabePinyinIMETreeBuilder {

	static public PinyinTree<PinyinIMETraditionalEntry> buildTree(InputStream libTabeStream, int minimumCharacterFrequency, int minimumWordFrequency) throws IOException {
		List<LibTabeParser.Entry> parserEntries = LibTabeParser.parse(libTabeStream, minimumCharacterFrequency, minimumWordFrequency);
		
		final PinyinTree<PinyinIMETraditionalEntry> tree = new PinyinTree<PinyinIMETraditionalEntry>();
		for(LibTabeParser.Entry parserEntry : parserEntries) {
			PinyinIMETraditionalEntry entry = new PinyinIMETraditionalEntry(parserEntry.getTraditional(), parserEntry.getFrequency());
			
			List<List<PinyinUnit>> pinyinAlts = parserEntry.getPinyin();
			int row = 0;
			List<PinyinUnit> soFar = new ArrayList<PinyinUnit>();
			addEachPinyinAlternative(pinyinAlts, entry, row, soFar, tree);
		}
		
		return tree;
	}
	
    static public void addEachPinyinAlternative(List<List<PinyinUnit>> pinyinAlts, PinyinIMETraditionalEntry entry, int row, List<PinyinUnit> soFar, PinyinTree<PinyinIMETraditionalEntry> tree) {
        if(row >= pinyinAlts.size()) {
        	tree.add(soFar, entry);
        
        } else {
            List<PinyinUnit> rowAlts = pinyinAlts.get(row);
                    
            for(PinyinUnit alt : rowAlts) {
                soFar.add(alt);
                addEachPinyinAlternative(pinyinAlts, entry, row + 1, soFar, tree);
                soFar.remove(soFar.size() - 1);
            }
        }
    }

	
	static public void main(String[] args) throws IOException {
		InputStream libTabeStream = LibTabePinyinIMETreeBuilder.class.getResourceAsStream("tsi.src");
		
		long start = System.currentTimeMillis();
		PinyinTree<PinyinIMETraditionalEntry> tree = LibTabePinyinIMETreeBuilder.buildTree(libTabeStream, 1, 100);
		
		List<PinyinUnit> testPinyin = new ArrayList<PinyinUnit>();
		//testPinyin.add(PinyinUnit.parseValue("wo3"));
		testPinyin.add(PinyinUnit.parseValue("zhong0"));
		testPinyin.add(PinyinUnit.parseValue("guo2"));
		//testPinyin.add(PinyinUnit.parseValue("hua"));
		
		Set<PinyinIMETraditionalEntry> setValues = tree.get(testPinyin, true);
		
		List<PinyinIMETraditionalEntry> sortedValues = new ArrayList<PinyinIMETraditionalEntry>(setValues);
		Collections.sort(sortedValues);
		
		for(PinyinIMETraditionalEntry entry : sortedValues) {
			System.out.println(entry.getTraditional());
		}
		long end = System.currentTimeMillis();
		System.out.println("elapsed: " + (end - start));
	}
}
