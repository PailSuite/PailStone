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
import hafnium.bukkit.pail.stone.trigger.EventArgs;
import hafnium.bukkit.pail.stone.trigger.EventHook;
import hafnium.bukkit.pail.stone.trigger.EventType;
import hafnium.bukkit.pail.stone.trigger.events.MovementEvent;
import hafnium.bukkit.pail.util.Region;

public class MovementHook extends EventHook {
	private final Region region;
	private final ICStub stub;

	public MovementHook(ICStub stub, Region region) {
		this.region = region;
		this.stub = stub;
	}

	@Override
	public boolean isApplicable(EventArgs gEvent) {
		if (gEvent.getType() != EventType.MOVEMENT)
			return false;

		if (this.region == null)
			return true;

		MovementEvent event = (MovementEvent) gEvent;

		if (this.region.contains(event.getFrom()))
			return true;

		if (this.region.contains(event.getTo()))
			return true;

		return false;
	}

	@Override
	public void trigger(EventArgs event) {
		this.stub.trigger(event);
	}

	@Override
	public EventType getType() {
		return EventType.MOVEMENT;
	}

}
