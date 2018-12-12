package world.bentobox.addons.biomes.commands;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import world.bentobox.addons.biomes.BiomesAddon;
import world.bentobox.addons.biomes.objects.BiomesObject;
import world.bentobox.addons.biomes.panel.BiomesPanel;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;


/**
 * This class process "biomes" method.
 */
public class BiomesCommand extends CompositeCommand
{
	public BiomesCommand(BiomesAddon plugin, CompositeCommand bsbIslandCmd)
	{
		super(bsbIslandCmd, "biomes");
		this.addon = plugin;
	}


	@Override
	public void setup()
	{
		this.setPermission("island.biomes");
		this.setOnlyPlayer(true);
		this.setParametersHelp("biomes.help.parameters");
		this.setDescription("biomes.help.description");
	}


	@Override
	public boolean execute(User user, String string, List<String> args)
	{
		// Open up the biomes GUI
		if (user.isPlayer())
		{
			new BiomesPanel(
				this.addon,
				user,
				user,
				args.isEmpty() ? "" : args.get(0),
				this.getWorld(),
				this.getPermissionPrefix(),
				this.getTopLabel(),
				BiomesPanel.Mode.PLAYER);

			return true;
		}
		// Show help

		this.showHelp(this, user);
		return false;
	}


	@Override
	public Optional<List<String>> tabComplete(User user, String alias, List<String> args)
	{
		List<String> options = new ArrayList<>();
		final List<BiomesObject> biomesList = this.addon.getAddonManager().getBiomes();

		for (BiomesObject biome : biomesList)
		{
			options.add(biome.getFriendlyName());
		}

		return Optional.of(options);
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


	/**
	 * Variable stores biomes addon.
	 */
	private BiomesAddon addon;
}
