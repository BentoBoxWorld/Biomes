package world.bentobox.addons.biomes.panel;


import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import world.bentobox.addons.biomes.BiomesAddon;
import world.bentobox.addons.biomes.BiomesAddonManager;
import world.bentobox.addons.biomes.objects.BiomesObject;
import world.bentobox.addons.biomes.utils.Utils;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;


/**
 * This class implements Biomes Panel for all users.
 */
public class BiomesPanel
{
	public BiomesPanel(BiomesAddon addon,
		User player,
		User targetUser,
		String level,
		World world,
		String permissionPrefix,
		String label,
		Mode workingMode)
	{
		this.addon = addon;
		this.biomesManager = addon.getAddonManager();
		this.player = player;
		this.targetUser = targetUser;
		this.world = world;
		this.permissionPrefix = permissionPrefix;
		this.workingMode = workingMode;

		switch (workingMode)
		{
			case ADMIN:
				this.panelTitle = this.player.getTranslation("biomes.admin.gui-title");
				break;
			case EDIT:
				this.panelTitle = this.player.getTranslation("biomes.admin.edit-gui-title");
				break;
			case PLAYER:
				this.panelTitle = this.player.getTranslation("biomes.gui-title");
				break;
			default:
				this.panelTitle = "";
				break;
		}

		this.updateMode = this.parseDefaultUpdateType();
		this.updateNumber = this.addon.getConfig().getInt("defaultsize", 3);

		this.createBiomesPanel(0);
	}


// ---------------------------------------------------------------------
// Section: Panel creating methods
// ---------------------------------------------------------------------


	/**
	 * This method creates Biomes Panel with elements that should be in page by given index.
	 * @param pageIndex Page index.
	 */
	private void createBiomesPanel(int pageIndex)
	{
		// normalize updatenumber
		if (this.updateNumber > 200)
		{
			this.updateNumber = 200;
		}
		else if (this.updateNumber < 0)
		{
			this.updateNumber = 0;
		}

		List<BiomesObject> biomes = this.biomesManager.getBiomes();

		final int biomeCount = biomes.size();

		if (pageIndex < 0)
		{
			pageIndex = 0;
		}
		else if (pageIndex > (biomeCount / PANEL_MAX_SIZE))
		{
			pageIndex = biomeCount / PANEL_MAX_SIZE;
		}

		// Add page index only when necessary.
		String indexString = biomeCount > PANEL_MAX_SIZE ? " " + String.valueOf(pageIndex + 1) : "";

		PanelBuilder panelBuilder = new PanelBuilder().user(this.player).name(this.panelTitle + indexString);

		int itemIndex = pageIndex * PANEL_MAX_SIZE;

		while (itemIndex < (pageIndex * PANEL_MAX_SIZE + PANEL_MAX_SIZE) &&
			itemIndex < biomes.size())
		{
			panelBuilder.item(this.createBiomeButton(biomes.get(itemIndex)));
			itemIndex++;
		}

		if (this.addon.getConfig().getBoolean("advancedmenu", false))
		{
			// Create advanced menu.

			// Add Type Buttons
			panelBuilder.item(PANEL_MAX_SIZE + 2,
				this.createAdvancedButton(AdvancedButtons.ISLAND, pageIndex));
			panelBuilder.item(PANEL_MAX_SIZE + 9 + 2,
				this.createAdvancedButton(AdvancedButtons.CHUNK, pageIndex));
			panelBuilder.item(PANEL_MAX_SIZE + 18 + 2,
				this.createAdvancedButton(AdvancedButtons.SQUARE, pageIndex));

			// Add counter
			panelBuilder.item(PANEL_MAX_SIZE + 9 + 3,
				this.createAdvancedButton(AdvancedButtons.COUNTER, pageIndex));

			// Add Setters
			panelBuilder.item(PANEL_MAX_SIZE + 4,
				this.createAdvancedButton(AdvancedButtons.ONE, pageIndex));
			panelBuilder.item(PANEL_MAX_SIZE + 9 + 4,
				this.createAdvancedButton(AdvancedButtons.FIVE, pageIndex));
			panelBuilder.item(PANEL_MAX_SIZE + 18 + 4,
				this.createAdvancedButton(AdvancedButtons.TEN, pageIndex));

			// Add increments
			panelBuilder.item(PANEL_MAX_SIZE + 5,
				this.createAdvancedButton(AdvancedButtons.PLUS_ONE, pageIndex));
			panelBuilder.item(PANEL_MAX_SIZE + 9 + 5,
				this.createAdvancedButton(AdvancedButtons.PLUS_THREE, pageIndex));
			panelBuilder.item(PANEL_MAX_SIZE + 18 + 5,
				this.createAdvancedButton(AdvancedButtons.PLUS_FIVE, pageIndex));

			// Add decrements
			panelBuilder.item(PANEL_MAX_SIZE + 6,
				this.createAdvancedButton(AdvancedButtons.MINUS_ONE, pageIndex));
			panelBuilder.item(PANEL_MAX_SIZE + 9 + 6,
				this.createAdvancedButton(AdvancedButtons.MINUS_THREE, pageIndex));
			panelBuilder.item(PANEL_MAX_SIZE + 18 + 6,
				this.createAdvancedButton(AdvancedButtons.MINUS_FIVE, pageIndex));

			if (itemIndex < biomes.size())
			{
				// Next
				panelBuilder.item(PANEL_MAX_SIZE + 8 + 9,
					this.createAdvancedButton(AdvancedButtons.NEXT, pageIndex + 1));
			}

			if (itemIndex > PANEL_MAX_SIZE)
			{
				// Previous
				panelBuilder.item(PANEL_MAX_SIZE + 9,
					this.createAdvancedButton(AdvancedButtons.PREVIOUS, pageIndex - 1));
			}
		}
		else
		{
			// Add next and previous buttons in new line.

			if (itemIndex < biomes.size())
			{
				// Next
				panelBuilder.item(PANEL_MAX_SIZE + 8,
					this.createAdvancedButton(AdvancedButtons.NEXT, pageIndex + 1));
			}

			if (itemIndex > PANEL_MAX_SIZE)
			{
				// Previous
				panelBuilder.item(PANEL_MAX_SIZE,
					this.createAdvancedButton(AdvancedButtons.PREVIOUS, pageIndex - 1));
			}
		}

		panelBuilder.build();
	}


