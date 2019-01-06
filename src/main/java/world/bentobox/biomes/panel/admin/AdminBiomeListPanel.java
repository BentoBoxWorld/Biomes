package world.bentobox.biomes.panel.admin;


import org.bukkit.World;
import java.util.List;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.biomes.BiomesAddon;
import world.bentobox.biomes.objects.BiomesObject;
import world.bentobox.biomes.panel.CommonPanel;


/**
 * This Panel creates list with all biomes for their editing.
 */
public class AdminBiomeListPanel extends CommonPanel
{
	/**
	 * @inheritDoc
	 * @param editPanel boolean that indicate if biomes must be edited or removed.
	 */
	public AdminBiomeListPanel(BiomesAddon addon,
		World world,
		User user,
		boolean editPanel,
		String topLabel,
		String permissionPrefix)
	{
		this(addon, world, user, editPanel, topLabel, permissionPrefix, null);
	}

	/**
	 * @inheritDoc
	 * @param editPanel boolean that indicate if biomes must be edited or removed.
	 */
	public AdminBiomeListPanel(BiomesAddon addon,
		World world,
		User user,
		boolean editPanel,
		String topLabel,
		String permissionPrefix,
		CommonPanel parentPanel)
	{
		super(addon, world, user, topLabel, permissionPrefix, parentPanel);

		this.editPanel = editPanel;
		this.returnButton = this.parentPanel == null ? null : this.createCommonButton(CommonButtons.RETURN);
	}


	@Override
	public void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder().user(this.user).name(
			this.user.getTranslation("biomes.gui.admin.edit-title"));

		List<BiomesObject> biomes = this.addon.getAddonManager().getBiomes(this.world);

		int MAX_ELEMENTS = 45;
		if (this.pageIndex < 0)
		{
			this.pageIndex = 0;
		}
		else if (this.pageIndex > (biomes.size() / MAX_ELEMENTS))
		{
			this.pageIndex = biomes.size() / MAX_ELEMENTS;
		}

		int playerIndex = MAX_ELEMENTS * this.pageIndex;

		while (playerIndex < ((this.pageIndex + 1) * MAX_ELEMENTS) &&
			playerIndex < biomes.size())
		{
			panelBuilder.item(this.createBiomeIcon(biomes.get(playerIndex)));
			playerIndex++;
		}

		int nextIndex = playerIndex % MAX_ELEMENTS == 0 ?
			MAX_ELEMENTS :
			(((playerIndex % MAX_ELEMENTS) - 1) / 9 + 1) * 9;

		if (playerIndex > MAX_ELEMENTS)
		{
			panelBuilder.item(nextIndex, this.createCommonButton(CommonButtons.PREVIOUS));
		}

		if (playerIndex < biomes.size())
		{
			panelBuilder.item(nextIndex + 8, this.createCommonButton(CommonButtons.NEXT));
		}

		if (this.returnButton != null)
		{
			panelBuilder.item(nextIndex + 4, this.returnButton);
		}

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
			description(biome.getDescription()).
			clickHandler((panel, user1, clickType, slot) -> {
				if (this.editPanel)
				{
					new AdminBiomeEditPanel(this.addon,
						this.world,
						this.user,
						biome,
						this.topLabel,
						this.permissionPrefix,
						this).build();
				}
				else
				{
					this.addon.getAddonManager().removeBiome(biome);

					this.user.sendMessage("biomes.messages.information.biome-removed", "[biome]", biome.getFriendlyName());

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
	 * This indicate if current mode is edit or delete.
	 */
	private boolean editPanel;

	private PanelItem returnButton;
}
