package world.bentobox.biomes;


import org.bukkit.Bukkit;
import org.bukkit.Material;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.configuration.Config;
import world.bentobox.bentobox.api.flags.Flag;
import world.bentobox.bentobox.hooks.VaultHook;
import world.bentobox.biomes.commands.admin.AdminCommand;
import world.bentobox.biomes.commands.player.BiomesCommand;
import world.bentobox.biomes.config.Settings;
import world.bentobox.biomes.handlers.BiomeDataRequestHandler;
import world.bentobox.biomes.handlers.BiomeListRequestHandler;
import world.bentobox.biomes.handlers.ChangeBiomeRequestHandler;
import world.bentobox.biomes.listeners.ChangeOwnerListener;
import world.bentobox.biomes.listeners.IslandLevelListener;
import world.bentobox.biomes.listeners.JoinLeaveListener;
import world.bentobox.biomes.managers.BiomesAddonManager;
import world.bentobox.biomes.managers.BiomesImportManager;
import world.bentobox.biomes.tasks.UpdateQueue;
import world.bentobox.biomes.web.WebManager;
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
    private void setupAddon()
    {
        // If hooked init Manager
        this.addonManager = new BiomesAddonManager(this);
        this.importManager = new BiomesImportManager(this);

        // Register the reset listener
        this.registerListener(new ChangeOwnerListener(this));
        this.registerListener(new JoinLeaveListener(this));
        this.registerListener(new IslandLevelListener(this));

        // Register Flags
        this.registerFlag(BIOMES_WORLD_PROTECTION);
        this.registerFlag(BIOMES_ISLAND_PROTECTION);

        // Register Request Handlers
        this.registerRequestHandler(new BiomeDataRequestHandler(this));
        this.registerRequestHandler(new BiomeListRequestHandler(this));

        this.registerRequestHandler(new ChangeBiomeRequestHandler(this));

        this.webManager = new WebManager(this);
    }


    /**
     * This method hooks commands and flags into each GameModeAddon.
     */
    private void hookInGameModes()
    {
        this.getPlugin().getAddonsManager().getGameModeAddons().forEach(gameModeAddon ->
        {
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
     * Process Level addon and Vault Hook when everything is loaded.
     */
    @Override
    public void allLoaded()
    {
        super.allLoaded();

        // Try to find Level addon and if it does not exist, display a warning
        this.getAddonByName("Level").ifPresentOrElse(addon ->
        {
            this.levelAddon = (Level) addon;
            this.levelProvided = true;
            this.log("Biomes Addon hooked into Level addon.");
        }, () ->
        {
            this.levelAddon = null;
            this.logWarning("Level add-on not found. Some features from Biomes Addon will not work!");
        });

        // Try to find Level addon and if it does not exist, display a warning
        this.getAddonByName("Greenhouses").ifPresentOrElse(addon ->
        {
            this.greenhousesProvided = true;
            this.greenhouses = (Greenhouses) addon;
            this.log("Biomes Addon hooked into Greenhouses addon.");
        }, () ->
        {
            this.greenhouses = null;
        });

        // Try to find Vault Plugin and if it does not exist, display a warning
        this.getPlugin().getVault().ifPresentOrElse(hook ->
        {
            this.vaultHook = hook;

            if (this.vaultHook.hook())
            {
                this.log("Biomes Addon hooked into Economy.");
            }
            else
            {
                this.logWarning("Biomes Addon could not hook into valid Economy.");
            }
        }, () ->
        {
            this.vaultHook = null;
            this.logWarning("Vault plugin not found. Economy will not work!");
        });

        // Start update task when everything is loaded.

        if (this.hooked)
        {
            this.biomeUpdateQueue = new UpdateQueue(this);
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
            this.biomeUpdateQueue.getTask().cancel();
            this.getLogger().info("Biomes addon disabled.");
        }
    }


    /**
     * Save settings.
     */
    public void saveSettings()
    {
        if (this.settings != null)
        {
            new Config<>(this, Settings.class).saveConfigObject(this.settings);
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

        // Save existing panels.
        this.saveResource("panels/main_panel.yml", false);
        this.saveResource("panels/advanced_panel.yml", false);

        // Save template
        this.saveResource("biomesTemplate.yml", false);
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
     * Gets import manager.
     *
     * @return the import manager
     */
    public BiomesImportManager getImportManager()
    {
        return this.importManager;
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
     *
     * @return the value of economyProvided.
     */
    public boolean isEconomyProvided()
    {
        return this.vaultHook != null && this.vaultHook.hook();
    }


    /**
     * This method returns the vaultHook value.
     *
     * @return the value of vaultHook.
     */
    public VaultHook getVaultHook()
    {
        return this.vaultHook;
    }


    /**
     * This method returns the levelAddon value.
     *
     * @return the value of levelAddon.
     */
    public Level getLevelAddon()
    {
        return this.levelAddon;
    }


    /**
     * This method returns the levelProvided value.
     *
     * @return the value of levelProvided.
     */
    public boolean isLevelProvided()
    {
        return this.levelProvided;
    }


    /**
     * This method returns the Greenhouses value.
     *
     * @return the value of Greenhouses.
     */
    public Greenhouses getGreenhouses()
    {
        return this.greenhouses;
    }


    /**
     * This method returns the greenhousesProvided value.
     *
     * @return the value of greenhousesProvided.
     */
    public boolean isGreenhousesProvided()
    {
        return this.greenhousesProvided;
    }


    /**
     * Gets web manager.
     *
     * @return the web manager
     */
    public WebManager getWebManager()
    {
        return webManager;
    }


    /**
     * Gets update queue.
     *
     * @return the update queue
     */
    public UpdateQueue getUpdateQueue()
    {
        return this.biomeUpdateQueue;
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
     * This variable stores biomes manager.
     */
    private BiomesImportManager importManager;

    /**
     * Variable holds web manager object.
     */
    private WebManager webManager;

    /**
     * This variable stores biomes addon settings.
     */
    private Settings settings;

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
     * This indicates if greenhouses addon exists.
     */
    private boolean greenhousesProvided;


    /**
     * The Biome update queue.
     */
    private UpdateQueue biomeUpdateQueue;


    // ---------------------------------------------------------------------
    // Section: Flags
    // ---------------------------------------------------------------------


    /**
     * This flag allows to change biomes in any part of the world. It will not limit player to their island. Useful for
     * skygrid without protection flags.
     */
    public static final Flag BIOMES_WORLD_PROTECTION =
        new Flag.Builder("BIOMES_WORLD_PROTECTION", Material.GRASS_BLOCK).
            type(Flag.Type.WORLD_SETTING).
            mode(Flag.Mode.ADVANCED).
            defaultSetting(true).
            build();

    /**
     * This flag allows to define which users can change biomes. F.e. it can be set that only Island owner can change
     * biomes. By default it is set to Visitor.
     */
    public static final Flag BIOMES_ISLAND_PROTECTION =
        new Flag.Builder("BIOMES_ISLAND_PROTECTION", Material.GRASS_BLOCK).
            mode(Flag.Mode.ADVANCED).
            build();
}
