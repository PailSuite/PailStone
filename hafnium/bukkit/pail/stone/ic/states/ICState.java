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
package hafnium.bukkit.pail.stone.ic.states;

import hafnium.bukkit.pail.stone.PailStone;
import hafnium.bukkit.pail.stone.ic.ICStub;
import hafnium.bukkit.pail.stone.ic.ICType;
import hafnium.bukkit.pail.util.sign.BlockSignText;
import hafnium.bukkit.pail.util.sign.SignLocation;
import hafnium.bukkit.pail.util.sign.SignText;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;

public class ICState {
	private static long TIMEOUT = 5;

	private SignLocation location;
	private SignLocation debugLocation = null;
	private String ownerName;
	private ICType type;

	private boolean dirty = false;
	private long lastAccessed = -1;

	private boolean wasTicking = false;

	public static ICState load(File f) throws IOException {
		FileReader read = new FileReader(f);

		Yaml y = new Yaml(new CustomClassLoaderConstructor(ICState.class.getClassLoader()));

		Object o;

		try {
			o = y.load(read);
		} finally {
			read.close();
		}

		if (o instanceof ICState) {
			ICState state = (ICState) o;
			state.clean();
			return state;
		}

		throw new IOException("The object retrieved was not an ICState. ( " + o + " )");
	}

	public void ping() {
		this.lastAccessed = System.currentTimeMillis() / 1000;
	}

	public boolean isActive() {
		return (this.lastAccessed > 0) && ((this.lastAccessed + ICState.TIMEOUT) > (System.currentTimeMillis() / 1000));
	}

	public void save() throws IOException {
		if (!this.isDirty())
			return;

		FileWriter writer = new FileWriter(hafnium.bukkit.pail.stone.ic.ICFactory.getFileFor(this.getLocation()));

		Yaml y = new Yaml(new CustomClassLoaderConstructor(ICState.class.getClassLoader()));

		try {
			y.dump(this, writer);
		} finally {
			writer.close();
		}

		this.clean();
	}

	/**
	 * Called before the sign is unloaded.
	 */
	public void wrapUp(ICStub stub) {
		if (stub.isTicking())
			this.setWasTicking(true);
		else
			this.setWasTicking(false);
	}

	protected void dirty() {
		this.dirty = true;
	}

	private void clean() {
		this.dirty = false;
	}

	public boolean isDirty() {
		return this.dirty;
	}

	/**
	 * @return the location
	 */
	public SignLocation getLocation() {
		return this.location;
	}

	/**
	 * @param location
	 *            the location to set
	 */
	public void setLocation(SignLocation location) {
		this.location = location;
		this.dirty();
	}

	/**
	 * @return the ownerName
	 */
	public String getOwnerName() {
		return this.ownerName;
	}

	/**
	 * @param ownerName
	 *            the ownerName to set
	 */
	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
		this.dirty();
	}

	/**
	 * @return the type
	 */
	public ICType getType() {
		return this.type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(ICType type) {
		this.type = type;
		this.dirty();
	}

	/**
	 * @return the wasTicking
	 */
	public boolean getWasTicking() {
		return this.wasTicking;
	}

	/**
	 * @param wasTicking
	 *            the wasTicking to set
	 */
	public void setWasTicking(boolean wasTicking) {
		this.wasTicking = wasTicking;
		this.dirty();
	}

	/**
	 * @return the debugLocation
	 */
	public SignLocation getDebugLocation() {
		return this.debugLocation;
	}

	/**
	 * @param debugLocation
	 *            the debugLocation to set
	 */
	public void setDebugLocation(SignLocation debugLocation) {
		this.debugLocation = debugLocation;
		this.dirty();
	}

	private int flashTaskId = -1;

	private void flashDebug() {
		final SignText text = new BlockSignText(this.getDebugLocation());

		text.setLine(0, org.bukkit.ChatColor.LIGHT_PURPLE + ":debug");

		if (this.flashTaskId != -1)
			Bukkit.getScheduler().cancelTask(this.flashTaskId);
		Bukkit.getScheduler().scheduleSyncDelayedTask(PailStone.getInstance(), new Runnable() {

			@Override
			public void run() {
				text.setLine(0, org.bukkit.ChatColor.DARK_PURPLE + ":debug");
				ICState.this.flashTaskId = -1;
			}

		}, 10);
	}

	private void purgeMissingDebug() {
		if (this.getDebugLocation() == null)
			return;

		if (!this.getDebugLocation().isLoaded())
			return;

		if (!this.getDebugLocation().getBlock().getType().equals(Material.WALL_SIGN)) {
			if (this.flashTaskId != -1)
				Bukkit.getScheduler().cancelTask(this.flashTaskId);

			this.setDebugLocation(null);

			PailStone.getInstance().getLogger().info("[" + this.toString() + "] Debug sign was removed.");

		}
	}

	public boolean hasDebugSign() {
		this.purgeMissingDebug();
		return this.getDebugLocation() != null;
	}

	public void debug(String string) {

		if (!this.hasDebugSign())
			return;

		SignText text = new BlockSignText(this.getDebugLocation());

		text.setLine(1, "");
		text.setLine(2, "");
		text.setLine(3, "");

		String[] lines = string.split("\n", 3);
		for (int i = 0; i < lines.length; i++)
			text.setLine(i + 1, lines[i]);

		this.flashDebug();
	}

	@Override
	public String toString() {
		return this.type.name().toLowerCase() + ":" + this.location.toString();
	}
}