	/**
	 * This method creates menu buttons for biome changing menu.
	 * @param button Button type that must be created.
	 * @param pageIndex Page index.
	 * @return new panel item button.
	 */
	private PanelItem createAdvancedButton(AdvancedButtons button, int pageIndex)
	{
		PanelItem item;

		switch (button)
		{
			case ISLAND:
				item = new PanelItemBuilder().
					name(this.player.getTranslation("biomes.gui.buttons.island.name")).
					description(this.player.getTranslation("biomes.gui.buttons.island.description")).
					icon(new ItemStack(Material.GRASS_BLOCK)).
					clickHandler((panel, clicker, click, slot) -> {
						this.updateMode = UpdateMode.ISLAND;
						this.createBiomesPanel(pageIndex);
						return true;
					}).glow(this.updateMode == UpdateMode.ISLAND).
					build();
				break;
			case CHUNK:
				item = new PanelItemBuilder().
					name(this.player.getTranslation("biomes.gui.buttons.chunk.name")).
					description(this.player.getTranslation("biomes.gui.buttons.chunk.description")).
					icon(new ItemStack(Material.DIRT)).
					clickHandler((panel, clicker, click, slot) -> {
						this.updateMode = UpdateMode.CHUNK;
						this.createBiomesPanel(pageIndex);
						return true;
					}).glow(this.updateMode == UpdateMode.CHUNK).
					build();
				break;
			case SQUARE:
				item = new PanelItemBuilder().
					name(this.player.getTranslation("biomes.gui.buttons.region.name")).
					description(this.player.getTranslation("biomes.gui.buttons.region.description")).
					icon(new ItemStack(Material.GLASS)).
					clickHandler((panel, clicker, click, slot) -> {
						this.updateMode = UpdateMode.SQUARE;
						this.createBiomesPanel(pageIndex);
						return true;
					}).glow(this.updateMode == UpdateMode.SQUARE).
					build();
				break;

			case COUNTER:
				item = new PanelItemBuilder().
					name(this.player.getTranslation("biomes.gui.buttons.counter.name") + " " + this.updateNumber).
					description(this.player.getTranslation("biomes.gui.buttons.counter.description")).
					icon(new ItemStack(Material.PAPER)).
					build();
				break;

			case ONE:
				item = new PanelItemBuilder().
					name(this.player.getTranslation("biomes.gui.buttons.setone.name")).
					description(this.player.getTranslation("biomes.gui.buttons.setone.description")).
					icon(new ItemStack(Material.BLUE_STAINED_GLASS)).
					clickHandler((panel, clicker, click, slot) -> {
						this.updateNumber = 1;
						this.createBiomesPanel(pageIndex);
						return true;
					}).build();
				break;
			case FIVE:
				item = new PanelItemBuilder().
					name(this.player.getTranslation("biomes.gui.buttons.setfive.name")).
					description(this.player.getTranslation("biomes.gui.buttons.setfive.description")).
					icon(new ItemStack(Material.BLUE_STAINED_GLASS)).
					clickHandler((panel, clicker, click, slot) -> {
						this.updateNumber = 5;
						this.createBiomesPanel(pageIndex);
						return true;
					}).build();
				break;
			case TEN:
				item = new PanelItemBuilder().
					name(this.player.getTranslation("biomes.gui.buttons.setten.name")).
					description(this.player.getTranslation("biomes.gui.buttons.setten.description")).
					icon(new ItemStack(Material.BLUE_STAINED_GLASS)).
					clickHandler((panel, clicker, click, slot) -> {
						this.updateNumber = 10;
						this.createBiomesPanel(pageIndex);
						return true;
					}).build();
				break;

			case PLUS_ONE:
				item = new PanelItemBuilder().
					name(this.player.getTranslation("biomes.gui.buttons.addone.name")).
					description(this.player.getTranslation("biomes.gui.buttons.addone.description")).
					icon(new ItemStack(Material.GREEN_STAINED_GLASS_PANE)).
					clickHandler((panel, clicker, click, slot) -> {
						this.updateNumber += 1;
						this.createBiomesPanel(pageIndex);
						return true;
					}).build();
				break;
			case PLUS_THREE:
				item = new PanelItemBuilder().
					name(this.player.getTranslation("biomes.gui.buttons.addthree.name")).
					description(this.player.getTranslation("biomes.gui.buttons.addthree.description")).
					icon(new ItemStack(Material.GREEN_STAINED_GLASS_PANE)).
					clickHandler((panel, clicker, click, slot) -> {
						this.updateNumber += 3;
						this.createBiomesPanel(pageIndex);
						return true;
					}).build();
				break;
			case PLUS_FIVE:
				item = new PanelItemBuilder().
					name(this.player.getTranslation("biomes.gui.buttons.addfive.name")).
					description(this.player.getTranslation("biomes.gui.buttons.addfive.description")).
					icon(new ItemStack(Material.GREEN_STAINED_GLASS_PANE)).
					clickHandler((panel, clicker, click, slot) -> {
						this.updateNumber += 5;
						this.createBiomesPanel(pageIndex);
						return true;
					}).build();
				break;

			case MINUS_ONE:
				item = new PanelItemBuilder().
					name(this.player.getTranslation("biomes.gui.buttons.minusone.name")).
					description(this.player.getTranslation("biomes.gui.buttons.minusone.description")).
					icon(new ItemStack(Material.RED_STAINED_GLASS_PANE)).
					clickHandler((panel, clicker, click, slot) -> {
						this.updateNumber -= 1;
						this.createBiomesPanel(pageIndex);
						return true;
					}).build();
				break;
			case MINUS_THREE:
				item = new PanelItemBuilder().
					name(this.player.getTranslation("biomes.gui.buttons.minusthree.name")).
					description(this.player.getTranslation("biomes.gui.buttons.minusthree.description")).
					icon(new ItemStack(Material.RED_STAINED_GLASS_PANE)).
					clickHandler((panel, clicker, click, slot) -> {
						this.updateNumber -= 3;
						this.createBiomesPanel(pageIndex);
						return true;
					}).build();
				break;
			case MINUS_FIVE:
				item = new PanelItemBuilder().
					name(this.player.getTranslation("biomes.gui.buttons.minusfive.name")).
					description(this.player.getTranslation("biomes.gui.buttons.minusfive.description")).
					icon(new ItemStack(Material.RED_STAINED_GLASS_PANE)).
					clickHandler((panel, clicker, click, slot) -> {
						this.updateNumber -= 5;
						this.createBiomesPanel(pageIndex);
						return true;
					}).build();
				break;
			case NEXT:
				item = new PanelItemBuilder().
					name(this.player.getTranslation("biomes.gui.buttons.next.name")).
					icon(new ItemStack(Material.SIGN)).
					clickHandler((panel, clicker, click, slot) -> {
						this.createBiomesPanel(pageIndex);
						return true;
					}).build();
				break;
			case PREVIOUS:
				item = new PanelItemBuilder().
					name(this.player.getTranslation("biomes.gui.buttons.previous.name")).
					icon(new ItemStack(Material.SIGN)).
					clickHandler((panel, clicker, click, slot) -> {
						this.createBiomesPanel(pageIndex);
						return true;
					}).build();
				break;
			default:
				item = new PanelItemBuilder().name("").build();
		}

		return item;
	}


