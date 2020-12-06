package org.kiang.chinese.pinyin.im.app.applet;

import java.applet.AppletContext;
import java.applet.AppletStub;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.DefaultFocusTraversalPolicy;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import org.kiang.chinese.font.ChineseFontFinder;
import org.kiang.chinese.pinyin.im.PinyinInputContext;
import org.kiang.chinese.pinyin.im.PinyinInputTermSource.PinyinInputMethodControl;
import org.kiang.chinese.pinyin.im.app.Messages;
import org.kiang.chinese.pinyin.im.app.PinyinInputConfig;
import org.kiang.chinese.pinyin.im.app.PinyinInputUIBuilder;
import org.kiang.chinese.pinyin.im.app.PinyinInputUIBuilder.FontSource;
import org.kiang.chinese.pinyin.im.swing.PinyinInputTextArea;
import org.kiang.swing.JFontChooser;

/**
 * Applet for inputting Pinyin.
 * @author jkiang
 */
public class PinyinInputApplet extends JApplet {
	
	private PinyinInputTextArea textArea;
	private JMenuItem downloadFontMenuItem;
	
	private Font downloadedFont;
	
	private PinyinInputAppletConfig config;
	
	/**
	 * @see java.applet.Applet#init()
	 */
	@Override
	public void init() {
		// configuration is derived from Applet params
		this.config = new PinyinInputAppletConfig(this);
		
		this.textArea = this.buildTextArea();
		
		if(this.supportsFontDownload()) {
			this.downloadFontMenuItem = new JMenuItem();
		}
		this.setupFont();
		
		this.getContentPane().add(this.buildScrollPane(this.textArea));
		
		this.setJMenuBar(this.buildMenuBar());
		
		// explicitly set the text area as the default focus component.
		// otherwise it seems that it can get unfocused and becomes
		// difficult to refocus on within the applet.
		this.setFocusTraversalPolicy(new DefaultFocusTraversalPolicy() {
			/** @see java.awt.ContainerOrderFocusTraversalPolicy#getDefaultComponent(java.awt.Container) */
			@Override
			public Component getDefaultComponent(Container container) {
				return PinyinInputApplet.this.textArea;
			}
		});
	}
	
	private PinyinInputTextArea buildTextArea() {
		PinyinInputTextArea textArea = new PinyinInputTextArea();
		
		PinyinInputContext inputContext = textArea.getInputContext();
		PinyinInputMethodControl control = inputContext.getInputMethodControlObject();
		control.setChooserOrientation(this.config.getChooserOrientation());
		control.setUsingRawWindow(this.config.getRawMode());

		Font currentFont = textArea.getFont();
		Font font = this.config.getFont(currentFont);
		textArea.setFont(font);
		
		return textArea;
	}
	
	private JScrollPane buildScrollPane(JComponent component) {
		// put the text area into a 
		JScrollPane scrollPane = new JScrollPane(component);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		
		return scrollPane;
	}
	
	private boolean supportsFontDownload() {
		return null != this.config.getFontDownloadURL();
	}
	
