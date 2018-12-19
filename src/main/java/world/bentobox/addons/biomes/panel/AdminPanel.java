package world.bentobox.addons.biomes.panel;


import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

import world.bentobox.addons.biomes.BiomesAddon;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;


/**
 * This class creates AdminPanel GUI that allows to change user biome, edit, add or remove biome and import
 * biomes.
 */
public class AdminPanel
{
	public AdminPanel(BiomesAddon addon, World world, User user)
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
			this.player.getTranslation("biomes.admin.gui-title"));

		// Change Other players Biome
		panelBuilder.item(1, new PanelItemBuilder().
			name(this.player.getTranslation("biomes.admin.buttons.change")).
			icon(Material.LEVER).
			clickHandler((panel, user, clickType, slot) -> {
				this.createPlayerListMenu(0);
				return true;
			}).build());

		// Add New Biome
//		panelBuilder.item(3, new PanelItemBuilder().build());

		// Edit Biome
		panelBuilder.item(4, new PanelItemBuilder().
			name(this.player.getTranslation("biomes.admin.buttons.edit")).
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
			name(this.player.getTranslation("biomes.admin.buttons.import")).
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
		// panelBuilder.item(8, new PanelItemBuilder().build());

		panelBuilder.build();
	}


	/**
	 * This method creates panel with all players which has islands. On player head click it will open
	 * BiomesPanel in ADMIN mode, that will allow to change selected user biome.
	 * @param pageIndex Index of page.
	 */
	private void createPlayerListMenu(int pageIndex)
	{
		PanelBuilder panelBuilder = new PanelBuilder().user(this.player).name(
			this.player.getTranslation("biomes.admin.gui-title"));

		List<User> activeUsers = new ArrayList<>(this.world.getPlayers().size());

		// Collect all players to create correct menu.
		int activePlayersCount = 0;

		for (Player player : this.world.getPlayers())
		{
			if (this.addon.getIslands().hasIsland(this.world, player.getUniqueId()))
			{
				activeUsers.add(this.addon.getPlayers().getUser(player.getUniqueId()));
				activePlayersCount++;
			}
		}

		// Normalize page INDEX.
		if (pageIndex < 0)
		{
			pageIndex = 0;
		}
		else if (pageIndex > (activePlayersCount / MAX_PLAYERS_PER_PAGE))
		{
			pageIndex = activePlayersCount / MAX_PLAYERS_PER_PAGE;
		}

		int playerIndex = MAX_PLAYERS_PER_PAGE * pageIndex;

		while (playerIndex < ((pageIndex + 1) * MAX_PLAYERS_PER_PAGE) &&
			playerIndex < activePlayersCount)
		{
			User user = activeUsers.get(playerIndex);

			panelBuilder.item(new PanelItemBuilder().
				name(user.getName()).
				icon(user.getName()).
				clickHandler((panel, clicker, click, slot) -> {
					this.player.closeInventory();

					new BiomesPanel(
						this.addon,
						this.player,
						user,
						this.world,
						"",
						BiomesPanel.Mode.ADMIN);
					return true;
				}).build()
			);

			playerIndex++;
		}

		// Next next button
		if (playerIndex < activeUsers.size())
		{
			final int nextPage = pageIndex + 1;

			panelBuilder.item(MAX_PLAYERS_PER_PAGE + 8, new PanelItemBuilder().
				name(this.player.getTranslation("biomes.gui.buttons.next")).
				icon(new ItemStack(Material.SIGN)).
				clickHandler((panel, clicker, click, slot) -> {
					this.createPlayerListMenu(nextPage);
					return true;
				}).build());
		}

		// Previous button
		if (playerIndex > MAX_PLAYERS_PER_PAGE)
		{
			final int previousPage = pageIndex - 1;

			panelBuilder.item(MAX_PLAYERS_PER_PAGE, new PanelItemBuilder().
				name(this.player.getTranslation("biomes.gui.buttons.previous")).
				icon(new ItemStack(Material.SIGN)).
				clickHandler((panel, clicker, click, slot) -> {
					this.createPlayerListMenu(previousPage);
					return true;
				}).build());
		}

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
