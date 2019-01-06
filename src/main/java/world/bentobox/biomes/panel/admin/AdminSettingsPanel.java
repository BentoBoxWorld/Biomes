package world.bentobox.biomes.panel.admin;


import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import java.util.List;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.biomes.BiomesAddon;
import world.bentobox.biomes.panel.CommonPanel;
import world.bentobox.biomes.utils.Utils;
import world.bentobox.biomes.utils.Utils.UpdateMode;
import world.bentobox.biomes.utils.Utils.VisibilityMode;


/**
 * This Panel allows to edit biome settings via GUI.
 */
public class AdminSettingsPanel extends CommonPanel
{
	/**
	 * @inheritDoc
	 */
	public AdminSettingsPanel(BiomesAddon addon,
		World world,
		User user,
		String topLabel,
		String permissionPrefix)
	{
		this(addon, world, user, topLabel, permissionPrefix, null);
	}


	/**
	 * @inheritDoc
	 */
	public AdminSettingsPanel(BiomesAddon addon,
		World world,
		User user,
		String topLabel,
		String permissionPrefix,
		CommonPanel parentPanel)
	{
		super(addon, world, user, topLabel, permissionPrefix, parentPanel);
		this.initDefaultButtons();
	}


	/**
	 * These buttons always will be enabled. Others must be generated only when needed.
	 */
	private void initDefaultButtons()
	{
		this.currentEditMode = ConfigButtons.NULL;
		this.returnButton = this.parentPanel == null ? null : this.createCommonButton(CommonButtons.RETURN);
	}


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


	@Override
	public void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder().user(this.user).
			name(this.user.getTranslation("biomes.gui.admin.settings-title"));

		// Add Configuration buttons.
		panelBuilder.item(0,
			this.createConfigButtons(ConfigButtons.ADVANCED_MENU,
				this.currentEditMode.equals(ConfigButtons.ADVANCED_MENU)));
		panelBuilder.item(9,
			this.createConfigButtons(ConfigButtons.DEFAULT_MODE,
				this.currentEditMode.equals(ConfigButtons.DEFAULT_MODE)));
		panelBuilder.item(18,
			this.createConfigButtons(ConfigButtons.DEFAULT_SIZE,
				this.currentEditMode.equals(ConfigButtons.DEFAULT_SIZE)));
		panelBuilder.item(27,
			this.createConfigButtons(ConfigButtons.COOLDOWN,
				this.currentEditMode.equals(ConfigButtons.COOLDOWN)));
		panelBuilder.item(36,
			this.createConfigButtons(ConfigButtons.RESET_BIOMES,
				this.currentEditMode.equals(ConfigButtons.RESET_BIOMES)));
		panelBuilder.item(45,
			this.createConfigButtons(ConfigButtons.BIOMES_VISIBILITY,
				this.currentEditMode.equals(ConfigButtons.BIOMES_VISIBILITY)));

