///
// Created by BONNe
// Copyright - 2022
///

package world.bentobox.biomes.config;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import world.bentobox.bentobox.api.configuration.ConfigComment;
import world.bentobox.bentobox.api.configuration.ConfigEntry;
import world.bentobox.bentobox.api.configuration.ConfigObject;
import world.bentobox.bentobox.api.configuration.StoreAt;


/**
 * All the plugin settings are here
 *
 * @author BONNe
 */
@StoreAt(filename = "config.yml", path = "addons/Biomes")
@ConfigComment("Biomes Configuration [version]")
public class Settings implements ConfigObject
{
    // ---------------------------------------------------------------------
    // Section: Getters
    // ---------------------------------------------------------------------


    /**
     * This method returns default update mode.
     *
     * @return Default update mode.
     */
    public UpdateMode getDefaultMode()
    {
        return this.defaultMode;
    }


    /**
     * This method sets value of defaultMode variable.
     *
     * @param defaultMode new value.
     */
    public void setDefaultMode(UpdateMode defaultMode)
    {
        this.defaultMode = defaultMode;
    }


    /**
     * This method returns default update range.
     *
     * @return default update range
     */
    public int getDefaultSize()
    {
        return this.defaultSize;
    }


    /**
     * This method sets value of defaultSize variable.
     *
     * @param defaultSize new value.
     */
    public void setDefaultSize(int defaultSize)
    {
        this.defaultSize = defaultSize;
    }


    /**
     * This method returns if biome reset is necessary on owner change.
     *
     * @return true - if biome reset is required.
     */
    public boolean isResetBiomes()
    {
        return this.resetBiomes;
    }


    /**
     * This method sets value of resetBiomes variable.
     *
     * @param resetBiomes new value.
     */
    public void setResetBiomes(boolean resetBiomes)
    {
        this.resetBiomes = resetBiomes;
    }


    // ---------------------------------------------------------------------
    // Section: Setters
    // ---------------------------------------------------------------------


    /**
     * This method returns cooldown between biome changes.
     *
     * @return integer that represents seconds between biome changes.
     */
    public int getCoolDown()
    {
        return this.coolDown;
    }


    /**
     * This method sets value of coolDown variable.
     *
     * @param coolDown new value.
     */
    public void setCoolDown(int coolDown)
    {
        this.coolDown = coolDown;
    }


    /**
     * This method returns disabled game modes.
     *
     * @return Set with disabled game modes.
     */
    public Set<String> getDisabledGameModes()
    {
        return this.disabledGameModes;
    }


    /**
     * This method sets value for disabledGameModes variable.
     *
     * @param disabledGameModes new value.
     */
    @SuppressWarnings("unused")
    public void setDisabledGameModes(Set<String> disabledGameModes)
    {
        this.disabledGameModes = disabledGameModes;
    }


    /**
     * This method returns the useProtectionRange value
     *
     * @return the useProtectionRange value.
     */
    public boolean isUseProtectionRange()
    {
        return this.useProtectionRange;
    }


    /**
     * This method sets the useProtectionRange object value.
     *
     * @param useProtectionRange the useProtectionRange object new value.
     */
    public void setUseProtectionRange(boolean useProtectionRange)
    {
        this.useProtectionRange = useProtectionRange;
    }


    /**
     * Is notify unlocked biomes boolean.
     *
     * @return the boolean
     */
    public boolean isNotifyUnlockedBiomes()
    {
        return notifyUnlockedBiomes;
    }


    /**
     * Sets notify unlocked biomes.
     *
     * @param notifyUnlockedBiomes the notify unlocked biomes
     */
    public void setNotifyUnlockedBiomes(boolean notifyUnlockedBiomes)
    {
        this.notifyUnlockedBiomes = notifyUnlockedBiomes;
    }


    /**
     * Gets player command.
     *
     * @return the player command
     */
    public String getPlayerCommand()
    {
        return playerCommand;
    }


    /**
     * Sets player command.
     *
     * @param playerCommand the player command
     */
    public void setPlayerCommand(String playerCommand)
    {
        this.playerCommand = playerCommand;
    }


    /**
     * Gets player set command.
     *
     * @return the player set command
     */
    public String getPlayerSetCommand()
    {
        return playerSetCommand;
    }


