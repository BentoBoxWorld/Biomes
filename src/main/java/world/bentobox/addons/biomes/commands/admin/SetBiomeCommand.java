package world.bentobox.addons.biomes.commands.admin;


import java.util.List;
import java.util.Optional;

import world.bentobox.addons.biomes.BiomesAddon;
import world.bentobox.addons.biomes.panel.AdminUserPanel;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;


/**
 * This is admin command that allows to change user biomes.
 */
public class SetBiomeCommand extends CompositeCommand
{
	public SetBiomeCommand(BiomesAddon addon, CompositeCommand parent)
	{
		super(addon, parent, "set");
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
			new AdminUserPanel(this.addon, this.getWorld(), user, false);
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


	@Override
	public Optional<List<String>> tabComplete(User user, String alias, List<String> args)
	{
		return Optional.empty();
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


	private BiomesAddon addon;
}
