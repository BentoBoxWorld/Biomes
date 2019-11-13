package world.bentobox.biomes.panels.admin;


import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.NonNull;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.biomes.BiomesAddon;
import world.bentobox.biomes.config.Settings;
import world.bentobox.biomes.panels.CommonGUI;
import world.bentobox.biomes.panels.GuiUtils;
import world.bentobox.biomes.panels.util.NumberGUI;


public class EditSettingsGUI extends CommonGUI
{
	private static final String CURRENT_VALUE = "biomes.gui.descriptions.current-value";
    private static final String ENABLED_REF = "biomes.gui.descriptions.enabled";
    private static final String DISABLED_REF = "biomes.gui.descriptions.disabled";


    /**
	 * {@inheritDoc}
	 */
	public EditSettingsGUI(@NonNull CommonGUI parentGUI)
	{
		super(parentGUI);
		this.config = this.addon.getSettings();
	}


	public EditSettingsGUI(BiomesAddon addon,
		World world,
		User user,
		String topLabel,
		String permissionPrefix)
	{
		super(addon, world, user, topLabel, permissionPrefix);
		this.config = this.addon.getSettings();
	}


	/**
	 * This method builds all necessary elements in GUI panel.
	 */
	@Override
	public void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder().user(this.user).name(
			this.user.getTranslation("biomes.gui.title.admin.settings"));

		GuiUtils.fillBorder(panelBuilder, Material.ORANGE_STAINED_GLASS_PANE);

		panelBuilder.item(19, this.createButton(Button.ADVANCED_MENU));

		panelBuilder.item(11, this.createButton(Button.DEFAULT_MODE));
		panelBuilder.item(20, this.createButton(Button.DEFAULT_SIZE));
		panelBuilder.item(29, this.createButton(Button.VISIBILITY));

		panelBuilder.item(21, this.createButton(Button.LORE_MESSAGE));
		panelBuilder.item(30, this.createButton(Button.LORE_LENGTH));

		panelBuilder.item(23, this.createButton(Button.COOLDOWN));
		panelBuilder.item(24, this.createButton(Button.PROTECTION_RANGE));
		panelBuilder.item(25, this.createButton(Button.RESET));

		panelBuilder.item(44, this.returnButton);

