package world.bentobox.biomes.commands.admin;


import java.util.List;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.commands.admin.AdminReloadCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.biomes.BiomesAddon;
import world.bentobox.biomes.panel.admin.AdminGUI;


/**
 * This class process "biomes" as admin command.
 */
public class AdminCommand extends CompositeCommand
{
	public AdminCommand(BiomesAddon addon, CompositeCommand parent)
	{
		super(addon, parent, "biomes");
		this.addon = addon;
	}


	@Override
	public void setup()
	{
		this.setPermission("admin.biomes");
		this.setParametersHelp("biomes.commands.admin.help.parameters");
		this.setDescription("biomes.commands.admin.help.description");

		// Register sub commands

		// Biome related
		new SetBiomeCommand(this.getAddon(), this);
		new EditBiomeCommand(this.getAddon(), this);
		new AddBiomeCommand(this.getAddon(), this);

		// Addon related
		new ImportCommand(this.getAddon(), this);
		new SettingsCommand(this.getAddon(), this);

		// Addon reload command
		new AdminReloadCommand(this);
	}


	@Override
	public boolean execute(User user, String label, List<String> args)
	{
		// Open up the admin GUI
		if (user.isPlayer())
		{
			// Create GUI
			new AdminGUI(this.addon,
				this.getWorld(),
				user,
				this.getTopLabel(),
				this.getPermissionPrefix()).build();
			return true;
		}
		else
		{
			this.showHelp(this, user);
			return false;
		}
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


	/**
	 * Variable stores biomes addon.
	 */
	private BiomesAddon addon;
}
