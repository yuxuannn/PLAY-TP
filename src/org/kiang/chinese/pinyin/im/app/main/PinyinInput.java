
package org.kiang.chinese.pinyin.im.app.main;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.im.InputContext;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Properties;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;

import org.kiang.chinese.pinyin.im.PinyinInputTermSource.PinyinInputMethodControl;
import org.kiang.chinese.pinyin.im.app.Messages;
import org.kiang.chinese.pinyin.im.app.PinyinInputConfig;
import org.kiang.chinese.pinyin.im.app.PinyinInputUIBuilder;
import org.kiang.chinese.pinyin.im.app.PinyinInputUIBuilder.FontSource;
import org.kiang.chinese.pinyin.im.swing.PinyinInputTextArea;
import org.kiang.swing.JFontChooser;

import CHARACTERTOIMAGE.utftoimage;


/**
 * A simple Java Swing text editor hard wired with a Pinyin input method.
 * Runnable through its main method.  Reads configuration properties through
 * a bundled resource if available, otherwise uses defaults.  Can read/write
 * files in a couple of character encodings.
 * 
 * @author Jordan Kiang
 */
public class PinyinInput extends JFrame {
	
	String transferchar,name;

	static String lessonname;
	
	// resource path of the configuration
	static private final String PROPERTIES_RESOURCE = "input.properties";
	
	static private final String[] SUPPORTED_CHARSETS = new String[] {
		// FileFilters get added to a JFileChooser in the order
		// they appear in the array.  seems like JFileChooser
		// will use the last as the selected no matter what,
		// so make the last the one that should be selected.
		"GB2312",
		"Big5",
		"UTF-8",
	};

	// the text area
	private PinyinInputTextArea textArea;
	
	// a Font bundled as a resource, if available
	private Font bundledFont;
	
	// save menu item enabled after save as or open.
	private JMenuItem saveMenuItem;
	// file to use, initialized after a save as or open
	private File saveFile;
	// character set of the saved file
	private Charset saveCharset;
	// whether or not the document has changed since the last save.
	// if so, then we need to confirm before clearing the contents.
	private boolean changedSinceSave;
	
