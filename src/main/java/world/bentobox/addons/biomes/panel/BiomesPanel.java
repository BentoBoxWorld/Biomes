package world.bentobox.addons.biomes.panel;


import org.bukkit.*;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import world.bentobox.addons.biomes.BiomesAddon;
import world.bentobox.addons.biomes.BiomesAddonManager;
import world.bentobox.addons.biomes.objects.BiomesObject;
import world.bentobox.addons.biomes.tasks.BiomeUpdateHelper;
import world.bentobox.addons.biomes.utils.Utils;
import world.bentobox.addons.biomes.utils.Utils.UpdateMode;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;

/**
 * This class implements Biomes Panel for all users.
 */
public class BiomesPanel
{
	public BiomesPanel(BiomesAddon addon,
		User player,
		User targetUser,
		World world,
		String permissionPrefix,
		Mode workingMode)
	{
		this.addon = addon;
		this.biomesManager = addon.getAddonManager();
		this.player = player;
		this.targetUser = targetUser;
		this.world = world;
		this.permissionPrefix = permissionPrefix;
		this.workingMode = workingMode;

		switch (workingMode)
		{
			case ADMIN:
				this.panelTitle = this.player.getTranslation("biomes.admin.gui-title");
				PANEL_MAX_SIZE = 18;
				break;
			case EDIT:
				this.panelTitle = this.player.getTranslation("biomes.admin.edit-gui-title");
				PANEL_MAX_SIZE = 36;
				break;
			case PLAYER:
				this.panelTitle = this.player.getTranslation("biomes.gui.title");
				PANEL_MAX_SIZE = 18;
				break;
			default:
				PANEL_MAX_SIZE = 18;
				this.panelTitle = "";
				break;
		}

		this.updateMode = Utils.parseStringToUpdateMode(this.addon.getConfig().getString("defaulttype", ""));
		this.updateNumber = this.addon.getConfig().getInt("defaultsize", 3);

		this.createBiomesPanel(0);
	}


// ---------------------------------------------------------------------
// Section: Panel creating methods
// ---------------------------------------------------------------------


