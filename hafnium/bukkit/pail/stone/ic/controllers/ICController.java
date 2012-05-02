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
package hafnium.bukkit.pail.stone.ic.controllers;

import hafnium.bukkit.pail.stone.ic.ICStub;
import hafnium.bukkit.pail.stone.trigger.EventArgs;
import hafnium.bukkit.pail.stone.trigger.HookFactory;
import hafnium.bukkit.pail.stone.trigger.events.RedstoneEvent;
import hafnium.bukkit.pail.util.BlockLocation;
import hafnium.bukkit.pail.util.redstone.Current;
import hafnium.bukkit.pail.util.sign.SignLocation;

public abstract class ICController {
	protected final Output[] outputs;
	protected final Input[] inputs;

	private final SignLocation loc;

	/**
	 * Constructs an IC with the specified number of inputs and outputs.
	 * 
	 * @param numInputs
	 * @param numOutputs
	 */
	protected ICController(int numInputs, int numOutputs, SignLocation loc) {
		this.inputs = new Input[numInputs];
		this.outputs = new Output[numOutputs];
		this.loc = loc;
	}

	public int getInputCount() {
		return this.inputs.length;
	}

	public int getOutputCount() {
		return this.outputs.length;
	}

	/**
	 * Returns the state of the requested input.
	 * 
	 * @param index
	 * @return
	 */
	public final Current getInput(int index) {
		return this.inputs[index].getCurrent();
	}

	/**
	 * Returns the state of the requested output.
	 * 
	 * @param index
	 * @return
	 */
	public final Current getOutput(int index) {
		return this.outputs[index].getCurrent();
	}

	/**
	 * Sets the specified output to the specified position.
	 * 
	 * @param index
	 * @param position
	 * @return Sucess. False means that there is no lever, or that it is
	 *         unloaded.
	 */
	public boolean setOutput(int index, boolean position) {
		return this.outputs[index].setOutput(position);
	}

	public void registerInputs(ICStub stub) {
		for (Input input : this.inputs)
			stub.registerHook(HookFactory.makeRedstoneHook(stub, input));
	}

	public void forceInputUpdate() {
		for (int i = 0; i < this.getInputCount(); i++)
			this.forceInputUpdate(i);
	}

	public void forceInputUpdateExcluding(int index) {
		for (int i = 0; i < this.getInputCount(); i++)
			if (i != index)
				this.forceInputUpdate(i);
	}

	public void forceInputUpdate(int index) {
		this.inputs[index].forceUpdate();
	}

	/**
	 * Preprocesses an event.
	 * 
	 * @param event
	 * @return true if the event has been rejected and should not be passed on.
	 */
	public boolean preprocess(EventArgs event) {
		switch (event.getType()) {
		case REDSTONE:
			RedstoneEvent sEvent = (RedstoneEvent) event;

			int updated = -1;

			for (int i = 0; i < this.getInputCount(); i++)
				if (this.inputs[i].getLocation().locates(sEvent.getWrappedEvent().getBlock()))
					if (this.inputs[i].isReady()) {
						this.inputs[i].apply(sEvent);
						updated = i;
					} else
						return true;

			this.forceInputUpdateExcluding(updated);

			break;
		default:
			this.forceInputUpdate();

			break;
		}

		return false;
	}

	/**
	 * Returns this object to a static state.
	 */
	public void postprocess() {
		for (Input i : this.inputs)
			i.conclude();
	}

	public void offload() {

	}

	public BlockLocation getHostBlockLocation() {
		return this.loc.getHostBlockLocation();
	}
}
