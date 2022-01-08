//
// Created by BONNe
// Copyright - 2020
//


package world.bentobox.biomes.panels.admin;


import com.google.common.collect.ImmutableSet;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.biomes.panels.CommonPagedPanel;
import world.bentobox.biomes.panels.CommonPanel;
import world.bentobox.biomes.panels.ConversationUtils;
import world.bentobox.biomes.utils.Constants;


/**
 * This class manages User islands that are stored in database with some custom data. It also allows to add a custom
 * data to island.
 */
public class IslandManagePanel extends CommonPagedPanel<Island>
{
    /**
     * This is default constructor for all classes that extends CommonPanel.
     *
     * @param parentPanel Parent panel of current panel.
     */
    private IslandManagePanel(CommonPanel parentPanel)
    {
        super(parentPanel);
        this.activeTab = Tab.IS_ONLINE;

        // Store bundles in local list to avoid building it every time.
        this.elementList = this.addon.getIslands().getIslands(this.world).stream().
            filter(Island::isOwned).
            sorted((o1, o2) -> {
                User u1 = User.getInstance(o1.getOwner());
                User u2 = User.getInstance(o2.getOwner());

                if (u1 == null || !u1.isPlayer())
                {
                    return -1;
                }
                else if (u2 == null || !u2.isPlayer())
                {
                    return 1;
                }
                else
                {
                    return u1.getName().compareTo(u2.getName());
                }
            }).
            distinct().
            collect(Collectors.toList());

        this.filterElements = this.elementList;
        this.currentAction = Action.CHANGE_BIOME;
    }


    /**
     * This method build island panel from parent panel.
     *
     * @param panel ParentPanel.
     */
    public static void open(CommonPanel panel)
    {
        new IslandManagePanel(panel).build();
    }


    /**
     * This method allows to build panel.
     */
    @Override
    protected void build()
    {
        // PanelBuilder is a BentoBox API that provides ability to easy create Panels.
        PanelBuilder panelBuilder = new PanelBuilder().
            user(this.user).
            name(this.user.getTranslation(Constants.TITLE + "manage-islands"));

        PanelUtils.fillBorder(panelBuilder, 5, Material.MAGENTA_STAINED_GLASS_PANE);

        panelBuilder.item(1, this.createButton(Tab.IS_ONLINE));
        panelBuilder.item(2, this.createButton(Tab.ALL_ISLANDS));

        panelBuilder.item(5, this.createButton(Action.CHANGE_BIOME));
        panelBuilder.item(6, this.createButton(Action.MANAGE_DATA));
        panelBuilder.item(7, this.createButton(Action.CLEAR_DATA));


        this.populateElements(panelBuilder, this.filterElements);

        panelBuilder.item(44, this.returnButton);

        // Build panel.
        panelBuilder.build();
    }


    /**
     * This method is called when filter value is updated.
     */
    @Override
    protected void updateFilters()
    {
        if (this.searchString == null || this.searchString.isBlank())
        {
            this.filterElements = new ArrayList<>(this.elementList);
        }
        else
        {
            this.filterElements = this.elementList.stream().
                filter(island -> {
                    // If island name is set and name contains search field, then do not filter out.
                    if (island.getName() != null &&
                        !island.getName().isEmpty() &&
                        island.getName().toLowerCase().contains(this.searchString.toLowerCase()))
                    {
                        return true;
                    }

                    // If island member names do not contains search field, then filter it out.
                    for (UUID uuid : island.getMemberSet())
                    {
                        User member = User.getInstance(uuid);

                        if (member != null &&
                            member.isPlayer() &&
                            member.getName().toLowerCase().contains(this.searchString.toLowerCase()))
                        {
                            return true;
                        }
                    }

                    // Island do not contains filter field.
                    return false;
                }).
                distinct().
                collect(Collectors.toList());
        }

        if (this.activeTab == Tab.IS_ONLINE)
        {
            Set<UUID> onlinePlayerSet = Bukkit.getOnlinePlayers().stream().
                map(Player::getUniqueId).collect(Collectors.toSet());

            // Remove all islands from filter list where none of members are online.
            this.filterElements.removeIf(island ->
                island.getMemberSet().stream().noneMatch(onlinePlayerSet::contains));
        }
    }


