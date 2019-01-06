package world.bentobox.addons.biomes.panel.user;


import org.bukkit.Material;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import world.bentobox.addons.biomes.BiomesAddon;
import world.bentobox.addons.biomes.objects.BiomesObject;
import world.bentobox.addons.biomes.panel.CommonPanel;
import world.bentobox.addons.biomes.utils.Utils.UpdateMode;
import world.bentobox.addons.biomes.utils.Utils.VisibilityMode;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;


/**
 * This panel allows to change biome for user.
 */
public class ChangeBiomePanel extends CommonPanel
{
	/**
	 * @inheritDoc
	 * @param target Targeted user. Can be null.
	 */
	public ChangeBiomePanel(BiomesAddon addon,
		World world,
		User user,
		User target,
		String topLabel,
		String permissionPrefix)
	{
		this(addon, world, user, target, topLabel, permissionPrefix, null);
	}


	/**
	 * @inheritDoc
	 * @param target Targeted user. Can be null.
	 */
	public ChangeBiomePanel(BiomesAddon addon,
		World world,
		User user,
		User target,
		String topLabel,
		String permissionPrefix,
		CommonPanel parentPanel)
	{
		super(addon, world, user, topLabel, permissionPrefix, parentPanel);
		this.targetUser = target;

		this.biomeList = this.addon.getAddonManager().getBiomes(this.world, this.user);
		this.returnButton = this.parentPanel == null ? null : this.createCommonButton(CommonButtons.RETURN);
		this.currentVisibilityMode = VisibilityMode.ACCESSIBLE;
		this.valueObject = this.addon.getSettings().getDefaultMode();
		this.updateRange = this.addon.getSettings().getDefaultSize();
	}


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


	@Override
	public void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder().user(this.user);

		if (this.targetUser == null)
		{
			panelBuilder.name(
				this.user.getTranslation("biomes.gui.title"));
		}
		else
		{
			panelBuilder.name(
				this.user.getTranslation("biomes.gui.admin.gui-title"));
		}

		int MAX_ELEMENTS = this.addon.getSettings().isAdvancedMenu() ? 18 : 45;

		if (this.pageIndex < 0)
		{
			this.pageIndex = 0;
		}
		else if (this.pageIndex > (this.biomeList.size() / MAX_ELEMENTS))
		{
			this.pageIndex = this.biomeList.size() / MAX_ELEMENTS;
		}

		int playerIndex = MAX_ELEMENTS * this.pageIndex;

		while (playerIndex < ((this.pageIndex + 1) * MAX_ELEMENTS) &&
			playerIndex < this.biomeList.size())
		{
			panelBuilder.item(this.createBiomeIcon(this.biomeList.get(playerIndex)));
			playerIndex++;
		}

		int nextIndex = playerIndex % MAX_ELEMENTS == 0 ?
			MAX_ELEMENTS :
			(((playerIndex % MAX_ELEMENTS) - 1) / 9 + 1) * 9;

