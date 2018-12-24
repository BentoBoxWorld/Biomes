package world.bentobox.addons.biomes.commands.user;


import java.util.*;

import world.bentobox.addons.biomes.BiomesAddon;
import world.bentobox.addons.biomes.objects.BiomesObject;
import world.bentobox.addons.biomes.tasks.BiomeUpdateHelper;
import world.bentobox.addons.biomes.utils.Utils;
import world.bentobox.addons.biomes.utils.Utils.UpdateMode;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;


/**
 * This command process <code>/is biomes set</code> command.
 */
public class BiomesSetCommand extends CompositeCommand
{
	public BiomesSetCommand(BiomesAddon addon, CompositeCommand command)
	{
		super(addon, command, "set");
	}


	@Override
	public void setup()
	{
		this.setPermission("island.biomes");
		this.setParametersHelp("biomes.set.parameters");
		this.setDescription("biomes.set.description");

		this.setOnlyPlayer(true);
	}


	@Override
	public boolean execute(User user, String label, List<String> args)
	{
		BiomesObject biome = this.getBiomeObject(args, user);
		UpdateMode updateMode = this.getUpdateMode(args, user);
		int size = this.getUpdateRange(args, user);

		if (biome == null || updateMode == null || size < 1)
		{
			// Show help if something fails.
			this.showHelp(this, user);
			return false;
		}
		else
		{
			// Use BiomeUpdateHelper to change biome for user.

			BiomeUpdateHelper helper = new BiomeUpdateHelper(this.getParent().getAddon(),
				user,
				user,
				biome,
				user.getWorld(),
				updateMode,
				size,
				true);

			if (helper.canChangeBiome())
			{
				helper.updateIslandBiome();
			}

			return true;
		}
	}


	@Override
	public Optional<List<String>> tabComplete(User user, String alias, List<String> args)
	{
		String lastString = args.get(args.size() - 1);

		final List<String> returnList = new ArrayList<>();
		final int size = args.size();

		switch (size)
		{
			case 3:
				List<BiomesObject> biomes =
					((BiomesAddon) this.getParent().getAddon()).getAddonManager().getBiomes();

				// Create suggestions with all biomes that is available for users.

				biomes.forEach(biomesObject -> {
					returnList.addAll(Util.tabLimit(
						Collections.singletonList(biomesObject.getBiomeName()), lastString));
				});

				break;
			case 4:
				// Create suggestions with all biomes that is available for users.

				returnList.addAll(Util.tabLimit(Collections.singletonList("ISLAND"), lastString));
				returnList.addAll(Util.tabLimit(Collections.singletonList("CHUNK"), lastString));
				returnList.addAll(Util.tabLimit(Collections.singletonList("SQUARE"), lastString));

				break;
			case 5:
				if (lastString.isEmpty() || lastString.matches("[0-9]*"))
				{
					returnList.addAll(Util.tabLimit(Collections.singletonList("<number>"), lastString));
				}

				break;
			default:
			{
				returnList.addAll(Util.tabLimit(Collections.singletonList("help"), lastString));
				break;
			}
		}

		return Optional.of(returnList);
	}



// ---------------------------------------------------------------------
// Section: Private methods
// ---------------------------------------------------------------------


	/**
	 * This method returns BiomesObject or null.
	 * @param args Args that contains all command arguments
	 * @param user Caller user.
	 * @return BiomesObject or null.
	 */
	private BiomesObject getBiomeObject(List<String> args, User user)
	{
		if (!args.isEmpty())
		{
			BiomesObject biome = ((BiomesAddon) this.getParent().getAddon()).getAddonManager().
				getBiomeFromString(args.get(0));

			if (biome == null)
			{
				user.sendMessage(user.getTranslation("biomes.command.error.wrong-biome-name",
					"[biome]",
					args.get(0)));
			}

			return biome;
		}
		else
		{
			user.sendMessage(user.getTranslation("biomes.command.error.biome-not-defined"));
			return null;
		}
	}


	/**
	 * This method returns Update Mode type.
	 * @param args Args that contains all command arguments
	 * @param user Caller user.
	 * @return UpdateMode or null.
	 */
	private UpdateMode getUpdateMode(List<String> args, User user)
	{
		if (args.size() > 1)
		{
			UpdateMode mode = Utils.parseStrictToUpdateMode(args.get(1));

			if (mode == null)
			{
				user.sendMessage(user.getTranslation("biomes.command.error.wrong-mode-name",
					"[mode]",
					args.get(1)));
			}

			return  mode;
		}
		else
		{
			return Utils.parseStringToUpdateMode(
				this.getParent().getAddon().getConfig().getString("defaulttype"));
		}
	}


	/**
	 * This method returns third parameter, that is expected to be integer which represents update distance
	 * for chunk and square mode.
	 * @param args List of arguments that is passed via set command.
	 * @param user User that calls current method.
	 * @return Integer that represents update range distance.
	 */
	private int getUpdateRange(List<String> args, User user)
	{
		if (args.size() > 2)
		{
			int size;

			try
			{
				size = Integer.parseInt(args.get(2));
			}
			catch (Exception e)
			{
				size = -1;
			}

			if (size < 1)
			{
				user.sendMessage(user.getTranslation("biomes.command.error.incorrect-size",
					TextVariables.NUMBER,
					args.get(2)));
			}

			return size;
		}
		else
		{
			return this.getParent().getAddon().getConfig().getInt("defaultsize", 1);
		}
	}
}
