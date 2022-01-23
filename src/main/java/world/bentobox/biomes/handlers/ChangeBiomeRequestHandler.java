package world.bentobox.biomes.handlers;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;

import world.bentobox.bentobox.api.addons.request.AddonRequestHandler;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;
import world.bentobox.biomes.BiomesAddon;
import world.bentobox.biomes.config.Settings;
import world.bentobox.biomes.database.objects.BiomesObject;
import world.bentobox.biomes.tasks.BiomeUpdateHelper;


/**
 * This Request Handler returns if requested inputdata can be enough to change biome.
 */
public class ChangeBiomeRequestHandler extends AddonRequestHandler
{
    /**
     * Constructor creates a new ChangeBiomeRequestHandler instance.
     *
     * @param addon of type ChallengesAddon
     */
    public ChangeBiomeRequestHandler(BiomesAddon addon)
    {
        super("biome-request-change");
        this.addon = addon;
    }


    /**
     * Handle object.
     *
     * @param metaData Required meta data.
     * @return Set of strings that contains completed challenges.
     * @see AddonRequestHandler#handle(Map AddonRequestHandler AddonRequestHandler#handle(Map
     * AddonRequestHandlerhandleMapltString,Objectgt)
     */
    @Override
    public Object handle(Map<String, Object> metaData)
    {
		/*
            What we need in the metaData:
            	0. "player" -> UUID that represents targeted player UUID.
				1. "world-name" -> String that represents world name where biome must be changed
				2. "biomeId" -> String that represents biome unique ID.
			What you can specify more in metaData:
				0. "updateMode" -> String that represents how biome is necessary to be changed.
				1. "range" -> Integer that represents range of biome update change.
				2. "checkRequirements" -> Boolean that represents if requirements must be checked or not. By default it is true.
				3. "withdraw" -> Boolean that indicates that money will be withdraw from players account. By default it is true.
            What we will return:
            	- Map that contains:
            	  1. key "status" which is boolean that indicate if biome change was successful.
            	  2. key "reason" which is string that returns errror message.
         */


        Map<String, Object> returnMap = new HashMap<>(2);

        if (metaData == null || metaData.isEmpty())
        {
            returnMap.put(STATUS, false);
            returnMap.put(REASON, "Given MetaData map is not defined!");
        }
        else if (!metaData.containsKey(WORLD_NAME) ||
            !(metaData.get(WORLD_NAME) instanceof String) ||
            Bukkit.getWorld((String) metaData.get(WORLD_NAME)) == null)
        {
            returnMap.put(STATUS, false);
            returnMap.put(REASON, "Missing 'world-name' or it is not valid!");
        }
        else if (!metaData.containsKey(PLAYER) ||
            !(metaData.get(PLAYER) instanceof UUID))
        {
            returnMap.put(STATUS, false);
            returnMap.put(REASON, "Missing 'player' or it is not valid!");
        }
        else if (!metaData.containsKey(BIOME_ID) ||
            !(metaData.get(BIOME_ID) instanceof String) ||
            this.addon.getAddonManager().getBiomeByID((String) metaData.get(BIOME_ID)) == null)
        {
            returnMap.put(STATUS, false);
            returnMap.put(REASON, "Missing 'biomeId' or it is not valid!");
        }
        else
        {
            World world = Util.getWorld(Bukkit.getWorld((String) metaData.get(WORLD_NAME)));

            UUID player = (UUID) metaData.get(PLAYER);
            BiomesObject biome = this.addon.getAddonManager().
                getBiomeByID((String) metaData.get(BIOME_ID));

            // Get Update Mode.

            Settings.UpdateMode mode = metaData.containsKey(UPDATE_MODE) &&
                metaData.get(UPDATE_MODE) instanceof String &&
                Settings.UpdateMode.getMode((String) metaData.get(UPDATE_MODE)) != null ?
                Settings.UpdateMode.getMode((String) metaData.get(UPDATE_MODE)) :
                this.addon.getSettings().getDefaultMode();

            // Get Update Range.

            int range = metaData.containsKey(RANGE) &&
                metaData.get(RANGE) instanceof Integer ? (int) metaData.get(RANGE) :
                this.addon.getSettings().getDefaultSize();

            // Get Requirement Checking

            boolean checkRequirements = !metaData.containsKey(CHECK_REQ) ||
                !(metaData.get(CHECK_REQ) instanceof Boolean) ||
                (boolean) metaData.get(CHECK_REQ);

            // Get Withdraw value

            boolean withdraw = !metaData.containsKey(WITHDRAW) ||
                !(metaData.get(WITHDRAW) instanceof Boolean) ||
                (boolean) metaData.get(WITHDRAW);

            BiomeUpdateHelper helper = new BiomeUpdateHelper(this.addon,
                User.getInstance(player),
                User.getInstance(player),
                biome,
                this.addon.getAddonManager().getIslandData(world, User.getInstance(player)),
                world,
                mode,
                range,
                withdraw);

            if (checkRequirements)
            {
                if (helper.canChangeBiome())
                {
                    helper.updateIslandBiome();

                    returnMap.put(STATUS, true);
                    returnMap.put(REASON, "Biome is updated by checking all requirements!");
                }
                else
                {
                    returnMap.put(STATUS, false);
                    returnMap.put(REASON, "Player does not met requirements for biome changing!");
                }
            }
            else
            {
                helper.updateIslandBiome();

                returnMap.put(STATUS, true);
                returnMap.put(REASON, "Biome is updated by skipping all requirements!");
            }
        }

        return returnMap;
    }


    /**
     * Variable stores biomes addon.
     */
    private final BiomesAddon addon;

    /**
     * The constant STATUS.
     */
    private static final String STATUS = "status";

    /**
     * The constant REASON.
     */
    private static final String REASON = "status";

    /**
     * The constant WORLD_NAME.
     */
    private static final String WORLD_NAME = "world-name";

    /**
     * The constant PLAYER.
     */
    private static final String PLAYER = "player";

    /**
     * The constant BIOME_ID.
     */
    private static final String BIOME_ID = "biomeId";

    /**
     * The constant UPDATE_MODE.
     */
    private static final String UPDATE_MODE = "updateMode";

    /**
     * The constant RANGE.
     */
    private static final String RANGE = "range";

    /**
     * The constant CHECK_REQ.
     */
    private static final String CHECK_REQ = "checkRequirements";

    /**
     * The constant WITHDRAW.
     */
    private static final String WITHDRAW = "withdraw";
}
