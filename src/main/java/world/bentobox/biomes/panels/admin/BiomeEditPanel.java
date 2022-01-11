package world.bentobox.biomes.panels.admin;


import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.PanelListener;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;
import world.bentobox.biomes.database.objects.BiomesObject;
import world.bentobox.biomes.panels.CommonPanel;
import world.bentobox.biomes.panels.ConversationUtils;
import world.bentobox.biomes.panels.util.*;
import world.bentobox.biomes.utils.Constants;
import world.bentobox.biomes.utils.Utils;


/**
 * This class contains methods that allows to edit specific biome object.
 */
public class BiomeEditPanel extends CommonPanel
{
    /**
     * Instantiates a new Edit biome panel.
     *
     * @param parentPanel the parent panel
     * @param biome the biome
     */
    private BiomeEditPanel(CommonPanel parentPanel, BiomesObject biome)
    {
        super(parentPanel);
        this.biome = biome;
        this.activeMenu = MenuType.PROPERTIES;
    }


    /**
     * This method is used to open GeneratorManagePanel outside this class. It will be much easier to open panel with
     * single method call then initializing new object.
     *
     * @param parentPanel Parent Panel object.
     * @param biome Biome object.
     */
    public static void open(CommonPanel parentPanel, BiomesObject biome)
    {
        new BiomeEditPanel(parentPanel, biome).build();
    }


    /**
     * This method builds all necessary elements in GUI panel.
     */
    @Override
    protected void build()
    {
        // PanelBuilder is a BentoBox API that provides ability to easily create Panels.
        PanelBuilder panelBuilder = new PanelBuilder().
            user(this.user).
            name(this.user.getTranslation(Constants.TITLE + "edit-biome",
                "[biome]", this.biome.getFriendlyName()));

        PanelUtils.fillBorder(panelBuilder, Material.PURPLE_STAINED_GLASS_PANE);

        panelBuilder.item(2, this.createMenuButton(MenuType.PROPERTIES));
        panelBuilder.item(4, this.createMenuButton(MenuType.UNLOCK_PROPERTIES));
        panelBuilder.item(6, this.createMenuButton(MenuType.CHANGE_PROPERTIES));

        switch (this.activeMenu)
        {
            case UNLOCK_PROPERTIES -> {
                panelBuilder.item(12, this.createButton(Action.UNLOCK_COST));
                panelBuilder.item(21, this.createButton(Action.UNLOCK_ITEMS));

                panelBuilder.item(14, this.createButton(Action.UNLOCK_LEVEL));
                panelBuilder.item(23, this.createButton(Action.UNLOCK_PERMISSIONS));
            }
            case CHANGE_PROPERTIES -> {
                panelBuilder.item(12, this.createButton(Action.CHANGE_COST));
                panelBuilder.item(21, this.createButton(Action.CHANGE_ITEMS));
                panelBuilder.item(10, this.createButton(Action.CHANGE_TYPE));

                if (BiomesObject.CostMode.PER_USAGE.equals(this.biome.getCostMode()))
                {
                    panelBuilder.item(19, this.createButton(Action.CHANGE_INCREMENT));
                }
            }
            case PROPERTIES -> {
                panelBuilder.item(19, this.createButton(Action.BIOME));
                panelBuilder.item(28, this.createButton(Action.ENVIRONMENT));

                panelBuilder.item(11, this.createButton(Action.NAME));
                panelBuilder.item(20, this.createButton(Action.ICON));
                panelBuilder.item(29, this.createButton(Action.DESCRIPTION));

                panelBuilder.item(21, this.createButton(Action.ORDER));

                panelBuilder.item(25, this.createButton(Action.DEPLOYED));

                panelBuilder.listener(new IconChanger());
            }
        }

        panelBuilder.item(44, this.returnButton);

        // Build panel.
        panelBuilder.build();
    }


