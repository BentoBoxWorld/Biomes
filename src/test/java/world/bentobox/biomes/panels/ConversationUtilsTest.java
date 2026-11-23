package world.bentobox.biomes.panels;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.event.player.PlayerQuitEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import world.bentobox.bentobox.api.prompts.ChatInputListener;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.biomes.CommonTestSetup;

/**
 * Tests that {@link ConversationUtils} hands its questions to BentoBox's prompts and passes the answers back
 * the way its callers expect: the value on success, and null (or false for a confirmation) when the player
 * backs out.
 * <p>
 * Dialogs cannot be built without a server, so they are switched off here and every prompt is answered in
 * chat. Translations come back as their locale key, so the cancel, done and confirm words are the keys.
 */
class ConversationUtilsTest extends CommonTestSetup
{
    private static final String CANCEL = "prompts.chat.cancel-word";

    private User user;

    private final ChatInputListener listener = new ChatInputListener();

    private final List<Object> answers = new ArrayList<>();


    @Override
    @BeforeEach
    public void setUp() throws Exception
    {
        super.setUp();
        this.plugin.getSettings().setDialogPrompts(false);
        this.user = User.getInstance(this.mockPlayer);
    }


    @Override
    @AfterEach
    public void tearDown() throws Exception
    {
        PlayerQuitEvent quit = mock(PlayerQuitEvent.class);
        when(quit.getPlayer()).thenReturn(this.mockPlayer);
        this.listener.onQuit(quit);
        super.tearDown();
    }


    /**
     * Sends a chat message as the player and runs the task that carries it to the main thread.
     */
    private void say(String text)
    {
        AsyncChatEvent chat = mock(AsyncChatEvent.class);
        when(chat.getPlayer()).thenReturn(this.mockPlayer);
        when(chat.message()).thenReturn(Component.text(text));
        this.listener.onChat(chat);
        ArgumentCaptor<Runnable> task = ArgumentCaptor.forClass(Runnable.class);
        verify(this.sch, atLeastOnce()).runTask(eq(this.plugin), task.capture());
        task.getValue().run();
    }


    @Test
    void testStringInput()
    {
        ConversationUtils.createStringInput(this.answers::add, this.user, "Search for?", "Updated");
        verify(this.mockPlayer).closeInventory();
        assertTrue(ChatInputListener.isWaitingFor(this.uuid));

        this.say("diamond");

        assertEquals(List.of("diamond"), this.answers);
        assertFalse(ChatInputListener.isWaitingFor(this.uuid));
    }


    @Test
    void testStringInputCancelGivesNull()
    {
        ConversationUtils.createStringInput(this.answers::add, this.user, "Search for?", null);

        this.say(CANCEL);

        assertEquals(1, this.answers.size());
        assertNull(this.answers.getFirst());
        assertFalse(ChatInputListener.isWaitingFor(this.uuid));
    }


    @Test
    void testNumericInput()
    {
        ConversationUtils.createNumericInput(this.answers::add, this.user, "How many?", 0, 10);

        this.say("lots");
        this.say("11");
        assertTrue(this.answers.isEmpty());
        assertTrue(ChatInputListener.isWaitingFor(this.uuid));

        this.say("7");

        assertEquals(1, this.answers.size());
        assertEquals(7, ((Number) this.answers.getFirst()).intValue());
    }


    @Test
    void testNumericInputCancelGivesNull()
    {
        ConversationUtils.createNumericInput(this.answers::add, this.user, "How many?", 0, 10);

        this.say(CANCEL);

        assertEquals(1, this.answers.size());
        assertNull(this.answers.getFirst());
    }


    @Test
    void testStringListInput()
    {
        ConversationUtils.createStringListInput(this.answers::add, this.user, "Describe it", "Saved");

        this.say("&aFirst");
        this.say("Second");
        assertTrue(this.answers.isEmpty());
        this.say("prompts.chat.done-word");

        assertEquals(List.of(List.of("§aFirst", "Second")), this.answers);
    }


    @Test
    void testConfirmation()
    {
        ConversationUtils.createConfirmation(this.answers::add, this.user, "Wipe everything?", "Wiped");

        this.say("prompts.chat.confirm-word");

        assertEquals(List.of(true), this.answers);
    }


    @Test
    void testConfirmationDeclined()
    {
        ConversationUtils.createConfirmation(this.answers::add, this.user, "Wipe everything?", "Wiped");

        this.say("no");

        assertEquals(List.of(false), this.answers);
    }


    /**
     * The consumer receives what the player typed, not the sanitized ID: callers sanitize it themselves.
     */
    @Test
    void testIDStringInputGivesRawText()
    {
        ConversationUtils.createIDStringInput(this.answers::add, text -> true, this.user, "New ID?", "Created",
            "biomes.conversations.object-already-exists");

        this.say("My New-Biome");

        assertEquals(List.of("My New-Biome"), this.answers);
        assertFalse(ChatInputListener.isWaitingFor(this.uuid));
    }


    /**
     * A refused ID is explained with the failure message and asked for again; nothing reaches the consumer.
     */
    @Test
    void testIDStringInputRefusedIsAskedAgain()
    {
        List<String> checked = new ArrayList<>();
        ConversationUtils.createIDStringInput(this.answers::add, text ->
        {
            checked.add(text);
            return !text.equals("taken");
        }, this.user, "New ID?", null, "biomes.conversations.object-already-exists");

        this.say("taken");

        assertEquals(List.of("taken"), checked);
        assertTrue(this.answers.isEmpty());
        assertTrue(ChatInputListener.isWaitingFor(this.uuid));
        ArgumentCaptor<Component> sent = ArgumentCaptor.forClass(Component.class);
        verify(this.mockPlayer, atLeastOnce()).sendMessage(sent.capture());
        assertTrue(sent.getAllValues().stream().
            map(PlainTextComponentSerializer.plainText()::serialize).
            anyMatch(text -> text.contains("biomes.conversations.object-already-exists")));

        this.say("free");

        assertEquals(List.of("free"), this.answers);
    }


    @Test
    void testIDStringInputCancelGivesNull()
    {
        ConversationUtils.createIDStringInput(this.answers::add, text -> true, this.user, "New ID?", null, null);

        this.say(CANCEL);

        assertEquals(1, this.answers.size());
        assertNull(this.answers.getFirst());
    }
}
