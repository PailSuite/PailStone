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
package hafnium.bukkit.pail.stone.ic;

import hafnium.bukkit.pail.stone.PailStone;
import hafnium.bukkit.pail.stone.ic.controllers.IC3ISO;
import hafnium.bukkit.pail.stone.ic.controllers.ICController;
import hafnium.bukkit.pail.stone.ic.controllers.ICNISO;
import hafnium.bukkit.pail.stone.ic.controllers.ICSI3O;
import hafnium.bukkit.pail.stone.ic.controllers.ICSINO;
import hafnium.bukkit.pail.stone.ic.controllers.ICSISO;
import hafnium.bukkit.pail.stone.ic.executors.spec.AnnounceExec;
import hafnium.bukkit.pail.stone.ic.executors.spec.AreaExec;
import hafnium.bukkit.pail.stone.ic.executors.spec.ArrowExec;
import hafnium.bukkit.pail.stone.ic.executors.spec.BoltExec;
import hafnium.bukkit.pail.stone.ic.executors.spec.ClickExec;
import hafnium.bukkit.pail.stone.ic.executors.spec.ClockExec;
import hafnium.bukkit.pail.stone.ic.executors.spec.CommandExec;
import hafnium.bukkit.pail.stone.ic.executors.spec.CountExec;
import hafnium.bukkit.pail.stone.ic.executors.spec.CuboidExec;
import hafnium.bukkit.pail.stone.ic.executors.spec.DelayExec;
import hafnium.bukkit.pail.stone.ic.executors.spec.DispExec;
import hafnium.bukkit.pail.stone.ic.executors.spec.ItemExec;
import hafnium.bukkit.pail.stone.ic.executors.spec.LoggedExec;
import hafnium.bukkit.pail.stone.ic.executors.spec.LogicExec;
import hafnium.bukkit.pail.stone.ic.executors.spec.PulseExec;
import hafnium.bukkit.pail.stone.ic.executors.spec.RandExec;
import hafnium.bukkit.pail.stone.ic.executors.spec.RecvExec;
import hafnium.bukkit.pail.stone.ic.executors.spec.SendExec;
import hafnium.bukkit.pail.stone.ic.executors.spec.SensorExec;
import hafnium.bukkit.pail.stone.ic.executors.spec.SpawnExec;
import hafnium.bukkit.pail.stone.ic.executors.spec.TeleportExec;
import hafnium.bukkit.pail.stone.ic.executors.spec.TestExec;
import hafnium.bukkit.pail.stone.ic.executors.spec.TimeExec;
import hafnium.bukkit.pail.stone.ic.executors.spec.ToggleExec;
import hafnium.bukkit.pail.stone.ic.executors.spec.TriggerExec;
import hafnium.bukkit.pail.stone.ic.executors.spec.WeatherExec;
import hafnium.bukkit.pail.util.AliasedEnum;
import hafnium.bukkit.pail.util.sign.SignText;

import org.bukkit.ChatColor;
import org.bukkit.event.block.SignChangeEvent;

public enum ICType implements AliasedEnum {

	/*
	 * LOGICAL: ICs that deal only with redstone input and output.
	 */

	TEST(ICFamily.LOGICAL, new TestExec(), ICSISO.class, false),

	RAND(ICFamily.LOGICAL, new RandExec(), ICSI3O.class, false, "random", "rbit", "bit", "rnd", "rng"),

	LOGIC(ICFamily.LOGICAL, new LogicExec(), IC3ISO.class, false, "gate", "logicgate"),

	PULSE(ICFamily.LOGICAL, new PulseExec(), ICSISO.class, false, "p"),

	TOGGLE(ICFamily.LOGICAL, new ToggleExec(), ICSISO.class, false, "t"),

	DELAY(ICFamily.LOGICAL, new DelayExec(), ICSISO.class, false, "t"),

	CLOCK(ICFamily.LOGICAL, new ClockExec(), ICSISO.class, false),

	COUNT(ICFamily.LOGICAL, new CountExec(), IC3ISO.class, true),

