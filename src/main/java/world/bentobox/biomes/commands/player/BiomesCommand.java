package world.bentobox.biomes.commands.player;


import java.util.*;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.util.Util;
import world.bentobox.biomes.BiomesAddon;
import world.bentobox.biomes.config.Settings;
import world.bentobox.biomes.database.objects.BiomesIslandDataObject;
import world.bentobox.biomes.database.objects.BiomesObject;
import world.bentobox.biomes.managers.BiomesAddonManager;
import world.bentobox.biomes.panels.user.BiomesPanel;
import world.bentobox.biomes.tasks.BiomeUpdateHelper;
import world.bentobox.biomes.utils.Constants;
import world.bentobox.biomes.utils.Utils;


/**
 * This class process "biomes" method.
 */
public class BiomesCommand extends CompositeCommand
{
    /**
     * Instantiates a new Biomes command.
     *
     * @param addon the addon
     * @param parentCommand the parent command
     */
    public BiomesCommand(BiomesAddon addon, CompositeCommand parentCommand)
    {
        super(addon,
            parentCommand,
            addon.getSettings().getPlayerCommand().split(" ")[0],
            addon.getSettings().getPlayerCommand().split(" "));
    }


    /**
     * Sets up command settings.
     */
    @Override
    public void setup()
    {
        this.setPermission("biomes");
        this.setOnlyPlayer(true);
        this.setParametersHelp(Constants.PLAYER_COMMANDS + "main.parameters");
        this.setDescription(Constants.PLAYER_COMMANDS + "main.description");

        new BiomesSetCommand(this.getAddon(), this);
        new BiomesInfoCommand(this.getAddon(), this);
        new BiomesBuyCommand(this.getAddon(), this);
    }


    /**
     * This method checks if user can execute this command. {@inheritDoc}
     */
    @Override
    public boolean canExecute(User user, String label, List<String> args)
    {
        Optional<GameModeAddon> optionalAddon = this.getAddon().getPlugin().getIWM().getAddon(this.getWorld());

        if (optionalAddon.isEmpty())
        {
            // Not a GameMode world.
            user.sendMessage("general.errors.wrong-world");
            return false;
        }

        BiomesAddon addon = this.getAddon();

        if (!addon.getAddonManager().hasAnyBiome(this.getWorld()))
        {
            // Do not open gui if there is no biomes.

            this.getAddon().getLogger().severe("There are no biomes set up in " + this.getWorld() + "!");

            // Show admin better explanation.
            if (user.isOp() || user.hasPermission(this.getPermissionPrefix() + "admin.biomes"))
            {
                String topLabel = optionalAddon.get().getAdminCommand().orElseGet(this::getParent).getTopLabel();
                Utils.sendMessage(user, user.getTranslation(Constants.ERRORS + "no-biomes-admin",
                    "[command]",
                    topLabel + " " + addon.getSettings().getAdminCommand().split(" ")[0]));
            }
            else
            {
                Utils.sendMessage(user, user.getTranslation(Constants.ERRORS + "no-biomes"));
            }

            return false;
        }

        if (this.getPlugin().getIslands().getIsland(this.getWorld(), user.getUniqueId()) == null)
        {
            // Do not open gui if there is no island for this player.
            Utils.sendMessage(user, user.getTranslation("general.errors.no-island"));
            return false;
        }

        return true;
    }


    /**
     * Execute biomes command.
     *
     * @param user the user
     * @param string the string
     * @param args the args
     * @return the boolean
     */
    @Override
    public boolean execute(User user, String string, List<String> args)
    {
        // Open up the biomes GUI
        if (user.isPlayer())
        {
            BiomesPanel.open(this.getAddon(),
                this.getWorld(),
                user,
                this.getTopLabel(),
                this.getPermissionPrefix());

            return true;
        }
        else
        {
            // Show help
            this.showHelp(this, user);
            return false;
        }
    }


    /**
     * This method returns BiomesObject or null.
     *
     * @param args Args that contains all command arguments
     * @param user Caller user.
     * @return BiomesObject or null.
     */
    protected BiomesObject getBiomeObject(List<String> args, User user)
    {
        if (args.size() > 0)
        {
            String uniqueID = args.get(0);
            String addonName = Utils.getGameMode(this.getWorld());

            if (!uniqueID.startsWith(addonName))
            {
                uniqueID = addonName + "_" + uniqueID;
            }

            BiomesObject biome = this.<BiomesAddon>getAddon().getAddonManager().getBiomeByID(uniqueID);

            if (biome == null)
            {
                Utils.sendMessage(user, user.getTranslation(Constants.ERRORS + "incorrect-object",
                    "[biome]",
                    args.get(0)));
            }

            return biome;
        }
        else
        {
            Utils.sendMessage(user, user.getTranslation(Constants.ERRORS + "missing-biome"));
            return null;
        }
    }


