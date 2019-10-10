package world.bentobox.biomes.panels.admin;


import java.util.List;

import org.bukkit.Material;
import org.bukkit.World;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.biomes.BiomesAddon;
import world.bentobox.biomes.database.objects.BiomesObject;
import world.bentobox.biomes.panels.CommonGUI;
import world.bentobox.biomes.panels.GuiUtils;


public class ListBiomesGUI extends CommonGUI
{
	/**
	 * {@inheritDoc}
	 */
	public ListBiomesGUI(CommonGUI parentPanel, boolean editMode)
	{
		super(parentPanel);
		this.editMode = editMode;
	}


	public ListBiomesGUI(BiomesAddon addon,
		World world,
		User user,
		String topLabel,
		String permissionPrefix,
		boolean editMode)
	{
		super(addon, world, user, topLabel, permissionPrefix);
		this.editMode = editMode;
	}


	@Override
	public void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder().user(this.user);

		if (this.editMode)
		{
			panelBuilder.name(this.user.getTranslation("biomes.gui.title.admin.edit-list"));
			GuiUtils.fillBorder(panelBuilder, Material.BLUE_STAINED_GLASS_PANE);
		}
		else
		{
			panelBuilder.name(this.user.getTranslation("biomes.gui.title.admin.remove-list"));
			GuiUtils.fillBorder(panelBuilder, Material.RED_STAINED_GLASS_PANE);
		}

		List<BiomesObject> biomes = this.addon.getAddonManager().getBiomes(this.world);

		final int MAX_ELEMENTS = 21;

		if (this.pageIndex < 0)
		{
			this.pageIndex = biomes.size() / MAX_ELEMENTS;
		}
		else if (this.pageIndex > (biomes.size() / MAX_ELEMENTS))
		{
			this.pageIndex = 0;
		}

		int biomeIndex = MAX_ELEMENTS * this.pageIndex;

		// I want first row to be only for navigation and return button.
		int index = 10;

		while (biomeIndex < ((this.pageIndex + 1) * MAX_ELEMENTS) &&
			biomeIndex < biomes.size() &&
			index < 36)
		{
			if (!panelBuilder.slotOccupied(index))
			{
				panelBuilder.item(index, this.createBiomeIcon(biomes.get(biomeIndex++)));
			}

			index++;
		}

		// Navigation buttons only if necessary
		if (biomes.size() > MAX_ELEMENTS)
		{
			panelBuilder.item(18, this.getButton(CommonButtons.PREVIOUS));
			panelBuilder.item(26, this.getButton(CommonButtons.NEXT));
		}

		panelBuilder.item(44, this.returnButton);

		panelBuilder.build();
	}


	/**
	 * This method creates biomes icon. It sets it to glow, if biome is not deployed.
	 * @param biome Biome which icon must be created.
	 * @return New Biome Icon.
	 */
	private PanelItem createBiomeIcon(BiomesObject biome)
	{
		return new PanelItemBuilder().
			name(biome.getFriendlyName()).
			icon(biome.getIcon()).
			description(this.generateBiomesDescription(biome)).
			clickHandler((panel, user1, clickType, slot) -> {
				if (this.editMode)
				{
					new EditBiomeGUI(this, biome).build();
				}
				else
				{
					this.addon.getAddonManager().removeBiome(biome);

					this.user.sendMessage("biomes.messages.biome-removed", "[biome]", biome.getUniqueId());

					this.pageIndex = 0;
					this.build();
				}

				return true;
			}).
			glow(!biome.isDeployed()).
			build();
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


	/**
	 * Allows to change between edit and delete mode.
	 */
	private boolean editMode;
}
