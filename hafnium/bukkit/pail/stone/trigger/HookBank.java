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
package hafnium.bukkit.pail.stone.trigger;

import java.util.ArrayList;
import java.util.Iterator;

public class HookBank {
	private final ArrayList<EventHook> hooks;
	private final ArrayList<EventHook> deregisterQueue;
	private final ArrayList<EventHook> registerQueue;

	private boolean iterating = false;

	public HookBank() {
		this.hooks = new ArrayList<EventHook>();
		this.deregisterQueue = new ArrayList<EventHook>();
		this.registerQueue = new ArrayList<EventHook>();
	}

	/*
	 * Public functionality:
	 */

	public void register(EventHook hook) {
		if (this.iterating)
			this.registerQueue.add(hook);
		else
			this.hooks.add(hook);
	}

	public void deregister(EventHook hook) {
		if (this.iterating)
			this.deregisterQueue.add(hook);
		else
			this.hooks.remove(hook);
	}

	private void startIterating() {
		this.iterating = true;
	}

	private void stopIterating() {
		this.iterating = false;

		for (EventHook hook : this.deregisterQueue)
			this.deregister(hook);

		for (EventHook hook : this.registerQueue)
			this.register(hook);

		this.deregisterQueue.clear();
		this.registerQueue.clear();
	}

	public void trigger(EventArgs event) {
		if (!event.isNotable())
			return;

		this.startIterating();

		Iterator<EventHook> i = this.hooks.iterator();
		while (i.hasNext()) {
			EventHook hook = i.next();
			if (hook.isApplicable(event))
				hook.trigger(event);
		}

		this.stopIterating();
	}
}
