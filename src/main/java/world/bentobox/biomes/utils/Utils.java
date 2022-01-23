package world.bentobox.biomes.utils;


import java.util.*;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.Nullable;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.configuration.WorldSettings;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.hooks.LangUtilsHook;
import world.bentobox.bentobox.util.Util;
import world.bentobox.biomes.BiomesAddon;
import world.bentobox.biomes.config.Settings;
import world.bentobox.biomes.database.objects.BiomesObject;


/**
 * Util methods used in different situations.
 */
public class Utils
{
    /**
     * This method groups input items in single itemstack with correct amount and returns it.
     * Allows to remove duplicate items from list.
     * @param requiredItems Input item list
     * @param ignoreMetaData Set that contains items which can ignore meta data.
     * @return List that contains unique items that cannot be grouped.
     */
    public static List<ItemStack> groupEqualItems(List<ItemStack> requiredItems, Set<Material> ignoreMetaData)
    {
        List<ItemStack> returnItems = new ArrayList<>(requiredItems.size());

        // Group all equal items in single stack, as otherwise it will be too complicated to check if all
        // items are in players inventory.

        for (ItemStack item : requiredItems)
        {
            boolean isUnique = item != null;

            int i = 0;
            final int requiredSize = returnItems.size();

            while (i < requiredSize && isUnique)
            {
                ItemStack required = returnItems.get(i);

                // Merge items which meta can be ignored or is similar to item in required list.
                if (required.isSimilar(item) ||
                    ignoreMetaData.contains(item.getType()) && item.getType().equals(required.getType()))
                {
                    required.setAmount(required.getAmount() + item.getAmount());
                    isUnique = false;
                }

                i++;
            }

            if (isUnique)
            {
                // The same issue as in other places. Clone prevents from changing original item.
                returnItems.add(item.clone());
            }
        }

        return returnItems;
    }


    /**
     * This method returns if player has required item in his inventory.
     *
     * @param user the user
     * @param requiredItem the required item
     * @param ignoreMetaData the ignore meta data
     * @return {@code true} if item is in his inventory, otherwise {@code false}
     */
    public static boolean hasRequiredItem(@Nullable User user,
        ItemStack requiredItem,
        Set<Material> ignoreMetaData)
    {
        if (user == null)
        {
            // If user is not given, return false.
            return false;
        }
        else if (user.getPlayer().getGameMode() == GameMode.CREATIVE)
        {
            /// If user is in creative menu, return true.
            return true;
        }
        else
        {
            int numInInventory;

            if (user.getInventory() == null)
            {
                // Sanity check. User always has inventory at this point of code.
                numInInventory = 0;
            }
            else if (!ignoreMetaData.isEmpty() && ignoreMetaData.contains(requiredItem.getType()))
            {
                numInInventory = Arrays.stream(user.getInventory().getContents()).
                    filter(Objects::nonNull).
                    filter(i -> i.getType().equals(requiredItem.getType())).
                    mapToInt(ItemStack::getAmount).
                    sum();
            }
            else
            {
                numInInventory = Arrays.stream(user.getInventory().getContents()).
                    filter(Objects::nonNull).
                    filter(i -> i.isSimilar(requiredItem)).
                    mapToInt(ItemStack::getAmount).
                    sum();
            }

            return numInInventory >= requiredItem.getAmount();
        }
    }


    /**
     * This method gets string value of given permission prefix. If user does not have given permission or it have all
     * (*), then return default value.
     *
     * @param user User who's permission should be checked.
     * @param permissionPrefix Prefix that need to be found.
     * @param defaultValue Default value that will be returned if permission not found.
     * @return String value that follows permissionPrefix.
     */
    public static String getPermissionValue(User user, String permissionPrefix, String defaultValue)
    {
        if (user.isPlayer())
        {
            if (permissionPrefix.endsWith("."))
            {
                permissionPrefix = permissionPrefix.substring(0, permissionPrefix.length() - 1);
            }

            String permPrefix = permissionPrefix + ".";

            List<String> permissions = user.getEffectivePermissions().stream().
                map(PermissionAttachmentInfo::getPermission).
                filter(permission -> permission.startsWith(permPrefix)).
                collect(Collectors.toList());

            for (String permission : permissions)
            {
                if (permission.contains(permPrefix + "*"))
                {
                    // * means all. So continue to search more specific.
                    continue;
                }

                String[] parts = permission.split(permPrefix);

                if (parts.length > 1)
                {
                    return parts[1];
                }
            }
        }

        return defaultValue;
    }


