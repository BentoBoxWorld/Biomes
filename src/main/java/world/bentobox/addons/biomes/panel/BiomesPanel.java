package world.bentobox.addons.biomes.panel;


import org.bukkit.Chunk;
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
		Mode mode)
	{
		this.addon = addon;
		this.biomesManager = addon.getAddonManager();
		this.player = player;
		this.targetUser = targetUser;
		this.world = world;
		this.permissionPrefix = permissionPrefix;
		this.mode = mode;

		switch (mode)
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

		this.nextText = this.player.getTranslation("biomes.gui-next");
		this.previousText = this.player.getTranslation("biomes.gui-prev");

		this.createBiomesPanel(0);
	}


	/**
	 * This method creates Biomes Panel with elements that should be in page by given index.
	 * @param pageIndex Page index.
	 */
	private void createBiomesPanel(int pageIndex)
	{
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
			panelBuilder.item(this.createItem(biomes.get(itemIndex)));
			itemIndex++;
		}

		final int panelNum = pageIndex;

		// Add Next / previous buttons.

		if (itemIndex < biomes.size())
		{
			// Next
			panelBuilder.item(PANEL_MAX_SIZE + 8,
				new PanelItemBuilder().name(this.nextText).icon(
					new ItemStack(Material.SIGN)).clickHandler(
						(panel, clicker, click, slot) -> {
							this.player.closeInventory();
							this.createBiomesPanel(panelNum + 1);
							return true;
						}).build());
		}

		if (itemIndex > PANEL_MAX_SIZE)
		{
			// Previous
			panelBuilder.item(PANEL_MAX_SIZE,
				new PanelItemBuilder().name(this.previousText).icon(
					new ItemStack(Material.SIGN)).clickHandler(
						(panel, clicker, click, slot) -> {
							this.player.closeInventory();
							this.createBiomesPanel(panelNum - 1);
							return true;
						}).build());
		}

		panelBuilder.build();
	}


	/**
	 * This method creates new icon for each biome objecct.
	 * @param biome BiomeObject that must be added to panel.
	 */
	private PanelItem createItem(BiomesObject biome)
	{
		PanelItemBuilder itemBuilder =
			new PanelItemBuilder().icon(biome.getIcon()).name(
				biome.getFriendlyName().isEmpty() ? biome.getUniqueId() : biome.getFriendlyName()).
				description(biome.getDescription()).glow(false);

		if (this.mode.equals(Mode.ADMIN))
		{
			// TODO: need to implement.
		}
		else if (this.mode.equals(Mode.EDIT))
		{
			// TODO: need to implement.
		}
		else
		{
			// Player click
			itemBuilder.clickHandler((panel, player, click, slot) -> {
				if (this.canChangeBiome())
				{
					this.updateToNewBiome(player, biome, UpdateMode.COMPLETE);
				}

				return true;
			});
		}

		return itemBuilder.build();
	}


	/**
	 * This method checks if user can change biome in desired place.
	 * @return true, if biome changing is possible.
	 */
	private boolean canChangeBiome()
	{
		// TODO: implement necessary!!

		return true;
	}


	/**
	 * This method changes biome on island.
	 * @param player User that changes biome.
	 * @param biome New Biome that must be set.
	 * @param mode UpdateMode how new biome location is calculated.
	 */
	private void updateToNewBiome(User player, BiomesObject biome, UpdateMode mode)
	{
		int minX;
		int minZ;
		int maxX;
		int maxZ;

		// Calculate minimal and maximal coordinate based on update mode.

		switch (mode)
		{
			case COMPLETE:
				Island island = this.addon.getIslands().getIsland(this.world, player);

				int range = island.getRange();

				minX = island.getMinX();
				minZ = island.getMinZ();

				maxX = minX + 2 * range;
				maxZ = minZ + 2 * range;

				break;
			case CHUNK:
				Chunk chunk = player.getLocation().getChunk();

				minX = chunk.getX();
				minZ = chunk.getZ();

				maxX = minX + 16;
				maxZ = minZ + 16;

				break;
			case DIAMETER:

				// TODO: pass update diameter till here!

				int halfDiameter = 10;

				minX = player.getLocation().getBlockX() - halfDiameter;
				minZ = player.getLocation().getBlockZ() - halfDiameter;

				maxX = player.getLocation().getBlockX() + halfDiameter;
				maxZ = player.getLocation().getBlockZ() + halfDiameter;

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
	private Mode mode;

	/**
	 * This variable holds NEXT button text.
	 */
	private final String nextText;

	/**
	 * This variable holds PREVIOUS button text.
	 */
	private final String previousText;

	/**
	 * This enum stores all possible panel creation modes.
	 */
	public enum Mode
	{
		ADMIN,
		EDIT,
		PLAYER
	}

	/**
	 * This enum stores all possible variants how to calculate new biome location.
	 */
	public enum UpdateMode
	{
		COMPLETE,
		CHUNK,
		DIAMETER
	}

	/**
	 * This variable stores maximal panel size.
	 */
	private static final int PANEL_MAX_SIZE = 18;
}