	SEND(ICFamily.LOGICAL, new SendExec(), ICSINO.class, false, "transmit", "s"),

	RECV(ICFamily.LOGICAL, new RecvExec(), ICNISO.class, true, "r"),

	/*
	 * META: ICs that work with
	 */

	TRIGGER(ICFamily.META, new TriggerExec(), ICNISO.class, true),

	DISP(ICFamily.META, new DispExec(), ICSINO.class, true, "text", "message", "msg"),

	ANNOUNCE(ICFamily.META, new AnnounceExec(), ICSINO.class, true, "broadcast"),

	COMMAND(ICFamily.META, new CommandExec(), ICSINO.class, false, "cmd", "console"),

	/*
	 * SENSING: ICs that sense conditions and trigger redstone.
	 */

	LOGGED(ICFamily.SENSING, new LoggedExec(), ICNISO.class, true, "player", "isonline", "online"),

	CLICK(ICFamily.SENSING, new ClickExec(), ICNISO.class, true, "csensor", "switch"),

	TIME(ICFamily.SENSING, new TimeExec(), ICNISO.class, false, "ctime"),

	WEATHER(ICFamily.SENSING, new WeatherExec(), ICNISO.class, false, "cweather"),

	SENSOR(ICFamily.SENSING, new SensorExec(), ICNISO.class, true, "psensor", "psense", "sense"),

	/*
	 * ACTING: ICs that act on the world and the things in it.
	 */

	TELEPORT(ICFamily.ACTING, new TeleportExec(), ICSINO.class, false, "tp"),

	BOLT(ICFamily.ACTING, new BoltExec(), ICSINO.class, false, "thunder", "lightning"),

	ARROW(ICFamily.ACTING, new ArrowExec(), ICSINO.class, false),

	ITEM(ICFamily.ACTING, new ItemExec(), ICSINO.class, false, "drop"),

	SPAWN(ICFamily.ACTING, new SpawnExec(), ICSINO.class, false, "mob", "mspawn", "spawner", "mspawner"),

	CUBOID(ICFamily.ACTING, new CuboidExec(), ICSINO.class, false, "c", "set", "draw", "ellipsoid"),

	AREA(ICFamily.ACTING, new AreaExec(), ICSINO.class, true);

	private static final ICType[] expermiental = { TEST };

	private final ICFamily family;

	private final ICExecutor executor;
	private final Class<? extends ICController> controller;
	private final String[] aliases;
	private final boolean debug;

	private ICType(ICFamily family, ICExecutor executor, Class<? extends ICController> controller, boolean debug, String... aliases) {
		this.family = family;

		this.executor = executor;
		this.controller = controller;

		this.debug = debug;

		this.aliases = aliases;
	}

	public ICFamily getFamily() {
		return this.family;
	}

	/**
	 * @return the executor
	 */
	public ICExecutor getExecutor() {
		return this.executor;
	}

	/**
	 * @return the controller
	 */
	public Class<? extends ICController> getController() {
		return this.controller;
	}

	public static enum ICFamily {
		LOGICAL(ChatColor.AQUA), ACTING(ChatColor.YELLOW), META(ChatColor.DARK_PURPLE), SENSING(ChatColor.GREEN);

		private final ChatColor color;

		private ICFamily(ChatColor color) {
			this.color = color;
		}

		public ChatColor getColor() {
			return this.color;
		}
	}

	public String getSignName() {
		return this.getFamily().getColor() + ":" + this.name().toLowerCase();
	}

	public void applyToEvent(SignChangeEvent event) {
		event.setLine(0, this.getSignName());
	}

	public void applyToText(SignText text) {
		text.setLine(0, this.getSignName());
	}

	public boolean hasDebug() {
		return this.debug;
	}

	@Override
	public String[] getAliases() {
		return this.aliases;
	}

	public String getPermission() {
		return PailStone.getInstance().getPermissionRoot() + ".ic." + this.name();
	}

	public boolean isExperimental() {
		for (ICType type : ICType.expermiental)
			if (type == this)
				return true;
		return false;
	}
}
