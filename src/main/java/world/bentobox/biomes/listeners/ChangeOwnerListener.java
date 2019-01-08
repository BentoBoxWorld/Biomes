package world.bentobox.biomes.listeners;


import org.bukkit.block.Biome;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import java.util.Optional;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.events.team.TeamEvent.TeamSetownerEvent;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;
import world.bentobox.biomes.BiomesAddon;
import world.bentobox.biomes.objects.BiomesObject;
import world.bentobox.biomes.objects.Settings.UpdateMode;
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


	@EventHandler(priority = EventPriority.LOW)
	public void onTeamSetOwnerEvent(TeamSetownerEvent event)
	{
		// Do nothing if biome reset is diabled.
		if (!this.addon.getSettings().isResetBiomes())
		{
			return;
		}

		boolean hasPermissions;

		User newUser = User.getInstance(event.getNewOwner());

		Optional<Addon> skyBlock = this.addon.getPlugin().getAddonsManager().getAddonByName("BSkyBlock");
		Optional<Addon> acidIsland = this.addon.getPlugin().getAddonsManager().getAddonByName("AcidIsland");

		String defaultBiome;

		// TODO: The same issue as with BiomesAddon class. It gets values but better would be to add them as
		// dependencies.
		if (skyBlock.isPresent() &&
			skyBlock.get().getConfig().getString("world.world-name").
				equalsIgnoreCase(Util.getWorld(event.getIsland().getWorld()).getName()))
		{
			hasPermissions = newUser.hasPermission("bskyblock.biomes.set");
			defaultBiome = skyBlock.get().getConfig().getString("world.default-biome");
		}
		else if (acidIsland.isPresent() &&
			acidIsland.get().getConfig().getString("world.world-name").
				equalsIgnoreCase(Util.getWorld(event.getIsland().getWorld()).getName()))
		{
			hasPermissions = newUser.hasPermission("acidisland.biomes.set");
			defaultBiome = acidIsland.get().getConfig().getString("world.default-biome");
		}
		else
		{
			// Do nothing if failed to get correct world permission.
			hasPermissions = true;
			defaultBiome = "";
		}

		if (!hasPermissions)
		{
			BiomesObject defaultBiomeObject = this.addon.getAddonManager().getBiomeFromString(defaultBiome);

			if (!defaultBiome.isEmpty() && defaultBiomeObject == null)
			{
				Biome biome = Utils.getBiomeNameMap().getOrDefault(defaultBiome.toUpperCase(), null);

				if (biome == null)
				{
					this.addon.logError("Biome defined in GameMode addon is not valid!!!");
					return;
				}
				else
				{
					defaultBiomeObject = new BiomesObject();
					defaultBiomeObject.setBiomeName(biome.name().toUpperCase());
					defaultBiomeObject.setBiomeID(biome.ordinal());
					defaultBiomeObject.setRequiredCost(0);
					defaultBiomeObject.setRequiredLevel(0);
				}
			}

			// Forcefully update biome on whole user island.
			new BiomeUpdateHelper(this.addon,
				newUser,
				newUser,
				defaultBiomeObject,
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
