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
import hafnium.bukkit.pail.stone.ic.states.ICState;
import hafnium.bukkit.pail.stone.parsing.ICParser;
import hafnium.bukkit.pail.stone.parsing.args.CoordinateCapture;
import hafnium.bukkit.pail.stone.parsing.args.IntegerCapture;
import hafnium.bukkit.pail.stone.parsing.args.ItemFormCapture;
import hafnium.bukkit.pail.stone.trigger.EventArgs;
import hafnium.bukkit.pail.util.ArgumentConstraints;
import hafnium.bukkit.pail.util.Coordinate;
import hafnium.bukkit.pail.util.ItemForm;
import hafnium.bukkit.pail.util.redstone.Current;
import hafnium.bukkit.pail.util.sign.SignLocation;
import hafnium.bukkit.pail.util.sign.SignText;
import hafnium.bukkit.pail.util.text.MessageableException;

public class ItemExec extends ICExecutor {

	@Override
	public ICState create(SignLocation loc, SignText text, String owner, ICParser signParser) throws MessageableException {
		ItemState state = new ItemState();

		ItemFormCapture item = new ItemFormCapture("item", null, false, true);
		IntegerCapture amount = new IntegerCapture("amount", new ArgumentConstraints.IntMinConstraint(1), 1);

		CoordinateCapture location = new CoordinateCapture(PlayerCoord.C);

		signParser.getLine(1).add(item).add(amount);
		signParser.getLine(2).add(location);

		state.setItem(item.getResult());
		state.setAmount(amount.getResult());
		state.setSpawnLocation(location.getResult());

		return state;
	}

	@Override
	public ICState legacyCreate(SignLocation loc, SignText text, String owner, String data, ICParser signParser) throws MessageableException {
		text.setLine(
				2,
				hafnium.bukkit.pail.stone.ic.RewriteHelper.firstAir(loc,
						hafnium.bukkit.pail.stone.LegacyLoader.LegacyDirection.fromString(text.getLine(2))).toSimpleString());
		return this.create(loc, text, owner, new ICParser(loc, text, owner));
	}

	@Override
	public void trigger(ICStub ic, EventArgs event) {
		if (ic.getInput(0) != Current.RISING)
			return;

		ItemState state = ic.getState();

		state.getItem().spawn(state.getSpawnLocation().getAsRelativeTo(ic.getHostBlockLocation()), state.getAmount());
	}

	public static class ItemState extends ICState {
		private ItemForm item;
		private int amount;
		private Coordinate spawnLocation;

		/**
		 * @return the item
		 */
		public ItemForm getItem() {
			return this.item;
		}

		/**
		 * @param item
		 *            the item to set
		 */
		public void setItem(ItemForm item) {
			this.item = item;
			this.dirty();
		}

		/**
		 * @return the amount
		 */
		public int getAmount() {
			return this.amount;
		}

		/**
		 * @param amount
		 *            the amount to set
		 */
		public void setAmount(int amount) {
			this.amount = amount;
			this.dirty();
		}

		/**
		 * @return the spawnLocation
		 */
		public Coordinate getSpawnLocation() {
			return this.spawnLocation;
		}

		/**
		 * @param spawnLocation
		 *            the spawnLocation to set
		 */
		public void setSpawnLocation(Coordinate spawnLocation) {
			this.spawnLocation = spawnLocation;
			this.dirty();
		}

	}
}
