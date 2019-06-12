package world.bentobox.biomes.panel.admin;


import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

import net.wesjd.anvilgui.AnvilGUI;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;
import world.bentobox.biomes.BiomesAddon;
import world.bentobox.biomes.panel.CommonGUI;
import world.bentobox.biomes.utils.GuiUtils;


/**
 * This is main Admin GUI that is opened with admin command.
 */
public class AdminGUI extends CommonGUI
{
	/**
	 * {@inheritDoc}
	 */
	public AdminGUI(BiomesAddon addon,
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
			this.user.getTranslation("biomes.gui.title.admin.main-gui"));

		GuiUtils.fillBorder(panelBuilder, Material.ORANGE_STAINED_GLASS_PANE);

		// Change Other players Biome
		panelBuilder.item(20, this.createButton(Button.CHANGE_USER_BIOME));

		// Add New Biome
		panelBuilder.item(13, this.createButton(Button.ADD_BIOME));
		// Edit Biome
		panelBuilder.item(22, this.createButton(Button.EDIT_BIOME));
		// Remove Biome
		panelBuilder.item(31, this.createButton(Button.DELETE_BIOME));

		// Import Biomes
		panelBuilder.item(24, this.createButton(Button.IMPORT_BIOMES));
		// Edit Addon Settings
		panelBuilder.item(25, this.createButton(Button.EDIT_SETTINGS));

		panelBuilder.build();
	}


	/**
	 * This method returns button for admin panel of given type.
	 * @param button Type of button.
	 * @return new panel button with requested type.
	 */
	private PanelItem createButton(Button button)
	{
		ItemStack icon;
		String name;
		List<String> description;
		boolean glow;
		PanelItem.ClickHandler clickHandler;

		String permissionSuffix;

		switch (button)
		{
			case CHANGE_USER_BIOME:
			{
				permissionSuffix = SET;

				name = this.user.getTranslation("biomes.gui.buttons.admin.change");
				description = Collections.singletonList(this.user.getTranslation("biomes.gui.descriptions.admin.change"));
				icon = new ItemStack(Material.LEVER);
				clickHandler = (panel, user, clickType, slot) -> {
					new ListUsersGUI(this).build();
					return true;
				};
				glow = false;

				break;
			}
			case ADD_BIOME:
			{
				permissionSuffix = ADD;

				name = this.user.getTranslation("biomes.gui.buttons.admin.add");
				description = Collections.singletonList(this.user.getTranslation("biomes.gui.descriptions.admin.add"));
				icon = new ItemStack(Material.BOOK);
				clickHandler = (panel, user, clickType, slot) -> {
					new AnvilGUI(this.addon.getPlugin(),
						this.user.getPlayer(),
						"unique_id",
						(player, reply) -> {
							String newName = Util.getWorld(this.world).getName() + "-" + reply.toLowerCase();

							if (!this.addon.getAddonManager().containsBiome(newName))
							{
								new EditBiomeGUI(AdminGUI.this, this.addon.getAddonManager().createBiome(newName)).build();
							}
							else
							{
								this.user.sendMessage("biomes.errors.unique-id", "[id]", reply);
							}

							return reply;
						});

					return true;
				};
				glow = false;

				break;
			}
			case EDIT_BIOME:
			{
				permissionSuffix = EDIT;

				name = this.user.getTranslation("biomes.gui.buttons.admin.edit");
				description = Collections.singletonList(this.user.getTranslation("biomes.gui.descriptions.admin.edit"));
				icon = new ItemStack(Material.ANVIL);
				clickHandler = (panel, user, clickType, slot) -> {
					new ListBiomesGUI(this, true).build();
					return true;
				};
				glow = false;

				break;
			}
			case DELETE_BIOME:
			{
				permissionSuffix = DELETE;

				name = this.user.getTranslation("biomes.gui.buttons.admin.remove");
				description = Collections.singletonList(this.user.getTranslation("biomes.gui.descriptions.admin.remove"));
				icon = new ItemStack(Material.LAVA_BUCKET);
				clickHandler = (panel, user, clickType, slot) -> {
					new ListBiomesGUI(this, false).build();
					return true;
				};
				glow = false;

				break;
			}
			case IMPORT_BIOMES:
			{
				permissionSuffix = IMPORT;

				name = this.user.getTranslation("biomes.gui.buttons.admin.import");
				description = Collections.singletonList(this.user.getTranslation("biomes.gui.descriptions.admin.import"));
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

				name = this.user.getTranslation("biomes.gui.buttons.admin.settings");
				description = Collections.singletonList(this.user.getTranslation("biomes.gui.descriptions.admin.settings"));
				icon = new ItemStack(Material.CRAFTING_TABLE);
				clickHandler = (panel, user, clickType, slot) -> {
					new EditSettingsGUI(this).build();
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

		return new PanelItemBuilder().
			icon(icon).
			name(name).
			description(GuiUtils.stringSplit(description, this.addon.getSettings().getLoreLineLength())).
			glow(glow).
			clickHandler(clickHandler).
			build();
	}


// ---------------------------------------------------------------------
// Section: Enums
// ---------------------------------------------------------------------


	/**
	 * This enum shows which button should be created.
	 */
	private enum Button
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

	/**
	 * Overwrite mode for import manager.
	 */
	private boolean overwriteMode;
}
