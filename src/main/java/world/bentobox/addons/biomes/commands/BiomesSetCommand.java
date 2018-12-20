package world.bentobox.addons.biomes.commands;


import java.util.*;

import world.bentobox.addons.biomes.BiomesAddon;
import world.bentobox.addons.biomes.objects.BiomesObject;
import world.bentobox.addons.biomes.tasks.BiomeUpdateHelper;
import world.bentobox.addons.biomes.utils.Utils;
import world.bentobox.addons.biomes.utils.Utils.UpdateMode;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;


/**
 * This command process <code>/is biomes set</code> command.
 */
public class BiomesSetCommand extends CompositeCommand
{
	public BiomesSetCommand(BiomesAddon addon, CompositeCommand command)
	{
		super(addon, command, "set");
	}


	@Override
	public void setup()
	{
		this.setPermission("island.biomes");
		this.setParametersHelp("biomes.set.parameters");
		this.setDescription("biomes.set.description");

		this.setOnlyPlayer(true);
	}


	@Override
	public boolean execute(User user, String label, List<String> args)
	{
		BiomesObject biome = this.getBiomeObject(args, user);
		UpdateMode updateMode = this.getUpdateMode(args, user);
		int size = this.getUpdateSize(args, user);

		if (biome == null || updateMode == null || size < 1)
		{
			this.showHelp(this, user);
			return false;
		}
		else
		{
			BiomeUpdateHelper helper = new BiomeUpdateHelper(this.getParent().getAddon(),
				user,
				user,
				biome,
				user.getWorld(),
				updateMode,
				size,
				true);

			if (helper.canChangeBiome())
			{
				helper.updateIslandBiome();
			}

			return true;
		}
	}


	@Override
	public Optional<List<String>> tabComplete(User user, String alias, List<String> args)
	{
		String lastString = args.get(args.size() - 1);

		final List<String> returnList;
		final int size = args.size();

		switch (size)
		{
			case 3:
			case 5:
			case 7:
			{
				// Add method keys
				returnList = new ArrayList<>();

				if (!args.contains(KEY_BIOME))
				{
					returnList.addAll(Util.tabLimit(Collections.singletonList(KEY_BIOME), lastString));
				}

				if (!args.contains(KEY_TYPE))
				{
					returnList.addAll(Util.tabLimit(Collections.singletonList(KEY_TYPE), lastString));
				}

				if (!args.contains(KEY_SIZE))
				{
					returnList.addAll(Util.tabLimit(Collections.singletonList(KEY_SIZE), lastString));
				}

				break;
			}
			case 4:
			case 6:
			case 8:
			{
				// Get key values
				String lastValidKey = args.get(size - 2);

				if (lastValidKey != null)
				{
					if (lastValidKey.equals(KEY_BIOME))
					{
						returnList = new ArrayList<>();
						List<BiomesObject> biomes =
							((BiomesAddon) this.getParent().getAddon()).getAddonManager()
								.getBiomes();

						biomes.forEach(biomesObject -> {
							returnList.addAll(Util.tabLimit(Collections
								.singletonList(biomesObject.getBiomeName()), lastString));
						});
					}
					else if (lastValidKey.equals(KEY_TYPE))
					{
						returnList = new ArrayList<>();
						returnList.addAll(Util.tabLimit(Collections.singletonList("island"), lastString));
						returnList.addAll(Util.tabLimit(Collections.singletonList("chunk"), lastString));
						returnList.addAll(Util.tabLimit(Collections.singletonList("square"), lastString));
					}
					else if (lastValidKey.equals(KEY_SIZE))
					{
						returnList = new ArrayList<>();

						if (lastString.isEmpty() || lastString.matches("[0-9]*"))
						{
							returnList.addAll(Util.tabLimit(Collections.singletonList("<number>"), "<n"));
						}
					}
					else
					{
						returnList = Collections.emptyList();
					}
				}
				else
				{
					returnList = Collections.emptyList();
				}

				break;
			}
			default:
			{
				returnList = new ArrayList<>(Util.tabLimit(Collections.singletonList("help"), lastString));

				break;
			}
		}

		return Optional.of(returnList);
	}



// ---------------------------------------------------------------------
// Section: Private methods
// ---------------------------------------------------------------------


	/**
	 * This method returns BiomesObject or null.
	 * @param args Args that contains all command arguments
	 * @param user Caller user.
	 * @return BiomesObject or null.
	 */
	private BiomesObject getBiomeObject(List<String> args, User user)
	{
		if (args.contains(KEY_BIOME))
		{
			BiomesObject biome = ((BiomesAddon) this.getParent().getAddon()).getAddonManager().
				getBiomeFromString(args.get(args.indexOf(KEY_BIOME) + 1));

			if (biome == null)
			{
				user.sendMessage(user.getTranslation("biomes.command.error.wrong-biome-name",
					"[biome]",
					args.get(args.indexOf(KEY_BIOME) + 1)));
			}

			return biome;
		}
		else
		{
			user.sendMessage(user.getTranslation("biomes.command.error.biome-not-defined"));
			return null;
		}
	}


	/**
	 * This method returns Update Mode type.
	 * @param args Args that contains all command arguments
	 * @param user Caller user.
	 * @return UpdateMode or null.
	 */
	private UpdateMode getUpdateMode(List<String> args, User user)
	{
		if (args.contains(KEY_TYPE))
		{
			UpdateMode mode = Utils.parseStrictToUpdateMode(args.get(args.indexOf(KEY_TYPE) + 1));

			if (mode == null)
			{
				user.sendMessage(user.getTranslation("biomes.command.error.wrong-mode-name",
					"[mode]",
					args.get(args.indexOf(KEY_TYPE) + 1)));
			}

			return  mode;
		}
		else
		{
			return Utils.parseStringToUpdateMode(
				this.getParent().getAddon().getConfig().getString("defaulttype"));
		}
	}


	private int getUpdateSize(List<String> args, User user)
	{
		if (args.contains(KEY_SIZE))
		{
			int size;

			try
			{
				size = Integer.parseInt(args.get(args.indexOf(KEY_SIZE) + 1));
			}
			catch (Exception e)
			{
				size = -1;
			}

			if (size < 1)
			{
				user.sendMessage(user.getTranslation("biomes.command.error.incorrect-size",
					TextVariables.NUMBER,
					args.get(args.indexOf(KEY_SIZE) + 1)));
			}

			return size;
		}
		else
		{
			return this.getParent().getAddon().getConfig().getInt("defaultsize", 1);
		}
	}


// ---------------------------------------------------------------------
// Section: CONSTANTS
// ---------------------------------------------------------------------


	private final static String KEY_BIOME = "biome";

	private final static String KEY_TYPE = "type";

	private final static String KEY_SIZE = "size";
}
