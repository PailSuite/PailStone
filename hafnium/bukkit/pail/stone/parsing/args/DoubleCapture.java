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

import java.text.DecimalFormat;

public class DoubleCapture extends ArgCapture<Double> {
	private static final DecimalFormat display = new DecimalFormat("0.0#");

	private final String purpose;
	private final DoubleConstraint constraint;

	public DoubleCapture(String purpose, DoubleConstraint constraint, Double def) {
		super(def, 1);
		this.purpose = purpose;
		this.constraint = constraint;
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
		return "^eThe value you enter must be a ^nnumber^e" + (this.constraint != null ? (" that is ^n" + this.constraint.toString()) + "^e" : "")
				+ ".";
	}

	@Override
	public Double parse(String arg, int line, SignLocation loc, String owner) throws MessageableException {
		try {
			Double res = Double.parseDouble(arg);
			if ((this.constraint != null) && !this.constraint.matches(res))
				throw this.getInvalidException(arg, line);
			return this.setResult(res);
		} catch (NumberFormatException e) {
			throw this.getInvalidException(arg, line);
		}
	}

	@Override
	public String getTextFor(Object object) {
		return display.format(object);
	}

	public static DoubleConstraint getConstraint(Double min, Double max) {
		return new DoubleConstraint(min, max);
	}

	public static class DoubleConstraint {
		private final Double min, max;

		public DoubleConstraint(Double min, Double max) {
			this.min = min;
			this.max = max;
		}

		public boolean matches(double test) {
			if ((this.min != null) && (test < this.min - 0.0001))
				return false;
			if ((this.max != null) && (test > this.max + 0.0001))
				return false;
			return true;
		}

		@Override
		public String toString() {
			if ((this.max != null) && (this.min != null))
				return "between " + display.format(this.min) + " and " + display.format(this.max);
			else if (this.max != null)
				return "less than " + display.format(this.max);
			else if (this.min != null)
				return "greater than " + display.format(this.min);
			else
				return null;
		}
	}
}
