package org.kiang.chinese.pinyin.im.app.applet;

import java.applet.Applet;

import org.kiang.chinese.pinyin.im.app.PinyinInputConfig;

class PinyinTextInputAppletConfig extends PinyinInputConfig {

	/**
	 * Applet Parameter key for specifying whether composition
	 * is on a single line (i.e. as in a text field) or on multiple
	 * lines (i.e. as in a text area).
	 */
	static final String LINE_MODE_PARAM = "lineMode";
	/**
	 * if LINE_MODE_PARAM is "single" then single line mode,
	 * anything else and it will be multi-line.
	 */
	static final String SINGLE_MODE		= "single";
	
	/**
	 * Parameter specifies the wrap mode.
	 * "true" to line wrap, false otherwise.
	 */
	static final String WRAP_PARAM 		= "wrap";
	
	private Applet applet;
	
	/**
	 * @param applet the associated applet
	 */
	PinyinTextInputAppletConfig(Applet applet) {
		if(null == applet) {
			throw new NullPointerException("applet cannot be null!");
		}
		
		this.applet = applet;
	}
	
	/** @see org.kiang.chinese.pinyin.im.app.PinyinInputConfig#getString(java.lang.String) */
	@Override
	protected String getString(String key) {
		return this.applet.getParameter(key);
	}
	
	/**
	 * @return true for multi line mode (a text area), false for single line (a field)
	 */
	public boolean getLineMode() {
		String paramStr = this.getString(LINE_MODE_PARAM);
		
		// default is a multi-line text area, so if the
		// parameter value is anything but "true", then
		// we return true.
		boolean multi = null == paramStr ||
						!SINGLE_MODE.equals(paramStr.toLowerCase());
		return multi;	
	}
	
	/**
	 * @return true for line wrapping, false otherwise
	 */
	public boolean getWrapMode() {
		String wrapStr = this.getString(WRAP_PARAM);
		
		// if there is no wrap parameter, or the parameter
		// is anything other than "false", then wrap is on.
		boolean wrap = null == wrapStr || !Boolean.FALSE.toString().equals(wrapStr.toLowerCase());
		return wrap;
	}
}
