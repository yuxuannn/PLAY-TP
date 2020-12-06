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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An InputStreamSource that reads from a File.
 * @author Jordan Kiang
 */
public class FileInputStreamSource implements InputStreamSource {

	private File file;
	
	/**
	 * @param file the file
	 */
	public FileInputStreamSource(File file) {
		if(null == file) {
			throw new NullPointerException("file cannot be null!");
		}
		
		this.file = file;
	}
	
	/**
	 * @see org.kiang.io.InputStreamSource#openStream()
	 */
	public InputStream openStream() throws IOException {
		return new FileInputStream(this.file);
	}
}
