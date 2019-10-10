package world.bentobox.biomes.panels.admin;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Players;
import world.bentobox.biomes.BiomesAddon;
import world.bentobox.biomes.panels.CommonGUI;
import world.bentobox.biomes.panels.GuiUtils;
import world.bentobox.biomes.panels.user.BiomesChooseGUI;


/**
 * This class contains methods that allows to select specific user.
 */
public class ListUsersGUI extends CommonGUI
{
// ---------------------------------------------------------------------
// Section: Constructors
// ---------------------------------------------------------------------


	/**
	 * {@inheritDoc}
	 */
	public ListUsersGUI(CommonGUI parentPanel)
	{
		super(parentPanel);
		this.onlineUsers = this.collectUsers(ViewMode.IN_WORLD);
	}


	public ListUsersGUI(BiomesAddon addon,
		World world,
		User user,
		String topLabel,
		String permissionPrefix)
	{
		super(addon, world, user, topLabel, permissionPrefix);
		this.onlineUsers = this.collectUsers(ViewMode.IN_WORLD);
	}


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


	@Override
	public void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder().user(this.user).name(
			this.user.getTranslation("biomes.gui.title.admin.user-list"));

		GuiUtils.fillBorder(panelBuilder);

		final int MAX_ELEMENTS = 21;

		if (this.pageIndex < 0)
		{
			this.pageIndex = this.onlineUsers.size() / MAX_ELEMENTS;
		}
		else if (this.pageIndex > (this.onlineUsers.size() / MAX_ELEMENTS))
		{
			this.pageIndex = 0;
		}

		int playerIndex = MAX_ELEMENTS * this.pageIndex;

		// I want first row to be only for navigation and return button.
		int index = 10;

		while (playerIndex < ((this.pageIndex + 1) * MAX_ELEMENTS) &&
			playerIndex < this.onlineUsers.size() &&
			index < 36)
		{
			if (!panelBuilder.slotOccupied(index))
			{
				panelBuilder.item(index, this.createPlayerIcon(this.onlineUsers.get(playerIndex++)));
			}

			index++;
		}

		// Add button that allows to toggle different player lists.
		panelBuilder.item( 4, this.createToggleButton());

		// Navigation buttons only if necessary
		if (this.onlineUsers.size() > MAX_ELEMENTS)
		{
			panelBuilder.item(18, this.getButton(CommonButtons.PREVIOUS));
			panelBuilder.item(26, this.getButton(CommonButtons.NEXT));
		}

		panelBuilder.item(44, this.returnButton);


		panelBuilder.build();
	}


	/**
	 * This method creates button for given user. If user has island it will add valid click handler.
	 * @param player Player which button must be created.
	 * @return Player button.
	 */
	private PanelItem createPlayerIcon(Player player)
	{
		int lineLength = this.addon.getSettings().getLoreLineLength();

		if (this.addon.getIslands().getIsland(this.world, player.getUniqueId()) != null)
		{
			return new PanelItemBuilder().name(player.getName()).icon(player.getName()).clickHandler(
				(panel, user1, clickType, slot) -> {
					new BiomesChooseGUI(this, User.getInstance(player)).build();

					return true;
				}).build();
		}
		else
		{
			return new PanelItemBuilder().
				name(player.getName()).
				icon(Material.BARRIER).
				description(GuiUtils.stringSplit(this.user.getTranslation("general.errors.player-has-no-island"), lineLength)).
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
		else if (mode.equals(ViewMode.WITH_ISLAND))
		{
			return this.addon.getPlayers().getPlayers().stream().
				filter(player -> this.addon.getIslands().getIsland(this.world, player.getPlayerUUID()) != null).
				map(Players::getPlayer).
				collect(Collectors.toList());
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
		List<String> description = new ArrayList<>(ViewMode.values().length + 1);
		description.add(this.user.getTranslation("biomes.gui.descriptions.admin.toggle-user-list"));
		description.add((ViewMode.ONLINE == ViewMode.values()[this.modeIndex] ? "&2" : "&c") +
			this.user.getTranslation("biomes.gui.descriptions.view-mode.online"));
		description.add((ViewMode.WITH_ISLAND == ViewMode.values()[this.modeIndex] ? "&2" : "&c") +
			this.user.getTranslation("biomes.gui.descriptions.view-mode.in-world"));
		description.add((ViewMode.IN_WORLD == ViewMode.values()[this.modeIndex] ? "&2" : "&c") +
			this.user.getTranslation("biomes.gui.descriptions.view-mode.with-island"));

		return new PanelItemBuilder().
			name(this.user.getTranslation("biomes.gui.buttons.admin.toggle-user-list")).
			description(GuiUtils.stringSplit(description, this.addon.getSettings().getLoreLineLength())).
			icon(Material.STONE_BUTTON).
			clickHandler(
				(panel, user1, clickType, slot) -> {
					if (clickType.isRightClick())
					{
						this.modeIndex--;

						if (this.modeIndex < 0)
						{
							this.modeIndex = ViewMode.values().length - 1;
						}
					}
					else
					{
						this.modeIndex++;

						if (this.modeIndex >= ViewMode.values().length)
						{
							this.modeIndex = 0;
						}
					}

					this.onlineUsers = this.collectUsers(ViewMode.values()[this.modeIndex]);
					this.pageIndex = 0;
					this.build();
					return true;
				}).build();
	}

// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

	/**
	 * List with players that should be in GUI.
	 */
	private List<Player> onlineUsers;

	/**
	 * Current index of view mode
	 */
	private int modeIndex = 2;

	/**
	 * This allows to switch which users should be in the list.
	 */
	private enum ViewMode
	{
		ONLINE,
		WITH_ISLAND,
		IN_WORLD
	}
}