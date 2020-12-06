package org.kiang.chinese.dictionary;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.kiang.chinese.dictionary.cedict.CEDICTParser;
import org.kiang.chinese.dictionary.hsk.HSKParser;
import org.kiang.chinese.dictionary.libtabe.LibTabeParser;
import org.kiang.chinese.dictionary.libtabe.LibTabePinyinIMETreeBuilder;
import org.kiang.chinese.pinyin.PinyinUnit;
import org.kiang.chinese.pinyin.im.PinyinIMETraditionalEntry;
import org.kiang.chinese.pinyin.im.PinyinIMETreeByteStreamSerializer;
import org.kiang.chinese.pinyin.im.PinyinIMETreeTextSerializer;
import org.kiang.chinese.pinyin.tree.PinyinTree;


public class DictionaryCollater {
	
	// some hard coded values to start us off
	static private Map<String, Integer> buildMasterFrequenciesBase() {
		Map<String, Integer> base = new HashMap<String, Integer>();
		base.put("是", Integer.valueOf(10));
		base.put("在", Integer.valueOf(10));
		base.put("的", Integer.valueOf(10));
		base.put("上海", Integer.valueOf(8));
		base.put("北京", Integer.valueOf(8));
		base.put("根", Integer.valueOf(8));
		base.put("南", Integer.valueOf(8));
		base.put("紅", Integer.valueOf(7));
		base.put("吶", Integer.valueOf(7));
		
		
		return base;
	}
	
