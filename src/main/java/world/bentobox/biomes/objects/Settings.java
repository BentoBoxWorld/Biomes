package world.bentobox.biomes.objects;


import java.util.HashSet;
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
@ConfigComment("This config file is dynamic and saved when the server is shutdown.")
@ConfigComment("You cannot edit it while the server is running because changes will")
@ConfigComment("be lost! Use in-game settings GUI or edit when server is offline.")
@ConfigComment("")
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
	public void setDisabledGameModes(Set<String> disabledGameModes)
	{
		this.disabledGameModes = disabledGameModes;
	}


// ---------------------------------------------------------------------
// Section: Enums used for Settings.
// ---------------------------------------------------------------------


	/**
	 * This enum describes all possible variants how to calculate new biome location.
	 */
	public enum UpdateMode
	{
		ISLAND,
		CHUNK,
		SQUARE
	}


	/**
	 * This enum describes all possible variants which biomes users should see
	 */
	public enum VisibilityMode
	{
		ALL,
		ACCESSIBLE,
		DEPLOYED,
		TOGGLEABLE
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


	@ConfigComment("This indicates if advanced menu will be enabled in Biomes GUI when users choose")
	@ConfigComment("biome or not. Advanced menu contains extra buttons that allows to choose biome")
	@ConfigComment("change mode and range.")
	@ConfigEntry(path = "advanced-menu")
	private boolean advancedMenu = true;

	@ConfigComment("")
	@ConfigComment("For advanced menu this indicate which mode will be active.")
	@ConfigComment("If advanced menu is disabled or in biome set command update mode is not passed")
	@ConfigComment("then update algorithm will use this value as update mode.")
	@ConfigComment("Valid values are:")
	@ConfigComment("    'ISLAND' - updates biome on whole island")
	@ConfigComment("    'CHUNK' - updates biome on whole chunks around player")
	@ConfigComment("    'SQUARE' - updates biome by block in given range")
	@ConfigEntry(path = "default-mode")
	private UpdateMode defaultMode = UpdateMode.ISLAND;

	@ConfigComment("")
	@ConfigComment("For advanced menu this indicate how large range will be set on GUI opening.")
	@ConfigComment("If advanced menu is disabled or in biome set command update range is not set")
	@ConfigComment("then update algorithm will use this value as update range.")
	@ConfigEntry(path = "default-size")
	private int defaultSize = 3;

	@ConfigComment("")
	@ConfigComment("This indicates if biome on island must be restored to default biome, if")
	@ConfigComment("island changes owner and it does not have biome set ability.")
	@ConfigComment("'true' means that biome will be reset.")
	@ConfigEntry(path = "reset-biomes")
	private boolean resetBiomes = false;

	@ConfigComment("")
	@ConfigComment("This indicates cool down in seconds between users can change biomes on their island.")
	@ConfigEntry(path = "cooldown")
	private int coolDown = 60;

	@ConfigComment("")
	@ConfigComment("This variable allows to choose which biomes users can see in Biomes GUI.")
	@ConfigComment("Valid values are:")
	@ConfigComment("    'ALL' - there will be no hidden biomes. All biomes will be viewable in GUI.")
	@ConfigComment("    'DEPLOYED' - shows all biomes that are deployed.")
	@ConfigComment("    'ACCESSIBLE' - only biomes that is unlocked via permission or other unlock type will be visible in GUI.")
	@ConfigComment("    'TOGGLEABLE' - there will be button in GUI that allows users to switch from ALL to ACCESSIBLE modes.")
	@ConfigEntry(path = "biomes-visibility")
	private VisibilityMode visibilityMode = VisibilityMode.DEPLOYED;

	@ConfigComment("")
	@ConfigComment("This list stores GameModes in which Biomes addon should not work.")
	@ConfigComment("To disable addon it is necessary to write its name in new line that starts with -. Example:")
	@ConfigComment("disabled-gamemodes:")
	@ConfigComment(" - BSkyBlock")
	@ConfigEntry(path = "disabled-gamemodes")
	private Set<String> disabledGameModes = new HashSet<>();
}
