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

package org.kiang.chinese.pinyin.im.app.main;

import java.awt.Font;
import java.io.InputStream;
import java.util.Properties;

import org.kiang.chinese.pinyin.im.app.PinyinInputConfig;


/**
 * For use with the main app.
 * Config that reads from a java.util.Properties
 * Adds support for using a Font loaded from a resource.
 * 
 * @author Jordan Kiang
 */
class PinyinInputAppConfig extends PinyinInputConfig {
	
	private Properties props = new Properties();
	
	/**
	 * @param props key/value pairs
	 */
	PinyinInputAppConfig(Properties props) {
		if(null == props) {
			throw new NullPointerException("props cannot be null");
		}
		
		this.props = props;
	}

	/**
	 * Tries to first interpret the font param as a font
	 * bundled font resource path, falling back on
	 * treating it as a comma delimited list of preferred font
	 * family names.
	 * 
	 * @see org.kiang.chinese.pinyin.im.app.PinyinInputConfig#getFont()
	 */
	@Override
	public Font getFont(Font currentFont) {
		Font font = this.getBundledFont();
		if(null == font) {
			// font param wasn't interpretable
			// as a font resource.  instead
			// we try to interpet it as a comma
			// delimited String of preferred
			// font families.
			font = super.getFont(currentFont);
		}
		
		return font;
	}
	
	/**
	 * Try to interpet the font string parameter as the resource
	 * path to a bundled font.
	 * @return the bundled font, null if not bundled font available
	 */
	private Font getBundledFont() {
		// see if we can interpret the font param as the
		// resource path of a bundled font resource.
		// if so we use that Font.
		String fontStr = this.getString(FONT_PARAM);
		if(null != fontStr) {
			InputStream fontStream = PinyinInputAppConfig.class.getResourceAsStream(fontStr);
			if(null != fontStream) {
				try {
					Font font = Font.createFont(Font.TRUETYPE_FONT, fontStream);
					fontStream.close();
					
					// adjust the bundled font 
					int fontSize = this.getFontSize();
					return font.deriveFont((float)fontSize);
					
				} catch(Exception e) {
					// bundled font not available,
					// not necessarily wrong.
				}
			}
		}
		
		// font not available
		return null;
	}
	
	/**
	 * @see org.kiang.chinese.pinyin.im.app.PinyinInputConfig#getString(java.lang.String)
	 */
	@Override
	protected String getString(String key) {
		return this.props.getProperty(key);
	}
}