	/**
	 * This method creates new icon for each biome objecct.
	 * @param biome BiomeObject that must be added to panel.
	 */
	private PanelItem createBiomeButton(BiomesObject biome)
	{
		PanelItemBuilder itemBuilder = new PanelItemBuilder().
			icon(biome.getIcon()).
			name(biome.getFriendlyName().isEmpty() ? biome.getUniqueId() : biome.getFriendlyName()).
			description(biome.getDescription());

		if (this.workingMode.equals(Mode.ADMIN))
		{
			// TODO: need to implement.
		}
		else if (this.workingMode.equals(Mode.EDIT))
		{
			// TODO: need to implement.
		}
		else
		{
			// Player click
			itemBuilder.clickHandler((panel, player, click, slot) -> {
				if (this.canChangeBiome())
				{
					// TODO: move to event??
					this.updateToNewBiome(biome);

					this.player.closeInventory();
				}

				return true;
			});
		}

		return itemBuilder.build();
	}


// ---------------------------------------------------------------------
// Section: Biome Changing related methods
// ---------------------------------------------------------------------


	/**
	 * This method checks if user can change biome in desired place.
	 * @return true, if biome changing is possible.
	 */
	private boolean canChangeBiome()
	{
		boolean canChange;

		if (this.player == this.targetUser)
		{
			if (!this.updateMode.equals(UpdateMode.ISLAND) && this.updateNumber <= 0)
			{
				// Cannot update negative numbers.

				canChange = false;
			}
			else
			{
				Island island =
					this.addon.getIslands().getIsland(this.world, this.targetUser);

				if (island == null)
				{
					// User has no island.

					canChange = false;
				}
				else
				{
					Optional<Island> onIsland =
						this.addon.getIslands().getIslandAt(this.player.getLocation());

					// Return true only if player is on his island.
					canChange = onIsland.isPresent() && onIsland.get() == island;
				}
			}
		}
		else
		{
			// TODO: implement necessary!!

			canChange = true;
		}

		return canChange;
	}


