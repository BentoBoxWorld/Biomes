package world.bentobox.biomes.commands.admin;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;
import world.bentobox.biomes.commands.ExpandedCompositeCommand;
import world.bentobox.biomes.objects.Settings.UpdateMode;
import world.bentobox.biomes.panel.admin.AdminSettingsPanel;
import world.bentobox.biomes.utils.Utils;

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


	@Override
	public boolean execute(User user, String label, List<String> args)
	{
		if (user.isPlayer() && args.isEmpty())
		{
			// Shows admin panel
			new AdminSettingsPanel(this.addon,
				this.getWorld(),
				user,
				this.getTopLabel(),
				this.getPermissionPrefix()).build();
			return true;
		}
		else if (args.isEmpty())
		{
			this.showHelp(this, user);
			return false;
		}
		else if (args.size() < 2)
		{
			user.sendMessage("biomes.messages.errors.missing-arguments");
			this.showHelp(this, user);
			return false;
		}
		else
		{
			switch (args.get(0).toLowerCase())
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
						user.sendMessage("biomes.messages.errors.incorrect-boolean",
							"[boolean]",
							args.get(1));
						return false;
					}
				case TYPE:
					UpdateMode mode = Utils.parseStrictToUpdateMode(args.get(1).toUpperCase());

					if (mode == null)
					{
						user.sendMessage("biomes.messages.errors.incorrect-mode",
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
						user.sendMessage("biomes.messages.errors.incorrect-range",
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
						user.sendMessage("biomes.messages.errors.incorrect-boolean",
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
						user.sendMessage("biomes.messages.errors.incorrect-range",
							"[number]",
							args.get(1));
						return false;
					}
				default:
					user.sendMessage("biomes.messages.errors.incorrect-parameter",
						"[property]",
						args.get(0));
					return false;
			}

			user.sendMessage("biomes.messages.information.saved-config");
			return true;
		}
	}


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

				returnList.add(MENU);
				returnList.add(TYPE);
				returnList.add(SIZE);
				returnList.add(RESET_BIOME);
				returnList.add(TIMEOUT);

				break;
			case 5:
				// Change last variable depending on previous value.

				switch (args.get(3))
				{
					case MENU:
					case RESET_BIOME:
						returnList.add("<boolean>");
						break;
					case TYPE:
						returnList.add("island");
						returnList.add("chunk");
						returnList.add("square");
						break;
					case SIZE:
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


	private static final String MENU = "advancedmenu";

	private static final String TYPE = "defaulttype";

	private static final String SIZE = "defaultsize";

	private static final String TIMEOUT = "timeout";

	private static final String RESET_BIOME = "resetbiomes";
}
