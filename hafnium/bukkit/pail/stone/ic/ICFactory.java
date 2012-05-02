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
import hafnium.bukkit.pail.stone.parsing.ICParser;
import hafnium.bukkit.pail.util.BlockLocation;
import hafnium.bukkit.pail.util.hf;
import hafnium.bukkit.pail.util.sign.BlockSignText;
import hafnium.bukkit.pail.util.sign.EventSignText;
import hafnium.bukkit.pail.util.sign.SignLocation;
import hafnium.bukkit.pail.util.sign.SignText;
import hafnium.bukkit.pail.util.text.MessageableException;
import hafnium.bukkit.pail.util.text.PailMessage;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

public class ICFactory {
	private static final String IC_EXTENSION = ".psic";

	private static File icDir;

	static {
		icDir = new File(PailStone.getInstance().getDataFolder(), "ic");
		if (!icDir.exists())
			icDir.mkdirs();
	}

	public static File getICDir() {
		return icDir;
	}

	private static Pattern icName = Pattern.compile("^\\:([a-zA-Z]+)$");

	public static ICType getICType(SignText text) {
		Matcher m = icName.matcher(org.bukkit.ChatColor.stripColor(text.getLine(0)).trim());

		if (!m.matches())
			return null;

		String supplied = m.group(1);

		for (ICType type : ICType.values())
			if (type.name().equalsIgnoreCase(supplied) || hf.containsIgnoreCase(type.getAliases(), supplied))
				return type;

		return null;
	}

	public static void formatICName(SignText text) {
		ICType type = getICType(text);

		if (type != null)
			text.setLine(0, type.getSignName());
	}

	public static boolean parseSignChange(SignChangeEvent event) {
		Result res = createIC(event);
		if (res == Result.SUCCESS)
			return true;
		return false;
	}

	public static Result createIC(SignChangeEvent event) {
		return createIC(new BlockLocation(event.getBlock().getLocation()), new EventSignText(event), event.getPlayer().getName(), false, false);
	}

	public static Result createIC(BlockLocation location, String owner, boolean forceLoad, boolean quiet) {
		return createIC(location, null, owner, forceLoad, quiet);
	}

	public static Result createIC(BlockLocation location, SignText text, String owner, boolean forceLoad, boolean quiet) {
		return createIC(location, text, owner, forceLoad, quiet, null);
	}

	/**
	 * Creates an IC at the specified location. If the player is online, this
	 * will run a permissions check. If not, it will assume that the player has
	 * the proper permissions.
	 * 
	 * @param location
	 * @param text
	 * @param owner
	 * @throws MessageableException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 */
	public static Result createIC(BlockLocation location, SignText text, String owner, boolean forceLoad, boolean quiet, String legacyData) {
		if ((location == null) || (owner == null))
			throw new IllegalArgumentException("The location and owner name MUST be non-null.");

		World world = location.getWorld();

		if (world == null)
			return Result.ERR_WORLD_UNLOADED;

		Chunk chunk = world.getChunkAt(location.asLocation());

		if (!chunk.isLoaded()) {
			if (!forceLoad)
				return Result.ERR_CHUNK_UNLOADED;

			if (!chunk.load())
				return Result.ERR_CHUNK_UNLOADED;
		}

		SignLocation loc;

		if (text == null) {
			BlockState state = location.getBlock().getState();

			if (!(state instanceof Sign))
				return Result.ERR_NO_SIGN;

			Sign sign = (Sign) state;

			text = new BlockSignText(sign);

			loc = new SignLocation(sign);
		} else
			loc = text.getLocation();

		ICType type = getICType(text);

		if (type == null)
			return Result.ERR_NO_IC;

		Player pOwner = Bukkit.getPlayer(owner);

		if (pOwner != null)
			if (!pOwner.hasPermission(type.getPermission()))
				return Result.ERR_NO_PERMISSION;

		ICExecutor exec = type.getExecutor();

		try {
			ICState state;
			ICParser parser = new ICParser(loc, text, owner);

			if (legacyData != null)
				state = exec.legacyCreate(loc, text, owner, legacyData, parser);
			else
				state = exec.create(loc, text, owner, parser);

			state.setLocation(loc);
			state.setOwnerName(owner);
			state.setType(type);

			ICController ctrl = (ICController) type.getController().getConstructors()[0].newInstance(loc);
			ICStub stub = new ICStub(type, loc, state, ctrl);

			PailStone.getInstance().getRegistry().register(stub);

			ctrl.registerInputs(stub);
			exec.initialize(stub);

			state.save();

			type.applyToText(text);

			if (!quiet) {
				PailMessage msg = PailMessage.from("^gCreated ^n" + type.name() + " IC^g.");
				if (pOwner != null)
					msg.sendTo(pOwner);
				else
					msg.logAs(Level.INFO, PailStone.getInstance());
			}

			return Result.SUCCESS;
		} catch (MessageableException e) {
			PailMessage msg = e.getPSMessage();

			if (quiet || (pOwner == null))
				msg.logAs(Level.FINE, PailStone.getInstance());
			else
				msg.sendTo(pOwner);

			return Result.ERR_IC_SYNTAX;
		} catch (Exception e) {
			PailStone.getInstance().getLogger().log(Level.SEVERE, "There was a serious error while instantiating a:\n\t" + type.name() + " IC", e);
			PailStone.getInstance().getLogger().log(Level.INFO, "The error was handled and will not affect server stability.");
			return Result.ERR_IMPLEMENTATION;
		}
	}

	public static enum Result {
		SUCCESS, ERR_IMPLEMENTATION, ERR_NO_SIGN, ERR_NO_IC, ERR_IC_SYNTAX, ERR_NO_PERMISSION, ERR_CHUNK_UNLOADED, ERR_WORLD_UNLOADED;
	}

	public static void breakSign(BlockLocation blockLocation) {
		ICStub stub;
		while ((stub = PailStone.getInstance().getRegistry().getICAt(blockLocation)) != null)
			stub.delete("Block Broken");
	}

	public static void load() {
		File[] ics = getICDir().listFiles(new ICFileFilter());

		for (File icf : ics)
			try {
				ICState state = ICState.load(icf);
				ICType type = state.getType();
				SignLocation location = state.getLocation();

				ICController ctrl = (ICController) (type.getController().getConstructors()[0].newInstance(state.getLocation()));

				ICStub stub = new ICStub(type, location, state, ctrl);

				PailStone.getInstance().getRegistry().register(stub);

				ctrl.registerInputs(stub);

				type.getExecutor().initialize(stub);

				state.save();
			} catch (Exception e) {
				PailStone.getInstance().log("Error loading " + icf.getName() + ":");
				e.printStackTrace();
			}
	}

	public static File getFileFor(BlockLocation loc) {
		return new File(getICDir(), loc.toString() + IC_EXTENSION);

	}

	private static class ICFileFilter implements FileFilter {
		@Override
		public boolean accept(File file) {
			return file.getName().toLowerCase().endsWith(IC_EXTENSION);
		}
	}

	/**
	 * Archive the stub on deletion.
	 * 
	 * @param stub
	 */
	public static void deleteFile(ICStub stub) {
		File original = getFileFor(stub.getLocation());
		original.delete();
	}
}