	/**
	 * This method changes biome on island.
	 * @param biome New Biome that must be set.
	 */
	private void updateToNewBiome(BiomesObject biome)
	{
		Island island = this.addon.getIslands().getIsland(this.world, this.targetUser);
		int range = island.getRange();

		int minX = island.getMinX();
		int minZ = island.getMinZ();

		int maxX = minX + 2 * range;
		int maxZ = minZ + 2 * range;

		Location playerLocation = this.player.getLocation();

		// Calculate minimal and maximal coordinate based on update mode.

		switch (this.updateMode)
		{
			case ISLAND:
				// Everything is already calculated.
				break;
			case CHUNK:
				// TODO: implement multiple chunk updating

				Chunk chunk = playerLocation.getChunk();

				minX = Math.max(minX, chunk.getX());
				minZ = Math.max(minZ, chunk.getZ());

				maxX = Math.min(maxX, minX + 16);
				maxZ = Math.min(maxZ, minZ + 16);

				break;
			case SQUARE:

				int halfDiameter = this.updateNumber / 2;

				minX = Math.max(minX, playerLocation.getBlockX() - halfDiameter);
				minZ = Math.max(minZ, playerLocation.getBlockZ() - halfDiameter);

				maxX = Math.min(maxX, playerLocation.getBlockX() - halfDiameter);
				maxZ = Math.min(maxZ, playerLocation.getBlockZ() - halfDiameter);

				break;
			default:
				// Setting all values to 0 will skip biome changing.

				minX = 0;
				minZ = 0;
				maxX = 0;
				maxZ = 0;
		}

		// Update world coordinates with new biomes.

		Biome newBiome = Utils.parseBiome(biome);

		Set<Chunk> changedChunks = new HashSet<>();

		for (int x = minX; x < maxX; x++)
		{
			for (int z = minZ; z < maxZ; z++)
			{
				this.world.setBiome(x, z, newBiome);

				changedChunks.add(this.world.getChunkAt(x, z));
			}
		}

		//TODO: need to check how to do it, if it is truly deprecated.
		changedChunks.forEach(chunk -> this.world.refreshChunk(chunk.getX(), chunk.getZ()));
	}


// ---------------------------------------------------------------------
// Section: Other methods
// ---------------------------------------------------------------------


