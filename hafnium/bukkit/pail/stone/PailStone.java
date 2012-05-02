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

package hafnium.bukkit.pail.stone;

import hafnium.bukkit.pail.pipe.plugins.AbstractPailPlugin;
import hafnium.bukkit.pail.stone.bukkitevent.CommandTriggerListener;
import hafnium.bukkit.pail.stone.bukkitevent.InteractListener;
import hafnium.bukkit.pail.stone.bukkitevent.MoveListener;
import hafnium.bukkit.pail.stone.bukkitevent.RedstoneChangeListener;
import hafnium.bukkit.pail.stone.bukkitevent.SignListener;
import hafnium.bukkit.pail.stone.bukkitevent.TickGenerator;
import hafnium.bukkit.pail.stone.ic.ICFactory;
import hafnium.bukkit.pail.stone.ic.ICMaintainer;
import hafnium.bukkit.pail.stone.ic.ICRegistry;
import hafnium.bukkit.pail.stone.trigger.HookManager;

public class PailStone extends AbstractPailPlugin {
	private static PailStone inst;

	public static PailStone getInstance() {
		return PailStone.inst;
	}

	private ICRegistry registry;
	private HookManager hooks;
	private ICMaintainer maintainer;

	private final static int TICKS_PER_SECOND = 20;

	public PailStone() {
		if (inst == null)
			inst = this;
	}

	@Override
	public void onPailEnable() {
		this.getConfig().options().copyHeader(true);
		this.getConfig().options().copyDefaults(true);

		this.saveConfig();

		this.registry = new ICRegistry();
		this.hooks = new HookManager();

		this.log("\tLoading ICs...");

		ICFactory.load();

		this.log("\tScheduling integrity checks...");

		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, this.maintainer = new ICMaintainer(), 1 * TICKS_PER_SECOND, 5);

		new SignListener(this);
		new RedstoneChangeListener(this);
		new MoveListener(this);
		new TickGenerator();
		new CommandTriggerListener(this, this.getCommand("trigger"));
		new InteractListener(this);

		LegacyLoader.loadOldData();
	}

	@Override
	public void onPailDisable() {
		this.maintainer.wrapUp();
	}

	public ICRegistry getRegistry() {
		return this.registry;
	}

	public HookManager getHookManager() {
		return this.hooks;
	}
}