	static public PinyinTree<PinyinIMETraditionalEntry> collate2(InputStream libTabeStream,
												InputStream hskStream,
												InputStream cedictStream) throws IOException {

		PinyinTree<PinyinIMETraditionalEntry> tree = new PinyinTree<PinyinIMETraditionalEntry>();
		
		Map<String, Integer> masterFrequencies = buildMasterFrequenciesBase();
		
		Map<String, Integer> hskFrequencies = new HashMap<String, Integer>();
		for(HSKParser.Entry hskEntry : HSKParser.parse(hskStream)) {
			hskFrequencies.put(hskEntry.getWord(), Integer.valueOf(10 - hskEntry.getLevel()));
		}
		
		int[] hskToLibTabeFrequencies = new int[10];
		for(int i = 0; i < hskToLibTabeFrequencies.length; i++) {
			hskToLibTabeFrequencies[i] = Integer.MAX_VALUE;
		}
		
		Set<LibTabeParser.Entry> unaddedLibTabeEntries = new HashSet<LibTabeParser.Entry>();
		List<LibTabeParser.Entry> libTabeEntries = LibTabeParser.parse(libTabeStream, Integer.MIN_VALUE, Integer.MIN_VALUE);
		Map<String, Integer> libTabeFrequencies = new HashMap<String, Integer>(libTabeEntries.size());
		for(LibTabeParser.Entry libTabeEntry : libTabeEntries) {
			Integer frequency = masterFrequencies.get(libTabeEntry.getTraditional());
			if(null != frequency) {
				int libTabeFrequency = hskToLibTabeFrequencies[frequency.intValue() - 1];
				hskToLibTabeFrequencies[frequency.intValue() - 1] = Math.min(hskToLibTabeFrequencies[frequency.intValue() - 1], libTabeFrequency);
			
				PinyinIMETraditionalEntry imeEntry = new PinyinIMETraditionalEntry(libTabeEntry.getTraditional(), frequency.intValue());
				LibTabePinyinIMETreeBuilder.addEachPinyinAlternative(libTabeEntry.getPinyin(), imeEntry, 0, new ArrayList<PinyinUnit>(0), tree);

			} else {
				unaddedLibTabeEntries.add(libTabeEntry);
			}
			
			libTabeFrequencies.put(libTabeEntry.getTraditional(), Integer.valueOf(libTabeEntry.getFrequency()));
		}
		
		Iterator<LibTabeParser.Entry> unaddedLibTabeEntryIter = unaddedLibTabeEntries.iterator();
		while(unaddedLibTabeEntryIter.hasNext()) {
			LibTabeParser.Entry libTabeEntry = unaddedLibTabeEntryIter.next();
			
			for(int i = 10; i > 0; i--) {
				if(hskToLibTabeFrequencies[i - 1] < libTabeEntry.getFrequency()) {
					PinyinIMETraditionalEntry imeEntry = new PinyinIMETraditionalEntry(libTabeEntry.getTraditional(), i);
					masterFrequencies.put(libTabeEntry.getTraditional(), Integer.valueOf(i));
					LibTabePinyinIMETreeBuilder.addEachPinyinAlternative(libTabeEntry.getPinyin(), imeEntry, 0, new ArrayList<PinyinUnit>(0), tree);
					
					unaddedLibTabeEntryIter.remove();
					libTabeEntry = null;
					break;
				}
			}
		}
		
		class CEDICTEntryWrapper implements Comparable<CEDICTEntryWrapper> {
			CEDICTParser.Entry entry;
			int frequency;
			
			public CEDICTEntryWrapper(CEDICTParser.Entry entry, int frequency) {
				this.entry = entry;
				this.frequency = frequency;
			}
			
			public int compareTo(CEDICTEntryWrapper o) {
				return o.frequency - this.frequency;
			}
		}
		
		List<CEDICTEntryWrapper> unaddedCedictEntries = new ArrayList<CEDICTEntryWrapper>();
		List<CEDICTParser.Entry> cedictEntries = CEDICTParser.parse(cedictStream);
		for(CEDICTParser.Entry cedictEntry : cedictEntries) {			
			
			Integer frequency = masterFrequencies.get(cedictEntry.getTraditional());
			if(null != frequency) {
				PinyinIMETraditionalEntry imeEntry = new PinyinIMETraditionalEntry(cedictEntry.getTraditional(), frequency.intValue());
				tree.add(cedictEntry.getPinyin(), imeEntry);
			
			} else if(null != (frequency = hskFrequencies.get(cedictEntry.getSimplified()))) {
				masterFrequencies.put(cedictEntry.getTraditional(), frequency);
				
				PinyinIMETraditionalEntry imeEntry = new PinyinIMETraditionalEntry(cedictEntry.getTraditional(), frequency.intValue());
				tree.add(cedictEntry.getPinyin(), imeEntry);
			
			} else {
				Integer libTabeFrequency = libTabeFrequencies.get(cedictEntry.getTraditional());
				if(null != libTabeFrequency) {
					CEDICTEntryWrapper wrapper = new CEDICTEntryWrapper(cedictEntry, libTabeFrequency.intValue());
					unaddedCedictEntries.add(wrapper);
				}
			}
		}
		
		Collections.sort(unaddedCedictEntries);
		// roughly evenly distribute entries found
		// in CEDICT and libTabe evenly according
		// to their LibTabe frequencies.
		int quarters = unaddedCedictEntries.size() / 4;
		for(int i = 0; i < 4; i++) {
			int fromIndex = quarters * i;
			int toIndex = i < 3 ? fromIndex + quarters : unaddedCedictEntries.size();
			
			List<CEDICTEntryWrapper> subList = unaddedCedictEntries.subList(fromIndex, toIndex);
			for(CEDICTEntryWrapper cedictEntry : subList) {
				String word = cedictEntry.entry.getTraditional();
				int frequency =  5 - i;
				
				PinyinIMETraditionalEntry imeEntry = new PinyinIMETraditionalEntry(word, frequency);
				tree.add(cedictEntry.entry.getPinyin(), imeEntry);
				masterFrequencies.put(word, Integer.valueOf(frequency));
			}
		}

		unaddedLibTabeEntryIter = unaddedLibTabeEntries.iterator();
		while(unaddedLibTabeEntryIter.hasNext()) {
			LibTabeParser.Entry libTabeEntry = unaddedLibTabeEntryIter.next();
			if(libTabeEntry.getTraditional().length() == 1) {
				Integer frequency = masterFrequencies.get(libTabeEntry.getTraditional());
				if(null == frequency) {
					frequency = Integer.valueOf(1);
				}
				
				PinyinIMETraditionalEntry imeEntry = new PinyinIMETraditionalEntry(libTabeEntry.getTraditional(), frequency.intValue());
				LibTabePinyinIMETreeBuilder.addEachPinyinAlternative(libTabeEntry.getPinyin(), imeEntry, 0, new ArrayList<PinyinUnit>(0), tree);
			}
		}
			
		/*
		int[] counts = new int[10];
		Set<PinyinIMETraditionalEntry> entries = tree.get(new ArrayList<PinyinUnit>(0), true);
		for(PinyinIMETraditionalEntry entry : entries) {
			System.out.println(entry.getTraditional() + " : " + entry.getFrequency());
			
			counts[entry.getFrequency() - 1]++;
		}
		
		for(int i = 10; i > 0; i--) {
			System.out.println("" + i + " : " + counts[i - 1]);
		}
		System.out.println(entries.size());
		*/
		
		return tree;
	}
		
		
	