	/**
	 * This method creates Biomes Panel with elements that should be in page by given index.
	 * @param pageIndex Page index.
	 */
	private void createBiomesPanel(int pageIndex)
	{
		// normalize updatenumber
		if (this.updateNumber > 200)
		{
			this.updateNumber = 200;
		}
		else if (this.updateNumber < 0)
		{
			this.updateNumber = 0;
		}

		List<BiomesObject> biomes = this.biomesManager.getBiomes();

		final int biomeCount = biomes.size();

		if (pageIndex < 0)
		{
			pageIndex = 0;
		}
		else if (pageIndex > (biomeCount / PANEL_MAX_SIZE))
		{
			pageIndex = biomeCount / PANEL_MAX_SIZE;
		}

		// Add page index only when necessary.
		String indexString = biomeCount > PANEL_MAX_SIZE ? " " + String.valueOf(pageIndex + 1) : "";

		PanelBuilder panelBuilder = new PanelBuilder().user(this.player).name(this.panelTitle + indexString);

		int itemIndex = pageIndex * PANEL_MAX_SIZE;

		while (itemIndex < (pageIndex * PANEL_MAX_SIZE + PANEL_MAX_SIZE) &&
			itemIndex < biomes.size())
		{
			panelBuilder.item(this.createBiomeButton(pageIndex, biomes.get(itemIndex)));
			itemIndex++;
		}

		if (this.workingMode != Mode.EDIT &&
			this.addon.getConfig().getBoolean("advancedmenu", false))
		{
			// Create advanced menu.

			// Add Type Buttons
			panelBuilder.item(PANEL_MAX_SIZE + 2,
				this.createAdvancedButton(AdvancedButtons.ISLAND, pageIndex));
			panelBuilder.item(PANEL_MAX_SIZE + 9 + 2,
				this.createAdvancedButton(AdvancedButtons.CHUNK, pageIndex));
			panelBuilder.item(PANEL_MAX_SIZE + 18 + 2,
				this.createAdvancedButton(AdvancedButtons.SQUARE, pageIndex));

			// Add counter
			panelBuilder.item(PANEL_MAX_SIZE + 9 + 3,
				this.createAdvancedButton(AdvancedButtons.COUNTER, pageIndex));

			// Add Setters
			panelBuilder.item(PANEL_MAX_SIZE + 4,
				this.createAdvancedButton(AdvancedButtons.ONE, pageIndex));
			panelBuilder.item(PANEL_MAX_SIZE + 9 + 4,
				this.createAdvancedButton(AdvancedButtons.FIVE, pageIndex));
			panelBuilder.item(PANEL_MAX_SIZE + 18 + 4,
				this.createAdvancedButton(AdvancedButtons.TEN, pageIndex));

			// Add increments
			panelBuilder.item(PANEL_MAX_SIZE + 5,
				this.createAdvancedButton(AdvancedButtons.PLUS_ONE, pageIndex));
			panelBuilder.item(PANEL_MAX_SIZE + 9 + 5,
				this.createAdvancedButton(AdvancedButtons.PLUS_THREE, pageIndex));
			panelBuilder.item(PANEL_MAX_SIZE + 18 + 5,
				this.createAdvancedButton(AdvancedButtons.PLUS_FIVE, pageIndex));

			// Add decrements
			panelBuilder.item(PANEL_MAX_SIZE + 6,
				this.createAdvancedButton(AdvancedButtons.MINUS_ONE, pageIndex));
			panelBuilder.item(PANEL_MAX_SIZE + 9 + 6,
				this.createAdvancedButton(AdvancedButtons.MINUS_THREE, pageIndex));
			panelBuilder.item(PANEL_MAX_SIZE + 18 + 6,
				this.createAdvancedButton(AdvancedButtons.MINUS_FIVE, pageIndex));

			if (itemIndex < biomes.size())
			{
				// Next
				panelBuilder.item(PANEL_MAX_SIZE + 8 + 9,
					this.createAdvancedButton(AdvancedButtons.NEXT, pageIndex + 1));
			}

			if (itemIndex > PANEL_MAX_SIZE)
			{
				// Previous
				panelBuilder.item(PANEL_MAX_SIZE + 9,
					this.createAdvancedButton(AdvancedButtons.PREVIOUS, pageIndex - 1));
			}
		}
		else
		{
			// Add next and previous buttons in new line.

			if (itemIndex < biomes.size())
			{
				// Next
				panelBuilder.item(PANEL_MAX_SIZE + 8,
					this.createAdvancedButton(AdvancedButtons.NEXT, pageIndex + 1));
			}

			if (itemIndex > PANEL_MAX_SIZE)
			{
				// Previous
				panelBuilder.item(PANEL_MAX_SIZE,
					this.createAdvancedButton(AdvancedButtons.PREVIOUS, pageIndex - 1));
			}
		}

		panelBuilder.build();
	}


	/**
	 * This method creates menu buttons for biome changing menu.
	 * @param button Button type that must be created.
	 * @param pageIndex Page index.
	 * @return new panel item button.
	 */
	private PanelItem createAdvancedButton(AdvancedButtons button, int pageIndex)
	{
		return this.createAdvancedButton(button, pageIndex, null, false, false);
	}


	/**
	 * This method creates menu buttons for biome editing menu.
	 * @param button Button type that must be created.
	 * @param biome Biome that is changed
	 * @param glowLevel Change level.
	 * @param glowCost Change Cost.
	 * @return New panel item button.
	 */
	private PanelItem createAdvancedButton(AdvancedButtons button,
		BiomesObject biome,
		boolean glowLevel,
		boolean glowCost)
	{
		return this.createAdvancedButton(button, 0, biome, glowLevel, glowCost);
	}


