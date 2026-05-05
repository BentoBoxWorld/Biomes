package world.bentobox.biomes.listeners;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.bukkit.configuration.file.FileConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.events.team.TeamSetownerEvent;
import world.bentobox.biomes.BiomesAddon;
import world.bentobox.biomes.CommonTestSetup;
import world.bentobox.biomes.config.Settings;
import world.bentobox.biomes.database.objects.BiomesIslandDataObject;
import world.bentobox.biomes.managers.BiomesAddonManager;

/**
 * Tests for {@link ChangeOwnerListener}.
 */
class ChangeOwnerListenerTest extends CommonTestSetup {

    @Mock
    private BiomesAddon addon;
    @Mock
    private Settings biomesSettings;
    @Mock
    private GameModeAddon gameModeAddon;
    @Mock
    private TeamSetownerEvent event;
    @Mock
    private BiomesAddonManager addonManager;
    @Mock
    private FileConfiguration gameModeConfig;

    private ChangeOwnerListener listener;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        when(addon.getSettings()).thenReturn(biomesSettings);
        when(addon.getPlugin()).thenReturn(plugin);
        when(event.getIsland()).thenReturn(island);
        when(island.getWorld()).thenReturn(world);
        when(event.getNewOwner()).thenReturn(uuid);
        when(iwm.getAddon(world)).thenReturn(Optional.of(gameModeAddon));
        when(gameModeAddon.getConfig()).thenReturn(gameModeConfig);
        when(gameModeAddon.getPermissionPrefix()).thenReturn("bskyblock.");
        listener = new ChangeOwnerListener(addon);
    }

    @Test
    void testResetBiomesDisabledDoesNothing() {
        when(biomesSettings.isResetBiomes()).thenReturn(false);
        listener.onTeamSetOwnerEvent(event);
        // Early return: getNewOwner() never called
        verify(event, never()).getNewOwner();
    }

    @Test
    void testResetBiomesEnabledNoGameMode() {
        when(biomesSettings.isResetBiomes()).thenReturn(true);
        when(iwm.getAddon(world)).thenReturn(Optional.empty());
        // No game mode → hasPermissions = true → block skipped → no logError
        listener.onTeamSetOwnerEvent(event);
        verify(addon, never()).logError(anyString());
    }

    @Test
    void testResetBiomesEnabledHasPermission() {
        when(biomesSettings.isResetBiomes()).thenReturn(true);
        when(mockPlayer.hasPermission("bskyblock.biomes.set")).thenReturn(true);
        // Has permission → block skipped → no logError
        listener.onTeamSetOwnerEvent(event);
        verify(addon, never()).logError(anyString());
    }

    @Test
    void testResetBiomesEnabledNoPermissionInvalidBiome() {
        when(biomesSettings.isResetBiomes()).thenReturn(true);
        when(mockPlayer.hasPermission("bskyblock.biomes.set")).thenReturn(false);
        when(gameModeConfig.getString("world.default-biome", "PLAINS")).thenReturn("INVALID_BIOME_XYZ");
        // Invalid biome → logError called once
        listener.onTeamSetOwnerEvent(event);
        verify(addon).logError(anyString());
    }

    @Test
    void testResetBiomesEnabledNoPermissionValidBiome() {
        when(biomesSettings.isResetBiomes()).thenReturn(true);
        when(mockPlayer.hasPermission("bskyblock.biomes.set")).thenReturn(false);
        when(gameModeConfig.getString("world.default-biome", "PLAINS")).thenReturn("PLAINS");
        when(addon.getAddonManager()).thenReturn(addonManager);
        when(addonManager.getIslandData(any())).thenReturn(new BiomesIslandDataObject());
        // Valid biome → code reaches getIslandData()
        // (BiomeUpdateHelper is constructed inline so we can only verify this far)
        listener.onTeamSetOwnerEvent(event);
        verify(addonManager).getIslandData(island);
    }
}
