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
import hafnium.bukkit.pail.stone.ic.states.PulsingICState;
import hafnium.bukkit.pail.stone.parsing.ICParser;
import hafnium.bukkit.pail.stone.parsing.args.EnumCapture;
import hafnium.bukkit.pail.stone.parsing.args.IntegerCapture;
import hafnium.bukkit.pail.stone.trigger.EventArgs;
import hafnium.bukkit.pail.util.Edge;
import hafnium.bukkit.pail.util.StaticCurrent;
import hafnium.bukkit.pail.util.sign.SignLocation;
import hafnium.bukkit.pail.util.sign.SignText;
import hafnium.bukkit.pail.util.text.MessageableException;

public class PulseExec extends ICExecutor {

	@Override
	public ICState create(SignLocation loc, SignText text, String owner, ICParser parser) throws MessageableException {
		PulseState state = new PulseState();

		EnumCapture<Edge> edge = new EnumCapture<Edge>("edge", Edge.class, Edge.RISING);
		IntegerCapture duration = new IntegerCapture("pulse duration", IntegerCapture.getMinConstraint(1), 10);
		EnumCapture<StaticCurrent> pulse = new EnumCapture<StaticCurrent>("pulse", StaticCurrent.class, StaticCurrent.HIGH);

		parser.getLine(1).add(edge);
		parser.getLine(2).add(duration);
		parser.getLine(3).add(pulse);

		state.setEdge(edge.getResult());
		state.setPulseDuration(duration.getResult());
		state.setPulseMode(pulse.getResult());

		return state;
	}

	@Override
	public void initialize(ICStub ic) {
		if (((PulseState) ic.getState()).getTicksLeft() > 0)
			ic.startTicking();
	}

	@Override
	public void trigger(ICStub ic, EventArgs event) {
		PulseState state = ic.getState();

		switch (event.getType()) {
		case REDSTONE:
			if (state.getEdge().isMatch(ic.getInput(0))) {
				state.doStartPulse();
				ic.startTicking();
				ic.setOutput(0, state.getPulseMode().asBoolean());
			}
			break;
		case TICK:
			if (state.getTicksLeft() <= 0) {
				ic.stopTicking();
				ic.setOutput(0, !state.getPulseMode().asBoolean());
			}

			break;
		}
	}

	public static class PulseState extends PulsingICState {
		private Edge edge = Edge.RISING;
		private StaticCurrent pulseMode = StaticCurrent.HIGH;

		/**
		 * @return the edge
		 */
		public Edge getEdge() {
			return this.edge;
		}

		/**
		 * @param edge
		 *            the edge to set
		 */
		public void setEdge(Edge edge) {
			this.edge = edge;
			this.dirty();
		}

		/**
		 * @return the pulseMode
		 */
		public StaticCurrent getPulseMode() {
			return this.pulseMode;
		}

		/**
		 * @param pulseMode
		 *            the pulseMode to set
		 */
		public void setPulseMode(StaticCurrent pulseMode) {
			this.pulseMode = pulseMode;
		}

	}
}
