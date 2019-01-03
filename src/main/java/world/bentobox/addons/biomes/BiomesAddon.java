package world.bentobox.addons.biomes;


import world.bentobox.addons.biomes.commands.admin.AdminCommand;
import world.bentobox.addons.biomes.commands.user.BiomesCommand;
import world.bentobox.addons.biomes.listeners.ChangeOwnerListener;
import world.bentobox.addons.biomes.objects.Settings;
import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.configuration.Config;
import world.bentobox.bentobox.managers.CommandsManager;


/**
 * This is Biomes addon class. It enables everything that is necessary for this addon to work.
 */
public class BiomesAddon extends Addon
{
	@Override
	public void onLoad()
	{
		// Load the plugin's config
		this.saveDefaultConfig();

		this.loadSettings();
	}


	@Override
	public void onEnable()
	{
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

			// Probably better would be casting and getting from settings, but then it should be added as
			// dependency.
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

			// Probably better would be casting and getting from settings, but then it should be added as
			// dependency.
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
		if (this.settings != null)
		{
			new Config<>(this, Settings.class).saveConfigObject(this.settings);
		}
	}


	@Override
	public void onReload()
	{
		this.loadSettings();
		// Reload biomes manager.
		this.addonManager.reloadManager();

		this.getLogger().info("Biomes addon reloaded.");
	}


	/**
	 * Load addon settings.
	 */
	private void loadSettings()
	{
		this.settings = new Config<>(this, Settings.class).loadConfigObject();

		if (this.settings == null)
		{
			// Disable
			this.logError("Biomes settings could not load! Addon disabled.");
			this.setState(State.DISABLED);
		}
	}


	/**
	 * This method returns addon manager.
	 * @return Addon manager object.
	 */
	public BiomesAddonManager getAddonManager()
	{
		return this.addonManager;
	}


	/**
	 * This method returns addon settings.
	 * @return Addon settings object.
	 */
	public Settings getSettings()
	{
		return this.settings;
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

	/**
	 * This variable stores biomes manager.
	 */
	private BiomesAddonManager addonManager;

	/**
	 * This variable stores biomes addon settings.
	 */
	private Settings settings;
}
