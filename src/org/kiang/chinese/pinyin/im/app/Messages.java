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

import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.kiang.i18n.EnumBundle;


/**
 * A programmatic ResourceBundle that encapsulates
 * both tokens representing each message as well
 * as default text for each of those messages.
 * 
 * It will get its text from whatever other
 * localized ResourceBundles are most appropriate,
 * falling back on its hard-coded default text
 * when more a more appropriate bundle does
 * not exist.
 * 
 * To localize, add a localized bundle to the same
 * location with the appropriate localized bundle
 * name, using the Key enum names as your resource
 * keys.
 * 
 * @author Jordan Kiang
 */
public class Messages extends EnumBundle {

	/**
	 * Messages loaded from the enum.
	 */
	public Messages() {
		super(Key.class);
	}

	/**
	 * A Key for each message that exists.
	 */
	static public enum Key {
		/** Title bar on the JFrame */
		TITLE("Pinyin Input"),
		
		/** OK confirmation */
		OK("OK"),
		/** Cancel */
		CANCEL("Cancel"),
		
		/** "File" menu */
		FILE_MENU("File"),
		/** "Open" menu item */
		OPEN_MENU_ITEM("Open"),
		/** "Save" menu item */
		SAVE_MENU_ITEM("Save"),
		/** "Save As" menu item */
		SAVE_AS_MENU_ITEM("Save As"),
		
		/** "Edit" menu */
		EDIT_MENU("Edit"),
		/** "Cut" menu item */
		CUT_MENU_ITEM("Cut"),
		/** "Copy" menu item */
		COPY_MENU_ITEM("Copy"),
		/** "Paste" menu item */ 
		PASTE_MENU_ITEM("Paste"),
		
		/** "Format" menu */
		FORMAT_MENU("Format"),
		/** Choose simplified character type menu item */
		SIMPLIFIED_MENU_ITEM("Simplified"),
		/** Choose traditional character type menu item */
		TRADITIONAL_MENU_ITEM("Traditional"),
		/** Turn off Pinyin input menu item */
		OFF_MENU_ITEM("Off"),
		/** Choose font menu item */
		CHOOSE_FONT_MENU_ITEM("Choose Font"),
		
		/** "About" */
		ABOUT("About"),
		
		/** Save changes dialog text */
		SAVE_CHANGES("Save changes?"),
		/** OK to Overwrite existing file when saving dialog text */
		OK_TO_OVERWRITE("OK to overwrite?"),
		/** IO error writing file dialog text */
		ERROR_WRITING_FILE("Error writing File!"),
		/** IO error reading file dialog text */
		ERROR_READING_FILE("Error reading File!"),
		
		/** Text on the download Font menu item */
		FONT_DOWNLOAD("Download Font"),
		/** Text on a Font download Dialog during download */
		FONT_DOWNLOADING("Downloading Font"),
		/** Displayed after Font downloaded, arg is the font name */
		FONT_DOWNLOADED("Downloaded font {0}."),
		/** Displayed after Font download is aborted */
		FONT_DOWNLOAD_ABORTED("Font download aborted."),
		/** Displayed when there is an error downloading/parsing a Font */
		FONT_DOWNLOAD_ERROR("Encountered an error during Font download."),
		/** Displayed when no Chinese Font is detected on init */
		FONT_DOWNLOAD_PROMPT("No Chinese Font was detected on your system.  Would you like to download one?");
		
		String defaultText;
		
		Key(String defaultText) {
			this.defaultText = defaultText;
		}
		
		/**
		 * @see java.lang.Enum#toString()
		 */
		@Override
		public String toString() {
			return this.defaultText;
		}
		
		/**
		 * @param args message format args
		 * @return the localized text for the key
		 */
		public String getText(Object... args) {
			ResourceBundle bundle = ResourceBundle.getBundle(Messages.class.getName());
			String message = bundle.getString(this.name());
			return MessageFormat.format(message, args);
		}
	}
}
