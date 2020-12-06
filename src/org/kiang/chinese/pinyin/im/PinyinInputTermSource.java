/* Copyright (c) 2007 Jordan Kiang
 * jordan-at-kiang.org
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.kiang.chinese.pinyin.im;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.kiang.chinese.pinyin.PinyinSyllable;
import org.kiang.chinese.pinyin.PinyinUnit;
import org.kiang.im.term.DefaultTermInputMethodControl;
import org.kiang.im.term.InputTerm;
import org.kiang.im.term.InputTermSource;
import org.kiang.im.term.InputTermUnit;


/**
 * An InputTermSource that works with PinyinUnits.
 * Serves as a InputTermSource for PinyinInputMethod.
 * 
 * @author Jordan Kiang
 */
public class PinyinInputTermSource implements InputTermSource<PinyinUnit> {

	private PinyinIMEDictionary dictionary;
	private PinyinInputMethodControl control = new PinyinInputMethodControl();
	
	// partial syallable Strings that can be used to validate
	// raw input.  i.e. if "zhong" is a syllable, then
	// "z", "zh", "zho", "zhon", and "zhong" all are in the Set.
	private Set<String> syllablePartials;
	
	/**
	 * Instantiate a new PinyinInputTermSource backed by the given
	 * dictionary and using the given control Object.
	 * 
	 * @param dictionary
	 */
	public PinyinInputTermSource(PinyinIMEDictionary dictionary) {
		if(null == dictionary) {
			throw new NullPointerException("dictionary cannot be null!");
		}
		
		this.dictionary = dictionary;
		this.syllablePartials = this.initPartials();
	}
	
	/**
	 * Initialize a Set of partials from valid Pinyin syllables.
	 * @return Set of partials
	 */
	private Set<String> initPartials() {
		Set<String> partials = new HashSet<String>();
		
		// add each substring starting at the beginning
		// of each syllable.
		PinyinSyllable[] syllables = PinyinSyllable.values();
		for(PinyinSyllable syllable : syllables) {
			String syllableStr = syllable.name();
			for(int i = 1; i <= syllableStr.length(); i++) {
				
				// convert to lower case since the input
				// comes in lower case
				partials.add(syllableStr.substring(0, i).toLowerCase());
			}
		}
		
		return partials;
	}
	
	/**
	 * Generates a PinyinUnit from the raw input,
	 * or null if the raw String isn't parseable
	 * to a unit.
	 * 
	 * @see org.kiang.im.term.InputTermSource#getInputKey(java.lang.String)
	 */
	public PinyinUnit getInputKey(String raw) {
		PinyinUnit key = null;
		try {
			key = PinyinUnit.parseValue(raw);
		} catch(IllegalArgumentException iae) {
			// not exceptional, just means
			// the input didn't amount to Pinyin,
			// just return null
		}
		
		return key;
	}

	/**
	 * Checks whether the given raw input is valid.
	 * 
	 * @see org.kiang.im.term.InputTermSource#isPartialInputKey(java.lang.String)
	 */
	public boolean isPartialInputKey(String raw) {
		// if the raw input is contained in the partials, then it's valid.
		return this.syllablePartials.contains(raw);
	}

	/**
	 * @see org.kiang.im.term.InputTermSource#convertRawCharacter(char)
	 */
	public String convertRawCharacter(char characterInput) {
		// deal only with lower case pinyin
		return Character.toString(characterInput).toLowerCase();
	}