    /**
     * This method returns Update Mode type.
     *
     * @param args Args that contains all command arguments
     * @param user Caller user.
     * @return UpdateMode or null.
     */
    protected Settings.UpdateMode getUpdateMode(List<String> args, User user)
    {
        if (args.size() > 1)
        {
            Settings.UpdateMode mode = Settings.UpdateMode.getMode(args.get(1));

            if (mode == null)
            {
                Utils.sendMessage(user, user.getTranslation(Constants.ERRORS + "incorrect-mode",
                    "[mode]",
                    args.get(1)));
            }

            return mode;
        }
        else
        {
            return this.<BiomesAddon>getAddon().getSettings().getDefaultMode();
        }
    }


    /**
     * This method returns integer from args with given index which represents update distance for chunk and square
     * mode.
     *
     * @param args List of arguments that is passed via set command.
     * @param user User that calls current method.
     * @return Integer that represents update range distance.
     */
    protected int getUpdateRange(List<String> args, User user)
    {
        if (args.size() > 2)
        {
            int range;

            try
            {
                range = Integer.parseInt(args.get(2));
            }
            catch (Exception e)
            {
                range = -1;
            }

            if (range < 1)
            {
                Utils.sendMessage(user, user.getTranslation(Constants.ERRORS + "incorrect-range",
                    TextVariables.NUMBER,
                    args.get(2)));
            }

            return range;
        }
        else
        {
            return this.<BiomesAddon>getAddon().getSettings().getDefaultSize();
        }
    }


    /**
     * This method transforms stringList to string by appending each list at the end.
     *
     * @param stringList List with strings that must be contacted.
     * @return String from stringList.
     */
    protected String getSingleLineDescription(List<String> stringList)
    {
        if (stringList.isEmpty())
        {
            return "";
        }
        else if (stringList.size() > 1)
        {
            Iterator<String> iter = stringList.iterator();

            StringBuilder returnString = new StringBuilder(iter.next());

            while (iter.hasNext())
            {
                returnString.append(" ").append(iter.next());
            }

            return returnString.toString();
        }
        else
        {
            return stringList.get(0);
        }
    }


    /**
     * The subcommand that displays info about biomes object.
     */
    private class BiomesInfoCommand extends CompositeCommand
    {
        /**
         * Instantiates a new Biomes info command.
         *
         * @param addon the addon
         * @param parentCommand the parent command
         */
        public BiomesInfoCommand(BiomesAddon addon, BiomesCommand parentCommand)
        {
            super(addon,
                parentCommand,
                addon.getSettings().getPlayerInfoCommand().split(" ")[0],
                addon.getSettings().getPlayerInfoCommand().split(" "));
        }


        /**
         * Sets up command settings.
         */
        @Override
        public void setup()
        {
            this.setPermission("biomes.info");
            this.setParametersHelp(Constants.PLAYER_COMMANDS + "info.parameters");
            this.setDescription(Constants.PLAYER_COMMANDS + "info.description");

            this.setOnlyPlayer(true);
        }


        /**
         * Execute info command.
         *
         * @param user the user
         * @param string the string
         * @param args the args
         * @return the boolean
         */
        @Override
        public boolean execute(User user, String label, List<String> args)
        {
            BiomesObject biomesObject = BiomesCommand.this.getBiomeObject(args, user);

            if (biomesObject != null)
            {
                // TODO: rework information pasting or remove it.
                user.sendMessage("biomes.information.header",
                    "[name]", biomesObject.getFriendlyName());
                user.sendMessage("biomes.information.type",
                    "[type]", biomesObject.getBiome().name());
                user.sendMessage("biomes.information.description",
                    "[description]", BiomesCommand.this.getSingleLineDescription(biomesObject.getDescription()));
                user.sendMessage("biomes.information.level",
                    "[level]", Long.toString(biomesObject.getRequiredLevel()));
                user.sendMessage("biomes.information.cost",
                    "[cost]", Double.toString(biomesObject.getRequiredCost()));

                biomesObject.getRequiredPermissions().forEach(s ->
                    user.sendMessage("biomes.information.permission", "[permission]", s));

                return true;
            }
            else
            {
                this.showHelp(this, user);
                return false;
            }
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
            if (args.size() > 3)
            {
                // Show biome for first tab.
                return Optional.of(new ArrayList<>());
            }

            final List<String> returnList = new ArrayList<>();

            // Create suggestions with all biomes that is available for users.

            this.<BiomesAddon>getAddon().getAddonManager().getBiomes(this.getWorld()).forEach(biomesObject ->
                returnList.add(biomesObject.getUniqueId().substring(Utils.getGameMode(this.getWorld()).length() + 1)));

            return Optional.of(Util.tabLimit(returnList, args.get(args.size() - 1)));
        }
    }


