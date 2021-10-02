package world.bentobox.biomes.utils;


import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.Nullable;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.hooks.LangUtilsHook;
import world.bentobox.bentobox.util.Util;
import world.bentobox.biomes.config.Settings;


/**
 * Util methods used in different situations.
 */
public class Utils
{
    /**
     * This method returns if given user has a permission to change biome to given biomeId
     * with given updateMode.
     * @param user User who must be checked for permissions.
     * @param permissionPrefix String that represents gamemode permission prefix.
     * @param updateMode UpdateMode that will be performed in biome change.
     * @param biomeId BiomeObject it that will be applied.
     * @return {@code true} if user has permissions to change biome with given ID and
     * update mode, {@code false} otherwise.
     */
    public static boolean hasUserUpdateModePermission(User user,
        String permissionPrefix,
        Settings.UpdateMode updateMode,
        String biomeId)
    {
        String updateModePermission =
            permissionPrefix + "biomes.set." + updateMode.name().toLowerCase();

        return user.hasPermission(updateModePermission + ".*") ||
            user.hasPermission(updateModePermission + "." + biomeId.toLowerCase());
    }


    /**
     * Sanitizes the provided input. It replaces spaces and hyphens with underscores and lower cases the input.
     * This code also removes all color codes from the input.
     * @param input input to sanitize
     * @return sanitized input
     */
    public static String sanitizeInput(String input)
    {
        return ChatColor.stripColor(
            Util.translateColorCodes(input.toLowerCase(Locale.ENGLISH).
                replace(" ", "_").
                replace("-", "_")));
    }


    /**
     * This method transforms given World into GameMode name. If world is not a GameMode
     * world then it returns an empty string.
     * @param world World which gameMode name must be found out.
     * @return GameMode name or empty string if not known.
     */
    public static String getGameMode(World world)
    {
        return BentoBox.getInstance().getIWM().getAddon(world).
                map(gameModeAddon -> gameModeAddon.getDescription().getName()).
                orElse("");
    }


    /**
     * This method normalizes given coordinate to factor 4.
     * In Minecraft 1.15 and 1.16, biomes are stored in 4x4x4 blocks.
     * @param coordinate Input coordinate.
     * @return Normalized coordinate.
     */
    public static int normalizeBy4(int coordinate)
    {
        return (coordinate >> 2) << 2;
    }


    /**
     * This method allows to get next value from array list after given value.
     * @param values Array that should be searched for given value.
     * @param currentValue Value which next element should be found.
     * @param <T> Instance of given object.
     * @return Next value after currentValue in values array.
     */
    public static <T> T getNextValue(T[] values, T currentValue)
    {
        for (int i = 0; i < values.length; i++)
        {
            if (values[i].equals(currentValue))
            {
                if (i + 1 == values.length)
                {
                    return values[0];
                }
                else
                {
                    return values[i + 1];
                }
            }
        }

        return currentValue;
    }


    /**
     * This method allows to get previous value from array list after given value.
     * @param values Array that should be searched for given value.
     * @param currentValue Value which previous element should be found.
     * @param <T> Instance of given object.
     * @return Previous value before currentValue in values array.
     */
    public static <T> T getPreviousValue(T[] values, T currentValue)
    {
        for (int i = 0; i < values.length; i++)
        {
            if (values[i].equals(currentValue))
            {
                if (i > 0)
                {
                    return values[i - 1];
                }
                else
                {
                    return values[values.length - 1];
                }
            }
        }

        return currentValue;
    }


    /**
     * This method returns map that contains biomes name as key and biome as value.
     * @return Map that contains relation from biome name to biome.
     */
    public static Map<String, Biome> getBiomeNameMap()
    {
        Biome[] biomes = Biome.values();

        Map<String, Biome> returnMap = new HashMap<>(biomes.length);

        for (Biome biome : biomes)
        {
            returnMap.put(biome.name(), biome);
        }

        return returnMap;
    }


