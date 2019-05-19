package world.bentobox.biomes.tasks;


import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.scheduler.BukkitRunnable;

import world.bentobox.bentobox.api.user.User;
import world.bentobox.biomes.BiomesAddon;
import world.bentobox.biomes.database.objects.BiomesObject;


/**
 * This class updates biome from min coordinate till max coordinate.
 */
public class BiomeUpdateTask extends BukkitRunnable
{
    public BiomeUpdateTask(BiomesAddon addon, User user, World world, BiomesObject biome)
    {
        this.addon = addon;
        this.user = user;
        this.world = world;
        this.biome = biome;
    }


    @Override
    public void run()
    {
        this.user.sendMessage("biomes.messages.update-start");

        // Update world coordinates with new biomes.

        Biome newBiome = this.biome.getBiome();

        for (int x = this.minX; x <= this.maxX; x++)
        {
            for (int z = this.minZ; z <= this.maxZ; z++)
            {
                this.world.setBiome(x, z, newBiome);
            }
        }

        this.user.sendMessage("biomes.messages.update-done",
                "[biome]",
                this.biome.getFriendlyName());

        if (user.isPlayer()) {
            this.addon.log(this.user.getName() + " change biome to " +
                    this.biome.getBiome() + " from x=" + this.minX + ":" + this.maxX + " z=" + this.minZ + ":" + this.maxZ +
                    " while standing on x=" + this.user.getLocation().getBlockX() + " z=" + this.user.getLocation().getBlockZ());
        } else {
            this.addon.log("Console changed biome to " +
                    this.biome.getBiome() + " from x=" + this.minX + ":" + this.maxX + " z=" + this.minZ + ":" + this.maxZ);
        }
    }


    // ---------------------------------------------------------------------
    // Section: Setters
    // ---------------------------------------------------------------------


    /**
     * Default Setter.
     * @param minX Integer.
     */
    public void setMinX(int minX)
    {
        this.minX = minX;
    }


    /**
     * Default Setter.
     * @param maxX Integer.
     */
    public void setMaxX(int maxX)
    {
        this.maxX = maxX;
    }


    /**
     * Default Setter.
     * @param minZ Integer.
     */
    public void setMinZ(int minZ)
    {
        this.minZ = minZ;
    }


    /**
     * Default Setter.
     * @param maxZ Integer.
     */
    public void setMaxZ(int maxZ)
    {
        this.maxZ = maxZ;
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------

    private BiomesAddon addon;

    private User user;

    private World world;

    private int minX;

    private int maxX;

    private int minZ;

    private int maxZ;

    private BiomesObject biome;
}
