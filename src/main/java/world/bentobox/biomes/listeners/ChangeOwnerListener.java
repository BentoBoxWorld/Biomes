package world.bentobox.biomes.listeners;


import java.util.Optional;

import org.bukkit.block.Biome;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.events.team.TeamSetownerEvent;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.biomes.BiomesAddon;
import world.bentobox.biomes.config.Settings.UpdateMode;
import world.bentobox.biomes.database.objects.BiomesObject;
import world.bentobox.biomes.tasks.BiomeUpdateHelper;
import world.bentobox.biomes.utils.Utils;


/**
 * This listener checks if new user can change biomes. If he does not have permission to it, then restore
 * biome to default.
 */
public class ChangeOwnerListener implements Listener
{
    public  ChangeOwnerListener(BiomesAddon addon)
    {
        this.addon = addon;
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onTeamSetOwnerEvent(TeamSetownerEvent event)
    {
        // Do nothing if biome reset is diabled.
        if (!this.addon.getSettings().isResetBiomes())
        {
            return;
        }

        User newUser = User.getInstance(event.getNewOwner());

        Optional<GameModeAddon> gameModeAddon =
                this.addon.getPlugin().getIWM().getAddon(event.getIsland().getWorld());

        final boolean hasPermissions;
        final String defaultBiome;

        if (gameModeAddon.isPresent())
        {
            GameModeAddon addon2 = gameModeAddon.get();
            hasPermissions = newUser.hasPermission(addon2.getPermissionPrefix() + "biomes.set");
            defaultBiome = addon2.getConfig().getString("world.default-biome", "PLAINS");
        }
        else
        {
            // Do nothing if failed to get correct world permission.
            hasPermissions = true;
            defaultBiome = "";
        }

        // It is assumed that biomes.set permission is required to change biome.
        if (!hasPermissions)
        {
            BiomesObject defaultBiomeObject;

            Biome biome = Utils.getBiomeNameMap().getOrDefault(defaultBiome.toUpperCase(), null);

            if (biome == null)
            {
                this.addon.logError("Biome defined in GameMode addon is not valid!!!");
                return;
            }
            else
            {
                defaultBiomeObject = new BiomesObject();
                defaultBiomeObject.setBiome(biome);
            }

            // Forcefully update biome on whole user island.
            new BiomeUpdateHelper(this.addon,
                newUser,
                newUser,
                defaultBiomeObject,
                this.addon.getAddonManager().getIslandData(event.getIsland()),
                event.getIsland().getWorld(),
                UpdateMode.ISLAND,
                1,
                false).updateIslandBiome();
        }
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------


    private BiomesAddon addon;
}
