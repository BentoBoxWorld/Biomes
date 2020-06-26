package world.bentobox.biomes.panels.admin;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.biomes.BiomesAddon;
import world.bentobox.biomes.database.objects.BiomesObject;
import world.bentobox.biomes.panels.CommonGUI;
import world.bentobox.biomes.panels.GuiUtils;
import world.bentobox.biomes.panels.util.NumberGUI;
import world.bentobox.biomes.panels.util.SelectBiomeGUI;
import world.bentobox.biomes.panels.util.SelectBlocksGUI;
import world.bentobox.biomes.panels.util.StringListGUI;
import world.bentobox.biomes.utils.Utils;


/**
 * This class contains methods that allows to edit specific biome object.
 */
public class EditBiomeGUI extends CommonGUI
{
	private static final String CURRENT_VALUE = "biomes.gui.descriptions.current-value";
    private static final String VALUE = "[value]";


    /**
	 * {@inheritDoc}
	 * @param biome Object that must be edited.
	 */
	public EditBiomeGUI(CommonGUI parentPanel, BiomesObject biome)
	{
		super(parentPanel);
		this.biome = biome;
	}


	/**
	 * {@inheritDoc}
	 * @param biome Object that must be edited.
	 */
	public EditBiomeGUI(BiomesAddon addon,
		World world,
		User user,
		String topLabel,
		String permissionPrefix,
		BiomesObject biome)
	{
		super(addon, world, user, topLabel, permissionPrefix);
		this.biome = biome;
	}


	/**
	 * This method builds all necessary elements in GUI panel.
	 */
	@Override
	public void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder().user(this.user).
			name(this.user.getTranslation("biomes.gui.title.admin.edit", "[biome]", this.biome.getFriendlyName()));

		GuiUtils.fillBorder(panelBuilder, Material.PURPLE_STAINED_GLASS_PANE);

		panelBuilder.item(19, this.createButton(Button.BIOME));
		panelBuilder.item(28, this.createButton(Button.ENVIRONMENT));

		panelBuilder.item(11, this.createButton(Button.NAME));
		panelBuilder.item(20, this.createButton(Button.ICON));
		panelBuilder.item(29, this.createButton(Button.DESCRIPTION));

		panelBuilder.item(21, this.createButton(Button.ORDER));

		panelBuilder.item(14, this.createButton(Button.LEVEL));
		panelBuilder.item(23, this.createButton(Button.COST));
		panelBuilder.item(32, this.createButton(Button.PERMISSION));

		panelBuilder.item(25, this.createButton(Button.DEPLOYED));

		panelBuilder.item(44, this.returnButton);