    /**
     * This method replaces "[gamemode] and [number] in permission template with a requested gamemode and empty space
     * accordantly.
     *
     * @param world World where permission is operating.
     * @param permissionTemplate permission template.
     * @return Parsed permission String.
     */
    public static String getPermissionString(World world, String permissionTemplate)
    {
        String permissionPrefix = BentoBox.getInstance().getIWM().getPermissionPrefix(world);

        return permissionPrefix.isEmpty() ? permissionTemplate :
            permissionTemplate.replace("[gamemode].", permissionPrefix);
    }


    /**
     * This method returns if given user has all required permissions.
     *
     * @param user User who must be checked.
     * @param permissions List of permissions that must be checked.
     * @return {@code true} if player has all required permissions, {@code flase} otherwise.
     */
    public static boolean matchAllPermissions(User user, Collection<String> permissions)
    {
        return permissions.isEmpty() ||
            user.isOp() ||
            permissions.stream().allMatch(user::hasPermission);
    }


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
     *
     * @param biome Biome that must be checked.
     * @return {@code true} if I think it is snowy biome, {@code false} otherwise.
     */
    public static boolean isSnowyBiome(Biome biome)
    {
        return switch (biome) {
            //case SNOWY_SLOPES:
            case SNOWY_PLAINS,
                SNOWY_TAIGA,
                ICE_SPIKES,
                FROZEN_RIVER,
                SNOWY_BEACH -> true;
            default -> false;
        };
    }


    /**
     * This method returns if current biome is locally detected as cold biome.
     *
     * @param biome Biome that must be checked.
     * @return {@code true} if I think it is cold biome, {@code false} otherwise.
     */
    public static boolean isColdBiome(Biome biome)
    {
        return switch (biome) {
            case WINDSWEPT_HILLS,
                WINDSWEPT_GRAVELLY_HILLS,
                WINDSWEPT_FOREST,
                TAIGA,
                OLD_GROWTH_PINE_TAIGA,
                OLD_GROWTH_SPRUCE_TAIGA,
                STONY_SHORE -> true;
            default -> false;
        };
    }


    /**
     * This method returns if current biome is locally detected as temperate biome.
     *
     * @param biome Biome that must be checked.
     * @return {@code true} if I think it is temperate biome, {@code false} otherwise.
     */
    public static boolean isTemperateBiome(Biome biome)
    {
        return switch (biome) {
            case PLAINS,
                SUNFLOWER_PLAINS,
                FOREST,
                FLOWER_FOREST,
                BIRCH_FOREST,
                OLD_GROWTH_BIRCH_FOREST,
                DARK_FOREST,
                SWAMP,
                JUNGLE,
                SPARSE_JUNGLE,
                BAMBOO_JUNGLE,
                RIVER,
                BEACH,
                MUSHROOM_FIELDS -> true;
            default -> false;
        };
    }


    /**
     * This method returns if current biome is locally detected as warm biome.
     *
     * @param biome Biome that must be checked.
     * @return {@code true} if I think it is warm biome, {@code false} otherwise.
     */
    public static boolean isWarmBiome(Biome biome)
    {
        return switch (biome) {
            // case BADLANDS_PLATEAU:
            case DESERT,
                SAVANNA,
                WINDSWEPT_SAVANNA,
                BADLANDS,
                ERODED_BADLANDS,
                WOODED_BADLANDS,
                SAVANNA_PLATEAU -> true;
            default -> false;
        };
    }


    /**
     * This method returns if current biome is locally detected as aquatic biome.
     *
     * @param biome Biome that must be checked.
     * @return {@code true} if I think it is aquatic biome, {@code false} otherwise.
     */
    public static boolean isAquaticBiome(Biome biome)
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
     *
     * @param biome Biome that must be checked.
     * @return {@code true} if I think it is neutral biome, {@code false} otherwise.
     */
    public static boolean isNeutralBiome(Biome biome)
    {
        return biome == Biome.THE_VOID;
    }


