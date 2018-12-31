package world.bentobox.addons.biomes.panel;


import org.bukkit.Material;
import org.bukkit.World;

import world.bentobox.addons.biomes.BiomesAddon;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;


/**
 * This class contains everything that is necessary for changing Biomes Addon settings via
 * GUI.
 */
public class AdminSettingsPanel
{
	/**
	 * Default constructor that will create new Panel with addon settings.
	 * @param addon BiomeAddon.
	 * @param world World form which command is called.
	 * @param user User who calls command.
	 */
	protected AdminSettingsPanel(BiomesAddon addon, World world, User user)
	{
		this(addon, world, user, true);
	}


	/**
	 * Default constructor that will create new Panel with addon settings.
	 * @param addon BiomeAddon.
	 * @param world World form which command is called.
	 * @param user User who calls command.
	 * @param fromMainMenu indicate if panel is created from main menu.
	 */
	public AdminSettingsPanel(BiomesAddon addon, World world, User user, boolean fromMainMenu)
	{
		this.addon = addon;
		this.world = world;
		this.user = user;
		this.fromMainMenu = fromMainMenu;
		this.mode = Mode.RETURN;

		this.createSettingPanel();
	}


	/**
	 * This method builds Settings panel.
	 */
	private void createSettingPanel()
	{
		PanelBuilder panelBuilder = new PanelBuilder().user(this.user).name(
			this.user.getTranslation("biomes.gui.admin.settings-title"));

		boolean advancedMenu = this.addon.getConfig().getBoolean("advancedmenu", false);
		String defaultType = this.addon.getConfig().getString("defaulttype", "ISLAND");
		int defaultSize = this.addon.getConfig().getInt("defaultsize", 1);
		int timeout = this.addon.getConfig().getInt("timeout", 1);
		boolean resetBiomes = this.addon.getConfig().getBoolean("resetBiomes", false);

		panelBuilder.item(0, new PanelItemBuilder().
			icon(Material.COMMAND_BLOCK).
			name(this.user.getTranslation("biomes.gui.admin.buttons.advancedmenu",
				"[value]",
				advancedMenu ?
					this.user.getTranslation("biomes.gui.admin.descriptions.enabled") :
					this.user.getTranslation("biomes.gui.admin.descriptions.disabled") )).
			description(this.user.getTranslation("biomes.admin.descriptions.advancedmenu")).
			clickHandler((panel, user1, clickType, slot) -> {
				this.mode = Mode.MENU;
				this.newValue = advancedMenu;
				this.createSettingPanel();
				return true;
			}).glow(this.mode == Mode.MENU).
			build());

		panelBuilder.item(9, new PanelItemBuilder().
			icon(defaultType.equals("ISLAND") ? Material.GRASS_BLOCK :
				defaultType.equals("CHUNK") ? Material.DIRT : Material.GLASS).
			name(this.user.getTranslation("biomes.gui.admin.buttons.type", "[value]", defaultType)).
			description(this.user.getTranslation("biomes.gui.admin.descriptions.type")).
			clickHandler((panel, user1, clickType, slot) -> {
				this.mode = Mode.TYPE;
				this.newValue = defaultType;
				this.createSettingPanel();
				return true;
			}).glow(this.mode == Mode.TYPE).
			build());

		panelBuilder.item(18, new PanelItemBuilder().
			icon(Material.PISTON).
			name(this.user.getTranslation("biomes.gui.admin.buttons.size", "[number]", Integer.toString(defaultSize))).
			description(this.user.getTranslation("biomes.gui.admin.descriptions.size")).
			clickHandler((panel, user1, clickType, slot) -> {
				this.mode = Mode.SIZE;
				this.newValue = defaultSize;
				this.createSettingPanel();
				return true;
			}).glow(this.mode == Mode.SIZE).
			build());

		panelBuilder.item(27, new PanelItemBuilder().
			icon(Material.DROPPER).
			name(this.user.getTranslation("biomes.gui.admin.buttons.resetBiomes",
				"[value]",
				advancedMenu ?
					this.user.getTranslation("biomes.gui.admin.descriptions.enabled") :
					this.user.getTranslation("biomes.gui.admin.descriptions.disabled"))).
			description(this.user.getTranslation("biomes.admin.descriptions.resetBiomes")).
			clickHandler((panel, user1, clickType, slot) -> {
				this.mode = Mode.RESET_BIOME;
				this.newValue = resetBiomes;
				this.createSettingPanel();
				return true;
			}).glow(this.mode == Mode.RESET_BIOME).
			build());

		panelBuilder.item(36, new PanelItemBuilder().
			icon(Material.DAYLIGHT_DETECTOR).
			name(this.user.getTranslation("biomes.gui.admin.buttons.timeout", "[number]", Integer.toString(timeout))).
			description(this.user.getTranslation("biomes.gui.admin.descriptions.timeout")).
			clickHandler((panel, user1, clickType, slot) -> {
				this.mode = Mode.TIMEOUT;
				this.newValue = timeout;
				this.createSettingPanel();
				return true;
			}).glow(this.mode == Mode.TIMEOUT).
			build());

		// Create different buttons for each mode.
		switch (this.mode)
		{
			case RETURN:
				if (this.fromMainMenu)
				{
					panelBuilder.item(8, new PanelItemBuilder().
						icon(Material.OAK_DOOR).
						name(this.user.getTranslation("biomes.gui.buttons.back")).
						clickHandler((panel, user1, clickType, slot) -> {
							new AdminMainPanel(this.addon, this.world, this.user);
							return true;
						}).build());
				}
				break;
			case MENU:
				panelBuilder.item(1, new PanelItemBuilder().
					icon(Material.PAPER).
					name(this.user.getTranslation("biomes.gui.admin.buttons.save")).
					clickHandler((panel, user1, clickType, slot) -> {
						this.mode = Mode.RETURN;
						this.addon.getConfig().set("advancedmenu", this.newValue);
						this.addon.saveConfig();
						this.createSettingPanel();
						return true;
					}).build());
				panelBuilder.item(2, new PanelItemBuilder().
					icon(Material.BARRIER).
					name(this.user.getTranslation("biomes.gui.admin.buttons.cancel")).
					clickHandler((panel, user1, clickType, slot) -> {
						this.mode = Mode.RETURN;
						this.createSettingPanel();
						return true;
					}).build());
				panelBuilder.item(4, new PanelItemBuilder().
					icon(Material.GREEN_CONCRETE).
					name(this.user.getTranslation("biomes.gui.admin.buttons.enabled")).
					clickHandler((panel, user1, clickType, slot) -> {
						this.newValue = true;
						this.createSettingPanel();
						return true;
					}).glow((boolean) this.newValue)
					.build());
				panelBuilder.item(13, new PanelItemBuilder().
					icon(Material.RED_CONCRETE).
					name(this.user.getTranslation("biomes.gui.admin.buttons.disabled")).
					clickHandler((panel, user1, clickType, slot) -> {
						this.newValue = false;
						this.createSettingPanel();
						return true;
					}).glow(!((boolean) this.newValue)).
					build());

				break;
			case TYPE:
				panelBuilder.item(10, new PanelItemBuilder().
					icon(Material.PAPER).
					name(this.user.getTranslation("biomes.gui.admin.buttons.save")).
					clickHandler((panel, user1, clickType, slot) -> {
						this.mode = Mode.RETURN;
						this.addon.getConfig().set("defaulttype", this.newValue);
						this.addon.saveConfig();
						this.createSettingPanel();
						return true;
					}).build());
				panelBuilder.item(11, new PanelItemBuilder().
					icon(Material.BARRIER).
					name(this.user.getTranslation("biomes.gui.admin.buttons.cancel")).
					clickHandler((panel, user1, clickType, slot) -> {
						this.mode = Mode.RETURN;
						this.createSettingPanel();
						return true;
					}).build());
				panelBuilder.item(4, new PanelItemBuilder().
					icon(Material.GRASS_BLOCK).
					name(this.user.getTranslation("biomes.gui.buttons.island")).
					clickHandler((panel, user1, clickType, slot) -> {
						this.newValue = "ISLAND";
						this.createSettingPanel();
						return true;
					}).glow(this.newValue.equals("ISLAND")).
					build());
				panelBuilder.item(13, new PanelItemBuilder().
					icon(Material.DIRT).
					name(this.user.getTranslation("biomes.gui.buttons.chunk")).
					clickHandler((panel, user1, clickType, slot) -> {
						this.newValue = "CHUNK";
						this.createSettingPanel();
						return true;
					}).glow(this.newValue.equals("CHUNK")).
					build());
				panelBuilder.item(22, new PanelItemBuilder().
					icon(Material.GLASS).
					name(this.user.getTranslation("biomes.gui.buttons.region")).
					clickHandler((panel, user1, clickType, slot) -> {
						this.newValue = "SQUARE";
						this.createSettingPanel();
						return true;
					}).glow(this.newValue.equals("SQUARE")).
					build());

				break;
			case SIZE:
				panelBuilder.item(19, new PanelItemBuilder().
					icon(Material.PAPER).
					name(this.user.getTranslation("biomes.gui.admin.buttons.save")).
					clickHandler((panel, user1, clickType, slot) -> {
						this.mode = Mode.RETURN;

						if (((int) this.newValue) < 0)
						{
							this.newValue = 0;
						}

						this.addon.getConfig().set("defaultsize", this.newValue);
						this.addon.saveConfig();
						this.createSettingPanel();
						return true;
					}).build());
				panelBuilder.item(20, new PanelItemBuilder().
					icon(Material.BARRIER).
					name(this.user.getTranslation("biomes.gui.admin.buttons.cancel")).
					clickHandler((panel, user1, clickType, slot) -> {
						this.mode = Mode.RETURN;
						this.createSettingPanel();
						return true;
					}).build());

				this.addNumberButtons(panelBuilder);

				break;
			case RESET_BIOME:
				panelBuilder.item(28, new PanelItemBuilder().
					icon(Material.PAPER).
					name(this.user.getTranslation("biomes.gui.admin.buttons.save")).
					clickHandler((panel, user1, clickType, slot) -> {
						this.mode = Mode.RETURN;
						this.addon.getConfig().set("resetBiomes", this.newValue);
						this.addon.saveConfig();
						this.createSettingPanel();
						return true;
					}).build());
				panelBuilder.item(29, new PanelItemBuilder().
					icon(Material.BARRIER).
					name(this.user.getTranslation("biomes.gui.admin.buttons.cancel")).
					clickHandler((panel, user1, clickType, slot) -> {
						this.mode = Mode.RETURN;
						this.createSettingPanel();
						return true;
					}).build());

				panelBuilder.item(31, new PanelItemBuilder().
					icon(Material.GREEN_CONCRETE).
					name(this.user.getTranslation("biomes.gui.admin.buttons.enabled")).
					clickHandler((panel, user1, clickType, slot) -> {
						this.newValue = true;
						this.createSettingPanel();
						return true;
					}).glow((boolean) this.newValue)
					.build());
				panelBuilder.item(32, new PanelItemBuilder().
					icon(Material.RED_CONCRETE).
					name(this.user.getTranslation("biomes.gui.admin.buttons.disabled")).
					clickHandler((panel, user1, clickType, slot) -> {
						this.newValue = false;
						this.createSettingPanel();
						return true;
					}).glow(!((boolean) this.newValue)).
					build());

				break;
			case TIMEOUT:
				panelBuilder.item(37, new PanelItemBuilder().
					icon(Material.PAPER).
					name(this.user.getTranslation("biomes.gui.admin.buttons.save")).
					clickHandler((panel, user1, clickType, slot) -> {
						this.mode = Mode.RETURN;

						if (((int) this.newValue) < 0)
						{
							this.newValue = 0;
						}

						this.addon.getConfig().set("timeout", this.newValue);
						this.addon.saveConfig();
						this.createSettingPanel();
						return true;
					}).build());
				panelBuilder.item(38, new PanelItemBuilder().
					icon(Material.BARRIER).
					name(this.user.getTranslation("biomes.gui.admin.buttons.cancel")).
					clickHandler((panel, user1, clickType, slot) -> {
						this.mode = Mode.RETURN;
						this.createSettingPanel();
						return true;
					}).build());

				this.addNumberButtons(panelBuilder);
		}

		// Create Reload Config un Reload Lang buttons.

		panelBuilder.build();
	}