	private void setupFont() {
		Font currentFont = this.textArea.getFont();
		
		Boolean characterMode = null;
		if(ChineseFontFinder.isSimplifiedFont(currentFont)) {
			// font is capable of displaying simplified (the default).
			characterMode = Boolean.TRUE;
			
		} else if(ChineseFontFinder.isTraditionalFont(currentFont)) {
			// font is capable of displaying traditional, but not simplified.
			characterMode = Boolean.FALSE;
			
		}
		
		if(null != characterMode) {
			// font is capable of displaying Chinese.
			// set the character mode to use the type
			// of characters it was determined were
			// supported by the Font.
			this.textArea.getInputContext().getInputMethodControlObject().setCharacterMode(characterMode.booleanValue());	
		
		} else if(this.supportsFontDownload()) {
			// font is not capable of dispaying Chinese.
			// prompt if they want to download 
			// if an item was defined on the menu,
			// that means that we the applet should
			// give the option of downloading a Font.
			
			if(JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this, Messages.Key.FONT_DOWNLOAD_PROMPT.getText(), "", JOptionPane.YES_NO_OPTION)) {
				this.initiateDownload();
			}
		}
	}
	
	private JMenuBar buildMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		
		menuBar.add(PinyinInputUIBuilder.buildEditMenu());
		menuBar.add(this.buildFormatMenu());
		
		// push the about menu over to the right
		menuBar.add(Box.createHorizontalGlue());
		menuBar.add(PinyinInputUIBuilder.buildAboutMenu(this));
		
		return menuBar;
	}
	
	
	private JMenu buildFormatMenu() {
		FontSource fontSource = new FontSource() {
			/** @see org.kiang.chinese.pinyin.im.app.PinyinInputUIBuilder.FontSource#getFonts() */
			public Font[] getFonts() {
				Font currentFont = PinyinInputApplet.this.textArea.getFont();
				int fontStyle = currentFont.getStyle();
				int fontSize = currentFont.getSize();
				
				Font[] fonts = JFontChooser.getSystemFonts(fontStyle, fontSize);
				if(null != PinyinInputApplet.this.downloadedFont) {
					Font[] fonts2 = new Font[fonts.length + 1];
					System.arraycopy(fonts, 0, fonts2, 1, fonts.length);
					fonts2[0] = PinyinInputApplet.this.downloadedFont;
					fonts = fonts2;
				}
				
				return fonts;
			}
		};
		
		JMenu menu = PinyinInputUIBuilder.buildFormatMenu(this.textArea, fontSource);
		
		// a Font download URL was specified, then we add a menu item
		// for downloading the Font.
		if(this.supportsFontDownload()) {
			this.downloadFontMenuItem.setText(Messages.Key.FONT_DOWNLOAD.getText());
			this.downloadFontMenuItem.addActionListener(new ActionListener() {
				/** @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent) */
				public void actionPerformed(ActionEvent e) {
					PinyinInputApplet.this.initiateDownload();
				}
			});
		
			menu.add(this.downloadFontMenuItem);
		
			this.downloadFontMenuItem.setMnemonic(KeyEvent.VK_D);
		}
		
		return menu;
	}
	
	/**
	 * Initiate download of a Font.
	 */
	private void initiateDownload() {
		// disable the item during the download.
		// disallows concurrent downloading.
		this.downloadFontMenuItem.setEnabled(false);
		
		// the download needs to occur in 
		Thread thread = new Thread() {

			@Override
			public void run() {
				String downloadMessageResult = null;
				int messageType;
				try {
					if(PinyinInputApplet.this.downloadFont()) {
						downloadMessageResult = Messages.Key.FONT_DOWNLOADED.getText(PinyinInputApplet.this.downloadedFont.getFamily());
						messageType = JOptionPane.INFORMATION_MESSAGE;
					} else {
						downloadMessageResult = Messages.Key.FONT_DOWNLOAD_ABORTED.getText();
						messageType = JOptionPane.WARNING_MESSAGE;
						
						// renable download after abort to allow
						// a subsequent download
						PinyinInputApplet.this.downloadFontMenuItem.setEnabled(true);
					}
					
				} catch(Exception e) {
					downloadMessageResult = Messages.Key.FONT_DOWNLOAD_ERROR.getText();
					messageType = JOptionPane.ERROR_MESSAGE;
					
					// renable download after error to allow
					// a subsequent download attempt
					PinyinInputApplet.this.downloadFontMenuItem.setEnabled(true);
					
					e.printStackTrace();
				}
				
				// display the result of downloading the Font.
				JOptionPane.showMessageDialog(PinyinInputApplet.this, downloadMessageResult, "", messageType);
			}
		};
		
		// kick off the download in the separate thread.
		thread.start();
	}
	
	private boolean downloadFont() throws IOException, FontFormatException {		
		String fontURL = this.config.getFontDownloadURL();
		//"http://www.kiang.org/misc/fireflysung-1.3.0/fireflysung.ttf"
		URL url = new URL(fontURL);
		
		URLConnection connection = url.openConnection();
		
		int contentLength = -1;
		String contentLengthStr = connection.getHeaderField("Content-Length");
		if(null != contentLengthStr) {
			try {
				contentLength = Integer.parseInt(contentLengthStr);
			} catch(NumberFormatException nfe) {
				// will use an indeterminate progress bar
			}
		}
		
		Frame owner = JOptionPane.getFrameForComponent(this);
		JDialog dialog = new JDialog(owner, Messages.Key.FONT_DOWNLOADING.getText());
		
		JProgressBar progressBar;
		if(contentLength > 0) {
			// set the progress bar to the number of bytes
			// to download in the font file
			progressBar = new JProgressBar(0, contentLength);
		} else {
			// unable to determine the size of the file
			// to download, use an indeterminate progress bar
			progressBar = new JProgressBar();
			progressBar.setIndeterminate(true);
		}
		progressBar.setPreferredSize(new Dimension(200, 15));
		
		JButton cancelButton = new JButton(Messages.Key.CANCEL.getText());
		cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		final AtomicBoolean aborted = new AtomicBoolean(false);
		cancelButton.addActionListener(new ActionListener() {
			/** @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent) */
			public void actionPerformed(ActionEvent e) {
				aborted.set(true);
			}
		});
		
		Container container = dialog.getContentPane();
		
		Box box = Box.createVerticalBox();
		box.add(progressBar);
		box.add(cancelButton);
		
		container.add(box);
		
		dialog.pack();
		
		// locate the dialog relative to the
		// applet component
		dialog.setLocationRelativeTo(this);
		
		// add a WindowListener to abort the download on
		// closing the dialog
		dialog.addWindowListener(new WindowAdapter() {
			/** @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent) */
			@Override
			public void windowClosing(WindowEvent e) {
				aborted.set(true);
			}
		});
		
		dialog.setVisible(true);
		
		ByteArrayOutputStream fontBytes = new ByteArrayOutputStream();
		
		byte[] buffer = new byte[1024];
		try {
			InputStream in = new BufferedInputStream(connection.getInputStream());
			for(int bytesRead = in.read(buffer); bytesRead >= 0 && !aborted.get(); bytesRead = in.read(buffer)) {
				fontBytes.write(buffer, 0, bytesRead);
				progressBar.setValue(progressBar.getValue() + bytesRead);
				progressBar.repaint();
			}
		
			if(!aborted.get()) {
				InputStream fontStream = new ByteArrayInputStream(fontBytes.toByteArray());
				String zipPath = this.config.getFontZipPath();
				if(null != zipPath) {
					fontStream = extractFontStreamFromZip(zipPath, fontStream);
				}
				
				Font downloadedFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);		
				
				Font currentFont = PinyinInputApplet.this.textArea.getFont();
				int fontSize = currentFont.getSize();
				int fontStyle = currentFont.getStyle();
				
				downloadedFont = downloadedFont.deriveFont(fontStyle, fontSize);
				PinyinInputApplet.this.textArea.setFont(downloadedFont);
				PinyinInputApplet.this.downloadedFont = downloadedFont;
				
				return true;
			}
			
			return false;
			
		} finally {
			dialog.dispose();
		}
	}
	
	static private InputStream extractFontStreamFromZip(String zipPath, InputStream in) throws IOException {
		ByteArrayOutputStream fontBytes = new ByteArrayOutputStream();
		
		ZipInputStream zipIn = new ZipInputStream(in);
		
		ZipEntry entry;
		while(null != (entry = zipIn.getNextEntry())) {
			if(entry.isDirectory()) {
				continue;
			
			} else if(zipPath.equals(entry.getName())) {
				
				long size = entry.getSize();
				
				byte[] buffer = new byte[1024];
				
				for(int totalBytesRead = 0; totalBytesRead < size;) {
					int len = (int)Math.min(buffer.length, size - totalBytesRead);
					int bytesRead = zipIn.read(buffer, 0, len);
					
					fontBytes.write(buffer, 0, bytesRead);
					totalBytesRead += bytesRead;
				}
				
				break;
				
			}
		}
		
		zipIn.close();
		
		return new ByteArrayInputStream(fontBytes.toByteArray());
	}
	
	/**
	 * @return the currently input text
	 */
	public String getText() {
		return this.textArea.getText();
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
				{PinyinInputAppletConfig.FONT_DOWNLOAD_URL_PARAM, "url", "url of a Chinese TrueType font to download if necessary"},
				{PinyinInputAppletConfig.FONT_ZIP_PATH_PARAM, "path", "path of a TrueType font within a zip file if the download Font url is to a zip file"}
		};		
	}
	
	/**
	 * Testing main method.
	 * @param args
	 * @throws Exception
	 */
	static public void main(String[] args) throws Exception {
		final Map<String, String> appletParams = new HashMap<String, String>();
		appletParams.put(PinyinInputAppletConfig.FONT_DOWNLOAD_URL_PARAM, "http://www.kiang.org/misc/fireflysung-1.3.0.zip");
		appletParams.put(PinyinInputAppletConfig.FONT_ZIP_PATH_PARAM, "fireflysung-1.3.0/fireflysung.ttf");
		appletParams.put(PinyinInputConfig.RAW_MODE_PARAM, "window");
		
		
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
				return appletParams.get(name);
			}
		};
		
		
		JFrame frame = new JFrame();
		PinyinInputApplet applet = new PinyinInputApplet();
		applet.setStub(appletStub);
		applet.init();
		
		frame.getContentPane().add(applet);
		
		applet.setPreferredSize(new Dimension(500, 100));
	
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
}
