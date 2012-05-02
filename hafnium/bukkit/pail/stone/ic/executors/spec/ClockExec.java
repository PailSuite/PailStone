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

import hafnium.bukkit.pail.pipe.PailPipe;
import hafnium.bukkit.pail.stone.ic.ICExecutor;
import hafnium.bukkit.pail.stone.ic.ICStub;
import hafnium.bukkit.pail.stone.ic.states.ICState;
import hafnium.bukkit.pail.stone.parsing.ICParser;
import hafnium.bukkit.pail.stone.parsing.args.IntegerCapture;
import hafnium.bukkit.pail.stone.trigger.EventArgs;
import hafnium.bukkit.pail.util.sign.SignLocation;
import hafnium.bukkit.pail.util.sign.SignText;
import hafnium.bukkit.pail.util.text.MessageableException;

public class ClockExec extends ICExecutor {

	@Override
	public ICState create(SignLocation loc, SignText text, String owner, ICParser parser) throws MessageableException {
		ClockState state = new ClockState();

		IntegerCapture on, off;
		on = new IntegerCapture("on duration", IntegerCapture.getMinConstraint(1), 10);
		off = new IntegerCapture("off duration", IntegerCapture.getMinConstraint(1), 10);

		parser.getLine(1).add(on);
		parser.getLine(2).add(off);

		state.setOnDuration(on.getResult());
		state.setOffDuration(off.getResult());

		return state;
	}

	@Override
	public void trigger(ICStub ic, EventArgs event) {
		ClockState state = ic.getState();

		switch (event.getType()) {
		case REDSTONE:
			if (ic.getController().getInput(0).asBoolean()) {
				state.startCycle();
				ic.startTicking();
			} else
				ic.stopTicking();
			break;
		case TICK:
			ic.getController().setOutput(0, state.findState());
			break;
		}
	}

	public static class ClockState extends ICState {
		private int onDuration;
		private int offDuration;
		private long cycleStart;

		/**
		 * @return the onDuration
		 */
		public int getOnDuration() {
			return this.onDuration;
		}

		/**
		 * @param onDuration
		 *            the onDuration to set
		 */
		public void setOnDuration(int onDuration) {
			this.onDuration = onDuration;
			this.dirty();
		}

		/**
		 * @return the offDuration
		 */
		public int getOffDuration() {
			return this.offDuration;
		}

		/**
		 * @param offDuration
		 *            the offDuration to set
		 */
		public void setOffDuration(int offDuration) {
			this.offDuration = offDuration;
			this.dirty();
		}

		/**
		 * @return the cycleStart
		 */
		public long getCycleStart() {
			return this.cycleStart;
		}

		/**
		 * @param cycleStart
		 *            the cycleStart to set
		 */
		public void setCycleStart(long cycleStart) {
			this.cycleStart = cycleStart;
			this.dirty();
		}

		public void startCycle() {
			this.setCycleStart(PailPipe.getServerTime());
		}

		/**
		 * Finds the current IC state.
		 * 
		 * @return
		 */
		public boolean findState() {
			int cyclePos = (int) ((PailPipe.getServerTime() - this.getCycleStart()) % (this.getOnDuration() + this.getOffDuration()));
			if (cyclePos < this.getOnDuration())
				return true;

			return false;
		}

		@Override
		public void wrapUp(ICStub stub) {
			super.wrapUp(stub);
			this.setCycleStart(this.getCycleStart() - PailPipe.getServerTime());
		}
	}
}
