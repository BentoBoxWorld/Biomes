package world.bentobox.biomes.panels.admin;


import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.conversations.*;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;
import world.bentobox.biomes.BiomesAddon;
import world.bentobox.biomes.panels.CommonGUI;
import world.bentobox.biomes.panels.GuiUtils;


/**
 * This is main Admin GUI that is opened with admin command.
 */
public class AdminGUI extends CommonGUI
{
	/**
	 * {@inheritDoc}
	 */
	public AdminGUI(BiomesAddon addon,
		World world,
		User user,
		String topLabel,
		String permissionPrefix)
	{
		super(addon, world, user, topLabel, permissionPrefix);
	}



	/**
	 * This method construct admin panel with predefined button placements.
	 */
	@Override
	public void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder().user(this.user).name(
			this.user.getTranslation("biomes.gui.title.admin.main-gui"));

		GuiUtils.fillBorder(panelBuilder, Material.ORANGE_STAINED_GLASS_PANE);

		// Change Other players Biome
		panelBuilder.item(20, this.createButton(Button.CHANGE_USER_BIOME));

		// Add New Biome
		panelBuilder.item(13, this.createButton(Button.ADD_BIOME));
		// Edit Biome
		panelBuilder.item(22, this.createButton(Button.EDIT_BIOME));
		// Remove Biome
		panelBuilder.item(31, this.createButton(Button.DELETE_BIOME));

		// Import Biomes
		panelBuilder.item(24, this.createButton(Button.IMPORT_BIOMES));
		// Edit Addon Settings
		panelBuilder.item(25, this.createButton(Button.EDIT_SETTINGS));

