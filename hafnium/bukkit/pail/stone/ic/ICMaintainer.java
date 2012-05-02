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
package hafnium.bukkit.pail.stone.ic;

import java.io.IOException;

public class ICMaintainer implements Runnable {
	private final ICIterator i;

	public ICMaintainer() {
		this.i = new ICIterator();
	}

	@Override
	public void run() {
		if (this.i.hasNext()) {
			ICStub next = this.i.next();

			if (!next.isValid())
				this.i.remove("Sign Invalidated");
			else
				this.maintainState(next);
		} else
			this.i.repopulate();
	}

	public void maintainState(ICStub stub) {
		if (stub.isLoaded())
			if (!stub.isActive())
				stub.unload(false);
			else if (stub.quietlyGetState().isDirty())
				try {
					stub.quietlyGetState().save();
				} catch (IOException e) {
					hafnium.bukkit.pail.stone.PailStone.getInstance().error("Could not save state for " + stub, e);
				}
	}

	public void wrapUp() {
		this.i.repopulate();
		while (this.i.hasNext())
			this.i.next().unload(true);
	}
}
