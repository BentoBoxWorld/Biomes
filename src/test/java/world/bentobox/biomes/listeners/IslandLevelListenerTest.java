package world.bentobox.biomes.listeners;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.biomes.BiomesAddon;
import world.bentobox.biomes.CommonTestSetup;
import world.bentobox.biomes.managers.BiomesAddonManager;
import world.bentobox.level.events.IslandLevelCalculatedEvent;

/**
 * Tests for {@link IslandLevelListener}.
 */
class IslandLevelListenerTest extends CommonTestSetup {

    @Mock
    private BiomesAddon addon;
    @Mock
    private BiomesAddonManager addonManager;
    @Mock
    private Island testIsland;

    private IslandLevelListener listener;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        when(addon.getAddonManager()).thenReturn(addonManager);
        listener = new IslandLevelListener(addon);
    }

    @Test
    void testOnLevelCalculated() {
        IslandLevelCalculatedEvent event = mock(IslandLevelCalculatedEvent.class);
        when(event.getIsland()).thenReturn(testIsland);
        when(event.getTargetPlayer()).thenReturn(uuid);
        when(event.getLevel()).thenReturn(42L);

        listener.onLevelCalculated(event);

        verify(addonManager).checkBiomesUnlockStatus(eq(testIsland), any(User.class), eq(42L));
    }
}