    /**
     * This method returns button for edit panel of given type.
     * @param button Type of button.
     * @return new panel button with requested type.
     */
    private PanelItem createButton(Action button)
    {
        final String reference = Constants.BUTTON + button.name().toLowerCase() + ".";
        String name = this.user.getTranslation(reference + "name");
        List<String> description = new ArrayList<>();
        description.add(this.user.getTranslationOrNothing(reference + "description"));

        PanelItem.ClickHandler clickHandler;
        boolean glow;

        ItemStack icon;
        int count = 1;

        switch (button)
        {
            case BIOME -> {
                name = this.user.getTranslation(reference + "name",
                    Constants.PARAMETER_BIOME, Utils.prettifyObject(this.biome.getBiome(), this.user));

                description.add(this.user.getTranslation(reference + "value",
                    Constants.PARAMETER_ID, this.biome.getBiome().name()));
                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));

                icon = new ItemStack(Material.WATER_BUCKET);
                clickHandler = (panel, user1, clickType, slot) ->
                {
                    this.selectedButton = null;

                    BiConsumer<Boolean, Biome> biomeConsumer = (success, biome) -> {
                        if (success)
                        {
                            this.biome.setBiome(biome);
                            // Save biome
                            this.manager.saveBiome(this.biome);
                        }

                        this.build();
                    };

                    BiomeSelector.open(this.user, Collections.singleton(this.biome.getBiome()), biomeConsumer);
                    return true;
                };

                glow = false;
            }
            case UNLOCK_PERMISSIONS -> {
                if (this.biome.getUnlockPermissions().isEmpty())
                {
                    description.add(this.user.getTranslation(reference + "none"));
                }
                else
                {
                    description.add(this.user.getTranslation(reference + "title"));

                    this.biome.getUnlockPermissions().forEach(permission ->
                        description.add(this.user.getTranslation(reference + "element",
                            "[permission]", permission)));
                }

                icon = new ItemStack(Material.REDSTONE_LAMP);

                clickHandler = (panel, user, clickType, i) ->
                {
                    // Create consumer that process description change
                    Consumer<List<String>> consumer = value ->
                    {
                        if (value != null)
                        {
                            this.biome.setUnlockPermissions(new HashSet<>(value));
                            this.manager.saveBiome(this.biome);
                        }

                        this.build();
                    };

                    if (!this.biome.getUnlockPermissions().isEmpty() &&
                        clickType.isShiftClick())
                    {
                        // Reset to the empty value
                        consumer.accept(Collections.emptyList());
                    }
                    else
                    {
                        // start conversation
                        ConversationUtils.createStringListInput(consumer,
                            user,
                            user.getTranslation(Constants.CONVERSATIONS + "write-permissions"),
                            user.getTranslation(Constants.CONVERSATIONS + "permissions-changed"));
                    }

                    return true;
                };

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));

                if (!this.biome.getUnlockPermissions().isEmpty())
                {
                    description.add(this.user.getTranslation(Constants.TIPS + "shift-click-to-reset"));
                }

                glow = false;
            }
            case UNLOCK_COST -> {
                description.add(this.user.getTranslation(reference + "value",
                    Constants.PARAMETER_NUMBER, String.valueOf(this.biome.getUnlockCost())));

                icon = new ItemStack(this.addon.isEconomyProvided() ? Material.GOLD_INGOT : Material.BARRIER);
                clickHandler = (panel, user, clickType, i) -> {
                    Consumer<Number> numberConsumer = number -> {
                        if (number != null)
                        {
                            this.biome.setUnlockCost(number.doubleValue());
                            this.manager.saveBiome(this.biome);
                        }

                        // reopen panel
                        this.build();
                    };

                    ConversationUtils.createNumericInput(numberConsumer,
                        this.user,
                        this.user.getTranslation(Constants.CONVERSATIONS + "input-number"),
                        0,
                        Double.MAX_VALUE);

                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
            }
            case UNLOCK_ITEMS -> {
                if (this.biome.getUnlockItems().isEmpty())
                {
                    description.add(this.user.getTranslation(reference + "none"));
                }
                else
                {
                    description.add(this.user.getTranslation(reference + "title"));

                    Utils.groupEqualItems(this.biome.getUnlockItems(), Collections.emptySet()).
                        stream().
                        sorted(Comparator.comparing(ItemStack::getType)).
                        forEach(itemStack ->
                            description.add(this.user.getTranslationOrNothing(reference + "element",
                                "[number]", String.valueOf(itemStack.getAmount()),
                                "[item]", Utils.prettifyObject(itemStack, this.user))));
                }

                icon = new ItemStack(Material.CHEST);
                clickHandler = (panel, user, clickType, slot) -> {
                    ItemSelector.open(this.user,
                        this.biome.getUnlockItems(),
                        (status, value) -> {
                            if (status)
                            {
                                this.biome.setUnlockItems(value);
                                this.manager.saveBiome(this.biome);
                            }

                            this.build();
                        });
                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
            }
            case UNLOCK_LEVEL -> {
                description.add(this.user.getTranslation(reference + "value",
                    Constants.PARAMETER_NUMBER, String.valueOf(this.biome.getUnlockLevel())));

                icon = new ItemStack(this.addon.isLevelProvided() ? Material.BEACON : Material.BARRIER,
                    (int) Math.max(1, this.biome.getUnlockLevel()));
                clickHandler = (panel, user, clickType, i) -> {
                    Consumer<Number> numberConsumer = number -> {
                        if (number != null)
                        {
                            this.biome.setUnlockLevel(number.longValue());
                            this.manager.saveBiome(this.biome);
                        }

                        // reopen panel
                        this.build();
                    };

                    ConversationUtils.createNumericInput(numberConsumer,
                        this.user,
                        this.user.getTranslation(Constants.CONVERSATIONS + "input-number"),
                        0,
                        Long.MAX_VALUE);

                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
            }
            case ORDER -> {
                description.add(this.user.getTranslation(reference + "value",
                    Constants.PARAMETER_NUMBER, String.valueOf(this.biome.getOrder())));

                icon = new ItemStack(Material.HOPPER, Math.max(1, this.biome.getOrder()));
                clickHandler = (panel, user, clickType, i) -> {
                    Consumer<Number> numberConsumer = number -> {
                        if (number != null)
                        {
                            this.biome.setOrder(number.intValue());
                            this.manager.saveBiome(this.biome);
                        }

                        // reopen panel
                        this.build();
                    };

                    ConversationUtils.createNumericInput(numberConsumer,
                        this.user,
                        this.user.getTranslation(Constants.CONVERSATIONS + "input-number"),
                        0,
                        2000);

                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
            }
            case ENVIRONMENT -> {
                description.add(this.user.getTranslation(this.biome.getEnvironment().equals(World.Environment.NORMAL) ?
                    reference + "enabled" : reference + "disabled") +
                    Utils.prettifyObject(World.Environment.NORMAL, this.user));
                description.add(this.user.getTranslation(this.biome.getEnvironment().equals(World.Environment.NETHER) ?
                    reference + "enabled" : reference + "disabled") +
                    Utils.prettifyObject(World.Environment.NETHER, this.user));
                description.add(this.user.getTranslation(this.biome.getEnvironment().equals(World.Environment.THE_END) ?
                    reference + "enabled" : reference + "disabled") +
                    Utils.prettifyObject(World.Environment.THE_END, this.user));

                icon = new ItemStack(Material.DROPPER);
                clickHandler = (panel, user, clickType, slot) -> {
                    this.selectedButton = null;

                    EnvironmentSelector.open(this.user,
                        this.biome.getEnvironment(),
                        (status, value) -> {
                            if (status)
                            {
                                this.biome.setEnvironment(value);
                                this.manager.saveBiome(this.biome);
                            }

                            this.build();
                        });

                    return true;
                };
                glow = false;
            }
            case DESCRIPTION -> {
                icon = new ItemStack(Material.WRITTEN_BOOK);

                if (this.biome.getDescription().isEmpty())
                {
                    description.add(this.user.getTranslation(reference + "none"));
                }
                else
                {
                    description.add(this.user.getTranslation(reference + "value"));
                    this.biome.getDescription().forEach(line -> description.add(Util.translateColorCodes(line)));
                }

                clickHandler = (panel, user, clickType, i) ->
                {
                    // Create consumer that process description change
                    Consumer<List<String>> consumer = value ->
                    {
                        if (value != null)
                        {
                            this.biome.setDescription(value);
                            this.manager.saveBiome(this.biome);
                        }

                        this.build();
                    };

                    if (!this.biome.getDescription().isEmpty() && clickType.isShiftClick())
                    {
                        // Reset to the empty value
                        consumer.accept(Collections.emptyList());
                    }
                    else
                    {
                        // start conversation
                        ConversationUtils.createStringListInput(consumer,
                            user,
                            user.getTranslation(Constants.CONVERSATIONS + "write-description"),
                            user.getTranslation(Constants.CONVERSATIONS + "description-changed"));
                    }

                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));

                if (!this.biome.getDescription().isEmpty())
                {
                    description.add(this.user.getTranslation(Constants.TIPS + "shift-click-to-reset"));
                }
            }
            case ICON -> {
                icon = this.biome.getIcon();
                clickHandler = (panel, user, clickType, i) ->
                {
                    this.selectedButton = this.selectedButton == button ? null : button;
                    this.build();
                    return true;
                };
                glow = this.selectedButton == button;

                if (this.selectedButton != button)
                {
                    description.add("");
                    description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
                }
                else
                {
                    description.add("");
                    description.add(this.user.getTranslation(Constants.TIPS + "click-on-item"));
                }
            }
            case NAME -> {
                description.add(this.user.getTranslation(reference + "value",
                    Constants.PARAMETER_NAME, this.biome.getFriendlyName()));

                icon = new ItemStack(Material.NAME_TAG);

                clickHandler = (panel, user, clickType, i) ->
                {
                    this.selectedButton = null;

                    // Create consumer that process description change
                    Consumer<String> consumer = value ->
                    {
                        if (value != null)
                        {
                            this.biome.setFriendlyName(value);
                            this.manager.saveBiome(this.biome);
                        }

                        this.build();
                    };

                    // start conversation
                    ConversationUtils.createStringInput(consumer,
                        user,
                        user.getTranslation(Constants.CONVERSATIONS + "write-name"),
                        user.getTranslation(Constants.CONVERSATIONS + "name-changed"));

                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
            }
            case DEPLOYED -> {
                description.add(this.user.getTranslation(reference +
                    (this.biome.isDeployed() ? "enabled" : "disabled")));

                icon = new ItemStack(Material.LEVER);
                clickHandler = (panel, user, clickType, slot) -> {
                    this.selectedButton = null;

                    if (this.biome.isValid())
                    {
                        this.biome.setDeployed(!this.biome.isDeployed());
                    }
                    else
                    {
                        Utils.sendMessage(this.user,
                            this.user.getTranslation(Constants.CONVERSATIONS + "invalid-biome",
                                "[biome]", this.biome.getFriendlyName()));
                        this.biome.setDeployed(false);
                    }

                    this.manager.saveBiome(this.biome);

                    this.build();
                    return true;
                };
                glow = this.biome.isDeployed();

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-toggle"));
            }
            case CHANGE_COST -> {
                description.add(this.user.getTranslation(reference + "value",
                    Constants.PARAMETER_NUMBER, String.valueOf(this.biome.getCost())));

                icon = new ItemStack(this.addon.isEconomyProvided() ? Material.GOLD_INGOT : Material.BARRIER);
                clickHandler = (panel, user, clickType, i) -> {
                    Consumer<Number> numberConsumer = number -> {
                        if (number != null)
                        {
                            this.biome.setCost(number.doubleValue());
                            this.manager.saveBiome(this.biome);
                        }

                        // reopen panel
                        this.build();
                    };

                    ConversationUtils.createNumericInput(numberConsumer,
                        this.user,
                        this.user.getTranslation(Constants.CONVERSATIONS + "input-number"),
                        0,
                        Double.MAX_VALUE);

                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
            }
            case CHANGE_ITEMS -> {
                if (this.biome.getItemCost().isEmpty())
                {
                    description.add(this.user.getTranslation(reference + "none"));
                }
                else
                {
                    description.add(this.user.getTranslation(reference + "title"));

                    Utils.groupEqualItems(this.biome.getItemCost(), Collections.emptySet()).
                        stream().
                        sorted(Comparator.comparing(ItemStack::getType)).
                        forEach(itemStack ->
                            description.add(this.user.getTranslationOrNothing(reference + "element",
                                "[number]", String.valueOf(itemStack.getAmount()),
                                "[item]", Utils.prettifyObject(itemStack, this.user))));
                }

                icon = new ItemStack(Material.CHEST);
                clickHandler = (panel, user, clickType, slot) -> {
                    ItemSelector.open(this.user,
                        this.biome.getItemCost(),
                        (status, value) -> {
                            if (status)
                            {
                                this.biome.setItemCost(value);
                                this.manager.saveBiome(this.biome);
                            }

                            this.build();
                        });
                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
            }
            case CHANGE_TYPE -> {
                String type = this.biome.getCostMode().name().toLowerCase();

                name = this.user.getTranslation(reference + "name",
                    "[type]", this.user.getTranslation(reference + type + ".name"));

                description.add(this.user.getTranslation(reference + type + ".description"));
                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));

                switch (this.biome.getCostMode())
                {
                    case PER_BLOCK -> icon = new ItemStack(Material.DIRT);
                    case PER_USAGE -> icon = new ItemStack(Material.OBSERVER);
                    case STATIC -> icon = new ItemStack(Material.OBSIDIAN);
                    default -> icon = new ItemStack(Material.PAPER);
                }

                clickHandler = (panel, user1, clickType, slot) ->
                {
                    this.selectedButton = null;

                    if (clickType.isLeftClick())
                    {
                        this.biome.setCostMode(Utils.getNextValue(
                            BiomesObject.CostMode.values(),
                            this.biome.getCostMode()));
                    }
                    else
                    {
                        this.biome.setCostMode(Utils.getPreviousValue(
                            BiomesObject.CostMode.values(),
                            this.biome.getCostMode()));
                    }

                    this.manager.saveBiome(this.biome);
                    this.build();

                    return true;
                };

                glow = false;
            }
            case CHANGE_INCREMENT -> {
                description.add(this.user.getTranslation(reference + "value",
                    Constants.PARAMETER_NUMBER, String.valueOf(this.biome.getCostIncrement())));

                icon = new ItemStack(Material.REDSTONE_TORCH);
                clickHandler = (panel, user, clickType, i) -> {
                    Consumer<Number> numberConsumer = number -> {
                        if (number != null)
                        {
                            this.biome.setCostIncrement(number.doubleValue());
                            this.manager.saveBiome(this.biome);
                        }

                        // reopen panel
                        this.build();
                    };

                    ConversationUtils.createNumericInput(numberConsumer,
                        this.user,
                        this.user.getTranslation(Constants.CONVERSATIONS + "input-number"),
                        0,
                        Double.MAX_VALUE);

                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
            }
            default -> {
                icon = new ItemStack(Material.PAPER);
                clickHandler = (panel, user1, clickType, slot) -> true;
                glow = false;
            }
        }

        return new PanelItemBuilder().
            name(name).
            description(description).
            icon(icon).
            amount(count).
            clickHandler(clickHandler).
            glow(glow).
            build();
    }


    /**
     * This method creates top menu buttons, that allows to switch "tabs".
     * @param menuType Menu Type which button must be constructed.
     * @return PanelItem that represents given menu type.
     */
    private PanelItem createMenuButton(MenuType menuType)
    {
        final String reference = Constants.BUTTON + menuType.name().toLowerCase() + ".";

        final String name = this.user.getTranslation(reference + "name");
        final List<String> description = new ArrayList<>(3);
        description.add(this.user.getTranslation(reference + "description"));
        description.add("");
        description.add(this.user.getTranslation(Constants.TIPS + "click-to-select"));

        ItemStack icon;
        boolean glow;
        PanelItem.ClickHandler clickHandler;

        switch (menuType)
        {
            case PROPERTIES -> {
                icon = new ItemStack(Material.CRAFTING_TABLE);
                clickHandler = (panel, user, clickType, slot) -> {
                    this.activeMenu = MenuType.PROPERTIES;
                    this.build();

                    return true;
                };
                glow = this.activeMenu.equals(MenuType.PROPERTIES);
            }
            case CHANGE_PROPERTIES -> {
                icon = new ItemStack(Material.HOPPER);
                clickHandler = (panel, user, clickType, slot) -> {
                    this.activeMenu = MenuType.CHANGE_PROPERTIES;
                    this.build();

                    return true;
                };
                glow = this.activeMenu.equals(MenuType.CHANGE_PROPERTIES);
            }
            case UNLOCK_PROPERTIES -> {
                icon = new ItemStack(Material.DROPPER);
                clickHandler = (panel, user, clickType, slot) -> {
                    this.activeMenu = MenuType.UNLOCK_PROPERTIES;
                    this.build();

                    return true;
                };
                glow = this.activeMenu.equals(MenuType.UNLOCK_PROPERTIES);
            }
            default -> {
                icon = new ItemStack(Material.PAPER);
                clickHandler = null;
                glow = false;
            }
        }

        return new PanelItemBuilder().
            icon(icon).
            name(name).
            description(description).
            glow(glow).
            clickHandler(clickHandler).
            build();
    }


    // ---------------------------------------------------------------------
    // Section: Classes
    // ---------------------------------------------------------------------


    /**
     * This class allows changing icon for Generator Tier
     */
    private class IconChanger implements PanelListener
    {
        /**
         * Process inventory click. If generator icon is selected and user clicks on item in his inventory, then change
         * icon to the item from inventory.
         *
         * @param user the user
         * @param event the event
         */
        @Override
        public void onInventoryClick(User user, InventoryClickEvent event)
        {
            // Handle icon changing
            if (BiomeEditPanel.this.selectedButton == Action.ICON &&
                event.getCurrentItem() != null &&
                !event.getCurrentItem().getType().equals(Material.AIR) &&
                event.getRawSlot() > 44)
            {
                // set material and amount only. Other data should be removed.

                BiomeEditPanel.this.biome.setIcon(event.getCurrentItem().clone());
                BiomeEditPanel.this.manager.saveBiome(BiomeEditPanel.this.biome);
                // Deselect icon
                BiomeEditPanel.this.selectedButton = null;
                // Rebuild icon
                BiomeEditPanel.this.build();
            }
        }


        /**
         * On inventory close.
         *
         * @param event the event
         */
        @Override
        public void onInventoryClose(InventoryCloseEvent event)
        {
            // Do nothing
        }


        /**
         * Setup current listener.
         */
        @Override
        public void setup()
        {
            // Do nothing
        }
    }


    // ---------------------------------------------------------------------
    // Section: Enums
    // ---------------------------------------------------------------------


    /**
     * The enum Menu type.
     */
    private enum MenuType
    {
        /**
         * Unlock properties menu type.
         */
        UNLOCK_PROPERTIES,
        /**
         * Change properties menu type.
         */
        CHANGE_PROPERTIES,
        /**
         * Properties menu type.
         */
        PROPERTIES
    }


    /**
     * This enum shows which button should be created.
     */
    private enum Action
    {
        /**
         * Biome action.
         */
        BIOME,
        /**
         * Order action.
         */
        ORDER,
        /**
         * Environment action.
         */
        ENVIRONMENT,
        /**
         * Description action.
         */
        DESCRIPTION,
        /**
         * Icon action.
         */
        ICON,
        /**
         * Name action.
         */
        NAME,
        /**
         * Deployed action.
         */
        DEPLOYED,
        /**
         * Unlock permissions action.
         */
        UNLOCK_PERMISSIONS,
        /**
         * Unlock cost action.
         */
        UNLOCK_COST,
        /**
         * Unlock items action.
         */
        UNLOCK_ITEMS,
        /**
         * Unlock level action.
         */
        UNLOCK_LEVEL,
        /**
         * Change cost action.
         */
        CHANGE_COST,
        /**
         * Change items action.
         */
        CHANGE_ITEMS,
        /**
         * Change type action.
         */
        CHANGE_TYPE,
        /**
         * Change increment action.
         */
        CHANGE_INCREMENT
    }


    // ---------------------------------------------------------------------
    // Section: Variable
    // ---------------------------------------------------------------------


    /**
     * Holds a biome object that is edited.
     */
    private final BiomesObject biome;

    /**
     * Action that allows to change icon.
     */
    private Action selectedButton;

    /**
     * MenuType that is currently active.
     */
    private MenuType activeMenu;
}
