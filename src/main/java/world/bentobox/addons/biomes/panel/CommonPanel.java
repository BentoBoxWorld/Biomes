package world.bentobox.addons.biomes.panel;


import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

import world.bentobox.addons.biomes.BiomesAddon;
import world.bentobox.addons.biomes.utils.Utils.VisibilityMode;
import world.bentobox.addons.biomes.utils.Utils.UpdateMode;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.user.User;



/**
 * This class contains common methods that is necessary for biomes addon panels.
 */
public abstract class CommonPanel
{
	/**
	 * Default constructor that inits panels with minimal requirements, without parent panel.
	 *
	 * @param addon Addon where panel operates.
	 * @param world World from which panel was created.
	 * @param user User who created panel.
	 * @param topLabel Command top label which creates panel (f.e. island or ai)
	 * @param permissionPrefix Command permission prefix (f.e. bskyblock.)
	 */
	public CommonPanel(BiomesAddon addon,
		World world,
		User user,
		String topLabel,
		String permissionPrefix)
	{
		this(addon, world, user, topLabel, permissionPrefix, null);
	}


	/**
	 * Default constructor that inits panels with minimal requirements.
	 *
	 * @param addon Addon where panel operates.
	 * @param world World from which panel was created.
	 * @param user User who created panel.
	 * @param topLabel Command top label which creates panel (f.e. island or ai)
	 * @param permissionPrefix Command permission prefix (f.e. bskyblock.)
	 * @param parentPanel Parent panel for current panel.
	 */
	public CommonPanel(BiomesAddon addon,
		World world,
		User user,
		String topLabel,
		String permissionPrefix,
		CommonPanel parentPanel)
	{
		this.addon = addon;
		this.world = world;
		this.user = user;

		this.topLabel = topLabel;
		this.permissionPrefix = permissionPrefix;

		this.parentPanel = parentPanel;

		this.pageIndex = 0;
	}


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


	/**
	 * This method is necessary to build panel.
	 */
	public abstract void build();


	/**
	 * This method returns button from CommonButtons enum.
	 * @param button Necessary button.
	 * @return new PanelItem button.
	 */
	protected PanelItem createCommonButton(CommonButtons button)
	{
		return this.createCommonButton(button, null);
	}


