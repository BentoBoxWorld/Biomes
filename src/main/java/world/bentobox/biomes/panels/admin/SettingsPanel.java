package world.bentobox.biomes.panels.admin;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.biomes.BiomesAddon;
import world.bentobox.biomes.config.Settings;
import world.bentobox.biomes.panels.CommonPanel;
import world.bentobox.biomes.panels.ConversationUtils;
import world.bentobox.biomes.utils.Constants;
import world.bentobox.biomes.utils.Utils;


/**
 * This class manages Addon Settings Editing Panel.
 */
public class SettingsPanel extends CommonPanel
{
	/**
	 * Instantiates a new Edit settings panel.
	 *
	 * @param parentPanel the parent panel
	 */
	private SettingsPanel(CommonPanel parentPanel)
	{
		super(parentPanel);
		this.settings = this.addon.getSettings();
	}


	/**
	 * Instantiates a new Edit settings panel.
	 *
	 * @param addon the addon
	 * @param world the world
	 * @param user the user
	 * @param topLabel the top label
	 * @param permissionPrefix the permission prefix
	 */
	private SettingsPanel(BiomesAddon addon,
		World world,
		User user,
		String topLabel,
		String permissionPrefix)
	{
		super(addon, user, world, topLabel, permissionPrefix);
		this.settings = this.addon.getSettings();
	}


	/**
	 * This method opens new Edit Settings Panel based on parameters from ParentPanel.
	 *
	 * @param parentPanel the parent panel
	 */
	public static void open(CommonPanel parentPanel)
	{
		new SettingsPanel(parentPanel).build();
	}


	/**
	 * This method opens new Edit Settings Panel based on given parameters.
	 *
	 * @param addon the addon
	 * @param world the world
	 * @param user the user
	 * @param topLabel the top label
	 * @param permissionPrefix the permission prefix
	 */
	public static void open(BiomesAddon addon,
		World world,
		User user,
		String topLabel,
		String permissionPrefix)
	{
		new SettingsPanel(addon, world, user, topLabel, permissionPrefix).build();
	}


	/**
	 * This method builds all necessary elements in GUI panel.
	 */
	@Override
	public void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder().
			user(this.user).
			name(this.user.getTranslation(Constants.TITLE + "settings"));

		PanelUtils.fillBorder(panelBuilder, Material.PURPLE_STAINED_GLASS_PANE);

		panelBuilder.item(10, this.createButton(Action.DEFAULT_MODE));
		panelBuilder.item(11, this.createButton(Action.DEFAULT_SIZE));

		panelBuilder.item(28, this.createButton(Action.CHANGE_TIMEOUT));
		panelBuilder.item(29, this.createButton(Action.PARALLEL_UPDATES));

		panelBuilder.item(14, this.createButton(Action.COOL_DOWN));
		panelBuilder.item(15, this.createButton(Action.PROTECTION_RANGE));
		panelBuilder.item(16, this.createButton(Action.RESET));

		panelBuilder.item(23, this.createButton(Action.UNLOCK_NOTIFY));



		panelBuilder.item(44, this.returnButton);

