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

import hafnium.bukkit.pail.pipe.doc.PermissionDef;
import hafnium.bukkit.pail.pipe.drawing.BrushFill;
import hafnium.bukkit.pail.pipe.drawing.CuboidBrush;
import hafnium.bukkit.pail.pipe.drawing.DrawingBrush;
import hafnium.bukkit.pail.pipe.drawing.DrawingOperation;
import hafnium.bukkit.pail.pipe.drawing.EllipsoidBrush;
import hafnium.bukkit.pail.pipe.players.PailPlayer.PlayerCoord;
import hafnium.bukkit.pail.stone.PailStone;
import hafnium.bukkit.pail.stone.ic.ICExecutor;
import hafnium.bukkit.pail.stone.ic.ICFormatException;
import hafnium.bukkit.pail.stone.ic.ICStub;
import hafnium.bukkit.pail.stone.ic.ICType;
import hafnium.bukkit.pail.stone.ic.states.ICState;
import hafnium.bukkit.pail.stone.parsing.ICParser;
import hafnium.bukkit.pail.stone.parsing.args.CoordinateCapture;
import hafnium.bukkit.pail.stone.parsing.args.EnumCapture;
import hafnium.bukkit.pail.stone.parsing.args.ItemFormCapture;
import hafnium.bukkit.pail.stone.trigger.EventArgs;
import hafnium.bukkit.pail.util.AliasedEnum;
import hafnium.bukkit.pail.util.Coordinate;
import hafnium.bukkit.pail.util.ItemForm;
import hafnium.bukkit.pail.util.redstone.Current;
import hafnium.bukkit.pail.util.sign.SignLocation;
import hafnium.bukkit.pail.util.sign.SignText;
import hafnium.bukkit.pail.util.text.MessageableException;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class CuboidExec extends ICExecutor {

	@Override
	@PermissionDef(permission = "pailstone.ic.cuboid.ignoremax", definition = "Allows the player to ignore the cuboid maximum specified in config.")
	public ICState create(SignLocation loc, SignText text, String owner, ICParser signParser) throws MessageableException {
		CuboidState state = new CuboidState();

		CoordinateCapture mk1 = new CoordinateCapture(PlayerCoord.A);
		CoordinateCapture mk2 = new CoordinateCapture(PlayerCoord.B);

		ItemFormCapture high = new ItemFormCapture("high block", true);
		ItemFormCapture low = new ItemFormCapture("high block", true);

		EnumCapture<Shape> shape = new EnumCapture<Shape>("cuboid shape", Shape.class, Shape.RCT);

		signParser.getLine(1).add(mk1);
		signParser.getLine(2).add(mk2);
		signParser.getLine(3).add(high).add(low).add(shape);

		state.setMarkA(mk1.getResult());
		state.setMarkB(mk2.getResult());

		state.setHighForm(high.getResult());
		state.setLowForm(low.getResult());

		state.setShape(shape.getResult());

		// Vector a = state.getMarkA().getAsRelativeTo(loc).asVector();
		// Vector b = state.getMarkB().getAsRelativeTo(loc).asVector();

		// DrawingBrush brush = state.getShape().getBrushFor(a, b);

		// TODO: Size check.

		Vector a = state.getMarkA().getAsRelativeTo(loc.getHostBlockLocation()).asVector();
		Vector b = state.getMarkB().getAsRelativeTo(loc.getHostBlockLocation()).asVector();
		DrawingBrush brush = state.getShape().getBrushFor(a, b);

		int max = PailStone.getInstance().getConfig().getInt("cuboid-maximum");

		Player pOwner = org.bukkit.Bukkit.getPlayer(owner);

		if ((max != -1) && (brush.estimateSize() > max) && !((pOwner == null) || pOwner.hasPermission(ICType.CUBOID.getPermission() + ".ignoremax")))
			throw new ICFormatException("^eYou do not have permission to cuboid more than ^n" + max
					+ " ^eblocks. The cuboid you defined effects approximatly ^n" + brush.estimateSize() + " ^eblocks.");

		return state;
	}

	@Override
	public void trigger(ICStub ic, EventArgs event) {
		if ((ic.getInput(0) != Current.RISING) && (ic.getInput(0) != Current.FALLING))
			return;

		CuboidState state = ic.getState();

		Vector a = state.getMarkA().getAsRelativeTo(ic.getHostBlockLocation()).asVector();
		Vector b = state.getMarkB().getAsRelativeTo(ic.getHostBlockLocation()).asVector();

		DrawingBrush brush = state.getShape().getBrushFor(a, b);
		DrawingOperation op = new BrushFill(ic.getWorld(), brush, (ic.getInput(0) == Current.RISING ? state.getHighForm() : state.getLowForm()));

		hafnium.bukkit.pail.pipe.PailPipe.getInstance().getDrawingManager().draw(op);
	}

	public enum Shape implements AliasedEnum {
		RCT("R", "C"), EL("E", "S", "O");

		private final String[] aliases;

		private Shape(String... aliases) {
			this.aliases = aliases;
		}

		public DrawingBrush getBrushFor(Vector a, Vector b) {
			switch (this) {
			case RCT:
				return new CuboidBrush(a, b);
			case EL:
				return new EllipsoidBrush(a, b);
			}

			return null;
		}

		@Override
		public String[] getAliases() {
			return this.aliases;
		}
	}

	public static class CuboidState extends ICState {
		private Coordinate markA, markB;
		private ItemForm highForm, lowForm;
		private Shape shape;

		/**
		 * @return the markA
		 */
		public Coordinate getMarkA() {
			return this.markA;
		}

		/**
		 * @param markA
		 *            the markA to set
		 */
		public void setMarkA(Coordinate markA) {
			this.markA = markA;
			this.dirty();
		}

		/**
		 * @return the markB
		 */
		public Coordinate getMarkB() {
			return this.markB;
		}

		/**
		 * @param markB
		 *            the markB to set
		 */
		public void setMarkB(Coordinate markB) {
			this.markB = markB;
			this.dirty();
		}

		/**
		 * @return the highForm
		 */
		public ItemForm getHighForm() {
			return this.highForm;
		}

		/**
		 * @param highForm
		 *            the highForm to set
		 */
		public void setHighForm(ItemForm highForm) {
			this.highForm = highForm;
			this.dirty();
		}

		/**
		 * @return the lowForm
		 */
		public ItemForm getLowForm() {
			return this.lowForm;
		}

		/**
		 * @param lowForm
		 *            the lowForm to set
		 */
		public void setLowForm(ItemForm lowForm) {
			this.lowForm = lowForm;
			this.dirty();
		}

		/**
		 * @return the shape
		 */
		public Shape getShape() {
			return this.shape;
		}

		/**
		 * @param shape
		 *            the shape to set
		 */
		public void setShape(Shape shape) {
			this.shape = shape;
			this.dirty();
		}

	}
}
