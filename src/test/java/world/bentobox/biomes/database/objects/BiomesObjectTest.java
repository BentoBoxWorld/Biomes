package world.bentobox.biomes.database.objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.bukkit.World;
import org.bukkit.block.Biome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import world.bentobox.biomes.CommonTestSetup;
import world.bentobox.biomes.database.objects.BiomesObject.CostMode;

/**
 * Tests for {@link BiomesObject}.
 */
class BiomesObjectTest extends CommonTestSetup {

    private BiomesObject obj;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        obj = new BiomesObject();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(obj);
    }

    @Test
    void testGetSetUniqueId() {
        obj.setUniqueId("test-id");
        assertEquals("test-id", obj.getUniqueId());
    }

    @Test
    void testGetSetBiome() {
        obj.setBiome(Biome.PLAINS);
        assertEquals(Biome.PLAINS, obj.getBiome());
    }

    @Test
    void testGetSetDeployedTrue() {
        obj.setDeployed(true);
        assertTrue(obj.isDeployed());
    }

    @Test
    void testGetSetDeployedFalse() {
        obj.setDeployed(false);
        assertFalse(obj.isDeployed());
    }

    @Test
    void testGetSetFriendlyName() {
        obj.setFriendlyName("My Biome");
        assertEquals("My Biome", obj.getFriendlyName());
    }

    @Test
    void testGetSetDescription() {
        List<String> desc = Arrays.asList("line1", "line2");
        obj.setDescription(desc);
        assertEquals(desc, obj.getDescription());
    }

    @Test
    void testGetSetEnvironment() {
        obj.setEnvironment(World.Environment.NORMAL);
        assertEquals(World.Environment.NORMAL, obj.getEnvironment());
    }

    @Test
    void testGetUnlockLevelDefaultZero() {
        assertEquals(0, obj.getUnlockLevel());
    }

    @Test
    void testGetSetUnlockLevel() {
        obj.setUnlockLevel(100L);
        assertEquals(100L, obj.getUnlockLevel());
    }

    @Test
    void testGetCostDefaultZero() {
        assertEquals(0.0, obj.getCost());
    }

    @Test
    void testGetSetCost() {
        obj.setCost(50.0);
        assertEquals(50.0, obj.getCost());
    }

    @Test
    void testGetUnlockCostDefaultZero() {
        assertEquals(0.0, obj.getUnlockCost());
    }

    @Test
    void testGetSetUnlockCost() {
        obj.setUnlockCost(25.0);
        assertEquals(25.0, obj.getUnlockCost());
    }

    @Test
    void testGetCostModeDefault() {
        assertEquals(CostMode.STATIC, obj.getCostMode());
    }

    @Test
    void testGetSetCostMode() {
        obj.setCostMode(CostMode.PER_BLOCK);
        assertEquals(CostMode.PER_BLOCK, obj.getCostMode());
    }

    @Test
    void testGetCostIncrementDefaultZero() {
        assertEquals(0.0, obj.getCostIncrement());
    }

    @Test
    void testGetSetCostIncrement() {
        obj.setCostIncrement(1.5);
        assertEquals(1.5, obj.getCostIncrement());
    }

    @Test
    void testGetUnlockPermissionsDefaultEmpty() {
        assertNotNull(obj.getUnlockPermissions());
        assertTrue(obj.getUnlockPermissions().isEmpty());
    }

    @Test
    void testGetUnlockItemsDefaultEmpty() {
        assertNotNull(obj.getUnlockItems());
        assertTrue(obj.getUnlockItems().isEmpty());
    }

    @Test
    void testGetItemCostDefaultEmpty() {
        assertNotNull(obj.getItemCost());
        assertTrue(obj.getItemCost().isEmpty());
    }

    @Test
    void testGetOrderDefault() {
        assertEquals(-1, obj.getOrder());
    }

    @Test
    void testGetSetOrder() {
        obj.setOrder(5);
        assertEquals(5, obj.getOrder());
    }

    @Test
    void testIsValidWithBiome() {
        obj.setBiome(Biome.PLAINS);
        assertTrue(obj.isValid());
    }

    @Test
    void testIsValidWithoutBiome() {
        assertFalse(obj.isValid());
    }

    @Test
    void testEqualsAndHashCodeSameId() {
        BiomesObject other = new BiomesObject();
        obj.setUniqueId("same-id");
        other.setUniqueId("same-id");
        assertEquals(obj, other);
        assertEquals(obj.hashCode(), other.hashCode());
    }

    @Test
    void testNotEqualsDifferentId() {
        BiomesObject other = new BiomesObject();
        obj.setUniqueId("id-a");
        other.setUniqueId("id-b");
        assertFalse(obj.equals(other));
    }

    @Test
    void testEqualsWithNull() {
        assertFalse(obj.equals(null));
    }

    @Test
    void testEqualsWithDifferentType() {
        assertFalse(obj.equals("not a BiomesObject"));
    }

    @Test
    void testEqualsBothNullIdSameBiome() {
        BiomesObject other = new BiomesObject();
        obj.setBiome(Biome.PLAINS);
        other.setBiome(Biome.PLAINS);
        assertTrue(obj.equals(other));
    }

    @Test
    void testEqualsBothNullIdDifferentBiome() {
        BiomesObject other = new BiomesObject();
        obj.setBiome(Biome.PLAINS);
        other.setBiome(Biome.DESERT);
        assertFalse(obj.equals(other));
    }

    @Test
    void testGetIconDefaultNotNull() {
        // icon defaults to new ItemStack(Material.PAPER), so getIcon() returns a clone
        assertNotNull(obj.getIcon());
    }

    @Test
    void testCompareToByOrder() {
        BiomesObject lower = new BiomesObject();
        lower.setOrder(0);
        lower.setEnvironment(World.Environment.NORMAL);
        lower.setBiome(Biome.PLAINS);
        lower.setFriendlyName("a");

        BiomesObject higher = new BiomesObject();
        higher.setOrder(1);
        higher.setEnvironment(World.Environment.NORMAL);
        higher.setBiome(Biome.PLAINS);
        higher.setFriendlyName("a");

        assertTrue(lower.compareTo(higher) < 0);
        assertTrue(higher.compareTo(lower) > 0);
    }

    @Test
    void testCompareToEqualObjects() {
        BiomesObject a = new BiomesObject();
        a.setOrder(0);
        a.setEnvironment(World.Environment.NORMAL);
        a.setBiome(Biome.PLAINS);
        a.setFriendlyName("same");

        BiomesObject b = new BiomesObject();
        b.setOrder(0);
        b.setEnvironment(World.Environment.NORMAL);
        b.setBiome(Biome.PLAINS);
        b.setFriendlyName("same");

        assertEquals(0, a.compareTo(b));
    }
}
