///
// Created by BONNe
// Copyright - 2022
///

package world.bentobox.biomes.tasks;


import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.util.BlockVector;

import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.paperlib.PaperLib;
import world.bentobox.bentobox.util.Util;
import world.bentobox.biomes.BiomesAddon;
import world.bentobox.biomes.database.objects.BiomesObject;
import world.bentobox.biomes.utils.Constants;
import world.bentobox.biomes.utils.Utils;


/**
 * This class updates biome from min coordinate till max coordinate.
 */
public class BiomeUpdateTask
{
    /**
     * Default Update task constructor.
     *
     * @param addon BiomeAddon object.
     * @param user Player who calls biome update.
     * @param biome BiomeObject that will changed.
     */
    public BiomeUpdateTask(BiomesAddon addon,
        User user,
        BiomesObject biome)
    {
        this.addon = addon;
        this.user = user;

        this.biome = biome.getBiome();
        this.biomesObject = biome;

        this.chunksToUpdate = new ConcurrentLinkedQueue<>();
        this.result = new CompletableFuture<>();

        this.processCounter = new AtomicInteger(0);
    }


    /**
     * Update chunk queue based on min and max coordinate.
     */
    public void updateChunkQueue()
    {
        if (this.minCoordinate == null || this.maxCoordinate == null)
        {
            return;
        }

        for (int x = this.minCoordinate.getBlockX() >> 4, maxX = this.maxCoordinate.getBlockX() >> 4; x <= maxX; x++)
        {
            for (int z = this.minCoordinate.getBlockZ() >> 4, maxZ = this.maxCoordinate.getBlockZ() >> 4; z <= maxZ;
                z++)
            {
                this.chunksToUpdate.add(this.constructChunkData(x, z));
            }
        }

        this.numberOfChunks = this.chunksToUpdate.size();
    }


    /**
     * Process biome change.
     *
     * @param updateQueue the update queue
     */
    public void processBiomeChange(UpdateQueue updateQueue)
    {
        // Scan the next chunk
        this.scanNextChunk().thenAccept(result ->
        {
            if (!PaperLib.isPaper() && !Bukkit.isPrimaryThread())
            {
                this.addon.logError("scanChunk not on Primary Thread!");
            }

            long runTime = System.currentTimeMillis() - updateQueue.getProcessStartMap().get(this);

            // Timeout check
            if (runTime > this.addon.getSettings().getChangeTimeout() * 60000)
            {
                // Done
                updateQueue.getProcessStartMap().remove(this);
                this.result.complete(UpdateQueue.Result.TIMEOUT);
                this.addon.logError(
                    "Biome change timed out after " + this.addon.getSettings().getChangeTimeout() + "m for user: " +
                        this.user.getName());
                return;
            }

            if (Boolean.TRUE.equals(result) && !updateQueue.getTask().isCancelled())
            {
                // Increment counter for the biome change.
                this.processCounter.incrementAndGet();

                if (this.chunksToUpdate.isEmpty())
                {
                    // Done there are no more things to process
                    updateQueue.updateTimer(runTime, this.getNumberOfChunks());
                    updateQueue.getProcessStartMap().remove(this);

                    this.result.complete(UpdateQueue.Result.FINISHED);
                }
                else
                {
                    // scanNextChunk returns true if there are more chunks to scan
                    this.processBiomeChange(updateQueue);
                }
            }
            else
            {
                // Done, result returned false.
                updateQueue.getProcessStartMap().remove(this);
                this.result.complete(UpdateQueue.Result.FAILED);
            }
        });
    }


    /**
     * Scan the next chunk on the island
     *
     * @return completable boolean future that will be true if more chunks are left to be scanned, and false if not
     */
    private CompletableFuture<Boolean> scanNextChunk()
    {
        if (this.chunksToUpdate.isEmpty())
        {
            this.addon.logError("Unexpected: no chunks to scan!");
            // This should not be needed, but just in case
            return CompletableFuture.completedFuture(false);
        }

        // Set up the completed
        CompletableFuture<Boolean> completed = new CompletableFuture<>();

        ChunkData chunkData = this.chunksToUpdate.poll();

        chunkData.getChunk(this.world).thenAccept(chunk ->
            this.scanChunk(chunkData, chunk).thenAccept(completed::complete));

        return completed;
    }


    /**
     * This method scans given chunk and updates biome in it based on chunk data.
     *
     * @param chunkData the chunk data
     * @param chunk the chunk
     * @return the completable future
     */
    private CompletableFuture<Boolean> scanChunk(ChunkData chunkData, Chunk chunk)
    {
        CompletableFuture<Boolean> completed = new CompletableFuture<>();

        if (chunk == null)
        {
            completed.complete(false);
            return completed;
        }

        if (PaperLib.isPaper())
        {
            Bukkit.getScheduler().runTaskAsynchronously(this.addon.getPlugin(),
                () -> this.runBiomeChange(chunkData, chunk, completed));
        }
        else
        {
            Bukkit.getScheduler().runTask(this.addon.getPlugin(),
                () -> this.runBiomeChange(chunkData, chunk, completed));
        }

        return completed;
    }


