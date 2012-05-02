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

import hafnium.bukkit.pail.stone.ic.ICFormatException;
import hafnium.bukkit.pail.stone.parsing.ICParser.ArgCapture;
import hafnium.bukkit.pail.util.PlayerFilter;
import hafnium.bukkit.pail.util.PlayerFilter.AnyPlayer;
import hafnium.bukkit.pail.util.sign.SignLocation;
import hafnium.bukkit.pail.util.text.MessageableException;

public class PlayerFilterCapture extends ArgCapture<PlayerFilter> {
	private final boolean allowPermission;
	private final boolean allowExclusive;

	public PlayerFilterCapture() {
		this(true, false);
	}

	/**
	 * 
	 * @param allowPermission
	 *            If the filter should support string player-names.
	 */
	public PlayerFilterCapture(boolean allowPermission, boolean allowExclusive) {
		this(new AnyPlayer(), allowPermission, allowExclusive);
	}

	public PlayerFilterCapture(PlayerFilter def) {
		this(def, true, false);
	}

	/**
	 * 
	 * @param def
	 * @param stringFilter
	 *            If the filter should support string player-names.
	 */
	public PlayerFilterCapture(PlayerFilter def, boolean stringFilter, boolean allowExclusive) {
		super(def, 1);
		this.allowPermission = stringFilter;
		this.allowExclusive = allowExclusive;
		if (stringFilter && !((def == null) || def.supportsString()))
			throw new UnsupportedOperationException("The specified default does not support strings as requested.");
		if (!allowExclusive && !((def == null) || !def.isExclusive()))
			throw new UnsupportedOperationException("The specified default is exclusive, but that is not supported here.");
	}

	@Override
	public ICFormatException getBlankException(int line) {
		return new ICFormatException("^eYou must specify a player filter.", line);
	}

	@Override
	public ICFormatException getInvalidException(String input, int line) {
		return new ICFormatException("^eI could not understand ^u" + input + " ^eas a player filter. You can specify ^c* ^efor any player"
				+ (this.allowPermission ? ", " : " or") + " the player's ^nname ^efor a specific player"
				+ (this.allowPermission ? " or ^c?^g<^npermission^g> ^efor any player with a specific permission." : ".")
				+ " You can also prefix a filter with ^c^ ^eto negate the filter"
				+ (this.allowExclusive ? ", and prefix it with ^c/ ^eto make it exclusive." : "."), line);
	}

	@Override
	public String getTextFor(Object object) {
		// FIXME Potential null pointer on SOMETHING. Nulls should not have
		// propagated this far.
		return object.toString();
	}

	@Override
	public PlayerFilter parse(String arg, int line, SignLocation loc, String owner) throws MessageableException {
		PlayerFilter filter = PlayerFilter.parse(arg);
		if (filter == null)
			throw this.getInvalidException(arg, line);
		if ((this.allowPermission || filter.supportsString()) && (this.allowExclusive || !filter.isExclusive()))
			return this.setResult(filter);

		throw this.getInvalidException(arg, line);
	}
}
