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

import hafnium.bukkit.pail.stone.ic.ICFormatException;
import hafnium.bukkit.pail.stone.parsing.ICParser.ArgCapture;
import hafnium.bukkit.pail.util.sign.SignLocation;
import hafnium.bukkit.pail.util.text.MessageableException;

import org.bukkit.DyeColor;

public class DyeColorCapture extends ArgCapture<DyeColor> {

	public DyeColorCapture(DyeColor def) {
		super(def, 1);
	}

	@Override
	public ICFormatException getBlankException(int line) {
		return new ICFormatException("^eYou must specify a dye color.", line);
	}

	@Override
	public ICFormatException getInvalidException(String input, int line) {
		return new ICFormatException("^eI could not understand ^u" + input
				+ " ^eas a dye color. I expected a single character color-code, ^n0^g-^n9 ^eor ^nA^g-^nF^e, or one of these: ^c"
				+ EnumCapture.getValueList(DyeColor.values()));
	}

	@Override
	public String getTextFor(Object object) {
		DyeColor color = (DyeColor) object;
		return Integer.toHexString(color.getData()).toUpperCase();
	}

	@Override
	public DyeColor parse(String arg, int line, SignLocation loc, String owner) throws MessageableException {
		try {
			for (DyeColor color : DyeColor.values())
				if (color.name().equalsIgnoreCase(arg))
					return this.setResult(color);

			int data = Integer.parseInt(arg, 16);
			if ((data < 0) || (data >= 16))
				throw this.getInvalidException(arg, line);
			return this.setResult(DyeColor.getByData((byte) data));
		} catch (Exception e) {
			throw this.getInvalidException(arg, line);
		}
	}
}
