package world.bentobox.biomes.listeners;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bukkit.event.player.PlayerJoinEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import world.bentobox.bentobox.api.events.island.IslandCreatedEvent;
import world.bentobox.bentobox.api.events.island.IslandDeleteEvent;
import world.bentobox.bentobox.api.events.island.IslandRegisteredEvent;
import world.bentobox.bentobox.api.events.island.IslandResettedEvent;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.biomes.BiomesAddon;
import world.bentobox.biomes.CommonTestSetup;
import world.bentobox.biomes.managers.BiomesAddonManager;
import world.bentobox.biomes.tasks.UpdateQueue;

/**
 * Tests for {@link JoinLeaveListener}.
 */
class JoinLeaveListenerTest extends CommonTestSetup {

    @Mock
    private BiomesAddon addon;
    @Mock
    private BiomesAddonManager addonManager;
    @Mock
    private UpdateQueue updateQueue;
    @Mock
    private Island testIsland;

    private JoinLeaveListener listener;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        when(addon.getAddonManager()).thenReturn(addonManager);
        when(addon.getUpdateQueue()).thenReturn(updateQueue);
        when(testIsland.getUniqueId()).thenReturn("test-island-id");
        listener = new JoinLeaveListener(addon);
    }

    @Test
    void testOnPlayerJoin() {
        PlayerJoinEvent event = mock(PlayerJoinEvent.class);
        when(event.getPlayer()).thenReturn(mockPlayer);
        listener.onPlayerJoin(event);
        verify(addonManager).loadUserIslands(uuid);
    }

    @Test
    void testOnIslandCreatedEvent() {
        IslandCreatedEvent event = mock(IslandCreatedEvent.class);
        when(event.getIsland()).thenReturn(testIsland);
        listener.onIslandCreated(event);
        verify(addonManager).validateIslandData(testIsland);
    }

    @Test
    void testOnIslandResettedEvent() {
        IslandResettedEvent event = mock(IslandResettedEvent.class);
        when(event.getIsland()).thenReturn(testIsland);
        listener.onIslandCreated(event);
        verify(updateQueue).cancelBiomeUpdates("test-island-id");
        verify(addonManager).validateIslandData(testIsland);
    }

    @Test
    void testOnIslandRegisteredEvent() {
        IslandRegisteredEvent event = mock(IslandRegisteredEvent.class);
        when(event.getIsland()).thenReturn(testIsland);
        listener.onIslandCreated(event);
        verify(addonManager).validateIslandData(testIsland);
    }

    @Test
    void testOnIslandDelete() {
        IslandDeleteEvent event = mock(IslandDeleteEvent.class);
        when(event.getIsland()).thenReturn(testIsland);
        listener.onIslandDelete(event);
        verify(updateQueue).cancelBiomeUpdates("test-island-id");
        verify(addonManager).wipeIslandData("test-island-id");
    }
}
