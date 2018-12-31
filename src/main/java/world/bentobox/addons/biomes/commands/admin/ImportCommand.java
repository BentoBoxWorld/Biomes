package world.bentobox.addons.biomes.commands.admin;


import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import world.bentobox.addons.biomes.BiomesAddon;
import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;


/**
 * This class process "biomes import" command for admins.
 */
public class ImportCommand extends CompositeCommand
{
	public ImportCommand(Addon addon, CompositeCommand cmd)
	{
		super(addon, cmd, "import");
	}


	@Override
	public void setup()
	{
		this.setPermission("admin.biomes.import");
		this.setParametersHelp("biomes.commands.admin.import.parameters");
		this.setDescription("biomes.commands.admin.import.description");
	}


	@Override
	public boolean execute(User user, String label, List<String> args)
	{
		return ((BiomesAddon) this.getAddon()).getAddonManager().importBiomes(user,
			this.getWorld(),
			!args.isEmpty() && args.get(0).equalsIgnoreCase("overwrite"));
	}


	@Override
	public Optional<List<String>> tabComplete(User user, String alias, List<String> args)
	{
		String lastArg = !args.isEmpty() ? args.get(args.size() - 1) : "";
		return Optional.of(Util.tabLimit(Arrays.asList("overwrite"), lastArg));
	}
}
