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
import hafnium.bukkit.pail.stone.parsing.args.PlayerFilterCapture;
import hafnium.bukkit.pail.stone.parsing.args.StringCapture;
import hafnium.bukkit.pail.stone.trigger.EventArgs;
import hafnium.bukkit.pail.stone.trigger.events.TriggerEvent;
import hafnium.bukkit.pail.stone.trigger.hooks.TriggerHook;
import hafnium.bukkit.pail.util.PlayerFilter;
import hafnium.bukkit.pail.util.sign.SignLocation;
import hafnium.bukkit.pail.util.sign.SignText;
import hafnium.bukkit.pail.util.text.MessageableException;

public class TriggerExec extends ICExecutor {

	@Override
	public ICState create(SignLocation loc, SignText text, String owner, ICParser signParser) throws MessageableException {
		TriggerState state = new TriggerState();

		StringCapture trigger = new StringCapture("trigger name");
		PlayerFilterCapture filter = new PlayerFilterCapture(new PlayerFilter.LiteralPlayer(owner), true, false);
		EnumCapture<TriggerType> type = new EnumCapture<TriggerType>("trigger action", TriggerType.class, TriggerType.TOGGLE);

		signParser.getLine(1).add(trigger);
		signParser.getLine(2).add(filter);
		signParser.getLine(3).add(type);

		state.setTrigger(trigger.getResult());
		state.setPlayerFilter(filter.getResult());
		state.setTriggerType(type.getResult());

		return state;
	}

	@Override
	public void initialize(ICStub ic) {
		ic.registerHook(new TriggerHook(ic, ((TriggerState) ic.getState()).getTrigger()));
	}

	@Override
	public void trigger(ICStub ic, EventArgs event) {
		TriggerState state = ic.getState();

		switch (event.getType()) {
		case TRIGGER:
			TriggerEvent tEvent = (TriggerEvent) event;

			if (!state.getPlayerFilter().matches(tEvent.getSender(), state.getOwnerName()))
				return;

			tEvent.setHandled(true);

			state.debug("Triggered by\n" + tEvent.getSender().getName());

			if (state.getTriggerType() == TriggerType.TOGGLE)
				ic.setOutput(0, !ic.getOutput(0).asBoolean());
			else {
				state.doStartPulse();
				ic.startTicking();
				ic.setOutput(0, true);
			}

			break;
		case TICK:
			if (state.getTicksLeft() <= 0) {
				ic.stopTicking();
				ic.setOutput(0, false);
			}

			break;
		}
	}

	public static enum TriggerType {
		PULSE, TOGGLE;
	}

	public static class TriggerState extends PulsingICState {
		private String trigger;
		private PlayerFilter playerFilter;
		private TriggerType triggerType = TriggerType.TOGGLE;

		/**
		 * @return the trigger
		 */
		public String getTrigger() {
			return this.trigger;
		}

		/**
		 * @param trigger
		 *            the trigger to set
		 */
		public void setTrigger(String trigger) {
			this.trigger = trigger;
			this.dirty();
		}

		/**
		 * @return the playerFilter
		 */
		public PlayerFilter getPlayerFilter() {
			return this.playerFilter;
		}

		/**
		 * @param playerFilter
		 *            the playerFilter to set
		 */
		public void setPlayerFilter(PlayerFilter playerFilter) {
			this.playerFilter = playerFilter;
			this.dirty();
		}

		/**
		 * @param triggerType
		 *            the triggerType to set
		 */
		public void setTriggerType(TriggerType triggerType) {
			this.triggerType = triggerType;
		}

		/**
		 * @return the triggerType
		 */
		public TriggerType getTriggerType() {
			return this.triggerType;
		}
	}
}
