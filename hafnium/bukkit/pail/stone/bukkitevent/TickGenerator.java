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

import hafnium.bukkit.pail.pipe.PailPipe;
import hafnium.bukkit.pail.stone.PailStone;
import hafnium.bukkit.pail.stone.trigger.events.SecondEvent;
import hafnium.bukkit.pail.stone.trigger.events.TickEvent;

public class TickGenerator implements Runnable {
	public TickGenerator() {
		org.bukkit.Bukkit.getScheduler().scheduleSyncRepeatingTask(PailStone.getInstance(), this, 1, 1);
	}

	@Override
	public void run() {
		TickEvent.tick();

		if (PailPipe.getServerTime() % 20 == 0)
			SecondEvent.tick();
	}
}
