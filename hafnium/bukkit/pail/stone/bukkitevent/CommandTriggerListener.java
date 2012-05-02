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
import hafnium.bukkit.pail.stone.trigger.events.TriggerEvent;
import hafnium.bukkit.pail.util.commands.CommandDefinition;
import hafnium.bukkit.pail.util.commands.CommandExec;
import hafnium.bukkit.pail.util.commands.node.StringNode;
import hafnium.bukkit.pail.util.commands.node.StructuralNode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandTriggerListener implements Listener {

	/** The plugin. */
	private final PailStone plugin;

	/** The exec. */
	private TriggerExecutor exec;

	/**
	 * Instantiates a new command trigger listener.
	 * 
	 * @param plugin
	 *            the plugin
	 * @param triggerCommand
	 *            the trigger command
	 */
	public CommandTriggerListener(PailStone plugin, PluginCommand triggerCommand) {
		this.plugin = plugin;

		if (plugin.getConfig().getBoolean("greedy-triggers"))
			plugin.getServer().getPluginManager().registerEvents(this, plugin);

		StringNode trigger;

		StructuralNode root = new StructuralNode();
		root.addNode(trigger = new StringNode("trigger name"));
		trigger.setExecutor(this.exec = new TriggerExecutor());

		triggerCommand.setExecutor(root);
	}

	/** The trigger command. */
	private static Pattern triggerCommand = Pattern.compile("^[/]([^ ]+)$");

	/**
	 * On command preprocess.
	 * 
	 * @param event
	 *            the event
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
		Matcher cmdMatcher = triggerCommand.matcher(event.getMessage());
		if (cmdMatcher.matches())
			this.exec.trigger(event.getPlayer(), cmdMatcher.group(1));
	}

	/**
	 * The Class TriggerExecutor.
	 */
	public class TriggerExecutor implements CommandDefinition {

		/**
		 * Trigger.
		 * 
		 * @param sender
		 *            the sender
		 * @param trigger
		 *            the trigger
		 * @return true, if successful
		 */
		@CommandExec
		public boolean trigger(CommandSender sender, String trigger) {
			TriggerEvent event = new TriggerEvent(trigger, sender);
			CommandTriggerListener.this.plugin.getHookManager().trigger(event);
			return event.isHandled();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see hafnium.bukkit.pail.util.commands.CommandDefinition#getHelp()
		 */
		@Override
		public String getHelp() {
			return "Toggles all :trigger ICs that listen for the specified trigger.";
		}
	}
}