		panelBuilder.build();
	}


	/**
	 * This method returns button for edit panel of given type.
	 * @param button Type of button.
	 * @return new panel button with requested type.
	 */
	private PanelItem createButton(Button button)
	{
		PanelItemBuilder itemBuilder = new PanelItemBuilder();
		final int lineLength = this.addon.getSettings().getLoreLineLength();
		List<String> description = new ArrayList<>(2);
		switch (button)
		{
			case BIOME:
				itemBuilder.name(this.user.getTranslation("biomes.gui.buttons.admin.change-biome"));
				description.add(this.user.getTranslation("biomes.gui.descriptions.admin.change-biome"));
				description.add(this.user.getTranslation(CURRENT_VALUE,
					VALUE, this.biome.getBiome().name()));
				itemBuilder.description(GuiUtils.stringSplit(description, this.addon.getSettings().getLoreLineLength()));

				itemBuilder.icon(Material.WATER_BUCKET);
				itemBuilder.clickHandler((panel, user, clickType, slot) -> {
					// Open select gui
					new SelectBiomeGUI(this.user, this.biome.getBiome(), lineLength, (status, value) -> {
						this.biome.setBiome(value);
						this.build();
					});

					return true;
				});
				break;
			case PERMISSION:
				itemBuilder.name(this.user.getTranslation("biomes.gui.buttons.admin.required-permissions"));

				List<String> desc = new ArrayList<>(this.biome.getRequiredPermissions().size() + 1);
				desc.add(this.user.getTranslation(
					"biomes.gui.descriptions.admin.required-permissions"));

				for (String permission : this.biome.getRequiredPermissions())
				{
					desc.add(this.user.getTranslation("biomes.gui.descriptions.permission",
						"[permission]", permission));
				}

				itemBuilder.description(GuiUtils.stringSplit(desc, this.addon.getSettings().getLoreLineLength()));

				itemBuilder.icon(Material.REDSTONE_LAMP);
				itemBuilder.clickHandler((panel, user, clickType, slot) -> {
					new StringListGUI(this.user, this.biome.getRequiredPermissions(), lineLength, (status, value) -> {
						if (status)
						{
							this.biome.setRequiredPermissions(new HashSet<>(value));
						}

						this.build();
					});

					return true;
				});
				break;
			case LEVEL:
				itemBuilder.name(this.user.getTranslation("biomes.gui.buttons.admin.required-level"));
				description.add(this.user.getTranslation("biomes.gui.descriptions.admin.required-level"));
				description.add(this.user.getTranslation(CURRENT_VALUE,
					VALUE, Long.toString(this.biome.getRequiredLevel())));
				itemBuilder.description(GuiUtils.stringSplit(description, this.addon.getSettings().getLoreLineLength()));

				itemBuilder.icon(this.addon.isLevelProvided() ? Material.BEACON : Material.BARRIER);
				itemBuilder.clickHandler((panel, user, clickType, slot) -> {
					new NumberGUI(this.user, (int) this.biome.getRequiredLevel(), lineLength, (status, value) -> {
						if (status)
						{
							this.biome.setRequiredLevel(value);
						}

						this.build();
					});

					return true;
				});

				break;
			case COST:
			{
				itemBuilder.name(this.user.getTranslation("biomes.gui.buttons.admin.required-money"));
				description.add(this.user.getTranslation("biomes.gui.descriptions.admin.required-money"));
				description.add(this.user.getTranslation(CURRENT_VALUE,
					VALUE, Double.toString(this.biome.getRequiredCost())));
				itemBuilder.description(GuiUtils.stringSplit(description, this.addon.getSettings().getLoreLineLength()));

				itemBuilder.icon(this.addon.isEconomyProvided() ? Material.GOLD_INGOT : Material.BARRIER);
				itemBuilder.clickHandler((panel, user, clickType, slot) -> {
					// TODO: this must be implemented in gui.
					new NumberGUI(this.user, ((int) this.biome.getRequiredCost()), 0, lineLength, (status, value) -> {
						if (status)
						{
							this.biome.setRequiredCost(value);
						}

						this.build();
					});
					return true;
				});

				break;
			}
			case NAME:
			{
				itemBuilder.name(this.user.getTranslation("biomes.gui.buttons.admin.name"));
				description.add(this.user.getTranslation("biomes.gui.descriptions.admin.name"));
				description.add(this.user.getTranslation(CURRENT_VALUE,
					VALUE, this.biome.getFriendlyName()));
				itemBuilder.description(GuiUtils.stringSplit(description, this.addon.getSettings().getLoreLineLength()));

				itemBuilder.icon(Material.BOOK);
				itemBuilder.clickHandler((panel, user, clickType, slot) -> {

					this.startConversation(reply -> {
						this.biome.setFriendlyName(reply);
						this.build();
						},
						this.user.getTranslation("biomes.gui.questions.admin.name"),
						this.biome.getFriendlyName());

					return true;
				});
				break;
			}
			case DEPLOYED:
				itemBuilder.name(this.user.getTranslation("biomes.gui.buttons.admin.deployment"));
				description.add(this.user.getTranslation("biomes.gui.descriptions.admin.deployment"));
				description.add(this.user.getTranslation(CURRENT_VALUE,
					VALUE,
					this.biome.isDeployed() ?
						this.user.getTranslation("biomes.gui.descriptions.enabled") :
						this.user.getTranslation("biomes.gui.descriptions.disabled")));
				itemBuilder.description(GuiUtils.stringSplit(description, this.addon.getSettings().getLoreLineLength()));
				itemBuilder.icon(Material.LEVER);
				itemBuilder.clickHandler((panel, user, clickType, slot) -> {
					this.biome.setDeployed(!this.biome.isDeployed());
					panel.getInventory().setItem(slot, this.createButton(button).getItem());
					return true;
				});
				itemBuilder.glow(this.biome.isDeployed());
				break;
			case ICON:
				itemBuilder.name(this.user.getTranslation("biomes.gui.buttons.admin.icon"));

				itemBuilder.description(GuiUtils.stringSplit(
					this.user.getTranslation("biomes.gui.descriptions.admin.icon"),
					this.addon.getSettings().getLoreLineLength()));

				itemBuilder.icon(this.biome.getIcon());
				itemBuilder.clickHandler((panel, user, clickType, slot) -> {

					new SelectBlocksGUI(this.user, true, (status, materials) -> {
						if (status)
						{
							materials.forEach(material ->
								this.biome.setIcon(new ItemStack(material)));
						}

						this.build();
					});

					return true;
				});
				break;
			case DESCRIPTION:
				itemBuilder.name(this.user.getTranslation("biomes.gui.buttons.admin.description"));

				List<String> values = new ArrayList<>();
				values.add(this.user.getTranslation("biomes.gui.descriptions.admin.description"));
				values.add(this.user.getTranslation(CURRENT_VALUE, VALUE, ""));
				values.addAll(this.generateBiomesDescription(this.biome));

				itemBuilder.description(GuiUtils.stringSplit(values, this.addon.getSettings().getLoreLineLength()));
				itemBuilder.icon(Material.WRITTEN_BOOK);
				itemBuilder.clickHandler((panel, user, clickType, slot) -> {
					new StringListGUI(this.user, this.biome.getDescription(), lineLength, (status, value) -> {
						if (status)
						{
							this.biome.setDescription(value);
						}

						this.build();
					});

					return true;
				});
				break;
			case ORDER:
				itemBuilder.name(this.user.getTranslation("biomes.gui.buttons.admin.order"));
				description.add(this.user.getTranslation("biomes.gui.descriptions.admin.order"));
				description.add(this.user.getTranslation(CURRENT_VALUE,
					VALUE, Integer.toString(this.biome.getOrder())));
				itemBuilder.description(description);

				itemBuilder.icon(Material.DROPPER);
				itemBuilder.clickHandler((panel, user, clickType, slot) -> {
					new NumberGUI(this.user, this.biome.getOrder(), -1, 54, lineLength, (status, value) -> {
						if (status)
						{
							this.biome.setOrder(value);
						}

						this.build();
					});

					return true;
				});
				break;
			case ENVIRONMENT:
				itemBuilder.name(this.user.getTranslation("biomes.gui.buttons.admin.environment"));
				description.add(this.user.getTranslation("biomes.gui.descriptions.admin.environment"));

				switch (this.biome.getEnvironment())
				{
					case NORMAL:
						description.add(this.user.getTranslation("biomes.gui.descriptions.admin.environment-active",
							"[value]", "NORMAL"));
						description.add(this.user.getTranslation("biomes.gui.descriptions.admin.environment-inactive",
							"[value]", "NETHER"));
						description.add(this.user.getTranslation("biomes.gui.descriptions.admin.environment-inactive",
							"[value]", "THE_END"));

						itemBuilder.icon(Material.GRASS_BLOCK);
						break;
					case NETHER:
						description.add(this.user.getTranslation("biomes.gui.descriptions.admin.environment-inactive",
							"[value]", "NORMAL"));
						description.add(this.user.getTranslation("biomes.gui.descriptions.admin.environment-active",
							"[value]", "NETHER"));
						description.add(this.user.getTranslation("biomes.gui.descriptions.admin.environment-inactive",
							"[value]", "THE_END"));

						itemBuilder.icon(Material.NETHERRACK);
						break;
					case THE_END:
						description.add(this.user.getTranslation("biomes.gui.descriptions.admin.environment-inactive",
							"[value]", "NORMAL"));
						description.add(this.user.getTranslation("biomes.gui.descriptions.admin.environment-inactive",
							"[value]", "NETHER"));
						description.add(this.user.getTranslation("biomes.gui.descriptions.admin.environment-active",
							"[value]", "THE_END"));

						itemBuilder.icon(Material.END_STONE);
						break;
				}

				itemBuilder.description(description);

				itemBuilder.clickHandler((panel, user, clickType, slot) -> {

					if (clickType.isLeftClick())
					{
						this.biome.setEnvironment(Utils.getNextValue(World.Environment.values(),
							this.biome.getEnvironment()));
					}
					else
					{
						this.biome.setEnvironment(Utils.getPreviousValue(World.Environment.values(),
							this.biome.getEnvironment()));
					}

					panel.getItems().put(slot, this.createButton(button));

					return true;
				});
				break;
		}

		return itemBuilder.build();
	}