    /**
     * This method returns if current biome is locally detected as snowy biome.
     * @param biome Biome that must be checked.
     * @return {@code true} if I think it is snowy biome, {@code false} otherwise.
     */
    public static boolean isSnowyBiome(Biome biome)
    {
        return switch (biome) {
            case SNOWY_TUNDRA,
                ICE_SPIKES,
                SNOWY_TAIGA,
                SNOWY_TAIGA_MOUNTAINS,
                FROZEN_RIVER,
                SNOWY_BEACH -> true;
            default -> false;
        };
    }


    /**
     * This method returns if current biome is locally detected as cold biome.
     * @param biome Biome that must be checked.
     * @return {@code true} if I think it is cold biome, {@code false} otherwise.
     */
    public static boolean isColdBiome(Biome biome)
    {
        return switch (biome) {
            case MOUNTAINS,
                GRAVELLY_MOUNTAINS,
                WOODED_MOUNTAINS,
                MODIFIED_GRAVELLY_MOUNTAINS,
                TAIGA,
                TAIGA_MOUNTAINS,
                GIANT_TREE_TAIGA,
                GIANT_SPRUCE_TAIGA,
                STONE_SHORE -> true;
            default -> false;
        };
    }


    /**
     * This method returns if current biome is locally detected as lush biome.
     * @param biome Biome that must be checked.
     * @return {@code true} if I think it is lush biome, {@code false} otherwise.
     */
    public static boolean isLushBiome(Biome biome)
    {
        return switch (biome) {
            case PLAINS,
                SUNFLOWER_PLAINS,
                FOREST,
                FLOWER_FOREST,
                BIRCH_FOREST,
                TALL_BIRCH_FOREST,
                DARK_FOREST,
                DARK_FOREST_HILLS,
                SWAMP,
                SWAMP_HILLS,
                JUNGLE,
                MODIFIED_JUNGLE,
                JUNGLE_EDGE,
                MODIFIED_JUNGLE_EDGE,
                BAMBOO_JUNGLE,
                RIVER,
                BEACH,
                MUSHROOM_FIELDS,
                MUSHROOM_FIELD_SHORE -> true;
            default -> false;
        };
    }


    /**
     * This method returns if current biome is locally detected as dry biome.
     * @param biome Biome that must be checked.
     * @return {@code true} if I think it is dry biome, {@code false} otherwise.
     */
    public static boolean isDryBiome(Biome biome)
    {
        return switch (biome) {
            case DESERT,
                DESERT_LAKES,
                SAVANNA,
                SHATTERED_SAVANNA,
                BADLANDS,
                ERODED_BADLANDS,
                WOODED_BADLANDS_PLATEAU,
                MODIFIED_WOODED_BADLANDS_PLATEAU,
                BADLANDS_PLATEAU,
                SAVANNA_PLATEAU,
                MODIFIED_BADLANDS_PLATEAU,
                SHATTERED_SAVANNA_PLATEAU -> true;
            default -> false;
        };
    }


    /**
     * This method returns if current biome is locally detected as ocean biome.
     * @param biome Biome that must be checked.
     * @return {@code true} if I think it is ocean biome, {@code false} otherwise.
     */
    public static boolean isOceanBiome(Biome biome)
    {
        return switch (biome) {
            case WARM_OCEAN,
                LUKEWARM_OCEAN,
                DEEP_LUKEWARM_OCEAN,
                OCEAN,
                DEEP_OCEAN,
                COLD_OCEAN,
                DEEP_COLD_OCEAN,
                FROZEN_OCEAN,
                DEEP_FROZEN_OCEAN -> true;
            default -> false;
        };
    }


