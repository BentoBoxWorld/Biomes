package world.bentobox.biomes.panels.admin;


import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.World;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;
import world.bentobox.biomes.BiomesAddon;
import world.bentobox.biomes.database.objects.BiomesObject;
import world.bentobox.biomes.panels.*;
import world.bentobox.biomes.utils.Constants;
import world.bentobox.biomes.utils.Utils;


/**
 * This class manages Biome Object Listing Panel.
 */
public class ListBiomesPanel extends CommonPagedPanel<BiomesObject>
{
	/**
	 * Instantiates a new List biomes panel.
	 *
	 * @param parentPanel the parent panel
	 * @param editMode the edit mode
	 */
	private ListBiomesPanel(CommonPanel parentPanel, boolean editMode)
	{
		super(parentPanel);
		this.editMode = editMode;
	}


	/**
	 * Instantiates a new List biomes panel.
	 *
	 * @param addon the addon
	 * @param world the world
	 * @param user the user
	 * @param topLabel the top label
	 * @param permissionPrefix the permission prefix
	 * @param editMode the edit mode
	 */
	private ListBiomesPanel(BiomesAddon addon,
		World world,
		User user,
		String topLabel,
		String permissionPrefix,
		boolean editMode)
	{
		super(addon, user, world, topLabel, permissionPrefix);
		this.editMode = editMode;
	}


	/**
	 * This method opens new List biomes Panel based on parameters from ParentPanel.
	 *
	 * @param parentPanel the parent panel
	 * @param editMode the edit mode
	 */
	public static void open(CommonPanel parentPanel, boolean editMode)
	{
		new ListBiomesPanel(parentPanel, editMode).build();
	}


	/**
	 * This method opens new List biomes Panel based on given parameters.
	 *
	 * @param addon the addon
	 * @param world the world
	 * @param user the user
	 * @param topLabel the top label
	 * @param permissionPrefix the permission prefix
	 * @param editMode the edit mode
	 */
	public static void open(BiomesAddon addon,
		World world,
		User user,
		String topLabel,
		String permissionPrefix,
		boolean editMode)
	{
		new ListBiomesPanel(addon, world, user, topLabel, permissionPrefix, editMode).build();
	}


	/**
	 * Builds List Biomes Panel for the user.
	 */
	@Override
	protected void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder().user(this.user).name(
			this.user.getTranslation(Constants.TITLE + "choose-biome"));

		PanelUtils.fillBorder(panelBuilder,
			this.editMode ? Material.PURPLE_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE);

		List<BiomesObject> biomesList = this.addon.getAddonManager().getBiomes(this.world).
			stream().
			filter(biomesObject -> this.searchString.isBlank() ||
				biomesObject.getFriendlyName().toLowerCase().contains(this.searchString.toLowerCase()) ||
				biomesObject.getUniqueId().toLowerCase().contains(this.searchString.toLowerCase()) ||
				biomesObject.getBiome().name().toLowerCase().contains(this.searchString)).
			collect(Collectors.toList());

		this.populateElements(panelBuilder, biomesList);

		panelBuilder.item(44, this.returnButton);

		panelBuilder.build();
	}


	/**
	 * This method is called when filter value is updated.
	 */
	@Override
	protected void updateFilters()
	{
		// Do nothing here.
	}


	/**
	 * This method creates button for given biome.
	 * @param biome biome which button must be created.
	 * @return Biome button.
	 */
	@Override
	protected PanelItem createElementButton(BiomesObject biome)
	{
		PanelItemBuilder itemBuilder = new PanelItemBuilder().
			name(Util.translateColorCodes(biome.getFriendlyName())).
			description(this.generateBiomesDescription(biome, null)).
			icon(biome.getIcon()).
			glow(!biome.isDeployed());

		if (this.editMode)
		{
			itemBuilder.description("");
			itemBuilder.description(this.user.getTranslation(Constants.TIPS + "click-to-edit"));

			itemBuilder.clickHandler((panel, user1, clickType, i) -> {
				EditBiomePanel.open(this, biome);
				return true;
			});
		}
		else
		{
			itemBuilder.description("");
			itemBuilder.description(this.user.getTranslation(Constants.TIPS + "click-to-remove"));

			itemBuilder.clickHandler((panel, user1, clickType, i) -> {
				Consumer<Boolean> consumer = value -> {
					if (value)
					{
						this.addon.getAddonManager().removeBiome(biome);
					}

					this.build();
				};

				// Create conversation that gets user acceptance to delete generator data.
				ConversationUtils.createConfirmation(
					consumer,
					this.user,
					this.user.getTranslation(Constants.CONVERSATIONS + "confirm-biome-deletion",
						Constants.PARAMETER_GAMEMODE, Utils.getGameMode(this.world),
						Constants.PARAMETER_BIOME, biome.getFriendlyName()),
					this.user.getTranslation(Constants.CONVERSATIONS + "biome-removed",
						Constants.PARAMETER_GAMEMODE, Utils.getGameMode(this.world),
						Constants.PARAMETER_BIOME, biome.getFriendlyName()));
				return true;
			});
		}

		return itemBuilder.build();
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


	/**
	 * Allows to change between edit and delete mode.
	 */
	private final boolean editMode;
}
