package world.bentobox.biomes;


import org.bukkit.Bukkit;
import org.bukkit.Material;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.configuration.Config;
import world.bentobox.bentobox.api.flags.Flag;
import world.bentobox.bentobox.hooks.VaultHook;
import world.bentobox.bentobox.managers.RanksManager;
import world.bentobox.biomes.commands.admin.AdminCommand;
import world.bentobox.biomes.commands.user.BiomesCommand;
import world.bentobox.biomes.handlers.BiomeDataRequestHandler;
import world.bentobox.biomes.handlers.BiomeListRequestHandler;
import world.bentobox.biomes.handlers.ChangeBiomeRequestHandler;
import world.bentobox.biomes.listeners.ChangeOwnerListener;
import world.bentobox.biomes.config.Settings;
import world.bentobox.level.Level;


/**
 * This is Biomes addon class. It enables everything that is necessary for this addon to work.
 */
public class BiomesAddon extends Addon
{
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onLoad()
	{
		// Save default config.yml
		this.saveDefaultConfig();
		// Load the plugin's config
		this.loadSettings();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onEnable()
	{
		// Check if it is enabled - it might be loaded, but not enabled.
		if (this.getPlugin() == null || !this.getPlugin().isEnabled())
		{
			Bukkit.getLogger().severe("BentoBox is not available or disabled!");
			this.setState(State.DISABLED);
			return;
		}

		// Check if addon is not disabled before.
		if (this.getState().equals(State.DISABLED))
		{
			Bukkit.getLogger().severe("Biomes Addon is not available or disabled!");
			return;
		}

		this.hooked = false;

		this.getPlugin().getAddonsManager().getGameModeAddons().forEach(gameModeAddon -> {
			if (!this.settings.getDisabledGameModes().contains(gameModeAddon.getDescription().getName()))
			{
				if (gameModeAddon.getPlayerCommand().isPresent())
				{
					new BiomesCommand(this, gameModeAddon.getPlayerCommand().get());
					this.hooked = true;
				}

				if (gameModeAddon.getAdminCommand().isPresent())
				{
					new AdminCommand(this, gameModeAddon.getAdminCommand().get());
				}

				// Add FLAGS
				BIOMES_WORLD_PROTECTION.addGameModeAddon(gameModeAddon);
				BIOMES_ISLAND_PROTECTION.addGameModeAddon(gameModeAddon);
			}
		});

		if (this.hooked)
		{
			// If hooked init Manager
			this.addonManager = new BiomesAddonManager(this);

			// Try to find Level addon and if it does not exist, display a warning

			Optional<Addon> level = this.getAddonByName("Level");

			if (!level.isPresent())
			{
				this.logWarning(
					"Level add-on not found so level requirements will be ignored!");
				this.levelAddon = null;
			}
			else
			{
				this.levelProvided = true;
				this.levelAddon = (Level) level.get();
			}

			Optional<VaultHook> vault = this.getPlugin().getVault();

			if (!vault.isPresent() || !vault.get().hook())
			{
				this.vaultHook = null;
				this.logWarning(
					"Economy plugin not found so money requirements will be ignored!");
			}
			else
			{
				this.economyProvided = true;
				this.vaultHook = vault.get();
			}

			// Register the reset listener
			this.registerListener(new ChangeOwnerListener(this));

			// Register Flags
			this.registerFlag(BIOMES_WORLD_PROTECTION);
			this.registerFlag(BIOMES_ISLAND_PROTECTION);

			// Register Request Handlers
			this.registerRequestHandler(new BiomeDataRequestHandler(this));
			this.registerRequestHandler(new BiomeListRequestHandler(this));

			this.registerRequestHandler(new ChangeBiomeRequestHandler(this));
		}
		else
		{
			this.logError(
				"Biomes could not hook into any GameMode so will not do anything!");
			this.setState(State.DISABLED);
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onReload()
	{
		super.onReload();

		if (this.hooked)
		{
			this.loadSettings();
			// Reload biomes manager.
			this.addonManager.reload();

			this.getLogger().info("Biomes addon reloaded.");
		}
	}


	/**
	 * {@inheritDoc}
	 */
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
				this.addonManager.save();
			}
		}
	}


	/**
	 * This method loads addon configuration settings in memory.
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


// ---------------------------------------------------------------------
// Section: Getters
// ---------------------------------------------------------------------


	/**
	 * This method returns addon manager.
	 *
	 * @return Addon manager object.
	 */
	public BiomesAddonManager getAddonManager()
	{
		return this.addonManager;
	}


	/**
	 * This method returns addon settings.
	 *
	 * @return Addon settings object.
	 */
	public Settings getSettings()
	{
		return this.settings;
	}


	/**
	 * This method returns the economyProvided value.
	 * @return the value of economyProvided.
	 */
	public boolean isEconomyProvided()
	{
		return this.economyProvided;
	}


	/**
	 * This method returns the vaultHook value.
	 * @return the value of vaultHook.
	 */
	public VaultHook getVaultHook()
	{
		return this.vaultHook;
	}


	/**
	 * This method returns the levelAddon value.
	 * @return the value of levelAddon.
	 */
	public Level getLevelAddon()
	{
		return this.levelAddon;
	}


	/**
	 * This method returns the levelProvided value.
	 * @return the value of levelProvided.
	 */
	public boolean isLevelProvided()
	{
		return this.levelProvided;
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

	/**
	 * This boolean indicate if economy is enabled.
	 */
	private boolean economyProvided;

	/**
	 * VaultHook that process economy.
	 */
	private VaultHook vaultHook;

	/**
	 * Level addon.
	 */
	private Level levelAddon;

	/**
	 * This indicate if level addon exists.
	 */
	private boolean levelProvided;


// ---------------------------------------------------------------------
// Section: Flags
// ---------------------------------------------------------------------


	/**
	 * This flag allows to change biomes in any part of the world. It will not limit
	 * player to their island. Useful for skygrid without protection flags.
	 */
	public static Flag BIOMES_WORLD_PROTECTION =
		new Flag.Builder("BIOMES_WORLD_PROTECTION", Material.GRASS_BLOCK).type(Flag.Type.WORLD_SETTING).defaultSetting(true).build();

	/**
	 * This flag allows to define which users can change biomes. F.e. it can be set
	 * that only Island owner can change biomes.
	 * By default it is set to Visitor.
	 */
	public static Flag BIOMES_ISLAND_PROTECTION =
		new Flag.Builder("BIOMES_ISLAND_PROTECTION", Material.GRASS_BLOCK).defaultRank(RanksManager.VISITOR_RANK).build();


// ---------------------------------------------------------------------
// Section: Constants
// ---------------------------------------------------------------------

	/**
	 * This ir ugly way how to fix comparability issues between 1.13 and 1.14 versions.
	 * @deprecated Should be removed as soon as 1.13 support are dropped down.
	 */
	@Deprecated
	public static final Material SIGN_MATERIAL = Bukkit.getBukkitVersion().startsWith("1.13") ? Material.valueOf("SIGN") : Material.valueOf("OAK_SIGN");
}