    /**
     * This method creates button for given island.
     *
     * @param island Island which button must be created.
     * @return PanelItem button for given island.
     */
    @Override
    protected PanelItem createElementButton(Island island)
    {
        final String reference = Constants.BUTTON + "island_name.";

        // Generate island name.
        String name = island.getName();

        // If name is not set, then use owner island translation.
        if (name == null || name.equals(""))
        {
            // Deal with situations when island name is not set.

            User user = User.getInstance(island.getOwner());

            if (user != null)
            {
                name = this.user.getTranslation(reference + "island-owner",
                    Constants.PARAMETER_PLAYER, user.getName());
            }
            else
            {
                name = this.user.getTranslation(reference + "island-owner",
                    Constants.PARAMETER_PLAYER, this.user.getTranslation(reference + "unknown"));
            }
        }

        // Transform name into button title.
        name = this.user.getTranslation(reference + "name",
            Constants.PARAMETER_NAME, name);

        // Create owner name translated string.
        String ownerName = this.addon.getPlayers().getName(island.getOwner());

        if (ownerName.equals(""))
        {
            ownerName = this.user.getTranslation(reference + "unknown");
        }

        ownerName = this.user.getTranslation(reference + "owner",
            Constants.PARAMETER_PLAYER, ownerName);

        // Create island members translated string.

        StringBuilder builder = new StringBuilder();

        ImmutableSet<UUID> members = island.getMemberSet();
        if (members.size() > 1)
        {
            builder.append(this.user.getTranslation(reference + "title"));

            for (UUID uuid : members)
            {
                if (uuid != island.getOwner())
                {
                    builder.append("\n").append(this.user.getTranslation(reference + "element",
                        Constants.PARAMETER_PLAYER, this.addon.getPlayers().getName(uuid)));
                }
            }
        }

        // Create description list
        List<String> description = new ArrayList<>();

        description.add(this.user.getTranslation(reference + "description",
            Constants.PARAMETER_OWNER, ownerName,
            "[members]", builder.toString(),
            Constants.PARAMETER_ID, island.getUniqueId()));

        description.add("");

        PanelItem.ClickHandler clickHandler;

        switch (this.currentAction)
        {
            case CHANGE_BIOME -> {
                clickHandler = (panel, user, clickType, i) -> {
                    // TODO: Create Biome Choose Panel.

                    return true;
                };
                // Add tip
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change-biome"));
            }
            case MANAGE_DATA -> {
                clickHandler = (panel, user, clickType, i) -> {
                    IslandEditPanel.open(this, island);
                    // Always return true.
                    return true;
                };

                // Add tip
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-manage"));
            }
            case CLEAR_DATA -> {
                clickHandler = (panel, user, clickType, i) -> {

                    // Create consumer that accepts value from conversation.
                    Consumer<Boolean> consumer = value ->
                    {
                        if (value)
                        {
                            this.addon.getAddonManager().wipeIslandData(island);
                        }

                        this.build();
                    };

                    // Create conversation that gets user acceptance to delete selected generator data.
                    ConversationUtils.createConfirmation(
                        consumer,
                        this.user,
                        this.user.getTranslation(Constants.CONVERSATIONS + "confirm-island-data-deletion",
                            "[player]", User.getInstance(island.getOwner()).getName()),
                        this.user.getTranslation(Constants.CONVERSATIONS + "island-data-removed",
                            "[player]", User.getInstance(island.getOwner()).getName()));


                    // Always return true.
                    return true;
                };

                description.add(this.user.getTranslation(Constants.TIPS + "click-to-clear"));
            }
            default -> {
                // empty action.
                clickHandler = (panel, user1, clickType, i) -> true;
            }
        }

        return new PanelItemBuilder().
            name(name).
            description(description).
            icon(this.addon.getPlayers().getName(island.getOwner())).
            clickHandler(clickHandler).
            build();
    }


    /**
     * This method creates panel item for given button type.
     *
     * @param button Button type.
     * @return Clickable PanelItem button.
     */
    private PanelItem createButton(Tab button)
    {
        String name = this.user.getTranslation(Constants.BUTTON + button.name().toLowerCase() + ".name");
        List<String> description = new ArrayList<>();
        description.add(this.user.getTranslationOrNothing(Constants.BUTTON + button.name().toLowerCase() + ".description"));
        description.add("");
        description.add(this.user.getTranslation(Constants.TIPS + "click-to-view"));

        PanelItem.ClickHandler clickHandler = (panel, user, clickType, i) -> {
            this.activeTab = button;

            this.build();
            return true;
        };

        Material material = switch (button) {
            case IS_ONLINE -> Material.WRITTEN_BOOK;
            case ALL_ISLANDS -> Material.CHEST;
        };

        return new PanelItemBuilder().
            name(name).
            description(description).
            icon(material).
            clickHandler(clickHandler).
            glow(this.activeTab == button).
            build();
    }


    /**
     * This method creates panel item for given button type.
     *
     * @param button Button type.
     * @return Clickable PanelItem button.
     */
    private PanelItem createButton(Action button)
    {
        String name = this.user.getTranslation(Constants.BUTTON + button.name().toLowerCase() + ".name");
        List<String> description = new ArrayList<>();
        description.add(this.user.getTranslationOrNothing(Constants.BUTTON + button.name().toLowerCase() + ".description"));
        description.add("");
        description.add(this.user.getTranslation(Constants.TIPS + "click-to-select"));

        PanelItem.ClickHandler clickHandler = (panel, user, clickType, i) -> {
            this.currentAction = button;

            this.build();
            return true;
        };

        Material material = switch (button) {
            case CHANGE_BIOME -> Material.GRASS_BLOCK;
            case MANAGE_DATA -> Material.CRAFTING_TABLE;
            case CLEAR_DATA -> Material.LAVA_BUCKET;
        };

        return new PanelItemBuilder().
            name(name).
            description(description).
            icon(material).
            clickHandler(clickHandler).
            glow(this.currentAction == button).
            build();
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


    /**
     * This enum holds all possible tabs in current GUI.
     */
    private enum Tab
    {
        /**
         * Shows islands with online users.
         */
        IS_ONLINE,
        /**
         * Shows islands with data.
         */
        ALL_ISLANDS,
    }


    /**
     * This enum holds all possible actions in current GUI.
     */
    private enum Action
    {
        /**
         * Change biome action.
         */
        CHANGE_BIOME,
        /**
         * Manage data action.
         */
        MANAGE_DATA,
        /**
         * Clear data action.
         */
        CLEAR_DATA
    }


    /**
     * Allows switching between active tabs.
     */
    private Tab activeTab;

    /**
     * Allows switching active action.
     */
    private Action currentAction;

    /**
     * This variable stores all islands in the given world.
     */
    private final List<Island> elementList;

    /**
     * This variable stores all islands in the given world.
     */
    private List<Island> filterElements;
}
