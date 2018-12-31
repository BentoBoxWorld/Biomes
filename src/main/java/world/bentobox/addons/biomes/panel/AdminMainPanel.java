package world.bentobox.addons.biomes.panel;


import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

import world.bentobox.addons.biomes.BiomesAddon;
import world.bentobox.addons.biomes.objects.BiomesObject;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;


/**
 * This class creates AdminPanel GUI that allows to change user biome, edit, add or remove biome and import
 * biomes.
 */
public class AdminMainPanel
{
	public AdminMainPanel(BiomesAddon addon, World world, User user)
	{
		this.addon = addon;
		this.world = world;
		this.player = user;

		this.createAdminMainMenu(false);
	}


	/**
	 * This method creates main admin menu.
	 * @param glow indicate if Import must glow.
	 */
	private void createAdminMainMenu(boolean glow)
	{
		PanelBuilder panelBuilder = new PanelBuilder().user(this.player).name(
			this.player.getTranslation("biomes.gui.admin.gui-title"));

		// Change Other players Biome
		panelBuilder.item(1, new PanelItemBuilder().
			name(this.player.getTranslation("biomes.gui.admin.buttons.change")).
			icon(Material.LEVER).
			clickHandler((panel, user, clickType, slot) -> {
				new AdminUserPanel(this.addon,
					this.world,
					this.player);
				return true;
			}).build());

		// Add New Biome
		panelBuilder.item(3, new PanelItemBuilder().
			name(this.player.getTranslation("biomes.gui.admin.buttons.add")).
			icon(Material.BOOK).
			clickHandler((panel, clicker, click, slot) -> {
				this.player.closeInventory();

				new BiomesPanel(
					this.addon,
					this.player,
					new BiomesObject(),
					this.world,
					"",
					BiomesPanel.Mode.EDIT);
				return true;
			}).build());

		// Edit Biome
		panelBuilder.item(4, new PanelItemBuilder().
			name(this.player.getTranslation("biomes.gui.admin.buttons.edit")).
			icon(Material.ANVIL).
			clickHandler((panel, clicker, click, slot) -> {
				this.player.closeInventory();

				new BiomesPanel(
					this.addon,
					this.player,
					this.player,
					this.world,
					"",
					BiomesPanel.Mode.EDIT);
				return true;
			}).build());

		// Remove Biome
//		panelBuilder.item(5, new PanelItemBuilder().build());

		// Import Biomes
		panelBuilder.item(7, new PanelItemBuilder().
			name(this.player.getTranslation("biomes.gui.admin.buttons.import")).
			icon(Material.HOPPER).
			clickHandler((panel, user, clickType, slot) -> {
				if (clickType.isRightClick())
				{
					// Change to overwrite mode.
					this.createAdminMainMenu(!glow);
				}
				else
				{
					this.addon.getAddonManager().importBiomes(user, this.world, glow);
				}

				return true;
			}).glow(glow).
			build());

		// Edit Addon Settings
		panelBuilder.item(8, new PanelItemBuilder().
			name(this.player.getTranslation("biomes.gui.admin.buttons.settings")).
			icon(Material.ENCHANTING_TABLE).
			clickHandler((panel, user, clickType, slot) -> {
				new AdminSettingsPanel(this.addon,
					this.world,
					this.player);
				return true;
			}).build());

		panelBuilder.build();
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


	private User player;

	private World world;

	private BiomesAddon addon;

	/**
	 * Set only 5 rows to be for Users. Last row should be reserved for navigation.
	 */
	private static int MAX_PLAYERS_PER_PAGE = 45;
}
