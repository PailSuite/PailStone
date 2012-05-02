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
package hafnium.bukkit.pail.stone.parsing.args;

import hafnium.bukkit.pail.pipe.PailPipe;
import hafnium.bukkit.pail.pipe.players.PailPlayer;
import hafnium.bukkit.pail.pipe.players.PailPlayer.PlayerCoord;
import hafnium.bukkit.pail.stone.ic.ICFormatException;
import hafnium.bukkit.pail.stone.parsing.ICParser.ArgCapture;
import hafnium.bukkit.pail.util.BlockLocation;
import hafnium.bukkit.pail.util.Coordinate;
import hafnium.bukkit.pail.util.sign.SignLocation;
import hafnium.bukkit.pail.util.text.MessageableException;

public class CoordinateCapture extends ArgCapture<Coordinate> {
	private final PlayerCoord defSlot;

	public CoordinateCapture(PlayerCoord def) {
		super(null, 3);
		this.defSlot = def;
	}

	@Override
	public boolean hasDefault() {
		return this.defSlot != null;
	}

	@Override
	public Coordinate getDefault(int line, SignLocation loc, String owner) throws ICFormatException {
		PailPlayer pp = hafnium.bukkit.pail.pipe.PailPipe.getInstance().getPlayerManager().getPlayer(owner);

		if (!pp.hasCoordinate(this.defSlot))
			throw this.getBlankException(line);

		if (!pp.getCoordinateWorld().equals(loc.getWorldName()))
			throw new ICFormatException("^eYour coordinates are defined on ^n" + pp.getCoordinateWorld() + "^e. They must be defined on this world.",
					line);

		return Coordinate.getRelative(loc.getHostBlockLocation(), pp.getCoordinate(this.defSlot));
	}

	@Override
	public ICFormatException getBlankException(int line) {
		if (this.hasDefault())
			return new ICFormatException("^eYou must either designate coordinate ^n" + this.defSlot.name() + " ^ewith the coordinate wand ( ^n"
					+ PailPipe.getInstance().getCoordinateManager().getWandName() + " ^e) or specify it on the sign.");
		return new ICFormatException("^eYou must specify a coordinate on the sign.");
	}

	@Override
	public ICFormatException getInvalidException(String input, int line) {
		return new ICFormatException("^eI could not understand ^u" + input + " ^eas a coordinate triplet.\nThe proper form is: ^n<x> <y> <z>");
	}

	@Override
	public String getTextFor(Object object) {
		Coordinate c = (Coordinate) object;
		return c.getX() + " " + c.getY() + " " + c.getZ();
	}

	@Override
	public Coordinate parse(String arg, int line, SignLocation loc, String owner) throws ICFormatException {
		String[] sParts = arg.split("[ ]", 3);
		int[] parts = new int[3];

		if (sParts.length != 3)
			throw this.getInvalidException(arg, line);

		for (int i = 0; i < 3; i++)
			try {
				parts[i] = Integer.parseInt(sParts[i]);
			} catch (NumberFormatException e) {
				throw this.getInvalidException(arg, line);
			}

		return this.setResult(new Coordinate(parts[0], parts[1], parts[2]));
	}

	public BlockLocation getResultAsLocation(BlockLocation reference) throws MessageableException {
		return this.getResult().getAsRelativeTo(reference);
	}
}
