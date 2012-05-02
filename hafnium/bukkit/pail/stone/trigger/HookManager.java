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

import java.util.EnumMap;

public class HookManager {
	private final EnumMap<EventType, HookBank> banks;

	public HookManager() {
		this.banks = new EnumMap<EventType, HookBank>(EventType.class);
		this.init();
	}

	private void init() {
		for (EventType type : EventType.values())
			this.banks.put(type, new HookBank());
	}

	public HookBank getFor(EventType type) {
		return this.banks.get(type);
	}

	public void deregister(EventHook hook) {
		for (HookBank bank : this.banks.values())
			bank.deregister(hook);
	}

	public void register(EventHook hook) {
		this.getFor(hook.getType()).register(hook);
	}

	public void trigger(EventArgs event) {
		this.getFor(event.getType()).trigger(event);
	}
}
