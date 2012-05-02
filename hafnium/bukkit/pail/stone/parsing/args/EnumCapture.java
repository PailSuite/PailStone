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
import hafnium.bukkit.pail.util.AliasedEnum;
import hafnium.bukkit.pail.util.hf;
import hafnium.bukkit.pail.util.sign.SignLocation;

public class EnumCapture<T extends Enum<?>> extends ArgCapture<T> {
	private final String purpose;

	private final Class<T> enumClass;
	private final T[] values;

	public EnumCapture(String purpose, Class<T> enumClass, T def) {
		super(def, 1);

		this.purpose = purpose;

		this.enumClass = enumClass;
		this.values = enumClass.getEnumConstants();
	}

	public static String getValueList(Enum<?>[] values) {
		String vals = "";

		boolean first = true;

		for (Enum<?> val : values) {
			if (!first)
				vals += ", ";
			vals += val.name();
			first = false;
		}

		return vals.trim();
	}

	@Override
	public ICFormatException getBlankException(int line) {
		return new ICFormatException("^eYou must specify a ^n" + this.purpose + "^e.\nAcceptable values: ^c" + getValueList(this.values), line);
	}

	@Override
	public ICFormatException getInvalidException(String input, int line) {
		return new ICFormatException("^eThe argument you entered (^u" + input + "^e) is not a valid ^n" + this.purpose + "^e.\nAcceptable values: ^c"
				+ getValueList(this.values), line);
	}

	@Override
	public String getTextFor(Object object) {
		if (!(object.getClass() == this.enumClass))
			throw new IllegalArgumentException("This capture can only get the text representation on an Enum value from " + this.enumClass.getName()
					+ ".");

		return (this.enumClass.cast(object)).name().toUpperCase();
	}

	@Override
	public T parse(String arg, int line, SignLocation loc, String owner) throws ICFormatException {
		for (T val : this.values)
			if (arg.equalsIgnoreCase(val.name()) || ((val instanceof AliasedEnum) && hf.containsIgnoreCase(((AliasedEnum) val).getAliases(), arg)))
				return this.setResult(val);

		throw this.getInvalidException(arg, line);
	}
}
