package world.bentobox.biomes;


import world.bentobox.acidisland.AcidIsland;
import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.configuration.Config;
import world.bentobox.bentobox.managers.CommandsManager;
import world.bentobox.biomes.commands.admin.AdminCommand;
import world.bentobox.biomes.commands.user.BiomesCommand;
import world.bentobox.biomes.listeners.ChangeOwnerListener;
import world.bentobox.biomes.objects.Settings;
import world.bentobox.bskyblock.BSkyBlock;


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
		this.hooked = false;
		this.addonManager = new BiomesAddonManager(this);

		CommandsManager commandsManager = this.getPlugin().getCommandsManager();

		// AcidIsland hook in
		if (this.settings.getDisabledGameModes().isEmpty() ||
			!this.settings.getDisabledGameModes().contains("AcidIsland"))
		{
			this.getPlugin().getAddonsManager().getAddonByName("AcidIsland").ifPresent(
				addon -> {
					AcidIsland acidIsland = (AcidIsland) addon;

					new AdminCommand(this,
						commandsManager.getCommand(acidIsland.getSettings().getAdminCommand()));
					new BiomesCommand(this,
						commandsManager.getCommand(acidIsland.getSettings().getIslandCommand()));

					String currentWorld = acidIsland.getWorldSettings().getWorldName();

					if (this.addonManager.getBiomes(currentWorld).isEmpty())
					{
						this.addonManager.importBiomes(currentWorld);
					}

					this.hooked = true;
				});
		}

		// BSkyBlock hook in
		if (this.settings.getDisabledGameModes().isEmpty() ||
			!this.settings.getDisabledGameModes().contains("BSkyBlock"))
		{
			this.getPlugin().getAddonsManager().getAddonByName("BSkyBlock").ifPresent(
				addon -> {
					BSkyBlock skyBlock = (BSkyBlock) addon;

					new AdminCommand(this,
						commandsManager.getCommand("bsbadmin"));
					new BiomesCommand(this,
						commandsManager.getCommand("island"));

					String currentWorld = skyBlock.getWorldSettings().getWorldName();

					if (this.addonManager.getBiomes(currentWorld).isEmpty())
					{
						this.addonManager.importBiomes(currentWorld);
					}

					this.hooked = true;
				});
		}

		if (this.hooked)
		{
			// This listener fires on each owner change.
			this.getServer().getPluginManager().registerEvents(
				new ChangeOwnerListener(this), this.getPlugin());
		}
		else
		{
			this.logError("Biomes addon is not loaded, as it does not find valid GameModes.");
			this.setState(State.DISABLED);
		}
	}


	@Override
	public void onDisable()
	{
		if (this.hooked)
		{
			if (this.settings != null)
			{
				new Config<>(this, Settings.class).saveConfigObject(this.settings);
			}

			if (this.addonManager != null)
			{
				this.addonManager.save(false);
			}
		}
	}


	@Override
	public void onReload()
	{
		if (this.hooked)
		{
			this.loadSettings();
			// Reload biomes manager.
			this.addonManager.reloadManager();

			this.getLogger().info("Biomes addon reloaded.");
		}
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
	 * This variable stores if current addon is hooked or not.
	 */
	private boolean hooked;

	/**
	 * This variable stores biomes manager.
	 */
	private BiomesAddonManager addonManager;

	/**
	 * This variable stores biomes addon settings.
	 */
	private Settings settings;
}
