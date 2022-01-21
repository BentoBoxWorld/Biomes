///
// Created by BONNe
// Copyright - 2022
///


package world.bentobox.biomes.tasks;


import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentLinkedQueue;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.biomes.BiomesAddon;


/**
 * This class process a queue for biome update on islands.
 */
public class UpdateQueue
{
    /**
     * Initializes biome update queue.
     * @param addon Biomes Addon instance.
     */
    public UpdateQueue(BiomesAddon addon)
    {
        this.addon = addon;
        this.processQueue = new ConcurrentLinkedQueue<>();
        this.processStartMap = new HashMap<>();

        this.task = Bukkit.getScheduler().runTaskTimer(BentoBox.getInstance(), () -> {
            if (!BentoBox.getInstance().isEnabled() || !this.addon.isEnabled())
            {
                this.cancel();
                return;
            }

            // Complete the current to Process queue first
            if (!this.processStartMap.isEmpty() || this.processQueue.isEmpty())
            {
                return;
            }

            for (int i = 0; i < this.addon.getSettings().getConcurrentBiomeUpdates() && !this.processQueue.isEmpty(); i++)
            {
                BiomeUpdateTask updateTask = this.processQueue.poll();
                updateTask.notifyStarting();
                this.processStartMap.put(updateTask, System.currentTimeMillis());
                // Start the scanning of an area chunks
                updateTask.processBiomeChange(this);
            }
        }, 1L, 10L);
    }


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


    /**
     * This class stops current executing queue.
     */
    private void cancel()
    {
        this.task.cancel();
    }


    /**
     * Gets process queue.
     *
     * @return the process queue
     */
    public Queue<BiomeUpdateTask> getProcessQueue()
    {
        return this.processQueue;
    }


    /**
     * Gets process start map.
     *
     * @return the process start map
     */
    public Map<BiomeUpdateTask, Long> getProcessStartMap()
    {
        return this.processStartMap;
    }


    /**
     * Gets task.
     *
     * @return the task
     */
    public BukkitTask getTask()
    {
        return this.task;
    }


    /**
     * Add update task completion stage.
     *
     * @param task the task
     * @return the completion stage
     */
    public CompletionStage<Result> addUpdateTask(BiomeUpdateTask task)
    {
        this.processQueue.add(task);
        return task.getResult();
    }


// ---------------------------------------------------------------------
// Section: Emums
// ---------------------------------------------------------------------


    /**
     * The enum Result.
     */
    public enum Result
    {
        /**
         * Result is finished
         */
        FINISHED,
        /**
         * Result if change timed out
         */
        TIMEOUT,
        /**
         * Result if change failed
         */
        FAILED
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


    /**
     * Instance of the Biomes Addon.
     */
    private final BiomesAddon addon;

    /**
     * This queue contains elements that are processed by current process.
     */
    private final Queue<BiomeUpdateTask> processQueue;

    /**
     * This map links processed element with a time when it was started.
     */
    private final Map<BiomeUpdateTask, Long> processStartMap;

    /**
     * Main bukkit task that process queue.
     */
    private final BukkitTask task;
}