	/**
	 * @param title title bar String
	 */
	public PinyinInput(String title, String lesson) {
		super(title);
		
		lessonname = lesson;
		
		// don't automatically close on close, instead check
		// open an exit confirmation dialog that asks them to save
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			/** @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent) */
			@Override
			public void windowClosing(WindowEvent e) {
				PinyinInput.this.exit();
			}
		});
		
		this.textArea = this.initConfig();
		
		this.setJMenuBar(this.buildMenuBar());
	
		// put the text area into a 
		JScrollPane scrollPane = new JScrollPane(this.textArea);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setPreferredSize(new Dimension(600, 300));
		
		this.getContentPane().add(scrollPane);
	}

	/**
	 * Initialize the text area using the resource configuration.
	 * @return the configured text area
	 */
	private PinyinInputTextArea initConfig() {
		PinyinInputTextArea textArea = this.buildTextArea();
		
		Properties props = new Properties();
		try {
			props.load(PinyinInputAppConfig.class.getResourceAsStream(PROPERTIES_RESOURCE));
		} catch(IOException ioe) {
			// couldn't read props, will just resort to defaults.
			// not necessarily wrong, since the properties are optional
		}
		
		// use the properties to initialize a configuration
		PinyinInputConfig config = new PinyinInputAppConfig(props);
		
		// obtain the InputContext from the text area, and use
		// its control object in the configuration.
		InputContext inputContext = textArea.getInputContext();
		PinyinInputMethodControl control = (PinyinInputMethodControl)inputContext.getInputMethodControlObject();
		control.setCharacterMode(config.getCharacterMode());
		control.setChooserOrientation(config.getChooserOrientation());
		control.setUsingRawWindow(config.getRawMode());
		

		Font currentFont = textArea.getFont();
		Font font = config.getFont(currentFont);
		// check if the font is one that exists on the system already.
		// if not, then we store it as an instance variable so we
		// can re-present it as an option later on the font chooser.
		boolean fontBundled = true;
		Font[] systemFonts = JFontChooser.getSystemFonts(font.getStyle(), font.getSize());
		for(Font systemFont : systemFonts) {
			if(font.getFamily().equals(systemFont.getFamily())) {
				fontBundled = false;
				break;
			}
		}
		if(fontBundled) {
			this.bundledFont = font;
		}
	
		textArea.setFont(font);
		control.setFont(font);
		
		return textArea;
	}
	
	/**
	 * @return build the JTextArea component in which text is composed
	 */
	private PinyinInputTextArea buildTextArea() {
		PinyinInputTextArea textArea = new PinyinInputTextArea();
		
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		
		// register a DocumentListener so that we can detect
		// when the text content changes so we know if we
		// need to show a save confirmation when exiting.
		textArea.getDocument().addDocumentListener(new DocumentListener() {
			/** @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent) */
			public void changedUpdate(DocumentEvent e) {
				PinyinInput.this.markChanged();
			}
			/** @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent) */
			public void insertUpdate(DocumentEvent e) {
				this.changedUpdate(e);
			}
			/** @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent) */
			public void removeUpdate(DocumentEvent e) {
				this.changedUpdate(e);
			}
		});	
		
		return textArea;
	}
	

	
	/**
	 * @return the File menu
	 */
	private JMenu buildFileMenu() {
		JMenu fileMenu = new JMenu(Messages.Key.FILE_MENU.getText());
		fileMenu.setMnemonic(KeyEvent.VK_F);
		
		JMenuItem openMenuItem = this.buildOpenMenuItem();
		JMenuItem saveMenuItem = this.buildSaveMenuItem();
		JMenuItem saveAsMenuItem = this.buildSaveAsMenuItem();
		JMenuItem transferMenuItem = this.buildTransferMenuItem();
		JMenuItem exitItem = this.buildExitMenuItem();
		
		fileMenu.add(openMenuItem);
		fileMenu.add(saveMenuItem);
		fileMenu.add(saveAsMenuItem);
		fileMenu.add(transferMenuItem);
		fileMenu.add(exitItem);
		
		// register the save item so we can
		// enable it later on
		this.saveMenuItem = saveMenuItem;
		
		return fileMenu;
	}
	
	/**
	 * @return the menu item for opening a file
	 */
	private JMenuItem buildOpenMenuItem() {
		final JMenuItem openItem = new JMenuItem(Messages.Key.OPEN_MENU_ITEM.getText());
		openItem.setMnemonic(KeyEvent.VK_O);
		
		openItem.addActionListener(new ActionListener() {
			/** @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent) */
			public void actionPerformed(ActionEvent e) {
				PinyinInput.this.open();
			}
		});
		
		return openItem;
	}
	
	/**
	 * Build a menu item for saving the current text contents.
	 * If file has been opened or already saved once, then
	 * the text is saved to that location.  Otherwise it
	 * runs as a save as.
	 * 
	 * @return the menu item for saving
	 */
	private JMenuItem buildSaveMenuItem() {
		final JMenuItem saveItem = new JMenuItem("Save");
		saveItem.setMnemonic(KeyEvent.VK_S);
		
		saveItem.addActionListener(new ActionListener() {
			/** @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent) */
			public void actionPerformed(ActionEvent e) {
				PinyinInput.this.save();
			}
		});
		
		// initially disabled until there has either
		// been an save or an open to establish
		// the file location.
		saveItem.setEnabled(false);
		
		return saveItem;
	}
	
	/**
	 * @return save as menu item
	 */
	private JMenuItem buildSaveAsMenuItem() {	
		final JMenuItem saveAsItem = new JMenuItem("Save As");
		saveAsItem.setMnemonic(KeyEvent.VK_A);
		
		saveAsItem.addActionListener(new ActionListener() {
			/** @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent) */
			public void actionPerformed(ActionEvent e) {
				PinyinInput.this.saveAs();
			}
		});
		
		return saveAsItem;
	}
	
	/**
	 * @return build the exit menu item
	 */
	private JMenuItem buildExitMenuItem() {
		final JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.setMnemonic(KeyEvent.VK_X);
		
		exitItem.addActionListener(new ActionListener() {
			/** @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent) */
			public void actionPerformed(ActionEvent e) {
				PinyinInput.this.exit();
			}
		});
		
		return exitItem;
	}
	
	private JMenuItem buildTransferMenuItem() {
		final JMenuItem transferItem = new JMenuItem("Transfer");
		transferItem.setMnemonic(KeyEvent.VK_T);
		
		final JFrame parent = new JFrame();
    	JButton enter = new JButton();
    	enter.setText("Enter");
		
		transferItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			try {
				name = JOptionPane.showInputDialog(parent,"Enter name of word (to be saved as image) :",null);
				PinyinInput.this.transfer(name);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		});
		return transferItem;
		}
	

	/**
	 * @param selectedFile the initially selected file, null if unspecified
	 * @param selectedCharset the character set of the initially selected file, null if unspecifed
	 * @return a JFileChooser with character set FileFilters
	 */
    private JFileChooser buildFileChooser(File selectedFile, Charset selectedCharset) {
    	JFileChooser fileChooser = new JFileChooser();
    	
    	// we only ever deal with individual files.
    	fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    	
    	// have to specify one of the FileFilters
		fileChooser.setAcceptAllFileFilterUsed(false);
		
		// for each of the CharacterSets, add a filter
		for(final String charset : SUPPORTED_CHARSETS) {
			if(null == selectedCharset || !charset.equals(selectedCharset.toString())) {
				// if there's a specified character set, don't add it now,
				// we'll add it after the loop to ensure it's selected.
				fileChooser.addChoosableFileFilter(this.buildFileFilter(charset));	
			}
		}
		
		// a selected charset.  we add it last to make sure it's selected.
		// seems like JFileChooser will always have the last added fitler selected.
		if(null != selectedCharset) {
			fileChooser.addChoosableFileFilter(this.buildFileFilter(selectedCharset.name()));
		}
		
		if(null != selectedFile) {
			fileChooser.setSelectedFile(selectedFile);
		}
		
		return fileChooser;
    }
    
    /**
     * Build a JFileChooser FileFilter for the given CharacterSet.s
     * @param charset the charset
     * @return the FileFilter
     */
    private FileFilter buildFileFilter(final String charset) {
    	return new FileFilter() {
    		/**
			 * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
			 */
			@Override
			public boolean accept(File f) {
				// accept all files,
				// we're just using the filter
				// to let them select the character
				// set encoding, not for filtering.
				return true;
			}

			/**
			 * @see javax.swing.filechooser.FileFilter#getDescription()
			 */
			@Override
			public String getDescription() {
				return charset;
			}
    	};
    }
	
	/**
	 * @return build the menu bar
	 */
	private JMenuBar buildMenuBar() {
		FontSource fontSource = new FontSource() {
			/** @see org.kiang.chinese.pinyin.im.app.PinyinInputUIBuilder.FontSource#getFonts() */
			public Font[] getFonts() {
				Font currentFont = PinyinInput.this.textArea.getFont();
				int fontStyle = currentFont.getStyle();
				int fontSize = currentFont.getSize();
				
				Font[] fonts = JFontChooser.getSystemFonts(fontStyle, fontSize);
				if(null != PinyinInput.this.bundledFont) {
					Font[] fonts2 = new Font[fonts.length + 1];
					System.arraycopy(fonts, 0, fonts2, 1, fonts.length);
					fonts2[0] = PinyinInput.this.bundledFont;
					fonts = fonts2;
				}
				
				return fonts;
			}
		};
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(this.buildFileMenu());
		menuBar.add(PinyinInputUIBuilder.buildEditMenu());
		menuBar.add(PinyinInputUIBuilder.buildFormatMenu(this.textArea, fontSource));
		
		// push the about menu over to the right
		menuBar.add(Box.createHorizontalGlue());
		menuBar.add(PinyinInputUIBuilder.buildAboutMenu(this));
			
		return menuBar;
	}
    
    /**
     * Helper method reads the given File into the text component
     * @param file
     * @param charset the file's character encoding
     */
    private boolean readFile(File file, Charset charset) {
    	try {
			Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));
			this.textArea.read(reader, null);
			
			this.setSaveFile(file, charset);
			reader.close();
			
			return true;
			
		} catch(IOException ioe) {
			// an error reading the file.
			// show an error message.
			JOptionPane.showMessageDialog(PinyinInput.this, Messages.Key.ERROR_READING_FILE.getText(), null, JOptionPane.ERROR_MESSAGE);
			ioe.printStackTrace();
		}
		
		return false;
    }
    
    /**
     * Helper method writes the current text component content to the given File.
     * @param file
     * @param charset the encoding to write in
     */
    private boolean writeFile(File file, Charset charset) {
    	try {
	    	Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), charset));
	    	
	    	// if there's any uncommitted text, commit it before writing
	    	this.textArea.getInputContext().endComposition();
	    	
	    	this.textArea.write(writer);
	    	
	    	this.setSaveFile(file, charset);
	    	writer.close();
	    	
	    	return true;
	    	
    	} catch(IOException ioe) {
    		// an error writing the file.
    		// show a message.
			JOptionPane.showMessageDialog(PinyinInput.this, Messages.Key.ERROR_WRITING_FILE.getText(), null, JOptionPane.ERROR_MESSAGE);
    		ioe.printStackTrace();
    	}
    	
    	return false;
	}
    
    /**
     * Register the save file so we can use the "Save" option
     * vs. Save as in the future.
     * @param file
     * @param charset the file's character encoding
     */
    private void setSaveFile(File file, Charset charset) {
    	this.saveFile = file;
    	this.saveCharset = charset;
    	
    	// item becomes enabled once we have used one File
    	this.saveMenuItem.setEnabled(true);
    }
    
    /**
     * Use to check whether a File location is known so we can save.
     * @return whether the file has been saved
     */
    private boolean isSaved() {
    	return null != this.saveFile;
    }
    
    /**
     * Mark that the text has changed since the last save.
     * Can use this to test whether we can open/close without saving.
     */
    private void markChanged() {
    	this.changedSinceSave = true;
    }
    
    /**
     * Check if we need to save before taking another action.
     * If so, pop up a save confirmation dialog.
     * @return true if should continue, false if cancelled
     */
    private boolean checkSaveContents() {
    	if(this.changedSinceSave) {
    		int result = JOptionPane.showConfirmDialog(this, Messages.Key.SAVE_CHANGES.getText());
    		if(JOptionPane.CANCEL_OPTION == result) {
    			// if cancelled, then indicate that we shouldn't
    			// take a subsequent action.
    			return false;
    		
    		} else if(JOptionPane.YES_OPTION == result) {
    			// if yes, then we might pop up a save as dialog
    			// if necessary.  if so, and they hit cancel,
    			// then we return that they cancelled from there.
    			return this.save();
    		}
    	}
    	
    	// didn't change, no need to save
    	return true;
    }
    
    /**
     * Check if we need to save first, then pop up
     * an open file dialog.
     */
    private boolean open() {
    	if(this.checkSaveContents()) {
    		// check if we need to save the current contents
    		// before we allow them to overwrite the existing
    		// contents by opening over it
    		
			JFileChooser fileChooser = PinyinInput.this.buildFileChooser(PinyinInput.this.saveFile, PinyinInput.this.saveCharset);
			if(JFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog(this)) {
				// file was selected
				
				File file = fileChooser.getSelectedFile();
				Charset charset = Charset.forName(fileChooser.getFileFilter().getDescription());
				
				return this.readFile(file, charset);
			}
    	}
    	
    	return false;
    }
    
    /**
     * Save the file.
     * If the location is known, then just save.
     * If not, then save as.
     * @return true if saved, false if save cancelled
     */
    private boolean save() {
    	if(this.isSaved()) {
    		this.writeFile(this.saveFile, this.saveCharset);
    		return true;
    	}
    	
    	// when saving as, they might cancel, return that result
    	return this.saveAs();
    }
    
    /**
     * Save the file to a new location.
     * @return true if saved, false if cancelled.
     */
    private boolean saveAs() {
    	JFileChooser fileChooser = PinyinInput.this.buildFileChooser(PinyinInput.this.saveFile, PinyinInput.this.saveCharset);
		if(JFileChooser.APPROVE_OPTION == fileChooser.showSaveDialog(this)) {
			File file = fileChooser.getSelectedFile();
			if(file.exists()) {
				// file exists.
				// confirm it's OK to overwrite.
				
				int overwrite = JOptionPane.showConfirmDialog(PinyinInput.this, Messages.Key.OK_TO_OVERWRITE.getText(), null, JOptionPane.YES_NO_OPTION);
				if(overwrite == JOptionPane.NO_OPTION) {
					// elected not to overwrite the existing file.
					// treat as a cancel.
					return false;
				}
			}
			
			Charset charset = Charset.forName(fileChooser.getFileFilter().getDescription());
			return this.writeFile(file, charset);
		}
		
		return false;
    }
    
    /**
     * If necessary pop up a save confirmation, then exit
     * @return true if not cancelled during save confirmation
     */
    private boolean exit() {
    	if(this.checkSaveContents()) {
    		// either saved, or elected to not save
    		
    		this.dispose();
    		return true;
    	}
    	
    	// cancelled
    	return false;
    }
    
    private void transfer(String name) throws IOException{
    	
    	System.out.println("Word name : "+name);
    	transferchar = textArea.getText();
    	byte[] utf8 = transferchar.getBytes("UTF-8");
    	System.out.println("Byte data : "+utf8);
    	transferchar = new String(utf8);
    	System.out.println("Text transferred : "+transferchar);
    	
    	utftoimage runthis = new utftoimage(transferchar,name,lessonname); 
    	
    }
    
    
    /**
     * Main method runs the program.
     * @param args
     */
    
    
    static public void main(String[] args) {
    	PinyinInput input = new PinyinInput(Messages.Key.TITLE.getText(), lessonname);
    	
    	input.pack();
    	input.setVisible(true);
    }
    
}