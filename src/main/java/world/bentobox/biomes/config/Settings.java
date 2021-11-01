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
 * @author BONNe
 */
@StoreAt(filename="config.yml", path="addons/Biomes")
@ConfigComment("Biomes Configuration [version]")
public class Settings implements ConfigObject
{
    // ---------------------------------------------------------------------
    // Section: Getters
    // ---------------------------------------------------------------------


    /**
     * This method returns if advanced menu is enabled.
     * @return true - if enabled, otherwise false.
     */
    public boolean isAdvancedMenu()
    {
        return this.advancedMenu;
    }


    /**
     * This method returns default update mode.
     * @return Default update mode.
     */
    public UpdateMode getDefaultMode()
    {
        return this.defaultMode;
    }


    /**
     * This method returns default update range.
     * @return default update range
     */
    public int getDefaultSize()
    {
        return this.defaultSize;
    }


    /**
     * This method returns if biome reset is necessary on owner change.
     * @return true - if biome reset is required.
     */
    public boolean isResetBiomes()
    {
        return this.resetBiomes;
    }


    /**
     * This method returns cooldown between biome changes.
     * @return integer that represents seconds between biome changes.
     */
    public int getCoolDown()
    {
        return this.coolDown;
    }


    /**
     * This method returns active biomes visibilityMode.
     * @return Enum that represents biomes visibility.
     */
    public VisibilityMode getVisibilityMode()
    {
        return this.visibilityMode;
    }


    /**
     * This method returns disabled game modes.
     * @return Set with disabled game modes.
     */
    public Set<String> getDisabledGameModes()
    {
        return this.disabledGameModes;
    }


    /**
     * This method returns the useProtectionRange value
     * @return the useProtectionRange value.
     */
    public boolean isUseProtectionRange()
    {
        return this.useProtectionRange;
    }


    // ---------------------------------------------------------------------
    // Section: Setters
    // ---------------------------------------------------------------------


    /**
     * This method sets value of advancedMenu variable.
     * @param advancedMenu new value.
     */
    public void setAdvancedMenu(boolean advancedMenu)
    {
        this.advancedMenu = advancedMenu;
    }


    /**
     * This method sets value of defaultMode variable.
     * @param defaultMode new value.
     */
    public void setDefaultMode(UpdateMode defaultMode)
    {
        this.defaultMode = defaultMode;
    }


    /**
     * This method sets value of defaultSize variable.
     * @param defaultSize new value.
     */
    public void setDefaultSize(int defaultSize)
    {
        this.defaultSize = defaultSize;
    }


    /**
     * This method sets value of resetBiomes variable.
     * @param resetBiomes new value.
     */
    public void setResetBiomes(boolean resetBiomes)
    {
        this.resetBiomes = resetBiomes;
    }


    /**
     * This method sets value of coolDown variable.
     * @param coolDown new value.
     */
    public void setCoolDown(int coolDown)
    {
        this.coolDown = coolDown;
    }


    /**
     * This method sets value of visibilityMode variable.
     * @param visibilityMode new value.
     */
    public void setVisibilityMode(VisibilityMode visibilityMode)
    {
        this.visibilityMode = visibilityMode;
    }


    /**
     * This method sets value for disabledGameModes variable.
     * @param disabledGameModes new value.
     */
    @SuppressWarnings("unused")
    public void setDisabledGameModes(Set<String> disabledGameModes)
    {
        this.disabledGameModes = disabledGameModes;
    }


    /**
     * This method sets the useProtectionRange object value.
     * @param useProtectionRange the useProtectionRange object new value.
     */
    public void setUseProtectionRange(boolean useProtectionRange)
    {
        this.useProtectionRange = useProtectionRange;
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


    /**
     * This enum describes all possible variants which biomes users should see
     */
    public enum VisibilityMode
    {
        /**
         * All visibility mode.
         */
        ALL,
        /**
         * Deployed visibility mode.
         */
        DEPLOYED,
        /**
         * Accessible visibility mode.
         */
        ACCESSIBLE,
        /**
         * Toggleable visibility mode.
         */
        TOGGLEABLE;


        /**
         * This method returns stored parameter from string.
         * @param parameter String of object that must be returned
         * @return CommandParameters object or null.
         */
        public static VisibilityMode getMode(String parameter)
        {
            return BY_NAME.get(parameter);
        }

        /**
         * This map allows to access all enum values via their string.
         */
        private static final Map<String, VisibilityMode> BY_NAME = new HashMap<>();

        /*
          This static method populated BY_NAME map.
         */
        static
        {
            for (VisibilityMode visibility : VisibilityMode.values())
            {
                BY_NAME.put(visibility.name(), visibility);
            }
        }
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------


    @ConfigComment("This indicates if advanced menu will be enabled in Biomes GUI when users choose")
    @ConfigComment("biome or not. Advanced menu contains extra buttons that allows to choose biome")
    @ConfigComment("change mode and range.")
    @ConfigEntry(path = "advanced-menu")
    @SuppressWarnings("javadoc")
    private boolean advancedMenu = true;

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
    @ConfigComment("This variable allows to choose which biomes users can see in Biomes GUI.")
    @ConfigComment("Valid values are:")
    @ConfigComment("    'ALL' - there will be no hidden biomes. All biomes will be viewable in GUI.")
    @ConfigComment("    'DEPLOYED' - shows all biomes that are deployed.")
    @ConfigComment("    'ACCESSIBLE' - only biomes that is unlocked via permission or other unlock type will be visible in GUI.")
    @ConfigComment("    'TOGGLEABLE' - there will be button in GUI that allows users to switch from ALL to ACCESSIBLE modes.")
    @ConfigEntry(path = "biomes-visibility")
    @SuppressWarnings("javadoc")
    private VisibilityMode visibilityMode = VisibilityMode.DEPLOYED;

    @ConfigComment("")
    @ConfigComment("This list stores GameModes in which Biomes addon should not work.")
    @ConfigComment("To disable addon it is necessary to write its name in new line that starts with -. Example:")
    @ConfigComment("disabled-gamemodes:")
    @ConfigComment(" - BSkyBlock")
    @ConfigEntry(path = "disabled-gamemodes")
    @SuppressWarnings("javadoc")
    private Set<String> disabledGameModes = new HashSet<>();
}