	/**
	 * @see org.kiang.im.term.InputTermSource#lookupTerms(java.util.List)
	 */
	public List<InputTerm<PinyinUnit>> lookupTerms(List<PinyinUnit> pinyinKeys) {
		// don't anticipate with one character... too cluttered
		// with too many potential words returned.
		boolean anticipate = pinyinKeys.size() > 1;
		List<PinyinIMEEntry> entries = this.dictionary.lookup(pinyinKeys, anticipate);

		final boolean simplified = this.control.getCharacterMode();
		if(anticipate) {
			// if we anticipated longer words, sort so that the shorter
			// words are first, with the frequencies as tiebreakers between
			// words of the same length.  if there was no anticipation,
			// then the entries are already sufficiently sorted.
			
			Collections.sort(entries, new Comparator<PinyinIMEEntry>() {
				public int compare(PinyinIMEEntry o1, PinyinIMEEntry o2) {
					String word1 = simplified ? o1.getSimplified() : o1.getTraditional();
					String word2 = simplified ? o2.getSimplified() : o2.getTraditional();
					
					// first compare lengths, shorter first
					int compareTo = word1.length() - word2.length();
					if(compareTo == 0) {
						// then compare frequencies, higher frequencies first
						compareTo = o2.getFrequency() - o1.getFrequency();
					}
					
					return compareTo;
				}
			});
		}
		
		List<InputTerm<PinyinUnit>> terms = new ArrayList<InputTerm<PinyinUnit>>(entries.size());
		
		// keep track of words that we've already added so that
		// we can exclude words that start with this word to
		// avoid repetition.
		Set<String> seenWords = new HashSet<String>();
		for(PinyinIMEEntry entry : entries) {
			// obtain the appropriate representation of the word
			// depending on the current character mode.
			String word = simplified ?
					entry.getSimplified() :
					entry.getTraditional();
			
			int inputSize = pinyinKeys.size();
			List<InputTermUnit<PinyinUnit>> termUnits = new ArrayList<InputTermUnit<PinyinUnit>>();
			for(int i = 0; i < inputSize; i++) {
				PinyinUnit pinyinKey = pinyinKeys.get(i);
				
				// each word should have at least as many chars as PinyinUnit keys.
				// if for some reason it doesn't, we use a zero-length String
				// for the unit display String.
				String keyStr = i < word.length() ? Character.toString(word.charAt(i)) : "";
				
				InputTermUnit<PinyinUnit> unit = new InputTermUnit<PinyinUnit>(pinyinKey, keyStr);
				termUnits.add(unit);
			}
			
			// any characters beyond those matched to input
			// are entered as anticipated text on the term.
			String anticipatedSuffix = word.substring(inputSize);
			
			InputTerm<PinyinUnit> term = new InputTerm<PinyinUnit>(termUnits, anticipatedSuffix);
			
			// check the words that are already going
			// to be returned and don't return this
			// word if it starts with a previous word
			// to avoid some repetition.  i.e. if
			// we're returning AB and ABC is next,
			// then we don't return ABC.
			String termString = term.toAnticipatedString();
			boolean seen = false;
			for(String seenWord : seenWords) {
				if(termString.startsWith(seenWord)) {
					seen = true;
					break;
				}
			}
			
			if(!seen) {
				// haven't seen a word starting
				// with the given word, so we
				// can return this term.
				seenWords.add(termString);
				terms.add(term);
			}
		}
		
		return terms;
	}
	
	/**
	 * @see org.kiang.im.term.InputTermSource#shouldPassThrough(char)
	 */
	public boolean shouldPassThrough(char characterInput) {
		// no pinyin syllable starts with any of the below.
		// don't accept them while the InputMethod is on.
		return 'i' != characterInput &&
				'v' != characterInput &&
				'u' != characterInput;
	}
	
	/**
	 * @return control ooject
	 */
	public PinyinInputMethodControl getControlObject() {
		return this.control;
	}
	
	/**
	 * @see org.kiang.im.term.TermInputMethod#setLocale(java.util.Locale)
	 */
	public boolean setLocale(Locale locale) {
		// we store the Locale setting (simplified or traditional)
		// on the control Object.  this allows programatic
		// toggling of the mode through the control independent
		// of having to adjust the Locale through the framework.
		
		if(Locale.SIMPLIFIED_CHINESE.equals(locale)) {
			this.control.setCharacterMode(true);
			return true;
			
		} else if(Locale.TRADITIONAL_CHINESE.equals(locale)) {
			this.control.setCharacterMode(false);
			return true;
			
		} else if(Locale.CHINESE.equals(locale)) {
			// just stick with whatever mode they're already in
			// (no sense in getting political about what "CHINESE" means...
			// but return true to indicate that the mode was accepted.
			return true;
		}
		
		// this InputMethod doesn't accept the given mode
		return false;
	}
	
	/**
	 * @see java.awt.im.spi.InputMethod#getLocale()
	 */
	public Locale getLocale() {
		// locale is determined by their character mode
		return this.control.getCharacterMode() ?
				Locale.SIMPLIFIED_CHINESE :
				Locale.TRADITIONAL_CHINESE;
	}
	
	/**
	 * A control object, with toggleability of character mode.
	 * Gives access to change the character mode without having
	 * to set the Locale through the InputContext, which is not
	 * normally publicly accessible.
	 */
	public class PinyinInputMethodControl extends DefaultTermInputMethodControl {
		// default to simplified
		private boolean characterMode = true;

		/**
		 * @return true for simplified false for traditional
		 */
		public boolean getCharacterMode() {
			return this.characterMode;
		}
		
		/**
		 * Set the character mode.
		 * @param simplified true for simplified false for traditional
		 */
		public void setCharacterMode(boolean simplified) {
			this.characterMode = simplified;
		}
	}
}