    /**
     * This method changes biome for given chunk.
     *
     * @param chunkData the chunk data
     * @param chunk the chunk
     * @param completed the completed
     */
    @SuppressWarnings("deprecated")
    private void runBiomeChange(ChunkData chunkData, Chunk chunk, CompletableFuture<Boolean> completed)
    {
        for (int x = chunkData.minX();
            x <= chunkData.maxX();
            x += 4)
        {
            for (int z = chunkData.minZ();
                z <= chunkData.maxZ();
                z += 4)
            {
                for (int y = chunkData.minY();
                    y <= chunkData.maxY();
                    y += 4)
                {
                    // Biome should not be changed in Greenhouses.
                    if (!this.addon.getAddonManager().hasGreenhouseInLocation(this.world, x, y, z))
                    {
                        // Change Biome
                        this.world.setBiome(x, y, z, this.biome);
                    }
                }
            }
        }

        if (this.addon.getSettings().isUseChunkRefresh())
        {
            chunk.getWorld().refreshChunk(chunk.getX(), chunk.getZ());
        }

        completed.complete(true);
    }


    /**
     * Gets chunk data.
     *
     * @param chunkX the chunk x
     * @param chunkZ the chunk z
     * @return the chunk data
     */
    private ChunkData constructChunkData(int chunkX, int chunkZ)
    {
        int minX, minZ, maxX, maxZ;

        // transform to chunk coordinates
        if (this.minCoordinate.getBlockX() >> 4 == chunkX)
        {
            minX = this.minCoordinate.getBlockX();
        }
        else
        {
            minX = chunkX << 4;
        }

        if (this.maxCoordinate.getBlockX() >> 4 == chunkX)
        {
            maxX = this.maxCoordinate.getBlockX();
        }
        else
        {
            maxX = (chunkX << 4) + 15;
        }

        if (this.minCoordinate.getBlockZ() >> 4 == chunkZ)
        {
            minZ = this.minCoordinate.getBlockZ();
        }
        else
        {
            minZ = chunkZ << 4;
        }

        if (this.maxCoordinate.getBlockZ() >> 4 == chunkZ)
        {
            maxZ = this.maxCoordinate.getBlockZ();
        }
        else
        {
            maxZ = (chunkZ << 4) + 15;
        }

        return new ChunkData(chunkX, chunkZ,
            minX, this.minCoordinate.getBlockY(), minZ,
            maxX, this.maxCoordinate.getBlockY(), maxZ);
    }


// ---------------------------------------------------------------------
// Section: Setters
// ---------------------------------------------------------------------


    /**
     * This method sets the world value.
     *
     * @param world the world new value.
     */
    public void setWorld(World world)
    {
        this.world = world;
    }


    /**
     * Sets min coordinate.
     *
     * @param minCoordinate the min coordinate
     */
    public void setMinCoordinate(BlockVector minCoordinate)
    {
        this.minCoordinate = minCoordinate;
    }


    /**
     * Sets max coordinate.
     *
     * @param maxCoordinate the max coordinate
     */
    public void setMaxCoordinate(BlockVector maxCoordinate)
    {
        this.maxCoordinate = maxCoordinate;
    }


// ---------------------------------------------------------------------
// Section: Getters
// ---------------------------------------------------------------------


    /**
     * Gets result.
     *
     * @return the result
     */
    public CompletableFuture<UpdateQueue.Result> getResult()
    {
        return this.result;
    }


    /**
     * Gets number of chunks.
     *
     * @return the number of chunks
     */
    public int getNumberOfChunks()
    {
        return this.numberOfChunks;
    }


    /**
     * Notify that biome is added in processing queue.
     */
    public void notifyStarting()
    {
        Utils.sendMessage(this.user, this.user.getTranslation(
            Constants.MESSAGES + "update-start",
            "[biome]", this.biomesObject.getFriendlyName(),
            "[number]", String.valueOf(this.getNumberOfChunks()),
            "[time]", String.valueOf((int)
                Math.max(1, this.getNumberOfChunks() * this.addon.getUpdateQueue().getChunkTime()))));
    }


    /**
     * Notify that biome is added in waiting queue.
     */
    public void notifyWaiting()
    {
        Utils.sendMessage(this.user,
            this.user.getTranslation(Constants.MESSAGES + "waiting",
                "[time]", String.valueOf(this.addon.getUpdateQueue().getQueueTime())));
    }


// ---------------------------------------------------------------------
// Section: Classes
// ---------------------------------------------------------------------


    /**
     * The record that contains info about chunk coordinates and limit of blocks where biome must be updated inside
     * chunk.
     */
    private record ChunkData(int chunkX, int chunkZ, int minX, int minY, int minZ, int maxX, int maxY, int maxZ)
    {
        /**
         * Gets chunk.
         *
         * @param world the world
         * @return the chunk
         */
        public CompletableFuture<Chunk> getChunk(World world)
        {
            return Util.getChunkAtAsync(world, this.chunkX, this.chunkZ, true);
        }
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


    /**
     * Instance of biomes addon.
     */
    private final BiomesAddon addon;

    /**
     * The queue of chunk id's that must be updated.
     */
    private final Queue<ChunkData> chunksToUpdate;

    /**
     * Instance of user who will be informed about update task.
     */
    private final User user;

    /**
     * Instance of results exporter.
     */
    private final CompletableFuture<UpdateQueue.Result> result;

    /**
     * Instance of AtomicInteger that counts processed chunks.
     */
    private final AtomicInteger processCounter;

    /**
     * Instance of biome that is required to be changed.
     */
    private final Biome biome;

    /**
     * Instance of biomesObject that is required to be changed.
     */
    private final BiomesObject biomesObject;

    /**
     * Instance of world where update will be processed.
     */
    private World world;

    /**
     * Minimal Coordinate.
     */
    private BlockVector minCoordinate;

    /**
     * Maximal Coordinate.
     */
    private BlockVector maxCoordinate;

    /**
     * The Number of chunks.
     */
    private int numberOfChunks;
}
