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
package hafnium.bukkit.pail.stone.parsing.args;

import hafnium.bukkit.pail.pipe.data.DatumID;
import hafnium.bukkit.pail.stone.ic.ICFormatException;
import hafnium.bukkit.pail.stone.parsing.ICParser.ArgCapture;
import hafnium.bukkit.pail.util.sign.SignLocation;

public class DatumIDCapture extends ArgCapture<DatumID> {
	public DatumIDCapture() {
		super(null, 1);
	}

	@Override
	public ICFormatException getBlankException(int line) {
		return new ICFormatException("^eYou must specify a data id.", line);
	}

	@Override
	public ICFormatException getInvalidException(String input, int line) {
		return new ICFormatException("^eInvalid data id.", line);
	}

	@Override
	public String getTextFor(Object object) {
		return ((DatumID) object).toMinimalString();
	}

	@Override
	public DatumID parse(String arg, int line, SignLocation loc, String owner) throws ICFormatException {
		DatumID id = DatumID.getFor(owner, arg);

		if (id == null)
			throw this.getInvalidException(arg, line);

		return this.setResult(id);
	}
}
