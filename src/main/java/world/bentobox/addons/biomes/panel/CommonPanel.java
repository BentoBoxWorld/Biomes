package world.bentobox.addons.biomes.panel;


import org.bukkit.World;

import world.bentobox.addons.biomes.BiomesAddon;
import world.bentobox.bentobox.api.user.User;


/**
 * This class contains common methods that is necessary for biomes addon panels.
 */
public abstract class CommonPanel
{
	/**
	 * Default constructor that inits panels with minimal requirements, without parent panel.
	 * @param addon Addon where panel operates.
	 * @param world World from which panel was created.
	 * @param user User who created panel.
	 * @param topLabel Command top label which creates panel (f.e. island or ai)
	 * @param permissionPrefix Command permission prefix (f.e. bskyblock.)
	 */
	public CommonPanel(BiomesAddon addon,
		World world,
		User user,
		String topLabel,
		String permissionPrefix)
	{
		this(addon, world, user, topLabel, permissionPrefix, null);
	}


	/**
	 * Default constructor that inits panels with minimal requirements.
	 * @param addon Addon where panel operates.
	 * @param world World from which panel was created.
	 * @param user User who created panel.
	 * @param topLabel Command top label which creates panel (f.e. island or ai)
	 * @param permissionPrefix Command permission prefix (f.e. bskyblock.)
	 * @param parentPanel Parent panel for current panel.
	 */
	public CommonPanel(BiomesAddon addon,
		World world,
		User user,
		String topLabel,
		String permissionPrefix,
		CommonPanel parentPanel)
	{
		this.addon = addon;
		this.world = world;
		this.user = user;

		this.topLabel = topLabel;
		this.permissionPrefix = permissionPrefix;

		this.parentPanel = parentPanel;
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

	/**
	 * Variable stores biomes addon.
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
	 * Variable stores parent of current panel.
	 */
	protected CommonPanel parentPanel;


// ---------------------------------------------------------------------
// Section: Constants for permission and command generation
// ---------------------------------------------------------------------

	protected final static String BIOMES = "biomes";

	protected final static String ADMIN = "admin";

	protected final static String SET = "set";

	protected final static String ADD = "add";

	protected final static String EDIT = "edit";

	protected final static String DELETE = "remove";

	protected final static String SETTINGS = "settings";

	protected final static String IMPORT = "import";

	protected final static String INFO = "info";
}
