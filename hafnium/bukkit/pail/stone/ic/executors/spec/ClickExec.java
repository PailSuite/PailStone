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

import hafnium.bukkit.pail.stone.ic.ICExecutor;
import hafnium.bukkit.pail.stone.ic.ICStub;
import hafnium.bukkit.pail.stone.ic.states.ICState;
import hafnium.bukkit.pail.stone.ic.states.PulsingICState;
import hafnium.bukkit.pail.stone.parsing.ICParser;
import hafnium.bukkit.pail.stone.parsing.args.EnumCapture;
import hafnium.bukkit.pail.stone.parsing.args.RegionCapture;
import hafnium.bukkit.pail.stone.trigger.EventArgs;
import hafnium.bukkit.pail.stone.trigger.events.InteractEvent;
import hafnium.bukkit.pail.stone.trigger.hooks.InteractHook;
import hafnium.bukkit.pail.stone.trigger.hooks.InteractHook.ActionFilter;
import hafnium.bukkit.pail.util.AliasedEnum;
import hafnium.bukkit.pail.util.Region;
import hafnium.bukkit.pail.util.sign.SignLocation;
import hafnium.bukkit.pail.util.sign.SignText;
import hafnium.bukkit.pail.util.text.MessageableException;

public class ClickExec extends ICExecutor {

	@Override
	public ICState create(SignLocation loc, SignText text, String owner, ICParser signParser) throws MessageableException {
		ClickState state = new ClickState();

		RegionCapture region = new RegionCapture(loc);
		EnumCapture<ActionFilter> filter = new EnumCapture<ActionFilter>("action", ActionFilter.class, ActionFilter.BOTH);
		EnumCapture<Response> response = new EnumCapture<Response>("output response", Response.class, Response.PULSE);

		signParser.getLine(1).add(region.getCaptureA());
		signParser.getLine(2).add(region.getCaptureB());
		signParser.getLine(3).add(filter).add(response);

		state.setClickRegion(region.getResult());
		state.setFilter(filter.getResult());
		state.setResponse(response.getResult());

		return state;
	}

	@Override
	public void initialize(ICStub ic) {
		ClickState state = ic.getState();

		ic.registerHook(new InteractHook(ic, state.getClickRegion(), state.getFilter()));
	}

	@Override
	public void trigger(ICStub ic, EventArgs event) {
		ClickState state = ic.getState();

		switch (event.getType()) {
		case INTERACT:
			state.debug("Triggered by\n" + ((InteractEvent) event).getPlayer().getName());

			switch (state.getResponse()) {
			case TOGGLE:
				ic.setOutput(0, !ic.getOutput(0).asBoolean());
				break;
			case PULSE:
				state.doStartPulse();
				ic.startTicking();
				ic.setOutput(0, true);
				break;
			}
			break;
		case TICK:
			if (state.getTicksLeft() <= 0) {
				ic.stopTicking();
				ic.setOutput(0, false);
			}

			break;
		}
	}

	public static enum Response implements AliasedEnum {
		PULSE("p"), TOGGLE("t", "switch");

		private final String[] aliases;

		private Response(String... aliases) {
			this.aliases = aliases;
		}

		@Override
		public String[] getAliases() {
			return this.aliases;
		}
	}

	public static class ClickState extends PulsingICState {
		private Region clickRegion;
		private ActionFilter filter;
		private Response response;

		/**
		 * @return the clickRegion
		 */
		public Region getClickRegion() {
			return this.clickRegion;
		}

		/**
		 * @param clickRegion
		 *            the clickRegion to set
		 */
		public void setClickRegion(Region clickRegion) {
			this.clickRegion = clickRegion;
			this.dirty();
		}

		/**
		 * @return the filter
		 */
		public ActionFilter getFilter() {
			return this.filter;
		}

		/**
		 * @param filter
		 *            the filter to set
		 */
		public void setFilter(ActionFilter filter) {
			this.filter = filter;
			this.dirty();
		}

		/**
		 * @return the response
		 */
		public Response getResponse() {
			return this.response;
		}

		/**
		 * @param response
		 *            the response to set
		 */
		public void setResponse(Response response) {
			this.response = response;
			this.dirty();
		}

	}
}
