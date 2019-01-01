package world.bentobox.addons.biomes;


import world.bentobox.addons.biomes.commands.admin.AdminCommand;
import world.bentobox.addons.biomes.commands.user.BiomesCommand;
import world.bentobox.addons.biomes.listeners.ChangeOwnerListener;
import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.managers.CommandsManager;


/**
 * This is Biomes addon class. It enables everything that is necessary for this addon to work.
 */
public class BiomesAddon extends Addon
{
	@Override
	public void onEnable()
	{
		// Load the plugin's config
		this.saveDefaultConfig();

		this.addonManager = new BiomesAddonManager(this);

		CommandsManager commandsManager = this.getPlugin().getCommandsManager();

		this.getPlugin().getAddonsManager().getAddonByName("AcidIsland").ifPresent(a -> {
			CompositeCommand acidIslandCmd =
				commandsManager.getCommand(this.getConfig().getString("acidisland.user-command", "ai"));

			if (acidIslandCmd != null)
			{
				new BiomesCommand(this, acidIslandCmd);

				CompositeCommand acidCmd =
					commandsManager.getCommand(this.getConfig().getString("acidisland.admin-command", "acid"));

				new AdminCommand(this, acidCmd);
			}

			String currentWorld = a.getConfig().getString("world.world-name");

			if (this.addonManager.getBiomes(currentWorld).isEmpty())
			{
				this.addonManager.importBiomes(currentWorld);
			}
		});

		// BSkyBlock hook in
		this.getPlugin().getAddonsManager().getAddonByName("BSkyBlock").ifPresent(a -> {
			CompositeCommand bsbIslandCmd =
				commandsManager.getCommand(this.getConfig().getString("bskyblock.user-command", "island"));

			if (bsbIslandCmd != null)
			{
				new BiomesCommand(this, bsbIslandCmd);

				CompositeCommand bsbAdminCmd =
					commandsManager.getCommand(this.getConfig().getString("bskyblock.admin-command", "bsbadmin"));

				new AdminCommand(this, bsbAdminCmd);
			}

			String currentWorld = a.getConfig().getString("world.world-name");

			if (this.addonManager.getBiomes(currentWorld).isEmpty())
			{
				this.addonManager.importBiomes(currentWorld);
			}
		});

		// This listener fires on each owner change.
		this.getServer().getPluginManager().registerEvents(new ChangeOwnerListener(this), this.getPlugin());
	}


	@Override
	public void onDisable()
	{
	}


	/**
	 * This method returns addon manager.
	 * @return Addon manager object.
	 */
	public BiomesAddonManager getAddonManager()
	{
		return this.addonManager;
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

	/**
	 * This variable stores biomes manager.
	 */
	private BiomesAddonManager addonManager;
}
