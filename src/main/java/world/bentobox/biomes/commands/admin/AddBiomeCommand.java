package world.bentobox.biomes.commands.admin;


import java.util.List;
import java.util.function.Consumer;

import org.bukkit.World;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.ValidatingPrompt;
import org.eclipse.jdt.annotation.NonNull;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;
import world.bentobox.biomes.commands.ExpandedCompositeCommand;
import world.bentobox.biomes.database.objects.BiomesObject;
import world.bentobox.biomes.panels.admin.EditBiomeGUI;
import world.bentobox.biomes.utils.Utils;


/**
 * This command allows to add new biome.
 * This will be dummy biome, without proper name and settings. All could be edited via /biomes edit command.
 */
public class AddBiomeCommand extends ExpandedCompositeCommand
{
	/**
	 * Default constructor. Inits command with "add" parameter.
	 */
	public AddBiomeCommand(Addon addon, CompositeCommand parent)
	{
		super(addon, parent, "add");
	}


	/**
	 * This method setup this command permissions, parameters and descriptions.
	 * {@inheritDoc}
	 */
	@Override
	public void setup()
	{
		this.setPermission("admin.biomes.add");
		this.setParametersHelp("biomes.commands.admin.add.parameters");
		this.setDescription("biomes.commands.admin.add.description");
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canExecute(User user, String label, List<String> args)
	{
		if (user.isPlayer() && args.isEmpty())
		{
			return true;
		}
		else if (args.size() > 1)
		{
			user.sendMessage("biomes.errors.too-many-arguments");
		}
		else if (args.size() == 1)
		{
			// If biome with given ID already exist, then show error. Otherwise process command.
			if (!this.addon.getAddonManager().containsBiome(Utils.getGameMode(this.getWorld()) + "_" + args.get(0)))
			{
				return true;
			}
			else
			{
				user.sendMessage("biomes.errors.unique-id", "[id]", args.get(0));
				return false;
			}
		}

		this.showHelp(this, user);
		return false;
	}


	/**
	 *
	 * {@inheritDoc}
	 */
	@Override
	public boolean execute(User user, String label, List<String> args)
	{
		if (user.isPlayer() && args.isEmpty())
		{
			// Shows BiomesPanel in Edit mode.

			this.getNewUniqueID(uniqueID ->
				{
					String worldName = Util.getWorld(this.getWorld()).getName();
					String newName = Utils.getGameMode(this.getWorld()) + "_" + uniqueID.toLowerCase();

					new EditBiomeGUI(this.addon,
						this.getWorld(),
						user,
						this.getTopLabel(),
						this.getPermissionPrefix(),
						this.addon.getAddonManager().createBiome(newName, worldName)).build();
				},
				user.getTranslation("biomes.gui.questions.admin.uniqueID"),
				user,
				this.getWorld());

			return true;
		}
		else
		{
			String worldName = Util.getWorld(this.getWorld()).getName();
			String newName = Utils.getGameMode(this.getWorld()) + "_" + args.get(0).toLowerCase();

			BiomesObject biome = this.addon.getAddonManager().createBiome(newName, worldName);

			if (user.isPlayer())
			{
				new EditBiomeGUI(this.addon,
					this.getWorld(),
					user,
					this.getTopLabel(),
					this.getPermissionPrefix(),
					biome).build();
			}
			else
			{
				user.sendMessage("biomes.messages.biome-created",
					"[id]",
					args.get(0));
			}

			return true;
		}
	}


// ---------------------------------------------------------------------
// Section: Conversation
// ---------------------------------------------------------------------

	/**
	 * This method will close opened gui and writes inputText in chat. After players answers on inputText in
	 * chat, message will trigger consumer and gui will reopen.
	 * @param consumer Consumer that accepts player output text.
	 * @param question Message that will be displayed in chat when player triggers conversion.
	 */
	private void getNewUniqueID(Consumer<String> consumer,
		@NonNull String question,
		@NonNull User user,
		@NonNull World world)
	{
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
						String newName = Utils.getGameMode(world) + "_" + input.toLowerCase();

						return !AddBiomeCommand.this.addon.getAddonManager().containsBiome(newName);
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
}