		if (this.addon.getSettings().isAdvancedMenu())
		{
			panelBuilder.item(nextIndex + 2, this.createCommonButton(CommonButtons.ISLAND));
			panelBuilder.item(nextIndex + 11, this.createCommonButton(CommonButtons.CHUNK));
			panelBuilder.item(nextIndex + 20, this.createCommonButton(CommonButtons.SQUARE));

			// Value Paper
			panelBuilder.item(nextIndex + 12, this.createCurrentRangeButton());

			// Setters.
			panelBuilder.item(nextIndex + 4,
				this.createCommonButton(NumberButtons.SET_1, (panel, user1, clickType, slot) -> {
					this.updateRange = 1;
					this.build();
					return true;
				}));
			panelBuilder.item(nextIndex + 4 + 9,
				this.createCommonButton(NumberButtons.SET_5, (panel, user1, clickType, slot) -> {
					this.updateRange = 5;
					this.build();
					return true;
				}));;
			panelBuilder.item(nextIndex + 4 + 18,
				this.createCommonButton(NumberButtons.SET_10, (panel, user1, clickType, slot) -> {
					this.updateRange = 10;
					this.build();
					return true;
				}));

			// Increases.
			panelBuilder.item(nextIndex + 5,
				this.createCommonButton(NumberButtons.INCREASE_1, (panel, user1, clickType, slot) -> {
					this.updateRange += 1;
					this.build();
					return true;
				}));
			panelBuilder.item(nextIndex + 5 + 9,
				this.createCommonButton(NumberButtons.INCREASE_5, (panel, user1, clickType, slot) -> {
					this.updateRange += 5;
					this.build();
					return true;
				}));
			panelBuilder.item(nextIndex + 5 + 18,
				this.createCommonButton(NumberButtons.INCREASE_10, (panel, user1, clickType, slot) -> {
					this.updateRange += 10;
					this.build();
					return true;
				}));

			// Reducers.
			panelBuilder.item(nextIndex + 6,
				this.createCommonButton(NumberButtons.DECREASE_1, (panel, user1, clickType, slot) -> {
					this.updateRange = Math.max(1, this.updateRange - 1);
					this.build();
					return true;
				}));
			panelBuilder.item(nextIndex + 6 + 9,
				this.createCommonButton(NumberButtons.DECREASE_5, (panel, user1, clickType, slot) -> {
					this.updateRange = Math.max(1, this.updateRange - 5);
					this.build();
					return true;
				}));
			panelBuilder.item(nextIndex + 6 + 18,
				this.createCommonButton(NumberButtons.DECREASE_10, (panel, user1, clickType, slot) -> {
					this.updateRange = Math.max(1, this.updateRange - 10);
					this.build();
					return true;
				}));

			if (playerIndex > MAX_ELEMENTS)
			{
				panelBuilder.item(nextIndex + 9, this.createCommonButton(CommonButtons.PREVIOUS));
			}

			if (playerIndex < this.biomeList.size())
			{
				panelBuilder.item(nextIndex + 17, this.createCommonButton(CommonButtons.NEXT));
			}

			if (this.addon.getSettings().getVisibilityMode().equals(VisibilityMode.TOGGLEABLE))
			{
				panelBuilder.item(nextIndex + 18, this.createToggleButton());
			}

			if (this.returnButton != null)
			{
				// If advanced menu is active, then return button has no place in middle. But it can be placed
				// in corner.
				panelBuilder.item(nextIndex + 26, this.returnButton);
			}
		}
		else
		{
			if (playerIndex > MAX_ELEMENTS)
			{
				panelBuilder.item(nextIndex, this.createCommonButton(CommonButtons.PREVIOUS));
			}

			if (playerIndex < this.biomeList.size())
			{
				panelBuilder.item(nextIndex + 8, this.createCommonButton(CommonButtons.NEXT));
			}

			if (this.addon.getSettings().getVisibilityMode().equals(VisibilityMode.TOGGLEABLE))
			{
				panelBuilder.item(nextIndex + 3, this.createToggleButton());
				nextIndex++;
			}

			if (this.returnButton != null)
			{
				panelBuilder.item(nextIndex + 4, this.returnButton);
			}
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
				// Use command Manager to get necessary command. Biome changing will be processed via command only.

				CompositeCommand command = this.addon.getPlugin().getCommandsManager().getCommand(this.topLabel);
				Optional<CompositeCommand> commandOptional = command.getSubCommand(BIOMES);

				if (!commandOptional.isPresent())
				{
					// Throw error that biomes command not found.
					return true;
				}

				commandOptional = commandOptional.get().getSubCommand(SET);

				if (!commandOptional.isPresent())
				{
					// Throw error that biomes command not found.
					return true;
				}

				command = commandOptional.get();

				List<String> arguments = new ArrayList<>(4);

				if (this.targetUser != null)
				{
					arguments.add(this.targetUser.getName());
				}

				arguments.add(biome.getBiomeName());
				arguments.add(((UpdateMode) this.valueObject).name());
				arguments.add(Integer.toString(this.updateRange));

				if (command.execute(this.user, SET, arguments))
				{
					// Resets cursor to default option
					this.user.closeInventory();
					this.build();

					return true;
				}

				return true;
			}).
			glow(!biome.isDeployed()).
			build();
	}


	/**
	 * This method creates toggle button that changes which biomes is visible.
	 * @return New Toggle Icon.
	 */
	private PanelItem createToggleButton()
	{
		return new PanelItemBuilder().
			name(this.user.getTranslation("biomes.gui.buttons.visibility", "[value]", this.currentVisibilityMode.name())).
			description(this.user.getTranslation("biomes.gui.admin.descriptions.visibility")).
			icon(Material.BIRCH_BUTTON).
			clickHandler((panel, user1, clickType, slot) -> {
				switch (this.currentVisibilityMode)
				{
					case ALL:
						this.currentVisibilityMode = VisibilityMode.ACCESSIBLE;
						break;
					case ACCESSIBLE:
						this.currentVisibilityMode = VisibilityMode.DEPLOYED;
						break;
					case DEPLOYED:
					case TOGGLEABLE:
						this.currentVisibilityMode = VisibilityMode.ALL;
						break;
				}

				this.biomeList = this.addon.getAddonManager().getBiomes(this.world, this.user, this.currentVisibilityMode);
				this.pageIndex = 0;
				this.build();

				return true;
			}).
			glow(false).
			build();
	}


	/**
	 * This method creates value icon. It shows current value for updateRange.
	 * @return New values Icon.
	 */
	private PanelItem createCurrentRangeButton()
	{
		return new PanelItemBuilder().
			name(this.user.getTranslation("biomes.gui.buttons.value","[number]", Integer.toString(this.updateRange))).
			icon(Material.PAPER).
			clickHandler((panel, user1, clickType, slot) -> true).
			build();
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


	private PanelItem returnButton;

	private User targetUser;

	/**
	 * Biome List cache as each list can be toggleable.
	 */
	private List<BiomesObject> biomeList;

	/**
	 * This variable stores current biomes visibility mode.
	 */
	private VisibilityMode currentVisibilityMode;

	/**
	 * This variable stores current update range.
	 */
	private int updateRange;
}
