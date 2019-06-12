package world.bentobox.biomes.commands.admin;


import java.util.*;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;
import world.bentobox.biomes.BiomesAddon;
import world.bentobox.biomes.commands.ExpandedCompositeCommand;
import world.bentobox.biomes.database.objects.BiomesObject;
import world.bentobox.biomes.config.Settings.UpdateMode;
import world.bentobox.biomes.panels.admin.ListUsersGUI;
import world.bentobox.biomes.tasks.BiomeUpdateHelper;

/**
 * This is admin command that allows to change user biomes.
 */
public class SetBiomeCommand extends ExpandedCompositeCommand
{
    public SetBiomeCommand(Addon addon, CompositeCommand parent)
    {
        super(addon, parent, "set");
    }

    @Override
    public void setup()
    {
        this.setPermission("admin.biomes.set");
        this.setParametersHelp("biomes.commands.admin.set.parameters");
        this.setDescription("biomes.commands.admin.set.description");
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
        else
        {
            User targetUser = this.getPlayer(args, 0, user);
            BiomesObject biome = this.getBiomeObject(args, 1, user);
            UpdateMode updateMode = this.getUpdateMode(args, 2, user);
            int size = this.getUpdateRange(args, 3, user);

            if (targetUser != null && biome != null && updateMode != null && size >= 1)
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
            // Shows admin panel
            new ListUsersGUI(this.addon,
                    this.getWorld(),
                    user,
                    this.getTopLabel(),
                    this.getPermissionPrefix()).build();
            return true;
        }
        else
        {
            User targetUser = this.getPlayer(args, 0, user);
            BiomesObject biome = this.getBiomeObject(args, 1, user);
            UpdateMode updateMode = this.getUpdateMode(args, 2, user);
            int size = this.getUpdateRange(args, 3, user);

            // Use BiomeUpdateHelper to change biome for user.
            BiomeUpdateHelper helper = new BiomeUpdateHelper(this.addon,
                user,
                targetUser,
                biome,
                getWorld(),
                updateMode,
                size,
                false);

            if (helper.canChangeBiome())
            {
                helper.updateIslandBiome();
                return true;
            }

            return false;
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
                returnList.addAll(Util.getOnlinePlayerList(user));
                break;
            case 4:
                String worldName = this.getWorld() != null && Util.getWorld(this.getWorld()) != null ?
                    Util.getWorld(this.getWorld()).getName() : "";

                // Create suggestions with all biomes that is available for users.
                this.addon.getAddonManager().getBiomes(worldName).forEach(biomesObject -> {
                    returnList.add(biomesObject.getUniqueId().replaceFirst(worldName + "-", ""));
                });

                break;
            case 5:
                // Create suggestions with all update modes that is available for users.
                Arrays.stream(UpdateMode.values()).
                    map(Enum::name).
                    forEach(returnList::add);

                if (!BiomesAddon.BIOMES_WORLD_PROTECTION.isSetForWorld(this.getWorld()))
                {
                    // Do not suggest island as it is not valid option
                    returnList.remove(UpdateMode.ISLAND.name());
                }

                break;
            case 6:
                if (lastString.isEmpty() || lastString.matches("[0-9]*"))
                {
                    returnList.add("<number>");
                }

                break;
            default:
            {
                break;
            }
        }

        return Optional.of(Util.tabLimit(returnList, lastString));
    }
}
