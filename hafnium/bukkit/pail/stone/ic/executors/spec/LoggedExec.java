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
import hafnium.bukkit.pail.stone.parsing.args.PlayerFilterCapture;
import hafnium.bukkit.pail.stone.trigger.EventArgs;
import hafnium.bukkit.pail.stone.trigger.hooks.SecondHook;
import hafnium.bukkit.pail.util.AliasedEnum;
import hafnium.bukkit.pail.util.PlayerFilter;
import hafnium.bukkit.pail.util.sign.SignLocation;
import hafnium.bukkit.pail.util.sign.SignText;
import hafnium.bukkit.pail.util.text.MessageableException;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class LoggedExec extends ICExecutor {
	@Override
	public ICState create(SignLocation loc, SignText text, String owner, ICParser signParser) throws MessageableException {
		LoggedState state = new LoggedState();

		PlayerFilterCapture filter = new PlayerFilterCapture(null, true, true);
		EnumCapture<Scope> scope = new EnumCapture<Scope>("scope", Scope.class, Scope.WORLD);

		signParser.getLine(1).add(filter);
		signParser.getLine(2).add(scope);

		state.setPlayerFilter(filter.getResult());
		state.setScope(scope.getResult());

		return state;
	}

	@Override
	public void initialize(ICStub ic) {
		ic.registerHook(new SecondHook(ic));
	}

	@Override
	public void trigger(ICStub ic, EventArgs event) {
		// TODO: WorldChange/ServerJoinQuit implementation.

		LoggedState state = ic.getState();

		Player[] inScope = new Player[0];

		if (state.getScope() == Scope.SERVER)
			inScope = Bukkit.getServer().getOnlinePlayers();
		else
			inScope = ic.getWorld().getPlayers().toArray(inScope);

		boolean any = false;
		boolean anyNot = false;

		ArrayList<Player> matches = new ArrayList<Player>();

		for (Player p : inScope)
			if (state.getPlayerFilter().matches(p, state.getOwnerName())) {
				any = true;
				matches.add(p);
			} else
				anyNot = true;

		if (any && !(anyNot && state.getPlayerFilter().isExclusive())) {
			if (state.hasDebugSign()) {
				String debug = "";

				for (int i = 0; (i < matches.size()) && (i < 3); i++) {
					if (i != 0)
						debug += '\n';
					debug += matches.get(i).getName();
				}

				if (matches.size() > 3) {
					int lineStart = debug.lastIndexOf('\n') + 1;
					if (debug.length() - lineStart < 12)
						debug += "...";
					else
						debug = debug.substring(0, lineStart + 11) + "...";
				}
				state.debug(debug);
			}

			ic.setOutput(0, true);
		} else if (ic.getOutput(0).asBoolean() != false) {
			state.debug("NO MATCHES");
			ic.setOutput(0, false);
		}
	}

	public static class LoggedState extends ICState {
		private PlayerFilter playerFilter;
		private Scope scope;

		/**
		 * @return the playerFilter
		 */
		public PlayerFilter getPlayerFilter() {
			return this.playerFilter;
		}

		/**
		 * @param playerFilter
		 *            the playerFilter to set
		 */
		public void setPlayerFilter(PlayerFilter playerFilter) {
			this.playerFilter = playerFilter;
			this.dirty();
		}

		/**
		 * @return the scope
		 */
		public Scope getScope() {
			return this.scope;
		}

		/**
		 * @param scope
		 *            the scope to set
		 */
		public void setScope(Scope scope) {
			this.scope = scope;
		}

	}

	public static enum Scope implements AliasedEnum {
		SERVER("global", "all"), WORLD("local", "map");

		private final String[] aliases;

		private Scope(String... aliases) {
			this.aliases = aliases;
		}

		@Override
		public String[] getAliases() {
			return this.aliases;
		}
	}
}
