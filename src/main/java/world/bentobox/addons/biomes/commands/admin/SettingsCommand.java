package world.bentobox.addons.biomes.commands.admin;


import java.util.List;

import world.bentobox.addons.biomes.BiomesAddon;
import world.bentobox.addons.biomes.panel.AdminSettingsPanel;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;


/**
 * This command allows to edit biomes addon settings.
 */
public class SettingsCommand extends CompositeCommand
{
	public SettingsCommand(BiomesAddon addon, CompositeCommand parent)
	{
		super(addon, parent, "settings");
		this.addon = addon;
	}

	@Override
	public void setup()
	{
		this.setPermission("admin.biomes");
		this.setParametersHelp("biomes.admin.parameters");
		this.setDescription("biomes.admin.description");
	}


	@Override
	public boolean execute(User user, String label, List<String> args)
	{
		if (user.isPlayer() && args.isEmpty())
		{
			// Shows admin panel
			new AdminSettingsPanel(this.addon, this.getWorld(), user, false);
			return true;
		}
		else if (args.isEmpty())
		{
			this.showHelp(this, user);
			return false;
		}
		else
		{
			// Process command.

			return false;
		}
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


	private BiomesAddon addon;
}
