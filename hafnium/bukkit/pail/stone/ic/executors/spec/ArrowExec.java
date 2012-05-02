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
import hafnium.bukkit.pail.stone.parsing.args.DoubleCapture;
import hafnium.bukkit.pail.stone.parsing.args.IntegerCapture;
import hafnium.bukkit.pail.stone.trigger.EventArgs;
import hafnium.bukkit.pail.util.Coordinate;
import hafnium.bukkit.pail.util.sign.SignLocation;
import hafnium.bukkit.pail.util.sign.SignText;
import hafnium.bukkit.pail.util.text.MessageableException;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.util.Vector;

public class ArrowExec extends ICExecutor {

	@Override
	public ICState create(SignLocation loc, SignText text, String owner, ICParser signParser) throws MessageableException {
		ArrowState state = new ArrowState();

		CoordinateCapture from = new CoordinateCapture(PlayerCoord.A);
		CoordinateCapture to = new CoordinateCapture(PlayerCoord.B);

		IntegerCapture amount = new IntegerCapture("number of arrows", IntegerCapture.getMinConstraint(0), 1);

		DoubleCapture speed = new DoubleCapture("arrow speed", DoubleCapture.getConstraint(0.0, null), 1.0);
		DoubleCapture variance = new DoubleCapture("arrow variance", DoubleCapture.getConstraint(0.0, null), 0.1);

		signParser.getLine(1).add(from);
		signParser.getLine(2).add(to);
		signParser.getLine(3).add(amount).add(speed).add(variance);

		state.setFrom(from.getResult());
		state.setTo(to.getResult());

		state.setAmount(amount.getResult());

		state.setSpeed(speed.getResult());
		state.setVariance(variance.getResult());

		return state;
	}

	@Override
	public void trigger(ICStub ic, EventArgs event) {
		if (ic.getInput(0) != hafnium.bukkit.pail.util.redstone.Current.RISING)
			return;

		ArrowState state = ic.getState();

		Location to = state.getTo().getAsRelativeTo(ic.getHostBlockLocation()).getAbsoluteCenter();
		Location from = state.getFrom().getAsRelativeTo(ic.getHostBlockLocation()).getAbsoluteCenter();

		Vector v = new Vector(to.getX() - from.getX(), to.getY() - from.getY(), to.getZ() - from.getZ());

		v.normalize();

		v.multiply(state.getSpeed());

		while (from.getBlock().getType() != Material.AIR)
			from = from.toVector().add(v.clone().normalize().multiply(.2)).toLocation(ic.getWorld());

		for (int i = 0; i < state.getAmount(); i++) {

			Arrow a = ic.getWorld().spawn(from, Arrow.class);
			a.setVelocity(v.clone().add(this.getVariance(state.getVariance())));
			// this.main.cleaner.register(a, 5000);

		}

		ic.getWorld().playEffect(from, org.bukkit.Effect.BOW_FIRE, 0);
	}

	private Vector getVariance(double variance) {
		Vector v = new Vector((Math.random() * 2 - 1), (Math.random() * 2 - 1), (Math.random() * 2 - 1)).normalize().multiply(
				Math.random() * variance);
		return (v);
	}

	public static class ArrowState extends ICState {
		private Coordinate from, to;
		private int amount;
		private double speed, variance;

		/**
		 * @return the from
		 */
		public Coordinate getFrom() {
			return this.from;
		}

		/**
		 * @param from
		 *            the from to set
		 */
		public void setFrom(Coordinate from) {
			this.from = from;
			this.dirty();
		}

		/**
		 * @return the to
		 */
		public Coordinate getTo() {
			return this.to;
		}

		/**
		 * @param to
		 *            the to to set
		 */
		public void setTo(Coordinate to) {
			this.to = to;
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
		 * @return the speed
		 */
		public double getSpeed() {
			return this.speed;
		}

		/**
		 * @param speed
		 *            the speed to set
		 */
		public void setSpeed(double speed) {
			this.speed = speed;
			this.dirty();
		}

		/**
		 * @return the variance
		 */
		public double getVariance() {
			return this.variance;
		}

		/**
		 * @param variance
		 *            the variance to set
		 */
		public void setVariance(double variance) {
			this.variance = variance;
			this.dirty();
		}
	}
}
