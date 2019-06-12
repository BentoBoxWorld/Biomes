package world.bentobox.biomes.commands.admin;


import java.util.*;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;
import world.bentobox.biomes.commands.ExpandedCompositeCommand;
import world.bentobox.biomes.config.Settings.UpdateMode;
import world.bentobox.biomes.config.Settings.VisibilityMode;
import world.bentobox.biomes.panels.admin.EditSettingsGUI;

/**
 * This command allows to edit biomes addon settings.
 */
public class SettingsCommand extends ExpandedCompositeCommand
{
	public SettingsCommand(Addon addon, CompositeCommand parent)
	{
		super(addon, parent, "settings");
	}

	@Override
	public void setup()
	{
		this.setPermission("admin.biomes.settings");
		this.setParametersHelp("biomes.commands.admin.settings.parameters");
		this.setDescription("biomes.commands.admin.settings.description");
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
		else if (args.size() < 2)
		{
			user.sendMessage("biomes.errors.missing-arguments");
		}
		else if (args.size() > 2)
		{
			user.sendMessage("biomes.errors.too-many-arguments");
		}
		else
		{
			if (CommandParameters.getParameter(args.get(0)) == null)
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


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean execute(User user, String label, List<String> args)
	{
		if (user.isPlayer() && args.isEmpty())
		{
			// Shows admin panel
			new EditSettingsGUI(this.addon,
				this.getWorld(),
				user,
				this.getTopLabel(),
				this.getPermissionPrefix()).build();
			return true;
		}
		else
		{
			switch (CommandParameters.getParameter(args.get(0)))
			{
				case MENU:
					if (args.get(1).equalsIgnoreCase("true"))
					{
						this.addon.getSettings().setAdvancedMenu(true);
						break;
					}
					else if (args.get(1).equalsIgnoreCase("false"))
					{
						this.addon.getSettings().setAdvancedMenu(false);
						break;
					}
					else
					{
						user.sendMessage("biomes.errors.incorrect-boolean",
							"[boolean]",
							args.get(1));
						return false;
					}
				case TYPE:
					UpdateMode mode = UpdateMode.getMode(args.get(1).toUpperCase());

					if (mode == null)
					{
						user.sendMessage("biomes.errors.incorrect-mode",
							"[mode]",
							args.get(1));
						return false;
					}
					this.addon.getSettings().setDefaultMode(mode);
					break;
				case SIZE:
					try
					{
						this.addon.getSettings().setDefaultSize(Integer.parseInt(args.get(1)));
						break;
					}
					catch (Exception e)
					{
						user.sendMessage("biomes.errors.incorrect-range",
							"[number]",
							args.get(1));
						return false;
					}
				case RESET_BIOME:
					if (args.get(1).equalsIgnoreCase("true"))
					{
						this.addon.getSettings().setResetBiomes(true);
						break;
					}
					else if (args.get(1).equalsIgnoreCase("false"))
					{
						this.addon.getSettings().setResetBiomes(false);
						break;
					}
					else
					{
						user.sendMessage("biomes.errors.incorrect-boolean",
							"[boolean]",
							args.get(1));
						return false;
					}
				case TIMEOUT:
					try
					{
						this.addon.getSettings().setCoolDown(Integer.parseInt(args.get(1)));
						break;
					}
					catch (Exception e)
					{
						user.sendMessage("biomes.errors.incorrect-range",
							"[number]",
							args.get(1));
						return false;
					}
				case VISIBILITY:
				{
					VisibilityMode visibility = VisibilityMode.getMode(args.get(1).toUpperCase());

					if (visibility == null)
					{
						user.sendMessage("biomes.errors.incorrect-visibility",
							"[mode]",
							args.get(1));
						return false;
					}
					this.addon.getSettings().setVisibilityMode(visibility);
					break;
				}
				case LORE_LENGTH:
				{
					try
					{
						this.addon.getSettings().setLoreLineLength(Integer.parseInt(args.get(1)));
						break;
					}
					catch (Exception e)
					{
						user.sendMessage("biomes.errors.incorrect-range",
							"[number]",
							args.get(1));
						return false;
					}
				}
				case LORE_MESSAGE:
				{
					this.addon.getSettings().setLoreMessage(args.get(1));
					break;
				}
			}

			user.sendMessage("biomes.messages.saved-config");
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
				// Create list with all addon settings values.

				Arrays.stream(CommandParameters.values()).
					map(properties -> properties.parameter).
					forEach(returnList::add);

				break;
			case 5:
				// Change last variable depending on previous value.

				switch (CommandParameters.getParameter(args.get(3)))
				{
					case MENU:
					case RESET_BIOME:
						returnList.add("<boolean>");
						break;
					case TYPE:
						Arrays.stream(UpdateMode.values()).
							map(Enum::name).
							forEach(returnList::add);
						break;
					case VISIBILITY:
						Arrays.stream(VisibilityMode.values()).
							map(Enum::name).
							forEach(returnList::add);
						break;
					case LORE_MESSAGE:
						returnList.add("<char>");
						break;
					case SIZE:
					case LORE_LENGTH:
					case TIMEOUT:
						returnList.add("<number>");
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


// ---------------------------------------------------------------------
// Section: Constants
// ---------------------------------------------------------------------


	/**
	 * This enum contains all values that can be changed in BiomeObject.
	 */
	private enum CommandParameters
	{
		MENU("advancedmenu"),
		TYPE("defaulttype"),
		SIZE("defaultsize"),
		TIMEOUT("timeout"),
		RESET_BIOME("resetbiomes"),
		VISIBILITY("visibilitymode"),
		LORE_LENGTH("lorelength"),
		LORE_MESSAGE("loremessage");


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
		private final static Map<String, CommandParameters> BY_NAME = new HashMap<>();

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
