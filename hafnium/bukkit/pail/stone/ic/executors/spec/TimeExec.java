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
import hafnium.bukkit.pail.stone.parsing.args.IntegerCapture;
import hafnium.bukkit.pail.stone.trigger.EventArgs;
import hafnium.bukkit.pail.stone.trigger.hooks.SecondHook;
import hafnium.bukkit.pail.util.sign.SignLocation;
import hafnium.bukkit.pail.util.sign.SignText;
import hafnium.bukkit.pail.util.text.MessageableException;

public class TimeExec extends ICExecutor {

	@Override
	public ICState create(SignLocation loc, SignText text, String owner, ICParser parser) throws MessageableException {
		TimeState state = new TimeState();

		IntegerCapture start, stop;
		start = new IntegerCapture("start time", IntegerCapture.getRangeConstraint(0, 24000), 0);
		stop = new IntegerCapture("stop time", IntegerCapture.getRangeConstraint(0, 24000), 12000);

		parser.getLine(1).add(start);
		parser.getLine(2).add(stop);

		state.setStartTime(start.getResult());
		state.setStopTime(stop.getResult());

		return state;
	}

	@Override
	public void initialize(ICStub ic) {
		ic.registerHook(new SecondHook(ic));
	}

	@Override
	public void trigger(ICStub ic, EventArgs event) {
		int ctime = (int) (ic.getLocation().getWorld().getTime() % 24000);
		TimeState state = ic.getState();

		ic.getController().setOutput(0, state.findShouldBeOn(ctime));
	}

	public static class TimeState extends ICState {
		private int startTime = 0;
		private int stopTime = 12000;

		private int highTime;
		private int lowTime;

		private boolean betweenState;

		/**
		 * @return the startTime
		 */
		public int getStartTime() {
			return this.startTime;
		}

		/**
		 * @param startTime
		 *            the startTime to set
		 */
		public void setStartTime(int startTime) {
			this.startTime = startTime;
			this.dirty();
			this.updateInternalTimes();
		}

		/**
		 * @return the stopTime
		 */
		public int getStopTime() {
			return this.stopTime;
		}

		/**
		 * @param stopTime
		 *            the stopTime to set
		 */
		public void setStopTime(int stopTime) {
			this.stopTime = stopTime;
			this.dirty();
			this.updateInternalTimes();
		}

		/**
		 * Updates the internal state.
		 */
		private void updateInternalTimes() {
			if (this.getStartTime() < this.getStopTime()) {
				this.lowTime = this.getStartTime();
				this.highTime = this.getStopTime();
				this.betweenState = true;
			} else {
				this.lowTime = this.getStopTime();
				this.highTime = this.getStartTime();
				this.betweenState = false;
			}
		}

		/**
		 * Finds if this state represents an IC that should be on at the
		 * supplied time.
		 * 
		 * @param time
		 * @return
		 */
		public boolean findShouldBeOn(int time) {
			if ((time >= this.lowTime) && (time <= this.highTime))
				return this.betweenState;
			else
				return !this.betweenState;
		}
	}
}
