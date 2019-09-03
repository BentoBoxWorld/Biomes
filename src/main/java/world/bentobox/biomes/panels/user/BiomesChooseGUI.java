package world.bentobox.biomes.panels.user;


import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.biomes.BiomesAddon;
import world.bentobox.biomes.config.Settings;
import world.bentobox.biomes.database.objects.BiomesObject;

import world.bentobox.biomes.panels.CommonGUI;

import world.bentobox.biomes.config.Settings.UpdateMode;
import world.bentobox.biomes.config.Settings.VisibilityMode;
import world.bentobox.biomes.panels.GuiUtils;


/**
 * This class manages Player GUI panel.
 */
public class BiomesChooseGUI extends CommonGUI
{
	/**
	 * Default constructor.
	 * {@inheritDoc}
	 */
	public BiomesChooseGUI(BiomesAddon addon,
		World world,
		User user,
		String topLabel,
		String permissionPrefix)
	{
		super(addon, world, user, topLabel, permissionPrefix);

		this.visibilityMode = this.addon.getSettings().getVisibilityMode().equals(VisibilityMode.TOGGLEABLE) ?
			VisibilityMode.ACCESSIBLE :
			this.addon.getSettings().getVisibilityMode();

		// Allocate biomes for player
		this.biomeList = this.addon.getAddonManager().getBiomes(this.world, this.user, this.visibilityMode);

		this.target = null;
	}


