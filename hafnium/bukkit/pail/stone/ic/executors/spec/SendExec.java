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
import hafnium.bukkit.pail.stone.parsing.ICParser;
import hafnium.bukkit.pail.stone.parsing.args.EnumCapture;
import hafnium.bukkit.pail.stone.parsing.args.StringCapture;
import hafnium.bukkit.pail.stone.trigger.EventArgs;
import hafnium.bukkit.pail.stone.trigger.events.TransmissionEvent;
import hafnium.bukkit.pail.stone.trigger.events.TransmissionEvent.Mode;
import hafnium.bukkit.pail.util.sign.SignLocation;
import hafnium.bukkit.pail.util.sign.SignText;
import hafnium.bukkit.pail.util.text.MessageableException;

public class SendExec extends ICExecutor {
	@Override
	public ICState create(SignLocation loc, SignText text, String owner, ICParser signParser) throws MessageableException {
		SendState state = new SendState();

		StringCapture channel = new StringCapture("transmit channel", null);
		EnumCapture<Mode> mode = new EnumCapture<Mode>("transmit mode", Mode.class, Mode.SET);

		signParser.getLine(1).add(channel);
		signParser.getLine(2).add(mode);

		state.setChannel(channel.getResult());
		state.setMode(mode.getResult());

		return state;
	}

	@Override
	public ICState legacyCreate(SignLocation loc, SignText text, String owner, String data, ICParser signParser) throws MessageableException {
		RewriteHelper.updateSendRecv(text, true);
		return this.create(loc, text, owner, new ICParser(loc, text, owner));
	}

	@Override
	public void trigger(ICStub ic, EventArgs event) {
		SendState state = ic.getState();
		ic.sendEvent(new TransmissionEvent(state.getChannel(), state.getOwnerName(), ic.getInput(0), state.getMode()));
	}

	public static class SendState extends ICState {
		private String channel;
		private Mode mode;

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
		 * @return the mode
		 */
		public Mode getMode() {
			return this.mode;
		}

		/**
		 * @param mode
		 *            the mode to set
		 */
		public void setMode(Mode mode) {
			this.mode = mode;
			this.dirty();
		}

	}
}