	/**
	 * This is general advanced button creation. It creates button for biome changing menu or biome editing
	 * menu,
	 * @param button Button type that must be created.
	 * @param pageIndex Page index.
	 * @param biome Biome that is changed
	 * @param glowLevel Change level.
	 * @param glowCost Change Cost.
	 * @return New panel item button.
	 */
	private PanelItem createAdvancedButton(AdvancedButtons button,
		int pageIndex,
		BiomesObject biome,
		boolean glowLevel,
		boolean glowCost)
	{
		PanelItem item;

		// TODO: Need to cleanup this mess. It looks ugly!

		switch (button)
		{
			case ISLAND:
				item = new PanelItemBuilder().
					name(this.player.getTranslation("biomes.gui.buttons.island")).
					icon(new ItemStack(Material.GRASS_BLOCK)).
					clickHandler((panel, clicker, click, slot) -> {
						this.updateMode = UpdateMode.ISLAND;
						this.createBiomesPanel(pageIndex);
						return true;
					}).glow(this.updateMode == UpdateMode.ISLAND).
					build();
				break;
			case CHUNK:
				item = new PanelItemBuilder().
					name(this.player.getTranslation("biomes.gui.buttons.chunk")).
					icon(new ItemStack(Material.DIRT)).
					clickHandler((panel, clicker, click, slot) -> {
						this.updateMode = UpdateMode.CHUNK;
						this.createBiomesPanel(pageIndex);
						return true;
					}).glow(this.updateMode == UpdateMode.CHUNK).
					build();
				break;
			case SQUARE:
				item = new PanelItemBuilder().
					name(this.player.getTranslation("biomes.gui.buttons.region")).
					icon(new ItemStack(Material.GLASS)).
					clickHandler((panel, clicker, click, slot) -> {
						this.updateMode = UpdateMode.SQUARE;
						this.createBiomesPanel(pageIndex);
						return true;
					}).glow(this.updateMode == UpdateMode.SQUARE).
					build();
				break;

			case COUNTER:
				item = new PanelItemBuilder().
					name(this.player.getTranslation("biomes.gui.buttons.counter",
						TextVariables.NUMBER,
						Integer.toString(this.updateNumber))).
					icon(new ItemStack(Material.PAPER)).
					glow(true).
					build();
				break;

			case ONE:
				item = new PanelItemBuilder().
					name(this.player.getTranslation("biomes.gui.buttons.set", TextVariables.NUMBER, "1")).
					icon(new ItemStack(Material.BLUE_STAINED_GLASS)).
					clickHandler((panel, clicker, click, slot) -> {
						this.updateNumber = 1;

						if (this.workingMode == Mode.EDIT)
						{
							this.createBiomeEditPanel(pageIndex, biome, glowLevel, glowCost);
						}
						else
						{
							this.createBiomesPanel(pageIndex);
						}

						return true;
					}).build();
				break;
			case FIVE:
				item = new PanelItemBuilder().
					name(this.player.getTranslation("biomes.gui.buttons.set", TextVariables.NUMBER, "5")).
					icon(new ItemStack(Material.BLUE_STAINED_GLASS)).
					clickHandler((panel, clicker, click, slot) -> {
						this.updateNumber = 5;

						if (this.workingMode == Mode.EDIT)
						{
							this.createBiomeEditPanel(pageIndex, biome, glowLevel, glowCost);
						}
						else
						{
							this.createBiomesPanel(pageIndex);
						}

						return true;
					}).build();
				break;
			case TEN:
				item = new PanelItemBuilder().
					name(this.player.getTranslation("biomes.gui.buttons.set", TextVariables.NUMBER, "10")).
					icon(new ItemStack(Material.BLUE_STAINED_GLASS)).
					clickHandler((panel, clicker, click, slot) -> {
						this.updateNumber = 10;

						if (this.workingMode == Mode.EDIT)
						{
							this.createBiomeEditPanel(pageIndex, biome, glowLevel, glowCost);
						}
						else
						{
							this.createBiomesPanel(pageIndex);
						}

						return true;
					}).build();
				break;
			case HUNDRED:
				item = new PanelItemBuilder().
					name(this.player.getTranslation("biomes.gui.buttons.set", TextVariables.NUMBER, "100")).
					icon(new ItemStack(Material.BLUE_STAINED_GLASS)).
					clickHandler((panel, clicker, click, slot) -> {
						this.updateNumber = 100;

						if (this.workingMode == Mode.EDIT)
						{
							this.createBiomeEditPanel(pageIndex, biome, glowLevel, glowCost);
						}
						else
						{
							this.createBiomesPanel(pageIndex);
						}

						return true;
					}).build();
				break;
			case PLUS_ONE:
				item = new PanelItemBuilder().
					name(this.player.getTranslation("biomes.gui.buttons.increase", TextVariables.NUMBER, "1")).
					icon(new ItemStack(Material.GREEN_STAINED_GLASS_PANE)).
					clickHandler((panel, clicker, click, slot) -> {
						this.updateNumber += 1;

						if (this.workingMode == Mode.EDIT)
						{
							this.createBiomeEditPanel(pageIndex, biome, glowLevel, glowCost);
						}
						else
						{
							this.createBiomesPanel(pageIndex);
						}

						return true;
					}).build();
				break;
			case PLUS_THREE:
				item = new PanelItemBuilder().
					name(this.player.getTranslation("biomes.gui.buttons.increase", TextVariables.NUMBER, "3")).
					icon(new ItemStack(Material.GREEN_STAINED_GLASS_PANE)).
					clickHandler((panel, clicker, click, slot) -> {
						this.updateNumber += 3;

						if (this.workingMode == Mode.EDIT)
						{
							this.createBiomeEditPanel(pageIndex, biome, glowLevel, glowCost);
						}
						else
						{
							this.createBiomesPanel(pageIndex);
						}

						return true;
					}).build();
				break;
			case PLUS_FIVE:
				item = new PanelItemBuilder().
					name(this.player.getTranslation("biomes.gui.buttons.increase", TextVariables.NUMBER, "5")).
					icon(new ItemStack(Material.GREEN_STAINED_GLASS_PANE)).
					clickHandler((panel, clicker, click, slot) -> {
						this.updateNumber += 5;

						if (this.workingMode == Mode.EDIT)
						{
							this.createBiomeEditPanel(pageIndex, biome, glowLevel, glowCost);
						}
						else
						{
							this.createBiomesPanel(pageIndex);
						}

						return true;
					}).build();
				break;
			case PLUS_FIFTY:
				item = new PanelItemBuilder().
					name(this.player.getTranslation("biomes.gui.buttons.increase", TextVariables.NUMBER, "50")).
					icon(new ItemStack(Material.GREEN_STAINED_GLASS_PANE)).
					clickHandler((panel, clicker, click, slot) -> {
						this.updateNumber += 50;

						if (this.workingMode == Mode.EDIT)
						{
							this.createBiomeEditPanel(pageIndex, biome, glowLevel, glowCost);
						}
						else
						{
							this.createBiomesPanel(pageIndex);
						}

						return true;
					}).build();
				break;
			case PLUS_HUNDRED:
				item = new PanelItemBuilder().
					name(this.player.getTranslation("biomes.gui.buttons.increase", TextVariables.NUMBER, "100")).
					icon(new ItemStack(Material.GREEN_STAINED_GLASS_PANE)).
					clickHandler((panel, clicker, click, slot) -> {
						this.updateNumber += 100;

						if (this.workingMode == Mode.EDIT)
						{
							this.createBiomeEditPanel(pageIndex, biome, glowLevel, glowCost);
						}
						else
						{
							this.createBiomesPanel(pageIndex);
						}

						return true;
					}).build();
				break;
			case MINUS_ONE:
				item = new PanelItemBuilder().
					name(this.player.getTranslation("biomes.gui.buttons.reduce", TextVariables.NUMBER, "1")).
					icon(new ItemStack(Material.RED_STAINED_GLASS_PANE)).
					clickHandler((panel, clicker, click, slot) -> {
						this.updateNumber -= 1;

						if (this.workingMode == Mode.EDIT)
						{
							this.createBiomeEditPanel(pageIndex, biome, glowLevel, glowCost);
						}
						else
						{
							this.createBiomesPanel(pageIndex);
						}

						return true;
					}).build();
				break;
			case MINUS_THREE:
				item = new PanelItemBuilder().
					name(this.player.getTranslation("biomes.gui.buttons.reduce", TextVariables.NUMBER, "3")).
					icon(new ItemStack(Material.RED_STAINED_GLASS_PANE)).
					clickHandler((panel, clicker, click, slot) -> {
						this.updateNumber -= 3;

						if (this.workingMode == Mode.EDIT)
						{
							this.createBiomeEditPanel(pageIndex, biome, glowLevel, glowCost);
						}
						else
						{
							this.createBiomesPanel(pageIndex);
						}

						return true;
					}).build();
				break;
			case MINUS_FIVE:
				item = new PanelItemBuilder().
					name(this.player.getTranslation("biomes.gui.buttons.reduce", TextVariables.NUMBER, "5")).
					icon(new ItemStack(Material.RED_STAINED_GLASS_PANE)).
					clickHandler((panel, clicker, click, slot) -> {
						this.updateNumber -= 5;

						if (this.workingMode == Mode.EDIT)
						{
							this.createBiomeEditPanel(pageIndex, biome, glowLevel, glowCost);
						}
						else
						{
							this.createBiomesPanel(pageIndex);
						}

						return true;
					}).build();
				break;
			case MINUS_FIFTY:
				item = new PanelItemBuilder().
					name(this.player.getTranslation("biomes.gui.buttons.reduce", TextVariables.NUMBER, "50")).
					icon(new ItemStack(Material.RED_STAINED_GLASS_PANE)).
					clickHandler((panel, clicker, click, slot) -> {
						this.updateNumber -= 50;

						if (this.workingMode == Mode.EDIT)
						{
							this.createBiomeEditPanel(pageIndex, biome, glowLevel, glowCost);
						}
						else
						{
							this.createBiomesPanel(pageIndex);
						}

						return true;
					}).build();
				break;
			case MINUS_HUNDRED:
				item = new PanelItemBuilder().
					name(this.player.getTranslation("biomes.gui.buttons.reduce", TextVariables.NUMBER, "100")).
					icon(new ItemStack(Material.RED_STAINED_GLASS_PANE)).
					clickHandler((panel, clicker, click, slot) -> {
						this.updateNumber -= 100;

						if (this.workingMode == Mode.EDIT)
						{
							this.createBiomeEditPanel(pageIndex, biome, glowLevel, glowCost);
						}
						else
						{
							this.createBiomesPanel(pageIndex);
						}

						return true;
					}).build();
				break;
			case NEXT:
				item = new PanelItemBuilder().
					name(this.player.getTranslation("biomes.gui.buttons.next")).
					icon(new ItemStack(Material.SIGN)).
					clickHandler((panel, clicker, click, slot) -> {
						this.createBiomesPanel(pageIndex);
						return true;
					}).build();
				break;
			case PREVIOUS:
				item = new PanelItemBuilder().
					name(this.player.getTranslation("biomes.gui.buttons.previous")).
					icon(new ItemStack(Material.SIGN)).
					clickHandler((panel, clicker, click, slot) -> {
						this.createBiomesPanel(pageIndex);
						return true;
					}).build();
				break;
			default:
				item = new PanelItemBuilder().name("").build();
		}

		return item;
	}


