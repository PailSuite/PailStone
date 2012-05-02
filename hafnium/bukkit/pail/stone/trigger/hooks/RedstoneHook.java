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
package hafnium.bukkit.pail.stone.trigger.hooks;

import hafnium.bukkit.pail.stone.ic.ICStub;
import hafnium.bukkit.pail.stone.ic.controllers.Input;
import hafnium.bukkit.pail.stone.trigger.EventArgs;
import hafnium.bukkit.pail.stone.trigger.EventHook;
import hafnium.bukkit.pail.stone.trigger.EventType;
import hafnium.bukkit.pail.stone.trigger.events.RedstoneEvent;
import hafnium.bukkit.pail.util.BlockLocation;

public class RedstoneHook extends EventHook {
	private final ICStub stub;
	private final BlockLocation loc;

	public RedstoneHook(ICStub stub, Input input) {
		this.stub = stub;
		this.loc = input.getLocation();
	}

	@Override
	public boolean isApplicable(EventArgs event) {
		if (event.getType() != EventType.REDSTONE)
			return false;

		RedstoneEvent rse = (RedstoneEvent) event;

		if (!this.loc.locates(rse.getWrappedEvent().getBlock()))
			return false;

		return true;
	}

	@Override
	public void trigger(EventArgs event) {
		this.stub.trigger(event);
	}

	@Override
	public EventType getType() {
		return EventType.REDSTONE;
	}
}
