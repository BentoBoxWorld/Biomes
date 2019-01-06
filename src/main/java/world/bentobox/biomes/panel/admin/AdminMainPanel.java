package world.bentobox.addons.biomes.panel.admin;


import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

import world.bentobox.addons.biomes.BiomesAddon;
import world.bentobox.addons.biomes.panel.CommonPanel;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.user.User;


/**
 * This is Admin Main Panel. It contains all abilities that admin has available.
 */
public class AdminMainPanel extends CommonPanel
{
	public AdminMainPanel(BiomesAddon addon,
		World world,
		User user,
		String topLabel,
		String permissionPrefix)
	{
		super(addon, world, user, topLabel, permissionPrefix);
	}


	/**
	 * This method construct admin panel with predefined button placements.
	 */
	@Override
	public void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder().user(this.user).name(
			this.user.getTranslation("biomes.gui.admin.gui-title"));

		// Change Other players Biome
		panelBuilder.item(1, this.createButton(ButtonType.CHANGE_USER_BIOME));

		// Add New Biome
		panelBuilder.item(3, this.createButton(ButtonType.ADD_BIOME));

		// Edit Biome
		panelBuilder.item(4, this.createButton(ButtonType.EDIT_BIOME));

		// Remove Biome
		panelBuilder.item(5, this.createButton(ButtonType.DELETE_BIOME));

		// Import Biomes
		panelBuilder.item(7, this.createButton(ButtonType.IMPORT_BIOMES));

		// Edit Addon Settings
		panelBuilder.item(8, this.createButton(ButtonType.EDIT_SETTINGS));

		panelBuilder.build();
	}


	/**
	 * This method returns button for admin panel of given type.
	 * @param buttonType Type of button.
	 * @return new panel button with requested type.
	 */
	private PanelItem createButton(ButtonType buttonType)
	{
		ItemStack icon;
		String name;
		List<String> description;
		boolean glow;
		PanelItem.ClickHandler clickHandler;

		String permissionSuffix;

		switch (buttonType)
		{
			case CHANGE_USER_BIOME:
			{
				permissionSuffix = SET;

				name = this.user.getTranslation("biomes.gui.admin.buttons.change");
				description = Collections.emptyList();
				icon = new ItemStack(Material.LEVER);
				clickHandler = (panel, user, clickType, slot) -> {
					new AdminUserListPanel(this.addon,
						this.world,
						this.user,
						this.topLabel,
						this.permissionPrefix,
						this).build();
					return true;
				};
				glow = false;

				break;
			}
			case ADD_BIOME:
			{
				permissionSuffix = ADD;

				name = this.user.getTranslation("biomes.gui.admin.buttons.add");
				description = Collections.emptyList();
				icon = new ItemStack(Material.BOOK);
				clickHandler = (panel, user, clickType, slot) -> {
					new AdminBiomeEditPanel(this.addon,
						this.world,
						this.user,
						null,
						this.topLabel,
						this.permissionPrefix,
						this).build();

					return true;
				};
				glow = false;

				break;
			}
			case EDIT_BIOME:
			{
				permissionSuffix = EDIT;

				name = this.user.getTranslation("biomes.gui.admin.buttons.edit");
				description = Collections.emptyList();
				icon = new ItemStack(Material.ANVIL);
				clickHandler = (panel, user, clickType, slot) -> {
					new AdminBiomeListPanel(this.addon,
						this.world,
						this.user,
						true,
						this.topLabel,
						this.permissionPrefix,
						this).build();
					return true;
				};
				glow = false;

				break;
			}
			case DELETE_BIOME:
			{
				permissionSuffix = DELETE;

				name = this.user.getTranslation("biomes.gui.admin.buttons.remove");
				description = Collections.emptyList();
				icon = new ItemStack(Material.LAVA_BUCKET);
				clickHandler = (panel, user, clickType, slot) -> {
					new AdminBiomeListPanel(this.addon,
						this.world,
						this.user,
						false,
						this.topLabel,
						this.permissionPrefix,
						this).build();
					return true;
				};
				glow = false;

				break;
			}
			case IMPORT_BIOMES:
			{
				permissionSuffix = IMPORT;

				name = this.user.getTranslation("biomes.gui.admin.buttons.import");
				description = Collections.emptyList();
				icon = new ItemStack(Material.HOPPER);
				clickHandler = (panel, user, clickType, slot) -> {
					if (clickType.isRightClick())
					{
						this.overwriteMode = !this.overwriteMode;
						this.build();
					}
					else
					{
						// Run import command.
						this.user.performCommand(this.topLabel + " " + BIOMES + " " + IMPORT +
							(this.overwriteMode ? " overwrite" : ""));
					}
					return true;
				};
				glow = this.overwriteMode;

				break;
			}
			case EDIT_SETTINGS:
			{
				permissionSuffix = SETTINGS;

				name = this.user.getTranslation("biomes.gui.admin.buttons.settings");
				description = Collections.emptyList();
				icon = new ItemStack(Material.CRAFTING_TABLE);
				clickHandler = (panel, user, clickType, slot) -> {
					new AdminSettingsPanel(this.addon,
						this.world,
						this.user,
						this.topLabel,
						this.permissionPrefix,
						this).build();
					return true;
				};
				glow = false;

				break;
			}
			default:
				// This should never happen.
				return null;
		}

		// If user does not have permission to run command, then change icon and clickHandler.
		final String actionPermission = this.permissionPrefix + ADMIN + "." + BIOMES + "." + permissionSuffix;

		if (!this.user.hasPermission(actionPermission))
		{
			icon = new ItemStack(Material.BARRIER);
			clickHandler = (panel, user, clickType, slot) -> {
				this.user.sendMessage("general.errors.no-permission", "[permission]", actionPermission);
				return true;
			};
		}

		return new PanelItem(icon, name, description, glow, clickHandler, false);
	}


// ---------------------------------------------------------------------
// Section: Enums
// ---------------------------------------------------------------------


	/**
	 * This enum shows which button should be created.
	 */
	private enum ButtonType
	{
		CHANGE_USER_BIOME,
		ADD_BIOME,
		EDIT_BIOME,
		DELETE_BIOME,
		IMPORT_BIOMES,
		EDIT_SETTINGS
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


	private boolean overwriteMode;
}
