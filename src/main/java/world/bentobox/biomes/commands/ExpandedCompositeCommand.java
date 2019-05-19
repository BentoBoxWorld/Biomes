package world.bentobox.biomes.commands;


import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;
import world.bentobox.biomes.BiomesAddon;
import world.bentobox.biomes.database.objects.BiomesObject;
import world.bentobox.biomes.config.Settings.UpdateMode;


/**
 * This is simple abstract class that defines Common methods used a lot in BiomeAddon commands to avoid code
 * duplications.
 */
public abstract class ExpandedCompositeCommand extends CompositeCommand
{
	public ExpandedCompositeCommand(Addon addon, CompositeCommand parent, String command)
	{
		super(addon, parent, command);
		this.addon = (BiomesAddon) addon;
	}


	/**
	 * This method returns BiomesObject or null.
	 * @param args Args that contains all command arguments
	 * @param index Index of argument.
	 * @param user Caller user.
	 * @return BiomesObject or null.
	 */
	protected BiomesObject getBiomeObject(List<String> args, int index, User user)
	{
		if (args.size() > index)
		{
			String uniqueID = args.get(index).toLowerCase();
			String worldName = Util.getWorld(this.getWorld()).getName();

			if (!uniqueID.startsWith(worldName))
			{
				uniqueID = worldName  + "_" + uniqueID;
			}

			BiomesObject biome = this.addon.getAddonManager().getBiomeFromString(uniqueID);

			if (biome == null)
			{
				user.sendMessage(user.getTranslation("biomes.errors.incorrect-object",
					"[biome]",
					args.get(index)));
			}

			return biome;
		}
		else
		{
			user.sendMessage(user.getTranslation("biomes.errors.missing-biome"));
			return null;
		}
	}


	/**
	 * This method returns Update Mode type.
	 * @param args Args that contains all command arguments
	 * @param index Index of argument.
	 * @param user Caller user.
	 * @return UpdateMode or null.
	 */
	protected UpdateMode getUpdateMode(List<String> args, int index, User user)
	{
		if (args.size() > index)
		{
			UpdateMode mode = UpdateMode.getMode(args.get(index));

			if (mode == null)
			{
				user.sendMessage(user.getTranslation("biomes.errors.incorrect-mode",
					"[mode]",
					args.get(index)));
			}

			return  mode;
		}
		else
		{
			return this.addon.getSettings().getDefaultMode();
		}
	}


	/**
	 * This method returns integer from args with given index which represents update distance for chunk and
	 * square mode.
	 * @param args List of arguments that is passed via set command.
	 * @param index Index of argument.
	 * @param user User that calls current method.
	 * @return Integer that represents update range distance.
	 */
	protected int getUpdateRange(List<String> args, int index, User user)
	{
		if (args.size() > index)
		{
			int range;

			try
			{
				range = Integer.parseInt(args.get(index));
			}
			catch (Exception e)
			{
				range = -1;
			}

			if (range < 1)
			{
				user.sendMessage(user.getTranslation("biomes.errors.incorrect-range",
					TextVariables.NUMBER,
					args.get(index)));
			}

			return range;
		}
		else
		{
			return this.addon.getSettings().getDefaultSize();
		}
	}


	/**
	 * This method returns User from arguments in given index. If Object is not a player, then it send error
	 * message to given user and returns null.
	 * @param args List of arguments that is passed via set command.
	 * @param index Index of argument.
	 * @param user User that calls current method.
	 * @return User Object or null.
	 */
	protected User getPlayer(List<String> args, int index, User user)
	{
		if (args.size() > index)
		{
			UUID targetUUID = this.getPlayers().getUUID(args.get(index));

			if (targetUUID == null)
			{
				user.sendMessage("general.errors.unknown-player",
					TextVariables.NAME,
					args.get(index));
				return null;
			}

			if (!this.getPlugin().getIslands().hasIsland(this.getWorld(), targetUUID))
			{
				user.sendMessage("general.errors.player-has-no-island");
				return null;
			}

			return User.getInstance(targetUUID);
		}
		else
		{
			user.sendMessage(user.getTranslation("biomes.errors.missing-user"));
			return null;
		}
	}


	/**
	 * This method transforms stringList to string by appending each list at the end.
	 * @param stringList List with strings that must be concasted.
	 * @return String from stringList.
	 */
	protected String getSingleLineDescription(List<String> stringList)
	{
		if (stringList.size() > 1)
		{
			Iterator<String> iter = stringList.iterator();

			StringBuilder returnString = new StringBuilder(iter.next());

			while (iter.hasNext())
			{
				returnString.append(" ").append(iter.next());
			}

			return returnString.toString();
		}
		else
		{
			return stringList.get(0);
		}
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


	protected BiomesAddon addon;
}
