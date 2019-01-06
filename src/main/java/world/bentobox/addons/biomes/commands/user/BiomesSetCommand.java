package world.bentobox.addons.biomes.commands.user;


import java.util.*;

import world.bentobox.addons.biomes.commands.ExpandedCompositeCommand;
import world.bentobox.addons.biomes.objects.BiomesObject;
import world.bentobox.addons.biomes.tasks.BiomeUpdateHelper;
import world.bentobox.addons.biomes.utils.Utils.UpdateMode;
import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;


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
		this.setParametersHelp("biomes.commands.set.parameters");
		this.setDescription("biomes.commands.set.description");

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
				List<BiomesObject> biomes = this.addon.getAddonManager().getBiomes(this.getWorld());

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
}