	/**
	 * This method parse default update type from config file.
	 * @return Default Update mode.
	 */
	private UpdateMode parseDefaultUpdateType()
	{
		String type = this.addon.getConfig().getString("defaulttype", "").toUpperCase();

		if (type.equals("ISLAND"))
		{
			return UpdateMode.ISLAND;
		}
		else if (type.equals("CHUNK"))
		{
			return UpdateMode.CHUNK;
		}
		else if (type.equals("SQUARE"))
		{
			return UpdateMode.SQUARE;
		}
		else
		{
			return UpdateMode.ISLAND;
		}
	}


// ---------------------------------------------------------------------
// Section: Enums
// ---------------------------------------------------------------------


	/**
	 * This enum describes all possible panel creation modes.
	 */
	public enum Mode
	{
		ADMIN,
		EDIT,
		PLAYER
	}

	/**
	 * This enum describes all possible variants how to calculate new biome location.
	 */
	private enum UpdateMode
	{
		ISLAND,
		CHUNK,
		SQUARE
	}


	/**
	 * This enum describes all advanced buttons.
	 */
	private enum AdvancedButtons
	{
		ISLAND,
		CHUNK,
		SQUARE,
		COUNTER,
		ONE,
		FIVE,
		TEN,
		PLUS_ONE,
		PLUS_THREE,
		PLUS_FIVE,
		MINUS_ONE,
		MINUS_THREE,
		MINUS_FIVE,
		NEXT,
		PREVIOUS
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

	/**
	 * This variable stores current biomes addon.
	 */
	private BiomesAddon addon;

	/**
	 * This variable stores biomes addon manager.
	 */
	private BiomesAddonManager biomesManager;

	/**
	 * This variable stores user who calls panel creation.
	 */
	private User player;

	/**
	 * This variable stores user who is targeted by current panel.
	 */
	private User targetUser;

	/**
	 * This variable stores world in which necessary changes will be done.
	 */
	private World world;

	/**
	 * This variable stores user permissions.
	 */
	private String permissionPrefix;

	/**
	 * This variable stores current panel name.
	 */
	private String panelTitle;

	/**
	 * This variable stores current panel working mode.
	 */
	private Mode workingMode;

	/**
	 * This variable stores current panel update mode.
	 */
	private UpdateMode updateMode;

	/**
	 * This variable stores current update distance for SQUARE mode or chunk count for
	 * CHUNK mode.
	 */
	private int updateNumber;

	/**
	 * This variable stores maximal panel size.
	 */
	private static final int PANEL_MAX_SIZE = 18;
}