	static public PinyinTree<PinyinIMETraditionalEntry> collate(InputStream libTabeStream,
											InputStream hskStream,
											InputStream cedictStream) throws IOException {
		
		PinyinTree<PinyinIMETraditionalEntry> tree = new PinyinTree<PinyinIMETraditionalEntry>();
		
		Map<String, Integer> hskFrequenceies = new HashMap<String, Integer>();
		Map<String, Integer> masterFrequencies = new HashMap<String, Integer>();
		Map<String, Integer> libTabeFrequencies = new HashMap<String, Integer>();
		
		List<HSKParser.Entry> hskEntries = HSKParser.parse(hskStream);
		for(HSKParser.Entry hskEntry : hskEntries) {
			String word = hskEntry.getWord();
			int level = hskEntry.getLevel();
			int frequency = 11 - level;
			
			PinyinIMETraditionalEntry imeEntry = new PinyinIMETraditionalEntry(word, frequency);
			tree.add(hskEntry.getPinyin(), imeEntry);
			
			hskFrequenceies.put(hskEntry.getWord(), Integer.valueOf(frequency));	
		}
		
		List<LibTabeParser.Entry> libTabeEntries = LibTabeParser.parse(libTabeStream, Integer.MIN_VALUE, Integer.MIN_VALUE);
		for(LibTabeParser.Entry entry : libTabeEntries) {
			libTabeFrequencies.put(entry.getTraditional(), Integer.valueOf(entry.getFrequency()));
		}
		
		class CEDICTEntryWrapper implements Comparable<CEDICTEntryWrapper> {
			CEDICTParser.Entry entry;
			int frequency;
			
			public int compareTo(CEDICTEntryWrapper o) {
				return o.frequency - this.frequency;
			}
		}
		
		List<CEDICTEntryWrapper> cedictInLibTabeEntries = new ArrayList<CEDICTEntryWrapper>();
		List<CEDICTParser.Entry> cedictUnseenEntries = new ArrayList<CEDICTParser.Entry>();
		
		List<CEDICTParser.Entry> cedictEntries = CEDICTParser.parse(cedictStream);
		for(CEDICTParser.Entry entry : cedictEntries) {
			Integer frequency = masterFrequencies.get(entry.getTraditional());
			if(null != frequency) {
				// add possible pinyin alternatives
				PinyinIMETraditionalEntry imeEntry = new PinyinIMETraditionalEntry(entry.getTraditional(), frequency.intValue());
				tree.add(entry.getPinyin(), imeEntry);
				
			} else {
				// store CEDICT entries,
				// calculate frequencies in a moment
				frequency = libTabeFrequencies.get(entry.getTraditional());
				if(null != frequency) {
					CEDICTEntryWrapper wrapper = new CEDICTEntryWrapper();
					wrapper.entry = entry;
					wrapper.frequency = frequency.intValue();
					cedictInLibTabeEntries.add(wrapper);
				} else {
					cedictUnseenEntries.add(entry);
				}
			}
		}
		
		Collections.sort(cedictInLibTabeEntries);
		
		// roughly evenly distribute entries found
		// in CEDICT and libTabe evenly according
		// to their LibTabe frequencies.
		int quarters = cedictInLibTabeEntries.size() / 4;
		for(int i = 0; i < 4; i++) {
			int fromIndex = quarters * i;
			int toIndex = i < 3 ? fromIndex + quarters : cedictInLibTabeEntries.size();
			
			List<CEDICTEntryWrapper> subList = cedictInLibTabeEntries.subList(fromIndex, toIndex);
			for(CEDICTEntryWrapper cedictEntry : subList) {
				String word = cedictEntry.entry.getTraditional();
				int frequency =  5 - i;
				
				PinyinIMETraditionalEntry imeEntry = new PinyinIMETraditionalEntry(word, frequency);
				tree.add(cedictEntry.entry.getPinyin(), imeEntry);
				masterFrequencies.put(word, Integer.valueOf(frequency));
			}
		}
		
		for(CEDICTParser.Entry unseen : cedictUnseenEntries) {
			PinyinIMETraditionalEntry imeEntry = new PinyinIMETraditionalEntry(unseen.getTraditional(), 1);
			tree.add(unseen.getPinyin(), imeEntry);
		}
		
		for(LibTabeParser.Entry libTabeEntry : libTabeEntries) {
			String word = libTabeEntry.getTraditional();
			Integer frequency = null;
			if(word.length() == 1) {
				frequency = Integer.valueOf(1);
				
			} else if(null != (frequency = masterFrequencies.get(word))) {
				// in condition
			}
			
			if(null != frequency) {
				PinyinIMETraditionalEntry imeEntry = new PinyinIMETraditionalEntry(word, frequency.intValue());
				
				int row = 0;
				List<PinyinUnit> soFar = new ArrayList<PinyinUnit>();
				LibTabePinyinIMETreeBuilder.addEachPinyinAlternative(libTabeEntry.getPinyin(), imeEntry, row, soFar, tree);
			}
		}

		Map<Integer, Integer> counts = new TreeMap<Integer, Integer>();
		Set<PinyinIMETraditionalEntry> entries = tree.get(new ArrayList<PinyinUnit>(0), true);
		for(PinyinIMETraditionalEntry entry : entries) {
			if(entry.getFrequency() == 10) {
				System.out.println(entry.getTraditional() + " : " + entry.getFrequency());
			}
			
			Integer frequency = Integer.valueOf(entry.getFrequency());
			Integer count = counts.get(frequency);
			if(null == count) {
				count = Integer.valueOf(1);
			} else {
				count = Integer.valueOf(count.intValue() + 1);
			}
			counts.put(frequency, count);
		}
		
		/*
		for(Map.Entry<Integer, Integer> count : counts.entrySet()) {
			System.out.println(count.getKey() + " : " + count.getValue());
		}
		
		System.out.println(entries.size());
		*/
		
		libTabeStream.close();
		hskStream.close();
		cedictStream.close();
		
		return tree;
	}
	
	static public void main(String[] args) throws IOException {
		InputStream libTabeStream = LibTabeParser.class.getResourceAsStream("tsi.src");
		InputStream hskStream = HSKParser.class.getResourceAsStream("hsk_parsed.txt");
		InputStream cedictStream = CEDICTParser.class.getResourceAsStream("cedict_ts.u8");
		
		PinyinTree<PinyinIMETraditionalEntry> tree = DictionaryCollater.collate2(libTabeStream, hskStream, cedictStream);
		
		FileOutputStream textOut = new FileOutputStream("/home/jkiang/eclipse-workspaces/ime/ime2/src/java/kiang/chinese/dictionary/dict.txt");
		PinyinIMETreeTextSerializer.writeTree(tree, textOut);
		textOut.close();
		
		FileOutputStream bytesOut =  new FileOutputStream("/home/jkiang/eclipse-workspaces/ime/ime2/src/java/kiang/chinese/dictionary/dict.dat");
		PinyinIMETreeByteStreamSerializer.writeTree(tree, bytesOut);
		bytesOut.close();
	}
}
