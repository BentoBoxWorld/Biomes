package world.bentobox.addons.biomes.panel.admin;


import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

import net.wesjd.anvilgui.AnvilGUI;
import world.bentobox.addons.biomes.BiomesAddon;
import world.bentobox.addons.biomes.objects.BiomesObject;
import world.bentobox.addons.biomes.panel.CommonPanel;
import world.bentobox.addons.biomes.utils.Utils;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.user.User;


/**
 * This panel allows to edit given biome properties.
 */
public class AdminBiomeEditPanel extends CommonPanel
{
	/**
	 * @inheritDoc
	 * @param biome Biome that must be edited.
	 */
	public AdminBiomeEditPanel(BiomesAddon addon,
		World world,
		User user,
		BiomesObject biome,
		String topLabel,
		String permissionPrefix)
	{
		this(addon, world, user, biome, topLabel, permissionPrefix, null);
	}


	/**
	 * @inheritDoc
	 * @param biome Biome that must be edited.
	 */
	public AdminBiomeEditPanel(BiomesAddon addon,
		World world,
		User user,
		BiomesObject biome,
		String topLabel,
		String permissionPrefix,
		CommonPanel parentPanel)
	{
		super(addon, world, user, topLabel, permissionPrefix, parentPanel);
		this.biome = biome;
		this.currentEditMode = PropertyButtons.NULL;
		this.returnButton = this.parentPanel == null ? null : this.createCommonButton(CommonButtons.RETURN);

		this.commandHeader = topLabel + " " + BIOMES + " " + EDIT + " " + biome.getBiomeName() + " ";
	}


	@Override
	public void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder().user(this.user).name(
			this.user.getTranslation("biomes.gui.admin.edit-title", "[biome]", this.biome.getFriendlyName()));

		// If current edit mode is name, then shift biome and description 4 blocks to right.
		int biomeSlot = this.currentEditMode.equals(PropertyButtons.NAME) ? 5 : 1;
		// If current edit mode is biome, then shift description 4 blocks to right.
		int descriptionSlot = this.currentEditMode.equals(PropertyButtons.BIOME) ? 6 : biomeSlot + 1;

		panelBuilder.item(0, this.createPropertyButton(PropertyButtons.NAME));
		panelBuilder.item(biomeSlot, this.createPropertyButton(PropertyButtons.BIOME));
		panelBuilder.item(descriptionSlot, this.createPropertyButton(PropertyButtons.DESCRIPTION));
		panelBuilder.item(9, this.createPropertyButton(PropertyButtons.DEPLOYED));
		panelBuilder.item(18, this.createPropertyButton(PropertyButtons.ICON));

		panelBuilder.item(27, this.createPropertyButton(PropertyButtons.LEVEL));
		panelBuilder.item(36, this.createPropertyButton(PropertyButtons.COST));
		panelBuilder.item(45, this.createPropertyButton(PropertyButtons.PERMISSION));

