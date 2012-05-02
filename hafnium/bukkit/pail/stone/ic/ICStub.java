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
import hafnium.bukkit.pail.stone.ic.controllers.ICController;
import hafnium.bukkit.pail.stone.ic.states.ICState;
import hafnium.bukkit.pail.stone.trigger.EventArgs;
import hafnium.bukkit.pail.stone.trigger.EventHook;
import hafnium.bukkit.pail.stone.trigger.hooks.TickHook;
import hafnium.bukkit.pail.util.BlockLocation;
import hafnium.bukkit.pail.util.redstone.Current;
import hafnium.bukkit.pail.util.sign.SignLocation;

import java.io.IOException;

import org.bukkit.World;

// TODO: Log files anyone?

public class ICStub {
	private final ICType type;
	private final SignLocation location;

	private final ICExecutor exec;

	private ICController ctrl;
	private ICState state;

	private EventHook[] hooks;
	private TickHook tickHook;

	public ICStub(ICType type, SignLocation loc, ICState state, ICController ctrl) {
		this.type = type;
		this.location = loc;
		this.state = state;
		this.ctrl = ctrl;

		this.exec = this.type.getExecutor();

		this.hooks = new EventHook[0];

		if (state.getWasTicking())
			this.startTicking();
	}

	public SignLocation getLocation() {
		return this.location;
	}

	public ICType getType() {
		return this.type;
	}

	@SuppressWarnings("unchecked")
	public <T extends ICState> T getState() {
		if (!this.isLoaded())
			this.reload();

		this.state.ping();

		return (T) this.state;
	}

	@SuppressWarnings("unchecked")
	public <T extends ICState> T quietlyGetState() {
		if (!this.isLoaded())
			this.reload();

		return (T) this.state;
	}

	@SuppressWarnings("unchecked")
	public <T extends ICController> T getController() {
		if (!this.isLoaded())
			this.reload();

		this.state.ping();

		return (T) this.ctrl;
	}

	public void registerDebugSignLocation(SignLocation loc) {
		this.getState().setDebugLocation(loc);
	}

	/*
	 * Convenience Methods
	 */

	public World getWorld() {
		return this.location.getWorld();
	}

	public Current getInput(int i) {
		return this.getController().getInput(i);
	}

	public void setOutput(int i, boolean state) {
		this.getController().setOutput(i, state);
	}

	public Current getOutput(int i) {
		return this.getController().getOutput(i);
	}

	public void sendEvent(EventArgs event) {
		if (event.isNotable())
			PailStone.getInstance().getHookManager().trigger(event);
	}

	public BlockLocation getHostBlockLocation() {
		return this.getController().getHostBlockLocation();
	}

	// *** Internal management: ***

	/**
	 * Remove this sign from the system.
	 * 
	 * @param reason
	 */
	public void delete(String reason) {
		PailStone.getInstance().getRegistry().deregister(this);

		for (EventHook hook : this.hooks)
			PailStone.getInstance().getHookManager().deregister(hook);

		this.stopTicking();

		ICFactory.deleteFile(this);

		PailStone.getInstance().log(this + " has been removed. (" + reason + ")");
	}

	public boolean isLoaded() {
		return this.state != null;
	}

	public void unload() {
		this.unload(false);
	}

	public void unload(boolean wrapUp) {
		if (wrapUp)
			this.getState().wrapUp(this);

		if (this.isLoaded()) {
			if (this.state.isDirty())
				try {
					this.state.save();
				} catch (IOException e) {
					PailStone.getInstance().error("Could not save " + this + ". Some data was probably lost.", e);
				}

			this.ctrl = null;
			this.state = null;
		}
	}

	public void reload() {
		if (this.isLoaded())
			this.unload();

		try {
			this.ctrl = (ICController) this.getType().getController().getConstructors()[0].newInstance(this.getLocation());
			this.state = ICState.load(ICFactory.getFileFor(this.getLocation()));
		} catch (Exception e) {
			hafnium.bukkit.pail.stone.PailStone.getInstance().log("There was an error reloading " + this + ".");
			this.ctrl = null;
			this.state = null;
		}
	}

	public boolean isActive() {
		return this.isLoaded() && this.state.isActive();
	}

	// *** Hook Management: ***

	/**
	 * Called by the executor in the initialize method.
	 */
	public void registerHook(EventHook hook) {
		EventHook[] newHooks = new EventHook[this.hooks.length + 1];

		for (int i = 0; i < this.hooks.length; i++)
			newHooks[i] = this.hooks[i];

		newHooks[this.hooks.length] = hook;

		this.hooks = newHooks;

		PailStone.getInstance().getHookManager().register(hook);
	}

	public boolean trigger(EventArgs event) {
		if (!this.isValid()) {
			this.delete("Invalid on trigger.");
			return false;
		}

		boolean ctrlScreened = event.getType().isControllerPreprocessed();

		boolean ctrlRejected = false;

		if (ctrlScreened)
			ctrlRejected = this.getController().preprocess(event);

		if (!ctrlRejected) {
			this.exec.trigger(this, event);

			if (ctrlScreened)
				this.getController().postprocess();

			try {
				this.state.save();
			} catch (IOException e) {
				PailStone.getInstance().error("Failed to save " + this.toString() + ". The sign may corrupt when the server unloads.");
			}

			return true;
		}

		return false;
	}

	public void startTicking() {
		this.stopTicking();

		if (!this.isTicking()) {
			this.tickHook = new TickHook(this);
			PailStone.getInstance().getHookManager().register(this.tickHook);
		}
	}

	public void stopTicking() {
		if (this.isTicking()) {
			PailStone.getInstance().getHookManager().deregister(this.tickHook);
			this.tickHook = null;
		}
	}

	public boolean isTicking() {
		return this.tickHook != null;
	}

	public void defer(final DeferredOperation deferredOperation) {
		org.bukkit.Bukkit.getScheduler().scheduleSyncDelayedTask(PailStone.getInstance(), new Runnable() {
			@Override
			public void run() {
				deferredOperation.perform(ICStub.this);
			}

		});
	}

	/*
	 * Housekeeping:
	 */

	@Override
	public String toString() {
		return this.type.name().toLowerCase() + ":" + this.location.toString();
	}

	public boolean isValid() {
		if (!this.location.getChunk().isLoaded())
			return true;

		return this.location.getBlock().getType() == org.bukkit.Material.WALL_SIGN;
	}

	public static abstract class DeferredOperation {
		public abstract void perform(ICStub stub);
	}
}
