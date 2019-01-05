package world.bentobox.addons.biomes.panel.admin;


import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import world.bentobox.addons.biomes.BiomesAddon;
import world.bentobox.addons.biomes.panel.CommonPanel;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;


/**
 * This method creates panel that contains users. It will be used to allow select user which island
 * will change biome.
 */
public class AdminUserListPanel extends CommonPanel
{
	public AdminUserListPanel(BiomesAddon addon,
		World world,
		User user, String topLabel, String permissionPrefix)
	{
		this(addon, world, user, topLabel, permissionPrefix, null);
	}


	public AdminUserListPanel(BiomesAddon addon,
		World world,
		User user,
		String topLabel,
		String permissionPrefix, CommonPanel parentPanel)
	{
		super(addon, world, user, topLabel, permissionPrefix, parentPanel);

		this.returnButton = this.parentPanel == null ? null : this.createCommonButton(CommonButtons.RETURN);

		// Create list with users to avoid issues when user logs out in a process and mess element count.
		this.onlineUsers = this.collectUsers(ViewMode.IN_WORLD);
	}


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


	@Override
	public void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder().user(this.user).name(
			this.user.getTranslation("biomes.gui.admin.choose-user-title"));

		int MAX_ELEMENTS = 45;
		if (this.pageIndex < 0)
		{
			this.pageIndex = 0;
		}
		else if (this.pageIndex > (this.onlineUsers.size() / MAX_ELEMENTS))
		{
			this.pageIndex = this.onlineUsers.size() / MAX_ELEMENTS;
		}

		int playerIndex = MAX_ELEMENTS * this.pageIndex;

		while (playerIndex < ((this.pageIndex + 1) * MAX_ELEMENTS) &&
			playerIndex < this.onlineUsers.size())
		{
			panelBuilder.item(this.createPlayerIcon(this.onlineUsers.get(playerIndex))).build();
			playerIndex++;
		}

		int nextIndex = playerIndex + 9 - (playerIndex % 9);

		if (playerIndex > MAX_ELEMENTS)
		{
			panelBuilder.item(nextIndex, this.createCommonButton(CommonButtons.PREVIOUS));
		}

		if (playerIndex < this.onlineUsers.size())
		{
			panelBuilder.item(nextIndex + 8, this.createCommonButton(CommonButtons.NEXT));
		}

		if (this.returnButton != null)
		{
			panelBuilder.item(nextIndex + 6, this.returnButton);
		}

		panelBuilder.item(nextIndex + 3, this.createToggleButton());

		panelBuilder.build();
	}


	/**
	 * This method creates button for given user. If user has island it will add valid click handler.
	 * @param player Player which button must be created.
	 * @return Player button.
	 */
	private PanelItem createPlayerIcon(Player player)
	{
		if (this.addon.getIslands().hasIsland(this.world, player.getUniqueId()))
		{
			return new PanelItemBuilder().name(player.getName()).icon(player.getName()).clickHandler(
				(panel, user1, clickType, slot) -> {
					// TODO: Create Biome Chooose Panel
					// Open Biome Choose panel.
					return true;
				}).build();
		}
		else
		{
			return new PanelItemBuilder().
				name(player.getName()).
				icon(Material.BARRIER).
				description(this.user.getTranslation("general.errors.player-has-no-island")).
				clickHandler((panel, user1, clickType, slot) -> false).
				build();
		}
	}


	/**
	 * This method collects users based on view mode.
	 * @param mode Given view mode.
	 * @return List with players in necessary view mode.
	 */
	private List<Player> collectUsers(ViewMode mode)
	{
		if (mode.equals(ViewMode.ONLINE))
		{
			return new ArrayList<>(Bukkit.getOnlinePlayers());
		}
		else if (mode.equals(ViewMode.OFFLINE))
		{
			List<Player> offlinePlayer = new ArrayList<>(Bukkit.getOfflinePlayers().length);

			for (int index = 0; index < Bukkit.getOfflinePlayers().length; index++)
			{
				OfflinePlayer player = Bukkit.getOfflinePlayers()[index];
				offlinePlayer.add(player.getPlayer());
			}

			return offlinePlayer;
		}
		else
		{
			return  new ArrayList<>(this.world.getPlayers());
		}
	}


	/**
	 * This method creates Player List view Mode toggle button.
	 * @return Button that toggles through player view mode.
	 */
	private PanelItem createToggleButton()
	{
		List<String> values = new ArrayList<>(ViewMode.values().length);

		for (int i = 0; i < ViewMode.values().length; i++)
		{
			values.add((this.modeIndex == i ? "§2" : "§c") +
				this.user.getTranslation("biomes.gui.admin.descriptions." +
					ViewMode.values()[i].name().toLowerCase()));
		}

		return new PanelItemBuilder().
			name(this.user.getTranslation("biomes.gui.admin.buttons.toggle-users",
				"[value]",
				this.user.getTranslation("biomes.gui.admin.descriptions." + ViewMode.values()[this.modeIndex].name().toLowerCase()))).
			description(values).
			icon(Material.STONE_BUTTON).
			clickHandler(
				(panel, user1, clickType, slot) -> {
					this.modeIndex++;

					if (this.modeIndex >= ViewMode.values().length)
					{
						this.modeIndex = 0;
					}

					this.onlineUsers = this.collectUsers(ViewMode.values()[this.modeIndex]);
					this.pageIndex = 0;
					this.build();
					return true;
			}).build();
	}


// ---------------------------------------------------------------------
// Section: Private enums
// ---------------------------------------------------------------------


	private enum ViewMode
	{
		ONLINE,
		OFFLINE,
		IN_WORLD
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


	private PanelItem returnButton;

	private List<Player> onlineUsers;

	private int modeIndex = 2;
}
