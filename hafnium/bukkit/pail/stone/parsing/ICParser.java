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
package hafnium.bukkit.pail.stone.parsing;

import hafnium.bukkit.pail.stone.ic.ICFormatException;
import hafnium.bukkit.pail.util.sign.SignLocation;
import hafnium.bukkit.pail.util.sign.SignText;
import hafnium.bukkit.pail.util.text.MessageableException;

import java.util.ArrayList;

public class ICParser {
	private final ICLine[] lines;

	public ICParser(SignLocation loc, SignText text, String owner) {
		this.lines = new ICLine[4];
		for (int i = 0; i < 4; i++)
			this.lines[i] = new ICLine(text.getLine(i), i, text, loc, owner, this);
	}

	public ICLine getLine(int index) {
		return this.lines[index];
	}

	private boolean parsingStarted = false;

	public void parse() throws MessageableException {
		if (!this.hasStartedParsing()) {
			this.parsingStarted = true;

			for (int i = 1; i < 4; i++)
				this.lines[i].parse();
		}
	}

	public boolean hasStartedParsing() {
		return this.parsingStarted;
	}

	public class ICLine {
		private final String line;
		private final int lineNumber;
		private final SignText fullText;
		private final SignLocation loc;
		private final String owner;

		private final ICParser parser;

		private final ArrayList<ArgWrapper> captures;

		private ICLine(String line, int lineNumber, SignText fullText, SignLocation loc, String owner, ICParser parser) {
			this.line = line;
			this.lineNumber = lineNumber;
			this.fullText = fullText;
			this.loc = loc;
			this.owner = owner;

			this.parser = parser;

			this.captures = new ArrayList<ArgWrapper>();
		}

		public void parse() throws MessageableException {
			String formattedLine = "";

			String[] tokens = this.line.trim().split(" ");

			if (tokens[0].equals("") && (tokens.length == 1))
				tokens = new String[0];

			int tokenPos = 0;

			ArgCapture<?> arg;
			for (ArgWrapper wrap : this.captures) {
				if (!wrap.areConditionsMet()) {

					try {
						wrap.getArgument().setResultAsDefault(this.lineNumber, this.loc, this.owner);
					} catch (Exception e) {
						throw new RuntimeException("The plugin dev specified a conditional sign arg without supplying a default.");
					}

					continue;
				}

				arg = wrap.getArgument();

				if (tokens.length - tokenPos >= arg.getTokens()) {

					String nToken = "";

					for (int i = 0; i < arg.getTokens(); ++i)
						nToken += tokens[tokenPos++] + " ";

					Object result = arg.parse(nToken.trim(), this.lineNumber, this.loc, this.owner);

					if (arg.showResult())
						formattedLine += arg.getTextFor(result) + " ";
				} else if (arg.hasDefault()) {
					Object result = arg.setResultAsDefault(this.lineNumber, this.loc, this.owner);
					if (arg.showResult())
						formattedLine += arg.getTextFor(result) + " ";
				} else
					throw arg.getBlankException(this.lineNumber);

			}

			this.fullText.setLine(this.lineNumber, formattedLine.trim());
		}

		public ICLine add(ArgCapture<?> capture) {
			return this.add(capture, null);
		}

		public ICLine add(ArgCapture<?> capture, ParseCondition condition) {
			this.captures.add(new ArgWrapper(capture, condition));
			capture.setParser(this.parser);
			return this;
		}
	}

	private static class ArgWrapper {
		private final ArgCapture<?> capture;
		private final ParseCondition condition;

		public ArgWrapper(ArgCapture<?> capture, ParseCondition condition) {
			this.capture = capture;
			this.condition = condition;

			if ((condition != null) && !capture.hasDefault())
				throw new RuntimeException("The plugin dev specified a conditional sign arg without supplying a default.");
		}

		public ArgCapture<?> getArgument() {
			return this.capture;
		}

		public boolean areConditionsMet() throws MessageableException {
			if (this.condition == null)
				return true;

			if ((this.condition != null) && !this.condition.isSatisfied())
				return false;

			return true;
		}
	}

	public abstract static class ParseCondition {
		public abstract boolean isSatisfied() throws MessageableException;
	}

	public static abstract class ArgCapture<T> {
		private final T def;
		private final int tokens;
		private boolean hasParsed = false;

		private T result;

		public ArgCapture(T def, int tokens) {
			this.def = def;
			this.tokens = tokens;
		}

		public boolean hasParsed() {
			return this.hasParsed;
		}

		public int getTokens() {
			return this.tokens;
		}

		/**
		 * @param line
		 * @param loc
		 * @param owner
		 */
		public T getDefault(int line, SignLocation loc, String owner) throws MessageableException {
			return this.def;
		}

		public boolean hasDefault() {
			return this.def != null;
		}

		protected T setResult(T result) {
			this.hasParsed = true;
			return this.result = result;
		}

		protected T setResultAsDefault(int line, SignLocation loc, String owner) throws MessageableException {
			this.hasParsed = true;
			return this.setResult(this.getDefault(line, loc, owner));
		}

		public boolean showResult() {
			return true;
		}

		public T getResult() throws MessageableException {
			if (!this.hasParsed)
				if (!this.parser.hasStartedParsing())
					this.parser.parse();
				else
					throw new IllegalStateException("Attempted to access a capture before it was parsed.");

			return this.result;
		}

		private ICParser parser = null;

		private void setParser(ICParser parser) {
			this.parser = parser;
		}

		public abstract ICFormatException getBlankException(int line);

		public abstract ICFormatException getInvalidException(String input, int line);

		public abstract String getTextFor(Object object);

		public abstract T parse(String arg, int line, SignLocation loc, String owner) throws MessageableException;
	}
}
