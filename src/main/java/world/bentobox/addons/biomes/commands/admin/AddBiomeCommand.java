package world.bentobox.addons.biomes.commands.admin;


import org.bukkit.block.Biome;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import world.bentobox.addons.biomes.commands.ExpandedCompositeCommand;
import world.bentobox.addons.biomes.objects.BiomesObject;
import world.bentobox.addons.biomes.utils.Utils;
import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;


/**
 * This command allows to add new biome.
 * This will be dummy biome, without proper name and settings. All could be edited via /biomes edit command.
 */
public class AddBiomeCommand extends ExpandedCompositeCommand
{
	public AddBiomeCommand(Addon addon, CompositeCommand parent)
	{
		super(addon, parent, "add");
	}


	@Override
	public void setup()
	{
		this.setPermission("admin.biomes");
		this.setParametersHelp("biomes.admin.add.parameters");
		this.setDescription("biomes.admin.add.description");
	}


	@Override
	public boolean execute(User user, String label, List<String> args)
	{
		if (args.isEmpty())
		{
			this.showHelp(this, user);
			return false;
		}
		else if (args.size() > 1)
		{
			user.sendMessage("biomes.command.error.too-many-arguments");
			this.showHelp(this, user);
			return false;
		}
		else
		{
			Biome newBiome = Utils.getBiomeNameMap().getOrDefault(args.get(0).toUpperCase(), null);

			if (newBiome == null)
			{
				user.sendMessage("biomes.command.error.wrong-biome-name", "[biome]", args.get(0));
				return false;
			}
			else
			{
				BiomesObject biomesObject = new BiomesObject(newBiome);
				biomesObject.setFriendlyName(newBiome.name());
				biomesObject.setUniqueId(newBiome.name() + "-" + user.getUniqueId());
				this.addon.getAddonManager().storeBiome(new BiomesObject(newBiome), false, user, false);
				user.sendMessage("biomes.command.success.biome-created", "[biome]", args.get(0));

				return true;
			}
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
				// Add all biomes that is not in BiomesObject but is defined in game.
				returnList.addAll(Utils.getBiomeNameMap().keySet());

				// Remove biomes that is already added, to avoid overlaps.
				List<BiomesObject> biomes = this.addon.getAddonManager().getBiomes();
				biomes.forEach(biomesObject -> {
					returnList.remove(biomesObject.getBiomeName());
				});

				break;
			default:
			{
				break;
			}
		}

		return Optional.of(Util.tabLimit(returnList, lastString));
	}
}
