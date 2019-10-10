package world.bentobox.biomes.panels.user;


import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.biomes.BiomesAddon;
import world.bentobox.biomes.config.Settings.UpdateMode;
import world.bentobox.biomes.database.objects.BiomesObject;
import world.bentobox.biomes.panels.CommonGUI;
import world.bentobox.biomes.panels.GuiUtils;
import world.bentobox.biomes.panels.util.NumberGUI;


/**
 * This class manages Player GUI panel.
 */
public class UpdateModeGUI extends CommonGUI
{
	/**
	 * Default constructor.
	 * {@inheritDoc}
	 * @param target - Targeted player if Biome is changed by Administrator.
	 */
	public UpdateModeGUI(CommonGUI parentGui, User target, BiomesObject biome)
	{
		super(parentGui);

		this.target = target;
		this.biome = biome;

		this.updateMode = this.addon.getSettings().getDefaultMode();
		this.distanceValue = this.addon.getSettings().getDefaultSize();

		if (this.updateMode.equals(UpdateMode.ISLAND) &&
			!BiomesAddon.BIOMES_WORLD_PROTECTION.isSetForWorld(this.world))
		{
			// Fix default mode and size if WORLD flag is disabled.
			this.updateMode = UpdateMode.RANGE;
			this.distanceValue = this.addon.getPlugin().getIWM().getIslandDistance(this.world);
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder().user(this.user).
			name(this.user.getTranslation("biomes.gui.title.mode-choose"));

		GuiUtils.fillBorder(panelBuilder, Material.GRAY_STAINED_GLASS_PANE);

		// Map at the top of the GUI
		panelBuilder.item(4, this.createButton(Button.HEADER));

		// Choose Update Mode Type
		if (BiomesAddon.BIOMES_WORLD_PROTECTION.isSetForWorld(this.world))
		{
			// Island mode should be available only if world protection is enabled.
			panelBuilder.item(12, this.createButton(Button.ISLAND));
		}

		panelBuilder.item(13, this.createButton(Button.CHUNK));
		panelBuilder.item(14, this.createButton(Button.SQUARE));

		// Decrease of current value
		panelBuilder.item(19, this.createButton(Button.DECREASE, 10));
		panelBuilder.item(20, this.createButton(Button.DECREASE, 5));
		panelBuilder.item(21, this.createButton(Button.DECREASE, 1));
		// Paper that shows current value
		panelBuilder.item(22, this.createButton(Button.VALUE));
		// Increase of current value
		panelBuilder.item(23, this.createButton(Button.INCREASE, 1));
		panelBuilder.item(24, this.createButton(Button.INCREASE, 5));
		panelBuilder.item(25, this.createButton(Button.INCREASE, 10));

		// Set values
		panelBuilder.item(28, this.createButton(Button.SET, 0));
		panelBuilder.item(29, this.createButton(Button.SET, 2));
		panelBuilder.item(30, this.createButton(Button.SET, 4));
		panelBuilder.item(31, this.createButton(Button.SET, 8));
		panelBuilder.item(32, this.createButton(Button.SET, 16));
		panelBuilder.item(33, this.createButton(Button.SET, 32));
		panelBuilder.item(34, this.createButton(Button.SET, 64));

		// Bottom buttons
		panelBuilder.item(39, this.createButton(Button.DECLINE));
		panelBuilder.item(41, this.createButton(Button.ACCEPT));

		panelBuilder.item(44, this.returnButton);

		panelBuilder.build();
	}


	/**
	 * This method returns PanelItem that represents required button with 1 amount.
	 * @param button Required button type.
	 * @return PanelItem that represents required button.
	 */
	private PanelItem createButton(Button button)
	{
		return this.createButton(button, 1);
	}


	/**
	 * This method creates PanelItem button with number representing how many amount of item will be required.
	 * @param button Button that need to be created.
	 * @param number Amount that will represent item stack size and in math operations.
	 * @return Required type of PanelItem.
	 */
	private PanelItem createButton(Button button, int number)
	{
		PanelItemBuilder itemBuilder = new PanelItemBuilder();

		switch (button)
		{
			case VALUE:
			{
				itemBuilder.name(this.user.getTranslation("biomes.gui.buttons.value",
					"[number]", Integer.toString(this.distanceValue)));
				itemBuilder.icon(new ItemStack(Material.PAPER, number));

				if (!this.updateMode.equals(UpdateMode.ISLAND))
				{
					itemBuilder.clickHandler((panel, user1, clickType, i) -> {

						// On right click open NumberGui for +/- manual input.
						if (clickType.isRightClick())
						{
							new NumberGUI(this.user,
								this.distanceValue,
								0,
								this.addon.getSettings().getLoreLineLength(),
								(status, value) -> {
									if (status)
									{
										this.distanceValue = value;
									}

									UpdateModeGUI.this.build();
								});
						}
						return true;
					});
				}

				break;
			}
			case INCREASE:
			{
				itemBuilder.name(this.user.getTranslation("biomes.gui.buttons.increase", "[number]", Integer.toString(number)));

				if (this.updateMode.equals(UpdateMode.ISLAND))
				{
					itemBuilder.icon(new ItemStack(Material.BLACK_STAINED_GLASS_PANE, number));
				}
				else
				{
					itemBuilder.icon(new ItemStack(Material.GREEN_STAINED_GLASS_PANE, number));
					itemBuilder.clickHandler((panel, user1, clickType, i) -> {
						this.distanceValue += number;

						// Several icons will be updated, so need to rebuild all?
						this.build();
						return true;
					});
				}

				break;
			}
			case DECREASE:
			{
				itemBuilder.name(this.user.getTranslation("biomes.gui.buttons.decrease", "[number]", Integer.toString(number)));

				if (this.updateMode.equals(UpdateMode.ISLAND))
				{
					itemBuilder.icon(new ItemStack(Material.BLACK_STAINED_GLASS_PANE, number));
				}
				else
				{
					itemBuilder.icon(new ItemStack(Material.RED_STAINED_GLASS_PANE, number));
					itemBuilder.clickHandler((panel, user1, clickType, i) -> {
						this.distanceValue -= number;

						if (this.distanceValue < 0)
						{
							this.distanceValue = 0;
							// TODO: Probably need to inform or block - buttons.
						}

						// Several icons will be updated, so need to rebuild all?
						this.build();
						return true;
					});
				}

				break;
			}
			case SET:
			{
				itemBuilder.name(this.user.getTranslation("biomes.gui.buttons.set", "[number]", Integer.toString(number)));

				if (this.updateMode.equals(UpdateMode.ISLAND))
				{
					itemBuilder.icon(new ItemStack(Material.BLACK_STAINED_GLASS_PANE, number == 0 ? 1 : number));
				}
				else
				{
					if (number == 0)
					{
						itemBuilder.icon(new ItemStack(Material.YELLOW_STAINED_GLASS_PANE, 1));
					}
					else
					{
						itemBuilder.icon(new ItemStack(Material.WHITE_STAINED_GLASS_PANE, number));
					}

					itemBuilder.clickHandler((panel, user1, clickType, i) -> {
						this.distanceValue = number;
						this.build();
						return true;
					});
				}

				break;
			}
			case ISLAND:
			{
				itemBuilder.name(this.user.getTranslation("biomes.gui.descriptions.update-mode.island"));
				itemBuilder.icon(new ItemStack(Material.GRASS_BLOCK, number));
				itemBuilder.clickHandler((panel, user1, clickType, i) -> {
					this.updateMode = UpdateMode.ISLAND;
					this.build();
					return true;
				});
				itemBuilder.glow(this.updateMode.equals(UpdateMode.ISLAND));

				break;
			}
			case CHUNK:
			{
				itemBuilder.name(this.user.getTranslation("biomes.gui.descriptions.update-mode.chunk"));
				itemBuilder.icon(new ItemStack(Material.DIRT, number));
				itemBuilder.clickHandler((panel, user1, clickType, i) -> {
					this.updateMode = UpdateMode.CHUNK;
					this.build();
					return true;
				});
				itemBuilder.glow(this.updateMode.equals(UpdateMode.CHUNK));

				break;
			}
			case SQUARE:
			{
				itemBuilder.name(this.user.getTranslation("biomes.gui.descriptions.update-mode.square"));
				itemBuilder.icon(new ItemStack(Material.GLASS, number));
				itemBuilder.clickHandler((panel, user1, clickType, i) -> {
					this.updateMode = UpdateMode.RANGE;
					this.build();
					return true;
				});
				itemBuilder.glow(this.updateMode.equals(UpdateMode.RANGE));

				break;
			}
			case ACCEPT:
			{
				itemBuilder.name(this.user.getTranslation("biomes.gui.buttons.accept"));
				itemBuilder.icon(new ItemStack(Material.GREEN_STAINED_GLASS_PANE, number));
				itemBuilder.clickHandler((panel, user1, clickType, i) -> {
					List<String> arguments = new ArrayList<>(4);

					if (this.target != null)
					{
						arguments.add(this.target.getName());
					}

					arguments.add(this.biome.getUniqueId());
					arguments.add(this.updateMode.name());
					arguments.add(Integer.toString(this.distanceValue));

					this.callCommand(SET, arguments);

					return true;
				});

				break;
			}
			case DECLINE:
			{
				itemBuilder.name(this.user.getTranslation("biomes.gui.buttons.decline"));
				itemBuilder.icon(new ItemStack(Material.RED_STAINED_GLASS_PANE, number));
				itemBuilder.clickHandler((panel, user1, clickType, i) -> {
					this.user.closeInventory();
					return true;
				});

				break;
			}
			case HEADER:
			{
				itemBuilder.name(this.user.getTranslation("biomes.gui.buttons.information"));

				itemBuilder.description(GuiUtils.stringSplit(
					this.user.getTranslation("biomes.gui.descriptions.information",
						"[biome]", this.biome.getFriendlyName(),
						"[mode]", this.user.getTranslation("biomes.gui.descriptions.update-mode." + this.updateMode.name().toLowerCase()),
						"[range]", Integer.toString(this.distanceValue)),
					this.addon.getSettings().getLoreLineLength()));

				itemBuilder.icon(new ItemStack(Material.MAP, number));
			}
		}

		return itemBuilder.build();
	}


// ---------------------------------------------------------------------
// Section: Private Classes
// ---------------------------------------------------------------------


	/**
	 * Creates any button that may be created by current GUI.
	 */
	private enum Button
	{
		SET,
		INCREASE,
		DECREASE,
		VALUE,
		ISLAND,
		CHUNK,
		SQUARE,
		ACCEPT,
		DECLINE,
		HEADER
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

	/**
	 * Biomes that user can be changed.
	 */
	private BiomesObject biome;

	/**
	 * Target player. Most of times it will be equal user, but if admin changes, target will be different user.
	 */
	private User target;

	/**
	 * Integer value that is larger then 0 and provides information about chunk radius or block radius where
	 * biome will be updated.
	 */
	private int distanceValue;

	/**
	 * Allows to change biomes update mode.
	 */
	private UpdateMode updateMode;
}