    /**
     * The subcommand that changes biomes on the island..
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
            super(addon,
                parentCommand,
                addon.getSettings().getPlayerSetCommand().split(" ")[0],
                addon.getSettings().getPlayerSetCommand().split(" "));
        }


        /**
         * Sets up command settings.
         */
        @Override
        public void setup()
        {
            this.setPermission("biomes.set");
            this.setParametersHelp(Constants.PLAYER_COMMANDS + "set.parameters");
            this.setDescription(Constants.PLAYER_COMMANDS + "set.description");
            this.setOnlyPlayer(true);
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
         * @param string the string
         * @param args the args
         * @return the boolean
         */
        @Override
        public boolean execute(User user, String label, List<String> args)
        {
            BiomesObject biome = BiomesCommand.this.getBiomeObject(args, user);
            Settings.UpdateMode updateMode = BiomesCommand.this.getUpdateMode(args, user);
            int size = BiomesCommand.this.getUpdateRange(args, user);

            if (biome == null || updateMode == null || size < 1)
            {
                // Show help if something fails.
                this.showHelp(this, user);
            }
            else
            {
                // Use BiomeUpdateHelper to change biome for user.

                BiomeUpdateHelper helper = new BiomeUpdateHelper(this.getAddon(),
                    user,
                    user,
                    biome,
                    this.getWorld(),
                    updateMode,
                    size,
                    true);

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
                    List<BiomesObject> biomes =
                        this.<BiomesAddon>getAddon().getAddonManager().getBiomes(this.getWorld());

                    // Create suggestions with all biomes that is available for users.
                    biomes.forEach(biomesObject ->
                        returnList.add(biomesObject.getUniqueId()
                            .substring(Utils.getGameMode(this.getWorld()).length() + 1)));

                    break;
                case 4:
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
                case 5:
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
     * The subcommand that allows to buy biomes object.
     */
    private class BiomesBuyCommand extends CompositeCommand
    {
        /**
         * Instantiates a new Biomes info command.
         *
         * @param addon the addon
         * @param parentCommand the parent command
         */
        public BiomesBuyCommand(BiomesAddon addon, BiomesCommand parentCommand)
        {
            super(addon,
                parentCommand,
                addon.getSettings().getPlayerBuyCommand().split(" ")[0],
                addon.getSettings().getPlayerBuyCommand().split(" "));
        }


        /**
         * Sets up command settings.
         */
        @Override
        public void setup()
        {
            this.setPermission("biomes.buy");
            this.setParametersHelp(Constants.PLAYER_COMMANDS + "buy.parameters");
            this.setDescription(Constants.PLAYER_COMMANDS + "buy.description");

            this.setOnlyPlayer(true);
        }


        /**
         * Execute info command.
         *
         * @param user the user
         * @param string the string
         * @param args the args
         * @return the boolean
         */
        @Override
        public boolean execute(User user, String label, List<String> args)
        {
            BiomesObject biomesObject = BiomesCommand.this.getBiomeObject(args, user);
            BiomesAddonManager addonManager = this.<BiomesAddon>getAddon().getAddonManager();

            if (biomesObject != null)
            {
                Island island = this.getAddon().getIslands().getIsland(this.getWorld(), user);

                if (island == null)
                {
                    Utils.sendMessage(user, user.getTranslation("general.errors.no-island"));
                    return false;
                }

                BiomesIslandDataObject data = addonManager.getIslandData(island);

                if (data == null)
                {
                    Utils.sendMessage(user, user.getTranslation("general.errors.no-island"));
                    return false;
                }

                if (addonManager.canPurchaseGenerator(user, island, data, biomesObject))
                {
                    addonManager.purchaseGenerator(user, data, biomesObject);
                    return true;
                }

                return false;
            }
            else
            {
                this.showHelp(this, user);
                return false;
            }
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
            if (args.size() > 3)
            {
                // Show biome for first tab.
                return Optional.of(new ArrayList<>());
            }

            final List<String> returnList = new ArrayList<>();

            // Create suggestions with all biomes that is available for users.

            this.<BiomesAddon>getAddon().getAddonManager().getBiomes(this.getWorld()).forEach(biomesObject ->
                returnList.add(biomesObject.getUniqueId().substring(Utils.getGameMode(this.getWorld()).length() + 1)));

            return Optional.of(Util.tabLimit(returnList, args.get(args.size() - 1)));
        }
    }
}
