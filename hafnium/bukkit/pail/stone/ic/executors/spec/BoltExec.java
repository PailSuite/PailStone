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
package hafnium.bukkit.pail.stone.ic.executors.spec;

import hafnium.bukkit.pail.stone.ic.ICExecutor;
import hafnium.bukkit.pail.stone.ic.ICStub;
import hafnium.bukkit.pail.stone.ic.states.ICState;
import hafnium.bukkit.pail.stone.parsing.ICParser;
import hafnium.bukkit.pail.stone.parsing.args.EnumCapture;
import hafnium.bukkit.pail.stone.trigger.EventArgs;
import hafnium.bukkit.pail.util.redstone.Current;
import hafnium.bukkit.pail.util.sign.SignLocation;
import hafnium.bukkit.pail.util.sign.SignText;
import hafnium.bukkit.pail.util.text.MessageableException;

import org.bukkit.Location;
import org.bukkit.Material;

public class BoltExec extends ICExecutor {
	@Override
	public ICState create(SignLocation loc, SignText text, String owner, ICParser parser) throws MessageableException {
		BoltState state = new BoltState();

		EnumCapture<BoltType> type = new EnumCapture<BoltType>("bolt type", BoltType.class, BoltType.FAKE);

		parser.getLine(1).add(type);

		state.setBoltType(type.getResult());

		return state;
	}

	@Override
	public void trigger(ICStub ic, EventArgs event) {
		if (ic.getController().getInput(0) == Current.RISING) {
			BoltState state = ic.getState();

			Location strikeTarget = ic.getController().getHostBlockLocation().asLocation();
			strikeTarget.setY(127);

			for (int y = 127; y > 0; y--) {
				strikeTarget.setY(y);
				if (strikeTarget.getBlock().getType() != Material.AIR) {
					strikeTarget.setY(y + 1);
					break;
				}
			}

			switch (state.getBoltType()) {
			case REAL:
				ic.getLocation().getWorld().strikeLightning(strikeTarget);
				break;
			case FAKE:
				ic.getLocation().getWorld().strikeLightningEffect(strikeTarget);
				break;
			}
		}
	}

	public static enum BoltType {
		REAL, FAKE;
	}

	public static class BoltState extends ICState {
		private BoltType boltType;

		/**
		 * @return the boltType
		 */
		public BoltType getBoltType() {
			return this.boltType;
		}

		/**
		 * @param boltType
		 *            the boltType to set
		 */
		public void setBoltType(BoltType boltType) {
			this.boltType = boltType;
			this.dirty();
		}
	}
}
