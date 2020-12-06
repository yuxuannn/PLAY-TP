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

/**
 * Defines a source of an InputStream that
 * can be repeatedly reopened.
 * 
 * @author Jordan Kiang
 */
public interface InputStreamSource {
	
	/**
	 * Obtain the stream.
	 * Every time this method is invoked a new stream
	 * instance is returned positioned at the start
	 * of the stream.
	 * 
	 * @return the streams
	 * @throws IOException
	 */
	public InputStream openStream() throws IOException;
}