		panelBuilder.build();
	}


	/**
	 * This method returns button for admin panel of given type.
	 * @param button Type of button.
	 * @return new panel button with requested type.
	 */
	private PanelItem createButton(Button button)
	{
		ItemStack icon;
		String name;
		List<String> description;
		boolean glow;
		PanelItem.ClickHandler clickHandler;

		String permissionSuffix;

		switch (button)
		{
			case CHANGE_USER_BIOME:
			{
				permissionSuffix = SET;

				name = this.user.getTranslation("biomes.gui.buttons.admin.change");
				description = Collections.singletonList(this.user.getTranslation("biomes.gui.descriptions.admin.change"));
				icon = new ItemStack(Material.LEVER);
				clickHandler = (panel, user, clickType, slot) -> {
					new ListUsersGUI(this).build();
					return true;
				};
				glow = false;

				break;
			}
			case ADD_BIOME:
			{
				permissionSuffix = ADD;

				name = this.user.getTranslation("biomes.gui.buttons.admin.add");
				description = Collections.singletonList(this.user.getTranslation("biomes.gui.descriptions.admin.add"));
				icon = new ItemStack(Material.BOOK);
				clickHandler = (panel, user, clickType, slot) -> {
					this.getNewUniqueID(uniqueID ->
						{
							String worldName = Util.getWorld(this.world).getName();
							String newName = worldName + "-" + uniqueID.toLowerCase();

							new EditBiomeGUI(AdminGUI.this, this.addon.getAddonManager().createBiome(newName, worldName)).build();
						},
						this.user.getTranslation("biomes.gui.questions.admin.uniqueID"),
						null);

					return true;
				};
				glow = false;

				break;
			}
			case EDIT_BIOME:
			{
				permissionSuffix = EDIT;

				name = this.user.getTranslation("biomes.gui.buttons.admin.edit");
				description = Collections.singletonList(this.user.getTranslation("biomes.gui.descriptions.admin.edit"));
				icon = new ItemStack(Material.ANVIL);
				clickHandler = (panel, user, clickType, slot) -> {
					new ListBiomesGUI(this, true).build();
					return true;
				};
				glow = false;

				break;
			}
			case DELETE_BIOME:
			{
				permissionSuffix = DELETE;

				name = this.user.getTranslation("biomes.gui.buttons.admin.remove");
				description = Collections.singletonList(this.user.getTranslation("biomes.gui.descriptions.admin.remove"));
				icon = new ItemStack(Material.LAVA_BUCKET);
				clickHandler = (panel, user, clickType, slot) -> {
					new ListBiomesGUI(this, false).build();
					return true;
				};
				glow = false;

				break;
			}
			case IMPORT_BIOMES:
			{
				permissionSuffix = IMPORT;

				name = this.user.getTranslation("biomes.gui.buttons.admin.import");
				description = Collections.singletonList(this.user.getTranslation("biomes.gui.descriptions.admin.import"));
				icon = new ItemStack(Material.HOPPER);
				clickHandler = (panel, user, clickType, slot) -> {
					if (clickType.isRightClick())
					{
						this.overwriteMode = !this.overwriteMode;
						this.build();
					}
					else
					{
						// Run import command.
						this.user.performCommand(this.topLabel + " " + BIOMES + " " + IMPORT +
							(this.overwriteMode ? " overwrite" : ""));
					}
					return true;
				};
				glow = this.overwriteMode;

				break;
			}
			case EDIT_SETTINGS:
			{
				permissionSuffix = SETTINGS;

				name = this.user.getTranslation("biomes.gui.buttons.admin.settings");
				description = Collections.singletonList(this.user.getTranslation("biomes.gui.descriptions.admin.settings"));
				icon = new ItemStack(Material.CRAFTING_TABLE);
				clickHandler = (panel, user, clickType, slot) -> {
					new EditSettingsGUI(this).build();
					return true;
				};
				glow = false;

				break;
			}
			default:
				// This should never happen.
				return null;
		}

		// If user does not have permission to run command, then change icon and clickHandler.
		final String actionPermission = this.permissionPrefix + ADMIN + "." + BIOMES + "." + permissionSuffix;

		if (!this.user.hasPermission(actionPermission))
		{
			icon = new ItemStack(Material.BARRIER);
			clickHandler = (panel, user, clickType, slot) -> {
				this.user.sendMessage("general.errors.no-permission", "[permission]", actionPermission);
				return true;
			};
		}

		return new PanelItemBuilder().
			icon(icon).
			name(name).
			description(GuiUtils.stringSplit(description, this.addon.getSettings().getLoreLineLength())).
			glow(glow).
			clickHandler(clickHandler).
			build();
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
	private void getNewUniqueID(Consumer<String> consumer,
		@NonNull String question,
		@Nullable String message)
	{
		final User user = this.user;

		Conversation conversation =
			new ConversationFactory(BentoBox.getInstance()).withFirstPrompt(
				new ValidatingPrompt()
				{

					/**
					 * Gets the text to display to the user when
					 * this prompt is first presented.
					 *
					 * @param context Context information about the
					 * conversation.
					 * @return The text to display.
					 */
					@Override
					public String getPromptText(ConversationContext context)
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
					 * Override this method to check the validity of
					 * the player's input.
					 *
					 * @param context Context information about the
					 * conversation.
					 * @param input The player's raw console input.
					 * @return True or false depending on the
					 * validity of the input.
					 */
					@Override
					protected boolean isInputValid(ConversationContext context, String input)
					{
						String worldName = Util.getWorld(AdminGUI.this.world).getName();
						String newName = worldName + "-" + input.toLowerCase();

						return !AdminGUI.this.addon.getAddonManager().containsBiome(newName);
					}


					/**
					 * Optionally override this method to
					 * display an additional message if the
					 * user enters an invalid input.
					 *
					 * @param context Context information
					 * about the conversation.
					 * @param invalidInput The invalid input
					 * provided by the user.
					 * @return A message explaining how to
					 * correct the input.
					 */
					@Override
					protected String getFailedValidationText(ConversationContext context,
						String invalidInput)
					{
						return user.getTranslation("biomes.errors.unique-id", "[id]", invalidInput);
					}


					/**
					 * Override this method to accept and processes
					 * the validated input from the user. Using the
					 * input, the next Prompt in the prompt graph
					 * should be returned.
					 *
					 * @param context Context information about the
					 * conversation.
					 * @param input The validated input text from
					 * the user.
					 * @return The next Prompt in the prompt graph.
					 */
					@Override
					protected Prompt acceptValidatedInput(ConversationContext context, String input)
					{
						// Add answer to consumer.
						consumer.accept(input);
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
		CHANGE_USER_BIOME,
		ADD_BIOME,
		EDIT_BIOME,
		DELETE_BIOME,
		IMPORT_BIOMES,
		EDIT_SETTINGS
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

	/**
	 * Overwrite mode for import manager.
	 */
	private boolean overwriteMode;
}