// ---------------------------------------------------------------------
// Section: Conversation
// ---------------------------------------------------------------------


	/**
	 * This method will close opened gui and writes inputText in chat. After players answers on inputText in
	 * chat, message will trigger consumer and gui will reopen.
	 * @param consumer Consumer that accepts player output text.
	 * @param question Message that will be displayed in chat when player triggers conversion.
	 * @param message Message that will be set in player text field when clicked on question.
	 */
	private void startConversation(Consumer<String> consumer, @NonNull String question, @Nullable String message)
	{
		final User user = this.user;

		Conversation conversation =
			new ConversationFactory(BentoBox.getInstance()).withFirstPrompt(
				new StringPrompt()
				{
					/**
					 * @see Prompt#getPromptText(ConversationContext)
					 */
					@Override
					public String getPromptText(ConversationContext conversationContext)
					{
						// Close input GUI.
						user.closeInventory();

						if (message != null)
						{
							// Create Edit Text message.
							TextComponent component = new TextComponent(user.getTranslation("biomes.gui.descriptions.admin.click-to-edit"));
							component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, message));
							// Send question and message to player.
							user.getPlayer().spigot().sendMessage(component);
						}

						// There are no editable message. Just return question.
						return question;
					}


					/**
					 * @see Prompt#acceptInput(ConversationContext, String)
					 */
					@Override
					public Prompt acceptInput(ConversationContext conversationContext, String answer)
					{
						// Add answer to consumer.
						consumer.accept(answer);
						// End conversation
						return Prompt.END_OF_CONVERSATION;
					}
				}).
				withLocalEcho(false).
				withPrefix(context -> user.getTranslation("biomes.gui.questions.prefix")).
				buildConversation(user.getPlayer());

		conversation.begin();
	}


// ---------------------------------------------------------------------
// Section: Enums
// ---------------------------------------------------------------------


	/**
	 * This enum shows which button should be created.
	 */
	private enum Button
	{
		BIOME,
		PERMISSION,
		COST,
		LEVEL,
		ORDER,
		ENVIRONMENT,
		DESCRIPTION,
		ICON,
		NAME,
		DEPLOYED
	}


// ---------------------------------------------------------------------
// Section: Variable
// ---------------------------------------------------------------------


	/**
	 * Holds a biome object that is edited.
	 */
	private BiomesObject biome;
}
