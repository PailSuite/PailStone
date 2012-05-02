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

import hafnium.bukkit.pail.pipe.events.MajorMoveEvent;
import hafnium.bukkit.pail.stone.trigger.EventArgs;
import hafnium.bukkit.pail.stone.trigger.EventType;
import hafnium.bukkit.pail.util.BlockLocation;

import org.bukkit.entity.Player;

public class MovementEvent extends EventArgs {
	private final BlockLocation from, to;
	private final Player player;

	public MovementEvent(MajorMoveEvent event) {
		this.from = new BlockLocation(event.getFrom());
		this.to = new BlockLocation(event.getTo());
		this.player = event.getPlayer();
	}

	/**
	 * @return the from
	 */
	public BlockLocation getFrom() {
		return this.from;
	}

	/**
	 * @return the to
	 */
	public BlockLocation getTo() {
		return this.to;
	}

	public Player getPlayer() {
		return this.player;
	}

	@Override
	public EventType getType() {
		return EventType.MOVEMENT;
	}

	@Override
	public boolean isNotable() {
		return true;
	}
}