		panelBuilder.build();
	}


	/**
	 * This method returns button for settings panel of given type.
	 * @param button Type of button.
	 * @return new panel button with requested type.
	 */
	private PanelItem createButton(Action button)
	{
		final String reference = Constants.BUTTON + button.name().toLowerCase() + ".";
		String name = this.user.getTranslation(reference + "name");
		List<String> description = new ArrayList<>();
		description.add(this.user.getTranslation(reference + "description"));

		PanelItem.ClickHandler clickHandler;
		boolean glow;

		ItemStack icon;
		int count = 1;

		switch (button)
		{
			case DEFAULT_MODE -> {
				description.add(this.user.getTranslation(reference +
					(this.settings.getDefaultMode().equals(Settings.UpdateMode.ISLAND) ? "enabled" : "disabled")) +
					this.user.getTranslation(reference + "island"));
				description.add(this.user.getTranslation(reference +
					(this.settings.getDefaultMode().equals(Settings.UpdateMode.CHUNK) ? "enabled" : "disabled")) +
					this.user.getTranslation(reference + "chunk"));
				description.add(this.user.getTranslation(reference +
					(this.settings.getDefaultMode().equals(Settings.UpdateMode.RANGE) ? "enabled" : "disabled")) +
					this.user.getTranslation(reference + "range"));

				if (this.settings.getDefaultMode().equals(Settings.UpdateMode.ISLAND))
				{
					icon = new ItemStack(Material.GRASS_BLOCK);
				}
				else if (this.settings.getDefaultMode().equals(Settings.UpdateMode.CHUNK))
				{
					icon = new ItemStack(Material.DIRT);
				}
				else
				{
					icon = new ItemStack(Material.GLASS);
				}

				clickHandler = (panel, user, clickType, slot) -> {
					if (clickType.isRightClick())
					{
						this.settings.setDefaultMode(Utils.getPreviousValue(Settings.UpdateMode.values(),
							this.settings.getDefaultMode()));
					}
					else
					{
						this.settings.setDefaultMode(Utils.getNextValue(Settings.UpdateMode.values(),
							this.settings.getDefaultMode()));
					}

					// Rebuild just this icon
					panel.getInventory().setItem(slot, this.createButton(button).getItem());
					this.addon.saveSettings();
					return true;
				};
				glow = false;

				description.add("");
				description.add(this.user.getTranslation(Constants.TIPS + "left-click-to-cycle"));
				description.add(this.user.getTranslation(Constants.TIPS + "right-click-to-cycle"));
			}
			case DEFAULT_SIZE -> {
				description.add(this.user.getTranslation(reference + "value",
					Constants.PARAMETER_NUMBER, String.valueOf(this.settings.getDefaultSize())));

				icon = new ItemStack(Material.PISTON, Math.max(1, this.settings.getDefaultSize()));
				clickHandler = (panel, user, clickType, i) -> {
					Consumer<Number> numberConsumer = number -> {
						if (number != null)
						{
							this.settings.setDefaultSize(number.intValue());
							this.addon.saveSettings();
						}

						// reopen panel
						this.build();
					};

					ConversationUtils.createNumericInput(numberConsumer,
						this.user,
						this.user.getTranslation(Constants.CONVERSATIONS + "input-number"),
						0,
						Integer.MAX_VALUE);

					return true;
				};
				glow = false;

				description.add("");
				description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
			}
			case COOL_DOWN -> {
				if (this.settings.getCoolDown() > 0)
				{
					description.add(this.user.getTranslation(reference + "value",
						Constants.PARAMETER_NUMBER, String.valueOf(this.settings.getCoolDown())));
				}
				else
				{
					description.add(this.user.getTranslation(reference + "disabled"));
				}

				icon = new ItemStack(Material.DAYLIGHT_DETECTOR, Math.max(1, this.settings.getCoolDown()));
				clickHandler = (panel, user, clickType, i) -> {
					Consumer<Number> numberConsumer = number -> {
						if (number != null)
						{
							this.settings.setCoolDown(number.intValue());
							this.addon.saveSettings();
						}

						// reopen panel
						this.build();
					};

					ConversationUtils.createNumericInput(numberConsumer,
						this.user,
						this.user.getTranslation(Constants.CONVERSATIONS + "input-number"),
						0,
						Integer.MAX_VALUE);

					return true;
				};
				glow = false;

				description.add("");
				description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
			}
			case PROTECTION_RANGE -> {
				description.add(this.user.getTranslation(reference +
					(this.settings.isUseProtectionRange() ? "enabled" : "disabled")));

				icon = new ItemStack(Material.FILLED_MAP);
				clickHandler = (panel, user, clickType, slot) -> {
					this.settings.setUseProtectionRange(!this.settings.isUseProtectionRange());
					this.addon.saveSettings();
					this.build();
					return true;
				};
				glow = this.settings.isUseProtectionRange();

				description.add("");
				description.add(this.user.getTranslation(Constants.TIPS + "click-to-toggle"));

			}
			case RESET -> {
				description.add(this.user.getTranslation(reference +
					(this.settings.isResetBiomes() ? "enabled" : "disabled")));

				icon = new ItemStack(Material.DROPPER);
				clickHandler = (panel, user, clickType, slot) -> {
					this.settings.setResetBiomes(!this.settings.isResetBiomes());
					this.addon.saveSettings();
					this.build();
					return true;
				};
				glow = this.settings.isResetBiomes();

				description.add("");
				description.add(this.user.getTranslation(Constants.TIPS + "click-to-toggle"));
			}
			case UNLOCK_NOTIFY -> {
				description.add(this.user.getTranslation(reference +
					(this.settings.isNotifyUnlockedBiomes() ? "enabled" : "disabled")));

				icon = new ItemStack(Material.PAPER);

				clickHandler = (panel, user, clickType, i) -> {
					this.settings.setNotifyUnlockedBiomes(!this.settings.isNotifyUnlockedBiomes());
					this.addon.saveSettings();
					// Update button in panel
					this.build();

					return true;
				};

				glow = this.settings.isNotifyUnlockedBiomes();

				description.add("");
				description.add(this.user.getTranslation(Constants.TIPS + "click-to-toggle"));
			}
			case CHANGE_TIMEOUT -> {
				description.add(this.user.getTranslation(reference + "value",
					Constants.PARAMETER_NUMBER, String.valueOf(this.settings.getChangeTimeout())));

				icon = new ItemStack(Material.REDSTONE_LAMP, (int) Math.max(1, this.settings.getChangeTimeout()));
				clickHandler = (panel, user, clickType, i) -> {
					Consumer<Number> numberConsumer = number -> {
						if (number != null)
						{
							this.settings.setChangeTimeout(number.intValue());
							this.addon.saveSettings();
						}

						// reopen panel
						this.build();
					};

					ConversationUtils.createNumericInput(numberConsumer,
						this.user,
						this.user.getTranslation(Constants.CONVERSATIONS + "input-number"),
						0,
						Integer.MAX_VALUE);

					return true;
				};
				glow = false;

				description.add("");
				description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
			}
			case PARALLEL_UPDATES -> {
				description.add(this.user.getTranslation(reference + "value",
					Constants.PARAMETER_NUMBER, String.valueOf(this.settings.getConcurrentBiomeUpdates())));

				icon = new ItemStack(Material.SUGAR_CANE, Math.max(1, this.settings.getConcurrentBiomeUpdates()));
				clickHandler = (panel, user, clickType, i) -> {
					Consumer<Number> numberConsumer = number -> {
						if (number != null)
						{
							this.settings.setConcurrentBiomeUpdates(number.intValue());
							this.addon.saveSettings();
						}

						// reopen panel
						this.build();
					};

					ConversationUtils.createNumericInput(numberConsumer,
						this.user,
						this.user.getTranslation(Constants.CONVERSATIONS + "input-number"),
						1,
						Integer.MAX_VALUE);

					return true;
				};
				glow = false;

				description.add("");
				description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
			}
			default -> {
				icon = new ItemStack(Material.PAPER);
				clickHandler = (panel, user1, clickType, slot) -> true;
				glow = false;
			}
		}

		return new PanelItemBuilder().
			name(name).
			description(description).
			icon(icon).
			amount(count).
			clickHandler(clickHandler).
			glow(glow).
			build();
	}


	// ---------------------------------------------------------------------
	// Section: Variables
	// ---------------------------------------------------------------------


	/**
	 * The enum Action.
	 */
	private enum Action
	{
		/**
		 * Default mode action.
		 */
		DEFAULT_MODE,
		/**
		 * Default size action.
		 */
		DEFAULT_SIZE,
		/**
		 * Cool down action.
		 */
		COOL_DOWN,
		/**
		 * Protection range action.
		 */
		PROTECTION_RANGE,
		/**
		 * Reset action.
		 */
		RESET,
		/**
		 * Notify Biome Unlock
		 */
		UNLOCK_NOTIFY,
		/**
		 * Change timeout action.
		 */
		CHANGE_TIMEOUT,
		/**
		 * Parallel biome updates action.
		 */
		PARALLEL_UPDATES
	}


	/**
	 * The Settings object that stores all values.
	 */
	private final Settings settings;
}
