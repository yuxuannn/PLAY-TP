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

package org.kiang.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * An implementation of InputStreamSource that opens
 * a stream from the given URL.
 * @author Jordan Kiang
 */
public class URLInputStreamSource implements InputStreamSource {
	
	private URL url;
	
	/**
	 * Create a source from the given url.
	 * @param url the url
	 */
	public URLInputStreamSource(URL url) {
		if(null == url) {
			throw new NullPointerException("url cannot be null!");
		}
		
		this.url = url;
	}
	
	/**
	 * Generate a source from the given resource path.
	 * @param resourcePath the path to the resource
	 */
	public URLInputStreamSource(String resourcePath) {
		if(null == resourcePath) {
			throw new NullPointerException("resourcePath cannot be null!");
		}
		
		URL url = URLInputStreamSource.class.getResource(resourcePath);
		if(null == url) {
			throw new IllegalArgumentException("can't find resource: " + resourcePath);
		}
		
		this.url = url;
	}

	/**
	 * @see org.kiang.io.InputStreamSource#openStream()
	 */
	public InputStream openStream() throws IOException {
		return this.url.openStream();
	}
}
