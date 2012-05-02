/* 
 * Copyright (c) 2012 Chris Bode
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal in the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the 
 * Software, and to permit persons to whom the Software is furnished to do so, subject to the following 
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions 
 * of the Software.
 *
 * No affiliation with PailStone or any related projects is claimed.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED 
 * TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
 * DEALINGS IN THE SOFTWARE.
 */
package hafnium.bukkit.pail.stone.ic;

import hafnium.bukkit.pail.util.text.MessageableException;
import hafnium.bukkit.pail.util.text.PailMessage;

import org.bukkit.entity.Player;

public class ICFormatException extends MessageableException {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private final int errorLine;

	public ICFormatException(String message, int line) {
		super(message);
		this.errorLine = line;
	}

	public ICFormatException(String message) {
		this(message, -1);
	}

	public ICFormatException() {
		this("^eThere is an unspecified error in this IC. Please try again.");
	}

	public void announce(Player p) {
		PailMessage m = new PailMessage(this.getMessage());
		m.sendTo(p);
	}

	public boolean isLineSpecified() {
		return this.errorLine != -1;
	}

	public int getErrorLine() {
		return this.errorLine;
	}
}
