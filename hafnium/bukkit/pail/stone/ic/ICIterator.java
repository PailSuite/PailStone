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

import java.util.ArrayList;
import java.util.Iterator;

public class ICIterator implements Iterator<ICStub> {
	private ArrayList<ICStub> ics;
	private int pos = 0;

	public ICIterator() {
		this.ics = null;
	}

	public void repopulate() {
		this.ics = hafnium.bukkit.pail.stone.PailStone.getInstance().getRegistry().getCopy();
		this.pos = 0;
	}

	@Override
	public boolean hasNext() {
		return (this.ics != null) && (this.pos < this.ics.size());
	}

	@Override
	public ICStub next() {
		if (this.hasNext())
			return this.ics.get(this.pos++);
		return null;
	}

	public ICStub current() {
		return this.ics.get(this.pos - 1);
	}

	@Override
	public void remove() {
		this.remove(null);
	}

	public void remove(String reason) {
		this.ics.get(this.pos - 1).delete(reason);
	}
}
