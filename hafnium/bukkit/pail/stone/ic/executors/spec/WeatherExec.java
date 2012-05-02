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
import hafnium.bukkit.pail.stone.parsing.ICParser;
import hafnium.bukkit.pail.stone.parsing.args.EnumCapture;
import hafnium.bukkit.pail.stone.trigger.EventArgs;
import hafnium.bukkit.pail.stone.trigger.hooks.SecondHook;
import hafnium.bukkit.pail.util.Weather;
import hafnium.bukkit.pail.util.sign.SignLocation;
import hafnium.bukkit.pail.util.sign.SignText;
import hafnium.bukkit.pail.util.text.MessageableException;

public class WeatherExec extends ICExecutor {
	@Override
	public ICState create(SignLocation loc, SignText text, String owner, ICParser signParser) throws MessageableException {
		WeatherState state = new WeatherState();

		EnumCapture<Weather> weather = new EnumCapture<Weather>("weather condition", Weather.class, Weather.SUNNY);

		signParser.getLine(1).add(weather);

		state.setWeather(weather.getResult());

		return state;
	}

	@Override
	public void initialize(ICStub ic) {
		ic.registerHook(new SecondHook(ic));
	}

	@Override
	public void trigger(ICStub ic, EventArgs event) {
		WeatherState state = ic.getState();
		if (state.getWeather().isOccuring(ic.getWorld()))
			ic.setOutput(0, true);
		else
			ic.setOutput(0, false);
	}

	public static class WeatherState extends ICState {
		private Weather weather;

		/**
		 * @return the weather
		 */
		public Weather getWeather() {
			return this.weather;
		}

		/**
		 * @param weather
		 *            the weather to set
		 */
		public void setWeather(Weather weather) {
			this.weather = weather;
			this.dirty();
		}

	}
}
