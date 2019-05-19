package world.bentobox.biomes.commands.user;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;
import world.bentobox.biomes.commands.ExpandedCompositeCommand;
import world.bentobox.biomes.database.objects.BiomesObject;


/**
 * This method returns information about provided biome in chat.
 */
public class BiomesInfoCommand extends ExpandedCompositeCommand
{
	public BiomesInfoCommand(Addon addon, BiomesCommand command)
	{
		super(addon, command, "info");
	}

	@Override
	public void setup()
	{
		this.setPermission("biomes.info");
		this.setParametersHelp("biomes.commands.user.info.parameters");
		this.setDescription("biomes.commands.user.info.description");

		this.setOnlyPlayer(true);
	}


	@Override
	public boolean execute(User user, String label, List<String> args)
	{
		BiomesObject biomesObject = this.getBiomeObject(args, 0, user);

		if (biomesObject != null)
		{
			user.sendMessage("biomes.information.header", "[name]", biomesObject.getFriendlyName());
			user.sendMessage("biomes.information.type", "[type]", biomesObject.getBiome().name());
			user.sendMessage("biomes.information.description", "[description]", this.getSingleLineDescription(biomesObject.getDescription()));
			user.sendMessage("biomes.information.level", "[level]", Long.toString(biomesObject.getRequiredLevel()));
			user.sendMessage("biomes.information.cost","[cost]", Integer.toString(biomesObject.getRequiredCost()));

			biomesObject.getRequiredPermissions().forEach(s -> {
				user.sendMessage("biomes.information.permission","[permission]", s);
			});

			return true;
		}
		else
		{
			this.showHelp(this, user);
			return false;
		}
	}


	@Override
	public Optional<List<String>> tabComplete(User user, String alias, List<String> args)
	{
 		if (args.size() > 3)
		{
			// Show biome for first tab.
			return Optional.of(new ArrayList<>());
		}

		String lastString = args.get(args.size() - 1);

		final List<String> returnList = new ArrayList<>();

		List<BiomesObject> biomes = this.addon.getAddonManager().getBiomes(this.getWorld());

		// Create suggestions with all biomes that is available for users.

		biomes.forEach(biomesObject -> {
			returnList.addAll(Util.tabLimit(
				Collections.singletonList(biomesObject.getBiome().name()), lastString));
		});

		return Optional.of(returnList);
	}
}
