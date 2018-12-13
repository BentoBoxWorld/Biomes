package world.bentobox.addons.biomes.panel;


import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Biome;

import java.util.HashSet;
import java.util.Set;

import world.bentobox.addons.biomes.BiomesAddon;
import world.bentobox.addons.biomes.BiomesAddonManager;
import world.bentobox.addons.biomes.objects.BiomesObject;
import world.bentobox.addons.biomes.utils.Utils;
import world.bentobox.bentobox.api.panels.Panel;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;


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
		this.label = label;
		this.mode = mode;

		PanelBuilder panelBuilder = new PanelBuilder();

		switch (mode)
		{
			case ADMIN:
				panelBuilder.name(this.player.getTranslation("biomes.admin.gui-title"));
				break;
			case EDIT:
				panelBuilder.name(this.player.getTranslation("biomes.admin.edit-gui-title"));
				break;
			case PLAYER:
				panelBuilder.name(this.player.getTranslation("biomes.gui-title"));
				break;
			default:
				break;
		}

		this.addBiomesItems(panelBuilder);

		// Create the panel
		Panel panel = panelBuilder.build();
		panel.open(this.player);
	}


	/**
	 * This method adds biomes to PanelBuilder
	 * @param panelBuilder PanelBuilder that contains all menus.
	 */
	private void addBiomesItems(PanelBuilder panelBuilder)
	{
		for (BiomesObject biome : this.addon.getAddonManager().getBiomes())
		{
			this.createItem(panelBuilder, biome);
		}
	}


	/**
	 * This method creates new icon for each biome objecct.
	 * @param panelBuilder PanelBuilder in which icon must be added.
	 * @param biome BiomeObject that must be added to panel.
	 */
	private void createItem(PanelBuilder panelBuilder, BiomesObject biome)
	{
		PanelItemBuilder itemBuilder =
			new PanelItemBuilder().icon(biome.getIcon()).name(
				biome.getFriendlyName().isEmpty() ? biome.getUniqueId() : biome.getFriendlyName()).
				description(biome.getDescription()).glow(false);

		if (this.mode.equals(Mode.ADMIN))
		{
			// TODO: need to implement.

			return;
		}
		else if (this.mode.equals(Mode.EDIT))
		{
			// TODO: need to implement.

			return;
		}
		else
		{
			// Player click
			itemBuilder.clickHandler((panel, player, c, s) -> {
				if (this.canChangeBiome())
				{
					this.updateToNewBiome(player, biome);
				}

				return true;
			});
		}

		// If the biome has a specific slot allocated, use it
		if (biome.getSlot() >= 0)
		{
			panelBuilder.item(biome.getSlot(), itemBuilder.build());
		}
		else
		{
			panelBuilder.item(itemBuilder.build());
		}
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
	 */
	private void updateToNewBiome(User player, BiomesObject biome)
	{
		Biome newBiome = Utils.parseBiome(biome);

		int radius = 8;

		int minX = player.getLocation().getBlockX() - radius;
		int minZ = player.getLocation().getBlockZ() - radius;

		int maxX = player.getLocation().getBlockX() + radius;
		int maxZ = player.getLocation().getBlockZ() + radius;

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
	private String label;

	/**
	 * This variable stores current panel working mode.
	 */
	private Mode mode;

	/**
	 * This enum stores all possible panel creation modes.
	 */
	public enum Mode
	{
		ADMIN,
		EDIT,
		PLAYER
	}
}
