package world.bentobox.biomes.panels.admin;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.PanelListener;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.biomes.config.Settings.Lore;
import world.bentobox.biomes.panels.CommonGUI;
import world.bentobox.biomes.panels.GuiUtils;


/**
 * This class allows to change Input ItemStacks to different ItemStacks.
 */
public class EditLoreGUI extends CommonGUI
{
	/**
	 * Default constructor class
	 * @param parent Parent GUI
	 */
	public EditLoreGUI(CommonGUI parent)
	{
		super(parent);

		this.activeValues = new ArrayList<>();

		for (Lore lore : this.addon.getSettings().getLore())
		{
			this.activeValues.add(lore.name());
		}
	}


	/**
	 * This is static call method for easier GUI opening.
	 * @param parent Parent GUI.
	 */
	public static void open(CommonGUI parent)
	{
		new EditLoreGUI(parent).build();
	}


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


	/**
	 * This method builds panel that allows to change given number value.
	 */
	@Override
	public void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder().
			name(this.user.getTranslation("biomes.gui.title.admin.lore-edit")).
			user(this.user).
			listener(new CustomPanelListener());

		GuiUtils.fillBorder(panelBuilder, 5, Material.MAGENTA_STAINED_GLASS_PANE);

		// Define all active buttons
		panelBuilder.item(1, this.getButton(Button.SAVE));

		panelBuilder.item(3, this.getButton(Button.ADD));
		panelBuilder.item(4, this.getButton(Button.REMOVE));

		// TODO: Need 2 View Buttons
		// One for closes / One for opened.
//		panelBuilder.item(6, this.getButton(Button.VIEW));

		panelBuilder.item(44, this.returnButton);

		// necessary as I have a border around this GUI
		int currentIndex = 10;

		// Only 21 elements will be displayed. On porpoise!
		for (int i = 0; i < this.activeValues.size() || i > 21; i++)
		{
			panelBuilder.item(currentIndex++, this.getLoreButton(this.activeValues.get(i)));

			// Border element
			if (currentIndex % 9 == 8)
			{
				currentIndex += 2;
			}

			// Just in case. Should never occur.
			if (currentIndex % 9 == 0)
			{
				currentIndex++;
			}
		}

		panelBuilder.build();
	}


	/**
	 * This method create button that does some functionality in current gui.
	 * @param button Button functionality.
	 * @return PanelItem.
	 */
	private PanelItem getButton(Button button)
	{
		ItemStack icon;
		String name;
		List<String> description;
		PanelItem.ClickHandler clickHandler;

		switch (button)
		{
			case SAVE:
			{
				name = this.user.getTranslation("biomes.gui.buttons.admin.save");
				description = Collections.emptyList();
				icon = new ItemStack(Material.COMMAND_BLOCK);
				clickHandler = (panel, user, clickType, slot) -> {

					List<Lore> lore = this.activeValues.stream().
						map(Lore::valueOf).
						collect(Collectors.toCollection(() -> new ArrayList<>(this.activeValues.size())));

					this.addon.getSettings().setLore(lore);

					// Save and return to parent gui.
					this.parentGUI.build();

					return true;
				};
				break;
			}
			case ADD:
			{
				name = this.user.getTranslation("biomes.gui.buttons.admin.add-element");
				description = Collections.emptyList();
				icon = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
				clickHandler = (panel, user, clickType, slot) -> {
					new AddLoreElementGUI(element -> {
						this.activeValues.add(element);
						this.build();
					});

					return true;
				};

				break;
			}
			case REMOVE:
			{
				name = this.user.getTranslation("biomes.gui.buttons.admin.remove-element");
				description = Collections.emptyList();
				icon = new ItemStack(Material.RED_STAINED_GLASS_PANE);
				clickHandler = (panel, user, clickType, slot) -> {
					new RemoveLoreElementGUI((element, index) -> {
						if (this.activeValues.get(index).equals(element))
						{
							this.activeValues.remove(element);
						}

						this.build();
					});

					return true;
				};

				break;
			}
			case VIEW:
			{
				name = this.user.getTranslation("biomes.gui.buttons.admin.view");
				description = Collections.emptyList();
				icon = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
				clickHandler = (panel, user, clickType, slot) -> {
					return true;
				};

				break;
			}
			default:
				return null;
		}

		return new PanelItemBuilder().
			icon(icon).
			name(name).
			description(GuiUtils.stringSplit(description, this.addon.getSettings().getLoreLineLength())).
			glow(false).
			clickHandler(clickHandler).
			build();
	}


	/**
	 * This method creates button for lore element.
	 * @param loreElement String that represents current lore element.
	 * @return PanelItem.
	 */
	private PanelItem getLoreButton(String loreElement)
	{
		Material icon;
		String name = loreElement;
		List<String> description = new ArrayList<>();
		description.add(this.user.getTranslation(REFERENCE_DESCRIPTION + "lore." + loreElement.toLowerCase()));

		PanelItem.ClickHandler clickHandler = (panel, user1, clickType, slot) -> true;

		switch (Lore.valueOf(loreElement))
		{
			case DESCRIPTION:
			{
				icon = Material.WRITTEN_BOOK;
				break;
			}
			case ORIGINAL_BIOME:
			{
				icon = Material.FILLED_MAP;
				break;
			}
			case REQUIRED_LEVEL:
			{
				icon = Material.BEACON;
				break;
			}
			case REQUIRED_MONEY:
			{
				icon = Material.DIAMOND;
				break;
			}
			case REQUIRED_PERMISSION:
			{
				icon = Material.COMMAND_BLOCK;
				break;
			}
			default:
			{
				icon = Material.BARRIER;
				break;
			}
		}

		return new PanelItemBuilder().
			name(name).
			icon(icon).
			description(GuiUtils.stringSplit(description, this.addon.getSettings().getLoreLineLength())).
			clickHandler(clickHandler).
			glow(false).
			build();
	}


