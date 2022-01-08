package world.bentobox.biomes.commands.admin;


import java.util.List;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.commands.ConfirmableCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.biomes.BiomesAddon;
import world.bentobox.biomes.panels.admin.AdminPanel;
import world.bentobox.biomes.utils.Constants;
import world.bentobox.biomes.utils.Utils;


/**
 * This class process "biomes" as admin command.
 */
public class AdminCommand extends CompositeCommand
{
	/**
	 * This is simple constructor for initializing /{admin_command} biomes command.
	 *
	 * @param addon BiomesAddon addon.
	 * @param parentCommand Parent Command where we hook our command into.
	 */
	public AdminCommand(BiomesAddon addon, CompositeCommand parentCommand)
	{
		super(addon,
			parentCommand,
			addon.getSettings().getAdminCommand().split(" ")[0],
			addon.getSettings().getAdminCommand().split(" "));
	}


	/**
	 * Setups anything that is needed for this command. <br/><br/> It is recommended you do the following in this
	 * method:
	 * <ul>
	 * <li>Register any of the sub-commands of this command;</li>
	 * <li>Define the permission required to use this command using {@link
	 * CompositeCommand#setPermission(String)};</li>
	 * <li>Define whether this command can only be run by players or not using {@link
	 * CompositeCommand#setOnlyPlayer(boolean)};</li>
	 * </ul>
	 */
	@Override
	public void setup()
	{
		this.setPermission("admin.biomes");
		this.setParametersHelp(Constants.ADMIN_COMMANDS + "main.parameters");
		this.setDescription(Constants.ADMIN_COMMANDS + "main.description");

		this.setOnlyPlayer(false);

		new ImportCommand(this.getAddon(), this);
		new MigrateCommand(this.getAddon(), this);
	}


	/**
	 * Defines what will be executed when this command is run.
	 *
	 * @param user the {@link User} who is executing this command.
	 * @param label the label which has been used to execute this command. It can be {@link CompositeCommand#getLabel()}
	 * or an alias.
	 * @param args the command arguments.
	 * @return {@code true} if the command executed successfully, {@code false} otherwise.
	 */
	@Override
	public boolean execute(User user, String label, List<String> args)
	{
		if (user.isPlayer())
		{
			// Create GUI
			AdminPanel.open(this.getAddon(),
				this.getWorld(),
				user,
				this.getTopLabel(),
				this.getPermissionPrefix());
			return true;
		}
		else
		{
			this.showHelp(this, user);
			return false;
		}
	}


	// ---------------------------------------------------------------------
	// Section: Subcommadns
	// ---------------------------------------------------------------------


	/**
	 * This is a confirmation command for importing generators from template file. It requires confirmation as it
	 * removes every data from database.
	 */
	private static class ImportCommand extends ConfirmableCommand
	{
		/**
		 * This is simple constructor for initializing /{admin_command} generator import command.
		 *
		 * @param addon StoneGeneratorAddon addon.
		 * @param parentCommand Parent Command where we hook our command into.
		 */
		public ImportCommand(BiomesAddon addon, CompositeCommand parentCommand)
		{
			super(addon, parentCommand, "import");
		}


		/**
		 * Setups anything that is needed for this command. <br/><br/> It is recommended you do the following in this
		 * method:
		 * <ul>
		 * <li>Register any of the sub-commands of this command;</li>
		 * <li>Define the permission required to use this command using {@link
		 * CompositeCommand#setPermission(String)};</li>
		 * <li>Define whether this command can only be run by players or not using {@link
		 * CompositeCommand#setOnlyPlayer(boolean)};</li>
		 * </ul>
		 */
		@Override
		public void setup()
		{
			this.inheritPermission();
			this.setParametersHelp(Constants.ADMIN_COMMANDS + "import.parameters");
			this.setDescription(Constants.ADMIN_COMMANDS + "import.description");

			this.setOnlyPlayer(false);
		}


		/**
		 * Defines what will be executed when this command is run.
		 *
		 * @param user the {@link User} who is executing this command.
		 * @param label the label which has been used to execute this command. It can be {@link
		 * CompositeCommand#getLabel()} or an alias.
		 * @param args the command arguments.
		 * @return {@code true} if the command executed successfully, {@code false} otherwise.
		 */
		@Override
		public boolean execute(User user, String label, List<String> args)
		{
			if (!args.isEmpty())
			{
				this.askConfirmation(user,
					user.getTranslation(Constants.CONVERSATIONS + "prefix") +
						user.getTranslation(Constants.ADMIN_COMMANDS + "import.confirmation",
							Constants.PARAMETER_GAMEMODE, Utils.getGameMode(this.getWorld())),
					() -> this.<BiomesAddon>getAddon().getImportManager()
						.importFile(user, this.getWorld(), args.get(0)));
			}
			else
			{
				this.askConfirmation(user,
					user.getTranslation(Constants.CONVERSATIONS + "prefix") +
						user.getTranslation(Constants.ADMIN_COMMANDS + "import.confirmation",
							Constants.PARAMETER_GAMEMODE, Utils.getGameMode(this.getWorld())),
					() -> this.<BiomesAddon>getAddon().getImportManager().importFile(user, this.getWorld()));
			}

			return true;
		}
	}


	/**
	 * The type Migrate command.
	 */
	private static class MigrateCommand extends CompositeCommand
	{
		/**
		 * Migrates biomes
		 *
		 * @param addon - addon
		 * @param cmd - command
		 */
		public MigrateCommand(Addon addon, CompositeCommand cmd)
		{
			super(addon, cmd, "migrate");
		}


		/**
		 * Execute command.
		 *
		 * @param user the user
		 * @param label the command top label
		 * @param args the args
		 * @return always true.
		 */
		@Override
		public boolean execute(User user, String label, List<String> args)
		{
			this.<BiomesAddon>getAddon().getAddonManager().migrateDatabase(user, getWorld());
			return true;
		}


		/**
		 * Sets command settings.
		 */
		@Override
		public void setup()
		{
			this.setPermission("biomes.admin");
			this.setParametersHelp(Constants.ADMIN_COMMANDS + "migrate.parameters");
			this.setDescription(Constants.ADMIN_COMMANDS + "migrate.description");
		}
	}
}
