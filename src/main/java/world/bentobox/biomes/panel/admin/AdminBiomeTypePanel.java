package world.bentobox.biomes.panel.admin;


import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.biomes.BiomesAddon;
import world.bentobox.biomes.panel.CommonPanel;


/**
 * This panel will open menu with all biomes that are available in current Minecraft server.
 */
public class AdminBiomeTypePanel extends CommonPanel
{
	/**
	 * @inheritDoc
	 * @param targetPanel Panel that is necessary to open when biome is choosed.
	 */
	public AdminBiomeTypePanel(BiomesAddon addon,
		World world,
		User user,
		String topLabel,
		String permissionPrefix,
		CommonPanel targetPanel)
	{
		this(addon, world, user, topLabel, permissionPrefix, targetPanel, null);
	}


	/**
	 * @inheritDoc
	 * @param targetPanel Panel that is necessary to open when biome is choosed.
	 */
	public AdminBiomeTypePanel(BiomesAddon addon,
		World world,
		User user,
		String topLabel,
		String permissionPrefix,
		CommonPanel targetPanel,
		CommonPanel parentPanel)
	{
		super(addon, world, user, topLabel, permissionPrefix, parentPanel);
		this.targetPanel = targetPanel;
		this.cancelButton =
			this.createCommonButton(CommonButtons.CANCEL,
				(panel, user1, clickType, slot) -> {
					if (this.parentPanel != null)
					{
						this.parentPanel.build();
					}
					else
					{
						this.user.closeInventory();
					}

				return false;
			});
	}


	@Override
	public void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder().user(this.user).name(
			this.user.getTranslation("biomes.gui.admin.choose-biome-title"));

		int MAX_ELEMENTS = 45;
		if (this.pageIndex < 0)
		{
			this.pageIndex = 0;
		}
		else if (this.pageIndex > (Biome.values().length / MAX_ELEMENTS))
		{
			this.pageIndex = Biome.values().length / MAX_ELEMENTS;
		}

		int biomeIndex = MAX_ELEMENTS * this.pageIndex;

		while (biomeIndex < ((this.pageIndex + 1) * MAX_ELEMENTS) &&
			biomeIndex < Biome.values().length)
		{
			panelBuilder.item(this.createBiomeButton(Biome.values()[biomeIndex]));
			biomeIndex++;
		}

		int nextIndex = biomeIndex % MAX_ELEMENTS == 0 ?
			MAX_ELEMENTS :
			(((biomeIndex % MAX_ELEMENTS) - 1) / 9 + 1) * 9;

		if (biomeIndex > MAX_ELEMENTS)
		{
			panelBuilder.item(nextIndex, this.createCommonButton(CommonButtons.PREVIOUS));
		}

		if (biomeIndex < Biome.values().length)
		{
			panelBuilder.item(nextIndex + 8, this.createCommonButton(CommonButtons.NEXT));
		}

		panelBuilder.item(nextIndex + 4, this.cancelButton);

		panelBuilder.build();
	}


	/**
	 * This method creates biomes button.
	 * @param biome Biome which button is necessary.
	 * @return PanelItem button.
	 */
	private PanelItem createBiomeButton(Biome biome)
	{
		return new PanelItemBuilder().
			name(biome.name()).
			icon(Material.MAP).
			clickHandler((panel, user, clickType, slot) -> {
				this.targetPanel.setValueObject(biome);
				this.targetPanel.build();
				return true;
			}).build();
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

	private CommonPanel targetPanel;

	private PanelItem cancelButton;
}
