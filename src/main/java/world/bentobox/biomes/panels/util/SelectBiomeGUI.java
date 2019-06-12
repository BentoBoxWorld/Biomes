package world.bentobox.biomes.panels.util;


import org.bukkit.Material;
import org.bukkit.block.Biome;
import java.util.Collections;
import java.util.function.BiConsumer;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.biomes.BiomesAddon;
import world.bentobox.biomes.panels.GuiUtils;


public class SelectBiomeGUI
{
	public SelectBiomeGUI(User user, Biome inputBiome, int lineLength, BiConsumer<Boolean, Biome> consumer)
	{
		this.consumer = consumer;
		this.user = user;
		this.inputBiome = inputBiome;
		this.lineLength = lineLength;

		this.build(0);
	}


	/**
	 * This method builds panel that allows to select single challenge from input challenges.
	 */
	private void build(int pageIndex)
	{
		PanelBuilder panelBuilder = new PanelBuilder().user(this.user).name(this.user.getTranslation("biomes.gui.title.admin.select-biome"));

		GuiUtils.fillBorder(panelBuilder, Material.BLUE_STAINED_GLASS_PANE);

		// Maximal elements in page.
		final int MAX_ELEMENTS = 21;

		final int correctPage;

		if (pageIndex < 0)
		{
			correctPage = (Biome.values().length - 1) / MAX_ELEMENTS;
		}
		else if (pageIndex > ((Biome.values().length - 1) / MAX_ELEMENTS))
		{
			correctPage = 0;
		}
		else
		{
			correctPage = pageIndex;
		}

		panelBuilder.item(4,
			new PanelItemBuilder().
				icon(Material.RED_STAINED_GLASS_PANE).
				name(this.user.getTranslation("biomes.gui.buttons.return")).
				clickHandler( (panel, user1, clickType, slot) -> {
					this.consumer.accept(true, this.inputBiome);
					return true;
				}).build());

		if ((Biome.values().length - 1) > MAX_ELEMENTS)
		{
			// Navigation buttons if necessary

			panelBuilder.item(18,
				new PanelItemBuilder().
					icon(BiomesAddon.SIGN_MATERIAL).
					name(this.user.getTranslation("biomes.gui.buttons.previous")).
					clickHandler((panel, user1, clickType, slot) -> {
						this.build(correctPage - 1);
						return true;
					}).build());

			panelBuilder.item(26,
				new PanelItemBuilder().
					icon(BiomesAddon.SIGN_MATERIAL).
					name(this.user.getTranslation("biomes.gui.buttons.next")).
					clickHandler((panel, user1, clickType, slot) -> {
						this.build(correctPage + 1);
						return true;
					}).build());
		}

		int biomesIndex = MAX_ELEMENTS * correctPage;

		// I want first row to be only for navigation and return button.
		int index = 10;

		while (biomesIndex < ((correctPage + 1) * MAX_ELEMENTS) &&
			biomesIndex < (Biome.values().length - 1) &&
			index < 36)
		{
			if (!panelBuilder.slotOccupied(index))
			{
				// Skip inputBiome
				if (Biome.values()[biomesIndex] == this.inputBiome)
				{
					biomesIndex++;
				}

				if (Biome.values().length != biomesIndex)
				{
					panelBuilder.item(index,
						this.createBiomeIcon(Biome.values()[(biomesIndex++)]));
				}
			}

			index++;
		}

		panelBuilder.item(44,
			new PanelItemBuilder().
				icon(Material.OAK_DOOR).
				name(this.user.getTranslation("biomes.gui.buttons.return")).
				clickHandler( (panel, user1, clickType, slot) -> {
					this.consumer.accept(true, this.inputBiome);
					return true;
				}).build());

		panelBuilder.build();
	}


	/**
	 * This method builds PanelItem for given biome.
	 * @param biome Biome which PanelItem must be created.
	 * @return new PanelItem for given biome.
	 */
	private PanelItem createBiomeIcon(Biome biome)
	{
		return new PanelItemBuilder().
			name(biome.name()).
			description(Collections.emptyList()).
			icon(Material.MAP).
			clickHandler((panel, user1, clickType, slot) -> {
				this.consumer.accept(true, biome);

				return true;
			}).
			glow(false).
			build();
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


	/**
	 * This variable stores consumer.
	 */
	private BiConsumer<Boolean, Biome> consumer;

	/**
	 * User who runs GUI.
	 */
	private User user;

	private Biome inputBiome;

	/**
	 * This variable stores how large line can be, before warp it.
	 */
	private int lineLength;
}
