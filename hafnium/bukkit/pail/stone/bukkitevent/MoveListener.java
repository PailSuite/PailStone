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
package hafnium.bukkit.pail.stone.bukkitevent;

import hafnium.bukkit.pail.pipe.events.MajorMoveEvent;
import hafnium.bukkit.pail.stone.trigger.HookManager;
import hafnium.bukkit.pail.stone.trigger.events.MovementEvent;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MoveListener implements Listener {
	private final HookManager manager;

	public MoveListener(hafnium.bukkit.pail.stone.PailStone pailStone) {
		pailStone.getServer().getPluginManager().registerEvents(this, pailStone);
		this.manager = pailStone.getHookManager();
	}

	@EventHandler
	public void onMajorMove(MajorMoveEvent event) {
		this.manager.trigger(new MovementEvent(event));
	}
}
