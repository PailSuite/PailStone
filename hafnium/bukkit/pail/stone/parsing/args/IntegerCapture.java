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
import hafnium.bukkit.pail.util.ArgumentConstraints;
import hafnium.bukkit.pail.util.ArgumentConstraints.IntegerConstraint;
import hafnium.bukkit.pail.util.sign.SignLocation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IntegerCapture extends ArgCapture<Integer> {
	private final IntegerConstraint constraint;
	private final String purpose;
	private final boolean nullAsDefault;
	private boolean set = false;

	public IntegerCapture(String purpose, IntegerConstraint constraint, Integer def) {
		this(purpose, constraint, def, false);
	}

	public IntegerCapture(String purpose, IntegerConstraint constraint, boolean nullAsDefault) {
		this(purpose, constraint, null, nullAsDefault);
	}

	public IntegerCapture(String purpose, IntegerConstraint constraint, Integer def, boolean nullAsDefault) {
		super(def, 1);

		this.constraint = constraint;
		this.purpose = purpose;
		this.nullAsDefault = nullAsDefault;
	}

	@Override
	public boolean hasDefault() {
		return this.nullAsDefault || super.hasDefault();
	}

	@Override
	public boolean showResult() {
		return !this.nullAsDefault || this.set;
	}

	@Override
	public ICFormatException getBlankException(int line) {
		return new ICFormatException("^eYou must specify a ^n" + this.purpose + "^e.");
	}

	@Override
	public ICFormatException getInvalidException(String arg, int line) {
		return new ICFormatException("^eThe argument you entered (^u" + arg + "^e) is not a valid ^n" + this.purpose + "^e. "
				+ this.getConstraintExplanation(), line);
	}

	private String getConstraintExplanation() {
		return "^eThe value you enter must be a ^nwhole number^e"
				+ (this.constraint != null ? (" that is " + this.constraint.andBeString()) + "^e" : "") + ".";
	}

	private static Pattern decimal = Pattern.compile("(-?[0-9]+)");
	private static Pattern hex = Pattern.compile("(-?)(?:0x)?([0-9a-f]+)");
	private static Pattern bin = Pattern.compile("(-?)0?b([0-1]+)");

	@Override
	public Integer parse(String arg, int line, SignLocation loc, String owner) throws ICFormatException {
		try {
			int val;

			Matcher mDec, mHex, mBin;
			mDec = decimal.matcher(arg);
			mHex = hex.matcher(arg);
			mBin = bin.matcher(arg);

			if (mDec.matches())
				val = Integer.parseInt(arg);
			else if (mHex.matches())
				val = Integer.parseInt((mHex.group(1) != null ? mHex.group(1) : "") + mHex.group(2), 16);
			else if (mBin.matches())
				val = Integer.parseInt((mBin.group(1) != null ? mBin.group(1) : "") + mBin.group(2), 2);
			else
				throw this.getInvalidException(arg, line);

			if (this.constraint != null)
				if (!this.constraint.isAcceptable(val))
					throw this.getInvalidException(arg, line);

			this.set = true;
			return this.setResult(Integer.parseInt(arg));
		} catch (NumberFormatException e) {
			throw this.getInvalidException(arg, line);
		}
	}

	@Override
	public String getTextFor(Object object) {
		if (!(object instanceof Integer))
			throw new IllegalArgumentException("This capture can only get the text representation on an Integer.");

		return ((Integer) object).toString();
	}

	/*
	 * Constraint convenience methods:
	 */

	public static IntegerConstraint getMaxConstraint(int max) {
		return new ArgumentConstraints.IntMaxConstraint(max);
	}

	public static IntegerConstraint getMinConstraint(int min) {
		return new ArgumentConstraints.IntMinConstraint(min);
	}

	public static IntegerConstraint getRangeConstraint(int min, int max) {
		return new ArgumentConstraints.IntRangeConstraint(min, max);
	}

	// TODO: Redo constraints to Double's format.
}
