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

import java.applet.AppletContext;
import java.applet.AppletStub;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.JTextComponent;

import org.kiang.chinese.pinyin.im.PinyinInputTermSource.PinyinInputMethodControl;
import org.kiang.chinese.pinyin.im.app.PinyinInputConfig;
import org.kiang.chinese.pinyin.im.swing.PinyinInputTextArea;
import org.kiang.chinese.pinyin.im.swing.PinyinInputTextField;



/**
 * An applet for inputting Chinese characters via Pinyin on
 * a web page.  It contains a single text field or text area,
 * depending on the applet parameters.  It tries to mimic
 * the appearance of the unstyled corresponding html components
 * in most browsers.
 *  
 * @author Jordan Kiang
 */
public class PinyinTextInputApplet extends JApplet {
	
	private JTextComponent textComponent;
	
	/**
	 * @see java.applet.Applet#init()
	 */
	@Override
	public void init() {
		// configuration is derived from Applet params
		PinyinTextInputAppletConfig config = new PinyinTextInputAppletConfig(this);
		
		// component to add to the applet container
		Component component;
		
		// if the multi-line, then we use a text area.
		// if single line, then use a text field.
		boolean multiLine = config.getLineMode();
		if(multiLine) {
			// multi line so use a text area
			PinyinInputTextArea textArea = new PinyinInputTextArea();
			
			boolean wrap = config.getWrapMode();
			textArea.setLineWrap(wrap);
			textArea.setWrapStyleWord(wrap);
			
			// try to mimic the scroll function of an html text area,
			// so wrap the text area component in a scroll pane.
			JScrollPane scrollPane = new JScrollPane(textArea);
			scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			
			this.textComponent = textArea;
			component = scrollPane;
			
		} else {
			// single line, so use a field
			this.textComponent = new PinyinInputTextField();
			component = this.textComponent;
		}
		
		// mimics the appearance of an unstyled html input
		// for other styles, could tweak the border.
		this.textComponent.setBorder(BorderFactory.createLoweredBevelBorder());
		
		// setup the font
		Font currentFont = this.textComponent.getFont();
		this.textComponent.setFont(config.getFont(currentFont));
		
		// various control parameters are set onto the InputMethod control
		// object, so we get it from the text component, and set the values
		PinyinInputMethodControl control = (PinyinInputMethodControl)this.textComponent.getInputContext().getInputMethodControlObject();

		control.setCharacterMode(config.getCharacterMode());
		control.setUsingRawWindow(config.getRawMode());
		control.setChooserOrientation(config.getChooserOrientation());
		
		this.getContentPane().add(component);
	}
	
	/**
	 * @see java.awt.Container#setFont(java.awt.Font)
	 */
	@Override
	public void setFont(Font font) {
		super.setFont(font);
		
		// additionally set the font onto the child text component
		if(null != this.textComponent) {
			this.textComponent.setFont(font);
		}
	}
	
	/**
	 * @see java.applet.Applet#getParameterInfo()
	 */
	@Override
	public String[][] getParameterInfo() {
		return new String[][] {
				{PinyinInputConfig.CHARACTER_MODE_PARAM, "simplified or traditional", "whether to use simplified or traditional characters"},
				{PinyinInputConfig.RAW_MODE_PARAM, "window or inline", "whether raw input should be displayed in a window below-the-spot or inline"},
				{PinyinInputConfig.CHOOSER_ORIENTATION_PARAM, "vertical or horizontal", "whether alternatives displayed in a window should be oriented vertically or horizontally"},
				{PinyinInputConfig.FONT_PARAM, "font names", "comma delimited list of font names to try to use in order, if not found, uses the default"},
				{PinyinInputConfig.FONT_SIZE_PARAM, "int", "size of the font to use"},
				{PinyinTextInputAppletConfig.LINE_MODE_PARAM, "multi or single", "whether to accept multi lines of input (a text area) or just one (a field)"},
				{PinyinTextInputAppletConfig.WRAP_PARAM, "boolean", "true for line wrapping when in multi line mode"}
		};		
	}
	
	public String getText() {
		return this.textComponent.getText();
	}
	
	/**
	 * Main method simulates an applet.
	 * A normal applet doesn't use this method.
	 * @param args
	 */
	static public void main(String[] args) {
		// run the applet as an application to try it out
		
		// some params we can use in the applet stub
		final Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(PinyinInputConfig.CHARACTER_MODE_PARAM, "traditional");
		parameters.put(PinyinInputConfig.RAW_MODE_PARAM, "window");
		parameters.put(PinyinInputConfig.CHOOSER_ORIENTATION_PARAM, "vertical");
		parameters.put(PinyinInputConfig.FONT_SIZE_PARAM, "32");
		parameters.put(PinyinTextInputAppletConfig.LINE_MODE_PARAM, "multi");
		parameters.put(PinyinTextInputAppletConfig.WRAP_PARAM, "false");
		
		Dimension size = new Dimension(400, 150);
		
		// generate a fake stub so we can run this applet from main
		AppletStub appletStub = new AppletStub() {
			/** @see java.applet.AppletStub#appletResize(int, int) */
			public void appletResize(int width, int height) {}
			/** @see java.applet.AppletStub#getAppletContext() */
			public AppletContext getAppletContext() { return null; }
			/** @see java.applet.AppletStub#getCodeBase() */
			public URL getCodeBase() { return null; }
			/** @see java.applet.AppletStub#getDocumentBase() */
			public URL getDocumentBase() { return null; }
			/** @see java.applet.AppletStub#isActive() */
			public boolean isActive() { return true; }
			
			/**
			 * @see java.applet.AppletStub#getParameter(java.lang.String)
			 */
			public String getParameter(String name) {
				return parameters.get(name);
			}
		};
		
		PinyinTextInputApplet applet = new PinyinTextInputApplet();
		applet.setStub(appletStub);
		
		JFrame frame = new JFrame();
		frame.getContentPane().add(applet);
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		applet.init();
		applet.setPreferredSize(size);
		
		frame.pack();
		frame.setVisible(true);
	}
}