	/**
	 * This method creates new icon for each biome object.
	 * @param pageIndex index of page from which button is called.
	 * @param biome BiomeObject that must be added to panel.
	 */
	private PanelItem createBiomeButton(int pageIndex, BiomesObject biome)
	{
		PanelItemBuilder itemBuilder = new PanelItemBuilder().
			icon(biome.getIcon()).
			name(biome.getFriendlyName().isEmpty() ? biome.getUniqueId() : biome.getFriendlyName()).
			description(biome.getDescription());

		if (this.workingMode.equals(Mode.EDIT))
		{
			itemBuilder.clickHandler((panel, player, click, slot) -> {
				if (click.isRightClick())
				{
					biome.setDeployed(!biome.isDeployed());
					this.biomesManager.saveBiome(biome);
					this.createBiomesPanel(pageIndex);
				}
				else
				{
					this.createBiomeEditPanel(pageIndex, biome, false, false);
				}

				return true;
			});

			itemBuilder.glow(biome.isDeployed());
		}
		else
		{
			// Player click
			itemBuilder.clickHandler((panel, player, click, slot) -> {
				if (this.workingMode.equals(Mode.PLAYER) && !biome.isDeployed())
				{
					this.player.sendMessage(this.player.getTranslation("biomes.error.disabled"));
				}
				else
				{
					BiomeUpdateHelper helper = new BiomeUpdateHelper(this.addon,
						this.player,
						this.targetUser,
						biome,
						this.world,
						this.updateMode,
						this.updateNumber,
						this.workingMode.equals(Mode.PLAYER));

					if (helper.canChangeBiome())
					{
						helper.updateIslandBiome();
						this.player.closeInventory();
					}
				}

				return true;
			});

			itemBuilder.glow(!biome.isDeployed());
		}

		return itemBuilder.build();
	}


// ---------------------------------------------------------------------
// Section: Biome Changing related methods
// ---------------------------------------------------------------------


