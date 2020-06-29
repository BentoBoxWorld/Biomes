package world.bentobox.biomes.panels;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.NonNull;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.biomes.BiomesAddon;
import world.bentobox.biomes.database.objects.BiomesObject;


/**
 * This class contains common methods that will be used over all GUIs. It also allows
 * easier navigation between different GUIs.
 */
public abstract class CommonGUI
{
// ---------------------------------------------------------------------
// Section: Constructors
// ---------------------------------------------------------------------


	/**
	 * Default constructor that inits panels with minimal requirements, without parent panel.
	 *
	 * @param addon Addon where panel operates.
	 * @param world World from which panel was created.
	 * @param user User who created panel.
	 * @param topLabel Command top label which creates panel (f.e. island or ai)
	 * @param permissionPrefix Command permission prefix (f.e. bskyblock.)
	 */
	public CommonGUI(BiomesAddon addon,
		World world,
		User user,
		String topLabel,
		String permissionPrefix)
	{
		this(addon, world, user, topLabel, permissionPrefix, null);
	}


	/**
	 * Default constructor that inits panels with minimal requirements, without parent panel.
	 *
	 * @param parentGUI ParentGUI of current GUI.
	 */
	public CommonGUI(@NonNull CommonGUI parentGUI)
	{
		this(parentGUI.addon,
			parentGUI.world,
			parentGUI.user,
			parentGUI.topLabel,
			parentGUI.permissionPrefix,
			parentGUI);
	}


	/**
	 * Default constructor that inits panels with minimal requirements.
	 *
	 * @param addon Addon where panel operates.
	 * @param world World from which panel was created.
	 * @param user User who created panel.
	 * @param topLabel Command top label which creates panel (f.e. island or ai)
	 * @param permissionPrefix Command permission prefix (f.e. bskyblock.)
	 * @param parentGUI Parent panel for current panel.
	 */
	public CommonGUI(BiomesAddon addon,
		World world,
		User user,
		String topLabel,
		String permissionPrefix,
		CommonGUI parentGUI)
	{
		this.addon = addon;
		this.world = world;
		this.user = user;

		this.topLabel = topLabel;
		this.permissionPrefix = permissionPrefix;

		this.parentGUI = parentGUI;

		this.pageIndex = 0;

		this.returnButton = new PanelItemBuilder().
			name(this.user.getTranslation("biomes.gui.buttons.return")).
			icon(Material.OAK_DOOR).
			clickHandler((panel, user1, clickType, i) -> {
				if (this.parentGUI == null)
				{
					this.user.closeInventory();
					return true;
				}

				this.parentGUI.build();
				return true;
			}).build();
	}


// ---------------------------------------------------------------------
// Section: Common methods
// ---------------------------------------------------------------------


	/**
	 * This method builds all necessary elements in GUI panel.
	 */
	public abstract void build();


	/**
	 * This method returns PanelItem that represents given Button.
	 * @param button Button that must be returned.
	 * @return PanelItem with requested functionality.
	 */
	protected PanelItem getButton(CommonButtons button)
	{
		ItemStack icon;
		String name;
		List<String> description;
		PanelItem.ClickHandler clickHandler;

		switch (button)
		{
			case NEXT:
				name = this.user.getTranslation("biomes.gui.buttons.next");
				description = Collections.emptyList();
				icon = new ItemStack(Material.OAK_SIGN);
				clickHandler = (panel, u, clickType, slot) -> {
					this.pageIndex++;
					this.build();
					return true;
				};

				break;
			case PREVIOUS:
				name = this.user.getTranslation("biomes.gui.buttons.previous");
				description = Collections.emptyList();
				icon = new ItemStack(Material.OAK_SIGN);
				clickHandler = (panel, u, clickType, slot) -> {
					this.pageIndex--;
					this.build();
					return true;
				};

				break;
			case RETURN:
				return this.returnButton;
			default:
				return null;
		}

		return new PanelItemBuilder().
			icon(icon).
			name(name).
			description(description).
			glow(false).
			clickHandler(clickHandler).
			build();
	}


// ---------------------------------------------------------------------
// Section: Calling command from GUI
// ---------------------------------------------------------------------