    /**
     * Sets player set command.
     *
     * @param playerSetCommand the player set command
     */
    public void setPlayerSetCommand(String playerSetCommand)
    {
        this.playerSetCommand = playerSetCommand;
    }


    /**
     * Gets player buy command.
     *
     * @return the player buy command
     */
    public String getPlayerBuyCommand()
    {
        return playerBuyCommand;
    }


    /**
     * Sets player buy command.
     *
     * @param playerBuyCommand the player buy command
     */
    public void setPlayerBuyCommand(String playerBuyCommand)
    {
        this.playerBuyCommand = playerBuyCommand;
    }


    /**
     * Gets player info command.
     *
     * @return the player info command
     */
    public String getPlayerInfoCommand()
    {
        return playerInfoCommand;
    }


    /**
     * Sets player info command.
     *
     * @param playerInfoCommand the player info command
     */
    public void setPlayerInfoCommand(String playerInfoCommand)
    {
        this.playerInfoCommand = playerInfoCommand;
    }


    /**
     * Gets admin command.
     *
     * @return the admin command
     */
    public String getAdminCommand()
    {
        return adminCommand;
    }


    /**
     * Sets admin command.
     *
     * @param adminCommand the admin command
     */
    public void setAdminCommand(String adminCommand)
    {
        this.adminCommand = adminCommand;
    }


    /**
     * Gets concurrent biome updates.
     *
     * @return the concurrent biome updates
     */
    public int getConcurrentBiomeUpdates()
    {
        return concurrentBiomeUpdates;
    }


    /**
     * Sets concurrent biome updates.
     *
     * @param concurrentBiomeUpdates the concurrent biome updates
     */
    public void setConcurrentBiomeUpdates(int concurrentBiomeUpdates)
    {
        this.concurrentBiomeUpdates = concurrentBiomeUpdates;
    }


    /**
     * Gets change timeout.
     *
     * @return the change timeout
     */
    public long getChangeTimeout()
    {
        return changeTimeout;
    }


    /**
     * Sets change timeout.
     *
     * @param changeTimeout the change timeout
     */
    public void setChangeTimeout(long changeTimeout)
    {
        this.changeTimeout = changeTimeout;
    }


// ---------------------------------------------------------------------
// Section: Enums used for Settings.
// ---------------------------------------------------------------------


    /**
     * This enum describes all possible variants how to calculate new biome location.
     */
    public enum UpdateMode
    {
        /**
         * Island update mode.
         */
        ISLAND,
        /**
         * Chunk update mode.
         */
        CHUNK,
        /**
         * Range update mode.
         */
        RANGE;


        /**
         * This method returns stored parameter from string.
         *
         * @param parameter String of object that must be returned
         * @return CommandParameters object or null.
         */
        public static UpdateMode getMode(String parameter)
        {
            return BY_NAME.get(parameter);
        }


        /**
         * This map allows to access all enum values via their string.
         */
        private static final Map<String, UpdateMode> BY_NAME = new HashMap<>();

        /*
          This static method populated BY_NAME map.
         */
        static
        {
            for (UpdateMode mode : UpdateMode.values())
            {
                BY_NAME.put(mode.name(), mode);
            }
        }
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------


    @ConfigComment("")
    @ConfigComment("For advanced menu this indicate which mode will be active.")
    @ConfigComment("If advanced menu is disabled or in biome set command update mode is not passed")
    @ConfigComment("then update algorithm will use this value as update mode.")
    @ConfigComment("Valid values are:")
    @ConfigComment("    'ISLAND' - updates biome on whole island")
    @ConfigComment("    'CHUNK' - updates biome on whole chunks around player")
    @ConfigComment("    'RANGE' - updates biome by block in given range")
    @ConfigEntry(path = "default-mode")
    @SuppressWarnings("javadoc")
    private UpdateMode defaultMode = UpdateMode.ISLAND;

    @ConfigComment("")
    @ConfigComment("For advanced menu this indicate how large range will be set on GUI opening.")
    @ConfigComment("If advanced menu is disabled or in biome set command update range is not set")
    @ConfigComment("then update algorithm will use this value as update range.")
    @ConfigEntry(path = "default-size")
    @SuppressWarnings("javadoc")
    private int defaultSize = 3;