    /**
     * This method returns if current biome is locally detected as cave biome.
     *
     * @param biome Biome that must be checked.
     * @return {@code true} if I think it is cave biome, {@code false} otherwise.
     */
    public static boolean isCaveBiome(Biome biome)
    {
        return switch (biome) {
            case LUSH_CAVES,
                DRIPSTONE_CAVES -> true;
            default -> false;
        };
    }


    /**
     * This method returns if current biome is locally detected as nether biome.
     *
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
     *
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
     * Send unlock message for user with given UUID.
     *
     * @param uuid the uuid
     * @param island the island
     * @param biome the biome
     * @param addon instance of biome addon.
     * @param available the available
     */
    public static void sendUnlockMessage(UUID uuid,
        Island island,
        BiomesObject biome,
        BiomesAddon addon,
        boolean available)
    {
        User user = User.getInstance(uuid);

        WorldSettings settings = addon.getPlugin().getIWM().getWorldSettings(island.getWorld());

        if (user.isOnline())
        {
            TextComponent component;

            StringBuilder commandBuilder = new StringBuilder();
            commandBuilder.append("/");
            commandBuilder.append(settings.getPlayerCommandAliases().split(" ")[0]);
            commandBuilder.append(" ");
            commandBuilder.append(addon.getSettings().getPlayerCommand().split(" ")[0]);

            if (!available)
            {
                component = new TextComponent(user.getTranslation(Constants.CONVERSATIONS + "click-text-to-purchase",
                    Constants.PARAMETER_BIOME, biome.getFriendlyName()));
            }
            else
            {
                component = new TextComponent(user.getTranslation(Constants.CONVERSATIONS + "click-text-to-set",
                    Constants.PARAMETER_BIOME, biome.getFriendlyName()));
            }

            component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, commandBuilder.toString()));

