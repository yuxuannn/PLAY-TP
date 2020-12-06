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

import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.im.InputContext;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;

import org.kiang.chinese.font.ChineseFontChooserFactory;
import org.kiang.chinese.pinyin.im.PinyinInputTermSource.PinyinInputMethodControl;
import org.kiang.chinese.pinyin.im.swing.PinyinInputComponent;
import org.kiang.swing.JFontChooser;

/**
 * Some common reusable UI builder methods.
 * @author jkiang
 */
public class PinyinInputUIBuilder {
		
	/**
     * @return the standard edit menu (cut, copy, paste).
     */
    static public JMenu buildEditMenu() {
        JMenu editMenu = new JMenu(Messages.Key.EDIT_MENU.getText());
        editMenu.setMnemonic(KeyEvent.VK_E);
        
        JMenuItem cutItem = new JMenuItem(new DefaultEditorKit.CutAction());
        cutItem.setText(Messages.Key.CUT_MENU_ITEM.getText());
        cutItem.setMnemonic(KeyEvent.VK_T);
        editMenu.add(cutItem);
        
        JMenuItem copyItem = new JMenuItem(new DefaultEditorKit.CopyAction());
        copyItem.setText(Messages.Key.COPY_MENU_ITEM.getText());
        copyItem.setMnemonic(KeyEvent.VK_C);
        editMenu.add(copyItem);
        
        JMenuItem pasteItem = new JMenuItem(new DefaultEditorKit.PasteAction());
        pasteItem.setText(Messages.Key.PASTE_MENU_ITEM.getText());
        pasteItem.setMnemonic(KeyEvent.VK_P);
        editMenu.add(pasteItem);
        
        return editMenu;
    }
    
