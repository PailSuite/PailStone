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
import hafnium.bukkit.pail.stone.trigger.EventType;
import hafnium.bukkit.pail.util.ArgumentConstraints;
import hafnium.bukkit.pail.util.redstone.Current;
import hafnium.bukkit.pail.util.sign.SignLocation;
import hafnium.bukkit.pail.util.sign.SignText;
import hafnium.bukkit.pail.util.text.MessageableException;

public class DelayExec extends ICExecutor {

	@Override
	public ICState create(SignLocation loc, SignText text, String owner, ICParser signParser) throws MessageableException {
		DelayState state = new DelayState();

		IntegerCapture ticks = new IntegerCapture("ticks", new ArgumentConstraints.IntMinConstraint(1), 8);

		signParser.getLine(1).add(ticks);

		state.setTicks(ticks.getResult());

		return state;
	}

	@Override
	public void trigger(ICStub ic, EventArgs event) {
		DelayState state = ic.getState();

		if (event.getType() == EventType.REDSTONE) {
			state.trigger(ic.getInput(0));
			ic.startTicking();
		}

		if (event.getType() == EventType.TICK) {
			boolean out = state.getOutput();
			if (out != ic.getOutput(0).asBoolean())
				ic.setOutput(0, out);

			if (state.isDone())
				ic.stopTicking();
		}
	}

	public static class DelayState extends ICState {
		private int ticks;
		private long lastRising = 0;
		private long lastFalling = 1;
		private boolean newlyLoaded = true;

		/**
		 * @return the ticks
		 */
		public int getTicks() {
			return this.ticks;
		}

		/**
		 * @param ticks
		 *            the ticks to set
		 */
		public void setTicks(int ticks) {
			this.ticks = ticks;
			this.dirty();
		}

		/**
		 * @return the lastRising
		 */
		public long getLastRising() {
			return this.lastRising;
		}

		/**
		 * @param lastRising
		 *            the lastRising to set
		 */
		public void setLastRising(long lastRising) {
			this.lastRising = lastRising;
			this.dirty();
		}

		/**
		 * @return the lastFalling
		 */
		public long getLastFalling() {
			return this.lastFalling;
		}

		/**
		 * @param lastFalling
		 *            the lastFalling to set
		 */
		public void setLastFalling(long lastFalling) {
			this.lastFalling = lastFalling;
			this.dirty();
		}

		/**
		 * Sanitizes times if required.
		 */
		private void sanitize() {
			if (this.newlyLoaded && ((this.getLastFalling() > PailPipe.getServerTime()) || (this.getLastRising() > PailPipe.getServerTime()))) {
				this.setLastRising(0);
				this.setLastFalling(1);
			}

			this.newlyLoaded = false;
		}

		public void trigger(Current c) {
			if (c == Current.RISING)
				this.setLastRising(PailPipe.getServerTime());

			if (c == Current.FALLING)
				this.setLastFalling(PailPipe.getServerTime());
		}

		public boolean getOutput() {
			this.sanitize();

			long time = PailPipe.getServerTime();

			long riseEvent = this.getLastRising() + this.getTicks();
			long fallEvent = this.getLastFalling() + this.getTicks();

			if ((riseEvent < time) && ((fallEvent > time) || (fallEvent < riseEvent)))
				return true;

			if ((fallEvent < time) && ((riseEvent > time) || (riseEvent < fallEvent)))
				return false;

			return false;
		}

		public boolean isDone() {
			this.sanitize();

			long time = PailPipe.getServerTime();

			long riseEvent = this.getLastRising() + this.getTicks();
			long fallEvent = this.getLastFalling() + this.getTicks();

			return ((time > riseEvent) && (time > fallEvent));
		}

		@Override
		public void wrapUp(ICStub stub) {
			super.wrapUp(stub);

			this.setLastRising(this.getLastRising() - PailPipe.getServerTime());
			this.setLastFalling(this.getLastFalling() - PailPipe.getServerTime());
		}
	}
}
