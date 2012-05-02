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
package hafnium.bukkit.pail.stone.ic.controllers;

import hafnium.bukkit.pail.util.BlockLocation;
import hafnium.bukkit.pail.util.redstone.Current;
import hafnium.bukkit.pail.util.redstone.RedstoneUtil;

public class Output {
	private final BlockLocation location;

	/**
	 * Creates an Output object for the specified location.
	 * 
	 * @param location
	 */
	public Output(BlockLocation location) {
		this.location = location;
	}

	/**
	 * Gets the position of the lever. This will be ON or OFF for a loaded
	 * lever, DISCONN for a block that is not a lever, and UNLOADED when
	 * unloaded.
	 * 
	 * @return
	 */
	public Current getCurrent() {
		if (RedstoneUtil.isLever(this.location))
			return RedstoneUtil.getCurrent(this.location);

		return Current.DISCONN;
	}

	/**
	 * Sets the position of the lever.
	 * 
	 * @param position
	 * @return Sucess. False means that there is no lever, or that it is
	 *         unloaded. This should be checked via getCurrent().
	 */
	public boolean setOutput(boolean position) {
		return RedstoneUtil.setLever(this.location, position);
	}

	public BlockLocation getLocation() {
		return this.location;
	}
}
