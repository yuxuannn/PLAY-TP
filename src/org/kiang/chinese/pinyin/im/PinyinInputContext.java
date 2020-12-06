package org.kiang.chinese.pinyin.im;

import java.awt.Component;

import org.kiang.chinese.pinyin.im.PinyinInputTermSource.PinyinInputMethodControl;
import org.kiang.im.settable.SettableInputMethodContext;

/**
 * A simple InputContext for Pinyin input.
 * @author Jordan Kiang
 */
public class PinyinInputContext extends SettableInputMethodContext {

	/**
	 * @param clientComponent the component this InputContext is associated with
	 */
	public PinyinInputContext(Component clientComponent) {
		super(clientComponent, new PinyinInputMethodDescriptor());
	}
	
	/**
	 * Covariant return with the typed control Object.
	 * @see org.kiang.im.settable.SettableInputMethodContext#getInputMethodControlObject()
	 */
	@Override
	public PinyinInputMethodControl getInputMethodControlObject() {
		return (PinyinInputMethodControl)super.getInputMethodControlObject();
	}
}