            user.getPlayer().spigot().sendMessage(component);
        }
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
        // biomes:
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


    /**
     * Prettify ItemStack object for user.
     * @param object Object that must be pretty.
     * @param user User who will see the object.
     * @return Prettified string for ItemStack.
     */
    public static String prettifyObject(@Nullable ItemStack object, User user)
    {
        // Nothing to translate
        if (object == null)
        {
            return "";
        }

        // Find addon structure with:
        // [addon]:
        //   item-stacks:
        //     [material]: ...
        //     meta:
        //       potion-type: ...
        //       ...
        //     generic: [amount] [name] [meta]
        String translation;
        switch (object.getType())
        {
            case POTION, SPLASH_POTION, LINGERING_POTION, TIPPED_ARROW ->
                // Get Potion Meta
                translation = prettifyObject(object, (PotionMeta) object.getItemMeta(), user);
            case PLAYER_HEAD, PLAYER_WALL_HEAD ->
                translation = prettifyObject(object, (SkullMeta) object.getItemMeta(), user);
            case ENCHANTED_BOOK ->
                translation = prettifyObject(object, (EnchantmentStorageMeta) object.getItemMeta(), user);
            case WRITTEN_BOOK, WRITABLE_BOOK ->
                translation = prettifyObject(object, (BookMeta) object.getItemMeta(), user);
            case LEATHER_BOOTS,LEATHER_CHESTPLATE,LEATHER_HELMET,LEATHER_LEGGINGS,LEATHER_HORSE_ARMOR,
                TRIDENT,CROSSBOW,CHAINMAIL_HELMET,CHAINMAIL_CHESTPLATE,CHAINMAIL_LEGGINGS,CHAINMAIL_BOOTS,IRON_HELMET,
                IRON_CHESTPLATE,IRON_LEGGINGS,IRON_BOOTS,DIAMOND_HELMET,DIAMOND_CHESTPLATE,DIAMOND_LEGGINGS,DIAMOND_BOOTS,
                GOLDEN_HELMET,GOLDEN_CHESTPLATE,GOLDEN_LEGGINGS,GOLDEN_BOOTS,NETHERITE_HELMET,NETHERITE_CHESTPLATE,
                NETHERITE_LEGGINGS,NETHERITE_BOOTS,WOODEN_SWORD,WOODEN_SHOVEL,WOODEN_PICKAXE,WOODEN_AXE,WOODEN_HOE,
                STONE_SWORD,STONE_SHOVEL,STONE_PICKAXE,STONE_AXE,STONE_HOE,GOLDEN_SWORD,GOLDEN_SHOVEL,GOLDEN_PICKAXE,
                GOLDEN_AXE,GOLDEN_HOE,IRON_SWORD,IRON_SHOVEL,IRON_PICKAXE,IRON_AXE,IRON_HOE,DIAMOND_SWORD,DIAMOND_SHOVEL,
                DIAMOND_PICKAXE,DIAMOND_AXE,DIAMOND_HOE,NETHERITE_SWORD,NETHERITE_SHOVEL,NETHERITE_PICKAXE,NETHERITE_AXE,
                NETHERITE_HOE,TURTLE_HELMET,SHEARS,SHIELD,FLINT_AND_STEEL,BOW ->
                translation = prettifyObject(object, object.getItemMeta(), user);
            default ->
                translation = "";
        }

        if (!translation.isEmpty())
        {
            // We found our translation.
            return translation;
        }

        // Find addon structure with:
        // [addon]:
        //   materials:
        //     [material]: [name]

        translation = user.getTranslationOrNothing(Constants.MATERIALS + object.getType().name().toLowerCase());

        if (!translation.isEmpty())
        {
            // We found our translation.
            return translation;
        }

        // Find general structure with:
        // materials:
        //   [material]: [name]

        translation = user.getTranslationOrNothing("materials." + object.getType().name().toLowerCase());

        if (!translation.isEmpty())
        {
            // We found our translation.
            return translation;
        }

        // Use Lang Utils
        return LangUtilsHook.getItemDisplayName(object, user);
    }


    /**
     * Prettify enchant string.
     *
     * @param enchantment the enchantment
     * @param user the user
     * @return the string
     */
    public static String prettifyObject(Enchantment enchantment, User user)
    {
        if (enchantment == null)
        {
            return "";
        }

        String type = user.getTranslationOrNothing(Constants.ITEM_STACKS + "enchant." + enchantment.getKey().getKey());

        if (type.isEmpty())
        {
            type = LangUtilsHook.getEnchantName(enchantment, user);
        }

        return type;
    }


    /**
     * Prettify type string.
     *
     * @param type the potion type
     * @param user the user
     * @return the string
     */
    public static String prettifyObject(PotionType type, User user)
    {
        if (type == null)
        {
            return "";
        }

        String text = user.getTranslationOrNothing(Constants.ITEM_STACKS + "potion-type." + type.name().toLowerCase());

        if (text.isEmpty())
        {
            text = LangUtilsHook.getPotionBaseEffectName(type, user);
        }

        return text;
    }


    /**
     * Prettify potion item string.
     *
     * @param item the item
     * @param potionMeta the potion meta
     * @param user the user
     * @return the string
     */
    public static String prettifyObject(ItemStack item, @Nullable PotionMeta potionMeta, User user)
    {
        if (potionMeta == null)
        {
            return "";
        }

        Material itemType = item.getType();

        final String itemReference = Constants.ITEM_STACKS + itemType.name().toLowerCase() + ".";
        final String metaReference = Constants.ITEM_STACKS + "meta.";

        PotionData potionData = potionMeta.getBasePotionData();

        // Check custom translation for potions.
        String type = user.getTranslationOrNothing(itemReference + potionData.getType().name().toLowerCase());

        if (type.isEmpty())
        {
            // Check potion types translation.
            type = prettifyObject(potionData.getType(), user);
        }

        String upgraded = user.getTranslationOrNothing(metaReference + "upgraded");
        String extended = user.getTranslationOrNothing(metaReference + "extended");

        // Get item specific translation.
        String specific = user.getTranslationOrNothing(itemReference + "name",
            "[type]", type,
            "[upgraded]", (potionData.isUpgraded() ? upgraded : ""),
            "[extended]", (potionData.isExtended() ? extended : ""));

        if (specific.isEmpty())
        {
            // Use generic translation.
            String meta = user.getTranslationOrNothing(metaReference + "potion-meta",
                "[type]", type,
                "[upgraded]", (potionData.isUpgraded() ? upgraded : ""),
                "[extended]", (potionData.isExtended() ? extended : ""));

            specific = user.getTranslationOrNothing(Constants.ITEM_STACKS + "generic",
                "[type]", prettifyObject(itemType, user),
                "[meta]", meta);
        }

        return specific;
    }


    /**
     * Prettify skull item string.
     *
     * @param item the item
     * @param skullMeta the skull meta
     * @param user the user
     * @return the string
     */
    public static String prettifyObject(ItemStack item, @Nullable SkullMeta skullMeta, User user)
    {
        if (skullMeta == null)
        {
            return "";
        }

        Material itemType = item.getType();
        final String metaReference = Constants.ITEM_STACKS + "meta.";

        String meta = user.getTranslationOrNothing(metaReference + "skull-meta",
            "[player-name]", skullMeta.getDisplayName());

        return user.getTranslationOrNothing(Constants.ITEM_STACKS + "generic",
            "[type]", prettifyObject(itemType, user),
            "[meta]", meta);
    }


    /**
     * Prettify item string.
     *
     * @param item the item
     * @param itemMeta the item meta
     * @param user the user
     * @return the string
     */
    public static String prettifyObject(ItemStack item, @Nullable ItemMeta itemMeta, User user)
    {
        if (itemMeta == null)
        {
            return "";
        }

        StringBuilder builder = new StringBuilder();

        itemMeta.getEnchants().forEach((enchantment, level) -> {
            builder.append("\n");
            builder.append(user.getTranslationOrNothing(Constants.ITEM_STACKS + "meta.enchant-meta",
                "[type]", prettifyObject(enchantment, user),
                "[level]", String.valueOf(level)));
        });


        Material itemType = item.getType();
        final String itemReference = Constants.ITEM_STACKS + itemType.name().toLowerCase() + ".";

        String translation = user.getTranslationOrNothing(itemReference + "name",
            "[type]", prettifyObject(itemType, user),
            "[enchant]", builder.toString());

        if (translation.isEmpty())
        {
            translation = user.getTranslationOrNothing(Constants.ITEM_STACKS + "generic",
                "[type]", prettifyObject(itemType, user),
                "[meta]", builder.toString());
        }

        return translation;
    }


    /**
     * Prettify enchantment storage string.
     *
     * @param item the item
     * @param enchantmentMeta the enchantment storage meta
     * @param user the user
     * @return the string
     */
    public static String prettifyObject(ItemStack item, @Nullable EnchantmentStorageMeta enchantmentMeta, User user)
    {
        if (enchantmentMeta == null)
        {
            return "";
        }

        StringBuilder builder = new StringBuilder();

        enchantmentMeta.getEnchants().forEach((enchantment, level) -> {
            builder.append("\n");
            builder.append(user.getTranslationOrNothing(Constants.ITEM_STACKS + "meta.enchant-meta",
                "[type]", prettifyObject(enchantment, user),
                "[level]", String.valueOf(level)));
        });


        Material itemType = item.getType();
        final String itemReference = Constants.ITEM_STACKS + itemType.name().toLowerCase() + ".";

        String translation = user.getTranslationOrNothing(itemReference + "name",
            "[type]", prettifyObject(itemType, user),
            "[enchant]", builder.toString());

        if (translation.isEmpty())
        {
            translation = user.getTranslationOrNothing(Constants.ITEM_STACKS + "generic",
                "[type]", prettifyObject(itemType, user),
                "[meta]", builder.toString());
        }

        return translation;
    }


    /**
     * Prettify book item string.
     *
     * @param item the item
     * @param bookMeta the book meta
     * @param user the user
     * @return the string
     */
    public static String prettifyObject(ItemStack item, @Nullable BookMeta bookMeta, User user)
    {
        if (bookMeta == null)
        {
            return "";
        }

        Material itemType = item.getType();
        final String metaReference = Constants.ITEM_STACKS + "meta.";

        String meta = user.getTranslationOrNothing(metaReference + "book-meta",
            "[title]", bookMeta.getTitle(),
            "[author]", bookMeta.getAuthor());

        return user.getTranslationOrNothing(Constants.ITEM_STACKS + "generic",
            "[type]", prettifyObject(itemType, user),
            "[meta]", meta);
    }
}
