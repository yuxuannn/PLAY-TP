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

package org.kiang.chinese.pinyin.im.app.applet;

import java.applet.Applet;

import org.kiang.chinese.pinyin.im.app.PinyinInputConfig;


/**
 * A PinyinInputConfig that reads from Applet configuration.
 * @author Jordan Kiang
 */
class PinyinInputAppletConfig extends PinyinInputConfig {

	/**
	 * Applet parameter that specifies where to download a custom
	 * TrueType font from.  Should be a URL.  Depending on if it's
	 * a signed applet, the URL may have to be served
	 * from the same host as the applet itself.
	 */
	static final String FONT_DOWNLOAD_URL_PARAM = "fontDownloadURL";
	
	/**
	 * If the font download file is zipped, then this parameter
	 * specifies the path within the zip file of the Font
	 * within the zip file.
	 */
	static final String FONT_ZIP_PATH_PARAM = "fontZipPath";
	
	private Applet applet;
	
	/** @see org.kiang.chinese.pinyin.im.app.PinyinInputConfig#getString(java.lang.String) */
	@Override
	protected String getString(String key) {
		return this.applet.getParameter(key);
	}
	
	/**
	 * @param applet the associated applet
	 */
	PinyinInputAppletConfig(Applet applet) {
		if(null == applet) {
			throw new NullPointerException("applet cannot be null!");
		}
		
		this.applet = applet;
	}
	
	/**
	 * @return url of a zipped file containing a chinese font
	 */
	public String getFontDownloadURL() {
		return this.getString(FONT_DOWNLOAD_URL_PARAM);
	}
	
	/**
	 * @return the path within the (zipped) file at the download url of the font files
	 */
	public String getFontZipPath() {
		return this.getString(FONT_ZIP_PATH_PARAM);
	}
}