    /**
     * This method returns if current biome is locally detected as neutral biome.
     * @param biome Biome that must be checked.
     * @return {@code true} if I think it is neutral biome, {@code false} otherwise.
     */
    public static boolean isNeutralBiome(Biome biome)
    {
        return switch (biome) {
            case THE_VOID,
                WOODED_HILLS,
                TAIGA_HILLS,
                SNOWY_TAIGA_HILLS,
                JUNGLE_HILLS,
                DESERT_HILLS,
                BIRCH_FOREST_HILLS,
                TALL_BIRCH_HILLS,
                GIANT_TREE_TAIGA_HILLS,
                GIANT_SPRUCE_TAIGA_HILLS,
                SNOWY_MOUNTAINS -> true;
            default -> false;
        };
    }


    /**
     * This method returns if current biome is locally detected as unused biome.
     * @param biome Biome that must be checked.
     * @return {@code true} if I think it is unused biome, {@code false} otherwise.
     */
    public static boolean isUnusedBiome(Biome biome)
    {
        return switch (biome) {
            case MOUNTAIN_EDGE,
                DEEP_WARM_OCEAN -> true;
            default -> false;
        };
    }


    /**
     * This method returns if current biome is locally detected as nether biome.
     * @param biome Biome that must be checked.
     * @return {@code true} if I think it is nether biome, {@code false} otherwise.
     */
    public static boolean isNetherBiome(Biome biome)
    {
        return switch (biome) {
            case NETHER_WASTES,
                SOUL_SAND_VALLEY,
                CRIMSON_FOREST,
                WARPED_FOREST,
                BASALT_DELTAS -> true;
            default -> false;
        };
    }


    /**
     * This method returns if current biome is locally detected as the end biome.
     * @param biome Biome that must be checked.
     * @return {@code true} if I think it is the end biome, {@code false} otherwise.
     */
    public static boolean isTheEndBiome(Biome biome)
    {
        return switch (biome) {
            case THE_END,
                SMALL_END_ISLANDS,
                END_MIDLANDS,
                END_HIGHLANDS,
                END_BARRENS -> true;
            default -> false;
        };
    }


    /**
     * Send given message to user and add prefix to the start of the message.
     *
     * @param user User who need to receive message.
     * @param message String of message that must be send.
     */
    public static void sendMessage(User user, String message)
    {
        user.sendMessage(user.getTranslation(Constants.CONVERSATIONS + "prefix") + message);
    }


    /**
     * Prettify World.Environment object for user.
     * @param object Object that must be pretty.
     * @param user User who will see the object.
     * @return Prettified string for World.Environment.
     */
    public static String prettifyObject(World.Environment object, User user)
    {
        // Find addon structure with:
        // [addon]:
        //   environments:
        //     [environment]:
        //       name: [name]
        String translation = user.getTranslationOrNothing(Constants.ENVIRONMENTS + object.name().toLowerCase() + ".name");

        if (!translation.isEmpty())
        {
            // We found our translation.
            return translation;
        }

        // Find addon structure with:
        // [addon]:
        //   environments:
        //     [environment]: [name]

        translation = user.getTranslationOrNothing(Constants.ENVIRONMENTS + object.name().toLowerCase());

        if (!translation.isEmpty())
        {
            // We found our translation.
            return translation;
        }

        // Find general structure with:
        // environments:
        //   [environment]: [name]

        translation = user.getTranslationOrNothing("environments." + object.name().toLowerCase());

        if (!translation.isEmpty())
        {
            // We found our translation.
            return translation;
        }

        // Lang Utils do not have Environment :(
        //LangUtilsHook.getEnvrionmentName(object, user);

        // Nothing was found. Use just a prettify text function.
        return Util.prettifyText(object.name());
    }


    /**
     * Prettify World.Environment object description for user.
     * @param object Object that must be pretty.
     * @param user User who will see the object.
     * @return Prettified description string for World.Environment.
     */
    public static String prettifyDescription(World.Environment object, User user)
    {
        // Find addon structure with:
        // [addon]:
        //   environments:
        //     [environment]:
        //       description: [text]
        String translation = user.getTranslationOrNothing(Constants.ENVIRONMENTS + object.name().toLowerCase() + ".description");

        if (!translation.isEmpty())
        {
            // We found our translation.
            return translation;
        }

        // No text to return.
        return "";
    }


