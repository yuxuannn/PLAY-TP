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

package org.kiang.i18n;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * A ResourceBundle that aggregates a bunch of children ResourceBundles
 * into a single bundle.  Pass in a Collection of ResourceBundles in
 * the constructor.  The returned resource will be from the first
 * ResourceBundle in the Collection that has a match, order determined
 * by the Collections' Iterator.
 * 
 * @author Jordan Kiang
 */
public class AggregateResourceBundle extends ResourceBundle {

	private Collection<ResourceBundle> childBundles;
	
	/**
	 * @param childBundles the bundles to aggregate
	 */
	public AggregateResourceBundle(Collection<ResourceBundle> childBundles) {
		if(null == childBundles) {
			throw new NullPointerException("childBundles cannot be null!");
		}
		
		this.childBundles = childBundles;
	}

	/**
	 * @see java.util.ResourceBundle#getKeys()
	 */
	@Override
	public Enumeration<String> getKeys() {
		// aggregate all the keys into one Enumeration.
		// don't think this gets called much, otherwise
		// could cache this...
		Set<String> keys = new LinkedHashSet<String>();
		for(ResourceBundle childBundle : this.childBundles) {
			for(Enumeration<String> keyEnumeration = childBundle.getKeys(); keyEnumeration.hasMoreElements();) {
				keys.add(keyEnumeration.nextElement());
			}
		}
		
		return Collections.enumeration(keys);
	}

	/**
	 * @see java.util.ResourceBundle#handleGetObject(java.lang.String)
	 */
	@Override
	protected Object handleGetObject(String key) {
		// check bundles successively until we find one that
		// contains a resource for the given key.
		for(ResourceBundle childBundle : this.childBundles) {
			try {
				Object object = childBundle.getObject(key);
				if(null != object) {
					// technically don't think this can be null
					// by ResourceBundle's contract, but might
					// as well check.
					return object;
				}
			} catch(MissingResourceException mre) {
				// accessing through getObject means
				// that an MRE is thrown when no entry exists.
				// not an error since we'll continue
				// looking in the other bundles.
			}
		}
		
		// doesn't exist
		return null;
	}
}
