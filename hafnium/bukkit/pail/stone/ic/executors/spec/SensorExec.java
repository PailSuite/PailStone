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

import hafnium.bukkit.pail.pipe.PailPipe;
import hafnium.bukkit.pail.stone.ic.ICExecutor;
import hafnium.bukkit.pail.stone.ic.ICStub;
import hafnium.bukkit.pail.stone.ic.ICStub.DeferredOperation;
import hafnium.bukkit.pail.stone.ic.states.ICState;
import hafnium.bukkit.pail.stone.parsing.ICParser;
import hafnium.bukkit.pail.stone.parsing.args.PlayerFilterCapture;
import hafnium.bukkit.pail.stone.parsing.args.RegionCapture;
import hafnium.bukkit.pail.stone.trigger.EventArgs;
import hafnium.bukkit.pail.stone.trigger.EventType;
import hafnium.bukkit.pail.stone.trigger.events.MovementEvent;
import hafnium.bukkit.pail.stone.trigger.hooks.MovementHook;
import hafnium.bukkit.pail.util.PlayerFilter;
import hafnium.bukkit.pail.util.Region;
import hafnium.bukkit.pail.util.sign.SignLocation;
import hafnium.bukkit.pail.util.sign.SignText;
import hafnium.bukkit.pail.util.text.MessageableException;

import java.util.List;

import org.bukkit.entity.Player;

public class SensorExec extends ICExecutor {
	@Override
	public ICState create(SignLocation loc, SignText text, String owner, ICParser signParser) throws MessageableException {
		SensorState state = new SensorState();

		RegionCapture region = new RegionCapture(loc);
		PlayerFilterCapture filter = new PlayerFilterCapture(true, true);

		signParser.getLine(1).add(region.getCaptureA());
		signParser.getLine(2).add(region.getCaptureB());
		signParser.getLine(3).add(filter);

		state.setRegion(region.getResult());
		state.setFilter(filter.getResult());

		return state;
	}

	@Override
	public void initialize(ICStub stub) {
		stub.registerHook(new MovementHook(stub, ((SensorState) stub.getState()).getRegion()));
	}

	@Override
	public void trigger(ICStub ic, EventArgs event) {
		// Cut down tick execution to once every two seconds.

		if ((event.getType() == EventType.TICK) && (PailPipe.getServerTime() % 40 != 0))
			return;

		SensorState state = ic.getState();

		boolean match = false;
		boolean notMatch = false;

		List<Player> players = state.getRegion().getContainedPlayers();

		// If the event is a movement event, the change in location it
		// represents will not be available to region. Correct this by manually
		// placing/removing the player that the movement event applies to
		// in/from the player list as needed.

		if (event.getType() == EventType.MOVEMENT) {
			MovementEvent me = (MovementEvent) event;

			if (state.getRegion().contains(me.getTo()))
				players.add(me.getPlayer());
			else
				players.remove(me.getPlayer());
		}

		int matches = 0;

		for (Player player : players)
			if (state.getFilter().matches(player, state.getOwnerName())) {
				match = true;
				matches++;
			} else
				notMatch = true;

		state.debug(matches + " matches");

		final boolean outputState = match && !(notMatch && state.getFilter().isExclusive());

		if (match)
			ic.startTicking();
		else
			ic.stopTicking();

		// We defer the operation until after the event that triggered this
		// execution has finished propagating. This is a bugfix for :announce
		// ICs covering the same area as a :sensor that triggers it directly.
		// Without the deferment, the announce sign would execute before the
		// player movement into the announce's area resolves.

		if (outputState != ic.getController().getOutput(0).asBoolean())
			ic.defer(new DeferredOperation() {

				@Override
				public void perform(ICStub stub) {
					stub.getController().setOutput(0, outputState);
				}

			});
	}

	public static class SensorState extends ICState {
		private Region region;
		private PlayerFilter filter;

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
			this.dirty();
		}

		/**
		 * @return the matcher
		 */
		public PlayerFilter getFilter() {
			return this.filter;
		}

		/**
		 * @param filter
		 *            the matcher to set
		 */
		public void setFilter(PlayerFilter filter) {
			this.filter = filter;
			this.dirty();
		}

	}
}
