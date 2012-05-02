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
import hafnium.bukkit.pail.stone.LegacyLoader.LegacyDirection;
import hafnium.bukkit.pail.stone.ic.ICExecutor;
import hafnium.bukkit.pail.stone.ic.ICFormatException;
import hafnium.bukkit.pail.stone.ic.ICStub;
import hafnium.bukkit.pail.stone.ic.states.ICState;
import hafnium.bukkit.pail.stone.parsing.ICParser;
import hafnium.bukkit.pail.stone.parsing.args.CoordinateCapture;
import hafnium.bukkit.pail.stone.parsing.args.MobSignatureCapture;
import hafnium.bukkit.pail.stone.trigger.EventArgs;
import hafnium.bukkit.pail.util.Coordinate;
import hafnium.bukkit.pail.util.MobUtil.MobSignature;
import hafnium.bukkit.pail.util.sign.SignLocation;
import hafnium.bukkit.pail.util.sign.SignText;
import hafnium.bukkit.pail.util.text.MessageableException;

import org.bukkit.entity.CreatureType;

public class SpawnExec extends ICExecutor {
	@Override
	public ICState create(SignLocation loc, SignText text, String owner, ICParser signParser) throws MessageableException {
		SpawnState state = new SpawnState();

		MobSignatureCapture mob = new MobSignatureCapture();
		CoordinateCapture coord = new CoordinateCapture(PlayerCoord.C);

		signParser.getLine(1).add(mob.getTypeCapture());
		mob.applyStateArgs(signParser.getLine(2));
		signParser.getLine(3).add(coord);

		state.setMob(mob.getResult());
		state.setSpawnCoordinate(coord.getResult());

		return state;
	}

	@Override
	public ICState legacyCreate(SignLocation loc, SignText text, String owner, String data, ICParser signParser) throws MessageableException {
		String[] mobLine = text.getLine(1).split("\\:");

		if ((mobLine.length == 0) || (mobLine.length > 2))
			throw new ICFormatException("Legacy spawner has unexpected mob syntax.");

		CreatureType mobType = CreatureType.fromName(mobLine[0]);

		if (mobType == null)
			for (CreatureType cType : CreatureType.values())
				if (cType.name().equalsIgnoreCase(mobLine[0])) {
					mobType = cType;
					break;
				}

		String mobData = (mobLine.length > 1 ? mobLine[1] : "");
		Coordinate coord = hafnium.bukkit.pail.stone.ic.RewriteHelper.firstAir(loc, LegacyDirection.fromString(text.getLine(2).toUpperCase()));
		text.setLine(1, mobType.name());
		text.setLine(2, mobData);
		text.setLine(3, coord.toSimpleString());

		return this.create(loc, text, owner, new ICParser(loc, text, owner));
	}

	@Override
	public void trigger(ICStub ic, EventArgs event) {
		if (ic.getInput(0) != hafnium.bukkit.pail.util.redstone.Current.RISING)
			return;
		SpawnState state = ic.getState();
		state.getMob().spawnAt(state.getSpawnCoordinate().getAsRelativeTo(ic.getHostBlockLocation()));
	}

	public static class SpawnState extends ICState {
		private MobSignature mob;
		private Coordinate spawnCoordinate;

		/**
		 * @return the mob
		 */
		public MobSignature getMob() {
			return this.mob;
		}

		/**
		 * @param mob
		 *            the mob to set
		 */
		public void setMob(MobSignature mob) {
			this.mob = mob;
		}

		/**
		 * @return the spawnCoordinate
		 */
		public Coordinate getSpawnCoordinate() {
			return this.spawnCoordinate;
		}

		/**
		 * @param spawnCoordinate
		 *            the spawnCoordinate to set
		 */
		public void setSpawnCoordinate(Coordinate spawnCoordinate) {
			this.spawnCoordinate = spawnCoordinate;
		}

	}
}