    /**
     * @param pinyinComponent the PinyinComponent the font chooser is associated with
     * @param fontSource an optional source of Fonts to use instead of the default environment Fonts
     * @return build the format menu for selecting character mode and choosing a Font
     */
    static public JMenu buildFormatMenu(final PinyinInputComponent pinyinComponent, FontSource fontSource) {
    	JMenu formatMenu = new JMenu(Messages.Key.FORMAT_MENU.getText());
    	formatMenu.setMnemonic(KeyEvent.VK_O);
    	
    	// need to get check if simplified or traditional should be checked.
    	final InputContext inputContext = pinyinComponent.getInputContext();
    	boolean compositionEnabled = inputContext.isCompositionEnabled();
		
    	final PinyinInputMethodControl control = (PinyinInputMethodControl)inputContext.getInputMethodControlObject();
    	boolean simplified = control.getCharacterMode();
    	
    	final JRadioButtonMenuItem simplifiedItem = new JRadioButtonMenuItem(Messages.Key.SIMPLIFIED_MENU_ITEM.getText(), simplified && compositionEnabled);
    	simplifiedItem.setMnemonic(KeyEvent.VK_S);
    	
    	final JRadioButtonMenuItem traditionalItem = new JRadioButtonMenuItem(Messages.Key.TRADITIONAL_MENU_ITEM.getText(), !simplified && compositionEnabled);
    	traditionalItem.setMnemonic(KeyEvent.VK_T);
    	
    	final JRadioButtonMenuItem offItem = new JRadioButtonMenuItem(Messages.Key.OFF_MENU_ITEM.getText(), !compositionEnabled);
    	offItem.setMnemonic(KeyEvent.VK_O);
    	
    	// an ActionListener to fire when when the character mode is toggled.
    	ActionListener characterModeListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				PinyinInputMethodControl control = pinyinComponent.getInputContext().getInputMethodControlObject();
				
				Object source = e.getSource();
				if(offItem == source) {
					// off radio button selected.
					// turn off the input method.
					inputContext.setCompositionEnabled(false);
					
				} else {
					// simplified or traditional mode selected.
					
					// may or may not be off, just set it on
					// in either case.
					inputContext.setCompositionEnabled(true);
					
					boolean simplified = simplifiedItem == source;
					control.setCharacterMode(simplified);
				}
			}
    	};
    	
    	ButtonGroup characterModeGroup = new ButtonGroup();
    	
    	formatMenu.add(simplifiedItem);
    	characterModeGroup.add(simplifiedItem);
    	simplifiedItem.addActionListener(characterModeListener);
    	
    	formatMenu.add(traditionalItem);
    	characterModeGroup.add(traditionalItem);
    	traditionalItem.addActionListener(characterModeListener);
    	
    	formatMenu.add(offItem);
    	characterModeGroup.add(offItem);
    	offItem.addActionListener(characterModeListener);
    	
    	formatMenu.addSeparator();
    	
    	JMenuItem fontChooserItem = buildFontChooserMenuItem(pinyinComponent, fontSource);
    	formatMenu.add(fontChooserItem);
    	
    	return formatMenu;
    }
    
    /**
     * @param pinyinComponent the PinyinComponent the font chooser is associated with
     * @param bundledFont an optional bundled Font to include in the chooser not among the System fonts
     * @return a menu item for popping up a font chooser dialog
     */
    static private JMenuItem buildFontChooserMenuItem(final PinyinInputComponent pinyinComponent, final FontSource fontSource) {
    	// a PinyinInputComponent must be a subclass of Component.
    	final JTextComponent componentCast = (JTextComponent)pinyinComponent;
    	
    	JMenuItem fontMenuItem = new JMenuItem(Messages.Key.CHOOSE_FONT_MENU_ITEM.getText());
    	fontMenuItem.setMnemonic(KeyEvent.VK_F);
    	
    	fontMenuItem.addActionListener(new ActionListener() {
    		/** @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent) */
			public void actionPerformed(ActionEvent e) {
				Font currentFont = componentCast.getFont();
				int fontStyle = currentFont.getStyle();
				int fontSize = currentFont.getSize();
				
				Font[] fonts;
				if(null == fontSource) {
					// no FontSource was specified, use environment Fonts.
					fonts = JFontChooser.getSystemFonts(fontStyle, fontSize);
					
				} else {
					// get Fonts from the FontSource
					fonts = fontSource.getFonts();
				}
				
				Font font = ChineseFontChooserFactory.showDialog(componentCast, currentFont, fonts, JFontChooser.DEFAULT_SIZE_OPTIONS, null);
				if(null != font) {
					componentCast.setFont(font);
				}
			}
    	});
    	
    	return fontMenuItem;
    }
    
    /**
	 * Build an "About" menu that shows the author
	 * @param owner the owner Frame
	 * @return about menu component
	 */
	static public JComponent buildAboutMenu(final Component owner) {
		final JDialog aboutDialog = buildAboutDialog(owner);
		
		// use a JLabel for the menu item.
		// can't figure out how to make a JMenu work
		// if it's just supposed to act like a button
		// (i.e. just pop up a dialog, no menu items).
		// label seems to work fine.  tried using a 
		// JButton, but it looks wrong.
		JLabel aboutMenu = new JLabel(Messages.Key.ABOUT.getText());
		aboutMenu.addMouseListener(new MouseAdapter() {
			/**
			 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
			 */
			@Override
			public void mouseClicked(MouseEvent e) {
				aboutDialog.setLocationRelativeTo(owner);
				aboutDialog.setVisible(true);
			}
		});
		
		return aboutMenu;
	}
	
	/**
	 * Build an About dialog.
	 * A simple dialog that shows the author
	 * put my name and email and an OK button to close.
	 * 
	 * @param owner the owner component for the dialog
	 * @return the dialog
	 */
	static private JDialog buildAboutDialog(Component owner) {
		Frame rootFrame = JOptionPane.getFrameForComponent(owner);
		final JDialog aboutDialog = new JDialog(rootFrame, Messages.Key.ABOUT.getText(), true);
		Box box = Box.createVerticalBox();
		
		JLabel nameLabel = new JLabel("Jordan Kiang");
		nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		// obfuscate my the email String a bit so it isn't
		// spidered if the source happens to be posted somewhere.
		String emailString = "jordan" + "@" + "kiang" + ".org";
		JLabel emailLabel = new JLabel(emailString);
		emailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		JButton okButton = new JButton(Messages.Key.OK.getText());
		okButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		okButton.addActionListener(new ActionListener() {
			/**
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			public void actionPerformed(ActionEvent e) {
				// close the dialog
				aboutDialog.setVisible(false);
			}
			
		});
		
		box.add(nameLabel);
		box.add(emailLabel);
		box.add(okButton);
		
		aboutDialog.add(box);
		aboutDialog.setResizable(false);
		aboutDialog.pack();
		
		return aboutDialog;
	}
	
	static public interface FontSource {
		
		public Font[] getFonts();
		
	}
}
