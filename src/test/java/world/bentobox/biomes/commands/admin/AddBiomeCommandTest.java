package world.bentobox.biomes.commands.admin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.CommandsManager;
import world.bentobox.bentobox.managers.IslandWorldManager;
import world.bentobox.bentobox.managers.IslandsManager;
import world.bentobox.bentobox.managers.LocalesManager;
import world.bentobox.bentobox.managers.PlaceholdersManager;
import world.bentobox.bentobox.util.Util;
import world.bentobox.biomes.BiomesAddon;
import world.bentobox.biomes.BiomesAddonManager;
import world.bentobox.biomes.config.Settings;
import world.bentobox.biomes.database.objects.BiomesObject;
import world.bentobox.biomes.utils.Utils;

/**
 * @author tastybento
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Bukkit.class, BentoBox.class, Utils.class, Util.class})
public class AddBiomeCommandTest {

    @Mock
    private CompositeCommand ic;
    private UUID uuid;
    @Mock
    private User user;
    @Mock
    private IslandsManager im;
    @Mock
    private Island island;
    @Mock
    private BiomesAddon addon;
    @Mock
    private World world;
    @Mock
    private IslandWorldManager iwm;
    @Mock
    private GameModeAddon gameModeAddon;

    private AddBiomeCommand abc;

    @Mock
    private Player p;
    @Mock
    private LocalesManager lm;
    @Mock
    private PlaceholdersManager phm;
    @Mock
    private BiomesAddonManager am;
    @Mock
    private Inventory top;


    @BeforeClass
    public static void beforeClass() throws Exception {
        PowerMockito.mockStatic(Bukkit.class);
        // version - has to be 1.13 because code is only built to 1.13
        when(Bukkit.getBukkitVersion()).thenReturn("1.13");
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        // Set up plugin
        BentoBox plugin = mock(BentoBox.class);
        Whitebox.setInternalState(BentoBox.class, "instance", plugin);
        User.setPlugin(plugin);

        // Command manager
        CommandsManager cm = mock(CommandsManager.class);
        when(plugin.getCommandsManager()).thenReturn(cm);
        // Addon
        when(ic.getAddon()).thenReturn(addon);
        when(ic.getPermissionPrefix()).thenReturn("bskyblock.");
        when(ic.getLabel()).thenReturn("island");
        when(ic.getTopLabel()).thenReturn("island");
        when(ic.getWorld()).thenReturn(world);
        when(ic.getTopLabel()).thenReturn("bsb");

        // IWM friendly name
        when(iwm.getFriendlyName(any())).thenReturn("BSkyBlock");
        when(iwm.inWorld(any(World.class))).thenReturn(true);
        Optional<GameModeAddon> optionalAddon = Optional.of(gameModeAddon);
        when(iwm.getAddon(any())).thenReturn(optionalAddon);
        when(plugin.getIWM()).thenReturn(iwm);

        // Game Mode Addon
        @NonNull
        Optional<CompositeCommand> optionalAdmin = Optional.of(ic);
        when(gameModeAddon.getAdminCommand()).thenReturn(optionalAdmin);

        // World
        when(world.toString()).thenReturn("world");
        when(world.getName()).thenReturn("BSkyBlock_world");

        // Player
        // Sometimes use Mockito.withSettings().verboseLogging()
        when(user.isOp()).thenReturn(false);
        uuid = UUID.randomUUID();
        when(user.getUniqueId()).thenReturn(uuid);
        when(user.getPlayer()).thenReturn(p);
        when(user.getName()).thenReturn("tastybento");
        when(user.getPermissionValue(anyString(), anyInt())).thenReturn(-1);
        when(user.isPlayer()).thenReturn(true);

        // Mock item factory (for itemstacks)
        PowerMockito.mockStatic(Bukkit.class);
        ItemFactory itemFactory = mock(ItemFactory.class);
        when(Bukkit.getItemFactory()).thenReturn(itemFactory);
        ItemMeta itemMeta = mock(ItemMeta.class);
        when(itemFactory.getItemMeta(any())).thenReturn(itemMeta);

        // Island
        when(plugin.getIslands()).thenReturn(im);
        when(im.getIsland(any(), any(User.class))).thenReturn(island);

        // Locales
        User.setPlugin(plugin);
        when(lm.get(any(), any())).thenAnswer(invocation -> invocation.getArgument(1, String.class));
        when(plugin.getLocalesManager()).thenReturn(lm);
        when(phm.replacePlaceholders(any(), anyString())).thenAnswer(invocation -> invocation.getArgument(1, String.class));
        when(plugin.getPlaceholdersManager()).thenReturn(phm);
        when(user.getTranslation(anyString(), anyString(), anyString())).thenAnswer(invocation -> invocation.getArgument(0, String.class));
        when(user.getTranslation(anyString())).thenAnswer(invocation -> invocation.getArgument(0, String.class));

        // Utils
        PowerMockito.mockStatic(Utils.class);
        when(Utils.getGameMode(any())).thenReturn("BSkyBlock");

        // Addon
        when(am.containsBiome(anyString())).thenReturn(true);
        when(addon.getAddonManager()).thenReturn(am);
        // Settings
        Settings settings = new Settings();
        when(addon.getSettings()).thenReturn(settings);

        // Biome Object
        BiomesObject bo = mock(BiomesObject.class);
        when(bo.getFriendlyName()).thenReturn("friendly");
        when(am.createBiome(anyString(), anyString())).thenReturn(bo);
        when(bo.getBiome()).thenReturn(Biome.BADLANDS);
        when(bo.getIcon()).thenReturn(new ItemStack(Material.DEAD_BUSH));
        when(bo.getRequiredPermissions()).thenReturn(Collections.emptySet());
        when(bo.getDescription()).thenReturn(Collections.singletonList("Badlands biome"));
        when(bo.getUniqueId()).thenReturn("badlands");
        when(bo.getWorld()).thenReturn("world");

        // Util
        PowerMockito.mockStatic(Util.class);
        when(Util.getWorld(any())).thenReturn(world);

        // Inventory
        when(top.getSize()).thenReturn(9);

        when(Bukkit.createInventory(any(), Mockito.anyInt(), any())).thenReturn(top);


        abc = new AddBiomeCommand(addon, ic);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        User.clearUsers();
    }

    /**
     * Test method for {@link world.bentobox.biomes.commands.admin.AddBiomeCommand#AddBiomeCommand(world.bentobox.bentobox.api.addons.Addon, world.bentobox.bentobox.api.commands.CompositeCommand)}.
     */
    @Test
    public void testAddBiomeCommand() {
        assertEquals("add", abc.getLabel());
    }

    /**
     * Test method for {@link world.bentobox.biomes.commands.admin.AddBiomeCommand#setup()}.
     */
    @Test
    public void testSetup() {
        assertEquals("bskyblock.admin.biomes.add", abc.getPermission());
        assertEquals("biomes.commands.admin.add.parameters", abc.getParameters());
        assertEquals("biomes.commands.admin.add.description", abc.getDescription());
        assertFalse(abc.isOnlyPlayer());

    }

    /**
     * Test method for {@link world.bentobox.biomes.commands.admin.AddBiomeCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecutePlayerNoArgs() {
        assertTrue(abc.canExecute(user, "add", Collections.emptyList()));
    }

    /**
     * Test method for {@link world.bentobox.biomes.commands.admin.AddBiomeCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecuteConsoleNoArgs() {
        CommandSender sender = mock(CommandSender.class);
        User console = User.getInstance(sender);
        assertFalse(abc.canExecute(console, "add", Collections.emptyList()));
        verify(lm).get(eq(console), eq("commands.help.header"));
    }

    /**
     * Test method for {@link world.bentobox.biomes.commands.admin.AddBiomeCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecutePlayer2Args() {
        assertFalse(abc.canExecute(user, "add", Arrays.asList("arg1", "arg2")));
        verify(user).sendMessage(eq("biomes.errors.too-many-arguments"));
    }

    /**
     * Test method for {@link world.bentobox.biomes.commands.admin.AddBiomeCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecutePlayer1ArgUnknownBiome() {
        when(am.containsBiome(anyString())).thenReturn(false);
        assertTrue(abc.canExecute(user, "add", Collections.singletonList("arg")));
        verify(user, never()).sendMessage(any(), any(), any());
    }

    /**
     * Test method for {@link world.bentobox.biomes.commands.admin.AddBiomeCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecutePlayer1ArgKnownBiome() {
        assertFalse(abc.canExecute(user, "add", Collections.singletonList("arg")));
        verify(user).sendMessage(eq("biomes.errors.unique-id"), eq("[id]"), eq("arg"));
    }

    /**
     * Test method for {@link world.bentobox.biomes.commands.admin.AddBiomeCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testExecuteUserStringListOfStringConsole() {
        when(user.isPlayer()).thenReturn(false);
        assertTrue(abc.execute(user, "add", Collections.singletonList("arg")));
        verify(am).createBiome(eq("BSkyBlock_arg"), eq("BSkyBlock_world"));
        verify(user).sendMessage(eq("biomes.messages.biome-created"),
                eq("[id]"),
                eq("arg"));
    }

    /**
     * Test method for {@link world.bentobox.biomes.commands.admin.AddBiomeCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testExecuteUserStringListOfStringPlayer() {
        assertTrue(abc.execute(user, "add", Collections.singletonList("arg")));
        verify(am).createBiome(eq("BSkyBlock_arg"), eq("BSkyBlock_world"));
    }

    /**
     * Test method for {@link world.bentobox.biomes.commands.admin.AddBiomeCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testExecuteUserStringListOfStringPlayerNoArgs() {
        assertTrue(abc.execute(user, "add", Collections.emptyList()));
        // TODO: test conversation
    }

}
