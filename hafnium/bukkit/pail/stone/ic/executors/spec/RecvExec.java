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
import hafnium.bukkit.pail.stone.ic.RewriteHelper;
import hafnium.bukkit.pail.stone.ic.states.ICState;
import hafnium.bukkit.pail.stone.ic.states.PulsingICState;
import hafnium.bukkit.pail.stone.parsing.ICParser;
import hafnium.bukkit.pail.stone.parsing.args.PlayerFilterCapture;
import hafnium.bukkit.pail.stone.parsing.args.StringCapture;
import hafnium.bukkit.pail.stone.trigger.EventArgs;
import hafnium.bukkit.pail.stone.trigger.events.TransmissionEvent;
import hafnium.bukkit.pail.stone.trigger.events.TransmissionEvent.Mode;
import hafnium.bukkit.pail.stone.trigger.hooks.TransmissionHook;
import hafnium.bukkit.pail.util.PlayerFilter;
import hafnium.bukkit.pail.util.sign.SignLocation;
import hafnium.bukkit.pail.util.sign.SignText;
import hafnium.bukkit.pail.util.text.MessageableException;

public class RecvExec extends ICExecutor {

	@Override
	public ICState create(SignLocation loc, SignText text, String owner, ICParser signParser) throws MessageableException {
		RecvState state = new RecvState();

		StringCapture channel = new StringCapture("transmit channel", null);
		PlayerFilterCapture filter = new PlayerFilterCapture(new PlayerFilter.LiteralPlayer(owner), false, false);

		signParser.getLine(1).add(channel);
		signParser.getLine(2).add(filter);

		state.setChannel(channel.getResult());
		state.setFilter(filter.getResult());

		return state;
	}

	@Override
	public ICState legacyCreate(SignLocation loc, SignText text, String owner, String data, ICParser signParser) throws MessageableException {
		RewriteHelper.updateSendRecv(text, false);
		return this.create(loc, text, owner, new ICParser(loc, text, owner));
	}

	@Override
	public void initialize(ICStub ic) {
		ic.registerHook(new TransmissionHook(ic, ((RecvState) ic.getState()).getChannel()));
	}

	@Override
	public void trigger(ICStub ic, EventArgs anonEvent) {
		RecvState state = ic.getState();

		if (anonEvent.getType() == hafnium.bukkit.pail.stone.trigger.EventType.TICK) {
			if (state.getTicksLeft() <= 0) {
				ic.stopTicking();
				ic.setOutput(0, false);
			}
		} else {
			TransmissionEvent event = (TransmissionEvent) anonEvent;

			if (!state.getFilter().matches(event.getSender(), state.getOwnerName()))
				return;

			state.debug(event.getMode() + (event.getMode() == Mode.SET ? " " + event.getState() : "") + "\nfrom\n" + event.getSender());

			switch (event.getMode()) {
			case SET:
				ic.setOutput(0, event.getState().asBoolean());
				return;
			case PULSE:
				state.doStartPulse();
				ic.startTicking();
				ic.setOutput(0, true);
				return;
			case TOGGLE:
				ic.setOutput(0, !ic.getOutput(0).asBoolean());
				return;
			}
		}
	}

	public static class RecvState extends PulsingICState {
		private String channel;
		private PlayerFilter filter;

		/**
		 * @return the channel
		 */
		public String getChannel() {
			return this.channel;
		}

		/**
		 * @param channel
		 *            the channel to set
		 */
		public void setChannel(String channel) {
			this.channel = channel;
			this.dirty();
		}

		/**
		 * @return the filter
		 */
		public PlayerFilter getFilter() {
			return this.filter;
		}

		/**
		 * @param filter
		 *            the filter to set
		 */
		public void setFilter(PlayerFilter filter) {
			this.filter = filter;
			this.dirty();
		}
	}
}