	/**
	 * This method finds and try to execute given sub command with given arguments.
	 * @param subCommand Sub Command that need to be called.
	 * @param arguments List of arguments for current command.
	 */
	protected void callCommand(String subCommand, List<String> arguments)
	{
		CompositeCommand command = this.addon.getPlugin().getCommandsManager().getCommand(this.topLabel);
		Optional<CompositeCommand> commandOptional = command.getSubCommand(BIOMES);

		if (!commandOptional.isPresent())
		{
			// TODO: Throw error that biomes command not found.
			return;
		}

		commandOptional = commandOptional.get().getSubCommand(subCommand);

		if (!commandOptional.isPresent())
		{
			// TODO: Throw error that biomes sub-command not found.
			return;
		}

		command = commandOptional.get();

		if (command.canExecute(this.user, subCommand, arguments))
		{
			command.execute(this.user, subCommand, arguments);
		}

		this.user.closeInventory();
	}

// ---------------------------------------------------------------------
// Section: Generate Biomes Object Description
// ---------------------------------------------------------------------


	/**
	 * This method generates and returns given biomesObject description. It is used here to avoid multiple
	 * duplicates, as it would be nice to have single place where biomesObject could be generated.
	 * @param biomesObject BiomesObject which description must be generated.
	 * @return List of strings that will be used in Biomes description.
	 */
	protected List<String> generateBiomesDescription(BiomesObject biomesObject)
	{
		List<String> result = new ArrayList<>();

		this.addon.getSettings().getLore().forEach(lore ->
		{
			switch (lore)
			{
				case DESCRIPTION:
				{
					// This adds biomes description
					result.addAll(biomesObject.getDescription());
					break;
				}
				case ORIGINAL_BIOME:
				{    // This adds biomes original name.
					result.add(this.user
						.getTranslation("biomes.gui.biomes-description.biome-name",
							VALUE_TAG, biomesObject.getBiome().name()));
					break;
				}
				case REQUIRED_MONEY:
				{    // Add message about required money
					if (this.addon.isEconomyProvided() &&
						biomesObject.getRequiredCost() > 0)
					{
						result.add(this.user
							.getTranslation("biomes.gui.biomes-description.required-money",
								VALUE_TAG,
								Double.toString(biomesObject.getRequiredCost())));
					}
					break;
				}
				case REQUIRED_LEVEL:
				{
					// Add message about required island level
					if (this.addon.isLevelProvided() && biomesObject.getRequiredLevel() > 0)
					{
						result.add(this.user.getTranslation("biomes.gui.biomes-description.required-island-level",
							VALUE_TAG, Long.toString(biomesObject.getRequiredLevel())));
					}
					break;
				}
				case REQUIRED_PERMISSION:
				{
					// Add message about required permissions
					if (!biomesObject.getRequiredPermissions().isEmpty())
					{
						result.add(this.user.getTranslation("biomes.gui.biomes-description.required-permissions"));

						biomesObject.getRequiredPermissions().forEach(permission ->
						{
							result.add(this.user.getTranslation("biomes.gui.descriptions.permission",
								"[permission]", permission));
						});
					}
					break;
				}
			}
		});

		result.replaceAll(x -> x.replace("[label]", this.topLabel));

		return result;
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

	/**
	 * This variable stores parent gui.
	 */
	protected CommonGUI parentGUI;

	/**
	 * Variable stores Biomes addon.
	 */
	protected BiomesAddon addon;

	/**
	 * Variable stores world in which panel is referred to.
	 */
	protected World world;

	/**
	 * Variable stores user who created this panel.
	 */
	protected User user;

	/**
	 * Variable stores top label of command from which panel was called.
	 */
	protected String topLabel;

	/**
	 * Variable stores permission prefix of command from which panel was called.
	 */
	protected String permissionPrefix;

	/**
	 * This object holds current page index.
	 */
	protected int pageIndex;

	/**
	 * This object holds PanelItem that allows to return to previous panel.
	 */
	protected PanelItem returnButton;


	/**
	 * This enum contains buttons that is offten used in multiple GUIs.
	 */
	protected enum CommonButtons
	{
		NEXT,
		PREVIOUS,
		RETURN
	}


// ---------------------------------------------------------------------
// Section: Constants
// ---------------------------------------------------------------------


	protected static final String BIOMES = "biomes";

	protected static final String ADMIN = "admin";

	protected static final String SET = "set";

	protected static final String ADD = "add";

	protected static final String EDIT = "edit";

	protected static final String DELETE = "remove";

	protected static final String SETTINGS = "settings";

	protected static final String IMPORT = "import";

	protected static final String INFO = "info";
	
	protected static final String VALUE_TAG = "[value]";
}