	/**
	 * This method adds counter, setters, increments and decrements for given panel.
	 * @param panelBuilder Panel that must be populated with necessary buttons.
	 */
	private void addNumberButtons(PanelBuilder panelBuilder)
	{
		panelBuilder.item(22, new PanelItemBuilder().
			icon(Material.PAPER).
			name(this.user.getTranslation("biomes.gui.buttons.value",
				TextVariables.NUMBER, Integer.toString((int) this.newValue))).
			build());

		// Add Setters
		panelBuilder.item(14, new PanelItemBuilder().
			name(this.user.getTranslation("biomes.gui.buttons.set", TextVariables.NUMBER, "1")).
			icon(Material.BLUE_STAINED_GLASS).
			clickHandler((panel, clicker, click, slot) -> {
				this.newValue = 1;
				this.createSettingPanel();
				return true;
			}).build());
		panelBuilder.item(23, new PanelItemBuilder().
			name(this.user.getTranslation("biomes.gui.buttons.set", TextVariables.NUMBER, "5")).
			icon(Material.BLUE_STAINED_GLASS).
			clickHandler((panel, clicker, click, slot) -> {
				this.newValue = 5;
				this.createSettingPanel();
				return true;
			}).build());
		panelBuilder.item(32, new PanelItemBuilder().
			name(this.user.getTranslation("biomes.gui.buttons.set", TextVariables.NUMBER, "10")).
			icon(Material.BLUE_STAINED_GLASS).
			clickHandler((panel, clicker, click, slot) -> {
				this.newValue = 10;
				this.createSettingPanel();
				return true;
			}).build());

		// Add increments
		panelBuilder.item(15, new PanelItemBuilder().
			name(this.user.getTranslation("biomes.gui.buttons.increase", TextVariables.NUMBER, "1")).
			icon(Material.GREEN_STAINED_GLASS_PANE).
			clickHandler((panel, clicker, click, slot) -> {
				this.newValue = (int) this.newValue + 1;
				this.createSettingPanel();
				return true;
			}).build());
		panelBuilder.item(24, new PanelItemBuilder().
			name(this.user.getTranslation("biomes.gui.buttons.increase", TextVariables.NUMBER, "5")).
			icon(Material.GREEN_STAINED_GLASS_PANE).
			clickHandler((panel, clicker, click, slot) -> {
				this.newValue = (int) this.newValue + 5;
				this.createSettingPanel();
				return true;
			}).build());
		panelBuilder.item(33, new PanelItemBuilder().
			name(this.user.getTranslation("biomes.gui.buttons.increase", TextVariables.NUMBER, "10")).
			icon(Material.GREEN_STAINED_GLASS_PANE).
			clickHandler((panel, clicker, click, slot) -> {
				this.newValue = (int) this.newValue + 10;
				this.createSettingPanel();
				return true;
			}).build());

		// Add decrements
		panelBuilder.item(16, new PanelItemBuilder().
			name(this.user.getTranslation("biomes.gui.buttons.reduce", TextVariables.NUMBER, "1")).
			icon(Material.RED_STAINED_GLASS_PANE).
			clickHandler((panel, clicker, click, slot) -> {
				this.newValue = (int) this.newValue - 1;
				this.createSettingPanel();
				return true;
			}).build());
		panelBuilder.item(25, new PanelItemBuilder().
			name(this.user.getTranslation("biomes.gui.buttons.reduce", TextVariables.NUMBER, "5")).
			icon(Material.RED_STAINED_GLASS_PANE).
			clickHandler((panel, clicker, click, slot) -> {
				this.newValue = (int) this.newValue - 5;
				this.createSettingPanel();
				return true;
			}).build());
		panelBuilder.item(34, new PanelItemBuilder().
			name(this.user.getTranslation("biomes.gui.buttons.reduce", TextVariables.NUMBER, "10")).
			icon(Material.RED_STAINED_GLASS_PANE).
			clickHandler((panel, clicker, click, slot) -> {
				this.newValue = (int) this.newValue - 10;
				this.createSettingPanel();
				return true;
			}).build());
	}



// ---------------------------------------------------------------------
// Section: Enums
// ---------------------------------------------------------------------


	/**
	 * This enum contains all possible variants how Settings Panel can be build.
	 */
	private enum Mode
	{
		RETURN,
		MENU,
		TYPE,
		SIZE,
		TIMEOUT,
		RESET_BIOME
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


	private BiomesAddon addon;

	private User user;

	private World world;

	private Mode mode;

	private Object newValue;

	private boolean fromMainMenu;
}
