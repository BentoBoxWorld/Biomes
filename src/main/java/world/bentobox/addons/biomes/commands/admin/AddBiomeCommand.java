package world.bentobox.addons.biomes.commands.admin;


import org.bukkit.block.Biome;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import world.bentobox.addons.biomes.commands.ExpandedCompositeCommand;
import world.bentobox.addons.biomes.objects.BiomesObject;
import world.bentobox.addons.biomes.panel.BiomesPanel;
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
		this.setPermission("admin.biomes.add");
		this.setParametersHelp("biomes.commands.admin.add.parameters");
		this.setDescription("biomes.commands.admin.add.description");
	}


	@Override
	public boolean execute(User user, String label, List<String> args)
	{
		if (user.isPlayer() && args.isEmpty())
		{
			// Shows BiomesPanel in Edit mode.
			new BiomesPanel(this.addon,
				user,
				new BiomesObject(),
				this.getWorld(),
				"",
				BiomesPanel.Mode.EDIT);

			return true;
		}
		else
		if (args.isEmpty())
		{
			this.showHelp(this, user);
			return false;
		}
		else if (args.size() > 1)
		{
			user.sendMessage("biomes.messages.errors.too-many-arguments");
			this.showHelp(this, user);
			return false;
		}
		else
		{
			Biome newBiome = Utils.getBiomeNameMap().getOrDefault(args.get(0).toUpperCase(), null);

			if (newBiome == null)
			{
				user.sendMessage("biomes.messages.errors.incorrect-biome",
					"[biome]",
					args.get(0));
				return false;
			}
			else
			{
				BiomesObject biomesObject = new BiomesObject(newBiome);
				biomesObject.setFriendlyName(newBiome.name());
				biomesObject.setUniqueId(newBiome.name().toLowerCase());

				if (this.addon.getAddonManager().storeBiome(biomesObject, false, user, false))
				{
					user.sendMessage("biomes.messages.information.biome-created",
						"[biome]",
						args.get(0));
					return true;
				}

				return false;
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