		// Add buttons that allows to change value.
		switch (this.currentEditMode)
		{
			case ADVANCED_MENU:
			{
				// Create save button with custom handler
				panelBuilder.item(1, this.createCommonButton(CommonButtons.SAVE,
					(panel, user1, clickType, slot) -> {
						this.addon.getSettings().setAdvancedMenu((boolean) this.valueObject);
						this.currentEditMode = ConfigButtons.NULL;
						this.valueObject = null;
						this.build();
						return true;
					}));

				// Create cancel button with custom handler
				panelBuilder.item(2, this.createCommonButton(CommonButtons.CANCEL,
					(panel, user1, clickType, slot) -> {
						this.currentEditMode = ConfigButtons.NULL;
						this.valueObject = null;
						this.build();
						return true;
					}));

				panelBuilder.item(4, this.createCommonButton(CommonButtons.ENABLED));
				panelBuilder.item(5, this.createCommonButton(CommonButtons.DISABLED));

				break;
			}
			case DEFAULT_MODE:
			{
				// Create save button with custom handler
				panelBuilder.item(10, this.createCommonButton(CommonButtons.SAVE,
					(panel, user1, clickType, slot) -> {
						this.addon.getSettings().setDefaultMode((UpdateMode) this.valueObject);
						this.currentEditMode = ConfigButtons.NULL;
						this.valueObject = null;
						this.build();
						return true;
					}));

				// Create cancel button with custom handler
				panelBuilder.item(11, this.createCommonButton(CommonButtons.CANCEL,
					(panel, user1, clickType, slot) -> {
						this.currentEditMode = ConfigButtons.NULL;
						this.valueObject = null;
						this.build();
						return true;
					}));

				panelBuilder.item(4, this.createCommonButton(CommonButtons.ISLAND));
				panelBuilder.item(13, this.createCommonButton(CommonButtons.CHUNK));
				panelBuilder.item(22, this.createCommonButton(CommonButtons.SQUARE));

				break;
			}
			case DEFAULT_SIZE:
			{
				// Create save button with custom handler
				panelBuilder.item(19, this.createCommonButton(CommonButtons.SAVE,
					(panel, user1, clickType, slot) -> {
						this.addon.getSettings().setDefaultSize((int) this.valueObject);
						this.currentEditMode = ConfigButtons.NULL;
						this.valueObject = null;
						this.build();
						return true;
					}));

				// Create cancel button with custom handler
				panelBuilder.item(20, this.createCommonButton(CommonButtons.CANCEL,
					(panel, user1, clickType, slot) -> {
						this.currentEditMode = ConfigButtons.NULL;
						this.valueObject = null;
						this.build();
						return true;
					}));

				panelBuilder.item(14, this.createCommonButton(NumberButtons.SET_1));
				panelBuilder.item(23, this.createCommonButton(NumberButtons.SET_5));
				panelBuilder.item(32, this.createCommonButton(NumberButtons.SET_10));
				panelBuilder.item(41, this.createCommonButton(NumberButtons.SET_50));

				panelBuilder.item(15, this.createCommonButton(NumberButtons.INCREASE_1));
				panelBuilder.item(24, this.createCommonButton(NumberButtons.INCREASE_5));
				panelBuilder.item(33, this.createCommonButton(NumberButtons.INCREASE_10));
				panelBuilder.item(42, this.createCommonButton(NumberButtons.INCREASE_50));

				panelBuilder.item(16, this.createCommonButton(NumberButtons.DECREASE_1));
				panelBuilder.item(25, this.createCommonButton(NumberButtons.DECREASE_5));
				panelBuilder.item(34, this.createCommonButton(NumberButtons.DECREASE_10));
				panelBuilder.item(43, this.createCommonButton(NumberButtons.DECREASE_50));

				panelBuilder.item(22, this.createCommonButton(NumberButtons.VALUE));

				break;
			}
			case RESET_BIOMES:
			{
				// Create save button with custom handler
				panelBuilder.item(37, this.createCommonButton(CommonButtons.SAVE,
					(panel, user1, clickType, slot) -> {
						this.addon.getSettings().setResetBiomes((boolean) this.valueObject);
						this.currentEditMode = ConfigButtons.NULL;
						this.valueObject = null;
						this.build();
						return true;
					}));

				// Create cancel button with custom handler
				panelBuilder.item(38, this.createCommonButton(CommonButtons.CANCEL,
					(panel, user1, clickType, slot) -> {
						this.currentEditMode = ConfigButtons.NULL;
						this.valueObject = null;
						this.build();
						return true;
					}));

				panelBuilder.item(40, this.createCommonButton(CommonButtons.ENABLED));
				panelBuilder.item(41, this.createCommonButton(CommonButtons.DISABLED));

				break;
			}
			case COOLDOWN:
			{
				// Create save button with custom handler
				panelBuilder.item(28, this.createCommonButton(CommonButtons.SAVE,
					(panel, user1, clickType, slot) -> {
						this.addon.getSettings().setCoolDown((int) this.valueObject);
						this.currentEditMode = ConfigButtons.NULL;
						this.valueObject = null;
						this.build();
						return true;
					}));

				// Create cancel button with custom handler
				panelBuilder.item(29, this.createCommonButton(CommonButtons.CANCEL,
					(panel, user1, clickType, slot) -> {
						this.currentEditMode = ConfigButtons.NULL;
						this.valueObject = null;
						this.build();
						return true;
					}));

				panelBuilder.item(14, this.createCommonButton(NumberButtons.SET_10));
				panelBuilder.item(23, this.createCommonButton(NumberButtons.SET_50));
				panelBuilder.item(32, this.createCommonButton(NumberButtons.SET_100));
				panelBuilder.item(41, this.createCommonButton(NumberButtons.SET_500));

				panelBuilder.item(15, this.createCommonButton(NumberButtons.INCREASE_10));
				panelBuilder.item(24, this.createCommonButton(NumberButtons.INCREASE_50));
				panelBuilder.item(33, this.createCommonButton(NumberButtons.INCREASE_100));
				panelBuilder.item(42, this.createCommonButton(NumberButtons.INCREASE_500));

				panelBuilder.item(16, this.createCommonButton(NumberButtons.DECREASE_10));
				panelBuilder.item(25, this.createCommonButton(NumberButtons.DECREASE_50));
				panelBuilder.item(34, this.createCommonButton(NumberButtons.DECREASE_100));
				panelBuilder.item(43, this.createCommonButton(NumberButtons.DECREASE_500));

				panelBuilder.item(22, this.createCommonButton(NumberButtons.VALUE));

				break;
			}
			case BIOMES_VISIBILITY:
			{
				// Create save button with custom handler
				panelBuilder.item(46, this.createCommonButton(CommonButtons.SAVE,
					(panel, user1, clickType, slot) -> {
						this.addon.getSettings().setVisibilityMode((VisibilityMode) this.valueObject);
						this.currentEditMode = ConfigButtons.NULL;
						this.valueObject = null;
						this.build();
						return true;
					}));

				// Create cancel button with custom handler
				panelBuilder.item(47, this.createCommonButton(CommonButtons.CANCEL,
					(panel, user1, clickType, slot) -> {
						this.currentEditMode = ConfigButtons.NULL;
						this.valueObject = null;
						this.build();
						return true;
					}));

				panelBuilder.item(4, this.createCommonButton(CommonButtons.VISIBLE_ALL));
				panelBuilder.item(13, this.createCommonButton(CommonButtons.VISIBLE_ACTIVE));
				panelBuilder.item(22, this.createCommonButton(CommonButtons.VISIBLE_UNLOCKED));
				panelBuilder.item(31, this.createCommonButton(CommonButtons.VISIBLE_TOGGLE));

				break;
			}
			default:
				if (this.returnButton != null)
				{
					panelBuilder.item(53, this.returnButton);
				}
		}

