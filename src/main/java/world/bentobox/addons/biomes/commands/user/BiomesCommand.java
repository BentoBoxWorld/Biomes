package world.bentobox.addons.biomes.commands.user;


import java.util.List;

import world.bentobox.addons.biomes.BiomesAddon;
import world.bentobox.addons.biomes.panel.user.ChangeBiomePanel;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;


/**
 * This class process "biomes" method.
 */
public class BiomesCommand extends CompositeCommand
{
	public BiomesCommand(BiomesAddon plugin, CompositeCommand bsbIslandCmd)
	{
		super(plugin, bsbIslandCmd, "biomes");
		this.addon = plugin;
	}


	@Override
	public void setup()
	{
		this.setPermission("biomes");
		this.setOnlyPlayer(true);
		this.setParametersHelp("biomes.commands.help.parameters");
		this.setDescription("biomes.commands.help.description");

		new BiomesSetCommand(this.getAddon(), this);
		new BiomesInfoCommand(this.getAddon(), this);
	}


	@Override
	public boolean execute(User user, String string, List<String> args)
	{
		// Open up the biomes GUI
		if (user.isPlayer())
		{
			new ChangeBiomePanel(this.addon,
				this.getWorld(),
				user,
				null,
				this.getTopLabel(),
				this.getPermissionPrefix()).build();

			return true;
		}
		// Show help

		this.showHelp(this, user);
		return false;
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


	/**
	 * Variable stores biomes addon.
	 */
	private BiomesAddon addon;
}
