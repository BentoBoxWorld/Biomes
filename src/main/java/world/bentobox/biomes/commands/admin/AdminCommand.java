///
// Created by BONNe
// Copyright - 2022
///

package world.bentobox.biomes.commands.admin;


import org.bukkit.Bukkit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.commands.ConfirmableCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;
import world.bentobox.biomes.BiomesAddon;
import world.bentobox.biomes.commands.BiomesCompositeCommand;
import world.bentobox.biomes.config.Settings;
import world.bentobox.biomes.database.objects.BiomesObject;
import world.bentobox.biomes.panels.admin.AdminPanel;
import world.bentobox.biomes.tasks.BiomeUpdateHelper;
import world.bentobox.biomes.utils.Constants;
import world.bentobox.biomes.utils.Utils;


/**
 * This class process "biomes" as admin command.
 */
public class AdminCommand extends BiomesCompositeCommand
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
        new BiomesSetCommand(this.getAddon(), this);
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
    // Section: Subcommands
    // ---------------------------------------------------------------------


    /**
     * The subcommand that changes biomes on the island.
     */
    private class BiomesSetCommand extends CompositeCommand
    {
        /**
         * Instantiates a new Biomes set command.
         *
         * @param addon the addon
         * @param parentCommand the parent command
         */
        public BiomesSetCommand(BiomesAddon addon, CompositeCommand parentCommand)
        {
            super(addon, parentCommand, "set");
        }


        /**
         * Sets up command settings.
         */
        @Override
        public void setup()
        {
            this.inheritPermission();
            this.setParametersHelp(Constants.ADMIN_COMMANDS + "set.parameters");
            this.setDescription(Constants.ADMIN_COMMANDS + "set.description");
        }


        /**
         * Can execute boolean.
         *
         * @param user the user
         * @param label the label
         * @param args the args
         * @return the boolean
         */
        @Override
        public boolean canExecute(User user, String label, List<String> args)
        {
            return this.<BiomesAddon>getAddon().getSettings().getCoolDown() == 0 || !this.checkCooldown(user);
        }


        /**
         * Execute set command.
         *
         * @param user the user
         * @param label the label
         * @param input the input
         * @return the boolean
         */
        @Override
        public boolean execute(User user, String label, List<String> input)
        {
            List<String> args = input.subList(1, input.size());
            User target = this.getAddon().getPlayers().getUser(input.get(0));
            BiomesObject biome = AdminCommand.this.getBiomeObject(args, user);
            Settings.UpdateMode updateMode = AdminCommand.this.getUpdateMode(args, user);
            int size = AdminCommand.this.getUpdateRange(args, user);

            if (target == null || biome == null || updateMode == null || size < 1)
            {
                // Show help if something fails.
                this.showHelp(this, user);
            }
            else
            {
                // Use BiomeUpdateHelper to change biome for user.

                BiomeUpdateHelper helper = new BiomeUpdateHelper(this.getAddon(),
                    user,
                    target,
                    biome,
                    this.<BiomesAddon>getAddon().getAddonManager().getIslandData(this.getWorld(), user),
                    this.getWorld(),
                    updateMode,
                    size,
                    false);

                if (helper.canChangeBiome())
                {
                    helper.updateIslandBiome();
                    this.setCooldown(user.getUniqueId(), this.<BiomesAddon>getAddon().getSettings().getCoolDown());

                    return true;
                }
            }
            return false;
        }


        /**
         * Tab complete optional.
         *
         * @param user the user
         * @param alias the alias
         * @param args the args
         * @return the optional
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
                    // Create suggestions with all player names that is available for users.
                    Bukkit.getOnlinePlayers().forEach(player -> returnList.add(player.getName()));

                    break;
                case 4:
                    List<BiomesObject> biomes =
                        this.<BiomesAddon>getAddon().getAddonManager().getBiomes(this.getWorld());

                    // Create suggestions with all biomes that is available for users.
                    biomes.forEach(biomesObject ->
                        returnList.add(biomesObject.getUniqueId()
                            .substring(Utils.getGameMode(this.getWorld()).length() + 1)));

                    break;
                case 5:
                    // Create suggestions with all update modes that is available for users.
                    Arrays.stream(Settings.UpdateMode.values()).
                        map(Enum::name).
                        forEach(returnList::add);

                    if (!BiomesAddon.BIOMES_WORLD_PROTECTION.isSetForWorld(this.getWorld()))
                    {
                        // Do not suggest island as it is not valid option
                        returnList.remove(Settings.UpdateMode.ISLAND.name());
                    }

                    break;
                case 6:
                    if (lastString.isEmpty() || lastString.matches("[0-9]*"))
                    {
                        returnList.add("<number>");
                    }

                    break;
                default:
                    returnList.add("help");
                    break;
            }

            return Optional.of(Util.tabLimit(returnList, lastString));
        }
    }


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
            this.inheritPermission();
            this.setParametersHelp(Constants.ADMIN_COMMANDS + "migrate.parameters");
            this.setDescription(Constants.ADMIN_COMMANDS + "migrate.description");
        }
    }
}