		switch (this.currentEditMode)
		{
			case BIOME:
			{
				// Create save button with custom handler
				panelBuilder.item(2, this.createCommonButton(CommonButtons.SAVE,
					(panel, user1, clickType, slot) -> {
						if (this.user.performCommand(this.commandHeader + "biomeName " + this.valueObject))
						{
							this.currentEditMode = PropertyButtons.NULL;
							this.valueObject = null;
						}

						this.build();
						return true;
					}));

				// Create cancel button with custom handler
				panelBuilder.item(3, this.createCommonButton(CommonButtons.CANCEL,
					(panel, user1, clickType, slot) -> {
						this.currentEditMode = PropertyButtons.NULL;
						this.valueObject = null;
						this.build();
						return true;
					}));

				// TODO: Button that opens CHOOSE BIOME GUI
//				panelBuilder.item(4, this.createCommonButton(CommonButtons.ENABLED));

				break;
			}
			case NAME:
			{
				// Create save button with custom handler
				panelBuilder.item(1, this.createCommonButton(CommonButtons.SAVE,
					(panel, user1, clickType, slot) -> {
						if (this.user.performCommand(this.commandHeader + "friendlyName " + this.valueObject))
						{
							this.currentEditMode = PropertyButtons.NULL;
							this.valueObject = null;
						}

						this.build();
						return true;
					}));

				// Create cancel button with custom handler
				panelBuilder.item(2, this.createCommonButton(CommonButtons.CANCEL,
					(panel, user1, clickType, slot) -> {
						this.currentEditMode = PropertyButtons.NULL;
						this.valueObject = null;
						this.build();
						return true;
					}));

				panelBuilder.item(3, this.reopenAnvilGui());

				break;
			}
			case DEPLOYED:
			{
				// Create save button with custom handler
				panelBuilder.item(10, this.createCommonButton(CommonButtons.SAVE,
					(panel, user1, clickType, slot) -> {
						if (this.user.performCommand(this.commandHeader + "deployed " + this.valueObject))
						{
							this.currentEditMode = PropertyButtons.NULL;
							this.valueObject = null;
						}

						this.build();
						return true;
					}));

				// Create cancel button with custom handler
				panelBuilder.item(11, this.createCommonButton(CommonButtons.CANCEL,
					(panel, user1, clickType, slot) -> {
						this.currentEditMode = PropertyButtons.NULL;
						this.valueObject = null;
						this.build();
						return true;
					}));

				panelBuilder.item(14, this.createCommonButton(CommonButtons.ENABLED));
				panelBuilder.item(15, this.createCommonButton(CommonButtons.DISABLED));

				break;
			}
			case DESCRIPTION:
			{
				// Create save button with custom handler
				panelBuilder.item(3, this.createCommonButton(CommonButtons.SAVE,
					(panel, user1, clickType, slot) -> {
						if (this.user.performCommand(this.commandHeader + "description " + this.valueObject))
						{
							this.currentEditMode = PropertyButtons.NULL;
							this.valueObject = null;
						}

						this.build();
						return true;
					}));

				// Create cancel button with custom handler
				panelBuilder.item(4, this.createCommonButton(CommonButtons.CANCEL,
					(panel, user1, clickType, slot) -> {
						this.currentEditMode = PropertyButtons.NULL;
						this.valueObject = null;
						this.build();
						return true;
					}));

				panelBuilder.item(5, this.reopenAnvilGui());

				break;
			}
			case ICON:
			{
				// Create save button with custom handler
				panelBuilder.item(19, this.createCommonButton(CommonButtons.SAVE,
					(panel, user1, clickType, slot) -> {
						if (this.user.performCommand(this.commandHeader + "icon " + this.valueObject))
						{
							this.currentEditMode = PropertyButtons.NULL;
							this.valueObject = null;
						}

						this.build();
						return true;
					}));

				// Create cancel button with custom handler
				panelBuilder.item(20, this.createCommonButton(CommonButtons.CANCEL,
					(panel, user1, clickType, slot) -> {
						this.currentEditMode = PropertyButtons.NULL;
						this.valueObject = null;
						this.build();
						return true;
					}));

				// TODO: Button that opens CHOOSE ICON GUI
//				panelBuilder.item(21, this.createCommonButton(CommonButtons.ENABLED));

				break;
			}
			case LEVEL:
			{
				// Create save button with custom handler
				panelBuilder.item(19, this.createCommonButton(CommonButtons.SAVE,
					(panel, user1, clickType, slot) -> {
						if (this.user.performCommand(this.commandHeader + "requiredLevel " + this.valueObject))
						{
							this.currentEditMode = PropertyButtons.NULL;
							this.valueObject = null;
						}

						this.build();
						return true;
					}));

				// Create cancel button with custom handler
				panelBuilder.item(20, this.createCommonButton(CommonButtons.CANCEL,
					(panel, user1, clickType, slot) -> {
						this.currentEditMode = PropertyButtons.NULL;
						this.valueObject = null;
						this.build();
						return true;
					}));

				panelBuilder.item(5, this.createCommonButton(NumberButtons.SET_1));
				panelBuilder.item(14, this.createCommonButton(NumberButtons.SET_5));
				panelBuilder.item(23, this.createCommonButton(NumberButtons.SET_10));
				panelBuilder.item(32, this.createCommonButton(NumberButtons.SET_50));
				panelBuilder.item(41, this.createCommonButton(NumberButtons.SET_100));

				panelBuilder.item(6, this.createCommonButton(NumberButtons.INCREASE_1));
				panelBuilder.item(15, this.createCommonButton(NumberButtons.INCREASE_5));
				panelBuilder.item(24, this.createCommonButton(NumberButtons.INCREASE_10));
				panelBuilder.item(33, this.createCommonButton(NumberButtons.INCREASE_50));
				panelBuilder.item(42, this.createCommonButton(NumberButtons.INCREASE_100));

				panelBuilder.item(7, this.createCommonButton(NumberButtons.DECREASE_1));
				panelBuilder.item(16, this.createCommonButton(NumberButtons.DECREASE_5));
				panelBuilder.item(25, this.createCommonButton(NumberButtons.DECREASE_10));
				panelBuilder.item(34, this.createCommonButton(NumberButtons.DECREASE_50));
				panelBuilder.item(43, this.createCommonButton(NumberButtons.DECREASE_100));

				panelBuilder.item(22, this.createCommonButton(NumberButtons.VALUE));

				break;
			}
			case COST:
			{
				// Create save button with custom handler
				panelBuilder.item(37, this.createCommonButton(CommonButtons.SAVE,
					(panel, user1, clickType, slot) -> {
						if (this.user.performCommand(this.commandHeader + "requiredCost " + this.valueObject))
						{
							this.currentEditMode = PropertyButtons.NULL;
							this.valueObject = null;
						}

						this.build();
						return true;
					}));

				// Create cancel button with custom handler
				panelBuilder.item(38, this.createCommonButton(CommonButtons.CANCEL,
					(panel, user1, clickType, slot) -> {
						this.currentEditMode = PropertyButtons.NULL;
						this.valueObject = null;
						this.build();
						return true;
					}));

				panelBuilder.item(5, this.createCommonButton(NumberButtons.SET_10));
				panelBuilder.item(14, this.createCommonButton(NumberButtons.SET_50));
				panelBuilder.item(23, this.createCommonButton(NumberButtons.SET_100));
				panelBuilder.item(32, this.createCommonButton(NumberButtons.SET_500));
				panelBuilder.item(41, this.createCommonButton(NumberButtons.SET_1000));

				panelBuilder.item(6, this.createCommonButton(NumberButtons.INCREASE_10));
				panelBuilder.item(15, this.createCommonButton(NumberButtons.INCREASE_50));
				panelBuilder.item(24, this.createCommonButton(NumberButtons.INCREASE_100));
				panelBuilder.item(33, this.createCommonButton(NumberButtons.INCREASE_500));
				panelBuilder.item(42, this.createCommonButton(NumberButtons.INCREASE_1000));

				panelBuilder.item(7, this.createCommonButton(NumberButtons.DECREASE_10));
				panelBuilder.item(16, this.createCommonButton(NumberButtons.DECREASE_50));
				panelBuilder.item(25, this.createCommonButton(NumberButtons.DECREASE_100));
				panelBuilder.item(34, this.createCommonButton(NumberButtons.DECREASE_500));
				panelBuilder.item(43, this.createCommonButton(NumberButtons.DECREASE_1000));

				panelBuilder.item(40, this.createCommonButton(NumberButtons.VALUE));

				break;
			}
			case PERMISSION:
			{
				// Create save button with custom handler
				panelBuilder.item(46, this.createCommonButton(CommonButtons.SAVE,
					(panel, user1, clickType, slot) -> {
						if (this.user.performCommand(this.commandHeader + "permission " + this.valueObject))
						{
							this.currentEditMode = PropertyButtons.NULL;
							this.valueObject = null;
						}

						this.build();
						return true;
					}));

				// Create cancel button with custom handler
				panelBuilder.item(47, this.createCommonButton(CommonButtons.CANCEL,
					(panel, user1, clickType, slot) -> {
						this.currentEditMode = PropertyButtons.NULL;
						this.valueObject = null;
						this.build();
						return true;
					}));

				panelBuilder.item(48, this.reopenAnvilGui());

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
	 * This method creates button for each editable property for BiomeObject
	 * @param button Property Button that must be created.
	 * @return New PanelItem of requested property edit function.
	 */
	private PanelItem createPropertyButton(PropertyButtons button)
	{
		ItemStack icon;
		String name;
		List<String> description;
		PanelItem.ClickHandler clickHandler;

		switch (button)
		{
			case BIOME:
			{
				icon = new ItemStack(Material.BOOK);
				name = this.user.getTranslation("biomes.gui.admin.buttons.biome");
				description = Utils.splitString(
					this.user.getTranslation("biomes.gui.admin.descriptions.current", "[value]", this.biome.getBiomeName()));

				clickHandler = (panel, user1, clickType, slot) -> {
					// TODO IMPLEMENT BIOMES GUI
					// Open Biome Choose GUI
					this.currentEditMode = PropertyButtons.BIOME;
					this.valueObject = this.biome.getBiomeName();
					this.build();
					return true;
				};

				break;
			}
			case NAME:
			{
				icon = new ItemStack(Material.BOOK);
				name = this.user.getTranslation("biomes.gui.admin.buttons.name");
				description = Utils.splitString(
					this.user.getTranslation("biomes.gui.admin.descriptions.current", "[value]", this.biome.getFriendlyName()));
				clickHandler = (panel, user1, clickType, slot) -> {
					this.currentEditMode = PropertyButtons.NAME;
					this.valueObject = this.biome.getFriendlyName();

					// Open AnvilGUI that allows to change text.
					new AnvilGUI(this.addon.getPlugin(),
						this.user.getPlayer(),
						this.biome.getFriendlyName(),
						(player, reply) -> {
							if (reply.equalsIgnoreCase(Material.PAPER.name()))
							{
								// Weird anvilGui glitch. Does not allow empty values.
								reply = "";
							}

							// Sets anvil text output as new value object
							this.valueObject = reply;
							// rebuild gui.
							this.build();

							return reply;
						});

					return true;
				};

				break;
			}
			case DEPLOYED:
			{
				icon = this.biome.isDeployed() ? new ItemStack(Material.GREEN_CONCRETE) :  new ItemStack(Material.RED_CONCRETE);
				name = this.user.getTranslation("biomes.gui.admin.buttons.deployed");
				description = Utils.splitString(
					this.user.getTranslation("biomes.gui.admin.descriptions.current",
						"[value]", this.biome.isDeployed() ?
							this.user.getTranslation("biomes.gui.admin.descriptions.enabled") :
							this.user.getTranslation("biomes.gui.admin.descriptions.disabled")));
				clickHandler = (panel, user1, clickType, slot) ->{
					this.currentEditMode = PropertyButtons.DEPLOYED;
					this.valueObject = this.biome.isDeployed();
					this.build();
					return true;
				};

				break;
			}
			case DESCRIPTION:
			{
				icon = new ItemStack(Material.BOOK);
				name = this.user.getTranslation("biomes.gui.admin.buttons.description");
				description = Utils.splitString(
					this.user.getTranslation("biomes.gui.admin.descriptions.current",
						"[value]", Utils.mergeStringList(this.biome.getDescription())));

				clickHandler = (panel, user1, clickType, slot) -> {
					this.currentEditMode = PropertyButtons.DESCRIPTION;
					this.valueObject = Utils.mergeStringList(this.biome.getDescription());

					new AnvilGUI(this.addon.getPlugin(),
						this.user.getPlayer(),
						Utils.mergeStringList(this.biome.getDescription()),
						(player, reply) -> {
							if (reply.equalsIgnoreCase(Material.PAPER.name()))
							{
								// Weird anvilGui glitch. Does not allow empty values.
								reply = "";
							}

							this.valueObject = reply;
							this.build();

							return reply;
						});

					return true;
				};

				break;
			}
			case ICON:
			{
				icon = this.biome.getIcon();
				name = this.user.getTranslation("biomes.gui.admin.buttons.icon");
				description = Collections.emptyList();

				clickHandler = (panel, user1, clickType, slot) -> {
					this.currentEditMode = PropertyButtons.ICON;
					this.valueObject = this.biome.getIcon();
					this.build();
					// TODO IMPLEMENT ICON CHOOSE GUI
					// Open Biome Choose GUI
					return false;
				};

				break;
			}
			case LEVEL:
			{
				icon = new ItemStack(Material.MAGENTA_GLAZED_TERRACOTTA);
				name = this.user.getTranslation("biomes.gui.admin.buttons.level");
				description = Utils.splitString(
					this.user.getTranslation("biomes.gui.admin.descriptions.current",
						"[value]",
						Long.toString(this.biome.getRequiredLevel())));
				clickHandler = (panel, user1, clickType, slot) ->{
					this.currentEditMode = PropertyButtons.LEVEL;
					this.valueObject = (int) this.biome.getRequiredLevel();
					this.build();
					return true;
				};

				break;
			}
			case COST:
			{
				icon = new ItemStack(Material.GOLD_BLOCK);
				name = this.user.getTranslation("biomes.gui.admin.buttons.cost");
				description = Utils.splitString(
					this.user.getTranslation("biomes.gui.admin.descriptions.current",
						"[value]",
						Integer.toString(this.biome.getRequiredCost())));
				clickHandler = (panel, user1, clickType, slot) ->{
					this.currentEditMode = PropertyButtons.COST;
					this.valueObject = this.biome.getRequiredCost();
					this.build();
					return true;
				};

				break;
			}
			case PERMISSION:
			{
				icon = new ItemStack(Material.BOOK);
				name = this.user.getTranslation("biomes.gui.admin.buttons.permission");
				description = Utils.splitString(
					this.user.getTranslation("biomes.gui.admin.descriptions.current", "[value]", this.biome.getPermission()));

				clickHandler = (panel, user1, clickType, slot) -> {
					this.currentEditMode = PropertyButtons.PERMISSION;
					this.valueObject = this.biome.getPermission();

					new AnvilGUI(this.addon.getPlugin(),
						this.user.getPlayer(),
						this.biome.getPermission(),
						(player, reply) -> {
							if (reply.equalsIgnoreCase(Material.PAPER.name()))
							{
								// Weird anvilGui glitch. Does not allow empty values.
								reply = "";
							}

							this.valueObject = reply;
							this.build();

							return reply;
						});

					return true;
				};

				break;
			}
			default:
				// This should never happen.
				return null;
		}

		return new PanelItem(icon, name, description, this.currentEditMode.equals(button), clickHandler, false);
	}


	/**
	 * This method creates button that reopens anvil gui for editing last property.
	 * @return New Button that opens anvil gui.
	 */
	private PanelItem reopenAnvilGui()
	{
		ItemStack icon = new ItemStack(Material.ANVIL);
		String name = this.user.getTranslation("biomes.gui.admin.buttons.change");
		List<String> description = Utils.splitString(
			this.user.getTranslation("biomes.gui.admin.descriptions.current", "[value]", (String) this.valueObject));

		PanelItem.ClickHandler clickHandler = (panel, user1, clickType, slot) -> {
			new AnvilGUI(this.addon.getPlugin(),
				this.user.getPlayer(),
				(String) this.valueObject,
				(player, reply) -> {
					if (reply.equalsIgnoreCase(Material.PAPER.name()))
					{
						// Weird anvilGui glitch. Does not allow empty values.
						reply = "";
					}

					this.valueObject = reply;
					this.build();

					return reply;
				});

			return true;
		};

		return new PanelItem(icon, name, description, false, clickHandler, false);
	}


// ---------------------------------------------------------------------
// Section: Enums
// ---------------------------------------------------------------------


	/**
	 * This Enum contains all possible property values that can be edited.
 	 */
	private enum PropertyButtons
	{
		BIOME,
		NAME,
		DEPLOYED,
		DESCRIPTION,
		ICON,
		LEVEL,
		COST,
		PERMISSION,

		NULL
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


	private BiomesObject biome;

	private PropertyButtons currentEditMode;

	private PanelItem returnButton;

	private final String commandHeader;
}