	/**
	 * This method returns button from CommonButtons enum.
	 * @param button Necessary button.
	 * @param handler Custom click handler
	 * @return new PanelItem button.
	 */
	protected PanelItem createCommonButton(CommonButtons button, PanelItem.ClickHandler handler)
	{
		ItemStack icon;
		String name;
		List<String> description;
		boolean glow;
		PanelItem.ClickHandler clickHandler;

		switch (button)
		{
			case RETURN:
			{
				name = this.user.getTranslation("biomes.gui.buttons.back");
				description = Collections.emptyList();
				icon = new ItemStack(Material.OAK_DOOR);
				clickHandler = (panel, user, clickType, slot) -> {
					// Build parent panel is missing, then close inventory.
					if (this.parentPanel == null)
					{
						this.user.closeInventory();
					}

					this.parentPanel.build();
					return true;
				};
				glow = false;

				break;
			}
			case NEXT:
			{
				name = this.user.getTranslation("biomes.gui.buttons.next");
				description = Collections.emptyList();
				icon = new ItemStack(Material.SIGN);
				clickHandler = (panel, user, clickType, slot) -> {
					this.pageIndex++;
					this.build();
					return true;
				};
				glow = false;

				break;
			}
			case PREVIOUS:
			{
				name = this.user.getTranslation("biomes.gui.buttons.previous");
				description = Collections.emptyList();
				icon = new ItemStack(Material.SIGN);
				clickHandler = (panel, user, clickType, slot) -> {
					this.pageIndex--;
					this.build();
					return true;
				};
				glow = false;

				break;
			}
			case SAVE:
			{
				name = this.user.getTranslation("biomes.gui.admin.buttons.save");
				description = Collections.emptyList();
				icon = new ItemStack(Material.PAPER);
				// This will be overwrite by use case.
				clickHandler = handler;
				glow = false;

				break;
			}
			case CANCEL:
			{
				name = this.user.getTranslation("biomes.gui.admin.buttons.cancel");
				description = Collections.emptyList();
				icon = new ItemStack(Material.BARRIER);
				clickHandler = handler;
				glow = false;

				break;
			}
			case ENABLED:
			{
				name = this.user.getTranslation("biomes.gui.admin.buttons.enabled");
				description = Collections.emptyList();
				icon = new ItemStack(Material.GREEN_CONCRETE);
				clickHandler = (panel, user1, clickType, slot) -> {
					this.valueObject = true;
					this.build();
					return true;
				};
				glow = (boolean) this.valueObject;

				break;
			}
			case DISABLED:
			{
				name = this.user.getTranslation("biomes.gui.admin.buttons.disabled");
				description = Collections.emptyList();
				icon = new ItemStack(Material.RED_CONCRETE);
				clickHandler = (panel, user1, clickType, slot) -> {
					this.valueObject = false;
					this.build();
					return true;
				};
				glow = !(boolean) this.valueObject;

				break;
			}
			case ISLAND:
			{
				name = this.user.getTranslation("biomes.gui.buttons.island");
				description = Collections.emptyList();
				icon = new ItemStack(Material.GRASS_BLOCK);
				clickHandler = (panel, user1, clickType, slot) -> {
					this.valueObject = UpdateMode.ISLAND;
					this.build();
					return true;
				};
				glow = this.valueObject.equals(UpdateMode.ISLAND);

				break;
			}
			case CHUNK:
			{
				name = this.user.getTranslation("biomes.gui.buttons.chunk");
				description = Collections.emptyList();
				icon = new ItemStack(Material.DIRT);
				clickHandler = (panel, user1, clickType, slot) -> {
					this.valueObject = UpdateMode.CHUNK;
					this.build();
					return true;
				};
				glow = this.valueObject.equals(UpdateMode.CHUNK);

				break;
			}
			case SQUARE:
			{
				name = this.user.getTranslation("biomes.gui.buttons.region");
				description = Collections.emptyList();
				icon = new ItemStack(Material.GLASS);
				clickHandler = (panel, user1, clickType, slot) -> {
					this.valueObject = UpdateMode.SQUARE;
					this.build();
					return true;
				};
				glow = this.valueObject.equals(UpdateMode.SQUARE);

				break;
			}
			case VISIBLE_ALL:
			{
				name = this.user.getTranslation("biomes.gui.admin.buttons.visible-all");
				description = Collections.emptyList();
				icon = new ItemStack(Material.BIRCH_PLANKS);
				clickHandler = (panel, user1, clickType, slot) -> {
					this.valueObject = VisibilityMode.ALL;
					this.build();
					return true;
				};
				glow = this.valueObject.equals(VisibilityMode.ALL);

				break;
			}
			case VISIBLE_ACTIVE:
			{
				name = this.user.getTranslation("biomes.gui.admin.buttons.visible-active");
				description = Collections.emptyList();
				icon = new ItemStack(Material.BIRCH_STAIRS);
				clickHandler = (panel, user1, clickType, slot) -> {
					this.valueObject = VisibilityMode.DEPLOYED;
					this.build();
					return true;
				};
				glow = this.valueObject.equals(VisibilityMode.DEPLOYED);
				break;
			}
			case VISIBLE_UNLOCKED:
			{
				name = this.user.getTranslation("biomes.gui.admin.buttons.visible-accessible");
				description = Collections.emptyList();
				icon = new ItemStack(Material.BIRCH_SLAB);
				clickHandler = (panel, user1, clickType, slot) -> {
					this.valueObject = VisibilityMode.ACCESSIBLE;
					this.build();
					return true;
				};
				glow = this.valueObject.equals(VisibilityMode.ACCESSIBLE);
				break;
			}
			case VISIBLE_TOGGLE:
			{
				name = this.user.getTranslation("biomes.gui.admin.buttons.visible-toggle");
				description = Collections.emptyList();
				icon = new ItemStack(Material.BIRCH_BUTTON);
				clickHandler = (panel, user1, clickType, slot) -> {
					this.valueObject = VisibilityMode.TOGGLEABLE;
					this.build();
					return true;
				};
				glow = this.valueObject.equals(VisibilityMode.TOGGLEABLE);
				break;
			}
			default:
				// All buttons should be in switch case.
				return null;
		}

		return new PanelItem(icon, name, description, glow, clickHandler, false);
	}


