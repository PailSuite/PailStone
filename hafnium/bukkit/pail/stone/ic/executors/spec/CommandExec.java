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
import hafnium.bukkit.pail.stone.ic.states.ICState;
import hafnium.bukkit.pail.stone.parsing.ICParser;
import hafnium.bukkit.pail.stone.parsing.args.DatumIDCapture;
import hafnium.bukkit.pail.stone.trigger.EventArgs;
import hafnium.bukkit.pail.util.sign.SignLocation;
import hafnium.bukkit.pail.util.sign.SignText;
import hafnium.bukkit.pail.util.text.MessageableException;

import org.bukkit.Bukkit;

public class CommandExec extends ICExecutor {

	@Override
	public ICState create(SignLocation loc, SignText text, String owner, ICParser signParser) throws MessageableException {
		CommandState state = new CommandState();

		DatumIDCapture dat;

		signParser.getLine(1).add(dat = new DatumIDCapture());

		DatumID id = dat.getResult();

		PailPipe.getInstance().getTextManager().getController().fill(id, owner);

		id.assertCanPerform(owner, DataUse.USE, PailPipe.getInstance().getTextManager().getController());

		state.setCommandID(id);

		return state;
	}

	@Override
	public void trigger(ICStub ic, EventArgs event) {
		if (ic.getInput(0) != hafnium.bukkit.pail.util.redstone.Current.RISING)
			return;

		CommandState state = ic.getState();

		String cmd;

		DataController<TextDatum> ctrl = PailPipe.getInstance().getTextManager().getController();

		if (ctrl.has(state.getCommandID())) {
			state.debug("");
			cmd = PailPipe.getInstance().getTextManager().getController().get(state.getCommandID()).getData();
			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd);
		} else
			state.debug("Invalid\nDatumID");
	}

	public static class CommandState extends ICState {
		private DatumID commandID;

		/**
		 * @return the commandID
		 */
		public DatumID getCommandID() {
			return this.commandID;
		}

		/**
		 * @param commandID
		 *            the commandID to set
		 */
		public void setCommandID(DatumID commandID) {
			this.commandID = commandID;
		}

	}
}
