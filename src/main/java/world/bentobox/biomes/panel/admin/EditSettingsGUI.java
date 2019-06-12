package world.bentobox.biomes.panel.admin;


import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import net.wesjd.anvilgui.AnvilGUI;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.biomes.BiomesAddon;
import world.bentobox.biomes.config.Settings;
import world.bentobox.biomes.panel.CommonGUI;
import world.bentobox.biomes.panel.util.NumberGUI;
import world.bentobox.biomes.utils.GuiUtils;


public class EditSettingsGUI extends CommonGUI
{
	/**
	 * {@inheritDoc}
	 */
	public EditSettingsGUI(@NonNull CommonGUI parentGUI)
	{
		super(parentGUI);
		this.settings = this.addon.getSettings();
	}


	public EditSettingsGUI(BiomesAddon addon,
		World world,
		User user,
		String topLabel,
		String permissionPrefix)
	{
		super(addon, world, user, topLabel, permissionPrefix);
		this.settings = this.addon.getSettings();
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
				description.add(this.user.getTranslation("biomes.gui.descriptions.current-value",
					"[value]",
					this.settings.isAdvancedMenu() ?
						this.user.getTranslation("biomes.gui.descriptions.enabled") :
						this.user.getTranslation("biomes.gui.descriptions.disabled")));
				name = this.user.getTranslation("biomes.gui.buttons.admin.advanced-menu");
				icon = new ItemStack(Material.COMMAND_BLOCK);

				clickHandler = (panel, user1, clickType, i) -> {
					this.settings.setAdvancedMenu(
						!this.settings.isAdvancedMenu());

					panel.getInventory().setItem(i, this.createButton(button).getItem());
					return true;
				};

				glow = this.settings.isAdvancedMenu();
				break;
			}
			case DEFAULT_MODE:
			{
				name = this.user.getTranslation("biomes.gui.buttons.admin.default-mode");

				List<String> values = new ArrayList<>(5);
				values.add(this.user.getTranslation("biomes.gui.descriptions.admin.default-mode"));

				values.add((this.settings.getDefaultMode().equals(Settings.UpdateMode.ISLAND) ? "&2" : "&c") +
					this.user.getTranslation("biomes.gui.descriptions.update-mode.island"));
				values.add((this.settings.getDefaultMode().equals(Settings.UpdateMode.CHUNK) ? "&2" : "&c") +
					this.user.getTranslation("biomes.gui.descriptions.update-mode.chunk"));
				values.add((this.settings.getDefaultMode().equals(Settings.UpdateMode.RANGE) ? "&2" : "&c") +
					this.user.getTranslation("biomes.gui.descriptions.update-mode.square"));
				description = values;

				if (this.settings.getDefaultMode().equals(Settings.UpdateMode.ISLAND))
				{
					icon = new ItemStack(Material.GRASS_BLOCK);
				}
				else if (this.settings.getDefaultMode().equals(Settings.UpdateMode.CHUNK))
				{
					icon = new ItemStack(Material.DIRT);
				}
				else if (this.settings.getDefaultMode().equals(Settings.UpdateMode.RANGE))
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
						this.settings.setDefaultMode(
							Settings.UpdateMode.values()[
								this.getPreviousMode(this.settings.getDefaultMode(),
									Settings.UpdateMode.values())]);
					}
					else
					{
						this.settings.setDefaultMode(
							Settings.UpdateMode.values()[
								this.getNextMode(this.settings.getDefaultMode(),
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
				description.add(this.user.getTranslation("biomes.gui.descriptions.current-value",
					"[value]", Integer.toString(this.settings.getDefaultSize())));
				name = this.user.getTranslation("biomes.gui.buttons.admin.default-size");
				icon = new ItemStack(Material.PISTON);
				clickHandler = (panel, user1, clickType, i) -> {
					new NumberGUI(this.user,
						this.settings.getDefaultSize(),
						0,
						this.settings.getLoreLineLength(),
						(status, value) -> {
							if (status)
							{
								this.settings.setDefaultSize(value);
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

				values.add((this.settings.getVisibilityMode().equals(Settings.VisibilityMode.ALL) ? "&2" : "&c") +
					this.user.getTranslation("biomes.gui.descriptions.visibility-mode.all"));
				values.add((this.settings.getVisibilityMode().equals(Settings.VisibilityMode.DEPLOYED) ? "&2" : "&c") +
					this.user.getTranslation("biomes.gui.descriptions.visibility-mode.deployed"));
				values.add((this.settings.getVisibilityMode().equals(Settings.VisibilityMode.ACCESSIBLE) ? "&2" : "&c") +
					this.user.getTranslation("biomes.gui.descriptions.visibility-mode.accessible"));
				values.add((this.settings.getVisibilityMode().equals(Settings.VisibilityMode.TOGGLEABLE) ? "&2" : "&c") +
					this.user.getTranslation("biomes.gui.descriptions.visibility-mode.toggleable"));
				description = values;

				if (this.settings.getVisibilityMode().equals(Settings.VisibilityMode.ALL))
				{
					icon = new ItemStack(Material.BIRCH_PLANKS);
				}
				else if (this.settings.getVisibilityMode().equals(Settings.VisibilityMode.DEPLOYED))
				{
					icon = new ItemStack(Material.BIRCH_STAIRS);
				}
				else if (this.settings.getVisibilityMode().equals(Settings.VisibilityMode.ACCESSIBLE))
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
						this.settings.setVisibilityMode(
							Settings.VisibilityMode.values()[
								this.getPreviousMode(this.settings.getVisibilityMode(),
									Settings.VisibilityMode.values())]);
					}
					else
					{
						this.settings.setVisibilityMode(
							Settings.VisibilityMode.values()[
								this.getNextMode(this.settings.getVisibilityMode(),
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
				description.add(this.user.getTranslation("biomes.gui.descriptions.current-value",
					"[value]", Integer.toString(this.settings.getLoreLineLength())));
				name = this.user.getTranslation("biomes.gui.buttons.admin.line-length");
				icon = new ItemStack(Material.ANVIL);
				clickHandler = (panel, user1, clickType, i) -> {
					new NumberGUI(this.user,
						this.settings.getLoreLineLength(),
						0,
						this.settings.getLoreLineLength(),
						(status, value) -> {
							if (status)
							{
								this.settings.setLoreLineLength(value);
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
				description.add(this.user.getTranslation("biomes.gui.descriptions.current-value",
					"[value]", this.settings.getLoreMessage()));
				name = this.user.getTranslation("biomes.gui.buttons.admin.biomes-lore");
				icon = new ItemStack(Material.MAP);
				clickHandler = (panel, user1, clickType, i) -> {
					new AnvilGUI(this.addon.getPlugin(),
						this.user.getPlayer(),
						this.settings.getLoreMessage(),
						(player, reply) -> {
							this.settings.setLoreMessage(reply);
							EditSettingsGUI.this.build();
							return reply;
						});

					return true;
				};
				glow = false;
				break;
			}
			case COOLDOWN:
			{
				description = new ArrayList<>(2);
				description.add(this.user.getTranslation("biomes.gui.descriptions.admin.cooldown"));
				description.add(this.user.getTranslation("biomes.gui.descriptions.current-value",
					"[value]", Integer.toString(this.settings.getCoolDown())));
				name = this.user.getTranslation("biomes.gui.buttons.admin.cooldown");
				icon = new ItemStack(Material.DAYLIGHT_DETECTOR);
				clickHandler = (panel, user1, clickType, i) -> {
					new NumberGUI(this.user,
						this.settings.getCoolDown(),
						-1,
						this.settings.getLoreLineLength(),
						(status, value) -> {
							if (status)
							{
								this.settings.setCoolDown(value);
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
				description.add(this.user.getTranslation("biomes.gui.descriptions.current-value",
					"[value]",
					this.settings.isResetBiomes() ?
						this.user.getTranslation("biomes.gui.descriptions.enabled") :
						this.user.getTranslation("biomes.gui.descriptions.disabled")));
				name = this.user.getTranslation("biomes.gui.buttons.admin.reset-biomes");
				icon = new ItemStack(Material.DROPPER);

				clickHandler = (panel, user1, clickType, i) -> {
					this.settings.setResetBiomes(
						!this.settings.isResetBiomes());

					panel.getInventory().setItem(i, this.createButton(button).getItem());
					return true;
				};

				glow = this.settings.isResetBiomes();
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
			description(GuiUtils.stringSplit(description, this.settings.getLoreLineLength())).
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
		RESET
	}


	private Settings settings;
}