// ---------------------------------------------------------------------
// Section: Select GUI
// ---------------------------------------------------------------------


	/**
	 * This class opens new GUI that add an element from all available lore values.
	 */
	private class AddLoreElementGUI
	{
		private AddLoreElementGUI(Consumer<String> selectedElement)
		{
			PanelBuilder panelBuilder = new PanelBuilder().
				name(EditLoreGUI.this.user.getTranslation("biomes.gui.title.admin.lore-add")).
				user(EditLoreGUI.this.user);

			GuiUtils.fillBorder(panelBuilder, 5, Material.MAGENTA_STAINED_GLASS_PANE);

			int currentIndex = 10;

			List<String> values = Arrays.stream(Lore.values()).map(Enum::name).collect(Collectors.toList());

			// Populate list with all elements.

			for (String value : values)
			{
				PanelItem item = EditLoreGUI.this.getLoreButton(value);

				item.setClickHandler((panel, user1, clickType, slot) -> {
					selectedElement.accept(value);
					return true;
				});

				panelBuilder.item(currentIndex++, item);

				// Border element
				if (currentIndex % 9 == 8)
				{
					currentIndex += 2;
				}

				// Just in case. Should never occur.
				if (currentIndex % 9 == 0)
				{
					currentIndex++;
				}

				// Just in case. Should never occur.
				if (currentIndex > 35)
				{
					break;
				}
			}

			panelBuilder.build();
		}
	}


	/**
	 * This class opens new GUI that remove an element from all available lore values.
	 */
	private class RemoveLoreElementGUI
	{
		private RemoveLoreElementGUI(BiConsumer<String, Integer> selectedElement)
		{
			PanelBuilder panelBuilder = new PanelBuilder().
				name(EditLoreGUI.this.user.getTranslation("biomes.gui.title.admin.lore-remove")).
				user(EditLoreGUI.this.user);

			GuiUtils.fillBorder(panelBuilder, 5, Material.MAGENTA_STAINED_GLASS_PANE);

			int currentIndex = 10;

			List<String> values = EditLoreGUI.this.activeValues;

			for (int i = 0; i < values.size(); i++)
			{
				final int counter = i;

				String value = values.get(counter);
				PanelItem item = EditLoreGUI.this.getLoreButton(value);

				item.setClickHandler((panel, user1, clickType, slot) -> {
					selectedElement.accept(value, counter);
					return true;
				});

				panelBuilder.item(currentIndex++, item);

				// Border element
				if (currentIndex % 9 == 8)
				{
					currentIndex += 2;
				}

				// Just in case. Should never occur.
				if (currentIndex % 9 == 0)
				{
					currentIndex++;
				}

				// Just in case. Should never occur.
				if (currentIndex > 35)
				{
					break;
				}
			}

			panelBuilder.build();
		}
	}


// ---------------------------------------------------------------------
// Section: Private classes
// ---------------------------------------------------------------------


	/**
	 * This CustomPanelListener allows to move items in current panel.
	 */
	private class CustomPanelListener implements PanelListener
	{
		@Override
		public void setup()
		{
		    // No setup required
		}


		@Override
		public void onInventoryClose(InventoryCloseEvent inventoryCloseEvent)
		{
		    // No action on close
		}


		@Override
		public void onInventoryClick(User user, InventoryClickEvent event)
		{
			// First row of elements should be ignored, as it contains buttons and blocked slots.
			event.setCancelled(event.getRawSlot() < 9 ||
				event.getRawSlot() < 35 ||
				event.getRawSlot() % 9 == 0 ||
				event.getRawSlot() % 9 == 8);
		}
	}


// ---------------------------------------------------------------------
// Section: Enums
// ---------------------------------------------------------------------


	/**
	 * This enum holds all button values in current gui.
	 */
	private enum Button
	{
		SAVE,
		ADD,
		REMOVE,
		VIEW,
		RETURN
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


	/**
	 * List of lore elements that are currently enabled.
	 */
	private List<String> activeValues;


// ---------------------------------------------------------------------
// Section: Constants
// ---------------------------------------------------------------------


	private final static String REFERENCE_DESCRIPTION = "biomes.gui.descriptions.admin.";
}
