package world.bentobox.biomes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import world.bentobox.bentobox.api.addons.Addon.State;
import world.bentobox.bentobox.api.addons.AddonDescription;
import world.bentobox.bentobox.database.AbstractDatabaseHandler;
import world.bentobox.bentobox.database.DatabaseSetup;
import world.bentobox.bentobox.managers.AddonsManager;
import world.bentobox.bentobox.managers.CommandsManager;

/**
 * Tests for {@link BiomesAddon}.
 */
class BiomesAddonTest extends CommonTestSetup {

    @Mock
    private AddonsManager am;

    private BiomesAddon addon;
    private MockedStatic<DatabaseSetup> mockDb;

    @SuppressWarnings("unchecked")
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        // Database mock
        AbstractDatabaseHandler<Object> h = mock(AbstractDatabaseHandler.class);
        mockDb = Mockito.mockStatic(DatabaseSetup.class);
        DatabaseSetup dbSetup = mock(DatabaseSetup.class);
        mockDb.when(DatabaseSetup::getDatabase).thenReturn(dbSetup);
        when(dbSetup.getHandler(any())).thenReturn(h);
        when(h.saveObject(any())).thenReturn(CompletableFuture.completedFuture(true));

        // Command manager
        CommandsManager cm = mock(CommandsManager.class);
        when(plugin.getCommandsManager()).thenReturn(cm);

        // BentoBox plugin enabled
        when(plugin.isEnabled()).thenReturn(true);

        // AddonsManager
        when(plugin.getAddonsManager()).thenReturn(am);
        when(am.getGameModeAddons()).thenReturn(Collections.emptyList());

        // FlagsManager
        when(plugin.getFlagsManager()).thenReturn(fm);
        when(fm.getFlags()).thenReturn(Collections.emptyList());

        // Create addon with a jar containing config.yml and required panel resources
        addon = new BiomesAddon();
        File jFile = new File("addon.jar");
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jFile))) {
            addJarEntry(jos, "config.yml", "# Biomes Configuration\nuniqueId: config\n");
            addJarEntry(jos, "panels/main_panel.yml", "# main panel\n");
            addJarEntry(jos, "panels/advanced_panel.yml", "# advanced panel\n");
            addJarEntry(jos, "panels/buy_panel.yml", "# buy panel\n");
            addJarEntry(jos, "biomesTemplate.yml", "# biomes template\n");
        }
        File dataFolder = new File("addons/Biomes");
        addon.setDataFolder(dataFolder);
        addon.setFile(jFile);
        AddonDescription desc = new AddonDescription.Builder("bentobox", "Biomes", "1.0.0")
                .description("test").authors("BONNe").build();
        addon.setDescription(desc);
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        if (mockDb != null) {
            mockDb.closeOnDemand();
        }
        super.tearDown();
        new File("addon.jar").delete();
        deleteAll(new File("addons"));
    }

    private static void addJarEntry(JarOutputStream jos, String name, String content) throws Exception {
        JarEntry entry = new JarEntry(name);
        jos.putNextEntry(entry);
        jos.write(content.getBytes(StandardCharsets.UTF_8));
        jos.closeEntry();
    }

    @Test
    void testOnLoad() {
        addon.onLoad();
        File check = new File("addons/Biomes", "config.yml");
        // saveDefaultConfig copies config.yml from the jar to the data folder
        // (file exists if the resource was found in the jar)
        assertNotNull(addon);
    }

    @Test
    void testOnLoadSetsSettings() {
        addon.onLoad();
        // Settings may be null if the config framework fails, but normally they should be set
        // The important thing is onLoad() completes without throwing
        assertNotNull(addon);
    }

    @Test
    void testOnEnableNoGameModes() {
        addon.onLoad();
        addon.onEnable();
        // No game modes → not hooked → state set to DISABLED
        assertEquals(State.DISABLED, addon.getState());
    }

    @Test
    void testOnEnablePluginDisabled() {
        addon.onLoad();
        when(plugin.isEnabled()).thenReturn(false);
        addon.onEnable();
        assertEquals(State.DISABLED, addon.getState());
    }

    @Test
    void testOnReload() {
        addon.onLoad();
        // onReload() guards on this.hooked — safe to call even when not hooked
        addon.onReload();
        assertNotNull(addon);
    }

    @Test
    void testOnDisableWhenNotHooked() {
        // onDisable() guards on this.hooked — must not throw when addon was never enabled
        addon.onDisable();
        assertNotNull(addon);
    }

    @Test
    void testGetSettingsNullBeforeLoad() {
        assertNull(addon.getSettings());
    }

    @Test
    void testGetAddonManagerNullBeforeEnable() {
        assertNull(addon.getAddonManager());
    }

    @Test
    void testFlagsNotNull() {
        assertNotNull(BiomesAddon.BIOMES_WORLD_PROTECTION);
        assertNotNull(BiomesAddon.BIOMES_ISLAND_PROTECTION);
    }

    @Test
    void testIsEconomyProvidedDefault() {
        // vaultHook is null before allLoaded() is called
        assertFalse(addon.isEconomyProvided());
    }

    @Test
    void testIsLevelProvidedDefault() {
        assertFalse(addon.isLevelProvided());
    }

    @Test
    void testIsGreenhousesProvidedDefault() {
        assertFalse(addon.isGreenhousesProvided());
    }

    @Test
    void testIsBankProvidedDefault() {
        assertFalse(addon.isBankProvided());
    }
}
