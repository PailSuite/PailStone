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
import hafnium.bukkit.pail.stone.trigger.events.InteractEvent;
import hafnium.bukkit.pail.util.AliasedEnum;
import hafnium.bukkit.pail.util.Region;

import org.bukkit.event.block.Action;

public class InteractHook extends EventHook {

	private final ICStub stub;
	private final Region region;
	private final ActionFilter filter;

	public InteractHook(ICStub stub, Region region, ActionFilter filter) {
		this.stub = stub;
		this.region = region;
		this.filter = filter;
	}

	@Override
	public boolean isApplicable(EventArgs event) {
		if (!(event.getType() == EventType.INTERACT))
			return false;

		InteractEvent iEvent = ((InteractEvent) event);

		return this.filter.matches(iEvent.getClickAction()) && this.region.contains(iEvent.getClickedBlock().getLocation());
	}

	@Override
	public void trigger(EventArgs event) {
		this.stub.trigger(event);
	}

	@Override
	public EventType getType() {
		return EventType.INTERACT;
	}

	public static enum ActionFilter implements AliasedEnum {
		RIGHT("r", "secondary"), LEFT("l", "primary"), BOTH("*", "any", "either", "b");

		private String[] aliases;

		private ActionFilter(String... aliases) {
			this.aliases = aliases;
		}

		@Override
		public String[] getAliases() {
			return this.aliases;
		}

		public boolean matches(Action a) {
			switch (this) {
			case RIGHT:
				return a == Action.RIGHT_CLICK_BLOCK;
			case LEFT:
				return a == Action.LEFT_CLICK_BLOCK;
			case BOTH:
				return (a == Action.RIGHT_CLICK_BLOCK) || (a == Action.LEFT_CLICK_BLOCK);
			}

			return false;
		}
	}
}
