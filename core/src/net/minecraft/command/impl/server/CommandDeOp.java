package net.minecraft.command.impl.server;

import java.util.List;

import net.minecraft.command.impl.core.CommandBase;
import net.minecraft.command.impl.core.CommandException;
import net.minecraft.command.api.ICommandSender;
import net.minecraft.command.impl.core.WrongUsageException;
import net.minecraft.entity.player.MPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.functional.StringUtils;

public class CommandDeOp extends CommandBase {

	/**
	 * Gets the name of the command
	 */
	public String getCommandName() {
		return "deop";
	}

	/**
	 * Return the required permission level for this command.
	 */
	public int getRequiredPermissionLevel() {
		return 3;
	}

	/**
	 * Gets the usage string for the command.
	 */
	public String getCommandUsage(ICommandSender sender) {
		return "commands.deop.usage";
	}

	/**
	 * Callback when the command is invoked
	 */
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		if (args.length == 1 && args[0].length() > 0) {
			MinecraftServer minecraftserver = MinecraftServer.getServer();
			MPlayer player = minecraftserver.getConfigurationManager().getPlayerByUsername(args[0]);

			if (player == null)
				throw new CommandException("commands.deop.failed", args[0]);
			player.setPlayerPermission(0);
			notifyOperators(sender, this, "commands.deop.success", args[0]);
		} else {
			throw new WrongUsageException("commands.deop.usage");
		}
	}

	public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
		return args.length == 1 ? StringUtils.filterCompletions(args, MinecraftServer.getServer().getAllUsernames()) : null;
	}

}