package world.bentobox.addons.biomes.commands.user;


import java.util.List;

import world.bentobox.addons.biomes.BiomesAddon;
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
		super(plugin, bsbIslandCmd, "biomes");
		this.addon = plugin;
	}


	@Override
	public void setup()
	{
		this.setPermission("island.biomes");
		this.setOnlyPlayer(true);
		this.setParametersHelp("biomes.help.parameters");
		this.setDescription("biomes.help.description");

		new BiomesSetCommand(this.addon, this);
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
				this.getWorld(),
				this.getPermissionPrefix(),
				BiomesPanel.Mode.PLAYER);

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
