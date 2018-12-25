package world.bentobox.addons.biomes.commands.admin;


import java.util.List;

import world.bentobox.addons.biomes.BiomesAddon;
import world.bentobox.addons.biomes.panel.AdminMainPanel;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;


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
		this.setParametersHelp("biomes.admin.parameters");
		this.setDescription("biomes.admin.description");

		// Register sub commands

		new ImportCommand(this.getAddon(), this);

//		new ReloadBiomes(this.getAddon(), this);
//		new ResetBiomes(this.getAddon(), this);
//	 	new CreateBiomes(this.getAddon(), this);
	}


	@Override
	public boolean execute(User user, String label, List<String> args)
	{
		// Open up the admin GUI
		if (user.isPlayer())
		{
			// Create GUI
			new AdminMainPanel(this.addon, this.getWorld(), user);
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