    @ConfigComment("")
    @ConfigComment("This indicates if changing biome will affect whole island or just protection range.")
    @ConfigComment("Allows to fix issues when biome is changed in space between islands.")
    @ConfigEntry(path = "use-protection-range")
    @SuppressWarnings("javadoc")
    private boolean useProtectionRange = false;

    @ConfigComment("")
    @ConfigComment("This indicates that players will be notified about new unlocked biomes.")
    @ConfigEntry(path = "notify-on-unlock")
    @SuppressWarnings("javadoc")
    private boolean notifyUnlockedBiomes = true;

    @ConfigComment("")
    @ConfigComment("This indicates if biome on island must be restored to default biome, if")
    @ConfigComment("island changes owner and it does not have biome set ability.")
    @ConfigComment("'true' means that biome will be reset.")
    @ConfigEntry(path = "reset-biomes")
    @SuppressWarnings("javadoc")
    private boolean resetBiomes = false;

    @ConfigComment("")
    @ConfigComment("This indicates cool down in seconds between users can change biomes on their island.")
    @ConfigEntry(path = "cooldown")
    @SuppressWarnings("javadoc")
    private int coolDown = 60;

    @ConfigComment("")
    @ConfigComment("Number of minutes in which biome change will be cancelled. It may happen in busy server or")
    @ConfigComment("huge island areas.")
    @ConfigComment("Default value = 5")
    @ConfigEntry(path = "biome-change-timeout")
    @SuppressWarnings("javadoc")
    private long changeTimeout = 5;

    @ConfigComment("")
    @ConfigComment("Number of biome changes at the same time. Setting it to 0 or lower will disable biome change")
    @ConfigComment("completely.")
    @ConfigComment("Default value = 1")
    @ConfigEntry(path = "parallel-biome-changes")
    @SuppressWarnings("javadoc")
    private int concurrentBiomeUpdates = 1;

    @ConfigComment("Player main sub-command to access the addon.")
    @ConfigComment("This command label will be required to write after gamemode player command label, f.e. /[label] biomes")
    @ConfigComment("Each alias must be separated with an empty space.")
    @ConfigEntry(path = "commands.player.main", needsRestart = true)
    @SuppressWarnings("javadoc")
    private String playerCommand = "biomes";

    @ConfigComment("Player set sub-command that allows to set biome with a command.")
    @ConfigComment("This command label will be required to write after player main command, f.e. /[label] biomes set")
    @ConfigComment("Each alias must be separated with an empty space.")
    @ConfigEntry(path = "commands.player.set", needsRestart = true)
    @SuppressWarnings("javadoc")
    private String playerSetCommand = "set";

    @ConfigComment("Player buy sub-command that allows to buy biomes with a command.")
    @ConfigComment("This command label will be required to write after player main command, f.e. /[label] biomes buy")
    @ConfigComment("Each alias must be separated with an empty space.")
    @ConfigEntry(path = "commands.player.buy", needsRestart = true)
    @SuppressWarnings("javadoc")
    private String playerBuyCommand = "buy";

    @ConfigComment("Player info sub-command that allows to see info about biome with a command.")
    @ConfigComment("This command label will be required to write after player main command, f.e. /[label] biomes info")
    @ConfigComment("Each alias must be separated with an empty space.")
    @ConfigEntry(path = "commands.player.info", needsRestart = true)
    @SuppressWarnings("javadoc")
    private String playerInfoCommand = "info";

    @ConfigComment("Admin main sub-command to access the addon.")
    @ConfigComment("This command label will be required to write after gamemode admin command label, f.e. /[label] biomes")
    @ConfigComment("Each alias must be separated with an empty space.")
    @ConfigEntry(path = "commands.admin.main", needsRestart = true)
    @SuppressWarnings("javadoc")
    private String adminCommand = "biomes";

    @ConfigComment("")
    @ConfigComment("This list stores GameModes in which Biomes addon should not work.")
    @ConfigComment("To disable addon it is necessary to write its name in new line that starts with -. Example:")
    @ConfigComment("disabled-gamemodes:")
    @ConfigComment(" - BSkyBlock")
    @ConfigEntry(path = "disabled-gamemodes")
    @SuppressWarnings("javadoc")
    private Set<String> disabledGameModes = new HashSet<>();
}
