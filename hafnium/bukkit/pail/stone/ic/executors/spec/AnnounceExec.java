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
import hafnium.bukkit.pail.pipe.data.DatumID;
import hafnium.bukkit.pail.pipe.data.TextDatum;
import hafnium.bukkit.pail.stone.ic.ICExecutor;
import hafnium.bukkit.pail.stone.ic.ICStub;
import hafnium.bukkit.pail.stone.ic.RewriteHelper;
import hafnium.bukkit.pail.stone.ic.states.ICState;
import hafnium.bukkit.pail.stone.parsing.ICParser;
import hafnium.bukkit.pail.stone.parsing.args.DatumIDCapture;
import hafnium.bukkit.pail.stone.parsing.args.RegionCapture;
import hafnium.bukkit.pail.stone.trigger.EventArgs;
import hafnium.bukkit.pail.util.Region;
import hafnium.bukkit.pail.util.redstone.Current;
import hafnium.bukkit.pail.util.sign.SignLocation;
import hafnium.bukkit.pail.util.sign.SignText;
import hafnium.bukkit.pail.util.text.MessageableException;
import hafnium.bukkit.pail.util.text.PailMessage;
import hafnium.bukkit.pail.util.text.TextFormatter;

import java.util.List;

import org.bukkit.entity.Player;

public class AnnounceExec extends ICExecutor {
	@Override
	public ICState create(SignLocation loc, SignText text, String owner, ICParser signParser) throws MessageableException {
		AnnounceState state = new AnnounceState();

		RegionCapture region = new RegionCapture(loc);
		DatumIDCapture idCap = new DatumIDCapture();

		signParser.getLine(1).add(region.getCaptureA());
		signParser.getLine(2).add(region.getCaptureB());
		signParser.getLine(3).add(idCap);

		state.setRegion(region.getResult());
		state.setTextId(idCap.getResult());

		return state;
	}

	@Override
	public ICState legacyCreate(SignLocation loc, SignText text, String owner, String data, ICParser signParser) throws MessageableException {
		text.setLine(3, RewriteHelper.newTextDatum(data, owner).toMinimalString());
		return this.create(loc, text, owner, new ICParser(loc, text, owner));
	}

	@Override
	public void trigger(ICStub ic, EventArgs event) {
		if (ic.getController().getInput(0) != Current.RISING)
			return;

		AnnounceState state = ic.getState();

		List<Player> targets = state.getRegion().getContainedPlayers();

		if (targets.size() == 0) {
			state.debug("Announced to:\n" + 0 + " player(s)");
			return;
		}

		String txt;

		DataController<TextDatum> ctrl = PailPipe.getInstance().getTextManager().getController();

		if (ctrl.has(state.getTextId()))
			txt = PailPipe.getInstance().getTextManager().getController().get(state.getTextId()).getData();
		else
			txt = PailMessage.PColor.ERROR + "Text datum " + PailMessage.PColor.USER_INPUT + state.getTextId().toString() + PailMessage.PColor.ERROR
					+ " does not exist.";

		PailMessage msg = PailMessage.from(TextFormatter.format(txt));

		int announcedTo = 0;

		for (Player p : targets) {
			msg.sendTo(p);
			announcedTo++;
		}

		state.debug("Announced to:\n" + announcedTo + " player(s)");
	}

	public static class AnnounceState extends ICState {
		private Region region;
		private DatumID textId;

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
		 * @return the textId
		 */
		public DatumID getTextId() {
			return this.textId;
		}

		/**
		 * @param textId
		 *            the textId to set
		 */
		public void setTextId(DatumID textId) {
			this.textId = textId;
			this.dirty();
		}

	}
}
