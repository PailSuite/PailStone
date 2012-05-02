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
package hafnium.bukkit.pail.stone.trigger.events;

import hafnium.bukkit.pail.stone.trigger.EventArgs;
import hafnium.bukkit.pail.stone.trigger.EventType;
import hafnium.bukkit.pail.util.redstone.Current;

public class TransmissionEvent extends EventArgs {
	private final String channel;
	private final String sender;
	private final Current state;
	private final Mode mode;

	/**
	 * @param band
	 * @param channel
	 * @param state
	 * @param mode
	 */
	public TransmissionEvent(String channel, String sender, Current state, Mode mode) {
		this.channel = channel;
		this.sender = sender;
		this.state = state;
		this.mode = mode;
	}

	/**
	 * @return the channel
	 */
	public String getChannel() {
		return this.channel;
	}

	public String getSender() {
		return this.sender;
	}

	/**
	 * @return the state
	 */
	public Current getState() {
		return this.state;
	}

	/**
	 * @return the mode
	 */
	public Mode getMode() {
		return this.mode;
	}

	public static enum Mode {
		SET, PULSE, TOGGLE;
	}

	@Override
	public EventType getType() {
		return EventType.TRANSMISSION;
	}

	@Override
	public boolean isNotable() {
		if (this.mode == Mode.SET)
			return true;

		if (this.state == Current.RISING)
			return true;

		return false;
	}
}
