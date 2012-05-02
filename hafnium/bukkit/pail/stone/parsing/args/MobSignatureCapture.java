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

import hafnium.bukkit.pail.stone.parsing.ICParser.ArgCapture;
import hafnium.bukkit.pail.stone.parsing.ICParser.ICLine;
import hafnium.bukkit.pail.stone.parsing.ICParser.ParseCondition;
import hafnium.bukkit.pail.util.ArgumentConstraints;
import hafnium.bukkit.pail.util.MobUtil;
import hafnium.bukkit.pail.util.MobUtil.MobSignature;
import hafnium.bukkit.pail.util.text.MessageableException;

import org.bukkit.entity.CreatureType;

public class MobSignatureCapture {
	private final EnumCapture<CreatureType> type = new EnumCapture<CreatureType>("creature", CreatureType.class, null);
	private final EnumCapture<MobUtil.Age> age = new EnumCapture<MobUtil.Age>("age", MobUtil.Age.class, MobUtil.Age.ADULT);
	private final DyeColorCapture color = new DyeColorCapture(org.bukkit.DyeColor.WHITE);
	private final EnumCapture<MobUtil.Demeanor> demeanor = new EnumCapture<MobUtil.Demeanor>("demeanor", MobUtil.Demeanor.class,
			MobUtil.Demeanor.ANGRY);
	private final IntegerCapture size = new IntegerCapture("slime size", new ArgumentConstraints.IntRangeConstraint(1, 6), 4);

	public MobSignatureCapture() {

	}

	public ArgCapture<?> getTypeCapture() {
		return this.type;
	}

	public void applyStateArgs(ICLine line) {
		line.add(this.color, new ParseCondition() {
			@Override
			public boolean isSatisfied() throws MessageableException {
				return MobUtil.isColorable(MobSignatureCapture.this.type.getResult().getEntityClass());
			}
		});

		line.add(this.demeanor, new ParseCondition() {
			@Override
			public boolean isSatisfied() throws MessageableException {
				return MobUtil.isAgitatable(MobSignatureCapture.this.type.getResult().getEntityClass());
			}
		});

		line.add(this.age, new ParseCondition() {
			@Override
			public boolean isSatisfied() throws MessageableException {
				return MobUtil.isAged(MobSignatureCapture.this.type.getResult().getEntityClass());
			}
		});

		line.add(this.size, new ParseCondition() {
			@Override
			public boolean isSatisfied() throws MessageableException {
				return MobUtil.isSizeable(MobSignatureCapture.this.type.getResult().getEntityClass());
			}
		});
	}

	public MobSignature getResult() throws MessageableException {
		return new MobSignature(this.type.getResult(), this.age.getResult(), this.demeanor.getResult(), this.color.getResult(), this.size.getResult());
	}
}
