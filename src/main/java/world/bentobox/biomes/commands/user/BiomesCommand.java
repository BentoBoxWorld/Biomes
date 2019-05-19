package world.bentobox.biomes.commands.user;


import java.util.List;
import java.util.Optional;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.biomes.BiomesAddon;
import world.bentobox.biomes.panel.user.BiomesChooseGUI;


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
		this.setParametersHelp("biomes.commands.user.help.parameters");
		this.setDescription("biomes.commands.user.help.description");

		new BiomesSetCommand(this.getAddon(), this);
		new BiomesInfoCommand(this.getAddon(), this);
	}


	/**
	 * This method checks if user can execute this command.
	 * {@inheritDoc}
	 */
	@Override
	public boolean canExecute(User user, String label, List<String> args)
	{
		Optional<GameModeAddon> optionalAddon = this.getAddon().getPlugin().getIWM().getAddon(this.getWorld());

		if (!optionalAddon.isPresent())
		{
			// Not a GameMode world.
			user.sendMessage("general.errors.wrong-world");
			return false;
		}

		if (!((BiomesAddon) this.getAddon()).getAddonManager().hasAnyBiome(this.getWorld()))
		{
			// Do not open gui if there is no biomes.

			this.getAddon().getLogger().severe("There are no biomes set up in " + this.getWorld() + "!");

			// Show admin better explanation.
			if (user.isOp() || user.hasPermission(this.getPermissionPrefix() + ".admin.biomes"))
			{
				String topLabel = optionalAddon.get().getAdminCommand().orElseGet(this::getParent).getTopLabel();
				user.sendMessage("biomes.errors.no-biomes-admin", "[label]", topLabel);
			}
			else
			{
				user.sendMessage("biomes.errors.no-biomes");
			}

			return false;
		}

		if (this.getPlugin().getIslands().getIsland(this.getWorld(), user.getUniqueId()) == null)
		{
			// Do not open gui if there is no island for this player.
			user.sendMessage("general.errors.no-island");
			return false;
		}

		return true;
	}


	@Override
	public boolean execute(User user, String string, List<String> args)
	{
		// Open up the biomes GUI
		if (user.isPlayer())
		{
			new BiomesChooseGUI(this.addon,
				this.getWorld(),
				user,
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
