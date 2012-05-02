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
package hafnium.bukkit.pail.stone.bukkitevent;

import hafnium.bukkit.pail.stone.PailStone;
import hafnium.bukkit.pail.stone.ic.ICFactory;
import hafnium.bukkit.pail.stone.ic.ICStub;
import hafnium.bukkit.pail.stone.ic.ICType;
import hafnium.bukkit.pail.util.BlockLocation;
import hafnium.bukkit.pail.util.sign.EventSignText;
import hafnium.bukkit.pail.util.sign.SignLocation;
import hafnium.bukkit.pail.util.sign.SignText;
import hafnium.bukkit.pail.util.text.PailMessage;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;

public class SignListener implements Listener {
	private final PailStone plugin;

	public SignListener(PailStone plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		this.plugin = plugin;
	}

	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		if (!event.getBlock().getType().equals(org.bukkit.Material.WALL_SIGN))
			return;

		ICFactory.breakSign(new BlockLocation(event.getBlock().getLocation()));

		if (ICFactory.parseSignChange(event))
			return;

		SignText text = new EventSignText(event);

		if (!(text.getLine(0).trim().equals(":debug")))
			return;

		SignLocation loc = new SignLocation((Sign) event.getBlock().getState());

		BlockLocation searchLoc = loc.getRelative(0, 0, -1);

		Block found = searchLoc.getBlock();

		if ((found == null) || !found.getType().equals(org.bukkit.Material.WALL_SIGN)) {
			this.invalidDebug(text, event);
			return;
		}

		Sign foundSign = (Sign) found.getState();

		if (!this.plugin.getRegistry().isIC(foundSign)) {
			this.invalidDebug(text, event);
			return;
		}

		ICStub stub = this.plugin.getRegistry().getICAt(searchLoc);

		if (!stub.getType().hasDebug()) {
			this.noDebug(text, event, stub.getType());
			return;
		}

		text.paintLine(0, org.bukkit.ChatColor.DARK_PURPLE);

		stub.registerDebugSignLocation(loc);
	}

	private void noDebug(SignText text, SignChangeEvent event, ICType type) {
		text.paintLine(0, org.bukkit.ChatColor.DARK_GRAY);
		PailMessage.from("^eICs of type ^c" + type + " ^edo not have any debug information.").sendTo(event.getPlayer());
	}

	private void invalidDebug(SignText text, SignChangeEvent event) {
		text.paintLine(0, org.bukkit.ChatColor.DARK_GRAY);
		PailMessage.from("^eDebug signs must be placed over valid PailStone ICs.").sendTo(event.getPlayer());
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.getBlock().getType() == org.bukkit.Material.WALL_SIGN)
			ICFactory.breakSign(new BlockLocation(event.getBlock().getLocation()));
	}
}