		panelBuilder.build();
	}


	/**
	 * This method creates button with requested type.
	 * @param buttonType Button type
	 * @return Button that was requested.
	 */
	private PanelItem createConfigButtons(ConfigButtons buttonType, boolean glow)
	{
		ItemStack icon;
		String name;
		List<String> description;
		PanelItem.ClickHandler clickHandler;

		switch (buttonType)
		{
			case ADVANCED_MENU:
			{
				boolean advancedMenu = this.addon.getSettings().isAdvancedMenu();

				String valueString = advancedMenu ?
					this.user.getTranslation("biomes.gui.admin.descriptions.enabled") :
					this.user.getTranslation("biomes.gui.admin.descriptions.disabled");

				name = this.user.getTranslation("biomes.gui.admin.buttons.advancedmenu", "[value]", valueString);
				description = Utils.splitString(this.user.getTranslation("biomes.gui.admin.descriptions.advancedmenu"));
				icon = new ItemStack(Material.COMMAND_BLOCK);
				clickHandler = (panel, user, clickType, slot) -> {
					this.valueObject = advancedMenu;
					this.currentEditMode = buttonType;
					this.build();
					return true;
				};

				break;
			}
			case DEFAULT_MODE:
			{
				UpdateMode updateMode = this.addon.getSettings().getDefaultMode();

				name = this.user.getTranslation("biomes.gui.admin.buttons.type", "[value]", updateMode.name());
				description = Utils.splitString(this.user.getTranslation("biomes.gui.admin.descriptions.type"));

				if (updateMode.equals(UpdateMode.ISLAND))
				{
					icon = new ItemStack(Material.GRASS_BLOCK);
				}
				else if (updateMode.equals(UpdateMode.CHUNK))
				{
					icon = new ItemStack(Material.DIRT);
				}
				else
				{
					icon = new ItemStack(Material.GLASS);
				}

				clickHandler = (panel, user, clickType, slot) -> {
					this.valueObject = updateMode;
					this.currentEditMode = buttonType;
					this.build();
					return true;
				};

				break;
			}
			case DEFAULT_SIZE:
			{
				int size = this.addon.getSettings().getDefaultSize();

				name = this.user.getTranslation("biomes.gui.admin.buttons.size", "[number]", Integer.toString(size));
				description = Utils.splitString(this.user.getTranslation("biomes.gui.admin.descriptions.size"));
				icon = new ItemStack(Material.PISTON);
				clickHandler = (panel, user, clickType, slot) -> {
					this.valueObject = size;
					this.currentEditMode = buttonType;
					this.build();
					return true;
				};

				break;
			}
			case RESET_BIOMES:
			{
				boolean resetBiomes = this.addon.getSettings().isResetBiomes();

				String valueString = resetBiomes ?
					this.user.getTranslation("biomes.gui.admin.descriptions.enabled") :
					this.user.getTranslation("biomes.gui.admin.descriptions.disabled");

				name = this.user.getTranslation("biomes.gui.admin.buttons.resetBiomes", "[value]", valueString);
				description = Utils.splitString(this.user.getTranslation("biomes.gui.admin.descriptions.resetbiomes"));
				icon = new ItemStack(Material.DROPPER);
				clickHandler = (panel, user, clickType, slot) -> {
					this.valueObject = resetBiomes;
					this.currentEditMode = buttonType;
					this.build();
					return true;
				};

				break;
			}
			case COOLDOWN:
			{
				int cooldown = this.addon.getSettings().getCoolDown();

				name = this.user.getTranslation("biomes.gui.admin.buttons.timeout", "[number]", Integer.toString(cooldown));
				description = Utils.splitString(this.user.getTranslation("biomes.gui.admin.descriptions.timeout"));
				icon = new ItemStack(Material.DAYLIGHT_DETECTOR);
				clickHandler = (panel, user, clickType, slot) -> {
					this.valueObject = cooldown;
					this.currentEditMode = buttonType;
					this.build();
					return true;
				};

				break;
			}
			case BIOMES_VISIBILITY:
			{
				VisibilityMode visibilityMode = this.addon.getSettings().getVisibilityMode();

				name = this.user.getTranslation("biomes.gui.admin.buttons.visibility", "[value]", visibilityMode.name());
				description = Utils.splitString(this.user.getTranslation("biomes.gui.admin.descriptions.visibility"));

				if (visibilityMode.equals(VisibilityMode.ALL))
				{
					icon = new ItemStack(Material.BIRCH_PLANKS);
				}
				else if (visibilityMode.equals(VisibilityMode.DEPLOYED))
				{
					icon = new ItemStack(Material.BIRCH_STAIRS);
				}
				else if (visibilityMode.equals(VisibilityMode.ACCESSIBLE))
				{
					icon = new ItemStack(Material.BIRCH_SLAB);
				}
				else
				{
					icon = new ItemStack(Material.BIRCH_BUTTON);
				}

				clickHandler = (panel, user, clickType, slot) -> {
					this.valueObject = visibilityMode;
					this.currentEditMode = buttonType;
					this.build();
					return true;
				};

				break;
			}
			default:
				// This should never happen.
				return null;
		}

		return new PanelItem(icon, name, description, glow, clickHandler, false);
	}


// ---------------------------------------------------------------------
// Section: Enums
// ---------------------------------------------------------------------


	/**
	 * This are all settings buttons. Each setting has own button.
	 */
	private enum ConfigButtons
	{
		ADVANCED_MENU,
		DEFAULT_MODE,
		DEFAULT_SIZE,
		RESET_BIOMES,
		COOLDOWN,
		BIOMES_VISIBILITY,
		NULL
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

	/**
	 * Mode in which panel currently is hanging.
	 */
	private ConfigButtons currentEditMode;

	/**
	 * Return button. Not necessary to generate all time.
	 */
	private PanelItem returnButton;
}
