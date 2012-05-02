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
import hafnium.bukkit.pail.stone.ic.ICStub;
import hafnium.bukkit.pail.stone.ic.states.ICState;
import hafnium.bukkit.pail.stone.parsing.ICParser;
import hafnium.bukkit.pail.stone.parsing.args.EnumCapture;
import hafnium.bukkit.pail.stone.trigger.EventArgs;
import hafnium.bukkit.pail.util.redstone.Current;
import hafnium.bukkit.pail.util.sign.SignLocation;
import hafnium.bukkit.pail.util.sign.SignText;
import hafnium.bukkit.pail.util.text.MessageableException;

import java.util.ArrayList;

public class LogicExec extends ICExecutor {

	@Override
	public ICState create(SignLocation loc, SignText text, String owner, ICParser parser) throws MessageableException {
		LogicState state = new LogicState();

		EnumCapture<LogicOp> logicOp = new EnumCapture<LogicOp>("logic operation type", LogicOp.class, null);

		parser.getLine(1).add(logicOp);

		state.setOperation(logicOp.getResult());

		return state;
	}

	// TODO: Reduce the cyclomatic complexity of this method.

	@Override
	public void trigger(ICStub ic, EventArgs event) {
		ArrayList<Boolean> currents = new ArrayList<Boolean>();

		for (int i = 0; i < ic.getController().getInputCount(); i++) {
			Current c = ic.getController().getInput(i);
			if ((c != Current.DISCONN) && (c != Current.UNLOADED))
				currents.add(c.asBoolean());
		}

		LogicOp op = ((LogicState) ic.getState()).getOperation();

		boolean on, off;

		switch (op) {
		case AND:
			on = true;

			for (boolean c : currents)
				if (c != true)
					on = false;

			ic.getController().setOutput(0, on);
			break;

		case OR:
			off = true;

			for (boolean c : currents)
				if (c == true)
					off = false;

			ic.getController().setOutput(0, !off);
			break;

		case NOR:
			on = true;

			for (boolean c : currents)
				if (c == true)
					on = false;

			ic.getController().setOutput(0, on);
			break;

		case NAND:
			off = true;

			for (boolean c : currents)
				if (c == false)
					off = false;

			ic.getController().setOutput(0, !off);
			break;

		case XOR:
			boolean res = false;

			for (int i = 0; i < currents.size(); i++)
				if (i == 0)
					res = currents.get(i);
				else
					res = res ^ currents.get(i);

			ic.getController().setOutput(0, res);
			break;

		case XNOR:
			boolean ftrue = false;
			boolean ffalse = false;

			for (boolean c : currents)
				if (c)
					ftrue = true;
				else
					ffalse = true;

			ic.getController().setOutput(0, ftrue ^ ffalse);
		}
	}

	public static enum LogicOp {
		AND, OR, NOR, NAND, XOR, XNOR;
	}

	public static class LogicState extends ICState {
		private LogicOp operation;

		/**
		 * @return the operation
		 */
		public LogicOp getOperation() {
			return this.operation;
		}

		/**
		 * @param operation
		 *            the operation to set
		 */
		public void setOperation(LogicOp operation) {
			this.operation = operation;
			this.dirty();
		}
	}
}
