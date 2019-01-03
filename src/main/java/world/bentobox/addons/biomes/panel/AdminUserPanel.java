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
 * This Admin panel will show list with users and it allows to select user, whoes biome admin want to change.
 * @deprecated Biomes Panel is Deprecated. Panels will be recreated by using CommonPanel.
 */
@Deprecated
public class AdminUserPanel
{
	/**
	 * Default constructor that will create new Panel with players.
	 * @param addon BiomeAddon.
 	 * @param world World form which command is called.
	 * @param user User who calls command.
	 */
	public AdminUserPanel(BiomesAddon addon, World world, User user)
	{
		this(addon, world, user, true);
	}


	/**
	 * Default constructor that will create new Panel with players.
	 * @param addon BiomeAddon.
	 * @param world World form which command is called.
	 * @param user User who calls command.
	 * @param fromMainMenu indicate if panel is created from main menu.
	 */
	public AdminUserPanel(BiomesAddon addon, World world, User user, boolean fromMainMenu)
	{
		this.addon = addon;
		this.world = world;
		this.player = user;
		this.fromMainMenu = fromMainMenu;

		this.createPlayerListMenu(0);
	}


	/**
	 * This method creates panel with all players which has islands. On player head click it will open
	 * BiomesPanel in ADMIN mode, that will allow to change selected user biome.
	 * @param pageIndex Index of page.
	 */
	private void createPlayerListMenu(int pageIndex)
	{
		PanelBuilder panelBuilder = new PanelBuilder().user(this.player).name(
			this.player.getTranslation("biomes.gui.admin.choose-user-title"));

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

		if (this.fromMainMenu)
		{
			// Return button
			panelBuilder.item(MAX_PLAYERS_PER_PAGE + 4, new PanelItemBuilder().
				name(this.player.getTranslation("biomes.gui.buttons.back")).
				icon(new ItemStack(Material.OAK_DOOR)).
				clickHandler((panel, clicker, click, slot) -> {
					new AdminMainPanel(this.addon, this.world, this.player);
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
	 * This variable stores if current menu should show return button.
	 */
	private boolean fromMainMenu;

	/**
	 * Set only 5 rows to be for Users. Last row should be reserved for navigation.
	 */
	private static int MAX_PLAYERS_PER_PAGE = 45;
}
