package world.bentobox.biomes.commands.user;


import java.util.*;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;
import world.bentobox.biomes.BiomesAddon;
import world.bentobox.biomes.commands.ExpandedCompositeCommand;
import world.bentobox.biomes.database.objects.BiomesObject;
import world.bentobox.biomes.config.Settings.UpdateMode;
import world.bentobox.biomes.tasks.BiomeUpdateHelper;
import world.bentobox.biomes.utils.Utils;


/**
 * This command process <code>/is biomes set</code> command.
 */
public class BiomesSetCommand extends ExpandedCompositeCommand
{
	public BiomesSetCommand(Addon addon, CompositeCommand command)
	{
		super(addon, command, "set");
	}


	@Override
	public void setup()
	{
		this.setPermission("biomes.set");
		this.setParametersHelp("biomes.commands.user.set.parameters");
		this.setDescription("biomes.commands.user.set.description");

		this.setOnlyPlayer(true);
	}


	@Override
	public boolean execute(User user, String label, List<String> args)
	{
		BiomesObject biome = this.getBiomeObject(args, 0, user);
		UpdateMode updateMode = this.getUpdateMode(args, 1, user);
		int size = this.getUpdateRange(args, 2, user);

		if (biome == null || updateMode == null || size < 1)
		{
			// Show help if something fails.
			this.showHelp(this, user);
			return false;
		}
		else
		{
			// Use BiomeUpdateHelper to change biome for user.

			BiomeUpdateHelper helper = new BiomeUpdateHelper(this.addon,
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

				return true;
			}

			return false;
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
				String worldName = this.getWorld() != null &&
					Utils.getGameMode(this.getWorld()) != null ?
					Utils.getGameMode(this.getWorld()) : "";

				List<BiomesObject> biomes = this.addon.getAddonManager().getBiomes(worldName);

				// Create suggestions with all biomes that is available for users.
				biomes.forEach(biomesObject -> {
					returnList.add(biomesObject.getUniqueId().substring(worldName.length() + 1));
				});

				break;
			case 4:
				// Create suggestions with all update modes that is available for users.
				Arrays.stream(UpdateMode.values()).
					map(Enum::name).
					forEach(returnList::add);

				if (!BiomesAddon.BIOMES_WORLD_PROTECTION.isSetForWorld(this.getWorld()))
				{
					// Do not suggest island as it is not valid option
					returnList.remove(UpdateMode.ISLAND.name());
				}

				break;
			case 5:
				if (lastString.isEmpty() || lastString.matches("[0-9]*"))
				{
					returnList.add("<number>");
				}

				break;
			default:
			{
				returnList.add("help");
				break;
			}
		}

		return Optional.of(Util.tabLimit(returnList, lastString));
	}
}
