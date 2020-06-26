package world.bentobox.biomes.commands.admin;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.inventory.ItemStack;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;
import world.bentobox.biomes.BiomesAddonManager;
import world.bentobox.biomes.commands.ExpandedCompositeCommand;
import world.bentobox.biomes.database.objects.BiomesObject;
import world.bentobox.biomes.panels.GuiUtils;
import world.bentobox.biomes.panels.admin.ListBiomesGUI;
import world.bentobox.biomes.utils.Utils;


/**
 * This is admin command that allows to change user biomes.
 */
public class EditBiomeCommand extends ExpandedCompositeCommand
{
	private static final String INCORRECT_RANGE_ERROR = "biomes.errors.incorrect-range";


    public EditBiomeCommand(Addon addon, CompositeCommand parent)
	{
		super(addon, parent, "edit");
	}

	@Override
	public void setup()
	{
		this.setPermission("admin.biomes.edit");
		this.setParametersHelp("biomes.commands.admin.edit.parameters");
		this.setDescription("biomes.commands.admin.edit.description");
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canExecute(User user, String label, List<String> args)
	{
		if (user.isPlayer() && args.isEmpty())
		{
			return true;
		}
		else if (args.size() < 3)
		{
			user.sendMessage("biomes.errors.missing-arguments");
		}
		else if (args.size() > 3)
		{
			user.sendMessage("biomes.errors.too-many-arguments");
		}
		else if (this.getBiomeObject(args, 0, user) != null)
		{
			if (CommandParameters.getParameter(args.get(1)) == null)
			{
				user.sendMessage("biomes.errors.unknown-argument");
			}
			else
			{
				return true;
			}
		}

		this.showHelp(this, user);
		return false;
	}


	@Override
	public boolean execute(User user, String label, List<String> args)
	{
		if (user.isPlayer() && args.isEmpty())
		{
			// Shows BiomesPanel in Edit mode.
			new ListBiomesGUI(this.addon,
				this.getWorld(),
				user,
				this.getTopLabel(),
				this.getPermissionPrefix(),
				true).build();
			return true;
		}
		else
		{
			BiomesObject biomesObject = this.getBiomeObject(args, 0, user);

			// Use proper setter based on 2. argument.
			switch (CommandParameters.getParameter(args.get(1)))
			{
				case BIOME:
					String newBiomeString = args.get(2);
					Biome newBiome = Utils.getBiomeNameMap().getOrDefault(newBiomeString.toUpperCase(), null);

					if (newBiome == null)
					{
						user.sendMessage("biomes.errors.incorrect-biome",
							"[biome]",
							newBiomeString);
						return false;
					}
					else
					{
						biomesObject.setBiome(newBiome);
					}

					break;
				case FRIENDLY_NAME:
					biomesObject.setFriendlyName(this.buildStringFromValue(args));
					break;
				case DESCRIPTION:
					biomesObject.setDescription(GuiUtils.stringSplit(this.buildStringFromValue(args), this.addon.getSettings().getLoreLineLength()));
					break;
				case ICON:
					Material newIcon = Material.getMaterial(args.get(2).toUpperCase());

					if (newIcon == null)
					{
						user.sendMessage("biomes.errors.incorrect-icon",
							"[icon]",
							args.get(2));
						return false;
					}
					else
					{
						biomesObject.setIcon(new ItemStack(newIcon));
					}

					break;
				case DEPLOYED:

					if (args.get(2).equalsIgnoreCase("true"))
					{
						biomesObject.setDeployed(true);
						break;
					}
					else if (args.get(2).equalsIgnoreCase("false"))
					{
						biomesObject.setDeployed(false);
						break;
					}
					else
					{
						user.sendMessage("biomes.errors.incorrect-boolean",
							"[boolean]",
							args.get(2));
						return false;
					}

				case REQUIRED_COST:

					try
					{
						biomesObject.setRequiredCost(Double.parseDouble(args.get(2)));
						break;
					}
					catch (Exception e)
					{
						user.sendMessage(INCORRECT_RANGE_ERROR,
							TextVariables.NUMBER,
							args.get(2));
						return false;
					}
				case ORDER:
					try
					{
						biomesObject.setOrder(Integer.parseUnsignedInt(args.get(2)));
						break;
					}
					catch (Exception e)
					{
						user.sendMessage(INCORRECT_RANGE_ERROR,
							TextVariables.NUMBER,
							args.get(2));
						return false;
					}
				case REQUIRED_LEVEL:
					try
					{
						biomesObject.setRequiredLevel(Integer.parseUnsignedInt(args.get(2)));
						break;
					}
					catch (Exception e)
					{
						user.sendMessage(INCORRECT_RANGE_ERROR,
							TextVariables.NUMBER,
							args.get(2));
						return false;
					}
				case PERMISSIONS:
					biomesObject.setRequiredPermissions(new HashSet<>(Arrays.asList(args.get(2).split(";"))));
					break;
			}

			this.addon.getAddonManager().saveBiome(biomesObject);
			user.sendMessage("biomes.messages.saved",
				"[biome]",
				biomesObject.getFriendlyName());
			return true;
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Optional<List<String>> tabComplete(User user, String alias, List<String> args)
	{
		String lastString = args.get(args.size() - 1);

		final List<String> returnList = new ArrayList<>();
		final int size = args.size();

		switch (size)
		{
			case 3:

				// Create suggestions with all biomes that is available for users.
				this.addon.getAddonManager().getBiomes(this.getWorld()).forEach(biomesObject -> {
					returnList.add(biomesObject.getUniqueId().substring(Utils.getGameMode(this.getWorld()).length() + 1));
				});

				break;
			case 4:
				// Create list with all biome properties values.

				Arrays.stream(CommandParameters.values()).
					map(properties -> properties.parameter).
					forEach(returnList::add);

				break;
			case 5:
				// Change last variable depending on previous value.

				switch (CommandParameters.getParameter(args.get(3)))
				{
					case BIOME:
						returnList.addAll(Utils.getBiomeNameMap().keySet());
						break;
					case FRIENDLY_NAME:
						returnList.add("<name>");
						break;
					case DESCRIPTION:
						returnList.add("<description>");
						break;
					case ICON:
						// Add all possible materials
						for (Material material : Material.values())
						{
							returnList.add(material.name());
						}

						break;
					case DEPLOYED:
						returnList.add("<boolean>");
						break;
					case REQUIRED_COST:
					case ORDER:
					case REQUIRED_LEVEL:
						returnList.add("<number>");
						break;
					case PERMISSIONS:
						returnList.add("<permissions>");
						break;
				}

				break;
			default:
			{
				break;
			}
		}

		return Optional.of(Util.tabLimit(returnList, lastString));
	}


	/**
	 * This method builds single string from args.
	 * @param args List of strings that contains values which must be merged.
	 * @return String that contains all values from input list after 2. index.
	 */
	private String buildStringFromValue(List<String> args)
	{
		StringBuilder builder = new StringBuilder(args.get(2));

		for (int i = 3; i < args.size(); i++)
		{
			builder.append(" ");
			builder.append(args.get(i));
		}

		return builder.toString();
	}


// ---------------------------------------------------------------------
// Section: Constants
// ---------------------------------------------------------------------


	/**
	 * This enum contains all values that can be changed in BiomeObject.
	 */
	private enum CommandParameters
	{
		BIOME("biomeName"),
		DEPLOYED("deployed"),
		FRIENDLY_NAME("friendlyName"),
		DESCRIPTION("description"),
		ICON("icon"),
		REQUIRED_COST("requiredCost"),
		REQUIRED_LEVEL("requiredLevel"),
		PERMISSIONS("requiredPermissions"),
		ORDER("order");


		/**
		 * Constructor.
		 * @param parameter of the command.
		 */
		CommandParameters(String parameter)
		{
			this.parameter = parameter;
		}


		/**
		 * This method returns stored parameter from string.
		 * @param parameter String of object that must be returned
		 * @return CommandParameters object or null.
		 */
		public static CommandParameters getParameter(String parameter)
		{
			return BY_NAME.get(parameter);
		}


		/**
		 * Parameter name.
		 */
		private String parameter;

		/**
		 * This map allows to access all enum values via their string.
		 */
		private static final Map<String, CommandParameters> BY_NAME = new HashMap<>();

		/**
		 * This static method populated BY_NAME map.
		 */
		static
		{
			for (CommandParameters command : CommandParameters.values())
			{
				BY_NAME.put(command.parameter, command);
			}
		}
	}
}
