///
// Created by BONNe
// Copyright - 2022
///


package world.bentobox.biomes.commands;


import java.util.Iterator;
import java.util.List;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.biomes.BiomesAddon;
import world.bentobox.biomes.config.Settings;
import world.bentobox.biomes.database.objects.BiomesObject;
import world.bentobox.biomes.utils.Constants;
import world.bentobox.biomes.utils.Utils;


/**
 * The over class for Biomes addon commands that contains some useful methods.
 */
public abstract class BiomesCompositeCommand extends CompositeCommand
{
    /**
     * Instantiates a new Biomes composite command.
     *
     * @param addon the addon
     * @param parentCommand the parent command
     * @param label the label
     * @param aliases the aliases
     */
    public BiomesCompositeCommand(BiomesAddon addon,
        CompositeCommand parentCommand,
        String label,
        String... aliases)
    {
        super(addon, parentCommand, label, aliases);
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
}