    /**
     * Prettify Material object for user.
     * @param object Object that must be pretty.
     * @param user User who will see the object.
     * @return Prettified string for Material.
     */
    public static String prettifyObject(@Nullable Material object, User user)
    {
        // Nothing to translate
        if (object == null)
        {
            return "";
        }

        // Find addon structure with:
        // [addon]:
        //   materials:
        //     [material]:
        //       name: [name]
        String translation = user.getTranslationOrNothing(Constants.MATERIALS + object.name().toLowerCase() + ".name");

        if (!translation.isEmpty())
        {
            // We found our translation.
            return translation;
        }

        // Find addon structure with:
        // [addon]:
        //   materials:
        //     [material]: [name]

        translation = user.getTranslationOrNothing(Constants.MATERIALS + object.name().toLowerCase());

        if (!translation.isEmpty())
        {
            // We found our translation.
            return translation;
        }

        // Find general structure with:
        // materials:
        //   [material]: [name]

        translation = user.getTranslationOrNothing("materials." + object.name().toLowerCase());

        if (!translation.isEmpty())
        {
            // We found our translation.
            return translation;
        }

        // Use Lang Utils Hook to translate material
        return LangUtilsHook.getMaterialName(object, user);
    }


    /**
     * Prettify Material object description for user.
     * @param object Object that must be pretty.
     * @param user User who will see the object.
     * @return Prettified description string for Material.
     */
    public static String prettifyDescription(@Nullable Material object, User user)
    {
        // Nothing to translate
        if (object == null)
        {
            return "";
        }

        // Find addon structure with:
        // [addon]:
        //   materials:
        //     [material]:
        //       description: [text]
        String translation = user.getTranslationOrNothing(Constants.MATERIALS + object.name().toLowerCase() + ".description");

        if (!translation.isEmpty())
        {
            // We found our translation.
            return translation;
        }

        // No text to return.
        return "";
    }


    /**
     * Prettify Biome object for user.
     * @param object Object that must be pretty.
     * @param user User who will see the object.
     * @return Prettified string for Biome.
     */
    public static String prettifyObject(@Nullable Biome object, User user)
    {
        // Nothing to translate
        if (object == null)
        {
            return "";
        }

        // Find addon structure with:
        // [addon]:
        //   biomes:
        //     [biome]:
        //       name: [name]
        String translation = user.getTranslationOrNothing(Constants.BIOMES + object.name().toLowerCase() + ".name");

        if (!translation.isEmpty())
        {
            // We found our translation.
            return translation;
        }

        // Find addon structure with:
        // [addon]:
        //   biomes:
        //     [biome]: [name]

        translation = user.getTranslationOrNothing(Constants.BIOMES + object.name().toLowerCase());

        if (!translation.isEmpty())
        {
            // We found our translation.
            return translation;
        }

        // Find general structure with:
        // materials:
        //   [biome]: [name]

        translation = user.getTranslationOrNothing("biomes." + object.name().toLowerCase());

        if (!translation.isEmpty())
        {
            // We found our translation.
            return translation;
        }

        // Use Lang Utils Hook to translate material
        return LangUtilsHook.getBiomeName(object, user);
    }


    /**
     * Prettify Biome object description for user.
     * @param object Object that must be pretty.
     * @param user User who will see the object.
     * @return Prettified description string for Biome.
     */
    public static String prettifyDescription(@Nullable Biome object, User user)
    {
        // Nothing to translate
        if (object == null)
        {
            return "";
        }

        // Find addon structure with:
        // [addon]:
        //   biomes:
        //     [biome]:
        //       description: [text]
        String translation = user.getTranslationOrNothing(Constants.BIOMES + object.name().toLowerCase() + ".description");

        if (!translation.isEmpty())
        {
            // We found our translation.
            return translation;
        }

        // No text to return.
        return "";
    }
}
