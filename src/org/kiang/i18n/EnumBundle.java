package org.kiang.i18n;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * A ResourceBundle backed by an Enum.
 * The keys are the names of the Enums, and the value Objects
 * is the toString of the enum instances (can be different).
 * 
 * The purpose is to use with enum instances that serve as both programmatic
 * tokens that represent particular messages, as well as default
 * messages when no other bundles are available.
 * 
 * To have the bundle loaded by from the classpath by ResourceBundle,
 * make an empty subclass of this class that passes up the correct enum
 * class in its default constructor.
 * 
 * @author Jordan Kiang
 */
public class EnumBundle extends ResourceBundle {
	
	private Class<? extends Enum<?>> type;
	
	/**
	 * @param type the Enum type
	 */
	public EnumBundle(Class<? extends Enum<?>> type) {
		if(null == type) {
			throw new NullPointerException("type cannot be null!");
		}
		
		this.type = type;
	}
	
	// uses reflection to read keys and resources
	// seems like there should be a better since we know
	// that it's an Enum.
	
	/**
	 * @see java.util.ResourceBundle#getKeys()
	 */
	@Override
	public Enumeration<String> getKeys() {
		Set<String> keys = new HashSet<String>();
		try {
			Method valuesMethod = this.type.getMethod("values");
			Enum<?>[] values = (Enum<?>[])valuesMethod.invoke(this.type);
			
			for(Enum<?> instance : values) {
				keys.add(instance.name());
			}
			return Collections.enumeration(keys);
			
		} catch(Exception e) {
			// shouldn't be possible
			throw new IllegalStateException("Couldn't generate keys!", e);
		}
	}

	/**
	 * @see java.util.ResourceBundle#handleGetObject(java.lang.String)
	 */
	@Override
	protected Object handleGetObject(String key) {
		if(null == key) {
			throw new NullPointerException("null key");
		}
		
		try {
			Method method = this.type.getMethod("valueOf", new Class<?>[] {String.class});
			Object object = method.invoke(this.type, new Object[] {key});
			return object.toString();
			
		} catch(Exception e) {
			if(e.getCause() instanceof IllegalArgumentException) {
				// reflection will wrap the source Exception.
				// if it's an IllegalArgumentException, then that
				// just means the value wasn't found in this enum.
				// return null.
				return null;
			}
			
			// shouldn't be possible
			throw new IllegalStateException("Couldn't get Object!", e);
		}
	}
}
	
