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
import hafnium.bukkit.pail.pipe.data.DataController;
import hafnium.bukkit.pail.pipe.data.DataUse;
import hafnium.bukkit.pail.pipe.data.DatumID;
import hafnium.bukkit.pail.pipe.data.TextDatum;
import hafnium.bukkit.pail.stone.ic.ICExecutor;
import hafnium.bukkit.pail.stone.ic.ICStub;
import hafnium.bukkit.pail.stone.ic.RewriteHelper;
import hafnium.bukkit.pail.stone.ic.states.ICState;
import hafnium.bukkit.pail.stone.parsing.ICParser;
import hafnium.bukkit.pail.stone.parsing.args.DatumIDCapture;
import hafnium.bukkit.pail.stone.parsing.args.PlayerNameCapture;
import hafnium.bukkit.pail.stone.trigger.EventArgs;
import hafnium.bukkit.pail.util.redstone.Current;
import hafnium.bukkit.pail.util.sign.SignLocation;
import hafnium.bukkit.pail.util.sign.SignText;
import hafnium.bukkit.pail.util.text.MessageableException;
import hafnium.bukkit.pail.util.text.PailMessage;
import hafnium.bukkit.pail.util.text.TextFormatter;

import org.bukkit.entity.Player;

public class DispExec extends ICExecutor {
	@Override
	public ICState create(SignLocation loc, SignText text, String owner, ICParser signParser) throws MessageableException {
		DispState state = new DispState();

		DatumIDCapture dat;
		PlayerNameCapture player;

		signParser.getLine(1).add(player = new PlayerNameCapture(owner));
		signParser.getLine(2).add(dat = new DatumIDCapture());

		DatumID id = dat.getResult();

		PailPipe.getInstance().getTextManager().getController().fill(id, owner);

		id.assertCanPerform(owner, DataUse.USE, PailPipe.getInstance().getTextManager().getController());

		state.setTextID(id);
		state.setTargetPlayer(player.getResult());

		return state;
	}

	@Override
	public ICState legacyCreate(SignLocation loc, SignText text, String owner, String data, ICParser signParser) throws MessageableException {
		text.setLine(2, RewriteHelper.newTextDatum(data, owner).toMinimalString());
		return this.create(loc, text, owner, new ICParser(loc, text, owner));
	}

	@Override
	public void trigger(ICStub ic, EventArgs event) {
		if (ic.getController().getInput(0) != Current.RISING)
			return;

		DispState state = ic.getState();
		Player p = org.bukkit.Bukkit.getPlayer(state.getTargetPlayer());

		if (p == null) {
			state.debug(state.getTargetPlayer() + "\nis offline");
			return;
		}

		DataController<TextDatum> ctrl = PailPipe.getInstance().getTextManager().getController();

		if (ctrl.has(state.getTextID())) {
			state.debug("Displaying\nMessage");
			String txt = PailPipe.getInstance().getTextManager().getController().get(state.getTextID()).getData();
			PailMessage.from(TextFormatter.format(txt)).sendTo(p);
		} else
			state.debug(org.bukkit.ChatColor.DARK_RED + "Invalid\n" + org.bukkit.ChatColor.DARK_RED + "Message");
	}

	public static class DispState extends ICState {
		private DatumID textID;
		private String targetPlayer;

		/**
		 * @return the textDatum
		 */
		public DatumID getTextID() {
			return this.textID;
		}

		/**
		 * @param textDatum
		 *            the textDatum to set
		 */
		public void setTextID(DatumID textDatum) {
			this.textID = textDatum;
			this.dirty();
		}

		/**
		 * @return the targetPlayer
		 */
		public String getTargetPlayer() {
			return this.targetPlayer;
		}

		/**
		 * @param targetPlayer
		 *            the targetPlayer to set
		 */
		public void setTargetPlayer(String targetPlayer) {
			this.targetPlayer = targetPlayer;
			this.dirty();
		}
	}
}
