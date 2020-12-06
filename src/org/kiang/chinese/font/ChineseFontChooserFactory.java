/*
 * Copyright (C) 2005 Jordan Kiang
 * jordan-at-kiang.org
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.kiang.chinese.font;

import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.kiang.i18n.AggregateResourceBundle;
import org.kiang.i18n.EnumBundle;
import org.kiang.swing.JFontChooser;
import org.kiang.swing.JFontChooser.FontFilter;


/**
 * Defines some methods for building instances of a JFontChooser that can filter for
 * those Fonts that support Simplified and Traditional chinese character sets.
 * 
 * This is a factory and does not extend JFontChooser since there really is nothing to extend.
 */
public class ChineseFontChooserFactory {
	
	/**
	 * Localization keys specific to a Chinese Font chooser.
	 * Localization ResourceBundles should include these keys,
	 * in addition to those in JFontChooser.i18nKey.
	 */
	static public enum i18nKey {
		/** Overrides JFontChooser's English preview text */
		PREVIEW_TEXT("\u6c49  \u6f22"),
		/** Simplified character mode */
		SIMPLIFIED("Simplified"),
		/** Traditional character mode */
		TRADITIONAL("Traditional");
		
		private String defaultText;
		private i18nKey(String defaultText) {
			this.defaultText = defaultText;
		}
		
		/**
		 * Overrides to provide default text.
		 * @see java.lang.Enum#toString()
		 */
		@Override
		public String toString() {
			return this.defaultText;
		}
	}
	
    /**
     * Uses a SimplifiedFontFilter and a TraditionalFontFilter when invoking other methods.
     * @param owner the parent component
     * @return Font the selected font
     * 
     * @see JFontChooser#showDialog(Component)
     * @see ChineseFontChooserFactory#showDialog(Component, Font, Font[], int[], String)
     */
    static public Font showDialog(Component owner) {
        // default options       
        return showDialog(owner, null, null, null, null);
    }
    
    /**
     * Uses a SimplifiedFontFilter and a TraditionalFontFilter when invoking other methods.
     * Can pass null for any parameters to use defaults (except owner component).
     * 
     * @param owner the parent component
     * @param initialFont the font that should be initially selected
     * @param fontOptions the fonts to show in the chooser
     * @param sizeOptions the sizes that should be available in the chooser
     * @param messageBundle to localize
     * @return Font the selected font
     * 
     * @see JFontChooser#showDialog(Component, Font, Font[], int[], FontFilter[], String)
     */
    static public Font showDialog(Component owner,
            					  Font initialFont,
            					  Font[] fontOptions,
            					  int[] sizeOptions,
            					  ResourceBundle messageBundle) {
    	if(null == owner) {
    		throw new NullPointerException("owner cannot be null!");
    	}
    	if(null == messageBundle) {
    		// need to send in Chinese chooser bundle
    		messageBundle = getDefaultBundle();
    	}
        
        FontFilter[] chineseFilters = getChineseFilters(initialFont, messageBundle);
        return JFontChooser.showDialog(owner, initialFont, fontOptions, sizeOptions, chineseFilters, messageBundle);
    }
    
    /**
     * Gets an instance of a JFontChooser using a SimplifiedFontFilter and a TraditionalFontFilter.
     * 
     * @see JFontChooser#JFontChooser(Font, Font[], int[], FontFilter[], String)
     */
    static JFontChooser getInstance(Font initialFont, Font[] fontOptions, int[] sizeOptions, ResourceBundle messageBundle) {
        FontFilter[] chineseFilters = getChineseFilters(initialFont, messageBundle);
        
        return new JFontChooser(initialFont, fontOptions, sizeOptions, chineseFilters, messageBundle);
    }
    
    /**
     * @param initialFont the initialFont to use
     * @return a prepared set of filters for filter by Simplified and Traditional characters
     */
    static private FontFilter[] getChineseFilters(Font initialFont, ResourceBundle bundle) {
        return new FontFilter[] {new SimplifiedFontFilter(initialFont, bundle), new TraditionalFontFilter(initialFont, bundle)};
    }
    
    static private ResourceBundle getDefaultBundle() {
        List<ResourceBundle> bundles = new ArrayList<ResourceBundle>(2);
        bundles.add(new EnumBundle(ChineseFontChooserFactory.i18nKey.class));
        bundles.add(new EnumBundle(JFontChooser.i18nKey.class));
        return new AggregateResourceBundle(bundles);
    }
    
    static private String getMessage(ResourceBundle bundle, i18nKey key) {
    	String message = bundle.getString(key.name());
    	if(null == message) {
    		// if using a custom bundle that doesn't define a particular
    		// message, then fall back on the default text.
    		message = key.toString();
    	}
    	
    	return message;
    }
    
    /**
     * Filters fonts for those that support some common Simplified characters.
     */
    static private class SimplifiedFontFilter implements FontFilter {
        private Font initialFont;
        private String displayName;
        
        private SimplifiedFontFilter(Font initialFont, ResourceBundle bundle) {
            this.initialFont = initialFont;
            this.displayName = getMessage(bundle, i18nKey.SIMPLIFIED);
        }
        
        public String getDisplayName() {
            return this.displayName;
        }
        
        public boolean isDefaultOn() {
            return this.shouldInclude(this.initialFont);
        }
        
        public boolean shouldInclude(Font font) {
            return ChineseFontFinder.isSimplifiedFont(font);
        }
    }
    
    /**
     * Filters fonts for those that support some common Traditinal characters.
     */
    static private class TraditionalFontFilter implements FontFilter {
        private Font initialFont;
        private String displayName;
        
        private TraditionalFontFilter(Font initialFont, ResourceBundle bundle) {
            this.initialFont = initialFont;
            this.displayName = getMessage(bundle, i18nKey.TRADITIONAL);
        }
        
        public String getDisplayName() {
        	return this.displayName;
        }
        
        public boolean isDefaultOn() {
            return this.shouldInclude(this.initialFont);
        }
        
        public boolean shouldInclude(Font font) {
            return ChineseFontFinder.isTraditionalFont(font);
        }
        
    }
    
}
