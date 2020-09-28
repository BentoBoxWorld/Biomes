package world.bentobox.biomes;


import java.util.Iterator;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.configuration.Config;
import world.bentobox.bentobox.api.flags.Flag;
import world.bentobox.bentobox.hooks.VaultHook;
import world.bentobox.bentobox.util.Util;
import world.bentobox.biomes.commands.admin.AdminCommand;
import world.bentobox.biomes.commands.user.BiomesCommand;
import world.bentobox.biomes.config.Settings;
import world.bentobox.biomes.database.objects.BiomeChunkUpdateObject;
import world.bentobox.biomes.handlers.BiomeDataRequestHandler;
import world.bentobox.biomes.handlers.BiomeListRequestHandler;
import world.bentobox.biomes.handlers.ChangeBiomeRequestHandler;
import world.bentobox.biomes.listeners.ChangeOwnerListener;
import world.bentobox.biomes.listeners.ChunkLoadListener;
import world.bentobox.greenhouses.Greenhouses;
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

        this.hookInGameModes();

        if (this.hooked)
        {
            this.setupAddon();
        }
        else
        {
            this.logError("Biomes could not hook into any GameMode so will not do anything!");
            this.setState(State.DISABLED);
        }
    }


    /**
     * Sets up everything once the addon is hooked into Game Modes
     */
    private void setupAddon() {
        // If hooked init Manager
        this.addonManager = new BiomesAddonManager(this);

        // Try to find Level addon and if it does not exist, display a warning
        this.findLevelAddon();
        // Try to find Greenhouses addon
        this.findGreenhousesAddon();
        // Try to find Economy Plugin
        this.findVaultPlugin();

		// Register the reset listener
        this.registerListener(new ChangeOwnerListener(this));
        this.registerListener(new ChunkLoadListener(this));

        // Register Flags
        this.registerFlag(BIOMES_WORLD_PROTECTION);
        this.registerFlag(BIOMES_ISLAND_PROTECTION);

        // Register Request Handlers
        this.registerRequestHandler(new BiomeDataRequestHandler(this));
        this.registerRequestHandler(new BiomeListRequestHandler(this));

        this.registerRequestHandler(new ChangeBiomeRequestHandler(this));

        if (this.settings.getUpdateTickCounter() > 0)
        {
            // This task will force-load chunk every update tick if its biome is not updated.
            this.runChunkUpdatingScheduler();
        }
    }


    /**
     * This task will force-load chunk every update tick if its biome is not updated.
     */
    private void runChunkUpdatingScheduler()
    {
        Bukkit.getScheduler().runTaskTimer(this.getPlugin(), () -> {
                Iterator<BiomeChunkUpdateObject> iterator =
                    this.addonManager.getBiomeUpdaterCollection().iterator();

                // if there is nothing to load, then skip.
                if (!iterator.hasNext())
                {
                    return;
                }

                BiomeChunkUpdateObject updater = iterator.next();

                // if chunk is already force-loaded, then skip.
                while (iterator.hasNext() && updater.isForceLoaded())
                {
                    updater = iterator.next();
                }

                World world = updater.getWorld();

                // if chunk is loaded then skip.
                if (!world.isChunkLoaded(updater.getChunkX(), updater.getChunkZ()))
                {
                    // Set flag as force-loaded.
                    updater.setForceLoaded(true);

                    // force-load chunk asynchronously
                    Util.getChunkAtAsync(world,
                        updater.getChunkX(),
                        updater.getChunkZ());
                }
            },

            this.settings.getUpdateTickCounter(),
            this.settings.getUpdateTickCounter());
    }


    /**
     * This is silly method that was introduced to reduce main method complexity, and just reports
     * if economy is enabled or not.
     */
    private void findVaultPlugin()
    {
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
    }


    /**
     * This is silly method that was introduced to reduce main method complexity, and just reports
     * if level addon is enabled or not.
     */
    private void findLevelAddon()
    {
        Optional<Addon> level = this.getAddonByName("Level");

        if (!level.isPresent())
        {
            this.logWarning(
                "Level add-on not found so level requirements will be ignored!");
            this.levelAddon = null;
            this.levelProvided = false;
        }
        else
        {
            this.levelProvided = true;
            this.levelAddon = (Level) level.get();
        }
    }


    /**
     * This is silly method that was introduced to reduce main method complexity, and just reports
     * if greenhouses is enabled or not.
     */
    private void findGreenhousesAddon()
    {
        Optional<Addon> greenhouses = this.getAddonByName("Greenhouses");

        if (greenhouses.isPresent())
        {
            this.greenhousesProvided = true;
            this.greenhouses = (Greenhouses) greenhouses.get();
        }
    }


    /**
     * This method hooks commands and flags into each GameModeAddon.
     */
    private void hookInGameModes()
    {
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


	/**
	 * This method returns the Greenhouses value.
	 * @return the value of Greenhouses.
	 */
	public Greenhouses getGreenhouses()
	{
		return this.greenhouses;
	}


	/**
	 * This method returns the greenhousesProvided value.
	 * @return the value of greenhousesProvided.
	 */
	public boolean isGreenhousesProvided()
	{
		return this.greenhousesProvided;
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

	/**
	 * Greenhouses addon.
	 */
	private Greenhouses greenhouses;

	/**
	 * This indicate if greenhouses addon exists.
	 */
	private boolean greenhousesProvided;


    // ---------------------------------------------------------------------
    // Section: Flags
    // ---------------------------------------------------------------------


    /**
     * This flag allows to change biomes in any part of the world. It will not limit
     * player to their island. Useful for skygrid without protection flags.
     */
    public static final Flag BIOMES_WORLD_PROTECTION =
            new Flag.Builder("BIOMES_WORLD_PROTECTION", Material.GRASS_BLOCK).
            type(Flag.Type.WORLD_SETTING).
            mode(Flag.Mode.ADVANCED).
            defaultSetting(true).
            build();

    /**
     * This flag allows to define which users can change biomes. F.e. it can be set
     * that only Island owner can change biomes.
     * By default it is set to Visitor.
     */
    public static final Flag BIOMES_ISLAND_PROTECTION =
            new Flag.Builder("BIOMES_ISLAND_PROTECTION", Material.GRASS_BLOCK).
            mode(Flag.Mode.ADVANCED).
            build();
}
