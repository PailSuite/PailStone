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
package hafnium.bukkit.pail.stone.ic.executors.spec;

import hafnium.bukkit.pail.stone.ic.ICExecutor;
import hafnium.bukkit.pail.stone.ic.ICFormatException;
import hafnium.bukkit.pail.stone.ic.ICStub;
import hafnium.bukkit.pail.stone.ic.states.ICState;
import hafnium.bukkit.pail.stone.parsing.ICParser;
import hafnium.bukkit.pail.stone.parsing.ICParser.ArgCapture;
import hafnium.bukkit.pail.stone.parsing.args.IntegerCapture;
import hafnium.bukkit.pail.stone.trigger.EventArgs;
import hafnium.bukkit.pail.util.redstone.Current;
import hafnium.bukkit.pail.util.sign.SignLocation;
import hafnium.bukkit.pail.util.sign.SignText;
import hafnium.bukkit.pail.util.text.MessageableException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CountExec extends ICExecutor {
	@Override
	public ICState create(SignLocation loc, SignText text, String owner, ICParser signParser) throws MessageableException {
		CountState state = new CountState();

		IntegerCapture count = new IntegerCapture("count", IntegerCapture.getMinConstraint(1), null);
		IntegerCapture max = new IntegerCapture("count", IntegerCapture.getMinConstraint(1), true);

		ActionCapture left = new ActionCapture(ActionOperator.SET, 0);
		ActionCapture center = new ActionCapture(ActionOperator.ADD, 1);
		ActionCapture right = new ActionCapture(ActionOperator.SET, 0);

		signParser.getLine(1).add(count).add(max);
		signParser.getLine(2).add(left).add(center).add(right);

		state.setCountTarget(count.getResult());

		if (max.getResult() != null) {
			state.setHasMax(true);
			state.setMaxCount(max.getResult());
		} else
			state.setHasMax(false);

		state.setAction1(left.getResult());
		state.setAction2(center.getResult());
		state.setAction3(right.getResult());

		return state;
	}

	@Override
	public void trigger(ICStub ic, EventArgs event) {
		CountState state = ic.getState();

		for (int i = 0; i < 3; i++)
			if (ic.getInput(i) == Current.RISING)
				state.act(i);

		ic.setOutput(0, state.getOutput());
	}

	public static class ActionCapture extends ArgCapture<CountAction> {
		public ActionCapture() {
			this(null);
		}

		public ActionCapture(ActionOperator defOperator, int defOperand) {
			this(new CountAction(defOperator, defOperand));
		}

		public ActionCapture(CountAction def) {
			super(def, 1);
		}

		@Override
		public ICFormatException getBlankException(int line) {
			// TODO: This should be revamped if I ever consider actions with no
			// default. I don't like its phrasing.
			return new ICFormatException("^eYou must specify an action.", line);
		}

		@Override
		public ICFormatException getInvalidException(String input, int line) {

			return new ICFormatException("^eI could not understand ^u" + input
					+ " ^eas a count action. You must specify an operator ^g(^c+^g, ^c-^g, or ^c=^g) ^eand an ^ninteger operand^e.");
		}

		@Override
		public String getTextFor(Object object) {
			return object.toString();
		}

		private static Pattern argPattern = Pattern.compile("^([-+=])([0-9]+)$");

		@Override
		public CountAction parse(String arg, int line, SignLocation loc, String owner) throws MessageableException {
			Matcher argParser = argPattern.matcher(arg);

			if (!argParser.matches())
				throw this.getInvalidException(arg, line);

			ActionOperator op = ActionOperator.getByChar(argParser.group(1).charAt(0));
			int operand = Integer.parseInt(argParser.group(2));

			return this.setResult(new CountAction(op, operand));
		}
	}

	public static class CountAction {
		private ActionOperator operator;
		private int operand;

		public CountAction() {

		}

		public CountAction(ActionOperator operator, int operand) {
			this.setOperand(operand);
			this.setOperator(operator);
		}

		/**
		 * @return the operator
		 */
		public ActionOperator getOperator() {
			return this.operator;
		}

		/**
		 * @param operator
		 *            the operator to set
		 */
		public void setOperator(ActionOperator operator) {
			this.operator = operator;
		}

		/**
		 * @return the operand
		 */
		public int getOperand() {
			return this.operand;
		}

		/**
		 * @param operand
		 *            the operand to set
		 */
		public void setOperand(int operand) {
			this.operand = operand;
		}

		public void actOn(CountState state) {
			switch (this.getOperator()) {
			case ADD:
				state.setCount(state.getCount() + this.operand);
				break;
			case SUB:
				state.setCount(state.getCount() - this.operand);
				break;
			case SET:
				state.setCount(this.operand);
				break;
			}
		}

		@Override
		public String toString() {
			return "" + this.getOperator().getCharacter() + this.getOperand();
		}
	}

	public static enum ActionOperator {
		ADD('+'), SUB('-'), SET('=');

		private final char ch;

		private ActionOperator(char ch) {
			this.ch = ch;
		}

		public char getCharacter() {
			return this.ch;
		}

		public static ActionOperator getByChar(char ch) {
			for (ActionOperator op : ActionOperator.values())
				if (ch == op.getCharacter())
					return op;
			return null;
		}
	}

	public static class CountState extends ICState {
		private int countTarget;
		private int maxCount;

		private boolean hasMax;

		private int count;

		private CountAction action1, action2, action3;

		/**
		 * @return the countTarget
		 */
		public int getCountTarget() {
			return this.countTarget;
		}

		/**
		 * @param countTarget
		 *            the countTarget to set
		 */
		public void setCountTarget(int countTarget) {
			this.countTarget = countTarget;
			this.dirty();
		}

		/**
		 * @return the hasMax
		 */
		public boolean getHasMax() {
			return this.hasMax;
		}

		/**
		 * @param hasMax
		 *            the hasMax to set
		 */
		public void setHasMax(boolean hasMax) {
			this.hasMax = hasMax;
			this.dirty();
		}

		/**
		 * @return the maxCount
		 */
		public int getMaxCount() {
			return this.maxCount;
		}

		/**
		 * @param maxCount
		 *            the maxCount to set
		 */
		public void setMaxCount(int maxCount) {
			this.maxCount = maxCount;
			this.dirty();
		}

		/**
		 * @return the count
		 */
		public int getCount() {
			return this.count;
		}

		/**
		 * @param count
		 *            the count to set
		 */
		public void setCount(int count) {
			if (this.getHasMax()) {
				if (count < 0)
					count = 0;
				count %= this.getMaxCount();
			}

			int lastCount = this.count;
			this.count = count;
			this.dirty();
			this.debug("[" + count + "]\nPrev: [" + lastCount + "]");
		}

		/**
		 * @return the action1
		 */
		public CountAction getAction1() {
			return this.action1;
		}

		/**
		 * @param action1
		 *            the action1 to set
		 */
		public void setAction1(CountAction action1) {
			this.action1 = action1;
			this.dirty();
		}

		/**
		 * @return the action2
		 */
		public CountAction getAction2() {
			return this.action2;
		}

		/**
		 * @param action2
		 *            the action2 to set
		 */
		public void setAction2(CountAction action2) {
			this.action2 = action2;
			this.dirty();
		}

		/**
		 * @return the action3
		 */
		public CountAction getAction3() {
			return this.action3;
		}

		/**
		 * @param action3
		 *            the action3 to set
		 */
		public void setAction3(CountAction action3) {
			this.action3 = action3;
			this.dirty();
		}

		private void act(int index) {
			switch (index) {
			case 0:
				this.action1.actOn(this);
				return;
			case 1:
				this.action2.actOn(this);
				return;
			case 2:
				this.action3.actOn(this);
				return;
			}
		}

		public boolean getOutput() {
			return ((this.count == 0) && this.hasMax) || (this.count == this.getCountTarget());
		}
	}
}