	/**
	 * This method creates menu that allows to change given biome object.
	 * @param pageIndex Index of page form which panel is created.
	 * @param biome BiomeObject that need to be changed.
	 * @param glowLevel Indicate if currently level change is active.
	 * @param glowCost Indicate if currently cost change is active.
	 */
	private void createBiomeEditPanel(int pageIndex, BiomesObject biome, boolean glowLevel, boolean glowCost)
	{
		PanelBuilder panelBuilder = new PanelBuilder().user(this.player).
			name(this.panelTitle + " " + biome.getFriendlyName());

		panelBuilder.item(0, new PanelItemBuilder().
			name(biome.getFriendlyName()).
			icon(Material.BOOK).
			clickHandler((panel, user, clickType, slot) -> {
				// TODO: Implement ability to change friendly name
				// if user add renamed paper via listener.
				// TODO: Implement ability to choose biome from all menu.
				return true;
			}).build());

		panelBuilder.item(9, new PanelItemBuilder().
			name(this.player.getTranslation("biomes.admin.buttons.icon")).
			icon(biome.getIcon()).
			clickHandler((panel, user, clickType, slot) -> {
				// TODO: Implement ability to change icon name if user add item in inventory.
				return true;
			}).build());

		panelBuilder.item(18, new PanelItemBuilder().
			name(Long.toString(biome.getRequiredLevel())).
			description(this.player.getTranslation("biomes.admin.buttons.level")).
			icon(Material.BOOK).
			clickHandler((panel, user, clickType, slot) -> {
				this.updateNumber = (int) biome.getRequiredLevel();

				this.createBiomeEditPanel(pageIndex, biome, true, false);
				return true;
			}).glow(glowLevel).
			build());

		panelBuilder.item(27, new PanelItemBuilder().
			name(Integer.toString(biome.getRequiredCost())).
			description(this.player.getTranslation("biomes.admin.buttons.cost")).
			icon(Material.BOOK).
			clickHandler((panel, user, clickType, slot) -> {
				this.updateNumber = (int) biome.getRequiredCost();
				this.createBiomeEditPanel(pageIndex, biome, false, true);
				return true;
			}).glow(glowCost).
			build());

		if (glowCost || glowLevel)
		{
			// Setters
			panelBuilder.item(3,
				this.createAdvancedButton(AdvancedButtons.ONE, biome, glowLevel, glowCost));
			panelBuilder.item(12,
				this.createAdvancedButton(AdvancedButtons.FIVE, biome, glowLevel, glowCost));
			panelBuilder.item(21,
				this.createAdvancedButton(AdvancedButtons.TEN, biome, glowLevel, glowCost));
			panelBuilder.item(30,
				this.createAdvancedButton(AdvancedButtons.HUNDRED, biome, glowLevel, glowCost));

			// Increasers
			panelBuilder.item(5,
				this.createAdvancedButton(AdvancedButtons.PLUS_ONE, biome, glowLevel, glowCost));
			panelBuilder.item(14,
				this.createAdvancedButton(AdvancedButtons.PLUS_FIVE, biome, glowLevel, glowCost));
			panelBuilder.item(23,
				this.createAdvancedButton(AdvancedButtons.PLUS_FIFTY, biome, glowLevel, glowCost));
			panelBuilder.item(32,
				this.createAdvancedButton(AdvancedButtons.PLUS_HUNDRED, biome, glowLevel, glowCost));

			// Reducers
			panelBuilder.item(6,
				this.createAdvancedButton(AdvancedButtons.MINUS_ONE, biome, glowLevel, glowCost));
			panelBuilder.item(15,
				this.createAdvancedButton(AdvancedButtons.MINUS_FIVE, biome, glowLevel, glowCost));
			panelBuilder.item(24,
				this.createAdvancedButton(AdvancedButtons.MINUS_FIFTY, biome, glowLevel, glowCost));
			panelBuilder.item(33,
				this.createAdvancedButton(AdvancedButtons.MINUS_HUNDRED, biome, glowLevel, glowCost));

			// Savers
			if (glowLevel)
			{
				panelBuilder.item(19, new PanelItemBuilder().
					icon(Material.PAPER).
					name(this.player.getTranslation("biomes.admin.buttons.save")).
					description(this.player.getTranslation("biomes.admin.change-value",
						TextVariables.NUMBER,
						Integer.toString(this.updateNumber))).
					clickHandler((panel, user, clickType, slot) -> {
						biome.setRequiredLevel(this.updateNumber);
						this.biomesManager.saveBiome(biome);
						this.createBiomeEditPanel(pageIndex, biome, false, false);
						user.sendMessage("biomes.admin.saved");
						return true;
					}).build());
			}
			else
			{
				panelBuilder.item(28, new PanelItemBuilder().
					icon(Material.PAPER).
					name(this.player.getTranslation("biomes.admin.buttons.save")).
					description(this.player.getTranslation("biomes.admin.change-value",
						TextVariables.NUMBER,
						Integer.toString(this.updateNumber))).
					clickHandler((panel, user, clickType, slot) -> {
						biome.setRequiredCost(this.updateNumber);
						this.biomesManager.saveBiome(biome);
						this.createBiomeEditPanel(pageIndex, biome, false, false);
						user.sendMessage("challenges.admin.saved");
						return true;
					}).build());
			}
		}

		// back button
		panelBuilder.item(1, new PanelItemBuilder().
			name(this.player.getTranslation("biomes.gui.buttons.back")).
			icon(Material.IRON_DOOR).
			clickHandler((panel, user, clickType, slot) -> {
				this.createBiomesPanel(pageIndex);
				return true;
			}).build());

		panelBuilder.build();
	}


// ---------------------------------------------------------------------
// Section: Enums
// ---------------------------------------------------------------------