	/**
	 * Default constructor.
	 * {@inheritDoc}
	 * @param target - Targeted player if Biome is changed by Administrator.
	 */
	public BiomesChooseGUI(CommonGUI parentGui, User target)
	{
		super(parentGui);

		// Admins should see all biomes.
		this.visibilityMode = this.addon.getSettings().getVisibilityMode().equals(VisibilityMode.TOGGLEABLE) ?
			(target == null ? VisibilityMode.ACCESSIBLE : VisibilityMode.ALL) :
			this.addon.getSettings().getVisibilityMode();

		// Allocate biomes for player
		this.biomeList = this.addon.getAddonManager().getBiomes(this.world, this.user, this.visibilityMode);
		this.target = target;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void build()
	{
		// Do not open gui if there is no challenges.
		if (this.biomeList.isEmpty())
		{
			this.user.sendMessage("biomes.errors.no-biomes-for-you");
			return;
		}

		PanelBuilder panelBuilder = new PanelBuilder().user(this.user).
			name(this.user.getTranslation("biomes.gui.title.biomes-choose"));


		// GUI should open with border around all elements, so need to calculate how large gui is necessary.
		// Max will be 3 rows.
		// As these operations are fast, then do not use extra memory.

		int rowCount = this.biomeList.size() > 14 ? 3 : this.biomeList.size() > 7 ? 2 : 1;
		// Maximal elements in page. This avoids flickering between different size GUIs.
		final int MAX_ELEMENTS = rowCount * 7;

		GuiUtils.fillBorder(panelBuilder, rowCount + 2, Material.GRAY_STAINED_GLASS_PANE);

		final int correctPage;

		if (this.pageIndex < 0)
		{
			correctPage = this.biomeList.size() / MAX_ELEMENTS;
		}
		else if (this.pageIndex > (this.biomeList.size() / MAX_ELEMENTS))
		{
			correctPage = 0;
		}
		else
		{
			correctPage = this.pageIndex;
		}

		if (this.biomeList.size() > MAX_ELEMENTS)
		{
			// Navigation buttons if necessary

			panelBuilder.item(9, this.getButton(CommonButtons.PREVIOUS));

			panelBuilder.item(17, this.getButton(CommonButtons.NEXT));
		}

		int biomesIndex = MAX_ELEMENTS * correctPage;

		// I want first row to be only for navigation and return button.
		int index = 10;

		while (biomesIndex < ((correctPage + 1) * MAX_ELEMENTS) &&
			biomesIndex < this.biomeList.size() &&
			index < 36)
		{
			if (!panelBuilder.slotOccupied(index))
			{
				panelBuilder.item(index,
					this.createBiomeIcon(this.biomeList.get(biomesIndex++)));
			}

			index++;
		}

		if (this.addon.getSettings().getVisibilityMode().equals(VisibilityMode.TOGGLEABLE))
		{
			panelBuilder.item(8, this.createVisibilityModeButton());
		}

		// Element in the corner should be CANCEL button
		panelBuilder.item((rowCount + 2) * 9 - 1, this.getButton(CommonButtons.RETURN));

		panelBuilder.build();
	}


	/**
	 * This method builds PanelItem for given biome.
	 * @param biome BiomesObject which PanelItem must be created.
	 * @return new PanelItem for given BiomesObject.
	 */
	private PanelItem createBiomeIcon(BiomesObject biome)
	{
		List<String> description = this.generateBiomesDescription(biome);

		Settings settings = this.addon.getSettings();

		return new PanelItemBuilder().
			name(biome.getFriendlyName()).
			description(GuiUtils.stringSplit(description, settings.getLoreLineLength())).
			icon(biome.getIcon()).
			clickHandler((panel, user1, clickType, slot) -> {

				if (settings.isAdvancedMenu())
				{
					new UpdateModeGUI(this, this.target, biome).build();
				}
				else
				{
					List<String> arguments = new ArrayList<>(4);

					if (this.target != null)
					{
						arguments.add(this.target.getName());
					}

					arguments.add(biome.getUniqueId());

					if (BiomesAddon.BIOMES_WORLD_PROTECTION.isSetForWorld(this.world))
					{
						arguments.add(settings.getDefaultMode().name());
						arguments.add(Integer.toString(settings.getDefaultSize()));
					}
					else
					{
						// This fix issues when admin disables Advanced GUI and sets
						// incompatible options

						if (settings.getDefaultMode().equals(UpdateMode.ISLAND))
						{
							arguments.add(UpdateMode.RANGE.name());
							arguments.add(Integer.toString(this.addon.getPlugin().getIWM().getIslandDistance(this.world)));
						}
						else
						{
							arguments.add(settings.getDefaultMode().name());
							arguments.add(Integer.toString(settings.getDefaultSize()));
						}
					}

					this.callCommand(SET, arguments);

					this.user.closeInventory();
					return true;
				}

				return true;
			}).
			glow(false).
			build();
	}


	/**
	 * This method creates button that allows to switch which biomes are displayed.
	 * @return PanelItem that represents Visibility button.
	 */
	private PanelItem createVisibilityModeButton()
	{
		List<String> values = new ArrayList<>(4);
		values.add(this.user.getTranslation("biomes.gui.descriptions.visibility-mode.info"));

		values.add((this.visibilityMode.equals(Settings.VisibilityMode.ALL) ? "&2" : "&c") +
			this.user.getTranslation("biomes.gui.descriptions.visibility-mode.all"));
		values.add((this.visibilityMode.equals(Settings.VisibilityMode.DEPLOYED) ? "&2" : "&c") +
			this.user.getTranslation("biomes.gui.descriptions.visibility-mode.deployed"));
		values.add((this.visibilityMode.equals(Settings.VisibilityMode.ACCESSIBLE) ? "&2" : "&c") +
			this.user.getTranslation("biomes.gui.descriptions.visibility-mode.accessible"));

		ItemStack icon;

		if (this.visibilityMode.equals(Settings.VisibilityMode.ALL))
		{
			icon = new ItemStack(Material.BIRCH_PLANKS);
		}
		else if (this.visibilityMode.equals(Settings.VisibilityMode.DEPLOYED))
		{
			icon = new ItemStack(Material.BIRCH_STAIRS);
		}
		else if (this.visibilityMode.equals(Settings.VisibilityMode.ACCESSIBLE))
		{
			icon = new ItemStack(Material.BIRCH_SLAB);
		}
		else
		{
			icon = new ItemStack(Material.AIR);
		}

		return new PanelItemBuilder().
			name(this.user.getTranslation("biomes.gui.buttons.admin.visibility-mode")).
			description(GuiUtils.stringSplit(values, this.addon.getSettings().getLoreLineLength())).
			icon(icon).
			clickHandler((panel, user1, clickType, slot) -> {

				if (clickType.isRightClick())
				{
					if (this.visibilityMode.equals(VisibilityMode.ALL))
					{
						this.visibilityMode = VisibilityMode.DEPLOYED;
					}
					else if (this.visibilityMode.equals(VisibilityMode.DEPLOYED))
					{
						this.visibilityMode = VisibilityMode.ACCESSIBLE;
					}
					else if (this.visibilityMode.equals(VisibilityMode.ACCESSIBLE))
					{
						this.visibilityMode = VisibilityMode.ALL;
					}
				}
				else
				{
					if (this.visibilityMode.equals(VisibilityMode.ALL))
					{
						this.visibilityMode = VisibilityMode.ACCESSIBLE;
					}
					else if (this.visibilityMode.equals(VisibilityMode.DEPLOYED))
					{
						this.visibilityMode = VisibilityMode.ALL;
					}
					else if (this.visibilityMode.equals(VisibilityMode.ACCESSIBLE))
					{
						this.visibilityMode = VisibilityMode.DEPLOYED;
					}
				}

				this.biomeList = this.addon.getAddonManager().getBiomes(this.world, this.user, this.visibilityMode);
				this.build();

				return true;
			}).
			glow(false).
			build();
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

	/**
	 * List of biomes that user can change.
	 */
	private List<BiomesObject> biomeList;

	/**
	 * Target player. Most of times it will be equal user, but if admin changes, target will be different user.
	 */
	private User target;

	/**
	 * Mode that shows which biomes users can see.
	 */
	private VisibilityMode visibilityMode;
}
