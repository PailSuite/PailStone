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

import org.bukkit.command.CommandSender;

public class TriggerEvent extends EventArgs {
	private final String trigger;
	private final CommandSender sender;
	private boolean handled;

	public TriggerEvent(String trigger, CommandSender sender) {
		this.trigger = trigger;
		this.sender = sender;
		this.handled = false;
	}

	/**
	 * @return true if has been handled
	 */
	public boolean isHandled() {
		return this.handled;
	}

	/**
	 * @param handled
	 *            the handled to set
	 */
	public void setHandled(boolean handled) {
		this.handled = handled;
	}

	/**
	 * @return the trigger
	 */
	public String getTrigger() {
		return this.trigger;
	}

	/**
	 * @return the sender
	 */
	public CommandSender getSender() {
		return this.sender;
	}

	@Override
	public EventType getType() {
		return EventType.TRIGGER;
	}

	@Override
	public boolean isNotable() {
		return true;
	}
}