	/**
	 * This enum describes all possible panel creation modes.
	 */
	public enum Mode
	{
		ADMIN,
		EDIT,
		PLAYER
	}


	/**
	 * This enum describes all advanced buttons.
	 */
	private enum AdvancedButtons
	{
		ISLAND,
		CHUNK,
		SQUARE,
		COUNTER,
		ONE,
		FIVE,
		TEN,
		HUNDRED,
		PLUS_ONE,
		PLUS_THREE,
		PLUS_FIVE,
		PLUS_FIFTY,
		PLUS_HUNDRED,
		MINUS_ONE,
		MINUS_THREE,
		MINUS_FIVE,
		MINUS_FIFTY,
		MINUS_HUNDRED,
		NEXT,
		PREVIOUS
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

	/**
	 * This variable stores current biomes addon.
	 */
	private BiomesAddon addon;

	/**
	 * This variable stores biomes addon manager.
	 */
	private BiomesAddonManager biomesManager;

	/**
	 * This variable stores user who calls panel creation.
	 */
	private User player;

	/**
	 * This variable stores user who is targeted by current panel.
	 */
	private User targetUser;

	/**
	 * This variable stores world in which necessary changes will be done.
	 */
	private World world;

	/**
	 * This variable stores user permissions.
	 */
	private String permissionPrefix;

	/**
	 * This variable stores current panel name.
	 */
	private String panelTitle;

	/**
	 * This variable stores current panel working mode.
	 */
	private Mode workingMode;

	/**
	 * This variable stores current panel update mode.
	 */
	private UpdateMode updateMode;

	/**
	 * This variable stores current update distance for SQUARE mode or chunk count for
	 * CHUNK mode.
	 */
	private int updateNumber;

	/**
	 * This variable stores maximal panel size.
	 */
	private static int PANEL_MAX_SIZE;
}