		panelBuilder.build();
	}


	private PanelItem createButton(Button button)
	{
		ItemStack icon;
		String name;
		List<String> description;
		boolean glow;
		PanelItem.ClickHandler clickHandler;

		switch (button)
		{
			case ADVANCED_MENU:
			{
				description = new ArrayList<>(2);
				description.add(this.user.getTranslation("biomes.gui.descriptions.admin.advanced-menu"));
				description.add(this.user.getTranslation(CURRENT_VALUE,
					VALUE_TAG,
					this.config.isAdvancedMenu() ?
						this.user.getTranslation(ENABLED_REF) :
						this.user.getTranslation(DISABLED_REF)));
				name = this.user.getTranslation("biomes.gui.buttons.admin.advanced-menu");
				icon = new ItemStack(Material.COMMAND_BLOCK);

				clickHandler = (panel, user1, clickType, i) -> {
					this.config.setAdvancedMenu(
						!this.config.isAdvancedMenu());

					panel.getInventory().setItem(i, this.createButton(button).getItem());
					return true;
				};

				glow = this.config.isAdvancedMenu();
				break;
			}
			case DEFAULT_MODE:
			{
				name = this.user.getTranslation("biomes.gui.buttons.admin.default-mode");

				List<String> values = new ArrayList<>(5);
				values.add(this.user.getTranslation("biomes.gui.descriptions.admin.default-mode"));

				values.add((this.config.getDefaultMode().equals(Settings.UpdateMode.ISLAND) ? "&2" : "&c") +
					this.user.getTranslation("biomes.gui.descriptions.update-mode.island"));
				values.add((this.config.getDefaultMode().equals(Settings.UpdateMode.CHUNK) ? "&2" : "&c") +
					this.user.getTranslation("biomes.gui.descriptions.update-mode.chunk"));
				values.add((this.config.getDefaultMode().equals(Settings.UpdateMode.RANGE) ? "&2" : "&c") +
					this.user.getTranslation("biomes.gui.descriptions.update-mode.square"));
				description = values;

				if (this.config.getDefaultMode().equals(Settings.UpdateMode.ISLAND))
				{
					icon = new ItemStack(Material.GRASS_BLOCK);
				}
				else if (this.config.getDefaultMode().equals(Settings.UpdateMode.CHUNK))
				{
					icon = new ItemStack(Material.DIRT);
				}
				else if (this.config.getDefaultMode().equals(Settings.UpdateMode.RANGE))
				{
					icon = new ItemStack(Material.GLASS);
				}
				else
				{
					icon = new ItemStack(Material.STRUCTURE_VOID);
				}

				clickHandler = (panel, user, clickType, slot) -> {
					if (clickType.isRightClick())
					{
						this.config.setDefaultMode(
							Settings.UpdateMode.values()[
								this.getPreviousMode(this.config.getDefaultMode(),
									Settings.UpdateMode.values())]);
					}
					else
					{
						this.config.setDefaultMode(
							Settings.UpdateMode.values()[
								this.getNextMode(this.config.getDefaultMode(),
									Settings.UpdateMode.values())]);
					}

					panel.getInventory().setItem(slot, this.createButton(button).getItem());

					return true;
				};
				glow = false;
				break;
			}
			case DEFAULT_SIZE:
			{
				description = new ArrayList<>(2);
				description.add(this.user.getTranslation("biomes.gui.descriptions.admin.default-size"));
				description.add(this.user.getTranslation(CURRENT_VALUE,
					VALUE_TAG, Integer.toString(this.config.getDefaultSize())));
				name = this.user.getTranslation("biomes.gui.buttons.admin.default-size");
				icon = new ItemStack(Material.PISTON);
				clickHandler = (panel, user1, clickType, i) -> {
					new NumberGUI(this.user,
						this.config.getDefaultSize(),
						0,
						this.config.getLoreLineLength(),
						(status, value) -> {
							if (status)
							{
								this.config.setDefaultSize(value);
							}

							EditSettingsGUI.this.build();
						});

					return true;
				};
				glow = false;
				break;
			}
			case VISIBILITY:
			{
				name = this.user.getTranslation("biomes.gui.buttons.admin.visibility-mode");

				List<String> values = new ArrayList<>(5);
				values.add(this.user.getTranslation("biomes.gui.descriptions.admin.visibility-mode"));

				values.add((this.config.getVisibilityMode().equals(Settings.VisibilityMode.ALL) ? "&2" : "&c") +
					this.user.getTranslation("biomes.gui.descriptions.visibility-mode.all"));
				values.add((this.config.getVisibilityMode().equals(Settings.VisibilityMode.DEPLOYED) ? "&2" : "&c") +
					this.user.getTranslation("biomes.gui.descriptions.visibility-mode.deployed"));
				values.add((this.config.getVisibilityMode().equals(Settings.VisibilityMode.ACCESSIBLE) ? "&2" : "&c") +
					this.user.getTranslation("biomes.gui.descriptions.visibility-mode.accessible"));
				values.add((this.config.getVisibilityMode().equals(Settings.VisibilityMode.TOGGLEABLE) ? "&2" : "&c") +
					this.user.getTranslation("biomes.gui.descriptions.visibility-mode.toggleable"));
				description = values;

				if (this.config.getVisibilityMode().equals(Settings.VisibilityMode.ALL))
				{
					icon = new ItemStack(Material.BIRCH_PLANKS);
				}
				else if (this.config.getVisibilityMode().equals(Settings.VisibilityMode.DEPLOYED))
				{
					icon = new ItemStack(Material.BIRCH_STAIRS);
				}
				else if (this.config.getVisibilityMode().equals(Settings.VisibilityMode.ACCESSIBLE))
				{
					icon = new ItemStack(Material.BIRCH_SLAB);
				}
				else
				{
					icon = new ItemStack(Material.BIRCH_BUTTON);
				}

				clickHandler = (panel, user, clickType, slot) -> {
					if (clickType.isRightClick())
					{
						this.config.setVisibilityMode(
							Settings.VisibilityMode.values()[
								this.getPreviousMode(this.config.getVisibilityMode(),
									Settings.VisibilityMode.values())]);
					}
					else
					{
						this.config.setVisibilityMode(
							Settings.VisibilityMode.values()[
								this.getNextMode(this.config.getVisibilityMode(),
									Settings.VisibilityMode.values())]);
					}

					panel.getInventory().setItem(slot, this.createButton(button).getItem());

					return true;
				};
				glow = false;
				break;
			}
			case LORE_LENGTH:
			{
				description = new ArrayList<>(2);
				description.add(this.user.getTranslation("biomes.gui.descriptions.admin.line-length"));
				description.add(this.user.getTranslation(CURRENT_VALUE,
					VALUE_TAG, Integer.toString(this.config.getLoreLineLength())));
				name = this.user.getTranslation("biomes.gui.buttons.admin.line-length");
				icon = new ItemStack(Material.ANVIL);
				clickHandler = (panel, user1, clickType, i) -> {
					new NumberGUI(this.user,
						this.config.getLoreLineLength(),
						0,
						this.config.getLoreLineLength(),
						(status, value) -> {
							if (status)
							{
								this.config.setLoreLineLength(value);
							}

							EditSettingsGUI.this.build();
						});

					return true;
				};
				glow = false;
				break;
			}
			case LORE_MESSAGE:
			{
				description = new ArrayList<>(2);
				description.add(this.user.getTranslation("biomes.gui.descriptions.admin.biomes-lore"));
				name = this.user.getTranslation("biomes.gui.buttons.admin.biomes-lore");
				icon = new ItemStack(Material.MAP);
				clickHandler = (panel, user1, clickType, i) -> {

					EditLoreGUI.open(this);

					return true;
				};
				glow = false;
				break;
			}
			case COOLDOWN:
			{
				description = new ArrayList<>(2);
				description.add(this.user.getTranslation("biomes.gui.descriptions.admin.cooldown"));
				description.add(this.user.getTranslation(CURRENT_VALUE,
					VALUE_TAG, Integer.toString(this.config.getCoolDown())));
				name = this.user.getTranslation("biomes.gui.buttons.admin.cooldown");
				icon = new ItemStack(Material.DAYLIGHT_DETECTOR);
				clickHandler = (panel, user1, clickType, i) -> {
					new NumberGUI(this.user,
						this.config.getCoolDown(),
						-1,
						this.config.getLoreLineLength(),
						(status, value) -> {
							if (status)
							{
								this.config.setCoolDown(value);
							}

							EditSettingsGUI.this.build();
						});

					return true;
				};
				glow = false;
				break;
			}
			case RESET:
			{
				description = new ArrayList<>(2);
				description.add(this.user.getTranslation("biomes.gui.descriptions.admin.reset-biomes"));
				description.add(this.user.getTranslation(CURRENT_VALUE,
					VALUE_TAG,
					this.config.isResetBiomes() ?
						this.user.getTranslation(ENABLED_REF) :
						this.user.getTranslation(DISABLED_REF)));
				name = this.user.getTranslation("biomes.gui.buttons.admin.reset-biomes");
				icon = new ItemStack(Material.DROPPER);

				clickHandler = (panel, user1, clickType, i) -> {
					this.config.setResetBiomes(
						!this.config.isResetBiomes());

					panel.getInventory().setItem(i, this.createButton(button).getItem());
					return true;
				};

				glow = this.config.isResetBiomes();
				break;
			}
			case PROTECTION_RANGE:
			{
				description = new ArrayList<>(2);
				description.add(this.user.getTranslation("biomes.gui.descriptions.admin.use-protection-range"));
				description.add(this.user.getTranslation(CURRENT_VALUE,
					VALUE_TAG,
					this.config.isUseProtectionRange() ?
						this.user.getTranslation(ENABLED_REF) :
						this.user.getTranslation(DISABLED_REF)));
				name = this.user.getTranslation("biomes.gui.buttons.admin.use-protection-range");
				icon = new ItemStack(Material.FILLED_MAP);

				clickHandler = (panel, user1, clickType, i) -> {
					this.config.setUseProtectionRange(
						!this.config.isUseProtectionRange());

					panel.getInventory().setItem(i, this.createButton(button).getItem());
					return true;
				};

				glow = this.config.isUseProtectionRange();
				break;
			}
			default:
			{
				return new PanelItemBuilder().build();
			}
		}

		return new PanelItemBuilder().
			icon(icon).
			name(name).
			description(GuiUtils.stringSplit(description, this.config.getLoreLineLength())).
			glow(glow).
			clickHandler(clickHandler).
			build();
	}


	/**
	 * Method iterates through given list in descending order
	 * @param mode Current object
	 * @param values Object array
	 * @return Previous object
	 */
	private int getPreviousMode(Object mode, Object[] values)
	{
		for (int i = 0; i < values.length; i++)
		{
			if (values[i].equals(mode))
			{
				if (i > 0)
				{
					return i - 1;
				}
				else
				{
					return values.length - 1;
				}
			}
		}

		return 0;
	}


	/**
	 * Method iterates through given list in ascending order
	 * @param mode Current object
	 * @param values Object array
	 * @return Next object
	 */
	private int getNextMode(Object mode, Object[] values)
	{
		for (int i = 0; i < values.length; i++)
		{
			if (values[i].equals(mode))
			{
				if (i + 1 == values.length)
				{
					return 0;
				}
				else
				{
					return i + 1;
				}
			}
		}

		return 0;
	}


	// ---------------------------------------------------------------------
	// Section: Variables
	// ---------------------------------------------------------------------


	private enum Button
	{
		ADVANCED_MENU,
		DEFAULT_MODE,
		DEFAULT_SIZE,
		VISIBILITY,
		LORE_LENGTH,
		LORE_MESSAGE,
		COOLDOWN,
		PROTECTION_RANGE,
		RESET
	}


	private Settings config;
}
