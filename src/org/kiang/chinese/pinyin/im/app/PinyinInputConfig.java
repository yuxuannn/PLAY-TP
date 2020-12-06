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

package org.kiang.chinese.pinyin.im.app;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base class for a set of configuration properties that
 * an be used in Pinyin input components.  Parses String properties into
 * usable values.
 * 
 * @author Jordan Kiang
 */
public abstract class PinyinInputConfig {

	/** key for the character mode, value should be "simplified" or "traditional" */
	static public final String CHARACTER_MODE_PARAM			= "characterMode";
	/** value for traditional mode (defaults to simplified if anything else) */ 
	static public final String TRADITIONAL_MODE				= "traditional";
	
	/** key for the raw mode, should be "window" or "inline */
	static public final String RAW_MODE_PARAM				= "rawMode";
	/** value for inline mode (defualts to window if anything else */
	static public final String INLINE_MODE					= "inline";
	
	/** key for the chooser orientation, should be "vertical" or "horizontal" */
	static public final String CHOOSER_ORIENTATION_PARAM	= "chooserOrientation";
	/** value for horizontal mode (defaults to vertical if anything else */
	static public final String HORIZONTAL_MODE 				= "horizontal";
	
	/** key for the font parameter */
	static public final String FONT_PARAM 					= "font";
	/** key for the font size parameter */
	static public final String FONT_SIZE_PARAM				= "fontSize";
	/** default for font size if nothing specified */
	static public final int DEFAULT_FONT_SIZE				= 24;
	
	/**
	 * @return true for simplified mode, false for traditional
	 */
	public boolean getCharacterMode() {
		String characterModeStr = this.getString(CHARACTER_MODE_PARAM);
		
		// if either there is no parameter defined, or the parameter
		// is anything other than "traditional" case insensitive, then
		// we use simplified.
		boolean simplified = null == characterModeStr ||
								!TRADITIONAL_MODE.equals(characterModeStr.toLowerCase());
		return simplified;
	}

	/**
	 * @return true to show raw input in a separate window below-the-spot, false for on-the-spot
	 */
	public boolean getRawMode() {
		String rawModeStr = this.getString(RAW_MODE_PARAM);
		
		// if either there is no raw mode parameter, or the
		// parameter is anything other than "inline" case insensitive,
		// then use a below-the-spot window for raw mode.  otherwise inline.
		boolean rawWindowMode = null == rawModeStr ||
								!INLINE_MODE.equals(rawModeStr.toLowerCase());
		return rawWindowMode;
	}

	
	/**
	 * @return true to show alternatives vertically, false for horizontal
	 */
	public boolean getChooserOrientation() {
		String chooserOrientationStr = this.getString(CHOOSER_ORIENTATION_PARAM);
		
		// if either there is no parameter, or the
		// parameter is anything other than "horizontal" case insensitive,
		// then defaults to vertical.
		boolean vertical = null == chooserOrientationStr ||
							!HORIZONTAL_MODE.equals(chooserOrientationStr.toLowerCase());
		return vertical;
	}

	/**
	 * @param currentFont the currently used Font, if one isn't specified
	 * @return preferred Font to use, null if not specified
	 */
	public Font getFont(Font currentFont) {
		int fontSize = this.getFontSize();
		int fontStyle = currentFont.getStyle();
		
		Font font = null;
		String fontNameStr = this.getString(FONT_PARAM);
		if(null != fontNameStr) {
			// collect the fonts available so we can check them
			// against the parameter value.
			Map<String, String> availableFontNames = new HashMap<String, String>();
			for(String availableFontName : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {
				// key by lower case so we can accept parameters case insensitively
				availableFontNames.put(availableFontName.toLowerCase(), availableFontName);
			}
			
			// split the comma separated values,
			// then check those against the values from the parameters.
			String[] requestedFontNames = fontNameStr.split(",");
			for(String requestedFontName : requestedFontNames) {
				String availableFontName = availableFontNames.get(requestedFontName.toLowerCase());
				if(null != availableFontName) {
					// found a usable font
					font = new Font(availableFontName, fontStyle, fontSize);
					break;
				}
			}
		}
		
		if(null == font) {
			// either a Font wasn't specified, or a specified
			// Font wasn't found, so we derive the Font from
			// the current Font using the size
			font = currentFont.deriveFont(fontStyle, fontSize);
		}
		
		return font;
	}
	
	/**
	 * @return parse the font size
	 */
	protected int getFontSize() {
		String fontSizeStr = this.getString(FONT_SIZE_PARAM);
		
		int fontSize = DEFAULT_FONT_SIZE;
		try {
			fontSize = Integer.parseInt(fontSizeStr);
		} catch(NumberFormatException nfe) {
			// it'll just stay as the default
		}
		
		return fontSize;
	}

	/**
	 * Abstract method for obtaining a String configuration parameter.
	 * @param key
	 * @return value
	 */
	protected abstract String getString(String key);
}
