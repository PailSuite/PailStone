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

import hafnium.bukkit.pail.util.BlockLocation;
import hafnium.bukkit.pail.util.sign.SignLocation;

import java.util.ArrayList;

import org.bukkit.block.Sign;

/*
 * This class will be filled with the ICStub for each extant IC.
 */

public class ICRegistry {
	private final ArrayList<ICStub> stubs;

	public ICRegistry() {
		this.stubs = new ArrayList<ICStub>();
	}

	public void register(ICStub stub) {
		this.stubs.add(stub);
	}

	public void deregister(ICStub stub) {
		this.stubs.remove(stub);
	}

	public boolean isIC(Sign sign) {
		SignLocation loc = new SignLocation(sign);
		return this.getICAt(loc) != null;
	}

	public ICStub getICAt(BlockLocation loc) {
		for (ICStub stub : this.stubs)
			if (stub.getLocation().equals(loc))
				return stub;

		return null;
	}

	public ArrayList<ICStub> getCopy() {
		ArrayList<ICStub> stubs = new ArrayList<ICStub>();
		for (ICStub stub : this.stubs)
			stubs.add(stub);
		return stubs;
	}
}
