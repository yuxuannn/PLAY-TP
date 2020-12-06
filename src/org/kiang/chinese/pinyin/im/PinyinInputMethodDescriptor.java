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

import java.awt.AWTException;
import java.awt.Image;
import java.awt.im.spi.InputMethod;
import java.awt.im.spi.InputMethodDescriptor;
import java.util.Locale;

import org.kiang.chinese.pinyin.PinyinUnit;
import org.kiang.im.term.TermInputMethod;
import org.kiang.io.URLInputStreamSource;


/**
 * InputMethodDescriptor for the PinyinInputMethod.
 * 
 * @author Jordan Kiang
 */
public class PinyinInputMethodDescriptor implements InputMethodDescriptor {

	static private final Locale[] AVAILABLE_LOCALES = new Locale[] {
		Locale.TRADITIONAL_CHINESE,
		Locale.SIMPLIFIED_CHINESE,
		Locale.CHINESE
	};
	
	/**
	 * @see java.awt.im.spi.InputMethodDescriptor#createInputMethod()
	 */
	public InputMethod createInputMethod() throws Exception {
		//PinyinIMEDictionary dictionary = new PinyinIMETreeDictionary(PinyinIMETreeTextSerializer.readTree(PinyinIMEDictionary.class.getResourceAsStream("dict.txt")));
		PinyinIMEDictionary dictionary = new PinyinIMEStreamDictionary(new URLInputStreamSource("/org/kiang/chinese/pinyin/im/dict.dat"));
		PinyinInputTermSource termSource = new PinyinInputTermSource(dictionary);
		TermInputMethod<PinyinUnit> inputMethod = new TermInputMethod<PinyinUnit>(termSource);
				
		return inputMethod;
	}

	/**
	 * @see java.awt.im.spi.InputMethodDescriptor#getAvailableLocales()
	 */
	public Locale[] getAvailableLocales() throws AWTException {
		return AVAILABLE_LOCALES;
	}

	/**
	 * @see java.awt.im.spi.InputMethodDescriptor#getInputMethodDisplayName(java.util.Locale, java.util.Locale)
	 */
	public String getInputMethodDisplayName(Locale inputLocale, Locale displayLanguage) {
		return "Pinyin";
	}

	/**
	 * @see java.awt.im.spi.InputMethodDescriptor#getInputMethodIcon(java.util.Locale)
	 */
	public Image getInputMethodIcon(Locale inputLocale) {
		return null;
	}

	/**
	 * @see java.awt.im.spi.InputMethodDescriptor#hasDynamicLocaleList()
	 */
	public boolean hasDynamicLocaleList() {
		return false;
	}	
}
