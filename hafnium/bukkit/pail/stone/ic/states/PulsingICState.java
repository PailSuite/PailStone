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
package hafnium.bukkit.pail.stone.ic.states;

import hafnium.bukkit.pail.pipe.PailPipe;
import hafnium.bukkit.pail.stone.ic.ICStub;

import java.io.IOException;

public abstract class PulsingICState extends ICState {
	private int pulseDuration = 10;
	private long pulseStartTime = 0;

	/**
	 * @return the pulseDuration
	 */
	public int getPulseDuration() {
		return this.pulseDuration;
	}

	/**
	 * @param pulseDuration
	 *            the pulseDuration to set
	 * @throws IOException
	 */
	public void setPulseDuration(int pulseDuration) {
		this.pulseDuration = pulseDuration;
		this.dirty();
	}

	/**
	 * @return the pulseStartTime
	 */
	public long getPulseStartTime() {
		return this.pulseStartTime;
	}

	/**
	 * @param pulseStartTime
	 *            the pulseStartTime to set
	 */
	public void setPulseStartTime(long pulseStartTime) {
		this.pulseStartTime = pulseStartTime;
		this.dirty();
	}

	/**
	 * Starts the pulse.
	 */
	public void doStartPulse() {
		this.setPulseStartTime(PailPipe.getServerTime());
	}

	/**
	 * Returns the number of ticks left in the pulse. Will return 0 or less if
	 * the pulse is over.
	 * 
	 * @return
	 */
	public int getTicksLeft() {
		if (this.getPulseStartTime() > PailPipe.getServerTime())
			this.doStartPulse();

		long result = ((this.getPulseStartTime() + this.getPulseDuration()) - PailPipe.getServerTime());

		if (result < 0)
			return -1;
		return (int) result;
	}

	@Override
	public void wrapUp(ICStub stub) {
		super.wrapUp(stub);
		this.setPulseStartTime(this.getPulseStartTime() - PailPipe.getServerTime());
	}
}
