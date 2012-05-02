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

import hafnium.bukkit.pail.pipe.PailPipe;
import hafnium.bukkit.pail.stone.trigger.events.RedstoneEvent;
import hafnium.bukkit.pail.util.BlockLocation;
import hafnium.bukkit.pail.util.redstone.Current;
import hafnium.bukkit.pail.util.redstone.RedstoneUtil;

// TODO: LOW PRIORITY: Block break events on inputs should drop the power.

public class Input {
	private final BlockLocation loc;
	private Current current = Current.DISCONN;

	private long lastTrigger = -1;

	/**
	 * Constructs an input for the specified location for the specified IC.
	 * 
	 * @param l
	 * @param ic
	 */
	public Input(BlockLocation l) {
		this.loc = l;
		this.initialize();
	}

	/**
	 * Gets the location of the input.
	 * 
	 * @return
	 */
	public BlockLocation getLocation() {
		return this.loc;
	}

	/**
	 * Checks if the input is in a loaded chunk.
	 * 
	 * @return
	 */
	public boolean isLoaded() {
		return this.loc.getChunk().isLoaded();
	}

	/**
	 * Grabs the current state of the input, ignoring the currrent state and
	 * foregoing transient states. This method will not set an update to the
	 * owning IC.
	 */
	public void initialize() {
		this.current = RedstoneUtil.getCurrent(this.loc);
	}

	/**
	 * Gets the current for this input.
	 * 
	 * @return
	 */
	public Current getCurrent() {
		return this.current;
	}

	public void apply(RedstoneEvent event) {
		this.lastTrigger = PailPipe.getServerTime();
		this.current = RedstoneUtil.getCurrent(event.getWrappedEvent());
	}

	/**
	 * If the current is a changing current, finalize it.
	 */
	public void conclude() {
		this.current = this.current.getConclusion();
	}

	public void forceUpdate() {
		this.initialize();
	}

	public boolean isReady() {
		return (this.lastTrigger == -1) || (this.lastTrigger < PailPipe.getServerTime()) || (this.lastTrigger > PailPipe.getServerTime());
	}
}