	/**
	 * This method returns button from NumberButtons enum.
	 * @param button Necessary button.
	 * @return new PanelItem button.
	 */
	protected PanelItem createCommonButton(NumberButtons button)
	{
		int number;

		boolean increase = false;
		boolean reduce = false;

		switch (button)
		{
			case SET_1:
				number = 1;
				break;
			case SET_5:
				number = 5;
				break;
			case SET_10:
				number = 10;
				break;
			case SET_50:
				number = 50;
				break;
			case SET_100:
				number = 100;
				break;
			case SET_500:
				number = 500;
				break;
			case SET_1000:
				number = 1000;
				break;
			case INCREASE_1:
				number = 1;
				increase = true;
				break;
			case INCREASE_5:
				number = 5;
				increase = true;
				break;
			case INCREASE_10:
				number = 10;
				increase = true;
				break;
			case INCREASE_50:
				number = 50;
				increase = true;
				break;
			case INCREASE_100:
				number = 100;
				increase = true;
				break;
			case INCREASE_500:
				number = 500;
				increase = true;
				break;
			case INCREASE_1000:
				number = 1000;
				increase = true;
				break;
			case DECREASE_1:
				number = 1;
				reduce = true;
				break;
			case DECREASE_5:
				number = 5;
				reduce = true;
				break;
			case DECREASE_10:
				number = 10;
				reduce = true;
				break;
			case DECREASE_50:
				number = 50;
				reduce = true;
				break;
			case DECREASE_100:
				number = 100;
				reduce = true;
				break;
			case DECREASE_500:
				number = 500;
				reduce = true;
				break;
			case DECREASE_1000:
				number = 1000;
				reduce = true;
				break;
			case VALUE:
				number = (int) this.valueObject;
				increase = true;
				reduce = true;
				break;
			default:
				// All buttons should be in switch case.
				return null;
		}

		ItemStack icon;
		String name;
		PanelItem.ClickHandler clickHandler;

		if (increase && reduce)
		{
			icon = new ItemStack(Material.PAPER);
			name = this.user.getTranslation("biomes.gui.buttons.value","[number]", Integer.toString(number));
			clickHandler = (panel, user1, clickType, slot) -> {
				this.valueObject = (int) this.valueObject + number;
				this.build();
				return true;
			};
		}
		else if (increase)
		{
			icon = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
			name = this.user.getTranslation("biomes.gui.buttons.increase","[number]", Integer.toString(number));
			clickHandler = (panel, user1, clickType, slot) -> {
				this.valueObject = (int) this.valueObject + number;
				this.build();
				return true;
			};
		}
		else if (reduce)
		{
			icon = new ItemStack(Material.RED_STAINED_GLASS_PANE);
			name = this.user.getTranslation("biomes.gui.buttons.reduce","[number]", Integer.toString(number));
			clickHandler = (panel, user1, clickType, slot) -> {
				this.valueObject = (int) this.valueObject - number;
				this.build();
				return true;
			};
		}
		else
		{
			icon = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
			name = this.user.getTranslation("biomes.gui.buttons.set","[number]", Integer.toString(number));
			clickHandler = (panel, user1, clickType, slot) -> {
				this.valueObject = number;
				this.build();
				return true;
			};
		}

		return new PanelItem(icon, name, Collections.emptyList(), false, clickHandler, false);
	}


// ---------------------------------------------------------------------
// Section: Enums
// ---------------------------------------------------------------------


	/**
	 * This enum contains all common buttons that is used in more then 1 panel.
	 */
	protected enum CommonButtons
	{
		RETURN,
		NEXT,
		PREVIOUS,

		SAVE,
		CANCEL,

		ENABLED,
		DISABLED,

		ISLAND,
		CHUNK,
		SQUARE,

		VISIBLE_ALL,
		VISIBLE_ACTIVE,
		VISIBLE_UNLOCKED,
		VISIBLE_TOGGLE,
	}


	/**
	 * This enum contains buttons that is made of numbers.
	 */
	protected enum NumberButtons
	{
		SET_1,
		SET_5,
		SET_10,
		SET_50,
		SET_100,
		SET_500,
		SET_1000,

		INCREASE_1,
		INCREASE_5,
		INCREASE_10,
		INCREASE_50,
		INCREASE_100,
		INCREASE_500,
		INCREASE_1000,

		DECREASE_1,
		DECREASE_5,
		DECREASE_10,
		DECREASE_50,
		DECREASE_100,
		DECREASE_500,
		DECREASE_1000,

		VALUE
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

	/**
	 * Variable stores biomes addon.
	 */
	protected BiomesAddon addon;

	/**
	 * Variable stores world in which panel is referred to.
	 */
	protected World world;

	/**
	 * Variable stores user who created this panel.
	 */
	protected User user;

	/**
	 * Variable stores top label of command from which panel was called.
	 */
	protected String topLabel;

	/**
	 * Variable stores permission prefix of command from which panel was called.
	 */
	protected String permissionPrefix;

	/**
	 * Variable stores parent of current panel.
	 */
	protected CommonPanel parentPanel;

	/**
	 * Variable stores any value.
	 */
	protected Object valueObject;

	/**
	 * This object holds current page index.
	 */
	protected int pageIndex;


// ---------------------------------------------------------------------
// Section: Constants for permission and command generation
// ---------------------------------------------------------------------

	protected final static String BIOMES = "biomes";

	protected final static String ADMIN = "admin";

	protected final static String SET = "set";

	protected final static String ADD = "add";

	protected final static String EDIT = "edit";

	protected final static String DELETE = "remove";

	protected final static String SETTINGS = "settings";

	protected final static String IMPORT = "import";

	protected final static String INFO = "info";
}
