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

import hafnium.bukkit.pail.pipe.players.PailPlayer.PlayerCoord;
import hafnium.bukkit.pail.stone.ic.ICExecutor;
import hafnium.bukkit.pail.stone.ic.ICStub;
import hafnium.bukkit.pail.stone.ic.ICStub.DeferredOperation;
import hafnium.bukkit.pail.stone.ic.states.ICState;
import hafnium.bukkit.pail.stone.parsing.ICParser;
import hafnium.bukkit.pail.stone.parsing.args.CoordinateCapture;
import hafnium.bukkit.pail.stone.parsing.args.RegionCapture;
import hafnium.bukkit.pail.stone.trigger.EventArgs;
import hafnium.bukkit.pail.util.BlockLocation;
import hafnium.bukkit.pail.util.Coordinate;
import hafnium.bukkit.pail.util.Region;
import hafnium.bukkit.pail.util.redstone.Current;
import hafnium.bukkit.pail.util.sign.SignLocation;
import hafnium.bukkit.pail.util.sign.SignText;
import hafnium.bukkit.pail.util.text.MessageableException;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TeleportExec extends ICExecutor {

	@Override
	public ICState create(SignLocation loc, SignText text, String owner, ICParser signParser) throws MessageableException {
		TeleportState state = new TeleportState();

		RegionCapture region = new RegionCapture(loc);
		CoordinateCapture target = new CoordinateCapture(PlayerCoord.C);

		signParser.getLine(1).add(region.getCaptureA());
		signParser.getLine(2).add(region.getCaptureB());
		signParser.getLine(3).add(target);

		state.setRegion(region.getResult());
		state.setTarget(target.getResult());

		return state;
	}

	@Override
	public void trigger(ICStub ic, EventArgs event) {
		if (ic.getInput(0) != Current.RISING)
			return;

		TeleportState state = ic.getState();

		List<Player> players = state.getRegion().getContainedPlayers();

		if (players.size() == 0)
			return;

		BlockLocation target = state.getTarget().getAsRelativeTo(ic.getController().getHostBlockLocation());

		ic.defer(new TeleportOperation(players, target));
	}

	public static class TeleportOperation extends DeferredOperation {
		private final List<Player> players;
		private final BlockLocation target;

		public TeleportOperation(List<Player> players, BlockLocation target) {
			this.players = players;
			this.target = target;
		}

		@Override
		public void perform(ICStub stub) {
			Location dest = this.target.asLocation();
			for (Player p : this.players) {
				dest.setPitch(p.getLocation().getPitch());
				dest.setYaw(p.getLocation().getYaw());
				p.teleport(dest);
			}
		}
	}

	public static class TeleportState extends ICState {
		private Region region;
		private Coordinate target;

		/**
		 * @return the region
		 */
		public Region getRegion() {
			return this.region;
		}

		/**
		 * @param region
		 *            the region to set
		 */
		public void setRegion(Region region) {
			this.region = region;
		}

		/**
		 * @return the target
		 */
		public Coordinate getTarget() {
			return this.target;
		}

		/**
		 * @param target
		 *            the target to set
		 */
		public void setTarget(Coordinate target) {
			this.target = target;
		}

	}
}
